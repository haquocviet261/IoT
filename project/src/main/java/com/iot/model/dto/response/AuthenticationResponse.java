package com.iot.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuthenticationResponse {
   @JsonProperty("access_token")
   private String accessToken;
   @JsonProperty("refresh_token")
   private String refresh_token;
}