/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.form;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link FormNewOperation}</h3> To create a new form. A form is either a view, editor or a dialog.
 * 
 * @author Andreas Hoegger
 * @since 1.0.0 2008
 */
public class FormNewOperation extends PrimaryTypeNewOperation {

  private INlsEntry m_nlsEntry;
  private String m_formDataSignature;
  private String m_formIdName;
  private String m_formIdSignature;
  private boolean m_createButtonOk;
  private boolean m_createButtonCancel;
  private boolean m_createMainBox;

  private IType m_createdMainBox;
  private IMethod m_createdNlsLabelMethod;
  private IMethod m_createdMainBoxGetter;

  public FormNewOperation(String typeName, String packageName, IJavaProject clientProject) throws JavaModelException {
    super(typeName, packageName, clientProject);

    // defaults
    setPackageExportPolicy(ExportPolicy.AddPackage);
    setFlags(Flags.AccPublic);
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IForm, getJavaProject()));
    getSourceBuilder().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    getCompilationUnitNewOp().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setFormatSource(true);
    setCreateMainBox(true);
  }

  @Override
  public String getOperationName() {
    return "New Form...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.hasText(getFormIdName()) && StringUtility.isNullOrEmpty(getFormIdSignature()) ||
        StringUtility.isNullOrEmpty(getFormIdName()) && StringUtility.hasText(getFormIdSignature())) {
      throw new IllegalArgumentException("Form id is not set properly. 'formIdName' and 'formIdSignature' must be set!");
    }
    super.validate();
  }

  protected void createConstructor(ITypeSourceBuilder formBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    constructorBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    constructorBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    constructorBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    formBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (!StringUtility.isNullOrEmpty(getFormDataSignature())) {
      addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation(getFormDataSignature(), SdkCommand.CREATE, null));
    }

    createConstructor(getSourceBuilder(), monitor, workingCopyManager);

    // nls text method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }

    if (StringUtility.hasText(getFormIdName())) {
      createFormIdProperty(getSourceBuilder());
    }

    if (isCreateMainBox()) {
      createMainBox(getSourceBuilder(), monitor, workingCopyManager);
    }

    super.run(monitor, workingCopyManager);

    m_createdMainBox = TypeUtility.findInnerType(getCreatedType(), SdkProperties.TYPE_NAME_MAIN_BOX);

    workingCopyManager.register(getCreatedType().getCompilationUnit(), monitor);
  }

  /**
   * @param formBuilder
   * @return mainbox builder
   * @throws CoreException
   */
  protected void createMainBox(ITypeSourceBuilder formBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // main box
    ITypeSourceBuilder mainBoxBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_MAIN_BOX);
    mainBoxBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10.0));
    mainBoxBuilder.setFlags(Flags.AccPublic);
    mainBoxBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IGroupBox, getJavaProject()));
    addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(mainBoxBuilder, 10.0), mainBoxBuilder);

    // main box getter
    final String mainBoxSignature = SignatureCache.createTypeSignature(getPackageName() + "." + getElementName() + "." + SdkProperties.TYPE_NAME_MAIN_BOX);
    IMethodSourceBuilder mainBoxGetterBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(mainBoxSignature);
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(mainBoxGetterBuilder), mainBoxGetterBuilder);

    fillMainBox(formBuilder, mainBoxBuilder);
  }

  /**
   * @param formBuilder
   * @param mainBoxBuilder
   * @param nextOrderNr
   */
  protected void fillMainBox(ITypeSourceBuilder formBuilder, ITypeSourceBuilder mainBoxBuilder) {
    if (isCreateButtonOk()) {
      createOkButton(getSourceBuilder(), mainBoxBuilder, 10);
    }

    // cancel button
    if (isCreateButtonCancel()) {
      createCancelButton(getSourceBuilder(), mainBoxBuilder, 20);
    }
  }

  protected void createOkButton(ITypeSourceBuilder formBuilder, ITypeSourceBuilder mainboxBuilder, double order) {
    // ok button
    ITypeSourceBuilder okButtonBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_OK_BUTTON);
    okButtonBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(order));
    okButtonBuilder.setFlags(Flags.AccPublic);
    okButtonBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.AbstractOkButton, getJavaProject()));
    mainboxBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(okButtonBuilder, order), okButtonBuilder);
    // getter
    final String okButtonSignature = SignatureCache.createTypeSignature(getPackageName() + "." + getElementName() + "." + SdkProperties.TYPE_NAME_MAIN_BOX + "." + okButtonBuilder.getElementName());
    IMethodSourceBuilder okButtonGetterBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(okButtonSignature);
    formBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(okButtonGetterBuilder), okButtonGetterBuilder);
  }

  protected void createCancelButton(ITypeSourceBuilder formBuilder, ITypeSourceBuilder mainboxBuilder, double order) {
    ITypeSourceBuilder cancelButtonBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_CANCEL_BUTTON);
    cancelButtonBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(order));
    cancelButtonBuilder.setFlags(Flags.AccPublic);
    cancelButtonBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.AbstractCancelButton, getJavaProject()));
    mainboxBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(cancelButtonBuilder, order), cancelButtonBuilder);
    // getter
    final String cancelButtonSignature = SignatureCache.createTypeSignature(getPackageName() + "." + getElementName() + "." + SdkProperties.TYPE_NAME_MAIN_BOX + "." + cancelButtonBuilder.getElementName());
    IMethodSourceBuilder cancelButtonGetterBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(cancelButtonSignature);
    formBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(cancelButtonGetterBuilder), cancelButtonGetterBuilder);
  }

  protected void createFormIdProperty(ITypeSourceBuilder formBuilder) {
    String propertyName = getFormIdName();
    IFieldSourceBuilder fieldBuilder = new FieldSourceBuilder("m_" + NamingUtility.ensureStartWithLowerCase(propertyName));
    fieldBuilder.setSignature(getFormIdSignature());
    fieldBuilder.setFlags(Flags.AccPrivate);
    addFieldSourceBuilder(fieldBuilder);

    // getter
    IMethodSourceBuilder getterBuilder = new MethodSourceBuilder("get" + NamingUtility.ensureStartWithUpperCase(propertyName));
    getterBuilder.setFlags(Flags.AccPublic);
    getterBuilder.setReturnTypeSignature(getFormIdSignature());
    getterBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation());
    getterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodGetterCommentBuilder());
    getterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return " + fieldBuilder.getElementName() + ";"));
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(getterBuilder), getterBuilder);

    // setter
    IMethodSourceBuilder setterBuilder = new MethodSourceBuilder("set" + NamingUtility.ensureStartWithUpperCase(propertyName));
    setterBuilder.setFlags(Flags.AccPublic);
    setterBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    setterBuilder.setParameters(new MethodParameter[]{new MethodParameter(NamingUtility.ensureStartWithLowerCase(propertyName), getFormIdSignature())});
    setterBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation());
    setterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodSetterCommentBuilder());
    setterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody(fieldBuilder.getElementName() + " = " + NamingUtility.ensureStartWithLowerCase(propertyName) + ";"));
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(setterBuilder), setterBuilder);
  }

  public IType getCreatedMainBox() {
    return m_createdMainBox;
  }

  public IMethod getCreatedNlsLabelMethod() {
    return m_createdNlsLabelMethod;
  }

  public IMethod getCreatedMainBoxGetter() {
    return m_createdMainBoxGetter;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public void setFormIdName(String formIdName) {
    m_formIdName = formIdName;
  }

  public String getFormIdName() {
    return m_formIdName;
  }

  public void setFormIdSignature(String formIdSignature) {
    m_formIdSignature = formIdSignature;
  }

  public String getFormIdSignature() {
    return m_formIdSignature;
  }

  /**
   * @param formDataSignature
   *          the formDataSignature to set
   */
  public void setFormDataSignature(String formDataSignature) {
    m_formDataSignature = formDataSignature;
  }

  /**
   * @return the formDataSignature
   */
  public String getFormDataSignature() {
    return m_formDataSignature;
  }

  public void setCreateButtonOk(boolean createButtonOk) {
    m_createButtonOk = createButtonOk;
  }

  public boolean isCreateButtonOk() {
    return m_createButtonOk;
  }

  public void setCreateButtonCancel(boolean createButtonCancel) {
    m_createButtonCancel = createButtonCancel;
  }

  public boolean isCreateButtonCancel() {
    return m_createButtonCancel;
  }

  public boolean isCreateMainBox() {
    return m_createMainBox;
  }

  public void setCreateMainBox(boolean createMainBox) {
    m_createMainBox = createMainBox;
  }

}
