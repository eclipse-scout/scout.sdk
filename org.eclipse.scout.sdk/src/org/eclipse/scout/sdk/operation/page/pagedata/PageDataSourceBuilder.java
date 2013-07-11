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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.data.table.AbstractTableBeanSourceContentBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * Auto-update handler responding on {@link IRuntimeClasses#PageData} annotations that are placed on table pages (i.e.
 * {@link IRuntimeClasses#IPageWithTable}). It generates sub classes of {@link IRuntimeClasses#AbstractTablePageData}.
 * 
 * @since 3.10.0-M1
 */
public class PageDataSourceBuilder extends TypeSourceBuilder {

  private final IType m_tablePage;
  private final ITypeHierarchy m_hierarchy;

  public PageDataSourceBuilder(IType tablePage, ITypeHierarchy hierarchy) {
    super(ResourceUtility.getLineSeparator(tablePage.getOpenable()));
    m_tablePage = tablePage;
    m_hierarchy = hierarchy;
  }

  @Override
  public String createSource(IImportValidator validator) throws JavaModelException {
    TablePageBeanSourceContentBuilder tableBeanBuilder = new TablePageBeanSourceContentBuilder(m_tablePage, m_hierarchy, NL);
    tableBeanBuilder.addContentBuilders(this, validator);
    return super.createSource(validator);
  }

  protected static class TablePageBeanSourceContentBuilder extends AbstractTableBeanSourceContentBuilder {

    public TablePageBeanSourceContentBuilder(IType tablePage, ITypeHierarchy hierarchy, String nl) {
      super(tablePage, hierarchy, nl, TypeUtility.getType(RuntimeClasses.IPageWithTable));
    }

    @Override
    protected String getTableRowDataSuperClassSignature(IImportValidator validator, IType table, ITypeHierarchy hierarchy) {
      try {
        IType parentTable = hierarchy.getSuperclass(table);
        if (TypeUtility.exists(parentTable)) {
          IType declaringType = parentTable.getDeclaringType();
          if (TypeUtility.exists(declaringType)) {
            PageDataAnnotation pageDataAnnotation = ScoutTypeUtility.findPageDataAnnotation(declaringType);
            if (pageDataAnnotation != null) {
              IType superPageDataType = ScoutTypeUtility.getTypeBySignature(pageDataAnnotation.getPageDataTypeSignature());
              String superTableRowBeanName = getTableRowBeanName(parentTable);
              IType superTableBeanData = superPageDataType.getType(superTableRowBeanName);
              if (TypeUtility.exists(superTableBeanData)) {
                validator.addImport(superPageDataType.getFullyQualifiedName());
                return Signature.createTypeSignature(superTableBeanData.getTypeQualifiedName(), false);
              }
            }
          }
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("error while computing super class signature for [" + table + "]", e);
      }
      return Signature.createTypeSignature(RuntimeClasses.AbstractTableRowData, true);
    }
  }
}
