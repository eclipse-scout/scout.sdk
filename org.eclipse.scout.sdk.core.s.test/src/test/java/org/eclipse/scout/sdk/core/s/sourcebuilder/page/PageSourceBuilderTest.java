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

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.sourcebuilder.service.ServiceInterfaceSourceBuilder;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
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
    String pageDataSignature = Signature.createTypeSignature(BaseTablePageData.class.getName());

    // page service
    ServiceInterfaceSourceBuilder svcIfcBuilder = new ServiceInterfaceSourceBuilder("IMyPageService", "org.eclipse.scout.sdk.core.s.test", clientEnv);
    svcIfcBuilder.setup();
    final IMethodSourceBuilder methodBuilder = new MethodSourceBuilder(PageSourceBuilder.DATA_FETCH_METHOD_NAME);
    methodBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);
    methodBuilder.setComment(CommentSourceBuilderFactory.createDefaultMethodComment(methodBuilder));
    methodBuilder.setReturnTypeSignature(pageDataSignature);
    methodBuilder.addParameter(new MethodParameterSourceBuilder("filter", Signature.createTypeSignature(IScoutRuntimeTypes.SearchFilter)));
    svcIfcBuilder.getMainType().addMethod(methodBuilder);
    String source = CoreUtils.createJavaCode(svcIfcBuilder, clientEnv, "\n", null);
    IType createdSvcIfc = CoreTestingUtils.assertNoCompileErrors(clientEnv, svcIfcBuilder.getPackageName(), svcIfcBuilder.getMainType().getElementName(), source);

    // page
    PageSourceBuilder pageBuilder = new PageSourceBuilder("MyPage", "org.eclipse.scout.sdk.core.s.test", clientEnv);
    pageBuilder.setClassIdValue("whatever");
    pageBuilder.setPageDataSignature(pageDataSignature);
    pageBuilder.setPageWithTable(true);
    pageBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractPageWithTable));
    pageBuilder.setPageServiceIfcSignature(Signature.createTypeSignature(createdSvcIfc.name()));
    pageBuilder.setup();

    source = CoreUtils.createJavaCode(pageBuilder, clientEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(clientEnv, pageBuilder.getPackageName(), pageBuilder.getMainType().getElementName(), source);
  }

  @Test
  public void testPageWithNodes() {
    IJavaEnvironment clientEnv = CoreScoutTestingUtils.createClientJavaEnvironment();

    // page
    PageSourceBuilder pageBuilder = new PageSourceBuilder("MyPage", "org.eclipse.scout.sdk.core.s.test", clientEnv);
    pageBuilder.setClassIdValue("whatever");
    pageBuilder.setPageDataSignature(null);
    pageBuilder.setPageWithTable(false);
    pageBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractPageWithNodes));
    pageBuilder.setup();

    String source = CoreUtils.createJavaCode(pageBuilder, clientEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(clientEnv, pageBuilder.getPackageName(), pageBuilder.getMainType().getElementName(), source);
  }
}
