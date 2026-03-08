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

import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Maps X12 278 denial reason codes to FHIR ClaimResponse adjudication codes.
 *
 * <p>CMS-0057-F requires payers to provide denial reasons in both X12 and FHIR formats.
 * This mapper translates X12 278 Health Care Services Review denial/modification codes
 * to FHIR R4 ClaimResponse.item.adjudication entries.
 *
 * <p>Common X12 278 denial categories:
 * <ul>
 *   <li>A1-A6: Certification action codes (approved, modified, denied, pended)</li>
 *   <li>CT: Category codes for service types</li>
 *   <li>HCR: Health care services review reason codes</li>
 * </ul>
 *
 * <p>For full mapping, see code harmonization files:
 * {@code mappings/hl7v2_fhir_r4/mappings/code_harmonization/X12_Denial_Reason.harmonization.json}
 */
@Service
public class X12DenialCodeMapper {

  // X12 278 certification action codes → FHIR ClaimResponse outcome
  private static final Map<String, String> ACTION_TO_OUTCOME = Map.of(
      "A1", "complete",       // Certified in total
      "A2", "partial",        // Certified - modified
      "A3", "error",          // Not certified
      "A4", "queued",         // Pended
      "A6", "complete",       // Modified
      "CT", "complete"        // Contact payer
  );

  // X12 HCR reason codes → human-readable denial reasons
  private static final Map<String, String> REASON_DESCRIPTIONS = Map.ofEntries(
      Map.entry("01", "Not medically necessary"),
      Map.entry("02", "Experimental/investigational"),
      Map.entry("03", "Member not eligible"),
      Map.entry("04", "Pre-existing condition"),
      Map.entry("05", "Service not covered by plan"),
      Map.entry("06", "Duplicate request"),
      Map.entry("07", "Service already provided"),
      Map.entry("08", "Insufficient information"),
      Map.entry("09", "Requires additional documentation"),
      Map.entry("10", "Out of network"),
      Map.entry("11", "Benefit maximum reached"),
      Map.entry("12", "Authorization expired"),
      Map.entry("13", "Requires peer-to-peer review"),
      Map.entry("14", "Alternative service recommended")
  );

  /**
   * Maps X12 action code to FHIR ClaimResponse outcome.
   */
  public String mapActionToOutcome(String x12ActionCode) {
    return ACTION_TO_OUTCOME.getOrDefault(x12ActionCode, "error");
  }

  /**
   * Gets human-readable description for X12 reason code.
   */
  public String getReasonDescription(String x12ReasonCode) {
    return REASON_DESCRIPTIONS.getOrDefault(x12ReasonCode, "Unspecified reason: " + x12ReasonCode);
  }
}
