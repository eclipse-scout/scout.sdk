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
package org.eclipse.scout.sdk.ui.view.outline.pages;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.workspace.IScoutElement;

public interface IPage {

  /**
   * @return a unique identifier of the page usually the fully qualified class name
   */
  String getPageId();

  String getName();

  void setImageDescriptor(ImageDescriptor descriptor);

  void setParent(IPage parent);

  IPage getParent();

  int getOrder();

  IScoutElement getScoutResource();

  ScoutExplorerPart getOutlineView();

  void addChild(IPage childPage);

  boolean removeChild(IPage childPage);

  void unloadChildren();

  IPage[] getChildArray();

  IPage[] getChildArray(IPageFilter filter);

  List<IPage> getChildren();

  boolean hasChildren();

  boolean isChildrenLoaded();

  boolean isInitiallyLoaded();

  void loadChildren();

  void refresh(boolean clearCache);

  /**
   * @return one of the {@link IMarker#SEVERITY_*} constants
   */
  int getQuality();

  int accept(INodeVisitor visitor);

  void markStructureDirty();

  /**
   * can be overridden to remove listeners and to some clean up stuff.
   */
  void unloadPage();

  /**
   * @return
   */
  boolean isFolder();
}
