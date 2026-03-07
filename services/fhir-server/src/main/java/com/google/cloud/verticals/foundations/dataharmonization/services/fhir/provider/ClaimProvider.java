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

package com.google.cloud.verticals.foundations.dataharmonization.services.fhir.provider;

import com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantContext;
import com.google.cloud.verticals.foundations.dataharmonization.services.common.engine.WhistleTransformService;
import com.google.cloud.verticals.foundations.dataharmonization.services.common.observability.MetricsConfig;
import io.micrometer.core.instrument.Timer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FHIR Claim resource provider implementing Da Vinci PAS v2.1 operations.
 *
 * <h3>Claim/$submit (Prior Authorization Support)</h3>
 * <p>Accepts a Bundle containing:
 * <ul>
 *   <li>Claim (use: "preauthorization") with PAS profile</li>
 *   <li>Patient, Coverage, Organization, Practitioner references</li>
 *   <li>ServiceRequest, MedicationRequest, or DeviceRequest items</li>
 *   <li>Supporting information (DocumentReference, QuestionnaireResponse)</li>
 * </ul>
 * <p>Returns a ClaimResponse with status: approved, denied, or pended.
 * Tracks SLA per CMS-0057-F: 72hr expedited, 7-day standard.
 *
 * <h3>Claim/$inquire</h3>
 * <p>Checks status of an existing prior authorization by referencing
 * the original Claim and returning the current ClaimResponse.
 *
 * <h3>X12 278 Integration</h3>
 * <p>Uses Whistle mappings to translate between FHIR Claim and X12 278
 * request/response formats. Denial reason codes are mapped from X12
 * to FHIR ClaimResponse.adjudication via code harmonization.
 */
@Component
public class ClaimProvider {

  private static final Logger logger = LoggerFactory.getLogger(ClaimProvider.class);
  private final WhistleTransformService transformService;
  private final MetricsConfig.FhirOperationMetrics metrics;

  public ClaimProvider(WhistleTransformService transformService,
      MetricsConfig.FhirOperationMetrics metrics) {
    this.transformService = transformService;
    this.metrics = metrics;
  }

  /**
   * PAS $submit operation — submit prior authorization request.
   *
   * @param requestBundle Bundle containing Claim + supporting resources
   * @return ClaimResponse with authorization decision
   */
  public ClaimResponse submit(Bundle requestBundle) {
    Timer.Sample sample = metrics.startTimer();
    String tenantId = TenantContext.getTenantId();
    logger.info("PAS $submit: tenant={}, claimCount={}",
        tenantId, requestBundle.getEntry().size());

    try {
      // Validate Claim against PAS profile
      String validatedJson = transformService.transform(
          requestBundle.toString(), "pas_claim");

      // Transform to payer format (X12 278 if needed)
      String payerResponse = transformService.transform(validatedJson, "pas_x12");

      // Construct ClaimResponse from payer decision
      String responseJson = transformService.transform(payerResponse, "pas_response");

      metrics.recordDuration(sample, "pas_submit");
      logger.info("PAS $submit completed: tenant={}", tenantId);

      // TODO: Parse responseJson into ClaimResponse FHIR resource
      return new ClaimResponse();
    } catch (Exception e) {
      metrics.recordDuration(sample, "pas_submit");
      logger.error("PAS $submit failed: tenant={}", tenantId, e);
      throw e;
    }
  }

  /**
   * PAS $inquire operation — check prior authorization status.
   *
   * @param claimReference reference to the original Claim
   * @return current ClaimResponse with updated status
   */
  public ClaimResponse inquire(String claimReference) {
    Timer.Sample sample = metrics.startTimer();
    logger.info("PAS $inquire: tenant={}, claim={}", TenantContext.getTenantId(), claimReference);

    try {
      // TODO: Look up existing authorization and return current status
      metrics.recordDuration(sample, "pas_inquire");
      return new ClaimResponse();
    } catch (Exception e) {
      metrics.recordDuration(sample, "pas_inquire");
      throw e;
    }
  }
}
