/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractImportExportWizard;
import org.osgi.framework.Bundle;

/**
 * <h4>ExportExtensionPojnts</h4>
 *
 * @since 1.1.0 (11.11.2010)
 */
public final class TranslationImportExportExtensionPoint {

  public static final String EXTENSION_POINT_ID_NLS_EXPORTER = "nlsExporter";
  public static final String EXTENSION_POINT_ID_NLS_IMPORTER = "nlsImporter";

  private static volatile List<TranslationImportExportWizardExtension> importers;
  private static volatile List<TranslationImportExportWizardExtension> exporters;

  private TranslationImportExportExtensionPoint() {
  }

  public static List<TranslationImportExportWizardExtension> getImporters() {
    List<TranslationImportExportWizardExtension> result = importers;
    if (result != null) {
      return result;
    }
    return loadImporters();
  }

  private static synchronized List<TranslationImportExportWizardExtension> loadImporters() {
    List<TranslationImportExportWizardExtension> result = importers;
    if (result != null) {
      return result;
    }

    result = loadExtensionPoints(EXTENSION_POINT_ID_NLS_IMPORTER);
    importers = result;
    return result;
  }

  public static List<TranslationImportExportWizardExtension> getExporters() {
    List<TranslationImportExportWizardExtension> result = exporters;
    if (result != null) {
      return result;
    }
    return loadExporters();
  }

  private static synchronized List<TranslationImportExportWizardExtension> loadExporters() {
    List<TranslationImportExportWizardExtension> result = exporters;
    if (result != null) {
      return result;
    }

    result = loadExtensionPoints(EXTENSION_POINT_ID_NLS_EXPORTER);
    exporters = result;
    return result;
  }

  @SuppressWarnings("unchecked")
  private static List<TranslationImportExportWizardExtension> loadExtensionPoints(String id) {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    List<TranslationImportExportWizardExtension> wizardExtensions = new ArrayList<>();
    IExtensionPoint xp = reg.getExtensionPoint(S2ESdkUiActivator.PLUGIN_ID, id);
    IExtension[] exts = xp.getExtensions();
    for (IExtension extension : exts) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        try {
          String attWizard = element.getAttribute("wizard");
          String attName = element.getAttribute("name");
          if (Strings.isBlank(attWizard) || Strings.isBlank(attName)) {
            SdkLog.warning("Invalid import/export wizard extension: {}. Name or wizard missing.", extension.getExtensionPointUniqueIdentifier());
            continue;
          }

          Bundle contributingBundle = Platform.getBundle(extension.getNamespaceIdentifier());
          Class<?> wizard = contributingBundle.loadClass(attWizard);
          if (!AbstractImportExportWizard.class.isAssignableFrom(wizard)) {
            SdkLog.error("extension '{}' has a wizard not instance of '{}'. Ignoring extension.", extension.getExtensionPointUniqueIdentifier(), AbstractImportExportWizard.class.getName());
            continue;
          }

          wizardExtensions.add(new TranslationImportExportWizardExtension((Class<? extends AbstractImportExportWizard>) wizard, attName.trim()));
        }
        catch (ClassNotFoundException | RuntimeException | NoClassDefFoundError e) {
          SdkLog.error("Could not create an executable extension of point '{}'.", extension.getExtensionPointUniqueIdentifier(), e);
        }
      }
    }
    return unmodifiableList(new ArrayList<>(wizardExtensions));
  }
}
