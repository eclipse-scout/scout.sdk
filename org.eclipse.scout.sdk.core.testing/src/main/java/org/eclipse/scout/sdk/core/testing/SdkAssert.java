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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.scout.sdk.core.model.CompilationUnit;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IAnnotation;
import org.eclipse.scout.sdk.core.model.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMember;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IMethodParameter;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.JavaModelUtils;
import org.eclipse.scout.sdk.core.model.MethodFilters;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;

/**
 * <h3>{@link SdkAssert}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 05.04.2013
 */
public class SdkAssert extends Assert {

  /**
   * @see SdkAssert#assertExist(String, Object)
   */
  public static void assertExist(Object element) {
    assertExist(null, element);
  }

  /**
   * fails if the <code>element</code> does not exist.
   *
   * @param message
   * @param element
   */
  public static void assertExist(String message, Object element) {
    if (element != null) {
      return;
    }

    if (message == null) {
      message = "Element does not exist";
    }

    fail(message);
  }

  /**
   * @see SdkAssert#assertNotExist(String, Object)
   */
  public static void assertNotExist(Object element) {
    assertNotExist(null, element);
  }

  /**
   * fails if the <code>element</code> does exist.
   *
   * @param message
   * @param element
   */
  public static void assertNotExist(String message, Object element) {
    if (element == null) {
      return;
    }

    if (message == null) {
      message = "Element does exist";
    }

    fail(message);
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

    IType type = CoreUtils.getInnerType(declaringType, typeName);
    if (type == null) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(typeName).append("'");
        messageBuilder.append(" in type '").append(declaringType.getName()).append("'");
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
    IMethod method = CoreUtils.getMethod(type, methodName);
    if (method == null) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getName()).append("'");
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
    IMethod method = CoreUtils.getMethod(type, new Predicate<IMethod>() {
      @Override
      public boolean evaluate(IMethod candidate) {
        if (Objects.equals(methodName, candidate.getName())) {
          List<IMethodParameter> refParameterSignatures = candidate.getParameters();
          if (parameterSignatures.length == refParameterSignatures.size()) {
            boolean matches = true;
            for (int i = 0; i < parameterSignatures.length; i++) {
              if (!equalSignature(getResolvedSignature(parameterSignatures[i], candidate.getDeclaringType()), SignatureUtils.getResolvedSignature(refParameterSignatures.get(i).getType()))) {
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
    if (method == null) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getName()).append("'");
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
    IMethod method = CoreUtils.findMethodInSuperHierarchy(type, MethodFilters.getNameFilter(methodName));
    if (method == null) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getName()).append("'");
        }
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return method;
  }

  public static void assertMethodReturnTypeSignature(IMethod method, String expectedSignature) {
    assertMethodReturnTypeSignature(null, method, expectedSignature);
  }

  public static void assertMethodReturnTypeSignature(String message, IMethod method, String expectedSignature) {
    String signature = SignatureUtils.getResolvedSignature(method.getReturnType());
    expectedSignature = getResolvedSignature(expectedSignature, method.getDeclaringType());
    if (!equalSignature(signature, expectedSignature)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Method return type not equal! [expected: '").append(expectedSignature).append("', actual: '").append(signature).append("'] '");
        message = messageBuilder.toString();
      }
      fail(message);
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
    IField field = CoreUtils.getField(type, fieldName);
    if (field == null) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Field '").append(fieldName).append("'");
        if (type != null) {
          messageBuilder.append(" in type '").append(type.getName()).append("'");
        }
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return field;
  }

  public static void assertFieldSignature(IField field, String expectedSignature) {
    assertFieldSignature(null, field, expectedSignature);
  }

  public static void assertFieldSignature(String message, IField field, String expectedSignature) {
    String resolvedSignature = SignatureUtils.getResolvedSignature(field.getDataType());
    expectedSignature = getResolvedSignature(expectedSignature, field.getDeclaringType());
    if (!equalSignature(resolvedSignature, expectedSignature)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Field '").append(field.getName()).append("' does not have the expected type signature! [expected:'").append(expectedSignature).append("', actual:'").append(resolvedSignature).append("']");
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
    if (!CoreUtils.isInstanceOf(type, superTypeFqn)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getName()).append("' does not have '").append(superTypeFqn).append("' as supertype!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  public static void assertHasSuperTypeSignature(IType type, String superTypeSignature) {
    assertHasSuperTypeSignature(null, type, superTypeSignature);
  }

  public static void assertHasSuperTypeSignature(String message, IType type, String superTypeSignature) {
    String refSuperTypeSig = SignatureUtils.getResolvedSignature(type.getSuperClass());
    superTypeSignature = getResolvedSignature(superTypeSignature, type);
    if (!equalSignature(refSuperTypeSig, superTypeSignature)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getName()).append("' does not have expected supertype! [expected '").append(superTypeSignature).append("', actual '").append(refSuperTypeSig).append("']");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  public static void assertHasSuperIntefaceSignatures(IType type, String[] interfaceSignatures) {
    assertHasSuperIntefaceSignatures(null, type, interfaceSignatures);
  }

  public static void assertHasSuperIntefaceSignatures(String message, IType type, String[] interfaceSignatures) {
    ListOrderedSet<IType> refInterfaces = type.getSuperInterfaces();
    if (refInterfaces.size() == interfaceSignatures.length) {
      String[] refInterfaceSignatures = new String[refInterfaces.size()];
      // resolve
      for (int i = 0; i < interfaceSignatures.length; i++) {
        interfaceSignatures[i] = getResolvedSignature(interfaceSignatures[i], type);
        refInterfaceSignatures[i] = SignatureUtils.getResolvedSignature(refInterfaces.get(i));
      }
      // sort
      Arrays.sort(interfaceSignatures);
      Arrays.sort(refInterfaceSignatures);
      for (int i = 0; i < interfaceSignatures.length; i++) {
        if (!equalSignature(interfaceSignatures[i], refInterfaceSignatures[i])) {
          if (message == null) {
            StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getName()).append("' does not have the same interfaces! [").append(refInterfaceSignatures[i]).append(", ").append(interfaceSignatures[i]).append("]");
            message = messageBuilder.toString();
          }
          fail(message);
          break;
        }
      }

    }
    else {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(type.getName()).append("' does not have the same same amount of interfaces! [expected: ").append(interfaceSignatures.length).append(", actual: ").append(refInterfaces.size()).append("]");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  /**
   * @see SdkAssert#assertSerialVersionUidExists(String, IType)
   */
  public static IField assertSerialVersionUidExists(IType type) {
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
  public static IField assertSerialVersionUidExists(String message, IType type) {
    IField field = assertFieldExist(message, type, "serialVersionUID");
    assertPrivate(message, field).assertStatic().assertFinal();
    return field;
  }

  /**
   * Asserts that the given annotation exists on the given object.
   *
   * @param annotatable
   * @param fqAnnotationTypeName
   * @return
   */
  public static IAnnotation assertAnnotation(IAnnotatable annotatable, String fqAnnotationTypeName) {
    IAnnotation annotation = CoreUtils.getAnnotation(annotatable, fqAnnotationTypeName);
    if (annotation == null) {
      StringBuilder message = new StringBuilder("Element '");
      if (annotatable instanceof IMember) {
        message.append(((IMember) annotatable).getName());
      }
      else {
        message.append(annotatable.toString());
      }
      message.append("' does not have the expected annotation '").append(fqAnnotationTypeName).append("'.");
      fail(message.toString());
    }
    return annotation;
  }

  public static void assertHasFlags(IMember member, int flags) {
    assertHasFlags(null, member, flags);
  }

  public static void assertHasFlags(String message, IMember member, int flags) {
    int memberFlags = member.getFlags();
    if ((flags & memberFlags) != flags) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("member '").append(member.getName()).append("'");
        messageBuilder.append(" has flags [").append(Flags.toString(memberFlags)).append("] expected [").append(Flags.toString(flags)).append("]!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
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
  public static FlagAssert assertPrivate(IMember member) {
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
  public static FlagAssert assertPrivate(String message, IMember member) {
    return new FlagAssert(message, member).assertPrivate();
  }

  /**
   * @see SdkAssert#assertProtected(String, IMember)
   */
  public static FlagAssert assertProtected(IMember member) {
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
  public static FlagAssert assertProtected(String message, IMember member) {
    return new FlagAssert(message, member).assertProtected();
  }

  /**
   * @see SdkAssert#assertPublic(String, IMember)
   */
  public static FlagAssert assertPublic(IMember member) {
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
  public static FlagAssert assertPublic(String message, IMember member) {
    return new FlagAssert(message, member).assertPublic();
  }

  /**
   * @see SdkAssert#assertAbstract(String, IMember)
   */
  public static FlagAssert assertAbstract(IMember member) {
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
  public static FlagAssert assertAbstract(String message, IMember member) {
    return new FlagAssert(message, member).assertAbstract();
  }

  /**
   * @see SdkAssert#assertFinal(String, IMember)
   */
  public static FlagAssert assertFinal(IMember member) {
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
  public static FlagAssert assertFinal(String message, IMember member) {
    return new FlagAssert(message, member).assertFinal();
  }

  /**
   * @see SdkAssert#assertInterface(String, IMember)
   */
  public static FlagAssert assertInterface(IMember member) {
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
  public static FlagAssert assertInterface(String message, IMember member) {
    return new FlagAssert(message, member).assertInterface();
  }

  /**
   * @see SdkAssert#assertStatic(String, IMember)
   */
  public static FlagAssert assertStatic(IMember member) {
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
  public static FlagAssert assertStatic(String message, IMember member) {
    return new FlagAssert(message, member).assertStatic();
  }

  public static TypeAssert typeAssert(IType declaringType, String typeName) {
    IType type = assertTypeExists(declaringType, typeName);
    return new TypeAssert(type);
  }

  public static MethodAssert methodAssert(IType type, String methodname) {
    IMethod method = assertMethodExist(type, methodname);
    return new MethodAssert(method);
  }

  private static boolean equalSignature(String a, String b) {
    if (a != null) {
      a = a.replace('$', '.');
    }
    if (b != null) {
      b = b.replace('$', '.');
    }

    return Objects.equals(a, b);
  }

  public static class FlagAssert {
    private int m_flags;
    private final IMember m_member;
    private final String m_message;

    public FlagAssert(String message, IMember member) {
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
            message.append(" '").append(m_member.getName()).append("'");
          }
          message.append(" has still flags [").append(Flags.toString(m_flags)).append("]!");
          fail(message.toString());
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
          message.append(" '").append(m_member.getName()).append("'");
        }
        message.append(" is not ").append(flagName).append("!");
        fail(message.toString());
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

    public TypeAssert assertExist() {
      SdkAssert.assertExist(m_type);
      return this;
    }

    public TypeAssert assertSuperClass(String fqn) {
      SdkAssert.assertHasSuperType(m_type, fqn);
      return this;
    }

    public FlagAssert flagAssert() {
      return new FlagAssert(null, m_type);
    }

    public IType getType() {
      return m_type;
    }

  }

  public static class MethodAssert {
    private IMethod m_method;

    public MethodAssert(IMethod method) {
      m_method = method;
    }

    public MethodAssert assertExits() {
      SdkAssert.assertExist(m_method);
      return this;
    }

    public MethodAssert assertParameterCount(int expected) {
      List<IMethodParameter> parameters = m_method.getParameters();
      if (parameters.size() != expected) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Parameter count of method '").append(m_method.getName()).append("': expected:'").append(expected).append("' actual:'").append(parameters.size()).append("'.");
        fail(messageBuilder.toString());
      }
      return this;
    }

    public MethodAssert assertConstructor() {
      if (!m_method.isConstructor()) {
        fail("method '" + m_method.getName() + "' is expected to be a constructor.");
      }
      return this;
    }

    public MethodAssert assertReturnType(String returnTypeFqn) {
      String retSig = SignatureUtils.getResolvedSignature(m_method.getReturnType());
      String qualifier = Signature.getSignatureQualifier(retSig);
      String simpleName = Signature.getSignatureSimpleName(retSig);
      if (StringUtils.isNotBlank(qualifier)) {
        assertEquals(qualifier, Signature.getQualifier(returnTypeFqn));
      }
      assertEquals(simpleName, Signature.getSimpleName(returnTypeFqn));
      return this;
    }

    public FlagAssert flagAssert() {
      return new FlagAssert(null, m_method);
    }

    public IMethod getMethod() {
      return m_method;
    }
  }

  private static String getResolvedSignature(String unresolvedSignature, IType contextType) {
    if (unresolvedSignature == null) {
      return null;
    }

    StringBuilder sigBuilder = new StringBuilder();
    getResolvedSignature(unresolvedSignature, contextType, sigBuilder);
    return sigBuilder.toString();
  }

  private static void getResolvedSignature(String unresolvedSignature, IType context, StringBuilder sigBuilder) {
    switch (Signature.getTypeSignatureKind(unresolvedSignature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append(unresolvedSignature.charAt(0));
        if (unresolvedSignature.length() > 1) {
          sigBuilder.append(resolveSignature(unresolvedSignature.substring(1), context));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        sigBuilder.append(Signature.C_ARRAY);
        getResolvedSignature(unresolvedSignature.substring(1), context, sigBuilder);
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        if (endsWith(unresolvedSignature, Signature.C_NAME_END)) {
          unresolvedSignature = unresolvedSignature.substring(0, unresolvedSignature.length() - 1);
        }
        sigBuilder.append(unresolvedSignature);
        break;
      case Signature.CLASS_TYPE_SIGNATURE:
        String[] typeArguments = Signature.getTypeArguments(unresolvedSignature);
        unresolvedSignature = Signature.getTypeErasure(unresolvedSignature);

        if (SignatureUtils.isUnresolved(unresolvedSignature)) {
          unresolvedSignature = resolveSignature(unresolvedSignature, context);
        }

        if (endsWith(unresolvedSignature, Signature.C_NAME_END)) {
          unresolvedSignature = unresolvedSignature.substring(0, unresolvedSignature.length() - 1);
        }
        sigBuilder.append(unresolvedSignature);
        if (typeArguments.length > 0) {
          sigBuilder.append(Signature.C_GENERIC_START);
          for (int i = 0; i < typeArguments.length; i++) {
            getResolvedSignature(typeArguments[i], context, sigBuilder);
          }
          sigBuilder.append(Signature.C_GENERIC_END);
        }
        sigBuilder.append(Signature.C_NAME_END);
        break;
    }
  }

  private static String resolveSignature(String unresolvedSig, final IType context) {
    String signatureSimpleName = Signature.getSignatureSimpleName(unresolvedSig);
    IType type = null;
    ICompilationUnit compilationUnit = context.getCompilationUnit();
    if (compilationUnit != null) {
      type = compilationUnit.findTypeBySimpleName(signatureSimpleName);
    }

    if (type != null) {
      return SignatureUtils.getResolvedSignature(type);
    }

    // cannot be found by the scope. search for fully qualified references
    final char[] signatureSimpleNameChar = signatureSimpleName.toCharArray();
    final String[] holder = new String[1];
    CompilationUnitDeclaration ast = ((CompilationUnit) context.getCompilationUnit()).getAst();
    ast.traverse(new ASTVisitor() {
      @Override
      public boolean visit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
        char[][] tokens = qualifiedTypeReference.tokens;
        char[] simpleName = tokens[tokens.length - 1];
        if (holder[0] == null && Arrays.equals(signatureSimpleNameChar, simpleName)) {
          if (qualifiedTypeReference.resolvedType instanceof ReferenceBinding) {
            holder[0] = SignatureUtils.getResolvedSignature(JavaModelUtils.bindingToType(qualifiedTypeReference.resolvedType, context.getLookupEnvironment()));
            return false;
          }
        }
        return true;
      }
    }, ast.scope, false);
    if (holder[0] != null) {
      return holder[0];
    }

    // also search in raw type args
    for (IMethod m : context.getMethods()) {
      IType rawDefType = findRawType(m.getReturnType(), signatureSimpleName);
      if (rawDefType != null) {
        return SignatureUtils.getResolvedSignature(rawDefType);
      }
    }
    return null;
  }

  private static IType findRawType(IType t, String simpleName) {
    if (t == null) {
      return null;
    }

    if (Objects.equals(simpleName, t.getSimpleName())) {
      return t;
    }

    for (IType ta : t.getTypeArguments()) {
      IType result = findRawType(ta, simpleName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static boolean endsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(stringToSearchIn.length() - 1) == charToFind;
  }
}
