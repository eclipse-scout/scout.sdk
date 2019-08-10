/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.form;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FormGenerator}</h3>
 *
 * @since 5.2.0
 */
public class FormGenerator<TYPE extends FormGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  public static final String STORE_METHOD_NAME = "execStore";
  public static final String LOAD_METHOD_NAME = "execLoad";
  public static final String SERVICE_LOAD_METHOD_NAME = "load";
  public static final String SERVICE_STORE_METHOD_NAME = "store";
  public static final String MODIFY_HANDLER_NAME = "ModifyHandler";

  public static final String SERVICE_PREPARECREATE_METHOD_NAME = "prepareCreate";
  public static final String SERVICE_CREATE_METHOD_NAME = "create";
  public static final String NEW_HANDLER_NAME = "NewHandler";

  public static final int NUM_CLASS_IDS = 5;

  private String m_formData;
  private String m_serviceIfc;
  private String m_updatePermission;
  private String m_createPermission;
  private String[] m_classIdValues;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .withAnnotation(formData()
            .map(dto -> ScoutAnnotationGenerator.createFormData(dto, SdkCommand.CREATE, null))
            .orElse(null))
        .withAnnotation(classIdGenerator(0).orElse(null))
        .withMethod(createGetConfiguredTitle(), 10)
        .withType(createMainBox(), 20)
        .withType(createHandler(NEW_HANDLER_NAME), 50)
        .withType(createHandler(MODIFY_HANDLER_NAME), 60);

    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> startModify = createStartMethod("startModify", MODIFY_HANDLER_NAME);
    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> startNew = createStartMethod("startNew", NEW_HANDLER_NAME);
    if (startModify != null) {
      mainType.withMethod(startModify, 30);
    }
    if (startNew != null) {
      mainType.withMethod(startNew, 40);
    }
  }

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createGetConfiguredTitle() {
    String nlsKeyName = elementName().orElseThrow(() -> newFail("Form has no name."));
    if (nlsKeyName.endsWith(ISdkProperties.SUFFIX_FORM)) {
      nlsKeyName = Strings.ensureStartWithUpperCase(nlsKeyName.substring(0, nlsKeyName.length() - ISdkProperties.SUFFIX_FORM.length()));
    }
    return ScoutMethodGenerator.createNlsMethod("getConfiguredTitle", nlsKeyName);
  }

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createStartMethod(String methodName, String handlerSimpleName) {
    String modifyHandlerFqn = fullyQualifiedName() + JavaTypes.C_DOLLAR + handlerSimpleName;
    return MethodGenerator.create()
        .asPublic()
        .withElementName(methodName)
        .withReturnType(JavaTypes._void)
        .withBody(b -> {
          if (MODIFY_HANDLER_NAME.equals(handlerSimpleName)) {
            b.append("startInternalExclusive");
          }
          else {
            b.append("startInternal");
          }
          b.parenthesisOpen().appendNew().ref(modifyHandlerFqn).parenthesisOpen().parenthesisClose().parenthesisClose().semicolon();
        });
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createMainBox() {
    String groupBoxName = "GroupBox";
    String okButtonName = "Ok" + ISdkProperties.SUFFIX_BUTTON;
    String cancelButtonName = "Cancel" + ISdkProperties.SUFFIX_BUTTON;
    String mainBoxName = "MainBox";

    ITypeGenerator<? extends ITypeGenerator<?>> mainBox = TypeGenerator.create()
        .asPublic()
        .withAnnotation(ScoutAnnotationGenerator.createOrder(ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP))
        .withAnnotation(classIdGenerator(1).orElse(null))
        .withElementName(mainBoxName)
        .withSuperClass(IScoutRuntimeTypes.AbstractGroupBox)
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName(groupBoxName)
            .withAnnotation(ScoutAnnotationGenerator.createOrder(ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP))
            .withAnnotation(classIdGenerator(4).orElse(null))
            .withSuperClass(IScoutRuntimeTypes.AbstractGroupBox), 100)
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName(okButtonName)
            .withAnnotation(ScoutAnnotationGenerator.createOrder(2 * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP))
            .withAnnotation(classIdGenerator(2).orElse(null))
            .withSuperClass(IScoutRuntimeTypes.AbstractOkButton), 200)
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName(cancelButtonName)
            .withAnnotation(ScoutAnnotationGenerator.createOrder(3 * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP))
            .withAnnotation(classIdGenerator(3).orElse(null))
            .withSuperClass(IScoutRuntimeTypes.AbstractCancelButton), 300)
        .setDeclaringFullyQualifiedName(fullyQualifiedName());

    // form field getters
    withMethod(ScoutMethodGenerator.createFieldGetter(mainBox
        .fullyQualifiedName()), 12)
            .withMethod(ScoutMethodGenerator.createFieldGetter(mainBox
                .type(groupBoxName).get()
                .fullyQualifiedName()), 14)
            .withMethod(ScoutMethodGenerator.createFieldGetter(mainBox
                .type(okButtonName).get()
                .fullyQualifiedName()), 16)
            .withMethod(ScoutMethodGenerator.createFieldGetter(mainBox
                .type(cancelButtonName).get()
                .fullyQualifiedName()), 18);
    return mainBox;
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createHandler(String name) {
    boolean isModify = MODIFY_HANDLER_NAME.equals(name);
    return TypeGenerator.create()
        .asPublic()
        .withElementName(name)
        .withSuperClass(IScoutRuntimeTypes.AbstractFormHandler)
        .withMethod(MethodGenerator.create()
            .asProtected()
            .withReturnType(JavaTypes._void)
            .withElementName(LOAD_METHOD_NAME)
            .withBody(b -> createHandlerMethodBodyGenerator(isModify, true).generate(b))
            .withAnnotation(AnnotationGenerator.createOverride()))
        .withMethod(MethodGenerator.create()
            .asProtected()
            .withReturnType(JavaTypes._void)
            .withElementName(STORE_METHOD_NAME)
            .withBody(b -> createHandlerMethodBodyGenerator(isModify, false).generate(b))
            .withAnnotation(AnnotationGenerator.createOverride()));
  }

  /**
   * @param isModify
   *          Specifies if the current method is in a modify handler
   * @param isLoad
   *          specifies if the current method is an execLoad or an execStore method
   */
  protected HandlerMethodBodyGenerator createHandlerMethodBodyGenerator(boolean isModify, boolean isLoad) {
    HandlerMethodBodyGenerator handlerMethodBodyGenerator = new HandlerMethodBodyGenerator(isModify)
        .withFormDataType(formData().orElse(null))
        .withServiceInterface(serviceInterface().orElse(null));

    if (isModify) {
      return handlerMethodBodyGenerator.withPermission(permissionUpdate().orElse(null));
    }
    return handlerMethodBodyGenerator.withPermission(permissionCreate().orElse(null));
  }

  public Optional<String> formData() {
    return Strings.notBlank(m_formData);
  }

  public TYPE withFormData(String formData) {
    m_formData = formData;
    return currentInstance();
  }

  public Optional<String> serviceInterface() {
    return Strings.notBlank(m_serviceIfc);
  }

  public TYPE withServiceInterface(String serviceIfc) {
    m_serviceIfc = serviceIfc;
    return currentInstance();
  }

  public Optional<String> permissionUpdate() {
    return Strings.notBlank(m_updatePermission);
  }

  public TYPE withPermissionUpdate(String updatePermission) {
    m_updatePermission = updatePermission;
    return currentInstance();
  }

  public Optional<String> permissionCreate() {
    return Strings.notBlank(m_createPermission);
  }

  public TYPE withPermissionCreate(String createPermission) {
    m_createPermission = createPermission;
    return currentInstance();
  }

  public String[] classIdValues() {
    return m_classIdValues;
  }

  protected Optional<IAnnotationGenerator<?>> classIdGenerator(int index) {
    if (m_classIdValues == null || index >= m_classIdValues.length) {
      return Optional.empty();
    }
    return Optional.of(ScoutAnnotationGenerator.createClassId(m_classIdValues[index]));
  }

  public TYPE withClassIdValues(String[] classIdValues) {
    Ensure.same(Ensure.notNull(classIdValues).length, NUM_CLASS_IDS);
    m_classIdValues = Arrays.copyOf(classIdValues, classIdValues.length);
    return currentInstance();
  }
}
