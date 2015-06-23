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
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;

/**
 * <h3>{@link RowDataDtoUpdateOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 19.11.2014
 */
public class RowDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final DataAnnotation m_dataAnnotation;

  public RowDataDtoUpdateOperation(IType modelType, IProject modelProject, DataAnnotation dataAnnotation) {
    super(modelType, modelProject);
    m_dataAnnotation = dataAnnotation;
  }

  @Override
  public void validate() {
    if (getDataAnnotation() == null) {
      throw new IllegalArgumentException("DataAnnotation can not be null.");
    }
    super.validate();
  }

  public DataAnnotation getDataAnnotation() {
    return m_dataAnnotation;
  }

  @Override
  protected String createDerivedTypeSource() throws CoreException {
    IProject project = getDerivedFile().getProject();
    IJavaProject targetProject = JavaCore.create(project);
    ILookupEnvironment sharedEnv = ScoutSdkCore.createLookupEnvironment(targetProject, true);
    PropertyMap context = new PropertyMap();
    context.setProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, targetProject);
    StringBuilder dtoSource = DtoUtils.createTableRowDataTypeSource(getModelType(), getDataAnnotation(), sharedEnv, getDerivedFileLineSeparator(), context);
    return dtoSource.toString();
  }

  @Override
  protected String getDerivedTypeFqn() {
    return getDataAnnotation().getDataType().getName();
  }
}
