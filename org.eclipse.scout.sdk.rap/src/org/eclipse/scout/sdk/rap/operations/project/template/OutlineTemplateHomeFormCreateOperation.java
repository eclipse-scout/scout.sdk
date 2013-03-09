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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.TableColumnNewOperation;
import org.eclipse.scout.sdk.operation.form.FormHandlerNewOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.GroupBoxNewOperation;
import org.eclipse.scout.sdk.operation.form.field.TableFieldNewOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateMobileClientPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link OutlineTemplateHomeFormCreateOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 08.03.2013
 */
public class OutlineTemplateHomeFormCreateOperation extends AbstractScoutProjectNewOperation {

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
    String homeFormName = MobileDesktopExtensionInstallOperation.MOBILE_HOME_FORM_NAME;
    ScoutTypeNewOperation homeFormOp = new ScoutTypeNewOperation(homeFormName, mobileClient.getPackageName(".ui.forms"), mobileClient);
    homeFormOp.addInterfaceSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm"));
    homeFormOp.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm"));
    homeFormOp.validate();
    homeFormOp.run(monitor, workingCopyManager);
    IType homeForm = homeFormOp.getCreatedType();

    // constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(homeForm, false);
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    constructorOp.setSimpleBody("  super();");
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);

    // getConfiguredAskIfNeedSave
    MethodOverrideOperation getConfiguredAskIfNeedSave = new MethodOverrideOperation(homeForm, "getConfiguredAskIfNeedSave", false);
    getConfiguredAskIfNeedSave.setSimpleBody("  return false;");
    getConfiguredAskIfNeedSave.validate();
    getConfiguredAskIfNeedSave.run(monitor, workingCopyManager);

    // getConfiguredDisplayHint
    MethodOverrideOperation getConfiguredDisplayHint = new MethodOverrideOperation(homeForm, "getConfiguredDisplayHint", false);
    getConfiguredDisplayHint.setSimpleBody("return DISPLAY_HINT_VIEW;");
    getConfiguredDisplayHint.validate();
    getConfiguredDisplayHint.run(monitor, workingCopyManager);

    // getConfiguredDisplayViewId
    MethodOverrideOperation getConfiguredDisplayViewId = new MethodOverrideOperation(homeForm, "getConfiguredDisplayViewId", false);
    getConfiguredDisplayViewId.setSimpleBody("return VIEW_ID_CENTER;");
    getConfiguredDisplayViewId.validate();
    getConfiguredDisplayViewId.run(monitor, workingCopyManager);

    // getConfiguredTitle
    MethodOverrideOperation getConfiguredTitle = new MethodOverrideOperation(homeForm, "getConfiguredTitle", false);
    getConfiguredTitle.setSimpleBody("return TEXTS.get(\"MobileOutlineChooserTitle\");");
    getConfiguredTitle.validate();
    getConfiguredTitle.run(monitor, workingCopyManager);

    // getConfiguredTitle
    MethodOverrideOperation getConfiguredFooterVisible = new MethodOverrideOperation(homeForm, "getConfiguredFooterVisible", false);
    getConfiguredFooterVisible.setSimpleBody("return true;");
    getConfiguredFooterVisible.validate();
    getConfiguredFooterVisible.run(monitor, workingCopyManager);

    // main box
    GroupBoxNewOperation mainBoxOp = new GroupBoxNewOperation(homeForm, false);
    mainBoxOp.setTypeName(SdkProperties.TYPE_NAME_MAIN_BOX);
    mainBoxOp.validate();
    mainBoxOp.run(monitor, workingCopyManager);

    fillMainBox(mainBoxOp.getCreatedField(), homeForm, mobileClient, monitor, workingCopyManager);

    // view handler
    FormHandlerNewOperation viewHandlerOp = new FormHandlerNewOperation(homeForm);
    viewHandlerOp.setTypeName("ViewHandler");
    viewHandlerOp.validate();
    viewHandlerOp.setFormatSource(false);
    viewHandlerOp.run(monitor, workingCopyManager);
    IType viewHandler = viewHandlerOp.getCreatedHandler();

    // execLoad
    MethodOverrideOperation execLoad = new MethodOverrideOperation(viewHandler, "execLoad", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("OutlinesTableField.Table table = getOutlinesTableField().getTable();\n");
        body.append("IOutline[] outlines = getDesktop().getAvailableOutlines();\n");
        body.append("for (IOutline outline : outlines) {\n");
        body.append("  if (outline.isVisible() && outline.getRootNode() != null) {\n");
        body.append("    ITableRow row = table.createRow(new Object[]{outline, outline.getTitle()});\n");
        body.append("    row.setEnabled(outline.isEnabled());\n");
        body.append("    table.addRow(row);\n");
        body.append("  }\n");
        body.append("}");
        return body.toString();
      }
    };
    execLoad.validate();
    execLoad.run(monitor, workingCopyManager);

    // execFinally
    MethodOverrideOperation execFinally = new MethodOverrideOperation(viewHandler, "execFinally", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("OutlinesTableField.Table table = getOutlinesTableField().getTable();\n");
        body.append("table.discardAllRows();");
        return body.toString();
      }
    };
    execFinally.validate();
    execFinally.run(monitor, workingCopyManager);

    return homeForm;
  }

  private void fillMainBox(IType mainBox, IType formType, IScoutBundle mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // getConfiguredBorderVisible
    MethodOverrideOperation getConfiguredBorderVisible = new MethodOverrideOperation(mainBox, "getConfiguredBorderVisible", false);
    getConfiguredBorderVisible.setSimpleBody("return false;");
    getConfiguredBorderVisible.validate();
    getConfiguredBorderVisible.run(monitor, workingCopyManager);

    // execLoad
    MethodOverrideOperation execInitField = new MethodOverrideOperation(mainBox, "execInitField", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("// Table already is scrollable, it's not necessary to make the form scrollable too\n");
        body.append("IDeviceTransformationService service = SERVICES.getService(IDeviceTransformationService.class);\n");
        body.append("if (service != null && service.getDeviceTransformer() != null) {\n");
        body.append("  service.getDeviceTransformer().getDeviceTransformationExcluder().excludeFieldTransformation(this, MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);\n");
        body.append("}");
        return body.toString();
      }
    };
    execInitField.validate();
    execInitField.run(monitor, workingCopyManager);

    createOutlinesTable(mainBox, formType, mobileClient, monitor, workingCopyManager);

    FormFieldNewOperation buttonOp = new FormFieldNewOperation(mainBox, false);
    buttonOp.setCreateFormFieldGetterMethod(true);
    buttonOp.setFormType(formType);
    buttonOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IButton, mobileClient));
    buttonOp.setTypeName("LogoutButton");
    buttonOp.validate();
    buttonOp.run(monitor, workingCopyManager);
    IType logoutButton = buttonOp.getCreatedFormField();

    // getConfiguredLabel
    MethodOverrideOperation getConfiguredLabel = new MethodOverrideOperation(logoutButton, "getConfiguredLabel", false);
    getConfiguredLabel.setSimpleBody("return TEXTS.get(\"Logoff\");");
    getConfiguredLabel.validate();
    getConfiguredLabel.run(monitor, workingCopyManager);

    // execClickAction
    MethodOverrideOperation execClickAction = new MethodOverrideOperation(logoutButton, "execClickAction", false);
    execClickAction.setSimpleBody("ClientJob.getCurrentSession().stopSession();");
    execClickAction.validate();
    execClickAction.run(monitor, workingCopyManager);
  }

  private void createOutlinesTable(IType mainBox, IType formType, IScoutBundle mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    TableFieldNewOperation outlinesTableFieldOp = new TableFieldNewOperation(mainBox);
    outlinesTableFieldOp.setFormatSource(false);
    outlinesTableFieldOp.setFormType(formType);
    outlinesTableFieldOp.setTypeName("OutlinesTableField");
    outlinesTableFieldOp.setTableSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable"));
    outlinesTableFieldOp.validate();
    outlinesTableFieldOp.run(monitor, workingCopyManager);

    IType tableField = outlinesTableFieldOp.getCreatedField();
    IType table = outlinesTableFieldOp.getCreatedTable();

    // getConfiguredLabelVisible
    MethodOverrideOperation getConfiguredLabelVisible = new MethodOverrideOperation(tableField, "getConfiguredLabelVisible", false);
    getConfiguredLabelVisible.setSimpleBody("return false;");
    getConfiguredLabelVisible.validate();
    getConfiguredLabelVisible.run(monitor, workingCopyManager);

    // getConfiguredGridH
    MethodOverrideOperation getConfiguredGridH = new MethodOverrideOperation(tableField, "getConfiguredGridH", false);
    getConfiguredGridH.setSimpleBody("return 2;");
    getConfiguredGridH.validate();
    getConfiguredGridH.run(monitor, workingCopyManager);

    // execIsAutoCreateTableRowForm
    MethodOverrideOperation execIsAutoCreateTableRowForm = new MethodOverrideOperation(table, "execIsAutoCreateTableRowForm", false);
    execIsAutoCreateTableRowForm.setSimpleBody("return false;");
    execIsAutoCreateTableRowForm.validate();
    execIsAutoCreateTableRowForm.run(monitor, workingCopyManager);

    // getConfiguredAutoDiscardOnDelete
    MethodOverrideOperation getConfiguredAutoDiscardOnDelete = new MethodOverrideOperation(table, "getConfiguredAutoDiscardOnDelete", false);
    getConfiguredAutoDiscardOnDelete.setSimpleBody("return true;");
    getConfiguredAutoDiscardOnDelete.validate();
    getConfiguredAutoDiscardOnDelete.run(monitor, workingCopyManager);

    // getConfiguredDefaultIconId
    MethodOverrideOperation getConfiguredDefaultIconId = new MethodOverrideOperation(table, "getConfiguredDefaultIconId", false);
    getConfiguredDefaultIconId.setSimpleBody("return AbstractIcons.TreeNode;");
    getConfiguredDefaultIconId.validate();
    getConfiguredDefaultIconId.run(monitor, workingCopyManager);

    // getConfiguredAutoResizeColumns
    MethodOverrideOperation getConfiguredAutoResizeColumns = new MethodOverrideOperation(table, "getConfiguredAutoResizeColumns", false);
    getConfiguredAutoResizeColumns.setSimpleBody("return true;");
    getConfiguredAutoResizeColumns.validate();
    getConfiguredAutoResizeColumns.run(monitor, workingCopyManager);

    // getConfiguredGridH
    MethodOverrideOperation getConfiguredSortEnabled = new MethodOverrideOperation(table, "getConfiguredSortEnabled", false);
    getConfiguredSortEnabled.setSimpleBody("return false;");
    getConfiguredSortEnabled.validate();
    getConfiguredSortEnabled.run(monitor, workingCopyManager);

    // execLoad
    MethodOverrideOperation execDecorateRow = new MethodOverrideOperation(table, "execDecorateRow", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("String outlineIcon = getOutlineColumn().getValue(row).getIconId();\n");
        body.append("if (outlineIcon != null) {\n");
        body.append("  row.setIconId(outlineIcon);\n");
        body.append("}");
        return body.toString();
      }
    };
    execDecorateRow.validate();
    execDecorateRow.run(monitor, workingCopyManager);

    // execLoad
    MethodOverrideOperation execRowsSelected = new MethodOverrideOperation(table, "execRowsSelected", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("if (rows == null || rows.length == 0) {\n");
        body.append("  return;\n");
        body.append("}\n");
        body.append("IOutline outline = getOutlineColumn().getValue(rows[0]);\n");
        body.append("MobileDesktopUtility.activateOutline(outline);\n");
        body.append("getDesktop().removeForm(" + MobileDesktopExtensionInstallOperation.MOBILE_HOME_FORM_NAME + ".this);\n");
        body.append("clearSelectionDelayed();");
        return body.toString();
      }
    };
    execRowsSelected.validate();
    execRowsSelected.run(monitor, workingCopyManager);

    // outline column
    TableColumnNewOperation outlineColumnOp = new TableColumnNewOperation(table, false);
    String superType = RuntimeClasses.getSuperTypeName(RuntimeClasses.IColumn, mobileClient);
    superType = superType + "<" + RuntimeClasses.IOutline + ">";
    outlineColumnOp.setSuperTypeSignature(SignatureCache.createTypeSignature(superType));
    outlineColumnOp.setTypeName("OutlineColumn");
    outlineColumnOp.validate();
    outlineColumnOp.run(monitor, workingCopyManager);

    // getConfiguredDisplayable
    MethodOverrideOperation getConfiguredDisplayable = new MethodOverrideOperation(outlineColumnOp.getCreatedColumn(), "getConfiguredDisplayable", false);
    getConfiguredDisplayable.setSimpleBody("return false;");
    getConfiguredDisplayable.validate();
    getConfiguredDisplayable.run(monitor, workingCopyManager);

    // label column
    TableColumnNewOperation labelColumnOp = new TableColumnNewOperation(table, false);
    labelColumnOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IStringColumn, mobileClient));
    labelColumnOp.setTypeName("LabelColumn");
    labelColumnOp.validate();
    labelColumnOp.run(monitor, workingCopyManager);
  }
}
