/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.form;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.builder.java.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link HandlerMethodBodyGenerator}</h3>
 *
 * @since 6.1.0
 */
public class HandlerMethodBodyGenerator implements ISourceGenerator<IMethodBodyBuilder<?>> {

  public static final String SERVICE_VAR_NAME = "service";
  public static final String FORM_DATA_VAR_NAME = "formData";

  private String m_serviceIfc;
  private String m_formDataType;
  private String m_permission;
  private ISourceGenerator<IMethodBodyBuilder<?>> m_methodArgGenerator;
  private ISourceGenerator<IMethodBodyBuilder<?>> m_permissionArgGenerator;
  private ISourceGenerator<IMethodBodyBuilder<?>> m_formDataInstanceGenerator;
  private boolean m_createFormDataInLoad;
  private final boolean m_isModify;

  public HandlerMethodBodyGenerator(boolean isModify) {
    m_isModify = isModify;
    m_createFormDataInLoad = true;
  }

    @Override
    public void generate(IMethodBodyBuilder<?> builder) {
      IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> declaringMethod = builder.surroundingMethod();
      boolean isLoad = FormGenerator.LOAD_METHOD_NAME.equals(declaringMethod.elementName().get());

      serviceInterface().ifPresent(svcIfc -> buildBackendCall(ScoutMethodBodyBuilder.create(builder), svcIfc, isLoad));

      if (isLoad && permission().isPresent()) {
        builder
            .nl().nl().append("setEnabledPermission").parenthesisOpen().appendNew().ref(permission().get()).parenthesisOpen()
            .append(permissionArgGenerator()
                .orElseGet(ISourceGenerator::empty)
                .generalize(builder))
            .parenthesisClose().parenthesisClose().semicolon();
      }
    }

  protected void buildBackendCall(IScoutMethodBodyBuilder<?> builder, String serviceInterface, boolean isLoad) {
    boolean isDtoAvailable = formDataType().isPresent();

    builder.appendBeansGetVariable(serviceInterface, SERVICE_VAR_NAME).semicolon().nl();

    if (isDtoAvailable) {
      builder.ref(formDataType().get()).space().append(FORM_DATA_VAR_NAME).equalSign();
      if (!isLoad || isCreateFormDataInLoad()) {
        builder.append(
            formDataInstanceCreationGenerator()
                .orElseGet(() -> b -> b.appendNew().ref(formDataType().get()).parenthesisOpen().parenthesisClose().semicolon().nl())
                .generalize(builder))
            .appendExportFormData(FORM_DATA_VAR_NAME);
        if (isLoad) {
          builder.append(FORM_DATA_VAR_NAME).equalSign();
        }
      }
    }

    builder
        .append(SERVICE_VAR_NAME).dotSign().append(getBackendServiceMethodName(isLoad))
        .parenthesisOpen()
        .append(methodArgGenerator()
            .orElseGet(() -> b -> {
              if (isDtoAvailable) {
                b.append(FORM_DATA_VAR_NAME);
              }
            })
            .generalize(builder))
        .parenthesisClose()
        .semicolon();
    if (isLoad && isDtoAvailable) {
      builder.nl().appendImportFormData(FORM_DATA_VAR_NAME);
    }
  }

  protected String getBackendServiceMethodName(boolean isLoad) {
    if (isLoad) {
      if (isModify()) {
        return FormGenerator.SERVICE_LOAD_METHOD_NAME;
      }
      return FormGenerator.SERVICE_PREPARECREATE_METHOD_NAME;
    }

    if (isModify()) {
      return FormGenerator.SERVICE_STORE_METHOD_NAME;
    }
    return FormGenerator.SERVICE_CREATE_METHOD_NAME;
  }

  public Optional<String> serviceInterface() {
    return Strings.notBlank(m_serviceIfc);
  }

  public HandlerMethodBodyGenerator withServiceInterface(String serviceIfc) {
    m_serviceIfc = serviceIfc;
    return this;
  }

  public Optional<String> formDataType() {
    return Strings.notBlank(m_formDataType);
  }

  public HandlerMethodBodyGenerator withFormDataType(String formDataType) {
    m_formDataType = formDataType;
    return this;
  }

  public Optional<String> permission() {
    return Strings.notBlank(m_permission);
  }

  public HandlerMethodBodyGenerator withPermission(String permission) {
    m_permission = permission;
    return this;
  }

  public Optional<ISourceGenerator<IMethodBodyBuilder<?>>> methodArgGenerator() {
    return Optional.ofNullable(m_methodArgGenerator);
  }

  public HandlerMethodBodyGenerator withMethodArgGenerator(ISourceGenerator<IMethodBodyBuilder<?>> methodArgSourceBuilder) {
    m_methodArgGenerator = methodArgSourceBuilder;
    return this;
  }

  public Optional<ISourceGenerator<IMethodBodyBuilder<?>>> permissionArgGenerator() {
    return Optional.ofNullable(m_permissionArgGenerator);
  }

  public HandlerMethodBodyGenerator withPermissionArgGenerator(ISourceGenerator<IMethodBodyBuilder<?>> permissionArgSourceBuilder) {
    m_permissionArgGenerator = permissionArgSourceBuilder;
    return this;
  }

  public boolean isModify() {
    return m_isModify;
  }

  public Optional<ISourceGenerator<IMethodBodyBuilder<?>>> formDataInstanceCreationGenerator() {
    return Optional.ofNullable(m_formDataInstanceGenerator);
  }

  public HandlerMethodBodyGenerator withFormDataInstanceCreationGenerator(ISourceGenerator<IMethodBodyBuilder<?>> formDataInstanceCreationBuilder) {
    m_formDataInstanceGenerator = formDataInstanceCreationBuilder;
    return this;
  }

  public boolean isCreateFormDataInLoad() {
    return m_createFormDataInLoad;
  }

  public HandlerMethodBodyGenerator withCreateFormDataInLoad(boolean createFormDataInLoad) {
    m_createFormDataInLoad = createFormDataInLoad;
    return this;
  }
}
