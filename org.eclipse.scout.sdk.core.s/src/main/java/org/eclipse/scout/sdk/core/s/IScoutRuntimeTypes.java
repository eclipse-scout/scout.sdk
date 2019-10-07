/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
  String FormData = "org.eclipse.scout.rt.client.dto.FormData";
  String Order = "org.eclipse.scout.rt.platform.Order";
  String PageData = "org.eclipse.scout.rt.client.dto.PageData";
  String Data = "org.eclipse.scout.rt.client.dto.Data";
  String ClassId = "org.eclipse.scout.rt.platform.classid.ClassId";
  String DtoRelevant = "org.eclipse.scout.rt.platform.annotations.DtoRelevant";
  String Replace = "org.eclipse.scout.rt.platform.Replace";
  String ColumnData = "org.eclipse.scout.rt.client.dto.ColumnData";
  String Extends = "org.eclipse.scout.rt.platform.extension.Extends";
  String ConfigOperation = "org.eclipse.scout.rt.platform.annotations.ConfigOperation";
  String ConfigProperty = "org.eclipse.scout.rt.platform.annotations.ConfigProperty";
  String TunnelToServer = "org.eclipse.scout.rt.shared.TunnelToServer";
  String ApplicationScoped = "org.eclipse.scout.rt.platform.ApplicationScoped";
  String RunWithSubject = "org.eclipse.scout.rt.testing.platform.runner.RunWithSubject";
  String RunWithClientSession = "org.eclipse.scout.rt.testing.client.runner.RunWithClientSession";
  String RunWithServerSession = "org.eclipse.scout.rt.testing.server.runner.RunWithServerSession";
  String WebServiceEntryPoint = "org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint";
  String Authentication = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication";
  String Handler = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler";
  String Clazz = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz";
  String Test = "org.junit.Test";
  String BeanMock = "org.eclipse.scout.rt.testing.platform.mock.BeanMock";
  String RunWith = "org.junit.runner.RunWith";
  String Before = "org.junit.Before";

  // abstract implementations
  String AbstractActionNode = "org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode";
  String AbstractBigDecimalField = "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField";
  String AbstractBooleanField = "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField";
  String AbstractButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton";
  String AbstractCalendar = "org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar";
  String AbstractCalendarField = "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField";
  String AbstractCalendarItemProvider = "org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider";
  String AbstractCancelButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton";
  String AbstractCode = "org.eclipse.scout.rt.shared.services.common.code.AbstractCode";
  String AbstractCodeType = "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType";
  String AbstractCodeTypeWithGeneric = "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric";
  String AbstractColumn = "org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn";
  String AbstractComposerField = "org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField";
  String AbstractCompositeField = "org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField";
  String AbstractDataModel = "org.eclipse.scout.rt.shared.data.model.AbstractDataModel";
  String AbstractDataModelEntity = "org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity";
  String AbstractDateField = "org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField";
  String AbstractDesktop = "org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop";
  String AbstractDesktopExtension = "org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension";
  String AbstractDynamicNlsTextProviderService = "org.eclipse.scout.rt.platform.text.AbstractDynamicNlsTextProviderService";
  String AbstractExtension = "org.eclipse.scout.rt.shared.extension.AbstractExtension";
  String AbstractFileChooserField = "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField";
  String AbstractForm = "org.eclipse.scout.rt.client.ui.form.AbstractForm";
  String AbstractFormField = "org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField";
  String AbstractFormFieldData = "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData";
  String AbstractFormData = "org.eclipse.scout.rt.shared.data.form.AbstractFormData";
  String AbstractFormHandler = "org.eclipse.scout.rt.client.ui.form.AbstractFormHandler";
  String AbstractGroupBox = "org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox";
  String AbstractHtmlField = "org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField";
  String AbstractImageField = "org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField";
  String AbstractKeyStroke = "org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke";
  String AbstractLabelField = "org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField";
  String AbstractListBox = "org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox";
  String AbstractLongField = "org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField";
  String AbstractLookupService = "org.eclipse.scout.rt.server.services.lookup.AbstractLookupService";
  String AbstractMenu = "org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu";
  String AbstractOkButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton";
  String AbstractPage = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage";
  String AbstractPageWithNodes = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes";
  String AbstractPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable";
  String AbstractPlanner = "org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner";
  String AbstractPlannerField = "org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField";
  String AbstractPropertyData = "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData";
  String AbstractProposalField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField";
  String AbstractRadioButtonGroup = "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup";
  String AbstractRadioButton = "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton";
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
  String AbstractValueFieldData = "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData";
  String AbstractTree = "org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree";
  String AbstractTreeBox = "org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox";
  String AbstractTreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField";
  String AbstractTreeNode = "org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode";
  String AbstractValueField = "org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField";
  String AbstractWebServiceClient = "org.eclipse.scout.rt.server.jaxws.consumer.AbstractWebServiceClient";
  String AbstractWizard = "org.eclipse.scout.rt.client.ui.wizard.AbstractWizard";

  // interfaces
  String IAction = "org.eclipse.scout.rt.client.ui.action.IAction";
  String IActionNode = "org.eclipse.scout.rt.client.ui.action.tree.IActionNode";
  String IBigDecimalField = "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField";
  String IBooleanField = "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField";
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
  String IDateField = "org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField";
  String IDesktop = "org.eclipse.scout.rt.client.ui.desktop.IDesktop";
  String IDesktopExtension = "org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension";
  String IExtension = "org.eclipse.scout.rt.shared.extension.IExtension";
  String IFileChooserField = "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField";
  String IForm = "org.eclipse.scout.rt.client.ui.form.IForm";
  String IFormExtension = "org.eclipse.scout.rt.client.extension.ui.form.IFormExtension";
  String IFormField = "org.eclipse.scout.rt.client.ui.form.fields.IFormField";
  String IFormFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension";
  String IFormFieldMenu = "org.eclipse.scout.rt.client.ui.action.menu.form.fields.IFormFieldMenu";
  String IFormHandler = "org.eclipse.scout.rt.client.ui.form.IFormHandler";
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
  String IOutline = "org.eclipse.scout.rt.client.ui.desktop.outline.IOutline";
  String IOrdered = "org.eclipse.scout.rt.platform.IOrdered";
  String IPage = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage";
  String IPageWithNodes = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes";
  String IPageWithTable = "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable";
  String IPageWithTableExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithTableExtension";
  String IPlanner = "org.eclipse.scout.rt.client.ui.basic.planner.IPlanner";
  String IPlannerField = "org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField";
  String IProposalField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField";
  String IPrettyPrintDataObjectMapper = "org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper";
  String IRadioButtonGroup = "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup";
  String IRadioButton = "org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton";
  String ISequenceBox = "org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox";
  String ISession = "org.eclipse.scout.rt.shared.ISession";
  String IServerSession = "org.eclipse.scout.rt.server.IServerSession";
  String IService = "org.eclipse.scout.rt.platform.service.IService";
  String ISmartField = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField";
  String IStringField = "org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField";
  String ITabBox = "org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox";
  String ITable = "org.eclipse.scout.rt.client.ui.basic.table.ITable";
  String ITableControl = "org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl";
  String ITableExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension";
  String ITableField = "org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField";
  String ITree = "org.eclipse.scout.rt.client.ui.basic.tree.ITree";
  String ITypeWithClassId = "org.eclipse.scout.rt.platform.classid.ITypeWithClassId";
  String DynamicNls = "org.eclipse.scout.rt.platform.nls.DynamicNls";
  String ITreeField = "org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField";
  String IValueField = "org.eclipse.scout.rt.client.ui.form.fields.IValueField";
  String IViewButton = "org.eclipse.scout.rt.client.ui.action.view.IViewButton";
  String IWizard = "org.eclipse.scout.rt.client.ui.wizard.IWizard";
  String IWizardStep = "org.eclipse.scout.rt.client.ui.wizard.IWizardStep";
  String IDataObject = "org.eclipse.scout.rt.dataobject.IDataObject";
  String IUuId = "org.eclipse.scout.rt.dataobject.id.IUuId";

  // other runtime classes
  String ACCESS = "org.eclipse.scout.rt.security.ACCESS";
  String BEANS = "org.eclipse.scout.rt.platform.BEANS";
  String TEXTS = "org.eclipse.scout.rt.platform.text.TEXTS";
  String Logger = "org.slf4j.Logger";
  String CollectionUtility = "org.eclipse.scout.rt.platform.util.CollectionUtility";
  String VetoException = "org.eclipse.scout.rt.platform.exception.VetoException";
  String SearchFilter = "org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter";
  String LookupCall = "org.eclipse.scout.rt.shared.services.lookup.LookupCall";
  String TestEnvironmentClientSession = "org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession";
  String ClientTestRunner = "org.eclipse.scout.rt.testing.client.runner.ClientTestRunner";
  String ServerTestRunner = "org.eclipse.scout.rt.testing.server.runner.ServerTestRunner";
  String UiServlet = "org.eclipse.scout.rt.ui.html.UiServlet";
  String TriState = "org.eclipse.scout.rt.platform.util.TriState";
  String LogHandler = "org.eclipse.scout.rt.server.jaxws.handler.LogHandler";
  String WsConsumerCorrelationIdHandler = "org.eclipse.scout.rt.server.jaxws.handler.WsConsumerCorrelationIdHandler";
  String WsProviderCorrelationIdHandler = "org.eclipse.scout.rt.server.jaxws.handler.WsProviderCorrelationIdHandler";
  String Mockito = "org.mockito.Mockito";
  String ArgumentMatchers = "org.mockito.ArgumentMatchers";
  String BasicAuthenticationMethod = "org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod";
  String ConfigFileCredentialVerifier = "org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier";
  String NullClazz = "org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz$NullClazz";
  String BinaryResource = "org.eclipse.scout.rt.platform.resource.BinaryResource";
  String BooleanUtility = "org.eclipse.scout.rt.platform.util.BooleanUtility";
  String JaxWsMetroSpecifics = "org.eclipse.scout.rt.server.jaxws.implementor.JaxWsMetroSpecifics";
  String OfficialVersion = "org.eclipse.scout.rt.shared.OfficialVersion";

  // Menu Types
  String TableMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TableMenuType";
  String CalendarMenuType = "org.eclipse.scout.rt.client.ui.action.menu.CalendarMenuType";
  String TreeMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType";
  String TabBoxMenuType = "org.eclipse.scout.rt.client.ui.action.menu.TabBoxMenuType";
  String ValueFieldMenuType = "org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType";

  String TableMenuType_SingleSelection = "SingleSelection";
  String TableMenuType_MultiSelection = "MultiSelection";
  String TableMenuType_EmptySpace = "EmptySpace";
  String TableMenuType_Header = "Header";
  String ValueFieldMenuType_Null = "Null";
  String ValueFieldMenuType_NotNull = "NotNull";
  String TreeMenuType_EmptySpace = "EmptySpace";
  String TreeMenuType_SingleSelection = "SingleSelection";
  String TreeMenuType_MultiSelection = "MultiSelection";
  String TabBoxMenuType_Header = "Header";
  String CalendarMenuType_EmptySpace = "EmptySpace";
  String CalendarMenuType_CalendarComponent = "CalendarComponent";

  // extensions
  String AbstractActionNodeExtension = "org.eclipse.scout.rt.client.extension.ui.action.tree.AbstractActionNodeExtension";
  String AbstractActionExtension = "org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension";
  String AbstractCalendarItemProviderExtension = "org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.AbstractCalendarItemProviderExtension";
  String AbstractCalendarExtension = "org.eclipse.scout.rt.client.extension.ui.basic.calendar.AbstractCalendarExtension";
  String AbstractPlannerExtension = "org.eclipse.scout.rt.client.extension.ui.basic.planner.AbstractPlannerExtension";
  String AbstractTableExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension";
  String AbstractTreeExtension = "org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension";
  String AbstractTreeNodeExtension = "org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeNodeExtension";
  String AbstractPageWithTableExtension = "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension";
  String AbstractButtonExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.button.AbstractButtonExtension";
  String AbstractCalendarFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.calendarfield.AbstractCalendarFieldExtension";
  String AbstractComposerFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.composer.AbstractComposerFieldExtension";
  String AbstractGroupBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension";
  String AbstractImageFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.AbstractImageFieldExtension";
  String AbstractListBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.AbstractListBoxExtension";
  String AbstractPlannerFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.AbstractPlannerFieldExtension";
  String AbstractRadioButtonGroupExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroupExtension";
  String AbstractTabBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.AbstractTabBoxExtension";
  String AbstractTableFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.AbstractTableFieldExtension";
  String AbstractTreeBoxExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.AbstractTreeBoxExtension";
  String AbstractTreeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.AbstractTreeFieldExtension";
  String AbstractCompositeFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension";
  String AbstractFormFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension";
  String AbstractValueFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension";
  String AbstractFormExtension = "org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension";
  String AbstractWizardExtension = "org.eclipse.scout.rt.client.extension.ui.wizard.AbstractWizardExtension";
  String AbstractCodeTypeWithGenericExtension = "org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeTypeWithGenericExtension";
  String AbstractCodeExtension = "org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeExtension";
  String AbstractDataModelEntityExtension = "org.eclipse.scout.rt.shared.extension.data.model.AbstractDataModelEntityExtension";
  String AbstractKeyStrokeExtension = "org.eclipse.scout.rt.client.extension.ui.action.keystroke.AbstractKeyStrokeExtension";
  String AbstractBooleanFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.booleanfield.AbstractBooleanFieldExtension";
  String AbstractLabelFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield.AbstractLabelFieldExtension";
  String AbstractDateFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.AbstractDateFieldExtension";
  String AbstractStringFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension";
  String AbstractBigDecimalFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.bigdecimalfield.AbstractBigDecimalFieldExtension";
  String AbstractLongFieldExtension = "org.eclipse.scout.rt.client.extension.ui.form.fields.longfield.AbstractLongFieldExtension";
  String AbstractBooleanColumnExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractBooleanColumnExtension";
  String AbstractDateColumnExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractDateColumnExtension";
  String AbstractLongColumnExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractLongColumnExtension";
  String AbstractBigDecimalColumnExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractBigDecimalColumnExtension";
  String AbstractStringColumnExtension = "org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractStringColumnExtension";
  String AbstractFormHandlerExtension = "org.eclipse.scout.rt.client.extension.ui.form.AbstractFormHandlerExtension";
  String AbstractWizardStepExtension = "org.eclipse.scout.rt.client.extension.ui.wizard.AbstractWizardStepExtension";

  String WebService = "javax.jws.WebService";
  String WebServiceClient = "javax.xml.ws.WebServiceClient";
}
