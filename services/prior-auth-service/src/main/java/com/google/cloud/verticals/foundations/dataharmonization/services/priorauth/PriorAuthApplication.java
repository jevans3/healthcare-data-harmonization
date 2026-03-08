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

package com.google.cloud.verticals.foundations.dataharmonization.services.priorauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CMS-0057-F Prior Authorization compliance service.
 *
 * <p>Wraps the FHIR server's PAS operations with CMS-specific compliance:
 * <ul>
 *   <li>SLA tracking: 72-hour expedited, 7-day standard response times</li>
 *   <li>X12 278 denial reason code mapping</li>
 *   <li>Authorization lifecycle management (approved → modified → expired)</li>
 *   <li>Patient Access API for prior auth history</li>
 *   <li>Provider Access API for submission and status</li>
 *   <li>Payer-to-Payer API for member transitions</li>
 * </ul>
 *
 * <p>SLA violations trigger PagerDuty P1 alerts via Grafana alert rules.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
    "com.google.cloud.verticals.foundations.dataharmonization.services.priorauth",
    "com.google.cloud.verticals.foundations.dataharmonization.services.common"
})
public class PriorAuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(PriorAuthApplication.class, args);
  }
}
