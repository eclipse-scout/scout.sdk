/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace.dto.formdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractTableSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.DtoUtility;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link TableFieldFormDataSourceBuilder}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public class TableFieldFormDataSourceBuilder extends AbstractTableSourceBuilder {
  private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
  private static final String COLUMN_ID_SUFFIX = "_COLUMN_ID";
  private static final String OBJECT_SIG = SignatureCache.createTypeSignature(Object.class.getName()); // Ljava.lang.Object;

  private FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   */
  public TableFieldFormDataSourceBuilder(IType modelType, String elementName, FormDataAnnotation formDataAnnotation, IProgressMonitor monitor) {
    super(modelType, elementName, false, monitor);
    m_formDataAnnotation = formDataAnnotation;
    setup(monitor);
  }

  @Override
  protected void createContent(IProgressMonitor monitor) {
    super.createContent(monitor);
    collectProperties(monitor);
    if (monitor.isCanceled()) {
      return;
    }

    try {
      IType table = DtoUtility.findTable(getModelType(), getLocalTypeHierarchy());
      if (TypeUtility.exists(table)) {
        visitTable(table, monitor);
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not build form data for '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
  }

  protected void visitTable(IType table, IProgressMonitor monitor) throws CoreException {
    final Set<IType> columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IColumn), getLocalTypeHierarchy()), ScoutTypeComparators.getOrderAnnotationComparator());
    final String[] colunmSignatures = new String[columns.size()];
    final Map<Integer, String> columnIdMap = new HashMap<Integer, String>();

    if (columns.size() > 0) {
      int i = 0;
      for (IType column : columns) {
        String constantColName = getConstantName(ScoutUtility.removeFieldSuffix(column.getElementName())) + COLUMN_ID_SUFFIX;
        IFieldSourceBuilder fieldBuilder = new FieldSourceBuilder(constantColName);
        fieldBuilder.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
        fieldBuilder.setSignature(Signature.SIG_INT);
        fieldBuilder.setValue(Integer.toString(i));
        addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldConstantTableColumnIdKey(fieldBuilder, i), fieldBuilder);
        columnIdMap.put(Integer.valueOf(i), constantColName);

        if (monitor.isCanceled()) {
          return;
        }
        i++;
      }
    }

    int i = 0;
    for (IType column : columns) {
      try {
        String upperColName = NamingUtility.ensureStartWithUpperCase(ScoutUtility.removeFieldSuffix(column.getElementName()));
        String lowerColName = NamingUtility.ensureStartWithLowerCase(ScoutUtility.removeFieldSuffix(column.getElementName()));
        String methodParameterName = NamingUtility.ensureValidParameterName(lowerColName);
        final String colSignature = getColumnSignature(column, getLocalTypeHierarchy());
        colunmSignatures[i] = colSignature;
        // setter
        IMethodSourceBuilder columnSetterBuilder = new MethodSourceBuilder("set" + upperColName);
        columnSetterBuilder.setFlags(Flags.AccPublic);
        columnSetterBuilder.setReturnTypeSignature(Signature.SIG_VOID);
        columnSetterBuilder.addParameter(new MethodParameter("row", Signature.SIG_INT));
        columnSetterBuilder.addParameter(new MethodParameter(methodParameterName, colSignature));
        columnSetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("setValueInternal(row, " + getColumnConstantName(i, columnIdMap) + ", " + methodParameterName + ");"));
        addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormDataColumnAccessKey(columnSetterBuilder), columnSetterBuilder);

        // getter
        final String finalColumnName = getColumnConstantName(i, columnIdMap);
        IMethodSourceBuilder columnGetterBuilder = new MethodSourceBuilder("get" + upperColName);
        columnGetterBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            source.append("return ");
            if (!OBJECT_SIG.equals(colSignature)) {
              // cast
              source.append("(").append(SignatureUtility.getTypeReference(colSignature, validator)).append(") ");
            }
            source.append("getValueInternal(row, " + finalColumnName + ");");
          }
        });
        columnGetterBuilder.setFlags(Flags.AccPublic);
        columnGetterBuilder.setReturnTypeSignature(colSignature);
        columnGetterBuilder.addParameter(new MethodParameter("row", Signature.SIG_INT));
        addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormDataColumnAccessKey(columnGetterBuilder), columnGetterBuilder);
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not add column '" + column.getFullyQualifiedName() + "' to form data.", e);
      }

      if (monitor.isCanceled()) {
        return;
      }
      i++;
    }

    // getColumnCount method
    IMethodSourceBuilder getColumnCountBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getColumnCount");
    getColumnCountBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return " + columns.size() + ";"));
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getColumnCountBuilder), getColumnCountBuilder);

    if (columns.size() > 0) {
      // setValueAt method
      IMethodSourceBuilder setValueAtBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "setValueAt");
      setValueAtBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("  switch(column){").append(lineDelimiter);
          int j = 0;
          for (IType column : columns) {
            source.append("    case " + getColumnConstantName(j, columnIdMap) + ":").append(lineDelimiter);
            source.append("set").append(NamingUtility.ensureStartWithUpperCase(ScoutUtility.removeFieldSuffix(column.getElementName())));
            source.append("(row,");
            if (!OBJECT_SIG.equals(colunmSignatures[j])) {
              source.append("(").append(SignatureUtility.getTypeReference(colunmSignatures[j], validator)).append(") ");
            }
            source.append("value);").append(lineDelimiter);
            source.append("break;").append(lineDelimiter);
            j++;
          }
          source.append("  }");
        }
      });
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(setValueAtBuilder), setValueAtBuilder);

      // getValueAt method
      IMethodSourceBuilder getValueAtBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getValueAt");
      getValueAtBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("  switch(column){").append(lineDelimiter);
          int j = 0;
          for (IType column : columns) {
            source.append("    case " + getColumnConstantName(j, columnIdMap) + ":").append(lineDelimiter);
            source.append("return get").append(NamingUtility.ensureStartWithUpperCase(ScoutUtility.removeFieldSuffix(column.getElementName()))).append("(row);").append(lineDelimiter);
            j++;
          }
          source.append("    default: return null;").append(lineDelimiter);
          source.append("  }");
        }
      });
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getValueAtBuilder), getValueAtBuilder);
    }
  }

  private String getConstantName(String name) {
    String[] words = CONSTANT_NAME_PATTERN.split(name);
    return StringUtility.join("_", words).toUpperCase();
  }

  private String getColumnConstantName(int i, Map<Integer, String> map) {
    return StringUtility.nvl(map.get(Integer.valueOf(i)), String.valueOf(i));
  }

  @Override
  protected String computeSuperTypeSignature() throws JavaModelException {
    String superTypeSignature = null;
    if (ScoutTypeUtility.existsReplaceAnnotation(getModelType())) {
      IType replacedType = getLocalTypeHierarchy().getSuperclass(getModelType());
      IType replacedFormFieldDataType = DtoUtility.getFormDataType(replacedType, getLocalTypeHierarchy());
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureCache.createTypeSignature(replacedFormFieldDataType.getFullyQualifiedName());
      }
      addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createReplaceAnnotationBuilder());
    }
    if (superTypeSignature == null) {
      superTypeSignature = DtoUtility.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation(), getLocalTypeHierarchy());

    }
    return superTypeSignature;
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }
}