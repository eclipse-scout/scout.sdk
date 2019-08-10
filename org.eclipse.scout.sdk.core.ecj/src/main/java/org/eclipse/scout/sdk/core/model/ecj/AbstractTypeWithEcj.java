/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

public abstract class AbstractTypeWithEcj extends AbstractMemberWithEcj<IType> implements TypeSpi {

  protected AbstractTypeWithEcj(JavaEnvironmentWithEcj env) {
    super(env);
  }

  protected abstract TypeBinding getInternalBinding();

}
