/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.BuildPropertiesWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.BuildProperty;

public class BuildPropertiesWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private BuildJaxWsBean m_buildJaxWsBean;
  private List<BuildProperty> m_properties;

  private BuildPropertiesWizardPage m_wizardPage;

  public BuildPropertiesWizard(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean) {
    setWindowTitle(Texts.get("WsBuildDirectives"));
    m_bundle = bundle;
    m_buildJaxWsBean = buildJaxWsBean;
    m_properties = loadProperties(m_buildJaxWsBean);
  }

  @Override
  public void addPages() {
    m_wizardPage = new BuildPropertiesWizardPage();
    m_wizardPage.setProperties(m_properties);
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_properties = m_wizardPage.getProperties();
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    Map<String, List<String>> propertyMap = new HashMap<String, List<String>>();
    for (BuildProperty property : m_properties) {
      if (!propertyMap.containsKey(property.getName())) {
        propertyMap.put(property.getName(), new LinkedList<String>());
      }
      if (StringUtility.hasText(property.getValue())) {
        propertyMap.get(property.getName()).add(property.getValue());
      }
    }
    m_buildJaxWsBean.setProperties(propertyMap);

    // store buildJaxWsBean
    ScoutXmlDocument xmlDocument = m_buildJaxWsBean.getXml().getDocument();
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(xmlDocument, m_buildJaxWsBean.getAlias(), IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED, monitor);

    return true;
  }

  private List<BuildProperty> loadProperties(BuildJaxWsBean buildJaxWsBean) {
    List<BuildProperty> properties = new LinkedList<BuildProperty>();

    for (Entry<String, List<String>> entry : buildJaxWsBean.getPropertiers().entrySet()) {
      String name = entry.getKey();
      List<String> values = entry.getValue();

      if (values == null || values.size() == 0) {
        properties.add(new BuildProperty(entry.getKey(), null));
      }
      else {
        for (String value : values) {
          properties.add(new BuildProperty(name, value));
        }
      }
    }
    return properties;
  }
}
