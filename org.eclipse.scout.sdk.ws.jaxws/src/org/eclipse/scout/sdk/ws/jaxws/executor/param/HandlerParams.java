/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.executor.param;

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.w3c.dom.Element;

/**
 * <h3>{@link HandlerParams}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class HandlerParams {

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private Element m_xmlHandlerChain;

  /**
   * @param bundle
   * @param sunJaxWsBean
   * @param xmlHandlerChain
   */
  public HandlerParams(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, Element xmlHandlerChain) {
    super();
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_xmlHandlerChain = xmlHandlerChain;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }

  public Element getXmlHandlerChain() {
    return m_xmlHandlerChain;
  }

  public void setXmlHandlerChain(Element xmlHandlerChain) {
    m_xmlHandlerChain = xmlHandlerChain;
  }
}
