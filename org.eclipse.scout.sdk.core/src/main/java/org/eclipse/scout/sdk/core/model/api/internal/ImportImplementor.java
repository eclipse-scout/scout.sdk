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
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.spi.ImportSpi;

/**
 *
 */
public class ImportImplementor extends AbstractJavaElementImplementor<ImportSpi>implements IImport {

  public ImportImplementor(ImportSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return m_spi.getName();
  }

  @Override
  public String simpleName() {
    return m_spi.getSimpleName();
  }

  @Override
  public String qualifier() {
    return m_spi.getQualifier();
  }

  @Override
  public ICompilationUnit compilationUnit() {
    return m_spi.getCompilationUnit().wrap();
  }

  @Override
  public boolean isStatic() {
    return m_spi.isStatic();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }
}
