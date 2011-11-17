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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.InsertEdit;

/**
 * <h3>PlannerFieldNewOperation</h3> ...
 */
public class PlannerFieldNewOperation implements IOperation {

  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_createdField;
  private IType m_createdActivityMapType;
  private IType m_createdPlannerTable;

  public PlannerFieldNewOperation(IType declaringType) {
    m_declaringType = declaringType;
    // default
    setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractPlannerField, true));
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("typeName is null or empty.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    FormFieldNewOperation newOp = new FormFieldNewOperation(getDeclaringType());
    newOp.setTypeName(getTypeName());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setSiblingField(getSibling());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdField = newOp.getCreatedFormField();
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation labelOp = new NlsTextMethodUpdateOperation(getCreatedField(), NlsTextMethodUpdateOperation.GET_CONFIGURED_LABEL);
      labelOp.setNlsEntry(getNlsEntry());
      labelOp.validate();
      labelOp.run(monitor, workingCopyManager);
    }

    // planner field table
    m_createdPlannerTable = createPlannerTable(monitor, workingCopyManager);

    // planner field activity map
    m_createdActivityMapType = createActivityMap(monitor, workingCopyManager);

    // generic type
    Pattern p = Pattern.compile("extends\\s*" + SignatureUtility.getTypeReference(getSuperTypeSignature(), new SimpleImportValidator()), Pattern.MULTILINE);
    Matcher matcher = p.matcher(getCreatedField().getSource());
    if (matcher.find()) {
      Document doc = new Document(getCreatedField().getSource());
      InsertEdit genericEdit = new InsertEdit(matcher.end(), "<" + getCreatedField().getElementName() + "." + SdkProperties.TYPE_NAME_PLANNERFIELD_TABLE + "," + getCreatedField().getElementName() + "." + SdkProperties.TYPE_NAME_PLANNERFIELD_ACTIVITYMAP + ">");
      try {
        genericEdit.apply(doc);
        ScoutTypeUtility.setSource(getCreatedField(), doc.get(), workingCopyManager, monitor);
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not set the generic type of the calendar field.", e);
      }
    }
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedField(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  protected IType createPlannerTable(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    InnerTypeNewOperation plannerTableOp = new InnerTypeNewOperation(SdkProperties.TYPE_NAME_PLANNERFIELD_TABLE, getCreatedField(), false);
    plannerTableOp.setTypeModifiers(Flags.AccPublic);
    plannerTableOp.setSibling(null);
    plannerTableOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTable, true));
    AnnotationCreateOperation calendarAnnotOp = new AnnotationCreateOperation(null, Signature.createTypeSignature(RuntimeClasses.Order, true));
    calendarAnnotOp.addParameter("10.0");
    plannerTableOp.addAnnotation(calendarAnnotOp);
    plannerTableOp.validate();
    plannerTableOp.run(monitor, manager);
    IType createdType = plannerTableOp.getCreatedType();
    MethodOverrideOperation autoResizeColumnMehtodOp = new MethodOverrideOperation(createdType, "getConfiguredAutoResizeColumns", false);
    autoResizeColumnMehtodOp.setSimpleBody("return true;");
    autoResizeColumnMehtodOp.validate();
    autoResizeColumnMehtodOp.run(monitor, manager);
    MethodOverrideOperation sortEnabledMethodOp = new MethodOverrideOperation(createdType, "getConfiguredSortEnabled", false);
    sortEnabledMethodOp.setSimpleBody("return false;");
    sortEnabledMethodOp.validate();
    sortEnabledMethodOp.run(monitor, manager);

    return createdType;
  }

  protected IType createActivityMap(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    InnerTypeNewOperation activityMapOp = new InnerTypeNewOperation(SdkProperties.TYPE_NAME_PLANNERFIELD_ACTIVITYMAP, getCreatedField(), false);
    activityMapOp.setTypeModifiers(Flags.AccPublic);
    activityMapOp.setSibling(null);
    activityMapOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractActivityMap, true));
    AnnotationCreateOperation activityMapOrderOp = new AnnotationCreateOperation(null, Signature.createTypeSignature(RuntimeClasses.Order, true));
    activityMapOrderOp.addParameter("10.0");
    activityMapOp.addAnnotation(activityMapOrderOp);
    activityMapOp.validate();
    activityMapOp.run(monitor, manager);
    return activityMapOp.getCreatedType();
  }

  @Override
  public String getOperationName() {
    return "New Planner field";
  }

  public IType getCreatedField() {
    return m_createdField;
  }

  public IType getCreatedPlannerTable() {
    return m_createdPlannerTable;
  }

  public IType getCreatedActivityMapType() {
    return m_createdActivityMapType;
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

  public void setTypeName(String typeName) {
    m_typeName = typeName;
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
