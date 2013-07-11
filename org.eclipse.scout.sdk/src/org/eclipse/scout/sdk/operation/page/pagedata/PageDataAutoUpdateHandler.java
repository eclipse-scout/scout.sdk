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
package org.eclipse.scout.sdk.operation.page.pagedata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.data.IAutoUpdateHandler;
import org.eclipse.scout.sdk.operation.data.IAutoUpdateOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * Auto-update handler responding on {@link IRuntimeClasses#PageData} annotations that are placed on table pages (i.e.
 * {@link IRuntimeClasses#IPageWithTable}).
 * 
 * @since 3.10.0-M1
 */
public class PageDataAutoUpdateHandler implements IAutoUpdateHandler {

  @Override
  public IAutoUpdateOperation createUpdateOperation(IType modelType, ITypeHierarchy modelTypeHierarchy) throws CoreException {
    if (!modelTypeHierarchy.contains(TypeUtility.getType(RuntimeClasses.IPageWithTable))) {
      return null;
    }

    PageDataAnnotation pageDataAnnotation = ScoutTypeUtility.findPageDataAnnotation(modelType);
    if (pageDataAnnotation == null || StringUtility.isNullOrEmpty(pageDataAnnotation.getPageDataTypeSignature())) {
      return null;
    }

    return new PageDataUpdateOperation(modelType, pageDataAnnotation);
  }
}
