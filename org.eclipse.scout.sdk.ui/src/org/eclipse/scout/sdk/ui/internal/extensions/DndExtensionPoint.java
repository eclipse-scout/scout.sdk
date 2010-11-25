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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.IDragSourceDelegator;
import org.eclipse.scout.sdk.ui.extensions.IDropTargetDelegator;

public class DndExtensionPoint {

  private static final String dropDelegatorAttName = "dropTargetDelegator";
  private static final String dragDelegatorAttName = "dragSourceDelegator";
  private static DndExtensionPoint instance = new DndExtensionPoint();

  private List<IDragSourceDelegator> m_dragSourceDelegators;
  private List<IDropTargetDelegator> m_dropTargetDelegators;

  private DndExtensionPoint() {
    init();
  }

  private void init() {
    List<IDragSourceDelegator> dragDelegators = new ArrayList<IDragSourceDelegator>();
    List<IDropTargetDelegator> dropDelegators = new ArrayList<IDropTargetDelegator>();
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, "outlineDragAndDrop");
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        try {
          if (element.getAttribute(dragDelegatorAttName) != null) {
            IDragSourceDelegator dragDelegator = (IDragSourceDelegator) element.createExecutableExtension(dragDelegatorAttName);
            dragDelegators.add(dragDelegator);
          }
          else if (element.getAttribute(dropDelegatorAttName) != null) {
            IDropTargetDelegator dropDelegator = (IDropTargetDelegator) element.createExecutableExtension(dropDelegatorAttName);
            dropDelegators.add(dropDelegator);
          }
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not create an executable extension of point '" + extension.getExtensionPointUniqueIdentifier() + "'.");
        }
      }
    }
    m_dropTargetDelegators = dropDelegators;
    m_dragSourceDelegators = dragDelegators;
  }

  public static IDragSourceDelegator[] getDragSourceDelegators() {
    return instance.getDragSourceDelegatorsImpl();
  }

  private IDragSourceDelegator[] getDragSourceDelegatorsImpl() {
    return m_dragSourceDelegators.toArray(new IDragSourceDelegator[m_dragSourceDelegators.size()]);
  }

  public static IDropTargetDelegator[] getDropTargetDelegators() {
    return instance.getDropTargetDelegatorsImpl();
  }

  private IDropTargetDelegator[] getDropTargetDelegatorsImpl() {
    return m_dropTargetDelegators.toArray(new IDropTargetDelegator[m_dropTargetDelegators.size()]);
  }

}
