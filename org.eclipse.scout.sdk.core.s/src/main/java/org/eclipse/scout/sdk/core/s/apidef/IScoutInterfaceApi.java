/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
public interface IScoutInterfaceApi {
  IAccordion IAccordion();

  interface IAccordion extends IClassNameSupplier {
  }

  IAccordionField IAccordionField();

  interface IAccordionField extends IClassNameSupplier {
  }

  IAction IAction();

  interface IAction extends IClassNameSupplier {
  }

  IActionNode IActionNode();

  interface IActionNode extends IClassNameSupplier {
  }

  IBigDecimalField IBigDecimalField();

  interface IBigDecimalField extends IClassNameSupplier {
  }

  IBooleanField IBooleanField();

  interface IBooleanField extends IClassNameSupplier {
  }

  IBrowserField IBrowserField();

  interface IBrowserField extends IClassNameSupplier {
  }

  IButton IButton();

  interface IButton extends IClassNameSupplier {
  }

  ICalendar ICalendar();

  interface ICalendar extends IClassNameSupplier {
  }

  ICalendarField ICalendarField();

  interface ICalendarField extends IClassNameSupplier {
  }

  ICalendarItemProvider ICalendarItemProvider();

  interface ICalendarItemProvider extends IClassNameSupplier {
  }

  IClientSession IClientSession();

  interface IClientSession extends IClassNameSupplier {
  }

  ICode ICode();

  interface ICode extends IClassNameSupplier {
    String getIdMethodName();
  }

  ICodeType ICodeType();

  interface ICodeType extends IClassNameSupplier {
    int codeTypeIdTypeParamIndex();
    int codeIdTypeParamIndex();
    String getIdMethodName();
  }

  ITableBeanHolder ITableBeanHolder();

  interface ITableBeanHolder extends IClassNameSupplier {
    String addRowMethodName();
    String getRowTypeMethodName();
    String getRowsMethodName();
  }

  IColumn IColumn();

  interface IColumn extends IClassNameSupplier {
    int valueTypeParamIndex();
  }

  ICompositeField ICompositeField();

  interface ICompositeField extends IClassNameSupplier {
    String getFieldByClassMethodName();
  }

  IPropertyHolder IPropertyHolder();

  interface IPropertyHolder extends IClassNameSupplier {
    String getPropertyByClassMethodName();
  }

  ICompositeFieldExtension ICompositeFieldExtension();

  interface ICompositeFieldExtension extends IClassNameSupplier {
  }

  IContextMenuOwner IContextMenuOwner();

  interface IContextMenuOwner extends IClassNameSupplier {
  }

  IDataModelAttribute IDataModelAttribute();

  interface IDataModelAttribute extends IClassNameSupplier {
  }

  IDataModelEntity IDataModelEntity();

  interface IDataModelEntity extends IClassNameSupplier {
  }

  IDataObject IDataObject();

  interface IDataObject extends IClassNameSupplier {
  }

  IDoEntity IDoEntity();

  interface IDoEntity extends IClassNameSupplier {
  }

  IDateField IDateField();

  interface IDateField extends IClassNameSupplier {
  }

  IDesktop IDesktop();

  interface IDesktop extends IClassNameSupplier {
  }

  IDesktopExtension IDesktopExtension();

  interface IDesktopExtension extends IClassNameSupplier {
  }

  IExtension IExtension();

  interface IExtension extends IClassNameSupplier {
    int ownerTypeParamIndex();
    String getOwnerMethodName();
  }

  IFileChooserButton IFileChooserButton();

  interface IFileChooserButton extends IClassNameSupplier {
  }

  IFileChooserField IFileChooserField();

  interface IFileChooserField extends IClassNameSupplier {
  }

  IForm IForm();

  interface IForm extends IClassNameSupplier {
    String getFieldByClassMethodName();
    String exportFormDataMethodName();
    String importFormDataMethodName();
  }

  IFormExtension IFormExtension();

  interface IFormExtension extends IClassNameSupplier {
  }

  IFormField IFormField();

  interface IFormField extends IClassNameSupplier {
  }

  IFormFieldExtension IFormFieldExtension();

  interface IFormFieldExtension extends IClassNameSupplier {
  }

  IFormFieldMenu IFormFieldMenu();

  interface IFormFieldMenu extends IClassNameSupplier {
  }

  IFormHandler IFormHandler();

  interface IFormHandler extends IClassNameSupplier {
  }

  IGroup IGroup();

  interface IGroup extends IClassNameSupplier {
  }

  IGroupBox IGroupBox();

  interface IGroupBox extends IClassNameSupplier {
  }

  IHtmlField IHtmlField();

  interface IHtmlField extends IClassNameSupplier {
  }

  IImageField IImageField();

  interface IImageField extends IClassNameSupplier {
  }

  IKeyStroke IKeyStroke();

  interface IKeyStroke extends IClassNameSupplier {
  }

  ILabelField ILabelField();

  interface ILabelField extends IClassNameSupplier {
  }

  IListBox IListBox();

  interface IListBox extends IClassNameSupplier {
  }

  ILongField ILongField();

  interface ILongField extends IClassNameSupplier {
  }

  ILookupCall ILookupCall();

  interface ILookupCall extends IClassNameSupplier {
  }

  ILookupRow ILookupRow();

  interface ILookupRow extends IClassNameSupplier {
  }

  ILookupService ILookupService();

  interface ILookupService extends IClassNameSupplier {
    int keyTypeTypeParamIndex();
  }

  IMenu IMenu();

  interface IMenu extends IClassNameSupplier {
  }

  IMenuType IMenuType();

  interface IMenuType extends IClassNameSupplier {
  }

  IMode IMode();

  interface IMode extends IClassNameSupplier {
  }

  IModeSelectorField IModeSelectorField();

  interface IModeSelectorField extends IClassNameSupplier {
  }

  IOrdered IOrdered();

  interface IOrdered extends IClassNameSupplier {
  }

  IOutline IOutline();

  interface IOutline extends IClassNameSupplier {
  }

  IPage IPage();

  interface IPage extends IClassNameSupplier {
  }

  IPageWithNodes IPageWithNodes();

  interface IPageWithNodes extends IClassNameSupplier {
  }

  IPageWithTable IPageWithTable();

  interface IPageWithTable extends IClassNameSupplier {
  }

  IPageWithTableExtension IPageWithTableExtension();

  interface IPageWithTableExtension extends IClassNameSupplier {
  }

  IPrettyPrintDataObjectMapper IPrettyPrintDataObjectMapper();

  interface IPrettyPrintDataObjectMapper extends IClassNameSupplier {
  }

  IProposalField IProposalField();

  interface IProposalField extends IClassNameSupplier {
  }

  IRadioButton IRadioButton();

  interface IRadioButton extends IClassNameSupplier {
  }

  IRadioButtonGroup IRadioButtonGroup();

  interface IRadioButtonGroup extends IClassNameSupplier {
  }

  ISequenceBox ISequenceBox();

  interface ISequenceBox extends IClassNameSupplier {
  }

  IServerSession IServerSession();

  interface IServerSession extends IClassNameSupplier {
  }

  IService IService();

  interface IService extends IClassNameSupplier {
  }

  ISession ISession();

  interface ISession extends IClassNameSupplier {
  }

  ISmartField ISmartField();

  interface ISmartField extends IClassNameSupplier {
  }

  IStringField IStringField();

  interface IStringField extends IClassNameSupplier {
  }

  ITabBox ITabBox();

  interface ITabBox extends IClassNameSupplier {
  }

  ITable ITable();

  interface ITable extends IClassNameSupplier {
    String getColumnSetMethodName();
  }

  ITableControl ITableControl();

  interface ITableControl extends IClassNameSupplier {
  }

  ITableExtension ITableExtension();

  interface ITableExtension extends IClassNameSupplier {
  }

  ITableField ITableField();

  interface ITableField extends IClassNameSupplier {
  }

  ITagField ITagField();

  interface ITagField extends IClassNameSupplier {
  }

  ITextProviderService ITextProviderService();

  interface ITextProviderService extends IClassNameSupplier {
  }

  ITile ITile();

  interface ITile extends IClassNameSupplier {
  }

  ITileField ITileField();

  interface ITileField extends IClassNameSupplier {
  }

  ITileGrid ITileGrid();

  interface ITileGrid extends IClassNameSupplier {
  }

  ITree ITree();

  interface ITree extends IClassNameSupplier {
  }

  ITreeField ITreeField();

  interface ITreeField extends IClassNameSupplier {
  }

  ITreeNode ITreeNode();

  interface ITreeNode extends IClassNameSupplier {
  }

  ITypeWithClassId ITypeWithClassId();

  interface ITypeWithClassId extends IClassNameSupplier {
  }

  IUuId IUuId();

  interface IUuId extends IClassNameSupplier {
  }

  IValueField IValueField();

  interface IValueField extends IClassNameSupplier {
    int valueTypeParamIndex();
  }

  IViewButton IViewButton();

  interface IViewButton extends IClassNameSupplier {
  }

  IWidget IWidget();

  interface IWidget extends IClassNameSupplier {
    String setEnabledPermissionMethodName();
  }

  IWizard IWizard();

  interface IWizard extends IClassNameSupplier {
  }

  IWizardStep IWizardStep();

  interface IWizardStep extends IClassNameSupplier {
  }

  IConfigProperty IConfigProperty();

  interface IConfigProperty extends IClassNameSupplier {
    String getKeyMethodName();
    String descriptionMethodName();
  }

  IUiTextContributor IUiTextContributor();

  interface IUiTextContributor extends IClassNameSupplier {
  }
}
