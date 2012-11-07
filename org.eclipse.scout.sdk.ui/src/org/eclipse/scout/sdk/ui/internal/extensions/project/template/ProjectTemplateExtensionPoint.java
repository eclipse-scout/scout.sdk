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

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.extensions.project.template.IProjectTemplate;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 *
 */
public class ProjectTemplateExtensionPoint {
  private static String extensionId = "scoutProjectTemplate";
  private static String attributeId = "id";
  private static String attributeOrderNumber = "orderNumber";
  private static String attributeClass = "class";
  private static String attributeIcon = "icon";

  private static final ProjectTemplateExtensionPoint instance = new ProjectTemplateExtensionPoint();

  private TreeSet<ProjectTemplateExtension> m_extensions;

  private ProjectTemplateExtensionPoint() {
    init();
  }

  /**
   *
   */
  private void init() {
    m_extensions = new TreeSet<ProjectTemplateExtension>(new P_ExtensionComparator());
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, extensionId);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        ProjectTemplateExtension extensionPoint = new ProjectTemplateExtension();
        extensionPoint.setExtensionId(element.getAttribute(attributeId));
        try {
          extensionPoint.setTemplate((IProjectTemplate) element.createExecutableExtension(attributeClass));
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not load class '" + element.getAttribute(attributeClass) + "'.", e);
        }
        extensionPoint.setIconPath(element.getAttribute(attributeIcon));
        String attOrderNr = element.getAttribute(attributeOrderNumber);
        try {
          extensionPoint.setOrderNr(new Long(attOrderNr).intValue());
        }
        catch (NumberFormatException e) {
          ScoutSdkUi.logError("could not parse order number '" + attOrderNr + "' of extension '" + element.getNamespaceIdentifier() + "'.", e);
        }
        if (extensionPoint.isValidConfiguration()) {
          m_extensions.add(extensionPoint);
        }
      }
    }
  }

  public static final ProjectTemplateExtension[] getExtensions() {
    return instance.getExtensionsImpl();
  }

  private final ProjectTemplateExtension[] getExtensionsImpl() {
    return m_extensions.toArray(new ProjectTemplateExtension[m_extensions.size()]);
  }

  private class P_ExtensionComparator implements Comparator<ProjectTemplateExtension> {
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
