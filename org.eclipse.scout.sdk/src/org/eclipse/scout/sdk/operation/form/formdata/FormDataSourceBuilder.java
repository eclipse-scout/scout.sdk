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
package org.eclipse.scout.sdk.operation.form.formdata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormDataChecksum;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.jdt.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.operation.form.util.FormDataUtility;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;

public class FormDataSourceBuilder extends SourceBuilderWithProperties {
  final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  final IType iTableField = ScoutSdk.getType(RuntimeClasses.ITableField);
  final IType iComposerField = ScoutSdk.getType(RuntimeClasses.IComposerField);
  final IType iCompositeField = ScoutSdk.getType(RuntimeClasses.ICompositeField);

  private String m_packageName;
  private Document m_source;
  private final FormDataAnnotation m_formDataAnnotation;
  private String m_typeSource;

  public FormDataSourceBuilder(IType type, FormDataAnnotation formDataAnnotation) {
    this(type, formDataAnnotation, ScoutSdk.getLocalTypeHierarchy(type));
  }

  public FormDataSourceBuilder(IType type, FormDataAnnotation formDataAnnotation, ITypeHierarchy formFieldHierarchy) {
    super(type);
    final long checkSum = ScoutSdkUtility.getAdler32Checksum(type.getCompilationUnit());
    addAnnotation(new AnnotationSourceBuilder(Signature.createTypeSignature(FormDataChecksum.class.getName(), true)) {
      @Override
      public String createSource(IImportValidator validator) {
        return super.createSource(validator) + "(" + checkSum + "l)";
      }
    });
    m_formDataAnnotation = formDataAnnotation;
    setElementName(type.getElementName() + "Data");
    int flags = Flags.AccPublic;
    try {
      if (Flags.isAbstract(type.getFlags())) {
        flags |= Flags.AccAbstract;
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not determ abstract flag of '" + type.getFullyQualifiedName() + "'.", e);
    }
    setFlags(flags);
    // super type
    setSuperTypeSignature(m_formDataAnnotation.getSuperTypeSignature());
    IScoutBundle sharedBundle = SdkTypeUtility.getScoutBundle(type).getScoutProject().getSharedBundle();
    m_packageName = sharedBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS);
    visitFormFields(type, formFieldHierarchy);
  }

  public void createFormattedSource(IProgressMonitor monitor, IJavaProject project) throws MalformedTreeException, BadLocationException, CoreException {
    IImportValidator validator = new SimpleImportValidator();

    m_typeSource = createSource(validator);
    Document bodyDocument = new Document(m_typeSource);
    MultiTextEdit multiEdit = new MultiTextEdit();
    if (!StringUtility.isNullOrEmpty(m_packageName)) {
      multiEdit.addChild(new InsertEdit(0, "package " + m_packageName + ";" + bodyDocument.getDefaultLineDelimiter()));
    }
    for (String imp : validator.getImportsToCreate()) {
      multiEdit.addChild(new InsertEdit(0, "import " + imp + ";" + bodyDocument.getDefaultLineDelimiter()));
    }
    multiEdit.apply(bodyDocument);

    SourceFormatOperation formatOp = new SourceFormatOperation(project, bodyDocument, null);
    formatOp.run(monitor, null);
    m_source = formatOp.getDocument();
  }

  /**
   * @return the source
   */
  public String getSource() {
    return m_source.get();
  }

  public String getTypeSource() {
    return m_typeSource.trim();
  }

  protected void visitFormFields(IType declaringType, ITypeHierarchy formFieldHierarchy) {
    try {
      if (declaringType.getTypes().length > 0) {
        if (formFieldHierarchy == null) {
          formFieldHierarchy = ScoutSdk.getLocalTypeHierarchy(declaringType);
        }
        ITypeFilter formFieldFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getSubtypeFilter(iFormField, formFieldHierarchy));//, TypeFilters.getClassFilter());
        for (IType t : TypeUtility.getInnerTypes(declaringType, formFieldFilter, TypeComparators.getOrderAnnotationComparator())) {
          try {
            addFormField(t, formFieldHierarchy);
          }
          catch (JavaModelException e) {
            ScoutSdk.logError("could not add form field '" + declaringType.getElementName() + "' to form data.", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("error during visiting type '" + declaringType.getElementName() + "'", e);
    }
  }

  protected void addFormField(IType formField, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    FormDataAnnotation formDataAnnotation = SdkTypeUtility.findFormDataAnnotation(formField, formFieldHierarchy);
    if (formDataAnnotation != null) {
      if (FormDataAnnotation.isCreate(formDataAnnotation)) {
        String formDataElementName = FormDataUtility.getBeanName(FormDataUtility.getFieldIdWithoutSuffix(formField.getElementName()), true);

        String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
        if (formDataAnnotation.getGenericOrdinal() >= 0) {
          IType superType = ScoutSdk.getTypeBySignature(superTypeSignature);
          if (TypeUtility.isGenericType(superType)) {
            String genericTypeSig = createFormDataSignatureFor(formField, formFieldHierarchy);
            if (genericTypeSig != null) {
              superTypeSignature = superTypeSignature.replaceAll("\\;$", "<" + genericTypeSig + ">;");
            }
          }

        }
        ITypeSourceBuilder builder = getTypeSourceBuilderFor(superTypeSignature, formField, formFieldHierarchy);
        builder.setElementName(formDataElementName);
        builder.setSuperTypeSignature(superTypeSignature);
        addBuilder(builder, CATEGORY_TYPE_FIELD);
        MethodSourceBuilder getterBuilder = new MethodSourceBuilder();
        getterBuilder.setElementName("get" + formDataElementName);
        getterBuilder.setReturnSignature(Signature.createTypeSignature(formDataElementName, false));
        getterBuilder.setSimpleBody("return getFieldByClass(" + formDataElementName + ".class);");
        addBuilder(getterBuilder, CATEGORY_METHOD_FIELD_GETTER);

        return;
      }
      else if (FormDataAnnotation.isIgnore(formDataAnnotation)) {
        return;
      }
    }
    // visit children
    if (formFieldHierarchy.isSubtype(iCompositeField, formField)) {
      visitFormFields(formField, formFieldHierarchy);
    }
  }

  private ITypeSourceBuilder getTypeSourceBuilderFor(String superTypeSignature, IType formField, ITypeHierarchy hierarchy) {
    ITypeSourceBuilder builder = null;
    String typeErasure = Signature.getTypeErasure(superTypeSignature);
    if (Signature.toString(typeErasure).equals(RuntimeClasses.AbstractTableFieldData)) {
      builder = new TableFieldSourceBuilder(formField, hierarchy);
    }
    else {
      builder = new SourceBuilderWithProperties(formField);
    }
    return builder;
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

  /**
   * @param packageName
   *          the packageName to set
   */
  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }

  /**
   * @return the packageName
   */
  public String getPackageName() {
    return m_packageName;
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
