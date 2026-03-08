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

package com.google.cloud.verticals.foundations.dataharmonization.services.transform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot application for HL7v2 → FHIR R4 transformation service.
 *
 * <p>Exposes REST APIs for data transformation:
 * <ul>
 *   <li>POST /api/v1/transform/hl7v2 — Transform HL7v2 message to FHIR R4 Bundle</li>
 *   <li>POST /api/v1/convert/fhir — Convert between FHIR versions (R3↔R4)</li>
 * </ul>
 *
 * <p>Multi-tenant: uses X-Tenant-ID header for tenant-specific mapping selection.
 * Metrics exposed at /actuator/prometheus for Grafana dashboards.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.google.cloud.verticals.foundations.dataharmonization.services.transform",
    "com.google.cloud.verticals.foundations.dataharmonization.services.common"
})
public class TransformApplication {

  public static void main(String[] args) {
    SpringApplication.run(TransformApplication.class, args);
  }
}
