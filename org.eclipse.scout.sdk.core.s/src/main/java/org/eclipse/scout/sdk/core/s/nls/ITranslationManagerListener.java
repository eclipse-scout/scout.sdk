/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import java.util.EventListener;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent;

/**
 * <h3>{@link ITranslationManagerListener}</h3>
 * <p>
 * Listener interface to be notified when a {@link TranslationManager} changes.
 *
 * @since 7.0.0
 */
@FunctionalInterface
public interface ITranslationManagerListener extends EventListener {
  /**
   * Notification that a {@link TranslationManager} changed. The specified events hold details about each event.
   *
   * @param events
   *          The events that occurred.
   */
  void managerChanged(Stream<TranslationManagerEvent> events);
}
