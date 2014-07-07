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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport.BundlePresentation;

/**
 * <h3>{@link ScoutBundlePresentationActionGroup}</h3> ...
 *
 * @author Matthias Villiger
 * @since 3.9.0 20.03.2013
 */
public class ScoutBundlePresentationActionGroup extends MenuManager {

  private final P_GroupedPresentationAction m_grouped;
  private final P_HierarchicalPresentationAction m_hierarchical;
  private final P_FlatPresentationAction m_flat;
  private final P_WorkingSetPresentationAction m_workingSet;
  private final P_FlatGroupePresentationAction m_flatGroups;

  public ScoutBundlePresentationActionGroup() {
    super(Texts.get("ScoutBundlePresentation"));

    m_grouped = new P_GroupedPresentationAction();
    m_hierarchical = new P_HierarchicalPresentationAction();
    m_flat = new P_FlatPresentationAction();
    m_workingSet = new P_WorkingSetPresentationAction();
    m_flatGroups = new P_FlatGroupePresentationAction();

    BundlePresentation initial = ScoutExplorerSettingsSupport.get().getBundlePresentation();
    m_grouped.setChecked(BundlePresentation.Grouped.equals(initial));
    m_hierarchical.setChecked(BundlePresentation.Hierarchical.equals(initial));
    m_flat.setChecked(BundlePresentation.Flat.equals(initial));
    m_workingSet.setChecked(BundlePresentation.WorkingSet.equals(initial));
    m_flatGroups.setChecked(BundlePresentation.FlatGroups.equals(initial));

    add(m_grouped);
    add(m_flatGroups);
    add(m_hierarchical);
    add(m_flat);
    add(m_workingSet);
  }

  private final class P_GroupedPresentationAction extends Action {
    private P_GroupedPresentationAction() {
      super(Texts.get("Grouped"));
      setImageDescriptor(ScoutSdkUi.getImageDescriptor(SdkIcons.BundlePresentationGrouped));
    }

    @Override
    public void run() {
      P_GroupedPresentationAction.this.setChecked(true);
      m_hierarchical.setChecked(false);
      m_flat.setChecked(false);
      m_workingSet.setChecked(false);
      m_flatGroups.setChecked(false);
      ScoutExplorerSettingsSupport.get().setBundlePresentation(BundlePresentation.Grouped);
    }
  }

  private final class P_FlatGroupePresentationAction extends Action {
    private P_FlatGroupePresentationAction() {
      super(Texts.get("FlatGroups"));
      setImageDescriptor(ScoutSdkUi.getImageDescriptor(SdkIcons.BundlePresentationFlatGrouped));
    }

    @Override
    public void run() {
      P_FlatGroupePresentationAction.this.setChecked(true);
      m_grouped.setChecked(false);
      m_hierarchical.setChecked(false);
      m_flat.setChecked(false);
      m_workingSet.setChecked(false);
      ScoutExplorerSettingsSupport.get().setBundlePresentation(BundlePresentation.FlatGroups);
    }
  }

  private final class P_HierarchicalPresentationAction extends Action {
    private P_HierarchicalPresentationAction() {
      super(Texts.get("Hierarchical"));
      setImageDescriptor(ScoutSdkUi.getImageDescriptor(SdkIcons.BundlePresentationHierarchical));
    }

    @Override
    public void run() {
      P_HierarchicalPresentationAction.this.setChecked(true);
      m_grouped.setChecked(false);
      m_flat.setChecked(false);
      m_workingSet.setChecked(false);
      m_flatGroups.setChecked(false);
      ScoutExplorerSettingsSupport.get().setBundlePresentation(BundlePresentation.Hierarchical);
    }
  }

  private final class P_FlatPresentationAction extends Action {
    private P_FlatPresentationAction() {
      super(Texts.get("Flat"));
      setImageDescriptor(ScoutSdkUi.getImageDescriptor(SdkIcons.BundlePresentationFlat));
    }

    @Override
    public void run() {
      P_FlatPresentationAction.this.setChecked(true);
      m_grouped.setChecked(false);
      m_hierarchical.setChecked(false);
      m_workingSet.setChecked(false);
      m_flatGroups.setChecked(false);
      ScoutExplorerSettingsSupport.get().setBundlePresentation(BundlePresentation.Flat);
    }
  }

  private final class P_WorkingSetPresentationAction extends Action {
    private P_WorkingSetPresentationAction() {
      super(Texts.get("WorkingSets"));
      setImageDescriptor(ScoutSdkUi.getImageDescriptor(SdkIcons.ScoutWorkingSet));
    }

    @Override
    public void run() {
      P_WorkingSetPresentationAction.this.setChecked(true);
      m_grouped.setChecked(false);
      m_hierarchical.setChecked(false);
      m_flat.setChecked(false);
      m_flatGroups.setChecked(false);
      ScoutExplorerSettingsSupport.get().setBundlePresentation(BundlePresentation.WorkingSet);
    }
  }
}
