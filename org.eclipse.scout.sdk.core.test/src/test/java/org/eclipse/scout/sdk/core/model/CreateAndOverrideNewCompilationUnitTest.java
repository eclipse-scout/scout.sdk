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
package org.eclipse.scout.sdk.core.model;

import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.JavaEnvironmentBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CreateAndOverrideNewCompilationUnitTest}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class CreateAndOverrideNewCompilationUnitTest {

  @Test
  public void testCreateNewTypeWithErrors() {
    IJavaEnvironment env = new JavaEnvironmentBuilder().build();//empty

    //add an unresolved type error
    ICompilationUnitSourceBuilder cuSrc = createBaseClass();
    cuSrc.getMainType().getMethods().get(0).setReturnTypeSignature(Signature.createTypeSignature("FooBar"));
    StringBuilder buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);

    ICompilationUnit cu = env.findType("a.b.c.BaseClass").getCompilationUnit();
    String expected = "" +
        "package a.b.c;\n" +
        "public class BaseClass{\n" +
        "  public FooBar run(){\n" +
        "    System.out.println(\"base class\");\n" +
        "  }\n" +
        "}\n";
    Assert.assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.getSource().toString()));

    Assert.assertNotNull(env.getCompileErrors("a.b.c.BaseClass"));

    //now fix the unresolved type error
    cuSrc.getMainType().getMethods().get(0).setReturnTypeSignature(Signature.createTypeSignature("void"));
    buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    env.reload();
    cu = env.findType("a.b.c.BaseClass").getCompilationUnit();

    Assert.assertNull(env.getCompileErrors("a.b.c.BaseClass"));
  }

  @Test
  public void testCreateNewType() {
    IJavaEnvironment env = new JavaEnvironmentBuilder().build();//empty

    ICompilationUnitSourceBuilder cuSrc = createBaseClass();
    StringBuilder buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    ICompilationUnit cu = env.findType("a.b.c.BaseClass").getCompilationUnit();

    String expected = "" +
        "package a.b.c;\n" +
        "public class BaseClass{\n" +
        "  public void run(){\n" +
        "    System.out.println(\"base class\");\n" +
        "  }\n" +
        "}\n";
    Assert.assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.getSource().toString()));

    //now read the type from the env
    IType t2 = env.findType("a.b.c.BaseClass");
    Assert.assertEquals(cu.getMainType().methods().withName("run").first().getSource().toString(), t2.methods().withName("run").first().getSource().toString());
  }

  @Test
  public void testCreateNewSubType() {
    IJavaEnvironment env = new JavaEnvironmentBuilder().build();//empty

    ICompilationUnitSourceBuilder cuSrc = createBaseClass();
    StringBuilder buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    ICompilationUnit cu = env.findType("a.b.c.BaseClass").getCompilationUnit();

    //and now add a subclass

    cuSrc = createSubClass();
    buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    cu = env.findType("a.b.c.d.SubClass").getCompilationUnit();

    String expected = "" +
        "package a.b.c.d;\n" +
        "import a.b.c.BaseClass;\n" +
        "public class SubClass extends BaseClass{\n" +
        "  @Override\n" +
        "  public void run(){\n" +
        "    super.run();\n" +
        "    System.out.println(\"sub class\");\n" +
        "  }\n" +
        "}\n";
    Assert.assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.getSource().toString()));
  }

  @Test
  public void testCreateExistingTypes() {
    IJavaEnvironment env = new JavaEnvironmentBuilder().build();//empty

    //create base type
    ICompilationUnitSourceBuilder cuSrc = createBaseClass();
    StringBuilder buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    ICompilationUnit cu = env.findType("a.b.c.BaseClass").getCompilationUnit();

    //create sub type
    cuSrc = createSubClass();
    buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    cu = env.findType("a.b.c.d.SubClass").getCompilationUnit();

    //re-create modified base type

    cuSrc = createBaseClass();
    cuSrc.getMainType().getMethods().get(0).setBody(new RawSourceBuilder("System.out.println(\"modified base class\");"));
    buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    env.reload();
    cu = env.findType("a.b.c.BaseClass").getCompilationUnit();

    String expected = "" +
        "package a.b.c;\n" +
        "public class BaseClass{\n" +
        "  public void run(){\n" +
        "    System.out.println(\"modified base class\");\n" +
        "  }\n" +
        "}\n";
    Assert.assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.getSource().toString()));

    //now read the type from the env
    IType t2 = env.findType("a.b.c.BaseClass");
    Assert.assertEquals(cu.getMainType().methods().withName("run").first().getSource().toString(), t2.methods().withName("run").first().getSource().toString());

    //and again re-create modified base type

    cuSrc = createBaseClass();
    cuSrc.getMainType().getMethods().get(0).setBody(new RawSourceBuilder("System.out.println(\"again modified base class\");"));
    buf = new StringBuilder();
    cuSrc.createSource(buf, "\n", null, new ImportValidator(env));
    env.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), buf);
    env.reload();
    cu = env.findType("a.b.c.BaseClass").getCompilationUnit();

    expected = "" +
        "package a.b.c;\n" +
        "public class BaseClass{\n" +
        "  public void run(){\n" +
        "    System.out.println(\"again modified base class\");\n" +
        "  }\n" +
        "}\n";
    Assert.assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.getSource().toString()));

    //now read the type from the env
    t2 = env.findType("a.b.c.BaseClass");
    Assert.assertEquals(cu.getMainType().methods().withName("run").first().getSource().toString(), t2.methods().withName("run").first().getSource().toString());

  }

  private static ICompilationUnitSourceBuilder createBaseClass() {
    ICompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder("BaseClass.java", "a.b.c");

    ITypeSourceBuilder typeSrc = new TypeSourceBuilder("BaseClass");
    cuSrc.addType(typeSrc);
    typeSrc.setFlags(Flags.AccPublic);

    IMethodSourceBuilder mSrc = new MethodSourceBuilder("run");
    typeSrc.addMethod(mSrc);
    mSrc.setFlags(Flags.AccPublic);
    mSrc.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    mSrc.setBody(new RawSourceBuilder("System.out.println(\"base class\");"));

    return cuSrc;
  }

  private static ICompilationUnitSourceBuilder createSubClass() {
    ICompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder("SubClass.java", "a.b.c.d");

    ITypeSourceBuilder typeSrc = new TypeSourceBuilder("SubClass");
    cuSrc.addType(typeSrc);
    typeSrc.setFlags(Flags.AccPublic);
    typeSrc.setSuperTypeSignature(Signature.createTypeSignature("a.b.c.BaseClass"));

    IMethodSourceBuilder mSrc = new MethodSourceBuilder("run");
    typeSrc.addMethod(mSrc);
    mSrc.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
    mSrc.setFlags(Flags.AccPublic);
    mSrc.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    mSrc.setBody(new RawSourceBuilder("super.run();\nSystem.out.println(\"sub class\");"));

    return cuSrc;
  }
}
