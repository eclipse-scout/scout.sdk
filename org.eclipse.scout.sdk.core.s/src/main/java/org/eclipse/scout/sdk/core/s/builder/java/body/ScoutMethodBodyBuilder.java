/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.builder.java.body;

import java.util.function.Function;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;

/**
 * <h3>{@link ScoutMethodBodyBuilder}</h3>
 *
 * @since 6.1.0
 */
public class ScoutMethodBodyBuilder<TYPE extends IScoutMethodBodyBuilder<TYPE>> extends MethodBodyBuilder<TYPE> implements IScoutMethodBodyBuilder<TYPE> {

  protected ScoutMethodBodyBuilder(ISourceBuilder<?> inner, IMethodGenerator<?, ?> surroundingMethod) {
    super(inner, surroundingMethod);
  }

  public static IScoutMethodBodyBuilder<?> create(IMethodBodyBuilder<?> inner) {
    return create(inner, inner.surroundingMethod());
  }

  public static IScoutMethodBodyBuilder<?> create(ISourceBuilder<?> inner, IMethodGenerator<?, ?> surroundingMethod) {
    return new ScoutMethodBodyBuilder<>(inner, surroundingMethod);
  }

  @Override
  public TYPE appendBeansGetVariable(CharSequence beanClass, CharSequence varName) {
    return ref(beanClass).space().append(varName).equalSign().appendBeansGet(beanClass);
  }

  @Override
  public TYPE appendGetFieldByClass(CharSequence fieldFqn) {
    return appendFrom(IScoutApi.class, api -> api.IForm().getFieldByClassMethodName()).parenthesisOpen().classLiteral(fieldFqn).parenthesisClose();
  }

  @Override
  public TYPE appendGetPropertyByClass(CharSequence propName) {
    return appendFrom(IScoutApi.class, api -> api.IPropertyHolder().getPropertyByClassMethodName()).parenthesisOpen().classLiteral(propName).parenthesisClose();
  }

  @Override
  public TYPE appendTextsGet(CharSequence textKey) {
    return refClassFrom(IScoutApi.class, IScoutApi::TEXTS).dot().appendFrom(IScoutApi.class, api -> api.TEXTS().getMethodName()).parenthesisOpen().stringLiteral(textKey).parenthesisClose();
  }

  @Override
  public TYPE appendBeansGet(CharSequence beanClass) {
    var beanSupplier = IClassNameSupplier.raw(beanClass);
    return appendBeansGetFrom(null, api -> beanSupplier);
  }

  @Override
  public <T extends IApiSpecification> TYPE appendBeansGetFrom(Class<T> apiClass, Function<T, IClassNameSupplier> beanNameProvider) {
    return refClassFrom(IScoutApi.class, IScoutApi::BEANS).dot().appendFrom(IScoutApi.class, api -> api.BEANS().getMethodName()).parenthesisOpen()
        .classLiteralFrom(apiClass, beanNameProvider).parenthesisClose();
  }

  @Override
  public TYPE appendExportFormData(CharSequence formDataVarName) {
    return appendFrom(IScoutApi.class, api -> api.IForm().exportFormDataMethodName()).parenthesisOpen().append(formDataVarName).parenthesisClose().semicolon();
  }

  @Override
  public TYPE appendPermissionCheck(CharSequence permission) {
    return appendIf().parenthesisOpen().appendNot().refClassFrom(IScoutApi.class, IScoutApi::ACCESS).dot().appendFrom(IScoutApi.class, api -> api.ACCESS().checkMethodName()).parenthesisOpen().appendNew().ref(permission)
        .parenthesisOpen().parenthesisClose().parenthesisClose().parenthesisClose().space().blockStart().nl()
        .appendThrow().appendNew().refClassFrom(IScoutApi.class, IScoutApi::VetoException).parenthesisOpen().appendTextsGet("AuthorizationFailed").parenthesisClose().semicolon().nl()
        .blockEnd().nl();
  }

  @Override
  public TYPE appendImportFormData(CharSequence formDataVarName) {
    return appendFrom(IScoutApi.class, api -> api.IForm().importFormDataMethodName()).parenthesisOpen().append(formDataVarName).parenthesisClose().semicolon();
  }

  @Override
  public TYPE appendDoNodeSet(CharSequence nodeName, CharSequence value) {
    return append(nodeName).parenthesisOpen().parenthesisClose().dot().appendFrom(IScoutApi.class, api -> api.DoNode().setMethodName())
        .parenthesisOpen().append(value).parenthesisClose().semicolon();
  }

  @Override
  public TYPE appendDoNodeUpdateAll(CharSequence nodeName, CharSequence value) {
    return append(nodeName).parenthesisOpen().parenthesisClose().dot().appendFrom(IScoutApi.class, api -> api.DoList().updateAllMethodName())
        .parenthesisOpen().append(value).parenthesisClose().semicolon();
  }

  @Override
  public TYPE appendDoNodeGet(CharSequence nodeName) {
    return append(nodeName).parenthesisOpen().parenthesisClose().dot()
        .appendFrom(IScoutApi.class, api -> api.DoNode().getMethodName()).parenthesisOpen().parenthesisClose();
  }
}
