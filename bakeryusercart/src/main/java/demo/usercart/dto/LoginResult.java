package demo.usercart.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginResult(
        String accessToken,
        String username,

        @JsonIgnore
        String refreshToken,

        @JsonIgnore
        Instant refreshTokenExpiresAt
) {
}
