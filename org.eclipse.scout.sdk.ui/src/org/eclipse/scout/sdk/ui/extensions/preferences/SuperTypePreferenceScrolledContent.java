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
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.SimpleScrolledComposite;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <h3>{@link SuperTypePreferenceScrolledContent}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 24.11.2012
 */
public class SuperTypePreferenceScrolledContent implements IScoutProjectScrolledContent<DefaultSuperClassModel> {

  private final List<Combo> m_allSuperTypeCombos;
  private final List<Label> m_allLabels;

  private List<DefaultSuperClassModel> m_entries;
  private SimpleScrolledComposite m_scrollArea;

  public SuperTypePreferenceScrolledContent() {
    m_allSuperTypeCombos = new ArrayList<Combo>();
    m_allLabels = new ArrayList<Label>();
  }

  @Override
  public void loadModel(List<DefaultSuperClassModel> entries, IModelLoadProgressObserver<DefaultSuperClassModel> observer) {
    m_entries = new ArrayList<DefaultSuperClassModel>(entries);
    for (DefaultSuperClassModel model : m_entries) {
      model.load();
      if (observer != null) {
        observer.loaded(model);
      }
    }
  }

  @Override
  public void createContent(Composite parent) {
    m_scrollArea = new SimpleScrolledComposite(parent);

    Composite c = m_scrollArea.getBody();
    GridLayout gl = new GridLayout(2, false);
    gl.horizontalSpacing = 2;
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    c.setLayout(gl);

    for (DefaultSuperClassModel entry : m_entries) {
      String[] proposals = entry.getProposals();
      String selectedValue = entry.getProposals()[entry.getInitialSelectetdIndex()];
      Label l = new Label(c, SWT.NONE);
      l.setToolTipText(entry.interfaceFqn);
      l.setText(entry.label + ": ");
      l.setLayoutData(new GridData());
      m_allLabels.add(l);

      final DefaultSuperClassModel model = entry;
      final Combo co = new Combo(c, SWT.READ_ONLY | SWT.DROP_DOWN);
      co.setToolTipText(selectedValue);
      co.setItems(entry.getProposalDisplayTexts());
      co.setEnabled(proposals.length > 1);
      co.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          String selectedName = model.getProposals()[co.getSelectionIndex()];
          co.setToolTipText(selectedName);
        }
      });
      co.select(entry.getInitialSelectetdIndex());
      co.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      m_allSuperTypeCombos.add(co);
    }
  }

  @Override
  public void save() {
    final HashSet<IScoutProject> modifiedProjects = new HashSet<IScoutProject>(1);
    visitCombos(new ISuperTypeComboVisitor() {
      @Override
      public void visit(Combo combo, String selectedValue, DefaultSuperClassModel model) {
        String prefKey = RuntimeClasses.getPreferenceKey(model.interfaceFqn);
        modifiedProjects.add(model.scoutProject);
        if (model.defaultVal.equals(selectedValue)) {
          model.scoutProject.getPreferences().remove(prefKey);
        }
        else {
          model.scoutProject.getPreferences().put(prefKey, selectedValue);
        }
      }
    });

    for (IScoutProject p : modifiedProjects) {
      try {
        p.getPreferences().flush();
      }
      catch (BackingStoreException ex) {
        ScoutSdkUi.logError("Unable to save new super type configuration for project '" + p.getProjectName() + "'.", ex);
      }
    }
  }

  @Override
  public void reset() {
    visitCombos(new ISuperTypeComboVisitor() {
      @Override
      public void visit(Combo combo, String selectedValue, DefaultSuperClassModel model) {
        combo.select(model.getDefaultIndex());
      }
    });
  }

  private void visitCombos(ISuperTypeComboVisitor visitor) {
    for (int i = 0; i < m_entries.size(); i++) {
      Combo combo = m_allSuperTypeCombos.get(i);
      DefaultSuperClassModel model = m_entries.get(i);
      String curVal = model.getProposals()[combo.getSelectionIndex()];
      visitor.visit(combo, curVal, model);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    ((GridData) m_scrollArea.getLayoutData()).exclude = !visible;
    m_scrollArea.setVisible(visible);
    m_scrollArea.reflow(true);
  }

  @Override
  public void reflow() {
    m_scrollArea.reflow(true);
  }

  @Override
  public void setSearchPattern(String pattern) {
    char[] searchPatternArray = pattern.toCharArray();
    for (int i = 0; i < m_allSuperTypeCombos.size(); i++) {
      Combo cbo = m_allSuperTypeCombos.get(i);
      Label lbl = m_allLabels.get(i);
      DefaultSuperClassModel model = m_entries.get(i);

      boolean visible = CharOperation.match(searchPatternArray, model.interfaceFqn.toCharArray(), false) ||
          CharOperation.match(searchPatternArray, model.label.toCharArray(), false);
      if (!visible) {
        for (String s : model.getProposals()) {
          boolean matches = CharOperation.match(searchPatternArray, s.toCharArray(), false);
          if (matches) {
            visible = matches;
            break;
          }
        }
      }

      cbo.setVisible(visible);
      ((GridData) cbo.getLayoutData()).exclude = !visible;
      lbl.setVisible(visible);
      ((GridData) lbl.getLayoutData()).exclude = !visible;
    }
  }

  private interface ISuperTypeComboVisitor {
    void visit(Combo combo, String selectedValue, DefaultSuperClassModel model);
  }
}
