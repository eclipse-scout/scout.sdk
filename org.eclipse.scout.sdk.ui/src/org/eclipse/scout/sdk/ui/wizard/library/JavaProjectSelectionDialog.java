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
package org.eclipse.scout.sdk.ui.wizard.library;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

/**
 * <h3>{@link JavaProjectSelectionDialog}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 09.03.2012
 */
@SuppressWarnings("restriction")
public class JavaProjectSelectionDialog extends FilteredItemsSelectionDialog {
  private static final String DIALOG_SETTINGS = JavaProjectSelectionDialog.class.getName();
  private IJavaProject[] m_javaProjects;

  /**
   * @param shell
   */
  public JavaProjectSelectionDialog(Shell shell, boolean includeFragments, boolean multiSelect) {
    this(shell, getAllWorkspaceScoutProjects(includeFragments), multiSelect);
  }

  public JavaProjectSelectionDialog(Shell shell, IJavaProject[] projects, boolean multiSelect) {
    super(shell, multiSelect);
    setListLabelProvider(new JavaElementLabelProvider());
    setDetailsLabelProvider(new JavaElementLabelProvider());
    setInitialPattern("**");
    setHelpAvailable(false);
    m_javaProjects = projects;
  }

  public static IJavaProject[] getAllWorkspaceScoutProjects(boolean includeFragments) {
    IScoutBundle[] bundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().
        getBundles(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleComparators.getSymbolicNameAscComparator());
    List<IJavaProject> plugins = new ArrayList<IJavaProject>(bundles.length);
    for (IScoutBundle project : bundles) {
      try {
        if (!project.getProject().hasNature(ScoutSdk.LIBRARY_NATURE_ID)) {
          plugins.add(project.getJavaProject());
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logWarning("could not validate plugin '" + project.getSymbolicName() + "'.", e);
      }
    }
    return plugins.toArray(new IJavaProject[plugins.size()]);
  }

  @Override
  protected Control createExtendedContentArea(Composite parent) {
    return null;
  }

  @Override
  public int open() {
    return super.open();
  }

  @Override
  protected ItemsFilter createFilter() {
    return new P_JavaProjectSearchItemsFilter();
  }

  @Override
  protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
    for (IJavaProject project : m_javaProjects) {
      contentProvider.add(project, itemsFilter);
      progressMonitor.worked(1);
    }
    progressMonitor.done();
  }

  @Override
  protected IDialogSettings getDialogSettings() {
    IDialogSettings settings = ScoutSdkUi.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

    if (settings == null) {
      settings = ScoutSdkUi.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
    }

    return settings;
  }

  @Override
  public String getElementName(Object item) {
    if (item instanceof IJavaProject) {
      return ((IJavaProject) item).getElementName();
    }
    return null;
  }

  @Override
  protected Comparator getItemsComparator() {
    return new P_JavaProjectSearchComparator();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
   */
  @Override
  protected IStatus validateItem(Object item) {
    return new Status(IStatus.OK, ScoutSdkUi.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
  }

  private class P_JavaProjectSearchItemsFilter extends ItemsFilter {

    @Override
    public boolean isConsistentItem(Object item) {
      return true;
    }

    @Override
    public boolean matchItem(Object item) {
      String id = null;
      if (item instanceof IJavaProject) {
        IJavaProject project = (IJavaProject) item;
        id = project.getElementName();
      }

      return (matches(id));
    }

    @Override
    protected boolean matches(String text) {
      String pattern = patternMatcher.getPattern();
      if (pattern.indexOf("*") != 0 & pattern.indexOf("?") != 0 & pattern.indexOf(".") != 0) {//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pattern = "*" + pattern; //$NON-NLS-1$
        patternMatcher.setPattern(pattern);
      }
      return patternMatcher.matches(text);
    }
  } // end class P_JavaProjectSearchItemsFilter

  private class P_JavaProjectSearchComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
      int id1 = getId(o1);
      int id2 = getId(o2);

      if (id1 != id2) return id1 - id2;
      return compareSimilarObjects(o1, o2);
    }

    private int getId(Object element) {
      if (element instanceof IJavaProject) {
        return 100;
      }
      return 0;
    }

    private int compareSimilarObjects(Object o1, Object o2) {
      if (o1 instanceof IJavaProject && o2 instanceof IJavaProject) {
        IJavaProject ipmb1 = (IJavaProject) o1;
        IJavaProject ipmb2 = (IJavaProject) o2;
        return ipmb1.getElementName().compareTo(ipmb2.getElementName());
      }
      return 0;
    }

  } // end class P_JavaProjectSearchComparator
}
