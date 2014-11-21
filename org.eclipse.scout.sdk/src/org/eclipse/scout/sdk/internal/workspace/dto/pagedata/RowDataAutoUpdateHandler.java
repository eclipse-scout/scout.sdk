/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace.dto.pagedata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractDtoUpdateHandler;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.DtoUpdateProperties;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.DataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.RowDataDtoUpdateOperation;

/**
 * <h3>{@link RowDataAutoUpdateHandler}</h3>
 *
 * @author mvi
 * @since 4.1.0 19.11.2014
 */
public class RowDataAutoUpdateHandler extends AbstractDtoUpdateHandler {

  private boolean checkType(DtoUpdateProperties properties) throws CoreException {
    ITypeHierarchy superTypeHierarchy = ensurePropertySuperTypeHierarchy(properties);
    if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IColumn)) || superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITableExtension))) {
      DataAnnotation dataAnnotation = ensurePropertyDataAnnotation(properties);
      return dataAnnotation != null;
    }
    return false;
  }

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(DtoUpdateProperties properties) throws CoreException {
    if (checkType(properties)) {
      return new RowDataDtoUpdateOperation(properties.getType(), ensurePropertyDataAnnotation(properties));
    }
    return null;
  }
}