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
package org.eclipse.scout.sdk.extensions.codeid;

import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.extensions.codeid.parsers.ICodeIdParser;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public final class CodeIdExtensionPoint {

  private final static String EXTENSION_POINT_NAME = "codeId";
  private final static String CODE_ID_PROVIDER_EXT_NAME = "codeIdProvider";
  private final static String CODE_ID_PARSER_EXT_NAME = "codeIdParser";
  private final static String ATTRIB_CLASS = "class";
  private final static String ATTRIB_GENERIC_TYPE = "genericType";
  private final static String ATTRIB_PRIO = "priority";

  private final static Object codeIdProviderExtensionsCacheLock = new Object();
  private static volatile ICodeIdProvider[] codeIdProviderExtensions;

  private final static Object codeIdParsersExtensionsCacheLock = new Object();
  private static HashMap<String, TreeMap<CompositeObject, ICodeIdParser>> codeIdParsers;

  private CodeIdExtensionPoint() {
  }

  /**
   * @return all extensions in the prioritized order.
   */
  private static ICodeIdProvider[] getCodeIdProviderExtensions() {
    synchronized (codeIdProviderExtensionsCacheLock) {
      if (codeIdProviderExtensions == null) {
        TreeMap<CompositeObject, ICodeIdProvider> providers = new TreeMap<CompositeObject, ICodeIdProvider>();
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
        IExtension[] extensions = xp.getExtensions();
        for (IExtension extension : extensions) {
          IConfigurationElement[] providerElememts = extension.getConfigurationElements();
          for (int i = 0; i < providerElememts.length; i++) {
            IConfigurationElement providerElememt = providerElememts[i];
            if (CODE_ID_PROVIDER_EXT_NAME.equals(providerElememt.getName())) {
              String className = providerElememt.getAttribute(ATTRIB_CLASS);

              ScoutSdk.logInfo("found code id provider: " + className);
              try {
                ICodeIdProvider provider = (ICodeIdProvider) providerElememt.createExecutableExtension(ATTRIB_CLASS);
                providers.put(new CompositeObject(getPriority(providerElememt), i, provider), provider);
              }
              catch (Exception t) {
                ScoutSdk.logError("register code id provider: " + className, t);
              }
            }
          }
        }
        codeIdProviderExtensions = providers.values().toArray(new ICodeIdProvider[providers.size()]);
      }
    }
    return codeIdProviderExtensions;
  }

  private static int getPriority(IConfigurationElement element) {
    int priority = 0;
    try {
      String prio = element.getAttribute(ATTRIB_PRIO);
      priority = Integer.MAX_VALUE - Integer.parseInt(prio); /* descending order: highest prio first */
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not parse priority of " + EXTENSION_POINT_NAME + " extension '" + element.getName() + "'", e);
    }
    return priority;
  }

  private static HashMap<String, TreeMap<CompositeObject, ICodeIdParser>> getCodeIdParsers() {
    synchronized (codeIdParsersExtensionsCacheLock) {
      if (codeIdParsers == null) {
        HashMap<String, TreeMap<CompositeObject, ICodeIdParser>> parsers = new HashMap<String, TreeMap<CompositeObject, ICodeIdParser>>();
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
        IExtension[] extensions = xp.getExtensions();
        for (IExtension extension : extensions) {
          IConfigurationElement[] codeIdElements = extension.getConfigurationElements();
          for (int i = 0; i < codeIdElements.length; i++) {
            IConfigurationElement parserElememt = codeIdElements[i];
            if (CODE_ID_PARSER_EXT_NAME.equals(parserElememt.getName())) {
              String className = parserElememt.getAttribute(ATTRIB_CLASS);
              String genericTypeSignature = SignatureCache.createTypeSignature(parserElememt.getAttribute(ATTRIB_GENERIC_TYPE));
              int prio = getPriority(parserElememt);
              ScoutSdk.logInfo("found code id parser: " + className);

              try {
                ICodeIdParser parser = (ICodeIdParser) parserElememt.createExecutableExtension(ATTRIB_CLASS);
                TreeMap<CompositeObject, ICodeIdParser> typeParsers = parsers.get(genericTypeSignature);
                if (typeParsers == null) {
                  typeParsers = new TreeMap<CompositeObject, ICodeIdParser>();
                  parsers.put(genericTypeSignature, typeParsers);
                }
                typeParsers.put(new CompositeObject(prio, i, parser), parser);
              }
              catch (Exception t) {
                ScoutSdk.logError("register code id parser: " + className, t);
              }
            }
          }
        }
        codeIdParsers = parsers;
      }
      return codeIdParsers;
    }
  }

  public static ICodeIdParser getCodeIdParser(String genericType) {
    TreeMap<CompositeObject, ICodeIdParser> parsers = getCodeIdParsers().get(genericType);
    if (parsers != null && parsers.size() > 0) {
      return parsers.values().iterator().next();
    }
    return null;
  }

  public static String getNextCodeId(IScoutBundle projectGroup, String genericSignature) {
    String value = null;
    ICodeIdProvider[] providers = getCodeIdProviderExtensions();
    for (ICodeIdProvider p : providers) {
      try {
        value = p.getNextId(projectGroup, genericSignature);
        if (value != null) {
          break;
        }
      }
      catch (Exception e) {
        ScoutSdk.logWarning("Exception in codeIdExtension '" + p.getClass().getName() + "'", e);
      }
    }
    return value;
  }
}
