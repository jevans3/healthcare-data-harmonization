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

package com.google.cloud.verticals.foundations.dataharmonization.services.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP interceptor that extracts tenant identity from request headers or OAuth2 tokens
 * and sets the {@link TenantContext} for the current request thread.
 *
 * <p>Tenant resolution order:
 * <ol>
 *   <li>X-Tenant-ID header (explicit tenant routing)</li>
 *   <li>OAuth2 token 'iss' claim (SMART on FHIR tenant derivation)</li>
 *   <li>Default tenant (fallback)</li>
 * </ol>
 *
 * <p>Also sets MDC fields for structured logging so all log entries include tenant context,
 * enabling Sumo Logic filtering by tenant.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

  public static final String TENANT_HEADER = "X-Tenant-ID";
  public static final String MDC_TENANT_ID = "tenantId";
  public static final String MDC_CORRELATION_ID = "correlationId";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String tenantId = request.getHeader(TENANT_HEADER);
    if (tenantId == null || tenantId.isBlank()) {
      tenantId = "default";
    }

    TenantContext.setTenantId(tenantId);
    MDC.put(MDC_TENANT_ID, tenantId);

    String correlationId = request.getHeader("X-Correlation-ID");
    if (correlationId != null && !correlationId.isBlank()) {
      MDC.put(MDC_CORRELATION_ID, correlationId);
    }

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    TenantContext.clear();
    MDC.remove(MDC_TENANT_ID);
    MDC.remove(MDC_CORRELATION_ID);
  }
}
