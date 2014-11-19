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
package org.eclipse.scout.sdk.workspace.dto.formdata;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 *
 */
public class FormDataAnnotation {
  public static boolean isCreate(FormDataAnnotation anot) {
    if (anot == null) {
      return false;
    }
    return (anot.getSdkCommand() == SdkCommand.CREATE) || (anot.getSdkCommand() == null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE);
  }

  public static boolean isIgnore(FormDataAnnotation anot) {
    if (anot == null) {
      return false;
    }
    return (anot.getSdkCommand() == SdkCommand.IGNORE) || (anot.getSdkCommand() == null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.IGNORE);
  }

  public static boolean isSdkCommandDefault(FormDataAnnotation anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.DEFAULT;
  }

  public static boolean isSdkCommandCreate(FormDataAnnotation anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.CREATE;
  }

  public static boolean isSdkCommandUse(FormDataAnnotation anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.USE;
  }

  public static boolean isSdkCommandIgnore(FormDataAnnotation anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.IGNORE;
  }

  public static boolean isDefaultSubtypeSdkCommandCreate(FormDataAnnotation anot) {
    return anot != null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE;
  }

  public static boolean isDefaultSubtypeSdkCommandIgnore(FormDataAnnotation anot) {
    return anot != null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.IGNORE;
  }

  public static boolean isDefaultSubtypeSdkCommandDefault(FormDataAnnotation anot) {
    return anot != null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.DEFAULT;
  }

  private SdkCommand m_sdkCommand;
  private DefaultSubtypeSdkCommand m_defaultSubtypeSdkCommand;
  private int m_genericOrdinal = -1;
  private String m_formDataTypeSignature;
  private String m_superTypeSignature;
  private IJavaElement m_annotationOwner;
  private final Set<String> m_interfaceSignatures;
  private IType m_genericOrdinalDefinitionType;

  public FormDataAnnotation() {
    m_interfaceSignatures = new LinkedHashSet<String>();
  }

  /**
   * @param formDataTypeSignature
   *          the formDataTypeSignature to set
   */
  public void setFormDataTypeSignature(String formDataTypeSignature) {
    m_formDataTypeSignature = formDataTypeSignature;
  }

  /**
   * @return the formDataTypeSignature
   */
  public String getFormDataTypeSignature() {
    return m_formDataTypeSignature;
  }

  /**
   * Gets the form data type that is referenced in the receiver annotation.
   *
   * @return The form data type or null if it could not be found.
   */
  public IType getFormDataType() {
    if (getFormDataTypeSignature() != null) {
      IType formData = TypeUtility.getTypeBySignature(getFormDataTypeSignature());
      if (TypeUtility.exists(formData)) {
        return formData;
      }
    }
    return null;
  }

  /**
   * @param sdkCommand
   *          the sdkCommand to set
   */
  public void setSdkCommand(SdkCommand sdkCommand) {
    m_sdkCommand = sdkCommand;
  }

  /**
   * @return the sdkCommand
   */
  public SdkCommand getSdkCommand() {
    return m_sdkCommand;
  }

  /**
   * @param defaultSubtypeSdkCommand
   *          the defaultSubtypeSdkCommand to set
   */
  public void setDefaultSubtypeSdkCommand(DefaultSubtypeSdkCommand defaultSubtypeSdkCommand) {
    m_defaultSubtypeSdkCommand = defaultSubtypeSdkCommand;
  }

  /**
   * @return the defaultSubtypeSdkCommand
   */
  public DefaultSubtypeSdkCommand getDefaultSubtypeSdkCommand() {
    return m_defaultSubtypeSdkCommand;
  }

  /**
   * @param genericOrdinal
   *          the genericOrdinal to set
   */
  public void setGenericOrdinal(int genericOrdinal) {
    m_genericOrdinal = genericOrdinal;
  }

  /**
   * @return the genericOrdinal
   */
  public int getGenericOrdinal() {
    return m_genericOrdinal;
  }

  /**
   * @param superTypeSignature
   *          the superTypeSignature to set
   */
  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  /**
   * @return the superTypeSignature
   */
  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public IJavaElement getAnnotationOwner() {
    return m_annotationOwner;
  }

  public void setAnnotationOwner(IJavaElement annotationOwner) {
    this.m_annotationOwner = annotationOwner;
  }

  public void addInterfaceSignature(String sig) {
    m_interfaceSignatures.add(sig);
  }

  public Set<String> getInterfaceSignatures() {
    return new LinkedHashSet<String>(m_interfaceSignatures);
  }

  public IType getGenericOrdinalDefinitionType() {
    return m_genericOrdinalDefinitionType;
  }

  public void setGenericOrdinalDefinitionType(IType genericOrdinalDefinitionType) {
    m_genericOrdinalDefinitionType = genericOrdinalDefinitionType;
  }
}
