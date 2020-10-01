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
package org.eclipse.scout.sdk.s2e;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ApiTest}</h3>
 *
 * @since 5.2.0
 */
public class ApiTest {
  @Test
  public void testAstUtilsApi() throws NoSuchMethodException {
    ASTParser parser = ASTParser.newParser(AST.JLS14);
    parser.setBindingsRecovery(false);
    parser.setIgnoreMethodBodies(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setResolveBindings(false);
    parser.setSource("public class Test {}".toCharArray());
    ASTNode node = parser.createAST(new NullProgressMonitor());
    AST ast = node.getAST();
    Object resolver = AstUtils.getBindingResolver(ast);
    assertNotNull(resolver);
    AstUtils.getCompilationUnitScope(resolver);

    // test for org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstNodeFactory.resolveTypeBinding(String)
    Method m = resolver.getClass().getDeclaredMethod("getTypeBinding", TypeBinding.class);
    assertNotNull(m);
  }
}
