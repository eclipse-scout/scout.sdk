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
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.pde.PluginXml;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;

public class JaxWsServletRegistrationOperation implements IOperation {

  private static final String ELEMENT_SERVLET = "servlet";
  private static final String ATTR_ALIAS = "alias";
  private static final String ATTR_CLASS = "class";

  private IScoutBundle m_bundle;
  private String m_jaxWsAlias;

  public JaxWsServletRegistrationOperation() {
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle must not be null");
    }
    if (!StringUtility.hasText(m_jaxWsAlias)) {
      throw new IllegalArgumentException("JAX-WS alias must not be null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IScoutBundle servletContributingBundle = JaxWsSdkUtility.getServletContributingBundle(m_bundle);
    if (servletContributingBundle == null) {
      JaxWsSdk.logError("Failed to find servlet contributing bundle");
      return;
    }

    // plugin.xml of bundle
    String jaxWsServletClass = JaxWsRuntimeClasses.JaxWsServlet.getFullyQualifiedName();

    PluginXml pluginXml = new PluginXml(servletContributingBundle.getProject());
    SimpleXmlElement point = pluginXml.getOrCreateExtension(JaxWsConstants.SERVER_EXTENSION_POINT_SERVLETS);
    SimpleXmlElement registration = null;

    // find JAX-WS servlet extension
    for (SimpleXmlElement xmlElement : point.getChildren(ELEMENT_SERVLET)) {
      if (jaxWsServletClass.equals(xmlElement.getStringAttribute(ATTR_CLASS))) {
        registration = xmlElement;
        break;
      }
    }

    String servletAlias = StringUtility.trim(JaxWsSdkUtility.normalizePath(m_jaxWsAlias, SeparatorType.LeadingType));
    String oldServletAlias = null;
    if (registration == null) {
      registration = new SimpleXmlElement(ELEMENT_SERVLET);
      registration.setAttribute(ATTR_ALIAS, servletAlias);
      registration.setAttribute(ATTR_CLASS, jaxWsServletClass);
      point.addChild(registration);
      pluginXml.store(monitor);
      updateSunJaxWsEntries(oldServletAlias);
    }
    else {
      oldServletAlias = registration.getStringAttribute(ATTR_ALIAS);
      if (oldServletAlias == null || !oldServletAlias.equals(servletAlias)) {
        registration.setAttribute(ATTR_ALIAS, servletAlias);
        pluginXml.store(monitor);
        updateSunJaxWsEntries(oldServletAlias);
      }
    }
  }

  private void updateSunJaxWsEntries(String oldAlias) {
    try {
      IScoutBundle rootBundle = JaxWsSdkUtility.getRootBundle(m_bundle);
      if (rootBundle == null) {
        JaxWsSdk.logWarning("Root project bundle could not be found. URL-pattern of webservice providers could not be updated.");
        return;
      }

      // iterate through all dependent bundles to find JAX-WS dependent bundles
      IScoutBundle[] jaxWsServerBundles = rootBundle.getDependentBundles(new IScoutBundleFilter() {

        @Override
        public boolean accept(IScoutBundle bundle) {
          return bundle.getType() == IScoutBundle.BUNDLE_SERVER &&
                 TypeUtility.isOnClasspath(JaxWsRuntimeClasses.JaxWsActivator, bundle.getJavaProject()); // ensure JAX-WS installed on bundle
        }
      }, true);
      for (IScoutBundle jaxWsServerBundle : jaxWsServerBundles) {
        JaxWsSdk.logInfo("Update URL-pattern of JAX-registrations in bundle '" + jaxWsServerBundle.getBundleName() + "'");
        XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(jaxWsServerBundle);
        if (JaxWsSdkUtility.exists(sunJaxWsResource.getFile())) {
          ScoutXmlDocument sunJaxWsXmlDocument = sunJaxWsResource.loadXml();

          for (Object sunJaxWsXmlEntry : sunJaxWsXmlDocument.getRoot().getChildren(StringUtility.join(":", sunJaxWsXmlDocument.getRoot().getNamePrefix(), SunJaxWsBean.XML_ENDPOINT))) {
            SunJaxWsBean sunJaxWsEntryBean = new SunJaxWsBean((ScoutXmlElement) sunJaxWsXmlEntry);
            String urlPattern = sunJaxWsEntryBean.getUrlPattern();
            if (oldAlias == null) {
              urlPattern = sunJaxWsEntryBean.getAlias();
            }
            else if (urlPattern.startsWith(JaxWsSdkUtility.normalizePath(oldAlias, SeparatorType.BothType))) {
              urlPattern = urlPattern.substring(oldAlias.length());
            }
            urlPattern = JaxWsSdkUtility.normalizePath(m_jaxWsAlias, SeparatorType.BothType) + JaxWsSdkUtility.normalizePath(urlPattern, SeparatorType.None);
            sunJaxWsEntryBean.setUrlPattern(urlPattern);
          }
          sunJaxWsResource.storeXmlAsync(sunJaxWsXmlDocument, IResourceListener.ELEMENT_FILE, IResourceListener.EVENT_SUNJAXWS_REPLACED);
        }
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError("faild to update URL-pattern", e);
    }
  }

  public static IScoutBundleFilter getServerFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getType() == IScoutBundle.BUNDLE_SERVER;
      }
    };
  }

  @Override
  public String getOperationName() {
    return JaxWsServletRegistrationOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public String getJaxWsAlias() {
    return m_jaxWsAlias;
  }

  public void setJaxWsAlias(String jaxWsAlias) {
    m_jaxWsAlias = jaxWsAlias;
  }
}
