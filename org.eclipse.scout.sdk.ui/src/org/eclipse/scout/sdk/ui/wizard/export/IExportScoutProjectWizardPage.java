package org.eclipse.scout.sdk.ui.wizard.export;

import org.eclipse.scout.sdk.ui.internal.extensions.export.ExportScoutProjectEntry;

public interface IExportScoutProjectWizardPage {

  boolean isNodesSelected(String... entryIds);

  ExportScoutProjectEntry[] getSelectedEntries();
}
