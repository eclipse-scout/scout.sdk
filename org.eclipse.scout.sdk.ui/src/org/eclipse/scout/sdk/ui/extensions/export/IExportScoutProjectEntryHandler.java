package org.eclipse.scout.sdk.ui.extensions.export;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public interface IExportScoutProjectEntryHandler {
  /**
   * called on every change on the export module selection page (first). This gives the handler the possibility to
   * contribute a validation status to the page.
   * 
   * @param wizard
   *          The export wizard. gives access to all pages and parameters.
   * @return The status for this handler
   */
  IStatus getStatus(IExportScoutProjectWizard wizard);

  /**
   * Specifies if this handler should be offered on the export module selection page (first page).
   * If this method returns false, the entry that belongs to this handler is not visible.
   * 
   * @param wizard
   *          The export wizard. gives access to all pages and parameters.
   * @return true if the entry should be displayed in the tree.
   */
  boolean isAvailable(IExportScoutProjectWizard wizard);

  /**
   * Specifies if the entry associated with this handler should be checked by default or not.
   * 
   * @return true if it should be checked by default, false otherwise.
   */
  boolean getDefaultSelection();

  /**
   * called when the selection of the entry tree changes.
   * 
   * @param wizard
   *          The export wizard. gives access to all pages and parameters.
   * @param selected
   *          true if the entry this handler belongs to is checked now, false otherwise.
   */
  void selectionChanged(IExportScoutProjectWizard wizard, boolean selected);

  /**
   * Called when the wizard is finished. Creates the module that belongs to this handler.
   * 
   * @param wizard
   *          The export wizard. gives access to all pages and parameters.
   * @param monitor
   * @param workingCopyManager
   * @return the created module artifact.
   * @throws CoreException
   */
  File createModule(IExportScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException;
}
