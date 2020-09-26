/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.testing;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.normalizeNewLines;
import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.registerCompilationUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.opentest4j.AssertionFailedError;

/**
 * <h3>{@link SdkAssertions}</h3>
 *
 * @since 3.9.0 2013-04-05
 */
public final class SdkAssertions {

  private SdkAssertions() {
  }

  /**
   * Asserts that the primary class with given simple name, qualifier and source compiles within the given
   * {@link IJavaEnvironment}.
   *
   * @param env
   *          The {@link IJavaEnvironment} to do the compilation.
   * @param qualifier
   *          The qualifier of the class (e.g. org.eclipse.scout.sdk.test)
   * @param simpleName
   *          The simple name of the class (e.g. MyClass)
   * @param source
   *          The source of the whole compilation unit of the given primary class.
   * @return The {@link IType} representing the given type and source.
   * @throws AssertionError
   *           if the given type has compile errors within the given {@link IJavaEnvironment}.
   */
  public static IType assertNoCompileErrors(IJavaEnvironment env, String qualifier, String simpleName, CharSequence source) {
    return assertNoCompileErrors(registerCompilationUnit(env, qualifier, simpleName, source));
  }

  /**
   * Asserts that the compilation unit created using the specified {@link ICompilationUnitGenerator} compiles within the
   * specified {@link IJavaEnvironment}.
   *
   * @param env
   *          The {@link IJavaEnvironment} to do the compilation.
   * @param generator
   *          The {@link ICompilationUnitGenerator} to build.
   * @return The {@link IType} that corresponds to the main type of the specified {@link ICompilationUnitGenerator}.
   */
  public static IType assertNoCompileErrors(IJavaEnvironment env, ICompilationUnitGenerator<?> generator) {
    return assertNoCompileErrors(registerCompilationUnit(env, generator));
  }

  /**
   * Asserts that the specified {@link IType} has no compile errors.
   *
   * @param t
   *          The {@link IType} to check. Must not be {@code null}.
   * @return the input {@link IType}.
   * @throws AssertionError
   *           if there are compilation errors.
   */
  @SuppressWarnings("HardcodedLineSeparator")
  public static IType assertNoCompileErrors(IType t) {
    List<String> compileErrors = t.javaEnvironment().compileErrors(t);
    if (compileErrors.isEmpty()) {
      return t;
    }

    String errors = String.join("\n", compileErrors);
    String msg = "Compilation Failure: \n" + errors + "\nSource:\n" + t.requireCompilationUnit().source().get();
    throw new AssertionError(msg);
  }

  /**
   * Asserts that the source output of the given {@link ISourceGenerator} equals the content of the given file path.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the specified {@link ISourceGenerator} should be executed. Must not
   *          be {@code null}.
   * @param fileWithExpectedContent
   *          The absolute path on the java classpath where the reference file can be found.
   * @param generator
   *          The {@link ISourceGenerator} to use. Must not be {@code null}.
   */
  public static void assertEqualsRefFile(IJavaEnvironment env, String fileWithExpectedContent, ISourceGenerator<ISourceBuilder<?>> generator) {
    CharSequence src = Ensure.notNull(generator).toSource(identity(), new JavaBuilderContext(Ensure.notNull(env)));
    assertEqualsRefFile(fileWithExpectedContent, src);
  }

  /**
   * Asserts that the given actual content equals the content of the given file path.
   *
   * @param fileWithExpectedContent
   *          The absolute path on the java classpath where the reference file can be found.
   * @param actualContent
   *          The actual content to compare against.
   */
  public static void assertEqualsRefFile(String fileWithExpectedContent, CharSequence actualContent) {
    CharSequence refSrc;
    try (InputStream in = SdkAssertions.class.getClassLoader().getResourceAsStream(Ensure.notNull(fileWithExpectedContent))) {
      refSrc = Strings.fromInputStream(Ensure.notNull(in, "File '{}' could not be found on classpath.", fileWithExpectedContent), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
    CharSequence expected = normalizeNewLines(refSrc);
    CharSequence actual = normalizeNewLines(actualContent);
    if (!Strings.equals(expected, actual)) {
      throw new AssertionFailedError(null, expected, actual);
    }
  }

  /**
   * @see SdkAssertions#assertTypeExists(IType, String, String)
   */
  public static IType assertTypeExists(IType declaringType, String typeName) {
    return assertTypeExists(declaringType, typeName, null);
  }

  /**
   * fails if the {@code declaringType} does not contains an inner type named {@code typeName}.
   *
   * @return the type if found.
   */
  public static IType assertTypeExists(IType declaringType, String typeName, String message) {
    assertNotNull(declaringType);

    Optional<IType> type = declaringType.innerTypes().withSimpleName(typeName).first();
    if (type.isEmpty()) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Type '").append(typeName).append('\'');
        messageBuilder.append(" in type '").append(declaringType.name()).append('\'');
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return type.orElse(null);
  }

  public static IMethod assertMethodExist(IType type, String methodName, String[] parameterTypes) {
    return assertMethodExist(type, methodName, parameterTypes, null);
  }

  /**
   * fails if the {@code type} does not contain a method named {@code methodName}.
   *
   * @return the method if found
   */
  public static IMethod assertMethodExist(IType type, String methodName, String[] parameterTypes, String message) {
    String methodId = JavaTypes.createMethodIdentifier(methodName, Arrays.asList(parameterTypes));
    return type.methods().stream()
        .filter(method -> method.identifier().equals(methodId))
        .findAny()
        .<AssertionError> orElseThrow(() -> {
          String msg = message;
          if (msg == null) {
            StringBuilder messageBuilder = new StringBuilder("Method '").append(methodName).append('\'');
            messageBuilder.append(" in type '").append(type.name()).append('\'');
            messageBuilder.append(" does not exist! [parameters: ");
            for (int i = 0; i < parameterTypes.length; i++) {
              messageBuilder.append('\'').append(parameterTypes[i]).append('\'');
              if (i < parameterTypes.length - 1) {
                messageBuilder.append(", ");
              }
            }
            messageBuilder.append(']');
            msg = messageBuilder.toString();
          }
          throw new AssertionError(msg);
        });
  }

  public static void assertMethodReturnType(IMethod method, String expectedType) {
    assertMethodReturnType(method, expectedType, "Method return type not equal!");
  }

  public static void assertMethodReturnType(IMethod method, String expectedType, String message) {
    String typeRef = method.requireReturnType().reference();
    assertEquals(expectedType, typeRef, message);
  }

  /**
   * @see SdkAssertions#assertFieldExist(IType, String, String)
   */
  public static IField assertFieldExist(IType type, String fieldName) {
    return assertFieldExist(type, fieldName, null);
  }

  /**
   * fails if the type does not have a field named {@code fieldName}.
   *
   * @return the field if it exists.
   */
  public static IField assertFieldExist(IType type, String fieldName, String message) {
    return type.fields().withName(fieldName).first()
        .orElseThrow(() -> {
          String msg = message;
          if (msg == null) {
            StringBuilder messageBuilder = new StringBuilder("Field '").append(fieldName).append('\'');
            messageBuilder.append(" in type '").append(type.name()).append('\'');
            messageBuilder.append(" does not exist!");
            msg = messageBuilder.toString();
          }
          return new AssertionError(msg);
        });
  }

  public static void assertFieldType(IField field, String expectedType) {
    assertFieldType(field, expectedType, "Field '" + field.elementName() + "' does not have the expected type!");
  }

  public static void assertFieldType(IField field, String expectedType, String message) {
    String typeRef = field.dataType().reference();
    assertEquals(expectedType, typeRef, message);
  }

  public static void assertHasSuperClass(IType type, String expectedSuperClass) {
    assertHasSuperClass(type, expectedSuperClass, "Type '" + type.name() + "' does not have expected supertype!");
  }

  public static void assertHasSuperClass(IType type, String expectedSuperClass, String message) {
    String refSuperTypeSig = type.requireSuperClass().reference();
    assertEquals(expectedSuperClass, refSuperTypeSig, message);
  }

  public static void assertHasSuperInterfaces(IType type, String[] interfaceTypes) {
    assertHasSuperInterfaces(type, interfaceTypes, "Type '" + type.name() + "' does not have the same interfaces!");
  }

  public static void assertHasSuperInterfaces(IType type, String[] expectedInterfaces, String message) {
    List<String> interfaces = type.superInterfaces()
        .map(IType::reference)
        .collect(toList());
    assertEquals(Arrays.asList(expectedInterfaces), interfaces, message);
  }

  /**
   * Asserts that the given annotation exists on the given object.
   */
  public static IAnnotation assertAnnotation(IAnnotatable annotatable, String fqAnnotationTypeName) {
    return annotatable.annotations().withName(fqAnnotationTypeName).first()
        .orElseThrow(() -> {
          StringBuilder message = new StringBuilder("Element '");
          message.append(annotatable.elementName());
          message.append("' does not have the expected annotation '").append(fqAnnotationTypeName).append("'.");
          return new AssertionError(message.toString());
        });
  }

  public static void assertHasFlags(IMember member, int flags) {
    assertHasFlags(member, flags, null);
  }

  public static void assertHasFlags(IMember member, int flags, String message) {
    int memberFlags = member.flags();
    if ((flags & memberFlags) != flags) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("member '").append(member.elementName()).append('\'');
        messageBuilder.append(" has flags [").append(Flags.toString(memberFlags)).append("] expected [").append(Flags.toString(flags)).append("]!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }
}
