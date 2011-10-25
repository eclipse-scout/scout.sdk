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
package org.eclipse.scout.nls.sdk.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.osgi.framework.Bundle;

/**
 * <h4>ExportExtensionPojnts</h4>
 * 
 * @author Andreas Hoegger
 * @since 1.1.0 (11.11.2010)
 */
public class NlsExportImportExtensionPoints {

  private static final NlsExportImportExtensionPoints instance = new NlsExportImportExtensionPoints();
  public static final String EXTENSION_POINT_ID_NLS_EXPORTER = "nlsExporter";
  public static final String EXTENSION_POINT_ID_NLS_IMPORTER = "nlsImporter";
  HashMap<String/*extensionPointID*/, WizardExtension[] /*extensions*/> m_extensions;

  private NlsExportImportExtensionPoints() {
    m_extensions = new HashMap<String, WizardExtension[]>();
    init();
  }

  private void init() {
    m_extensions.put(EXTENSION_POINT_ID_NLS_EXPORTER, loadExtensionPoints(EXTENSION_POINT_ID_NLS_EXPORTER));
    m_extensions.put(EXTENSION_POINT_ID_NLS_IMPORTER, loadExtensionPoints(EXTENSION_POINT_ID_NLS_IMPORTER));
  }

  @SuppressWarnings("unchecked")
  private WizardExtension[] loadExtensionPoints(String id) {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    // export
    List<WizardExtension> wizardExtensions = new ArrayList<WizardExtension>();

    IExtensionPoint xp = reg.getExtensionPoint(NlsCore.PLUGIN_ID, id);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        try {
          WizardExtension wizardExt = new WizardExtension();
          String attWizard = element.getAttribute("wizard");
          if (!StringUtility.isNullOrEmpty(attWizard)) {
            Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
            Class wizard = contributerBundle.loadClass(attWizard);
            if (AbstractImportExportWizard.class.isAssignableFrom(wizard)) {
              wizardExt.setWizard(wizard);
            }
            else {
              NlsCore.logError("extension '" + extension.getExtensionPointUniqueIdentifier() + "' has a wizard not instance of '" + AbstractImportExportWizard.class.getName() + "'. Ignoring extension.");
              continue;
            }
          }
          else {
            continue;
          }
          String attName = element.getAttribute("name");
          if (!StringUtility.isNullOrEmpty(attName)) {
            wizardExt.setName(attName);
          }
          else {
            continue;
          }
          wizardExtensions.add(wizardExt);
        }
        catch (ClassNotFoundException e) {
          NlsCore.logError("could not create an executable extension of point '" + extension.getExtensionPointUniqueIdentifier() + "'.");
        }
      }
    }
    return wizardExtensions.toArray(new WizardExtension[wizardExtensions.size()]);
  }

  public static WizardExtension[] getExtensions(String extensionPointId) {
    return instance.getExtensionsImpl(extensionPointId);
  }

  private WizardExtension[] getExtensionsImpl(String extensionPointId) {
    return m_extensions.get(extensionPointId);
  }
}
