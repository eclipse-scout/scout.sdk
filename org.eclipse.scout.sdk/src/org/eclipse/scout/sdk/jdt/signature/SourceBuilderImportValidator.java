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
package org.eclipse.scout.sdk.jdt.signature;

import java.util.HashSet;

import org.eclipse.scout.sdk.operation.form.formdata.ISourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.ITypeSourceBuilder;

public class SourceBuilderImportValidator extends SimpleImportValidator {
  private final ITypeSourceBuilder m_sourceBuilder;
  private HashSet<String> m_innerTypeNames;

  public SourceBuilderImportValidator(ITypeSourceBuilder sourceBuilder, String packageName) {
    super(packageName);
    m_sourceBuilder = sourceBuilder;
    m_innerTypeNames = new HashSet<String>();
    for (ISourceBuilder childBuilder : m_sourceBuilder.getSourceBuilders(ISourceBuilder.TYPE_SOURCE_BUILDER)) {
      m_innerTypeNames.add(((ITypeSourceBuilder) childBuilder).getElementName());
    }
  }

  @Override
  protected boolean isAlreadyUsed(String packageName, String simpleName) {
    if (m_innerTypeNames.contains(simpleName)) {
      return true;
    }
    return super.isAlreadyUsed(packageName, simpleName);
  }
}
