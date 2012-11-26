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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.resources.ObservablePreferences;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutElement;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.osgi.service.prefs.Preferences;

public class ScoutProject implements IScoutProject {

  private final HashSet<ScoutBundle> m_scoutBundles;
  private final Object bundleMapLock = new Object();
  private final String m_projectName;
  private INlsProject m_nlsProject;
  private INlsProject m_docsNlsProject;
  private IIconProvider m_iconProvider;
  private final ScoutWorkspace m_scoutWorkspace;
  private boolean m_docsNlsProjectInitialized;
  private ObservablePreferences m_projectPreferences;

  public ScoutProject(String projectName, ScoutWorkspace scoutWorkspace) {
    m_projectName = projectName;
    m_scoutWorkspace = scoutWorkspace;
    m_scoutBundles = new HashSet<ScoutBundle>();
    m_docsNlsProjectInitialized = false;
  }

  @Override
  public ScoutWorkspace getScoutWorkspace() {
    return m_scoutWorkspace;
  }

  public boolean addScoutBundle(ScoutBundle bundle) {
    return m_scoutBundles.add(bundle);
  }

  public boolean removeScoutBundle(ScoutBundle bundle) {
    boolean removed = m_scoutBundles.remove(bundle);
    if (removed) {
      m_nlsProject = null;
      m_iconProvider = null;
    }
    return removed;
  }

  @Override
  public int getType() {
    return PROJECT;
  }

  @Override
  public String getProjectName() {
    return m_projectName;
  }

  @Override
  public ScoutBundle[] getAllScoutBundles() {
    synchronized (bundleMapLock) {
      return m_scoutBundles.toArray(new ScoutBundle[m_scoutBundles.size()]);
    }
  }

  @Override
  public ScoutProject getParentProject() {
    return getScoutWorkspace().getParentProject(this);
  }

  @Override
  public ScoutProject[] getSubProjects() {
    return getScoutWorkspace().getSubProjects(this);
  }

  @Override
  public boolean hasParentOrSubProjects() {
    if (getParentProject() != null) {
      return true;
    }
    return getSubProjects().length > 0;
  }

  @Override
  public IScoutBundle getUiSwingBundle() {
    IScoutBundle[] result = getAllBundles(IScoutElement.BUNDLE_UI_SWING);
    if (result.length > 1) {
      ScoutSdk.logWarning("scout project '" + getProjectName() + "' has more than 1 swing bundle.");
    }
    else if (result.length == 1) {
      return result[0];
    }
    return null;
  }

  @Override
  public IScoutBundle getUiSwtBundle() {
    IScoutBundle[] result = getAllBundles(IScoutElement.BUNDLE_UI_SWT);
    if (result.length > 1) {
      ScoutSdk.logWarning("scout project '" + getProjectName() + "' has more than 1 swt bundle.");
    }
    else if (result.length == 1) {
      return result[0];
    }
    return null;
  }

  @Override
  public IScoutBundle getClientBundle() {
    IScoutBundle[] result = getAllBundles(IScoutElement.BUNDLE_CLIENT);
    if (result.length > 1) {
      ScoutSdk.logWarning("scout project '" + getProjectName() + "' has more than 1 client bundle.");
    }
    else if (result.length == 1) {
      return result[0];
    }
    return null;
  }

  @Override
  public IScoutBundle getSharedBundle() {
    IScoutBundle[] result = getAllBundles(IScoutElement.BUNDLE_SHARED);
    if (result.length > 1) {
      ScoutSdk.logWarning("scout project '" + getProjectName() + "' has more than 1 shared bundle.");
    }
    else if (result.length == 1) {
      return result[0];
    }
    return null;
  }

  @Override
  public IScoutBundle getServerBundle() {
    IScoutBundle[] result = getAllBundles(IScoutElement.BUNDLE_SERVER);
    if (result.length > 1) {
      ScoutSdk.logWarning("scout project '" + getProjectName() + "' has more than 1 server bundle.");
    }
    else if (result.length == 1) {
      return result[0];
    }
    return null;
  }

  @Override
  public IScoutBundle[] getBundles(IScoutBundleFilter filter) {
    ArrayList<IScoutBundle> result = new ArrayList<IScoutBundle>();
    synchronized (bundleMapLock) {
      for (IScoutBundle b : m_scoutBundles) {
        if (filter.accept(b)) {
          result.add(b);
        }
      }
    }
    return result.toArray(new IScoutBundle[result.size()]);
  }

  @Override
  public IScoutBundle[] getAllBundles(int type) {
    List<IScoutBundle> bundles = new ArrayList<IScoutBundle>();
    synchronized (bundleMapLock) {
      for (IScoutBundle b : m_scoutBundles) {
        if (b.getType() == type) {
          bundles.add(b);
        }
      }
    }
    return bundles.toArray(new IScoutBundle[bundles.size()]);
  }

  @Override
  public IJavaSearchScope getSearchScope() {
    ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>();
    synchronized (bundleMapLock) {
      for (IScoutBundle p : m_scoutBundles) {
        elements.add(p.getJavaProject());
      }
    }
    return SearchEngine.createJavaSearchScope(elements.toArray(new IJavaElement[elements.size()]));
  }

  @Override
  public boolean contains(IJavaElement element) {
    synchronized (bundleMapLock) {
      for (IScoutBundle b : m_scoutBundles) {
        if (b.contains(element)) {
          return true;
        }
      }
    }
    for (ScoutProject subProject : getSubProjects()) {
      if (subProject.contains(element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public INlsProject findNlsProject() {
    INlsProject nlsProject = getNlsProject();
    if (nlsProject == null) {
      IScoutProject parentProject = getScoutWorkspace().getParentProject(this);
      if (parentProject != null) {
        nlsProject = parentProject.findNlsProject();
      }
    }
    return nlsProject;
  }

  @Override
  public synchronized INlsProject getNlsProject() {
    if (m_nlsProject == null && getSharedBundle() != null) {
      try {
        m_nlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{TypeUtility.getType(RuntimeClasses.TEXTS), this});
      }
      catch (CoreException e) {
        ScoutSdk.logError("during loading NLS project for: " + getProjectName(), e);
      }
    }
    return m_nlsProject;
  }

  @Override
  public synchronized void clearNlsProjectCache() {
    m_nlsProject = null;
    m_docsNlsProject = null;
    m_docsNlsProjectInitialized = false;
  }

  @Override
  public synchronized INlsProject getDocsNlsProject() {
    // a lot of projects do not have a documentation text provider. then the getNlsProjects will return null -> nls workspace would be triggered each time.
    if (!m_docsNlsProjectInitialized && getSharedBundle() != null) {
      try {
        m_docsNlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{TypeUtility.getType(RuntimeClasses.IDocumentationTextProviderService), this});
        m_docsNlsProjectInitialized = true;
      }
      catch (CoreException e) {
        ScoutSdk.logError("during loading Docs NLS project for: " + getProjectName(), e);
      }
    }
    return m_docsNlsProject;
  }

  @Override
  public IIconProvider findIconProvider() {
    IIconProvider iconProvider = getIconProvider();
    if (iconProvider == null) {
      IScoutProject parentProject = getScoutWorkspace().getParentProject(this);
      if (parentProject != null) {
        iconProvider = parentProject.findIconProvider();
      }
    }
    return iconProvider;
  }

  @Override
  public IIconProvider getIconProvider() {
    if (m_iconProvider == null) {
      m_iconProvider = new ScoutProjectIcons(this);
    }
    return m_iconProvider;
  }

  @Override
  public synchronized ObservablePreferences getPreferences() {
    if (m_projectPreferences == null) {
      IScopeContext prefScope = new ProjectScope(getSharedBundle().getProject());
      Preferences prefs = prefScope.getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
      m_projectPreferences = new ObservablePreferences(prefs);
    }
    return m_projectPreferences;
  }
}
