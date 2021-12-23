/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.testing;

import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.normalizeNewLines;
import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.registerCompilationUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.apidef.Api.ChildElementType;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
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
   * @param source
   *          The source of the whole compilation unit of the given primary class.
   * @param qualifier
   *          The qualifier of the class (e.g. org.eclipse.scout.sdk.test)
   * @param simpleName
   *          The simple name of the class (e.g. MyClass)
   * @return The {@link IType} representing the given type and source.
   * @throws AssertionError
   *           if the given type has compile errors within the given {@link IJavaEnvironment}.
   */
  public static IType assertNoCompileErrors(IJavaEnvironment env, CharSequence source, String qualifier, String simpleName) {
    return assertNoCompileErrors(registerCompilationUnit(env, source, qualifier, simpleName));
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
    var compileErrors = t.javaEnvironment().compileErrors(t);
    if (compileErrors.isEmpty()) {
      return t;
    }

    var errors = String.join("\n", compileErrors);
    var msg = "Compilation Failure: \n" + errors + "\nSource:\n" + t.requireCompilationUnit().source().orElseThrow();
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
    try (var in = SdkAssertions.class.getClassLoader().getResourceAsStream(Ensure.notNull(fileWithExpectedContent))) {
      refSrc = Strings.fromInputStream(Ensure.notNull(in, "File '{}' could not be found on classpath.", fileWithExpectedContent), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
    var expected = normalizeNewLines(refSrc);
    var actual = normalizeNewLines(actualContent);
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

    var type = declaringType.innerTypes().withSimpleName(typeName).first();
    if (type.isEmpty()) {
      if (message == null) {
        var messageBuilder = new StringBuilder("Type '").append(typeName).append('\'');
        messageBuilder.append(" in type '").append(declaringType.name()).append('\'');
        messageBuilder.append(" does not exist!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
    return type.orElse(null);
  }

  public static IMethod assertMethodExist(IType type, String methodName) {
    return assertMethodExist(type, methodName, new String[0]);
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
    var methodId = JavaTypes.createMethodIdentifier(methodName, Arrays.asList(parameterTypes));
    return type.methods().stream()
        .filter(method -> method.identifier(true).equals(methodId))
        .findAny()
        .<AssertionError> orElseThrow(() -> {
          var msg = message;
          if (msg == null) {
            var messageBuilder = new StringBuilder("Method '").append(methodName).append('\'');
            messageBuilder.append(" in type '").append(type.name()).append('\'');
            messageBuilder.append(" does not exist! [parameters: ");
            for (var i = 0; i < parameterTypes.length; i++) {
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
    var typeRef = method.requireReturnType().reference();
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
          var msg = message;
          if (msg == null) {
            var messageBuilder = new StringBuilder("Field '").append(fieldName).append('\'');
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
    var typeRef = field.dataType().reference();
    assertEquals(expectedType, typeRef, message);
  }

  public static void assertHasSuperClass(IType type, String expectedSuperClass) {
    assertHasSuperClass(type, expectedSuperClass, "Type '" + type.name() + "' does not have expected supertype!");
  }

  public static void assertHasSuperClass(IType type, String expectedSuperClass, String message) {
    var refSuperTypeSig = type.requireSuperClass().reference();
    assertEquals(expectedSuperClass, refSuperTypeSig, message);
  }

  public static void assertHasSuperInterfaces(IType type, String[] interfaceTypes) {
    assertHasSuperInterfaces(type, interfaceTypes, "Type '" + type.name() + "' does not have the same interfaces!");
  }

  public static void assertHasSuperInterfaces(IType type, String[] expectedInterfaces, String message) {
    var interfaces = type.superInterfaces()
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
          var message = new StringBuilder("Element '");
          message.append(annotatable.elementName());
          message.append("' does not have the expected annotation '").append(fqAnnotationTypeName).append("'.");
          return new AssertionError(message.toString());
        });
  }

  public static void assertHasFlags(IMember member, int flags) {
    assertHasFlags(member, flags, null);
  }

  public static void assertHasFlags(IMember member, int flags, String message) {
    var memberFlags = member.flags();
    if ((flags & memberFlags) != flags) {
      if (message == null) {
        var messageBuilder = new StringBuilder("member '").append(member.elementName()).append('\'');
        messageBuilder.append(" has flags [").append(Flags.toString(memberFlags)).append("] expected [").append(Flags.toString(flags)).append("]!");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }

  public static <A extends IApiSpecification> void assertApiValid(Class<A> api, IJavaEnvironment environment) {
    assertApiValid(api, environment, null);
  }

  public static <A extends IApiSpecification> void assertApiValid(Class<A> apiSpec, IJavaEnvironment environment, ApiValidationFilter<A> validateOthers) {
    var api = environment.requireApi(apiSpec);
    var errors = Api.dump(api).entrySet().stream()
        .map(e -> collectApiClassErrors(e.getKey(), e.getValue(), environment, api, validateOthers))
        .flatMap(Collection::stream)
        .collect(joining(lineSeparator()));
    if (errors.isEmpty()) {
      return;
    }
    fail("API validation failed with the following errors:" + lineSeparator() + errors + lineSeparator());
  }

  static <A extends IApiSpecification> Collection<String> collectApiClassErrors(String fqn, Map<ChildElementType, Map<String, String>> children, IJavaEnvironment env, A api, ApiValidationFilter<A> validateOthers) {
    var typeOpt = env.findType(fqn);
    if (typeOpt.isEmpty()) {
      return List.of(" - Type '" + fqn + "' could not be found.");
    }

    var type = typeOpt.orElseThrow();
    var errors = new ArrayList<String>();
    errors.addAll(testMethods(children.get(ChildElementType.METHOD_NAME).values(), type));
    errors.addAll(testMethods(children.get(ChildElementType.ANNOTATION_ELEMENT_NAME).values(), type));
    var notMatchingConvention = children.get(ChildElementType.OTHER);
    if (notMatchingConvention == null || notMatchingConvention.isEmpty()) {
      return errors;
    }

    if (validateOthers != null) {
      // if an validation strategy is present: reduce to the
      notMatchingConvention = validateOthers.invalid(notMatchingConvention, type, api);
      if (notMatchingConvention == null || notMatchingConvention.isEmpty()) {
        return errors;
      }
    }

    errors.add(" - The following methods in type '" + fqn + "' do not follow the naming convention: " + notMatchingConvention.values());
    return errors;
  }

  static Collection<String> testMethods(Collection<String> methodNames, IType owner) {
    if (methodNames == null || methodNames.isEmpty()) {
      return emptyList();
    }
    return methodNames.stream()
        .filter(name -> !owner.methods().withName(name).existsAny())
        .map(name -> " - Method '" + name + "' cannot be found in type '" + owner.name() + "'.")
        .collect(toList());
  }

  @FunctionalInterface
  public interface ApiValidationFilter<A extends IApiSpecification> {
    Map<String, String> invalid(Map<String, String> candidates, IType type, A api);
  }
}
