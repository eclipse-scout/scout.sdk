/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link ScoutModelHierarchy}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class ScoutModelHierarchy {

  private ScoutModelHierarchy() {
  }

  private static final Map<String /*container*/, Set<String>/* possible children */> POSSIBLE_CHILDREN_BY_CONTAINER = new HashMap<>();
  private static final Map<String /*container*/, Set<String>/* possible children */> INTERFACE_HIERARCHY = new HashMap<>();

  private static void addContainerElement(String container, String possibleChild) {
    addToMapSet(container, possibleChild, POSSIBLE_CHILDREN_BY_CONTAINER, 4);
  }

  private static void addInterfaceSuperType(String ifc, String superTypeIfc) {
    addToMapSet(ifc, superTypeIfc, INTERFACE_HIERARCHY, 4);
  }

  private static void addToMapSet(String first, String second, Map<String, Set<String>> store, int initialSetSize) {
    Set<String> val = store.get(first);
    if (val == null) {
      val = new HashSet<>(initialSetSize);
      store.put(first, val);
    }
    val.add(second);
  }

  static {
    // containers
    addContainerElement(IScoutRuntimeTypes.AbstractActionNode, IScoutRuntimeTypes.IActionNode);
    addContainerElement(IScoutRuntimeTypes.AbstractActionNodeExtension, IScoutRuntimeTypes.IActionNode);
    addContainerElement(IScoutRuntimeTypes.AbstractActionExtension, IScoutRuntimeTypes.IAction);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendarItemProvider, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendarItemProviderExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendar, IScoutRuntimeTypes.ICalendarItemProvider);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendar, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendarExtension, IScoutRuntimeTypes.ICalendarItemProvider);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendarExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractPlanner, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractPlannerExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTable, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTable, IScoutRuntimeTypes.ITableControl);
    addContainerElement(IScoutRuntimeTypes.AbstractTable, IScoutRuntimeTypes.IColumn);
    addContainerElement(IScoutRuntimeTypes.AbstractTable, IScoutRuntimeTypes.IKeyStroke);
    addContainerElement(IScoutRuntimeTypes.AbstractTableExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTableExtension, IScoutRuntimeTypes.ITableControl);
    addContainerElement(IScoutRuntimeTypes.AbstractTableExtension, IScoutRuntimeTypes.IColumn);
    addContainerElement(IScoutRuntimeTypes.AbstractTableExtension, IScoutRuntimeTypes.IKeyStroke);
    addContainerElement(IScoutRuntimeTypes.AbstractTree, IScoutRuntimeTypes.IKeyStroke);
    addContainerElement(IScoutRuntimeTypes.AbstractTree, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeExtension, IScoutRuntimeTypes.IKeyStroke);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeNode, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeNodeExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractPageWithTable, IScoutRuntimeTypes.ITable);
    addContainerElement(IScoutRuntimeTypes.AbstractPageWithTableExtension, IScoutRuntimeTypes.ITable);
    addContainerElement(IScoutRuntimeTypes.AbstractDesktop, IScoutRuntimeTypes.IAction);
    addContainerElement(IScoutRuntimeTypes.AbstractDesktopExtension, IScoutRuntimeTypes.IAction);
    addContainerElement(IScoutRuntimeTypes.AbstractButton, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractButtonExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendarField, IScoutRuntimeTypes.ICalendar);
    addContainerElement(IScoutRuntimeTypes.AbstractCalendarFieldExtension, IScoutRuntimeTypes.ICalendar);
    addContainerElement(IScoutRuntimeTypes.AbstractComposerField, IScoutRuntimeTypes.ITree);
    addContainerElement(IScoutRuntimeTypes.AbstractComposerFieldExtension, IScoutRuntimeTypes.ITree);
    addContainerElement(IScoutRuntimeTypes.AbstractGroupBox, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractGroupBoxExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractImageField, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractImageFieldExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractListBox, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractListBox, IScoutRuntimeTypes.ITable);
    addContainerElement(IScoutRuntimeTypes.AbstractListBoxExtension, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractListBoxExtension, IScoutRuntimeTypes.ITable);
    addContainerElement(IScoutRuntimeTypes.AbstractPlannerField, IScoutRuntimeTypes.IPlanner);
    addContainerElement(IScoutRuntimeTypes.AbstractPlannerFieldExtension, IScoutRuntimeTypes.IPlanner);
    addContainerElement(IScoutRuntimeTypes.AbstractRadioButtonGroup, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractRadioButtonGroupExtension, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractContentAssistField, IScoutRuntimeTypes.IContentAssistFieldTable);
    addContainerElement(IScoutRuntimeTypes.AbstractContentAssistFieldExtension, IScoutRuntimeTypes.IContentAssistFieldTable);
    addContainerElement(IScoutRuntimeTypes.AbstractTabBox, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTabBoxExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractTableField, IScoutRuntimeTypes.ITable);
    addContainerElement(IScoutRuntimeTypes.AbstractTableFieldExtension, IScoutRuntimeTypes.ITable);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeBox, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeBox, IScoutRuntimeTypes.ITree);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeBoxExtension, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeBoxExtension, IScoutRuntimeTypes.ITree);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeField, IScoutRuntimeTypes.ITree);
    addContainerElement(IScoutRuntimeTypes.AbstractTreeFieldExtension, IScoutRuntimeTypes.ITree);
    addContainerElement(IScoutRuntimeTypes.AbstractCompositeField, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractCompositeFieldExtension, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractFormField, IScoutRuntimeTypes.IKeyStroke);
    addContainerElement(IScoutRuntimeTypes.AbstractFormFieldExtension, IScoutRuntimeTypes.IKeyStroke);
    addContainerElement(IScoutRuntimeTypes.AbstractValueField, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractValueFieldExtension, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractForm, IScoutRuntimeTypes.IFormHandler);
    addContainerElement(IScoutRuntimeTypes.AbstractFormExtension, IScoutRuntimeTypes.IFormHandler);
    addContainerElement(IScoutRuntimeTypes.AbstractFormExtension, IScoutRuntimeTypes.IFormField);
    addContainerElement(IScoutRuntimeTypes.AbstractWizard, IScoutRuntimeTypes.IWizardStep);
    addContainerElement(IScoutRuntimeTypes.AbstractWizardExtension, IScoutRuntimeTypes.IWizardStep);
    addContainerElement(IScoutRuntimeTypes.AbstractCodeTypeWithGeneric, IScoutRuntimeTypes.ICode);
    addContainerElement(IScoutRuntimeTypes.AbstractCodeTypeWithGenericExtension, IScoutRuntimeTypes.ICode);
    addContainerElement(IScoutRuntimeTypes.AbstractCode, IScoutRuntimeTypes.ICode);
    addContainerElement(IScoutRuntimeTypes.AbstractCodeExtension, IScoutRuntimeTypes.ICode);
    addContainerElement(IScoutRuntimeTypes.AbstractDataModel, IScoutRuntimeTypes.IDataModelAttribute);
    addContainerElement(IScoutRuntimeTypes.AbstractDataModel, IScoutRuntimeTypes.IDataModelEntity);
    addContainerElement(IScoutRuntimeTypes.AbstractDataModelEntity, IScoutRuntimeTypes.IDataModelAttribute);
    addContainerElement(IScoutRuntimeTypes.AbstractDataModelEntity, IScoutRuntimeTypes.IDataModelEntity);
    addContainerElement(IScoutRuntimeTypes.AbstractDataModelEntity, IScoutRuntimeTypes.IDataModelEntity);
    addContainerElement(IScoutRuntimeTypes.AbstractDataModelEntityExtension, IScoutRuntimeTypes.IDataModelAttribute);
    addContainerElement(IScoutRuntimeTypes.IContextMenuOwner, IScoutRuntimeTypes.IMenu);
    addContainerElement(IScoutRuntimeTypes.AbstractExtension, IScoutRuntimeTypes.IExtension);

    // hierarchy
    addInterfaceSuperType(IScoutRuntimeTypes.IAction, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IAction, IScoutRuntimeTypes.IOrdered);
    addInterfaceSuperType(IScoutRuntimeTypes.IActionNode, IScoutRuntimeTypes.IAction);
    addInterfaceSuperType(IScoutRuntimeTypes.IBigDecimalField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IBooleanField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IButton, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.ICalendar, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.ICalendarField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.ICode, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.ICode, IScoutRuntimeTypes.IOrdered);
    addInterfaceSuperType(IScoutRuntimeTypes.ICodeType, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IColumn, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.ICompositeField, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.ICompositeFieldExtension, IScoutRuntimeTypes.IExtension);
    addInterfaceSuperType(IScoutRuntimeTypes.IContentAssistFieldTable, IScoutRuntimeTypes.ITable);
    addInterfaceSuperType(IScoutRuntimeTypes.IDataModelAttribute, IScoutRuntimeTypes.IOrdered);
    addInterfaceSuperType(IScoutRuntimeTypes.IDataModelEntity, IScoutRuntimeTypes.IOrdered);
    addInterfaceSuperType(IScoutRuntimeTypes.IDateField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IDesktopExtension, IScoutRuntimeTypes.IExtension);
    addInterfaceSuperType(IScoutRuntimeTypes.IFileChooserField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IFormExtension, IScoutRuntimeTypes.IExtension);
    addInterfaceSuperType(IScoutRuntimeTypes.IFormField, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IFormField, IScoutRuntimeTypes.IOrdered);
    addInterfaceSuperType(IScoutRuntimeTypes.IFormFieldExtension, IScoutRuntimeTypes.IExtension);
    addInterfaceSuperType(IScoutRuntimeTypes.IGroupBox, IScoutRuntimeTypes.ICompositeField);
    addInterfaceSuperType(IScoutRuntimeTypes.IGroupBox, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.IHtmlField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IImageField, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.IKeyStroke, IScoutRuntimeTypes.IAction);
    addInterfaceSuperType(IScoutRuntimeTypes.ILabelField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IListBox, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IListBox, IScoutRuntimeTypes.ICompositeField);
    addInterfaceSuperType(IScoutRuntimeTypes.ILongField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IMenu, IScoutRuntimeTypes.IActionNode);
    addInterfaceSuperType(IScoutRuntimeTypes.IOutline, IScoutRuntimeTypes.ITree);
    addInterfaceSuperType(IScoutRuntimeTypes.IOutline, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IOutline, IScoutRuntimeTypes.IOrdered);
    addInterfaceSuperType(IScoutRuntimeTypes.IPage, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IPageWithNodes, IScoutRuntimeTypes.IPage);
    addInterfaceSuperType(IScoutRuntimeTypes.IPageWithTable, IScoutRuntimeTypes.IPage);
    addInterfaceSuperType(IScoutRuntimeTypes.IPageWithTableExtension, IScoutRuntimeTypes.IExtension);
    addInterfaceSuperType(IScoutRuntimeTypes.IPlanner, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.IPlannerField, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.IProposalField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IRadioButton, IScoutRuntimeTypes.IButton);
    addInterfaceSuperType(IScoutRuntimeTypes.IRadioButtonGroup, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IRadioButtonGroup, IScoutRuntimeTypes.ICompositeField);
    addInterfaceSuperType(IScoutRuntimeTypes.ISequenceBox, IScoutRuntimeTypes.ICompositeField);
    addInterfaceSuperType(IScoutRuntimeTypes.ISmartField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.IStringField, IScoutRuntimeTypes.IValueField);
    addInterfaceSuperType(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.ICompositeField);
    addInterfaceSuperType(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.ITable, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.ITable, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.ITableControl, IScoutRuntimeTypes.IAction);
    addInterfaceSuperType(IScoutRuntimeTypes.ITableExtension, IScoutRuntimeTypes.IExtension);
    addInterfaceSuperType(IScoutRuntimeTypes.ITableField, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.ITree, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.ITreeField, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.IValueField, IScoutRuntimeTypes.IFormField);
    addInterfaceSuperType(IScoutRuntimeTypes.IValueField, IScoutRuntimeTypes.IContextMenuOwner);
    addInterfaceSuperType(IScoutRuntimeTypes.IViewButton, IScoutRuntimeTypes.IAction);
    addInterfaceSuperType(IScoutRuntimeTypes.IWizard, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IWizardStep, IScoutRuntimeTypes.ITypeWithClassId);
    addInterfaceSuperType(IScoutRuntimeTypes.IWizardStep, IScoutRuntimeTypes.IOrdered);
  }

  /**
   * Gets all interface fully qualified names of elements that may be added inside the given element.
   *
   * @param scoutClassFqn
   *          The fully qualified interface name of the container.
   * @return A {@link Set} with the fully qualified interface names.
   * @see IScoutRuntimeTypes
   */
  public static Set<String> getPossibleChildren(String scoutClassFqn) {
    Set<String> result = POSSIBLE_CHILDREN_BY_CONTAINER.get(scoutClassFqn);
    if (result == null || result.isEmpty()) {
      return Collections.emptySet();
    }
    return result;
  }

  /**
   * Checks if scoutTypeFqn is a subtype of scoutSuperTypeFqn.
   *
   * @param scoutTypeFqn
   *          The fully qualified interface name of the possible sub type.
   * @param scoutSuperTypeFqn
   *          The fully qualified interface name of the super type.
   * @return <code>true</code> if it is a subtype. <code>false</code> otherwise.
   */
  public static boolean isSubtypeOf(String scoutTypeFqn, String scoutSuperTypeFqn) {
    if (Objects.equals(scoutTypeFqn, scoutSuperTypeFqn)) {
      return true;
    }
    Set<String> directSuperTypes = INTERFACE_HIERARCHY.get(scoutTypeFqn);
    if (directSuperTypes == null || directSuperTypes.isEmpty()) {
      return false;
    }
    return isSubtypeOfRec(scoutSuperTypeFqn, directSuperTypes);
  }

  private static boolean isSubtypeOfRec(String scoutTypeFqn, Set<String> directSuperTypes) {
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
