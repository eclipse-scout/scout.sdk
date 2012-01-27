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
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public final class ResourceFactory {

  private static final Map<IScoutBundle, XmlResource> sm_sunJaxWsResourceMap = new HashMap<IScoutBundle, XmlResource>();
  private static final Map<IScoutBundle, XmlResource> sm_buildJaxWsResourceMap = new HashMap<IScoutBundle, XmlResource>();

  private ResourceFactory() {
  }

  public static synchronized XmlResource getSunJaxWsResource(IScoutBundle bundle) {
    return getSunJaxWsResource(bundle, false);
  }

  public static synchronized XmlResource getSunJaxWsResource(IScoutBundle bundle, boolean autoCreate) {
    if (!sm_sunJaxWsResourceMap.containsKey(bundle)) {
      XmlResource resource = new XmlResource(bundle);
      resource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_SUN_JAXWS, autoCreate));
      sm_sunJaxWsResourceMap.put(bundle, resource);
    }
    XmlResource xmlResource = sm_sunJaxWsResourceMap.get(bundle);
    if (autoCreate && (xmlResource.getFile() == null || !xmlResource.existsFile())) {
      xmlResource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_SUN_JAXWS, autoCreate));
    }
    return xmlResource;
  }

  public static synchronized XmlResource getBuildJaxWsResource(IScoutBundle bundle) {
    return getBuildJaxWsResource(bundle, false);
  }

  public static synchronized XmlResource getBuildJaxWsResource(IScoutBundle bundle, boolean autoCreate) {
    if (!sm_buildJaxWsResourceMap.containsKey(bundle)) {
      XmlResource resource = new XmlResource(bundle);
      resource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_BUILD_JAXWS, autoCreate));
      sm_buildJaxWsResourceMap.put(bundle, resource);
    }
    XmlResource xmlResource = sm_buildJaxWsResourceMap.get(bundle);
    if (autoCreate && (xmlResource.getFile() == null || !xmlResource.existsFile())) {
      xmlResource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_BUILD_JAXWS, autoCreate));
    }
    return xmlResource;
  }
}
