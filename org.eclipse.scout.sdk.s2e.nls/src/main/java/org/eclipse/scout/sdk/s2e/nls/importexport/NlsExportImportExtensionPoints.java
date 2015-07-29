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
package org.eclipse.scout.sdk.s2e.nls.importexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.osgi.framework.Bundle;

/**
 * <h4>ExportExtensionPojnts</h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (11.11.2010)
 */
public final class NlsExportImportExtensionPoints {

  public static final String EXTENSION_POINT_ID_NLS_EXPORTER = "nlsExporter";
  public static final String EXTENSION_POINT_ID_NLS_IMPORTER = "nlsImporter";

  private static Map<String/*extensionPointID*/, List<WizardExtension> /*extensions*/> extensions = null;

  private NlsExportImportExtensionPoints() {
  }

  private static synchronized Map<String/*extensionPointID*/, List<WizardExtension> /*extensions*/> getExtensions() {
    if (extensions == null) {
      Map<String, List<WizardExtension>> tmp = new HashMap<>(2);
      tmp.put(EXTENSION_POINT_ID_NLS_EXPORTER, loadExtensionPoints(EXTENSION_POINT_ID_NLS_EXPORTER));
      tmp.put(EXTENSION_POINT_ID_NLS_IMPORTER, loadExtensionPoints(EXTENSION_POINT_ID_NLS_IMPORTER));
      extensions = tmp;
    }
    return extensions;
  }

  @SuppressWarnings("unchecked")
  private static List<WizardExtension> loadExtensionPoints(String id) {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    // export
    List<WizardExtension> wizardExtensions = new ArrayList<>();

    IExtensionPoint xp = reg.getExtensionPoint(NlsCore.PLUGIN_ID, id);
    IExtension[] exts = xp.getExtensions();
    for (IExtension extension : exts) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        try {
          WizardExtension wizardExt = new WizardExtension();
          String attWizard = element.getAttribute("wizard");
          if (attWizard != null && attWizard.length() > 0) {
            Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
            Class<?> wizard = contributerBundle.loadClass(attWizard);
            if (AbstractImportExportWizard.class.isAssignableFrom(wizard)) {
              wizardExt.setWizard((Class<? extends AbstractImportExportWizard>) wizard);
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
          if (attName != null && attName.length() > 0) {
            wizardExt.setName(attName);
          }
          else {
            continue;
          }
          wizardExtensions.add(wizardExt);
        }
        catch (ClassNotFoundException e) {
          NlsCore.logError("could not create an executable extension of point '" + extension.getExtensionPointUniqueIdentifier() + "'.", e);
        }
      }
    }
    return new ArrayList<>(wizardExtensions);
  }

  public static List<WizardExtension> getExtensions(String extensionPointId) {
    return new ArrayList<>(getExtensions().get(extensionPointId));
  }
}
