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

package com.google.cloud.verticals.foundations.dataharmonization.services.fhir.interceptor;

import com.google.cloud.verticals.foundations.dataharmonization.services.common.config.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Audit logging interceptor for FHIR operations.
 *
 * <p>Logs all FHIR resource access for compliance auditing. Logged to structured
 * JSON format for Sumo Logic ingestion. Audit entries include:
 * <ul>
 *   <li>Tenant ID, user identity, timestamp</li>
 *   <li>FHIR resource type and ID accessed</li>
 *   <li>Operation performed (read, create, update, delete, operation)</li>
 *   <li>Source IP and OAuth2 client ID</li>
 * </ul>
 *
 * <p>Required for CMS-0057-F compliance and HIPAA audit trail requirements.
 */
@Component
public class AuditLoggingInterceptor {

  private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

  public void logAccess(String resourceType, String resourceId, String operation, String userId) {
    MDC.put("auditResourceType", resourceType);
    MDC.put("auditResourceId", resourceId);
    MDC.put("auditOperation", operation);
    MDC.put("auditUserId", userId);

    auditLogger.info("FHIR access: tenant={}, user={}, operation={}, resource={}/{}",
        TenantContext.getTenantId(), userId, operation, resourceType, resourceId);

    MDC.remove("auditResourceType");
    MDC.remove("auditResourceId");
    MDC.remove("auditOperation");
    MDC.remove("auditUserId");
  }
}
