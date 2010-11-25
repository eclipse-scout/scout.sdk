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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;

public class FormDataSourceBuilder extends AbstractSourceBuilder {

  private final IType m_formType;

  public FormDataSourceBuilder(String simpleTypeName, IType formType) {
    super(simpleTypeName, Signature.createTypeSignature(RuntimeClasses.AbstractFormData, true), Flags.AccPublic, null);
    m_formType = formType;
  }

  @Override
  public void build() throws CoreException {
    addBeanPropertiesFrom(m_formType, true);
  }

  public void appendToParent(ISourceBuilder parentBuilder) throws CoreException {
  }

}
