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

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link ScoutModelHierarchy}</h3>
 *
 * @since 5.2.0
 */
public class ScoutModelHierarchy {

  private final IScoutApi m_api;
  private final Map<String /*container*/, Set<String>/* possible children */> m_childrenByContainer = new HashMap<>();
  private final Map<String /*container*/, Set<String>/* possible children */> m_interfaceHierarchy = new HashMap<>();

  protected ScoutModelHierarchy(IScoutApi api) {
    m_api = Ensure.notNull(api);

    // containers
    addContainerElement(api.AbstractAccordion().fqn(), api.IGroup().fqn());
    addContainerElement(api.AbstractAccordionField().fqn(), api.IAccordion().fqn());
    addContainerElement(api.AbstractAccordionFieldExtension().fqn(), api.IAccordion().fqn());
    addContainerElement(api.AbstractActionExtension().fqn(), api.IAction().fqn());
    addContainerElement(api.AbstractActionNode().fqn(), api.IActionNode().fqn());
    addContainerElement(api.AbstractActionNodeExtension().fqn(), api.IActionNode().fqn());
    addContainerElement(api.AbstractButton().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractButtonExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractCalendar().fqn(), api.ICalendarItemProvider().fqn());
    addContainerElement(api.AbstractCalendar().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractCalendarExtension().fqn(), api.ICalendarItemProvider().fqn());
    addContainerElement(api.AbstractCalendarExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractCalendarField().fqn(), api.ICalendar().fqn());
    addContainerElement(api.AbstractCalendarFieldExtension().fqn(), api.ICalendar().fqn());
    addContainerElement(api.AbstractCalendarItemProvider().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractCalendarItemProviderExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractCode().fqn(), api.ICode().fqn());
    addContainerElement(api.AbstractCodeExtension().fqn(), api.ICode().fqn());
    addContainerElement(api.AbstractCodeTypeWithGeneric().fqn(), api.ICode().fqn());
    addContainerElement(api.AbstractCodeTypeWithGenericExtension().fqn(), api.ICode().fqn());
    addContainerElement(api.AbstractComposerField().fqn(), api.ITree().fqn());
    addContainerElement(api.AbstractComposerFieldExtension().fqn(), api.ITree().fqn());
    addContainerElement(api.AbstractCompositeField().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractCompositeFieldExtension().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractDataModel().fqn(), api.IDataModelAttribute().fqn());
    addContainerElement(api.AbstractDataModel().fqn(), api.IDataModelEntity().fqn());
    addContainerElement(api.AbstractDataModelEntity().fqn(), api.IDataModelAttribute().fqn());
    addContainerElement(api.AbstractDataModelEntity().fqn(), api.IDataModelEntity().fqn());
    addContainerElement(api.AbstractDataModelEntityExtension().fqn(), api.IDataModelAttribute().fqn());
    addContainerElement(api.AbstractDataModelEntityExtension().fqn(), api.IDataModelEntity().fqn());
    addContainerElement(api.AbstractDesktop().fqn(), api.IAction().fqn());
    addContainerElement(api.AbstractDesktopExtension().fqn(), api.IAction().fqn());
    addContainerElement(api.AbstractExtension().fqn(), api.IExtension().fqn());
    addContainerElement(api.AbstractForm().fqn(), api.IFormHandler().fqn());
    addContainerElement(api.AbstractFormExtension().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractFormExtension().fqn(), api.IFormHandler().fqn());
    addContainerElement(api.AbstractFormField().fqn(), api.IKeyStroke().fqn());
    addContainerElement(api.AbstractFormFieldExtension().fqn(), api.IKeyStroke().fqn());
    addContainerElement(api.AbstractGroup().fqn(), api.IWidget().fqn());
    addContainerElement(api.AbstractGroupExtension().fqn(), api.IWidget().fqn());
    addContainerElement(api.AbstractGroupBox().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractGroupBoxExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractImageField().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractImageFieldExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractListBox().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractListBox().fqn(), api.ITable().fqn());
    addContainerElement(api.AbstractListBoxExtension().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractListBoxExtension().fqn(), api.ITable().fqn());
    addContainerElement(api.AbstractModeSelectorField().fqn(), api.IMode().fqn());
    addContainerElement(api.AbstractPageWithTable().fqn(), api.ITable().fqn());
    addContainerElement(api.AbstractPageWithTableExtension().fqn(), api.ITable().fqn());
    addContainerElement(api.AbstractRadioButtonGroup().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractRadioButtonGroupExtension().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractTabBox().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTabBoxExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTable().fqn(), api.IColumn().fqn());
    addContainerElement(api.AbstractTable().fqn(), api.IKeyStroke().fqn());
    addContainerElement(api.AbstractTable().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTable().fqn(), api.ITableControl().fqn());
    addContainerElement(api.AbstractTableExtension().fqn(), api.IColumn().fqn());
    addContainerElement(api.AbstractTableExtension().fqn(), api.IKeyStroke().fqn());
    addContainerElement(api.AbstractTableExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTableExtension().fqn(), api.ITableControl().fqn());
    addContainerElement(api.AbstractTableField().fqn(), api.ITable().fqn());
    addContainerElement(api.AbstractTableFieldExtension().fqn(), api.ITable().fqn());
    addContainerElement(api.AbstractTileField().fqn(), api.ITileGrid().fqn());
    addContainerElement(api.AbstractTileFieldExtension().fqn(), api.ITileGrid().fqn());
    addContainerElement(api.AbstractTileGrid().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTileGrid().fqn(), api.ITile().fqn());
    addContainerElement(api.AbstractTileGridExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTileGridExtension().fqn(), api.ITile().fqn());
    addContainerElement(api.AbstractTree().fqn(), api.IKeyStroke().fqn());
    addContainerElement(api.AbstractTree().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTreeExtension().fqn(), api.IKeyStroke().fqn());
    addContainerElement(api.AbstractTreeExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTreeBox().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractTreeBox().fqn(), api.ITree().fqn());
    addContainerElement(api.AbstractTreeBoxExtension().fqn(), api.IFormField().fqn());
    addContainerElement(api.AbstractTreeBoxExtension().fqn(), api.ITree().fqn());
    addContainerElement(api.AbstractTreeField().fqn(), api.ITree().fqn());
    addContainerElement(api.AbstractTreeFieldExtension().fqn(), api.ITree().fqn());
    addContainerElement(api.AbstractTreeNode().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractTreeNodeExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractValueField().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractValueFieldExtension().fqn(), api.IMenu().fqn());
    addContainerElement(api.AbstractWizard().fqn(), api.IWizardStep().fqn());
    addContainerElement(api.AbstractWizardExtension().fqn(), api.IWizardStep().fqn());
    addContainerElement(api.IContextMenuOwner().fqn(), api.IMenu().fqn());

    // hierarchy
    addInterfaceSuperType(api.IAccordion().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.IAccordionField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.IAction().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IAction().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.IActionNode().fqn(), api.IAction().fqn());
    addInterfaceSuperType(api.IBigDecimalField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IBooleanField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IBrowserField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.IButton().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.IButton().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.ICalendar().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.ICalendar().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.ICalendarField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.ICode().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.ICode().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.ICodeType().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.IColumn().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IColumn().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.ICompositeField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.ICompositeFieldExtension().fqn(), api.IFormFieldExtension().fqn());
    addInterfaceSuperType(api.IDataModelAttribute().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IDataModelAttribute().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.IDataModelEntity().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IDataModelEntity().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.IDateField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IDesktop().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.IDesktop().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.IDesktopExtension().fqn(), api.IExtension().fqn());
    addInterfaceSuperType(api.IFileChooserButton().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IFileChooserField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IFormExtension().fqn(), api.IExtension().fqn());
    addInterfaceSuperType(api.IFormField().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IFormField().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.IFormFieldExtension().fqn(), api.IExtension().fqn());
    addInterfaceSuperType(api.IGroup().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IGroup().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.IGroupBox().fqn(), api.ICompositeField().fqn());
    addInterfaceSuperType(api.IGroupBox().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.IHtmlField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IImageField().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.IImageField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.IKeyStroke().fqn(), api.IAction().fqn());
    addInterfaceSuperType(api.ILabelField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IListBox().fqn(), api.ICompositeField().fqn());
    addInterfaceSuperType(api.IListBox().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.ILongField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IMenu().fqn(), api.IActionNode().fqn());
    addInterfaceSuperType(api.IMode().fqn(), api.IAction().fqn());
    addInterfaceSuperType(api.IModeSelectorField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IOutline().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IOutline().fqn(), api.ITree().fqn());
    addInterfaceSuperType(api.IPage().fqn(), api.ITreeNode().fqn());
    addInterfaceSuperType(api.IPage().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.IPageWithNodes().fqn(), api.IPage().fqn());
    addInterfaceSuperType(api.IPageWithTable().fqn(), api.IPage().fqn());
    addInterfaceSuperType(api.IPageWithTableExtension().fqn(), api.IExtension().fqn());
    addInterfaceSuperType(api.IProposalField().fqn(), api.ISmartField().fqn());
    addInterfaceSuperType(api.IRadioButton().fqn(), api.IButton().fqn());
    addInterfaceSuperType(api.IRadioButtonGroup().fqn(), api.ICompositeField().fqn());
    addInterfaceSuperType(api.IRadioButtonGroup().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.ISequenceBox().fqn(), api.ICompositeField().fqn());
    addInterfaceSuperType(api.ISmartField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.IStringField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.ITabBox().fqn(), api.ICompositeField().fqn());
    addInterfaceSuperType(api.ITabBox().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.ITable().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.ITable().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.ITableControl().fqn(), api.IAction().fqn());
    addInterfaceSuperType(api.ITableExtension().fqn(), api.IExtension().fqn());
    addInterfaceSuperType(api.ITableField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.ITagField().fqn(), api.IValueField().fqn());
    addInterfaceSuperType(api.ITile().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.ITile().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.ITileField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.ITileGrid().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.ITileGrid().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.ITree().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.ITree().fqn(), api.IWidget().fqn());
    addInterfaceSuperType(api.ITreeField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.ITreeNode().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.IValueField().fqn(), api.IContextMenuOwner().fqn());
    addInterfaceSuperType(api.IValueField().fqn(), api.IFormField().fqn());
    addInterfaceSuperType(api.IViewButton().fqn(), api.IAction().fqn());
    addInterfaceSuperType(api.IWidget().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.IWizard().fqn(), api.ITypeWithClassId().fqn());
    addInterfaceSuperType(api.IWizardStep().fqn(), api.IOrdered().fqn());
    addInterfaceSuperType(api.IWizardStep().fqn(), api.ITypeWithClassId().fqn());
  }

  public IScoutApi api() {
    return m_api;
  }

  private void addContainerElement(String container, String possibleChild) {
    addToMapSet(container, possibleChild, m_childrenByContainer, 3);
  }

  private void addInterfaceSuperType(String ifc, String superTypeIfc) {
    addToMapSet(ifc, superTypeIfc, m_interfaceHierarchy, 3);
  }

  protected static void addToMapSet(String first, String second, Map<String, Set<String>> store, int initialSetSize) {
    store.computeIfAbsent(first, k -> new HashSet<>(initialSetSize)).add(second);
  }

  /**
   * Gets all interface fully qualified names of elements that may be added inside the given element.
   *
   * @param superTypesOfDeclaringType
   *          The fully qualified super type names of the container.
   * @return A {@link Set} with the fully qualified interface names of possible children.
   * @see IScoutApi
   */
  public Set<String> possibleChildrenFor(Collection<String> superTypesOfDeclaringType) {
    if (superTypesOfDeclaringType.contains(m_api.AbstractTabBox().fqn()) || superTypesOfDeclaringType.contains(m_api.AbstractTabBoxExtension().fqn())) {
      // tab boxes are composites but only allow a reduced set of items
      Set<String> possibleChildrenIfcFqn = new HashSet<>(3);
      possibleChildrenIfcFqn.add(m_api.IGroupBox().fqn());
      possibleChildrenIfcFqn.add(m_api.IMenu().fqn());
      possibleChildrenIfcFqn.add(m_api.IKeyStroke().fqn());
      return possibleChildrenIfcFqn;
    }
    if (superTypesOfDeclaringType.contains(m_api.AbstractListBox().fqn()) || superTypesOfDeclaringType.contains(m_api.AbstractTreeBox().fqn()) ||
        superTypesOfDeclaringType.contains(m_api.AbstractListBoxExtension().fqn()) || superTypesOfDeclaringType.contains(m_api.AbstractTreeBoxExtension().fqn())) {
      // list boxes and tree boxes are composites but only allow a reduced set of items
      Set<String> possibleChildrenIfcFqn = new HashSet<>(2);
      possibleChildrenIfcFqn.add(m_api.IMenu().fqn());
      possibleChildrenIfcFqn.add(m_api.IKeyStroke().fqn());
      return possibleChildrenIfcFqn;
    }
    if (superTypesOfDeclaringType.contains(m_api.AbstractRadioButtonGroup().fqn()) || superTypesOfDeclaringType.contains(m_api.AbstractRadioButtonGroupExtension().fqn())) {
      // radiobutton groups are composites but only allow a reduced set of items
      Set<String> possibleChildrenIfcFqn = new HashSet<>(3);
      possibleChildrenIfcFqn.add(m_api.IRadioButton().fqn());
      possibleChildrenIfcFqn.add(m_api.IMenu().fqn());
      possibleChildrenIfcFqn.add(m_api.IKeyStroke().fqn());
      return possibleChildrenIfcFqn;
    }
    return superTypesOfDeclaringType.stream()
        .flatMap(this::getPossibleChildren)
        .collect(toSet());
  }

  protected Stream<String> getPossibleChildren(String superClass) {
    var children = m_childrenByContainer.get(superClass);
    if (children == null || children.isEmpty()) {
      return Stream.empty();
    }
    return children.stream();
  }

  /**
   * Checks if scoutTypeFqn is a subtype of scoutSuperTypeFqn.
   *
   * @param scoutTypeFqn
   *          The fully qualified interface name of the possible sub type.
   * @param scoutSuperTypeFqn
   *          The fully qualified interface name of the super type.
   * @return {@code true} if it is a subtype. {@code false} otherwise.
   */
  public boolean isSubtypeOf(String scoutTypeFqn, String scoutSuperTypeFqn) {
    if (Objects.equals(scoutTypeFqn, scoutSuperTypeFqn)) {
      return true;
    }
    var directSuperTypes = m_interfaceHierarchy.get(scoutTypeFqn);
    return !(directSuperTypes == null || directSuperTypes.isEmpty()) && isSubtypeOfRec(scoutSuperTypeFqn, directSuperTypes);
  }

  protected boolean isSubtypeOfRec(String scoutTypeFqn, Collection<String> directSuperTypes) {
    if (directSuperTypes.contains(scoutTypeFqn)) {
      return true;
    }

    for (var s : directSuperTypes) {
      var found = isSubtypeOf(s, scoutTypeFqn);
      if (found) {
        return true;
      }
    }
    return false;
  }
}
