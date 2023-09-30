package gr.aegean.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import gr.aegean.config.security.JwtConfig;
import gr.aegean.config.security.SecurityConfig;
import gr.aegean.config.security.AuthConfig;
import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.model.dto.auth.RegisterRequest;
import gr.aegean.model.dto.auth.LoginRequest;
import gr.aegean.service.auth.AuthService;
import gr.aegean.service.auth.PasswordResetService;
import gr.aegean.repository.UserRepository;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        AuthConfig.class,
        JwtConfig.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private PasswordResetService passwordResetService;
    @MockBean
    private UserRepository userRepository;
    private static final String AUTH_PATH = "/api/v1/auth";

    @Test
    void shouldReturnJwtTokenAndHTTP201WhenUserIsRegisteredSuccessfully() throws Exception {
        String requestBody = """
                {
                    "firstname": "Test",
                    "lastname": "Test",
                    "username": "Test",
                    "email": "test@example.com",
                    "password": "Igw4UQAlfX$E"
                }
                """;
        String responseBody = """
                {
                    "token": "jwtToken"
                }
                """;

        AuthResponse authResponse = new AuthResponse("jwtToken");
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterFirstnameIsNullOrEmpty(String firstname) throws Exception {
        String firstnameValue = firstname == null ? "null" : "\"" + firstname + "\"";
        String requestBody = String.format("""
            {
                "firstname": %s,
                "lastname": "Test",
                "username": "Test",
                "email": "test@example.com",
                "password": "Igw4UQAlfX$E"
            }
            """, firstnameValue);
        String responseBody = """
            {
                "message": "The First Name field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterLastnameIsNullOrEmpty(String lastname) throws Exception {
        String lastnameValue = lastname == null ? "null" : "\"" + lastname + "\"";
        String requestBody = String.format("""
            {
                "firstname": "Test",
                "lastname": %s,
                "username": "Test",
                "email": "test@example.com",
                "password": "Igw4UQAlfX$E"
            }
            """, lastnameValue);
        String responseBody = """
            {
                "message": "The Last Name field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterUsernameIsNullOrEmpty(String username) throws Exception {
        String usernameValue = username == null ? "null" : "\"" + username + "\"";
        String requestBody = String.format("""
            {
                "firstname": "Test",
                "lastname": "Test",
                "username": %s,
                "email": "test@example.com",
                "password": "Igw4UQAlfX$E"
            }
            """, usernameValue);
        String responseBody = """
            {
                "message": "The Username field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterEmailIsNullOrEmpty(String email) throws Exception {
        String emailValue = email == null ? "null" : "\"" + email + "\"";
        String requestBody = String.format("""
            {
                "firstname": "Test",
                "lastname": "Test",
                "username": "TestT",
                "email": %s,
                "password": "Igw4UQAlfX$E"
            }
            """, emailValue);

        String responseBody = """
            {
                "message": "The Email field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterPasswordIsNullOrEmpty(String password) throws Exception {
        String passwordValue = password == null ? "null" : "\"" + password + "\"";
        String requestBody = String.format("""
            {
                "firstname": "Test",
                "lastname": "Test",
                "username": "TestT",
                "email": "test@example.com",
                "password": %s
            }
            """, passwordValue);
        String responseBody = """
            {
                "message": "The Password field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @Test
    void shouldReturnJwtTokenAndHTTP200WhenUserIsLoggedInSuccessfully() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Igw4UQAlfX$E"
                }
                """;
        String responseBody = """
                {
                    "token": "jwtToken"
                }
                """;

        AuthResponse authResponse = new AuthResponse("jwtToken");
        when(authService.loginUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenLoginEmailIsNullOrEmpty(String email) throws Exception {
        String emailValue = email == null ? "null" : "\"" + email + "\"";
        String requestBody = String.format("""
            {
                "email": %s,
                "password": "Igw4UQAlfX$E"
            }
            """, emailValue);
        String responseBody = """
            {
                "message": "The Email field is necessary",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenLoginPasswordIsNullOrEmpty(String password) throws Exception {
        String passwordValue = password == null ? "null" : "\"" + password + "\"";
        String requestBody = String.format("""
            {
                "email": "test@example.com",
                "password": %s
            }
            """, passwordValue);
        String responseBody = """
            {
                "message": "The Password field is necessary",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }


    @Test
    void shouldReturnHTTP202ForPasswordResetRequestRegardlessIfEmailExists() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com"
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/password_reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenPasswordResetEmailIsNullOrEmpty(String email) throws Exception {
        String emailValue = email == null ? "null" : "\"" + email + "\"";
        String requestBody = String.format("""
            {
                "email": %s
            }
            """, emailValue);
        String responseBody = """
            {
                "message": "The Email field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(post(AUTH_PATH + "/password_reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenPasswordResetConfirmationTokenIsNullOrEmpty(String token) throws Exception {
        String tokenValue = token == null ? "null" : "\"" + token + "\"";
        String requestBody = String.format("""
            {
                "token": %s,
                "password": "somePassword"
            }
            """, tokenValue);
        String responseBody = """
            {
                "message": "No token provided",
                "statusCode": 400
            }
            """;

        mockMvc.perform(put(AUTH_PATH + "/password_reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenWhenPasswordResetConfirmationPasswordIsNullOrEmpty(String newPassword) throws Exception {
        String newPasswordValue = newPassword == null ? "null" : "\"" + newPassword + "\"";
        String requestBody = String.format("""
            {
                "token": "someToken",
                "password": %s
            }
            """, newPasswordValue);
        String responseBody = """
            {
                "message": "The Password field is required",
                "statusCode": 400
            }
            """;

        mockMvc.perform(put(AUTH_PATH + "/password_reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseBody));
    }

    @Test
    void shouldReturnHTTP204WhenTokenAndNewPasswordAreValid() throws Exception {
        String requestBody = """
                {
                    "token": "someToken",
                    "password": "password"
                }
                """;

        mockMvc.perform(put(AUTH_PATH + "/password_reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
