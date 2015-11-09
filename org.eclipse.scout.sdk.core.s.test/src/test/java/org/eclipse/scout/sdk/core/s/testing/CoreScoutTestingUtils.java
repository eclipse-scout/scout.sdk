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
package org.eclipse.scout.sdk.core.s.testing;

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.JavaEnvironmentBuilder;
import org.junit.Assert;

/**
 * helpers used for scout core unit tests
 */
public final class CoreScoutTestingUtils {

  private CoreScoutTestingUtils() {
  }

  public static IJavaEnvironment createClientJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .withSourceFolder("src/main/client")
        .withSourceFolder("src/main/shared")
        .build();
  }

  /**
   * @return a {@link org.eclipse.scout.sdk.core.model.api.IJavaEnvironment} for org.eclipse.*.shared tests, without the
   *         org.eclipse.scout.rt.client dependency
   */
  public static IJavaEnvironment createSharedJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .without(".*" + Pattern.quote("org.eclipse.scout.rt.client") + ".*")
        .withSourceFolder("src/main/shared")
        .build();
  }

  public static IType createPageDataAssertNoCompileErrors(String modelFqn) {
    return createDtoAssertNoCompileErrors(modelFqn, false);
  }

  public static IType createRowDataAssertNoCompileErrors(String modelFqn) {
    return createDtoAssertNoCompileErrors(modelFqn, true);
  }

  private static IType createDtoAssertNoCompileErrors(String modelFqn, boolean rowData) {
    // get model type
    IType modelType = createClientJavaEnvironment().findType(modelFqn);
    DataAnnotationDescriptor dataAnnotation = DtoUtils.getDataAnnotationDescriptor(modelType);

    // build classpath for shared project
    IJavaEnvironment sharedEnv = createSharedJavaEnvironment();

    // build source
    ICompilationUnitSourceBuilder cuSrc;
    if (rowData) {
      cuSrc = DtoUtils.createTableRowDataBuilder(modelType, dataAnnotation, sharedEnv);
    }
    else {
      cuSrc = DtoUtils.createPageDataBuilder(modelType, dataAnnotation, sharedEnv);
    }
    String source = DtoUtils.createJavaCode(cuSrc, sharedEnv, "\n", null);

    // ensure it compiles and get model of dto
    sharedEnv.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), new StringBuilder(source));
    sharedEnv.reload();
    ICompilationUnit dtoIcu = sharedEnv.findType(cuSrc.getMainType().getFullyQualifiedName()).compilationUnit();
    Assert.assertNull(CoreTestingUtils.getCompileErrors(sharedEnv, dtoIcu.mainType().name()));

    return dtoIcu.mainType();
  }

  public static IType createFormDataAssertNoCompileErrors(String modelFqn) {
    // get model type
    IType modelType = createClientJavaEnvironment().findType(modelFqn);

    // build classpath for shared project
    IJavaEnvironment sharedEnv = createSharedJavaEnvironment();

    // build source
    FormDataAnnotationDescriptor formDataAnnotation = DtoUtils.getFormDataAnnotationDescriptor(modelType);
    ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, formDataAnnotation, sharedEnv);
    String source = DtoUtils.createJavaCode(cuSrc, sharedEnv, "\n", null);

    // ensure it compiles and get model of dto
    sharedEnv.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), new StringBuilder(source));
    sharedEnv.reload();
    ICompilationUnit dtoIcu = sharedEnv.findType(cuSrc.getMainType().getFullyQualifiedName()).compilationUnit();
    Assert.assertNull(CoreTestingUtils.getCompileErrors(sharedEnv, dtoIcu.mainType().name()));

    return dtoIcu.mainType();
  }

  /**
   * fails if the {@link org.eclipse.scout.sdk.core.model.api.IAnnotatable} does not have an order annotation with the
   * <code>orderNr</code>.
   *
   * @param message
   * @param annotatable
   * @param orderNr
   */
  public static void assertOrderAnnotation(String message, IAnnotatable annotatable, double orderNr) {
    OrderAnnotation orderAnnotation = annotatable.annotations().withManagedWrapper(OrderAnnotation.class).first();
    if (orderAnnotation == null) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("No @Order annotation found on member '");
        messageBuilder.append(annotatable.elementName()).append("'.");
        messageBuilder.append(" Expected: order value '").append(orderNr).append("'!");
        message = messageBuilder.toString();
      }
      Assert.fail(message);
      return;
    }

    double memberOrderNr = orderAnnotation.value();
    if (orderNr != memberOrderNr) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Order annotation not equal: exptected '").append(orderNr).append("'; found on member '");
        messageBuilder.append(annotatable.elementName()).append("'");
        messageBuilder.append(" is '").append(memberOrderNr).append("'!");
        message = messageBuilder.toString();
      }
      Assert.fail(message);
    }

    Assert.assertEquals(message, memberOrderNr, orderNr, 0.00001);
  }

  /**
   * @see org.eclipse.scout.sdk.core.testing.SdkAssert#assertOrderAnnotation(String, IAnnotatable, Double)
   */
  public static void assertOrderAnnotation(IAnnotatable annotatable, Double orderNr) {
    assertOrderAnnotation(null, annotatable, orderNr);
  }

}
