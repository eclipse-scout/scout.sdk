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
package org.eclipse.scout.sdk.core.s.sourcebuilder.page;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Test;

import formdata.shared.services.pages.BaseTablePageData;

/**
 * <h3>{@link PageSourceBuilderTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageSourceBuilderTest {

  @Test
  public void testPageWithTable() {
    IJavaEnvironment clientEnv = CoreScoutTestingUtils.createClientJavaEnvironment();

    // page
    PageSourceBuilder pageBuilder = new PageSourceBuilder("MyPage", "org.eclispe.scout.sdk.core.s.test");
    pageBuilder.setClassIdValue("whatever");
    pageBuilder.setPageDataSignature(Signature.createTypeSignature(BaseTablePageData.class.getName()));
    pageBuilder.setPageWithTable(true);
    pageBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractPageWithTable));
    pageBuilder.setup();

    String source = CoreUtils.createJavaCode(pageBuilder, clientEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(clientEnv, pageBuilder.getPackageName(), pageBuilder.getMainType().getElementName(), source);
  }

  @Test
  public void testPageWithNodes() {
    IJavaEnvironment clientEnv = CoreScoutTestingUtils.createClientJavaEnvironment();

    // page
    PageSourceBuilder pageBuilder = new PageSourceBuilder("MyPage", "org.eclispe.scout.sdk.core.s.test");
    pageBuilder.setClassIdValue("whatever");
    pageBuilder.setPageDataSignature(null);
    pageBuilder.setPageWithTable(false);
    pageBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractPageWithNodes));
    pageBuilder.setup();

    String source = CoreUtils.createJavaCode(pageBuilder, clientEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(clientEnv, pageBuilder.getPackageName(), pageBuilder.getMainType().getElementName(), source);
  }
}
