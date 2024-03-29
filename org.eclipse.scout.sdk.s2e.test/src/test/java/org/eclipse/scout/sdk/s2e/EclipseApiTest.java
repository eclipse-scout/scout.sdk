/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link EclipseApiTest}</h3>
 *
 * @since 5.2.0
 */
public class EclipseApiTest {
  @Test
  public void testAstUtilsApi() throws NoSuchMethodException {
    var parser = ASTParser.newParser(AST.getJLSLatest());
    parser.setBindingsRecovery(false);
    parser.setIgnoreMethodBodies(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setResolveBindings(false);
    parser.setSource("public class Test {}".toCharArray());
    var node = parser.createAST(new NullProgressMonitor());
    var ast = node.getAST();
    var resolver = AstUtils.getBindingResolver(ast);
    assertNotNull(resolver);
    AstUtils.getCompilationUnitScope(resolver);

    // test for org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstNodeFactory.resolveTypeBinding(String)
    var m = resolver.getClass().getDeclaredMethod("getTypeBinding", TypeBinding.class);
    assertNotNull(m);
  }
}
