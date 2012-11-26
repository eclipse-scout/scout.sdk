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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

/**
 * <h3>{@link TableFieldSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.02.2011
 */
public class TableFieldSourceBuilder extends SourceBuilderWithProperties {
  private static final String COLUMN_ID_SUFFIX = "_COLUMN_ID";
  private static final String OBJECT_SIG = SignatureCache.createTypeSignature(Object.class.getName()); // Ljava.lang.Object;

  private final IType iTable = TypeUtility.getType(RuntimeClasses.ITable);
  private final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);

  private final IType m_tableField;

  public TableFieldSourceBuilder(IType tableField, ITypeHierarchy hierarchy) {
    super(tableField);
    m_tableField = tableField;
    // find table
    IType table = findTable(tableField, hierarchy);
    if (TypeUtility.exists(table)) {
      visitTable(table, hierarchy);
    }
  }

  private IType findTable(IType tableField, ITypeHierarchy hierarchy) {
    if (TypeUtility.exists(tableField)) {
      IType[] tables = TypeUtility.getInnerTypes(tableField, TypeFilters.getSubtypeFilter(iTable, hierarchy), null);
      if (tables.length > 0) {
        if (tables.length > 1) {
          ScoutSdk.logWarning("table field '" + tableField.getFullyQualifiedName() + "' contatins more than one table! Taking first for formdata creation.");
        }
        return tables[0];
      }
      else {
        return findTable(hierarchy.getSuperclass(tableField), hierarchy);
      }
    }
    return null;
  }

  protected void visitTable(IType table, ITypeHierarchy hierarchy) {

    final IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(iColumn, hierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
    final String[] colunmSignatures = new String[columns.length];
    final Map<Integer, String> columnIdMap = new HashMap<Integer, String>();

    if (columns.length > 0) {
      for (int i = 0; i < columns.length; i++) {
        IType column = columns[i];

        String constantColName = FormDataUtility.getConstantName(FormDataUtility.getFieldNameWithoutSuffix(column.getElementName())) + COLUMN_ID_SUFFIX;
        ConstantIntSourceBuilder constantBuilder = new ConstantIntSourceBuilder();
        constantBuilder.setElementName(constantColName);
        constantBuilder.setConstantValue(i);

        addBuilder(constantBuilder, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 1, i, 1, constantBuilder));

        columnIdMap.put(Integer.valueOf(i), constantColName);
      }
    }
    for (int i = 0; i < columns.length; i++) {
      try {
        IType column = columns[i];

        String upperColName = FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(column.getElementName()), true);
        String lowerColName = FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(column.getElementName()), false);
        String methodParameterName = FormDataUtility.getValidMethodParameterName(lowerColName);
        final String colSignature = getColumnSignature(column, hierarchy);
        colunmSignatures[i] = colSignature;
        // setter
        MethodSourceBuilder columnSetter = new MethodSourceBuilder(NL);
        columnSetter.setElementName("set" + upperColName);
        columnSetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
        columnSetter.addParameter(new MethodParameter(colSignature, methodParameterName));
        columnSetter.setSimpleBody("setValueInternal(row, " + getColumnConstantName(i, columnIdMap) + ", " + methodParameterName + ");");
        addBuilder(columnSetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 2, i, 1, columnSetter));
        // getter
        final String finalColumnName = getColumnConstantName(i, columnIdMap);
        MethodSourceBuilder columnGetter = new MethodSourceBuilder(NL) {
          private String simpleRef = null;

          @Override
          public String createSource(IImportValidator validator) throws JavaModelException {
            boolean addSuppressUncheckedWarning = false;
            if (!OBJECT_SIG.equals(colSignature)) {
              simpleRef = SignatureUtility.getTypeReference(colSignature, validator);
              if (SignatureUtility.isGenericSignature(colSignature)) {
                addSuppressUncheckedWarning = true;
              }
            }
            if (addSuppressUncheckedWarning) {
              addAnnotation(getSuppressUncheckedWarningBuilder());
            }
            return super.createSource(validator);
          }

          @Override
          protected String createMethodBody(IImportValidator validator) throws JavaModelException {
            StringBuilder getterBody = new StringBuilder();
            getterBody.append("return ");
            if (simpleRef != null) {
              getterBody.append("(" + simpleRef + ") ");
            }
            getterBody.append("getValueInternal(row, " + finalColumnName + ");");
            return getterBody.toString();
          }
        };
        columnGetter.setElementName("get" + upperColName);
        columnGetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
        columnGetter.setReturnSignature(colSignature);
        addBuilder(columnGetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 2, i, 2, columnSetter));
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not add column '" + columns[i].getFullyQualifiedName() + "' to form data.", e);
      }

    }
    if (columns.length > 0) {
      // gobal getter
      MethodSourceBuilder globalGetter = new MethodSourceBuilder(NL) {
        @Override
        protected String createMethodBody(IImportValidator validator) {
          StringBuilder builder = new StringBuilder();
          builder.append("  switch(column){\n");
          for (int i = 0; i < columns.length; i++) {
            builder.append("    case " + getColumnConstantName(i, columnIdMap) + ":\n return get");
            builder.append(FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(columns[i].getElementName()), true));
            builder.append("(row);\n");
          }
          builder.append("    default: return null;\n");
          builder.append("  }");
          return builder.toString();
        }
      };
      globalGetter.setElementName("getValueAt");
      globalGetter.addAnnotation(new AnnotationSourceBuilder(SignatureCache.createTypeSignature(Override.class.getName())));
      globalGetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
      globalGetter.addParameter(new MethodParameter(Signature.SIG_INT, "column"));
      globalGetter.setReturnSignature(SignatureCache.createTypeSignature(Object.class.getName()));
      addBuilder(globalGetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 3, globalGetter.getElementName(), globalGetter));

      // global setter
      MethodSourceBuilder globalSetter = new MethodSourceBuilder(NL) {
        private String[] simpleRefs = new String[columns.length];

        @Override
        public String createSource(IImportValidator validator) throws JavaModelException {
          boolean addSuppressUncheckedWarning = false;
          // pre-calculate simple refs
          for (int i = 0; i < simpleRefs.length; i++) {
            if (!OBJECT_SIG.equals(colunmSignatures[i])) {
              simpleRefs[i] = SignatureUtility.getTypeReference(colunmSignatures[i], validator);
              if (!addSuppressUncheckedWarning && SignatureUtility.isGenericSignature(colunmSignatures[i])) {
                addSuppressUncheckedWarning = true;
              }
            }
          }
          if (addSuppressUncheckedWarning) {
            addAnnotation(getSuppressUncheckedWarningBuilder());
          }
          return super.createSource(validator);
        }

        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder builder = new StringBuilder();
          builder.append("  switch(column){\n");
          for (int i = 0; i < columns.length; i++) {
            builder.append("    case " + getColumnConstantName(i, columnIdMap) + ": set");
            builder.append(FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(columns[i].getElementName()), true));
            builder.append("(row,");
            if (simpleRefs[i] != null) {
              builder.append("(" + simpleRefs[i] + ") ");
            }
            builder.append("value); break;\n");
          }
          builder.append("  }");
          return builder.toString();
        }
      };
      globalSetter.setElementName("setValueAt");
      globalSetter.addAnnotation(new AnnotationSourceBuilder(SignatureCache.createTypeSignature(Override.class.getName())));
      globalSetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
      globalSetter.addParameter(new MethodParameter(Signature.SIG_INT, "column"));
      globalSetter.addParameter(new MethodParameter(SignatureCache.createTypeSignature(Object.class.getName()), "value"));
      addBuilder(globalSetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 3, globalSetter.getElementName(), globalSetter));

      // column count
      MethodSourceBuilder columnCount = new MethodSourceBuilder(NL);
      columnCount.setElementName("getColumnCount");
      columnCount.addAnnotation(new AnnotationSourceBuilder(SignatureCache.createTypeSignature(Override.class.getName())));
      columnCount.setReturnSignature(Signature.SIG_INT);
      columnCount.setSimpleBody("return " + columns.length + ";");
      addBuilder(columnCount, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 3, columnCount.getElementName(), columnCount));
    }
  }

  private static AnnotationSourceBuilder getSuppressUncheckedWarningBuilder() {
    AnnotationSourceBuilder suppressUnchecked = new AnnotationSourceBuilder(SignatureCache.createTypeSignature(SuppressWarnings.class.getName()));
    suppressUnchecked.addParameter("\"unchecked\"");
    return suppressUnchecked;
  }

  private String getColumnConstantName(int i, Map<Integer, String> map) {
    return StringUtility.nvl(map.get(Integer.valueOf(i)), String.valueOf(i));
  }

  private String getColumnSignature(IType type, ITypeHierarchy columnHierarchy) throws JavaModelException {
    if (type == null || type.getFullyQualifiedName().equals(Object.class.getName())) {
      return null;
    }
    IType superType = columnHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        String superTypeSig = type.getSuperclassTypeSignature();
        return SignatureUtility.getResolvedSignature(Signature.getTypeArguments(superTypeSig)[0], type);
      }
      else {
        return getColumnSignature(superType, columnHierarchy);
      }
    }
    else {
      return null;
    }
  }

  /**
   * @return the tableField
   */
  public IType getTableField() {
    return m_tableField;
  }
}
