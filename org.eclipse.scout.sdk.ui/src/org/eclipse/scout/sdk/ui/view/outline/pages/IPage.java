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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.internal.extensions.ExplorerPageExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.view.outline.DirtyUpdateManager;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.RefreshOutlineSubTreeJob;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link IPage}</h3> Represents a single node in the Scout Explorer
 * 
 * @see IScoutExplorerPart
 * @since 3.7.0
 */
public interface IPage extends IAdaptable {

  /**
   * @return a unique identifier of the page (e.g. the fully qualified class name)
   * @see IScoutPageConstants
   */
  String getPageId();

  /**
   * Specifies the label text to display in the tree.<br>
   * The name should be set by the page itself.
   * 
   * @return the label of this node in the tree
   */
  String getName();

  /**
   * gets the image descriptor of the icon of this page
   * 
   * @param descriptor
   */
  void setImageDescriptor(ImageDescriptor descriptor);

  /**
   * Sets the parent of this page. This also adds this page to the children of the given parent.
   * 
   * @param parent
   *          the new parent
   */
  void setParent(IPage parent);

  /**
   * gets the parent page.
   * 
   * @return the parent page
   */
  IPage getParent();

  /**
   * gets the order of this page
   * 
   * @return the order
   */
  int getOrder();

  /**
   * Gets the scout bundle this page belongs to or null if this page is not below a scout bundle.
   * 
   * @return the scout bundle or null
   * @see IScoutBundle
   */
  IScoutBundle getScoutBundle();

  /**
   * Gets the Scout Explorer this page belongs to.
   * 
   * @return the Scout Explorer this page belongs to or null.
   */
  ScoutExplorerPart getOutlineView();

  /**
   * Adds the given page to the child list of this page.
   * 
   * @param childPage
   *          The page to add (may not be null!)
   */
  void addChild(IPage childPage);

  /**
   * Removes the given page from the child list of this page
   * 
   * @param childPage
   *          The page to remove (may not be null!).
   * @return true if the page has been removed, false otherwise (e.g. if the page could not be found).
   */
  boolean removeChild(IPage childPage);

  /**
   * Unloads this page and all its children recursively. Triggers the {@link IPage#unloadPage()} method for each page
   * unloaded. After this method call this page has no children anymore and reports as not loaded (see
   * {@link IPage#isChildrenLoaded()}.
   */
  void unloadChildren();

  /**
   * Gets an array of all direct children of this page
   * 
   * @return an array of all direct children of this page
   */
  IPage[] getChildArray();

  /**
   * Gets an array of all direct children of this page matching the given filter.
   * 
   * @param filter
   *          the filter or null
   * @return all direct child pages matching the given filter
   */
  IPage[] getChildArray(IPageFilter filter);

  /**
   * @return an unmodifiable list containing all direct children of this page
   */
  List<IPage> getChildren();

  /**
   * After the page has been loaded this method returns if this page has child pages.
   * If the page has not been loaded yet, this method always returns false.
   * 
   * @return true if the page has been loaded and this page has child pages.
   * @see IPage#isChildrenLoaded()
   */
  boolean hasChildren();

  /**
   * Specifies if the children of this page have already been loaded.
   * 
   * @return true if the children are loaded, false otherwise.
   * @see IPage#loadChildren()
   */
  boolean isChildrenLoaded();

  /**
   * Specifies if this page should load its children when the Scout Explorer is initialized.<br>
   * This must be true if the page should be expanded by default. Has only an effect, if all parents of this page also
   * have isInitiallyLoaded() set to true.
   * 
   * @return true if the page should load its children when the scout explorer is initialized.
   */
  boolean isInitiallyLoaded();

  /**
   * Loads all children of this page and marks the page as loaded (see {@link IPage#isChildrenLoaded()}).<br>
   * If available, also extensions of this page are loaded (see {@link ExplorerPageExtensionPoint}).
   */
  void loadChildren();

  /**
   * Refreshes this page.<br>
   * The default implementation just marks the page as dirty (see {@link IPage#markStructureDirty()}).<br>
   * Pages may add special refresh behavior if the clearCache flag is set (e.g. completely recalculate all children).
   * 
   * @param clearCache
   *          true if the user pressed shift+f5, false if the user just pressed f5. if true, pages should completely
   *          recalculate the children (e.g. refresh
   *          the type hierarchies).
   */
  void refresh(boolean clearCache);

  /**
   * returns the quality (e.g. Error) of this page
   * 
   * @return one of the {@link IMarker#SEVERITY_*} constants
   */
  int getQuality();

  /**
   * Accepts the given visitor to this page and (potentially) its children.<br>
   * If the visitor will continue can be controlled by the visitor or the page itself.
   * 
   * @param visitor
   *          the visitor to apply.
   * @return one of {@link INodeVisitor#CONTINUE}, {@link INodeVisitor#CONTINUE_BRANCH}, {@link INodeVisitor#CANCEL},
   *         {@link INodeVisitor#CANCEL_SUBTREE}
   */
  int accept(INodeVisitor visitor);

  /**
   * Marks this page as dirty. This will trigger the Scout Explorer to reload this part of the tree asynchronously.
   * 
   * @see DirtyUpdateManager
   * @see RefreshOutlineSubTreeJob
   */
  void markStructureDirty();

  /**
   * can be overridden to remove listeners and to some clean up stuff.
   */
  void unloadPage();

  /**
   * Specifies if this page is a folder. If true, the page will get another default image and contains a filter section
   * in
   * its property part (if available).
   * 
   * @return true if it is a folder.
   */
  boolean isFolder();
}
