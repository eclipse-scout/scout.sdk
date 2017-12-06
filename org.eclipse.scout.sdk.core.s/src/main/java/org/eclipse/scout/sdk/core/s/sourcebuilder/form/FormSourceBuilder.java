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
package org.eclipse.scout.sdk.core.s.sourcebuilder.form;

import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.model.ScoutMethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.IAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link FormSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormSourceBuilder extends AbstractEntitySourceBuilder {

  public static final String STORE_METHOD_NAME = "execStore";
  public static final String LOAD_METHOD_NAME = "execLoad";
  public static final String SERVICE_LOAD_METHOD_NAME = "load";
  public static final String SERVICE_STORE_METHOD_NAME = "store";
  public static final String MODIFY_HANDLER_NAME = "ModifyHandler";

  public static final String SERVICE_PREPARECREATE_METHOD_NAME = "prepareCreate";
  public static final String SERVICE_CREATE_METHOD_NAME = "create";
  public static final String NEW_HANDLER_NAME = "NewHandler";

  public static final int NUM_CLASS_IDS = 4;

  private String m_formDataSignature;
  private String m_superTypeSignature;
  private String m_serviceIfcSignature;
  private String m_updatePermissionSignature;
  private String m_createPermissionSignature;
  private String[] m_classIdValues;

  private ITypeSourceBuilder m_formBuilder;

  public FormSourceBuilder(String formName, String packageName, IJavaEnvironment env) {
    super(formName, packageName, env);
  }

  @Override
  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    m_formBuilder = new TypeSourceBuilder(getEntityName());
    m_formBuilder.setFlags(Flags.AccPublic);
    m_formBuilder.setSuperTypeSignature(getSuperTypeSignature());
    addType(m_formBuilder);

    if (getFormDataSignature() != null) {
      m_formBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createFormData(getFormDataSignature(), SdkCommand.CREATE, null));
    }

    addClassId(m_formBuilder, 0);

    addGetConfiguredTitle();

    createMainBox();

    createStartMethod("startModify", MODIFY_HANDLER_NAME);
    createStartMethod("startNew", NEW_HANDLER_NAME);

    createHandler(NEW_HANDLER_NAME);
    createHandler(MODIFY_HANDLER_NAME);
  }

  protected void addGetConfiguredTitle() {
    String nlsKeyName = getEntityName();
    if (nlsKeyName.endsWith(ISdkProperties.SUFFIX_FORM)) {
      nlsKeyName = CoreUtils.ensureStartWithUpperCase(nlsKeyName.substring(0, nlsKeyName.length() - ISdkProperties.SUFFIX_FORM.length()));
    }
    IMethodSourceBuilder getConfiguredTitle = ScoutMethodSourceBuilderFactory.createNlsMethod("getConfiguredTitle", nlsKeyName);
    m_formBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredTitle), getConfiguredTitle);
  }

  protected void createStartMethod(String methodName, final String handlerSimpleName) {
    IMethodSourceBuilder startModifyBuilder = new MethodSourceBuilder(methodName);
    startModifyBuilder.setFlags(Flags.AccPublic);
    startModifyBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    startModifyBuilder.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String modifyHandlerFqn = m_formBuilder.getFullyQualifiedName() + ISignatureConstants.C_DOLLAR + handlerSimpleName;
        if (MODIFY_HANDLER_NAME.equals(handlerSimpleName)) {
          source.append("startInternalExclusive");
        }
        else {
          source.append("startInternal");
        }
        source.append("(new ").append(validator.useName(modifyHandlerFqn)).append("());");
      }
    });
    m_formBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodStartFormKey(startModifyBuilder), startModifyBuilder);
  }

  protected void createMainBox() {
    String formFqn = m_formBuilder.getFullyQualifiedName();

    // main box
    ITypeSourceBuilder mainBoxBuilder = new TypeSourceBuilder("MainBox");
    mainBoxBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createOrder(ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP));
    mainBoxBuilder.setFlags(Flags.AccPublic);
    mainBoxBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractGroupBox));
    addClassId(mainBoxBuilder, 1);
    m_formBuilder.addSortedType(SortedMemberKeyFactory.createTypeFormFieldKey(mainBoxBuilder, ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP), mainBoxBuilder);

    String mainBoxFqn = formFqn + ISignatureConstants.C_DOLLAR + mainBoxBuilder.getElementName();

    // getMainBox
    IMethodSourceBuilder getMainBox = ScoutMethodSourceBuilderFactory.createFieldGetter(Signature.createTypeSignature(mainBoxFqn));
    m_formBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodFormFieldGetterKey(getMainBox), getMainBox);

    // ok button
    int order = 100 * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    ITypeSourceBuilder okButtonBuilder = new TypeSourceBuilder("Ok" + ISdkProperties.SUFFIX_BUTTON);
    okButtonBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createOrder(order));
    okButtonBuilder.setFlags(Flags.AccPublic);
    okButtonBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractOkButton));
    addClassId(okButtonBuilder, 2);
    mainBoxBuilder.addSortedType(SortedMemberKeyFactory.createTypeFormFieldKey(okButtonBuilder, order), okButtonBuilder);

    // getOkButton
    String okButtonSignature = Signature.createTypeSignature(mainBoxFqn + ISignatureConstants.C_DOLLAR + okButtonBuilder.getElementName());
    IMethodSourceBuilder okButtonGetterBuilder = ScoutMethodSourceBuilderFactory.createFieldGetter(okButtonSignature);
    m_formBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodFormFieldGetterKey(okButtonGetterBuilder), okButtonGetterBuilder);

    order += ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    ITypeSourceBuilder cancelButtonBuilder = new TypeSourceBuilder("Cancel" + ISdkProperties.SUFFIX_BUTTON);
    cancelButtonBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createOrder(order));
    cancelButtonBuilder.setFlags(Flags.AccPublic);
    cancelButtonBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractCancelButton));
    addClassId(cancelButtonBuilder, 3);
    mainBoxBuilder.addSortedType(SortedMemberKeyFactory.createTypeFormFieldKey(cancelButtonBuilder, order), cancelButtonBuilder);

    // getCancelButton
    String cancelButtonSignature = Signature.createTypeSignature(mainBoxFqn + ISignatureConstants.C_DOLLAR + cancelButtonBuilder.getElementName());
    IMethodSourceBuilder cancelButtonGetterBuilder = ScoutMethodSourceBuilderFactory.createFieldGetter(cancelButtonSignature);
    m_formBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodFormFieldGetterKey(cancelButtonGetterBuilder), cancelButtonGetterBuilder);
  }

  protected void createHandler(String name) {
    // handler class
    ITypeSourceBuilder handlerBuilder = new TypeSourceBuilder(name);
    handlerBuilder.setFlags(Flags.AccPublic);
    handlerBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractFormHandler));
    m_formBuilder.addSortedType(SortedMemberKeyFactory.createTypeFormHandlerKey(handlerBuilder), handlerBuilder);

    // execLoad
    IMethodSourceBuilder execLoad = MethodSourceBuilderFactory.createOverride(handlerBuilder, getPackageName(), getJavaEnvironment(), LOAD_METHOD_NAME);
    execLoad.setBody(createExecLoadStoreBody(execLoad, handlerBuilder));
    execLoad.removeAnnotation(IScoutRuntimeTypes.Order);
    execLoad.removeAnnotation(IScoutRuntimeTypes.ConfigOperation);
    handlerBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodExecKey(execLoad), execLoad);

    // execStore
    IMethodSourceBuilder execStore = MethodSourceBuilderFactory.createOverride(handlerBuilder, getPackageName(), getJavaEnvironment(), STORE_METHOD_NAME);
    execStore.setBody(createExecLoadStoreBody(execStore, handlerBuilder));
    execStore.removeAnnotation(IScoutRuntimeTypes.Order);
    execStore.removeAnnotation(IScoutRuntimeTypes.ConfigOperation);
    handlerBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodExecKey(execStore), execStore);
  }

  protected HandlerMethodBodySourceBuilder createExecLoadStoreBody(IMethodSourceBuilder methodBuilder, ITypeSourceBuilder handlerBuilder) {
    HandlerMethodBodySourceBuilder handlerMehodBodySourceBuilder = new HandlerMethodBodySourceBuilder(methodBuilder, handlerBuilder);
    handlerMehodBodySourceBuilder.setFormDataSignature(getFormDataSignature());
    handlerMehodBodySourceBuilder.setServiceIfcSignature(getServiceIfcSignature());
    if (MODIFY_HANDLER_NAME.equals(handlerBuilder.getElementName())) {
      handlerMehodBodySourceBuilder.setPermissionSignature(getUpdatePermissionSignature());
    }
    else {
      handlerMehodBodySourceBuilder.setPermissionSignature(getCreatePermissionSignature());
    }
    return handlerMehodBodySourceBuilder;
  }

  protected void addClassId(IAnnotatableSourceBuilder target, int index) {
    String[] classIdValues = getClassIdValues();
    if (classIdValues == null) {
      return;
    }

    target.addAnnotation(ScoutAnnotationSourceBuilderFactory.createClassId(classIdValues[index]));
  }

  public static final class HandlerMethodBodySourceBuilder implements ISourceBuilder {

    public static final String SERVICE_VAR_NAME = "service";
    public static final String FORM_DATA_VAR_NAME = "formData";

    private String m_serviceIfcSignature;
    private String m_formDataSignature;
    private String m_permissionSignature;
    private ISourceBuilder m_methodArgSourceBuilder;
    private ISourceBuilder m_permissionArgSourceBuilder;
    private ISourceBuilder m_formDataInstanceCreationBuilder;
    private final IMethodSourceBuilder m_handlerMethodBuilder;
    private final ITypeSourceBuilder m_handlerBuilder;
    private boolean m_createFormDataInLoad;

    public HandlerMethodBodySourceBuilder(IMethodSourceBuilder handlerMethodBuilder, ITypeSourceBuilder handlerBuilder) {
      m_handlerBuilder = handlerBuilder;
      m_handlerMethodBuilder = handlerMethodBuilder;
      m_createFormDataInLoad = true;
    }

    @Override
    @SuppressWarnings("pmd:NPathComplexity")
    public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
      final boolean isModify = MODIFY_HANDLER_NAME.equals(getHandlerBuilder().getElementName());
      final boolean isLoad = LOAD_METHOD_NAME.equals(getHandlerMethodBuilder().getElementName());

      if (getServiceIfcSignature() != null) {
        final String serviceInterfaceName = validator.useSignature(getServiceIfcSignature());
        source.append(serviceInterfaceName).append(' ').append(SERVICE_VAR_NAME).append(" = ");
        source.append(validator.useSignature(Signature.createTypeSignature(IScoutRuntimeTypes.BEANS))).append(".get(");
        source.append(serviceInterfaceName).append(SuffixConstants.SUFFIX_class).append(");").append(lineDelimiter);

        final boolean isDtoAvailable = getFormDataSignature() != null;
        if (isDtoAvailable) {
          String formDataTypeName = validator.useSignature(getFormDataSignature());
          source.append(formDataTypeName).append(' ').append(FORM_DATA_VAR_NAME).append(" = ");
          if (!isLoad || isCreateFormDataInLoad()) {
            ISourceBuilder formDataInstanceCreationBuilder = getFormDataInstanceCreationBuilder();
            if (formDataInstanceCreationBuilder == null) {
              source.append("new ").append(formDataTypeName).append("();").append(lineDelimiter);
            }
            else {
              formDataInstanceCreationBuilder.createSource(source, lineDelimiter, context, validator);
            }
            source.append("exportFormData(").append(FORM_DATA_VAR_NAME).append(");").append(lineDelimiter);
            if (isLoad) {
              source.append(FORM_DATA_VAR_NAME).append(" = ");
            }
          }
        }
        source.append(SERVICE_VAR_NAME).append('.');
        if (isLoad) {
          if (isModify) {
            source.append(SERVICE_LOAD_METHOD_NAME);
          }
          else {
            source.append(SERVICE_PREPARECREATE_METHOD_NAME);
          }
        }
        else {
          if (isModify) {
            source.append(SERVICE_STORE_METHOD_NAME);
          }
          else {
            source.append(SERVICE_CREATE_METHOD_NAME);
          }
        }
        source.append('(');
        if (getMethodArgSourceBuilder() != null) {
          getMethodArgSourceBuilder().createSource(source, lineDelimiter, context, validator);
        }
        else if (isDtoAvailable) {
          source.append(FORM_DATA_VAR_NAME);
        }
        source.append(");");
        if (isLoad && isDtoAvailable) {
          source.append(lineDelimiter).append("importFormData(").append(FORM_DATA_VAR_NAME).append(");");
        }
      }
      if (isLoad && getPermissionSignature() != null) {
        source.append(lineDelimiter).append(lineDelimiter).append("setEnabledPermission(new ").append(validator.useSignature(getPermissionSignature())).append('(');
        if (getPermissionArgSourceBuilder() != null) {
          getPermissionArgSourceBuilder().createSource(source, lineDelimiter, context, validator);
        }
        source.append("));");
      }
    }

    public String getServiceIfcSignature() {
      return m_serviceIfcSignature;
    }

    public void setServiceIfcSignature(String serviceIfcSignature) {
      m_serviceIfcSignature = serviceIfcSignature;
    }

    public String getFormDataSignature() {
      return m_formDataSignature;
    }

    public void setFormDataSignature(String formDataSignature) {
      m_formDataSignature = formDataSignature;
    }

    public String getPermissionSignature() {
      return m_permissionSignature;
    }

    public void setPermissionSignature(String permissionSignature) {
      m_permissionSignature = permissionSignature;
    }

    public ISourceBuilder getMethodArgSourceBuilder() {
      return m_methodArgSourceBuilder;
    }

    public void setMethodArgSourceBuilder(ISourceBuilder methodArgSourceBuilder) {
      m_methodArgSourceBuilder = methodArgSourceBuilder;
    }

    public ISourceBuilder getPermissionArgSourceBuilder() {
      return m_permissionArgSourceBuilder;
    }

    public void setPermissionArgSourceBuilder(ISourceBuilder permissionArgSourceBuilder) {
      m_permissionArgSourceBuilder = permissionArgSourceBuilder;
    }

    public IMethodSourceBuilder getHandlerMethodBuilder() {
      return m_handlerMethodBuilder;
    }

    public ITypeSourceBuilder getHandlerBuilder() {
      return m_handlerBuilder;
    }

    public ISourceBuilder getFormDataInstanceCreationBuilder() {
      return m_formDataInstanceCreationBuilder;
    }

    public void setFormDataInstanceCreationBuilder(ISourceBuilder formDataInstanceCreationBuilder) {
      m_formDataInstanceCreationBuilder = formDataInstanceCreationBuilder;
    }

    public boolean isCreateFormDataInLoad() {
      return m_createFormDataInLoad;
    }

    public void setCreateFormDataInLoad(boolean createFormDataInLoad) {
      m_createFormDataInLoad = createFormDataInLoad;
    }
  }

  public String getFormDataSignature() {
    return m_formDataSignature;
  }

  public void setFormDataSignature(String formDataSignature) {
    m_formDataSignature = formDataSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getServiceIfcSignature() {
    return m_serviceIfcSignature;
  }

  public void setServiceIfcSignature(String serviceIfcSignature) {
    m_serviceIfcSignature = serviceIfcSignature;
  }

  public String getUpdatePermissionSignature() {
    return m_updatePermissionSignature;
  }

  public void setUpdatePermissionSignature(String updatePermissionSignature) {
    m_updatePermissionSignature = updatePermissionSignature;
  }

  public String getCreatePermissionSignature() {
    return m_createPermissionSignature;
  }

  public void setCreatePermissionSignature(String createPermissionSignature) {
    m_createPermissionSignature = createPermissionSignature;
  }

  public String[] getClassIdValues() {
    return m_classIdValues;
  }

  public void setClassIdValues(String[] classIdValues) {
    Validate.isTrue(Validate.notNull(classIdValues).length == NUM_CLASS_IDS);
    m_classIdValues = Arrays.copyOf(classIdValues, classIdValues.length);
  }
}
