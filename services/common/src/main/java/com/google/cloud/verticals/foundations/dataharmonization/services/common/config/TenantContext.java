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

/**
 * Thread-local holder for multi-tenant context.
 *
 * <p>Stores the current tenant ID for the executing request thread. Used by
 * {@link TenantRoutingDataSource} for schema-per-tenant routing and by
 * observability components for tenant-aware logging and metrics.
 *
 * <p>Pattern follows ePA multi-tenancy where each tenant (payer/provider org)
 * has isolated data schemas while sharing compute infrastructure.
 */
public final class TenantContext {

  private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
  private static final String DEFAULT_TENANT = "default";

  private TenantContext() {}

  public static void setTenantId(String tenantId) {
    CURRENT_TENANT.set(tenantId);
  }

  public static String getTenantId() {
    String tenant = CURRENT_TENANT.get();
    return tenant != null ? tenant : DEFAULT_TENANT;
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
