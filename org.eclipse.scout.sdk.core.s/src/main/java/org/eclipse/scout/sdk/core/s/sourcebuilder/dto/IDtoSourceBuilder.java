/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;

/**
 * <h3>{@link IDtoSourceBuilder}</h3>
 *
 * @author Ivan Motsch
 */
public interface IDtoSourceBuilder extends ISourceBuilder {
  /**
   * @return the target {@link IJavaEnvironment}
   */
  IJavaEnvironment getJavaEnvironment();
}