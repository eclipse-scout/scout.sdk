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
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder;

import java.util.regex.Pattern;

import org.apache.commons.collections.Predicate;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.TypeFilters;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table.TableRowDataTypeSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodParameterDescription;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractTableBeanSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractTableBeanSourceBuilder extends AbstractDtoTypeSourceBuilder {

  private static final Pattern DATA_SUFFIX_PATTERN = Pattern.compile("(PageData|FieldData|Data)$");

  /**
   * @param elementName
   */
  public AbstractTableBeanSourceBuilder(IType modelType, String typeName, ILookupEnvironment lookupEnv, boolean setup) {
    super(modelType, typeName, lookupEnv, setup);
  }

  @Override
  protected void createContent() {
    super.createContent();
    IType table = CoreUtils.findInnerTypeInSuperHierarchy(getModelType(), TypeFilters.getSubtypeFilter(IRuntimeClasses.ITable));
    if (table != null) {
      visitTableBean(table);
    }
    else {
      addAbstractMethodImplementations();
    }
  }

  protected void visitTableBean(IType table) {
    // inner row data class
    String rowDataName = DATA_SUFFIX_PATTERN.matcher(getElementName()).replaceAll("") + "RowData";
    ITypeSourceBuilder tableRowDataBuilder = new TableRowDataTypeSourceBuilder(rowDataName, table, getModelType(), getLookupEnvironment());
    addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableRowDataBuilder), tableRowDataBuilder);

    // row access methods
    final String tableRowSignature = Signature.createTypeSignature(rowDataName, false);
    // getRows
    IMethodSourceBuilder getRowsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), "getRows");
    getRowsMethodBuilder.setReturnTypeSignature(Signature.createArraySignature(tableRowSignature, 1));
    getRowsMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return (").append(SignatureUtils.getTypeReference(Signature.createArraySignature(tableRowSignature, 1), validator)).append(") super.getRows();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowsMethodBuilder), getRowsMethodBuilder);

    // setRows
    IMethodSourceBuilder setRowsMethodBuilder = new MethodSourceBuilder("setRows");
    setRowsMethodBuilder.setFlags(Flags.AccPublic);
    setRowsMethodBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    setRowsMethodBuilder.addParameter(new MethodParameterDescription("rows", Signature.createArraySignature(tableRowSignature, 1)));
    setRowsMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("super.setRows(rows);");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(setRowsMethodBuilder), setRowsMethodBuilder);

    // addRow
    final String addRowMethodName = "addRow";
    IMethodSourceBuilder addRowMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), addRowMethodName, new Predicate/*<IMethod>*/() {
      @Override
      public boolean evaluate(Object candidate) {
        // choose the narrowed overload from the abstract super class instead of the method defined in the interface
        return ((IMethod) candidate).getParameters().size() == 0;
      }
    });
    addRowMethodBuilder.setReturnTypeSignature(tableRowSignature);
    addRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return (").append(SignatureUtils.getTypeReference(tableRowSignature, validator)).append(") super.addRow();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(addRowMethodBuilder), addRowMethodBuilder);

    // addRow(int state)
    IMethodSourceBuilder addRowWithStateMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), addRowMethodName, new Predicate/*<IMethod>*/() {
      @Override
      public boolean evaluate(Object candidate) {
        return ((IMethod) candidate).getParameters().size() == 1;
      }
    });
    addRowWithStateMethodBuilder.getParameters().get(0).setName("rowState"); // in case the param name cannot be parsed from the class file
    addRowWithStateMethodBuilder.setReturnTypeSignature(tableRowSignature);
    addRowWithStateMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return (").append(SignatureUtils.getTypeReference(tableRowSignature, validator)).append(") super.addRow(");
        source.append(methodBuilder.getParameters().get(0).getName()).append(");");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(addRowWithStateMethodBuilder), addRowWithStateMethodBuilder);

    // rowAt
    IMethodSourceBuilder rowAtMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), "rowAt");
    rowAtMethodBuilder.setReturnTypeSignature(tableRowSignature);
    rowAtMethodBuilder.getParameters().get(0).setName("index"); // in case the param name cannot be parsed from the class file
    rowAtMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return (").append(SignatureUtils.getTypeReference(tableRowSignature, validator)).append(") super.rowAt(").append(methodBuilder.getParameters().get(0).getName()).append(");");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(rowAtMethodBuilder), rowAtMethodBuilder);

    // createRow
    IMethodSourceBuilder createRowMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), "createRow");
    createRowMethodBuilder.setReturnTypeSignature(tableRowSignature);
    if (Flags.isAbstract(table.getFlags()) || Flags.isAbstract(getModelType().getFlags())) {
      createRowMethodBuilder.setFlags(createRowMethodBuilder.getFlags() | Flags.AccAbstract);
    }
    else {
      createRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
          source.append("return new ").append(SignatureUtils.getTypeReference(tableRowSignature, validator)).append("();");
        }
      });
    }
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowMethodBuilder), createRowMethodBuilder);

    // getRowType
    IMethodSourceBuilder getRowTypeMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), "getRowType");
    getRowTypeMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return ").append(SignatureUtils.getTypeReference(tableRowSignature, validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeMethodBuilder), getRowTypeMethodBuilder);
  }

  protected void addAbstractMethodImplementations() {
    // createRow
    IMethodSourceBuilder createRowSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), "createRow");

    createRowSourceBuilder.setReturnTypeSignature(Signature.createTypeSignature(IRuntimeClasses.AbstractTableRowData));
    createRowSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return new ").append(SignatureUtils.getTypeReference(Signature.createTypeSignature(IRuntimeClasses.AbstractTableRowData), validator));
        source.append("(){").append(lineDelimiter).append("private static final long serialVersionUID = 1L;").append(lineDelimiter).append("};");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowSourceBuilder), createRowSourceBuilder);

    IMethodSourceBuilder getRowTypeSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, getLookupEnvironment(), "getRowType");
    getRowTypeSourceBuilder.setReturnTypeSignature(Signature.createTypeSignature(Class.class.getName() + ISignatureConstants.C_GENERIC_START + "? extends " + IRuntimeClasses.AbstractTableRowData + ISignatureConstants.C_GENERIC_END));
    getRowTypeSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return ").append(SignatureUtils.getTypeReference(Signature.createTypeSignature(IRuntimeClasses.AbstractTableRowData), validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeSourceBuilder), getRowTypeSourceBuilder);
  }

}
