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
import org.eclipse.scout.sdk.core.model.TypeFilters;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateHandler;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

/**
 * <h3>{@link RowDataAutoUpdateHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 19.11.2014
 */
public class RowDataAutoUpdateHandler implements IDtoAutoUpdateHandler {

  private static DataAnnotation getDataAnnotationForType(IType model) {

    // direct column or table extension
    if (CoreUtils.isInstanceOf(model, IRuntimeClasses.IColumn) || CoreUtils.isInstanceOf(model, IRuntimeClasses.ITableExtension)) {
      return DtoUtils.findDataAnnotation(model);
    }

    // check for table extension in IPageWithTableExtension
    if (CoreUtils.isInstanceOf(model, IRuntimeClasses.IPageWithTableExtension)) {
      IType innerTableExtension = CoreUtils.getInnerType(model, TypeFilters.getSubtypeFilter(IRuntimeClasses.ITableExtension));
      if (innerTableExtension != null) {
        return DtoUtils.findDataAnnotation(model);
      }
    }
    return null;
  }

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(IType model, IProject modelProject) {
    DataAnnotation dataAnnotation = getDataAnnotationForType(model);
    if (dataAnnotation != null) {
      if (model.equals(dataAnnotation.getDataType())) {
        S2ESdkActivator.logError("Data annotation points to itself. DTO generation not possible.", new Exception("Wrong @FormData annotation value."));
        return null;
      }
      return new RowDataDtoUpdateOperation(model, modelProject, dataAnnotation);
    }
    return null;
  }
}
