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
package org.eclipse.scout.sdk.ui.internal.extensions.project.template;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ui.extensions.project.template.IProjectTemplate;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 *
 */
public final class ProjectTemplateExtensionPoint {

  public static final String EXTENSION_ID = "scoutProjectTemplate";
  public static final String ATTRIBUTE_ID = "id";
  public static final String ATTRIBUTE_ORDER_NUMBER = "orderNumber";
  public static final String ATTRIBUTE_CLASS = "class";
  public static final String ATTRIBUTE_ICON = "icon";
  private static final Object LOCK = new Object();

  private static volatile Set<ProjectTemplateExtension> templateExtensions;

  private ProjectTemplateExtensionPoint() {
  }

  private static Set<ProjectTemplateExtension> initTemplates() {
    TreeSet<ProjectTemplateExtension> result = new TreeSet<>(new P_ExtensionComparator());
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_ID);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        ProjectTemplateExtension extensionPoint = new ProjectTemplateExtension();
        extensionPoint.setExtensionId(element.getAttribute(ATTRIBUTE_ID));
        try {
          extensionPoint.setTemplate((IProjectTemplate) element.createExecutableExtension(ATTRIBUTE_CLASS));
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not load class '" + element.getAttribute(ATTRIBUTE_CLASS) + "'.", e);
        }
        extensionPoint.setIconPath(element.getAttribute(ATTRIBUTE_ICON));
        String attOrderNr = element.getAttribute(ATTRIBUTE_ORDER_NUMBER);
        try {
          extensionPoint.setOrderNr(Long.parseLong(attOrderNr));
        }
        catch (NumberFormatException e) {
          ScoutSdkUi.logError("could not parse order number '" + attOrderNr + "' of extension '" + element.getNamespaceIdentifier() + "'.", e);
        }
        if (extensionPoint.isValidConfiguration()) {
          result.add(extensionPoint);
        }
      }
    }
    return result;
  }

  private static Set<ProjectTemplateExtension> getTemplates() {
    if (templateExtensions == null) {
      synchronized (LOCK) {
        if (templateExtensions == null) {
          templateExtensions = initTemplates();
        }
      }
    }
    return templateExtensions;
  }

  public static List<ProjectTemplateExtension> getExtensions() {
    return CollectionUtility.arrayList(getTemplates());
  }

  private static final class P_ExtensionComparator implements Comparator<ProjectTemplateExtension>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ProjectTemplateExtension o1, ProjectTemplateExtension o2) {
      if (o1.getOrderNr() != o2.getOrderNr()) {
        int diff = (int) (o1.getOrderNr() - o2.getOrderNr());
        if (diff != 0) {
          return diff;
        }
      }
      return o1.getExtensionId().compareTo(o2.getExtensionId());
    }
  }
}
