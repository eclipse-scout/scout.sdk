/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.annotation;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scout.sdk.core.java.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.java.model.api.IMember;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.java.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;

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
    var descriptor = new FormDataAnnotationDescriptor();
    if (type == null) {
      return descriptor;
    }
    var scoutApi = type.javaEnvironment().requireApi(IScoutApi.class);
    if (type.isInstanceOf(scoutApi.IFormExtension()) || type.isInstanceOf(scoutApi.IFormFieldExtension())) {
      // extensions are annotated with @Data but behave like normal form fields -> bridge from @Data to @FormData

      var dataAnnotation = DataAnnotationDescriptor.of(type);
      dataAnnotation.ifPresent(dataAnnotationDescriptor -> {
        descriptor.setAnnotationOwner(type);
        descriptor.setDefaultSubtypeSdkCommand(DefaultSubtypeSdkCommand.CREATE);
        descriptor.setFormDataType(dataAnnotationDescriptor.getDataType());
        descriptor.setGenericOrdinal(-1);
        descriptor.setSdkCommand(SdkCommand.CREATE);
        descriptor.setSuperType(dataAnnotationDescriptor.getSuperDataType()
            .orElseGet(() -> type.javaEnvironment().findType(scoutApi.AbstractFormFieldData()).orElse(null)));
      });
    }
    else {
      parseFormDataAnnotationRec(descriptor, type, scoutApi, true);
    }
    return descriptor;
  }

  private static void parseFormDataAnnotationRec(FormDataAnnotationDescriptor descriptorToFill, IType type, IScoutApi api, boolean isOwner) {
    if (type == null) {
      return;
    }

    var replaceAnnotationFqn = api.Replace().fqn();
    var replaceAnnotationPresent = type.annotations().withName(replaceAnnotationFqn).existsAny();
    var superType = type.superClass().orElse(null);

    parseFormDataAnnotationRec(descriptorToFill, superType, api, replaceAnnotationPresent);
    type.superInterfaces()
        .forEach(superInterface -> parseFormDataAnnotationRec(descriptorToFill, superInterface, api, replaceAnnotationPresent));

    if (replaceAnnotationPresent && superType != null && !superType.annotations().withName(replaceAnnotationFqn).existsAny()) {
      // super type is the original field that is going to be replaced by the given type
      // check whether the super type is embedded into a form field that is annotated by @FormData with SdkCommand.IGNORE.
      var declaringType = superType.declaringType();
      while (declaringType.isPresent()) {
        var declaringTypeFormDataAnnotation = of(declaringType.orElseThrow());
        if (isIgnore(declaringTypeFormDataAnnotation)) {
          // super type is embedded into a ignored form field. Hence this field is ignored as well. Adjust parsed annotation.
          descriptorToFill.setSdkCommand(SdkCommand.IGNORE);
          break;
        }
        declaringType = declaringType.orElseThrow().declaringType();
      }
    }

    // If a replace annotation is present, the original field defines the attributes of the form data. In that case these attributes can be ignored for a formData annotation on a level.
    // An exception are attributes that are cumulative and may be added on any level. Those may be added even though the @Replace annotation is available.
    // A field that is once marked so that a DTO should be created, can never be set to ignore again. But an ignored field may be changed to create. Afterwards it can never be set to ignore again.
    // Therefore ignored fields may define all attributes and they are inherited from the first level that declares it to be created.
    // Forms are excluded from this rule: If a form has a @Replace annotation, it even though may define a different dto.
    var cumulativeAttribsOnly = replaceAnnotationPresent && !isIgnore(descriptorToFill) && !type.isInstanceOf(api.IForm());

    fillFormDataAnnotation(type, descriptorToFill, isOwner, cumulativeAttribsOnly);
  }

  @SuppressWarnings("pmd:NPathComplexity")
  private static void fillFormDataAnnotation(IAnnotatable element, FormDataAnnotationDescriptor descriptorToFill, boolean isOwner, boolean cumulativeAttributesOnly) {
    var optFda = element.annotations().withManagedWrapper(FormDataAnnotation.class).first();
    if (optFda.isEmpty()) {
      return;
    }

    var formDataAnnotation = optFda.orElseThrow();

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
    var genericOrdinal = formDataAnnotation.genericOrdinal();

    // interfaces
    var interfaces = formDataAnnotation.interfaces();

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
          descriptorToFill.setGenericOrdinalDefinitionType(((IMethod) element).requireDeclaringType());
        }
      }
    }

    // always add cumulative attributes
    descriptorToFill.setAnnotationOwner(element);
    if (interfaces != null && interfaces.length > 0) {
      descriptorToFill.addInterfaces(interfaces);
    }

    // correction
    var isMemberType = element instanceof IMember && ((IMember) element).declaringType().isPresent();
    if (isOwner && sdkCommand == SdkCommand.USE && dtoType != null && isMemberType) {
      descriptorToFill.setSuperType(dtoType);
      descriptorToFill.setFormDataType(null);
      descriptorToFill.setSdkCommand(SdkCommand.CREATE);
    }

    if (element instanceof IMethod && descriptorToFill.getSdkCommand() == null) {
      descriptorToFill.setSdkCommand(SdkCommand.CREATE);
    }
  }

  public static boolean isCreate(FormDataAnnotationDescriptor descriptor) {
    if (descriptor == null) {
      return false;
    }
    return (descriptor.getSdkCommand() == SdkCommand.CREATE) || (descriptor.getSdkCommand() == null && descriptor.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE);
  }

  public static boolean isIgnore(FormDataAnnotationDescriptor descriptor) {
    return (descriptor.getSdkCommand() == SdkCommand.IGNORE) || (descriptor.getSdkCommand() == null && descriptor.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.IGNORE);
  }

  public static boolean isSdkCommandDefault(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getSdkCommand() == SdkCommand.DEFAULT;
  }

  public static boolean isSdkCommandCreate(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getSdkCommand() == SdkCommand.CREATE;
  }

  public static boolean isSdkCommandUse(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getSdkCommand() == SdkCommand.USE;
  }

  public static boolean isSdkCommandIgnore(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getSdkCommand() == SdkCommand.IGNORE;
  }

  public static boolean isDefaultSubtypeSdkCommandCreate(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.CREATE;
  }

  public static boolean isDefaultSubtypeSdkCommandIgnore(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.IGNORE;
  }

  public static boolean isDefaultSubtypeSdkCommandDefault(FormDataAnnotationDescriptor descriptor) {
    return descriptor != null && descriptor.getDefaultSubtypeSdkCommand() == DefaultSubtypeSdkCommand.DEFAULT;
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
