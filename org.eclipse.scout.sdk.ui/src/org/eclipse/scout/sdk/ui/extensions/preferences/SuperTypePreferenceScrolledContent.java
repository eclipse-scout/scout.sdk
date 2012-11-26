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

import org.eclipse.scout.sdk.RuntimeClasses;
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
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <h3>{@link SuperTypePreferenceScrolledContent}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 24.11.2012
 */
public class SuperTypePreferenceScrolledContent {

  private final List<Combo> m_allSuperTypeCombos;

  private List<DefaultSuperClassModel> m_entries;
  private SimpleScrolledComposite m_scrollArea;

  public SuperTypePreferenceScrolledContent() {
    m_allSuperTypeCombos = new ArrayList<Combo>();
  }

  public void loadModel(List<DefaultSuperClassModel> entries, IModelLoadProgressObserver observer) {
    m_entries = new ArrayList<DefaultSuperClassModel>(entries);
    for (DefaultSuperClassModel model : m_entries) {
      model.load();
      if (observer != null) {
        observer.loaded(model);
      }
    }
  }

  public void createContent(Composite parent) {

    m_scrollArea = new SimpleScrolledComposite(parent);
    GridData dd = new GridData();
    dd.exclude = true;
    dd.heightHint = getNextParentSize(parent);
    m_scrollArea.setLayoutData(dd);

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

  public void setVisible(boolean visible) {
    ((GridData) m_scrollArea.getLayoutData()).exclude = !visible;
    m_scrollArea.setVisible(visible);
    m_scrollArea.reflow(true);
  }

  private static int getNextParentSize(Composite container) {
    Composite parent = container;
    while ((parent = parent.getParent()) != null) {
      if (parent.getSize().y > 0) {
        return parent.getSize().y - 100;
      }
    }
    return 520;
  }

  public interface IModelLoadProgressObserver {
    void loaded(DefaultSuperClassModel justLoadedModel);
  }

  private interface ISuperTypeComboVisitor {
    void visit(Combo combo, String selectedValue, DefaultSuperClassModel model);
  }

  private static class SimpleScrolledComposite extends SharedScrolledComposite {
    private SimpleScrolledComposite(Composite parent) {
      super(parent, SWT.V_SCROLL | SWT.NONE);
      setFont(parent.getFont());
      setExpandHorizontal(true);
      setExpandVertical(true);

      Composite body = new Composite(this, SWT.NONE);
      body.setFont(parent.getFont());
      setContent(body);
    }

    private Composite getBody() {
      return (Composite) getContent();
    }
  }
}
