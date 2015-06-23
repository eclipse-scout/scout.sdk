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
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateHandler;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

/**
 *
 */
public class FormDataDtoUpdateHandler implements IDtoAutoUpdateHandler {

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(IType modelType, IProject modelProject) {
    FormDataAnnotation formDataAnnotation = DtoUtils.findFormDataAnnotation(modelType);
    if (FormDataAnnotation.isCreate(formDataAnnotation) && formDataAnnotation.getFormDataType() != null) {
      if (modelType.equals(formDataAnnotation.getFormDataType())) {
        S2ESdkActivator.logError("FormData annotation points to itself. DTO generation not possible.", new Exception("Wrong @FormData annotation value."));
        return null;
      }
      return new FormDataDtoUpdateOperation(modelType, modelProject, formDataAnnotation);
    }
    return null;
  }
}
