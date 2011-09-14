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
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
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
import org.eclipse.scout.sdk.operation.template.sequencebox.DateFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateTimeFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DoubleFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.IntegerFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.LongFromToTemplate;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkMethodUtility;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 * <h3>SearchFormFromTablePageFillOperation</h3> assumes the given search form type is empty and created.
 */
public class SearchFormFromTablePageFillOperation implements IOperation {

  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);
  final IType iTable = ScoutSdk.getType(RuntimeClasses.ITable);
  final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);
  final IType iBigDecimalColumn = ScoutSdk.getType(RuntimeClasses.IBigDecimalColumn);
  final IType iBooleanColumn = ScoutSdk.getType(RuntimeClasses.IBooleanColumn);
  final IType iDateColumn = ScoutSdk.getType(RuntimeClasses.IDateColumn);
  final IType iDoubleColumn = ScoutSdk.getType(RuntimeClasses.IDoubleColumn);
  final IType iIntegerColumn = ScoutSdk.getType(RuntimeClasses.IIntegerColumn);
  final IType iLongColumn = ScoutSdk.getType(RuntimeClasses.ILongColumn);
  final IType iSmartColumn = ScoutSdk.getType(RuntimeClasses.ISmartColumn);
  final IType iStringColumn = ScoutSdk.getType(RuntimeClasses.IStringColumn);
  final IType iTimeColumn = ScoutSdk.getType(RuntimeClasses.ITimeColumn);

  final IType abstractDateColumn = ScoutSdk.getType(RuntimeClasses.AbstractDateColumn);
  final IType abstractStringColumn = ScoutSdk.getType(RuntimeClasses.AbstractStringColumn);
  final IType abstractSmartColumn = ScoutSdk.getType(RuntimeClasses.AbstractSmartColumn);
  final IType abstractDoubleColumn = ScoutSdk.getType(RuntimeClasses.AbstractDoubleColumn);
  final IType abstractLongColumn = ScoutSdk.getType(RuntimeClasses.AbstractLongColumn);
  final IType abstractBigDecimalColumn = ScoutSdk.getType(RuntimeClasses.AbstractBigDecimalColumn);

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
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getSearchFormType().getCompilationUnit(), monitor);

    INlsProject nlsProvider = SdkTypeUtility.findNlsProject(getTablePageType());

    // // first, add needed methods for the table page
    // if (getConfSearchMethodFromTablePage() != null) {
    // opDel = new BCMethodDeleteOperation(getConfSearchMethodFromTablePage());
    // opDel.run(m_monitor, m_workingCopyManager);
    // }
    // if (getSearchGetterMethodFromTablePage() != null) {
    // opDel = new BCMethodDeleteOperation(getSearchGetterMethodFromTablePage());
    // opDel.run(m_monitor, m_workingCopyManager);
    // }
    //
    // m_parentType.createMethod("@Override\npublic Class< ? extends ISearchForm> getConfiguredSearchForm(){\n"+TAB+"return "+searchFormName+".class;\n}", null, true, m_monitor);
    // m_parentType.createMethod("public "+searchFormName+" getSearchForm(){\n"+TAB+"return ("+searchFormName+") getSearchFormInternal();\n}", null, true, m_monitor);
    // m_parentType.createImport(getImplementationPackageName()+"."+searchFormName, m_monitor);
    // m_parentType.createImport(RuntimeClasses.ISearchForm, m_monitor);

    // create constructor & textimport of searchform
    // getSearchFormType().createImport(RuntimeClasses.ProcessingException, m_monitor);
    // getSearchFormType().createImport(m_group.getSharedProject().getRootPackageName()+".Texts", m_monitor);
    // getSearchFormType().createMethod("public " + m_model.getTypeName() + "() throws ProcessingException{\n"+TAB+"super();\n}", null, true, m_monitor);
    if (getFormDataType() != null) {
      CompilationUnitImportValidator icuvalidator = new CompilationUnitImportValidator(getSearchFormType().getCompilationUnit());
      StringBuilder content = new StringBuilder();
      content.append("@Override\n");
      content.append("protected void execResetSearchFilter(");
      content.append(icuvalidator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.SearchFilter, true)) + " searchFilter) ");
      content.append("throws " + icuvalidator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true)) + "{\n");
      content.append(ScoutIdeProperties.TAB + "super.execResetSearchFilter(searchFilter);\n");
      String simpleFormDataName = icuvalidator.getSimpleTypeRef(Signature.createTypeSignature(m_formDataType.getFullyQualifiedName(), true));
      content.append(ScoutIdeProperties.TAB + simpleFormDataName + " formData = new " + simpleFormDataName + "();\n");
      content.append(ScoutIdeProperties.TAB + "exportFormData(formData);\n");
      content.append(ScoutIdeProperties.TAB + "searchFilter.setFormData(formData);\n");
      content.append("}");
      getSearchFormType().createMethod(content.toString(), null, true, monitor);
      for (String imp : icuvalidator.getImportsToCreate()) {
        getSearchFormType().getCompilationUnit().createImport(imp, null, monitor);
      }
    }
    /* main box */
    GroupBoxNewOperation mainBoxOp = new GroupBoxNewOperation(getSearchFormType());
    mainBoxOp.setTypeName(ScoutIdeProperties.TYPE_NAME_MAIN_BOX);
    mainBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
    mainBoxOp.run(monitor, workingCopyManager);
    IType mainBox = mainBoxOp.getCreatedField();
    // tab box
    TabBoxNewOperation tabBoxOp = new TabBoxNewOperation(mainBox);
    tabBoxOp.setTypeName(ScoutIdeProperties.TYPE_NAME_TAB_BOX);
    tabBoxOp.run(monitor, workingCopyManager);
    IType tabBox = tabBoxOp.getCreatedField();
    // button reset
    ButtonFieldNewOperation resetButtonOp = new ButtonFieldNewOperation(mainBox);
    resetButtonOp.setTypeName("Reset" + ScoutIdeProperties.SUFFIX_BUTTON);
    resetButtonOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractResetButton, true));
    resetButtonOp.run(monitor, workingCopyManager);
    // button search
    ButtonFieldNewOperation searchButtonOp = new ButtonFieldNewOperation(mainBox);
    searchButtonOp.setTypeName("Search" + ScoutIdeProperties.SUFFIX_BUTTON);
    searchButtonOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractSearchButton, true));
    searchButtonOp.run(monitor, workingCopyManager);
    // // criteria box
    // GroupBoxNewOperation criteriaBoxOp=new GroupBoxNewOperation(tabBox);
    // criteriaBoxOp.setTypeName("Criteria" + ScoutIdeProperties.SUFFIX_GROUP_BOX);
    // criteriaBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
    // criteriaBoxOp.run(monitor, workingCopyManager);
    // IScoutType criteriaBox=criteriaBoxOp.getCreatedField();
    // getConfiguredLabel
    // field box
    GroupBoxNewOperation fieldBoxOp = new GroupBoxNewOperation(tabBox);
    fieldBoxOp.setTypeName("Field" + ScoutIdeProperties.SUFFIX_GROUP_BOX);
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
    StringBuilder getConfiguredLabelBuilder = new StringBuilder();
    getConfiguredLabelBuilder.append("@Override\npublic String getConfiguredLabel(){\n");
    getConfiguredLabelBuilder.append(ScoutIdeProperties.TAB + " return ");
    if (searchCriteriaEntry == null) {
      getConfiguredLabelBuilder.append("\"Search Criteria\";\n}");
    }
    else {
      getConfiguredLabelBuilder.append("Texts.get(\"" + searchCriteriaEntry.getKey() + "\");\n}");
    }
    fieldBox.createMethod(getConfiguredLabelBuilder.toString(), null, true, monitor);

    ITypeHierarchy tablePageHierarchy = ScoutSdk.getLocalTypeHierarchy(getTablePageType());
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
          ConfigurationMethod configurationMethod = SdkTypeUtility.getConfigurationMethod(column, "getConfiguredDisplayable");
          String retVal = SdkMethodUtility.getMethodReturnValue(configurationMethod.peekMethod());
          if ("true".equals(retVal)) {
            createField(fieldBox, column, tablePageHierarchy, monitor, workingCopyManager);
          }
        }
      }
    }
    // composer box
    // GroupBoxNewOperation composerBoxOp=new GroupBoxNewOperation(tabBox);
    // composerBoxOp.setTypeName("Composer" + ScoutIdeProperties.SUFFIX_GROUP_BOX);
    // composerBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
    // composerBoxOp.run(monitor, workingCopyManager);
    // IScoutType composerBox=composerBoxOp.getCreatedField();

    // ComposerFieldNewOperation composerFieldOp=new ComposerFieldNewOperation(composerBox);
    // composerFieldOp.setTypeName("ComposerField");
    // composerFieldOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractComposerField, true));
    // composerFieldOp.run(monitor, workingCopyManager);
    IType composerField = null;// composerFieldOp.getCreatedField();
    // composerField.createMethod("@Override\npublic boolean getConfiguredLabelVisible(){\n" + ScoutIdeProperties.TAB + "return false;\n}\n", null, true, monitor);
    // composerField.createMethod("@Override\npublic int getConfiguredGridW(){\n" + ScoutIdeProperties.TAB + "return FULL_WIDTH;\n}\n", null, true, monitor);
    // composerField.createMethod("@Override\npublic int getConfiguredGridH(){\n" + ScoutIdeProperties.TAB + "return 8;\n}\n", null, true, monitor);

    // sql columns
//    List<String> sqlColumns = new ArrayList<String>();
//
//    IMethod execLoadTableDataMethod = TypeUtility.getMethod(getTablePageType(), "execLoadTableData");
//    if (TypeUtility.exists(execLoadTableDataMethod)) {
//      // get outline statement
//      Matcher matcher = Pattern.compile("(SERVICES.getService)( +)?(\\()([A-Za-z0-9]+)(\\.class\\)\\.)(get[A-Za-z0-9]+TableData)").matcher(execLoadTableDataMethod.getSource());
//      if (matcher.find()) {
//        IType serviceInterface = ScoutUtility.getReferencedType(execLoadTableDataMethod.getDeclaringType(), matcher.group(4));
//        if (serviceInterface != null) {
//          // find implementation
//          IType[] foundServiceImpls = SdkTypeUtility.getServiceImplementations(serviceInterface);
//          IType serviceImpl = null;
//          if (foundServiceImpls.length > 0) {
//            if (foundServiceImpls.length > 1) {
//              ScoutSdk.logInfo("found more than one implemenation of the service '" + serviceInterface.getFullyQualifiedName() + "'.");
//            }
//            serviceImpl = foundServiceImpls[0];
//            String methodName = matcher.group(6);
//            IMethod theSelect = TypeUtility.getMethod(serviceImpl, methodName);
//            if (TypeUtility.exists(theSelect)) {
//              String sql = theSelect.getSource().substring(theSelect.getSource().indexOf("\""));
//              sql = ScoutUtility.removeComments(sql);
//              sql = ScoutUtility.removeSourceCodeIndent(sql, ScoutUtility.getSourceCodeIndent(sql, false));
//              sql = ScoutUtility.sourceCodeToSql(sql);
//              sqlColumns = ScoutUtility.extractSqlColumns(sql);
//            }
//          }
//          else {
//            ScoutSdk.logWarning("could not find a implemenation of the service '" + serviceInterface.getFullyQualifiedName() + "'.");
//          }
//        }
//      }
//    }
//
//    // go through all columns
//    IType[] columns = SdkTypeUtility.getColumns(getTablePageType().getType(ScoutIdeProperties.TYPE_NAME_TABLEFIELD_TABLE));
//    Iterator<String> sqlColumnIter = sqlColumns.iterator();
//    for (IType column : columns) {
//
//      IMethod confDisplay = TypeUtility.findMethodInHierarchy(column, MethodFilters.getNameFilter("getConfiguredDisplayable"));
//      String sqlColumn = sqlColumnIter.hasNext() ? sqlColumnIter.next() : "";
//      if (TypeUtility.exists(confDisplay) && confDisplay.getSource().contains("return true")) {
//        // create only for the "real" columns a search field, not for id's, fonts and such
//        createField(fieldBox, composerField, column, sqlColumn, monitor, workingCopyManager);
//      }
//    }

    /* search handler */
    FormHandlerNewOperation formHandlerOp = new FormHandlerNewOperation(getSearchFormType());
    formHandlerOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormHandler, true));
    formHandlerOp.setTypeName(ScoutIdeProperties.TYPE_NAME_SEARCH_HANDLER);
    formHandlerOp.setStartMethodSibling(SdkTypeUtility.createStructuredForm(getSearchFormType()).getSiblingMethodStartHandler(formHandlerOp.getStartMethodName()));
    formHandlerOp.run(monitor, workingCopyManager);
    IType searchHandler = formHandlerOp.getCreatedHandler();
    StringBuilder execLoadBuilder = new StringBuilder();
    execLoadBuilder.append("@Override\n");
    execLoadBuilder.append("public void execLoad(){\n");
    execLoadBuilder.append(ScoutIdeProperties.TAB + ScoutUtility.getCommentAutoGeneratedMethodStub() + "\n");
    execLoadBuilder.append("}\n");
    searchHandler.createMethod(execLoadBuilder.toString(), null, true, monitor);

//    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getSearchFormType(), true);
//    formatOp.validate();
//    formatOp.run(monitor, workingCopyManager);
  }

  private IType createField(IType declaringType, IType column, ITypeHierarchy tablePageHierarchy, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    IType createdField = null;
    // nls entry
    INlsEntry nlsEntry = null;
    IMethod nlsMethod = TypeUtility.getMethod(column, "getConfiguredHeaderText");
    if (TypeUtility.exists(nlsMethod)) {
      nlsEntry = SdkMethodUtility.getReturnNlsEntry(nlsMethod);
    }
    String fieldNamePlain = column.getElementName().replace(ScoutIdeProperties.SUFFIX_COLUMN, "");
    if (tablePageHierarchy.isSubtype(iBigDecimalColumn, column)) {
      BigdecimalFieldNewOperation op = new BigdecimalFieldNewOperation(declaringType, false);
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_FORM_FIELD);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iBooleanColumn, column)) {
      BooleanFieldNewOperation op = new BooleanFieldNewOperation(declaringType, false);
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_FORM_FIELD);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iDateColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new DateFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iDoubleColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new DoubleFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iIntegerColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new IntegerFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iLongColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new LongFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_BOX);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iSmartColumn, column)) {
      IMethod codeTypeMethod = TypeUtility.getMethod(column, "getConfiguredCodeType");
      if (TypeUtility.exists(codeTypeMethod)) {
        final IType codeType = PropertyMethodSourceUtilities.parseReturnParameterClass(PropertyMethodSourceUtilities.getMethodReturnValue(codeTypeMethod), codeTypeMethod);
        // listbox
        ListBoxFieldNewOperation op = new ListBoxFieldNewOperation(declaringType, false);
        op.setNlsEntry(nlsEntry);
        op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_FORM_FIELD);
        op.validate();
        op.run(monitor, manager);
        createdField = op.getCreatedField();
        if (TypeUtility.exists(codeType)) {
          MethodOverrideOperation codeTypeOp = new MethodOverrideOperation(createdField, "getConfiguredCodeType") {
            @Override
            protected String createMethodBody(IImportValidator validator) throws JavaModelException {
              String typeRef = validator.getSimpleTypeRef(Signature.createTypeSignature(codeType.getFullyQualifiedName(), true));
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
          final IType lookupCall = PropertyMethodSourceUtilities.parseReturnParameterClass(PropertyMethodSourceUtilities.getMethodReturnValue(lookupCallMethod), lookupCallMethod);
          // smartfield
          SmartFieldNewOperation op = new SmartFieldNewOperation(declaringType, false);
          op.setNlsEntry(nlsEntry);
          op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_FORM_FIELD);
          op.validate();
          op.run(monitor, manager);
          createdField = op.getCreatedField();
          if (TypeUtility.exists(lookupCall)) {
            MethodOverrideOperation lookupCallOp = new MethodOverrideOperation(createdField, "getConfiguredLookupCall") {
              @Override
              protected String createMethodBody(IImportValidator validator) throws JavaModelException {
                String typeRef = validator.getSimpleTypeRef(Signature.createTypeSignature(lookupCall.getFullyQualifiedName(), true));
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
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_FORM_FIELD);
      op.validate();
      op.run(monitor, manager);
      createdField = op.getCreatedField();
    }
    else if (tablePageHierarchy.isSubtype(iTimeColumn, column)) {
      SequenceBoxNewOperation op = new SequenceBoxNewOperation(declaringType, false);
      op.setContentTemplate(new DateTimeFromToTemplate());
      op.setNlsEntry(nlsEntry);
      op.setTypeName(fieldNamePlain + ScoutIdeProperties.SUFFIX_BOX);
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
