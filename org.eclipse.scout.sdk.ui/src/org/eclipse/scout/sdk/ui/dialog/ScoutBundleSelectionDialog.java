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
package org.eclipse.scout.sdk.ui.dialog;

import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.viewer.ScoutBundleLableProvider;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

/**
 * <h3>{@link ScoutBundleSelectionDialog}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 09.03.2012
 */
public class ScoutBundleSelectionDialog extends FilteredItemsSelectionDialog {
  private static final String DIALOG_SETTINGS = ScoutBundleSelectionDialog.class.getName();
  private IScoutBundle[] m_bundles;

  /**
   * @param shell
   */
  public ScoutBundleSelectionDialog(Shell shell, boolean multiSelect) {
    this(shell, ScoutSdkCore.getScoutWorkspace().getAllBundles(), multiSelect);
  }

  public ScoutBundleSelectionDialog(Shell shell, IScoutBundle[] bundles, boolean multiSelect) {
    super(shell, multiSelect);
    // XXX AHO
    setListLabelProvider(new ScoutBundleLableProvider());
    setDetailsLabelProvider(new ScoutBundleLableProvider());
    setInitialPattern("**");
    m_bundles = bundles;
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
    return new P_BundleSearchItemsFilter();
  }

  @Override
  protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
    for (IScoutBundle project : m_bundles) {
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
    if (item instanceof IScoutBundle) {
      return ((IScoutBundle) item).getBundleName();
    }
    return null;
  }

  @Override
  protected Comparator getItemsComparator() {
    return new P_BundleSearchComparator();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
   */
  @Override
  protected IStatus validateItem(Object item) {
    return new Status(IStatus.OK, ScoutSdkUi.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
  }

  private class P_BundleSearchItemsFilter extends ItemsFilter {

    @Override
    public boolean isConsistentItem(Object item) {
      return true;
    }

    @Override
    public boolean matchItem(Object item) {
      String id = null;
      if (item instanceof IScoutBundle) {
        IScoutBundle project = (IScoutBundle) item;
        id = project.getBundleName();
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
  } // end class P_BundleSearchItemsFilter

  private class P_BundleSearchComparator implements Comparator {

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
      if (o1 instanceof IScoutBundle && o2 instanceof IScoutBundle) {
        IScoutBundle ipmb1 = (IScoutBundle) o1;
        IScoutBundle ipmb2 = (IScoutBundle) o2;
        return ipmb1.getBundleName().compareTo(ipmb2.getBundleName());
      }
      return 0;
    }

  } // end class P_BundleSearchComparator
}
