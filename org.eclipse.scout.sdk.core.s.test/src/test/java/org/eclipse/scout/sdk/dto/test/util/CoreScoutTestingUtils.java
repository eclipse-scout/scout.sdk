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
package org.eclipse.scout.sdk.dto.test.util;

import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.s.util.ScoutUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.core.util.JavaEnvironmentBuilder;
import org.junit.Assert;

/**
 * helpers used for scout core unit tests
 */
public final class CoreScoutTestingUtils {

  private CoreScoutTestingUtils() {
  }

  public static IJavaEnvironment createClientJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withExcludeScoutSdk()
        .withSourceFolder("src/main/client")
        .withSourceFolder("src/main/shared")
        .build();
  }

  /**
   * @return a {@link IJavaEnvironment} for org.eclipse.*.shared tests, without the org.eclipse.scout.rt.client
   *         dependency
   */
  public static IJavaEnvironment createSharedJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withExcludeScoutSdk()
        .withExclude(".*" + Pattern.quote("org.eclipse.scout.rt.client") + ".*")
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
    DataAnnotation dataAnnotation = DtoUtils.findDataAnnotation(modelType);

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
    ICompilationUnit dtoIcu = sharedEnv.findType(cuSrc.getMainType().getFullyQualifiedName()).getCompilationUnit();
    Assert.assertNull(sharedEnv.getCompileErrors(dtoIcu.getMainType().getName()));

    return dtoIcu.getMainType();
  }

  public static IType createFormDataAssertNoCompileErrors(String modelFqn) {
    // get model type
    IType modelType = createClientJavaEnvironment().findType(modelFqn);

    // build classpath for shared project
    IJavaEnvironment sharedEnv = createSharedJavaEnvironment();

    // build source
    FormDataAnnotation formDataAnnotation = DtoUtils.findFormDataAnnotation(modelType);
    ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, formDataAnnotation, sharedEnv);
    String source = DtoUtils.createJavaCode(cuSrc, sharedEnv, "\n", null);

    // ensure it compiles and get model of dto
    sharedEnv.registerCompilationUnitOverride(cuSrc.getPackageName(), cuSrc.getElementName(), new StringBuilder(source));
    sharedEnv.reload();
    ICompilationUnit dtoIcu = sharedEnv.findType(cuSrc.getMainType().getFullyQualifiedName()).getCompilationUnit();
    Assert.assertNull(sharedEnv.getCompileErrors(dtoIcu.getMainType().getName()));

    return dtoIcu.getMainType();
  }

  /**
   * fails if the {@link IAnnotatable} does not have an order annotation with the <code>orderNr</code>.
   *
   * @param message
   * @param annotatable
   * @param orderNr
   */
  public static void assertOrderAnnotation(String message, IAnnotatable annotatable, Double orderNr) {
    Double memberOrderNr = ScoutUtils.getOrderAnnotationValue(annotatable);
    if (!Objects.equals(orderNr, memberOrderNr)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Order annotation not equal: exptected '").append(orderNr).append("'; found on member");
        if (annotatable instanceof IMember) {
          messageBuilder.append(" '").append(((IMember) annotatable).getElementName()).append("'");
        }
        messageBuilder.append(" is '").append(memberOrderNr).append("'!");
        message = messageBuilder.toString();
      }
      Assert.fail(message);
    }

    Assert.assertEquals(message, memberOrderNr, orderNr);
  }

  /**
   * @see SdkAssert#assertOrderAnnotation(String, IAnnotatable, Double)
   */
  public static void assertOrderAnnotation(IAnnotatable annotatable, Double orderNr) {
    assertOrderAnnotation(null, annotatable, orderNr);
  }

}
