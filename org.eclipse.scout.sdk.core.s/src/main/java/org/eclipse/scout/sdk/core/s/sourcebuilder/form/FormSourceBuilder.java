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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.model.ScoutMethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.sourcebuilder.service.ServiceInterfaceSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.IAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link FormSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormSourceBuilder extends CompilationUnitSourceBuilder {

  private static final String MODIFY_HANDLER_NAME = "ModifyHandler";
  public static final int NUM_CLASS_IDS = 4;

  private String m_formDataSignature;
  private String m_superTypeSignature;
  private String m_serviceIfcSignature;
  private String m_updatePermissionSignature;
  private String[] m_classIdValues;
  private final String m_formName;

  private ITypeSourceBuilder m_formBuilder;

  public FormSourceBuilder(String formName, String packageName) {
    super(formName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_formName = formName;
  }

  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    m_formBuilder = new TypeSourceBuilder(getFormName());
    m_formBuilder.setFlags(Flags.AccPublic);
    m_formBuilder.setSuperTypeSignature(getSuperTypeSignature());
    addType(m_formBuilder);

    // @FormData annotation
    if (getFormDataSignature() != null) {
      m_formBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createFormData(getFormDataSignature(), SdkCommand.CREATE, null));
    }
    // @ClassId annotation
    addClassId(m_formBuilder, 0);

    createConstructor();

    createMainBox();

    createModifyHandler();
  }

  protected void createConstructor() {
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructor(getFormName());
    constructorBuilder.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String modifyHandlerFqn = m_formBuilder.getFullyQualifiedName() + ISignatureConstants.C_DOLLAR + MODIFY_HANDLER_NAME;
        source.append("setHandler(new ").append(validator.useName(modifyHandlerFqn)).append("());");
      }
    });
    m_formBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
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

  protected void createModifyHandler() {
    // modify handler
    ITypeSourceBuilder modifyHandlerBuilder = new TypeSourceBuilder(MODIFY_HANDLER_NAME);
    modifyHandlerBuilder.setFlags(Flags.AccPublic);
    modifyHandlerBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractFormHandler));
    m_formBuilder.addSortedType(SortedMemberKeyFactory.createTypeFormHandlerKey(modifyHandlerBuilder), modifyHandlerBuilder);

    // execLoad
    IMethodSourceBuilder execLoad = new MethodSourceBuilder("execLoad");
    execLoad.setFlags(Flags.AccProtected);
    execLoad.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    execLoad.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
    execLoad.setBody(createExecLoadStoreBody(true));
    modifyHandlerBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodExecKey(execLoad), execLoad);

    // execStore
    IMethodSourceBuilder execStore = new MethodSourceBuilder("execStore");
    execStore.setFlags(Flags.AccProtected);
    execStore.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    execStore.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
    execStore.setBody(createExecLoadStoreBody(false));
    modifyHandlerBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodExecKey(execStore), execStore);
  }

  protected ExecLoadStoreBodySourceBuilder createExecLoadStoreBody(final boolean isLoad) {
    ExecLoadStoreBodySourceBuilder execLoadStoreBodySourceBuilder = new ExecLoadStoreBodySourceBuilder(isLoad);
    execLoadStoreBodySourceBuilder.setFormDataStaticSignature(getFormDataSignature());
    execLoadStoreBodySourceBuilder.setServiceIfcSignature(getServiceIfcSignature());
    execLoadStoreBodySourceBuilder.setUpdatePermissionSignature(getUpdatePermissionSignature());
    return execLoadStoreBodySourceBuilder;
  }

  protected void addClassId(IAnnotatableSourceBuilder target, int index) {
    String[] classIdValues = getClassIdValues();
    if (classIdValues == null) {
      return;
    }

    target.addAnnotation(ScoutAnnotationSourceBuilderFactory.createClassId(classIdValues[index]));
  }

  public static final class ExecLoadStoreBodySourceBuilder implements ISourceBuilder {

    private String m_serviceIfcSignature;
    private String m_formDataStaticSignature;
    private String m_formDataDynamicSignature;
    private String m_updatePermissionSignature;
    private ISourceBuilder m_methodArgSourceBuilder;
    private ISourceBuilder m_permissionArgSourceBuilder;
    private final boolean m_isLoad;

    public ExecLoadStoreBodySourceBuilder(boolean isLoad) {
      m_isLoad = isLoad;
    }

    @Override
    public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
      if (getServiceIfcSignature() != null) {
        boolean isDtoAvailable = getFormDataStaticSignature() != null;
        String formDataVarName = "formData";
        String serviceVarName = "service";
        String serviceInterfaceName = validator.useSignature(getServiceIfcSignature());
        String formDataStaticTypeName = null;
        String formDataDynamicTypeName = null;
        if (isDtoAvailable) {
          formDataStaticTypeName = validator.useSignature(getFormDataStaticSignature());
          formDataDynamicTypeName = validator.useSignature(getFormDataDynamicSignature());
        }
        source.append(serviceInterfaceName).append(' ').append(serviceVarName).append(" = ");
        source.append(validator.useSignature(Signature.createTypeSignature(IScoutRuntimeTypes.BEANS))).append(".get(");
        source.append(serviceInterfaceName).append(SuffixConstants.SUFFIX_STRING_class).append(");").append(lineDelimiter);
        if (isDtoAvailable) {
          source.append(formDataStaticTypeName).append(' ').append(formDataVarName).append(" = new ").append(formDataDynamicTypeName).append("();").append(lineDelimiter);
          source.append("exportFormData(").append(formDataVarName).append(");").append(lineDelimiter);
          if (isLoad()) {
            source.append(formDataVarName).append(" = ");
          }
        }
        source.append(serviceVarName).append('.');
        if (isLoad()) {
          source.append(ServiceInterfaceSourceBuilder.SERVICE_LOAD_METHOD_NAME);
        }
        else {
          source.append(ServiceInterfaceSourceBuilder.SERVICE_STORE_METHOD_NAME);
        }
        source.append('(');
        if (getMethodArgSourceBuilder() != null) {
          getMethodArgSourceBuilder().createSource(source, lineDelimiter, context, validator);
        }
        else if (isDtoAvailable) {
          source.append(formDataVarName);
        }
        source.append(");");
        if (isLoad() && isDtoAvailable) {
          source.append(lineDelimiter).append("importFormData(").append(formDataVarName).append(");");
        }
      }
      if (isLoad() && getUpdatePermissionSignature() != null) {
        source.append(lineDelimiter).append(lineDelimiter).append("setEnabledPermission(new ").append(validator.useSignature(getUpdatePermissionSignature())).append("(");
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

    public String getFormDataStaticSignature() {
      return m_formDataStaticSignature;
    }

    public void setFormDataStaticSignature(String formDataStaticSignature) {
      m_formDataStaticSignature = formDataStaticSignature;
    }

    public String getFormDataDynamicSignature() {
      if (m_formDataDynamicSignature == null) {
        return m_formDataStaticSignature;
      }
      return m_formDataDynamicSignature;
    }

    public void setFormDataDynamicSignature(String formDataDynamicSignature) {
      m_formDataDynamicSignature = formDataDynamicSignature;
    }

    public String getUpdatePermissionSignature() {
      return m_updatePermissionSignature;
    }

    public void setUpdatePermissionSignature(String updatePermissionSignature) {
      m_updatePermissionSignature = updatePermissionSignature;
    }

    public boolean isLoad() {
      return m_isLoad;
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
  }

  public String getFormDataSignature() {
    return m_formDataSignature;
  }

  public void setFormDataSignature(String formDataSignature) {
    m_formDataSignature = formDataSignature;
  }

  public String getFormName() {
    return m_formName;
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

  public String[] getClassIdValues() {
    return m_classIdValues;
  }

  public void setClassIdValues(String[] classIdValues) {
    Validate.isTrue(Validate.notNull(classIdValues).length == NUM_CLASS_IDS);
    m_classIdValues = Arrays.copyOf(classIdValues, classIdValues.length);
  }
}
