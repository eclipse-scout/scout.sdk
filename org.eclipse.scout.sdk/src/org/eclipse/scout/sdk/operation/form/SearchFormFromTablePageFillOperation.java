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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.BigdecimalFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.BooleanFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.ButtonFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.GroupBoxNewOperation;
import org.eclipse.scout.sdk.operation.form.field.ListBoxFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.SequenceBoxNewOperation;
import org.eclipse.scout.sdk.operation.form.field.SmartFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.StringFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.TabBoxNewOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateTimeFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DoubleFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.IntegerFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.LongFromToTemplate;
import org.eclipse.scout.sdk.util.ScoutMethodUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>SearchFormFromTablePageFillOperation</h3> assumes the given search form type is empty and created.
 */
public class SearchFormFromTablePageFillOperation implements IOperation {

  final IType iService = TypeUtility.getType(RuntimeClasses.IService);
  final IType iTable = TypeUtility.getType(RuntimeClasses.ITable);
  final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);
  final IType iBigDecimalColumn = TypeUtility.getType(RuntimeClasses.IBigDecimalColumn);
  final IType iBooleanColumn = TypeUtility.getType(RuntimeClasses.IBooleanColumn);
  final IType iDateColumn = TypeUtility.getType(RuntimeClasses.IDateColumn);
  final IType iDoubleColumn = TypeUtility.getType(RuntimeClasses.IDoubleColumn);
  final IType iIntegerColumn = TypeUtility.getType(RuntimeClasses.IIntegerColumn);
  final IType iLongColumn = TypeUtility.getType(RuntimeClasses.ILongColumn);
  final IType iSmartColumn = TypeUtility.getType(RuntimeClasses.ISmartColumn);
  final IType iStringColumn = TypeUtility.getType(RuntimeClasses.IStringColumn);
  final IType iTimeColumn = TypeUtility.getType(RuntimeClasses.ITimeColumn);

  final IType abstractDateColumn = TypeUtility.getType(RuntimeClasses.AbstractDateColumn);
  final IType abstractStringColumn = TypeUtility.getType(RuntimeClasses.AbstractStringColumn);
  final IType abstractSmartColumn = TypeUtility.getType(RuntimeClasses.AbstractSmartColumn);
  final IType abstractDoubleColumn = TypeUtility.getType(RuntimeClasses.AbstractDoubleColumn);
  final IType abstractLongColumn = TypeUtility.getType(RuntimeClasses.AbstractLongColumn);
  final IType abstractBigDecimalColumn = TypeUtility.getType(RuntimeClasses.AbstractBigDecimalColumn);

  private static final String NLS_KEY_SEARCH_CRITERIA = "searchCriteria";
  private IType m_tablePageType;
  private IType m_searchFormType;
  private IType m_formDataType;

  public SearchFormFromTablePageFillOperation() {
  }

  @Override
  public String getOperationName() {
    return "Create search form from table page";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getSearchFormType() == null) {
      throw new IllegalArgumentException("search form must not be null.");
    }
    if (getTablePageType() == null) {
      throw new IllegalArgumentException("table page must not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getSearchFormType().getCompilationUnit(), monitor);

    INlsProject nlsProvider = ScoutTypeUtility.findNlsProject(getTablePageType());
    if (getFormDataType() != null) {
      CompilationUnitImportValidator icuvalidator = new CompilationUnitImportValidator(getSearchFormType().getCompilationUnit());
      StringBuilder content = new StringBuilder();
      content.append("@Override\n");
      content.append("protected void execResetSearchFilter(");
      content.append(icuvalidator.getTypeName(Signature.createTypeSignature(RuntimeClasses.SearchFilter, true)) + " searchFilter) ");
      content.append("throws " + icuvalidator.getTypeName(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true)) + "{\n");
      content.append(SdkProperties.TAB + "super.execResetSearchFilter(searchFilter);\n");
      String simpleFormDataName = icuvalidator.getTypeName(Signature.createTypeSignature(m_formDataType.getFullyQualifiedName(), true));
      content.append(SdkProperties.TAB + simpleFormDataName + " formData = new " + simpleFormDataName + "();\n");
      content.append(SdkProperties.TAB + "exportFormData(formData);\n");
      content.append(SdkProperties.TAB + "searchFilter.setFormData(formData);\n");
      content.append("}");
      getSearchFormType().createMethod(content.toString(), null, true, monitor);
      for (String imp : icuvalidator.getImportsToCreate()) {
        getSearchFormType().getCompilationUnit().createImport(imp, null, monitor);
      }
    }
    /* main box */
    GroupBoxNewOperation mainBoxOp = new GroupBoxNewOperation(getSearchFormType());
    mainBoxOp.setTypeName(SdkProperties.TYPE_NAME_MAIN_BOX);
    mainBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
    mainBoxOp.run(monitor, workingCopyManager);
    IType mainBox = mainBoxOp.getCreatedField();
    // tab box
    TabBoxNewOperation tabBoxOp = new TabBoxNewOperation(mainBox);
    tabBoxOp.setTypeName(SdkProperties.TYPE_NAME_TAB_BOX);
    tabBoxOp.run(monitor, workingCopyManager);
    IType tabBox = tabBoxOp.getCreatedField();
    // button reset
    ButtonFieldNewOperation resetButtonOp = new ButtonFieldNewOperation(mainBox);
    resetButtonOp.setTypeName("Reset" + SdkProperties.SUFFIX_BUTTON);
    resetButtonOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractResetButton, true));
    resetButtonOp.run(monitor, workingCopyManager);
    // button search
    ButtonFieldNewOperation searchButtonOp = new ButtonFieldNewOperation(mainBox);
    searchButtonOp.setTypeName("Search" + SdkProperties.SUFFIX_BUTTON);
    searchButtonOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractSearchButton, true));
    searchButtonOp.run(monitor, workingCopyManager);
    // field box
    GroupBoxNewOperation fieldBoxOp = new GroupBoxNewOperation(tabBox);
    fieldBoxOp.setTypeName("Field" + SdkProperties.SUFFIX_GROUP_BOX);
    fieldBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
    fieldBoxOp.run(monitor, workingCopyManager);
    IType fieldBox = fieldBoxOp.getCreatedField();
    INlsEntry searchCriteriaEntry = null;
    if (nlsProvider != null) {
      searchCriteriaEntry = nlsProvider.getEntry(NLS_KEY_SEARCH_CRITERIA);
      if (searchCriteriaEntry == null) {
        NlsEntry entry = new NlsEntry(NLS_KEY_SEARCH_CRITERIA, nlsProvider);
        entry.addTranslation(Language.LANGUAGE_DEFAULT, "Search Criteria");
        nlsProvider.updateRow(entry, monitor);
        searchCriteriaEntry = entry;
      }
    }
    if (searchCriteriaEntry == null) {
      ConfigPropertyMethodUpdateOperation getConfiguredLabel = new ConfigPropertyMethodUpdateOperation(fieldBox, NlsTextMethodUpdateOperation.GET_CONFIGURED_LABEL, SdkProperties.TAB + "return \"Search Criteria\";", false);
      getConfiguredLabel.validate();
      getConfiguredLabel.run(monitor, workingCopyManager);
    }
    else {
      NlsTextMethodUpdateOperation getConfiguredLabel = new NlsTextMethodUpdateOperation(fieldBox, NlsTextMethodUpdateOperation.GET_CONFIGURED_LABEL, false);
      getConfiguredLabel.setNlsEntry(searchCriteriaEntry);
      getConfiguredLabel.validate();
      getConfiguredLabel.run(monitor, workingCopyManager);
    }

    ITypeHierarchy tablePageHierarchy = TypeUtility.getLocalTypeHierarchy(getTablePageType());
    IType[] tables = TypeUtility.getInnerTypes(getTablePageType(), TypeFilters.getSubtypeFilter(iTable, tablePageHierarchy));
    if (tables.length > 0) {
      IType table = tables[0];
      int i = 1;
      while (!TypeUtility.exists(table) && (i < tables.length)) {
        table = tables[i];
      }
      if (TypeUtility.exists(table)) {
        IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(iColumn, tablePageHierarchy));
        for (IType column : columns) {
          ConfigurationMethod configurationMethod = ScoutTypeUtility.getConfigurationMethod(column, "getConfiguredDisplayable");
          String retVal = ScoutMethodUtility.getMethodReturnValue(configurationMethod.peekMethod());
          if ("true".equals(retVal)) {
            createField(fieldBox, column, tablePageHierarchy, monitor, workingCopyManager);
          }
        }
      }
    }

    /* search handler */
    FormHandlerNewOperation formHandlerOp = new FormHandlerNewOperation(getSearchFormType());
    formHandlerOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormHandler, true));
    formHandlerOp.setTypeName(SdkProperties.TYPE_NAME_SEARCH_HANDLER);
    formHandlerOp.setStartMethodSibling(ScoutTypeUtility.createStructuredForm(getSearchFormType()).getSiblingMethodStartHandler(formHandlerOp.getStartMethodName()));
    formHandlerOp.run(monitor, workingCopyManager);
    IType searchHandler = formHandlerOp.getCreatedHandler();
    StringBuilder execLoadBuilder = new StringBuilder();
    execLoadBuilder.append("@Override\n");
    execLoadBuilder.append("public void execLoad(){\n");
    execLoadBuilder.append(SdkProperties.TAB + ScoutUtility.getCommentAutoGeneratedMethodStub() + "\n");
    execLoadBuilder.append("}\n");
    searchHandler.createMethod(execLoadBuilder.toString(), null, true, monitor);

//    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getSearchFormType(), true);
//    formatOp.validate();
//    formatOp.run(monitor, workingCopyManager);
  }

  private IType createField(IType declaringType, IType column, ITypeHierarchy tablePageHierarchy, IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    IType createdField = null;
    // nls entry
    INlsEntry nlsEntry = null;
    IMethod nlsMethod = TypeUtility.getMethod(column, "getConfiguredHeaderText");
    if (TypeUtility.exists(nlsMethod)) {
      nlsEntry = ScoutMethodUtility.getReturnNlsEntry(nlsMethod);
    }
    String fieldNamePlain = column.getElementName().replace(SdkProperties.SUFFIX_COLUMN, "");
    if (tablePageHierarchy.isSubtype(iBigDecimalColumn, column)) {
      BigdecimalFieldNewOperation op = new BigdecimalFieldNewOperation(declaringType, false);
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iBooleanColumn, column)) {
      BooleanFieldNewOperation op = new BooleanFieldNewOperation(declaringType, false);
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iDateColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new DateFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iDoubleColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new DoubleFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iIntegerColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new IntegerFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iLongColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new LongFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iSmartColumn, column)) {
      IMethod codeTypeMethod = TypeUtility.getMethod(column, "getConfiguredCodeType");
      if (TypeUtility.exists(codeTypeMethod)) {
        final IType codeType = PropertyMethodSourceUtility.parseReturnParameterClass(PropertyMethodSourceUtility.getMethodReturnValue(codeTypeMethod), codeTypeMethod);
        // listbox
        ListBoxFieldNewOperation op = new ListBoxFieldNewOperation(declaringType, false);
        op.setNlsEntry(nlsEntry);
        op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD);
        op.validate();
        op.run(monitor, manager);
        createdField = op.getCreatedField();
        if (TypeUtility.exists(codeType)) {
          MethodOverrideOperation codeTypeOp = new MethodOverrideOperation(createdField, "getConfiguredCodeType") {
            @Override
            protected String createMethodBody(IImportValidator validator) throws JavaModelException {
              String typeRef = validator.getTypeName(Signature.createTypeSignature(codeType.getFullyQualifiedName(), true));
              return "return " + typeRef + ".class;";
            }
          };
          codeTypeOp.validate();
          codeTypeOp.run(monitor, manager);
        }
      }
      else {
        IMethod lookupCallMethod = TypeUtility.getMethod(column, "getConfiguredLookupCall");
        if (TypeUtility.exists(lookupCallMethod)) {
          final IType lookupCall = PropertyMethodSourceUtility.parseReturnParameterClass(PropertyMethodSourceUtility.getMethodReturnValue(lookupCallMethod), lookupCallMethod);
          // smartfield
          SmartFieldNewOperation op = new SmartFieldNewOperation(declaringType, false);
          op.setNlsEntry(nlsEntry);
          op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD);
          op.validate();
          op.run(monitor, manager);
          createdField = op.getCreatedField();
          if (TypeUtility.exists(lookupCall)) {
            MethodOverrideOperation lookupCallOp = new MethodOverrideOperation(createdField, "getConfiguredLookupCall") {
              @Override
              protected String createMethodBody(IImportValidator validator) throws JavaModelException {
                String typeRef = validator.getTypeName(Signature.createTypeSignature(lookupCall.getFullyQualifiedName(), true));
                return "return " + typeRef + ".class;";
              }
            };
            lookupCallOp.validate();
            lookupCallOp.run(monitor, manager);
          }
        }
      }
    }
    else if (tablePageHierarchy.isSubtype(iStringColumn, column)) {
      StringFieldNewOperation op = new StringFieldNewOperation(declaringType, false);
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iTimeColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new DateTimeFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + SdkProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    return createdField;
  }

  public void setTablePageType(IType tablePageType) {
    m_tablePageType = tablePageType;
  }

  public IType getTablePageType() {
    return m_tablePageType;
  }

  public void setSearchFormType(IType searchFormType) {
    m_searchFormType = searchFormType;
  }

  public IType getSearchFormType() {
    return m_searchFormType;
  }

  /**
   * @param createdFormDataType
   */
  public void setFormDataType(IType formDataType) {
    m_formDataType = formDataType;
  }

  public IType getFormDataType() {
    return m_formDataType;
  }
}
