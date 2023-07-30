package gr.aegean.controller;

import gr.aegean.model.analysis.AnalysisReportDTO;
import gr.aegean.model.analysis.AnalysisRequest;
import gr.aegean.model.analysis.AnalysisResult;
import gr.aegean.service.analysis.AnalysisService;
import gr.aegean.service.analysis.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis")
public class AnalysisController {
    private final ProjectService projectService;
    private final AnalysisService analysisService;

    /*
         Have a message saying that if in the analysis report they don't see a repository from those they
         provided, it wasn't a valid GitHub repository URL, or it was a private one, or the language was not
         supported.
     */
    @PostMapping
    public ResponseEntity<Void> analyze(@Valid @RequestBody AnalysisRequest analysisRequest,
                                        HttpServletRequest httpServletRequest,
                                        UriComponentsBuilder uriBuilder) {
        Integer analysisId = projectService.processProject(analysisRequest, httpServletRequest).join();
        URI location = uriBuilder
                .path("/api/v1/analysis/{analysisId}")
                .buildAndExpand(analysisId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /*
        Returns a list of analysis reports for all the repositories submitted.
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResult> getAnalysisResult(@PathVariable Integer analysisId) {
        AnalysisResult result = projectService.findAnalysisResultByAnalysisId(analysisId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /*
        Returns the analysis report for a specific repository.
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<AnalysisReportDTO> getAnalysisReport(@PathVariable Integer reportId) {
        AnalysisReportDTO report = analysisService.findAnalysisReportById(reportId);

        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
