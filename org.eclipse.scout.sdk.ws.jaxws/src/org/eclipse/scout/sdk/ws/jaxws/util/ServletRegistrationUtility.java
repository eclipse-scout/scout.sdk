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
package org.eclipse.scout.sdk.ws.jaxws.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;

public final class ServletRegistrationUtility {

  public static final String XML_SERVLET_BUNDLE = "servlet-bundle";
  public static final String XML_NAME = "name";

  private ServletRegistrationUtility() {
  }

  public static String getBuildJaxServletRegistrationBundleName(IScoutBundle bundle) {
    if (bundle == null) {
      return null;
    }
    if (IScoutBundle.TYPE_SERVER.equals(bundle.getType())) {
      return null;
    }

    ScoutXmlDocument document = ResourceFactory.getBuildJaxWsResource(bundle).loadXml();
    if (document == null || document.getRoot() == null) {
      return null;
    }

    ScoutXmlElement xml = document.getRoot().getChild(XML_SERVLET_BUNDLE);
    if (xml == null) {
      return null;
    }
    if (!xml.hasAttribute(XML_NAME)) {
      return null;
    }
    final String bundleName = xml.getAttribute(XML_NAME);
    if (!StringUtility.hasText(bundleName)) {
      return null;
    }

    return bundleName;
  }

  public static Registration getServletRegistration(IScoutBundle bundle) {
    final String bundleName = getBuildJaxServletRegistrationBundleName(bundle);

    Registration[] registrations = getJaxWsServletRegistrationsOnClasspath(bundle);
    for (Registration registration : registrations) {
      if (registration.getBundle().getSymbolicName().equals(bundleName)) {
        return registration;
      }
    }
    return null;
  }

  public static IScoutBundle[] getJaxWsBundlesOnClasspath(IScoutBundle bundle) {
    // get bundles on classpath with JAX-WS dependency installed
    if (bundle == null) {
      return new IScoutBundle[0];
    }

    // find required bundles with JAX-WS on classpath
    return bundle.getParentBundles(new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle candidate) {
        // ensure server bundle
        if (!IScoutBundle.TYPE_SERVER.equals(candidate.getType())) {
          return false;
        }
        // check whether JAX-WS dependency is installed on bundle
        if (!TypeUtility.exists(TypeUtility.getType(JaxWsRuntimeClasses.JaxWsActivator)) || !TypeUtility.isOnClasspath(TypeUtility.getType(JaxWsRuntimeClasses.JaxWsActivator), candidate.getJavaProject())) {
          return false;
        }
        return true;
      }
    }, true);
  }

  /**
   * All JAX-WS servlet registrations on the classpath of the given bundle
   * 
   * @param bundle
   * @return
   */
  public static Registration[] getJaxWsServletRegistrationsOnClasspath(IScoutBundle bundle) {
    // filter bundles with JAX-WS servlet registration in plugin.xml
    Set<Registration> bundles = new HashSet<Registration>();
    for (IScoutBundle candidate : getJaxWsBundlesOnClasspath(bundle)) {
      String extensionPoint = IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS;
      HashMap<String, String> attributes = new HashMap<String, String>();
      attributes.put("class", TypeUtility.getType(JaxWsRuntimeClasses.JaxWsServlet).getFullyQualifiedName());
      PluginModelHelper h = new PluginModelHelper(candidate.getProject());
      IPluginElement ex = h.PluginXml.getSimpleExtension(extensionPoint, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes);
      if (ex == null) {
        continue;
      }
      IPluginAttribute a = ex.getAttribute("alias");
      if (a == null) {
        continue;
      }
      bundles.add(new Registration(candidate, a.getValue()));
    }

    return bundles.toArray(new Registration[bundles.size()]);
  }

  public static String getAlias(IScoutBundle bundle) {
    Registration registration = ServletRegistrationUtility.getServletRegistration(bundle);
    if (registration == null) {
      return null;
    }
    return registration.getAlias();
  }

  public static class Registration {
    private IScoutBundle m_bundle;
    private String m_alias;

    private Registration(IScoutBundle bundle, String alias) {
      m_bundle = bundle;
      m_alias = alias;
    }

    public IScoutBundle getBundle() {
      return m_bundle;
    }

    public void setBundle(IScoutBundle bundle) {
      m_bundle = bundle;
    }

    public String getAlias() {
      return m_alias;
    }

    public void setAlias(String alias) {
      m_alias = alias;
    }
  }
}
