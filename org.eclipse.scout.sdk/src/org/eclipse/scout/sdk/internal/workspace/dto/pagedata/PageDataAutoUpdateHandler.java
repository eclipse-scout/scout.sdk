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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractDtoUpdateHandler;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.DtoUpdateProperties;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataDtoUpdateOperation;

/**
 * Auto-update handler responding on {@link IRuntimeClasses#PageData} annotations that are placed on table pages (i.e.
 * {@link IRuntimeClasses#IPageWithTable}).
 * 
 * @since 3.10.0-M1
 */
public class PageDataAutoUpdateHandler extends AbstractDtoUpdateHandler {

  private boolean checkType(DtoUpdateProperties properties) throws CoreException {
    ITypeHierarchy superTypeHierarchy = ensurePropertySuperTypeHierarchy(properties);
    if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IPageWithTable))) {
      PageDataAnnotation pageDataAnnotation = ensurePropertyPageDataAnnotation(properties);
      return pageDataAnnotation != null && StringUtility.hasText(pageDataAnnotation.getSuperPageDataTypeSignature());
    }
    return false;
  }

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(DtoUpdateProperties properties) throws CoreException {
    if (checkType(properties)) {
      return new PageDataDtoUpdateOperation(properties.getType(), ensurePropertyPageDataAnnotation(properties));
    }
    else {
      return null;
    }
  }

}
