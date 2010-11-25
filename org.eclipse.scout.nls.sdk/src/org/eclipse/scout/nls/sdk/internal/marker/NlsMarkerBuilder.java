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
package org.eclipse.scout.nls.sdk.internal.marker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.search.AbstractNlsKeySearchRequestor;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * <h4>NlsMarkerBuilder</h4>
 */
@SuppressWarnings("restriction")
public class NlsMarkerBuilder {
  public static final String NLSKEY_MARKER_ID = NlsCore.PLUGIN_ID + ".undefinedNlsKey";

  private final INlsProject m_project;
  private P_NlsKeySearchJob m_pendingJob;
  private Object m_jobLock = new Object();

  public NlsMarkerBuilder(INlsProject project) {
    m_project = project;
    JavaCore.addElementChangedListener(new IElementChangedListener() {
      public void elementChanged(ElementChangedEvent event) {
        if (event.getType() == ElementChangedEvent.POST_CHANGE) {
          // visitRec(event.getDelta());
        }
      }

      public void visitRec(IJavaElementDelta delta) {
        for (IJavaElementDelta d : delta.getChangedChildren()) {
          visitRec(d);
        }
        if (delta.getElement().getElementType() == IJavaElement.COMPILATION_UNIT) {
          handleElementChanged(delta.getElement());
        }
      }
    });
    // init
    List<IJavaProject> jProjects = new LinkedList<IJavaProject>();
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    try {
      for (IProject p : projects) {
        if (p.isOpen() && p.hasNature(JavaCore.NATURE_ID) && p.hasNature(PDE.PLUGIN_NATURE)) {
          jProjects.add(JavaCore.create(p));
        }
      }
    }
    catch (CoreException e) {
      NlsCore.logError("Could not create java projects for nls search.");
    }
    m_pendingJob = new P_NlsKeySearchJob(new ArrayList<IJavaElement>(jProjects), new P_SearchRequestor(getProject()));
    m_pendingJob.schedule();

  }

  protected void handleElementChanged(IJavaElement element) {
    synchronized (m_jobLock) {
      if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
        List<IJavaElement> scope = null;
        if (m_pendingJob != null && m_pendingJob.getState() != Job.RUNNING) {
          m_pendingJob.setCanceled(true);
          scope = m_pendingJob.getSearchScope();

        }
        else {
          scope = new ArrayList<IJavaElement>(1);
          scope.add(element);
        }

        m_pendingJob = new P_NlsKeySearchJob(scope, new P_SearchRequestor(getProject()));
        m_pendingJob.schedule(500);
      }
    }
  }

  public INlsProject getProject() {
    return m_project;
  }

  private class P_NlsKeySearchJob extends Job {

    private List<IJavaElement> m_searchScope;
    private boolean m_canceled;
    private final AbstractNlsKeySearchRequestor m_requestor;

    public P_NlsKeySearchJob(List<IJavaElement> searchScope, AbstractNlsKeySearchRequestor requestor) {
      super("update nls Markers");
      m_searchScope = searchScope;
      m_requestor = requestor;
      setPriority(Job.BUILD);
    }

    public void setCanceled(boolean canceled) {

      m_canceled = canceled;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      synchronized (m_jobLock) {
        if (m_canceled) {
          return Status.CANCEL_STATUS;
        }

        for (IJavaElement e : getSearchScope()) {
          try {
            e.getResource().deleteMarkers(NLSKEY_MARKER_ID, false, IResource.DEPTH_ZERO);
          }
          catch (CoreException e1) {
            NlsCore.logError("could not delete nls markers.", e1);
          }
        }
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(getSearchScope().toArray(new IJavaElement[getSearchScope().size()]));
        SearchPattern pattern = SearchPattern.createPattern(getProject().getNlsType().getType(), IJavaSearchConstants.REFERENCES);
        try {
          SearchEngine engine = new SearchEngine();
          engine.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, m_requestor, monitor);
        }
        catch (CoreException e) {
          NlsCore.logError("NLS search failed.", e);
        }
        m_pendingJob = null;
      }
      return Status.OK_STATUS;
    }

    public List<IJavaElement> getSearchScope() {
      return m_searchScope;
    }

  }

  private class P_SearchRequestor extends AbstractNlsKeySearchRequestor {

    /**
     * @param project
     */
    public P_SearchRequestor(INlsProject project) {
      super(project);

    }

    @Override
    protected void acceptMatch(String statement, SearchMatch match) {
      IType nlsType = getProject().getNlsType().getType();
      if (match.getElement() instanceof IImportDeclaration || CompareUtility.equals(match.getResource(), nlsType.getResource())) {
        return;
      }
      Pattern p = Pattern.compile("\\A((" + nlsType.getElementName() + ")|(" + nlsType.getFullyQualifiedName() + "))\\s*\\.\\s*get\\s*\\(\\s*\\\"([^\\\"]*)\\\"\\s*[\\)\\,\\s]{1}", Pattern.MULTILINE);
      // Create a matcher with an input string
      Matcher m = p.matcher(statement);
      if (m.find()) {
        String nlsKey = m.group(4);
        if (getProject().getEntry(nlsKey) == null) {
          try {
            IMarker errorMarker = (match.getResource()).createMarker(NLSKEY_MARKER_ID);
            errorMarker.setAttribute(IMarker.MESSAGE, "The NLS key '" + nlsKey + "' is not defined in the NLS project.");
            errorMarker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            errorMarker.setAttribute(IMarker.CHAR_START, match.getOffset());
            errorMarker.setAttribute(IMarker.CHAR_END, match.getOffset() + match.getLength());
            errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not create NLS marker.", e);
          }
        }
      }
      else {
        try {
          IMarker errorMarker = (match.getResource()).createMarker(NLSKEY_MARKER_ID);
          errorMarker.setAttribute(IMarker.MESSAGE, "The nls key should be a plain string. Do not use variables nor method calls to get/callculate a key.");
          errorMarker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
          errorMarker.setAttribute(IMarker.CHAR_START, match.getOffset());
          errorMarker.setAttribute(IMarker.CHAR_END, match.getOffset() + match.getLength());
          errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
        }
        catch (CoreException e) {
          NlsCore.logWarning("Could not create NLS marker.", e);
        }
      }

    }

  }

}
