/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.apidef;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;

@SuppressWarnings({"squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // method naming conventions
public interface IScoutAbstractApi {
  AbstractAccordion AbstractAccordion();

  interface AbstractAccordion extends IClassNameSupplier {
  }

  AbstractAccordionField AbstractAccordionField();

  interface AbstractAccordionField extends IClassNameSupplier {
  }

  AbstractActionNode AbstractActionNode();

  interface AbstractActionNode extends IClassNameSupplier {
  }

  AbstractBigDecimalField AbstractBigDecimalField();

  interface AbstractBigDecimalField extends IClassNameSupplier {
  }

  AbstractBooleanField AbstractBooleanField();

  interface AbstractBooleanField extends IClassNameSupplier {
  }

  AbstractBrowserField AbstractBrowserField();

  interface AbstractBrowserField extends IClassNameSupplier {
  }

  AbstractButton AbstractButton();

  interface AbstractButton extends IClassNameSupplier {
    String execClickActionMethodName();
  }

  AbstractCalendar AbstractCalendar();

  interface AbstractCalendar extends IClassNameSupplier {
  }

  AbstractCalendarField AbstractCalendarField();

  interface AbstractCalendarField extends IClassNameSupplier {
  }

  AbstractCalendarItemProvider AbstractCalendarItemProvider();

  interface AbstractCalendarItemProvider extends IClassNameSupplier {
  }

  AbstractCancelButton AbstractCancelButton();

  interface AbstractCancelButton extends IClassNameSupplier {
  }

  AbstractCode AbstractCode();

  interface AbstractCode extends IClassNameSupplier {
    String getConfiguredTextMethodName();
  }

  AbstractCodeType AbstractCodeType();

  interface AbstractCodeType extends IClassNameSupplier {
  }

  AbstractCodeTypeWithGeneric AbstractCodeTypeWithGeneric();

  interface AbstractCodeTypeWithGeneric extends IClassNameSupplier {
  }

  AbstractComposerField AbstractComposerField();

  interface AbstractComposerField extends IClassNameSupplier {
  }

  AbstractCompositeField AbstractCompositeField();

  interface AbstractCompositeField extends IClassNameSupplier {
  }

  AbstractDataModel AbstractDataModel();

  interface AbstractDataModel extends IClassNameSupplier {
  }

  AbstractDataModelEntity AbstractDataModelEntity();

  interface AbstractDataModelEntity extends IClassNameSupplier {
  }

  AbstractDateField AbstractDateField();

  interface AbstractDateField extends IClassNameSupplier {
  }

  AbstractDesktop AbstractDesktop();

  interface AbstractDesktop extends IClassNameSupplier {
  }

  AbstractDesktopExtension AbstractDesktopExtension();

  interface AbstractDesktopExtension extends IClassNameSupplier {
  }

  AbstractDynamicNlsTextProviderService AbstractDynamicNlsTextProviderService();

  interface AbstractDynamicNlsTextProviderService extends IClassNameSupplier {
    String getDynamicNlsBaseNameMethodName();
  }

  AbstractExtension AbstractExtension();

  interface AbstractExtension extends IClassNameSupplier {
  }

  AbstractFileChooserButton AbstractFileChooserButton();

  interface AbstractFileChooserButton extends IClassNameSupplier {
  }

  AbstractFileChooserField AbstractFileChooserField();

  interface AbstractFileChooserField extends IClassNameSupplier {
  }

  AbstractForm AbstractForm();

  interface AbstractForm extends IClassNameSupplier {
    String getConfiguredTitleMethodName();
    String startInternalExclusiveMethodName();
    String startInternalMethodName();
  }

  AbstractFormData AbstractFormData();

  interface AbstractFormData extends IClassNameSupplier {
  }

  AbstractFormField AbstractFormField();

  interface AbstractFormField extends IClassNameSupplier {
    String getConfiguredLabelMethodName();
    String getConfiguredGridHMethodName();
    String getConfiguredLabelVisibleMethodName();
  }

  AbstractFormFieldData AbstractFormFieldData();

  interface AbstractFormFieldData extends IClassNameSupplier {
  }

  AbstractFormHandler AbstractFormHandler();

  interface AbstractFormHandler extends IClassNameSupplier {
    String execStoreMethodName();
    String execLoadMethodName();
  }

  AbstractGroup AbstractGroup();

  interface AbstractGroup extends IClassNameSupplier {
  }

  AbstractGroupBox AbstractGroupBox();

  interface AbstractGroupBox extends IClassNameSupplier {
  }

  AbstractHtmlField AbstractHtmlField();

  interface AbstractHtmlField extends IClassNameSupplier {
  }

  AbstractImageField AbstractImageField();

  interface AbstractImageField extends IClassNameSupplier {
    String getConfiguredAutoFitMethodName();
  }

  AbstractKeyStroke AbstractKeyStroke();

  interface AbstractKeyStroke extends IClassNameSupplier {
  }

  AbstractLabelField AbstractLabelField();

  interface AbstractLabelField extends IClassNameSupplier {
  }

  AbstractListBox AbstractListBox();

  interface AbstractListBox extends IClassNameSupplier {
  }

  AbstractLongField AbstractLongField();

  interface AbstractLongField extends IClassNameSupplier {
  }

  AbstractLookupService AbstractLookupService();

  interface AbstractLookupService extends IClassNameSupplier {
  }

  AbstractMenu AbstractMenu();

  interface AbstractMenu extends IClassNameSupplier {
    String getConfiguredMenuTypesMethodName();
  }

  AbstractMode AbstractMode();

  interface AbstractMode extends IClassNameSupplier {
  }

  AbstractModeSelectorField AbstractModeSelectorField();

  interface AbstractModeSelectorField extends IClassNameSupplier {
  }

  AbstractOkButton AbstractOkButton();

  interface AbstractOkButton extends IClassNameSupplier {
  }

  AbstractPage AbstractPage();

  interface AbstractPage extends IClassNameSupplier {
    String getConfiguredTitleMethodName();
  }

  AbstractPageWithNodes AbstractPageWithNodes();

  interface AbstractPageWithNodes extends IClassNameSupplier {
    String execCreateChildPagesMethodName();
  }

  AbstractPageWithTable AbstractPageWithTable();

  interface AbstractPageWithTable extends IClassNameSupplier {
    String importPageDataMethodName();
    String execLoadDataMethodName();
  }

  AbstractPermission AbstractPermission();

  interface AbstractPermission extends IClassNameSupplier {
  }

  AbstractPropertyData AbstractPropertyData();

  interface AbstractPropertyData extends IClassNameSupplier {
    String getValueMethodName();
    String setValueMethodName();
  }

  AbstractProposalField AbstractProposalField();

  interface AbstractProposalField extends IClassNameSupplier {
  }

  AbstractRadioButton AbstractRadioButton();

  interface AbstractRadioButton extends IClassNameSupplier {
    String getConfiguredRadioValueMethodName();
  }

  AbstractRadioButtonGroup AbstractRadioButtonGroup();

  interface AbstractRadioButtonGroup extends IClassNameSupplier {
  }

  AbstractSequenceBox AbstractSequenceBox();

  interface AbstractSequenceBox extends IClassNameSupplier {
    String getConfiguredAutoCheckFromToMethodName();
  }

  AbstractSmartField AbstractSmartField();

  interface AbstractSmartField extends IClassNameSupplier {
  }

  AbstractColumn AbstractColumn();

  interface AbstractColumn extends IClassNameSupplier {
    String getConfiguredHeaderTextMethodName();
    String getConfiguredWidthMethodName();
  }

  AbstractStringColumn AbstractStringColumn();

  interface AbstractStringColumn extends IClassNameSupplier {
  }

  AbstractStringConfigProperty AbstractStringConfigProperty();

  interface AbstractStringConfigProperty extends IClassNameSupplier {
  }

  AbstractStringField AbstractStringField();

  interface AbstractStringField extends IClassNameSupplier {
    String getConfiguredMaxLengthMethodName();
  }

  AbstractTabBox AbstractTabBox();

  interface AbstractTabBox extends IClassNameSupplier {
  }

  AbstractTable AbstractTable();

  interface AbstractTable extends IClassNameSupplier {
  }

  AbstractTableField AbstractTableField();

  interface AbstractTableField extends IClassNameSupplier {
  }

  AbstractTableFieldBeanData AbstractTableFieldBeanData();

  interface AbstractTableFieldBeanData extends IClassNameSupplier {
    String rowAtMethodName();
    String setRowsMethodName();
    String createRowMethodName();
  }

  AbstractTablePageData AbstractTablePageData();

  interface AbstractTablePageData extends IClassNameSupplier {
  }

  AbstractTableRowData AbstractTableRowData();

  interface AbstractTableRowData extends IClassNameSupplier {
  }

  AbstractTagField AbstractTagField();

  interface AbstractTagField extends IClassNameSupplier {
  }

  AbstractTile AbstractTile();

  interface AbstractTile extends IClassNameSupplier {
  }

  AbstractTileField AbstractTileField();

  interface AbstractTileField extends IClassNameSupplier {
  }

  AbstractTileGrid AbstractTileGrid();

  interface AbstractTileGrid extends IClassNameSupplier {
  }

  AbstractTree AbstractTree();

  interface AbstractTree extends IClassNameSupplier {
  }

  AbstractTreeBox AbstractTreeBox();

  interface AbstractTreeBox extends IClassNameSupplier {
  }

  AbstractTreeField AbstractTreeField();

  interface AbstractTreeField extends IClassNameSupplier {
  }

  AbstractTreeNode AbstractTreeNode();

  interface AbstractTreeNode extends IClassNameSupplier {
  }

  AbstractValueField AbstractValueField();

  interface AbstractValueField extends IClassNameSupplier {
  }

  AbstractValueFieldData AbstractValueFieldData();

  interface AbstractValueFieldData extends IClassNameSupplier {
  }

  AbstractWebServiceClient AbstractWebServiceClient();

  interface AbstractWebServiceClient extends IClassNameSupplier {
    String getConfiguredEndpointUrlPropertyMethodName();
    String execInstallHandlersMethodName();
    String newInvocationContextMethodName();
  }

  AbstractWizard AbstractWizard();

  interface AbstractWizard extends IClassNameSupplier {
  }

  AbstractNumberField AbstractNumberField();

  interface AbstractNumberField extends IClassNameSupplier {
    String getConfiguredMinValueMethodName();
    String getConfiguredMaxValueMethodName();
  }

  AbstractAction AbstractAction();

  interface AbstractAction extends IClassNameSupplier {
    String execActionMethodName();
    String getConfiguredKeyStrokeMethodName();
    String getConfiguredTextMethodName();
    String combineKeyStrokesMethodName();
  }
}
