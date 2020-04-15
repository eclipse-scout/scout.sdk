/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.builder.java.body;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link ScoutMethodBodyBuilder}</h3>
 *
 * @since 6.1.0
 */
public class ScoutMethodBodyBuilder<TYPE extends IScoutMethodBodyBuilder<TYPE>> extends MethodBodyBuilder<TYPE> implements IScoutMethodBodyBuilder<TYPE> {

  protected ScoutMethodBodyBuilder(ISourceBuilder<?> inner, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> surroundingMethod) {
    super(inner, surroundingMethod);
  }

  public static IScoutMethodBodyBuilder<?> create(IMethodBodyBuilder<?> inner) {
    return create(inner, inner.surroundingMethod());
  }

  public static IScoutMethodBodyBuilder<?> create(ISourceBuilder<?> inner, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> surroundingMethod) {
    return new ScoutMethodBodyBuilder<>(inner, surroundingMethod);
  }

  @Override
  public TYPE appendBeansGetVariable(CharSequence bean, CharSequence varName) {
    return ref(bean).space().append(varName).equalSign().appendBeansGet(bean);
  }

  @Override
  public TYPE appendGetFieldByClass(CharSequence fieldFqn) {
    return append("getFieldByClass").parenthesisOpen().classLiteral(fieldFqn).parenthesisClose();
  }

  @Override
  public TYPE appendGetPropertyByClass(CharSequence propName) {
    return append("getPropertyByClass").parenthesisOpen().classLiteral(propName).parenthesisClose();
  }

  @Override
  public TYPE appendTextsGet(String textKey) {
    return ref(IScoutRuntimeTypes.TEXTS).dot().append("get").parenthesisOpen().stringLiteral(textKey).parenthesisClose();
  }

  @Override
  public TYPE appendBeansGet(CharSequence bean) {
    return ref(IScoutRuntimeTypes.BEANS).dot().append("get").parenthesisOpen()
        .classLiteral(bean).parenthesisClose();
  }

  @Override
  public TYPE appendExportFormData(CharSequence formDataVarName) {
    return append("exportFormData").parenthesisOpen().append(formDataVarName).parenthesisClose().semicolon().nl();
  }

  @Override
  public TYPE appendPermissionCheck(CharSequence permission) {
    return appendIf().parenthesisOpen().appendNot().ref(IScoutRuntimeTypes.ACCESS).dot().append("check").parenthesisOpen().appendNew().ref(permission)
        .parenthesisOpen().parenthesisClose().parenthesisClose().parenthesisClose().space().blockStart().nl()
        .appendThrow().appendNew().ref(IScoutRuntimeTypes.VetoException).parenthesisOpen().appendTextsGet("AuthorizationFailed").parenthesisClose().semicolon().nl()
        .blockEnd().nl();
  }

  @Override
  public TYPE appendImportFormData(CharSequence formDataVarName) {
    return append("importFormData").parenthesisOpen().append(formDataVarName).parenthesisClose().semicolon();
  }
}
