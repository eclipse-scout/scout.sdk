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
package org.eclipse.scout.sdk.operation.form.field;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>PlannerFieldNewOperation</h3>
 */
public class PlannerFieldNewOperation implements IOperation {

  private final String m_typeName;
  private final IType m_declaringType;
  private boolean m_formatSource;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_createdField;

  public PlannerFieldNewOperation(String typeName, IType declaringType) {
    this(typeName, declaringType, true);
  }

  public PlannerFieldNewOperation(String typeName, IType declaringType, boolean formatSource) {
    m_typeName = typeName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IPlannerField, getDeclaringType().getJavaProject()));
  }

  @Override
  public void validate() {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("typeName is null or empty.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    FormFieldNewOperation newOp = new FormFieldNewOperation(getTypeName(), getDeclaringType());
    newOp.setSibling(getSibling());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    String superTypeFqn = SignatureUtility.getFullyQualifiedName(getSuperTypeSignature());
    if (CompareUtility.equals(superTypeFqn, IRuntimeClasses.AbstractPlannerField)) {
      // super type sig
      StringBuilder superTypeSigBuilder = new StringBuilder(superTypeFqn);
      superTypeSigBuilder.append(Signature.C_GENERIC_START).append(getTypeName()).append(".").append(SdkProperties.TYPE_NAME_PLANNERFIELD_TABLE).append(",");
      superTypeSigBuilder.append(getTypeName()).append(".").append(SdkProperties.TYPE_NAME_PLANNERFIELD_ACTIVITYMAP).append(",");
      superTypeSigBuilder.append(Long.class.getName()).append(",").append(Long.class.getName()).append(Signature.C_GENERIC_END);
      newOp.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeSigBuilder.toString()));
      createPlannerTable(newOp.getSourceBuilder(), monitor, workingCopyManager);
      createActivityMap(newOp.getSourceBuilder(), monitor, workingCopyManager);
    }
    // getConfiguredLabel method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }

    newOp.setFormatSource(isFormatSource());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdField = newOp.getCreatedType();

  }

  /**
   * @param sourceBuilder
   * @param monitor
   * @param workingCopyManager
   * @throws CoreException
   */
  private void createPlannerTable(ITypeSourceBuilder sourceBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ITypeSourceBuilder tableBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_PLANNERFIELD_TABLE);
    tableBuilder.setFlags(Flags.AccPublic);
    tableBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ITable, getDeclaringType().getJavaProject()));
    // order annotation
    tableBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(SdkProperties.ORDER_ANNOTATION_VALUE_STEP));
    // getConfiguredAutoResizeColumns method
    IMethodSourceBuilder getConfiguredAutoResizeColumnsBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "getConfiguredAutoResizeColumns");
    getConfiguredAutoResizeColumnsBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return true;"));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredAutoResizeColumnsBuilder), getConfiguredAutoResizeColumnsBuilder);

    // getConfiguredSortEnabled method
    IMethodSourceBuilder getConfiguredSortEnabledBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "getConfiguredSortEnabled");
    getConfiguredSortEnabledBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredSortEnabledBuilder), getConfiguredSortEnabledBuilder);

    sourceBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableBuilder), tableBuilder);
  }

  /**
   * @param sourceBuilder
   * @param monitor
   * @param workingCopyManager
   */
  private void createActivityMap(ITypeSourceBuilder sourceBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    ITypeSourceBuilder activityMapBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_PLANNERFIELD_ACTIVITYMAP);
    activityMapBuilder.setFlags(Flags.AccPublic);
    String activityMapSuperTypeSig = RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IActivityMap, getDeclaringType().getJavaProject());
    String superTypeFqn = SignatureUtility.getFullyQualifiedName(activityMapSuperTypeSig);
    if (CompareUtility.equals(superTypeFqn, IRuntimeClasses.AbstractActivityMap)) {
      // super type sig
      StringBuilder superTypeSigBuilder = new StringBuilder(superTypeFqn);
      superTypeSigBuilder.append(Signature.C_GENERIC_START).append(Long.class.getName()).append(',');
      superTypeSigBuilder.append(Long.class.getName()).append(Signature.C_GENERIC_END);
      activityMapSuperTypeSig = SignatureCache.createTypeSignature(superTypeSigBuilder.toString());
    }
    activityMapBuilder.setSuperTypeSignature(activityMapSuperTypeSig);

    // order annotation
    activityMapBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(SdkProperties.ORDER_ANNOTATION_VALUE_STEP));

    sourceBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeActivityMapKey(activityMapBuilder), activityMapBuilder);
  }

  @Override
  public String getOperationName() {
    return "New Planner field";
  }

  public IType getCreatedField() {
    return m_createdField;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

}
