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
package org.eclipse.scout.sdk.s2e.structuredtype;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.internal.structuredtype.ScoutStructuredType;
import org.eclipse.scout.sdk.s2e.structuredtype.IStructuredType.Categories;
import org.eclipse.scout.sdk.s2e.util.JdtTypeCache;

/**
 * <h3>{@link StructuredTypeFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class StructuredTypeFactory {

  private StructuredTypeFactory() {
  }

  private static boolean hierarchyContains(ITypeHierarchy h, String fqn, JdtTypeCache typeCache) throws CoreException {
    Set<IType> jdtTypes = typeCache.getTypes(fqn);
    for (IType t : jdtTypes) {
      if (h.contains(t)) {
        return true;
      }
    }
    return false;
  }

  public static IStructuredType createStructuredType(IType type, JdtTypeCache typeCache) throws CoreException {
    ITypeHierarchy supertypeHierarchy = type.newSupertypeHierarchy(null);
    if (supertypeHierarchy == null) {
      return null;
    }
    if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ICompositeField, typeCache)) {
      return createStructuredCompositeField(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ITableField, typeCache)) {
      return createStructuredTableField(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ITreeField, typeCache)) {
      return createStructuredTreeField(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IPlannerField, typeCache)) {
      return createStructuredPlannerField(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IComposerField, typeCache)) {
      return createStructuredComposer(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IDataModelAttribute, typeCache)) {
      return createStructuredComposer(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IDataModelEntity, typeCache)) {
      return createStructuredComposer(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IFormField, typeCache)) {
      return createStructuredFormField(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IForm, typeCache)) {
      return createStructuredForm(type, null);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ICalendar, typeCache)) {
      return createStructuredCalendar(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ICodeType, typeCache)) {
      return createStructuredCodeType(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ICode, typeCache)) {
      return createStructuredCode(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IDesktop, typeCache)) {
      return createStructuredDesktop(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IDesktopExtension, typeCache)) {
      return createStructuredDesktop(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IOutline, typeCache)) {
      return createStructuredOutline(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IPageWithNodes, typeCache)) {
      return createStructuredPageWithNodes(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IPageWithTable, typeCache)) {
      return createStructuredPageWithTable(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.ITable, typeCache)) {
      return createStructuredTable(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IWizard, typeCache)) {
      return createStructuredWizard(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IWizardStep, typeCache)) {
      return createStructuredWizardStep(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IMenu, typeCache)) {
      return createStructuredMenu(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IColumn, typeCache)) {
      return createStructuredColumn(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IActivityMap, typeCache)) {
      return createStructuredActivityMap(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IFormHandler, typeCache)) {
      return createStructuredFormHandler(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IKeyStroke, typeCache)) {
      return createStructuredKeyStroke(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IButton, typeCache)) {
      return createStructuredButton(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IViewButton, typeCache)) {
      return createStructuredViewButton(type);
    }
    else if (hierarchyContains(supertypeHierarchy, IRuntimeClasses.IToolButton, typeCache)) {
      return createStructuredToolButton(type);
    }
    else {
      S2ESdkActivator.logInfo("no structured type defined for type '" + type.getFullyQualifiedName() + "'.");
      return createUnknownStructuredType(type);
    }
  }

  /**
   * don not hang on this object.
   *
   * @param type
   * @return
   */
  private static IStructuredType createUnknownStructuredType(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_START_HANDLER, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED,
        Categories.TYPE_FORM_FIELD, Categories.TYPE_COLUMN, Categories.TYPE_CODE, Categories.TYPE_FORM, Categories.TYPE_TABLE, Categories.TYPE_ACTIVITY_MAP, Categories.TYPE_TREE, Categories.TYPE_CALENDAR,
        Categories.TYPE_CALENDAR_ITEM_PROVIDER, Categories.TYPE_WIZARD, Categories.TYPE_WIZARD_STEP, Categories.TYPE_MENU, Categories.TYPE_VIEW_BUTTON, Categories.TYPE_TOOL_BUTTON, Categories.TYPE_KEYSTROKE,
        Categories.TYPE_COMPOSER_ATTRIBUTE, Categories.TYPE_COMPOSER_ENTRY, Categories.TYPE_FORM_HANDLER, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredButton(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredViewButton(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredToolButton(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TOOL_BUTTON, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredKeyStroke(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredMenu(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredColumn(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredActivityMap(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredDesktop(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_VIEW_BUTTON, Categories.TYPE_TOOL_BUTTON, Categories.TYPE_KEYSTROKE,
        Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredFormHandler(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_MEMBER, Categories.FIELD_STATIC, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredForm(IType type) {
    return createStructuredForm(type, null);
  }

  public static IStructuredType createStructuredForm(IType type, ITypeHierarchy localHierarchy) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_START_HANDLER, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED,
        Categories.TYPE_FORM_FIELD, Categories.TYPE_KEYSTROKE, Categories.TYPE_FORM_HANDLER, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled, localHierarchy);
  }

  public static IStructuredType createStructuredOutline(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredFormField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.TYPE_MENU, Categories.METHOD_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredComposer(IType type) {
    EnumSet<Categories> enabled =
        EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
            Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_COMPOSER_ATTRIBUTE, Categories.TYPE_COMPOSER_ENTRY, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPageWithNodes(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPageWithTable(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TABLE, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTableField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TABLE, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTable(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_COLUMN, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCompositeField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_FORM_FIELD, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCodeType(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_CODE, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCode(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_CODE, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTreeField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TREE, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPlannerField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TABLE, Categories.TYPE_ACTIVITY_MAP, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredWizard(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_WIZARD_STEP, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredWizardStep(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCalendar(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_CALENDAR_ITEM_PROVIDER, Categories.TYPE_UNCATEGORIZED);
    return new ScoutStructuredType(type, enabled);
  }
}
