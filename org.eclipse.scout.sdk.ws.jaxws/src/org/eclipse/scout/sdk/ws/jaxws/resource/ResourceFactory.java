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
package org.eclipse.scout.sdk.ws.jaxws.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public final class ResourceFactory {

  private static final Map<IScoutBundle, XmlResource> SM_SUN_JAX_WS_RESOURCE_MAP = new HashMap<>();
  private static final Map<IScoutBundle, XmlResource> SM_BUILD_JAXWS_RESOURCE_MAP = new HashMap<>();

  private ResourceFactory() {
  }

  public static synchronized XmlResource getSunJaxWsResource(IScoutBundle bundle) {
    return getSunJaxWsResource(bundle, false);
  }

  public static synchronized XmlResource getSunJaxWsResource(IScoutBundle bundle, boolean autoCreate) {
    if (!SM_SUN_JAX_WS_RESOURCE_MAP.containsKey(bundle)) {
      XmlResource resource = new XmlResource(bundle);
      resource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_SUN_JAXWS, autoCreate));
      SM_SUN_JAX_WS_RESOURCE_MAP.put(bundle, resource);
    }
    XmlResource xmlResource = SM_SUN_JAX_WS_RESOURCE_MAP.get(bundle);
    if (autoCreate && (xmlResource.getFile() == null || !xmlResource.existsFile())) {
      xmlResource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_SUN_JAXWS, autoCreate));
    }
    return xmlResource;
  }

  public static synchronized XmlResource getBuildJaxWsResource(IScoutBundle bundle) {
    return getBuildJaxWsResource(bundle, false);
  }

  public static synchronized XmlResource getBuildJaxWsResource(IScoutBundle bundle, boolean autoCreate) {
    if (!SM_BUILD_JAXWS_RESOURCE_MAP.containsKey(bundle)) {
      XmlResource resource = new XmlResource(bundle);
      resource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_BUILD_JAXWS, autoCreate));
      SM_BUILD_JAXWS_RESOURCE_MAP.put(bundle, resource);
    }
    XmlResource xmlResource = SM_BUILD_JAXWS_RESOURCE_MAP.get(bundle);
    if (autoCreate && (xmlResource.getFile() == null || !xmlResource.existsFile())) {
      xmlResource.setFile(JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_BUILD_JAXWS, autoCreate));
    }
    return xmlResource;
  }
}
