/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
