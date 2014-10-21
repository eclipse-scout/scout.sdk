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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.codeid.parsers.ICodeIdParser;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public final class CodeIdExtensionPoint {

  private static final String EXTENSION_POINT_NAME = "codeId";
  private static final String CODE_ID_PROVIDER_EXT_NAME = "codeIdProvider";
  private static final String CODE_ID_PARSER_EXT_NAME = "codeIdParser";
  private static final String ATTRIB_CLASS = "class";
  private static final String ATTRIB_GENERIC_TYPE = "genericType";
  private static final String ATTRIB_PRIO = "priority";

  private static final Object CODE_ID_PROV_LOCK = new Object();
  private static volatile List<ICodeIdProvider> codeIdProviderExtensions;

  private static final Object CODE_ID_PARSER_LOCK = new Object();
  private static volatile Map<String, Map<CompositeObject, ICodeIdParser>> codeIdParsers;

  private CodeIdExtensionPoint() {
  }

  /**
   * @return all extensions in the prioritized order.
   */
  private static List<ICodeIdProvider> getCodeIdProviderExtensions() {
    if (codeIdProviderExtensions == null) {
      synchronized (CODE_ID_PROV_LOCK) {
        if (codeIdProviderExtensions == null) {
          Map<CompositeObject, ICodeIdProvider> providers = new TreeMap<CompositeObject, ICodeIdProvider>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] providerElememts = extension.getConfigurationElements();
            for (IConfigurationElement providerElememt : providerElememts) {
              if (CODE_ID_PROVIDER_EXT_NAME.equals(providerElememt.getName())) {
                try {
                  ICodeIdProvider provider = (ICodeIdProvider) providerElememt.createExecutableExtension(ATTRIB_CLASS);
                  providers.put(new CompositeObject(getPriority(providerElememt), provider.getClass().getName()), provider);
                }
                catch (Exception t) {
                  ScoutSdk.logError("Error registering code id provider '" + providerElememt.getNamespaceIdentifier() + "'.", t);
                }
              }
            }
          }
          codeIdProviderExtensions = CollectionUtility.arrayList(providers.values());
        }
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

  private static Map<String, Map<CompositeObject, ICodeIdParser>> getCodeIdParsers() {
    if (codeIdParsers == null) {
      synchronized (CODE_ID_PARSER_LOCK) {
        if (codeIdParsers == null) {
          Map<String, Map<CompositeObject, ICodeIdParser>> parsers = new HashMap<String, Map<CompositeObject, ICodeIdParser>>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            for (IConfigurationElement parserElememt : extension.getConfigurationElements()) {
              if (CODE_ID_PARSER_EXT_NAME.equals(parserElememt.getName())) {
                String genericType = parserElememt.getAttribute(ATTRIB_GENERIC_TYPE);
                if (StringUtility.hasText(genericType)) {
                  String genericTypeSignature = SignatureCache.createTypeSignature(genericType);
                  try {
                    int prio = getPriority(parserElememt);
                    ICodeIdParser parser = (ICodeIdParser) parserElememt.createExecutableExtension(ATTRIB_CLASS);
                    Map<CompositeObject, ICodeIdParser> typeParsers = parsers.get(genericTypeSignature);
                    if (typeParsers == null) {
                      typeParsers = new TreeMap<CompositeObject, ICodeIdParser>();
                      parsers.put(genericTypeSignature, typeParsers);
                    }
                    typeParsers.put(new CompositeObject(prio, parser.getClass().getName()), parser);
                  }
                  catch (Exception t) {
                    ScoutSdk.logError("Error registering code id parser '" + parserElememt.getNamespaceIdentifier() + "'.", t);
                  }
                }
              }
            }
          }
          codeIdParsers = CollectionUtility.copyMap(parsers);
        }
      }
    }
    return codeIdParsers;
  }

  public static ICodeIdParser getCodeIdParser(String genericType) {
    Map<CompositeObject, ICodeIdParser> parsers = getCodeIdParsers().get(genericType);
    if (parsers != null) {
      return CollectionUtility.firstElement(parsers.values());
    }
    return null;
  }

  public static String getNextCodeId(IScoutBundle projectGroup, String genericSignature) {
    for (ICodeIdProvider p : getCodeIdProviderExtensions()) {
      try {
        String value = p.getNextId(projectGroup, genericSignature);
        if (value != null) {
          return value;
        }
      }
      catch (Exception e) {
        ScoutSdk.logWarning("Exception in codeIdExtension '" + p.getClass().getName() + "'.", e);
      }
    }
    return null;
  }
}
