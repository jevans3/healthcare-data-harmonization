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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for FHIR version conversions.
 *
 * <p>Supports bidirectional conversion between FHIR versions using the existing
 * fhirVersionConversion mappings (R2↔R3: 114 resources, R3↔R4: 149 resources).
 */
@RestController
@RequestMapping("/api/v1/convert")
public class FhirConvertController {

  private static final Logger logger = LoggerFactory.getLogger(FhirConvertController.class);
  private final WhistleTransformService transformService;

  public FhirConvertController(WhistleTransformService transformService) {
    this.transformService = transformService;
  }

  /**
   * Converts a FHIR resource between versions.
   *
   * @param fhirJson the FHIR resource as JSON
   * @param conversion the conversion type (e.g., "r3_r4", "r4_r3", "r2_r3", "r3_r2")
   * @return the converted FHIR resource as JSON
   */
  @PostMapping(value = "/fhir",
      consumes = "application/fhir+json",
      produces = "application/fhir+json")
  public ResponseEntity<String> convertFhir(
      @RequestBody String fhirJson,
      @RequestParam(defaultValue = "r3_r4") String conversion) {
    try {
      String mappingType = "fhir_" + conversion;
      String result = transformService.transform(fhirJson, mappingType);
      return ResponseEntity.ok(result);
    } catch (WhistleTransformService.TransformException e) {
      logger.error("FHIR conversion failed: conversion={}", conversion, e);
      return ResponseEntity.unprocessableEntity().body(
          "{\"error\": \"Conversion failed\", \"detail\": \"" + e.getMessage() + "\"}");
    }
  }
}
