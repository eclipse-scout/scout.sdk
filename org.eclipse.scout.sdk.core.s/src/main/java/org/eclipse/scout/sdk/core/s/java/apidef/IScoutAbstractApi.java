/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;

@SuppressWarnings({"squid:S00100", "squid:S2166", "squid:S2176", "squid:S00118", "findbugs:NM_METHOD_NAMING_CONVENTION"}) // method naming conventions
public interface IScoutAbstractApi {
  AbstractAccordion AbstractAccordion();

  interface AbstractAccordion extends ITypeNameSupplier {
  }

  AbstractAccordionField AbstractAccordionField();

  interface AbstractAccordionField extends ITypeNameSupplier {
  }

  AbstractActionNode AbstractActionNode();

  interface AbstractActionNode extends ITypeNameSupplier {
  }

  AbstractBigDecimalField AbstractBigDecimalField();

  interface AbstractBigDecimalField extends ITypeNameSupplier {
  }

  AbstractBooleanField AbstractBooleanField();

  interface AbstractBooleanField extends ITypeNameSupplier {
  }

  AbstractBrowserField AbstractBrowserField();

  interface AbstractBrowserField extends ITypeNameSupplier {
  }

  AbstractButton AbstractButton();

  interface AbstractButton extends ITypeNameSupplier {
    String execClickActionMethodName();
  }

  AbstractCalendar AbstractCalendar();

  interface AbstractCalendar extends ITypeNameSupplier {
  }

  AbstractCalendarField AbstractCalendarField();

  interface AbstractCalendarField extends ITypeNameSupplier {
  }

  AbstractCalendarItemProvider AbstractCalendarItemProvider();

  interface AbstractCalendarItemProvider extends ITypeNameSupplier {
  }

  AbstractCancelButton AbstractCancelButton();

  interface AbstractCancelButton extends ITypeNameSupplier {
  }

  AbstractCode AbstractCode();

  interface AbstractCode extends ITypeNameSupplier {
    String getConfiguredTextMethodName();
  }

  AbstractCodeType AbstractCodeType();

  interface AbstractCodeType extends ITypeNameSupplier {
  }

  AbstractCodeTypeWithGeneric AbstractCodeTypeWithGeneric();

  interface AbstractCodeTypeWithGeneric extends ITypeNameSupplier {
  }

  AbstractComposerField AbstractComposerField();

  interface AbstractComposerField extends ITypeNameSupplier {
  }

  AbstractCompositeField AbstractCompositeField();

  interface AbstractCompositeField extends ITypeNameSupplier {
  }

  AbstractDataModel AbstractDataModel();

  interface AbstractDataModel extends ITypeNameSupplier {
  }

  AbstractDataModelEntity AbstractDataModelEntity();

  interface AbstractDataModelEntity extends ITypeNameSupplier {
  }

  AbstractDateField AbstractDateField();

  interface AbstractDateField extends ITypeNameSupplier {
  }

  AbstractDesktop AbstractDesktop();

  interface AbstractDesktop extends ITypeNameSupplier {
  }

  AbstractDesktopExtension AbstractDesktopExtension();

  interface AbstractDesktopExtension extends ITypeNameSupplier {
  }

  AbstractDynamicNlsTextProviderService AbstractDynamicNlsTextProviderService();

  interface AbstractDynamicNlsTextProviderService extends ITypeNameSupplier {
    String getDynamicNlsBaseNameMethodName();
  }

  AbstractExtension AbstractExtension();

  interface AbstractExtension extends ITypeNameSupplier {
  }

  AbstractFileChooserButton AbstractFileChooserButton();

  interface AbstractFileChooserButton extends ITypeNameSupplier {
  }

  AbstractFileChooserField AbstractFileChooserField();

  interface AbstractFileChooserField extends ITypeNameSupplier {
  }

  AbstractForm AbstractForm();

  interface AbstractForm extends ITypeNameSupplier {
    String getConfiguredTitleMethodName();

    String setHandlerMethodName();

    String startInternalExclusiveMethodName();

    String startInternalMethodName();
  }

  AbstractFormData AbstractFormData();

  interface AbstractFormData extends ITypeNameSupplier {
  }

  AbstractFormField AbstractFormField();

  interface AbstractFormField extends ITypeNameSupplier {
    String getConfiguredLabelMethodName();

    String getConfiguredGridHMethodName();

    String getConfiguredLabelVisibleMethodName();

    String getConfiguredMandatoryMethodName();
  }

  AbstractFormFieldData AbstractFormFieldData();

  interface AbstractFormFieldData extends ITypeNameSupplier {
  }

  AbstractFormHandler AbstractFormHandler();

  interface AbstractFormHandler extends ITypeNameSupplier {
    String execStoreMethodName();

    String execLoadMethodName();
  }

  AbstractGroup AbstractGroup();

  interface AbstractGroup extends ITypeNameSupplier {
  }

  AbstractGroupBox AbstractGroupBox();

  interface AbstractGroupBox extends ITypeNameSupplier {
  }

  AbstractHtmlField AbstractHtmlField();

  interface AbstractHtmlField extends ITypeNameSupplier {
  }

  AbstractImageField AbstractImageField();

  interface AbstractImageField extends ITypeNameSupplier {
    String getConfiguredAutoFitMethodName();
  }

  AbstractKeyStroke AbstractKeyStroke();

  interface AbstractKeyStroke extends ITypeNameSupplier {
  }

  AbstractLabelField AbstractLabelField();

  interface AbstractLabelField extends ITypeNameSupplier {
  }

  AbstractListBox AbstractListBox();

  interface AbstractListBox extends ITypeNameSupplier {
  }

  AbstractLongField AbstractLongField();

  interface AbstractLongField extends ITypeNameSupplier {
  }

  AbstractLookupService AbstractLookupService();

  interface AbstractLookupService extends ITypeNameSupplier {
  }

  AbstractMenu AbstractMenu();

  interface AbstractMenu extends ITypeNameSupplier {
    String getConfiguredMenuTypesMethodName();
  }

  AbstractMode AbstractMode();

  interface AbstractMode extends ITypeNameSupplier {
  }

  AbstractModeSelectorField AbstractModeSelectorField();

  interface AbstractModeSelectorField extends ITypeNameSupplier {
  }

  AbstractOkButton AbstractOkButton();

  interface AbstractOkButton extends ITypeNameSupplier {
  }

  AbstractPage AbstractPage();

  interface AbstractPage extends ITypeNameSupplier {
    String getConfiguredTitleMethodName();
  }

  AbstractPageWithNodes AbstractPageWithNodes();

  interface AbstractPageWithNodes extends ITypeNameSupplier {
    String execCreateChildPagesMethodName();
  }

  AbstractPageWithTable AbstractPageWithTable();

  interface AbstractPageWithTable extends ITypeNameSupplier {
    String importPageDataMethodName();

    String execLoadDataMethodName();
  }

  AbstractPermission AbstractPermission();

  interface AbstractPermission extends ITypeNameSupplier {
    String getAccessCheckFailedMessageMethodName();
  }

  AbstractPropertyData AbstractPropertyData();

  interface AbstractPropertyData extends ITypeNameSupplier {
    String getValueMethodName();

    String setValueMethodName();
  }

  AbstractProposalField AbstractProposalField();

  interface AbstractProposalField extends ITypeNameSupplier {
  }

  AbstractRadioButton AbstractRadioButton();

  interface AbstractRadioButton extends ITypeNameSupplier {
    String getConfiguredRadioValueMethodName();
  }

  AbstractRadioButtonGroup AbstractRadioButtonGroup();

  interface AbstractRadioButtonGroup extends ITypeNameSupplier {
  }

  AbstractSequenceBox AbstractSequenceBox();

  interface AbstractSequenceBox extends ITypeNameSupplier {
    String getConfiguredAutoCheckFromToMethodName();
  }

  AbstractSmartField AbstractSmartField();

  interface AbstractSmartField extends ITypeNameSupplier {
  }

  AbstractColumn AbstractColumn();

  interface AbstractColumn extends ITypeNameSupplier {
    String getConfiguredHeaderTextMethodName();

    String getConfiguredWidthMethodName();

    String getConfiguredDisplayableMethodName();

    String getConfiguredPrimaryKeyMethodName();

    String getSelectedValueMethodName();

    String getSelectedValuesMethodName();
  }

  AbstractStringColumn AbstractStringColumn();

  interface AbstractStringColumn extends ITypeNameSupplier {
  }

  AbstractStringConfigProperty AbstractStringConfigProperty();

  interface AbstractStringConfigProperty extends ITypeNameSupplier {
  }

  AbstractStringField AbstractStringField();

  interface AbstractStringField extends ITypeNameSupplier {
    String getConfiguredMaxLengthMethodName();
  }

  AbstractTabBox AbstractTabBox();

  interface AbstractTabBox extends ITypeNameSupplier {
  }

  AbstractTable AbstractTable();

  interface AbstractTable extends ITypeNameSupplier {
  }

  AbstractTableField AbstractTableField();

  interface AbstractTableField extends ITypeNameSupplier {
  }

  AbstractTableFieldBeanData AbstractTableFieldBeanData();

  interface AbstractTableFieldBeanData extends ITypeNameSupplier {
    String rowAtMethodName();

    String setRowsMethodName();

    String createRowMethodName();
  }

  AbstractTablePageData AbstractTablePageData();

  interface AbstractTablePageData extends ITypeNameSupplier {
  }

  AbstractTableRowData AbstractTableRowData();

  interface AbstractTableRowData extends ITypeNameSupplier {
  }

  AbstractTagField AbstractTagField();

  interface AbstractTagField extends ITypeNameSupplier {
  }

  AbstractTile AbstractTile();

  interface AbstractTile extends ITypeNameSupplier {
  }

  AbstractTileField AbstractTileField();

  interface AbstractTileField extends ITypeNameSupplier {
  }

  AbstractTileGrid AbstractTileGrid();

  interface AbstractTileGrid extends ITypeNameSupplier {
  }

  AbstractTree AbstractTree();

  interface AbstractTree extends ITypeNameSupplier {
  }

  AbstractTreeBox AbstractTreeBox();

  interface AbstractTreeBox extends ITypeNameSupplier {
  }

  AbstractTreeField AbstractTreeField();

  interface AbstractTreeField extends ITypeNameSupplier {
  }

  AbstractTreeNode AbstractTreeNode();

  interface AbstractTreeNode extends ITypeNameSupplier {
    String getConfiguredLeafMethodName();
  }

  AbstractValueField AbstractValueField();

  interface AbstractValueField extends ITypeNameSupplier {
  }

  AbstractValueFieldData AbstractValueFieldData();

  interface AbstractValueFieldData extends ITypeNameSupplier {
    String getValueMethodName();

    String setValueMethodName();
  }

  AbstractWebServiceClient AbstractWebServiceClient();

  interface AbstractWebServiceClient extends ITypeNameSupplier {
    String getConfiguredEndpointUrlPropertyMethodName();

    String execInstallHandlersMethodName();

    String newInvocationContextMethodName();
  }

  AbstractWizard AbstractWizard();

  interface AbstractWizard extends ITypeNameSupplier {
  }

  AbstractNumberField AbstractNumberField();

  interface AbstractNumberField extends ITypeNameSupplier {
    String getConfiguredMinValueMethodName();

    String getConfiguredMaxValueMethodName();
  }

  AbstractAction AbstractAction();

  interface AbstractAction extends ITypeNameSupplier {
    String execActionMethodName();

    String execInitActionMethodName();

    String getConfiguredKeyStrokeMethodName();

    String getConfiguredTextMethodName();

    String combineKeyStrokesMethodName();

    String setVisibleGrantedMethodName();
  }
}
