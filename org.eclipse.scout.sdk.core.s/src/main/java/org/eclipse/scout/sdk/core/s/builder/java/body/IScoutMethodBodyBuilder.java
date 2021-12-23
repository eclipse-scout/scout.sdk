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
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;

/**
 * <h3>{@link IScoutMethodBodyBuilder}</h3>
 *
 * @since 6.1.0
 */
public interface IScoutMethodBodyBuilder<TYPE extends IScoutMethodBodyBuilder<TYPE>> extends IMethodBodyBuilder<TYPE> {

  /**
   * Creates source like {@code BeanClass varName = BEANS.get(BeanClass.class)} without trailing semicolon.
   * 
   * @param beanClass
   *          The fully qualified name of the bean class. Must not be {@code null}.
   * @param varName
   *          The name of the variable. Must not be {@code null}.
   * @return this builder
   * @see #appendBeansGet(CharSequence)
   * @see #appendBeansGetFrom(Class, Function)
   */
  TYPE appendBeansGetVariable(CharSequence beanClass, CharSequence varName);

  /**
   * Creates source like {@code exportFormData(formDataVarName);} with trailing semicolon.
   * 
   * @param formDataVarName
   *          The name of the FormData variable to export to. Must not be {@code null}.
   * @return this builder
   * @see #appendImportFormData(CharSequence)
   */
  TYPE appendExportFormData(CharSequence formDataVarName);

  /**
   * Creates source like {@code importFormData(formDataVarName);} with trailing semicolon.
   * 
   * @param formDataVarName
   *          The name of the FormData variable to export to. Must not be {@code null}.
   * @return this builder
   * @see #appendExportFormData(CharSequence)
   */
  TYPE appendImportFormData(CharSequence formDataVarName);

  /**
   * Appends a set value of a DoNode like {@code nodeName().set(value);} including a trailing semicolon.
   * 
   * @param nodeName
   *          The node name to set.
   * @param value
   *          The value source to pass to the setter.
   * @return this builder
   */
  TYPE appendDoNodeSet(CharSequence nodeName, CharSequence value);

  /**
   * Appends an updateAll method call of a DoList like {@code nodeName().updateAll(value);} including a trailing
   * semicolon.
   * 
   * @param nodeName
   *          The node name to update.
   * @param value
   *          The source to pass to the updateAll method
   * @return this builder
   */
  TYPE appendDoCollectionUpdateAll(CharSequence nodeName, CharSequence value);

  /**
   * Appends a get call to a DoNode like {@code nodeName().get()} without a trailing semicolon.
   * 
   * @param nodeName
   *          The node name to get.
   * @return this builder.
   */
  TYPE appendDoNodeGet(CharSequence nodeName);

  /**
   * Creates source like {@code BEANS.get(BeanClass.class)} without trailing semicolon.
   * 
   * @param beanClass
   *          The fully qualified name of the bean class. Must not be {@code null}.
   * @return this builder
   * @see #appendBeansGetFrom(Class, Function)
   * @see #appendBeansGetVariable(CharSequence, CharSequence)
   */
  TYPE appendBeansGet(CharSequence beanClass);

  /**
   * Creates source like {@code BEANS.get(BeanClass.class)} without trailing semicolon.
   * 
   * @param apiClass
   *          The api class that contains the class name of the bean. May be {@code null} in case the supplied
   *          beanNameProvider can handle a {@code null} input.
   * @param beanNameProvider
   * @param <T>
   *          The api type
   * @return this builder
   * @see #appendBeansGet(CharSequence)
   * @see #appendBeansGetVariable(CharSequence, CharSequence)
   */
  <T extends IApiSpecification> TYPE appendBeansGetFrom(Class<T> apiClass, Function<T, ITypeNameSupplier> beanNameProvider);

  /**
   * Creates source like {@code getFieldByClass(FieldFqn.class)} without trailing semicolon.
   * 
   * @param fieldFqn
   *          The fully qualified name of the field. Must not be {@code null}.
   * @return this builder.
   */
  TYPE appendGetFieldByClass(CharSequence fieldFqn);

  /**
   * Creates source like {@code getPropertyByClass(PropName.class)} without trailing semicolon.
   *
   * @param propName
   *          The fully qualified name of the property class. Must not be {@code null}.
   * @return this builder
   */
  TYPE appendGetPropertyByClass(CharSequence propName);

  /**
   * Creates source like {@code TEXTS.get("textKey")} without trailing semicolon.
   * 
   * @param textKey
   *          The text key to use. May be {@code null}. In that case "null" is appended as key.
   * @return this builder
   */
  TYPE appendTextsGet(CharSequence textKey);

  /**
   * Creates the following code block with trailing newline after the end of the if block:
   * 
   * <pre>
   * if (!ACCESS.check(new Permission())) {
   *   throw new VetoException("AuthorizationFailed");
   * }
   * </pre>
   * 
   * @param permission
   *          The fully qualified name of the permission. Must not be {@code null}.
   * @return this builder
   */
  TYPE appendPermissionCheck(CharSequence permission);

  /**
   * Creates source like {@code form.setHandler(form.new Handler());}.
   *
   * @param formVariableName
   *          The variable name of the form.
   * @param handlerSimpleName
   *          The simple name of the handler class.
   * @return this builder
   */
  TYPE appendFormSetHandler(CharSequence formVariableName, CharSequence handlerSimpleName);

  /**
   * Creates source like {@code throw new VetoException(TEXTS.get("Something"));}.
   *
   * @param nlsKeyName
   *          The nls key to insert.
   * @param varArg
   *          A sourceGenerator that allows to add arguments to the TEXTS.get-call.
   * @return this builder
   */
  TYPE appendThrowVetoException(CharSequence nlsKeyName, ISourceGenerator<IExpressionBuilder<?>> varArg);
}
