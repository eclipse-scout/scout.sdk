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

/**
 * <h3>{@link ITranslationEntry}</h3>
 * <p>
 * Represents an {@link ITranslation} that is linked to an owner {@link ITranslationStore}.
 *
 * @since 7.0.0
 */
public interface ITranslationEntry extends ITranslation {

  /**
   * @return The {@link ITranslationStore} this {@link ITranslationEntry} belongs to.
   */
  ITranslationStore store();

}
