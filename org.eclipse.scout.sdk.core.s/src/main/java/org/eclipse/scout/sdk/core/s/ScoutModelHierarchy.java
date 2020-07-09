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

import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <h3>{@link ScoutModelHierarchy}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("squid:UnusedPrivateMethod")
public final class ScoutModelHierarchy {

  private static final Map<String /*container*/, Set<String>/* possible children */> POSSIBLE_CHILDREN_BY_CONTAINER = new HashMap<>();
  private static final Map<String /*container*/, Set<String>/* possible children */> INTERFACE_HIERARCHY = new HashMap<>();

  static {
    // containers
    addContainerElement(AbstractAccordion, IGroup);
    addContainerElement(AbstractAccordionField, IAccordion);
    addContainerElement(AbstractAccordionFieldExtension, IAccordion);
    addContainerElement(AbstractActionExtension, IAction);
    addContainerElement(AbstractActionNode, IActionNode);
    addContainerElement(AbstractActionNodeExtension, IActionNode);
    addContainerElement(AbstractButton, IMenu);
    addContainerElement(AbstractButtonExtension, IMenu);
    addContainerElement(AbstractCalendar, ICalendarItemProvider);
    addContainerElement(AbstractCalendar, IMenu);
    addContainerElement(AbstractCalendarExtension, ICalendarItemProvider);
    addContainerElement(AbstractCalendarExtension, IMenu);
    addContainerElement(AbstractCalendarField, ICalendar);
    addContainerElement(AbstractCalendarFieldExtension, ICalendar);
    addContainerElement(AbstractCalendarItemProvider, IMenu);
    addContainerElement(AbstractCalendarItemProviderExtension, IMenu);
    addContainerElement(AbstractCode, ICode);
    addContainerElement(AbstractCodeExtension, ICode);
    addContainerElement(AbstractCodeTypeWithGeneric, ICode);
    addContainerElement(AbstractCodeTypeWithGenericExtension, ICode);
    addContainerElement(AbstractComposerField, ITree);
    addContainerElement(AbstractComposerFieldExtension, ITree);
    addContainerElement(AbstractCompositeField, IFormField);
    addContainerElement(AbstractCompositeFieldExtension, IFormField);
    addContainerElement(AbstractDataModel, IDataModelAttribute);
    addContainerElement(AbstractDataModel, IDataModelEntity);
    addContainerElement(AbstractDataModelEntity, IDataModelAttribute);
    addContainerElement(AbstractDataModelEntity, IDataModelEntity);
    addContainerElement(AbstractDataModelEntityExtension, IDataModelAttribute);
    addContainerElement(AbstractDataModelEntityExtension, IDataModelEntity);
    addContainerElement(AbstractDesktop, IAction);
    addContainerElement(AbstractDesktopExtension, IAction);
    addContainerElement(AbstractExtension, IExtension);
    addContainerElement(AbstractForm, IFormHandler);
    addContainerElement(AbstractFormExtension, IFormField);
    addContainerElement(AbstractFormExtension, IFormHandler);
    addContainerElement(AbstractFormField, IKeyStroke);
    addContainerElement(AbstractFormFieldExtension, IKeyStroke);
    addContainerElement(AbstractGroup, IWidget);
    addContainerElement(AbstractGroupExtension, IWidget);
    addContainerElement(AbstractGroupBox, IMenu);
    addContainerElement(AbstractGroupBoxExtension, IMenu);
    addContainerElement(AbstractImageField, IMenu);
    addContainerElement(AbstractImageFieldExtension, IMenu);
    addContainerElement(AbstractListBox, IFormField);
    addContainerElement(AbstractListBox, ITable);
    addContainerElement(AbstractListBoxExtension, IFormField);
    addContainerElement(AbstractListBoxExtension, ITable);
    addContainerElement(AbstractModeSelectorField, IMode);
    addContainerElement(AbstractPageWithTable, ITable);
    addContainerElement(AbstractPageWithTableExtension, ITable);
    addContainerElement(AbstractRadioButtonGroup, IFormField);
    addContainerElement(AbstractRadioButtonGroupExtension, IFormField);
    addContainerElement(AbstractTabBox, IMenu);
    addContainerElement(AbstractTabBoxExtension, IMenu);
    addContainerElement(AbstractTable, IColumn);
    addContainerElement(AbstractTable, IKeyStroke);
    addContainerElement(AbstractTable, IMenu);
    addContainerElement(AbstractTable, ITableControl);
    addContainerElement(AbstractTableExtension, IColumn);
    addContainerElement(AbstractTableExtension, IKeyStroke);
    addContainerElement(AbstractTableExtension, IMenu);
    addContainerElement(AbstractTableExtension, ITableControl);
    addContainerElement(AbstractTableField, ITable);
    addContainerElement(AbstractTableFieldExtension, ITable);
    addContainerElement(AbstractTileField, ITileGrid);
    addContainerElement(AbstractTileFieldExtension, ITileGrid);
    addContainerElement(AbstractTileGrid, IMenu);
    addContainerElement(AbstractTileGrid, ITile);
    addContainerElement(AbstractTileGridExtension, IMenu);
    addContainerElement(AbstractTileGridExtension, ITile);
    addContainerElement(AbstractTree, IKeyStroke);
    addContainerElement(AbstractTree, IMenu);
    addContainerElement(AbstractTreeExtension, IKeyStroke);
    addContainerElement(AbstractTreeExtension, IMenu);
    addContainerElement(AbstractTreeBox, IFormField);
    addContainerElement(AbstractTreeBox, ITree);
    addContainerElement(AbstractTreeBoxExtension, IFormField);
    addContainerElement(AbstractTreeBoxExtension, ITree);
    addContainerElement(AbstractTreeField, ITree);
    addContainerElement(AbstractTreeFieldExtension, ITree);
    addContainerElement(AbstractTreeNode, IMenu);
    addContainerElement(AbstractTreeNodeExtension, IMenu);
    addContainerElement(AbstractValueField, IMenu);
    addContainerElement(AbstractValueFieldExtension, IMenu);
    addContainerElement(AbstractWizard, IWizardStep);
    addContainerElement(AbstractWizardExtension, IWizardStep);
    addContainerElement(IContextMenuOwner, IMenu);

    // hierarchy
    addInterfaceSuperType(IAccordion, IWidget);
    addInterfaceSuperType(IAccordionField, IFormField);
    addInterfaceSuperType(IAction, IOrdered);
    addInterfaceSuperType(IAction, IWidget);
    addInterfaceSuperType(IActionNode, IAction);
    addInterfaceSuperType(IBigDecimalField, IValueField);
    addInterfaceSuperType(IBooleanField, IValueField);
    addInterfaceSuperType(IBrowserField, IFormField);
    addInterfaceSuperType(IButton, IContextMenuOwner);
    addInterfaceSuperType(IButton, IFormField);
    addInterfaceSuperType(ICalendar, IContextMenuOwner);
    addInterfaceSuperType(ICalendar, IWidget);
    addInterfaceSuperType(ICalendarField, IValueField);
    addInterfaceSuperType(ICode, IOrdered);
    addInterfaceSuperType(ICode, ITypeWithClassId);
    addInterfaceSuperType(ICodeType, ITypeWithClassId);
    addInterfaceSuperType(IColumn, IOrdered);
    addInterfaceSuperType(IColumn, ITypeWithClassId);
    addInterfaceSuperType(ICompositeField, IFormField);
    addInterfaceSuperType(ICompositeFieldExtension, IFormFieldExtension);
    addInterfaceSuperType(IDataModelAttribute, IOrdered);
    addInterfaceSuperType(IDataModelAttribute, ITypeWithClassId);
    addInterfaceSuperType(IDataModelEntity, IOrdered);
    addInterfaceSuperType(IDataModelEntity, ITypeWithClassId);
    addInterfaceSuperType(IDateField, IValueField);
    addInterfaceSuperType(IDesktop, IContextMenuOwner);
    addInterfaceSuperType(IDesktop, IWidget);
    addInterfaceSuperType(IDesktopExtension, IExtension);
    addInterfaceSuperType(IFileChooserButton, IValueField);
    addInterfaceSuperType(IFileChooserField, IValueField);
    addInterfaceSuperType(IFormExtension, IExtension);
    addInterfaceSuperType(IFormField, IOrdered);
    addInterfaceSuperType(IFormField, IWidget);
    addInterfaceSuperType(IFormFieldExtension, IExtension);
    addInterfaceSuperType(IGroup, IOrdered);
    addInterfaceSuperType(IGroup, IWidget);
    addInterfaceSuperType(IGroupBox, ICompositeField);
    addInterfaceSuperType(IGroupBox, IContextMenuOwner);
    addInterfaceSuperType(IHtmlField, IValueField);
    addInterfaceSuperType(IImageField, IContextMenuOwner);
    addInterfaceSuperType(IImageField, IFormField);
    addInterfaceSuperType(IKeyStroke, IAction);
    addInterfaceSuperType(ILabelField, IValueField);
    addInterfaceSuperType(IListBox, ICompositeField);
    addInterfaceSuperType(IListBox, IValueField);
    addInterfaceSuperType(ILongField, IValueField);
    addInterfaceSuperType(IMenu, IActionNode);
    addInterfaceSuperType(IMode, IAction);
    addInterfaceSuperType(IModeSelectorField, IValueField);
    addInterfaceSuperType(IOutline, IOrdered);
    addInterfaceSuperType(IOutline, ITree);
    addInterfaceSuperType(IPage, ITreeNode);
    addInterfaceSuperType(IPage, ITypeWithClassId);
    addInterfaceSuperType(IPageWithNodes, IPage);
    addInterfaceSuperType(IPageWithTable, IPage);
    addInterfaceSuperType(IPageWithTableExtension, IExtension);
    addInterfaceSuperType(IProposalField, ISmartField);
    addInterfaceSuperType(IRadioButton, IButton);
    addInterfaceSuperType(IRadioButtonGroup, ICompositeField);
    addInterfaceSuperType(IRadioButtonGroup, IValueField);
    addInterfaceSuperType(ISequenceBox, ICompositeField);
    addInterfaceSuperType(ISmartField, IValueField);
    addInterfaceSuperType(IStringField, IValueField);
    addInterfaceSuperType(ITabBox, ICompositeField);
    addInterfaceSuperType(ITabBox, IContextMenuOwner);
    addInterfaceSuperType(ITable, IContextMenuOwner);
    addInterfaceSuperType(ITable, IWidget);
    addInterfaceSuperType(ITableControl, IAction);
    addInterfaceSuperType(ITableExtension, IExtension);
    addInterfaceSuperType(ITableField, IFormField);
    addInterfaceSuperType(ITagField, IValueField);
    addInterfaceSuperType(ITile, IOrdered);
    addInterfaceSuperType(ITile, IWidget);
    addInterfaceSuperType(ITileField, IFormField);
    addInterfaceSuperType(ITileGrid, IContextMenuOwner);
    addInterfaceSuperType(ITileGrid, IWidget);
    addInterfaceSuperType(ITree, IContextMenuOwner);
    addInterfaceSuperType(ITree, IWidget);
    addInterfaceSuperType(ITreeField, IFormField);
    addInterfaceSuperType(ITreeNode, IContextMenuOwner);
    addInterfaceSuperType(IValueField, IContextMenuOwner);
    addInterfaceSuperType(IValueField, IFormField);
    addInterfaceSuperType(IViewButton, IAction);
    addInterfaceSuperType(IWidget, ITypeWithClassId);
    addInterfaceSuperType(IWizard, ITypeWithClassId);
    addInterfaceSuperType(IWizardStep, IOrdered);
    addInterfaceSuperType(IWizardStep, ITypeWithClassId);
  }

  private ScoutModelHierarchy() {
  }

  private static void addContainerElement(String container, String possibleChild) {
    addToMapSet(container, possibleChild, POSSIBLE_CHILDREN_BY_CONTAINER, 4);
  }

  private static void addInterfaceSuperType(String ifc, String superTypeIfc) {
    addToMapSet(ifc, superTypeIfc, INTERFACE_HIERARCHY, 4);
  }

  private static void addToMapSet(String first, String second, Map<String, Set<String>> store, int initialSetSize) {
    store.computeIfAbsent(first, k -> new HashSet<>(initialSetSize)).add(second);
  }

  /**
   * Gets all interface fully qualified names of elements that may be added inside the given element.
   *
   * @param superTypesOfDeclaringType
   *          The fully qualified super type names of the container.
   * @return A {@link Set} with the fully qualified interface names of possible children.
   * @see IScoutRuntimeTypes
   */
  @SuppressWarnings("DuplicatedCode")
  public static Set<String> getPossibleChildren(Collection<String> superTypesOfDeclaringType) {
    if (superTypesOfDeclaringType.contains(AbstractTabBox) || superTypesOfDeclaringType.contains(AbstractTabBoxExtension)) {
      // tab boxes are composites but only allow a reduced set of items
      Set<String> possibleChildrenIfcFqn = new HashSet<>(3);
      possibleChildrenIfcFqn.add(IGroupBox);
      possibleChildrenIfcFqn.add(IMenu);
      possibleChildrenIfcFqn.add(IKeyStroke);
      return possibleChildrenIfcFqn;
    }
    if (superTypesOfDeclaringType.contains(AbstractListBox) || superTypesOfDeclaringType.contains(AbstractTreeBox) ||
        superTypesOfDeclaringType.contains(AbstractListBoxExtension) || superTypesOfDeclaringType.contains(AbstractTreeBoxExtension)) {
      // list boxes and tree boxes are composites but only allow a reduced set of items
      Set<String> possibleChildrenIfcFqn = new HashSet<>(2);
      possibleChildrenIfcFqn.add(IMenu);
      possibleChildrenIfcFqn.add(IKeyStroke);
      return possibleChildrenIfcFqn;
    }
    if (superTypesOfDeclaringType.contains(AbstractRadioButtonGroup) || superTypesOfDeclaringType.contains(AbstractRadioButtonGroupExtension)) {
      // radiobutton groups are composites but only allow a reduced set of items
      Set<String> possibleChildrenIfcFqn = new HashSet<>(3);
      possibleChildrenIfcFqn.add(IRadioButton);
      possibleChildrenIfcFqn.add(IMenu);
      possibleChildrenIfcFqn.add(IKeyStroke);
      return possibleChildrenIfcFqn;
    }
    return superTypesOfDeclaringType.stream()
        .flatMap(ScoutModelHierarchy::getPossibleChildren)
        .collect(toSet());
  }

  static Stream<String> getPossibleChildren(String superClass) {
    Set<String> children = POSSIBLE_CHILDREN_BY_CONTAINER.get(superClass);
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
  public static boolean isSubtypeOf(String scoutTypeFqn, String scoutSuperTypeFqn) {
    if (Objects.equals(scoutTypeFqn, scoutSuperTypeFqn)) {
      return true;
    }
    Set<String> directSuperTypes = INTERFACE_HIERARCHY.get(scoutTypeFqn);
    return !(directSuperTypes == null || directSuperTypes.isEmpty()) && isSubtypeOfRec(scoutSuperTypeFqn, directSuperTypes);
  }

  private static boolean isSubtypeOfRec(String scoutTypeFqn, Collection<String> directSuperTypes) {
    if (directSuperTypes.contains(scoutTypeFqn)) {
      return true;
    }

    for (String s : directSuperTypes) {
      boolean found = isSubtypeOf(s, scoutTypeFqn);
      if (found) {
        return true;
      }
    }
    return false;
  }
}
