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
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.osgi.framework.Bundle;

/**
 * <h3>ExplorerExtensions</h3>
 */
public final class ExplorerPageExtensionPoint {
  public static final String EXPLORER_PAGE_EXTENSION_ID = "explorerPage";
  public static final String PARENT_PAGE_ATTRIBUTE = "parentPageId";
  public static final String PAGE_ATTRIBUTE = "page";
  public static final String PAGE_FACTORY_ATTRIBUTE = "pageFactory";

  private static final Object LOCK = new Object();
  private static Map<String /* parentId */, List<ExplorerPageExtension> /* extensions */> pages = null;

  private ExplorerPageExtensionPoint() {
  }

  private static void load() {
    if (pages == null) {
      synchronized (LOCK) {
        if (pages == null) {
          Map<String, List<ExplorerPageExtension>> tmp = new HashMap<>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXPLORER_PAGE_EXTENSION_ID);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (PAGE_ATTRIBUTE.equals(element.getName())) {
                ExplorerPageExtension pageExt = new ExplorerPageExtension();
                pageExt.setParentPageId(element.getAttribute(PARENT_PAGE_ATTRIBUTE));
                Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
                Class<? extends IPage> pageClass = getClassOfContribution(contributerBundle, element, "page", IPage.class);
                if (pageClass != null) {
                  pageExt.setPageClass(pageClass);
                  addExtension(tmp, pageExt);
                }
              }
              else if (PAGE_FACTORY_ATTRIBUTE.equals(element.getName())) {
                ExplorerPageExtension pageExt = new ExplorerPageExtension();
                pageExt.setParentPageId(element.getAttribute(PARENT_PAGE_ATTRIBUTE));
                Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
                Class<? extends IPageFactory> factoryClass = getClassOfContribution(contributerBundle, element, "factory", IPageFactory.class);
                if (factoryClass != null) {
                  pageExt.setFactoryClass(factoryClass);
                  addExtension(tmp, pageExt);
                }
              }
            }
          }
          pages = tmp;
        }
      }
    }
  }

  private static void addExtension(Map<String, List<ExplorerPageExtension>> collector, ExplorerPageExtension page) {
    List<ExplorerPageExtension> childNodes = collector.get(page.getParentPageId());
    if (childNodes == null) {
      childNodes = new ArrayList<>();
      collector.put(page.getParentPageId(), childNodes);
    }
    childNodes.add(page);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<? extends T> getClassOfContribution(Bundle bundle, IConfigurationElement element, String attribute, Class<T> t) {
    Class<? extends T> clazz = null;
    if (bundle != null) {
      String clazzName = element.getAttribute(attribute);
      if (StringUtility.hasText(clazzName)) {
        try {
          clazz = (Class<? extends T>) bundle.loadClass(clazzName);
        }
        catch (Exception tt) {
          ScoutSdkUi.logWarning("Could not load class of extension '" + element.getName() + "'.", tt);
        }
      }
      else {
        ScoutSdkUi.logWarning("Page extension without class found. Contributed in bundle '" + bundle.getSymbolicName() + "'. Will be ignored.");
      }
    }
    return clazz;
  }

  public static List<ExplorerPageExtension> getExtensions(IPage parentPage) {
    load();
    List<ExplorerPageExtension> list = pages.get(parentPage.getPageId());
    if (list != null) {
      return CollectionUtility.arrayList(list);
    }
    return CollectionUtility.emptyArrayList();
  }

  public static class ExplorerPageExtension {
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

    public IPageFactory createFactoryInstance() {
      try {
        return getFactoryClass().newInstance();
      }
      catch (Exception e) {
        ScoutSdkUi.logError("could not instanciate class '" + getFactoryClass().getName() + "'.", e);
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
