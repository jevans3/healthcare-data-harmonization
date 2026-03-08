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

package com.google.cloud.verticals.foundations.dataharmonization.services.priorauth.service;

import com.google.cloud.verticals.foundations.dataharmonization.services.common.observability.MetricsConfig;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * CMS-0057-F Prior Authorization SLA tracking workflow.
 *
 * <p>Monitors pending prior authorization requests against CMS-mandated timelines:
 * <ul>
 *   <li><b>Expedited (urgent)</b>: 72-hour response requirement</li>
 *   <li><b>Standard</b>: 7-day (calendar) response requirement</li>
 * </ul>
 *
 * <p>When an SLA approaches breach (90% of allowed time elapsed), a P1 alert
 * is triggered via PagerDuty (through Grafana alert rules monitoring the
 * {@code prior_auth.sla.violations} metric).
 *
 * <p>Timeline per CMS-0057-F:
 * <ul>
 *   <li>Jan 2026: Payers must be operationally ready</li>
 *   <li>Jan 2027: Full API compliance required</li>
 * </ul>
 */
@Service
public class PriorAuthWorkflow {

  private static final Logger logger = LoggerFactory.getLogger(PriorAuthWorkflow.class);
  private static final Duration EXPEDITED_SLA = Duration.ofHours(72);
  private static final Duration STANDARD_SLA = Duration.ofDays(7);
  private static final double SLA_WARNING_THRESHOLD = 0.9;

  private final MetricsConfig.PriorAuthMetrics metrics;

  public PriorAuthWorkflow(MetricsConfig.PriorAuthMetrics metrics) {
    this.metrics = metrics;
  }

  /**
   * Checks for approaching SLA breaches every 15 minutes.
   * Increments the sla.violations metric when breaches occur,
   * which triggers PagerDuty alerts via Grafana.
   */
  @Scheduled(fixedRate = 900000) // 15 minutes
  public void checkSlaCompliance() {
    logger.debug("Running SLA compliance check");
    // TODO: Query database for pending authorizations
    // TODO: Check elapsed time against SLA limits
    // TODO: Record metrics for approaching/breached SLAs
  }

  /**
   * Determines the SLA deadline for a prior authorization request.
   *
   * @param submittedAt when the request was submitted
   * @param isExpedited whether this is an expedited (urgent) request
   * @return the deadline instant
   */
  public Instant getSlaDeadline(Instant submittedAt, boolean isExpedited) {
    Duration sla = isExpedited ? EXPEDITED_SLA : STANDARD_SLA;
    return submittedAt.plus(sla);
  }

  /**
   * Checks if an SLA is approaching breach.
   *
   * @param submittedAt when the request was submitted
   * @param isExpedited whether this is an expedited request
   * @return true if more than 90% of the SLA duration has elapsed
   */
  public boolean isApproachingBreach(Instant submittedAt, boolean isExpedited) {
    Duration sla = isExpedited ? EXPEDITED_SLA : STANDARD_SLA;
    Duration elapsed = Duration.between(submittedAt, Instant.now());
    return elapsed.toMillis() > (sla.toMillis() * SLA_WARNING_THRESHOLD);
  }

  /**
   * Records an SLA violation for metrics/alerting.
   *
   * @param tenantId the tenant
   * @param priority "expedited" or "standard"
   */
  public void recordViolation(String tenantId, String priority) {
    logger.error("SLA VIOLATION: tenant={}, priority={}", tenantId, priority);
    metrics.recordSlaViolation(priority);
  }
}
