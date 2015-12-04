/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;

/**
 * Descriptor of a form data annotation hierarchy holding all information required to create a derived resource.
 */
public class FormDataAnnotationDescriptor {
  public static boolean isCreate(FormDataAnnotationDescriptor anot) {
    if (anot == null) {
      return false;
    }
    return (anot.getSdkCommand() == SdkCommand.CREATE) || (anot.getSdkCommand() == null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE);
  }

  public static boolean isIgnore(FormDataAnnotationDescriptor anot) {
    if (anot == null) {
      return false;
    }
    return (anot.getSdkCommand() == SdkCommand.IGNORE) || (anot.getSdkCommand() == null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.IGNORE);
  }

  public static boolean isSdkCommandDefault(FormDataAnnotationDescriptor anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.DEFAULT;
  }

  public static boolean isSdkCommandCreate(FormDataAnnotationDescriptor anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.CREATE;
  }

  public static boolean isSdkCommandUse(FormDataAnnotationDescriptor anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.USE;
  }

  public static boolean isSdkCommandIgnore(FormDataAnnotationDescriptor anot) {
    return anot != null && anot.getSdkCommand() == SdkCommand.IGNORE;
  }

  public static boolean isDefaultSubtypeSdkCommandCreate(FormDataAnnotationDescriptor anot) {
    return anot != null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE;
  }

  public static boolean isDefaultSubtypeSdkCommandIgnore(FormDataAnnotationDescriptor anot) {
    return anot != null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.IGNORE;
  }

  public static boolean isDefaultSubtypeSdkCommandDefault(FormDataAnnotationDescriptor anot) {
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

  public FormDataAnnotationDescriptor() {
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

  public void addInterfaces(IType[] interfaces) {
    for (IType t : interfaces) {
      m_interfaceTypes.add(t);
    }
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
