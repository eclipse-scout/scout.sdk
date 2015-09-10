/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.dto.test;

import java.io.IOException;

import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.junit.Test;

/**
 * TODO imo remove and also remove FormDataTest.properties
 */
public class FormDataTest {
  @Test
  public void testCreateFormData() throws IOException {
    IJavaEnvironment env = CoreTestingUtils.importJavaEnvironment(getClass().getResourceAsStream("FormDataTest.properties"));
    IType t = env.findType("com.bsiag.crm.client.core.person.PersonForm");

    StringBuilder buf = new StringBuilder();
    ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(t, DtoUtils.findFormDataAnnotation(t), env);
    cuSrc.createSource(buf, "\n", new PropertyMap(), new ImportValidator(env));

    System.out.println(buf);
  }

}
