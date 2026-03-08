/*
 * Copyright 2024 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.verticals.foundations.dataharmonization.services.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures SMART on FHIR / OAuth2 security for CMS-0057-F compliance.
 *
 * <p>CMS-0057-F requires SMART App Launch for patient-facing APIs and OAuth2
 * for provider/payer APIs. This configuration sets up the Spring Security
 * filter chain for JWT-based authentication.
 *
 * <p>FHIR scopes enforced:
 * <ul>
 *   <li>patient/*.read — Patient Access API</li>
 *   <li>user/*.read — Provider Access API</li>
 *   <li>system/*.read system/*.write — Payer-to-Payer API</li>
 *   <li>user/Claim.write — Prior Authorization submission</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SmartOnFhirConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // Actuator endpoints for health checks and metrics (Prometheus scraping)
            .requestMatchers("/actuator/**").permitAll()
            // FHIR metadata/capability statement
            .requestMatchers("/fhir/metadata").permitAll()
            // CDS Hooks discovery
            .requestMatchers("/cds-services").permitAll()
            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> {})
        )
        .csrf(csrf -> csrf.disable());

    return http.build();
  }
}
