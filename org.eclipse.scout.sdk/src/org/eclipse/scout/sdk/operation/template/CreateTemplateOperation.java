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
package org.eclipse.scout.sdk.operation.template;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.FormDataAnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.javadoc.JavaDoc;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 *
 */
public class CreateTemplateOperation implements IOperation {
  private final IType iFormField = TypeUtility.getType(RuntimeClasses.IFormField);
  private final IType iCompositeField = TypeUtility.getType(RuntimeClasses.ICompositeField);
  private final static Pattern SUPER_CLASS_PATTERN = Pattern.compile("\\sextends\\s*(" + Regex.REGEX_JAVAFIELD + "(\\<[^{]*\\>)?)", Pattern.MULTILINE);

  private String m_templateName;
  private String m_packageName;
  private String m_formDataPackageSuffix;
  private boolean m_replaceFieldWithTemplate;
  private boolean m_createExternalFormData;
  private IType m_formField;
  private IScoutBundle m_templateBundle;

  public CreateTemplateOperation(IType formField, IScoutBundle bundle) {
    m_formField = formField;
    m_templateBundle = bundle;
  }

  @Override
  public String getOperationName() {
    return "Create template '" + getTemplateName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getTemplateBundle() == null) {
      throw new IllegalArgumentException("The bundle for the template is not set.");
    }
    if (StringUtility.isNullOrEmpty(getTemplateName())) {
      throw new IllegalArgumentException("Template name can not be null or empty.");
    }
    if (StringUtility.isNullOrEmpty(getFormDataPackageSuffix())) {
      throw new IllegalArgumentException("Formdata package can not be null or empty.");
    }
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      throw new IllegalArgumentException("Template package can not be null or empty.");
    }
    if (!TypeUtility.exists(getFormField())) {
      throw new IllegalArgumentException("Form field to create template of must exist.");
    }
    if (TypeUtility.existsType(getPackageName() + "." + getTemplateName())) {
      throw new IllegalArgumentException("Template already exists.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutTypeNewOperation op = new ScoutTypeNewOperation(getTemplateName(), getPackageName(), getTemplateBundle()) {
      @Override
      protected void createContent(StringBuilder source, IImportValidator validator) {
        try {
          int start = Integer.MAX_VALUE;
          int end = Integer.MIN_VALUE;
          for (IJavaElement e : getFormField().getChildren()) {
            if (e instanceof IMember) {
              IMember member = (IMember) e;
              ISourceRange r = member.getSourceRange();
              start = Math.min(r.getOffset(), start);
              end = Math.max(r.getOffset() + r.getLength(), end);
            }
          }
          if (start < end) {
            source.append("\n");
            source.append(getFormField().getCompilationUnit().getBuffer().getText(start, end - start));
            source.append("\n");
          }
        }
        catch (JavaModelException e) {

        }
      }
    };

    String superclassTypeSignature = SignatureUtility.getResolvedSignature(getFormField().getSuperclassTypeSignature(), getFormField());
    /*if (!StringUtility.isNullOrEmpty(superclassTypeSignature)) {
      StringBuilder superclassSignatureBuilder = new StringBuilder(Signature.getTypeErasure(superclassTypeSignature));
      String[] typeParameters = Signature.getTypeArguments(superclassTypeSignature);
      if (typeParameters.length > 0) {
        superclassSignatureBuilder.replace(superclassSignatureBuilder.length() - 1, superclassSignatureBuilder.length(), "<");
        for (int i = 0; i < typeParameters.length; i++) {
          IType parameterType = TypeUtility.getTypeBySignature(typeParameters[i]);
          if (TypeUtility.exists(parameterType) && parameterType.getParent().equals(getFormField())) {
            superclassSignatureBuilder.append(Signature.createTypeSignature(getTemplateName() + "." + parameterType.getElementName(), false));
            if (i < typeParameters.length - 1) {
              superclassSignatureBuilder.append(",");
            }
          }
        }
        superclassSignatureBuilder.append(">;");
      }*/
    op.setSuperTypeSignature(superclassTypeSignature);
    //}
    op.setTypeModifiers(Flags.AccAbstract | Flags.AccPublic);
    IScoutBundle sharedBundle = findFormDataBundle(getTemplateBundle().getScoutProject());
    if (isCreateExternalFormData() && sharedBundle != null) {
      ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(getTemplateName() + "Data", sharedBundle.getPackageName(getFormDataPackageSuffix()), sharedBundle);
      formDataOp.setTypeModifiers(Flags.AccAbstract | Flags.AccPublic);
      formDataOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractFormData));
      formDataOp.run(monitor, workingCopyManager);

      // add to exported packages
      ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{formDataOp.getCreatedType().getPackageFragment()}, true);
      manifestOp.run(monitor, workingCopyManager);

      FormDataAnnotationCreateOperation formDataAnnotationOp = new FormDataAnnotationCreateOperation(null);
      formDataAnnotationOp.setSdkCommand(SdkCommand.CREATE);
      formDataAnnotationOp.setDefaultSubtypeCommand(DefaultSubtypeSdkCommand.CREATE);
      formDataAnnotationOp.setFormDataSignature(SignatureCache.createTypeSignature(formDataOp.getCreatedType().getFullyQualifiedName()));
      op.addAnnotation(formDataAnnotationOp);
    }
    op.validate();
    op.run(monitor, workingCopyManager);
    IType templateType = op.getCreatedType();

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{templateType.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(templateType, true);
    formatOp.validate();
    formatOp.run(monitor, workingCopyManager);
    workingCopyManager.reconcile(templateType.getCompilationUnit(), monitor);

    // form data
    if (isCreateExternalFormData() && sharedBundle != null) {
      FormDataUpdateOperation formDataUpdateOp = new FormDataUpdateOperation(templateType);
      formDataUpdateOp.run(monitor, workingCopyManager);
    }

    // getter fields
    org.eclipse.scout.sdk.util.typecache.ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(templateType);
    IStructuredType structuredForm = ScoutTypeUtility.createStructuredForm(templateType);
    TreeMap<CompositeObject, IJavaElement> siblings = new TreeMap<CompositeObject, IJavaElement>();
    IJavaElement sibling = structuredForm.getSibling(CATEGORIES.METHOD_INNER_TYPE_GETTER);
    siblings.put(new CompositeObject(2, ""), sibling);
    HashMap<String /*simple type name form field*/, P_FormField /*form field and getter method*/> newFormFields = new HashMap<String, P_FormField>();
    for (IType t : templateType.getTypes()) {
      createFormFieldGetter(t, templateType, siblings, newFormFields, hierarchy, monitor, workingCopyManager);
    }

    if (isReplaceFieldWithTemplate()) {
      workingCopyManager.register(getFormField().getCompilationUnit(), monitor);
      IImportValidator validator = new CompilationUnitImportValidator(getFormField().getCompilationUnit());
      ITypeHierarchy formFieldHierarhy = TypeUtility.getLocalTypeHierarchy(getFormField());
      MultiTextEdit edit = new MultiTextEdit();
      int start = Integer.MAX_VALUE;
      int end = Integer.MIN_VALUE;
      for (IJavaElement e : getFormField().getChildren()) {
        if (e instanceof IMember) {
          IMember member = (IMember) e;
          ISourceRange sourceRange = member.getSourceRange();
          start = Math.min(sourceRange.getOffset(), start);
          end = Math.max(sourceRange.getOffset() + sourceRange.getLength(), end);
        }
      }
      if (start < end) {
        edit.addChild(new DeleteEdit(start, end - start));
      }
      // extends
      Matcher superClassMatcher = SUPER_CLASS_PATTERN.matcher(getFormField().getSource());
      if (superClassMatcher.find()) {
        edit.addChild(new ReplaceEdit(getFormField().getSourceRange().getOffset() + superClassMatcher.start(1), superClassMatcher.end(1) - superClassMatcher.start(1),
            validator.getTypeName(SignatureCache.createTypeSignature(templateType.getFullyQualifiedName()))));
      }
      IMethod templateFieldGetter = ScoutTypeUtility.getFormFieldGetterMethod(getFormField(), hierarchy);
      if (TypeUtility.exists(templateFieldGetter)) {
        for (IType formField : ScoutTypeUtility.getFormFields(getFormField(), formFieldHierarhy)) {
          updateFormFieldGetter(formField, templateFieldGetter, newFormFields, validator, edit, formFieldHierarhy);
        }
      }
      Document sourceDoc = new Document();
      IBuffer buffer = getFormField().getCompilationUnit().getBuffer();
      sourceDoc.set(buffer.getContents());
      try {
        edit.apply(sourceDoc);
        SourceFormatOperation sourceFormatOp = new SourceFormatOperation(getFormField().getJavaProject(), sourceDoc, null);
        sourceFormatOp.run(monitor, workingCopyManager);
        buffer.setContents(ScoutUtility.cleanLineSeparator(sourceDoc.get(), sourceDoc));
        for (String fqi : validator.getImportsToCreate()) {
          getFormField().getCompilationUnit().createImport(fqi, null, monitor);
        }
      }
      catch (Exception e) {
        ScoutSdk.logError("could not create template for '" + getFormField().getFullyQualifiedName() + "'.", e);
      }
    }
  }

  protected IScoutBundle findFormDataBundle(IScoutProject project) {
    if (project != null) {
      if (project.getSharedBundle() != null) {
        return project.getSharedBundle();
      }
      return findFormDataBundle(project.getParentProject());
    }
    return null;
  }

  protected void updateFormFieldGetter(IType formField, IMethod templateFieldGetter, HashMap<String, P_FormField> templateFormFields, IImportValidator validator, MultiTextEdit edit, ITypeHierarchy hierarchy) {
    String fqFormFieldName = formField.getFullyQualifiedName();
    fqFormFieldName = fqFormFieldName.replaceAll("\\$", ".");
    try {
      IMethod getterMethod = ScoutTypeUtility.getFormFieldGetterMethod(formField, hierarchy);
      if (TypeUtility.exists(getterMethod)) {
        String NL = ResourceUtility.getLineSeparator(getterMethod.getCompilationUnit());
        P_FormField templateFormField = templateFormFields.get(formField.getElementName());
        // find import
        IImportDeclaration formFieldImport = formField.getCompilationUnit().getImport(fqFormFieldName);
        if (TypeUtility.exists(formFieldImport)) {
          edit.addChild(new ReplaceEdit(formFieldImport.getSourceRange().getOffset(), formFieldImport.getSourceRange().getLength(),
              "import " + templateFormField.getFormField().getFullyQualifiedName().replaceAll("\\$", ".") + ";"));
        }
        String methodSource = getterMethod.getSource();
        // deprecation comment
        JavaDoc doc = new JavaDoc(getterMethod);
        doc.appendLine("@deprecated Use {@link #" + templateFieldGetter.getElementName() + "()#" + templateFormField.getGetterMethod().getElementName() + "()}");
        TextEdit commentEdit = doc.getEdit();
        if (commentEdit != null) {
          edit.addChild(commentEdit);
        }
        Matcher deprecatedMatcher = Pattern.compile("(public|protected|private)\\s*(" + formField.getElementName() + "|" + fqFormFieldName + ")", Pattern.MULTILINE).matcher(methodSource);
        if (deprecatedMatcher.find()) {
          edit.addChild(new InsertEdit(getterMethod.getSourceRange().getOffset() + deprecatedMatcher.start(), "@" + validator.getTypeName(SignatureCache.createTypeSignature(Deprecated.class.getName())) + NL));
        }
        Matcher returnMatcher = Pattern.compile("(\\s*return\\s*)getFieldByClass\\([^;]*\\;", Pattern.MULTILINE).matcher(methodSource);
        if (returnMatcher.find()) {
          edit.addChild(new ReplaceEdit(getterMethod.getSourceRange().getOffset() + returnMatcher.start(), returnMatcher.end() - returnMatcher.start(),
              returnMatcher.group(1) + templateFieldGetter.getElementName() + "()." + templateFormField.getGetterMethod().getElementName() + "();"));
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not update field getter for '" + fqFormFieldName + "'.", e);
    }
    for (IType childField : ScoutTypeUtility.getFormFields(formField, hierarchy)) {
      updateFormFieldGetter(childField, templateFieldGetter, templateFormFields, validator, edit, hierarchy);
    }

  }

  protected void createFormFieldGetter(IType type, IType formType, TreeMap<CompositeObject, IJavaElement> siblings, HashMap<String, P_FormField> getterMethods, org.eclipse.scout.sdk.util.typecache.ITypeHierarchy hierarchy, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    if (TypeUtility.exists(type)) {
      if (hierarchy.isSubtype(iFormField, type)) {
        InnerTypeGetterCreateOperation op = new InnerTypeGetterCreateOperation(type, formType, true);
        CompositeObject key = new CompositeObject(1, op.getMethodName());
        for (Entry<CompositeObject, IJavaElement> entry : siblings.entrySet()) {
          if (entry.getKey().compareTo(key) > 0) {
            op.setSibling(entry.getValue());
            break;
          }
        }
        op.validate();
        op.run(monitor, manager);
        siblings.put(key, op.getCreatedMethod());
        getterMethods.put(type.getElementName(), new P_FormField(type, op.getCreatedMethod()));
      }
      // visit children
      if (hierarchy.isSubtype(iCompositeField, type)) {
        IType[] innerFields = TypeUtility.getInnerTypes(type, TypeFilters.getSubtypeFilter(iFormField, hierarchy));
        for (IType t : innerFields) {
          createFormFieldGetter(t, formType, siblings, getterMethods, hierarchy, monitor, manager);
        }
      }
    }
  }

  public IType getFormField() {
    return m_formField;
  }

  public void setTemplateBundle(IScoutBundle templateBundle) {
    m_templateBundle = templateBundle;
  }

  public IScoutBundle getTemplateBundle() {
    return m_templateBundle;
  }

  public String getTemplateName() {
    return m_templateName;
  }

  public void setTemplateName(String templateName) {
    m_templateName = templateName;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }

  public boolean isReplaceFieldWithTemplate() {
    return m_replaceFieldWithTemplate;
  }

  public void setReplaceFieldWithTemplate(boolean replaceFieldWithTemplate) {
    m_replaceFieldWithTemplate = replaceFieldWithTemplate;
  }

  /**
   * @return the createExternalFormData
   */
  public boolean isCreateExternalFormData() {
    return m_createExternalFormData;
  }

  /**
   * @param createExternalFormData
   *          the createExternalFormData to set
   */
  public void setCreateExternalFormData(boolean createExternalFormData) {
    m_createExternalFormData = createExternalFormData;
  }

  public String getFormDataPackageSuffix() {
    return m_formDataPackageSuffix;
  }

  public void setFormDataPackageSuffix(String formDataPackageSuffix) {
    m_formDataPackageSuffix = formDataPackageSuffix;
  }

  private class P_FormField {
    private IType m_formField;
    private IMethod m_getterMethod;

    public P_FormField(IType formfield, IMethod getterMethod) {
      m_formField = formfield;
      m_getterMethod = getterMethod;
    }

    public IMethod getGetterMethod() {
      return m_getterMethod;
    }

    public IType getFormField() {
      return m_formField;
    }
  }

}
