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
package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.osgi.framework.Bundle;

/**
 * <h3>ExplorerExtensions</h3> ...
 */
public class ExplorerPageExtensionPoint {
  private static final String explorerPageExtensionId = "explorerPage";
  private static final String pageAttribute = "page";
  private static final String pageFactoryAttribute = "pageFactory";
  private HashMap<String /* parentId */, List<ExplorerPageExtension> /* extensions */> m_pages = new HashMap<String, List<ExplorerPageExtension>>();

  private static ExplorerPageExtensionPoint instance = new ExplorerPageExtensionPoint();

  private ExplorerPageExtensionPoint() {
    init();
  }

  private void init() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, explorerPageExtensionId);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if (element.getName().equals(pageAttribute)) {
          ExplorerPageExtension pageExt = new ExplorerPageExtension();
          pageExt.setParentPageId(element.getAttribute("parentPageId"));
          Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
          pageExt.setPageClass(getClassOfContribution(contributerBundle, elements, "page", IPage.class));
          addExtension(pageExt);
        }
        else if (element.getName().equals(pageFactoryAttribute)) {
          ExplorerPageExtension pageExt = new ExplorerPageExtension();
          pageExt.setParentPageId(element.getAttribute("parentPageId"));
          Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
          pageExt.setFactoryClass(getClassOfContribution(contributerBundle, elements, "factory", IPageFactory.class));
          addExtension(pageExt);
        }
      }
    }
  }

  private void addExtension(ExplorerPageExtension page) {
    List<ExplorerPageExtension> childNodes = m_pages.get(page.getParentPageId());
    if (childNodes == null) {
      childNodes = new ArrayList<ExplorerPageExtension>();
      m_pages.put(page.getParentPageId(), childNodes);
    }
    childNodes.add(page);
  }

  @SuppressWarnings("unchecked")
  private <T> Class<? extends T> getClassOfContribution(Bundle bundle, IConfigurationElement[] elements, String attribute, Class<T> t) {
    Class<? extends T> clazz = null;
    if (bundle != null) {
      // wizard
      if (elements != null && elements.length == 1) {
        String clazzName = elements[0].getAttribute(attribute);
        if (!StringUtility.isNullOrEmpty(clazzName)) {
          try {
            clazz = (Class<? extends T>) bundle.loadClass(clazzName);
          }
          catch (Throwable tt) {
            ScoutSdkUi.logWarning("could not load class of extension '" + elements[0].getName() + "'.", tt);
          }
        }
      }
    }
    return clazz;
  }

  public static ExplorerPageExtension[] getExtensions(IPage parentPage) {
    return instance.getExtensionsImpl(parentPage);
  }

  private ExplorerPageExtension[] getExtensionsImpl(IPage parentPage) {
    List<ExplorerPageExtension> list = m_pages.get(parentPage.getPageId());
    if (list != null) {
      return list.toArray(new ExplorerPageExtension[list.size()]);
    }
    return new ExplorerPageExtension[0];
  }

  public class ExplorerPageExtension {
    private Class<? extends IPage> m_pageClass;
    private Class<? extends IPageFactory> m_factoryClass;
    private String m_parentPageId;

    public void setPageClass(Class<? extends IPage> pageClass) {
      m_pageClass = pageClass;
    }

    public Class<? extends IPage> getPageClass() {
      return m_pageClass;
    }

    public IPage createPageInstance() {
      try {
        return getPageClass().newInstance();
      }
      catch (Exception e) {
        ScoutSdkUi.logError("could not instanciate class '" + getPageClass().getName() + "'.", e);
        return null;
      }
    }

    /**
     * @param factoryClass
     *          the factoryClass to set
     */
    public void setFactoryClass(Class<? extends IPageFactory> factoryClass) {
      m_factoryClass = factoryClass;
    }

    /**
     * @return the factoryClass
     */
    public Class<? extends IPageFactory> getFactoryClass() {
      return m_factoryClass;
    }

    public IPageFactory createFactoryClass() {
      try {
        return getFactoryClass().newInstance();
      }
      catch (Exception e) {
        ScoutSdkUi.logError("could not instanciate class '" + getFactoryClass().getName() + "'.");
        return null;
      }
    }

    public String getParentPageId() {
      return m_parentPageId;
    }

    private void setParentPageId(String parentPageId) {
      m_parentPageId = parentPageId;
    }

  } // end class ExplorerPageExtension
}
