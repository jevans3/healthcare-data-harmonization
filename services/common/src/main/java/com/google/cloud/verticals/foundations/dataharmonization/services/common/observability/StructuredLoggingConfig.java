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

package com.google.cloud.verticals.foundations.dataharmonization.services.common.observability;

import org.springframework.context.annotation.Configuration;

/**
 * Configures structured JSON logging for Sumo Logic ingestion via Fluent Bit sidecars.
 *
 * <p>Logging is configured via logback-spring.xml in each service's resources.
 * This class provides programmatic configuration hooks if needed.
 *
 * <p>Log pipeline:
 * <ol>
 *   <li>Application → SLF4J + Logback → JSON structured logs to /var/log/app/</li>
 *   <li>Fluent Bit sidecar tails /var/log/app/*.json</li>
 *   <li>Fluent Bit forwards to Sumo Logic HTTP source endpoint</li>
 *   <li>Sumo Logic indexes with tenant/service/correlation metadata</li>
 * </ol>
 *
 * <p>MDC fields automatically included in every log entry:
 * <ul>
 *   <li>tenantId — from {@link com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantInterceptor}</li>
 *   <li>correlationId — request correlation for distributed tracing</li>
 *   <li>messageType — HL7v2 message type being processed (e.g., ADT_A01)</li>
 *   <li>fhirResourceType — FHIR resource type being generated</li>
 * </ul>
 */
@Configuration
public class StructuredLoggingConfig {
  // Logging configuration is primarily in logback-spring.xml
  // This class exists for any programmatic logging setup needs
}
