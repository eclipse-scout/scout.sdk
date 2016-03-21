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
package org.eclipse.scout.sdk.core.s.model;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link ScoutMethodSourceBuilderFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class ScoutMethodSourceBuilderFactory {
  private ScoutMethodSourceBuilderFactory() {
  }

  public static IMethodSourceBuilder createFieldGetter(final String fieldSignature) {
    String fieldSimpleName = CoreUtils.ensureStartWithUpperCase(Signature.getSignatureSimpleName(fieldSignature.replace(ISignatureConstants.C_DOLLAR, ISignatureConstants.C_DOT)));
    IMethodSourceBuilder getterBuilder = new MethodSourceBuilder("get" + fieldSimpleName);
    getterBuilder.setFlags(Flags.AccPublic);
    getterBuilder.setReturnTypeSignature(fieldSignature);
    getterBuilder.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return getFieldByClass(")
            .append(validator.useSignature(fieldSignature))
            .append(SuffixConstants.SUFFIX_STRING_class)
            .append(");");
      }
    });
    return getterBuilder;
  }

  public static IMethodSourceBuilder createFormServiceIfcMethod(String name, String dtoSignature) {
    IMethodSourceBuilder methodBuilder = new MethodSourceBuilder(name);
    methodBuilder.setFlags(Flags.AccInterface);
    methodBuilder.setComment(CommentSourceBuilderFactory.createDefaultMethodComment(methodBuilder));
    if (dtoSignature != null) {
      methodBuilder.setReturnTypeSignature(dtoSignature);
      methodBuilder.addParameter(new MethodParameterSourceBuilder("input", dtoSignature));
    }
    else {
      methodBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    }
    return methodBuilder;
  }
}
