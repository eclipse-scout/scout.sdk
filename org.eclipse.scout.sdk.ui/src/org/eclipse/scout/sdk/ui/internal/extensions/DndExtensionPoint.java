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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ui.extensions.IDragSourceDelegator;
import org.eclipse.scout.sdk.ui.extensions.IDropTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public final class DndExtensionPoint {

  public static final String DROP_DELEGATOR_ATTR_NAME = "dropTargetDelegator";
  public static final String DRAG_DELEGATOR_ATTR_NAME = "dragSourceDelegator";
  public static final String EXTENSION_NAME = "outlineDragAndDrop";

  private static List<IDragSourceDelegator> dragSourceDelegates = null;
  private static List<IDropTargetDelegator> dropTargetDelegates = null;

  private DndExtensionPoint() {
  }

  private static synchronized void init() {
    if (dragSourceDelegates == null || dropTargetDelegates == null) {
      List<IDragSourceDelegator> dragDelegators = new ArrayList<>();
      List<IDropTargetDelegator> dropDelegators = new ArrayList<>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_NAME);
      IExtension[] extensions = xp.getExtensions();
      for (IExtension extension : extensions) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (IConfigurationElement element : elements) {
          try {
            if (element.getAttribute(DRAG_DELEGATOR_ATTR_NAME) != null) {
              IDragSourceDelegator dragDelegator = (IDragSourceDelegator) element.createExecutableExtension(DRAG_DELEGATOR_ATTR_NAME);
              dragDelegators.add(dragDelegator);
            }
            else if (element.getAttribute(DROP_DELEGATOR_ATTR_NAME) != null) {
              IDropTargetDelegator dropDelegator = (IDropTargetDelegator) element.createExecutableExtension(DROP_DELEGATOR_ATTR_NAME);
              dropDelegators.add(dropDelegator);
            }
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("could not create an executable extension of point '" + extension.getExtensionPointUniqueIdentifier() + "'.", e);
          }
        }
      }
      dropTargetDelegates = CollectionUtility.arrayList(dropDelegators);
      dragSourceDelegates = CollectionUtility.arrayList(dragDelegators);
    }
  }

  public static List<IDragSourceDelegator> getDragSourceDelegators() {
    init();
    return CollectionUtility.arrayList(dragSourceDelegates);
  }

  public static List<IDropTargetDelegator> getDropTargetDelegators() {
    init();
    return CollectionUtility.arrayList(dropTargetDelegates);
  }
}
