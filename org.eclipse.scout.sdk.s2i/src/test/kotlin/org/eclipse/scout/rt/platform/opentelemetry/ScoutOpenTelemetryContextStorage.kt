/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry

import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.ContextStorageProvider

/**
 * Replaces the class with same fqn from the Scout runtime which would start a Scout Platform during testing (because it tries to look up a config property).
 * Scout platform startup is not possible (duplicate classpath entries) and not required for the test (performance).
 */
open class ScoutOpenTelemetryContextStorage : ContextStorageProvider {
    override fun get(): ContextStorage = ContextStorage.defaultStorage()
}
