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

package com.google.cloud.verticals.foundations.dataharmonization.services.transform.controller;

import com.google.cloud.verticals.foundations.dataharmonization.services.common.engine.WhistleTransformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for HL7v2 → FHIR R4 transformations.
 *
 * <p>Accepts parsed HL7v2 messages as JSON and returns FHIR R4 Bundles.
 * Supports all implemented message types: ADT, ORU, ORM, OML, VXU, MDM,
 * SIU, RDE, RAS, PPR, plus gap-closure types (DFT, BAR, RDS, RGV, OMP, etc.).
 *
 * <p>Tenant-aware: uses X-Tenant-ID header (set by TenantInterceptor) to
 * select tenant-specific mapping configurations and harmonization codes.
 */
@RestController
@RequestMapping("/api/v1/transform")
public class Hl7v2TransformController {

  private static final Logger logger = LoggerFactory.getLogger(Hl7v2TransformController.class);
  private final WhistleTransformService transformService;

  public Hl7v2TransformController(WhistleTransformService transformService) {
    this.transformService = transformService;
  }

  /**
   * Transforms an HL7v2 message (as JSON) to a FHIR R4 Bundle.
   *
   * @param hl7v2Json the parsed HL7v2 message in JSON format
   * @return FHIR R4 Bundle as JSON
   */
  @PostMapping(value = "/hl7v2",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = "application/fhir+json")
  public ResponseEntity<String> transformHl7v2(@RequestBody String hl7v2Json) {
    MDC.put("messageType", "hl7v2");
    try {
      String fhirBundle = transformService.transform(hl7v2Json, "hl7v2_fhir");
      return ResponseEntity.ok(fhirBundle);
    } catch (WhistleTransformService.TransformException e) {
      logger.error("HL7v2 transformation failed", e);
      return ResponseEntity.unprocessableEntity().body(
          "{\"error\": \"Transformation failed\", \"detail\": \"" + e.getMessage() + "\"}");
    } finally {
      MDC.remove("messageType");
    }
  }
}
