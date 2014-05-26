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
package org.eclipse.scout.sdk.internal.workspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ScoutFileLocator;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.graphics.ImageData;

public class ScoutProjectIcons implements IIconProvider {

  private static final String[] PREDEFINED_EXTENSIONS = new String[]{"png", "ico", "gif"};
  private static final String[] PREDEFINED_SUB_FOLDERS = new String[]{"resources/icons/internal/", "resources/icons/"};

  private final Object cacheLock = new Object();
  private final IType abstractIcons = TypeUtility.getType(IRuntimeClasses.AbstractIcons);
  private final IScoutBundle m_bundle;
  private final ICachedTypeHierarchy m_iconsHierarchy;

  private HashMap<String, ScoutIconDesc> m_cachedIcons;
  private String[] m_baseUrls;

  public ScoutProjectIcons(IScoutBundle bundle) {
    m_bundle = bundle;
    m_iconsHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractIcons);
    m_iconsHierarchy.addHierarchyListener(new ITypeHierarchyChangedListener() {
      @Override
      public void hierarchyInvalidated() {
        clearCache();
      }
    });
  }

  @Override
  public ScoutIconDesc[] getIcons() {
    cache();
    return m_cachedIcons.values().toArray(new ScoutIconDesc[m_cachedIcons.size()]);
  }

  @Override
  public ScoutIconDesc getIcon(String key) {
    cache();
    return m_cachedIcons.get(key);
  }

  // internal cache methods

  private void clearCache() {
    m_cachedIcons = null;
  }

  protected void cache() {
    synchronized (cacheLock) {
      if (m_cachedIcons == null) {
        m_baseUrls = buildBaseUrls();
        Map<String, ScoutIconDesc> collector = new HashMap<String, ScoutIconDesc>();
        collectIconNames(collector);
        for (ScoutIconDesc desc : collector.values()) {
          findIconInProject(desc);
        }
        m_cachedIcons = new HashMap<String, ScoutIconDesc>(collector);
      }
    }
  }

  private String[] buildBaseUrls() {
    List<String> projects = new ArrayList<String>();
    for (IScoutBundle parentBundle : m_bundle.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), true)) {
      projects.add(parentBundle.getSymbolicName());
    }

    projects.add(IRuntimeClasses.ScoutClientBundleId);
    projects.add(IRuntimeClasses.ScoutUiSwtBundleId);
    projects.add(IRuntimeClasses.ScoutUiSwingBundleId);

    String[] result = projects.toArray(new String[projects.size()]);
    Arrays.sort(result, Collections.reverseOrder()); //TODO: correct to use natural ordering?
    return result;
  }

  protected void collectIconNames(Map<String, ScoutIconDesc> collector) {
    Set<IScoutBundle> parentSharedBundles = m_bundle.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true);
    for (IScoutBundle parentShared : parentSharedBundles) {
      for (IType iconType : m_iconsHierarchy.getAllSubtypes(abstractIcons, ScoutTypeFilters.getInScoutBundles(parentShared))) {
        if (TypeUtility.exists(iconType)) {
          try {
            collectIconNamesOfType(iconType, collector);
          }
          catch (Exception e) {
            ScoutSdk.logWarning("Unable to collect icon names for class '" + iconType.getFullyQualifiedName() + "'.", e);
          }
        }
      }
    }
  }

  protected void collectIconNamesOfType(IType iconType, Map<String, ScoutIconDesc> collector) throws JavaModelException, IllegalArgumentException {
    if (TypeUtility.exists(iconType)) {
      boolean inherited = CompareUtility.notEquals(ScoutTypeUtility.getScoutBundle(iconType), m_bundle);
      for (IField field : iconType.getFields()) {
        if (Flags.isPublic(field.getFlags()) && field.getSource() != null && field.getSource().contains(" String ")) {
          Object fieldConstant = TypeUtility.getFieldConstant(field);
          if (fieldConstant instanceof String) {
            String iconName = fieldConstant.toString();
            collector.put(iconName, new ScoutIconDesc(field.getElementName(), iconName, field, inherited));
          }
        }
      }
      collectIconNamesOfType(m_iconsHierarchy.getSuperclass(iconType), collector);
    }
  }

  @SuppressWarnings("resource")
  protected void findIconInProject(ScoutIconDesc desc) {
    if (desc == null || desc.getIconName() == null) return;

    for (String baseUrl : m_baseUrls) {
      for (String ext : PREDEFINED_EXTENSIONS) {
        for (String folder : PREDEFINED_SUB_FOLDERS) {
          String imgPath = folder + desc.getIconName() + "." + ext;
          InputStream is = null;
          try {
            is = ScoutFileLocator.resolve(baseUrl, imgPath);
            if (is != null) {
              ImageDescriptor desc1 = ImageDescriptor.createFromImageData(new ImageData(is));
              if (desc1 != null) {
                desc.setImgDesc(desc1);
                return;
              }
            }
          }
          finally {
            if (is != null) {
              try {
                is.close();
              }
              catch (IOException e) {
                ScoutSdk.logWarning("could not close input stream of file '" + baseUrl + "" + imgPath + "'.", e);
              }
            }
          }
        }
      }
    }
  }
}
