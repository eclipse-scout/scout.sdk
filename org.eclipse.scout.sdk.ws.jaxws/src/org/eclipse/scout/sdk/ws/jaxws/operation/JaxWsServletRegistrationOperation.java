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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JaxWsServletRegistrationOperation implements IOperation {

  private IScoutBundle m_bundle;
  private IScoutBundle m_registrationBundle;
  private String m_jaxWsAlias;
  // used for URL pattern
  private SunJaxWsBean m_sunJaxWsBean;
  private String m_urlPattern;

  @Override
  public void validate() {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle must not be null");
    }
    if (!m_bundle.hasType(IScoutBundle.TYPE_SERVER)) {
      throw new IllegalArgumentException("bundle must be SERVER bundle");
    }
    if (m_registrationBundle == null) {
      throw new IllegalArgumentException("servlet registration bundle must not be null");
    }
    if (!m_registrationBundle.hasType(IScoutBundle.TYPE_SERVER)) {
      throw new IllegalArgumentException("servlet registration bundle must be SERVER bundle");
    }
    if (!StringUtility.hasText(m_jaxWsAlias)) {
      throw new IllegalArgumentException("JAX-WS alias must not be null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String oldServletRegBundleName = null;

    // build-jaxws.xml
    XmlResource buildJaxWsResource = ResourceFactory.getBuildJaxWsResource(m_bundle);
    if (!JaxWsSdkUtility.exists(buildJaxWsResource.getFile())) {
      // create build-jaxws.xml file
      BuildJaxWsFileCreateOperation op = new BuildJaxWsFileCreateOperation(m_bundle);
      op.run(monitor, workingCopyManager);
    }
    // ensure servlet registration in build-jaxws.xml
    Document document = buildJaxWsResource.loadXml();
    Element xml = XmlUtility.getFirstChildElement(document.getDocumentElement(), ServletRegistrationUtility.XML_SERVLET_BUNDLE);
    if (xml == null) {
      xml = document.createElement(ServletRegistrationUtility.XML_SERVLET_BUNDLE);
      document.getDocumentElement().appendChild(xml);
    }
    if (xml.hasAttribute(ServletRegistrationUtility.XML_NAME)) {
      oldServletRegBundleName = xml.getAttribute(ServletRegistrationUtility.XML_NAME);
      xml.removeAttribute(ServletRegistrationUtility.XML_NAME);
    }
    xml.setAttribute(ServletRegistrationUtility.XML_NAME, m_registrationBundle.getSymbolicName());
    ResourceFactory.getBuildJaxWsResource(m_bundle, true).storeXml(xml.getOwnerDocument(), IResourceListener.EVENT_BUILDJAXWS_REPLACED, monitor, IResourceListener.ELEMENT_FILE);

    String jaxWsServletClass = TypeUtility.getType(JaxWsRuntimeClasses.JaxWsServlet).getFullyQualifiedName();

    String alias = PathNormalizer.toServletAlias(m_jaxWsAlias);

    // get original alias of servlet registration bundle and remove the servlet registration to be registered anew
    PluginModelHelper h = new PluginModelHelper(m_registrationBundle.getProject());
    HashMap<String, String> attributes = new HashMap<String, String>();
    attributes.put("class", jaxWsServletClass);
    IPluginElement ext = h.PluginXml.getSimpleExtension(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes);
    String originalAlias = null;
    if (ext != null) {
      IPluginAttribute attribute = ext.getAttribute("alias");
      if (attribute != null) {
        originalAlias = attribute.getValue();
      }
    }

    // remove old servlet registration if servlet defining bundle is this bundle itself
    if (CompareUtility.equals(oldServletRegBundleName, m_bundle.getSymbolicName())) {
      PluginModelHelper oldPluginHelper = new PluginModelHelper(m_bundle.getProject());
      attributes = new HashMap<String, String>();
      attributes.put("class", jaxWsServletClass);
      oldPluginHelper.PluginXml.removeSimpleExtension(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes);
      oldPluginHelper.save();
    }

    // remove existing registration
    attributes = new HashMap<String, String>();
    attributes.put("class", jaxWsServletClass);
    h = new PluginModelHelper(m_registrationBundle.getProject());
    h.PluginXml.removeSimpleExtension(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes);
    // add new registration
    attributes = new HashMap<String, String>();
    attributes.put("class", jaxWsServletClass);
    attributes.put("alias", alias);
    h.PluginXml.addSimpleExtension(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes);
    h.save();

    // update sun-jaxws entries
    try {
      // iterate through all dependent bundles to find JAX-WS dependent bundles
      Set<? extends IScoutBundle> candidateBundles = m_registrationBundle.getParentBundles(new IScoutBundleFilter() {
        @Override
        public boolean accept(IScoutBundle bundle) {
          return bundle.hasType(IScoutBundle.TYPE_SERVER) && TypeUtility.isOnClasspath(TypeUtility.getType(JaxWsRuntimeClasses.JaxWsActivator), bundle.getJavaProject()); // ensure JAX-WS installed on bundle
        }
      }, true);

      for (IScoutBundle candidateBundle : candidateBundles) {
        XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(candidateBundle);
        buildJaxWsResource = ResourceFactory.getBuildJaxWsResource(candidateBundle);

        if (JaxWsSdkUtility.exists(buildJaxWsResource.getFile())
            && JaxWsSdkUtility.exists(sunJaxWsResource.getFile())) {
          String bundleNameOfRegistration = ServletRegistrationUtility.getBuildJaxServletRegistrationBundleName(candidateBundle);
          if (bundleNameOfRegistration != null && bundleNameOfRegistration.equals(m_registrationBundle.getSymbolicName())) {
            Document sunJaxWsXmlDocument = sunJaxWsResource.loadXml();
            if (sunJaxWsXmlDocument == null) {
              continue;
            }
            List<String> changedEntries = new ArrayList<String>();
            Element root = sunJaxWsXmlDocument.getDocumentElement();
            String tagName = StringUtility.join(":", JaxWsSdkUtility.getXmlPrefix(root), SunJaxWsBean.XML_ENDPOINT);
            for (Element sunJaxWsXmlEntry : XmlUtility.getChildElements(root, tagName)) {
              SunJaxWsBean sunJaxWsEntryBean = new SunJaxWsBean(sunJaxWsXmlEntry);
              changedEntries.add(sunJaxWsEntryBean.getAlias());

              String endpointPattern = sunJaxWsEntryBean.getUrlPattern();
              if (originalAlias != null && endpointPattern.startsWith(originalAlias)) {
                endpointPattern = endpointPattern.substring(originalAlias.length());
              }
              String urlPattern = PathNormalizer.toUrlPattern(alias, endpointPattern);
              sunJaxWsEntryBean.setUrlPattern(urlPattern);
            }
            if (changedEntries.size() > 0) {
              sunJaxWsResource.storeXmlAsync(sunJaxWsXmlDocument, IResourceListener.EVENT_SUNJAXWS_URL_PATTERN_CHANGED, changedEntries.toArray(new String[changedEntries.size()]));
            }
          }
        }
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError("faild to update URL-pattern", e);
    }

    // change new URL pattern
    if (m_sunJaxWsBean != null && m_urlPattern != null) {
      String urlPattern = PathNormalizer.toUrlPattern(m_urlPattern);

      if (!urlPattern.startsWith(alias)) {
        urlPattern = PathNormalizer.toUrlPattern(alias, urlPattern);
      }
      m_sunJaxWsBean.setUrlPattern(urlPattern);
      ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_URL_PATTERN_CHANGED, m_sunJaxWsBean.getAlias());
    }
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

  public IScoutBundle getRegistrationBundle() {
    return m_registrationBundle;
  }

  public void setRegistrationBundle(IScoutBundle registrationBundle) {
    m_registrationBundle = registrationBundle;
  }

  public String getJaxWsAlias() {
    return m_jaxWsAlias;
  }

  public void setJaxWsAlias(String jaxWsAlias) {
    m_jaxWsAlias = jaxWsAlias;
  }

  public String getUrlPattern() {
    return m_urlPattern;
  }

  public void setUrlPattern(String urlPattern) {
    m_urlPattern = urlPattern;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }
}
