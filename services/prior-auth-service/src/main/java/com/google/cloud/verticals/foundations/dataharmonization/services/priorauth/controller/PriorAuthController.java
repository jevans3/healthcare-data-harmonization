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

package com.google.cloud.verticals.foundations.dataharmonization.services.priorauth.controller;

import com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantContext;
import com.google.cloud.verticals.foundations.dataharmonization.services.priorauth.service.PriorAuthWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CMS-0057-F Prior Authorization API controller.
 *
 * <p>Provides the Prior Authorization API required by CMS-0057-F final rule:
 * <ul>
 *   <li>POST /api/v1/prior-auth/submit — Submit new prior authorization</li>
 *   <li>GET /api/v1/prior-auth/{id}/status — Check authorization status</li>
 *   <li>GET /api/v1/prior-auth/{id} — Get authorization details</li>
 * </ul>
 *
 * <p>Enforces SLA compliance (72hr expedited, 7-day standard) and tracks
 * authorization lifecycle from submission through expiration.
 */
@RestController
@RequestMapping("/api/v1/prior-auth")
public class PriorAuthController {

  private static final Logger logger = LoggerFactory.getLogger(PriorAuthController.class);
  private final PriorAuthWorkflow workflow;

  public PriorAuthController(PriorAuthWorkflow workflow) {
    this.workflow = workflow;
  }

  /**
   * Submit a new prior authorization request.
   * Delegates to FHIR server's PAS Claim/$submit operation.
   */
  @PostMapping(value = "/submit",
      consumes = "application/fhir+json",
      produces = "application/fhir+json")
  public ResponseEntity<String> submitAuthorization(@RequestBody String claimBundle) {
    logger.info("Prior auth submission: tenant={}", TenantContext.getTenantId());
    // TODO: Forward to FHIR server's PAS Claim/$submit
    // TODO: Record submission timestamp for SLA tracking
    // TODO: Return ClaimResponse with auth number
    return ResponseEntity.accepted().body("{\"status\": \"submitted\"}");
  }

  /**
   * Check the status of an existing prior authorization.
   */
  @GetMapping(value = "/{authId}/status", produces = "application/fhir+json")
  public ResponseEntity<String> getStatus(@PathVariable String authId) {
    logger.info("Prior auth status check: tenant={}, authId={}",
        TenantContext.getTenantId(), authId);
    // TODO: Query authorization status
    // TODO: Return ClaimResponse with current status
    return ResponseEntity.ok("{\"status\": \"pending\"}");
  }

  /**
   * Get full details of a prior authorization.
   */
  @GetMapping(value = "/{authId}", produces = "application/fhir+json")
  public ResponseEntity<String> getAuthorization(@PathVariable String authId) {
    logger.info("Prior auth detail: tenant={}, authId={}",
        TenantContext.getTenantId(), authId);
    // TODO: Return full Claim + ClaimResponse bundle
    return ResponseEntity.ok("{\"resourceType\": \"Bundle\"}");
  }
}
