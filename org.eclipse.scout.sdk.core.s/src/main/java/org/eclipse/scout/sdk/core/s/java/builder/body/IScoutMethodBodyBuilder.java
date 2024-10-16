/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.builder.body;

import java.util.function.Function;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;

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
   *          A {@link Function} to be called to obtain the {@link ITypeNameSupplier} whose fully qualified name should
   *          be used as bean class literal.
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

  /**
   * Appends a reference to the given field. If the given reference looks like {@code "a.b.c.d.SomeClass.MY_FIELD"} an
   * import to {@code "a.b.c.d.SomeClass"} is added and {@code "SomeClass.MY_FIELD"} appended.
   *
   * @param fieldRef
   *          The field reference to append.
   * @return this builder
   */
  TYPE appendFieldReference(String fieldRef);
}
