/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s;

/**
 * Classes indices and values used in the scout runtime.
 */
@SuppressWarnings("squid:S00115")
public interface IScoutRuntimeTypes {

  // type parameter positions
  int TYPE_PARAM_EXTENSION__OWNER = 0;
  int TYPE_PARAM_COLUMN__VALUE_TYPE = 0;
  int TYPE_PARAM_CODETYPE__CODE_ID = 1;
  int TYPE_PARAM_CODETYPE__CODE_TYPE_ID = 0;
  int TYPE_PARAM_VALUEFIELD__VALUE = 0;
  int TYPE_PARAM_LOOKUP_SERVICE_KEY_TYPE = 0;

  // annotations
  String ApplicationScoped = "org.eclipse.scout.rt.platform.ApplicationScoped";
  String Authentication = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication";
  String BeanMock = "org.eclipse.scout.rt.testing.platform.mock.BeanMock";
  String Before = "org.junit.Before";
  String ClassId = "org.eclipse.scout.rt.platform.classid.ClassId";
  String Clazz = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz";
  String ColumnData = "org.eclipse.scout.rt.client.dto.ColumnData";
  String ConfigOperation = "org.eclipse.scout.rt.platform.annotations.ConfigOperation";
  String ConfigProperty = "org.eclipse.scout.rt.platform.annotations.ConfigProperty";
  String Data = "org.eclipse.scout.rt.client.dto.Data";
  String DtoRelevant = "org.eclipse.scout.rt.platform.annotations.DtoRelevant";
  String Extends = "org.eclipse.scout.rt.platform.extension.Extends";
  String FormData = "org.eclipse.scout.rt.client.dto.FormData";
  String Handler = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler";
  String Order = "org.eclipse.scout.rt.platform.Order";
  String PageData = "org.eclipse.scout.rt.client.dto.PageData";
  String Replace = "org.eclipse.scout.rt.platform.Replace";
  String RunWith = "org.junit.runner.RunWith";
  String RunWithClientSession = "org.eclipse.scout.rt.testing.client.runner.RunWithClientSession";
  String RunWithServerSession = "org.eclipse.scout.rt.testing.server.runner.RunWithServerSession";
  String RunWithSubject = "org.eclipse.scout.rt.testing.platform.runner.RunWithSubject";
  String Test = "org.junit.Test";
  String TunnelToServer = "org.eclipse.scout.rt.shared.TunnelToServer";
  String WebServiceEntryPoint = "org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint";

  // abstract implementations
  String AbstractAccordion = "org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion";
  String AbstractAccordionField = "org.eclipse.scout.rt.client.ui.form.fields.accordionfield.AbstractAccordionField";
  String AbstractActionNode = "org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode";
  String AbstractBigDecimalField = "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField";
  String AbstractBooleanField = "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField";
  String AbstractBrowserField = "org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField";
  String AbstractButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton";
  String AbstractCalendar = "org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar";
  String AbstractCalendarField = "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField";
  String AbstractCalendarItemProvider = "org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider";
  String AbstractCancelButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton";
  String AbstractCode = "org.eclipse.scout.rt.shared.services.common.code.AbstractCode";
  String AbstractCodeType = "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType";
  String AbstractCodeTypeWithGeneric = "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric";
  String AbstractComposerField = "org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField";
  String AbstractCompositeField = "org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField";
  String AbstractDataModel = "org.eclipse.scout.rt.shared.data.model.AbstractDataModel";
  String AbstractDataModelEntity = "org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity";
  String AbstractDateField = "org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField";
  String AbstractDesktop = "org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop";
  String AbstractDesktopExtension = "org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension";
  String AbstractDynamicNlsTextProviderService = "org.eclipse.scout.rt.platform.text.AbstractDynamicNlsTextProviderService";
  String AbstractExtension = "org.eclipse.scout.rt.shared.extension.AbstractExtension";
  String AbstractFileChooserButton = "org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.AbstractFileChooserButton";
  String AbstractFileChooserField = "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField";
  String AbstractForm = "org.eclipse.scout.rt.client.ui.form.AbstractForm";
  String AbstractFormData = "org.eclipse.scout.rt.shared.data.form.AbstractFormData";
  String AbstractFormField = "org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField";
  String AbstractFormFieldData = "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData";
  String AbstractFormHandler = "org.eclipse.scout.rt.client.ui.form.AbstractFormHandler";
  String AbstractGroup = "org.eclipse.scout.rt.client.ui.group.AbstractGroup";
  String AbstractGroupBox = "org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox";
  String AbstractHtmlField = "org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField";
  String AbstractImageField = "org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField";
  String AbstractKeyStroke = "org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke";
  String AbstractLabelField = "org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField";
  String AbstractListBox = "org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox";
  String AbstractLongField = "org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField";
  String AbstractLookupService = "org.eclipse.scout.rt.server.services.lookup.AbstractLookupService";
  String AbstractMenu = "org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu";
  String AbstractMode = "org.eclipse.scout.rt.client.ui.form.fields.mode.AbstractMode";
  String AbstractModeSelectorField = "org.eclipse.scout.rt.client.ui.form.fields.modeselector.AbstractModeSelectorField";
  String AbstractOkButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton";
  String AbstractPage = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage";
  String AbstractPageWithNodes = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes";
  String AbstractPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable";
  String AbstractPermission = "org.eclipse.scout.rt.security.AbstractPermission";
  String AbstractPropertyData = "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData";
  String AbstractProposalField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField";
  String AbstractRadioButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton";
  String AbstractRadioButtonGroup = "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup";
  String AbstractSequenceBox = "org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox";
  String AbstractSmartField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField";
  String AbstractStringColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn";
  String AbstractStringConfigProperty = "org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty";
  String AbstractStringField = "org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField";
  String AbstractTabBox = "org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox";
  String AbstractTable = "org.eclipse.scout.rt.client.ui.basic.table.AbstractTable";
  String AbstractTableField = "org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField";
  String AbstractTableFieldBeanData = "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData";
  String AbstractTablePageData = "org.eclipse.scout.rt.shared.data.page.AbstractTablePageData";
  String AbstractTableRowData = "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData";
  String AbstractTagField = "org.eclipse.scout.rt.client.ui.form.fields.tagfield.AbstractTagField";
  String AbstractTile = "org.eclipse.scout.rt.client.ui.tile.AbstractTile";
  String AbstractTileField = "org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField";
  String AbstractTileGrid = "org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid";
  String AbstractTree = "org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree";
  String AbstractTreeBox = "org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox";
  String AbstractTreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField";
  String AbstractTreeNode = "org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode";
  String AbstractValueField = "org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField";
  String AbstractValueFieldData = "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData";
  String AbstractWebServiceClient = "org.eclipse.scout.rt.server.jaxws.consumer.AbstractWebServiceClient";
  String AbstractWizard = "org.eclipse.scout.rt.client.ui.wizard.AbstractWizard";

  // interfaces
  String IAccordion = "org.eclipse.scout.rt.client.ui.accordion.IAccordion";
  String IAccordionField = "org.eclipse.scout.rt.client.ui.form.fields.accordionfield.IAccordionField";
  String IAction = "org.eclipse.scout.rt.client.ui.action.IAction";
  String IActionNode = "org.eclipse.scout.rt.client.ui.action.tree.IActionNode";
  String IBigDecimalField = "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField";
  String IBooleanField = "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField";
  String IBrowserField = "org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField";
  String IButton = "org.eclipse.scout.rt.client.ui.form.fields.button.IButton";
  String ICalendar = "org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar";
  String ICalendarField = "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField";
  String ICalendarItemProvider = "org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider";
  String IClientSession = "org.eclipse.scout.rt.client.IClientSession";
  String ICode = "org.eclipse.scout.rt.shared.services.common.code.ICode";
  String ICodeType = "org.eclipse.scout.rt.shared.services.common.code.ICodeType";
  String IColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn";
  String ICompositeField = "org.eclipse.scout.rt.client.ui.form.fields.ICompositeField";
  String ICompositeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension";
  String IContextMenuOwner = "org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner";
  String IDataModelAttribute = "org.eclipse.scout.rt.shared.data.model.IDataModelAttribute";
  String IDataModelEntity = "org.eclipse.scout.rt.shared.data.model.IDataModelEntity";
  String IDataObject = "org.eclipse.scout.rt.dataobject.IDataObject";
  String IDateField = "org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField";
  String IDesktop = "org.eclipse.scout.rt.client.ui.desktop.IDesktop";
  String IDesktopExtension = "org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension";
  String IExtension = "org.eclipse.scout.rt.shared.extension.IExtension";
  String IFileChooserButton = "org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.IFileChooserButton";
  String IFileChooserField = "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField";
  String IForm = "org.eclipse.scout.rt.client.ui.form.IForm";
  String IFormExtension = "org.eclipse.scout.rt.client.extension.ui.form.IFormExtension";
  String IFormField = "org.eclipse.scout.rt.client.ui.form.fields.IFormField";
  String IFormFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension";
  String IFormFieldMenu = "org.eclipse.scout.rt.client.ui.action.menu.form.fields.IFormFieldMenu";
  String IFormHandler = "org.eclipse.scout.rt.client.ui.form.IFormHandler";
  String IGroup = "org.eclipse.scout.rt.client.ui.group.IGroup";
  String IGroupBox = "org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox";
  String IHtmlField = "org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField";
  String IImageField = "org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField";
  String IKeyStroke = "org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke";
  String ILabelField = "org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField";
  String IListBox = "org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox";
  String ILongField = "org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField";
  String ILookupCall = "org.eclipse.scout.rt.shared.services.lookup.ILookupCall";
  String ILookupRow = "org.eclipse.scout.rt.shared.services.lookup.ILookupRow";
  String ILookupService = "org.eclipse.scout.rt.shared.services.lookup.ILookupService";
  String IMenu = "org.eclipse.scout.rt.client.ui.action.menu.IMenu";
  String IMenuType = "org.eclipse.scout.rt.client.ui.action.menu.IMenuType";
  String IMode = "org.eclipse.scout.rt.client.ui.form.fields.mode.IMode";
  String IModeSelectorField = "org.eclipse.scout.rt.client.ui.form.fields.modeselector.IModeSelectorField";
  String IOrdered = "org.eclipse.scout.rt.platform.IOrdered";
  String IOutline = "org.eclipse.scout.rt.client.ui.desktop.outline.IOutline";
  String IPage = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage";
  String IPageWithNodes = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes";
  String IPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable";
  String IPageWithTableExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithTableExtension";
  String IPrettyPrintDataObjectMapper = "org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper";
  String IProposalField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField";
  String IRadioButton = "org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton";
  String IRadioButtonGroup = "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup";
  String ISequenceBox = "org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox";
  String IServerSession = "org.eclipse.scout.rt.server.IServerSession";
  String IService = "org.eclipse.scout.rt.platform.service.IService";
  String ISession = "org.eclipse.scout.rt.shared.ISession";
  String ISmartField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField";
  String IStringField = "org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField";
  String ITabBox = "org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox";
  String ITable = "org.eclipse.scout.rt.client.ui.basic.table.ITable";
  String ITableControl = "org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl";
  String ITableExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension";
  String ITableField = "org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField";
  String ITagField = "org.eclipse.scout.rt.client.ui.form.fields.tagfield.ITagField";
  String ITextProviderService = "org.eclipse.scout.rt.platform.text.ITextProviderService";
  String ITile = "org.eclipse.scout.rt.client.ui.tile.ITile";
  String ITileField = "org.eclipse.scout.rt.client.ui.form.fields.tilefield.ITileField";
  String ITileGrid = "org.eclipse.scout.rt.client.ui.tile.ITileGrid";
  String ITree = "org.eclipse.scout.rt.client.ui.basic.tree.ITree";
  String ITreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField";
  String ITreeNode = "org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode";
  String ITypeWithClassId = "org.eclipse.scout.rt.platform.classid.ITypeWithClassId";
  String IUuId = "org.eclipse.scout.rt.dataobject.id.IUuId";
  String IValueField = "org.eclipse.scout.rt.client.ui.form.fields.IValueField";
  String IViewButton = "org.eclipse.scout.rt.client.ui.action.view.IViewButton";
  String IWidget = "org.eclipse.scout.rt.client.ui.IWidget";
  String IWizard = "org.eclipse.scout.rt.client.ui.wizard.IWizard";
  String IWizardStep = "org.eclipse.scout.rt.client.ui.wizard.IWizardStep";

  // other runtime classes
  String ACCESS = "org.eclipse.scout.rt.security.ACCESS";
  String ArgumentMatchers = "org.mockito.ArgumentMatchers";
  String BEANS = "org.eclipse.scout.rt.platform.BEANS";
  String BasicAuthenticationMethod = "org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod";
  String BinaryResource = "org.eclipse.scout.rt.platform.resource.BinaryResource";
  String BooleanUtility = "org.eclipse.scout.rt.platform.util.BooleanUtility";
  String ClientTestRunner = "org.eclipse.scout.rt.testing.client.runner.ClientTestRunner";
  String CollectionUtility = "org.eclipse.scout.rt.platform.util.CollectionUtility";
  String ConfigFileCredentialVerifier = "org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier";
  String KeyStroke = "org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke";
  String LogHandler = "org.eclipse.scout.rt.server.jaxws.handler.LogHandler";
  String Logger = "org.slf4j.Logger";
  String LookupCall = "org.eclipse.scout.rt.shared.services.lookup.LookupCall";
  String Mockito = "org.mockito.Mockito";
  String NullClazz = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz$NullClazz";
  String OfficialVersion = "org.eclipse.scout.rt.shared.OfficialVersion";
  String SearchFilter = "org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter";
  String ServerTestRunner = "org.eclipse.scout.rt.testing.server.runner.ServerTestRunner";
  String TEXTS = "org.eclipse.scout.rt.platform.text.TEXTS";
  String TestEnvironmentClientSession = "org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession";
  String TriState = "org.eclipse.scout.rt.platform.util.TriState";
  String UiServlet = "org.eclipse.scout.rt.ui.html.UiServlet";
  String UiTextContributor = "org.eclipse.scout.rt.ui.html.UiTextContributor";
  String VetoException = "org.eclipse.scout.rt.platform.exception.VetoException";
  String WsConsumerCorrelationIdHandler = "org.eclipse.scout.rt.server.jaxws.handler.WsConsumerCorrelationIdHandler";
  String WsProviderCorrelationIdHandler = "org.eclipse.scout.rt.server.jaxws.handler.WsProviderCorrelationIdHandler";

  // Menu Types
  String CalendarMenuType = "org.eclipse.scout.rt.client.ui.action.menu.CalendarMenuType";
  String ImageFieldMenuType = "org.eclipse.scout.rt.client.ui.action.menu.ImageFieldMenuType";
  String TabBoxMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TabBoxMenuType";
  String TableMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TableMenuType";
  String TileGridMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TileGridMenuType";
  String TreeMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType";
  String ValueFieldMenuType = "org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType";

  String CalendarMenuType_CalendarComponent = "CalendarComponent";
  String CalendarMenuType_EmptySpace = "EmptySpace";
  String ImageFieldMenuType_Null = "Null";
  String ImageFieldMenuType_ImageId = "ImageId";
  String ImageFieldMenuType_ImageUrl = "ImageUrl";
  String ImageFieldMenuType_Image = "Image";
  String TabBoxMenuType_Header = "Header";
  String TableMenuType_EmptySpace = "EmptySpace";
  String TableMenuType_Header = "Header";
  String TableMenuType_MultiSelection = "MultiSelection";
  String TableMenuType_SingleSelection = "SingleSelection";
  String TileGridMenuType_EmptySpace = "EmptySpace";
  String TileGridMenuType_SingleSelection = "SingleSelection";
  String TileGridMenuType_MultiSelection = "MultiSelection";
  String TreeMenuType_EmptySpace = "EmptySpace";
  String TreeMenuType_MultiSelection = "MultiSelection";
  String TreeMenuType_SingleSelection = "SingleSelection";
  String TreeMenuType_Header = "Header";
  String ValueFieldMenuType_NotNull = "NotNull";
  String ValueFieldMenuType_Null = "Null";

  // extensions
  String AbstractAccordionFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AbstractAccordionFieldExtension";
  String AbstractActionExtension = "org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension";
  String AbstractActionNodeExtension = "org.eclipse.scout.rt.client.extension.ui.action.tree.AbstractActionNodeExtension";
  String AbstractButtonExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.button.AbstractButtonExtension";
  String AbstractCalendarExtension = "org.eclipse.scout.rt.client.extension.ui.basic.calendar.AbstractCalendarExtension";
  String AbstractCalendarFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.calendarfield.AbstractCalendarFieldExtension";
  String AbstractCalendarItemProviderExtension = "org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.AbstractCalendarItemProviderExtension";
  String AbstractCodeExtension = "org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeExtension";
  String AbstractCodeTypeWithGenericExtension = "org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeTypeWithGenericExtension";
  String AbstractComposerFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.composer.AbstractComposerFieldExtension";
  String AbstractCompositeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension";
  String AbstractDataModelEntityExtension = "org.eclipse.scout.rt.shared.extension.data.model.AbstractDataModelEntityExtension";
  String AbstractFormExtension = "org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension";
  String AbstractFormFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension";
  String AbstractGroupBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension";
  String AbstractGroupExtension = "org.eclipse.scout.rt.client.extension.ui.group.AbstractGroupExtension";
  String AbstractImageFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.AbstractImageFieldExtension";
  String AbstractListBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.AbstractListBoxExtension";
  String AbstractPageWithTableExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension";
  String AbstractRadioButtonGroupExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroupExtension";
  String AbstractTabBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.AbstractTabBoxExtension";
  String AbstractTableExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension";
  String AbstractTableFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.AbstractTableFieldExtension";
  String AbstractTileFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.AbstractTileFieldExtension";
  String AbstractTileGridExtension = "org.eclipse.scout.rt.client.extension.ui.tile.AbstractTileGridExtension";
  String AbstractTreeBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.AbstractTreeBoxExtension";
  String AbstractTreeExtension = "org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension";
  String AbstractTreeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.AbstractTreeFieldExtension";
  String AbstractTreeNodeExtension = "org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeNodeExtension";
  String AbstractValueFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension";
  String AbstractWizardExtension = "org.eclipse.scout.rt.client.extension.ui.wizard.AbstractWizardExtension";

  String WebService = "javax.jws.WebService";
  String WebServiceClient = "javax.xml.ws.WebServiceClient";
}
