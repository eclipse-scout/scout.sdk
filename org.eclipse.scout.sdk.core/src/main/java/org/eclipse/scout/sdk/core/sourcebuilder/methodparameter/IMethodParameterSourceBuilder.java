/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder.methodparameter;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.sourcebuilder.IAnnotatableSourceBuilder;

/**
 * <h3>{@link IMethodParameterSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface IMethodParameterSourceBuilder extends IAnnotatableSourceBuilder {

  /**
   * @return {@link Flags}
   */
  int getFlags();

  /**
   * @param flags
   *          {@link Flags}
   */
  void setFlags(int flags);

  String getDataTypeSignature();

  void setDataTypeSignature(String dataTypeSignature);
}
