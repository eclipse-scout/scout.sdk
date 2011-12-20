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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;

public class JaxWsServletRegistrationOperation implements IOperation {

  private IScoutBundle m_bundle;
  private IScoutBundle m_registrationBundle;
  private String m_jaxWsAlias;
  // used for URL pattern
  private SunJaxWsBean m_sunJaxWsBean;
  private String m_urlPattern;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle must not be null");
    }
    if (m_bundle.getType() != IScoutBundle.BUNDLE_SERVER) {
      throw new IllegalArgumentException("bundle must be SERVER bundle");
    }
    if (m_registrationBundle == null) {
      throw new IllegalArgumentException("servlet registration bundle must not be null");
    }
    if (m_registrationBundle.getType() != IScoutBundle.BUNDLE_SERVER) {
      throw new IllegalArgumentException("servlet registration bundle must be SERVER bundle");
    }
    if (!StringUtility.hasText(m_jaxWsAlias)) {
      throw new IllegalArgumentException("JAX-WS alias must not be null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String oldServletRegBundleName = null;

    // build-jaxws.xml
    XmlResource buildJaxWsResource = ResourceFactory.getBuildJaxWsResource(m_bundle);
    if (!JaxWsSdkUtility.exists(buildJaxWsResource.getFile())) {
      // create build-jaxws.xml file
      BuildJaxWsFileCreateOperation op = new BuildJaxWsFileCreateOperation(m_bundle);
      op.run(monitor, workingCopyManager);
    }
    // ensure servlet registration in build-jaxws.xml
    ScoutXmlDocument document = buildJaxWsResource.loadXml();
    ScoutXmlElement xml = document.getRoot().getChild(ServletRegistrationUtility.XML_SERVLET_BUNDLE);
    if (xml == null) {
      xml = document.getRoot().addChild(ServletRegistrationUtility.XML_SERVLET_BUNDLE);
    }

    if (xml.hasAttribute(ServletRegistrationUtility.XML_NAME)) {
      oldServletRegBundleName = xml.getAttribute(ServletRegistrationUtility.XML_NAME);
      xml.removeAttribute(ServletRegistrationUtility.XML_NAME);
    }
    xml.setAttribute(ServletRegistrationUtility.XML_NAME, m_registrationBundle.getBundleName());
    ResourceFactory.getBuildJaxWsResource(m_bundle, true).storeXml(xml.getDocument(), IResourceListener.EVENT_BUILDJAXWS_REPLACED, monitor, IResourceListener.ELEMENT_FILE);

    String jaxWsServletClass = JaxWsRuntimeClasses.JaxWsServlet.getFullyQualifiedName();
    String alias = StringUtility.trim(JaxWsSdkUtility.normalizePath(m_jaxWsAlias, SeparatorType.LeadingType));

    // get original alias of servlet registration bundle and remove the servlet registration to be registered anew
    PluginModelHelper h = new PluginModelHelper(m_registrationBundle.getProject());
    HashMap<String, String> attributes = new HashMap<String, String>();
    attributes.put("class", jaxWsServletClass);
    IPluginElement ext = h.PluginXml.getSimpleExtension(JaxWsConstants.SERVER_EXTENSION_POINT_SERVLETS, "servlet", attributes);
    String originalAlias = null;
    if (ext != null) {
      IPluginAttribute attribute = ext.getAttribute("alias");
      if (attribute != null) {
        originalAlias = attribute.getValue();
      }
    }

    // remove old servlet registration if servlet defining bundle is this bundle itself
    if (CompareUtility.equals(oldServletRegBundleName, m_bundle.getBundleName())) {
      PluginModelHelper oldPluginHelper = new PluginModelHelper(m_bundle.getProject());
      attributes = new HashMap<String, String>();
      attributes.put("class", jaxWsServletClass);
      oldPluginHelper.PluginXml.removeSimpleExtension(JaxWsConstants.SERVER_EXTENSION_POINT_SERVLETS, "servlet", attributes);
      oldPluginHelper.save();
    }

    // remove existing registration
    attributes = new HashMap<String, String>();
    attributes.put("class", jaxWsServletClass);
    h = new PluginModelHelper(m_registrationBundle.getProject());
    h.PluginXml.removeSimpleExtension(JaxWsConstants.SERVER_EXTENSION_POINT_SERVLETS, "servlet", attributes);
    // add new registration
    attributes = new HashMap<String, String>();
    attributes.put("class", jaxWsServletClass);
    attributes.put("alias", alias);
    h.PluginXml.addSimpleExtension(JaxWsConstants.SERVER_EXTENSION_POINT_SERVLETS, "servlet", attributes);
    h.save();

    // update sun-jaxws entries
    try {
      // iterate through all dependent bundles to find JAX-WS dependent bundles
      IScoutBundle[] candidateBundles = m_registrationBundle.getDependentBundles(new IScoutBundleFilter() {

        @Override
        public boolean accept(IScoutBundle bundle) {
          return bundle.getType() == IScoutBundle.BUNDLE_SERVER && TypeUtility.isOnClasspath(JaxWsRuntimeClasses.JaxWsActivator, bundle.getJavaProject()); // ensure JAX-WS installed on bundle
        }
      }, true);

      for (IScoutBundle candidateBundle : candidateBundles) {
        XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(candidateBundle);
        buildJaxWsResource = ResourceFactory.getBuildJaxWsResource(candidateBundle);

        if (JaxWsSdkUtility.exists(buildJaxWsResource.getFile()) &&
            JaxWsSdkUtility.exists(sunJaxWsResource.getFile())) {
          String bundleNameOfRegistration = ServletRegistrationUtility.getBuildJaxServletRegistrationBundleName(candidateBundle);
          if (bundleNameOfRegistration != null && bundleNameOfRegistration.equals(m_registrationBundle.getBundleName())) {
            ScoutXmlDocument sunJaxWsXmlDocument = sunJaxWsResource.loadXml();
            if (sunJaxWsXmlDocument == null) {
              continue;
            }
            List<String> changedEntries = new ArrayList<String>();
            for (Object sunJaxWsXmlEntry : sunJaxWsXmlDocument.getRoot().getChildren(StringUtility.join(":", sunJaxWsXmlDocument.getRoot().getNamePrefix(), SunJaxWsBean.XML_ENDPOINT))) {
              SunJaxWsBean sunJaxWsEntryBean = new SunJaxWsBean((ScoutXmlElement) sunJaxWsXmlEntry);
              changedEntries.add(sunJaxWsEntryBean.getAlias());

              String endpointPattern = sunJaxWsEntryBean.getUrlPattern();
              if (originalAlias != null && endpointPattern.startsWith(originalAlias)) {
                endpointPattern = endpointPattern.substring(originalAlias.length());
              }
              String urlPattern = JaxWsSdkUtility.normalizePath(m_jaxWsAlias, SeparatorType.BothType) + JaxWsSdkUtility.normalizePath(endpointPattern, SeparatorType.None);
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

    // change new URL pattenr
    if (m_sunJaxWsBean != null && m_urlPattern != null) {
      String urlPattern = m_urlPattern;
      if (!JaxWsSdkUtility.normalizePath(m_urlPattern, SeparatorType.LeadingType).startsWith(JaxWsSdkUtility.normalizePath(m_jaxWsAlias, SeparatorType.LeadingType))) {
        urlPattern = JaxWsSdkUtility.normalizePath(m_jaxWsAlias, SeparatorType.LeadingType) + JaxWsSdkUtility.normalizePath(m_urlPattern, SeparatorType.LeadingType);
      }
      m_sunJaxWsBean.setUrlPattern(urlPattern);
      ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_URL_PATTERN_CHANGED, m_sunJaxWsBean.getAlias());
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
