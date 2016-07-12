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
package org.eclipse.scout.sdk.core.s.structured;

import java.util.EnumSet;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.structured.IStructuredType.Categories;

/**
 * <h3>{@link StructuredTypeFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class StructuredTypeFactory {

  private StructuredTypeFactory() {
  }

  public static IStructuredType createStructuredType(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_START_HANDLER, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED,
        Categories.TYPE_FORM_FIELD, Categories.TYPE_COLUMN, Categories.TYPE_CODE, Categories.TYPE_FORM, Categories.TYPE_TABLE, Categories.TYPE_TREE, Categories.TYPE_CALENDAR,
        Categories.TYPE_CALENDAR_ITEM_PROVIDER, Categories.TYPE_WIZARD, Categories.TYPE_WIZARD_STEP, Categories.TYPE_MENU, Categories.TYPE_VIEW_BUTTON, Categories.TYPE_KEYSTROKE,
        Categories.TYPE_COMPOSER_ATTRIBUTE, Categories.TYPE_COMPOSER_ENTRY, Categories.TYPE_FORM_HANDLER, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredButton(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredViewButton(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredToolButton(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredKeyStroke(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredMenu(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredColumn(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredActivityMap(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredDesktop(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_VIEW_BUTTON, Categories.TYPE_KEYSTROKE,
        Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredFormHandler(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_MEMBER, Categories.FIELD_STATIC, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
        Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredForm(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_START_HANDLER, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED,
        Categories.TYPE_FORM_FIELD, Categories.TYPE_KEYSTROKE, Categories.TYPE_FORM_HANDLER, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredOutline(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredFormField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.TYPE_MENU, Categories.METHOD_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredComposer(IType type) {
    EnumSet<Categories> enabled =
        EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY, Categories.METHOD_CONFIG_EXEC,
            Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_COMPOSER_ATTRIBUTE, Categories.TYPE_COMPOSER_ENTRY, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPageWithNodes(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPageWithTable(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TABLE, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTableField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TABLE, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTable(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_MENU, Categories.TYPE_COLUMN, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCompositeField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_FORM_FIELD, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCodeType(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_CODE, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCode(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_CODE, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTreeField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TREE, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPlannerField(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_TABLE, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredWizard(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_WIZARD_STEP, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredWizardStep(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCalendar(IType type) {
    EnumSet<Categories> enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_OVERRIDDEN, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED, Categories.TYPE_CALENDAR_ITEM_PROVIDER, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }
}
