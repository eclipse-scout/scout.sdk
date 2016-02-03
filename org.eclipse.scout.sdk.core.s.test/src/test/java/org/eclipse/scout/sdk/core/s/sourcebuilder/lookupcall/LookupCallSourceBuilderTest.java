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
package org.eclipse.scout.sdk.core.s.sourcebuilder.lookupcall;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Test;

/**
 * <h3>{@link LookupCallSourceBuilderTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class LookupCallSourceBuilderTest {
  @Test
  public void testLookupCallAllParams() {
    IJavaEnvironment sharedEnv = CoreScoutTestingUtils.createSharedJavaEnvironment();

    // lookup service interface
    String ifcName = "IMyLookupService";
    CompilationUnitSourceBuilder ifcBuilder = new CompilationUnitSourceBuilder(ifcName + SuffixConstants.SUFFIX_STRING_java, "org.eclispe.scout.sdk.core.s.test");
    ifcBuilder.setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(ifcBuilder));

    ITypeSourceBuilder lookupSvcIfcBuilder = new TypeSourceBuilder(ifcName);
    lookupSvcIfcBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);
    lookupSvcIfcBuilder.setComment(CommentSourceBuilderFactory.createDefaultTypeComment(lookupSvcIfcBuilder));

    StringBuilder superTypeBuilder = new StringBuilder(IScoutRuntimeTypes.ILookupService);
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
    superTypeBuilder.append(IJavaRuntimeTypes.java_lang_String);
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
    lookupSvcIfcBuilder.addInterfaceSignature(Signature.createTypeSignature(superTypeBuilder.toString()));
    ifcBuilder.addType(lookupSvcIfcBuilder);
    String source = CoreUtils.createJavaCode(ifcBuilder, sharedEnv, "\n", null);
    IType createdLookupSvcIfc = CoreTestingUtils.assertNoCompileErrors(sharedEnv, ifcBuilder.getPackageName(), ifcBuilder.getMainType().getElementName(), source);

    // lookup call
    LookupCallSourceBuilder lookupCallBuilder = new LookupCallSourceBuilder("MyLookupCall", "org.eclispe.scout.sdk.core.s.test");
    lookupCallBuilder.setKeyTypeSignature(ISignatureConstants.SIG_JAVA_LANG_STRING);
    lookupCallBuilder.setLookupServiceIfcSignature(Signature.createTypeSignature(createdLookupSvcIfc.name()));
    lookupCallBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.LookupCall));
    lookupCallBuilder.setup();

    source = CoreUtils.createJavaCode(lookupCallBuilder, sharedEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(sharedEnv, lookupCallBuilder.getPackageName(), lookupCallBuilder.getMainType().getElementName(), source);
  }
}
