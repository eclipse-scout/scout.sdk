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
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.SdkCommand;

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
  private IType m_formDataType;
  private IType m_superType;
  private IAnnotatable m_annotationOwner;
  private final Set<IType> m_interfaceTypes;
  private IType m_genericOrdinalDefinitionType;

  public FormDataAnnotation() {
    m_interfaceTypes = new LinkedHashSet<>();
  }

  /**
   * @param formDataTypeSignature
   *          the formDataTypeSignature to set
   */
  public void setFormDataType(IType formDataType) {
    m_formDataType = formDataType;
  }

  /**
   * @return the formDataTypeSignature
   */
  public IType getFormDataType() {
    return m_formDataType;
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
   * @param superType
   *          the superTypeSignature to set
   */
  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  /**
   * @return the superTypeSignature
   */
  public IType getSuperType() {
    return m_superType;
  }

  public IAnnotatable getAnnotationOwner() {
    return m_annotationOwner;
  }

  public void setAnnotationOwner(IAnnotatable annotationOwner) {
    this.m_annotationOwner = annotationOwner;
  }

  public void addInterface(IType sig) {
    m_interfaceTypes.add(sig);
  }

  public void addInterfaces(Collection<IType> interfaces) {
    m_interfaceTypes.addAll(interfaces);
  }

  public Set<IType> getInterfaceSignatures() {
    return new LinkedHashSet<>(m_interfaceTypes);
  }

  public IType getGenericOrdinalDefinitionType() {
    return m_genericOrdinalDefinitionType;
  }

  public void setGenericOrdinalDefinitionType(IType genericOrdinalDefinitionType) {
    m_genericOrdinalDefinitionType = genericOrdinalDefinitionType;
  }
}
