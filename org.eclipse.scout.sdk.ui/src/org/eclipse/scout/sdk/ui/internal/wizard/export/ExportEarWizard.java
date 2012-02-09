package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.export.ExportEarOperation;
import org.eclipse.scout.sdk.ui.internal.extensions.ear.EarEntry;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizard;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportEarWizard extends AbstractWorkspaceWizard implements IScoutEarExportWizard {
  private final ExportEarWizardPage m_page1;
  private final IScoutProject m_project;

  public ExportEarWizard(IScoutProject project) {
    m_project = project;
    m_page1 = new ExportEarWizardPage(project);
    setWindowTitle(Texts.get("ExportToEAR"));
    addPage(m_page1);
  }

  @Override
  public IScoutEarExportWizardPage getExportWizardPage() {
    return m_page1;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // get all selected entries
    EarEntry[] selectedEntries = m_page1.getSelectedEntries();

    // allow each entry to create its artefacts
    ArrayList<File> earModules = new ArrayList<File>();
    for (EarEntry entry : selectedEntries) {
      File module = entry.getHandler().createModule(this, monitor, workingCopyManager);
      if (module != null && module.exists()) {
        earModules.add(module);
      }
    }

    if (earModules.size() > 0) {
      // start ear packager
      ExportEarOperation op = new ExportEarOperation();
      op.addModule(earModules.toArray(new File[earModules.size()]));
      op.setEarFileName(m_page1.getEarFile().getAbsolutePath());
      op.validate();
      op.run(monitor, workingCopyManager);
    }
    return true;
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public IScoutProject getProject() {
    return m_project;
  }
}
