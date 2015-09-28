/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s;

/**
 * Classes indices and values used in the scout runtime.
 */
public interface IScoutRuntimeTypes {

  // type parameter positions
  int TYPE_PARAM_EXTENSION__OWNER = 0;
  int TYPE_PARAM_COLUMN_VALUE_TYPE = 0;

  // annotations
  String FormData = "org.eclipse.scout.commons.annotations.FormData";
  String Order = "org.eclipse.scout.commons.annotations.Order";
  String PageData = "org.eclipse.scout.commons.annotations.PageData";
  String Data = "org.eclipse.scout.commons.annotations.Data";
  String ClassId = "org.eclipse.scout.commons.annotations.ClassId";
  String DtoRelevant = "org.eclipse.scout.commons.annotations.DtoRelevant";
  String Replace = "org.eclipse.scout.commons.annotations.Replace";
  String ColumnData = "org.eclipse.scout.commons.annotations.ColumnData";
  String Extends = "org.eclipse.scout.commons.annotations.Extends";
  String ConfigOperation = "org.eclipse.scout.commons.annotations.ConfigOperation";
  String ConfigProperty = "org.eclipse.scout.commons.annotations.ConfigProperty";

  // abstract implementations
  String AbstractPropertyData = "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData";
  String AbstractFormFieldData = "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData";
  String AbstractTableRowData = "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData";
  String AbstractTablePageData = "org.eclipse.scout.rt.shared.data.page.AbstractTablePageData";
  String AbstractTableFieldBeanData = "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData";

  // interfaces
  String IClientSession = "org.eclipse.scout.rt.client.IClientSession";
  String ICodeType = "org.eclipse.scout.rt.shared.services.common.code.ICodeType";
  String IForm = "org.eclipse.scout.rt.client.ui.form.IForm";
  String IPageWithTableExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithTableExtension";
  String ITableExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension";
  String ITable = "org.eclipse.scout.rt.client.ui.basic.table.ITable";
  String IColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn";
  String IFormField = "org.eclipse.scout.rt.client.ui.form.fields.IFormField";
  String ICompositeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension";
  String ITableField = "org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField";
  String ICompositeField = "org.eclipse.scout.rt.client.ui.form.fields.ICompositeField";
  String IValueField = "org.eclipse.scout.rt.client.ui.form.fields.IValueField";
  String IPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable";
  String IExtension = "org.eclipse.scout.rt.shared.extension.IExtension";
  String IFormExtension = "org.eclipse.scout.rt.client.extension.ui.form.IFormExtension";
  String IFormFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension";
  String IScoutLogger = "org.eclipse.scout.commons.logger.IScoutLogger";
  String ITreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField";
  String IPlannerField = "org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField";
  String IComposerField = "org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField";
  String IDataModelAttribute = "org.eclipse.scout.rt.shared.data.model.IDataModelAttribute";
  String IDataModelEntity = "org.eclipse.scout.rt.shared.data.model.IDataModelEntity";
  String ICalendar = "org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar";
  String ICode = "org.eclipse.scout.rt.shared.services.common.code.ICode";
  String IDesktop = "org.eclipse.scout.rt.client.ui.desktop.IDesktop";
  String IDesktopExtension = "org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension";
  String IOutline = "org.eclipse.scout.rt.client.ui.desktop.outline.IOutline";
  String IPageWithNodes = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes";
  String IWizard = "org.eclipse.scout.rt.client.ui.wizard.IWizard";
  String IWizardStep = "org.eclipse.scout.rt.client.ui.wizard.IWizardStep";
  String IMenu = "org.eclipse.scout.rt.client.ui.action.menu.IMenu";
  String IActivityMap = "org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap";
  String IFormHandler = "org.eclipse.scout.rt.client.ui.form.IFormHandler";
  String IKeyStroke = "org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke";
  String IButton = "org.eclipse.scout.rt.client.ui.form.fields.button.IButton";
  String ILookupCall = "org.eclipse.scout.rt.shared.services.lookup.ILookupCall";
  String IViewButton = "org.eclipse.scout.rt.client.ui.action.view.IViewButton";
  String IToolButton = "org.eclipse.scout.rt.client.ui.action.tool.IToolButton";
  String ITree = "org.eclipse.scout.rt.client.ui.basic.tree.ITree";
  String ICalendarItemProvider = "org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider";
  String IPage = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage";
  String ITypeWithClassId = "org.eclipse.scout.commons.ITypeWithClassId";
}
