/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.swt.widgets.Composite;

public class AnnotationPropertyTypePresenter extends TypePresenter {

  private String m_property;
  private IType m_declaringType;
  private IType m_annotationType;

  public AnnotationPropertyTypePresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit);
  }

  @Override
  protected void execResetAction() throws CoreException {
    if (TypeUtility.exists(m_declaringType)) {
      AnnotationUpdateOperation op = new AnnotationUpdateOperation();
      op.setAnnotationType(m_annotationType);
      op.setDeclaringType(m_declaringType);
      op.removeProperty(m_property);
      new OperationJob(op).schedule();
    }
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setDeclaringType(IType declaringType) {
    m_declaringType = declaringType;
  }

  public String getProperty() {
    return m_property;
  }

  public void setProperty(String property) {
    m_property = property;
  }

  public IType getAnnotationType() {
    return m_annotationType;
  }

  public void setAnnotationType(IType annotationType) {
    m_annotationType = annotationType;
  }
}
