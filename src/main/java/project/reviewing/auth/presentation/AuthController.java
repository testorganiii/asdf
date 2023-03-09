package project.reviewing.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.reviewing.auth.application.AuthService;
import project.reviewing.auth.application.response.LoginResponse;
import project.reviewing.auth.application.response.RefreshResponse;
import project.reviewing.auth.infrastructure.TokenProvider;
import project.reviewing.common.util.CookieBuilder;
import project.reviewing.common.util.CookieType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.net.URI;

@Validated
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;

    @PostMapping(value = "/login/github")
    ResponseEntity<?> githubLogin(@RequestBody @NotBlank final String authorizationCode,
                                  final HttpServletResponse response) {
        LoginResponse loginResponse = authService.githubLogin(authorizationCode);

        addTokenPairCookie(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        return loginResponse.isCreated() ?
                ResponseEntity.created(URI.create("/members/" + loginResponse.getMemberId())).build()
                : ResponseEntity.ok().build();
    }

    @PostMapping(value = "/refresh")
    ResponseEntity<?> refreshTokens(final HttpServletRequest request, final HttpServletResponse response) {
        RefreshResponse refreshResponse = authService.refreshTokens((Long) request.getAttribute("id"));

        addTokenPairCookie(response, refreshResponse.getAccessToken(), refreshResponse.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/logout")
    ResponseEntity<?> logout(final HttpServletRequest request, final HttpServletResponse response) {
        authService.removeRefreshToken((long) (int) request.getAttribute("id"));

        response.addCookie(CookieBuilder.makeRemovedCookie(CookieType.ACCESS_TOKEN, "removed"));
        response.addCookie(CookieBuilder.makeRemovedCookie(CookieType.REFRESH_TOKEN, "removed"));
        return ResponseEntity.noContent().build();
    }

    private void addTokenPairCookie(
            final HttpServletResponse response, final String accessToken, final String refreshToken
    ) {
        response.addCookie(CookieBuilder.builder(CookieType.ACCESS_TOKEN, accessToken)
                .maxAge((int) tokenProvider.getAccessTokenValidTime())
                .path("/")
                .httpOnly(true)
                .build()
        );
        response.addCookie(CookieBuilder.builder(CookieType.REFRESH_TOKEN, refreshToken)
                .maxAge((int) tokenProvider.getRefreshTokenValidTime())
                .path("/auth/refresh")
                .httpOnly(true)
                .build()
        );
    }
}
