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
package org.eclipse.scout.sdk.ui.internal.extensions;

/**
 * <h3>BCTypeNameTuple</h3> ...
 */
public class ScoutTypeDecorator {
  private final int m_index;
  private final String m_text;
  private final String m_fullyQualifiedName;
  private final String m_simpleName;
  private final String m_packageName;

  public ScoutTypeDecorator(int index, String fullyQualifiedName, String text) {
    this(index, text, fullyQualifiedName, fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.')), fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.')));
  }

  public ScoutTypeDecorator(int index, Class<?> clazz, String text) {
    this(index, text, clazz.getName(), clazz.getSimpleName(), clazz.getPackage().getName());
  }

  public ScoutTypeDecorator(int index, String text, String fullyQualifiedName, String simpleName, String packageName) {
    m_index = index;
    m_text = text;
    m_fullyQualifiedName = fullyQualifiedName;
    m_simpleName = simpleName;
    m_packageName = packageName;

  }

  public String getText() {
    return m_text;
  }

  public String getFullyQualifiedName() {
    return m_fullyQualifiedName;
  }

  public int getIndex() {
    return m_index;
  }

  public String getSimpleName() {
    return m_simpleName;
  }

  public String getPackageName() {
    return m_packageName;
  }

}
