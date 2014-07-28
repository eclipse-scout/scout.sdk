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
package org.eclipse.scout.sdk.ui.internal.extensions.view.property;

import org.eclipse.scout.sdk.ui.extensions.view.property.IMultiPropertyViewPart;
import org.eclipse.scout.sdk.ui.extensions.view.property.ISinglePropertyViewPart;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>PropertyViewExtension</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 19.07.2010
 */
public class PropertyViewExtension {

  private Class<? extends IPage> m_pageClass;
  private long m_singleViewPartRanking = Long.MIN_VALUE;
  private long m_multiViewPartRanking = Long.MIN_VALUE;

  private Class<? extends ISinglePropertyViewPart> m_singleViewPartClazz;
  private Class<? extends IMultiPropertyViewPart> m_multiViewPartClazz;

  public IMultiPropertyViewPart createMultiViewPart() {
    if (getSingleViewPartClazz() == null) {
      ScoutSdkUi.logWarning("view part is not defined");
      return null;
    }
    try {
      return getMultiViewPartClazz().newInstance();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not instansiate new view part '" + getSingleViewPartClazz().getName() + "'.", e);
      return null;
    }
  }

  public ISinglePropertyViewPart createSingleViewPart() {
    if (getSingleViewPartClazz() == null) {
      ScoutSdkUi.logWarning("view part is not defined");
      return null;
    }
    try {
      return getSingleViewPartClazz().newInstance();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not instansiate new view part '" + getSingleViewPartClazz().getName() + "'.", e);
      return null;
    }
  }

  /**
   * @param pageClass
   *          the pageClass to set
   */
  public void setPageClass(Class<? extends IPage> pageClass) {
    m_pageClass = pageClass;
  }

  /**
   * @return the pageClass
   */
  public Class<? extends IPage> getPageClass() {
    return m_pageClass;
  }

  /**
   * @param singleViewPartClazz
   *          the singleViewPartClazz to set
   */
  public void setSingleViewPartClazz(Class<? extends ISinglePropertyViewPart> singleViewPartClazz, long ranking) {
    if (m_singleViewPartRanking < ranking) {
      m_singleViewPartClazz = singleViewPartClazz;
      m_singleViewPartRanking = ranking;
    }
  }

  /**
   * @return the singleViewPartClazz
   */
  public Class<? extends ISinglePropertyViewPart> getSingleViewPartClazz() {
    return m_singleViewPartClazz;
  }

  /**
   * @param multiViewPartClazz
   *          the multiViewPartClazz to set
   */
  public void setMultiViewPartClazz(Class<? extends IMultiPropertyViewPart> multiViewPartClazz, long ranking) {
    if (m_multiViewPartRanking < ranking) {
      m_multiViewPartClazz = multiViewPartClazz;
      m_multiViewPartRanking = ranking;
    }

  }

  /**
   * @return the multiViewPartClazz
   */
  public Class<? extends IMultiPropertyViewPart> getMultiViewPartClazz() {
    return m_multiViewPartClazz;
  }

}
