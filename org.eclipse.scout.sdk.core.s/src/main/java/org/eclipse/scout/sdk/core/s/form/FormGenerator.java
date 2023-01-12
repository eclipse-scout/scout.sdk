/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.form;

import static org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator.createOrder;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.Strings.capitalize;
import static org.eclipse.scout.sdk.core.util.Strings.removeSuffix;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FormGenerator}</h3>
 *
 * @since 5.2.0
 */
public class FormGenerator<TYPE extends FormGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  public static final String SERVICE_LOAD_METHOD_NAME = "load";
  public static final String SERVICE_STORE_METHOD_NAME = "store";
  public static final String MODIFY_HANDLER_NAME = "Modify" + ISdkConstants.SUFFIX_FORM_HANDLER;

  public static final String SERVICE_PREPARE_CREATE_METHOD_NAME = "prepareCreate";
  public static final String SERVICE_CREATE_METHOD_NAME = "create";
  public static final String NEW_HANDLER_NAME = "New" + ISdkConstants.SUFFIX_FORM_HANDLER;

  private final AtomicInteger m_nextFieldGetterSortCode = new AtomicInteger(200);

  private String m_formData;
  private String m_serviceIfc;
  private String m_updatePermission;
  private String m_createPermission;
  private List<String> m_formFields;

  @Override
  protected void setup() {
    var mainBox = createMainBox();
    this
        .withAnnotation(formData()
            .map(dto -> ScoutAnnotationGenerator.createFormData(dto, SdkCommand.CREATE, null))
            .orElse(null))
        .withAnnotation(classIdGenerator(fullyQualifiedName()))
        .withMethod(createGetConfiguredTitle(), 100)
        .withType(mainBox, 2000)
        .withType(createHandler(NEW_HANDLER_NAME), 5000)
        .withType(createHandler(MODIFY_HANDLER_NAME), 6000);

    withFieldGetter(mainBox.fullyQualifiedName());
    appendGroupBox(mainBox);
    appendButtons(mainBox);

    var startModify = createStartMethod("startModify", MODIFY_HANDLER_NAME);
    var startNew = createStartMethod("startNew", NEW_HANDLER_NAME);
    if (startModify != null) {
      withMethod(startModify, 3000);
    }
    if (startNew != null) {
      withMethod(startNew, 4000);
    }
  }

  protected IMethodGenerator<?, ?> createGetConfiguredTitle() {
    var nlsKeyName = capitalize(removeSuffix(elementName().orElseThrow(() -> newFail("Form has no name.")), ISdkConstants.SUFFIX_FORM));
    return ScoutMethodGenerator.createNlsMethodFrom(IScoutApi.class, api -> api.AbstractForm().getConfiguredTitleMethodName(), nlsKeyName);
  }

  protected IMethodGenerator<?, ?> createStartMethod(String methodName, String handlerSimpleName) {
    var modifyHandlerFqn = fullyQualifiedName() + JavaTypes.C_DOLLAR + handlerSimpleName;
    return MethodGenerator.create()
        .asPublic()
        .withElementName(methodName)
        .withReturnType(JavaTypes._void)
        .withBody(b -> {
          if (MODIFY_HANDLER_NAME.equals(handlerSimpleName)) {
            b.appendFrom(IScoutApi.class, api -> api.AbstractForm().startInternalExclusiveMethodName());
          }
          else {
            b.appendFrom(IScoutApi.class, api -> api.AbstractForm().startInternalMethodName());
          }
          b.parenthesisOpen().appendNew(modifyHandlerFqn).parenthesisClose().parenthesisClose().semicolon();
        });
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createMainBox() {
    return TypeGenerator.create()
        .asPublic()
        .withAnnotation(createOrder(ISdkConstants.VIEW_ORDER_ANNOTATION_VALUE_STEP))
        .withAnnotation(classIdGenerator(fullyQualifiedName()))
        .withElementName("MainBox")
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractGroupBox().fqn())
        .setDeclaringFullyQualifiedName(fullyQualifiedName());
  }

  protected void appendGroupBox(ITypeGenerator<?> parent) {
    var groupBox = createGroupBox(parent.fullyQualifiedName());
    parent.withType(groupBox, 100);
    withFieldGetter(groupBox.fullyQualifiedName());

    var index = new AtomicInteger(1);
    capitalize(formFields())
        .forEach(formField -> appendFormField(formField, groupBox, index.getAndIncrement() * ISdkConstants.VIEW_ORDER_ANNOTATION_VALUE_STEP));
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createGroupBox(String declaringFqn) {
    return TypeGenerator.create()
        .asPublic()
        .withElementName("GroupBox")
        .withAnnotation(createOrder(ISdkConstants.VIEW_ORDER_ANNOTATION_VALUE_STEP))
        .withAnnotation(classIdGenerator(fullyQualifiedName()))
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractGroupBox().fqn())
        .setDeclaringFullyQualifiedName(declaringFqn);
  }

  protected void appendFormField(CharSequence formField, ITypeGenerator<?> parent, double order) {
    var field = createFormField(formField, parent.fullyQualifiedName(), order);
    parent.withType(field);
    withFieldGetter(field.fullyQualifiedName());
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createFormField(CharSequence formField, String declaringFqn, double order) {
    return TypeGenerator.create()
        .asPublic()
        .withElementName(formField + ISdkConstants.SUFFIX_FORM_FIELD)
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractStringField().fqn())
        .withAnnotation(createOrder(order))
        .withAnnotation(classIdGenerator(fullyQualifiedName()))
        .withMethod(ScoutMethodGenerator.createNlsMethodFrom(IScoutApi.class, api -> api.AbstractFormField().getConfiguredLabelMethodName(), formField))
        .withMethod(ScoutMethodGenerator.createGetConfiguredMandatory(true))
        .withMethod(ScoutMethodGenerator.createGetConfiguredMaxLength(60))
        .setDeclaringFullyQualifiedName(declaringFqn);
  }

  protected void appendButtons(ITypeGenerator<?> parent) {
    var okButton = createOkButton(parent.fullyQualifiedName(), 2 * ISdkConstants.VIEW_ORDER_ANNOTATION_VALUE_STEP);
    var cancelButton = createCancelButton(parent.fullyQualifiedName(), 3 * ISdkConstants.VIEW_ORDER_ANNOTATION_VALUE_STEP);
    parent
        .withType(okButton, 200)
        .withType(cancelButton, 300);
    withFieldGetter(okButton.fullyQualifiedName());
    withFieldGetter(cancelButton.fullyQualifiedName());
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createOkButton(String declaringFqn, double order) {
    return createButtonFrom("Ok" + ISdkConstants.SUFFIX_BUTTON, declaringFqn, IScoutApi.class, api -> api.AbstractOkButton().fqn(), order);
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createCancelButton(String declaringFqn, double order) {
    return createButtonFrom("Cancel" + ISdkConstants.SUFFIX_BUTTON, declaringFqn, IScoutApi.class, api -> api.AbstractCancelButton().fqn(), order);
  }

  protected <API extends IApiSpecification> ITypeGenerator<? extends ITypeGenerator<?>> createButtonFrom(String buttonName, String declaringFqn, Class<API> apiDefinition, Function<API, String> superClassSupplier, double order) {
    return TypeGenerator.create()
        .asPublic()
        .withElementName(buttonName)
        .withAnnotation(createOrder(order))
        .withAnnotation(classIdGenerator(fullyQualifiedName()))
        .withSuperClassFrom(apiDefinition, superClassSupplier)
        .setDeclaringFullyQualifiedName(declaringFqn);
  }

  protected ITypeGenerator<? extends ITypeGenerator<?>> createHandler(String name) {
    var isModify = MODIFY_HANDLER_NAME.equals(name);
    return TypeGenerator.create()
        .asPublic()
        .withElementName(name)
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractFormHandler().fqn())
        .withMethod(MethodGenerator.create()
            .asProtected()
            .withReturnType(JavaTypes._void)
            .withElementNameFrom(IScoutApi.class, api -> api.AbstractFormHandler().execLoadMethodName())
            .withBody(b -> createHandlerMethodBody(b, isModify, true))
            .withAnnotation(AnnotationGenerator.createOverride()))
        .withMethod(MethodGenerator.create()
            .asProtected()
            .withReturnType(JavaTypes._void)
            .withElementNameFrom(IScoutApi.class, api -> api.AbstractFormHandler().execStoreMethodName())
            .withBody(b -> createHandlerMethodBody(b, isModify, false))
            .withAnnotation(AnnotationGenerator.createOverride()));
  }

  protected void createHandlerMethodBody(IMethodBodyBuilder<?> b, boolean isModify, boolean isLoad) {
    createHandlerMethodBodyGenerator(isModify, isLoad).generate(b);
  }

  /**
   * @param isModify
   *          Specifies if the current method is in a modify handler
   * @param isLoad
   *          specifies if the current method is an execLoad or an execStore method
   */
  protected HandlerMethodBodyGenerator createHandlerMethodBodyGenerator(boolean isModify, boolean isLoad) {
    var handlerMethodBodyGenerator = new HandlerMethodBodyGenerator(isModify)
        .withFormDataType(formData().orElse(null))
        .withServiceInterface(serviceInterface().orElse(null));

    if (isModify) {
      return handlerMethodBodyGenerator.withPermission(permissionUpdate().orElse(null));
    }
    return handlerMethodBodyGenerator.withPermission(permissionCreate().orElse(null));
  }

  protected static IAnnotationGenerator<?> classIdGenerator(String fqn) {
    return Strings.notBlank(ClassIds.nextIfEnabled(fqn))
        .map(ScoutAnnotationGenerator::createClassId)
        .orElse(null);
  }

  protected void withFieldGetter(String fqn) {
    withMethod(ScoutMethodGenerator.createFieldGetter(fqn), getNextFieldGetterSortCode());
  }

  protected int getNextFieldGetterSortCode() {
    return m_nextFieldGetterSortCode.getAndAdd(10);
  }

  public Optional<String> formData() {
    return Strings.notBlank(m_formData);
  }

  public TYPE withFormData(String formData) {
    m_formData = formData;
    return thisInstance();
  }

  public Optional<String> serviceInterface() {
    return Strings.notBlank(m_serviceIfc);
  }

  public TYPE withServiceInterface(String serviceIfc) {
    m_serviceIfc = serviceIfc;
    return thisInstance();
  }

  public Optional<String> permissionUpdate() {
    return Strings.notBlank(m_updatePermission);
  }

  public TYPE withPermissionUpdate(String updatePermission) {
    m_updatePermission = updatePermission;
    return thisInstance();
  }

  public Optional<String> permissionCreate() {
    return Strings.notBlank(m_createPermission);
  }

  public TYPE withPermissionCreate(String createPermission) {
    m_createPermission = createPermission;
    return thisInstance();
  }

  public List<String> formFields() {
    return m_formFields;
  }

  public TYPE withFormFields(List<String> formFields) {
    m_formFields = formFields;
    return thisInstance();
  }
}
