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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures multi-tenant data source routing and HTTP interceptors.
 *
 * <p>Each tenant defined in application.yml gets its own DataSource pointing
 * to a tenant-specific PostgreSQL schema. The {@link TenantRoutingDataSource}
 * selects the correct DataSource based on the current {@link TenantContext}.
 *
 * <p>Configuration example in application.yml:
 * <pre>
 * multitenancy:
 *   tenants:
 *     - id: payer-alpha
 *       url: jdbc:postgresql://db:5432/harmonization?currentSchema=payer_alpha
 *       username: app_user
 *       password: ${PAYER_ALPHA_DB_PASSWORD}
 *     - id: payer-beta
 *       url: jdbc:postgresql://db:5432/harmonization?currentSchema=payer_beta
 *       username: app_user
 *       password: ${PAYER_BETA_DB_PASSWORD}
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "multitenancy")
public class MultiTenantConfig implements WebMvcConfigurer {

  private List<TenantDataSourceProperties> tenants;
  private final TenantInterceptor tenantInterceptor;

  public MultiTenantConfig(TenantInterceptor tenantInterceptor) {
    this.tenantInterceptor = tenantInterceptor;
  }

  public void setTenants(List<TenantDataSourceProperties> tenants) {
    this.tenants = tenants;
  }

  public List<TenantDataSourceProperties> getTenants() {
    return tenants;
  }

  @Bean
  public DataSource dataSource() {
    TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();
    Map<Object, Object> targetDataSources = new HashMap<>();

    if (tenants != null) {
      for (TenantDataSourceProperties tenant : tenants) {
        DataSource ds = DataSourceBuilder.create()
            .url(tenant.getUrl())
            .username(tenant.getUsername())
            .password(tenant.getPassword())
            .build();
        targetDataSources.put(tenant.getId(), ds);
      }
    }

    // Default tenant data source
    if (!targetDataSources.containsKey("default")) {
      DataSource defaultDs = DataSourceBuilder.create()
          .url("jdbc:postgresql://localhost:5432/harmonization?currentSchema=public")
          .username("app_user")
          .password("")
          .build();
      targetDataSources.put("default", defaultDs);
    }

    routingDataSource.setTargetDataSources(targetDataSources);
    routingDataSource.setDefaultTargetDataSource(targetDataSources.get("default"));
    routingDataSource.afterPropertiesSet();
    return routingDataSource;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(tenantInterceptor);
  }

  public static class TenantDataSourceProperties {
    private String id;
    private String url;
    private String username;
    private String password;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
  }
}
