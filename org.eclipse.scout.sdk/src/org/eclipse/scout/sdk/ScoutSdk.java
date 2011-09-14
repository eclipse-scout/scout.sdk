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

import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.internal.workspace.typecache.HierarchyCache;
import org.eclipse.scout.sdk.internal.workspace.typecache.JavaResourceChangedEmitter;
import org.eclipse.scout.sdk.internal.workspace.typecache.TypeCache;
import org.eclipse.scout.sdk.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAutoUpdater;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScoutSdk extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk";
  public static final String NATURE_ID = PLUGIN_ID + ".ScoutNature";
  private static final String LOG_LEVEL = PLUGIN_ID + ".loglevel";
  private static ScoutSdk plugin;

  private Object m_initializeLock = new Object();
  private IScoutWorkspace m_workspace;
  private TypeCache m_typeCache;
  private HierarchyCache m_hierarchyCache;
  private JavaResourceChangedEmitter m_javaResourceChangedEmitter;
  private FormDataAutoUpdater m_formDataUpdateSupport;
  private int m_loglevel;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_loglevel = parseLogLevel(context.getProperty(LOG_LEVEL));
    plugin = this;
    m_typeCache = new TypeCache();
    m_hierarchyCache = new HierarchyCache();
//    m_structuredTypeCache = new StructuredTypeCache();
    log(new Status(IStatus.INFO, ScoutSdk.PLUGIN_ID, "Starting SCOUT SDK Plugin."));
    m_javaResourceChangedEmitter = new JavaResourceChangedEmitter(m_hierarchyCache);
//    new ScoutMarkerSupport();
    m_formDataUpdateSupport = new FormDataAutoUpdater();

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    TuningUtility.finishAll();
    m_typeCache.dispose();
    m_hierarchyCache.dispose();
    m_javaResourceChangedEmitter.dispose();
    m_formDataUpdateSupport.dispose();

//    m_structuredTypeCache.dispose();
//    ScoutTypeHierarchy.storeCaches();
    // SwtUtility.dispose();
    plugin = null;
    m_workspace = null;
    super.stop(context);
  }

  public static ScoutSdk getDefault() {
    return plugin;
  }

  public static void log(IStatus log) {
    if (log instanceof LogStatus) {
      getDefault().logImpl((LogStatus) log);
    }
    else {
      getDefault().logImpl(new LogStatus(ScoutSdk.class, log.getSeverity(), log.getPlugin(), log.getMessage(), log.getException()));
    }
  }

  private void logImpl(LogStatus log) {
    if ((log.getSeverity() & m_loglevel) != 0) {
      getLog().log(log);
    }
  }

  public static void logInfo(String message) {
    logInfo(message, null);
  }

  public static void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdk.class, IStatus.INFO, PLUGIN_ID, message, t));
  }

  public static void logWarning(String message) {
    logWarning(message, null);
  }

  public static void logWarning(Throwable t) {
    logWarning(null, t);
  }

  public static void logWarning(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdk.class, IStatus.WARNING, PLUGIN_ID, message, t));
  }

  public static void logError(Throwable t) {
    logError("", t);
  }

  public static void logError(String message) {
    logError(message, null);
  }

  public static void logError(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdk.class, IStatus.ERROR, PLUGIN_ID, message, t));
  }

  public static IScoutWorkspace getScoutWorkspace() {
    return getDefault().getScoutWorkspaceImpl();
  }

  private IScoutWorkspace getScoutWorkspaceImpl() {
    if (m_workspace == null && plugin != null) {
      synchronized (m_initializeLock) {
        if (m_workspace == null) {
          m_workspace = new ScoutWorkspace();
        }
      }
    }
    return m_workspace;
  }

  private TypeCache getTypeCache() {
    return m_typeCache;
  }

  private HierarchyCache getHierarchyCache() {
    return m_hierarchyCache;
  }

  public JavaResourceChangedEmitter getJavaResourceChangedEmitter() {
    return m_javaResourceChangedEmitter;
  }

  public static IType getType(String fullyQualifiedName) {
    return getDefault().getTypeCache().getType(fullyQualifiedName);
  }

  public static boolean existsType(String fullyQualifiedName) {
    return getDefault().getTypeCache().existsType(fullyQualifiedName);
  }

  public static IType getTypeBySignature(String signature) {
    signature = Signature.getTypeErasure(signature);
    int arrayCount = Signature.getArrayCount(signature);
    if (arrayCount > 0) {
      signature = signature.substring(arrayCount);
    }
    String fqn = Signature.toString(signature);
    return getType(fqn);
  }

  public static IType[] resolveTypes(String simpleName) {
    return getDefault().resolveTypesImpl(simpleName);
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   * 
   * @param elements
   * @return
   */
  public static ITypeHierarchy getLocalTypeHierarchy(IJavaElement... elements) {
    IRegion region = JavaCore.newRegion();
    if (elements != null) {
      for (IJavaElement e : elements) {
        region.add(e);
      }
    }
    return getDefault().getHierarchyCache().getLocalHierarchy(region);
  }

  public static ITypeHierarchy getLocalTypeHierarchy(IRegion region) {
    return getDefault().getHierarchyCache().getLocalHierarchy(region);
  }

  public static ITypeHierarchy getSuperTypeHierarchy(IType type) {
    return getDefault().getHierarchyCache().getSuperHierarchy(type);
  }

  /**
   * To get a type hierarchy containing no inner types and tracking changes of all primary types in the hierarchy. <br>
   * <br>
   * <b> Note: </b> the listener reference is a weak reference. If users do not keep the reference the type hierarchy
   * will be removed from cache and released.
   * 
   * @param type
   *          the type to get the primary type hierarchy for must be a primary type
   * @return the cached type hierarchy or null if type does not exist or hierarchy could not be created.
   * @throws IllegalArgumentException
   *           if the given type is not a primary type.
   */
  public static IPrimaryTypeTypeHierarchy getPrimaryTypeHierarchy(IType type) throws IllegalArgumentException {
    return getDefault().getHierarchyCache().getPrimaryTypeHierarchy(type);
  }

  public static void addJavaResourceChangedListener(IJavaResourceChangedListener listener) {
    getDefault().getJavaResourceChangedEmitter().addJavaResourceChangedListener(listener);
  }

  public static void removeJavaResourceChangedListener(IJavaResourceChangedListener listener) {
    getDefault().getJavaResourceChangedEmitter().removeJavaResouceChangedListener(listener);
  }

  /**
   * the listener will be notified when ever an inner type of the given type changes. Note that only direct inner types
   * are considered. Any sub element change of the inner type is ignored.<br>
   * <b> Note: </b> the listener reference is a weak reference. If users do not keep the reference the type hierarchy
   * will be removed from cache and released.
   * 
   * @param type
   *          which inner type changes are track.
   * @param listener
   */
  public static void addInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener) {
    getDefault().getJavaResourceChangedEmitter().addInnerTypeChangedListener(type, listener);
  }

  public static void removeInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener) {
    getDefault().getJavaResourceChangedEmitter().removeInnerTypeChangedListener(type, listener);
  }

  /**
   * the listener will be notified when ever a method of the given type changes. Note that only direct method changes
   * are considered. Any sub element change of the method is ignored.<br>
   * <b> Note: </b> the listener reference is a weak reference. If users do not keep the reference the type hierarchy
   * will be removed from cache and released.
   * 
   * @param type
   *          which method changes are track.
   * @param listener
   */
  public static void addMethodChangedListener(IType type, IJavaResourceChangedListener listener) {
    getDefault().getJavaResourceChangedEmitter().addMethodChangedListener(type, listener);
  }

  public static void removeMethodChangedListener(IType type, IJavaResourceChangedListener listener) {
    getDefault().getJavaResourceChangedEmitter().removeMethodChangedListener(type, listener);
  }

  private IType[] resolveTypesImpl(final String simpleName) {
    final TreeMap<CompositeLong, IType> matchList = new TreeMap<CompositeLong, IType>();
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = simpleName;
    int i = fastPat.lastIndexOf('.');
    if (i >= 0) {
      fastPat = fastPat.substring(i + 1);
    }
    try {
      new SearchEngine().search(
          SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          SearchEngine.createWorkspaceScope(),
          new SearchRequestor() {
            @Override
            public final void acceptSearchMatch(SearchMatch match) throws CoreException {
              if (match instanceof TypeDeclarationMatch) {
                TypeDeclarationMatch typeMatch = (TypeDeclarationMatch) match;
                IType t = (IType) typeMatch.getElement();
                if (t.getFullyQualifiedName('.').indexOf(simpleName) >= 0) {
                  matchList.put(new CompositeLong(t.isBinary() ? 1 : 0, matchList.size()), t);
                }
              }
            }
          },
          null
          );
    }
    catch (CoreException e) {
      ScoutSdk.logError("error during resolving type with name '" + simpleName + "'.", e);
    }
    if (matchList.size() < 1) {
      ScoutSdk.logWarning("found no type matches for '" + simpleName + "'.");
      return null;
    }
    return matchList.values().toArray(new IType[matchList.size()]);
  }

  private int parseLogLevel(String loglevel) {
    int level = IStatus.INFO | IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL;
    if (!StringUtility.isNullOrEmpty(loglevel)) {
      String lowerLoglevel = loglevel.toLowerCase();
      if (lowerLoglevel.equals("warning")) {
        level = IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL;
      }
      else if (lowerLoglevel.equals("error")) {
        level = IStatus.ERROR | IStatus.CANCEL;
      }
      else if (lowerLoglevel.equals("cancel")) {
        level = IStatus.CANCEL;
      }
    }
    return level;
  }

  public void setFormDataAutoUpdate(boolean autoUpdate) {
    m_formDataUpdateSupport.setEnabled(autoUpdate);
  }

  public boolean isFormDataAutoUpdate() {
    return m_formDataUpdateSupport.isEnabled();
  }
}
