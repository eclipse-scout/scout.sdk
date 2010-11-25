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
package org.eclipse.scout.sdk.operation.form.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;

public class SimpleFieldDataSourceBuilder extends AbstractSourceBuilder {

  private IType m_uiFieldType;
  // private final ISourceBuilder m_parentSourceBuilder;
  private final boolean m_isImplementation;

  public SimpleFieldDataSourceBuilder(String typeSimpleName, String superTypeSignature, IType uiFieldType, IImportValidator importValidator, boolean isImplementation) {
    super(typeSimpleName, superTypeSignature, Flags.AccPublic, importValidator);
    m_uiFieldType = uiFieldType;
    m_isImplementation = isImplementation;
  }

  public SimpleFieldDataSourceBuilder(String typeSimpleName, String superTypeSignature, IType uiFieldType, ISourceBuilder parentSourceBuilder, boolean isImplementation) {
    this(typeSimpleName, superTypeSignature, uiFieldType, parentSourceBuilder.getImportValidator(), isImplementation);
  }

  @Override
  public void build() throws CoreException {
    addBeanPropertiesFrom(m_uiFieldType, m_isImplementation);
  }

  public void appendToParent(ISourceBuilder parentBuilder) throws CoreException {
    //add type
    parentBuilder.addInnerClass(createDocumentText());
    //add type getter
    parentBuilder.addFieldGetterMethod("public " + getSimpleTypeName() + " get" + getSimpleTypeName() + "(){\n" + ScoutIdeProperties.TAB + "return getFieldByClass(" + getSimpleTypeName() + ".class);\n}");
  }

}
