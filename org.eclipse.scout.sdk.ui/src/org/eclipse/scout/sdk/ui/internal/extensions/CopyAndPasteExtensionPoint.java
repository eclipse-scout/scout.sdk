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
import org.eclipse.scout.sdk.ui.extensions.ICopySourceDelegator;
import org.eclipse.scout.sdk.ui.extensions.IPasteTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public final class CopyAndPasteExtensionPoint {

  // name of extension point as defined in XL
  private static final String extensionPointName = "outlineCopyAndPaste";

  // name of extension point element as defined in XML
  private static final String pasteDelegatorAttName = "pasteTargetDelegator";
  private static final String copyDelegatorAttName = "copySourceDelegator";

  // Singleton
  private static CopyAndPasteExtensionPoint instance = new CopyAndPasteExtensionPoint();

  // list for delegates
  private List<IPasteTargetDelegator> m_pasteTargetDelegators;
  private List<ICopySourceDelegator> m_copySourceDelegators;

  private CopyAndPasteExtensionPoint() {
    init();
  }

  private void init() {
    // temporary lists for delegators
    List<IPasteTargetDelegator> pasteDelegators = new ArrayList<IPasteTargetDelegator>();
    List<ICopySourceDelegator> copyDelegators = new ArrayList<ICopySourceDelegator>();

    // retrieve the extensions for the extension point
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, extensionPointName);
    IExtension[] extensions = xp.getExtensions();

    // loop over all extension and add the corresponding ones to the temporary lists
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        try {
          if (element.getAttribute(pasteDelegatorAttName) != null) {
            IPasteTargetDelegator pasteDelegator = (IPasteTargetDelegator) element.createExecutableExtension(pasteDelegatorAttName);
            pasteDelegators.add(pasteDelegator);
          }
          else if (element.getAttribute(copyDelegatorAttName) != null) {
            ICopySourceDelegator copyDelegator = (ICopySourceDelegator) element.createExecutableExtension(copyDelegatorAttName);
            copyDelegators.add(copyDelegator);
          }
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("Could not create an executable extension of point '" + extension.getExtensionPointUniqueIdentifier() + "'.");
        }
      }
    }
    m_pasteTargetDelegators = pasteDelegators;
    m_copySourceDelegators = copyDelegators;
  }

  public static IPasteTargetDelegator[] getPasteTargetDelegators() {
    return instance.getPasteTargetDelegatorsImpl();
  }

  private IPasteTargetDelegator[] getPasteTargetDelegatorsImpl() {
    return m_pasteTargetDelegators.toArray(new IPasteTargetDelegator[m_pasteTargetDelegators.size()]);
  }

  public static ICopySourceDelegator[] getCopySourceDelegators() {
    return instance.getCopySourceDelegatorsImpl();
  }

  private ICopySourceDelegator[] getCopySourceDelegatorsImpl() {
    return m_copySourceDelegators.toArray(new ICopySourceDelegator[m_copySourceDelegators.size()]);
  }
}
