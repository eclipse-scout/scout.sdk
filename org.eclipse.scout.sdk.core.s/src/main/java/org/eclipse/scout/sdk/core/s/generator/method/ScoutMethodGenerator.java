/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.generator.method;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.builder.java.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ScoutMethodGenerator}</h3>
 *
 * @since 6.1.0
 */
public class ScoutMethodGenerator<TYPE extends IScoutMethodGenerator<TYPE, BODY>, BODY extends IScoutMethodBodyBuilder<?>> extends MethodGenerator<TYPE, BODY> implements IScoutMethodGenerator<TYPE, BODY> {

  protected ScoutMethodGenerator() {
  }

  protected ScoutMethodGenerator(IMethod method, IWorkingCopyTransformer transformer) {
    super(method, transformer);
  }

  public static IScoutMethodGenerator<?, ? extends IScoutMethodBodyBuilder<?>> create(IMethod method, IWorkingCopyTransformer transformer) {
    return new ScoutMethodGenerator<>(method, transformer);
  }

  public static IScoutMethodGenerator<?, ? extends IScoutMethodBodyBuilder<?>> create() {
    return new ScoutMethodGenerator<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected BODY createMethodBodyBuilder(ISourceBuilder<?> inner) {
    return (BODY) ScoutMethodBodyBuilder.create(inner, this);
  }

  public static IScoutMethodGenerator<?, ?> createFieldGetter(String fieldFqn) {
    String dotBasedFqn = Ensure.notBlank(fieldFqn).replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT);
    String fieldSimpleName = JavaTypes.simpleName(dotBasedFqn);
    return create()
        .asPublic()
        .withElementName(PropertyBean.GETTER_PREFIX + Strings.ensureStartWithUpperCase(fieldSimpleName))
        .withReturnType(fieldFqn)
        .withBody(b -> b.returnClause().appendGetFieldByClass(fieldFqn).semicolon());
  }

  public static IScoutMethodGenerator<?, ?> createNlsMethod(String methodName, String nlsKeyName) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withElementName(methodName)
        .withReturnType(String.class.getName())
        .withBody(b -> b.appendTodo("verify translation")
            .returnClause().ref(IScoutRuntimeTypes.TEXTS).append(".get").parenthesisOpen().stringLiteral(nlsKeyName).parenthesisClose().semicolon());
  }
}