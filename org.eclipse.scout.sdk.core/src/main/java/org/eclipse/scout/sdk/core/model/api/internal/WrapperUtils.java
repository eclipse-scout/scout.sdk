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
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link WrapperUtils}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public final class WrapperUtils {

  private WrapperUtils() {
  }

  static ICompilationUnit wrapCompilationUnit(CompilationUnitSpi spi) {
    return spi != null ? spi.wrap() : null;
  }

  static IType wrapType(TypeSpi spi) {
    return spi != null ? spi.wrap() : null;
  }

}
