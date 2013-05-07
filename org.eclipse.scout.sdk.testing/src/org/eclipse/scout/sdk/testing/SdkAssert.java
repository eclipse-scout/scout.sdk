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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.FieldFilters;
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
  static public void assertExist(IJavaElement element) {
    assertExist(null, element);
  }

  /**
   * fails if the <code>element</code> does not exist.
   * 
   * @param message
   * @param element
   */
  static public void assertExist(String message, IJavaElement element) {
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
  static public void assertNotExist(IJavaElement element) {
    assertNotExist(null, element);
  }

  /**
   * fails if the <code>element</code> does exist.
   * 
   * @param message
   * @param element
   */
  static public void assertNotExist(String message, IJavaElement element) {
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
  static public IType assertTypeExists(String fullyQualifiedTypeName) {
    return assertTypeExists((String) null, fullyQualifiedTypeName);
  }

  /**
   * fails if no type with the <code>fullyQualifiedTypeName</code> exists.
   * 
   * @param message
   * @param fullyQualifiedTypeName
   * @return the type if found.
   */
  static public IType assertTypeExists(String message, String fullyQualifiedTypeName) {
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
   * @see SdkAssert#assertTypeExists(String, ICompilationUnit, String)
   */
  static public IType assertTypeExists(ICompilationUnit icu, String typeName) {
    return assertTypeExists(null, icu, typeName);
  }

  /**
   * fails if the <code>icu</code> does not contains an inner type named <code>typeName</code>.
   * 
   * @param message
   * @param icu
   * @param typeName
   * @return the type if found.
   */
  static public IType assertTypeExists(String message, ICompilationUnit icu, String typeName) {
    IType type = icu.getType(typeName);
    if (!TypeUtility.exists(type)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(typeName).append("'");
        if (icu != null) {
          messageBuilder.append(" in compilation unit '").append(icu.getElementName()).append("'");
        }
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
  static public IType assertTypeExists(IType declaringType, String typeName) {
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
  static public IType assertTypeExists(String message, IType declaringType, String typeName) {
    IType type = declaringType.getType(typeName);
    if (!TypeUtility.exists(type)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(typeName).append("'");
        if (declaringType != null) {
          messageBuilder.append(" in type '").append(declaringType.getElementName()).append("'");
        }
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
  static public IMethod assertMethodExist(IType type, String methodName) {
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
  static public IMethod assertMethodExist(String message, IType type, String methodName) {
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

  /**
   * @see SdkAssert#assertMethodExistInSuperTypeHierarchy(String, IType, String)
   */
  static public IMethod assertMethodExistInSuperTypeHierarchy(IType type, String methodName) {
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
  static public IMethod assertMethodExistInSuperTypeHierarchy(String message, IType type, String methodName) {
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

  /**
   * @see SdkAssert#assertFieldExist(String, IType, String)
   */
  static public IField assertFieldExist(IType type, String fieldName) {
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
  static public IField assertFieldExist(String message, IType type, String fieldName) {
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

  /**
   * @see SdkAssert#assertSerialVersionUidExists(String, IType)
   */
  static public IField assertSerialVersionUidExists(IType type) throws JavaModelException {
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
  static public IField assertSerialVersionUidExists(String message, IType type) throws JavaModelException {
    IField field = assertFieldExist(message, type, "serialVersionUID");
    assertPrivate(message, field).assertStatic().assertFinal();
    return field;
  }

  /**
   * @see SdkAssert#assertOrderAnnotation(String, IAnnotatable, Double)
   */
  static public void assertOrderAnnotation(IAnnotatable annotatable, Double orderNr) throws JavaModelException {
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
  static public void assertOrderAnnotation(String message, IAnnotatable annotatable, Double orderNr) throws JavaModelException {
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

    assertEquals(message, (Double) memberOrderNr, (Double) orderNr);
  }

  /**
   * @see SdkAssert#assertSameParent(String, IJavaElement...)
   */
  static public void assertSameParent(IJavaElement... elements) {
    assertSameParent(null, elements);
  }

  /**
   * fails if the elements does not have the same parent element.
   * 
   * @param message
   * @param elements
   */
  static public void assertSameParent(String message, IJavaElement... elements) {
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
  static public void assertElementSequenceInSource(IMember... elements) throws JavaModelException {
    assertElementSequenceInSource(null, elements);
  }

  /**
   * fails if the elements does not appear in the arrays sequence in the source code.
   * 
   * @param message
   * @param elements
   * @throws JavaModelException
   */
  static public void assertElementSequenceInSource(String message, IMember... elements) throws JavaModelException {
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

  static public void assertHasFlags(IMember member, int flags) throws JavaModelException {
    assertHasFlags(null, member, flags);
  }

  static public void assertHasFlags(String message, IMember member, int flags) throws JavaModelException {
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
  static public void assertServiceProxyRegistered(IProject project, IType serviceInterface) {
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
  static public void assertServiceRegistered(IProject project, IType serviceInterface) {
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
  static public FlagAssert assertPrivate(IMember member) throws JavaModelException {
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
  static public FlagAssert assertPrivate(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertPrivate();
  }

  /**
   * @see SdkAssert#assertProtected(String, IMember)
   */
  static public FlagAssert assertProtected(IMember member) throws JavaModelException {
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
  static public FlagAssert assertProtected(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertProtected();
  }

  /**
   * @see SdkAssert#assertPublic(String, IMember)
   */
  static public FlagAssert assertPublic(IMember member) throws JavaModelException {
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
  static public FlagAssert assertPublic(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertPublic();
  }

  /**
   * @see SdkAssert#assertAbstract(String, IMember)
   */
  static public FlagAssert assertAbstract(IMember member) throws JavaModelException {
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
  static public FlagAssert assertAbstract(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertAbstract();
  }

  /**
   * @see SdkAssert#assertFinal(String, IMember)
   */
  static public FlagAssert assertFinal(IMember member) throws JavaModelException {
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
  static public FlagAssert assertFinal(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertFinal();
  }

  /**
   * @see SdkAssert#assertInterface(String, IMember)
   */
  static public FlagAssert assertInterface(IMember member) throws JavaModelException {
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
  static public FlagAssert assertInterface(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertInterface();
  }

  /**
   * @see SdkAssert#assertStatic(String, IMember)
   */
  static public FlagAssert assertStatic(IMember member) throws JavaModelException {
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
  static public FlagAssert assertStatic(String message, IMember member) throws JavaModelException {
    return new FlagAssert(message, member).assertStatic();
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

}
