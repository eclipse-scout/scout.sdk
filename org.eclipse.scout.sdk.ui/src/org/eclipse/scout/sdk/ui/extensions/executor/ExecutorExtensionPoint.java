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
package org.eclipse.scout.sdk.ui.extensions.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ExecutorExtensionPoint}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 08.10.2014
 */
public final class ExecutorExtensionPoint {

  public static final IExecutor EMPTY_EXECUTOR = new IExecutor() {
    @Override
    public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
      return null;
    }

    @Override
    public boolean canRun(IStructuredSelection selection) {
      return true;
    }
  };
  public static final String EXTENSION_POINT_NAME = "executor";
  public static final String TAG_NAME_EXECUTOR = "executor";
  public static final String ATTRIB_NAME_CLASS = "class";
  public static final String ATTRIB_NAME_ID = "id";
  public static final String ATTRIB_NAME_ORDER = "order";
  public static final String ATTRIB_NAME_ACTIVE = "active";

  private static final Object LOCK = new Object();
  private static volatile Map<String /* id */, NavigableMap<CompositeObject, Class<? extends IExecutor>>> executors = null;

  private ExecutorExtensionPoint() {
  }

  private static Map<String, NavigableMap<CompositeObject, Class<? extends IExecutor>>> getExectutors() {
    if (executors == null) {
      synchronized (LOCK) {
        if (executors == null) {
          Map<String, NavigableMap<CompositeObject, Class<? extends IExecutor>>> tmp = new HashMap<String, NavigableMap<CompositeObject, Class<? extends IExecutor>>>();

          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME_EXECUTOR.equals(element.getName())) {
                String clazz = StringUtility.trim(element.getAttribute(ATTRIB_NAME_CLASS));
                String id = StringUtility.trim(element.getAttribute(ATTRIB_NAME_ID));
                String order = StringUtility.trim(element.getAttribute(ATTRIB_NAME_ORDER));
                String active = StringUtility.trim(element.getAttribute(ATTRIB_NAME_ACTIVE));
                if (!StringUtility.isNullOrEmpty(clazz) && !StringUtility.isNullOrEmpty(id) && !StringUtility.isNullOrEmpty(order)) {
                  if (StringUtility.isNullOrEmpty(active) || "true".equalsIgnoreCase(active)) {
                    Integer orderInt = RuntimeBundles.parseOrder(order);
                    if (orderInt != null) {
                      String contribPlugin = element.getContributor().getName();
                      Bundle bundle = Platform.getBundle(contribPlugin);
                      if (bundle != null) {
                        try {
                          @SuppressWarnings("unchecked")
                          Class<? extends IExecutor> execClass = (Class<? extends IExecutor>) bundle.loadClass(clazz);
                          NavigableMap<CompositeObject, Class<? extends IExecutor>> map = tmp.get(id);
                          if (map == null) {
                            map = new TreeMap<CompositeObject, Class<? extends IExecutor>>();
                            tmp.put(id, map);
                          }
                          map.put(new CompositeObject(orderInt, execClass.getName()), execClass);
                        }
                        catch (ClassNotFoundException e) {
                          ScoutSdkUi.logError("Unable to load class '" + clazz + "'. Executor for id '" + id + "' will be skipped.", e);
                        }
                      }
                      else {
                        ScoutSdkUi.logError("Contributing bundle of executor '" + id + "' could not be found.");
                      }
                    }
                    else {
                      ScoutSdkUi.logError("Invalid order for executor with id '" + id + "': " + order);
                    }
                  }
                }
                else {
                  ScoutSdkUi.logError("Invalid executor extension from bundle '" + element.getContributor().getName() + "'. At least " + ATTRIB_NAME_CLASS + ", " + ATTRIB_NAME_ID + " and " + ATTRIB_NAME_ORDER + " must be specified.");
                }
              }
            }
          }

          executors = CollectionUtility.copyMap(tmp);
        }
      }
    }
    return executors;
  }

  /**
   * Gets the executor for the given id.<br>
   * Executors must be registered with this id using the extension point.
   *
   * @param id
   * @return The executor matching the given id or the {@link #EMPTY_EXECUTOR} when no executor for the given id can be
   *         found. Never returns null.
   */
  public static IExecutor getExecutorFor(String id) {
    Map<String, NavigableMap<CompositeObject, Class<? extends IExecutor>>> allExectutors = getExectutors();
    NavigableMap<CompositeObject, Class<? extends IExecutor>> executorsForId = allExectutors.get(id);
    if (executorsForId != null && !executorsForId.isEmpty()) {
      Class<? extends IExecutor> executorClass = executorsForId.lastEntry().getValue();
      try {
        return executorClass.newInstance();
      }
      catch (Exception e) {
        ScoutSdkUi.logError("unable to create executor for id '" + id + "'.", e);
      }
    }
    ScoutSdkUi.logWarning("No executor found for id '" + id + "'.");
    return EMPTY_EXECUTOR;
  }
}
