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
package org.eclipse.scout.sdk.internal.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractTableBeanSourceBuilder}</h3>
 * 
 * @author aho
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractTableBeanSourceBuilder extends AbstractTableSourceBuilder {

  /**
   * @param elementName
   */
  public AbstractTableBeanSourceBuilder(IType modelType, String elementName, boolean setup) {
    super(modelType, elementName, setup);
  }

  @Override
  protected void createContent() {
    super.createContent();
    try {
      IType table = FormDataUtility.findTable(getModelType(), getLocalTypeHierarchy());
      if (TypeUtility.exists(table)) {
        if (getModelType().equals(table.getDeclaringType())) {
          visitTableBean(table, getLocalTypeHierarchy());
        }
      }
      else {
        addAbstractMethodImplementations();
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not build form data for '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
  }

  protected void visitTableBean(IType table, ITypeHierarchy fieldHierarchy) throws CoreException {
    final IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IColumn), fieldHierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
//    final String[] colunmSignatures = new String[columns.length];
    String tableRowDataName = getElementName();
    tableRowDataName = tableRowDataName.replaceAll("(FieldData|Data)$", "");
    tableRowDataName = tableRowDataName + "RowData";
    ITypeSourceBuilder tableRowDataBuilder = new TypeSourceBuilder(tableRowDataName);
    // flags
    int flags = Flags.AccPublic | Flags.AccStatic;
    try {
      if (Flags.isAbstract(table.getFlags())) {
        flags |= Flags.AccAbstract;
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not determ abstract flag of '" + table.getFullyQualifiedName() + "'.", e);
    }
    tableRowDataBuilder.setFlags(flags);
    tableRowDataBuilder.setSuperTypeSignature(getTableRowDataSuperClassSignature(table, fieldHierarchy));//SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData));
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    tableRowDataBuilder.addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(tableRowDataName);
    tableRowDataBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    // visit columns

    for (int i = 0; i < columns.length; i++) {
      IType column = columns[i];
      if (ScoutTypeUtility.existsReplaceAnnotation(column)) {
        // replaced columns already have a column data
        continue;
      }
      String columnBeanName = ScoutUtility.ensureStartWithLowerCase(ScoutUtility.removeFieldSuffix(column.getElementName()));
      // constant
      String constantColName = columnBeanName;
      if (ScoutUtility.isReservedJavaKeyword(constantColName)) {
        constantColName += "_";
      }
      IFieldSourceBuilder constantFieldBuilder = new FieldSourceBuilder(constantColName);
      constantFieldBuilder.setFlags(Flags.AccPublic | Flags.AccFinal | Flags.AccStatic);
      constantFieldBuilder.setSignature(SignatureCache.createTypeSignature(String.class.getName()));
      constantFieldBuilder.setValue("\"" + columnBeanName + "\"");
      tableRowDataBuilder.addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_CONSTANT + 1, i, columnBeanName), constantFieldBuilder);

      // member
      IFieldSourceBuilder memberFieldBuilder = new FieldSourceBuilder("m_" + columnBeanName);
      memberFieldBuilder.setFlags(Flags.AccPrivate);
      memberFieldBuilder.setSignature(getColumnSignature(column, fieldHierarchy));
      tableRowDataBuilder.addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_MEMBER + 1, i, columnBeanName), memberFieldBuilder);

      // getter
      IMethodSourceBuilder getterBuilder = MethodSourceBuilderFactory.createGetter(memberFieldBuilder);
      tableRowDataBuilder.addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 1, getterBuilder), getterBuilder);

      // setter
      IMethodSourceBuilder setterBuilder = MethodSourceBuilderFactory.createSetter(memberFieldBuilder);
      tableRowDataBuilder.addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 2, setterBuilder), setterBuilder);

    }
    addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableRowDataBuilder), tableRowDataBuilder);

    // row access methods
    final String tableRowSignature = Signature.createTypeSignature(tableRowDataBuilder.getElementName(), false);
    // getRows
    IMethodSourceBuilder getRowsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRows");
    getRowsMethodBuilder.setReturnTypeSignature(Signature.createArraySignature(tableRowSignature, 1));
    getRowsMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(Signature.createArraySignature(tableRowSignature, 1), validator)).append(") super.getRows();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowsMethodBuilder), getRowsMethodBuilder);

    // setRows
    IMethodSourceBuilder setRowsMethodBuilder = new MethodSourceBuilder("setRows");
    setRowsMethodBuilder.setFlags(Flags.AccPublic);
    setRowsMethodBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    setRowsMethodBuilder.addParameter(new MethodParameter("rows", Signature.createArraySignature(tableRowSignature, 1)));
    setRowsMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("super.setRows(rows);");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(setRowsMethodBuilder), setRowsMethodBuilder);

    // addRow
    final String addRowMethodName = "addRow";
    IMethodSourceBuilder addRowMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "addRow", new IMethodFilter() {

      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        if (addRowMethodName.equals(candidate.getElementName())) {
          return candidate.getParameters().length == 0;
        }
        return false;
      }
    });
    addRowMethodBuilder.setReturnTypeSignature(tableRowSignature);
    addRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(") super.addRow();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(addRowMethodBuilder), addRowMethodBuilder);

    // addRow(int state)
    IMethodSourceBuilder addRowWithStateMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "addRow", new IMethodFilter() {

      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        if (addRowMethodName.equals(candidate.getElementName())) {
          return candidate.getParameters().length == 1;
        }
        return false;
      }
    });
    addRowWithStateMethodBuilder.setReturnTypeSignature(tableRowSignature);
    addRowWithStateMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(") super.addRow(");
        source.append(methodBuilder.getParameters().get(0).getName()).append(");");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(addRowWithStateMethodBuilder), addRowWithStateMethodBuilder);

    // rowAt
    IMethodSourceBuilder rowAtMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "rowAt");
    rowAtMethodBuilder.setReturnTypeSignature(tableRowSignature);
    rowAtMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {

        source.append("return (").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(") super.rowAt(").append(methodBuilder.getParameters().get(0).getName()).append(");");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(rowAtMethodBuilder), rowAtMethodBuilder);

    // createRow
    IMethodSourceBuilder createRowMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "createRow");
    createRowMethodBuilder.setReturnTypeSignature(tableRowSignature);
    createRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return new ").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append("();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowMethodBuilder), createRowMethodBuilder);

    // getRowType
    IMethodSourceBuilder getRowTypeMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRowType");
    getRowTypeMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {

        source.append("return ").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeMethodBuilder), getRowTypeMethodBuilder);
  }

  /**
   * @param table
   * @param fieldHierarchy
   * @return
   */
  protected abstract String getTableRowDataSuperClassSignature(IType table, ITypeHierarchy fieldHierarchy);

  protected String getTableRowBeanName(IType table) {
    String tableRowBeanName = table.getElementName();
    if (TypeUtility.getType(IRuntimeClasses.ITable).equals(table) || "table".equalsIgnoreCase(tableRowBeanName)) {
      // use table fields name
      tableRowBeanName = ScoutUtility.removeFieldSuffix(table.getDeclaringType().getElementName());
    }
    tableRowBeanName = tableRowBeanName + "RowData";
    return tableRowBeanName;
  }

  protected void addAbstractMethodImplementations() throws CoreException {
    // createRow
    IMethodSourceBuilder createRowSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "createRow");

    createRowSourceBuilder.setReturnTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTableRowData, true));
    createRowSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return new ").append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(RuntimeClasses.AbstractTableRowData), validator));
        source.append("(){").append(lineDelimiter).append("private static final long serialVersionUID = 1L;").append(lineDelimiter).append("};");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowSourceBuilder), createRowSourceBuilder);

    IMethodSourceBuilder getRowTypeSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRowType");
    getRowTypeSourceBuilder.setReturnTypeSignature(Signature.createTypeSignature(Class.class.getName() + "<? extends " + RuntimeClasses.AbstractTableRowData + ">", true));
    getRowTypeSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(RuntimeClasses.AbstractTableRowData), validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeSourceBuilder), getRowTypeSourceBuilder);
  }

}
