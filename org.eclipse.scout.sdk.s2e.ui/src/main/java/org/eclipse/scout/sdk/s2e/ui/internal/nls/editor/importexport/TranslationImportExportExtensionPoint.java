/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractImportExportWizard;

/**
 * <h4>ExportExtensionPoints</h4>
 *
 * @since 1.1.0 (11.11.2010)
 */
public final class TranslationImportExportExtensionPoint {

  public static final String EXTENSION_POINT_ID_NLS_EXPORTER = "nlsExporter";
  public static final String EXTENSION_POINT_ID_NLS_IMPORTER = "nlsImporter";

  @SuppressWarnings("StaticCollection")
  private static volatile List<TranslationImportExportWizardExtension> importers;
  @SuppressWarnings("StaticCollection")
  private static volatile List<TranslationImportExportWizardExtension> exporters;

  private TranslationImportExportExtensionPoint() {
  }

  public static List<TranslationImportExportWizardExtension> getImporters() {
    var result = importers;
    if (result != null) {
      return result;
    }
    return loadImporters();
  }

  private static synchronized List<TranslationImportExportWizardExtension> loadImporters() {
    var result = importers;
    if (result != null) {
      return result;
    }

    result = loadExtensionPoints(EXTENSION_POINT_ID_NLS_IMPORTER);
    importers = result;
    return result;
  }

  public static List<TranslationImportExportWizardExtension> getExporters() {
    var result = exporters;
    if (result != null) {
      return result;
    }
    return loadExporters();
  }

  private static synchronized List<TranslationImportExportWizardExtension> loadExporters() {
    var result = exporters;
    if (result != null) {
      return result;
    }

    result = loadExtensionPoints(EXTENSION_POINT_ID_NLS_EXPORTER);
    exporters = result;
    return result;
  }

  @SuppressWarnings("unchecked")
  private static List<TranslationImportExportWizardExtension> loadExtensionPoints(String id) {
    var reg = Platform.getExtensionRegistry();
    Collection<TranslationImportExportWizardExtension> wizardExtensions = new ArrayList<>();
    var xp = reg.getExtensionPoint(S2ESdkUiActivator.PLUGIN_ID, id);
    var exts = xp.getExtensions();
    for (var extension : exts) {
      var elements = extension.getConfigurationElements();
      for (var element : elements) {
        try {
          var attWizard = element.getAttribute("wizard");
          var attName = element.getAttribute("name");
          if (Strings.isBlank(attWizard) || Strings.isBlank(attName)) {
            SdkLog.warning("Invalid import/export wizard extension: {}. Name or wizard missing.", extension.getExtensionPointUniqueIdentifier());
            continue;
          }

          var contributingBundle = Platform.getBundle(extension.getNamespaceIdentifier());
          var wizard = contributingBundle.loadClass(attWizard);
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
    return List.copyOf(wizardExtensions);
  }
}
