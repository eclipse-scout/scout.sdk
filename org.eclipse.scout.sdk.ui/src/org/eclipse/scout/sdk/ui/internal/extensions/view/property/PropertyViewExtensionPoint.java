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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.view.property.IMultiPropertyViewPart;
import org.eclipse.scout.sdk.ui.extensions.view.property.ISinglePropertyViewPart;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.osgi.framework.Bundle;

/**
 * <h3>PropertyViewExtensionPoint</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.07.2010
 */
public class PropertyViewExtensionPoint {
  private static final String propertyViewExtensionId = "propertyViewPart";
  private static PropertyViewExtensionPoint instance = new PropertyViewExtensionPoint();

  private ArrayList<PropertyViewExtension> m_extensions;

  private PropertyViewExtensionPoint() {
    init();
  }

  private void init() {
    try {
      HashMap<Class<? extends IPage>, PropertyViewExtension> extendsions = new HashMap<Class<? extends IPage>, PropertyViewExtension>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, propertyViewExtensionId);
      IExtension[] extensions = xp.getExtensions();
      for (IExtension extension : extensions) {
        Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
        for (IConfigurationElement partExtension : extension.getConfigurationElements()) {
          if (partExtension.getName().equals("part")) {
            long ranking = -1;
            String rankingAttribute = partExtension.getAttribute(IRuntimeClasses.EXTENSION_SERVICE_RANKING);
            if (!StringUtility.isNullOrEmpty(rankingAttribute)) {
              ranking = Long.parseLong(rankingAttribute);
            }
            Class<? extends IPage> pageClass = getClassOfElement(contributerBundle, partExtension.getAttribute("page"), IPage.class);
            PropertyViewExtension pageExt = extendsions.get(pageClass);
            if (pageExt == null) {
              pageExt = new PropertyViewExtension();
              pageExt.setPageClass(pageClass);
              extendsions.put(pageClass, pageExt);
            }
            for (IConfigurationElement part : partExtension.getChildren()) {
              if (part.getName().equals("singlePart")) {
                pageExt.setSingleViewPartClazz(getClassOfElement(contributerBundle, part.getAttribute("viewPart"), ISinglePropertyViewPart.class), ranking);
              }
              else if (part.getName().equals("multiPart")) {
                pageExt.setMultiViewPartClazz(getClassOfElement(contributerBundle, part.getAttribute("viewPart"), IMultiPropertyViewPart.class), ranking);
              }
            }
          }
        }
      }
      // order extensions
      TreeMap<CompositeObject, PropertyViewExtension> orderedExtensions = new TreeMap<CompositeObject, PropertyViewExtension>();
      for (PropertyViewExtension ext : extendsions.values()) {
        orderedExtensions.put(new CompositeObject(-distanceToIPage(ext.getPageClass(), 0), ext), ext);
      }
      m_extensions = new ArrayList<PropertyViewExtension>(orderedExtensions.values());
    }
    catch (Exception e) {
      ScoutSdkUi.logError("Error during reading property view extensions.", e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Class<? extends T> getClassOfElement(Bundle bundle, String clazzName, Class<T> t) {
    Class<? extends T> clazz = null;
    if (bundle != null) {
      if (!StringUtility.isNullOrEmpty(clazzName)) {
        try {
          clazz = (Class<? extends T>) bundle.loadClass(clazzName);
        }
        catch (Throwable tt) {
          ScoutSdkUi.logWarning("could not load class '" + clazzName + "' of bunlde '" + bundle.getSymbolicName() + "'.", tt);
        }
      }
    }
    return clazz;
  }

  private int distanceToIPage(Class<?> visitee, int dist) throws IllegalArgumentException {
    if (visitee == null) {
      throw new IllegalArgumentException("try to determ the distance to IPage of a instance not in subhierarchy of IPage.");
    }
    if (IPage.class.equals(visitee)) {
      return dist;
    }
    else {
      int locDist = 100000;
      Class<?> superclass = visitee.getSuperclass();
      if (superclass != null) {
        locDist = distanceToIPage(superclass, (dist + 1));
      }
      Class<?>[] interfaces = visitee.getInterfaces();
      if (interfaces != null) {
        for (Class<?> i : interfaces) {
          locDist = Math.min(locDist, distanceToIPage(i, (dist + 1)));
        }
      }
      dist = locDist;
      return dist;
    }
  }

  public static ISinglePropertyViewPart createSinglePageViewPart(IPage page) {
    return instance.createSinglePageViewPartImpl(page);
  }

  private ISinglePropertyViewPart createSinglePageViewPartImpl(IPage page) {
    for (PropertyViewExtension ext : m_extensions) {
      if (ext.getPageClass().isAssignableFrom(page.getClass())) {
        if (ext.getSingleViewPartClazz() != null) {
          return ext.createSingleViewPart();
        }
      }
    }
    return null;

  }

  public static IMultiPropertyViewPart createMultiPageViewPart(IPage[] pages) {
    return instance.createMultiPageViewPartImpl(pages);
  }

  private IMultiPropertyViewPart createMultiPageViewPartImpl(IPage[] pages) {
    Class<? extends IPage> commonClazz = findCommonPage(pages);
    for (PropertyViewExtension ext : m_extensions) {
      if (ext.getPageClass().isAssignableFrom(commonClazz)) {
        if (ext.getMultiViewPartClazz() != null) {
          return ext.createMultiViewPart();
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Class<? extends IPage> findCommonPage(IPage[] pages) {
    if (pages == null || pages.length == 0) {
      return null;
    }
    else if (pages.length == 1) {
      return pages[0].getClass();
    }
    else {
      ArrayList<Class<? extends IPage>> pageClasses = new ArrayList<Class<? extends IPage>>();
      Class<? extends IPage> visClazz = pages[0].getClass();
      while (visClazz != null && !visClazz.equals(Object.class)) {
        pageClasses.add(visClazz);
        visClazz = (Class<? extends IPage>) visClazz.getSuperclass();
        if (visClazz.equals(IPage.class)) {
          break;
        }
      }
      for (int i = 1; i < pages.length; i++) {
        ArrayList<Class<? extends IPage>> visClasses = new ArrayList<Class<? extends IPage>>();
        visClazz = pages[i].getClass();
        while (visClazz != null && !visClazz.equals(Object.class)) {
          if (pageClasses.contains(visClazz)) {
            visClasses.add(visClazz);
          }
          visClazz = (Class<? extends IPage>) visClazz.getSuperclass();
          if (visClazz.equals(IPage.class)) {
            break;
          }
        }
        pageClasses = visClasses;
      }
      if (pageClasses.size() > 0) {
        return pageClasses.get(0);
      }
      return null;
    }
  }
}
