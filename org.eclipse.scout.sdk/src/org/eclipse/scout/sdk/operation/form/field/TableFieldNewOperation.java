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
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.text.edits.InsertEdit;

/**
 * <h3>TableFieldNewOperation</h3> ...
 */
public class TableFieldNewOperation implements IOperation {

  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_createdField;
  private IType m_createdTable;

  public TableFieldNewOperation(IType declaringType) {
    m_declaringType = declaringType;
    // default
    setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTableField, true));
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
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
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

    m_createdTable = createTable(monitor, workingCopyManager);

    // generic type
    Pattern p = Pattern.compile("extends\\s*" + ScoutSdkUtility.getSimpleTypeSignature(getSuperTypeSignature()), Pattern.MULTILINE);
    Matcher matcher = p.matcher(getCreatedField().getSource());
    if (matcher.find()) {
      Document doc = new Document(getCreatedField().getSource());
      InsertEdit genericEdit = new InsertEdit(matcher.end(), "<" + getCreatedField().getElementName() + "." + ScoutIdeProperties.TYPE_NAME_TABLEFIELD_TABLE + ">");
      try {
        genericEdit.apply(doc);
        TypeUtility.setSource(getCreatedField(), doc.get(), workingCopyManager, monitor);
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not set the generic type of the table field.", e);
      }
    }
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedField(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  private IType createTable(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    InnerTypeNewOperation tableNewOp = new InnerTypeNewOperation(ScoutIdeProperties.TYPE_NAME_TABLEFIELD_TABLE, getCreatedField(), false);
    tableNewOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTable, true));
    tableNewOp.addTypeModifier(Flags.AccPublic);
    AnnotationCreateOperation orderAnnotOp = new AnnotationCreateOperation(null, Signature.createTypeSignature(RuntimeClasses.Order, true));
    orderAnnotOp.addParameter("10.0");
    tableNewOp.addAnnotation(orderAnnotOp);
    tableNewOp.validate();
    tableNewOp.run(monitor, manager);
    return tableNewOp.getCreatedType();
  }

  @Override
  public String getOperationName() {
    return "New table field";
  }

  public IType getCreatedField() {
    return m_createdField;
  }

  public IType getCreatedTable() {
    return m_createdTable;
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
