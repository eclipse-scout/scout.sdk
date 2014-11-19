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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.pagedata.TableRowDataTypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link AbstractTableBeanSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractTableBeanSourceBuilder extends AbstractDtoTypeSourceBuilder {

  private static final String ABS_TABLE_ROW_DATA_SIMPLE_NAME = Signature.getSimpleName(IRuntimeClasses.AbstractTableRowData);
  private static final Pattern DATA_SUFFIX_PATTERN = Pattern.compile("(PageData|FieldData|Data)$");

  /**
   * @param elementName
   */
  public AbstractTableBeanSourceBuilder(IType modelType, ITypeHierarchy modelLocalTypeHierarchy, String elementName, boolean setup, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    super(modelType, modelLocalTypeHierarchy, elementName, setup, derivedCu, monitor);
  }

  @Override
  protected void createContent(IProgressMonitor monitor) {
    super.createContent(monitor);
    try {
      IType table = DtoUtility.findTable(getModelType(), getLocalTypeHierarchy());
      if (TypeUtility.exists(table)) {
        visitTableBean(table, getLocalTypeHierarchy(), monitor);
      }
      else {
        addAbstractMethodImplementations();
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not build form data for '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
  }

  protected void visitTableBean(IType table, ITypeHierarchy fieldHierarchy, IProgressMonitor monitor) throws CoreException {
    // inner row data class
    String rowDataName = DATA_SUFFIX_PATTERN.matcher(getElementName()).replaceAll("") + "RowData";
    ITypeSourceBuilder tableRowDataBuilder = new TableRowDataTypeSourceBuilder(rowDataName, table, getModelType(), fieldHierarchy, monitor);
    addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableRowDataBuilder), tableRowDataBuilder);

    // row access methods
    final String tableRowSignature = Signature.createTypeSignature(rowDataName, false);
    // getRows
    IMethodSourceBuilder getRowsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRows", new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        // choose the narrowed overload from the abstract super class instead of the method defined in the interface
        return candidate.getReturnType().contains(ABS_TABLE_ROW_DATA_SIMPLE_NAME);
      }
    });
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
        // choose the narrowed overload from the abstract super class instead of the method defined in the interface
        return candidate.getParameters().length == 0 && candidate.getReturnType().contains(ABS_TABLE_ROW_DATA_SIMPLE_NAME);
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
    if (monitor.isCanceled()) {
      return;
    }

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
    if (Flags.isAbstract(table.getFlags()) || Flags.isAbstract(getModelType().getFlags())) {
      createRowMethodBuilder.setFlags(createRowMethodBuilder.getFlags() | Flags.AccAbstract);
    }
    else {
      createRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("return new ").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append("();");
        }
      });
    }
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

  protected void addAbstractMethodImplementations() throws CoreException {
    // createRow
    IMethodSourceBuilder createRowSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "createRow");

    createRowSourceBuilder.setReturnTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData));
    createRowSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return new ").append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData), validator));
        source.append("(){").append(lineDelimiter).append("private static final long serialVersionUID = 1L;").append(lineDelimiter).append("};");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowSourceBuilder), createRowSourceBuilder);

    IMethodSourceBuilder getRowTypeSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRowType");
    getRowTypeSourceBuilder.setReturnTypeSignature(SignatureCache.createTypeSignature(Class.class.getName() + Signature.C_GENERIC_START + "? extends " + IRuntimeClasses.AbstractTableRowData + Signature.C_GENERIC_END));
    getRowTypeSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData), validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeSourceBuilder), getRowTypeSourceBuilder);
  }

}
