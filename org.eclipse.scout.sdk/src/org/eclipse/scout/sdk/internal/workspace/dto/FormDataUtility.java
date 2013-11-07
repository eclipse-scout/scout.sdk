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
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.CompositeFormDataTypeSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.TableFieldBeanFormDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.TableFieldFormDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.pagedata.PageDataSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.method.MethodReturnExpression;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.validationrule.ValidationRuleMethod;

/**
 * <h3>{@link FormDataUtility}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public final class FormDataUtility {

  private static final Pattern VALIDATION_RULE_PATTERN = Pattern.compile("[@]ValidationRule\\s*[(]\\s*([^)]*value\\s*=)?\\s*([^,)]+)([,][^)]*)?[)]", Pattern.DOTALL);

  private FormDataUtility() {
  }

  public static ITypeSourceBuilder createFormDataSourceBuilder(IType modelType, FormDataAnnotation formDataAnnotation) {
    String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
    if (StringUtility.hasText(superTypeSignature)) {
      IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
      String typeErasure = Signature.getTypeErasure(superTypeSignature);
      ITypeHierarchy superTypeHierarchy = TypeUtility.getSuperTypeHierarchy(superType);
      String formDataTypeSignature = formDataAnnotation.getFormDataTypeSignature();
      String formDataTypeName = Signature.getSignatureSimpleName(formDataTypeSignature);
      ITypeSourceBuilder formDataSourceBuilder = null;
      if (SignatureUtility.isEqualSignature(typeErasure, SignatureCache.createTypeSignature(RuntimeClasses.AbstractTableFieldData))) {
        formDataSourceBuilder = new TableFieldFormDataSourceBuilder(modelType, formDataTypeName, formDataAnnotation);
      }
      else if (superTypeHierarchy != null && superTypeHierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractTableFieldBeanData))) {
        // fill table bean
        formDataSourceBuilder = new TableFieldBeanFormDataSourceBuilder(modelType, formDataTypeName, formDataAnnotation);
      }
      else {
        formDataSourceBuilder = new CompositeFormDataTypeSourceBuilder(modelType, formDataTypeName, formDataAnnotation);
      }
      formDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("<b>NOTE:</b><br>This class is auto generated, no manual modifications recommended.\n\n@generated"));
      return formDataSourceBuilder;
    }
    return null;
  }

  public static ITypeSourceBuilder createPageDataSourceBuilder(IType modelType, PageDataAnnotation pageDataAnnotation) {
    String pageDataSignature = pageDataAnnotation.getPageDataTypeSignature();
    ITypeSourceBuilder pageDataSourceBuilder = new PageDataSourceBuilder(modelType, Signature.getSignatureSimpleName(pageDataSignature), pageDataAnnotation);
    pageDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("<b>NOTE:</b><br>This class is auto generated, no manual modifications recommended.\n\n@generated"));
    return pageDataSourceBuilder;
  }

  public static String getFormDataName(String typeName) {
    String formDataName = typeName;
    if (typeName.endsWith("Field")) {
      formDataName = typeName.replaceAll("Field$", "");
    }
    else if (typeName.endsWith("Button")) {
      formDataName = typeName.replaceAll("Button$", "");
    }
    else if (typeName.endsWith("Column")) {
      formDataName = typeName.replaceAll("Column$", "");
    }
    String resultName = formDataName;
//    int i = 0;
//    if (operation != null) {
//      while (operation.getTypeNewOperation(resultName) != null) {
//        resultName = formDataName + i;
//        i++;
//      }
//    }
    return resultName;
  }

  public static IType findTable(IType tableOwner, ITypeHierarchy hierarchy) {

    if (TypeUtility.exists(tableOwner)) {
      if (hierarchy == null) {
        hierarchy = TypeUtility.getLocalTypeHierarchy(tableOwner);
      }
      IType[] tables = TypeUtility.getInnerTypes(tableOwner, TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ITable), hierarchy), null);
      if (tables.length > 0) {
        if (tables.length > 1) {
          ScoutSdk.logWarning("table field '" + tableOwner.getFullyQualifiedName() + "' contains more than one table! Taking first for dto creation.");
        }
        return tables[0];
      }
      else {
        IType superclass = hierarchy.getSuperclass(tableOwner);

        return findTable(superclass, null);
      }
    }
    return null;
  }

  public static String computeSuperTypeSignatureForFormData(IType formField, FormDataAnnotation formDataAnnotation, ITypeHierarchy localHierarchy) {
    String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
      if (TypeUtility.isGenericType(superType)) {
        try {
          String genericTypeSig = ScoutTypeUtility.computeFormFieldGenericType(formField, localHierarchy);
          if (genericTypeSig != null) {
            superTypeSignature = superTypeSignature.replaceAll("\\;$", "<" + genericTypeSig + ">;");
          }
        }
        catch (CoreException e) {
          ScoutSdk.logError("could not find generic type for form data of type '" + formField.getFullyQualifiedName() + "'.");
        }
      }

    }
    return superTypeSignature;
  }

  public static List<ValidationRuleMethod> getValidationRuleMethods(IType declaringType, org.eclipse.jdt.core.ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    IType validationRuleType = TypeUtility.getType(RuntimeClasses.ValidationRule);
    TreeMap<String, ValidationRuleMethod> ruleMap = new TreeMap<String, ValidationRuleMethod>();
    if (superTypeHierarchy == null) {
      try {
        superTypeHierarchy = declaringType.newSupertypeHierarchy(null);
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not build super type hierarchy for '" + declaringType.getFullyQualifiedName() + "'.", e);
        return Collections.emptyList();
      }
    }

    ArrayList<IType> targetTypeList = new ArrayList<IType>(5);
    targetTypeList.add(0, declaringType);
    IType[] superClasses = superTypeHierarchy.getAllSuperclasses(declaringType);
    for (IType t : superClasses) {
      if (TypeUtility.exists(t) && !t.getFullyQualifiedName().equals(Object.class.getName())) {
        targetTypeList.add(t);
      }
    }
    IType[] targetTypes = targetTypeList.toArray(new IType[targetTypeList.size()]);
    IMethod[][] targetMethods = new IMethod[targetTypes.length][];
    for (int i = 0; i < targetTypes.length; i++) {
      targetMethods[i] = targetTypes[i].getMethods();
    }

    HashSet<String> visitedMethodNames = new HashSet<String>();
    for (int i = 0; i < targetTypes.length; i++) {
      for (IMethod annotatedMethod : targetMethods[i]) {
        if (!TypeUtility.exists(annotatedMethod)) {
          continue;
        }
        if (visitedMethodNames.contains(annotatedMethod.getElementName())) {
          continue;
        }
        IAnnotation validationRuleAnnotation = JdtUtility.getAnnotation(annotatedMethod, RuntimeClasses.ValidationRule);
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
            IType fieldBaseType = ScoutUtility.getReferencedType(annotatedMethod.getDeclaringType(), fieldSource.substring(0, lastDot));

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
}
