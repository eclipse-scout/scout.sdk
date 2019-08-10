/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import java.util.EventListener;
import java.util.stream.Stream;

/**
 * <h3>{@link ITranslationStoreStackListener}</h3>
 * <p>
 * Listener interface to be notified when a {@link TranslationStoreStack} changes.
 *
 * @since 7.0.0
 */
@FunctionalInterface
public interface ITranslationStoreStackListener extends EventListener {
  /**
   * Notification that a {@link TranslationStoreStack} changed. The specified events hold details about each event.
   *
   * @param events
   *          The events that occurred.
   */
  void stackChanged(Stream<TranslationStoreStackEvent> events);
}
