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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link PageDataSourceBuilder}</h3>
 * 
 * @author aho
 * @since 3.10.0 28.08.2013
 */
public class PageDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private PageDataAnnotation m_pageDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public PageDataSourceBuilder(IType modelType, String elementName, PageDataAnnotation pageDataAnnotation) {
    super(modelType, elementName, false);
    m_pageDataAnnotation = pageDataAnnotation;
    setup();
  }

  @Override
  protected String getTableRowDataSuperClassSignature(IType table) throws CoreException {
    ITypeHierarchy tableHierarchy = TypeUtility.getSuperTypeHierarchy(table);
    IType parentTable = table;
    if (table.getDeclaringType().equals(getModelType())) {
      // we have our own inner table: take super class of our table
      parentTable = tableHierarchy.getSuperclass(table);
    }

    if (TypeUtility.exists(parentTable)) {
      IType declaringType = parentTable.getDeclaringType();
      if (TypeUtility.exists(declaringType)) {
        PageDataAnnotation pageDataAnnotation = ScoutTypeUtility.findPageDataAnnotation(declaringType, tableHierarchy);
        if (pageDataAnnotation != null) {
          IType superPageDataType = ScoutTypeUtility.getTypeBySignature(pageDataAnnotation.getPageDataTypeSignature());
          IType superTableBeanData = getTableRowDataType(superPageDataType);
          if (TypeUtility.exists(superTableBeanData)) {
            return SignatureCache.createTypeSignature(superTableBeanData.getFullyQualifiedName());
          }
        }
      }
    }
    return SignatureCache.createTypeSignature(RuntimeClasses.AbstractTableRowData);
  }

  @Override
  protected String computeSuperTypeSignature() throws CoreException {
    return getPageDataAnnotation().getSuperPageDataTypeSignature();
  }

  public PageDataAnnotation getPageDataAnnotation() {
    return m_pageDataAnnotation;
  }
}
