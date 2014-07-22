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
package org.eclipse.scout.sdk.ui.extensions.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.preferences.IScoutProjectScrolledContent.IModelLoadProgressObserver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * <h3>{@link AbstractScoutProjectPreferencePage}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 17.12.2012
 */
public abstract class AbstractScoutProjectPreferencePage<T extends IScoutProjectScrolledContent<U>, U> extends PreferencePage implements IWorkbenchPreferencePage {

  private Combo m_projectCombo;
  private Text m_searchFilter;
  private T m_currentProjectSetting;
  private Composite m_container;
  private Job m_curJob;

  private final Map<IScoutBundle, T> m_projectSettings;

  public AbstractScoutProjectPreferencePage(String desc, Class<T> contentClass, String... scoutBundleTypes) {
    setDescription(desc);

    IScoutBundleFilter projectFilter = ScoutBundleFilters.getMultiFilterAnd(
        ScoutBundleFilters.getWorkspaceBundlesFilter(),
        ScoutBundleFilters.getBundlesOfTypeFilter(scoutBundleTypes));
    Set<IScoutBundle> rootProjects = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(projectFilter);

    m_projectSettings = new HashMap<IScoutBundle, T>(rootProjects.size());
    for (IScoutBundle p : rootProjects) {
      try {
        m_projectSettings.put(p, contentClass.newInstance());
      }
      catch (InstantiationException e) {
        ScoutSdkUi.logError(e);
      }
      catch (IllegalAccessException e) {
        ScoutSdkUi.logError(e);
      }
    }
  }

  @Override
  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    cancelLoad();
    save();
    return super.performOk();
  }

  @Override
  protected void performApply() {
    save();
    super.performApply();
  }

  @Override
  public boolean performCancel() {
    cancelLoad();
    return super.performCancel();
  }

  private void cancelLoad() {
    Job j = m_curJob;
    if (j != null) {
      j.cancel();
    }
  }

  @Override
  protected void performDefaults() {
    reset();
    super.performDefaults();
  }

  private void save() {
    for (T c : m_projectSettings.values()) {
      c.save();
    }
  }

  private void reset() {
    for (T c : m_projectSettings.values()) {
      c.reset();
    }
  }

  @Override
  protected Control createContents(Composite p) {
    m_container = new Composite(p, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    m_container.setLayout(layout);
    if (m_projectSettings.size() < 1) {
      Label noProjectLabel = new Label(m_container, SWT.NONE);
      noProjectLabel.setText(Texts.get("NoScoutProjectForDefaultSuperClassMsg"));
      return m_container;
    }

    final Composite parent = m_container;
    final ProgressIndicator indicator = new ProgressIndicator(parent, SWT.SMOOTH);
    indicator.beginTask(getTotalWork());
    GridData indicatorData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    indicatorData.horizontalSpan = 2;
    indicatorData.heightHint = 15;
    indicator.setLayoutData(indicatorData);

    Job j = new Job("load scout sdk settings...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        if (!parent.isDisposed()) {
          try {
            loadAllModels(new IModelLoadProgressObserver<U>() {
              @Override
              public void loaded(U justLoadedModel) {
                if (parent != null && !parent.isDisposed()) {
                  parent.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                      if (!indicator.isDisposed()) {
                        indicator.worked(1);
                      }
                    }
                  });
                }
              }
            }, monitor);
          }
          finally {
            m_curJob = null;
            if (!parent.isDisposed()) {
              parent.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  if (parent != null && !parent.isDisposed()) {
                    try {
                      parent.getShell().setRedraw(false);
                      indicator.dispose();

                      createHeader(m_container);
                      createSettingsLists(m_container);
                      initializeValues();
                    }
                    finally {
                      parent.getShell().setRedraw(true);
                      parent.getShell().layout(true);
                      parent.getShell().redraw();
                    }
                  }
                }
              });
            }
          }
        }
        return Status.OK_STATUS;
      }
    };
    j.setSystem(true);
    m_curJob = j;
    j.schedule();

    return m_container;
  }

  private void createHeader(Composite parent) {
    Composite headerComposite = new Composite(parent, SWT.NONE);
    headerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayout gl = new GridLayout(2, false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.marginBottom = 8;
    headerComposite.setLayout(gl);

    Label projectLabel = new Label(headerComposite, SWT.NONE);
    projectLabel.setText(Texts.get("ScoutProjectLabel"));

    GridData labelGridData = new GridData();
    labelGridData.verticalIndent = 10;
    projectLabel.setLayoutData(labelGridData);

    m_projectCombo = new Combo(headerComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
    m_projectCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String selectedName = m_projectCombo.getItem(m_projectCombo.getSelectionIndex());
        projectChanged(selectedName);
      }
    });

    GridData comboGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    comboGridData.verticalIndent = 10;
    m_projectCombo.setLayoutData(comboGridData);

    m_searchFilter = new Text(headerComposite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
    m_searchFilter.setMessage(Texts.get("TypeFilterText"));
    m_searchFilter.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        applySearchPattern(m_searchFilter.getText());
      }
    });
    GridData filterGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    filterGridData.horizontalSpan = 2;
    filterGridData.verticalIndent = 10;
    m_searchFilter.setLayoutData(filterGridData);
  }

  private void createSettingsLists(final Composite parent) {
    for (Entry<IScoutBundle, T> e : m_projectSettings.entrySet()) {
      e.getValue().createContent(parent);
    }
  }

  private void applySearchPattern(String pattern) {
    try {
      m_container.setRedraw(false);
      pattern = "*" + pattern.trim() + "*";
      for (T scrolledArea : m_projectSettings.values()) {
        scrolledArea.setSearchPattern(pattern);
        scrolledArea.reflow();
      }
    }
    finally {
      m_container.setRedraw(true);
      m_container.layout(true, true);
      m_container.redraw();
    }
  }

  protected abstract void loadAllModels(IModelLoadProgressObserver<U> observer, IProgressMonitor monitor);

  protected abstract int getTotalWork();

  protected Map<IScoutBundle, T> getProjectModelMap() {
    return m_projectSettings;
  }

  private void initializeValues() {
    String[] projectNames = new String[m_projectSettings.size()];
    int i = 0;
    for (IScoutBundle p : m_projectSettings.keySet()) {
      projectNames[i++] = p.getSymbolicName();
    }
    Arrays.sort(projectNames);
    m_projectCombo.setItems(projectNames);
    m_projectCombo.select(0);

    projectChanged(projectNames[0]);
  }

  private void projectChanged(String projectName) {
    if (m_currentProjectSetting != null) {
      m_currentProjectSetting.setVisible(false);
    }

    for (IScoutBundle p : m_projectSettings.keySet()) {
      if (p.getSymbolicName().equals(projectName)) {
        m_currentProjectSetting = m_projectSettings.get(p);
        m_currentProjectSetting.setVisible(true);
        break;
      }
    }

    m_container.layout(true);
    m_container.redraw();
  }
}
