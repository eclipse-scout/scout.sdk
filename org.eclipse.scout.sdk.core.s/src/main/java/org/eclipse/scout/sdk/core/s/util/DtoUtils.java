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
package org.eclipse.scout.sdk.core.s.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.annotation.ColumnDataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.ColumnDataAnnotation.SdkColumnCommand;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.sourcebuilder.dto.CompositeFormDataTypeSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.dto.table.TableBeanDataSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.dto.table.TableFieldBeanFormDataSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.dto.table.TableRowDataTypeSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ExpressionSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * Contains utilities for DTO creation
 */
public final class DtoUtils {

  private static final String GENERATED_MSG = "This class is auto generated by the Scout SDK. No manual modifications recommended.";
  private static final String GENERATED_JAVADOC = "<b>NOTE:</b><br>" + GENERATED_MSG;
  private static final Pattern DATA_SUFFIX_PATTERN = Pattern.compile("(PageData|FieldData|Data)$");

  private DtoUtils() {
  }

  /**
   * Parses the possible available {@link IScoutRuntimeTypes#ColumnData} annotation on the given type. If the type is
   * not annotated, <code>null</code> is returned.
   *
   * @since 3.10.0-M5
   */
  public static SdkColumnCommand getSdkColumnCommand(IType type) {
    if (type == null) {
      return null;
    }

    SdkColumnCommand sdkColumnCommand = ColumnDataAnnotation.valueOf(type);
    if (sdkColumnCommand == SdkColumnCommand.IGNORE || !type.annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
      return sdkColumnCommand;
    }

    IType replacedType = type.superClass();
    if (getSdkColumnCommand(replacedType) != SdkColumnCommand.IGNORE) {
      return SdkColumnCommand.IGNORE;
    }
    if (sdkColumnCommand == null) {
      return SdkColumnCommand.IGNORE;
    }
    return sdkColumnCommand;
  }

  public static String getColumnValueTypeSignature(IType columnContainer) {
    List<String> resolvedTypeParamValues = CoreUtils.getResolvedTypeParamValueSignature(columnContainer, IScoutRuntimeTypes.IColumn, IScoutRuntimeTypes.TYPE_PARAM_COLUMN__VALUE_TYPE);
    if (resolvedTypeParamValues.isEmpty()) {
      return null;
    }
    return resolvedTypeParamValues.get(0); // only use first
  }

  public static String getRowDataName(String base) {
    return DATA_SUFFIX_PATTERN.matcher(base).replaceAll("") + "RowData";
  }

  public static String removeFieldSuffix(String fieldName) {
    if (fieldName.endsWith(ISdkProperties.SUFFIX_FORM_FIELD)) {
      fieldName = fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_FORM_FIELD.length());
    }
    else if (fieldName.endsWith(ISdkProperties.SUFFIX_BUTTON)) {
      fieldName = fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_BUTTON.length());
    }
    else if (fieldName.endsWith(ISdkProperties.SUFFIX_TABLE_COLUMN)) {
      fieldName = fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_TABLE_COLUMN.length());
    }
    else if (fieldName.endsWith(ISdkProperties.SUFFIX_OUTLINE_PAGE)) {
      fieldName = fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_OUTLINE_PAGE.length());
    }
    return fieldName;
  }

  /**
   * @return Returns the form field data/form data for the given form field/form or <code>null</code> if it does not
   *         have one.
   * @since 3.8.2
   */
  private static IType getFormDataType(IType modelType) {
    IType primaryType = getFormFieldDataPrimaryTypeRec(modelType);
    if (primaryType == null) {
      return null;
    }

    if (modelType.isInstanceOf(IScoutRuntimeTypes.IForm)) {
      // model type is a form and we have a corresponding DTO type (a form data).
      return primaryType;
    }

    // check if the primary type itself is the correct type
    String formDataName = removeFieldSuffix(modelType.elementName());
    if (primaryType.elementName().equals(formDataName)) {
      return primaryType;
    }

    // search field data within form data
    return primaryType.innerTypes().withRecursiveInnerTypes(true).withSimpleName(formDataName).first();
  }

  /**
   * @return Returns the form field data/form data for the given form field/form or <code>null</code> if it does not
   *         have one. The method walks recursively through the list of declaring classes until it has reached a primary
   *         type.
   * @since 3.8.2
   */
  private static IType getFormFieldDataPrimaryTypeRec(IType recursiveDeclaringType) {
    if (recursiveDeclaringType == null) {
      return null;
    }

    FormDataAnnotationDescriptor formDataAnnotation = getFormDataAnnotationDescriptor(recursiveDeclaringType);
    if (FormDataAnnotationDescriptor.isIgnore(formDataAnnotation)) {
      return null;
    }

    IType declaringType = recursiveDeclaringType.declaringType();
    if (declaringType == null) {
      // primary type
      if (FormDataAnnotationDescriptor.isCreate(formDataAnnotation) || FormDataAnnotationDescriptor.isSdkCommandUse(formDataAnnotation)) {
        return formDataAnnotation.getFormDataType();
      }
      return null;
    }

    return getFormFieldDataPrimaryTypeRec(declaringType);
  }

  public static String computeSuperTypeSignatureForFormData(IType modelType, FormDataAnnotationDescriptor formDataAnnotation, ITypeSourceBuilder sourceBuilder) {
    String superTypeSignature = null;
    if (modelType.annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
      IType replacedType = modelType.superClass();
      IType replacedFormFieldDataType = DtoUtils.getFormDataType(replacedType);
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureUtils.getTypeSignature(replacedFormFieldDataType);
      }
      sourceBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createReplace());
    }
    if (superTypeSignature == null) {
      superTypeSignature = DtoUtils.computeSuperTypeSignatureForFormData(modelType, formDataAnnotation);
    }
    return superTypeSignature;
  }

  private static String computeSuperTypeSignatureForFormData(IType formField, FormDataAnnotationDescriptor formDataAnnotation) {
    IType superType = formDataAnnotation.getSuperType();
    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      IType genericOrdinalDefinitionType = formDataAnnotation.getGenericOrdinalDefinitionType();
      if (genericOrdinalDefinitionType != null && superType != null && superType.hasTypeParameters()) {
        IType genericType = computeDtoGenericType(formField, genericOrdinalDefinitionType, formDataAnnotation.getGenericOrdinal());
        if (genericType != null) {
          String genericTypeName = SignatureUtils.toFullyQualifiedName(SignatureUtils.getTypeSignature(genericType));
          return Signature.createTypeSignature(superType.name() + ISignatureConstants.C_GENERIC_START + genericTypeName + ISignatureConstants.C_GENERIC_END);
        }
      }
    }
    return SignatureUtils.getTypeSignature(superType);
  }

  private static IType computeDtoGenericType(IType contextType, IType annotationOwnerType, int genericOrdinal) {
    if (contextType == null || Object.class.getName().equals(contextType.name()) || annotationOwnerType == null) {
      return null;
    }

    if (annotationOwnerType.typeArguments().size() <= genericOrdinal) {
      // cannot be found in arguments. check parameters
      int numTypeParams = annotationOwnerType.typeParameters().size();
      if (numTypeParams > genericOrdinal) {
        List<IType> params = annotationOwnerType.typeParameters().get(genericOrdinal).bounds();
        if (!params.isEmpty()) {
          return params.get(0);
        }
        return null;
      }
      // invalid index in annotation
      throw new SdkException("Invalid genericOrdinal value on class '" + annotationOwnerType.name() + "': " + genericOrdinal + ". This class has only " + numTypeParams + " type parameters.");
    }
    List<IType> resolvedTypeParamValue = CoreUtils.getResolvedTypeParamValue(contextType, annotationOwnerType, genericOrdinal);
    if (resolvedTypeParamValue.isEmpty()) {
      return null;
    }
    return resolvedTypeParamValue.get(0);
  }

  /**
   * Gets the form data type that is referenced in the form data annotation of the given form.<br>
   * If the annotation does not exist or points to an inexistent form data type, null is returned.
   *
   * @param form
   *          the form for which the form data should be returned.
   * @return the form data type or null if it could not be found.
   */
  public static IType findDtoForForm(IType form) {
    if (form == null) {
      return null;
    }
    FormDataAnnotationDescriptor a = getFormDataAnnotationDescriptor(form);
    return a.getFormDataType();
  }

  /**
   * Gets the page data type that is referenced in the page data annotation of the given page type.<br>
   * If the annotation does not exist or points to an inexistent page data type, null is returned.
   *
   * @param page
   *          the page for which the page data should be returned.
   * @return the page data class or null.
   */
  public static IType findDtoForPage(IType page) {
    if (page == null) {
      return null;
    }
    DataAnnotationDescriptor anot = getDataAnnotationDescriptor(page);
    if (anot == null) {
      return null;
    }
    return anot.getDataType();
  }

  /**
   * Parses the possible available {@link IScoutRuntimeTypes#PageData} or {@link IScoutRuntimeTypes#Data} annotation on
   * the given type. If the type is not annotated, <code>null</code> is returned.
   *
   * @since 3.10.0-M1
   */
  public static DataAnnotationDescriptor getDataAnnotationDescriptor(IType type) {
    if (type == null) {
      return null;
    }

    IType dtoType = getDataAnnotationValue(type);
    if (dtoType == null) {
      return null;
    }

    IType superType = null;
    IType curType = type.superClass();
    while (curType != null) {
      superType = getDataAnnotationValue(curType);
      if (superType != null) {
        break;
      }
      curType = curType.superClass();
    }
    return new DataAnnotationDescriptor(dtoType, superType, type);
  }

  /**
   * Checks whether the given type is annotated with a {@link IScoutRuntimeTypes#Data} annotation and if so, this method
   * returns its <code>value()</code> as resolved type signature. Otherwise <code>null</code>.
   *
   * @since 3.10.0-M1
   */
  private static IType getDataAnnotationValue(IType type) {
    IType dataType = DataAnnotation.valueOf(type);
    if (dataType != null) {
      return dataType;
    }

    // fall back to legacy name:
    IAnnotation annotation = type.annotations().withName(IScoutRuntimeTypes.PageData).first(); // fall back to old name
    if (annotation != null) {
      return annotation.element("value").value().get(IType.class);
    }

    return null; // not found
  }

  public static FormDataAnnotationDescriptor getFormDataAnnotationDescriptor(IType type) {
    FormDataAnnotationDescriptor anot = new FormDataAnnotationDescriptor();
    if (type.isInstanceOf(IScoutRuntimeTypes.IFormExtension) || type.isInstanceOf(IScoutRuntimeTypes.IFormFieldExtension)) {
      // extensions are annotated with @Data but behave like normal form fields -> bridge from @Data to @FormData

      DataAnnotationDescriptor dataAnnotation = getDataAnnotationDescriptor(type);
      if (dataAnnotation != null) {
        anot.setAnnotationOwner(type);
        anot.setDefaultSubtypeSdkCommand(DefaultSubtypeSdkCommand.CREATE);
        anot.setFormDataType(dataAnnotation.getDataType());
        anot.setGenericOrdinal(-1);
        anot.setSdkCommand(SdkCommand.CREATE);

        IType superDataType = dataAnnotation.getSuperDataType();
        if (superDataType != null) {
          anot.setSuperType(superDataType);
        }
        else {
          IType t = type.javaEnvironment().findType(IScoutRuntimeTypes.AbstractFormFieldData);
          anot.setSuperType(t);
        }
      }
    }
    else {
      parseFormDataAnnotationRec(anot, type, true);
    }
    return anot;
  }

  private static void parseFormDataAnnotationRec(FormDataAnnotationDescriptor descriptorToFill, IType type, boolean isOwner) {
    if (type != null) {
      boolean replaceAnnotationPresent = type.annotations().withName(IScoutRuntimeTypes.Replace).existsAny();
      IType superType = type.superClass();

      parseFormDataAnnotationRec(descriptorToFill, superType, replaceAnnotationPresent);
      for (IType superInterface : type.superInterfaces()) {
        parseFormDataAnnotationRec(descriptorToFill, superInterface, replaceAnnotationPresent);
      }

      if (replaceAnnotationPresent && superType != null && !superType.annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
        // super type is the original field that is going to be replaced by the given type
        // check whether the super type is embedded into a form field that is annotated by @FormData with SdkCommand.IGNORE.
        IType declaringType = superType.declaringType();
        while (declaringType != null) {
          FormDataAnnotationDescriptor declaringTypeformDataAnnotation = getFormDataAnnotationDescriptor(declaringType);
          if (FormDataAnnotationDescriptor.isIgnore(declaringTypeformDataAnnotation)) {
            // super type is embedded into a ignored form field. Hence this field is ignored as well. Adjust parsed annotation.
            descriptorToFill.setSdkCommand(SdkCommand.IGNORE);
            break;
          }
          declaringType = declaringType.declaringType();
        }
      }

      // If a replace annotation is present, the original field defines the attributes of the form data. In that case these attributes can be ignored for a formData annotation on a level.
      // An exception are attributes that are cumulative and may be added on any level. Those may be added even though the @Replace annotation is available.
      // A field that is once marked so that a DTO should be created, can never be set to ignore again. But an ignored field may be changed to create. Afterwards it can never be set to ignore again.
      // Therefore ignored fields may define all attributes and they are inherited from the first level that declares it to be created.
      // Forms are excluded from this rule: If a form has a @Replace annotation, it even though may define a different dto.
      boolean cumulativeAttribsOnly = replaceAnnotationPresent && !FormDataAnnotationDescriptor.isIgnore(descriptorToFill) && !type.isInstanceOf(IScoutRuntimeTypes.IForm);

      fillFormDataAnnotation(type, descriptorToFill, isOwner, cumulativeAttribsOnly);
    }
  }

  private static void fillFormDataAnnotation(IAnnotatable element, FormDataAnnotationDescriptor descriptorToFill, boolean isOwner, boolean cumulativeAttributesOnly) {

    FormDataAnnotation formDataAnnotation = element.annotations().withManagedWrapper(FormDataAnnotation.class).first();
    if (formDataAnnotation == null) {
      return;
    }

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
    IType[] interfaceSignatures = formDataAnnotation.interfaces();

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
    if (interfaceSignatures != null && interfaceSignatures.length > 0) {
      descriptorToFill.addInterfaces(interfaceSignatures);
    }

    // correction
    if (isOwner && sdkCommand == SdkCommand.USE && dtoType != null && element instanceof IType && ((IType) element).declaringType() != null) {
      descriptorToFill.setSuperType(dtoType);
      descriptorToFill.setFormDataType(null);
      descriptorToFill.setSdkCommand(SdkCommand.CREATE);
    }

    if (element instanceof IMethod && descriptorToFill.getSdkCommand() == null) {
      descriptorToFill.setSdkCommand(SdkCommand.CREATE);
    }
  }

  public static void addFormDataAdditionalInterfaces(FormDataAnnotationDescriptor formDataAnnotation, ITypeSourceBuilder sourceBuilder, IJavaEnvironment context) {
    Set<IType> interfaces = formDataAnnotation.getInterfaceSignatures();
    if (interfaces.isEmpty()) {
      return;
    }

    Set<IType> allSuperInterfaces = new HashSet<>();
    for (IType ifcType : interfaces) {
      if (CoreUtils.isOnClasspath(context, ifcType)) {
        sourceBuilder.addInterfaceSignature(SignatureUtils.getTypeSignature(ifcType));
        allSuperInterfaces.addAll(ifcType.superTypes().withSuperClasses(false).list());
      }
    }
    Set<String> allSuperInterfaceMethods = new HashSet<>();
    for (IType t : allSuperInterfaces) {
      for (IMethod m : t.methods().list()) {
        allSuperInterfaceMethods.add(SignatureUtils.createMethodIdentifier(m));
      }
    }
    for (IMethodSourceBuilder msb : sourceBuilder.getMethods()) {
      if (allSuperInterfaceMethods.contains(msb.getMethodIdentifier())) {
        msb.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
      }
    }
  }

  public static ICompilationUnitSourceBuilder createTableRowDataBuilder(IType modelType, DataAnnotationDescriptor dataAnnotation, IJavaEnvironment sharedEnv) {
    if (dataAnnotation == null) {
      return null;
    }
    Validate.notNull(modelType);
    Validate.notNull(sharedEnv);

    String targetPackage = Signature.getQualifier(dataAnnotation.getDataType().name());

    String dataTypeName = dataAnnotation.getDataType().elementName();

    ITypeSourceBuilder rowDataTypeSrc = new TableRowDataTypeSourceBuilder(dataTypeName, modelType, modelType, sharedEnv);

    // primary class comment
    rowDataTypeSrc.setComment(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

    // @Extends annotation
    addDtoExtendsAnnotation(rowDataTypeSrc, dataAnnotation.getAnnotationHolder());

    //@Generated annotation
    rowDataTypeSrc.addAnnotation(AnnotationSourceBuilderFactory.createGenerated(modelType.name(), GENERATED_MSG));

    ICompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(rowDataTypeSrc.getElementName() + SuffixConstants.SUFFIX_STRING_java, targetPackage);
    cuSrc.addType(rowDataTypeSrc);
    cuSrc.setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(cuSrc));
    return cuSrc;
  }

  public static ICompilationUnitSourceBuilder createPageDataBuilder(IType modelType, DataAnnotationDescriptor dataAnnotation, IJavaEnvironment sharedEnv) {
    if (dataAnnotation == null) {
      return null;
    }
    Validate.notNull(modelType);
    Validate.notNull(sharedEnv);

    String targetPackage = Signature.getQualifier(dataAnnotation.getDataType().name());

    ITypeSourceBuilder pageDataTypeSrc = new TableBeanDataSourceBuilder(modelType, dataAnnotation, dataAnnotation.getDataType().elementName(), sharedEnv);

    // primary class comment
    pageDataTypeSrc.setComment(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

    //@Generated annotation
    pageDataTypeSrc.addAnnotation(AnnotationSourceBuilderFactory.createGenerated(modelType.name(), GENERATED_MSG));

    ICompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(pageDataTypeSrc.getElementName() + SuffixConstants.SUFFIX_STRING_java, targetPackage);
    cuSrc.addType(pageDataTypeSrc);
    cuSrc.setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(cuSrc));
    return cuSrc;
  }

  public static ICompilationUnitSourceBuilder createFormDataBuilder(IType modelType, FormDataAnnotationDescriptor formDataAnnotation, IJavaEnvironment sharedEnv) {
    if (!FormDataAnnotationDescriptor.isCreate(formDataAnnotation)) {
      return null;
    }
    Validate.notNull(modelType);
    Validate.notNull(sharedEnv);

    String targetPackage = Signature.getQualifier(formDataAnnotation.getFormDataType().name());

    IType superType = formDataAnnotation.getSuperType();
    if (superType != null) {
      ITypeSourceBuilder formDataTypeSrc = null;
      if (superType.isInstanceOf(IScoutRuntimeTypes.AbstractTableFieldBeanData)) {
        // fill table bean
        formDataTypeSrc = new TableFieldBeanFormDataSourceBuilder(modelType, formDataAnnotation, formDataAnnotation.getFormDataType().elementName(), sharedEnv);
      }
      else {
        formDataTypeSrc = new CompositeFormDataTypeSourceBuilder(modelType, formDataAnnotation, formDataAnnotation.getFormDataType().elementName(), sharedEnv);
      }

      // primary class comment
      formDataTypeSrc.setComment(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

      // @Extends annotation
      addDtoExtendsAnnotation(formDataTypeSrc, formDataAnnotation.getAnnotationOwner());

      // @Generated annotation
      formDataTypeSrc.addAnnotation(AnnotationSourceBuilderFactory.createGenerated(modelType.name(), GENERATED_MSG));

      // add interfaces and @Override annotation for all methods that exist in the given interfaces
      addFormDataAdditionalInterfaces(formDataAnnotation, formDataTypeSrc, sharedEnv);

      ICompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(formDataTypeSrc.getElementName() + SuffixConstants.SUFFIX_STRING_java, targetPackage);
      cuSrc.addType(formDataTypeSrc);
      cuSrc.setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(cuSrc));
      return cuSrc;
    }
    return null;
  }

  public static void addDtoExtendsAnnotation(ITypeSourceBuilder target, IAnnotatable extendsAnnotationHolder) {
    AnnotationSourceBuilder extendsAnnotation = getExtendsAnnotationSourceBuilder(extendsAnnotationHolder);
    if (extendsAnnotation != null) {
      target.addAnnotation(extendsAnnotation);
    }
  }

  private static AnnotationSourceBuilder getExtendsAnnotationSourceBuilder(IAnnotatable element) {
    if (element instanceof IType) {
      IType extendedType = getExtendedType((IType) element);
      if (extendedType != null) {
        IType primaryType = CoreUtils.getPrimaryType(extendedType);
        IType extendedDto = null;
        if (primaryType.isInstanceOf(IScoutRuntimeTypes.IForm) || primaryType.isInstanceOf(IScoutRuntimeTypes.IFormField)) {
          if (extendedType.isInstanceOf(IScoutRuntimeTypes.ITable) && extendedType.declaringType() != null) {
            IType tableFieldDto = getFormDataType(extendedType.declaringType());
            extendedDto = getRowDataFor(tableFieldDto);
          }
          else {
            extendedDto = findDtoForForm(primaryType);
          }
        }
        else if (primaryType.isInstanceOf(IScoutRuntimeTypes.IExtension)) {
          extendedDto = findDtoForPage(primaryType);
        }
        else if (primaryType.isInstanceOf(IScoutRuntimeTypes.IPageWithTable)) {
          IType pageDto = findDtoForPage(primaryType);
          extendedDto = pageDto.innerTypes().withInstanceOf(IScoutRuntimeTypes.AbstractTableRowData).first();
        }

        if (extendedDto != null) {
          final IType dto = extendedDto;
          AnnotationSourceBuilder asb = new AnnotationSourceBuilder(IScoutRuntimeTypes.Extends);
          asb.putElement("value", ExpressionSourceBuilderFactory.createClassLiteral(SignatureUtils.getTypeSignature(dto)));
          return asb;
        }
      }
    }
    return null;
  }

  private static IType getRowDataFor(IType tableDto) {
    if (tableDto == null) {
      return null;
    }

    return tableDto.innerTypes().withInstanceOf(IScoutRuntimeTypes.AbstractTableRowData).first();
  }

  private static IType findExtendsAnnotationValue(IType element) {
    IType curType = element;
    while (curType != null) {
      IAnnotation extendsAnnotation = curType.annotations().withName(IScoutRuntimeTypes.Extends).first();
      if (extendsAnnotation != null) {
        IAnnotationElement value = extendsAnnotation.element("value");
        if (value != null && !value.isDefault()) {
          return value.value().get(IType.class);
        }
      }
      curType = curType.superClass();
    }
    return null;
  }

  /**
   * Gets the {@link IType} the given model type extends or <code>null</code> if none.<br>
   * The given modelType must be an IExtension or must have an @Extends annotation.
   *
   * @param modelType
   *          The extension whose owner should be returned.
   * @param localHierarchy
   *          The super hierarchy of the given model type.
   * @return The owner of the given extension or null.
   */
  public static IType getExtendedType(IType modelType) {
    // 1. try to read from @Extends annotation
    IType extendsValue = findExtendsAnnotationValue(modelType);
    if (extendsValue != null) {
      return extendsValue;
    }

    // 2. try to read from generic
    boolean isExtension = modelType.isInstanceOf(IScoutRuntimeTypes.IExtension);
    if (isExtension) {
      List<IType> owner = CoreUtils.getResolvedTypeParamValue(modelType, IScoutRuntimeTypes.IExtension, IScoutRuntimeTypes.TYPE_PARAM_EXTENSION__OWNER);
      if (!owner.isEmpty()) {
        return owner.get(0);
      }
    }

    // 3. try in declaring type
    IType declaringType = modelType.declaringType();
    if (declaringType != null) {
      IType extendsFromDeclaringType = getExtendedType(declaringType);
      if (extendsFromDeclaringType != null) {
        return extendsFromDeclaringType;
      }
    }

    // 4. if the model class has no annotation and is not an extension
    //    this can happen if e.g. a formfield is explicitly registered on the ExtensionRegistry.
    //    in this case we cannot detect anything

    return null;
  }
}
