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
package org.eclipse.scout.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 * <h3>{@link RuntimeClassExtensionPoint}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 22.11.2012
 */
public final class RuntimeClasses implements IRuntimeClasses {
  private final static String PREFERENCES_PREFIX = "default.super.types.";

  private final static String EXTENSION_POINT_NAME = "runtimeClass";
  private final static String TAG_NAME = "element";
  private final static String ATTRIB_DEFAULT_CLASS = "default";
  private final static String ATTRIB_INTERFACE = "interface";

  private final static Object lock = new Object();
  private final static Map<IScoutProject, Map<String /* interfaceFqn */, StringHolder /* configured value */>> configuredValues = new HashMap<IScoutProject, Map<String, StringHolder>>();
  private final static Map<IScoutProject, IPreferenceChangeListener> registeredListeners = new HashMap<IScoutProject, IPreferenceChangeListener>();
  private static Map<String /* interfaceFqn */, String /* default value */> defaultValues = null;

  private RuntimeClasses() {
  }

  private static Map<String, String> getDefaults() {
    if (defaultValues == null) {
      synchronized (lock) {
        if (defaultValues == null) {
          Map<String, String> tmp = new HashMap<String, String>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME.equals(element.getName())) {
                String def = element.getAttribute(ATTRIB_DEFAULT_CLASS);
                String interf = element.getAttribute(ATTRIB_INTERFACE);
                if (TypeUtility.exists(TypeUtility.getType(def)) && TypeUtility.exists(TypeUtility.getType(interf))) {
                  String existing = tmp.put(interf, def);
                  if (existing != null) {
                    ScoutSdk.logWarning("Multiple super type definitions found for interface '" + interf + "'.");
                  }
                }
                else {
                  ScoutSdk.logWarning("Super type definition (interface='" + interf + "', default='" + def + "') is not valid. Types could not be found.");
                }
              }
            }
          }
          defaultValues = tmp;

          P_ScoutWorkspaceListener l = new P_ScoutWorkspaceListener();
          ScoutWorkspace.getInstance().addWorkspaceListener(l);
        }
      }
    }
    return defaultValues;
  }

  private static String getConfiguredSuperTypeNameCached(IScoutProject context, String interfaceFqn) {
    synchronized (lock) {
      Map<String, StringHolder> projectConfigs = configuredValues.get(context);
      if (projectConfigs == null) {
        projectConfigs = new HashMap<String, StringHolder>();
        configuredValues.put(context, projectConfigs);

        // first time we are reading properties for the given project -> we are starting to cache for that project
        // add listener to get informed when the settings change so that we can clear our cache for the project.
        P_PreferenceChangeListener listener = new P_PreferenceChangeListener(projectConfigs);
        registeredListeners.put(context, listener); // remember in case we must remove it again later on
        context.getPreferences().addPreferenceChangeListener(listener);
      }
      StringHolder ret = projectConfigs.get(interfaceFqn);
      if (ret == null) {
        ret = new StringHolder(context.getPreferences().get(getPreferenceKey(interfaceFqn), null));
        projectConfigs.put(interfaceFqn, ret);
      }
      return ret.getValue();
    }
  }

  public static String getPreferenceKey(String interfaceFqn) {
    return PREFERENCES_PREFIX + interfaceFqn;
  }

  public static Map<String, String> getAllDefaults() {
    Map<String, String> classes = getDefaults();
    HashMap<String, String> ret = new HashMap<String, String>(classes.size());
    for (Entry<String, String> entry : classes.entrySet()) {
      ret.put(entry.getKey(), entry.getValue());
    }
    return ret;
  }

  public static IType getSuperType(IType interfaceType, IScoutProject context) {
    return getSuperType(interfaceType.getFullyQualifiedName(), context);
  }

  public static IType getSuperType(IType interfaceType, IJavaProject context) {
    return getSuperType(interfaceType.getFullyQualifiedName(), context);
  }

  public static IType getSuperType(String interfaceFqn, IJavaProject context) {
    return TypeUtility.getType(getSuperTypeName(interfaceFqn, context));
  }

  public static IType getSuperType(String interfaceFqn, IScoutProject context) {
    return TypeUtility.getType(getSuperTypeName(interfaceFqn, context));
  }

  public static String getSuperTypeName(IType interfaceType, IScoutProject context) {
    return getSuperTypeName(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeName(IType interfaceType, IJavaProject context) {
    return getSuperTypeName(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeName(String interfaceFqn, IJavaProject jp) {
    IScoutProject context = ScoutWorkspace.getInstance().getScoutProject(jp.getProject());
    return getSuperTypeName(interfaceFqn, context);
  }

  public static String getSuperTypeName(String interfaceFqn, IScoutProject context) {
    String config = getConfiguredSuperTypeNameCached(context, interfaceFqn);
    if (config == null) {
      return getDefaults().get(interfaceFqn);
    }
    else {
      IType t = TypeUtility.getType(config);
      if (TypeUtility.exists(t)) {
        return config;
      }
      else {
        return getDefaults().get(interfaceFqn);
      }
    }
  }

  public static String getSuperTypeSignature(IType interfaceType, IJavaProject context) {
    return getSuperTypeSignature(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeSignature(IType interfaceType, IScoutProject context) {
    return getSuperTypeSignature(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeSignature(String interfaceFqn, IScoutProject context) {
    String superType = getSuperTypeName(interfaceFqn, context);
    return SignatureCache.createTypeSignature(superType);
  }

  public static String getSuperTypeSignature(String interfaceFqn, IJavaProject context) {
    String superType = getSuperTypeName(interfaceFqn, context);
    return SignatureCache.createTypeSignature(superType);
  }

  private static class P_PreferenceChangeListener implements IPreferenceChangeListener {
    private Map<String, StringHolder> m_projectConfig;

    private P_PreferenceChangeListener(Map<String, StringHolder> projectConfigs) {
      m_projectConfig = projectConfigs;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
      String key = event.getKey();
      if (key != null && key.startsWith(PREFERENCES_PREFIX)) {
        m_projectConfig.remove(key.substring(PREFERENCES_PREFIX.length())); // will be lazily filled again on next use
      }
    }
  }

  private static class P_ScoutWorkspaceListener implements IScoutWorkspaceListener {
    @Override
    public void worspaceChanged(ScoutWorkspaceEvent event) {
      if (event.getType() == ScoutWorkspaceEvent.TYPE_PROJECT_REMOVED) {
        if (event.getScoutElement() instanceof IScoutProject) {
          IScoutProject removed = (IScoutProject) event.getScoutElement();
          // A scout project is removed from the workspace: also remove all caches of this project
          configuredValues.remove(removed);

          // there is still a listener attached to the project preferences -> remove and deregister
          IPreferenceChangeListener existingListener = registeredListeners.remove(removed);
          if (existingListener != null) {
            removed.getPreferences().removePreferenceChangeListener(existingListener);
          }
        }
      }
    }
  }
}
