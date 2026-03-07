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

import java.nio.file.Path;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registry that resolves Whistle mapping file paths for tenants and mapping types.
 *
 * <p>Supports tenant-specific mapping overrides. If a tenant has custom mappings,
 * those are used; otherwise, the default mappings from the repository are used.
 *
 * <p>Mapping types:
 * <ul>
 *   <li>hl7v2_fhir — HL7v2 to FHIR R4 transformation</li>
 *   <li>fhir_r3_r4 — FHIR R3 to R4 conversion</li>
 *   <li>pas_claim — Da Vinci PAS Claim validation/mapping</li>
 *   <li>pas_response — PAS ClaimResponse construction</li>
 *   <li>crd_rules — CRD coverage requirement rules</li>
 *   <li>dtr_questionnaire — DTR questionnaire pre-population</li>
 * </ul>
 */
@Component
public class MappingRegistry {

  @Value("${mappings.base-path:mappings}")
  private String basePath;

  @Value("${mappings.tenant-override-path:mappings/tenants}")
  private String tenantOverridePath;

  private static final Map<String, String> DEFAULT_MAPPING_PATHS = Map.of(
      "hl7v2_fhir", "hl7v2_fhir_r4/mappings/hl7v2_fhir.wstl",
      "fhir_r3_r4", "fhirVersionConversion/r3r4/fhir_r3r4.wstl",
      "pas_claim", "davinci/pas/pas_claim_validation.wstl",
      "pas_response", "davinci/pas/pas_claim_response.wstl",
      "pas_x12", "davinci/pas/pas_x12_mapping.wstl",
      "crd_rules", "davinci/crd/crd_coverage_rules.wstl",
      "dtr_questionnaire", "davinci/dtr/dtr_questionnaire_prepop.wstl"
  );

  /**
   * Resolves the mapping file path for a given tenant and mapping type.
   * Checks for tenant-specific override first, falls back to default.
   */
  public String getMappingPath(String tenantId, String mappingType) {
    // Check for tenant-specific override
    Path tenantMapping = Path.of(tenantOverridePath, tenantId, mappingType + ".wstl");
    if (tenantMapping.toFile().exists()) {
      return tenantMapping.toString();
    }

    // Fall back to default mapping
    String defaultPath = DEFAULT_MAPPING_PATHS.get(mappingType);
    if (defaultPath == null) {
      throw new IllegalArgumentException("Unknown mapping type: " + mappingType);
    }
    return Path.of(basePath, defaultPath).toString();
  }
}
