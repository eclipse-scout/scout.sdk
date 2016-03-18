/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.test;

import java.lang.reflect.Method;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;
import org.junit.Test;

/**
 * <h3>{@link ApiTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ApiTest {
  @Test
  public void testAstUtilsApi() throws NoSuchMethodException, SecurityException {
    ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setBindingsRecovery(false);
    parser.setIgnoreMethodBodies(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setResolveBindings(false);
    parser.setSource("public class Test {}".toCharArray());
    ASTNode node = parser.createAST(new NullProgressMonitor());
    AST ast = node.getAST();
    Object resolver = Validate.notNull(AstUtils.getBindingResolver(ast));
    AstUtils.getCompilationUnitScope(ast, resolver);
    Method m = resolver.getClass().getDeclaredMethod("getTypeBinding", TypeBinding.class);
    Validate.notNull(m);
  }

  @Test
  public void testLinkedModeUIApi() throws NoSuchMethodException, SecurityException {
    final Method m = LinkedModeUI.class.getDeclaredMethod("triggerContentAssist");
    Validate.notNull(m);
  }
}
