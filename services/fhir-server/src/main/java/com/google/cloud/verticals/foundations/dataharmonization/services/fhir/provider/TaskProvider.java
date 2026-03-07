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
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FHIR Task provider implementing Da Vinci CDex v2.1 operations.
 *
 * <h3>Task-based Clinical Data Exchange</h3>
 * <p>Supports the CDex workflow where payers request clinical data from providers:
 * <ol>
 *   <li>Payer creates Task (profile: cdex-task-data-request) specifying needed data</li>
 *   <li>Provider queries EHR for requested data</li>
 *   <li>Provider populates Task.output with clinical documents</li>
 *   <li>Task status progresses: requested → accepted → in-progress → completed</li>
 * </ol>
 *
 * <h3>$submit-attachment</h3>
 * <p>Provider submits completed documentation:
 * <ul>
 *   <li>DocumentReference — clinical documents (e.g., progress notes, lab results)</li>
 *   <li>QuestionnaireResponse — completed DTR questionnaires</li>
 *   <li>Binary — raw attachments</li>
 * </ul>
 * <p>Attachments are linked to the originating Claim or prior authorization.
 */
@Component
public class TaskProvider {

  private static final Logger logger = LoggerFactory.getLogger(TaskProvider.class);
  private final MetricsConfig.FhirOperationMetrics metrics;

  public TaskProvider(MetricsConfig.FhirOperationMetrics metrics) {
    this.metrics = metrics;
  }

  /**
   * Creates a CDex data request Task.
   *
   * @param task the Task resource with data request details
   * @return the created Task with server-assigned ID
   */
  public Task createDataRequest(Task task) {
    Timer.Sample sample = metrics.startTimer();
    logger.info("CDex Task create: tenant={}, status={}",
        TenantContext.getTenantId(), task.getStatus());

    try {
      // TODO: Validate against cdex-task-data-request profile
      // TODO: Persist task
      // TODO: Notify provider system

      metrics.recordDuration(sample, "cdex_task_create");
      return task;
    } catch (Exception e) {
      metrics.recordDuration(sample, "cdex_task_create");
      throw e;
    }
  }

  /**
   * CDex $submit-attachment operation.
   *
   * @param attachmentBundle Bundle containing DocumentReference/QuestionnaireResponse/Binary
   * @return operation outcome
   */
  public Bundle submitAttachment(Bundle attachmentBundle) {
    Timer.Sample sample = metrics.startTimer();
    logger.info("CDex $submit-attachment: tenant={}, entryCount={}",
        TenantContext.getTenantId(), attachmentBundle.getEntry().size());

    try {
      // TODO: Link attachments to originating Claim/prior auth
      // TODO: Update Task status if all requested data submitted
      // TODO: Validate attachment profiles

      metrics.recordDuration(sample, "cdex_submit_attachment");
      return new Bundle();
    } catch (Exception e) {
      metrics.recordDuration(sample, "cdex_submit_attachment");
      throw e;
    }
  }
}
