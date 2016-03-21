/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.page;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link PageSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageSourceBuilder extends CompilationUnitSourceBuilder {

  public static final String INNER_TABLE_NAME = "Table";
  public static final String DATA_FETCH_METHOD_NAME = "getTableData";
  public static final String EXEC_LOAD_DATA_FILTER_ARG_NAME = "filter";

  private final String m_pageName;
  private boolean m_isPageWithTable;
  private String m_pageDataSignature;
  private String m_superTypeSignature;
  private String m_pageServiceIfcSignature;
  private String m_classIdValue;

  public PageSourceBuilder(String elementName, String packageName) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_pageName = elementName;
  }

  public void setup() {
    ITypeSourceBuilder pageBuilder = new TypeSourceBuilder(getPageName());
    addType(pageBuilder);

    pageBuilder.setFlags(Flags.AccPublic);
    String superTypeSignature = getSuperTypeSignature();
    if (isPageWithTable()) {
      ITypeSourceBuilder tableBuilder = createTableBuilder();
      pageBuilder.addType(tableBuilder);

      StringBuilder superTypeBuilder = new StringBuilder(SignatureUtils.toFullyQualifiedName(getSuperTypeSignature()));
      superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
      superTypeBuilder.append(tableBuilder.getFullyQualifiedName());
      superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
      superTypeSignature = Signature.createTypeSignature(superTypeBuilder.toString());
    }
    pageBuilder.setSuperTypeSignature(superTypeSignature);

    // class id
    if (StringUtils.isNotBlank(getClassIdValue())) {
      pageBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createClassId(getClassIdValue()));
    }

    // @Data
    if (isPageWithTable() && StringUtils.isNotBlank(getPageDataSignature())) {
      pageBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createData(getPageDataSignature()));
    }

    if (isPageWithTable()) {
      IMethodSourceBuilder execLoadData = createExecLoadData();
      pageBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodExecKey(execLoadData), execLoadData);
    }
    else {
      IMethodSourceBuilder execCreateChildPages = createExecCreateChildPages();
      pageBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodExecKey(execCreateChildPages), execCreateChildPages);
    }
  }

  protected IMethodSourceBuilder createExecCreateChildPages() {
    IMethodSourceBuilder execCreateChildPages = new MethodSourceBuilder("execCreateChildPages");
    execCreateChildPages.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
    execCreateChildPages.setFlags(Flags.AccProtected);
    execCreateChildPages.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    String paramType = new StringBuilder(IJavaRuntimeTypes.java_util_List).append(ISignatureConstants.C_GENERIC_START).append(IScoutRuntimeTypes.IPage)
        .append(ISignatureConstants.C_GENERIC_START).append('?').append(ISignatureConstants.C_GENERIC_END).append(ISignatureConstants.C_GENERIC_END).toString();
    execCreateChildPages.addParameter(new MethodParameterSourceBuilder("pageList", Signature.createTypeSignature(paramType)));
    execCreateChildPages.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(CoreUtils.getCommentBlock("add child pages"));
      }
    });
    return execCreateChildPages;
  }

  protected IMethodSourceBuilder createExecLoadData() {

    IMethodSourceBuilder execLoadData = new MethodSourceBuilder("execLoadData");
    execLoadData.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
    execLoadData.setFlags(Flags.AccProtected);
    execLoadData.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    execLoadData.addParameter(new MethodParameterSourceBuilder(EXEC_LOAD_DATA_FILTER_ARG_NAME, Signature.createTypeSignature(IScoutRuntimeTypes.SearchFilter)));

    execLoadData.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        if (getPageServiceIfcSignature() == null) {
          source.append(CoreUtils.getCommentBlock("implement data load")).append(lineDelimiter);
          source.append("// e.g.: importPageData(BEANS.get(IMyService.class).getTableData(").append(EXEC_LOAD_DATA_FILTER_ARG_NAME).append("));");
        }
        else {
          source.append("importPageData(").append(validator.useName(IScoutRuntimeTypes.BEANS)).append(".get(")
              .append(validator.useSignature(getPageServiceIfcSignature())).append(SuffixConstants.SUFFIX_STRING_class).append(").").append(DATA_FETCH_METHOD_NAME).append("(").append(EXEC_LOAD_DATA_FILTER_ARG_NAME).append("));");
        }
      }
    });
    return execLoadData;
  }

  protected ITypeSourceBuilder createTableBuilder() {
    ITypeSourceBuilder tableBuilder = new TypeSourceBuilder(INNER_TABLE_NAME);
    tableBuilder.setFlags(Flags.AccPublic);
    tableBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTable));
    return tableBuilder;
  }

  public String getPageName() {
    return m_pageName;
  }

  public boolean isPageWithTable() {
    return m_isPageWithTable;
  }

  public void setPageWithTable(boolean isPageWithTable) {
    m_isPageWithTable = isPageWithTable;
  }

  public String getPageDataSignature() {
    return m_pageDataSignature;
  }

  public void setPageDataSignature(String pageDataSignature) {
    m_pageDataSignature = pageDataSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getClassIdValue() {
    return m_classIdValue;
  }

  public void setClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
  }

  public String getPageServiceIfcSignature() {
    return m_pageServiceIfcSignature;
  }

  public void setPageServiceIfcSignature(String pageServiceIfcSignature) {
    m_pageServiceIfcSignature = pageServiceIfcSignature;
  }
}
