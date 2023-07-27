package gr.aegean.service.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import gr.aegean.entity.Analysis;
import gr.aegean.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gr.aegean.exception.ServerErrorException;
import gr.aegean.model.analysis.AnalysisRequest;
import gr.aegean.entity.AnalysisReport;


@Service
public class ProjectService {
    private final GitHubService gitHubService;
    private final AnalysisService analysisService;
    private final AuthService authService;
    private final Executor taskExecutor;
    private final File baseDirectory;
    private static final String SERVER_ERROR_MSG = "The server encountered an internal error and was unable to " +
            "complete your request. Please try again later.";

    public ProjectService(GitHubService gitHubService,
                          AnalysisService analysisService,
                          AuthService authService,
                          /*
                                The default one and the one we configured, so we have to use @Qualifier
                           */
                          @Qualifier("taskExecutor") Executor taskExecutor,
                          @Value("${projects.base-directory}") String baseDirectoryPath) {
        this.gitHubService = gitHubService;
        this.analysisService = analysisService;
        this.authService = authService;
        this.taskExecutor = taskExecutor;
        baseDirectory = new File(baseDirectoryPath);
    }

    public CompletableFuture<Integer> processProject(AnalysisRequest analysisRequest,
                                                     HttpServletRequest httpServletRequest) {
        File requestFolder = new File(baseDirectory + "\\" + UUID.randomUUID());

        if (!requestFolder.mkdir()) {
            throw new ServerErrorException(SERVER_ERROR_MSG);
        }

        List<CompletableFuture<Optional<AnalysisReport>>> futures = analysisRequest.projectUrls().stream()
                .map(projectUrl -> cloneAndAnalyzeProjectAsync(requestFolder, projectUrl))
                .toList();

        /*
            Wait for all the threads to finish the collect the results remove empty optionals and return the analysis
            report. Empty optionals would have null values in the list.
         */
        CompletableFuture<List<AnalysisReport>> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList());

        return allFutures.thenCompose(reports -> {
            /*
                The user submitted repositories that either were all private, only unsupported languages were
                detected or a combination of both.
             */
            if (reports.isEmpty()) {
                throw new IllegalArgumentException("We could not run the analysis. Please ensure that at least one " +
                        "repository is public and uses a supported language.");
            }

            Integer userId = authService.getIdFromSubject(httpServletRequest);
            Integer analysisId = saveAnalysisProcess(userId, reports, analysisRequest);
            /*
                Delete the folder after all the threads are done being processed.
             */
            deleteProjectDirectory(requestFolder);

            return CompletableFuture.completedFuture(analysisId);
        });
    }

    private Optional<Path> cloneProject(File requestFolder, String projectUrl) {
        if (!gitHubService.isValidGitHubUrl(projectUrl)) {
            return Optional.empty();
        }

        /*
            We need two unique ids, 1 for the folder inside Projects and 1 for each repository we download.
            F:\Projects\UUID1\UUID2, F:\Projects\UUID1\UUID3. Each request will have a unique subfolder in the
            Projects folder that will contain all the repositories for that request.
        */
        File projectFile = new File(requestFolder, UUID.randomUUID().toString());
        try (Git git = gitHubService.cloneRepository(projectUrl, projectFile)) {
            /*
                Exception will be thrown when repository is private.
             */
        } catch (GitAPIException gae) {
            return Optional.empty();
        }

        return Optional.of(projectFile.toPath());
    }

    private CompletableFuture<Optional<AnalysisReport>> cloneAndAnalyzeProjectAsync(File requestFolder,
                                                                                    String projectUrl) {
        return CompletableFuture.supplyAsync(() -> cloneProject(requestFolder, projectUrl)
                .flatMap(projectPath -> analysisService.analyzeProject(projectPath, projectUrl)), taskExecutor);
    }

    private void deleteProjectDirectory(File requestFolder) {
        try {
            FileUtils.deleteDirectory(requestFolder);
        } catch (IOException e) {
            throw new ServerErrorException(SERVER_ERROR_MSG);
        }
    }

    private Integer saveAnalysisProcess(Integer userId, List<AnalysisReport> reports, AnalysisRequest analysisRequest) {
        Integer analysisId = saveAnalysis(userId);
        saveQualityMetricDetails(analysisId, analysisRequest);
        saveAllAnalysisReports(analysisId, reports);

        return analysisId;
    }

    private Integer saveAnalysis(Integer userId) {
        return analysisService.saveAnalysis(new Analysis(userId, LocalDateTime.now()));
    }

    private void saveQualityMetricDetails(Integer analysisId, AnalysisRequest analysisRequest) {
        analysisService.saveQualityMetricDetails(analysisId, analysisRequest.qualityMetricDetails());
    }

    private void saveAllAnalysisReports(Integer analysisId, List<AnalysisReport> reports) {
        reports.forEach(report -> {
            report.setAnalysisId(analysisId);
            analysisService.saveAnalysisReport(report);
        });
    }
}