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
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.InsertEdit;

public class CalendarFieldNewOperation implements IOperation {

  // in members
  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  // out members
  private IType m_createdCalendarField;
  private IType m_createdCalendar;

  public CalendarFieldNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public CalendarFieldNewOperation(IType declaringType, boolean fomatSource) {
    m_declaringType = declaringType;
    m_formatSource = fomatSource;
    // default
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ICalendarField, getDeclaringType().getJavaProject()));
  }

  @Override
  public String getOperationName() {
    return "Create Calendar field '" + getTypeName() + "'...";
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
    ScoutSdk.logInfo("run operation: [" + getOperationName() + "]");
    FormFieldNewOperation newOp = new FormFieldNewOperation(getDeclaringType());
    newOp.setTypeName(getTypeName());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setSiblingField(getSibling());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdCalendarField = newOp.getCreatedFormField();
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation labelOp = new NlsTextMethodUpdateOperation(getCreatedCalendarField(), NlsTextMethodUpdateOperation.GET_CONFIGURED_LABEL);
      labelOp.setNlsEntry(getNlsEntry());
      labelOp.validate();
      labelOp.run(monitor, workingCopyManager);
    }
    // calendar
    InnerTypeNewOperation calendarOp = new InnerTypeNewOperation(SdkProperties.TYPE_NAME_CALENDARFIELD_CALENDAR, getCreatedCalendarField());
    calendarOp.setTypeModifiers(Flags.AccPublic);
    calendarOp.setSibling(null);
    calendarOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ICalendar, getDeclaringType().getJavaProject()));
    AnnotationCreateOperation calendarAnnotOp = new AnnotationCreateOperation(null, SignatureCache.createTypeSignature(RuntimeClasses.Order));
    calendarAnnotOp.addParameter("10.0");
    calendarOp.addAnnotation(calendarAnnotOp);
    calendarOp.validate();
    calendarOp.run(monitor, workingCopyManager);
    // generic on calendar field
    Pattern p = Pattern.compile("extends\\s*" + SignatureUtility.getTypeReference(getSuperTypeSignature(), new SimpleImportValidator()), Pattern.MULTILINE);
    Matcher matcher = p.matcher(getCreatedCalendarField().getSource());
    if (matcher.find()) {
      Document doc = new Document(getCreatedCalendarField().getSource());
      InsertEdit genericEdit = new InsertEdit(matcher.end(), "<" + getCreatedCalendarField().getElementName() + "." + SdkProperties.TYPE_NAME_CALENDARFIELD_CALENDAR + ">");
      try {
        genericEdit.apply(doc);
        ScoutTypeUtility.setSource(getCreatedCalendarField(), doc.get(), workingCopyManager, monitor);
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not set the generic type of the calendar field.", e);
      }
    }
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedCalendarField(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }

  }

  public IType getCreatedCalendarField() {
    return m_createdCalendarField;
  }

  public IType getCreatedCalendar() {
    return m_createdCalendar;
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
