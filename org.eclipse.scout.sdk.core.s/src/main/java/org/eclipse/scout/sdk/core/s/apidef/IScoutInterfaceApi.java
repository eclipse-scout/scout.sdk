/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.apidef;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.generator.method.IScoutMethodGenerator;

@SuppressWarnings({"squid:S00100", "squid:S2166", "squid:S2176", "squid:S00118", "findbugs:NM_METHOD_NAMING_CONVENTION"}) // method naming conventions
public interface IScoutInterfaceApi {
  IAccordion IAccordion();

  interface IAccordion extends ITypeNameSupplier {
  }

  IAccordionField IAccordionField();

  interface IAccordionField extends ITypeNameSupplier {
  }

  IAction IAction();

  interface IAction extends ITypeNameSupplier {
  }

  IActionNode IActionNode();

  interface IActionNode extends ITypeNameSupplier {
  }

  IBigDecimalField IBigDecimalField();

  interface IBigDecimalField extends ITypeNameSupplier {
  }

  IBooleanField IBooleanField();

  interface IBooleanField extends ITypeNameSupplier {
  }

  IBrowserField IBrowserField();

  interface IBrowserField extends ITypeNameSupplier {
  }

  IButton IButton();

  interface IButton extends ITypeNameSupplier {
  }

  ICalendar ICalendar();

  interface ICalendar extends ITypeNameSupplier {
  }

  ICalendarField ICalendarField();

  interface ICalendarField extends ITypeNameSupplier {
  }

  ICalendarItemProvider ICalendarItemProvider();

  interface ICalendarItemProvider extends ITypeNameSupplier {
  }

  IClientSession IClientSession();

  interface IClientSession extends ITypeNameSupplier {
    String getDesktopMethodName();
  }

  ICode ICode();

  interface ICode extends ITypeNameSupplier {
    String getIdMethodName();
  }

  ICodeType ICodeType();

  interface ICodeType extends ITypeNameSupplier {
    int codeTypeIdTypeParamIndex();

    int codeIdTypeParamIndex();

    String getIdMethodName();
  }

  ITableBeanHolder ITableBeanHolder();

  interface ITableBeanHolder extends ITypeNameSupplier {
    String addRowMethodName();

    String getRowTypeMethodName();

    String getRowsMethodName();
  }

  IColumn IColumn();

  interface IColumn extends ITypeNameSupplier {
    int valueTypeParamIndex();
  }

  ICompositeField ICompositeField();

  interface ICompositeField extends ITypeNameSupplier {
    String getFieldByClassMethodName();
  }

  IPropertyHolder IPropertyHolder();

  interface IPropertyHolder extends ITypeNameSupplier {
    String getPropertyByClassMethodName();
  }

  ICompositeFieldExtension ICompositeFieldExtension();

  interface ICompositeFieldExtension extends ITypeNameSupplier {
  }

  IContextMenuOwner IContextMenuOwner();

  interface IContextMenuOwner extends ITypeNameSupplier {
  }

  IDataChangeObserver IDataChangeObserver();

  interface IDataChangeObserver extends ITypeNameSupplier {
    String registerDataChangeListenerMethodName();
  }

  IDataModelAttribute IDataModelAttribute();

  interface IDataModelAttribute extends ITypeNameSupplier {
  }

  IDataModelEntity IDataModelEntity();

  interface IDataModelEntity extends ITypeNameSupplier {
  }

  IDataObject IDataObject();

  interface IDataObject extends ITypeNameSupplier {
  }

  IDoEntity IDoEntity();

  interface IDoEntity extends ITypeNameSupplier {
    /**
     * Computes the getter prefix for a DoNode attribute. This is "get" for most objects and (depending on Scout
     * version) "is" for boolean types.
     * 
     * @param dataTypeRef
     *          The datatype reference of the attribute. This is the type parameter value of the DoNode.
     * @return The prefix to use for this type.
     */
    String computeGetterPrefixFor(CharSequence dataTypeRef);

    /**
     * Gets some additional getter {@link IMethodGenerator IMethodGenerators} for the given data object attribute.
     * 
     * @param name
     *          The name of attribute. This is the name of the method returning the DoNode.
     * @param dataTypeRef
     *          The datatype reference of the attribute. This is the type parameter value of the DoNode.
     * @param ownerType
     *          The {@link IType} that contains the attribute and will hold the created getters
     * @return A {@link Stream} returning any additional getters.
     */
    Stream<IScoutMethodGenerator<?, ?>> getAdditionalDoNodeGetters(CharSequence name, CharSequence dataTypeRef, IType ownerType);
  }

  IDateField IDateField();

  interface IDateField extends ITypeNameSupplier {
  }

  IDesktop IDesktop();

  interface IDesktop extends ITypeNameSupplier {
    String dataChangedMethodName();
  }

  IDesktopExtension IDesktopExtension();

  interface IDesktopExtension extends ITypeNameSupplier {
  }

  IExtension IExtension();

  interface IExtension extends ITypeNameSupplier {
    int ownerTypeParamIndex();

    String getOwnerMethodName();
  }

  IFileChooserButton IFileChooserButton();

  interface IFileChooserButton extends ITypeNameSupplier {
  }

  IFileChooserField IFileChooserField();

  interface IFileChooserField extends ITypeNameSupplier {
  }

  IForm IForm();

  interface IForm extends ITypeNameSupplier {
    String getFieldByClassMethodName();

    String exportFormDataMethodName();

    String importFormDataMethodName();

    String startMethodName();
  }

  IFormExtension IFormExtension();

  interface IFormExtension extends ITypeNameSupplier {
  }

  IFormField IFormField();

  interface IFormField extends ITypeNameSupplier {
  }

  IFormFieldExtension IFormFieldExtension();

  interface IFormFieldExtension extends ITypeNameSupplier {
  }

  IFormFieldMenu IFormFieldMenu();

  interface IFormFieldMenu extends ITypeNameSupplier {
  }

  IFormHandler IFormHandler();

  interface IFormHandler extends ITypeNameSupplier {
  }

  IGroup IGroup();

  interface IGroup extends ITypeNameSupplier {
  }

  IGroupBox IGroupBox();

  interface IGroupBox extends ITypeNameSupplier {
  }

  IHtmlField IHtmlField();

  interface IHtmlField extends ITypeNameSupplier {
  }

  IImageField IImageField();

  interface IImageField extends ITypeNameSupplier {
  }

  IKeyStroke IKeyStroke();

  interface IKeyStroke extends ITypeNameSupplier {
  }

  ILabelField ILabelField();

  interface ILabelField extends ITypeNameSupplier {
  }

  IListBox IListBox();

  interface IListBox extends ITypeNameSupplier {
  }

  ILongField ILongField();

  interface ILongField extends ITypeNameSupplier {
  }

  ILookupCall ILookupCall();

  interface ILookupCall extends ITypeNameSupplier {
  }

  ILookupRow ILookupRow();

  interface ILookupRow extends ITypeNameSupplier {
  }

  ILookupService ILookupService();

  interface ILookupService extends ITypeNameSupplier {
    int keyTypeTypeParamIndex();

    String getDataByKeyMethodName();

    String getDataByTextMethodName();
  }

  IMenu IMenu();

  interface IMenu extends ITypeNameSupplier {
  }

  IMenuType IMenuType();

  interface IMenuType extends ITypeNameSupplier {
  }

  IMessageBox IMessageBox();

  interface IMessageBox extends ITypeNameSupplier {
    String showMethodName();

    String withHeaderMethodName();
  }

  IMode IMode();

  interface IMode extends ITypeNameSupplier {
  }

  IModeSelectorField IModeSelectorField();

  interface IModeSelectorField extends ITypeNameSupplier {
  }

  IOrdered IOrdered();

  interface IOrdered extends ITypeNameSupplier {
  }

  IOutline IOutline();

  interface IOutline extends ITypeNameSupplier {
  }

  IPage IPage();

  interface IPage extends ITypeNameSupplier {
    String initPageMethodName();
  }

  IPageWithNodes IPageWithNodes();

  interface IPageWithNodes extends ITypeNameSupplier {
  }

  IPageWithTable IPageWithTable();

  interface IPageWithTable extends ITypeNameSupplier {
  }

  IPageWithTableExtension IPageWithTableExtension();

  interface IPageWithTableExtension extends ITypeNameSupplier {
  }

  IPrettyPrintDataObjectMapper IPrettyPrintDataObjectMapper();

  interface IPrettyPrintDataObjectMapper extends ITypeNameSupplier {
  }

  IProposalField IProposalField();

  interface IProposalField extends ITypeNameSupplier {
  }

  IRadioButton IRadioButton();

  interface IRadioButton extends ITypeNameSupplier {
  }

  IRadioButtonGroup IRadioButtonGroup();

  interface IRadioButtonGroup extends ITypeNameSupplier {
  }

  ISequenceBox ISequenceBox();

  interface ISequenceBox extends ITypeNameSupplier {
  }

  IServerSession IServerSession();

  interface IServerSession extends ITypeNameSupplier {
  }

  IService IService();

  interface IService extends ITypeNameSupplier {
  }

  ISession ISession();

  interface ISession extends ITypeNameSupplier {
  }

  ISmartField ISmartField();

  interface ISmartField extends ITypeNameSupplier {
  }

  IStringField IStringField();

  interface IStringField extends ITypeNameSupplier {
  }

  ITabBox ITabBox();

  interface ITabBox extends ITypeNameSupplier {
  }

  ITable ITable();

  interface ITable extends ITypeNameSupplier {
    String getColumnSetMethodName();

    String getSelectedRowCountMethodName();
  }

  ITableControl ITableControl();

  interface ITableControl extends ITypeNameSupplier {
  }

  ITableExtension ITableExtension();

  interface ITableExtension extends ITypeNameSupplier {
  }

  ITableField ITableField();

  interface ITableField extends ITypeNameSupplier {
  }

  ITagField ITagField();

  interface ITagField extends ITypeNameSupplier {
  }

  ITextProviderService ITextProviderService();

  interface ITextProviderService extends ITypeNameSupplier {
  }

  ITile ITile();

  interface ITile extends ITypeNameSupplier {
  }

  ITileField ITileField();

  interface ITileField extends ITypeNameSupplier {
  }

  ITileGrid ITileGrid();

  interface ITileGrid extends ITypeNameSupplier {
  }

  ITree ITree();

  interface ITree extends ITypeNameSupplier {
  }

  ITreeField ITreeField();

  interface ITreeField extends ITypeNameSupplier {
  }

  ITreeNode ITreeNode();

  interface ITreeNode extends ITypeNameSupplier {
    String setVisibleGrantedMethodName();
  }

  ITypeWithClassId ITypeWithClassId();

  interface ITypeWithClassId extends ITypeNameSupplier {
  }

  IUuId IUuId();

  interface IUuId extends ITypeNameSupplier {
  }

  IValueField IValueField();

  interface IValueField extends ITypeNameSupplier {
    int valueTypeParamIndex();
  }

  IViewButton IViewButton();

  interface IViewButton extends ITypeNameSupplier {
  }

  IWidget IWidget();

  interface IWidget extends ITypeNameSupplier {
    String setEnabledPermissionMethodName();

    String setEnabledGrantedMethodName();
  }

  IWizard IWizard();

  interface IWizard extends ITypeNameSupplier {
  }

  IWizardStep IWizardStep();

  interface IWizardStep extends ITypeNameSupplier {
  }

  IConfigProperty IConfigProperty();

  interface IConfigProperty extends ITypeNameSupplier {
    String getKeyMethodName();

    String descriptionMethodName();
  }

  IUiTextContributor IUiTextContributor();

  interface IUiTextContributor extends ITypeNameSupplier {
  }

  DoEntity DoEntity();

  interface DoEntity extends ITypeNameSupplier {
    String doValueMethodName();

    String doListMethodName();
  }
}
