/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.classid;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.s2e.util.JdtUtils.isOnClasspath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
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
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutAnnotationApi.ClassId;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
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
      for (var classIdType : m_classIdTypes) {
        if (monitor.isCanceled()) {
          return result;
        }
        if (JdtUtils.exists(classIdType)) {
          collectAllClassIdAnnotationsInWorkspace(classIdType, result, monitor);
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

  static void collectAllClassIdAnnotationsInWorkspace(IType classIdType, Collection<IAnnotation> collector, IProgressMonitor monitor) throws CoreException {
    var classIdFqn = classIdType.getFullyQualifiedName();
    var requestor = new SearchRequestor() {
      @Override
      public void acceptSearchMatch(SearchMatch match) {
        if (monitor.isCanceled()) {
          throw new OperationCanceledException("ClassId annotation search canceled by monitor.");
        }
        var owner = match.getElement();
        if (!(owner instanceof IType ownerType)) {
          return;
        }
        if (!JdtUtils.exists(ownerType)) {
          return;
        }

        var element = ((ReferenceMatch) match).getLocalElement();
        if (element == null) {
          // e.g. when the annotation is fully qualified. try reading from owner
          element = JdtUtils.getAnnotation(ownerType, classIdFqn);
        }

        if (element instanceof IAnnotation && JdtUtils.exists(element)) {
          collector.add((IAnnotation) element);
        }
      }
    };
    var pattern = SearchPattern.createPattern(classIdType, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
    new SearchEngine().search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, SearchEngine.createWorkspaceScope(), requestor, monitor);
  }

  @Override
  public boolean belongsTo(Object family) {
    return CLASS_ID_VALIDATION_JOB_FAMILY.equals(family);
  }

  private Map<String /*classid*/, List<IAnnotation>> getClassIdOccurrences(IProgressMonitor monitor) {
    var allClassIdAnnotationsInWorkspace = getAllClassIdAnnotationsInWorkspace(monitor);
    if (monitor.isCanceled()) {
      return null;
    }

    Map<String, List<IAnnotation>> ids = new HashMap<>();
    for (var r : allClassIdAnnotationsInWorkspace) {
      if (monitor.isCanceled()) {
        return null;
      }

      if (JdtUtils.exists(r)) {
        var id = JdtUtils.getAnnotationValueString(r, "value");
        if (!Strings.isEmpty(id)) {
          ids.computeIfAbsent(id, k -> new ArrayList<>()).add(r);
        }
      }
    }
    return ids;
  }

  private static IAnnotation getVisibleDuplicate(IJavaElement current, Iterable<IAnnotation> matchesById) {
    var jp = current.getJavaProject();
    for (var m : matchesById) {
      if (m != current && isOnClasspath(m, jp)) {
        return m;
      }
    }
    return null;
  }

  private static void createDuplicateMarkers(Map<String, List<IAnnotation>> annotations) throws CoreException {
    if (annotations == null || annotations.isEmpty()) {
      return;
    }
    for (var matches : annotations.entrySet()) {
      var matchesById = matches.getValue();
      if (matchesById.size() > 1) {
        for (var duplicate : matchesById) {
          var other = getVisibleDuplicate(duplicate, matchesById);
          var parent = (IType) duplicate.getAncestor(IJavaElement.TYPE);
          if (JdtUtils.exists(parent) && JdtUtils.exists(other)) {
            @SuppressWarnings("squid:S2259")
            var otherParent = (IType) other.getAncestor(IJavaElement.TYPE);
            var sourceRange = duplicate.getSourceRange();
            if (JdtUtils.exists(otherParent) && SourceRange.isAvailable(sourceRange)) {
              var marker = duplicate.getResource().createMarker(CLASS_ID_DUPLICATE_MARKER_ID);
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
    var classIdOccurrences = getClassIdOccurrences(monitor);
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
          var classIds = ScoutApi.allKnown()
              .map(IScoutApi::ClassId)
              .map(ClassId::fqn)
              .distinct()
              .flatMap(fqn -> JdtUtils.resolveJdtTypes(fqn).stream())
              .collect(toSet());
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
