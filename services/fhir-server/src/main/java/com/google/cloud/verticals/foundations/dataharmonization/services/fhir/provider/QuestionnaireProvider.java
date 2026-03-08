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
import com.google.cloud.verticals.foundations.dataharmonization.services.common.observability.MetricsConfig;
import io.micrometer.core.instrument.Timer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FHIR Questionnaire provider implementing Da Vinci DTR v2.1 operations.
 *
 * <h3>$questionnaire-package</h3>
 * <p>Returns a Bundle containing:
 * <ul>
 *   <li>Questionnaire — the form template for documentation requirements</li>
 *   <li>Library — CQL logic for pre-populating answers from patient data</li>
 *   <li>ValueSet — code lists for answer options</li>
 * </ul>
 * <p>Templates are stored per payer/tenant. CQL evaluation uses patient
 * data from the EHR to pre-fill known answers.
 *
 * <h3>$next-question (Adaptive Questionnaire)</h3>
 * <p>Evaluates the current QuestionnaireResponse and determines the next
 * question based on CQL logic. Returns an updated QuestionnaireResponse
 * with the next unanswered item.
 */
@Component
public class QuestionnaireProvider {

  private static final Logger logger = LoggerFactory.getLogger(QuestionnaireProvider.class);
  private final MetricsConfig.FhirOperationMetrics metrics;

  public QuestionnaireProvider(MetricsConfig.FhirOperationMetrics metrics) {
    this.metrics = metrics;
  }

  /**
   * DTR $questionnaire-package operation.
   *
   * @param parameters input parameters containing coverage and order references
   * @return Bundle with Questionnaire + Library + ValueSet resources
   */
  public Bundle questionnairePackage(Parameters parameters) {
    Timer.Sample sample = metrics.startTimer();
    String tenantId = TenantContext.getTenantId();
    logger.info("DTR $questionnaire-package: tenant={}", tenantId);

    try {
      // TODO: Look up questionnaire template for the coverage/service combination
      // TODO: Include CQL Library for pre-population logic
      // TODO: Include referenced ValueSets for answer options

      Bundle packageBundle = new Bundle();
      packageBundle.setType(Bundle.BundleType.COLLECTION);

      metrics.recordDuration(sample, "dtr_questionnaire_package");
      return packageBundle;
    } catch (Exception e) {
      metrics.recordDuration(sample, "dtr_questionnaire_package");
      logger.error("DTR $questionnaire-package failed: tenant={}", tenantId, e);
      throw e;
    }
  }

  /**
   * DTR $next-question operation for adaptive questionnaires.
   *
   * @param questionnaireResponse current response with answered items
   * @return updated QuestionnaireResponse with next question
   */
  public QuestionnaireResponse nextQuestion(QuestionnaireResponse questionnaireResponse) {
    Timer.Sample sample = metrics.startTimer();
    logger.info("DTR $next-question: tenant={}", TenantContext.getTenantId());

    try {
      // TODO: Evaluate CQL to determine next question based on current answers
      // TODO: Return updated QuestionnaireResponse with next item

      metrics.recordDuration(sample, "dtr_next_question");
      return questionnaireResponse;
    } catch (Exception e) {
      metrics.recordDuration(sample, "dtr_next_question");
      throw e;
    }
  }
}
