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

package com.google.cloud.verticals.foundations.dataharmonization.services.fhir.cds;

import com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantContext;
import com.google.cloud.verticals.foundations.dataharmonization.services.common.engine.WhistleTransformService;
import com.google.cloud.verticals.foundations.dataharmonization.services.common.observability.MetricsConfig;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CDS Hooks endpoint implementing Da Vinci CRD v2.1.
 *
 * <p>Evaluates coverage requirements when EHR triggers hooks during clinical workflows.
 * Returns CDS cards indicating documentation or prior auth requirements.
 *
 * <h3>Supported hooks:</h3>
 * <ul>
 *   <li><b>order-select</b> — triggered when a clinician selects an order</li>
 *   <li><b>order-sign</b> — triggered when orders are signed</li>
 *   <li><b>appointment-book</b> — triggered when scheduling an appointment</li>
 *   <li><b>encounter-start</b> — triggered at encounter admission</li>
 * </ul>
 *
 * <h3>CDS Card types returned:</h3>
 * <ul>
 *   <li>"Documentation required" — link to DTR questionnaire</li>
 *   <li>"Prior authorization required" — link to PAS submission</li>
 *   <li>"Info only" — informational coverage guidance</li>
 * </ul>
 *
 * <p>Coverage rules are evaluated using Whistle mappings in mappings/davinci/crd/.
 * Rules are tenant-specific (payer-specific coverage policies).
 */
@RestController
@RequestMapping("/cds-services")
public class CrdHookEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(CrdHookEndpoint.class);
  private final WhistleTransformService transformService;
  private final MetricsConfig.FhirOperationMetrics metrics;

  public CrdHookEndpoint(WhistleTransformService transformService,
      MetricsConfig.FhirOperationMetrics metrics) {
    this.transformService = transformService;
    this.metrics = metrics;
  }

  /**
   * CDS Hooks discovery endpoint — returns available services.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> discovery() {
    Map<String, Object> response = Map.of(
        "services", List.of(
            Map.of(
                "hook", "order-select",
                "title", "Coverage Requirements Discovery",
                "description", "Evaluate coverage requirements for selected orders",
                "id", "crd-order-select",
                "prefetch", Map.of(
                    "patient", "Patient/{{context.patientId}}",
                    "coverage", "Coverage?patient={{context.patientId}}&status=active"
                )
            ),
            Map.of(
                "hook", "order-sign",
                "title", "Coverage Requirements Discovery - Order Sign",
                "description", "Evaluate coverage requirements when orders are signed",
                "id", "crd-order-sign",
                "prefetch", Map.of(
                    "patient", "Patient/{{context.patientId}}",
                    "coverage", "Coverage?patient={{context.patientId}}&status=active"
                )
            ),
            Map.of(
                "hook", "appointment-book",
                "title", "Coverage Requirements Discovery - Appointment",
                "description", "Evaluate coverage requirements for appointments",
                "id", "crd-appointment-book"
            ),
            Map.of(
                "hook", "encounter-start",
                "title", "Coverage Requirements Discovery - Encounter",
                "description", "Evaluate coverage requirements at encounter start",
                "id", "crd-encounter-start"
            )
        )
    );
    return ResponseEntity.ok(response);
  }

  /**
   * Processes a CDS Hook request and returns coverage requirement cards.
   *
   * @param hookRequest the CDS Hooks request payload
   * @return CDS cards with coverage requirements
   */
  @PostMapping(value = "/{hookId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> processHook(@RequestBody String hookRequest) {
    Timer.Sample sample = metrics.startTimer();
    String tenantId = TenantContext.getTenantId();
    logger.info("CRD hook: tenant={}", tenantId);

    try {
      // Evaluate coverage rules using Whistle mappings
      String cardsJson = transformService.transform(hookRequest, "crd_rules");
      metrics.recordDuration(sample, "crd_hook");
      return ResponseEntity.ok(cardsJson);
    } catch (Exception e) {
      metrics.recordDuration(sample, "crd_hook");
      logger.error("CRD hook failed: tenant={}", tenantId, e);
      // Return empty cards on error (CDS Hooks spec: don't block clinical workflow)
      return ResponseEntity.ok("{\"cards\": []}");
    }
  }
}
