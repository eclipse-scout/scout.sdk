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
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link ScoutBundleAddWizardPage}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 02.03.2012
 */
public class ScoutBundleAddWizardPage extends ScoutProjectNewWizardPage {

  private static final Pattern PROJECT_NAME_REGEX = Pattern.compile("(.*)\\s*\\((.*)\\).*");

  private final IScoutBundle m_project;
  private final String m_name;
  private final String m_postfix;

  public ScoutBundleAddWizardPage(IScoutBundle project) {
    super();
    m_project = project;
    setTitle(Texts.get("CreateNewScoutBundles"));
    setDescription(Texts.get("NewScoutBundlesDesc"));
    String[] parts = getProjectNameParts(project.getSymbolicName());
    m_name = parts[0];
    m_postfix = parts[1];
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    m_projectNameField.setText(m_name);
    m_postFixField.setText(m_postfix);

    // force re-validate and dynamic page add/remove
    ITreeNode[] backup = m_bundleTree.getCheckedNodes();
    m_bundleTree.setChecked(new ITreeNode[]{});
    m_bundleTree.setChecked(backup);
  }

  public IScoutBundle getProject() {
    return m_project;
  }

  private static String[] getProjectNameParts(String name) {
    String newName = name;
    String postfix = "";
    if (name.contains("(")) {
      Matcher m = PROJECT_NAME_REGEX.matcher(name);
      if (m.find()) {
        newName = m.group(1).trim();
        postfix = m.group(2).trim();
      }
      else {
        newName = "";
      }
    }

    int p = newName.lastIndexOf('.');
    if (p > 0) {
      newName = newName.substring(0, p);
    }
    return new String[]{newName, postfix};
  }
}
