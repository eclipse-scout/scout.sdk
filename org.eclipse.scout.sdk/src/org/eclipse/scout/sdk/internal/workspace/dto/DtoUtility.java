/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.CompositeFormDataTypeSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.TableFieldBeanFormDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.TableFieldFormDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.pagedata.TableBeanDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.pagedata.TableRowDataTypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.method.MethodReturnExpression;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.ITypeParameterMapping;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.DataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.validationrule.ValidationRuleMethod;

/**
 * <h3>{@link DtoUtility}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public final class DtoUtility {

  private static final Pattern VALIDATION_RULE_PATTERN = Pattern.compile("[@]ValidationRule\\s*[(]\\s*([^)]*value\\s*=)?\\s*([^,)]+)([,][^)]*)?[)]", Pattern.DOTALL);
  static final Pattern ENDING_SEMICOLON_PATTERN = Pattern.compile("\\;$");
  private static final String GENERATED_MSG = "This class is auto generated by the Scout SDK. No manual modifications recommended.";
  private static final String GENERATED_JAVADOC = "<b>NOTE:</b><br>" + GENERATED_MSG + "\n\n@generated";

  private DtoUtility() {
  }

  private static AnnotationSourceBuilder getExtendsAnnotationSourceBuilder(IJavaElement element, ITypeHierarchy modelLocalHierarchy) throws CoreException {
    if (element instanceof IType) {
      IType extendedType = getExtendedType((IType) element, modelLocalHierarchy);
      if (extendedType != null) {
        IType primaryType = TypeUtility.getPrimaryType(extendedType);
        ITypeHierarchy extendedTypeSuperHierarchy = TypeUtility.getSupertypeHierarchy(primaryType);
        if (extendedTypeSuperHierarchy != null) {
          IType extendedDto = null;
          if (extendedTypeSuperHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IForm)) || extendedTypeSuperHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormField))) {
            extendedDto = ScoutTypeUtility.findDtoForForm(primaryType);
          }
          else if (extendedTypeSuperHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IPageWithTable))) {
            IType pageDto = ScoutTypeUtility.findDtoForPage(primaryType);

            Set<IType> rowDataInPageDto = TypeUtility.getInnerTypes(pageDto, TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.AbstractTableRowData)));
            extendedDto = CollectionUtility.firstElement(rowDataInPageDto);
          }

          if (extendedDto != null) {
            final IType dto = extendedDto;
            AnnotationSourceBuilder asb = new AnnotationSourceBuilder(SignatureCache.createTypeSignature(IRuntimeClasses.Extends)) {
              @Override
              public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
                String typeName = validator.getTypeName(SignatureCache.createTypeSignature(dto.getFullyQualifiedName()));
                addParameter(typeName + ".class");
                super.createSource(source, lineDelimiter, ownerProject, validator);
              }
            };
            return asb;
          }
        }
      }
    }
    return null;
  }

  private static IType getExtendedType(IType modelType, ITypeHierarchy localHierarchy) throws CoreException {
    IType iExtension = TypeUtility.getType(IRuntimeClasses.IExtension);
    boolean isExtension = TypeUtility.exists(iExtension) && localHierarchy.isSubtype(iExtension, modelType);
    if (isExtension) {
      // 1. try to read from generic
      String ownerSignature = SignatureUtility.resolveTypeParameter(modelType, localHierarchy, IRuntimeClasses.IExtension, IRuntimeClasses.TYPE_PARAM_EXTENSION__OWNER);
      if (ownerSignature != null) {
        return TypeUtility.getTypeBySignature(ownerSignature);
      }
    }

    // 2. try to read from @Extends annotation
    String extendsSignature = ScoutTypeUtility.findExtendsAnnotationSignature(modelType, localHierarchy);
    if (extendsSignature != null) {
      return TypeUtility.getTypeBySignature(extendsSignature);
    }

    // 3. try in declaring type
    IType declaringType = modelType.getDeclaringType();
    if (TypeUtility.exists(declaringType)) {
      IType extendsFromDeclaringType = getExtendedType(declaringType, localHierarchy);
      if (extendsFromDeclaringType != null) {
        return extendsFromDeclaringType;
      }
    }

    // 4. if the model class has no annotation and is not an extension
    //    this can happen if e.g. a formfield is explicitly registered on the ExtensionRegistry.
    //    in this case we cannot detect anything

    return null;
  }

  public static void addFormDataAdditionalInterfaces(FormDataAnnotation formDataAnnotation, ITypeSourceBuilder sourceBuilder, IJavaProject formDataJavaProject) {
    Set<String> interfaceSignatures = formDataAnnotation.getInterfaceSignatures();
    if (interfaceSignatures.isEmpty()) {
      return;
    }

    Set<IType> allSuperInterfaces = new HashSet<IType>();
    for (String ifcSig : interfaceSignatures) {
      IType ifcType = TypeUtility.getTypeBySignature(ifcSig);

      if (TypeUtility.isOnClasspath(ifcType, formDataJavaProject)) {
        sourceBuilder.addInterfaceSignature(ifcSig);
        allSuperInterfaces.addAll(TypeUtility.getSupertypeHierarchy(ifcType).getAllInterfaces());
      }
    }
    Set<String> allSuperInterfaceMethods = new HashSet<String>();
    try {
      for (IType t : allSuperInterfaces) {
        for (IMethod m : t.getMethods()) {
          allSuperInterfaceMethods.add(SignatureUtility.getMethodIdentifier(m));
        }
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("Unable to read existing methods from super interfaces of formdata for '" + formDataAnnotation.getAnnotationOwner().getElementName() + "'. The resulting formdata may miss some @Override annotations.", e);
    }
    for (IMethodSourceBuilder msb : sourceBuilder.getMethodSourceBuilders()) {
      if (allSuperInterfaceMethods.contains(msb.getMethodIdentifier())) {
        msb.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOverrideAnnotationSourceBuilder());
      }
    }
  }

  public static ITypeSourceBuilder createTableRowDataTypeSourceBuilder(IType modelType, DataAnnotation dataAnnotation, IProgressMonitor monitor) throws CoreException {
    String dataTypeName = Signature.getSignatureSimpleName(dataAnnotation.getDataTypeSignature());
    ITypeHierarchy modelLocalHierarchy = TypeUtility.getLocalTypeHierarchy(modelType);

    ITypeSourceBuilder rowDataSourceBuilder = new TableRowDataTypeSourceBuilder(dataTypeName, modelType, modelType, modelLocalHierarchy, monitor);

    // primary class comment
    rowDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

    // @Extends annotation
    addDtoExtendsAnnotation(rowDataSourceBuilder, dataAnnotation.getAnnotationHolder(), modelLocalHierarchy);

    //@Generated annotation
    rowDataSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createGeneratedAnnotation(PageDataDtoUpdateOperation.class.getName(), GENERATED_MSG));

    return rowDataSourceBuilder;
  }

  public static ITypeSourceBuilder createPageDataSourceBuilder(IType modelType, DataAnnotation pageDataAnnotation, ICompilationUnit pageDataIcu, IProgressMonitor monitor) {
    String pageDataSignature = pageDataAnnotation.getDataTypeSignature();
    ITypeHierarchy modelLocalHierarchy = TypeUtility.getLocalTypeHierarchy(modelType);

    ITypeSourceBuilder pageDataSourceBuilder = new TableBeanDataSourceBuilder(modelType, modelLocalHierarchy, Signature.getSignatureSimpleName(pageDataSignature), pageDataAnnotation, pageDataIcu, monitor);

    // primary class comment
    pageDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

    //@Generated annotation
    pageDataSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createGeneratedAnnotation(PageDataDtoUpdateOperation.class.getName(), GENERATED_MSG));
    return pageDataSourceBuilder;
  }

  public static ITypeSourceBuilder createFormDataSourceBuilder(IType modelType, FormDataAnnotation formDataAnnotation, ICompilationUnit formDataIcu, IProgressMonitor monitor) {
    String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
    if (StringUtility.hasText(superTypeSignature)) {
      IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
      ITypeHierarchy superTypeHierarchy = TypeUtility.getSupertypeHierarchy(superType);
      ITypeHierarchy modelLocalHierarchy = TypeUtility.getLocalTypeHierarchy(modelType);
      String formDataTypeName = Signature.getSignatureSimpleName(formDataAnnotation.getFormDataTypeSignature());

      ITypeSourceBuilder formDataSourceBuilder = null;
      if (superTypeHierarchy != null && superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.AbstractTableFieldData))) {
        // fill table legacy
        formDataSourceBuilder = new TableFieldFormDataSourceBuilder(modelType, modelLocalHierarchy, formDataTypeName, formDataAnnotation, formDataIcu, monitor);
      }
      else if (superTypeHierarchy != null && superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.AbstractTableFieldBeanData))) {
        // fill table bean
        formDataSourceBuilder = new TableFieldBeanFormDataSourceBuilder(modelType, modelLocalHierarchy, formDataTypeName, formDataAnnotation, formDataIcu, monitor);
      }
      else {
        formDataSourceBuilder = new CompositeFormDataTypeSourceBuilder(modelType, modelLocalHierarchy, formDataTypeName, formDataAnnotation, formDataIcu, monitor);
      }

      // primary class comment
      formDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder(GENERATED_JAVADOC));

      // @Extends annotation
      addDtoExtendsAnnotation(formDataSourceBuilder, formDataAnnotation.getAnnotationOwner(), modelLocalHierarchy);

      // @Generated annotation
      formDataSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createGeneratedAnnotation(FormDataDtoUpdateOperation.class.getName(), GENERATED_MSG));

      // add interfaces and @Override annotation for all methods that exist in the given interfaces
      addFormDataAdditionalInterfaces(formDataAnnotation, formDataSourceBuilder, formDataIcu.getJavaProject());

      return formDataSourceBuilder;
    }
    return null;
  }

  private static void addDtoExtendsAnnotation(ITypeSourceBuilder target, IJavaElement extendsAnnotationHolder, ITypeHierarchy modelLocalHierarchy) {
    if (TypeUtility.existsType(IRuntimeClasses.Extends)) {
      try {
        AnnotationSourceBuilder extendsAnnotation = getExtendsAnnotationSourceBuilder(extendsAnnotationHolder, modelLocalHierarchy);
        if (extendsAnnotation != null) {
          target.addAnnotationSourceBuilder(extendsAnnotation);
        }
      }
      catch (CoreException e) {
        ScoutSdk.logError("Unable to calculate @Extends annotation value of owner '" + extendsAnnotationHolder.getElementName() + "'.", e);
      }
    }
  }

  public static IType findTable(IType tableOwner, ITypeHierarchy hierarchy) {
    if (TypeUtility.exists(tableOwner)) {
      if (hierarchy == null) {
        hierarchy = TypeUtility.getLocalTypeHierarchy(tableOwner);
      }
      Set<IType> tables = TypeUtility.getInnerTypes(tableOwner, TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ITable), hierarchy), null);
      if (tables.size() > 0) {
        if (tables.size() > 1) {
          ScoutSdk.logWarning("table field '" + tableOwner.getFullyQualifiedName() + "' contains more than one table! Using first one for DTO creation.");
        }
        return CollectionUtility.firstElement(tables);
      }
      else {
        IType superclass = hierarchy.getSuperclass(tableOwner);
        return findTable(superclass, null);
      }
    }
    return null;
  }

  private static String computeDtoGenericType(IType contextType, IType annotationOwnerType, int genericOrdinal, ITypeHierarchy formFieldHierarchy) throws CoreException {
    if (!TypeUtility.exists(contextType) || contextType.getFullyQualifiedName().equals(Object.class.getName()) || !TypeUtility.exists(annotationOwnerType)) {
      return null;
    }

    Map<String, ITypeParameterMapping> collector = SignatureUtility.resolveTypeParameters(contextType, formFieldHierarchy);

    boolean annotOwnerPassed = false;
    for (Entry<String, ITypeParameterMapping> entry : collector.entrySet()) {
      if (!annotOwnerPassed && CompareUtility.equals(annotationOwnerType.getFullyQualifiedName(), entry.getKey())) {
        annotOwnerPassed = true;
      }

      if (annotOwnerPassed) {
        ITypeParameterMapping genericMapping = entry.getValue();
        if (genericMapping.getParameterCount() > genericOrdinal) {
          return CollectionUtility.firstElement(genericMapping.getTypeParameterBounds(genericOrdinal));
        }
      }
    }
    return null;
  }

  public static String computeSuperTypeSignatureForFormData(IType formField, FormDataAnnotation formDataAnnotation, ITypeHierarchy localHierarchy) {
    String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      IType genericOrdinalDefinitionType = formDataAnnotation.getGenericOrdinalDefinitionType();
      if (TypeUtility.exists(genericOrdinalDefinitionType)) {
        IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
        if (TypeUtility.isGenericType(superType)) {
          try {
            String genericTypeSig = computeDtoGenericType(formField, genericOrdinalDefinitionType, formDataAnnotation.getGenericOrdinal(), localHierarchy);
            if (genericTypeSig != null) {
              superTypeSignature = ENDING_SEMICOLON_PATTERN.matcher(superTypeSignature).replaceAll(Signature.C_GENERIC_START + genericTypeSig + Signature.C_GENERIC_END + Signature.C_SEMICOLON);
            }
          }
          catch (CoreException e) {
            ScoutSdk.logError("could not find generic type for form data of type '" + formField.getFullyQualifiedName() + "'.", e);
          }
        }
      }
    }
    return superTypeSignature;
  }

  public static List<ValidationRuleMethod> getValidationRuleMethods(IType declaringType, ITypeHierarchy superTypeHierarchy, IProgressMonitor monitor) throws JavaModelException {
    IType validationRuleType = TypeUtility.getType(IRuntimeClasses.ValidationRule);
    TreeMap<String, ValidationRuleMethod> ruleMap = new TreeMap<String, ValidationRuleMethod>();
    if (superTypeHierarchy == null) {
      superTypeHierarchy = TypeUtility.getSupertypeHierarchy(declaringType);
      if (superTypeHierarchy == null) {
        ScoutSdk.logWarning("could not build super type hierarchy for '" + declaringType.getFullyQualifiedName() + "'.");
        return Collections.emptyList();
      }
    }

    Deque<IType> superClassStack = superTypeHierarchy.getSuperClassStack(declaringType);
    IType[] targetTypes = superClassStack.toArray(new IType[superClassStack.size()]);
    IMethod[][] targetMethods = new IMethod[targetTypes.length][];
    for (int i = 0; i < targetTypes.length; i++) {
      targetMethods[i] = targetTypes[i].getMethods();
    }
    if (monitor.isCanceled()) {
      return null;
    }

    HashSet<String> visitedMethodNames = new HashSet<String>();
    for (int i = 0; i < targetTypes.length; i++) {
      for (IMethod annotatedMethod : targetMethods[i]) {
        if (monitor.isCanceled()) {
          return null;
        }

        if (!TypeUtility.exists(annotatedMethod)) {
          continue;
        }
        if (visitedMethodNames.contains(annotatedMethod.getElementName())) {
          continue;
        }
        IAnnotation validationRuleAnnotation = JdtUtility.getAnnotation(annotatedMethod, IRuntimeClasses.ValidationRule);
        if (!TypeUtility.exists(validationRuleAnnotation)) {
          continue;
        }

        //extract rule name and generated code order
        visitedMethodNames.add(annotatedMethod.getElementName());
        IMemberValuePair[] pairs = validationRuleAnnotation.getMemberValuePairs();
        if (pairs == null) {
          continue;
        }
        String ruleString = null;
        Boolean ruleSkip = false;
        MethodReturnExpression methodReturnExpression = null;
        for (IMemberValuePair pair : pairs) {
          if ("value".equals(pair.getMemberName())) {
            if (pair.getValue() instanceof String) {
              ruleString = (String) pair.getValue();
            }
          }
          else if ("generatedSourceCode".equals(pair.getMemberName())) {
            if (pair.getValue() instanceof String) {
              methodReturnExpression = new MethodReturnExpression();
              methodReturnExpression.setReturnClause((String) pair.getValue());
            }
          }
          else if ("skip".equals(pair.getMemberName())) {
            if (pair.getValue() instanceof Boolean) {
              ruleSkip = (Boolean) pair.getValue();
            }
          }
        }
        if (ruleString == null) {
          continue;
        }
        //find out the annotated source code field name (constant declaration)
        //this is either ValidationRule(value=text ) or simply ValidationRule(text)
        IField ruleField = null;
        Matcher annotationMatcher = VALIDATION_RULE_PATTERN.matcher("" + annotatedMethod.getSource());
        if (annotationMatcher.find()) {
          String fieldSource = annotationMatcher.group(2).trim();
          int lastDot = fieldSource.lastIndexOf('.');
          //fast check if scout rule
          if (fieldSource.startsWith("ValidationRule")) {
            if (TypeUtility.exists(validationRuleType)) {
              ruleField = validationRuleType.getField(fieldSource.substring(lastDot + 1));
              if (!TypeUtility.exists(ruleField)) {
                ruleField = null;
              }
            }
          }
          else if (!fieldSource.startsWith("\"") && lastDot > 0) {
            IType fieldBaseType = TypeUtility.getReferencedType(annotatedMethod.getDeclaringType(), fieldSource.substring(0, lastDot), false);

            if (fieldBaseType != null) {
              ruleField = fieldBaseType.getField(fieldSource.substring(lastDot + 1));
              if (!TypeUtility.exists(ruleField)) {
                ruleField = null;
              }
            }
          }
        }
        if (ruleField != null) {
          Object val = TypeUtility.getFieldConstant(ruleField);
          if (val instanceof String) {
            ruleString = (String) val;
          }
        }
        String hashKey = ruleString;
        if (ruleMap.containsKey(hashKey)) {
          continue;
        }
        //found new rule annotation, now find most specific method in subclasses to generate source code
        IMethod implementedMethod = null;
        if (methodReturnExpression == null) {
          for (int k = 0; k < i; k++) {
            for (IMethod tst : targetMethods[k]) {
              if (!TypeUtility.exists(tst)) {
                continue;
              }
              if (tst.getElementName().equals(annotatedMethod.getElementName())) {
                implementedMethod = tst;
                break;
              }
            }
            if (implementedMethod != null) {
              break;
            }
          }
          if (implementedMethod == null) {
            implementedMethod = annotatedMethod;
          }
          //found most specific override of new rule
          methodReturnExpression = ScoutUtility.getMethodReturnExpression(implementedMethod);
        }
        else {
          implementedMethod = annotatedMethod;
        }
        //new rule is sufficiently parsed
        ValidationRuleMethod vm = new ValidationRuleMethod(validationRuleAnnotation, ruleField, ruleString, methodReturnExpression, annotatedMethod, implementedMethod, superTypeHierarchy, ruleSkip);
        ruleMap.put(hashKey, vm);
      }
    }

    ArrayList<ValidationRuleMethod> list = new ArrayList<ValidationRuleMethod>(ruleMap.size());
    for (ValidationRuleMethod v : ruleMap.values()) {
      if (v != null) {
        list.add(v);
      }
    }
    return list;
  }

  /**
   * @return Returns the form field data for the given form field or <code>null</code> if it does not have one.
   * @since 3.8.2
   */
  public static IType getFormDataType(IType formField, ITypeHierarchy hierarchy) throws JavaModelException {
    IType primaryType = getFormFieldDataPrimaryTypeRec(formField, hierarchy);
    if (!TypeUtility.exists(primaryType)) {
      return null;
    }

    // check if the primary type itself is the correct type
    String formDataName = ScoutUtility.removeFieldSuffix(formField.getElementName());
    if (primaryType.getElementName().equals(formDataName)) {
      return primaryType;
    }

    // search field data within form data
    IType formDataType = TypeUtility.findInnerType(primaryType, formDataName);
    if (TypeUtility.exists(formDataType)) {
      return formDataType;
    }
    return null;
  }

  /**
   * @return Returns the form field data for the given form field or <code>null</code> if it does not have one. The
   *         method walks recursively through the list of declaring classes until it has reached a primary type.
   * @since 3.8.2
   */
  private static IType getFormFieldDataPrimaryTypeRec(IType recursiveDeclaringType, ITypeHierarchy hierarchy) throws JavaModelException {
    if (!TypeUtility.exists(recursiveDeclaringType)) {
      return null;
    }
    FormDataAnnotation formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(recursiveDeclaringType, hierarchy);
    if (formDataAnnotation == null) {
      return null;
    }
    if (FormDataAnnotation.isIgnore(formDataAnnotation)) {
      return null;
    }

    IType declaringType = recursiveDeclaringType.getDeclaringType();
    if (declaringType == null) {
      // primary type
      if (FormDataAnnotation.isSdkCommandCreate(formDataAnnotation) || FormDataAnnotation.isSdkCommandUse(formDataAnnotation)) {
        return TypeUtility.getTypeBySignature(formDataAnnotation.getFormDataTypeSignature());
      }
      return null;
    }

    return getFormFieldDataPrimaryTypeRec(declaringType, hierarchy);
  }
}
