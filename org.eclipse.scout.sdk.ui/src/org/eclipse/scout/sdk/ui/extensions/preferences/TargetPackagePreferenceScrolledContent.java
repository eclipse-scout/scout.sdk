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
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.ui.fields.SimpleScrolledComposite;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <h3>{@link TargetPackagePreferenceScrolledContent}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 24.11.2012
 */
public class TargetPackagePreferenceScrolledContent implements IScoutProjectScrolledContent<TargetPackageModel> {

  private final List<EntityTextField> m_allEntityTextFields;
  private List<TargetPackageModel> m_entries;
  private SimpleScrolledComposite m_scrollArea;

  public TargetPackagePreferenceScrolledContent() {
    m_allEntityTextFields = new ArrayList<EntityTextField>();
  }

  @Override
  public void loadModel(List<TargetPackageModel> entries, IModelLoadProgressObserver<TargetPackageModel> observer) {
    m_entries = new ArrayList<TargetPackageModel>(entries);
    for (TargetPackageModel model : m_entries) {
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
    GridLayout gl = new GridLayout(1, false);
    gl.horizontalSpacing = 2;
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    c.setLayout(gl);

    for (TargetPackageModel entry : m_entries) {
      EntityTextField txt = new EntityTextField(c, entry.m_label, 40, entry.m_context);
      txt.setText(entry.m_curVal);
      txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      m_allEntityTextFields.add(txt);
    }
  }

  @Override
  public void save() {
    final HashSet<IScoutBundle> modifiedProjects = new HashSet<IScoutBundle>(1);
    visitTextfields(new IPackageTextFieldVisitor() {
      @Override
      public void visit(EntityTextField txt, TargetPackageModel model) {
        String prefKey = DefaultTargetPackage.getPreferenceKey(model.m_id);
        modifiedProjects.add(model.m_context);
        if (model.m_defaultVal.equals(txt.getText())) {
          model.m_context.getPreferences().remove(prefKey);
        }
        else {
          model.m_context.getPreferences().put(prefKey, txt.getText());
        }
      }
    });

    for (IScoutBundle p : modifiedProjects) {
      try {
        p.getPreferences().flush();
      }
      catch (BackingStoreException ex) {
        ScoutSdkUi.logError("Unable to save new default package configuration for project '" + p.getSymbolicName() + "'.", ex);
      }
    }
  }

  @Override
  public void reset() {
    visitTextfields(new IPackageTextFieldVisitor() {
      @Override
      public void visit(EntityTextField txt, TargetPackageModel model) {
        txt.setText(model.m_defaultVal);
      }
    });
  }

  private void visitTextfields(IPackageTextFieldVisitor visitor) {
    for (int i = 0; i < m_entries.size(); i++) {
      EntityTextField txt = m_allEntityTextFields.get(i);
      TargetPackageModel model = m_entries.get(i);
      visitor.visit(txt, model);
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
    for (int i = 0; i < m_allEntityTextFields.size(); i++) {
      EntityTextField txt = m_allEntityTextFields.get(i);
      String curText = txt.getText();
      if (curText == null) {
        curText = "";
      }
      boolean visible = CharOperation.match(searchPatternArray, curText.toCharArray(), false) ||
          CharOperation.match(searchPatternArray, txt.getLabelText().toCharArray(), false);

      txt.setVisible(visible);
      ((GridData) txt.getLayoutData()).exclude = !visible;

      Composite section = txt.getParent().getParent();
      if (section instanceof ExpandableComposite) {
        ((ExpandableComposite) section).setExpanded(true);
      }
    }
  }

  private interface IPackageTextFieldVisitor {
    void visit(EntityTextField txt, TargetPackageModel model);
  }
}
