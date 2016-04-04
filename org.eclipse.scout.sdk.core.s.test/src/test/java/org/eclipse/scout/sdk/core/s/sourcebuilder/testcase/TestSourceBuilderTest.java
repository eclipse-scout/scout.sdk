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
package org.eclipse.scout.sdk.core.s.sourcebuilder.testcase;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link TestSourceBuilderTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class TestSourceBuilderTest {
  @Test
  public void testTestSourceBuilderWithDefaultValues() {
    IJavaEnvironment clientEnv = CoreScoutTestingUtils.createClientJavaEnvironment();

    TestSourceBuilder tsb = new TestSourceBuilder("MyTest", "org.eclipse.scout.sdk.core.s.test", clientEnv);
    tsb.setClientTest(true);
    tsb.setRunnerSignature(Signature.createTypeSignature(IScoutRuntimeTypes.ClientTestRunner));
    tsb.setRunWithSubjectValueBuilder(null);
    tsb.setSessionSignature(null);
    tsb.setup();

    String source = CoreUtils.createJavaCode(tsb, clientEnv, "\n", null);
    Assert.assertTrue(source.contains('@' + Signature.getSimpleName(IScoutRuntimeTypes.RunWithSubject) + "(\"anonymous"));
    Assert.assertTrue(source.contains('@' + Signature.getSimpleName(IScoutRuntimeTypes.RunWithClientSession) + "(" + Signature.getSimpleName(IScoutRuntimeTypes.TestEnvironmentClientSession)));
    CoreTestingUtils.assertNoCompileErrors(clientEnv, tsb.getPackageName(), tsb.getMainType().getElementName(), source);
  }

  @Test
  public void testTestSourceBuilderWithSpecificValues() {
    IJavaEnvironment clientEnv = CoreScoutTestingUtils.createClientJavaEnvironment();

    final String subjectValue = "myvalue";
    TestSourceBuilder tsb = new TestSourceBuilder("MyTest", "org.eclipse.scout.sdk.core.s.test", clientEnv);
    tsb.setClientTest(true);
    tsb.setRunnerSignature(Signature.createTypeSignature(IScoutRuntimeTypes.ClientTestRunner));
    tsb.setRunWithSubjectValueBuilder(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(CoreUtils.toStringLiteral(subjectValue));
      }
    });
    tsb.setSessionSignature(Signature.createTypeSignature(IScoutRuntimeTypes.IClientSession));
    tsb.setup();

    String source = CoreUtils.createJavaCode(tsb, clientEnv, "\n", null);
    Assert.assertTrue(source.contains('@' + Signature.getSimpleName(IScoutRuntimeTypes.RunWithSubject) + "(\"" + subjectValue));
    Assert.assertTrue(source.contains('@' + Signature.getSimpleName(IScoutRuntimeTypes.RunWithClientSession) + "(" + Signature.getSimpleName(IScoutRuntimeTypes.IClientSession)));
    CoreTestingUtils.assertNoCompileErrors(clientEnv, tsb.getPackageName(), tsb.getMainType().getElementName(), source);
  }
}
