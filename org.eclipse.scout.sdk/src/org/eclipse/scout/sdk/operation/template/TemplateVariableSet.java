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
package org.eclipse.scout.sdk.operation.template;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class TemplateVariableSet implements ITemplateVariableSet {

  private Map<String, String> m_variables;

  private TemplateVariableSet(ITemplateVariableSet template) {
    m_variables = new HashMap<String, String>();
    for (Entry<String, String> e : template.entrySet()) {
      m_variables.put(e.getKey(), e.getValue());
    }
  }

  private TemplateVariableSet() {
    m_variables = new HashMap<String, String>();
  }

  public static TemplateVariableSet createNew() {
    TemplateVariableSet templateVariableSet = new TemplateVariableSet();
    String os = ScoutSdk.getDefault().getBundle().getBundleContext().getProperty("osgi.os");
    if (!StringUtility.isNullOrEmpty(os)) {
      templateVariableSet.setVariable(VAR_OS, os);
    }
    String ws = ScoutSdk.getDefault().getBundle().getBundleContext().getProperty("osgi.ws");
    if (!StringUtility.isNullOrEmpty(ws)) {
      templateVariableSet.setVariable(VAR_WS, ws);
    }
    String arch = ScoutSdk.getDefault().getBundle().getBundleContext().getProperty("osgi.arch");
    if (!StringUtility.isNullOrEmpty(arch)) {
      templateVariableSet.setVariable(VAR_ARCH, arch);
    }
    templateVariableSet.setVariable(VAR_CURRENT_DATE, SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT).format(new Date()));
    templateVariableSet.setVariable(VAR_USER_NAME, System.getProperty("user.name"));
    try {
      templateVariableSet.setVariable(VAR_LOCALHOST, InetAddress.getLocalHost().getHostName().toLowerCase());
    }
    catch (UnknownHostException e) {
      ScoutSdk.logWarning("could not determinate 'LOCALHOST'", e);
    }
    return templateVariableSet;
  }

  public static TemplateVariableSet createNew(String projectName, String namePostfix, String alias) {
    TemplateVariableSet templateVariableSet = createNew();
    templateVariableSet.setVariable(VAR_PROJECT_ALIAS, alias);
    templateVariableSet.setVariable(VAR_PROJECT_NAME, projectName);
    // bundle names
    String postfix = "";
    if (!StringUtility.isNullOrEmpty(namePostfix)) {
      postfix = "." + namePostfix;
    }
    templateVariableSet.setVariable(VAR_PROJECT_POSTFIX, postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_SWING_NAME, projectName + ".ui.swing" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_SWT_NAME, projectName + ".ui.swt" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_CLIENT_NAME, projectName + ".client" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_SHARED_NAME, projectName + ".shared" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_SERVER_NAME, projectName + ".server" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_CLIENT_TEST_NAME, projectName + ".client.test" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_SERVER_TEST_NAME, projectName + ".server.test" + postfix);
    templateVariableSet.setVariable(VAR_BUNDLE_PROJECTSETS_NAME, projectName + ".projectsets" + postfix);
    // templateVariableSet.setVariable(VAR_BUNDLE_CLIENT_NAME, projectName+"client"+postfix);
    // templateVariableSet.setVariable(VAR_BUNDLE_CLIENT_NAME, projectName+"client"+postfix);

    return templateVariableSet;
  }

  public static TemplateVariableSet createNew(IProject project) {
    TemplateVariableSet varset = createNew();
    varset = createNew(project, varset);
    return varset;

  }

  public static TemplateVariableSet createNew(IProject project, ITemplateVariableSet template) {
    TemplateVariableSet templateVariableSet = new TemplateVariableSet(template);
    templateVariableSet.setVariable(VAR_FS_ROOT, new File(project.getLocation().toOSString()).getAbsolutePath().replace('\\', '/'));
    return templateVariableSet;
  }

  public static TemplateVariableSet createNew(IScoutBundle scoutBundle) {
    return createNew(scoutBundle, createNew());
  }

  public static TemplateVariableSet createNew(IScoutBundle scoutBundle, ITemplateVariableSet template) {
    TemplateVariableSet templateVariableSet = createNew(scoutBundle.getProject(), template);
    String groupId = scoutBundle.getScoutProject().getProjectName();
    templateVariableSet.setVariable(VAR_PROJECT_NAME, groupId);
    templateVariableSet.setVariable(VAR_ROOT_PACKAGE, scoutBundle.getRootPackageName());
    return templateVariableSet;
  }

  @Override
  public String getVariable(String var) {
    return m_variables.get(var);
  }

  public void setVariable(String var, String value) {
    m_variables.put(var, value);
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return m_variables.entrySet();
  }

}
