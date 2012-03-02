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
package org.eclipse.scout.sdk.ui.internal.wizard.newbundle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtension;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link ScoutBundleNewWizardPage}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 02.03.2012
 */
public class ScoutBundleNewWizardPage extends ScoutProjectNewWizardPage {

  private static final Pattern PROJECT_NAME_REGEX = Pattern.compile("(.*)\\s*\\((.*)\\).*");

  private final IScoutProject m_project;
  private final String m_name;
  private final String m_postfix;

  public ScoutBundleNewWizardPage(IScoutProject project) {
    super();
    m_project = project;
    setTitle(Texts.get("CreateNewScoutBundles"));
    setDescription(Texts.get("NewScoutBundlesDesc"));
    String[] parts = getProjectNameParts(project.getProjectName());
    m_name = parts[0];
    m_postfix = parts[1];
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    m_projectNameField.setText(m_name);
    m_postFixField.setText(m_postfix);

    for (ScoutBundleExtension e : ScoutBundleExtensionPoint.getExtensions()) {
      e.getBundleExtention().init(getWizard(), getProject());
    }

    // force re-validate and dynamic page add/remove
    ITreeNode[] backup = m_bundleTree.getCheckedNodes();
    m_bundleTree.setChecked(new ITreeNode[]{});
    m_bundleTree.setChecked(backup);
  }

  public IScoutProject getProject() {
    return m_project;
  }

  private static String[] getProjectNameParts(String name) {
    if (name.contains("(")) {
      Matcher m = PROJECT_NAME_REGEX.matcher(name);
      if (m.find()) {
        return new String[]{m.group(1).trim(), m.group(2).trim()};
      }
      else {
        return new String[]{"", ""};
      }
    }
    else {
      return new String[]{name, ""};
    }
  }
}
