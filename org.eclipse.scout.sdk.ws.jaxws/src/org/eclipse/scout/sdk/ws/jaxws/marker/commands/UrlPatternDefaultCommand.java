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
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;

public class UrlPatternDefaultCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private String m_defaultUrlPattern;

  public UrlPatternDefaultCommand(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, String servletAlias, String problem) {
    super(problem);
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_defaultUrlPattern = PathNormalizer.toUrlPattern(servletAlias, m_sunJaxWsBean.getAlias());
    setSolutionDescription("By using this task, the URL pattern is changed to '" + m_defaultUrlPattern + "'");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    m_sunJaxWsBean.setUrlPattern(m_defaultUrlPattern);
    XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(m_bundle);
    sunJaxWsResource.storeXmlAsync(m_sunJaxWsBean.getXml().getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_URL_PATTERN_CHANGED, m_sunJaxWsBean.getAlias());
  }
}
