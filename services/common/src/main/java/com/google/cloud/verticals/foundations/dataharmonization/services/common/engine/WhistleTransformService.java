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

package com.google.cloud.verticals.foundations.dataharmonization.services.common.engine;

import com.google.cloud.verticals.foundations.dataharmonization.init.Engine;
import com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantContext;
import com.google.cloud.verticals.foundations.dataharmonization.services.common.observability.MetricsConfig;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * High-level service that wraps the Whistle Engine for tenant-aware transformations.
 *
 * <p>Provides the primary transformation API used by all Spring Boot services:
 * <ul>
 *   <li>transform-service: HL7v2 → FHIR R4 conversions</li>
 *   <li>fhir-server: Da Vinci PAS/DTR/CRD mapping evaluations</li>
 *   <li>prior-auth-service: X12 278 ↔ FHIR Claim translations</li>
 * </ul>
 *
 * <p>Each call is:
 * <ul>
 *   <li>Tenant-scoped via {@link TenantContext}</li>
 *   <li>Instrumented with Micrometer metrics (for Prometheus/Grafana)</li>
 *   <li>Logged with structured MDC context (for Sumo Logic)</li>
 * </ul>
 */
@Service
public class WhistleTransformService {

  private static final Logger logger = LoggerFactory.getLogger(WhistleTransformService.class);
  private final WhistleEngineFactory engineFactory;
  private final MetricsConfig.TransformMetrics metrics;

  public WhistleTransformService(WhistleEngineFactory engineFactory,
      MetricsConfig.TransformMetrics metrics) {
    this.engineFactory = engineFactory;
    this.metrics = metrics;
  }

  /**
   * Transforms input data using the specified mapping type for the current tenant.
   *
   * @param inputJson JSON string of the input data (e.g., parsed HL7v2 message)
   * @param mappingType the mapping type to use (e.g., "hl7v2_fhir", "pas_claim")
   * @return JSON string of the transformed output (e.g., FHIR Bundle)
   */
  public String transform(String inputJson, String mappingType) {
    String tenantId = TenantContext.getTenantId();
    MDC.put("mappingType", mappingType);

    Timer.Sample sample = metrics.startTimer();
    try {
      Engine engine = engineFactory.getEngine(tenantId, mappingType);
      String result = engine.transform(inputJson);

      metrics.requestCounter(mappingType, "success").increment();
      metrics.recordDuration(sample, mappingType);
      logger.info("Transform completed: tenant={}, mapping={}", tenantId, mappingType);
      return result;

    } catch (Exception e) {
      metrics.requestCounter(mappingType, "error").increment();
      metrics.recordDuration(sample, mappingType);
      logger.error("Transform failed: tenant={}, mapping={}", tenantId, mappingType, e);
      throw new TransformException("Transformation failed for mapping: " + mappingType, e);

    } finally {
      MDC.remove("mappingType");
    }
  }

  public static class TransformException extends RuntimeException {
    public TransformException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
