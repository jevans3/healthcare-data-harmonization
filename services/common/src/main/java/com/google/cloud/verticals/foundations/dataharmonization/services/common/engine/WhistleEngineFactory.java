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
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and caches Whistle Engine instances per tenant.
 *
 * <p>Each tenant may have different mapping configurations (e.g., payer-specific
 * harmonization codes, custom value set translations). The factory caches Engine
 * instances to avoid re-transpiling Whistle mappings on every request.
 *
 * <p>Thread-safe: uses ConcurrentHashMap for engine cache, and each Engine
 * instance uses ThreadLocal RuntimeContext for per-request isolation.
 */
@Component
public class WhistleEngineFactory {

  private static final Logger logger = LoggerFactory.getLogger(WhistleEngineFactory.class);
  private final ConcurrentHashMap<String, Engine> engineCache = new ConcurrentHashMap<>();
  private final MappingRegistry mappingRegistry;

  public WhistleEngineFactory(MappingRegistry mappingRegistry) {
    this.mappingRegistry = mappingRegistry;
  }

  /**
   * Gets or creates a Whistle Engine instance for the given tenant and mapping type.
   *
   * @param tenantId the tenant identifier
   * @param mappingType the mapping type (e.g., "hl7v2_fhir", "pas_claim", "crd_rules")
   * @return a cached or newly created Engine instance
   */
  public Engine getEngine(String tenantId, String mappingType) {
    String cacheKey = tenantId + ":" + mappingType;
    return engineCache.computeIfAbsent(cacheKey, key -> {
      logger.info("Creating Whistle engine for tenant={}, mapping={}", tenantId, mappingType);
      return createEngine(tenantId, mappingType);
    });
  }

  /**
   * Invalidates cached engines for a tenant (e.g., when mappings are updated).
   */
  public void invalidateTenant(String tenantId) {
    engineCache.entrySet().removeIf(entry -> entry.getKey().startsWith(tenantId + ":"));
    logger.info("Invalidated engine cache for tenant={}", tenantId);
  }

  private Engine createEngine(String tenantId, String mappingType) {
    String mappingPath = mappingRegistry.getMappingPath(tenantId, mappingType);
    try {
      return new Engine.Builder(mappingPath).build();
    } catch (Exception e) {
      logger.error("Failed to create engine for tenant={}, mapping={}", tenantId, mappingType, e);
      throw new RuntimeException("Engine initialization failed for " + tenantId, e);
    }
  }
}
