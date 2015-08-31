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
package org.eclipse.scout.sdk.s2e.classid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.job.OperationJob;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link ClassIdValidationJob}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 20.05.2014
 */
public final class ClassIdValidationJob extends AbstractJob {

  public static final String CLASS_ID_VALIDATION_JOB_FAMILY = "CLASS_ID_VALIDATION_JOB_FAMILY";
  public static final String CLASS_ID_DUPLICATE_MARKER_ID = "org.eclipse.scout.sdk.classid.duplicate";
  public static final String CLASS_ID_ATTR_ANNOTATION = "SCOUT_CLASS_ID_ATTR_ANNOTATION";

  private static IElementChangedListener listener;
  private final IType m_classIdType;

  private ClassIdValidationJob(IType classIdType) {
    super(ClassIdValidationJob.class.getName());
    setSystem(true);
    setUser(false);
    setRule(new P_SchedulingRule());
    setPriority(Job.BUILD);
    m_classIdType = classIdType;
  }

  private Set<IAnnotation> getAllClassIdAnnotationsInWorkspace(final IProgressMonitor monitor) {
    final Set<IAnnotation> result = new HashSet<>();
    try {
      SearchEngine e = new SearchEngine();
      e.search(SearchPattern.createPattern(m_classIdType, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          SearchEngine.createWorkspaceScope(), new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              if (monitor.isCanceled()) {
                return;
              }
              Object owner = match.getElement();
              if (owner instanceof IType) {
                IType ownerType = (IType) owner;
                if (JdtUtils.exists(ownerType)) {
                  IJavaElement element = ((TypeReferenceMatch) match).getLocalElement();
                  if (element == null) {
                    // e.g. when the annotation is fully qualified. try reading from owner
                    element = JdtUtils.getAnnotation(ownerType, IRuntimeClasses.ClassId);
                  }
                  if (element instanceof IAnnotation && JdtUtils.exists(element)) {
                    result.add((IAnnotation) element);
                  }
                }
              }
            }
          }, monitor);
    }
    catch (IllegalStateException ise) {
      // nop (workspace closed)
    }
    catch (OperationCanceledException oce) {
      //nop
    }
    catch (CoreException ex) {
      S2ESdkActivator.logError("unable to find @ClassId annotation references in workspace.", ex);
    }
    catch (IllegalArgumentException iae) {
      S2ESdkActivator.logInfo("@ClassId validation job cancelled.", iae);
    }
    return result;
  }

  @Override
  public boolean belongsTo(Object family) {
    return CLASS_ID_VALIDATION_JOB_FAMILY.equals(family);
  }

  public static synchronized void install() {
    if (listener == null) {
      listener = new P_ResourceChangeListener();
      JavaCore.addElementChangedListener(listener);
    }
  }

  public static synchronized void uninstall() {
    if (listener != null) {
      JavaCore.removeElementChangedListener(listener);
      listener = null;
    }
    Job.getJobManager().cancel(CLASS_ID_VALIDATION_JOB_FAMILY);
  }

  private Map<String /*classid*/, List<IAnnotation>> getClassIdOccurrences(IProgressMonitor monitor) throws CoreException {
    Map<String, List<IAnnotation>> ids = new HashMap<>();
    Set<IAnnotation> allClassIdAnnotationsInWorkspace = getAllClassIdAnnotationsInWorkspace(monitor);
    if (monitor.isCanceled()) {
      return null;
    }

    for (IAnnotation r : allClassIdAnnotationsInWorkspace) {
      if (monitor.isCanceled()) {
        return null;
      }

      if (JdtUtils.exists(r)) {
        String id = JdtUtils.getAnnotationValueString(r, "value");
        if (StringUtils.isNotEmpty(id)) {
          List<IAnnotation> files = ids.get(id);
          if (files == null) {
            files = new LinkedList<>();
            ids.put(id, files);
          }
          files.add(r);
        }
      }
    }
    return ids;
  }

  private static Set<IAnnotation> getVisibleClassIds(IAnnotation current, List<IAnnotation> matchesById) {
    Set<IAnnotation> visibleMatches = new HashSet<>(matchesById.size());
    for (IAnnotation m : matchesById) {
      if (m != current && JdtUtils.isOnClasspath(m, current.getJavaProject())) {
        visibleMatches.add(m);
      }
    }
    return visibleMatches;
  }

  private static void createDuplicateMarkers(Map<String, List<IAnnotation>> annotations) throws CoreException {
    for (Entry<String, List<IAnnotation>> matches : annotations.entrySet()) {
      List<IAnnotation> matchesById = matches.getValue();
      if (matchesById.size() > 1) {
        for (IAnnotation duplicate : matchesById) {
          IType parent = (IType) duplicate.getAncestor(IJavaElement.TYPE);
          if (JdtUtils.exists(parent)) {
            // duplicate found: check if they can see each others
            Set<IAnnotation> visibleDuplicates = getVisibleClassIds(duplicate, matchesById);
            if (visibleDuplicates.size() > 0) {
              ISourceRange sourceRange = duplicate.getSourceRange();
              if (sourceRange != null && sourceRange.getOffset() >= 0) {
                IMarker marker = duplicate.getResource().createMarker(CLASS_ID_DUPLICATE_MARKER_ID);
                marker.setAttribute(IMarker.MESSAGE, "Duplicate @ClassId value '" + matches.getKey() + "' in type '" + parent.getFullyQualifiedName() + "'.");
                marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
                marker.setAttribute(IMarker.CHAR_START, sourceRange.getOffset());
                marker.setAttribute(IMarker.CHAR_END, sourceRange.getOffset() + sourceRange.getLength());
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                try {
                  Document doc = new Document(parent.getCompilationUnit().getSource());
                  marker.setAttribute(IMarker.LINE_NUMBER, doc.getLineOfOffset(sourceRange.getOffset()) + 1);
                }
                catch (BadLocationException e) {
                  //nop
                }
                marker.setAttribute(CLASS_ID_ATTR_ANNOTATION, duplicate);
              }
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
      S2ESdkActivator.logError("unable to remove old class id duplicate markers", e);
    }
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      Map<String, List<IAnnotation>> classIdOccurrences = getClassIdOccurrences(monitor);
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }

      deleteDuplicateMarkers();
      createDuplicateMarkers(classIdOccurrences);
      return Status.OK_STATUS;
    }
    catch (Exception e) {
      return new ScoutStatus("Error while updating class id duplicate markers.", e);
    }
  }

  private static final class P_ResourceChangeListener implements IElementChangedListener {
    @Override
    public void elementChanged(ElementChangedEvent event) {
      IJavaElementDelta delta = event.getDelta();
      visitDeltas(delta);
    }

    private boolean visitDeltas(IJavaElementDelta delta) {
      if (delta == null) {
        return false;
      }

      IJavaElement element = delta.getElement();
      if (element == null) {
        return false;
      }

      if (element.getElementType() == IJavaElement.ANNOTATION) {
        IAnnotation annotation = (IAnnotation) element;
        if (JdtUtils.exists(annotation) && annotation.getElementName().endsWith(Signature.getSimpleName(IRuntimeClasses.ClassId))) {
          executeAsync(1000);
        }
        return true; // finished processing
      }

      // step into children
      for (IJavaElementDelta d : delta.getAffectedChildren()) {
        boolean processed = visitDeltas(d);
        if (processed) {
          return true;
        }
      }

      // step into annotations
      for (IJavaElementDelta d : delta.getAnnotationDeltas()) {
        boolean processed = visitDeltas(d);
        if (processed) {
          return true;
        }
      }

      return false;
    }
  }

  public static synchronized void executeAsync(final long startDelay) {
    Job currentJob = Job.getJobManager().currentJob();
    if (currentJob instanceof OperationJob) {
      // do not schedule a check run if the event comes from the scout sdk itself. we assume it does a correct job.
      return;
    }

    Job j = new Job("schedule classid validation") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          // get the class id type outside of the validation job
          // because with the job rule a search cannot be performed -> IllegalArgumentException: Attempted to beginRule
          Set<IType> classIds = JdtUtils.resolveJdtTypes(IRuntimeClasses.ClassId);
          for (IType classId : classIds) {
            if (JdtUtils.exists(classId)) {
              // cancel currently running job. we are starting a new one right afterwards
              Job.getJobManager().cancel(CLASS_ID_VALIDATION_JOB_FAMILY);

              JdtUtils.waitForJdt();

              // start the new validation
              new ClassIdValidationJob(classId).schedule(startDelay);
            }
          }
        }
        catch (IllegalStateException e) {
          // can happen e.g. when the preference nodes are changed: "java.lang.IllegalStateException: Preference node "org.eclipse.jdt.core" has been removed."
          // in that case we just ignore the event and check back later.
          S2ESdkActivator.logInfo("Could not schedule class id validation.", e);
        }
        catch (Exception e) {
          S2ESdkActivator.logError("Error while preparing to search for duplicate @ClassIds.", e);
        }
        return Status.OK_STATUS;
      }
    };
    j.setSystem(true);
    j.setUser(false);
    j.schedule();
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
