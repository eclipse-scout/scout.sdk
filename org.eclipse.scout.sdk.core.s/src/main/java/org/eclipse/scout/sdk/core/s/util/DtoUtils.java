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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.ExpressionValueType;
import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IAnnotation;
import org.eclipse.scout.sdk.core.model.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.TypeFilters;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.SdkColumnCommand;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.SdkCommand;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.CompositeFormDataTypeSourceBuilder;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table.TableBeanDataSourceBuilder;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table.TableFieldBeanFormDataSourceBuilder;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table.TableRowDataTypeSourceBuilder;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 *
 */
public final class DtoUtils {

  private static final String GENERATED_MSG = "This class is auto generated by the Scout SDK. No manual modifications recommended.";
  private static final String GENERATED_JAVADOC = "<b>NOTE:</b><br>" + GENERATED_MSG + "\n\n@generated";

  /**
   * Parses the possible available {@link IRuntimeClasses#ColumnData} annotation on the given type. If the type is
   * not
   * annotated, <code>null</code> is returned.
   *
   * @throws JavaModelException
   * @since 3.10.0-M5
   */
  public static SdkColumnCommand findColumnDataSdkColumnCommand(IType type) {
    if (type == null) {
      return null;
    }

    SdkColumnCommand sdkColumnCommand = getColumnDataAnnotationValue(type);
    if (sdkColumnCommand == SdkColumnCommand.IGNORE || !ScoutUtils.existsReplaceAnnotation(type)) {
      return sdkColumnCommand;
    }

    IType replacedType = type.getSuperClass();
    if (findColumnDataSdkColumnCommand(replacedType) != SdkColumnCommand.IGNORE) {
      return SdkColumnCommand.IGNORE;
    }
    if (sdkColumnCommand == null) {
      return SdkColumnCommand.IGNORE;
    }
    return sdkColumnCommand;
  }

  /**
   * Checks whether the given type is annotated with a {@link IRuntimeClasses#ColumnData} annotation and if so, this
   * method returns its <code>value()</code> as resolved type signature. Otherwise <code>null</code>.
   *
   * @throws JavaModelException
   * @since 3.10.0-M5
   */
  private static SdkColumnCommand getColumnDataAnnotationValue(IType type) {
    if (type == null) {
      return null;
    }

    IAnnotation annotation = CoreUtils.getAnnotation(type, IRuntimeClasses.ColumnData);
    if (annotation == null) {
      return null;
    }

    String value = CoreUtils.getAnnotationValueString(annotation, "value");
    if (StringUtils.isNotBlank(value)) {
      return SdkColumnCommand.valueOf(Signature.getSimpleName(value));
    }

    return null;
  }

  public static String getColumnValueTypeSignature(IType columnContainer) {
    ListOrderedSet<String> resolvedTypeParamValues = CoreUtils.getResolvedTypeParamValueSignature(columnContainer, IRuntimeClasses.IColumn, IRuntimeClasses.TYPE_PARAM_COLUMN_VALUE_TYPE);
    if (CollectionUtils.isEmpty(resolvedTypeParamValues)) {
      return null;
    }
    return resolvedTypeParamValues.get(0); // only use first
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
   * @return Returns the form field data for the given form field or <code>null</code> if it does not have one.
   * @since 3.8.2
   */
  public static IType getFormDataType(IType formField) {
    IType primaryType = getFormFieldDataPrimaryTypeRec(formField);
    if (primaryType == null) {
      return null;
    }

    // check if the primary type itself is the correct type
    String formDataName = removeFieldSuffix(formField.getSimpleName());
    if (primaryType.getSimpleName().equals(formDataName)) {
      return primaryType;
    }

    // search field data within form data
    return CoreUtils.findInnerType(primaryType, formDataName);
  }

  /**
   * @return Returns the form field data for the given form field or <code>null</code> if it does not have one. The
   *         method walks recursively through the list of declaring classes until it has reached a primary type.
   * @since 3.8.2
   */
  private static IType getFormFieldDataPrimaryTypeRec(IType recursiveDeclaringType) {
    if (recursiveDeclaringType == null) {
      return null;
    }

    FormDataAnnotation formDataAnnotation = findFormDataAnnotation(recursiveDeclaringType);
    if (FormDataAnnotation.isIgnore(formDataAnnotation)) {
      return null;
    }

    IType declaringType = recursiveDeclaringType.getDeclaringType();
    if (declaringType == null) {
      // primary type
      if (FormDataAnnotation.isSdkCommandCreate(formDataAnnotation) || FormDataAnnotation.isSdkCommandUse(formDataAnnotation)) {
        return formDataAnnotation.getFormDataType();
      }
      return null;
    }

    return getFormFieldDataPrimaryTypeRec(declaringType);
  }

  public static String computeSuperTypeSignatureForFormData(IType modelType, FormDataAnnotation formDataAnnotation, ITypeSourceBuilder sourceBuilder) {
    String superTypeSignature = null;
    if (ScoutUtils.existsReplaceAnnotation(modelType)) {
      IType replacedType = modelType.getSuperClass();
      IType replacedFormFieldDataType = DtoUtils.getFormDataType(replacedType);
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureUtils.getResolvedSignature(replacedFormFieldDataType);
      }
      sourceBuilder.addAnnotationSourceBuilder(ScoutAnnotationSourceBuilderFactory.createReplaceAnnotationBuilder());
    }
    if (superTypeSignature == null) {
      superTypeSignature = DtoUtils.computeSuperTypeSignatureForFormData(modelType, formDataAnnotation);
    }
    return superTypeSignature;
  }

  private static String computeSuperTypeSignatureForFormData(IType formField, FormDataAnnotation formDataAnnotation) {
    IType superType = formDataAnnotation.getSuperType();
    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      IType genericOrdinalDefinitionType = formDataAnnotation.getGenericOrdinalDefinitionType();
      if (genericOrdinalDefinitionType != null) {
        if (CoreUtils.isGenericType(superType)) {
          IType genericType = computeDtoGenericType(formField, genericOrdinalDefinitionType, formDataAnnotation.getGenericOrdinal());
          if (genericType != null) {
            String genericTypeName = SignatureUtils.toFullyQualifiedName(SignatureUtils.getResolvedSignature(genericType));
            return Signature.createTypeSignature(superType.getName() + Signature.C_GENERIC_START + genericTypeName + Signature.C_GENERIC_END);
          }
        }
      }
    }
    return SignatureUtils.getResolvedSignature(superType);
  }

  private static IType computeDtoGenericType(IType contextType, IType annotationOwnerType, int genericOrdinal) {
    if (contextType == null || Object.class.getName().equals(contextType.getName()) || annotationOwnerType == null) {
      return null;
    }

    if (annotationOwnerType.getTypeArguments().size() <= genericOrdinal) {
      // invalid index in annotation
      throw new RuntimeException("Invalid genericOrdinal value on class '" + annotationOwnerType.getName() + "'.");
    }

    ListOrderedSet<IType> resolvedTypeParamValue = CoreUtils.getResolvedTypeParamValue(contextType, annotationOwnerType, genericOrdinal);
    if (CollectionUtils.isEmpty(resolvedTypeParamValue)) {
      return null;
    }
    return resolvedTypeParamValue.get(0);
  }

  public static FormDataAnnotation findFormDataAnnotation(IType type) {
    FormDataAnnotation anot = new FormDataAnnotation();
    if (CoreUtils.isInstanceOf(type, IRuntimeClasses.IFormExtension) || CoreUtils.isInstanceOf(type, IRuntimeClasses.IFormFieldExtension)) {
      // extensions are annotated with @Data but behave like normal form fields -> bridge from @Data to @FormData

      DataAnnotation dataAnnotation = findDataAnnotation(type);
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
          IType t = type.getLookupEnvironment().findType(IRuntimeClasses.AbstractFormFieldData);
          anot.setSuperType(t);
        }
      }
    }
    else {
      parseFormDataAnnotationRec(anot, type, true);
    }
    return anot;
  }

  /**
   * Gets the form data type that is referenced in the form data annotation of the given form.<br>
   * If the annotation does not exist or points to an inexistent form data type, null is returned.
   *
   * @param form
   *          the form for which the form data should be returned.
   * @return the form data type or null if it could not be found.
   * @throws JavaModelException
   */
  public static IType findDtoForForm(IType form) {
    if (form == null) {
      return null;
    }
    FormDataAnnotation a = findFormDataAnnotation(form);
    if (a != null) {
      return a.getFormDataType();
    }
    return null;
  }

  /**
   * Gets the page data type that is referenced in the page data annotation of the given page type.<br>
   * If the annotation does not exist or points to an inexistent page data type, null is returned.
   *
   * @param page
   *          the page for which the page data should be returned.
   * @return the page data class or null.
   * @throws JavaModelException
   */
  public static IType findDtoForPage(IType page) {
    if (page == null) {
      return null;
    }
    DataAnnotation anot = findDataAnnotation(page);
    if (anot != null && anot.getDataType() != null) {
      return anot.getDataType();
    }
    return null;
  }

  /**
   * Parses the possible available {@link IRuntimeClasses#PageData} or {@link IRuntimeClasses#Data} annotation on the
   * given type. If the type is not annotated, <code>null</code> is returned.
   *
   * @since 3.10.0-M1
   */
  public static DataAnnotation findDataAnnotation(IType type) {
    if (type == null) {
      return null;
    }

    IType dtoType = getDataAnnotationValue(type);
    if (dtoType == null) {
      return null;
    }

    IType superType = null;
    IType curType = type.getSuperClass();
    while (curType != null) {
      superType = getDataAnnotationValue(curType);
      if (superType != null) {
        break;
      }
      curType = curType.getSuperClass();
    }
    return new DataAnnotation(dtoType, superType, type);
  }

  /**
   * Checks whether the given type is annotated with a {@link IRuntimeClasses#Data} annotation and if so, this
   * method returns its <code>value()</code> as resolved type signature. Otherwise <code>null</code>.
   *
   * @since 3.10.0-M1
   */
  private static IType getDataAnnotationValue(IType type) {
    if (type == null) {
      return null;
    }

    IAnnotation annotation = CoreUtils.getAnnotation(type, IRuntimeClasses.Data);
    if (annotation == null) {
      annotation = CoreUtils.getAnnotation(type, IRuntimeClasses.PageData); // fall back to old name
      if (annotation == null) {
        return null;
      }
    }

    IType value = (IType) annotation.getValue("value").getValue();
    return value;
  }

  private static void parseFormDataAnnotationRec(FormDataAnnotation annotation, IType type, boolean isOwner) {
    if (type != null) {
      boolean replaceAnnotationPresent = ScoutUtils.existsReplaceAnnotation(type);
      IType superType = type.getSuperClass();

      parseFormDataAnnotationRec(annotation, superType, replaceAnnotationPresent);
      for (IType superInterface : type.getSuperInterfaces()) {
        parseFormDataAnnotationRec(annotation, superInterface, replaceAnnotationPresent);
      }

      if (replaceAnnotationPresent && superType != null && !ScoutUtils.existsReplaceAnnotation(superType)) {
        // super type is the original field that is going to be replaced by the given type
        // check whether the super type is embedded into a form field that is annotated by @FormData with SdkCommand.IGNORE.
        IType declaringType = superType.getDeclaringType();
        while (declaringType != null) {
          FormDataAnnotation declaringTypeformDataAnnotation = findFormDataAnnotation(declaringType);
          if (FormDataAnnotation.isIgnore(declaringTypeformDataAnnotation)) {
            // super type is embedded into a ignored form field. Hence this field is ignored as well. Adjust parsed annotation.
            annotation.setSdkCommand(SdkCommand.IGNORE);
            break;
          }
          declaringType = declaringType.getDeclaringType();
        }
      }

      // If a replace annotation is present, the original field defines the attributes of the form data. In that case these attributes can be ignored for a formData annotation on a level.
      // An exception are attributes that are cumulative and may be added on any level. Those may be added even though the @Replace annotation is available.
      // A field that is once marked so that a DTO should be created, can never be set to ignore again. But an ignored field may be changed to create. Afterwards it can never be set to ignore again.
      // Therefore ignored fields may define all attributes and they are inherited from the first level that declares it to be created.
      boolean cumulativeAttribsOnly = replaceAnnotationPresent && !FormDataAnnotation.isIgnore(annotation);

      fillFormDataAnnotation(type, annotation, isOwner, cumulativeAttribsOnly);
    }
  }

  private static void fillFormDataAnnotation(IAnnotatable element, FormDataAnnotation formDataAnnotation, boolean isOwner, boolean cumulativeAttributesOnly) {
    IAnnotation annotation = CoreUtils.getAnnotation(element, IRuntimeClasses.FormData);
    if (annotation == null) {
      return;
    }

    // find context type
    IType contextType = null;
    if (element instanceof IType) {
      contextType = (IType) element;
    }
    else if (element instanceof IMethod) {
      contextType = ((IMethod) element).getDeclaringType();
    }

    // value
    IAnnotationValue valueRaw = annotation.getValue("value");

    IType dtoType = null;
    if (valueRaw != null) {
      dtoType = (IType) valueRaw.getValue();
    }

    // sdk command
    SdkCommand sdkCommand = null;
    String sdkCommandRaw = CoreUtils.getAnnotationValueString(annotation, "sdkCommand");
    if (StringUtils.isNotBlank(sdkCommandRaw)) {
      sdkCommand = SdkCommand.valueOf(Signature.getSimpleName(sdkCommandRaw));
    }

    // subtype command
    DefaultSubtypeSdkCommand subTypeCommand = null;
    String defaultSubtypeSdkCommandRaw = CoreUtils.getAnnotationValueString(annotation, "defaultSubtypeSdkCommand");
    if (StringUtils.isNotBlank(defaultSubtypeSdkCommandRaw)) {
      subTypeCommand = DefaultSubtypeSdkCommand.valueOf(Signature.getSimpleName(defaultSubtypeSdkCommandRaw));
    }

    // generic ordinal
    int genericOrdinal = -1;
    IType genericOrdinalDefinitionType = null;
    BigDecimal go = CoreUtils.getAnnotationValueNumeric(annotation, "genericOrdinal");
    if (go != null) {
      genericOrdinal = go.intValue();
      genericOrdinalDefinitionType = contextType;
    }

    // interfaces
    List<IType> interfaceSignatures = null;
    IAnnotationValue interfacesRaw = annotation.getValue("interfaces");
    if (interfacesRaw != null) {
      if (interfacesRaw.getValueType() == ExpressionValueType.Type) {
        interfaceSignatures = new ArrayList<>(1);
        interfaceSignatures.add((IType) interfacesRaw.getValue());
      }
      else if (interfacesRaw.getValueType() == ExpressionValueType.Array) {
        IAnnotationValue[] interacesRaw = (IAnnotationValue[]) interfacesRaw.getValue();
        if (interacesRaw.length > 0) {
          interfaceSignatures = new ArrayList<>(interacesRaw.length);
          for (IAnnotationValue o : interacesRaw) {
            interfaceSignatures.add((IType) o.getValue());
          }
        }
      }
    }

    // default setup
    if (!cumulativeAttributesOnly) {
      if (dtoType != null) {
        if (isOwner) {
          formDataAnnotation.setFormDataType(dtoType);
        }
        else {
          formDataAnnotation.setSuperType(dtoType);
        }
      }
      if (isOwner && sdkCommand != null) {
        formDataAnnotation.setSdkCommand(sdkCommand);
      }
      if (subTypeCommand != null) {
        formDataAnnotation.setDefaultSubtypeSdkCommand(subTypeCommand);
      }
      if (genericOrdinal > -1) {
        formDataAnnotation.setGenericOrdinal(genericOrdinal);
        formDataAnnotation.setGenericOrdinalDefinitionType(genericOrdinalDefinitionType);
      }
    }

    // always add cumulative attributes
    formDataAnnotation.setAnnotationOwner(element);
    if (CollectionUtils.isNotEmpty(interfaceSignatures)) {
      formDataAnnotation.addInterfaces(interfaceSignatures);
    }

    // correction
    if (isOwner && sdkCommand == SdkCommand.USE && dtoType != null && element instanceof IType && ((IType) element).getDeclaringType() != null) {
      formDataAnnotation.setSuperType(dtoType);
      formDataAnnotation.setFormDataType(null);
      formDataAnnotation.setSdkCommand(SdkCommand.CREATE);
    }

    if (element instanceof IMethod && formDataAnnotation.getSdkCommand() == null) {
      formDataAnnotation.setSdkCommand(SdkCommand.CREATE);
    }
  }

  public static void addFormDataAdditionalInterfaces(FormDataAnnotation formDataAnnotation, ITypeSourceBuilder sourceBuilder, ILookupEnvironment context) {
    Set<IType> interfaces = formDataAnnotation.getInterfaceSignatures();
    if (interfaces.isEmpty()) {
      return;
    }

    Set<IType> allSuperInterfaces = new HashSet<>();
    for (IType ifcType : interfaces) {
      if (CoreUtils.isOnClasspath(ifcType, context)) {
        sourceBuilder.addInterfaceSignature(SignatureUtils.getResolvedSignature(ifcType));
        allSuperInterfaces.add(ifcType);
        allSuperInterfaces.addAll(CoreUtils.getAllSuperInterfaces(ifcType));
      }
    }
    Set<String> allSuperInterfaceMethods = new HashSet<>();
    for (IType t : allSuperInterfaces) {
      for (IMethod m : t.getMethods()) {
        allSuperInterfaceMethods.add(SignatureUtils.getMethodIdentifier(m));
      }
    }
    for (IMethodSourceBuilder msb : sourceBuilder.getMethodSourceBuilders()) {
      if (allSuperInterfaceMethods.contains(msb.getMethodIdentifier())) {
        msb.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOverrideAnnotationSourceBuilder());
      }
    }
  }

  public static StringBuilder createTableRowDataTypeSource(IType modelType, DataAnnotation dataAnnotation, ILookupEnvironment lookupEnvironment, String lineDelimiter, PropertyMap contextProperties) {
    if (dataAnnotation == null) {
      return null;
    }
    Validate.notNull(modelType);
    Validate.notNull(lookupEnvironment);

    String pckName = Signature.getQualifier(dataAnnotation.getDataType().getName());

    ITypeSourceBuilder rowDataSourceBuilder = createTableRowDataTypeSourceBuilder(modelType, dataAnnotation, lookupEnvironment);
    return createDtoSource(modelType, lineDelimiter, rowDataSourceBuilder, pckName, lookupEnvironment, contextProperties);
  }

  public static ITypeSourceBuilder createTableRowDataTypeSourceBuilder(IType modelType, DataAnnotation dataAnnotation, ILookupEnvironment lookupEnv) {
    String dataTypeName = dataAnnotation.getDataType().getSimpleName();

    ITypeSourceBuilder rowDataSourceBuilder = new TableRowDataTypeSourceBuilder(dataTypeName, modelType, modelType, lookupEnv);

    // primary class comment
    rowDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

    // @Extends annotation
    addDtoExtendsAnnotation(rowDataSourceBuilder, dataAnnotation.getAnnotationHolder());

    //@Generated annotation
    rowDataSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createGeneratedAnnotation("RowDataUpdateOperation", GENERATED_MSG));

    return rowDataSourceBuilder;
  }

  public static StringBuilder createPageDataSource(IType modelType, DataAnnotation dataAnnotation, ILookupEnvironment lookupEnvironment, String lineDelimiter, PropertyMap contextProperties) {
    if (dataAnnotation == null) {
      return null;
    }
    Validate.notNull(modelType);
    Validate.notNull(lookupEnvironment);

    String pckName = Signature.getQualifier(dataAnnotation.getDataType().getName());

    ITypeSourceBuilder pageDataSourceBuilder = createPageDataSourceBuilder(modelType, dataAnnotation, lookupEnvironment);
    return createDtoSource(modelType, lineDelimiter, pageDataSourceBuilder, pckName, lookupEnvironment, contextProperties);
  }

  public static ITypeSourceBuilder createPageDataSourceBuilder(IType modelType, DataAnnotation pageDataAnnotation, ILookupEnvironment lookupEnv) {
    ITypeSourceBuilder pageDataSourceBuilder = new TableBeanDataSourceBuilder(modelType, pageDataAnnotation, pageDataAnnotation.getDataType().getSimpleName(), lookupEnv);

    // primary class comment
    pageDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

    //@Generated annotation
    pageDataSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createGeneratedAnnotation("PageDataUpdateOperation", GENERATED_MSG));
    return pageDataSourceBuilder;
  }

  public static ITypeSourceBuilder createFormDataSourceBuilder(IType modelType, FormDataAnnotation formDataAnnotation, ILookupEnvironment lookupEnv) {
    IType superType = formDataAnnotation.getSuperType();
    if (superType != null) {
      ITypeSourceBuilder formDataSourceBuilder = null;
      if (CoreUtils.isInstanceOf(superType, IRuntimeClasses.AbstractTableFieldBeanData)) {
        // fill table bean
        formDataSourceBuilder = new TableFieldBeanFormDataSourceBuilder(modelType, formDataAnnotation, formDataAnnotation.getFormDataType().getSimpleName(), lookupEnv);
      }
      else {
        formDataSourceBuilder = new CompositeFormDataTypeSourceBuilder(modelType, formDataAnnotation, formDataAnnotation.getFormDataType().getSimpleName(), lookupEnv);
      }

      // primary class comment
      formDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

      // @Extends annotation
      addDtoExtendsAnnotation(formDataSourceBuilder, formDataAnnotation.getAnnotationOwner());

      // @Generated annotation
      formDataSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createGeneratedAnnotation("FormDataUpdateOperation", GENERATED_MSG));

      // add interfaces and @Override annotation for all methods that exist in the given interfaces
      addFormDataAdditionalInterfaces(formDataAnnotation, formDataSourceBuilder, lookupEnv);

      return formDataSourceBuilder;
    }
    return null;
  }

  public static StringBuilder createFormDataSource(IType modelType, FormDataAnnotation formDataAnnotation, ILookupEnvironment lookupEnvironment, String lineDelimiter, PropertyMap contextProperties) {
    if (!FormDataAnnotation.isCreate(formDataAnnotation)) {
      return null;
    }
    Validate.notNull(modelType);
    Validate.notNull(lookupEnvironment);

    String pckName = Signature.getQualifier(formDataAnnotation.getFormDataType().getName());
    ITypeSourceBuilder formDataSourceBuilder = DtoUtils.createFormDataSourceBuilder(modelType, formDataAnnotation, lookupEnvironment);
    return createDtoSource(modelType, lineDelimiter, formDataSourceBuilder, pckName, lookupEnvironment, contextProperties);
  }

  private static StringBuilder createDtoSource(IType modelType, String lineDelimiter, ITypeSourceBuilder dtoSourceBuilder, String targetPackage, ILookupEnvironment lookupEnvironment, PropertyMap contextProperties) {
    if (lineDelimiter == null) {
      lineDelimiter = System.getProperty("line.separator");
    }

    CompilationUnitSourceBuilder dtoCuSourceBuilder = new CompilationUnitSourceBuilder(dtoSourceBuilder.getElementName(), targetPackage);
    dtoCuSourceBuilder.addTypeSourceBuilder(dtoSourceBuilder);
    dtoCuSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());

    ImportValidator validator = new ImportValidator(targetPackage, lookupEnvironment);

    // loop through all types recursively to ensure all simple names that will be created are "consumed" in the import validator
    consumeAllTypeNamesRec(dtoSourceBuilder, validator);

    StringBuilder sourceBuilder = new StringBuilder();
    dtoCuSourceBuilder.createSource(sourceBuilder, lineDelimiter, contextProperties, validator);
    return sourceBuilder;
  }

  private static void consumeAllTypeNamesRec(ITypeSourceBuilder builder, IImportValidator validator) {
    String fqn = builder.getFullyQualifiedName();
    validator.getTypeName(Signature.createTypeSignature(fqn));
    for (ITypeSourceBuilder child : builder.getTypeSourceBuilder()) {
      consumeAllTypeNamesRec(child, validator);
    }
  }

  private static void addDtoExtendsAnnotation(ITypeSourceBuilder target, IAnnotatable extendsAnnotationHolder) {
    AnnotationSourceBuilder extendsAnnotation = getExtendsAnnotationSourceBuilder(extendsAnnotationHolder);
    if (extendsAnnotation != null) {
      target.addAnnotationSourceBuilder(extendsAnnotation);
    }
  }

  private static AnnotationSourceBuilder getExtendsAnnotationSourceBuilder(IAnnotatable element) {
    if (element instanceof IType) {
      IType extendedType = getExtendedType((IType) element);
      if (extendedType != null) {
        IType primaryType = CoreUtils.getPrimaryType(extendedType);
        IType extendedDto = null;
        if (CoreUtils.isInstanceOf(primaryType, IRuntimeClasses.IForm) || CoreUtils.isInstanceOf(primaryType, IRuntimeClasses.IFormField)) {
          extendedDto = findDtoForForm(primaryType);
        }
        else if (CoreUtils.isInstanceOf(primaryType, IRuntimeClasses.IPageWithTable)) {
          IType pageDto = findDtoForPage(primaryType);

          ListOrderedSet<IType> rowDataInPageDto = CoreUtils.getInnerTypes(pageDto, TypeFilters.getSubtypeFilter(IRuntimeClasses.AbstractTableRowData));
          extendedDto = null;
          if (CollectionUtils.isNotEmpty(rowDataInPageDto)) {
            extendedDto = rowDataInPageDto.get(0);
          }
        }

        if (extendedDto != null) {
          final IType dto = extendedDto;
          AnnotationSourceBuilder asb = new AnnotationSourceBuilder(Signature.createTypeSignature(IRuntimeClasses.Extends)) {
            @Override
            public void createSource(StringBuilder source, String lineDelimiter, PropertyMap ownerProject, IImportValidator validator) {
              String typeName = validator.getTypeName(SignatureUtils.getResolvedSignature(dto));
              addParameter(typeName + ".class");
              super.createSource(source, lineDelimiter, ownerProject, validator);
            }
          };
          return asb;
        }
      }
    }
    return null;
  }

  public static IType findExtendsAnnotationValue(IType element) {
    IType curType = element;
    while (curType != null) {
      IAnnotation extendsAnnotation = CoreUtils.getAnnotation(curType, IRuntimeClasses.Extends);
      if (extendsAnnotation != null) {
        IAnnotationValue value = extendsAnnotation.getValue("value");
        if (value != null) {
          return (IType) value.getValue();
        }
      }
      curType = curType.getSuperClass();
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
   * @throws CoreException
   */
  public static IType getExtendedType(IType modelType) {
    // 1. try to read from @Extends annotation
    IType extendsValue = findExtendsAnnotationValue(modelType);
    if (extendsValue != null) {
      return extendsValue;
    }

    // 2. try to read from generic
    boolean isExtension = CoreUtils.isInstanceOf(modelType, IRuntimeClasses.IExtension);
    if (isExtension) {
      ListOrderedSet<IType> owner = CoreUtils.getResolvedTypeParamValue(modelType, IRuntimeClasses.IExtension, IRuntimeClasses.TYPE_PARAM_EXTENSION__OWNER);
      if (CollectionUtils.isNotEmpty(owner)) {
        return owner.get(0);
      }
    }

    // 3. try in declaring type
    IType declaringType = modelType.getDeclaringType();
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