package org.eclipse.scout.sdk.ui.extensions.ear;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public interface IEarEntryHandler {
  /**
   * called on every change on the EAR export module selection page (first). This gives the handler the possibility to
   * contribute a validation status to the page.
   * 
   * @param wizard
   *          The ear export wizard. gives access to all pages and parameters.
   * @return The status for this handler
   */
  IStatus getStatus(IScoutEarExportWizard wizard);

  /**
   * Specifies if this handler should be offered on the EAR export module selection page (first page).
   * If this method returns false, the EAR entry that belongs to this handler is not visible.
   * 
   * @param wizard
   *          The ear export wizard. gives access to all pages and parameters.
   * @return true if the entry should be displayed in the tree.
   */
  boolean isAvailable(IScoutEarExportWizard wizard);

  /**
   * called when the selection of the EAR entry tree changes.
   * 
   * @param wizard
   *          The ear export wizard. gives access to all pages and parameters.
   * @param selected
   *          true if the entry this handler belongs to is checked now, false otherwise.
   */
  void selectionChanged(IScoutEarExportWizard wizard, boolean selected);

  /**
   * Called when the wizard is finished. Creates the module that belongs to this handler.
   * 
   * @param wizard
   *          The ear export wizard. gives access to all pages and parameters.
   * @param monitor
   * @param workingCopyManager
   * @return the created module artefact that should be added to the EAR or null if this handler creates no artefacts.
   * @throws CoreException
   */
  File createModule(IScoutEarExportWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException;
}
