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
package org.eclipse.scout.sdk.core.s.sourcebuilder.codetype;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Test;

/**
 * <h3>{@link CodeTypeSourceBuilderTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CodeTypeSourceBuilderTest {

  @Test
  public void testCodeTypeAllParams() {
    IJavaEnvironment sharedEnv = CoreScoutTestingUtils.createSharedJavaEnvironment();
    CodeTypeSourceBuilder sb = new CodeTypeSourceBuilder("MyCodeType", "org.eclispe.scout.sdk.core.s.test");
    sb.setClassIdValue("whocares");
    sb.setCodeTypeIdSignature(ISignatureConstants.SIG_JAVA_LANG_STRING);
    sb.setIdValueBuilder(new RawSourceBuilder("\"id_value\""));
    String superType = IScoutRuntimeTypes.AbstractCodeType + ISignatureConstants.C_GENERIC_START + IJavaRuntimeTypes.java_lang_String + "," + IJavaRuntimeTypes.java_lang_Long + ISignatureConstants.C_GENERIC_END;
    sb.setSuperTypeSignature(Signature.createTypeSignature(superType));
    sb.setup();

    String source = CoreUtils.createJavaCode(sb, sharedEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(sharedEnv, sb.getPackageName(), sb.getMainType().getElementName(), source);
  }
}
