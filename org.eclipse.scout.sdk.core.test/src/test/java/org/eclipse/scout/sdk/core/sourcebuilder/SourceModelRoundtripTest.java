/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder;

import java.io.IOException;

import org.eclipse.scout.sdk.core.fixture.ClassWithMembers;
import org.eclipse.scout.sdk.core.importcollector.EmptyImportCollector;
import org.eclipse.scout.sdk.core.importcollector.ImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link SourceModelRoundtripTest}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class SourceModelRoundtripTest {

  @Test
  public void testMembersOfSourceClass() throws IOException {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType type = env.findType(ClassWithMembers.class.getName());

    String src1 = CoreUtils.inputStreamToString(ClassWithMembers.class.getResourceAsStream("/ClassWithMembers_source.txt"), "UTF-8").toString();

    StringBuilder buf = new StringBuilder();
    IImportValidator validator = new ImportValidator(new ImportCollector());
    //v.addImport(type.getName());
    new CompilationUnitSourceBuilder(type.compilationUnit()).createSource(buf, "\n", new PropertyMap(), validator);
    String src2 = buf.toString();
    Assert.assertEquals(CoreTestingUtils.removeWhitespace(src1), CoreTestingUtils.removeWhitespace(src2));
  }

  @Test
  public void testMembersOfBinaryClass() throws IOException {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironmentWithBinaries();
    IType type = env.findType(ClassWithMembers.class.getName());

    String src1 = CoreUtils.inputStreamToString(ClassWithMembers.class.getResourceAsStream("/ClassWithMembers_binary.txt"), "UTF-8").toString();

    StringBuilder buf = new StringBuilder();
    new CompilationUnitSourceBuilder(type.compilationUnit()).createSource(buf, "\n", new PropertyMap(), new ImportValidator(new EmptyImportCollector()));
    String src2 = buf.toString();

    Assert.assertEquals(CoreTestingUtils.removeWhitespace(src1), CoreTestingUtils.removeWhitespace(src2));
  }
}
