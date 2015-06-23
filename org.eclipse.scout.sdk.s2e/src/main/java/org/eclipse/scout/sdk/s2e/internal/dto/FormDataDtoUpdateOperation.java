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
package org.eclipse.scout.sdk.s2e.internal.dto;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;

/**
 *
 */
public class FormDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   */
  public FormDataDtoUpdateOperation(IType modelType, IProject modelProject, FormDataAnnotation formDataAnnotation) {
    super(modelType, modelProject);
    m_formDataAnnotation = formDataAnnotation;
  }

  @Override
  public void validate() {
    if (getFormDataAnnotation() == null) {
      throw new IllegalArgumentException("FormDataAnnotation can not be null.");
    }
    super.validate();
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }

  @Override
  protected String createDerivedTypeSource() throws CoreException {
    IProject project = getDerivedFile().getProject();
    IJavaProject targetProject = JavaCore.create(project);
    ILookupEnvironment sharedEnv = ScoutSdkCore.createLookupEnvironment(targetProject, true);
    PropertyMap context = new PropertyMap();
    context.setProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, targetProject);
    StringBuilder dtoSource = DtoUtils.createFormDataSource(getModelType(), getFormDataAnnotation(), sharedEnv, getDerivedFileLineSeparator(), context);
    return dtoSource.toString();
  }

  @Override
  protected String getDerivedTypeFqn() {
    return getFormDataAnnotation().getFormDataType().getName();
  }
}
