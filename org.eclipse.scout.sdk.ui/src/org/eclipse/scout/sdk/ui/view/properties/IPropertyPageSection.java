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
package org.eclipse.scout.sdk.ui.view.properties;

import java.util.List;

public interface IPropertyPageSection {

  String getName();

  String getDescription();

  int getLogicalHeight();

  /**
   * @return read only list of attributes, list may be modified by caller
   */
  List<Object> getProperties();

  int getPropertyCount();

  void addProperty(Object p);

  boolean isExpanded();

  void setExpanded(boolean expanded);

}
