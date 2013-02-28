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
package org.eclipse.scout.sdk.rap.ui.internal.extensions.bundle;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.project.RapTargetPlatformWizardPage;
import org.eclipse.scout.sdk.ui.extensions.bundle.INewScoutBundleHandler;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.osgi.framework.Version;

public class RapScoutBundleExtension implements INewScoutBundleHandler {
  private boolean m_rapUiSelected;

  public final static String ID = "org.eclipse.scout.sdk.ui.UiRapBundle";

  public RapScoutBundleExtension() {
    m_rapUiSelected = true;/* by default all nodes are checked */
  }

  @Override
  public void init(IScoutProjectWizard wizard, ScoutBundleUiExtension extension) {
    IScoutBundle selected = wizard.getScoutProject();
    boolean available = selected == null || selected.getType().equals(IScoutSdkRapConstants.TYPE_UI_RAP) || selected.getType().equals(IScoutBundle.TYPE_CLIENT);
    wizard.getProjectWizardPage().setBundleNodeAvailable(available, available, ID);
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    IScoutBundle selected = wizard.getScoutProject();
    if (selected == null) {
      if (!wizard.getProjectWizardPage().hasSelectedBundle(IScoutBundle.TYPE_CLIENT)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoRapWithoutClient"));
      }
    }
    if (m_rapUiSelected && PlatformVersionUtility.getPlatformVersion().compareTo(new Version(3, 6, 0)) < 0) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoRapBeforeHelios"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected) {
    m_rapUiSelected = selected;
    AbstractScoutWizardPage page = wizard.getPage(RapTargetPlatformWizardPage.class.getName());
    if (page == null) {
      page = new RapTargetPlatformWizardPage();
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void putProperties(IScoutProjectWizard wizard, PropertyMap properties) {
    IScoutBundle selected = wizard.getScoutProject();
    if (selected != null) {
      IScoutBundle uiRap = selected.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), true);
      if (uiRap != null && uiRap.getJavaProject() != null) {
        properties.setProperty(CreateUiRapPluginOperation.PROP_BUNDLE_RAP_NAME, uiRap.getProject().getName());
        properties.getProperty(IScoutProjectNewOperation.PROP_CREATED_BUNDLES, List.class).add(uiRap.getJavaProject());
      }
    }
  }
}
