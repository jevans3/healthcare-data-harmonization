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

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routes database connections to tenant-specific schemas using {@link TenantContext}.
 *
 * <p>Implements the schema-per-tenant isolation model (ePA pattern) where each tenant
 * has its own PostgreSQL schema within a shared database cluster. This provides strong
 * data isolation while keeping operational complexity manageable.
 *
 * <p>In EKS, this combines with namespace-per-tenant Kubernetes isolation for a hybrid
 * multi-tenancy model: schema isolation for data, namespace isolation for compute.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return TenantContext.getTenantId();
  }
}
