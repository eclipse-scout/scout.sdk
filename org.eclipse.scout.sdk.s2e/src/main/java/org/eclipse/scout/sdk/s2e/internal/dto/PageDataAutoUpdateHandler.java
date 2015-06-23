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
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateHandler;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

/**
 * Auto-update handler responding on {@link IRuntimeClasses#PageData} annotations that are placed on table pages (i.e.
 * {@link IRuntimeClasses#IPageWithTable}).
 *
 * @since 3.10.0-M1
 */
public class PageDataAutoUpdateHandler implements IDtoAutoUpdateHandler {

  private DataAnnotation getDataAnnotationForType(IType model) {
    if (CoreUtils.isInstanceOf(model, IRuntimeClasses.IPageWithTable)) {
      return DtoUtils.findDataAnnotation(model);
    }
    return null;
  }

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(IType model, IProject modelProject) {
    DataAnnotation dataAnnotation = getDataAnnotationForType(model);
    if (dataAnnotation != null) {
      if (model.equals(dataAnnotation.getDataType())) {
        S2ESdkActivator.logError("Data annotation points to itself. DTO generation not possible.", new Exception("Wrong @Data annotation value"));
        return null;
      }
      return new PageDataDtoUpdateOperation(model, modelProject, dataAnnotation);
    }
    return null;
  }
}
