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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.ui.extensions.preferences.SuperTypePreferenceScrolledContent.IModelLoadProgressObserver;
import org.eclipse.scout.sdk.workspace.IScoutProject;
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
 * <h3>{@link SuperTypesPreferencePage}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 22.11.2012
 */
public class SuperTypesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  private Combo m_projectCombo;
  private Text m_searchFilter;
  private SuperTypePreferenceScrolledContent m_currentProjectSetting;
  private Composite m_container;

  private final Map<IScoutProject, SuperTypePreferenceScrolledContent> m_projectSettings;

  public SuperTypesPreferencePage() {
    setDescription(Texts.get("ScoutSDKSuperTypePreferences"));

    IScoutProject[] rootProjects = ScoutWorkspace.getInstance().getRootProjects();
    m_projectSettings = new HashMap<IScoutProject, SuperTypePreferenceScrolledContent>(rootProjects.length);
    for (IScoutProject p : rootProjects) {
      if (p.getSharedBundle() != null && p.getSharedBundle().getProject() != null && p.getSharedBundle().getProject().exists()) {
        m_projectSettings.put(p, new SuperTypePreferenceScrolledContent());
      }
    }
  }

  @Override
  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    save();
    return super.performOk();
  }

  @Override
  protected void performApply() {
    save();
    super.performApply();
  }

  @Override
  protected void performDefaults() {
    reset();
    super.performDefaults();
  }

  private void save() {
    for (SuperTypePreferenceScrolledContent c : m_projectSettings.values()) {
      c.save();
    }
  }

  private void reset() {
    for (SuperTypePreferenceScrolledContent c : m_projectSettings.values()) {
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
    final Set<Entry<String, String>> configuredClasses = RuntimeClasses.getAllDefaults().entrySet();
    indicator.beginTask(configuredClasses.size()/* * m_projectSettings.size()*/);
    GridData indicatorData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    indicatorData.horizontalSpan = 2;
    indicatorData.heightHint = 15;
    indicator.setLayoutData(indicatorData);

    Job j = new Job("load scout sdk default super type settings...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        if (parent != null && !parent.isDisposed()) {
          try {
            loadAllModels(configuredClasses, new IModelLoadProgressObserver() {
              @Override
              public void loaded(DefaultSuperClassModel justLoadedModel) {
                parent.getDisplay().asyncExec(new Runnable() {
                  @Override
                  public void run() {
                    if (!indicator.isDisposed()) {
                      indicator.worked(1);
                    }
                  }
                });
              }
            });
          }
          finally {
            if (parent != null && !parent.isDisposed()) {
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
    m_searchFilter.setMessage("type filter text");//TODO nls
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
    for (Entry<IScoutProject, SuperTypePreferenceScrolledContent> e : m_projectSettings.entrySet()) {
      e.getValue().createContent(parent);
    }
  }

  private void applySearchPattern(String pattern) {
    try {
      m_container.setRedraw(false);
      pattern = "*" + pattern.trim() + "*";
      for (SuperTypePreferenceScrolledContent scrolledArea : m_projectSettings.values()) {
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

  private void loadAllModels(Set<Entry<String, String>> configuredClasses, IModelLoadProgressObserver observer) {
    for (Entry<IScoutProject, SuperTypePreferenceScrolledContent> e : m_projectSettings.entrySet()) {
      List<DefaultSuperClassModel> list = new ArrayList<DefaultSuperClassModel>();
      for (Entry<String, String> entry : configuredClasses) {
        list.add(new DefaultSuperClassModel(entry.getKey(), entry.getValue(), e.getKey()));
      }
      Collections.sort(list);

      e.getValue().loadModel(list, observer);
    }
  }

  private void initializeValues() {
    String[] projectNames = new String[m_projectSettings.size()];
    int i = 0;
    for (IScoutProject p : m_projectSettings.keySet()) {
      projectNames[i++] = p.getProjectName();
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

    for (IScoutProject p : m_projectSettings.keySet()) {
      if (p.getProjectName().equals(projectName)) {
        m_currentProjectSetting = m_projectSettings.get(p);
        m_currentProjectSetting.setVisible(true);
        break;
      }
    }

    m_container.layout(true);
    m_container.redraw();
  }
}
