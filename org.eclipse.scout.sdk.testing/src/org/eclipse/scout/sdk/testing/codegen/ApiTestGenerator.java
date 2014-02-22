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
package org.eclipse.scout.sdk.testing.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.testing.internal.SdkTestingApi;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link ApiTestGenerator}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 26.08.2013
 */
public class ApiTestGenerator {
  public static final String NL = "\n";

  private IJavaElement m_element;
  private Set<String> m_usedMemberNames;

  public ApiTestGenerator(IJavaElement element) {
    m_element = element;
    m_usedMemberNames = new HashSet<String>();
  }

  public String buildSource() throws JavaModelException {
    IImportValidator validator = new SimpleImportValidator();
    StringBuilder sourceBuilder = new StringBuilder();
    sourceBuilder.append("/**").append(NL);
    sourceBuilder.append("* @Generated with ").append(getClass().getName()).append(NL);
    sourceBuilder.append("*/").append(NL);
    sourceBuilder.append("private void testApiOf").append(m_element.getElementName()).append("() throws ").append(validator.getTypeName(Signature.createTypeSignature(Exception.class.getName(), true))).append(" {").append(NL);
    buildElement(m_element, sourceBuilder, validator);
    sourceBuilder.append("}");
    //
    StringBuilder result = new StringBuilder();
    for (String imp : validator.getImportsToCreate()) {
      result.append(imp).append(NL);
    }
    result.append(sourceBuilder.toString());
    return result.toString();
  }

  protected void buildElement(IJavaElement element, StringBuilder source, IImportValidator validator) throws JavaModelException {
    switch (element.getElementType()) {
      case IJavaElement.COMPILATION_UNIT:
        for (IType t : ((ICompilationUnit) element).getTypes()) {
          buildType(t, source, validator);
        }
        break;
      case IJavaElement.TYPE:
        buildType((IType) element, source, validator);
        break;
      case IJavaElement.METHOD:
        buildMethod((IMethod) element, source, validator);
        break;
      case IJavaElement.FIELD:
        buildField((IField) element, source, validator);
        break;
    }
  }

  protected void buildType(IType type, StringBuilder source, IImportValidator validator) throws JavaModelException {
    String sdkAssertRef = validator.getTypeName(Signature.createTypeSignature(SdkAssert.class.getName(), true));
    String iTypeRef = validator.getTypeName(Signature.createTypeSignature(IType.class.getName(), true));
    String typeVarName = getMemberName(type);
    // type
    source.append("// type ").append(type.getElementName()).append(NL);
    source.append(iTypeRef).append(" ").append(typeVarName).append(" = ");
    source.append(sdkAssertRef).append(".assertTypeExists(").append("\"").append(type.getFullyQualifiedName()).append("\");").append(NL);
    buildType(type, typeVarName, source, validator, sdkAssertRef, iTypeRef);
  }

  protected void buildType(IType type, String typeVarName, StringBuilder source, IImportValidator validator, String sdkAssertRef, String iTypeRef) throws JavaModelException {

    source.append(sdkAssertRef).append(".assertHasFlags(").append(typeVarName).append(", ").append(type.getFlags()).append(");").append(NL);
    // super type
    String superClassSig = type.getSuperclassTypeSignature();
    if (!StringUtility.isNullOrEmpty(superClassSig)) {
      source.append(sdkAssertRef).append(".assertHasSuperTypeSignature(").append(typeVarName).append(", \"").append(superClassSig).append("\");").append(NL);
    }
    // interfaces
    String[] intSignatures = type.getSuperInterfaceTypeSignatures();
    if (intSignatures.length > 0) {
      source.append(sdkAssertRef).append(".assertHasSuperIntefaceSignatures(").append(typeVarName).append(", new String[]{");
      for (int i = 0; i < intSignatures.length; i++) {
        source.append("\"").append(intSignatures[i]).append("\"");
        if (i < intSignatures.length - 1) {
          source.append(", ");
        }
      }
      source.append("});").append(NL);
    }
    createAnnotationsAsserts(type, type, source, typeVarName, sdkAssertRef);
    source.append(NL);

    // fields
    source.append("// fields of ").append(type.getElementName()).append(NL);
    String iFieldRef = validator.getTypeName(Signature.createTypeSignature(IField.class.getName(), true));
    IField[] fields = type.getFields();
    source.append(sdkAssertRef).append(".assertEquals(\"field count of '").append(type.getElementName()).append("'\", ").append(Integer.toString(fields.length)).append(", ").append(typeVarName).append(".getFields().length);").append(NL);
    for (IField f : fields) {
      String fieldVarName = getMemberName(f);
      source.append(iFieldRef).append(" ").append(fieldVarName).append(" = ").append(sdkAssertRef).append(".assertFieldExist(").append(typeVarName).append(", \"").append(f.getElementName()).append("\");").append(NL);
      buildField(f, fieldVarName, source, validator, sdkAssertRef);
    }
    source.append(NL);

    // methods
    String iMethodRef = validator.getTypeName(Signature.createTypeSignature(IMethod.class.getName(), true));
    IMethod[] methods = type.getMethods();
    source.append(sdkAssertRef).append(".assertEquals(\"method count of '").append(type.getElementName()).append("'\", ").append(Integer.toString(methods.length)).append(", ").append(typeVarName).append(".getMethods().length);").append(NL);
    for (IMethod method : methods) {
      String methodVarName = getMemberName(method);
      source.append(iMethodRef).append(" ").append(methodVarName).append(" = ").append(sdkAssertRef).append(".assertMethodExist(").append(typeVarName).append(", \"").append(method.getElementName()).append("\", new String[]{");
      buildMethod(method, methodVarName, source, validator, sdkAssertRef);
    }
    source.append(NL);

    // inner types
    IType[] innerTypes = type.getTypes();
    source.append(sdkAssertRef).append(".assertEquals(\"inner types count of '").append(type.getElementName()).append("'\", ").append(Integer.toString(innerTypes.length)).append(", ").append(typeVarName).append(".getTypes().length);").append(NL);
    for (IType innerType : innerTypes) {
      String innerTypeVarName = getMemberName(innerType);
      source.append("// type ").append(innerType.getElementName()).append(NL);
      source.append(iTypeRef).append(" ").append(innerTypeVarName).append(" = ");
      source.append(sdkAssertRef).append(".assertTypeExists(").append(typeVarName).append(", \"").append(innerType.getElementName()).append("\");").append(NL);
      buildType(innerType, innerTypeVarName, source, validator, sdkAssertRef, iTypeRef);
    }
  }

  protected void buildMethod(IMethod method, StringBuilder source, IImportValidator validator) throws JavaModelException {
    String sdkAssertRef = validator.getTypeName(Signature.createTypeSignature(SdkAssert.class.getName(), true));
    String methodVarName = getMemberName(method);
    buildMethod(method, methodVarName, source, validator, sdkAssertRef);
  }

  protected void buildMethod(IMethod method, String methodVarName, StringBuilder source, IImportValidator validator, String sdkAssertRef) throws JavaModelException {
    String[] parameterSignatures = method.getParameterTypes();
    if (parameterSignatures.length > 0) {
      for (int i = 0; i < parameterSignatures.length; i++) {
        source.append("\"").append(parameterSignatures[i]).append("\"");
        if (i < parameterSignatures.length - 1) {
          source.append(", ");
        }
      }
    }
    source.append("});").append(NL);
    if (method.isConstructor()) {
      source.append(sdkAssertRef).append(".assertTrue(").append(methodVarName).append(".isConstructor());").append(NL);
    }
    String returnTypeSig = method.getReturnType();
    if (!StringUtility.isNullOrEmpty(returnTypeSig)) {
      source.append(sdkAssertRef).append(".assertMethodReturnTypeSignature(").append(methodVarName).append(", \"").append(returnTypeSig).append("\");").append(NL);
    }
    createAnnotationsAsserts(method, method.getDeclaringType(), source, methodVarName, sdkAssertRef);
    if (CompareUtility.equals(method.getElementName(), "initValidationRules") && method.getParameters().length == 1) {
      createInitValidationRulesAssert(method, methodVarName, source, sdkAssertRef);
    }
  }

  protected void buildField(IField field, StringBuilder source, IImportValidator validator) throws JavaModelException {
    String sdkAssertRef = validator.getTypeName(Signature.createTypeSignature(SdkAssert.class.getName(), true));
    String fieldVarName = getMemberName(field);
    buildField(field, fieldVarName, source, validator, sdkAssertRef);
  }

  protected void buildField(IField field, String fieldVarName, StringBuilder source, IImportValidator validator, String sdkAssertRef) throws JavaModelException {
    source.append(sdkAssertRef).append(".assertHasFlags(").append(fieldVarName).append(", ").append(field.getFlags()).append(");").append(NL);
    source.append(sdkAssertRef).append(".assertFieldSignature(").append(fieldVarName).append(", ").append("\"").append(field.getTypeSignature()).append("\");").append(NL);
    createAnnotationsAsserts(field, field.getDeclaringType(), source, fieldVarName, sdkAssertRef);
  }

  protected void createInitValidationRulesAssert(IMethod method, String methodVarName, StringBuilder source, String sdkAssertRef) {
    try {
      ISourceRange range = TypeUtility.getContentSourceRange(method);
      String methodSource = method.getOpenable().getBuffer().getText(range.getOffset(), range.getLength());
      // parse
      BufferedReader reader = null;
      try {
        source.append(sdkAssertRef).append(".assertMethodValidationRules(").append(methodVarName).append(", new String[]{");
        reader = new BufferedReader(new StringReader(methodSource));
        String line = reader.readLine();
        boolean superCall = false;
        boolean commaFirst = false;
        while (line != null) {
          line = line.trim();
          // super call
          if (!StringUtility.isNullOrEmpty(line)) {
            if (line.matches("^super\\.initValidationRules\\([^\\)]*\\)\\;$")) {
              superCall = true;
            }
            else {
              if (commaFirst) {
                source.append(", ");
              }
              else {
                commaFirst = true;
              }
              source.append("\"").append(line).append("\"");
            }
          }
          line = reader.readLine();
        }
        source.append("}, ").append(Boolean.toString(superCall)).append(");").append(NL);
      }
      catch (IOException e) {
        if (reader != null) {
          try {
            reader.close();
          }
          catch (IOException e1) {
            SdkTestingApi.logError("could not close reader.", e1);
          }
        }
        SdkTestingApi.logError("could not parse initValidationRulesMethod on '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
      }
    }
    catch (CoreException e) {
      SdkTestingApi.logError("could not parse initValidationRulesMethod on '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
    }
  }

  public void createAnnotationsAsserts(IAnnotatable annotatable, IType resolveContext, StringBuilder source, String annotatableRef, String sdkAssertRef) {
    try {
      for (IAnnotation a : annotatable.getAnnotations()) {
        String annotationSignature = SignatureUtility.getResolvedSignature(Signature.createTypeSignature(a.getElementName(), false), resolveContext);
        source.append(sdkAssertRef).append(".assertAnnotation(").append(annotatableRef).append(", \"").append(SignatureUtility.getFullyQualifiedName(annotationSignature)).append("\");").append(NL);
      }
    }
    catch (CoreException e) {
      StringBuilder message = new StringBuilder("could not create annotation asserts for '");
      if (annotatable instanceof IJavaElement) {
        message.append(((IJavaElement) annotatable).getElementName());
      }
      else {
        message.append(annotatable);
      }
      message.append("'.");
      SdkTestingApi.logError(message.toString(), e);
    }
  }

  private String getMemberName(IJavaElement e) {
    String memberName = ScoutUtility.ensureStartWithLowerCase(e.getElementName());
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
