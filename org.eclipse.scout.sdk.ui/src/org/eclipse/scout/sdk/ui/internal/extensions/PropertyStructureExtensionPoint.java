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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;

/**
 * <h3>PropertyStructureExtensionPoint</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 12.05.2010
 */
public class PropertyStructureExtensionPoint {
  private static final String propertyStructureExtensionId = "propertyStructure";
  private static final String propertyDefinitionAttribute = "propertyDefinition";
  private static final String fileAttribute = "file";

  private static final PropertyStructureExtensionPoint instance = new PropertyStructureExtensionPoint();

  private PropertyStructureExtensionPoint() {
    init();
  }

  private void init() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, propertyStructureExtensionId);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {

      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if (element.getName().equals(propertyDefinitionAttribute)) {
          String fileName = element.getAttribute(fileAttribute);

        }
      }
    }
  }

}
