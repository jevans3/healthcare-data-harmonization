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

package com.google.cloud.verticals.foundations.dataharmonization.services.fhir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * FHIR R4 server implementing Da Vinci Burden Reduction IGs 2.1.
 *
 * <p>Operations provided:
 * <ul>
 *   <li><b>PAS</b>: Claim/$submit, Claim/$inquire (Prior Authorization Support)</li>
 *   <li><b>DTR</b>: $questionnaire-package, $next-question (Documentation Templates and Rules)</li>
 *   <li><b>CRD</b>: CDS Hooks at /cds-services (Coverage Requirements Discovery)</li>
 *   <li><b>CDex</b>: Task CRUD, $submit-attachment (Clinical Data Exchange)</li>
 *   <li><b>ValueSet</b>: $expand for terminology support</li>
 * </ul>
 *
 * <p>Built on HAPI FHIR JPA Server with multi-tenant partition interceptor.
 * Validates resources against Da Vinci and US Core profiles.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.google.cloud.verticals.foundations.dataharmonization.services.fhir",
    "com.google.cloud.verticals.foundations.dataharmonization.services.common"
})
public class FhirServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(FhirServerApplication.class, args);
  }
}
