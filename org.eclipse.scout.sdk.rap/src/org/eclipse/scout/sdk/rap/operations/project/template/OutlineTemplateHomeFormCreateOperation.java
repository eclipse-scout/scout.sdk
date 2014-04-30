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
package org.eclipse.scout.sdk.rap.operations.project.template;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateMobileClientPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link OutlineTemplateHomeFormCreateOperation}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 08.03.2013
 */
public class OutlineTemplateHomeFormCreateOperation extends AbstractScoutProjectNewOperation {

  private static final String OUTLINE_TABLE_FIELD_NAME = "OutlinesTableField";

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID) && OutlineTemplateOperation.TEMPLATE_ID.equals(getTemplateName());
  }

  @Override
  public void init() {
  }

  @Override
  public String getOperationName() {
    return "Apply outline template...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    final IScoutBundle mobileClient = bundleGraph.getBundle(getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class));

    createHomeForm(mobileClient, monitor, workingCopyManager);
  }

  private IType createHomeForm(IScoutBundle mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    String packageName = mobileClient.getPackageName(".ui.forms");
    PrimaryTypeNewOperation homeFormOp = new PrimaryTypeNewOperation("MobileHomeForm", packageName, mobileClient.getJavaProject());
    homeFormOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    homeFormOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    homeFormOp.setFlags(Flags.AccPublic);
    homeFormOp.addInterfaceSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm"));
    homeFormOp.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm"));

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(homeFormOp.getElementName());
    constructorBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    constructorBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    constructorBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // method getConfiguredAskIfNeedSave
    IMethodSourceBuilder getConfiguredAskIfNeedSaveBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(homeFormOp.getSourceBuilder(), "getConfiguredAskIfNeedSave");
    getConfiguredAskIfNeedSaveBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredAskIfNeedSaveBuilder), getConfiguredAskIfNeedSaveBuilder);

    // method getConfiguredDisplayHint
    IMethodSourceBuilder getConfiguredDisplayHintBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(homeFormOp.getSourceBuilder(), "getConfiguredDisplayHint");
    getConfiguredDisplayHintBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return DISPLAY_HINT_VIEW;"));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredDisplayHintBuilder), getConfiguredDisplayHintBuilder);

    // method getConfiguredDisplayViewId
    IMethodSourceBuilder getConfiguredDisplayViewIdBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(homeFormOp.getSourceBuilder(), "getConfiguredDisplayViewId");
    getConfiguredDisplayViewIdBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return VIEW_ID_CENTER;"));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredDisplayViewIdBuilder), getConfiguredDisplayViewIdBuilder);

    // method getConfiguredTitle
    IMethodSourceBuilder getConfiguredTitleBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(homeFormOp.getSourceBuilder(), "getConfiguredTitle");
    getConfiguredTitleBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.TEXTS))).append(".get(\"MobileOutlineChooserTitle\");");
      }
    });
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredTitleBuilder), getConfiguredTitleBuilder);

    // method getConfiguredFooterVisible
    IMethodSourceBuilder getConfiguredFooterVisibleBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(homeFormOp.getSourceBuilder(), "getConfiguredFooterVisible");
    getConfiguredFooterVisibleBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return true;"));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredFooterVisibleBuilder), getConfiguredFooterVisibleBuilder);

    // main box
    ITypeSourceBuilder mainBoxBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_MAIN_BOX);
    mainBoxBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10.0));
    mainBoxBuilder.setFlags(Flags.AccPublic);
    mainBoxBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IGroupBox, mobileClient.getJavaProject()));
    // fill main box
    String mainBoxFqn = packageName + "." + homeFormOp.getElementName() + "." + SdkProperties.TYPE_NAME_MAIN_BOX;
    fillMainBox(mainBoxBuilder, mainBoxFqn, homeFormOp.getSourceBuilder(), mobileClient.getJavaProject(), monitor, workingCopyManager);
    homeFormOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(mainBoxBuilder, 10.0), mainBoxBuilder);

    // main box getter
    final String mainBoxSignature = SignatureCache.createTypeSignature(mainBoxFqn);
    IMethodSourceBuilder mainBoxGetterBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(mainBoxSignature);
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(mainBoxGetterBuilder), mainBoxGetterBuilder);

    // view handler
    ITypeSourceBuilder viewHandlerBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_VIEW_HANDLER);
    viewHandlerBuilder.setFlags(Flags.AccPublic);
    viewHandlerBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IFormHandler, mobileClient.getJavaProject()));
    fillViewHandler(viewHandlerBuilder, mainBoxFqn, monitor, workingCopyManager);
    homeFormOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormHandlerKey(viewHandlerBuilder), viewHandlerBuilder);

    // startView Method
    final String viewHandlerFqn = homeFormOp.getPackageName() + "." + homeFormOp.getElementName() + "." + viewHandlerBuilder.getElementName();
    IMethodSourceBuilder startViewBuilder = new MethodSourceBuilder("startView");
    startViewBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    startViewBuilder.setFlags(Flags.AccPublic);
    startViewBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    startViewBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    startViewBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("startInternal(new ").append(validator.getTypeName(SignatureCache.createTypeSignature(viewHandlerFqn))).append("());");
      }
    });
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodStartFormKey(startViewBuilder), startViewBuilder);

    homeFormOp.setFormatSource(true);
    homeFormOp.validate();
    homeFormOp.run(monitor, workingCopyManager);
    IType homeForm = homeFormOp.getCreatedType();

    return homeForm;
  }

  private void fillViewHandler(ITypeSourceBuilder viewHandlerBuilder, String mainBoxFqn, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    final String outlinesTableFieldTableFqn = new StringBuilder(mainBoxFqn).append('.').append(OUTLINE_TABLE_FIELD_NAME).append('.').append(SdkProperties.TYPE_NAME_TABLEFIELD_TABLE).toString();
    final String outlinesTableFieldTableSignature = SignatureCache.createTypeSignature(outlinesTableFieldTableFqn);

    // execLoad method
    IMethodSourceBuilder execLoadBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(viewHandlerBuilder, "execLoad");
    execLoadBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append(validator.getTypeName(outlinesTableFieldTableSignature)).append(" table = getOutlinesTableField().getTable();").append(lineDelimiter);
        String listTypeName = validator.getTypeName(SignatureCache.createTypeSignature(List.class.getName()));
        String outlineTypeName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.IOutline));
        source.append(listTypeName).append('<').append(outlineTypeName).append("> outlines = getDesktop().getAvailableOutlines();").append(lineDelimiter);
        source.append("for (").append(outlineTypeName).append(" outline : outlines) {").append(lineDelimiter);
        source.append("if (outline.isVisible() && outline.getRootNode() != null) {").append(lineDelimiter);
        source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.ITableRow))).append(" row = table.createRow(new Object[]{outline, outline.getTitle()});").append(lineDelimiter);
        source.append("row.setEnabled(outline.isEnabled());").append(lineDelimiter);
        source.append("table.addRow(row);").append(lineDelimiter);
        source.append("}").append(lineDelimiter);
        source.append("}").append(lineDelimiter);
      }
    });
    viewHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execLoadBuilder), execLoadBuilder);

    // execFinally method
    IMethodSourceBuilder execFinallyBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(viewHandlerBuilder, "execFinally");
    execFinallyBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append(validator.getTypeName(outlinesTableFieldTableSignature)).append(" table = getOutlinesTableField().getTable();").append(lineDelimiter);
        source.append("table.discardAllRows();");
      }
    });
    viewHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execFinallyBuilder), execFinallyBuilder);

  }

  /**
   * @param mainBoxBuilder
   * @param monitor
   * @param workingCopyManager
   * @throws CoreException
   */
  private void fillMainBox(ITypeSourceBuilder mainBoxBuilder, String mainBoxFqn, ITypeSourceBuilder formBuilder, IJavaProject mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // method getConfiguredBorderVisible
    IMethodSourceBuilder getConfiguredBorderVisibleBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(mainBoxBuilder, "getConfiguredBorderVisible");
    getConfiguredBorderVisibleBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    mainBoxBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredBorderVisibleBuilder), getConfiguredBorderVisibleBuilder);

    // execInitField method
    IMethodSourceBuilder execInitFieldBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(mainBoxBuilder, "execInitField");
    execInitFieldBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        String deviceTransformationConfig = validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationConfig"));
        String deviceTransformationUtility = validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationUtility"));
        String mobileDeviceTransformation = validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformation"));

        source.append("// Table already is scrollable, it's not necessary to make the form scrollable too\n");
        source.append(deviceTransformationConfig).append(" config = ").append(deviceTransformationUtility).append(".getDeviceTransformationConfig();\n");
        source.append("if (config != null) {\n");
        source.append("  config.excludeFieldTransformation(this, ").append(mobileDeviceTransformation).append(".MAKE_MAINBOX_SCROLLABLE);\n");
        source.append("}");
      }
    });
    mainBoxBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execInitFieldBuilder), execInitFieldBuilder);

    // OutlinesTableField type
    createOutlinesTableField(mainBoxBuilder, mainBoxFqn, formBuilder, mobileClient, monitor, workingCopyManager);
    // end OutlinesTableField type

    // LogoutButton type
    ITypeSourceBuilder logoutButtonBuilder = new TypeSourceBuilder("LogoutButton");
    logoutButtonBuilder.setFlags(Flags.AccPublic);
    logoutButtonBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(20));
    logoutButtonBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IButton, mobileClient));
    // getConfiguredLabel method
    IMethodSourceBuilder getConfiguredLabelBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(logoutButtonBuilder, "getConfiguredLabel");
    getConfiguredLabelBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.TEXTS))).append(".get(\"Logoff\");");
      }
    });
    logoutButtonBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelBuilder), getConfiguredLabelBuilder);
    // execClickAction method
    IMethodSourceBuilder execClickActionBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(logoutButtonBuilder, "execClickAction");
    execClickActionBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append(validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.ClientJob"))).append(".getCurrentSession().stopSession();");
      }
    });
    logoutButtonBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execClickActionBuilder), execClickActionBuilder);
    mainBoxBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(logoutButtonBuilder, 20.0), logoutButtonBuilder);
    // end LogoutButton type

    // LogOutButton getter
    IMethodSourceBuilder getLogoutButtonBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(SignatureCache.createTypeSignature(mainBoxFqn + "." + logoutButtonBuilder.getElementName()));
    formBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(getLogoutButtonBuilder), getLogoutButtonBuilder);
  }

  /**
   * @return The fully qualified name of the table inside the outlines table field.
   */
  private void createOutlinesTableField(ITypeSourceBuilder mainBoxBuilder, String mainBoxFqn, final ITypeSourceBuilder formBuilder, IJavaProject mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // Table type
    ITypeSourceBuilder tableBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_TABLEFIELD_TABLE);
    tableBuilder.setFlags(Flags.AccPublic);
    tableBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10));
    tableBuilder.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable"));

    // OutlinesTableField type
    ITypeSourceBuilder outlinesTableFieldBuilder = new TypeSourceBuilder(OUTLINE_TABLE_FIELD_NAME);
    String genericTableFqn = new StringBuilder(mainBoxFqn).append('.').append(outlinesTableFieldBuilder.getElementName()).append('.').append(tableBuilder.getElementName()).toString();
    outlinesTableFieldBuilder.setFlags(Flags.AccPublic);
    outlinesTableFieldBuilder.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.getSuperTypeName(IRuntimeClasses.ITableField, mobileClient) + "<" + genericTableFqn.toString() + ">"));
    outlinesTableFieldBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10));

    // method getConfiguredLabelVisible
    IMethodSourceBuilder getConfiguredLabelVisible = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(outlinesTableFieldBuilder, "getConfiguredLabelVisible");
    getConfiguredLabelVisible.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    outlinesTableFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(getConfiguredLabelVisible), getConfiguredLabelVisible);

    // method getConfiguredGridH
    IMethodSourceBuilder getConfiguredGridH = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(outlinesTableFieldBuilder, "getConfiguredGridH");
    getConfiguredGridH.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return 2;"));
    outlinesTableFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(getConfiguredGridH), getConfiguredGridH);

    // method execIsAutoCreateTableRowForm
    IMethodSourceBuilder execIsAutoCreateTableRowFormBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "execIsAutoCreateTableRowForm");
    execIsAutoCreateTableRowFormBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execIsAutoCreateTableRowFormBuilder), execIsAutoCreateTableRowFormBuilder);

    // method getConfiguredAutoDiscardOnDelete
    IMethodSourceBuilder getConfiguredAutoDiscardOnDeleteBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "getConfiguredAutoDiscardOnDelete");
    getConfiguredAutoDiscardOnDeleteBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return true;"));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredAutoDiscardOnDeleteBuilder), getConfiguredAutoDiscardOnDeleteBuilder);

    // method getConfiguredDefaultIconId
    IMethodSourceBuilder getConfiguredDefaultIconIdBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "getConfiguredDefaultIconId");
    getConfiguredDefaultIconIdBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractIcons))).append(".TreeNode;");
      }
    });
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredDefaultIconIdBuilder), getConfiguredDefaultIconIdBuilder);

    // method getConfiguredAutoResizeColumns
    IMethodSourceBuilder getConfiguredAutoResizeColumnsBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "getConfiguredAutoResizeColumns");
    getConfiguredAutoResizeColumnsBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return true;"));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredAutoResizeColumnsBuilder), getConfiguredAutoResizeColumnsBuilder);

    // method getConfiguredSortEnabled
    IMethodSourceBuilder getConfiguredSortEnabledBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "getConfiguredSortEnabled");
    getConfiguredSortEnabledBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredSortEnabledBuilder), getConfiguredSortEnabledBuilder);

    // method execDecorateRow
    IMethodSourceBuilder execDecorateRowBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "execDecorateRow");
    execDecorateRowBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("String outlineIcon = getOutlineColumn().getValue(row).getIconId();").append(lineDelimiter);
        source.append("if (outlineIcon != null) {").append(lineDelimiter);
        source.append("  row.setIconId(outlineIcon);").append(lineDelimiter);
        source.append("}");
      }
    });
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execDecorateRowBuilder), execDecorateRowBuilder);

    // method execRowsSelected
    IMethodSourceBuilder execRowsSelectedBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(tableBuilder, "execRowsSelected");
    execRowsSelectedBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("if (rows == null || rows.size() == 0) {").append(lineDelimiter);
        source.append("  return;").append(lineDelimiter);
        source.append("}").append(lineDelimiter);
        source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.IOutline))).append(" outline = getOutlineColumn().getValue(rows.get(0));").append(lineDelimiter);
        source.append(validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility"))).append(".activateOutline(outline);").append(lineDelimiter);
        source.append("getDesktop().removeForm(").append(formBuilder.getElementName()).append(".this);").append(lineDelimiter);
        source.append("clearSelectionDelayed();");
      }
    });
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execRowsSelectedBuilder), execRowsSelectedBuilder);

    // OutlineColumn type
    ITypeSourceBuilder outlineColumnBuilder = new TypeSourceBuilder("OutlineColumn");
    outlineColumnBuilder.setFlags(Flags.AccPublic);
    IType superType = RuntimeClasses.getSuperType(IRuntimeClasses.IColumn, mobileClient);
    if (TypeUtility.isGenericType(superType)) {
      outlineColumnBuilder.setSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName() + "<" + IRuntimeClasses.IOutline + ">"));
    }
    else {
      outlineColumnBuilder.setSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName()));
    }
    outlineColumnBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10));
    // getConfiguredDisplayable method
    IMethodSourceBuilder getConfiguredDisplayableBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(outlineColumnBuilder, "getConfiguredDisplayable");
    getConfiguredDisplayableBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    outlineColumnBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredDisplayableBuilder), getConfiguredDisplayableBuilder);

    tableBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableColumnKey(outlineColumnBuilder, 10), outlineColumnBuilder);

    // LabelColumn type
    ITypeSourceBuilder labelColumnBuilder = new TypeSourceBuilder("LabelColumn");
    labelColumnBuilder.setFlags(Flags.AccPublic);
    labelColumnBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IStringColumn, mobileClient));
    labelColumnBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(20));

    tableBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableColumnKey(labelColumnBuilder, 20), labelColumnBuilder);

    // LabelColumn getter
    String labelColumnFqn = mainBoxFqn + "." + outlinesTableFieldBuilder.getElementName() + "." + tableBuilder.getElementName() + "." + labelColumnBuilder.getElementName();
    IMethodSourceBuilder getLabelColumnBuilder = MethodSourceBuilderFactory.createColumnGetterSourceBuilder(SignatureCache.createTypeSignature(labelColumnFqn));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(getLabelColumnBuilder), getLabelColumnBuilder);

    // LabelColumn getter
    String outlineColumnFqn = mainBoxFqn + "." + outlinesTableFieldBuilder.getElementName() + "." + tableBuilder.getElementName() + "." + outlineColumnBuilder.getElementName();
    IMethodSourceBuilder getOutlineColumnBuilder = MethodSourceBuilderFactory.createColumnGetterSourceBuilder(SignatureCache.createTypeSignature(outlineColumnFqn));
    tableBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(getOutlineColumnBuilder), getOutlineColumnBuilder);

    outlinesTableFieldBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableBuilder), tableBuilder);
    // end Table type

    mainBoxBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(outlinesTableFieldBuilder, 10), outlinesTableFieldBuilder);
    // end OutlinesTableField type

    // getOutlineTableFieldMethod
    IMethodSourceBuilder getOutlineTableFieldBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(SignatureCache.createTypeSignature(mainBoxFqn + "." + outlinesTableFieldBuilder.getElementName()));
    formBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(getOutlineTableFieldBuilder), getOutlineTableFieldBuilder);
  }
}
