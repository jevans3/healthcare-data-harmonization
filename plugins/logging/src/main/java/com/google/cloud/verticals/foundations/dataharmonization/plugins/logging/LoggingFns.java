/*
 * Copyright 2020 Google LLC.
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

package com.google.cloud.verticals.foundations.dataharmonization.plugins.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions for logging.
 *
 * <p>Uses SLF4J for structured logging integration with Sumo Logic (via Fluent Bit sidecars)
 * and Grafana (via log-derived metrics). MDC context from TenantInterceptor automatically
 * includes tenantId and correlationId in all log entries.
 */
public class LoggingFns {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPlugin.PACKAGE_NAME);

  /**
   * Logs the provided message to info logs.
   *
   * @param message The message to log.
   */
  public static void logInfo(String message) {
    LOGGER.info(message);
  }

  /**
   * Logs the provided message to warning logs.
   *
   * @param message The message to log.
   */
  public static void logWarning(String message) {
    LOGGER.warn(message);
  }

  /**
   * Logs the provided message to severe logs.
   *
   * @param message The message to log.
   */
  public static void logSevere(String message) {
    LOGGER.error(message);
  }
}
