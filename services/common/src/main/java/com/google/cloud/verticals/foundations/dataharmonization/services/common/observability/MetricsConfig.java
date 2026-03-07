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

import com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Micrometer metrics for Prometheus scraping.
 *
 * <p>Exposes metrics at /actuator/prometheus for Prometheus to scrape via PodMonitor CRDs
 * in EKS. Grafana dashboards visualize these metrics with per-tenant breakdown.
 *
 * <p>Key metrics:
 * <ul>
 *   <li>transform.requests.total — counter of transformation requests by tenant/type/status</li>
 *   <li>transform.duration.seconds — timer of transformation execution time</li>
 *   <li>fhir.operation.duration.seconds — timer of FHIR operation execution time</li>
 *   <li>prior_auth.sla.violations — counter of SLA breaches per CMS-0057-F</li>
 *   <li>prior_auth.pending — gauge of pending authorizations</li>
 * </ul>
 *
 * <p>PagerDuty alerting is configured in Grafana alert rules, not in application code.
 */
@Configuration
public class MetricsConfig {

  @Bean
  public TransformMetrics transformMetrics(MeterRegistry registry) {
    return new TransformMetrics(registry);
  }

  @Bean
  public FhirOperationMetrics fhirOperationMetrics(MeterRegistry registry) {
    return new FhirOperationMetrics(registry);
  }

  @Bean
  public PriorAuthMetrics priorAuthMetrics(MeterRegistry registry) {
    return new PriorAuthMetrics(registry);
  }

  /**
   * Metrics for HL7v2/FHIR transformation operations.
   * Tags include tenant for multi-tenant Grafana dashboards.
   */
  public static class TransformMetrics {
    private final MeterRegistry registry;

    public TransformMetrics(MeterRegistry registry) {
      this.registry = registry;
    }

    public Counter requestCounter(String messageType, String status) {
      return Counter.builder("transform.requests.total")
          .tag("tenant", TenantContext.getTenantId())
          .tag("message_type", messageType)
          .tag("status", status)
          .description("Total transformation requests")
          .register(registry);
    }

    public Timer.Sample startTimer() {
      return Timer.start(registry);
    }

    public void recordDuration(Timer.Sample sample, String messageType) {
      sample.stop(Timer.builder("transform.duration.seconds")
          .tag("tenant", TenantContext.getTenantId())
          .tag("message_type", messageType)
          .description("Transformation execution time")
          .register(registry));
    }
  }

  /** Metrics for FHIR server operations (Da Vinci PAS, DTR, CRD, CDex). */
  public static class FhirOperationMetrics {
    private final MeterRegistry registry;

    public FhirOperationMetrics(MeterRegistry registry) {
      this.registry = registry;
    }

    public Timer.Sample startTimer() {
      return Timer.start(registry);
    }

    public void recordDuration(Timer.Sample sample, String operation) {
      sample.stop(Timer.builder("fhir.operation.duration.seconds")
          .tag("tenant", TenantContext.getTenantId())
          .tag("operation", operation)
          .description("FHIR operation execution time")
          .register(registry));
    }
  }

  /** Metrics for CMS-0057-F prior authorization SLA tracking and PagerDuty alerting. */
  public static class PriorAuthMetrics {
    private final MeterRegistry registry;

    public PriorAuthMetrics(MeterRegistry registry) {
      this.registry = registry;
    }

    public void recordSlaViolation(String priority) {
      Counter.builder("prior_auth.sla.violations")
          .tag("tenant", TenantContext.getTenantId())
          .tag("priority", priority)
          .description("Prior authorization SLA violations")
          .register(registry)
          .increment();
    }

    public void setPendingCount(String tenant, double count) {
      registry.gauge("prior_auth.pending", io.micrometer.core.instrument.Tags.of(
          "tenant", tenant), count);
    }
  }
}
