/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.classid;

import static org.eclipse.scout.sdk.s2e.util.JdtUtils.isOnClasspath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipError;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link ClassIdValidationJob}</h3>
 *
 * @since 4.0.0 2014-05-20
 */
public final class ClassIdValidationJob extends AbstractJob {

  public static final String CLASS_ID_VALIDATION_JOB_FAMILY = "CLASS_ID_VALIDATION_JOB_FAMILY";
  public static final String CLASS_ID_DUPLICATE_MARKER_ID = "org.eclipse.scout.sdk.classid.duplicate";
  public static final String CLASS_ID_ATTR_ANNOTATION = "SCOUT_CLASS_ID_ATTR_ANNOTATION";

  private final Set<IType> m_classIdTypes;

  private ClassIdValidationJob(Set<IType> classIdTypes, boolean showToUser) {
    super(ClassIdValidationJob.class.getName());
    setSystem(!showToUser);
    setUser(showToUser);
    setRule(new P_SchedulingRule());
    setPriority(Job.BUILD);
    m_classIdTypes = classIdTypes;
  }

  private Set<IAnnotation> getAllClassIdAnnotationsInWorkspace(IProgressMonitor monitor) {
    Set<IAnnotation> result = new HashSet<>();
    try {
      SearchEngine e = new SearchEngine();
      IJavaSearchScope workspaceScope = SearchEngine.createWorkspaceScope();
      SearchRequestor requestor = new SearchRequestor() {
        @Override
        public void acceptSearchMatch(SearchMatch match) {
          if (monitor.isCanceled()) {
            throw new OperationCanceledException("ClassId annotation search canceled by monitor.");
          }
          Object owner = match.getElement();
          if (owner instanceof IType) {
            IType ownerType = (IType) owner;
            if (JdtUtils.exists(ownerType)) {
              IJavaElement element = ((ReferenceMatch) match).getLocalElement();
              if (element == null) {
                // e.g. when the annotation is fully qualified. try reading from owner
                element = JdtUtils.getAnnotation(ownerType, IScoutRuntimeTypes.ClassId);
              }
              if (element instanceof IAnnotation && JdtUtils.exists(element)) {
                result.add((IAnnotation) element);
              }
            }
          }
        }
      };

      for (IType classIdType : m_classIdTypes) {
        if (monitor.isCanceled()) {
          return result;
        }
        if (JdtUtils.exists(classIdType)) {
          SearchPattern pattern = SearchPattern.createPattern(classIdType, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
          e.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, workspaceScope, requestor, monitor);
        }
      }
    }
    catch (IllegalStateException ise) {
      SdkLog.debug("@ClassId Validation Job canceled because workspace is closing.", ise);
    }
    catch (OperationCanceledException oce) {
      SdkLog.debug("@ClassId Validation Job canceled because monitor was canceled.", oce);
    }
    catch (ZipError ze) {
      // can happen if the search engine is running and a e.g. maven update changes the underlying runtime zip file
      SdkLog.warning("unable to find @ClassId annotation references in workspace.", ze);
    }
    catch (IllegalArgumentException iae) {
      SdkLog.info("@ClassId validation job canceled.", iae);
    }
    catch (CoreException | RuntimeException ex) {
      SdkLog.error("unable to find @ClassId annotation references in workspace.", ex);
    }
    return result;
  }

  @Override
  public boolean belongsTo(Object family) {
    return CLASS_ID_VALIDATION_JOB_FAMILY.equals(family);
  }

  private Map<String /*classid*/, List<IAnnotation>> getClassIdOccurrences(IProgressMonitor monitor) {
    Set<IAnnotation> allClassIdAnnotationsInWorkspace = getAllClassIdAnnotationsInWorkspace(monitor);
    if (monitor.isCanceled()) {
      return null;
    }

    Map<String, List<IAnnotation>> ids = new HashMap<>();
    for (IAnnotation r : allClassIdAnnotationsInWorkspace) {
      if (monitor.isCanceled()) {
        return null;
      }

      if (JdtUtils.exists(r)) {
        String id = JdtUtils.getAnnotationValueString(r, "value");
        if (!Strings.isEmpty(id)) {
          ids.computeIfAbsent(id, k -> new ArrayList<>()).add(r);
        }
      }
    }
    return ids;
  }

  private static IAnnotation getVisibleDuplicate(IJavaElement current, Iterable<IAnnotation> matchesById) {
    IJavaProject jp = current.getJavaProject();
    for (IAnnotation m : matchesById) {
      if (m != current && isOnClasspath(m, jp)) {
        return m;
      }
    }
    return null;
  }

  private static void createDuplicateMarkers(Map<String, List<IAnnotation>> annotations) throws CoreException {
    if(annotations == null || annotations.isEmpty()) {
      return;
    }
    for (Entry<String, List<IAnnotation>> matches : annotations.entrySet()) {
      List<IAnnotation> matchesById = matches.getValue();
      if (matchesById.size() > 1) {
        for (IAnnotation duplicate : matchesById) {
          IAnnotation other = getVisibleDuplicate(duplicate, matchesById);
          IType parent = (IType) duplicate.getAncestor(IJavaElement.TYPE);
          if (JdtUtils.exists(parent) && JdtUtils.exists(other)) {
            @SuppressWarnings("squid:S2259")
            IType otherParent = (IType) other.getAncestor(IJavaElement.TYPE);
            ISourceRange sourceRange = duplicate.getSourceRange();
            if (JdtUtils.exists(otherParent) && SourceRange.isAvailable(sourceRange)) {
              IMarker marker = duplicate.getResource().createMarker(CLASS_ID_DUPLICATE_MARKER_ID);
              marker.setAttribute(IMarker.MESSAGE, "Duplicate @ClassId. Value '" + matches.getKey() + "' of type '" + parent.getFullyQualifiedName()
                  + "' is the same as of type '" + otherParent.getFullyQualifiedName() + "'.");
              marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
              marker.setAttribute(IMarker.CHAR_START, sourceRange.getOffset());
              marker.setAttribute(IMarker.CHAR_END, sourceRange.getOffset() + sourceRange.getLength());
              marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
              try {
                IDocument doc = new Document(parent.getCompilationUnit().getSource());
                marker.setAttribute(IMarker.LINE_NUMBER, doc.getLineOfOffset(sourceRange.getOffset()) + 1);
              }
              catch (BadLocationException e) {
                throw new SdkException(e);
              }
              marker.setAttribute(CLASS_ID_ATTR_ANNOTATION, duplicate);
            }
          }
        }
      }
    }
  }

  private static void deleteDuplicateMarkers() {
    try {
      ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(CLASS_ID_DUPLICATE_MARKER_ID, true, IResource.DEPTH_INFINITE);
    }
    catch (CoreException e) {
      SdkLog.error("unable to remove old class id duplicate markers", e);
    }
  }

  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException {
    Map<String, List<IAnnotation>> classIdOccurrences = getClassIdOccurrences(monitor);
    if (monitor.isCanceled()) {
      return;
    }

    deleteDuplicateMarkers();
    createDuplicateMarkers(classIdOccurrences);
  }

  public static synchronized void executeAsync(long startDelay, boolean showToUser) {
    Job j = new AbstractJob("schedule classid validation") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        try {
          S2eUtils.waitForJdt();

          // get the class id type outside of the validation job
          // because with the job rule a search cannot be performed -> IllegalArgumentException: Attempted to beginRule
          Set<IType> classIds = JdtUtils.resolveJdtTypes(IScoutRuntimeTypes.ClassId);
          if (classIds.isEmpty()) {
            return;
          }

          // cancel currently running job. we are starting a new one right afterwards
          Job.getJobManager().cancel(CLASS_ID_VALIDATION_JOB_FAMILY);

          // start the new validation
          new ClassIdValidationJob(classIds, showToUser).schedule(TimeUnit.SECONDS.toMillis(1));
        }
        catch (IllegalStateException e) {
          // can happen e.g. when the preference nodes are changed: "java.lang.IllegalStateException: Preference node "org.eclipse.jdt.core" has been removed."
          // in that case we just ignore the event and check back later.
          SdkLog.info("Could not schedule class id validation.", e);
        }
        catch (RuntimeException e) {
          SdkLog.error("Error while preparing to search for duplicate @ClassIds.", e);
        }
      }
    };
    j.setPriority(Job.DECORATE);
    j.setSystem(true);
    j.setUser(false);
    j.schedule(startDelay);
  }

  private static final class P_SchedulingRule implements ISchedulingRule {

    @Override
    public boolean contains(ISchedulingRule rule) {
      return rule instanceof P_SchedulingRule;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      return rule instanceof P_SchedulingRule;
    }
  }
}
