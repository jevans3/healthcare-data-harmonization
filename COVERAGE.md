# HL7v2 & FHIR Coverage Summary

This document describes which HL7v2 message types, segments, and FHIR resources are supported by the mappings in this repository, and identifies known gaps.

## HL7v2 to FHIR R4 Mappings

### Supported Message Types (10 types, 30 trigger events)

| Message Type | Description | Trigger Events | Segments Covered |
|---|---|---|---|
| **ADT** | Admit/Discharge/Transfer | A01, A02, A03, A04, A05, A06, A07, A08, A09, A11, A12, A13, A14, A17, A18, A28, A31, A47 | MSH, PID, PD1, DG1, PV1, AL1, OBX, EVN, PR1, GT1, IN1, NK1 |
| **ORU** | Observation Result | R01 | MSH, PID, PD1, PV1, OBX, OBR, ORC |
| **ORM** | General Order | O01 | MSH, PID, PD1, PV1, OBX, OBR, ORC, SPM |
| **OML** | Lab Order | O21 | MSH, PID, PD1, PV1, OBX, OBR, ORC, SPM |
| **VXU** | Vaccination Update | V04 | MSH, PID, PD1, PV1, OBX, ORC, NK1, IN1, GT1, RXA, RXR |
| **MDM** | Medical Document Management | T02 | MSH, PID, PV1, TXA, OBX, ORC |
| **SIU** | Scheduling | S12, S13, S14 | MSH, SCH, NTE, PID, PV1, AIG, AIL, AIP, AIS |
| **RDE** | Pharmacy Encoded Order | O01, O11 | MSH, PID, PD1, PV1, ORC, RXE, RXR |
| **RAS** | Pharmacy Administration | O17 | MSH, PID, PD1, PV1, ORC, RXA, RXR, OBX |
| **PPR** | Patient Problem | PC1 | MSH, PID, PV1, PRB, GOL, OBX, ORC |

### Mapped Segments (25)

MSH, PID, PD1, PV1, EVN, NK1, DG1, AL1, OBX, OBR, ORC, PR1, GT1, IN1, SPM, TXA, PRB, GOL, SCH, RXA, RXE, RXR, AIG, AIL, AIP, AIS, NTE

### Mapped Datatypes (30)

CM, CNE, CNN, CQ, CWE, CX, DLD, DLN, DR, EI, FN, HD, ID, MSG, NDL, NK1, NR, NTE, PL, RI, SAD, SN, ST, TQ, VARIES, XAD, XCN, XON, XPN, XTN

### Segment Groups

PATIENT_RESULT, ORDER_OBSERVATION, ORDER, OBSERVATION, PATIENT, PROBLEM, GOAL, PROBLEM_ORDER

### Code Harmonization (26 value set mappings)

Address Type, Address Use, Allergy Category, Allergy Criticality, Allergy Severity, Allergy Type, Appointment Type, Completion Status, Document Completion Status, Encounter Class, Encounter Status, Event Reason, Filler Status, Gender, Interpretation Codes, Marital Status, Name Type, Observation Status, Order Control Codes, Order Status, Order Type, Priority, Relationship, Report Status, Telecom Equipment Type, Telecom Use

### Z-Segments

**Not supported.** There is no built-in handling for Z-segments (site-specific custom segments like ZPD, ZPI, etc.). Users must write their own Whistle mappings to process Z-segments.

---

## FHIR R4 Resources Produced (22)

The HL7v2-to-FHIR mappings produce the following FHIR R4 resources:

Account, AllergyIntolerance, Appointment, Bundle, Condition, Coverage, DiagnosticReport, Encounter, EpisodeOfCare, Goal, Immunization, Location, MedicationAdministration, MedicationRequest, MessageHeader, Observation, Organization, Patient, Practitioner, Procedure, Provenance, RelatedPerson, ServiceRequest, Specimen

---

## FHIR Version Conversion

| Conversion | Resource Count | Direction |
|---|---|---|
| **R3 to R4** | 149 resources (117 fully mapped, 32 stubs) | Bidirectional |
| **R2 to R3** | 114 resources | Bidirectional |
| **R4B / R5** | Not supported | - |

### R3-R4 Stub Resources (32 — new in R4 with no R3 equivalent)

BiologicallyDerivedProduct, CatalogEntry, ChargeItemDefinition, CoverageEligibilityRequest, CoverageEligibilityResponse, DeviceDefinition, EffectEvidenceSynthesis, EventDefinition, Evidence, EvidenceVariable, ExampleScenario, ImmunizationEvaluation, InsurancePlan, Invoice, MedicationKnowledge, MedicinalProduct, MedicinalProductAuthorization, MedicinalProductContraindication, MedicinalProductIndication, MedicinalProductIngredient, MedicinalProductInteraction, MedicinalProductManufactured, MedicinalProductPackaged, MedicinalProductPharmaceutical, MedicinalProductUndesirableEffect, ObservationDefinition, OrganizationAffiliation, ResearchDefinition, ResearchElementDefinition, RiskEvidenceSynthesis, SpecimenDefinition, SubstancePolymer, SubstanceProtein, SubstanceReferenceInformation, SubstanceSourceMaterial, SubstanceSpecification, TerminologyCapabilities, VerificationResult

### Version Conversion Limitations

- **Code value set changes**: Stored as extensions rather than smart value set translation
- **Reference target type changes**: Directly copied without validation
- **Missing required fields**: May produce technically invalid resources
- **Nested/contained resources**: Directly copied without version conversion in some cases
- **Top-level extensions**: Directly copied without field-level conversion
- **Sub-level extensions**: Completely ignored (data loss possible)
- **Modifier extensions**: Not specifically converted

---

## Edge Cases Handled

- Repeating segments (e.g., `NK1[]`, `DG1[]`, `OBX[]`)
- Segment groups (PATIENT_RESULT, ORDER_OBSERVATION, INSURANCE, PROCEDURE)
- Deep nested component/subcomponent access (e.g., `CX.4.3`, `PID.6[*].2[]`)
- OBX multi-valued observations via `component[]` arrays
- PV1-2 "N" (not applicable) skip logic to avoid creating empty Encounter resources
- MSH.6 cardinality change handling (1 vs * between HL7v2 versions)
- Conditional resource creation based on segment presence
- NULL/empty value handling with sensible defaults (e.g., `status: "unknown"`, `active: true`)
- Code harmonization/translation via 26 value set mapping tables

## Edge Cases Not Handled

- Z-segments (custom/site-specific segments)
- HL7v2 escape sequences (`\F\`, `\S\`, `\R\`, `\E\`, `\T\`)
- Non-standard delimiters (assumes `|^~\&`)
- Batch messages (BHS/BTS segment wrappers)
- Continuation messages (DSC segment)
- Multi-byte character encodings beyond UTF-8

---

## Known Gaps

### Missing HL7v2 Message Types

| Message Type | Description |
|---|---|
| BAR | Billing/Account (P01, P02, P05, P06) |
| DFT | Detailed Financial Transaction (P03, P11) |
| MFN/MFQ | Master File Notification/Query |
| ACK | General Acknowledgment |
| QRY/QBP/RSP | Query messages |
| OUL | Unsolicited Lab Observation (R22, R23, R24) |
| OMG | General Clinical Order (O19) |
| OMP | Pharmacy/Treatment Order (O09) |
| RGV | Pharmacy/Treatment Give (O15) |
| RDS | Pharmacy/Treatment Dispense (O13) |

### Missing ADT Trigger Events

A10, A15, A16, A19-A27, A29-A30, A32-A46, A48-A62

### Missing SIU Trigger Events

S15-S26

### Missing HL7v2 Segments

ROL (Role), RXD (Pharmacy Dispense), RXO (Pharmacy Order), RXC (Pharmacy Component), FT1 (Financial Transaction), ACC (Accident), UB1/UB2 (Universal Billing), MRG (Merge - partial in ADT_A18/A47), PDA (Patient Death), DB1 (Disability), RF1 (Referral), AUT (Authorization), PV2 (Patient Visit Additional), IN2/IN3 (Insurance Additional)

### Missing FHIR R4 Resources (not produced from HL7v2)

CarePlan, CareTeam, Claim, ClaimResponse, Consent, Device, DeviceRequest, DocumentReference, ExplanationOfBenefit, FamilyMemberHistory, Flag, HealthcareService, MedicationDispense, MedicationStatement, NutritionOrder, PractitionerRole, QuestionnaireResponse, Schedule, Slot, Task

### Incomplete Terminology Mappings

Several fields use hard-coded defaults or lack full ConceptMap coverage:

- PID.15 (Communication Language)
- PV1.4 (Admission Type)
- PV1.10 (Hospital Service)
- PR1.6 (Procedure Category)
- DG1.6 (Diagnosis Type)
- Observation category defaults to "laboratory"
- Procedure status defaults to "unknown"
- Coverage status defaults to "active"

### Known TODOs in Code

- NTE segment parser has a known investigation issue (`mappings/hl7v2_fhir_r4/mappings/datatypes/NTE.wstl`)

---

## Legacy Whistle v1 Mappings (wstl1/ — deprecated)

The `wstl1/` directory contains additional mapping configurations using the deprecated Whistle v1 language:

- **OMOP to FHIR STU3/R4** — Person, DrugExposure, ConditionOccurrence, ProcedureOccurrence, VisitOccurrence, Provider, Specimen, Observation
- **FHIR to OMOP** — Reverse mapping
- **CDA to FHIR R4** — Clinical Document Architecture conversion
- **CPCDS to FHIR R4** — Claims data (Organization, PractitionerRole, Claims)
- **DICOM to FHIR R4** — Imaging study metadata (ImagingStudy)

These are not actively maintained and are included for backwards compatibility only.
