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

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;

/**
 * Descriptor of a form data annotation hierarchy holding all information required to create a derived resource.
 */
public class FormDataAnnotationDescriptor {
  private final Set<IType> m_interfaceTypes;
  private SdkCommand m_sdkCommand;
  private DefaultSubtypeSdkCommand m_defaultSubtypeSdkCommand;
  private int m_genericOrdinal = -1;
  private IType m_formDataType;
  private IType m_superType;
  private IAnnotatable m_annotationOwner;
  private IType m_genericOrdinalDefinitionType;

  protected FormDataAnnotationDescriptor() {
    m_interfaceTypes = new LinkedHashSet<>();
  }

  public static FormDataAnnotationDescriptor of(IType type) {
    FormDataAnnotationDescriptor anot = new FormDataAnnotationDescriptor();
    if (type == null) {
      return anot;
    }
    if (type.isInstanceOf(IScoutRuntimeTypes.IFormExtension) || type.isInstanceOf(IScoutRuntimeTypes.IFormFieldExtension)) {
      // extensions are annotated with @Data but behave like normal form fields -> bridge from @Data to @FormData

      Optional<DataAnnotationDescriptor> dataAnnotation = DataAnnotationDescriptor.of(type);
      dataAnnotation.ifPresent(dataAnnotationDescriptor -> {
        anot.setAnnotationOwner(type);
        anot.setDefaultSubtypeSdkCommand(DefaultSubtypeSdkCommand.CREATE);
        anot.setFormDataType(dataAnnotationDescriptor.getDataType());
        anot.setGenericOrdinal(-1);
        anot.setSdkCommand(SdkCommand.CREATE);
        anot.setSuperType(dataAnnotationDescriptor.getSuperDataType()
            .orElseGet(() -> type.javaEnvironment().findType(IScoutRuntimeTypes.AbstractFormFieldData).orElse(null)));
      });
    }
    else {
      parseFormDataAnnotationRec(anot, type, true);
    }
    return anot;
  }

  private static void parseFormDataAnnotationRec(FormDataAnnotationDescriptor descriptorToFill, IType type, boolean isOwner) {
    if (type == null) {
      return;
    }

    boolean replaceAnnotationPresent = type.annotations().withName(IScoutRuntimeTypes.Replace).existsAny();
    IType superType = type.superClass().orElse(null);

    parseFormDataAnnotationRec(descriptorToFill, superType, replaceAnnotationPresent);
    type.superInterfaces()
        .forEach(superInterface -> parseFormDataAnnotationRec(descriptorToFill, superInterface, replaceAnnotationPresent));

    if (replaceAnnotationPresent && superType != null && !superType.annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
      // super type is the original field that is going to be replaced by the given type
      // check whether the super type is embedded into a form field that is annotated by @FormData with SdkCommand.IGNORE.
      Optional<IType> declaringType = superType.declaringType();
      while (declaringType.isPresent()) {
        FormDataAnnotationDescriptor declaringTypeformDataAnnotation = of(declaringType.get());
        if (isIgnore(declaringTypeformDataAnnotation)) {
          // super type is embedded into a ignored form field. Hence this field is ignored as well. Adjust parsed annotation.
          descriptorToFill.setSdkCommand(SdkCommand.IGNORE);
          break;
        }
        declaringType = declaringType.get().declaringType();
      }
    }

    // If a replace annotation is present, the original field defines the attributes of the form data. In that case these attributes can be ignored for a formData annotation on a level.
    // An exception are attributes that are cumulative and may be added on any level. Those may be added even though the @Replace annotation is available.
    // A field that is once marked so that a DTO should be created, can never be set to ignore again. But an ignored field may be changed to create. Afterwards it can never be set to ignore again.
    // Therefore ignored fields may define all attributes and they are inherited from the first level that declares it to be created.
    // Forms are excluded from this rule: If a form has a @Replace annotation, it even though may define a different dto.
    boolean cumulativeAttribsOnly = replaceAnnotationPresent && !isIgnore(descriptorToFill) && !type.isInstanceOf(IScoutRuntimeTypes.IForm);

    fillFormDataAnnotation(type, descriptorToFill, isOwner, cumulativeAttribsOnly);
  }

  @SuppressWarnings("pmd:NPathComplexity")
  private static void fillFormDataAnnotation(IAnnotatable element, FormDataAnnotationDescriptor descriptorToFill, boolean isOwner, boolean cumulativeAttributesOnly) {
    Optional<FormDataAnnotation> optFda = element.annotations().withManagedWrapper(FormDataAnnotation.class).first();
    if (!optFda.isPresent()) {
      return;
    }

    FormDataAnnotation formDataAnnotation = optFda.get();

    // value
    IType dtoType = null;
    if (!formDataAnnotation.isValueDefault()) {
      dtoType = formDataAnnotation.value();
    }

    // sdk command
    SdkCommand sdkCommand = null;
    if (!formDataAnnotation.isSdkCommandDefault()) {
      sdkCommand = formDataAnnotation.sdkCommand();
    }

    // subtype command
    DefaultSubtypeSdkCommand subTypeCommand = null;
    if (!formDataAnnotation.isDefaultSubtypeSdkCommandDefault()) {
      subTypeCommand = formDataAnnotation.defaultSubtypeSdkCommand();
    }

    // generic ordinal
    int genericOrdinal = formDataAnnotation.genericOrdinal();

    // interfaces
    IType[] interfaces = formDataAnnotation.interfaces();

    // default setup
    if (!cumulativeAttributesOnly) {
      if (dtoType != null) {
        if (isOwner) {
          descriptorToFill.setFormDataType(dtoType);
        }
        else {
          descriptorToFill.setSuperType(dtoType);
        }
      }
      if (isOwner && sdkCommand != null) {
        descriptorToFill.setSdkCommand(sdkCommand);
      }
      if (subTypeCommand != null) {
        descriptorToFill.setDefaultSubtypeSdkCommand(subTypeCommand);
      }
      if (genericOrdinal > -1) {
        descriptorToFill.setGenericOrdinal(genericOrdinal);

        if (element instanceof IType) {
          descriptorToFill.setGenericOrdinalDefinitionType((IType) element);
        }
        else if (element instanceof IMethod) {
          descriptorToFill.setGenericOrdinalDefinitionType(((IMethod) element).declaringType());
        }
      }
    }

    // always add cumulative attributes
    descriptorToFill.setAnnotationOwner(element);
    if (interfaces != null && interfaces.length > 0) {
      descriptorToFill.addInterfaces(interfaces);
    }

    // correction
    boolean isMemberType = element instanceof IType && ((IType) element).declaringType().isPresent();
    if (isOwner && sdkCommand == SdkCommand.USE && dtoType != null && isMemberType) {
      descriptorToFill.setSuperType(dtoType);
      descriptorToFill.setFormDataType(null);
      descriptorToFill.setSdkCommand(SdkCommand.CREATE);
    }

    if (element instanceof IMethod && descriptorToFill.getSdkCommand() == null) {
      descriptorToFill.setSdkCommand(SdkCommand.CREATE);
    }
  }

  public static boolean isCreate(FormDataAnnotationDescriptor anot) {
    if (anot == null) {
      return false;
    }
    return (anot.getSdkCommand() == SdkCommand.CREATE) || (anot.getSdkCommand() == null && anot.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE);
  }

  public static boolean isIgnore(FormDataAnnotationDescriptor anot) {
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

  /**
   * @return the formDataType
   */
  public IType getFormDataType() {
    return m_formDataType;
  }

  /**
   * @param formDataType
   *          the formDataType to set
   */
  protected void setFormDataType(IType formDataType) {
    m_formDataType = formDataType;
  }

  /**
   * @return the sdkCommand
   */
  public SdkCommand getSdkCommand() {
    return m_sdkCommand;
  }

  /**
   * @param sdkCommand
   *          the sdkCommand to set
   */
  protected void setSdkCommand(SdkCommand sdkCommand) {
    m_sdkCommand = sdkCommand;
  }

  /**
   * @return the defaultSubtypeSdkCommand
   */
  public DefaultSubtypeSdkCommand getDefaultSubtypeSdkCommand() {
    return m_defaultSubtypeSdkCommand;
  }

  /**
   * @param defaultSubtypeSdkCommand
   *          the defaultSubtypeSdkCommand to set
   */
  protected void setDefaultSubtypeSdkCommand(DefaultSubtypeSdkCommand defaultSubtypeSdkCommand) {
    m_defaultSubtypeSdkCommand = defaultSubtypeSdkCommand;
  }

  /**
   * @return the genericOrdinal
   */
  public int getGenericOrdinal() {
    return m_genericOrdinal;
  }

  /**
   * @param genericOrdinal
   *          the genericOrdinal to set
   */
  protected void setGenericOrdinal(int genericOrdinal) {
    m_genericOrdinal = genericOrdinal;
  }

  /**
   * @return the superType
   */
  public IType getSuperType() {
    return m_superType;
  }

  /**
   * @param superType
   *          the super {@link IType} to set
   */
  protected void setSuperType(IType superType) {
    m_superType = superType;
  }

  public IType getAnnotationOwnerAsType() {
    if (m_annotationOwner instanceof IType) { // @FormData annotations can also be placed on Methods
      return (IType) m_annotationOwner;
    }
    return null;
  }

  public IAnnotatable getAnnotationOwner() {
    return m_annotationOwner;
  }

  protected void setAnnotationOwner(IAnnotatable annotationOwner) {
    m_annotationOwner = annotationOwner;
  }

  protected void addInterface(IType ifc) {
    m_interfaceTypes.add(ifc);
  }

  protected void addInterfaces(IType[] interfaces) {
    addAll(m_interfaceTypes, interfaces);
  }

  public Set<IType> getInterfaces() {
    return unmodifiableSet(m_interfaceTypes);
  }

  public IType getGenericOrdinalDefinitionType() {
    return m_genericOrdinalDefinitionType;
  }

  protected void setGenericOrdinalDefinitionType(IType genericOrdinalDefinitionType) {
    m_genericOrdinalDefinitionType = genericOrdinalDefinitionType;
  }
}
