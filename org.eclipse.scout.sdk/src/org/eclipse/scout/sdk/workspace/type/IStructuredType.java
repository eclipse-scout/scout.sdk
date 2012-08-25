/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.type;

import java.io.PrintStream;

import org.eclipse.jdt.core.IJavaElement;

/**
 *
 */
public interface IStructuredType {

  public static enum CATEGORIES {
    FIELD_LOGGER,
    FIELD_STATIC,
    FIELD_MEMBER,
    FIELD_UNKNOWN,
    ENUM,
    METHOD_CONSTRUCTOR,
    METHOD_CONFIG_PROPERTY,
    METHOD_CONFIG_EXEC,
    METHOD_FORM_DATA_BEAN,
    METHOD_OVERRIDDEN,
    METHOD_START_HANDLER,
    METHOD_INNER_TYPE_GETTER,
    METHOD_LOCAL_BEAN,
    METHOD_UNCATEGORIZED,
    TYPE_FORM_FIELD,
    TYPE_COLUMN,
    TYPE_CODE,
    TYPE_FORM,
    TYPE_TABLE,
    TYPE_ACTIVITY_MAP,
    TYPE_TREE,
    TYPE_CALENDAR,
    TYPE_CALENDAR_ITEM_PROVIDER,
    TYPE_WIZARD,
    TYPE_WIZARD_STEP,
    TYPE_MENU,
    TYPE_VIEW_BUTTON,
    TYPE_TOOL_BUTTON,
    TYPE_KEYSTROKE,
    TYPE_COMPOSER_ATTRIBUTE,
    TYPE_COMPOSER_ENTRY,
    TYPE_FORM_HANDLER,
    TYPE_UNCATEGORIZED
  }

  /**
   * @param methodName
   * @return
   */
  IJavaElement getSiblingMethodFieldGetter(String methodName);

  /**
   * @param methodName
   * @return
   */
  IJavaElement getSiblingMethodConfigExec(String methodName);

  /**
   * @param category
   * @return
   */
  IJavaElement getSibling(CATEGORIES category);

  IJavaElement[] getElements(CATEGORIES category);

  <T extends IJavaElement> T[] getElements(CATEGORIES category, Class<T> clazz);

  /**
   * @param methodName
   * @return
   */
  IJavaElement getSiblingMethodConfigGetConfigured(String methodName);

  /**
   * @param methodName
   * @return
   */
  IJavaElement getSiblingMethodStartHandler(String methodName);

  /**
   * @param keyStrokeName
   * @return
   */
  IJavaElement getSiblingTypeKeyStroke(String keyStrokeName);

  /**
   * @param attributeName
   * @return
   */
  IJavaElement getSiblingComposerAttribute(String attributeName);

  /**
   * @param entityName
   * @return
   */
  IJavaElement getSiblingComposerEntity(String entityName);

  /**
   * @param formHandlerName
   * @return
   */
  IJavaElement getSiblingTypeFormHandler(String formHandlerName);

  /**
   * @param printer
   */
  void print(PrintStream printer);

}
