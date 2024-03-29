/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.CharBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.java.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaEnvironmentWithEcjTest}</h3>
 *
 * @since 9.0.0
 */
public class JavaEnvironmentWithEcjTest {

  /**
   * Tests that registerCompilationUnitOverride can be called on closed environments and has no effect (the environment
   * stays closed)
   */
  @Test
  public void testRegisterAfterCloseKeepsEnvironmentUntouched() throws ReflectiveOperationException {
    var pck = "xx.yy";
    var className = "Test";
    var closedEnv = createClosedJavaEnvironment();
    assertEnvironmentClosed(closedEnv);
    closedEnv.registerCompilationUnitOverride(("package " + pck + "; public class " + className + " {}").toCharArray(),
        new CompilationUnitInfo(null, null, className + JavaTypes.JAVA_FILE_SUFFIX));
    assertEnvironmentClosed(closedEnv);
    closedEnv.registerCompilationUnitOverride(("package " + pck + "; public class " + className + " {}").toCharArray(),
        new CompilationUnitInfo(null, pck, className + JavaTypes.JAVA_FILE_SUFFIX));

    var fqn = pck + '.' + className;
    assertThrows(IllegalArgumentException.class, () -> closedEnv.findType(fqn));
    closedEnv.reload();
    assertNotNull(closedEnv.findType(fqn));
    assertTrue(closedEnv.isInitialized());
  }

  private static JavaEnvironmentWithEcj createClosedJavaEnvironment() {
    var holder = new AtomicReference<IJavaEnvironment>();
    new CoreJavaEnvironmentBinaryOnlyFactory().accept(holder::set);
    return (JavaEnvironmentWithEcj) holder.get().unwrap();
  }

  private static void assertEnvironmentClosed(JavaEnvironmentWithEcj candidate) throws ReflectiveOperationException {
    assertNotNull(candidate);
    assertEquals(0, JavaEnvironmentWithEcjTest.<Map<Object, JavaElementSpi>> fieldValue(candidate, "m_elements").size());
    assertEquals(0, JavaEnvironmentWithEcjTest.<Map<ReferenceBinding, Map<String, ElementValuePair>>> fieldValue(candidate, "m_evpCache").size());
    assertEquals(0, JavaEnvironmentWithEcjTest.<Map<TypeBinding, Map<String, MemberValuePair>>> fieldValue(candidate, "m_mvpCache").size());
    assertEquals(0, JavaEnvironmentWithEcjTest.<Map<CharBuffer, char[]>> fieldValue(candidate, "m_sourceCache").size());
    assertFalse(JavaEnvironmentWithEcjTest.<Collection<? extends ClasspathEntry>> fieldValue(candidate, "m_rawClassPath").isEmpty()); // classpath is preserved so that the environment can be reinitialized
    assertFalse(JavaEnvironmentWithEcjTest.<FinalValue<EcjAstCompiler>> fieldValue(candidate, "m_compiler").isSet());
    assertFalse(JavaEnvironmentWithEcjTest.<FinalValue<List<ClasspathSpi>>> fieldValue(candidate, "m_classpath").isSet());
  }

  @SuppressWarnings("unchecked")
  private static <T> T fieldValue(Object instance, String fieldName) throws ReflectiveOperationException {
    var field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return (T) field.get(instance);
  }
}
