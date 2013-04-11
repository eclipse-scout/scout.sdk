/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under The terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal;

/**
 * <h3>{@link SdkIcons}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 28.11.2010
 */
public interface SdkIcons {
//CHECKSTYLE:OFF
  String ToolAdd = "add.png";
  String ToolRemove = "remove.png";
  String ToolDelete = "delete.gif";
  String ToolRename = "rename.gif";
  String ToolEdit = "usereditor.gif";
  String ToolMagnifier = "magnifier.png";
  String ToolLoading = "tool_loading.gif";
  String ToolProgress = "progress_monitor.gif";
  String ToolDropdown = "down.gif";
  String ToolSynchronize = "synced.gif";
  String ToolSearch = "search.gif";
  String ToolRun = "run_exc.gif";
  String ToolDebug = "debug_exc.gif";
  String ToolStop = "stop.gif";
  String StatusInfo = "info.gif";
  String StatusWarning = "warning.gif";
  String StatusError = "error.gif";

  String Default = "default.gif";
  String VerticalTop = "vertical_top.gif";
  String VerticalCenter = "vertical_center.gif";
  String VerticalBottom = "vertical_bottom.gif";
  String HorizontalLeft = "horizontal_left.gif";
  String HorizontalCenter = "horizontal_center.gif";
  String HorizontalRight = "horizontal_right.gif";

  String LauncherServer = "serverLauncher.gif";
  String LauncherSwt = "swtLauncher.gif";
  String LauncherSwing = "swingLauncher.gif";

  String File = "file.gif";
  String FolderOpen = "folder_open.png";
  String Class = "class_obj.gif";
  String Interface = "innerinterface_obj.gif";
  String FieldPrivate = "field_private_obj.gif";
  String FieldProtected = "field_protected_obj.gif";
  String FieldPublic = "field_public_obj.gif";
  String Public = "public_co.gif";
  String Package = "package_obj.gif";
  String CheckboxYes = "output_yes.gif";
  /**
   * only cache image
   */
  String CheckboxYesDisabled = "output_yes_disabled";
  String CheckboxNo = "output_no.gif";
  /**
   * only cache image
   */
  String CheckboxNoDisabled = "output_no_disabled";
  String ButtonStyle = Default; // TODO
  String ContentAssist = "content_assist_cue.gif";
  String Separator = "type_separator.gif";

  String Button = "button.png";
  String ButtonAdd = "button_add.gif";
  String ButtonRemove = "button_remove.gif";
  String Buttons = "buttons.png";
  String ClientBundle = "clientBundle.png";
  String ClientBundleAdd = "clientBundle_add.png";
  String ClientBundleRemove = "clientBundle_remove.png";
  String ClientSession = "clientSession.png";
  String ClientSessionAdd = "clientSession_add.png";
  String ClientSessionRemove = "clientSession_remove.png";
  String Code = "code.png";
  String Codes = "codes.png";
  String CodeType = "codeType.png";
  String CodeTypes = "codeTypes.png";
  String CodeTypeAdd = "codeType_add.png";
  String CodeTypeRemove = "codeType_remove.png";
  String CodeAdd = "code_add.png";
  String CodeRemove = "code_remove.png";
  String Desktop = "desktop.png";
  String Form = "form.png";
  String FormField = "formField.png";
  String FormFieldAdd = "formField_add.png";
  String FormFieldFrom = "formField_from.png";
  String FormFieldRemove = "formField_remove.png";
  String FormFieldTo = "formField_to.png";
  String FormHandler = "formHandler.png";
  String FormHandlers = "formHandlers.png";
  String Forms = "forms.png";
  String FormAdd = "form_add.png";
  String FormRemove = "form_remove.png";
  String Groupbox = "groupbox.png";
  String GroupboxAdd = "groupbox_add.png";
  String GroupboxRemove = "groupbox_remove.png";
  String Icon = "icon.png";
  String Icons = "icons.png";
  String IconAdd = "icon_add.png";
  String IconRemove = "icon_remove.png";
  String Keystroke = "keystroke.png";
  String Keystrokes = "keystrokes.png";
  String KeystrokeAdd = "keystroke_add.png";
  String KeystrokeRemove = "keystroke_remove.png";
  String Library = "library.png";
  String Libraries = "libraries.png";
  String LibrariesAdd = "libraries_add.png";
  String LibrariesRemove = "libraries_remove.png";
  String LookupCall = "lookupCall.png";
  String LookupCalls = "lookupCalls.png";
  String LookupCallAdd = "lookupCall_add.png";
  String LookupCallRemove = "lookupCall_remove.png";
  String Mainbox = "mainbox.png";
  String Menu = "menu.png";
  String Menus = "menus.png";
  String MenuAdd = "menu_add.png";
  String MenuRemove = "menu_remove.png";
  String Outline = "outline.png";
  String Outlines = "outlines.png";
  String OutlineAdd = "outline_add.png";
  String OutlineRemove = "outline_remove.png";
  String Page = "page.png";
  String PageAdd = "page_add.png";
  String PageLink = "page.png"; // TODO: own image
  String PageRemove = "page_remove.png";
  String PageNode = "pageNode.png";
  String PageTable = "pageTable.png";
  String Pages = "pages.png";
  String Permission = "permission.png";
  String Permissions = "permissions.png";
  String PermissionAdd = "permission_add.png";
  String PermissionRemove = "permission_remove.png";
  String ScoutProject = "projectGroup.png";
  String ScoutProjectExport = "projectGroup_export.png";
  String ScoutProjectAdd = "projectGroup_add.png";
  String ScoutProjectRemove = "projectGroup_remove.png";
  String SearchForm = "searchForm.png";
  String SearchForms = "searchForms.png";
  String SearchFormAdd = "searchForm_add.png";
  String SearchFormRemove = "searchForm_remove.png";
  String ServerBundle = "serverBundle.png";
  String ServerBundleAdd = "serverBundle_add.png";
  String ServerBundleRemove = "serverBundle_remove.png";
  String ServerBundleExport = "serverBundle_export.png";
  String ServerSession = "serverSession.png";
  String ServerSessionAdd = "serverSession_add.png";
  String ServerSessionRemove = "serverSession_remove.png";
  String Service = "service.png";
  String ServiceLocator = "serviceLocator.png";
  String ServiceLocators = "serviceLocators.png";
  String ServiceLocatorAdd = "serviceLocator_add.png";
  String ServiceLocatorRemove = "serviceLocator_remove.png";
  String ServiceOperation = "serviceOperation.png";
  String ServiceOperationAdd = "serviceOperation_add.png";
  String ServiceOperationRemove = "serviceOperation_remove.png";
  String Services = "services.png";
  String ServiceAdd = "service_add.png";
  String ServiceRemove = "service_remove.png";
  String SharedBundle = "sharedBundle.png";
  String SharedBundleAdd = "sharedBundle_add.png";
  String SharedBundleRemove = "sharedBundle_remove.png";
  String SqlService = "sqlService.png";
  String SqlServiceAdd = "sqlService_add.png";
  String SqlServiceRemove = "sqlService_remove.png";
  String SwtBundle = "uiBundle.png";
  String UiBundleExport = "uiBundle_export.png";
  String SwtBundleAdd = "uiBundle_add.png";
  String SwtBundleRemove = "uiBundle_remove.png";
  String SwingBundle = "uiBundle.png";
  String SwingBundleAdd = "uiBundle_add.png";
  String SwingBundleRemove = "uiBundle_remove.png";

  String Tree = "tree.gif";
  String TreeAdd = "tree_add.gif";
  String TreeRemove = "tree_add.gif";
  String Table = "table.png";
  String TableColumn = "tableColumn.png";
  String TableColumnAdd = "tableColumn_add.png";
  String TableColumnRemove = "tableColumn_remove.png";
  String TableColumns = FolderOpen; // TODO
  String TableField = "tablefield.gif";
  String TableFieldAdd = "tablefield_add.gif";
  String TableFieldRemove = "tablefield_remove.gif";
  String TableAdd = "table_add.png";
  String TableRemove = "table_remove.png";
  String Text = "text.png";
  String Texts = "texts.png";
  String TextAdd = "text_add.png";
  String TextKey = "text_key.png";
  String TextRemove = "text_remove.png";
  String TextForeign = "text_foreign.png";
  String TreeField = "treefield.gif";
  String TreeFieldAdd = "treefield_add.gif";
  String TreeFieldRemove = "treefield_remove.gif";

  String Variable = "variable.png";
  String Variables = "variables.png";
  String VariableAdd = "variable_add.png";
  String VariableRemove = "variable_remove.png";
  String webBundle = "webBundle.png";
  String WebBundleAdd = "webBundle_add.png";
  String WebBundleRemove = "webBundle_remove.png";
  String Webservice = "webservice.png";
  String Webservices = "webservices.png";
  String WebserviceAdd = "webservice_add.png";
  String WebserviceRemove = "webservice_remove.png";
  String Wizard = "wizzard.png";
  String Wizards = "wizzards.png";
  String WizardStep = "wizzardStep.png";
  String WizardSteps = "wizzardSteps.png";
  String WizardStepAdd = "wizzardStep_add.png";
  String WizardStepRemove = "wizzardStep_remove.png";
  String WizardAdd = "wizzard_add.png";
  String WizardRemove = "wizzard_remove.png";

  String Templates = FolderOpen; // TODO
  String ComposerAttribute = Default; // TODO
  String ComposerAttributeAdd = ToolAdd; // TODO
  String ComposerAttributeRemove = ToolRemove; // TODO
  String ComposerAttributes = FolderOpen; // TODO
  String ComposerEntry = Default; // TODO
  String ComposerEntryAdd = ToolAdd; // TODO
  String ComposerEntryRemove = ToolRemove; // TODO
  String ComposerEntries = FolderOpen; // TODO
  String ComposerField = FormField; // TODO
  String ComposerFieldAdd = ToolAdd; // TODO
  String ComposerRemove = ToolRemove; // TODO
  String Tabbox = "tabbox.gif";
  String TabboxAdd = "tabbox_add.gif";
  String TabboxRemove = "tabbox_remove.gif";
  String TabboxTab = Groupbox; // TODO
  String TabboxTabAdd = ToolAdd; // TODO
  String TabboxTabRemove = ToolRemove; // TODO
  String Sequencebox = "sequencebox.gif";
  String SequenceboxAdd = "sequencebox_add.gif";
  String SequenceboxRemove = "sequencebox_remove.gif";
  String Radiobutton = "radiobutton.gif";
  String RadiobuttonAdd = "radiobutton_add.gif";
  String RadiobuttonRemove = "radiobutton_remove.gif";
  String RadiobuttonGroup = "radiobuttonGroup.gif";
  String RadiobuttonGroupAdd = "radiobuttonGroup_add.gif";
  String RadiobuttonGroupRemove = "radiobuttonGroup_remove.gif";
  String FormHandlerAdd = ToolAdd; //TODO "formHandler_add";
  String FormHandlerRemove = ToolRemove;//TODO "formHandler_remove";
  String Calendar = "calendar_small.gif";
  String CalendarAdd = ToolAdd;
  String CalendarRemove = ToolRemove;
  String CalendarItemProvider = Default; // TODO
  String CalendarItemProviderAdd = ToolAdd; // TODO
  String CalendarItemProviderRemove = ToolRemove; // TODO
  String CalendarItemProviders = FolderOpen; // TODO
  String FormTemplate = Default; // TODO
  String FormFieldTemplate = Default; // TODO
  String FormFieldTemplateAdd = ToolAdd; // TODO
  String FormFieldTemplateRemove = ToolRemove; // TODO
  String BigDecimalField = FormField; // TODO
  String BigDecimalFieldAdd = ToolAdd; // TODO
  String BigDecimalFieldRemove = ToolRemove; // TODO
  String BooleanField = FormField; // TODO
  String BooleanFieldAdd = ToolAdd; // TODO
  String BooleanFieldRemove = ToolRemove; // TODO
  String CalendarField = FormField; // TODO
  String CalendarFieldAdd = ToolAdd; // TODO
  String CalendarFieldRemove = ToolRemove; // TODO
  String CheckboxField = FormField; // TODO
  String CheckboxFieldAdd = ToolAdd; // TODO
  String CheckboxFieldRemove = ToolRemove; // TODO
  String CustomField = FormField; // TODO
  String CustomFieldAdd = ToolAdd; // TODO
  String CustomFieldRemove = ToolRemove; // TODO
  String DateField = "datefield.gif";
  String DateFieldAdd = "datefield_add.gif";
  String DateFieldRemove = "datefield_remove.gif";
  String DoubleField = "doublefield.gif";
  String DoubleFieldAdd = "doublefield_add.gif";
  String DoubleFieldRemove = "doublefield_remove.gif";
  String FileChooserField = "filechooserfield.gif";
  String FileChooserFieldAdd = "filechooserfield_add.gif";
  String FileChooserFieldRemove = "filechooserfield_remove.gif";
  String HtmlField = FormField; // TODO
  String HtmlFieldAdd = ToolAdd; // TODO
  String HtmlFieldRemove = ToolRemove; // TODO
  String ImageField = FormField; // TODO
  String ImageFieldAdd = ToolAdd; // TODO
  String ImageFieldRemove = ToolRemove; // TODO
  String IntegerField = "integerfield.gif";
  String IntegerFieldAdd = "integerfield_add.gif";
  String IntegerFieldRemove = "integerfield_remove.gif";
  String LabelField = FormField; // TODO
  String LabelFieldAdd = ToolAdd; // TODO
  String LabelFieldRemove = ToolRemove; // TODO
  String ListboxField = FormField; // TODO
  String ListboxFieldAdd = ToolAdd; // TODO
  String ListboxFieldRemove = ToolRemove; // TODO
  String LongField = FormField; // TODO
  String LongFieldAdd = ToolAdd; // TODO
  String LongFieldRemove = ToolRemove; // TODO
  String MailField = FormField; // TODO
  String MailFieldAdd = ToolAdd; // TODO
  String MailFieldRemove = ToolRemove; // TODO
  String PlannerField = FormField; // TODO
  String PlannerFieldAdd = ToolAdd; // TODO
  String PlannerFieldRemove = ToolRemove; // TODO
  String SmartField = "smartfield.gif";
  String SmartFieldAdd = "smartfield_add.gif";
  String SmartFieldRemove = "smartfield_remove.gif";
  String SplitBox = "splitbox.gif";
  String SplitBoxAdd = "splitbox_add.gif";
  String SplitBoxRemove = "splitbox_remove.gif";
  String StringField = "stringfield.gif";
  String StringFieldAdd = "stringfield_add.gif";
  String StringFieldRemove = "stringfield_remove.gif";
  String TimeField = FormField; // TODO
  String TimeFieldAdd = ToolAdd; // TODO
  String TimeFieldRemove = ToolRemove; // TODO
  String TreeBox = "treefield.gif"; // TODO
  String TreeBoxAdd = "treefield_add.gif"; // TODO
  String TreeBoxRemove = "treefield_remove.gif"; // TODO
  String UnknownField = FormField; // TODO
  String UnknownFieldAdd = ToolAdd; // TODO
  String UnknownFieldRemove = ToolRemove; // TODO

  String BundlePresentationGrouped = "presentation_grouped.gif";
  String BundlePresentationHierarchical = "presentation_hierarchical.gif";
  String BundlePresentationFlat = "presentation_flat.gif";
  String ScoutWorkingSet = "scoutWorkingSet.png";

  String BinaryDecorator = "binary_decoration.png";
//CHECKSTYLE:ON
}
