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
package org.eclipse.scout.sdk.internal.workspace.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateEventFilter;

/**
 * <h3>{@link DtoUpdateEventFilter}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 07.10.2013
 */
public final class DtoUpdateEventFilter {

  private static final Object LOCK = new Object();
  private static final String EXTENSION_POINT_NAME = "dtoUpdateEventFilter";
  private static final String TAG_NAME = "filter";
  private static final String FILTER_CLASS_ATTRIB = "class";

  private static volatile IDtoAutoUpdateEventFilter[] filters = null;

  private DtoUpdateEventFilter() {
  }

  public static IDtoAutoUpdateEventFilter[] getFilters() {
    if (filters == null) {
      synchronized (LOCK) {
        if (filters == null) {
          List<IDtoAutoUpdateEventFilter> tmp = new ArrayList<IDtoAutoUpdateEventFilter>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME.equals(element.getName())) {
                try {
                  Object filterCandidate = element.createExecutableExtension(FILTER_CLASS_ATTRIB);
                  if (filterCandidate instanceof IDtoAutoUpdateEventFilter) {
                    tmp.add((IDtoAutoUpdateEventFilter) filterCandidate);
                  }
                  else {
                    ScoutSdk.logError("Invalid filter specified by element '" + element.getNamespaceIdentifier() + "'.");
                  }
                }
                catch (CoreException e) {
                  ScoutSdk.logError("Unable to load filter '" + element.getNamespaceIdentifier() + "'.", e);
                }
              }
            }
          }
          filters = tmp.toArray(new IDtoAutoUpdateEventFilter[tmp.size()]);
        }
      }
    }
    return filters.clone();
  }
}