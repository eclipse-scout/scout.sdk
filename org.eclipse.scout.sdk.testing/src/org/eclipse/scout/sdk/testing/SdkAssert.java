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
package org.eclipse.scout.sdk.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.testing.internal.SdkTestingApi;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.FieldFilters;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;

/**
 * <h3>{@link SdkAssert}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.9.0 05.04.2013
 */
public class SdkAssert extends Assert {

  /**
   * @see SdkAssert#assertExist(String, IJavaElement)
   */
  public static void assertExist(IJavaElement element) {
    assertExist(null, element);
  }

  /**
   * fails if the <code>element</code> does not exist.
   * 
   * @param message
   * @param element
   */
  public static void assertExist(String message, IJavaElement element) {
    if (!TypeUtility.exists(element)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Element does not exist");
        if (element != null) {
          messageBuilder.append(" '").append(element.getElementName()).append("'");
        }
        messageBuilder.append("!");
        message = messageBuilder.toString();
      }

      fail(message);
    }
  }

  /**
   * @see SdkAssert#assertNotExist(String, IJavaElement)
   */
  public static void assertNotExist(IJavaElement element) {
    assertNotExist(null, element);
  }

  /**
   * fails if the <code>element</code> does exist.
   * 
   * @param message
   * @param element
   */
  public static void assertNotExist(String message, IJavaElement element) {
    if (TypeUtility.exists(element)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Element does exist");
        if (element != null) {
          messageBuilder.append(" '").append(element.getElementName()).append("'");
        }
        messageBuilder.append("!");
        message = messageBuilder.toString();
      }

      fail(message);
    }
  }

  /**
   * @see SdkAssert#assertTypeExists(String, String)
   */
  public static IType assertTypeExists(String fullyQualifiedTypeName) {
    return assertTypeExists((String) null, fullyQualifiedTypeName);
  }

  /**
   * fails if no type with the <code>fullyQualifiedTypeName</code> exists.
   * 
   * @param message
   * @param fullyQualifiedTypeName
   * @return the type if found.
   */
  public static IType assertTypeExists(String message, String fullyQualifiedTypeName) {
    IType type = TypeUtility.getType(fullyQualifiedTypeName);
    if (!TypeUtility.exists(type)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(fullyQualifiedTypeName).append("'");
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return type;
  }

  /**
   * @see SdkAssert#assertTypeExistsBySignature(String, String)
   */
  public static IType assertTypeExistsBySignature(String signature) {
    return assertTypeExistsBySignature((String) null, signature);
  }

  /**
   * fails if no type with the <code>signature</code> exists.
   * 
   * @param message
   * @param signature
   * @return the type if found.
   */
  public static IType assertTypeExistsBySignature(String message, String signature) {
    IType type = TypeUtility.getTypeBySignature(signature);
    if (!TypeUtility.exists(type)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(signature).append("'");
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return type;
  }

  /**
   * @see SdkAssert#assertTypeExists(String, ICompilationUnit, String)
   */
  public static IType assertTypeExists(ICompilationUnit icu, String typeName) {
    return assertTypeExists(null, icu, typeName);
  }

  /**
   * fails if the <code>icu</code> does not contain an inner type named <code>typeName</code>.
   * 
   * @param message
   * @param icu
   * @param typeName
   * @return the type if found.
   */
  public static IType assertTypeExists(String message, ICompilationUnit icu, String typeName) {
    SdkAssert.assertNotNull(icu);

    IType type = icu.getType(typeName);
    if (!TypeUtility.exists(type)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(typeName).append("'");
        messageBuilder.append(" in compilation unit '").append(icu.getElementName()).append("'");
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return type;
  }

  /**
   * @see SdkAssert#assertTypeExists(String, IType, String)
   */
  public static IType assertTypeExists(IType declaringType, String typeName) {
    return assertTypeExists(null, declaringType, typeName);
  }

  /**
   * fails if the <code>declaringType</code> does not contains an inner type named <code>typeName</code>.
   * 
   * @param message
   * @param declaringType
   * @param typeName
   * @return the type if found.
   */
  public static IType assertTypeExists(String message, IType declaringType, String typeName) {
    SdkAssert.assertNotNull(declaringType);

    IType type = declaringType.getType(typeName);
    if (!TypeUtility.exists(type)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(typeName).append("'");
        messageBuilder.append(" in type '").append(declaringType.getElementName()).append("'");
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return type;
  }

  /**
   * @see SdkAssert#assertMethodExist(String, IType, String)
   */
  public static IMethod assertMethodExist(IType type, String methodName) {
    return assertMethodExist(null, type, methodName);
  }

  /**
   * fails if the <code>type</code> does not contain a method named <code>methodName</code>.
   * 
   * @param message
   * @param type
   * @param methodName
   * @return the method if found
   */
  public static IMethod assertMethodExist(String message, IType type, String methodName) {
    IMethod method = TypeUtility.getMethod(type, methodName);
    if (!TypeUtility.exists(method)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getElementName()).append("'");
        }
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return method;
  }

  public static IMethod assertMethodExist(IType type, String methodName, String[] parameterSignatures) {
    return assertMethodExist(null, type, methodName, parameterSignatures);
  }

  /**
   * fails if the <code>type</code> does not contain a method named <code>methodName</code>.
   * 
   * @param message
   * @param type
   * @param methodName
   * @return the method if found
   */
  public static IMethod assertMethodExist(String message, IType type, final String methodName, final String[] parameterSignatures) {
    IMethod method = TypeUtility.getFirstMethod(type, new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        if (CompareUtility.equals(methodName, candidate.getElementName())) {
          String[] refParameterSignatures = candidate.getParameterTypes();
          if (parameterSignatures.length == refParameterSignatures.length) {
            boolean matches = true;
            for (int i = 0; i < parameterSignatures.length; i++) {
              if (!CompareUtility.equals(SignatureUtility.getResolvedSignature(parameterSignatures[i], candidate.getDeclaringType()),
                  SignatureUtility.getResolvedSignature(refParameterSignatures[i], candidate.getDeclaringType()))) {
                matches = false;
                break;
              }
            }
            return matches;
          }

        }
        return false;
      }
    });
    if (!TypeUtility.exists(method)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getElementName()).append("'");
        }
        messageBuilder.append(" does not exist! [parameters: ");
        for (int i = 0; i < parameterSignatures.length; i++) {
          messageBuilder.append("'").append(parameterSignatures[i]).append("'");
          if (i < parameterSignatures.length - 1) {
            messageBuilder.append(", ");
          }
        }
        messageBuilder.append("]");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return method;
  }

  /**
   * @see SdkAssert#assertMethodExistInSuperTypeHierarchy(String, IType, String)
   */
  public static IMethod assertMethodExistInSuperTypeHierarchy(IType type, String methodName) {
    return assertMethodExistInSuperTypeHierarchy(null, type, methodName);
  }

  /**
   * fails if the <code>type</code> or any super type (interface or superclass) does not contain a method named
   * <code>methodName</code>.
   * 
   * @param message
   * @param type
   * @param methodName
   * @return the first method found in super type hierarchy. Each levels superclass is considered before all interfaces
   *         in no particular order.
   */
  public static IMethod assertMethodExistInSuperTypeHierarchy(String message, IType type, String methodName) {
    IMethod method = TypeUtility.findMethodInSuperHierarchy(methodName, type, TypeUtility.getSuperTypeHierarchy(type));
    if (!TypeUtility.exists(method)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getElementName()).append("'");
        }
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return method;
  }

  public static void assertMethodReturnTypeSignature(IMethod method, String expectedSignature) throws CoreException {
    assertMethodReturnTypeSignature(null, method, expectedSignature);
  }

  public static void assertMethodReturnTypeSignature(String message, IMethod method, String expectedSignature) throws CoreException {
    String signature = SignatureUtility.getResolvedSignature(method.getReturnType(), method.getDeclaringType());
    expectedSignature = SignatureUtility.getResolvedSignature(expectedSignature, method.getDeclaringType());
    if (!CompareUtility.equals(signature, expectedSignature)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method return type not equal! [expected: '").append(expectedSignature).append("', actual: '").append(signature).append("'] '");
        message = messageBuilder.toString();
      }
      fail(message);

    }
  }

  public static void assertMethodParameterSignatures(IMethod method, String[] expectedSignatures) throws CoreException {
    assertMethodParameterSignatures(null, method, expectedSignatures);
  }

  public static void assertMethodParameterSignatures(String message, IMethod method, String[] expectedSignatures) throws CoreException {
    String[] parameterSignatures = method.getParameterTypes();
    if (parameterSignatures.length == expectedSignatures.length) {
      // resolve
      for (int i = 0; i < parameterSignatures.length; i++) {
        parameterSignatures[i] = SignatureUtility.getResolvedSignature(parameterSignatures[i], method.getDeclaringType());
        expectedSignatures[i] = SignatureUtility.getResolvedSignature(expectedSignatures[i], method.getDeclaringType());
      }
      // sort
      Arrays.sort(parameterSignatures);
      Arrays.sort(expectedSignatures);
      for (int i = 0; i < parameterSignatures.length; i++) {
        if (!CompareUtility.equals(parameterSignatures[i], expectedSignatures[i])) {
          if (message == null) {
            StringBuilder messageBuilder = new StringBuilder("Method '").append(method.getElementName()).append("' does not have the same parameter signature! [expected '").append(expectedSignatures[i]).append("', actual '").append(parameterSignatures[i]).append("']");
            message = messageBuilder.toString();
          }
          fail(message);
          break;
        }
      }

    }
    else {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(method.getElementName()).append("' does not have the same same amount of parameters! [expected: ").append(expectedSignatures.length).append(", actual: ").append(parameterSignatures.length).append("]");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  public static void assertMethodValidationRules(IMethod initValidationRulesMethod, String[] validationRuleLines, boolean superCall) throws JavaModelException {
    ISourceRange range = TypeUtility.getContentSourceRange(initValidationRulesMethod);
    String source = initValidationRulesMethod.getOpenable().getBuffer().getText(range.getOffset(), range.getLength());
    assertNotNull(source);
    Set<String> expectedLines = new HashSet<String>(Arrays.asList(validationRuleLines));
    // parse
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(source));
      String line = reader.readLine();
      while (line != null) {
        line = line.trim();
        if (StringUtility.hasText(line)) {
          // super call
          if (!line.matches("^super\\.initValidationRules\\([^\\)]*\\)\\;$")) {
            if (!expectedLines.remove(line)) {
              StringBuilder messageBuilder = new StringBuilder();
              messageBuilder.append("Validation rules failer. Validation rule '").append(line).append("' was not expected!");
              fail(messageBuilder.toString());
            }
          }
        }
        line = reader.readLine();
      }
      if (!expectedLines.isEmpty()) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Validation rules failer. The following expected validation rules are not found in code: [");
        Iterator<String> expectedIt = expectedLines.iterator();
        messageBuilder.append(expectedIt.next());
        while (expectedIt.hasNext()) {
          messageBuilder.append(", ").append(expectedIt.next());
        }
        messageBuilder.append("]");
        fail(messageBuilder.toString());
      }
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
      SdkTestingApi.logError("could not parse initValidationRulesMethod on '" + initValidationRulesMethod.getDeclaringType().getFullyQualifiedName() + "'.", e);
    }

  }

  /**
   * @see SdkAssert#assertFieldExist(String, IType, String)
   */
  public static IField assertFieldExist(IType type, String fieldName) {
    return assertFieldExist(null, type, fieldName);
  }

  /**
   * fails if the type does not have a field named <code>fieldName</code>.
   * 
   * @param message
   * @param type
   * @param fieldName
   * @return the field if it exists.
   */
  public static IField assertFieldExist(String message, IType type, String fieldName) {
    IField field = TypeUtility.getFirstField(type, FieldFilters.getNameFilter(fieldName));
    if (!TypeUtility.exists(field)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Field '").append(fieldName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getElementName()).append("'");
        }
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return field;
  }

  public static void assertFieldSignature(IField field, String expectedSignature) throws CoreException {
    assertFieldSignature(null, field, expectedSignature);
  }

  public static void assertFieldSignature(String message, IField field, String expectedSignature) throws CoreException {
    String resolvedSignature = SignatureUtility.getResolvedSignature(field.getTypeSignature(), field.getDeclaringType());
    String expectedResolvedSignature = SignatureUtility.getResolvedSignature(expectedSignature, field.getDeclaringType());
    if (!CompareUtility.equals(resolvedSignature, expectedResolvedSignature)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Field '").append(field.getElementName()).append("' does not have the expected type signature! [expected:'").append(expectedResolvedSignature).append("', actual:'").append(resolvedSignature).append("']");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  /**
   * @see SdkAssert#assertHasSuperType(String, IType, String)
   */
  public static void assertHasSuperType(IType type, String superTypeFqn) {
    assertHasSuperType(null, type, superTypeFqn);
  }

  /**
   * fails if the type does not have a supertype (superclass or interface) with the <code>superTypeFqn</code> (fully
   * qualified name).
   * 
   * @param message
   * @param type
   * @param superTypeFqn
   */
  public static void assertHasSuperType(String message, IType type, String superTypeFqn) {
    if (!TypeUtility.getSuperTypeHierarchy(type).contains(TypeUtility.getType(superTypeFqn))) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getFullyQualifiedName()).append("' does not have '").append(superTypeFqn).append("' as supertype!");
        message = messageBuilder.toString();
      }
      fail(message);
    }

  }

  public static void assertHasSuperTypeSignature(IType type, String superTypeSignature) throws CoreException {
    assertHasSuperTypeSignature(null, type, superTypeSignature);
  }

  public static void assertHasSuperTypeSignature(String message, IType type, String superTypeSignature) throws CoreException {
    String refSuperTypeSig = SignatureUtility.getResolvedSignature(type.getSuperclassTypeSignature(), type);
    superTypeSignature = SignatureUtility.getResolvedSignature(superTypeSignature, type);
    if (!CompareUtility.equals(refSuperTypeSig, superTypeSignature)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getFullyQualifiedName()).append("' does not have expected supertype! [expected '").append(superTypeSignature).append("', actual '").append(refSuperTypeSig).append("']");
        message = messageBuilder.toString();
      }
      fail(message);

    }
  }

  public static void assertHasSuperIntefaceSignatures(IType type, String[] interfaceSignatures) throws CoreException {
    assertHasSuperIntefaceSignatures(null, type, interfaceSignatures);
  }

  public static void assertHasSuperIntefaceSignatures(String message, IType type, String[] interfaceSignatures) throws CoreException {
    String[] refInterfaceSignatures = type.getSuperInterfaceTypeSignatures();
    if (refInterfaceSignatures.length == interfaceSignatures.length) {
      // resolve
      for (int i = 0; i < interfaceSignatures.length; i++) {
        interfaceSignatures[i] = SignatureUtility.getResolvedSignature(interfaceSignatures[i], type);
        refInterfaceSignatures[i] = SignatureUtility.getResolvedSignature(refInterfaceSignatures[i], type);
      }
      // sort
      Arrays.sort(interfaceSignatures);
      Arrays.sort(refInterfaceSignatures);
      for (int i = 0; i < interfaceSignatures.length; i++) {
        if (!CompareUtility.equals(interfaceSignatures[i], refInterfaceSignatures[i])) {
          if (message == null) {
            StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getFullyQualifiedName()).append("' does not have the same interfaces! [").append(refInterfaceSignatures[i]).append(", ").append(interfaceSignatures[i]).append("]");
            message = messageBuilder.toString();
          }
          fail(message);
          break;
        }
      }

    }
    else {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getElementName()).append("' does not have the same same amount of interfaces! [expected: ").append(interfaceSignatures.length).append(", actual: ").append(refInterfaceSignatures.length).append("]");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  /**
   * @see SdkAssert#assertSerialVersionUidExists(String, IType)
   */
  public static IField assertSerialVersionUidExists(IType type) throws JavaModelException {
    return assertSerialVersionUidExists(null, type);
  }

  /**
   * fails if the type does not have a field called <code> serialVersionUID</code>.
   * 
   * @param message
   * @param type
   * @return
   * @throws JavaModelException
   */
  public static IField assertSerialVersionUidExists(String message, IType type) throws JavaModelException {
    IField field = assertFieldExist(message, type, "serialVersionUID");
    assertPrivate(message, field).assertStatic().assertFinal();
    return field;
  }

  public static IAnnotation assertAnnotation(IAnnotatable annotatable, String fqAnnotationTypeName) {

    IAnnotation annotation = JdtUtility.getAnnotation(annotatable, fqAnnotationTypeName);
    if (annotation == null || !annotation.exists()) {
      StringBuilder message = new StringBuilder("Element '");
      if (annotatable instanceof IJavaElement) {
        message.append(((IJavaElement) annotatable).getElementName());
      }
      else {
        message.append(annotatable.toString());
      }
      message.append("' does not have the expected annotation '").append(fqAnnotationTypeName).append("'.");
      fail(message.toString());
    }
    return annotation;
  }

  /**
   * @see SdkAssert#assertOrderAnnotation(String, IAnnotatable, Double)
   */
  public static void assertOrderAnnotation(IAnnotatable annotatable, Double orderNr) throws JavaModelException {
    assertOrderAnnotation(null, annotatable, orderNr);
  }

  /**
   * fails if the <code> annotatable</code> does not have an order annotation with the <code>orderNr</code>.
   * 
   * @param message
   * @param annotatable
   * @param orderNr
   * @throws JavaModelException
   */
  public static void assertOrderAnnotation(String message, IAnnotatable annotatable, Double orderNr) throws JavaModelException {
    Double memberOrderNr = ScoutTypeUtility.getOrderAnnotationValue(annotatable);
    if (!CompareUtility.equals(orderNr, memberOrderNr)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Order annotation not equal: exptected '").append(orderNr).append("'; found on member");
        if (annotatable != null && annotatable instanceof IMember) {
          messageBuilder.append(" '").append(((IMember) annotatable).getElementName()).append("'");
        }
        messageBuilder.append(" is '").append(memberOrderNr).append("'!");
        message = messageBuilder.toString();
      }
      fail(message);
    }

    assertEquals(message, memberOrderNr, orderNr);
  }

  /**
   * @see SdkAssert#assertSameParent(String, IJavaElement...)
   */
  public static void assertSameParent(IJavaElement... elements) {
    assertSameParent(null, elements);
  }

  /**
   * fails if the elements does not have the same parent element.
   * 
   * @param message
   * @param elements
   */
  public static void assertSameParent(String message, IJavaElement... elements) {
    if (elements != null) {
      IJavaElement parent = null;
      for (IJavaElement element : elements) {
        if (parent == null) {
          parent = element.getParent();
        }
        else {
          assertEquals(message, parent, element.getParent());
        }
      }
    }
  }

  /**
   * @see SdkAssert#assertElementSequenceInSource(String, IMember...)
   */
  public static void assertElementSequenceInSource(IMember... elements) throws JavaModelException {
    assertElementSequenceInSource(null, elements);
  }

  /**
   * fails if the elements does not appear in the arrays sequence in the source code.
   * 
   * @param message
   * @param elements
   * @throws JavaModelException
   */
  public static void assertElementSequenceInSource(String message, IMember... elements) throws JavaModelException {
    assertSameParent(elements);
    if (elements != null) {
      int index = -1;
      for (IMember element : elements) {
        ISourceRange sourceRange = element.getSourceRange();
        if (index >= sourceRange.getOffset()) {
          fail(message);
        }
        else {
          index = sourceRange.getOffset() + sourceRange.getLength();
        }
      }
    }
  }

  public static void assertHasFlags(IMember member, int flags) throws JavaModelException {
    assertHasFlags(null, member, flags);
  }

  public static void assertHasFlags(String message, IMember member, int flags) throws JavaModelException {
    int memberFlags = member.getFlags();
    if ((flags & memberFlags) != flags) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("member '").append(member.getElementName()).append("'");
        messageBuilder.append(" has flags [").append(Flags.toString(memberFlags)).append("] expected [").append(Flags.toString(flags)).append("]!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  /**
   * fails if the service interface is not registered as a proxy service in the projects plugin.xml.
   * 
   * @param project
   * @param serviceInterface
   */
  public static void assertServiceProxyRegistered(IProject project, IType serviceInterface) {
    IPluginElement[] simpleExtensions = new PluginModelHelper(project).PluginXml.getSimpleExtensions(
        "org.eclipse.scout.service.services", "proxy");
    for (IPluginElement element : simpleExtensions) {
      if (CompareUtility.equals(element.getAttribute("class").getValue(), serviceInterface.getFullyQualifiedName())) {
        return;
      }
    }
    fail("proxy for service '" + serviceInterface.getElementName() + "' ist not registered in project '" + project.getName() + "'");
  }

  /**
   * fails if the given service interface is not registered as a service extension in the projects plugin.xml.
   * 
   * @param project
   * @param serviceInterface
   */
  public static void assertServiceRegistered(IProject project, IType serviceInterface) {
    IPluginElement[] simpleExtensions = new PluginModelHelper(project).PluginXml.getSimpleExtensions(
        "org.eclipse.scout.service.services", "service");
    for (IPluginElement element : simpleExtensions) {
      if (CompareUtility.equals(element.getAttribute("class").getValue(), serviceInterface.getFullyQualifiedName())) {
        return;
      }
    }
    fail("service '" + serviceInterface.getElementName() + "' ist not registered in project '" + project.getName() + "'");
  }

  /**
   * fails if the member does not have the private flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */

  /**
   * @see SdkAssert#assertPrivate(String, IMember)
   */
  public static FlagAssert assertPrivate(IMember member) throws JavaModelException {
    return assertPrivate(null, member);
  }

  /**
   * fails if the member does not have the <b><code>private</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertPrivate(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertPrivate();
  }

  /**
   * @see SdkAssert#assertProtected(String, IMember)
   */
  public static FlagAssert assertProtected(IMember member) throws JavaModelException {
    return assertProtected(null, member);
  }

  /**
   * fails if the member does not have the <b><code>protected</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertProtected(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertProtected();
  }

  /**
   * @see SdkAssert#assertPublic(String, IMember)
   */
  public static FlagAssert assertPublic(IMember member) throws JavaModelException {
    return assertPublic(null, member);
  }

  /**
   * fails if the member does not have the <b><code>public</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertPublic(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertPublic();
  }

  /**
   * @see SdkAssert#assertAbstract(String, IMember)
   */
  public static FlagAssert assertAbstract(IMember member) throws JavaModelException {
    return assertAbstract(null, member);
  }

  /**
   * fails if the member does not have the <b><code>abstract</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertAbstract(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertAbstract();
  }

  /**
   * @see SdkAssert#assertFinal(String, IMember)
   */
  public static FlagAssert assertFinal(IMember member) throws JavaModelException {
    return assertFinal(null, member);
  }

  /**
   * fails if the member does not have the <b><code>final</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertFinal(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertFinal();
  }

  /**
   * @see SdkAssert#assertInterface(String, IMember)
   */
  public static FlagAssert assertInterface(IMember member) throws JavaModelException {
    return assertInterface(null, member);
  }

  /**
   * fails if the member does not have the <b><code>interface</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertInterface(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertInterface();
  }

  /**
   * @see SdkAssert#assertStatic(String, IMember)
   */
  public static FlagAssert assertStatic(IMember member) throws JavaModelException {
    return assertStatic(null, member);
  }

  /**
   * fails if the member does not have the <b><code>static</code></b> flag. <br>
   * The following example for a private static member.
   * 
   * <pre>
   * SdkAssert.assertPrivate(aMember).assertStatic().assertNoMoreFlags();
   * </pre>
   * 
   * @param message
   * @param member
   * @return {@link FlagAssert} where the private flag is not anymore included.
   * @throws JavaModelException
   */
  public static FlagAssert assertStatic(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertStatic();
  }

  public static TypeAssert typeAssert(IType declaringType, String typeName) throws JavaModelException {
    IType type = assertTypeExists(declaringType, typeName);
    return new TypeAssert(type);
  }

  public static MethodAssert methodAssert(IType type, String methodname) throws JavaModelException {
    IMethod method = assertMethodExist(type, methodname);
    return new MethodAssert(method);
  }

  public static class FlagAssert {
    private int m_flags;
    private final IMember m_member;
    private final String m_message;

    public FlagAssert(String message, IMember member) throws JavaModelException {
      m_message = message;
      m_member = member;
      m_flags = member.getFlags();
    }

    public FlagAssert assertPrivate() {
      if (!Flags.isPrivate(m_flags)) {
        flagFailed(Flags.toString(Flags.AccPrivate));
      }
      m_flags = m_flags ^ Flags.AccPrivate;
      return this;
    }

    public FlagAssert assertProtected() {
      if (!Flags.isProtected(m_flags)) {
        flagFailed(Flags.toString(Flags.AccProtected));
      }
      m_flags = m_flags ^ Flags.AccProtected;
      return this;
    }

    public FlagAssert assertPublic() {
      if (!Flags.isPublic(m_flags)) {
        flagFailed(Flags.toString(Flags.AccPublic));
      }
      m_flags = m_flags ^ Flags.AccPublic;
      return this;
    }

    public FlagAssert assertAbstract() {
      if (!Flags.isAbstract(m_flags)) {
        flagFailed(Flags.toString(Flags.AccAbstract));
      }
      m_flags = m_flags ^ Flags.AccAbstract;
      return this;
    }

    public FlagAssert assertFinal() {
      if (!Flags.isFinal(m_flags)) {
        flagFailed(Flags.toString(Flags.AccFinal));
      }
      m_flags = m_flags ^ Flags.AccFinal;
      return this;
    }

    public FlagAssert assertInterface() {
      if (!Flags.isInterface(m_flags)) {
        flagFailed(Flags.toString(Flags.AccInterface));
      }
      m_flags = m_flags ^ Flags.AccInterface;
      return this;
    }

    public FlagAssert assertStatic() {
      if (!Flags.isStatic(m_flags)) {
        flagFailed(Flags.toString(Flags.AccStatic));
      }
      m_flags = m_flags ^ Flags.AccStatic;
      return this;
    }

    public FlagAssert assertNoMoreFlags() {
      if (m_flags != 0) {
        if (m_message == null) {
          StringBuilder message = new StringBuilder("member");
          if (m_member != null) {
            message.append(" '").append(m_member.getElementName()).append("'");
          }
          message.append(" has still flags [").append(Flags.toString(m_flags)).append("]!");
        }
        else {
          fail(m_message);
        }
      }
      return this;
    }

    public void flagFailed(String flagName) {
      if (m_message == null) {
        StringBuilder message = new StringBuilder("member");
        if (m_member != null) {
          message.append(" '").append(m_member.getElementName()).append("'");
        }
        message.append(" is not ").append(flagName).append("!");
      }
      else {
        fail(m_message);
      }
    }
  }

  public static class TypeAssert {
    private IType m_type;

    public TypeAssert(IType type) {
      m_type = type;
    }

    public TypeAssert assertExist() throws JavaModelException {
      SdkAssert.assertExist(m_type);
      return this;
    }

    public TypeAssert assertSuperClass(String fqn) throws JavaModelException {
      SdkAssert.assertHasSuperType(m_type, fqn);
      return this;
    }

    public FlagAssert flagAssert() throws JavaModelException {
      return new FlagAssert(null, m_type);
    }

    public IType getType() {
      return m_type;
    }

  }

  public static class MethodAssert {
    private IMethod m_method;

    public MethodAssert(IMethod method) throws JavaModelException {
      m_method = method;
    }

    public MethodAssert assertExits() {
      SdkAssert.assertExist(m_method);
      return this;
    }

    public MethodAssert assertParameterCount(int expected) throws JavaModelException {
      ILocalVariable[] parameters = m_method.getParameters();
      if (parameters.length != expected) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Parameter count of method '").append(m_method.getElementName()).append("': expected:'").append(expected).append("' actual:'").append(parameters.length).append("'.");
        fail(messageBuilder.toString());
      }
      return this;
    }

    public MethodAssert assertConstructor() throws JavaModelException {
      if (!m_method.isConstructor()) {
        fail("method '" + m_method.getElementName() + "' is expected to be a constructor.");
      }
      return this;
    }

    public MethodAssert assertReturnType(String returnTypeFqn) throws JavaModelException {
      String retSig = m_method.getReturnType();
      String qualifier = Signature.getSignatureQualifier(retSig);
      String simpleName = Signature.getSignatureSimpleName(retSig);
      if (!StringUtility.isNullOrEmpty(qualifier)) {
        assertEquals(qualifier, Signature.getQualifier(returnTypeFqn));
      }
      assertEquals(simpleName, Signature.getSimpleName(returnTypeFqn));
      return this;
    }

    public FlagAssert flagAssert() throws JavaModelException {
      return new FlagAssert(null, m_method);
    }

    public IMethod getMethod() {
      return m_method;
    }
  }

}
