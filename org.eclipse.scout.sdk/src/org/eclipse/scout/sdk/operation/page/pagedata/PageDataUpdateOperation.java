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
package org.eclipse.scout.sdk.operation.page.pagedata;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.data.AbstractSingleDerivedTypeAutoUpdateOperation;
import org.eclipse.scout.sdk.operation.form.formdata.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * Update operations that creates {@link IRuntimeClasses#AbstractTablePageData} for
 * {@link IRuntimeClasses#IPageWithTable}.
 * 
 * @since 3.10.0-M1
 */
public class PageDataUpdateOperation extends AbstractSingleDerivedTypeAutoUpdateOperation {

  private PageDataAnnotation m_pageDataAnnotation;

  public PageDataUpdateOperation(IType type, PageDataAnnotation annotation) {
    super(type);
    m_pageDataAnnotation = annotation;
  }

  @Override
  public String getOperationName() {
    return "Update Page Data for '" + getModelType().getElementName() + "'";
  }

  @Override
  protected boolean prepare() {
    if (m_pageDataAnnotation == null) {
      try {
        m_pageDataAnnotation = ScoutTypeUtility.findPageDataAnnotation(getModelType());
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not find page data annotation on '" + getModelType().getElementName() + "'.", e);
      }
    }
    if (m_pageDataAnnotation == null || StringUtility.isNullOrEmpty(m_pageDataAnnotation.getPageDataTypeSignature())) {
      return false;
    }
    return true;
  }

  @Override
  protected String getDerivedTypeSignature() {
    return m_pageDataAnnotation.getPageDataTypeSignature();
  }

  @Override
  protected boolean checkExistingDerivedTypeSuperTypeHierarchy(IType type, ITypeHierarchy hierarchy) {
    return hierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractTablePageData));
  }

  @Override
  protected ITypeSourceBuilder createTypeSourceBuilder(IType pageDataType) {
    IType pageType = getModelType();
    ITypeHierarchy pageTypeHierarchy = TypeUtility.getLocalTypeHierarchy(pageType);
    ITypeSourceBuilder sourceBuilder = new PageDataSourceBuilder(pageType, pageTypeHierarchy);
    sourceBuilder.setElementName(pageDataType.getElementName());
    sourceBuilder.setSuperTypeSignature(m_pageDataAnnotation.getSuperPageDataTypeSignature());
    return sourceBuilder;
  }
}
