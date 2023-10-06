package gr.aegean.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gr.aegean.model.dto.user.UserDTO;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock
    private JwtEncoder jwtEncoder;
    private JwtService underTest;

    @BeforeEach
    void setup() {
        underTest = new JwtService(jwtEncoder);
    }

    @Test
    void shouldAssignToken() {
        //Arrange
        UserDTO userDTO = new UserDTO(
                1,
                "Test",
                "Test",
                "TestT",
                "test@example.com",
                "I have a real passion for teaching",
                "Cleveland, OH",
                "Code Monkey, LLC");

        String jwtToken = "jwtToken";
        Instant now = Instant.now();
        long expiresIn = 2;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(userDTO.email())
                .build();

        Map<String, Object> headers = Map.of(
                "alg", "RS256",
                "typ", "JWT");

        Jwt jwt = new Jwt(
                jwtToken,
                claims.getIssuedAt(),
                claims.getExpiresAt(),
                headers,
                claims.getClaims());

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        //Act
        String actual = underTest.assignToken(userDTO);

        //Assert
        assertThat(actual).isEqualTo(jwt.getTokenValue());
    }
}