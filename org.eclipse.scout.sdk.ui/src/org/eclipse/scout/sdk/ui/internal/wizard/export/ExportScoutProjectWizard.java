package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.export.ExportEarOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.export.ExportScoutProjectEntry;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ExportScoutProjectWizard extends AbstractWorkspaceWizard implements IExportScoutProjectWizard {
  private static final Pattern ALIAS_REGEX = Pattern.compile(".*products.*/([^-]*)-.*.product");

  private final ExportScoutProjectWizardPage m_page1;
  private final IScoutBundle m_project;

  private final String m_projectAlias;

  public ExportScoutProjectWizard(IScoutBundle project) {
    m_project = project;
    m_projectAlias = findProjectAlias();
    m_page1 = new ExportScoutProjectWizardPage(project);
    setWindowTitle(Texts.get("ExportScoutProject"));
    addPage(m_page1);
  }

  @Override
  public IExportScoutProjectWizardPage getExportWizardPage() {
    return m_page1;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // get all selected entries
    ExportScoutProjectEntry[] selectedEntries = m_page1.getSelectedEntries();

    // allow each entry to create its artifacts
    ArrayList<File> artifacts = new ArrayList<File>();
    for (ExportScoutProjectEntry entry : selectedEntries) {
      File module = entry.getHandler().createModule(this, monitor, workingCopyManager);
      if (module != null && module.exists()) {
        artifacts.add(module);
      }
    }

    if (artifacts.size() > 0) {
      if (m_page1.isExportEar()) {
        // start ear packager
        ExportEarOperation op = new ExportEarOperation();
        op.addModule(artifacts.toArray(new File[artifacts.size()]));
        op.setEarFileName(new File(m_page1.getTargetDirectory(), m_page1.getEarName()).getAbsolutePath());
        op.validate();
        op.run(monitor, workingCopyManager);
      }
      else {
        // do not pack the artifacts into an ear -> just move them to the destination
        for (File artifact : artifacts) {
          try {
            ResourceUtility.moveFile(artifact, m_page1.getTargetDirectory());
          }
          catch (IOException e) {
            throw new CoreException(new ScoutStatus(e));
          }
        }
      }

      for (File artifact : artifacts) {
        artifact.getParentFile().delete();
      }
    }
    return true;
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public IScoutBundle getProject() {
    return m_project;
  }

  @Override
  public String getProjectAlias() {
    return m_projectAlias;
  }

  private String findProjectAlias() {
    try {
      HashMap<String, Integer> aliasList = new HashMap<String, Integer>();

      // all product files that contain the selected project
      IResource[] prodFiles = ResourceUtility.getAllResources(ResourceFilters.getProductFileByContentFilter(false, getProject().getSymbolicName()));
      for (IResource f : prodFiles) {
        String alias = getAliasFromProductFile((IFile) f);
        if (alias != null) {
          Integer i = aliasList.get(alias);
          if (i == null) {
            i = 0;
          }
          aliasList.put(alias, ++i);
        }
      }

      String ret = null;
      Integer last = null;
      for (Entry<String, Integer> e : aliasList.entrySet()) {
        if (last == null || last < e.getValue()) {
          ret = e.getKey();
          last = e.getValue();
        }
      }
      return ret;
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning(e);
      return null;
    }
  }

  private String getAliasFromProductFile(IFile prodFile) {
    if (prodFile == null || !prodFile.exists()) {
      return null;
    }
    String path = prodFile.getFullPath().toString();
    Matcher m = ALIAS_REGEX.matcher(path);
    if (m.matches()) {
      return m.group(1);
    }
    return null;
  }
}
