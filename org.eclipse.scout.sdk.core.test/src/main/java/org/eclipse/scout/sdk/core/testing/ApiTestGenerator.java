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
package org.eclipse.scout.sdk.core.testing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IAnnotation;
import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IMethodParameter;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;

/**
 * <h3>{@link ApiTestGenerator}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 26.08.2013
 */
public class ApiTestGenerator {
  public static final String NL = "\n";

  private IType m_element;
  private Set<String> m_usedMemberNames;

  public ApiTestGenerator(IType element) {
    m_element = element;
    m_usedMemberNames = new HashSet<>();
  }

  public String buildSource() {
    IImportValidator validator = new ImportValidator();
    StringBuilder sourceBuilder = new StringBuilder();
    String typeVarName = getMemberName(m_element.getSimpleName());
    String sdkAssertRef = validator.getTypeName(Signature.createTypeSignature(SdkAssert.class.getName(), true));
    String assertRef = validator.getTypeName(Signature.createTypeSignature(Assert.class.getName(), true));
    String iTypeRef = validator.getTypeName(Signature.createTypeSignature(IType.class.getName(), true));

    sourceBuilder.append("/**").append(NL);
    sourceBuilder.append("* @Generated with ").append(getClass().getName()).append(NL);
    sourceBuilder.append("*/").append(NL);

    sourceBuilder.append("private static void testApiOf").append(m_element.getSimpleName()).append("(").append("IType ").append(typeVarName).append(") {").append(NL);
    buildType(m_element, typeVarName, sourceBuilder, validator, sdkAssertRef, assertRef, iTypeRef);
    sourceBuilder.append("}");
    //
    StringBuilder result = new StringBuilder();
    for (String imp : validator.getImportsToCreate()) {
      result.append(imp).append(NL);
    }
    result.append(sourceBuilder.toString());
    return result.toString();
  }

  protected void buildType(IType type, String typeVarName, StringBuilder source, IImportValidator validator, String sdkAssertRef, String assertRef, String iTypeRef) {

    source.append(sdkAssertRef).append(".assertHasFlags(").append(typeVarName).append(", ").append(type.getFlags()).append(");").append(NL);
    // super type
    String superClassSig = SignatureUtils.getResolvedSignature(type.getSuperClass());
    if (StringUtils.isNotEmpty(superClassSig)) {
      source.append(sdkAssertRef).append(".assertHasSuperTypeSignature(").append(typeVarName).append(", \"").append(superClassSig).append("\");").append(NL);
    }
    // interfaces
    ListOrderedSet<IType> interfaces = type.getSuperInterfaces();
    if (interfaces.size() > 0) {
      source.append(sdkAssertRef).append(".assertHasSuperIntefaceSignatures(").append(typeVarName).append(", new String[]{");
      for (int i = 0; i < interfaces.size(); i++) {
        source.append("\"").append(SignatureUtils.getResolvedSignature(interfaces.get(i))).append("\"");
        if (i < interfaces.size() - 1) {
          source.append(", ");
        }
      }
      source.append("});").append(NL);
    }
    createAnnotationsAsserts(type, type, source, typeVarName, sdkAssertRef);
    source.append(NL);

    // fields
    source.append("// fields of ").append(type.getSimpleName()).append(NL);
    String iFieldRef = validator.getTypeName(Signature.createTypeSignature(IField.class.getName(), true));
    ListOrderedSet<IField> fields = type.getFields();
    source.append(assertRef).append(".assertEquals(\"field count of '").append(type.getName()).append("'\", ").append(Integer.toString(fields.size())).append(", ").append(typeVarName).append(".getFields().size());").append(NL);
    for (IField f : fields) {
      String fieldVarName = getMemberName(f.getName());
      source.append(iFieldRef).append(" ").append(fieldVarName).append(" = ").append(sdkAssertRef).append(".assertFieldExist(").append(typeVarName).append(", \"").append(f.getName()).append("\");").append(NL);
      buildField(f, fieldVarName, source, validator, sdkAssertRef);
    }
    source.append(NL);

    // methods
    String iMethodRef = validator.getTypeName(Signature.createTypeSignature(IMethod.class.getName(), true));
    ListOrderedSet<IMethod> methods = type.getMethods();
    source.append(assertRef).append(".assertEquals(\"method count of '").append(type.getName()).append("'\", ").append(Integer.toString(methods.size())).append(", ").append(typeVarName).append(".getMethods().size());").append(NL);
    for (IMethod method : methods) {
      String methodVarName = getMemberName(method.getName());
      source.append(iMethodRef).append(" ").append(methodVarName).append(" = ").append(sdkAssertRef).append(".assertMethodExist(").append(typeVarName).append(", \"").append(method.getName()).append("\", new String[]{");
      buildMethod(method, methodVarName, source, validator, sdkAssertRef, assertRef);
    }
    source.append(NL);

    // inner types
    ListOrderedSet<IType> innerTypes = type.getTypes();
    source.append(assertRef).append(".assertEquals(\"inner types count of '").append(type.getSimpleName()).append("'\", ").append(Integer.toString(innerTypes.size())).append(", ").append(typeVarName).append(".getTypes().size());").append(NL);
    for (IType innerType : innerTypes) {
      String innerTypeVarName = getMemberName(innerType.getSimpleName());
      source.append("// type ").append(innerType.getSimpleName()).append(NL);
      source.append(iTypeRef).append(" ").append(innerTypeVarName).append(" = ");
      source.append(sdkAssertRef).append(".assertTypeExists(").append(typeVarName).append(", \"").append(innerType.getSimpleName()).append("\");").append(NL);
      buildType(innerType, innerTypeVarName, source, validator, sdkAssertRef, assertRef, iTypeRef);
    }
  }

  protected void buildMethod(IMethod method, StringBuilder source, IImportValidator validator) {
    String sdkAssertRef = validator.getTypeName(Signature.createTypeSignature(SdkAssert.class.getName(), true));
    String assertRef = validator.getTypeName(Signature.createTypeSignature(Assert.class.getName(), true));
    String methodVarName = getMemberName(method.getName());
    buildMethod(method, methodVarName, source, validator, sdkAssertRef, assertRef);
  }

  protected void buildMethod(IMethod method, String methodVarName, StringBuilder source, IImportValidator validator, String sdkAssertRef, String assertRef) {
    List<IMethodParameter> parameterSignatures = method.getParameters();
    if (parameterSignatures.size() > 0) {
      for (int i = 0; i < parameterSignatures.size(); i++) {
        source.append("\"").append(SignatureUtils.getResolvedSignature(parameterSignatures.get(i).getType())).append("\"");
        if (i < parameterSignatures.size() - 1) {
          source.append(", ");
        }
      }
    }
    source.append("});").append(NL);
    if (method.isConstructor()) {
      source.append(assertRef).append(".assertTrue(").append(methodVarName).append(".isConstructor());").append(NL);
    }
    String returnTypeSig = SignatureUtils.getResolvedSignature(method.getReturnType());
    if (StringUtils.isNotEmpty(returnTypeSig)) {
      source.append(sdkAssertRef).append(".assertMethodReturnTypeSignature(").append(methodVarName).append(", \"").append(returnTypeSig).append("\");").append(NL);
    }
    createAnnotationsAsserts(method, method.getDeclaringType(), source, methodVarName, sdkAssertRef);
  }

  protected void buildField(IField field, StringBuilder source, IImportValidator validator) {
    String sdkAssertRef = validator.getTypeName(Signature.createTypeSignature(SdkAssert.class.getName(), true));
    String fieldVarName = getMemberName(field.getName());
    buildField(field, fieldVarName, source, validator, sdkAssertRef);
  }

  protected void buildField(IField field, String fieldVarName, StringBuilder source, IImportValidator validator, String sdkAssertRef) {
    source.append(sdkAssertRef).append(".assertHasFlags(").append(fieldVarName).append(", ").append(field.getFlags()).append(");").append(NL);
    source.append(sdkAssertRef).append(".assertFieldSignature(").append(fieldVarName).append(", ").append("\"").append(SignatureUtils.getResolvedSignature(field.getDataType())).append("\");").append(NL);
    createAnnotationsAsserts(field, field.getDeclaringType(), source, fieldVarName, sdkAssertRef);
  }

  public void createAnnotationsAsserts(IAnnotatable annotatable, IType resolveContext, StringBuilder source, String annotatableRef, String sdkAssertRef) {
    for (IAnnotation a : annotatable.getAnnotations()) {
      String annotationSignature = SignatureUtils.getResolvedSignature(a.getType());
      source.append(sdkAssertRef).append(".assertAnnotation(").append(annotatableRef).append(", \"").append(Signature.toString(annotationSignature)).append("\");").append(NL);
    }
  }

  private String getMemberName(String e) {
    String memberName = CoreUtils.ensureStartWithLowerCase(e);
    if (m_usedMemberNames.contains(memberName)) {
      int counter = 1;
      String workingName = memberName + counter;
      while (m_usedMemberNames.contains(workingName)) {
        counter++;
        workingName = memberName + counter;
      }
      memberName = workingName;
    }
    m_usedMemberNames.add(memberName);
    return memberName;
  }

}
