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
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link SearchFormFromTablePageHelper}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.9.0 21.05.2013
 */
public final class SearchFormFromTablePageHelper {
  private static final String NLS_KEY_SEARCH_CRITERIA = "searchCriteria";

  private SearchFormFromTablePageHelper() {
  }

  public static void fillSearchForm(ITypeSourceBuilder searchFormBuilder, String searchFormFqn, final IType formData, IType tablePage, IJavaProject searchFormProject, IProgressMonitor monitor) throws CoreException {
    INlsProject nlsProvider = ScoutTypeUtility.findNlsProject(tablePage);
    if (formData != null) {
      // execResetSearchFilter method
      IMethodSourceBuilder execResetSearchFilterBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(searchFormBuilder, "execResetSearchFilter");
      execResetSearchFilterBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("super.execResetSearchFilter(searchFilter);").append(lineDelimiter);
          String simpleFormDataName = validator.getTypeName(SignatureCache.createTypeSignature(formData.getFullyQualifiedName()));
          source.append(simpleFormDataName + " formData = new " + simpleFormDataName + "();").append(lineDelimiter);
          source.append("exportFormData(formData);").append(lineDelimiter);
          source.append("searchFilter.setFormData(formData);");
        }
      });
      searchFormBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execResetSearchFilterBuilder), execResetSearchFilterBuilder);
    }
    // main box
    ITypeSourceBuilder mainBoxBuilder = addFormField(SdkProperties.TYPE_NAME_MAIN_BOX, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IGroupBox, searchFormProject), 10, searchFormFqn, searchFormBuilder, searchFormBuilder);
    String mainBoxFqn = searchFormFqn + "." + mainBoxBuilder.getElementName();
    // tabbox
    ITypeSourceBuilder tabBoxBuilder = addFormField("TabBox", RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ITabBox, searchFormProject), 10, mainBoxFqn, mainBoxBuilder, searchFormBuilder);
    String tabBoxFqn = mainBoxFqn + "." + tabBoxBuilder.getElementName();
    // field box (group box)
    ITypeSourceBuilder fieldBoxBuilder = addFormField("Field" + SdkProperties.SUFFIX_GROUP_BOX, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IGroupBox, searchFormProject), 10, tabBoxFqn, tabBoxBuilder, searchFormBuilder);
    String fieldBoxFqn = tabBoxFqn + "." + fieldBoxBuilder.getElementName();
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
    // getConfiguredLabel method
    IMethodSourceBuilder getConfiguredLabelBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(fieldBoxBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
    if (searchCriteriaEntry == null) {
      getConfiguredLabelBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return \"Search Criteria\";"));
    }
    else {
      getConfiguredLabelBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(searchCriteriaEntry));
    }
    fieldBoxBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelBuilder), getConfiguredLabelBuilder);
    // fields
    double fieldOrder = 10;
    ITypeHierarchy tablePageHierarchy = TypeUtility.getLocalTypeHierarchy(tablePage);
    IType[] tables = TypeUtility.getInnerTypes(tablePage, TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ITable), tablePageHierarchy));
    if (tables.length > 0) {
      IType table = tables[0];
      int i = 1;
      while (!TypeUtility.exists(table) && (i < tables.length)) {
        table = tables[i];
      }
      if (TypeUtility.exists(table)) {
        IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IColumn), tablePageHierarchy));
        for (IType column : columns) {
          ConfigurationMethod configurationMethod = ScoutTypeUtility.getConfigurationMethod(column, "getConfiguredDisplayable");
          String retVal = ScoutUtility.getMethodReturnValue(configurationMethod.peekMethod());
          if ("true".equals(retVal)) {
            addSearchField(column, tablePageHierarchy, fieldOrder, fieldBoxFqn, fieldBoxBuilder, searchFormBuilder, searchFormProject, nlsProvider);
            fieldOrder += 10;
          }
        }
      }
    }
    // end field box
    // button reset
    addFormField("Reset" + SdkProperties.SUFFIX_BUTTON, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IButton, searchFormProject), fieldOrder, mainBoxFqn, mainBoxBuilder, searchFormBuilder);
    // button search
    addFormField("Search" + SdkProperties.SUFFIX_BUTTON, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IButton, searchFormProject), fieldOrder, mainBoxFqn, mainBoxBuilder, searchFormBuilder);
  }

  private static ITypeSourceBuilder addFormField(String fieldName, String superTypeSignature, double orderNr, String fieldOwnerFqn, ITypeSourceBuilder fieldOwnerBuilder, ITypeSourceBuilder fieldGetterOwnerBuilder) throws CoreException {
    return addFormField(fieldName, null, superTypeSignature, orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
  }

  private static ITypeSourceBuilder addFormField(String fieldName, INlsEntry nlsEntry, String superTypeSignature, double orderNr, String fieldOwnerFqn, ITypeSourceBuilder fieldOwnerBuilder, ITypeSourceBuilder fieldGetterOwnerBuilder) throws CoreException {
    ITypeSourceBuilder fieldSourceBuilder = new TypeSourceBuilder(fieldName);
    fieldSourceBuilder.setFlags(Flags.AccPublic);
    fieldSourceBuilder.setSuperTypeSignature(superTypeSignature);
    fieldSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(orderNr));
    // nls entry
    if (nlsEntry != null) {
      IMethodSourceBuilder getConfiguredLabelBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(fieldSourceBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
      getConfiguredLabelBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(nlsEntry));
      fieldSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelBuilder), getConfiguredLabelBuilder);
    }
    fieldOwnerBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(fieldSourceBuilder, orderNr), fieldSourceBuilder);

    // field getter
    IMethodSourceBuilder fieldGetterBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(SignatureCache.createTypeSignature(fieldOwnerFqn + "." + fieldName));
    fieldGetterOwnerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(fieldGetterBuilder), fieldGetterBuilder);
    return fieldSourceBuilder;
  }

  private static ITypeSourceBuilder addSearchField(IType column, ITypeHierarchy tablePageHierarchy, double orderNr, String fieldOwnerFqn, ITypeSourceBuilder fieldOwnerBuilder, ITypeSourceBuilder fieldGetterOwnerBuilder, IJavaProject searchFormProject, INlsProject nlsProject) throws IllegalArgumentException, CoreException {

    // nls entry
    INlsEntry nlsEntry = null;
    IMethod nlsMethod = TypeUtility.getMethod(column, "getConfiguredHeaderText");
    if (TypeUtility.exists(nlsMethod)) {
      nlsEntry = ScoutUtility.getReturnNlsEntry(nlsMethod);
    }
    String fieldNamePlain = column.getElementName().replace(SdkProperties.SUFFIX_COLUMN, "");
    if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.IBigDecimalColumn), column)) {
      return addFormField(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IBigDecimalField, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.IBooleanColumn), column)) {
      return addFormField(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IBooleanField, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.IDateColumn), column)) {
      ITypeSourceBuilder sequenceBoxBuilder = addFormField(fieldNamePlain + SdkProperties.SUFFIX_BOX, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISequenceBox, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
      fillSequenceBox(sequenceBoxBuilder, fieldOwnerFqn + "." + sequenceBoxBuilder.getElementName(), RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IDateField, searchFormProject), fieldGetterOwnerBuilder, nlsProject);
      return sequenceBoxBuilder;
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.IDoubleColumn), column)) {
      ITypeSourceBuilder sequenceBoxBuilder = addFormField(fieldNamePlain + SdkProperties.SUFFIX_BOX, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISequenceBox, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
      fillSequenceBox(sequenceBoxBuilder, fieldOwnerFqn + "." + sequenceBoxBuilder.getElementName(), RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IDoubleField, searchFormProject), fieldGetterOwnerBuilder, nlsProject);
      return sequenceBoxBuilder;
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.IIntegerColumn), column)) {
      ITypeSourceBuilder sequenceBoxBuilder = addFormField(fieldNamePlain + SdkProperties.SUFFIX_BOX, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISequenceBox, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
      fillSequenceBox(sequenceBoxBuilder, fieldOwnerFqn + "." + sequenceBoxBuilder.getElementName(), RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IIntegerField, searchFormProject), fieldGetterOwnerBuilder, nlsProject);
      return sequenceBoxBuilder;
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.ILongColumn), column)) {
      ITypeSourceBuilder sequenceBoxBuilder = addFormField(fieldNamePlain + SdkProperties.SUFFIX_BOX, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISequenceBox, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
      fillSequenceBox(sequenceBoxBuilder, fieldOwnerFqn + "." + sequenceBoxBuilder.getElementName(), RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ILongField, searchFormProject), fieldGetterOwnerBuilder, nlsProject);
      return sequenceBoxBuilder;
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.ISmartColumn), column)) {
      IMethod codeTypeMethod = TypeUtility.getMethod(column, "getConfiguredCodeType");
      if (TypeUtility.exists(codeTypeMethod)) {
        final IType codeType = PropertyMethodSourceUtility.parseReturnParameterClass(PropertyMethodSourceUtility.getMethodReturnValue(codeTypeMethod), codeTypeMethod);
        // listbox
        ITypeSourceBuilder listBoxBuilder = addFormField(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD, nlsEntry, SignatureCache.createTypeSignature(RuntimeClasses.getSuperTypeName(RuntimeClasses.IListBox, searchFormProject) + "<" + Long.class.getName() + ">"), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
        if (TypeUtility.exists(codeType)) {
          // getConfiguredCodeType method
          IMethodSourceBuilder getConfiguredCodeTypeBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(listBoxBuilder, "getConfiguredCodeType");
          getConfiguredCodeTypeBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createReturnClassReferenceBody(SignatureCache.createTypeSignature(codeType.getFullyQualifiedName())));
          listBoxBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredCodeTypeBuilder), getConfiguredCodeTypeBuilder);
        }
      }
      else {
        IMethod lookupCallMethod = TypeUtility.getMethod(column, "getConfiguredLookupCall");
        if (TypeUtility.exists(lookupCallMethod)) {
          final IType lookupCall = PropertyMethodSourceUtility.parseReturnParameterClass(PropertyMethodSourceUtility.getMethodReturnValue(lookupCallMethod), lookupCallMethod);
          // smartfield
          ITypeSourceBuilder smartFieldBuilder = addFormField(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD, nlsEntry, SignatureCache.createTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISmartField, searchFormProject) + "<" + Long.class.getName() + ">"), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
          if (TypeUtility.exists(lookupCall)) {
            // getConfiguredLookupCall method
            IMethodSourceBuilder getConfiguredCodeTypeBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(smartFieldBuilder, "getConfiguredLookupCall");
            getConfiguredCodeTypeBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createReturnClassReferenceBody(SignatureCache.createTypeSignature(lookupCall.getFullyQualifiedName())));
            smartFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredCodeTypeBuilder), getConfiguredCodeTypeBuilder);
          }

        }
      }
    }
    else if (tablePageHierarchy.isSubtype(TypeUtility.getType(RuntimeClasses.IStringColumn), column)) {
      return addFormField(fieldNamePlain + SdkProperties.SUFFIX_FORM_FIELD, nlsEntry, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IStringField, searchFormProject), orderNr, fieldOwnerFqn, fieldOwnerBuilder, fieldGetterOwnerBuilder);
    }
    return null;
  }

  private static void fillSequenceBox(ITypeSourceBuilder sequenceBoxBuilder, String sequenceBoxFqn, String fromToSuperTypeSignature, ITypeSourceBuilder fieldGetterOwnerBuilder, INlsProject nlsProject) throws CoreException {

    String parentName = sequenceBoxBuilder.getElementName();
    int lastBoxIndex = parentName.lastIndexOf(SdkProperties.SUFFIX_BOX);
    if (lastBoxIndex > 0) {
      parentName = parentName.substring(0, lastBoxIndex);
    }
    // from
    String fromFieldName = parentName + SdkProperties.SUFFIX_FROM;
    ITypeSourceBuilder fromFieldBuilder = addFormField(fromFieldName, fromToSuperTypeSignature, 10, sequenceBoxFqn, sequenceBoxBuilder, fieldGetterOwnerBuilder);

    // to
    String toFieldName = parentName + SdkProperties.SUFFIX_TO;
    ITypeSourceBuilder toFieldBuilder = addFormField(toFieldName, fromToSuperTypeSignature, 20, sequenceBoxFqn, sequenceBoxBuilder, fieldGetterOwnerBuilder);

    // nls text methods
    if (nlsProject != null) {
      INlsEntry fromEntry = nlsProject.getEntry("from");
      if (fromEntry != null) {
        IMethodSourceBuilder getConfiguredLabelFromBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(fromFieldBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
        getConfiguredLabelFromBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(fromEntry));
        fromFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelFromBuilder), getConfiguredLabelFromBuilder);
      }
      INlsEntry toEntry = nlsProject.getEntry("to");
      if (toEntry != null) {
        IMethodSourceBuilder getConfiguredLabelToBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(fromFieldBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
        getConfiguredLabelToBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(toEntry));
        toFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelToBuilder), getConfiguredLabelToBuilder);
      }
    }

  }

}
