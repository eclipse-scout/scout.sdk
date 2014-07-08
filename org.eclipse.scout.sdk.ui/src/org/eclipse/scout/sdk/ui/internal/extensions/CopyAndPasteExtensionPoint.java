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
import org.eclipse.scout.sdk.ui.extensions.ICopySourceDelegator;
import org.eclipse.scout.sdk.ui.extensions.IPasteTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public final class CopyAndPasteExtensionPoint {

  // name of extension point as defined in XL
  private static final String EXTENSION_POINT_NAME = "outlineCopyAndPaste";

  // name of extension point element as defined in XML
  private static final String PASTE_DELEGATOR_ATT_NAME = "pasteTargetDelegator";
  private static final String COPY_DELEGATOR_ATT_NAME = "copySourceDelegator";

  // list for delegates
  private static List<IPasteTargetDelegator> pasteTargetDelegates = null;
  private static List<ICopySourceDelegator> copySourceDelegates = null;

  private CopyAndPasteExtensionPoint() {
  }

  private static synchronized void init() {
    if (pasteTargetDelegates == null || copySourceDelegates == null) {
      // temporary lists for delegates
      List<IPasteTargetDelegator> pasteDelegators = new ArrayList<IPasteTargetDelegator>();
      List<ICopySourceDelegator> copyDelegators = new ArrayList<ICopySourceDelegator>();

      // retrieve the extensions for the extension point
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_POINT_NAME);
      IExtension[] extensions = xp.getExtensions();

      // loop over all extension and add the corresponding ones to the temporary lists
      for (IExtension extension : extensions) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (IConfigurationElement element : elements) {
          try {
            if (element.getAttribute(PASTE_DELEGATOR_ATT_NAME) != null) {
              IPasteTargetDelegator pasteDelegator = (IPasteTargetDelegator) element.createExecutableExtension(PASTE_DELEGATOR_ATT_NAME);
              pasteDelegators.add(pasteDelegator);
            }
            else if (element.getAttribute(COPY_DELEGATOR_ATT_NAME) != null) {
              ICopySourceDelegator copyDelegator = (ICopySourceDelegator) element.createExecutableExtension(COPY_DELEGATOR_ATT_NAME);
              copyDelegators.add(copyDelegator);
            }
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("Could not create an executable extension of point '" + extension.getExtensionPointUniqueIdentifier() + "'.");
          }
        }
      }
      pasteTargetDelegates = CollectionUtility.arrayList(pasteDelegators);
      copySourceDelegates = CollectionUtility.arrayList(copyDelegators);
    }
  }

  public static List<IPasteTargetDelegator> getPasteTargetDelegators() {
    init();
    return CollectionUtility.arrayList(pasteTargetDelegates);
  }

  public static List<ICopySourceDelegator> getCopySourceDelegators() {
    init();
    return CollectionUtility.arrayList(copySourceDelegates);
  }
}
