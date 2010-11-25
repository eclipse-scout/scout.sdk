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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.form.util.ComposerFieldDataSourceBuilder;
import org.eclipse.scout.sdk.operation.form.util.FormDataSourceBuilder;
import org.eclipse.scout.sdk.operation.form.util.FormDataUtility;
import org.eclipse.scout.sdk.operation.form.util.ISourceBuilder;
import org.eclipse.scout.sdk.operation.form.util.SimpleFieldDataSourceBuilder;
import org.eclipse.scout.sdk.operation.form.util.TableFieldDataSourceBuilder;
import org.eclipse.scout.sdk.operation.form.util.FormDataUtility.FormDataAnnotationDesc;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 * <h3>FormDataNewProcess</h3> ...
 */
public class FormDataUpdateOperation implements IOperation {

  private final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  private final IType iTableField = ScoutSdk.getType(RuntimeClasses.ITableField);
  private final IType iComposerField = ScoutSdk.getType(RuntimeClasses.IComposerField);

  private final IType m_form;
  private IScoutBundle m_sharedBundle;
  private IType m_formDataType;
  // operation members
  private String m_formDataPackageName;
  private String m_formDataSimpleName;

  public FormDataUpdateOperation(IType form) {
    IScoutBundle clientBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(form.getJavaProject().getProject());
    IScoutBundle[] sharedBundles = clientBundle.getRequiredBundles(ScoutBundleFilters.getSharedFilter(), false);
    if (sharedBundles.length > 0) {
      setSharedBundle(sharedBundles[0]);
    }
    m_form = form;
    // default
    m_formDataPackageName = getSharedBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS);
  }

  public FormDataUpdateOperation(IType form, IScoutBundle sharedBundle) {
    this(form);
    setSharedBundle(sharedBundle);
  }

  public FormDataUpdateOperation(IType form, IType formDataType) {
    this(form);
    setFormDataType(formDataType);
  }

  public String getOperationName() {
    return "create form data";
  }

  public void validate() throws IllegalArgumentException {
    if (getFormDataType() == null) {
      // check shared
      if (getSharedBundle() == null) {
        throw new IllegalArgumentException("formDataType of shared bundle must be set");
      }
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ITypeHierarchy formFieldHierarchy = ScoutSdk.getLocalTypeHierarchy(getForm());
    if (getFormDataType() != null && getFormDataType().exists()) {
      m_formDataPackageName = getFormDataType().getPackageFragment().getElementName();
      m_formDataSimpleName = getFormDataType().getElementName();
    }
    else {
      // check if form uses auto formdata support
      FormDataAnnotationDesc desc = FormDataUtility.parseFormDataAnnotation(getForm(), formFieldHierarchy);
      if (desc != null) {
        m_formDataSimpleName = getForm().getElementName() + "Data";
      }
      else {
        ScoutSdk.logWarning("no form data name found.");
        return;
      }
    }
    FormDataSourceBuilder sourceBuilder = new FormDataSourceBuilder(m_formDataSimpleName, getForm());
    sourceBuilder.build();
    recAddFormFields(getForm(), formFieldHierarchy, true, sourceBuilder, monitor, workingCopyManager);

    // create java
    setFormDataType(storeFormData(m_formDataSimpleName, sourceBuilder, workingCopyManager, monitor));
  }

  private void recAddFormFields(IType parentUiType, ITypeHierarchy formFieldHierarchy, boolean implementation, ISourceBuilder sourceBuilder, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ITypeFilter formFieldFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getSubtypeFilter(iFormField, formFieldHierarchy), TypeFilters.getClassFilter());
    for (IType t : TypeUtility.getInnerTypes(parentUiType, formFieldFilter, TypeComparators.getOrderAnnotationComparator())) {
      recAddFormField(t, formFieldHierarchy, implementation, sourceBuilder, monitor, workingCopyManager);
    }
  }

  private void recAddFormField(IType uiFieldType, ITypeHierarchy formFieldHierarchy, boolean implementation, ISourceBuilder sourceBuilder, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    FormDataAnnotationDesc desc = FormDataUtility.parseFormDataAnnotation(uiFieldType, formFieldHierarchy);
    if (desc != null) {
      IType fieldDataSuperType = null;
      if (desc.externalFlag) {
        // create sub field data
        fieldDataSuperType = createExternalFieldData(formFieldHierarchy.getSuperclass(uiFieldType), monitor, workingCopyManager);
        implementation = false;
      }
      if (desc.usingFlag && desc.fullyQualifiedName != null) {
        fieldDataSuperType = ScoutSdk.getType(desc.fullyQualifiedName);
        implementation = false;
      }
      appendType(uiFieldType, formFieldHierarchy, implementation, fieldDataSuperType, sourceBuilder);
    }
    // recursion
    recAddFormFields(uiFieldType, formFieldHierarchy, implementation, sourceBuilder, monitor, workingCopyManager);
  }

  private void appendType(IType uiFieldType, ITypeHierarchy formFieldHierarchy, boolean implementation, IType fieldDataSuperType, ISourceBuilder sourceBuilder) throws CoreException {
    String fieldDataSimpleName = FormDataUtility.getBeanName(FormDataUtility.getFieldIdWithoutSuffix(uiFieldType.getElementName()), true);
    if (fieldDataSuperType != null) {
      SimpleFieldDataSourceBuilder bean = new SimpleFieldDataSourceBuilder(fieldDataSimpleName, Signature.createTypeSignature(fieldDataSuperType.getFullyQualifiedName(), true), uiFieldType, sourceBuilder, implementation);
      bean.build();
      bean.appendToParent(sourceBuilder);
    }
    else if (formFieldHierarchy.isSubtype(iComposerField, uiFieldType)) {
      ComposerFieldDataSourceBuilder builder = new ComposerFieldDataSourceBuilder(fieldDataSimpleName, uiFieldType, formFieldHierarchy, sourceBuilder, implementation);
      builder.build();
      builder.appendToParent(sourceBuilder);
    }
    else {
      if (formFieldHierarchy.isSubtype(iTableField, uiFieldType)) {
        TableFieldDataSourceBuilder bean = new TableFieldDataSourceBuilder(fieldDataSimpleName, uiFieldType, formFieldHierarchy, sourceBuilder, implementation);
        bean.build();
        bean.appendToParent(sourceBuilder);
      }
      else {
        String genericTypeSig = createFormDataSignatureFor(uiFieldType, formFieldHierarchy);
        if (genericTypeSig != null) {
          String superSig = Signature.createTypeSignature(RuntimeClasses.AbstractValueFieldData, true);
          superSig = superSig.replaceAll("\\;$", "<" + genericTypeSig + ">;");

          SimpleFieldDataSourceBuilder bean = new SimpleFieldDataSourceBuilder(fieldDataSimpleName, superSig, uiFieldType, sourceBuilder, implementation);
          bean.build();
          bean.appendToParent(sourceBuilder);
        }
        else {
          String sig = Signature.createTypeSignature(RuntimeClasses.AbstractFormFieldData, true);
          SimpleFieldDataSourceBuilder bean = new SimpleFieldDataSourceBuilder(fieldDataSimpleName, sig, uiFieldType, sourceBuilder, implementation);
          bean.build();
          bean.appendToParent(sourceBuilder);
        }
      }
    }
  }

  private String createFormDataSignatureFor(IType type, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    if (type == null || type.getFullyQualifiedName().equals(Object.class.getName())) {
      return null;
    }
    IType superType = formFieldHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        // compute generic parameter type by merging all super type generic parameter declarations
        List<GenericSignatureMapping> signatureMapping = new ArrayList<GenericSignatureMapping>();
        IType currentType = type;
        IType currentSuperType = superType;
        while (currentSuperType != null) {
          if (TypeUtility.isGenericType(currentSuperType)) {
            String superTypeGenericParameterName = currentSuperType.getTypeParameters()[0].getElementName();
            String currentSuperTypeSig = currentType.getSuperclassTypeSignature();
            String superTypeGenericParameterSignature = ScoutSdkUtility.getResolvedSignature(Signature.getTypeArguments(currentSuperTypeSig)[0], currentType);
            signatureMapping.add(0, new GenericSignatureMapping(superTypeGenericParameterName, superTypeGenericParameterSignature));
            currentType = currentSuperType;

            currentSuperType = formFieldHierarchy.getSuperclass(currentSuperType);
          }
          else {
            break;
          }
        }

        String signature = signatureMapping.get(0).getSuperTypeGenericParameterSignature();
        for (int i = 1; i < signatureMapping.size(); i++) {
          String replacement = signatureMapping.get(i).getSuperTypeGenericParameterSignature();
          replacement = replacement.substring(0, replacement.length() - 1);
          signature = signature.replaceAll("[T,L,Q]" + signatureMapping.get(i).getSuperTypeGenericParameterName(), replacement);
        }
        return ScoutSdkUtility.getResolvedSignature(signature, type);
      }
      else {
        return createFormDataSignatureFor(superType, formFieldHierarchy);
      }
    }
    else {
      return null;
    }
  }

  private IType createExternalFieldData(IType uiFieldType, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    String fieldDataSimpleName = uiFieldType.getElementName() + "Data";
    ISourceBuilder sourceBuilder;

    // compute type hierarchy for external form field
    ITypeHierarchy formFieldHierarchy = ScoutSdk.getLocalTypeHierarchy(uiFieldType);

    if (formFieldHierarchy.isSubtype(iTableField, uiFieldType)) {
      sourceBuilder = new TableFieldDataSourceBuilder(fieldDataSimpleName, uiFieldType, formFieldHierarchy, (IImportValidator) null, true);
      sourceBuilder.build();
    }
    else {
      FormDataAnnotationDesc desc = FormDataUtility.parseFormDataAnnotation(uiFieldType, formFieldHierarchy);
      String sig = null;
      if (desc != null) {
        IType fieldDataSuperType = null;
        if (desc.externalFlag) {
          // create sub field data
          fieldDataSuperType = createExternalFieldData(formFieldHierarchy.getSuperclass(uiFieldType), monitor, workingCopyManager);
        }
        if (desc.usingFlag && desc.fullyQualifiedName != null) {
          fieldDataSuperType = ScoutSdk.getType(desc.fullyQualifiedName);
        }
        if (fieldDataSuperType != null) {
          sig = Signature.createTypeSignature(fieldDataSuperType.getFullyQualifiedName(), true);
        }
      }

      if (sig == null || sig.isEmpty()) {
        String genericTypeSig = createFormDataSignatureFor(uiFieldType, formFieldHierarchy);
        if (genericTypeSig != null) {
          sig = Signature.createTypeSignature(RuntimeClasses.AbstractValueFieldData, true);
          sig = sig.replaceAll("\\;$", "<" + genericTypeSig + ">;");
        }
        else {
          sig = Signature.createTypeSignature(RuntimeClasses.AbstractFormFieldData, true);
        }
      }
      sourceBuilder = new SimpleFieldDataSourceBuilder(fieldDataSimpleName, sig, uiFieldType, (IImportValidator) null, true);
      sourceBuilder.build();
      recAddFormFields(uiFieldType, formFieldHierarchy, true, sourceBuilder, monitor, workingCopyManager);
    }

    // create java
    return storeFormData(fieldDataSimpleName, sourceBuilder, workingCopyManager, monitor);
  }

  private IType storeFormData(String formDataSimpleName, ISourceBuilder sourceBuilder, IScoutWorkingCopyManager workingCopyManager, IProgressMonitor monitor) throws CoreException {
    IType type = ScoutSdk.getType(m_formDataPackageName + "." + formDataSimpleName);
    ICompilationUnit icu = null;
    if (TypeUtility.exists(type)) {
      icu = type.getCompilationUnit();
      workingCopyManager.register(icu, monitor);
      icu.getBuffer().setContents("");
      workingCopyManager.reconcile(icu, monitor);
    }
    else {
      IPackageFragment pck = getSharedBundle().getSpecificPackageFragment(m_formDataPackageName, monitor, workingCopyManager);
      icu = pck.createCompilationUnit(formDataSimpleName + ".java", "", true, monitor);
    }
    icu.createPackageDeclaration(m_formDataPackageName, monitor);
    workingCopyManager.register(icu, monitor);
    Document doc = sourceBuilder.createDocument();
    // imports
    for (String imp : sourceBuilder.getImportValidator().getImportsToCreate()) {
      icu.createImport(imp, null, monitor);
    }
    // source
    type = icu.createType(doc.get(), null, true, monitor);
    JavaElementFormatOperation op = new JavaElementFormatOperation(type, false);
    op.run(monitor, workingCopyManager);
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{type.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);
    return type;
  }

  public void setFormDataType(IType formDataType) {
    m_formDataType = formDataType;
  }

  public IType getFormDataType() {
    return m_formDataType;
  }

  public void setSharedBundle(IScoutBundle sharedBundle) {
    m_sharedBundle = sharedBundle;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  public void setFormDataPackageName(String formDataPackageName) {
    m_formDataPackageName = formDataPackageName;
  }

  public String getFormDataPackageName() {
    return m_formDataPackageName;
  }

  public IType getForm() {
    return m_form;
  }

  private static class GenericSignatureMapping {
    private final String m_superTypeGenericParameterName;
    private final String m_superTypeGenericParameterSignature;

    public GenericSignatureMapping(String superTypeGenericParameterName, String superTypeGenericParameterSignature) {
      m_superTypeGenericParameterName = superTypeGenericParameterName;
      m_superTypeGenericParameterSignature = superTypeGenericParameterSignature;
    }

    public String getSuperTypeGenericParameterName() {
      return m_superTypeGenericParameterName;
    }

    public String getSuperTypeGenericParameterSignature() {
      return m_superTypeGenericParameterSignature;
    }
  }
}
