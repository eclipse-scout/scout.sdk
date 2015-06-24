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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.IMember;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.parser.JavaParser;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.s.util.ScoutUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.core.testing.TestingUtils;

/**
 *
 */
public final class CoreScoutTestingUtils {

  public static final String[] SOURCE_FOLDERS = new String[]{"src/main/client", "src/main/shared"};
  public static final String PROJECT_NAME = "org.eclipse.scout.sdk.core.s.test";

  private CoreScoutTestingUtils() {
  }

  public static IType createPageDataAssertNoCompileErrors(String modelFqn) throws IOException {
    return createDtoAssertNoCompileErrors(modelFqn, false);
  }

  public static IType createRowDataAssertNoCompileErrors(String modelFqn) throws IOException {
    return createDtoAssertNoCompileErrors(modelFqn, true);
  }

  private static IType createDtoAssertNoCompileErrors(String modelFqn, boolean rowData) throws IOException {
    // get model type
    IType modelType = TestingUtils.getType(modelFqn, SOURCE_FOLDERS);
    DataAnnotation dataAnnotation = DtoUtils.findDataAnnotation(modelType);

    // build classpath for shared project
    JavaParser sharedEnv = (JavaParser) JavaParser.create(getSharedClasspath(), false);

    // build source
    StringBuilder sourceBuilder = null;
    if (rowData) {
      sourceBuilder = DtoUtils.createTableRowDataTypeSource(modelType, dataAnnotation, sharedEnv, "\n", null);
    }
    else {
      sourceBuilder = DtoUtils.createPageDataSource(modelType, dataAnnotation, sharedEnv, "\n", null);
    }

    // ensure it compiles and get model of dto
    sharedEnv.reset(); // start from scratch for test compile
    ICompilationUnit dtoIcu = sharedEnv.parse(sourceBuilder, dataAnnotation.getDataType().getSimpleName(), dataAnnotation.getDataType().getName() /* don't care */);

    return dtoIcu.getMainType();
  }

  public static IType createFormDataAssertNoCompileErrors(String modelFqn) throws IOException {
    // get model type
    IType modelType = TestingUtils.getType(modelFqn, SOURCE_FOLDERS);

    // build classpath for shared project
    JavaParser sharedEnv = (JavaParser) JavaParser.create(getSharedClasspath(), false);

    // build source
    FormDataAnnotation formDataAnnotation = DtoUtils.findFormDataAnnotation(modelType);
    StringBuilder sourceBuilder = DtoUtils.createFormDataSource(modelType, formDataAnnotation, sharedEnv, "\n", null);

    // ensure it compiles and get model of dto
    sharedEnv.reset(); // start from scratch for test compile
    ICompilationUnit dtoIcu = sharedEnv.parse(sourceBuilder, formDataAnnotation.getFormDataType().getSimpleName(), formDataAnnotation.getFormDataType().getName() /* don't care */);

    return dtoIcu.getMainType();
  }

  /**
   * fails if the {@link IAnnotatable} does not have an order annotation with the <code>orderNr</code>.
   *
   * @param message
   * @param annotatable
   * @param orderNr
   * @throws JavaModelException
   */
  public static void assertOrderAnnotation(String message, IAnnotatable annotatable, Double orderNr) {
    Double memberOrderNr = ScoutUtils.getOrderAnnotationValue(annotatable);
    if (!Objects.equals(orderNr, memberOrderNr)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Order annotation not equal: exptected '").append(orderNr).append("'; found on member");
        if (annotatable != null && annotatable instanceof IMember) {
          messageBuilder.append(" '").append(((IMember) annotatable).getName()).append("'");
        }
        messageBuilder.append(" is '").append(memberOrderNr).append("'!");
        message = messageBuilder.toString();
      }
      SdkAssert.fail(message);
    }

    SdkAssert.assertEquals(message, memberOrderNr, orderNr);
  }

  /**
   * @see SdkAssert#assertOrderAnnotation(String, IAnnotatable, Double)
   */
  public static void assertOrderAnnotation(IAnnotatable annotatable, Double orderNr) {
    assertOrderAnnotation(null, annotatable, orderNr);
  }

  public static List<File> getSharedClasspath() {
    List<File> runningClasspath = TestingUtils.getRunningClasspath(); // use running path as starting point
    List<File> sharedClasspath = new ArrayList<>(runningClasspath.size());
    String testCasesPath = PROJECT_NAME + File.separatorChar + "target" + File.separatorChar + "classes";

    for (File f : runningClasspath) {
      String path = f.getAbsolutePath();
      if (!path.contains("org.eclipse.scout.rt.client")) { // remove rt client
        if (path.endsWith(testCasesPath)) {
          sharedClasspath.add(new File(f, "../../src/main/shared")); // replace build output with shared sources (this removes the client test classes)
        }
        else {
          sharedClasspath.add(f);
        }
      }
    }

    return sharedClasspath;
  }
}
