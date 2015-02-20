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
package org.eclipse.scout.sdk.extensions.runtime.classes;

import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link RuntimeClasses}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 24.01.2009
 */
public interface IRuntimeClasses {

// CHECKSTYLE:OFF

  // bundles
  String ScoutSharedBundleId = RuntimeBundles.getBundleSymbolicName(IScoutBundle.TYPE_SHARED);
  String ScoutClientBundleId = RuntimeBundles.getBundleSymbolicName(IScoutBundle.TYPE_CLIENT);
  String ScoutServerBundleId = RuntimeBundles.getBundleSymbolicName(IScoutBundle.TYPE_SERVER);
  String ScoutUiSwtBundleId = RuntimeBundles.getBundleSymbolicName(IScoutBundle.TYPE_UI_SWT);
  String ScoutUiSwingBundleId = RuntimeBundles.getBundleSymbolicName(IScoutBundle.TYPE_UI_SWING);

  // generic names
  int TYPE_PARAM_EXTENSION__OWNER = 0;
  int TYPE_PARAM_CODE__CODE_ID = 0;
  int TYPE_PARAM_CODETYPE__CODE_TYPE_ID = 0;
  int TYPE_PARAM_CODETYPE__CODE_ID = 1;
  int TYPE_PARAM_CODETYPE__CODE = 2;
  int TYPE_PARAM_LOOKUPSERVICE__KEY_TYPE = 0;
  int TYPE_PARAM_LOOKUPCALL__KEY_TYPE = 0;
  int TYPE_PARAM_VALUEFIELD__VALUE_TYPE = 0;
  int TYPE_PARAM_COLUMN_VALUE_TYPE = 0;
  int TYPE_PARAM_RADIOBUTTON__VALUE_TYPE = 0;
  int TYPE_PARAM_RADIOBUTTONGROUP__VALUE_TYPE = 0;

  //extension points
  String EXTENSION_POINT_SERVICES = "org.eclipse.scout.service.services"; // NO_UCD
  String EXTENSION_POINT_CLIENT_SERVICE_PROXIES = EXTENSION_POINT_SERVICES;
  String EXTENSION_POINT_DESKTOP_EXTENSIONS = "org.eclipse.scout.rt.extension.client.desktopExtensions"; // NO_UCD
  String EXTENSION_POINT_SERVLET_FILTERS = "org.eclipse.scout.rt.server.commons.filters"; // NO_UCD
  String EXTENSION_POINT_EQUINOX_SERVLETS = "org.eclipse.equinox.http.registry.servlets"; // NO_UCD
  String EXTENSION_POINT_PRODUCTS = "org.eclipse.core.runtime.products"; // NO_UCD

  String EXTENSION_ELEMENT_SERVICE = "service"; // NO_UCD
  String EXTENSION_ELEMENT_SERVLET = "servlet"; // NO_UCD
  String EXTENSION_ELEMENT_CLIENT_SERVICE_PROXY = "proxy"; // NO_UCD
  String EXTENSION_ELEMENT_FILTER = "filter"; // NO_UCD
  String EXTENSION_ELEMENT_PRODUCT = "product"; // NO_UCD
  String EXTENSION_ELEMENT_DESKTOP_EXTENSION = "desktopExtension";

  String EXTENSION_SERVICE_RANKING = "ranking"; // NO_UCD

  // Runtime classes
  String ACCESS = "org.eclipse.scout.rt.shared.services.common.security.ACCESS"; // NO_UCD
  String SERVICES = "org.eclipse.scout.service.SERVICES"; // NO_UCD
  String TEXTS = "org.eclipse.scout.rt.shared.TEXTS"; // NO_UCD

  String BasicHierarchyPermission = "org.eclipse.scout.rt.shared.security.BasicHierarchyPermission"; // NO_UCD
  String BasicPermission = "java.security.BasicPermission"; // NO_UCD
  String Permission = "java.security.Permission"; // NO_UCD
  String FormData = "org.eclipse.scout.commons.annotations.FormData"; // NO_UCD
  String PageData = "org.eclipse.scout.commons.annotations.PageData"; // NO_UCD
  String Data = "org.eclipse.scout.commons.annotations.Data"; // NO_UCD
  String ColumnData = "org.eclipse.scout.commons.annotations.ColumnData"; // NO_UCD
  String Replace = "org.eclipse.scout.commons.annotations.Replace"; // NO_UCD
  String ClassId = "org.eclipse.scout.commons.annotations.ClassId"; // NO_UCD
  String Extends = "org.eclipse.scout.commons.annotations.Extends"; // NO_UCD
  String DtoRelevant = "org.eclipse.scout.commons.annotations.DtoRelevant"; // NO_UCD
  String InjectFieldTo = "org.eclipse.scout.commons.annotations.InjectFieldTo"; // NO_UCD
  String ClientProxyServiceFactory = "org.eclipse.scout.rt.client.services.ClientProxyServiceFactory"; // NO_UCD
  String ClientServiceFactory = "org.eclipse.scout.rt.client.services.ClientServiceFactory"; // NO_UCD
  String ConfigProperty = "org.eclipse.scout.commons.annotations.ConfigProperty"; // NO_UCD
  String ConfigOperation = "org.eclipse.scout.commons.annotations.ConfigOperation"; // NO_UCD
  String DataModelConstants = "org.eclipse.scout.rt.shared.data.model.DataModelConstants"; // NO_UCD
  String DefaultOutlineTableForm = "org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm"; // NO_UCD
  String DefaultOutlineTreeForm = "org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTreeForm"; // NO_UCD
  String DefaultServiceFactory = "org.eclipse.scout.service.DefaultServiceFactory"; // NO_UCD
  String CopyWidthsOfColumnsMenu = "org.eclipse.scout.rt.client.ui.basic.table.menus.CopyWidthsOfColumnsMenu"; // NO_UCD
  String LocalLookupCall = "org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall"; // NO_UCD
  String CodeLookupCall = "org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall"; // NO_UCD
  String Order = "org.eclipse.scout.commons.annotations.Order"; // NO_UCD
  String Ranking = "org.eclipse.scout.commons.annotations.Priority"; // NO_UCD
  String SearchFilter = "org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter"; // NO_UCD
  String ServerServiceFactory = "org.eclipse.scout.rt.server.services.ServerServiceFactory"; // NO_UCD
  String ProcessingException = "org.eclipse.scout.commons.exception.ProcessingException"; // NO_UCD
  String ValidationRule = "org.eclipse.scout.rt.shared.data.form.ValidationRule"; // NO_UCD
  String InputValidation = "org.eclipse.scout.rt.shared.validate.InputValidation"; // NO_UCD
  String VetoException = "org.eclipse.scout.commons.exception.VetoException"; // NO_UCD
  String ResourceServlet = "org.eclipse.scout.rt.server.ResourceServlet"; // NO_UCD
  String UserAgentUtility = "org.eclipse.scout.rt.shared.ui.UserAgentUtility"; // NO_UCD
  String CollectionUtility = "org.eclipse.scout.commons.CollectionUtility"; // NO_UCD
  String TableMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TableMenuType"; // NO_UCD
  String TreeMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType"; // NO_UCD
  String ValueFieldMenuType = "org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType"; // NO_UCD

  String AbstractExtensionChain = "org.eclipse.scout.rt.shared.extension.AbstractExtensionChain"; // NO_UCD
  String AbstractFormData = "org.eclipse.scout.rt.shared.data.form.AbstractFormData"; // NO_UCD
  String AbstractFormFieldData = "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData"; // NO_UCD
  String AbstractPropertyData = "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData"; // NO_UCD
  String AbstractTableFieldData = "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData"; // NO_UCD
  String AbstractTableFieldBeanData = "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData"; // NO_UCD
  String AbstractTableRowData = "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData"; // NO_UCD
  String AbstractTablePageData = "org.eclipse.scout.rt.shared.data.page.AbstractTablePageData"; // NO_UCD

  String AbstractCodeTypeWithGeneric = "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric"; // NO_UCD
  String AbstractComposerField_Tree = "org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField.Tree"; // NO_UCD
  String AbstractIcons = "org.eclipse.scout.rt.shared.AbstractIcons"; // NO_UCD
  String AbstractLookupService = "org.eclipse.scout.rt.server.services.lookup.AbstractLookupService"; // NO_UCD
  String AbstractSqlLookupService = "org.eclipse.scout.rt.server.services.lookup.AbstractSqlLookupService"; // NO_UCD
  String AbstractDynamicNlsTextProviderService = "org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService"; // NO_UCD

  String AbstractOutlineViewButton = "org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton"; // NO_UCD
  String AbstractCancelButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton"; // NO_UCD
  String AbstractOkButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton"; // NO_UCD
  String AbstractResetButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractResetButton"; // NO_UCD
  String AbstractSearchButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractSearchButton"; // NO_UCD
  String AbstractCalendarField = "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField"; // NO_UCD
  String AbstractPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable"; // NO_UCD
  String AbstractWizardStep = "org.eclipse.scout.rt.client.ui.wizard.AbstractWizardStep";// NO_UCD
  String AbstractPlannerField = "org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField";// NO_UCD
  String AbstractActivityMap = "org.eclipse.scout.rt.client.ui.basic.activitymap.AbstractActivityMap";// NO_UCD
  String AbstractTableField = "org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField";// NO_UCD
  String AbstractTabBox = "org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox";// NO_UCD
  String AbstractTreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField"; // NO_UCD
  String AbstractTreeBox = "org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox"; // NO_UCD
  String AbstractGroupBox = "org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox"; // NO_UCD

  String IAccessControlService = "org.eclipse.scout.rt.shared.services.common.security.IAccessControlService";
  String IValidationStrategy = "org.eclipse.scout.rt.shared.validate.IValidationStrategy"; // NO_UCD
  String IActivityMap = "org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap"; // NO_UCD
  String IBigDecimalField = "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField"; // NO_UCD
  String IBigIntegerField = "org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.IBigIntegerField"; // NO_UCD
  String IBookmarkStorageService = "org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService"; // NO_UCD
  String IBooleanField = "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField"; // NO_UCD
  String IButton = "org.eclipse.scout.rt.client.ui.form.fields.button.IButton"; // NO_UCD
  String IButtonExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.button.IButtonExtension"; // NO_UCD
  String ICalendar = "org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar"; // NO_UCD
  String ICalendarField = "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField"; // NO_UCD
  String ICalendarItem = "org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem"; // NO_UCD
  String ICalendarItemProvider = "org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider"; // NO_UCD
  String ICalendarItemProviderExtension = "org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.ICalendarItemProviderExtension"; // NO_UCD
  String ICalendarService = "org.eclipse.scout.rt.shared.services.common.calendar.ICalendarService"; // NO_UCD
  String IChartBox = "org.eclipse.scout.rt.client.ui.form.fields.chartbox.IChartBox"; // NO_UCD
  String ICheckBox = "org.eclipse.scout.rt.client.ui.form.fields.checkbox.ICheckBox"; // NO_UCD
  String IClientSession = "org.eclipse.scout.rt.client.IClientSession"; // NO_UCD
  String ICode = "org.eclipse.scout.rt.shared.services.common.code.ICode"; // NO_UCD
  String ICodeType = "org.eclipse.scout.rt.shared.services.common.code.ICodeType"; // NO_UCD
  String IColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn"; // NO_UCD
  String IComposerField = "org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField"; // NO_UCD
  String ICompositeField = "org.eclipse.scout.rt.client.ui.form.fields.ICompositeField"; // NO_UCD
  String ICompositeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension"; // NO_UCD
  String ICustomField = "org.eclipse.scout.rt.client.ui.form.fields.customfield.ICustomField"; // NO_UCD
  String IDataModelAttribute = "org.eclipse.scout.rt.shared.data.model.IDataModelAttribute";// NO_UCD
  String IDataModelEntity = "org.eclipse.scout.rt.shared.data.model.IDataModelEntity";// NO_UCD
  String IDateField = "org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField"; // NO_UCD
  String IDesktop = "org.eclipse.scout.rt.client.ui.desktop.IDesktop"; // NO_UCD
  String IDesktopExtension = "org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension"; // NO_UCD
  String IDesktopExtensionExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension"; // NO_UCD
  String IDoubleField = "org.eclipse.scout.rt.client.ui.form.fields.doublefield.IDoubleField"; // NO_UCD
  String IDNDSupport = "org.eclipse.scout.rt.client.ui.IDNDSupport"; // NO_UCD
  String IExtension = "org.eclipse.scout.rt.shared.extension.IExtension"; // NO_UCD
  String ICodeTypeExtension = "org.eclipse.scout.rt.shared.extension.services.common.code.ICodeTypeExtension"; // NO_UCD
  String IFileChooserField = "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField"; // NO_UCD
  String IForm = "org.eclipse.scout.rt.client.ui.form.IForm"; // NO_UCD
  String IFormExtension = "org.eclipse.scout.rt.client.extension.ui.form.IFormExtension"; // NO_UCD
  String ICodeExtension = "org.eclipse.scout.rt.shared.extension.services.common.code.ICodeExtension"; // NO_UCD
  String IFormField = "org.eclipse.scout.rt.client.ui.form.fields.IFormField"; // NO_UCD
  String IFormFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension"; // NO_UCD
  String IFormHandler = "org.eclipse.scout.rt.client.ui.form.IFormHandler"; // NO_UCD
  String IGroupBox = "org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox"; // NO_UCD
  String IGroupBoxBodyGrid = "org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid"; // NO_UCD
  String IHtmlField = "org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField"; // NO_UCD
  String IImageField = "org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField"; // NO_UCD
  String IImageFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.IImageFieldExtension"; // NO_UCD
  String IIntegerField = "org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField"; // NO_UCD
  String IKeyStroke = "org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke"; // NO_UCD
  String ILabelField = "org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField"; // NO_UCD
  String IListBox = "org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox"; // NO_UCD
  String ILongField = "org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField"; // NO_UCD
  String ILookupCall = "org.eclipse.scout.rt.shared.services.lookup.ILookupCall"; // NO_UCD
  String ILookupRow = "org.eclipse.scout.rt.shared.services.lookup.ILookupRow"; // NO_UCD
  String ILookupService = "org.eclipse.scout.rt.shared.services.lookup.ILookupService"; // NO_UCD
  String IMailField = "org.eclipse.scout.rt.client.ui.form.fields.mailfield.IMailField"; // NO_UCD
  String ITextProviderService = "org.eclipse.scout.rt.shared.services.common.text.ITextProviderService"; // NO_UCD
  String IMenu = "org.eclipse.scout.rt.client.ui.action.menu.IMenu"; // NO_UCD
  String IMenuExtension = "org.eclipse.scout.rt.client.extension.ui.action.menu.IMenuExtension"; // NO_UCD
  String IMenuType = "org.eclipse.scout.rt.client.ui.action.menu.IMenuType"; // NO_UCD
  String IOutline = "org.eclipse.scout.rt.client.ui.desktop.outline.IOutline"; // NO_UCD
  String IPage = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage"; // NO_UCD
  String IPageWithNodes = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes"; // NO_UCD
  String IPageWithNodesExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithNodesExtension"; // NO_UCD
  String IPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable"; // NO_UCD
  String IPlannerField = "org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField"; // NO_UCD
  String IPropertyObserver = "org.eclipse.scout.commons.beans.IPropertyObserver"; // NO_UCD
  String IRadioButton = "org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton"; // NO_UCD
  String IRadioButtonGroup = "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup"; // NO_UCD
  String IScoutLogger = "org.eclipse.scout.commons.logger.IScoutLogger"; // NO_UCD
  String ISearchForm = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm"; // NO_UCD
  String ISequenceBox = "org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox"; // NO_UCD
  String IServerSession = "org.eclipse.scout.rt.server.IServerSession"; // NO_UCD
  String IService = "org.eclipse.scout.service.IService"; // NO_UCD
  String ISmartField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField"; // NO_UCD
  String IProposalField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField"; // NO_UCD
  String ISMTPService = "org.eclipse.scout.rt.server.services.common.smtp.ISMTPService"; // NO_UCD
  String ISplitBox = "org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox"; // NO_UCD
  String ISqlService = "org.eclipse.scout.rt.server.services.common.jdbc.ISqlService"; // NO_UCD
  String IStringField = "org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField"; // NO_UCD
  String ISwingEnvironment = "org.eclipse.scout.rt.ui.swing.ISwingEnvironment"; // NO_UCD
  String ITabBox = "org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox"; // NO_UCD
  String ITable = "org.eclipse.scout.rt.client.ui.basic.table.ITable"; // NO_UCD
  String ITableExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension"; // NO_UCD
  String ITableRow = "org.eclipse.scout.rt.client.ui.basic.table.ITableRow"; // NO_UCD
  String ITableField = "org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField"; // NO_UCD
  String IToolButton = "org.eclipse.scout.rt.client.ui.action.tool.IToolButton"; // NO_UCD
  String ITree = "org.eclipse.scout.rt.client.ui.basic.tree.ITree"; // NO_UCD
  String ITreeExtension = "org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension"; // NO_UCD
  String ITreeBox = "org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox"; // NO_UCD
  String ITreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField"; // NO_UCD
  String ITreeNode = "org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode"; // NO_UCD
  String ITreeNodeExtension = "org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension"; // NO_UCD
  String ITypeWithClassId = "org.eclipse.scout.commons.ITypeWithClassId"; // NO_UCD
  String IValueField = "org.eclipse.scout.rt.client.ui.form.fields.IValueField"; // NO_UCD
  String IValueFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension"; // NO_UCD
  String IViewButton = "org.eclipse.scout.rt.client.ui.action.view.IViewButton"; // NO_UCD
  String IWizard = "org.eclipse.scout.rt.client.ui.wizard.IWizard"; // NO_UCD
  String IWizardStep = "org.eclipse.scout.rt.client.ui.wizard.IWizardStep"; // NO_UCD
  String IBigDecimalColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn"; // NO_UCD
  String IBigIntegerColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IBigIntegerColumn"; // NO_UCD
  String IBooleanColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn"; // NO_UCD
  String IDateColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn"; // NO_UCD
  String IDoubleColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IDoubleColumn"; // NO_UCD
  String IIntegerColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn"; // NO_UCD
  String ILongColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn"; // NO_UCD
  String ISmartColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn"; // NO_UCD
  String IStringColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn"; // NO_UCD

//CHECKSTYLE:ON
}
