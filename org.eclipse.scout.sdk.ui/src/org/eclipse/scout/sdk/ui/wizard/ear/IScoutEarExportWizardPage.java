package org.eclipse.scout.sdk.ui.wizard.ear;

import org.eclipse.scout.sdk.ui.internal.extensions.ear.EarEntry;

public interface IScoutEarExportWizardPage {

  boolean isNodesSelected(String... entryIds);

  EarEntry[] getSelectedEntries();
}
