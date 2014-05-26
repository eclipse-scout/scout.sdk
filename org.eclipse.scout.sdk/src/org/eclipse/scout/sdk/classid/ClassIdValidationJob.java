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
package org.eclipse.scout.sdk.classid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link ClassIdValidationJob}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 20.05.2014
 */
public final class ClassIdValidationJob extends JobEx {

  public static final String CLASS_ID_VALIDATION_JOB_FAMILY = "CLASS_ID_VALIDATION_JOB_FAMILY";
  public static final String CLASS_ID_DUPLICATE_MARKER_ID = "org.eclipse.scout.sdk.classid.duplicate";
  public static final String CLASS_ID_ATTR_ANNOTATION = "SCOUT_CLASS_ID_ATTR_ANNOTATION";

  private static IJavaResourceChangedListener listener;
  private final IType m_classIdType;

  private ClassIdValidationJob(IType classIdType) {
    super(ClassIdValidationJob.class.getName());
    setSystem(true);
    setUser(false);
    setRule(new P_SchedulingRule());
    m_classIdType = classIdType;
  }

  private Set<IAnnotation> getAllClassIdAnnotationsInWorkspace(IProgressMonitor monitor) {
    final HashSet<IAnnotation> result = new HashSet<IAnnotation>();
    try {
      SearchEngine e = new SearchEngine();
      e.search(SearchPattern.createPattern(m_classIdType, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          SearchEngine.createWorkspaceScope(), new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              IJavaElement element = ((TypeReferenceMatch) match).getLocalElement();
              if (element == null) {
                // e.g. when the annotation is fully qualified. try reading from owner
                Object owner = match.getElement();
                if (owner instanceof IType) {
                  IType ownerType = (IType) owner;
                  if (!ownerType.isBinary() && TypeUtility.exists(ownerType)) {
                    element = JdtUtility.getAnnotation(ownerType, IRuntimeClasses.ClassId);
                  }
                }
              }

              if (element instanceof IAnnotation && !element.isReadOnly() && TypeUtility.exists(element)) {
                result.add((IAnnotation) element);
              }
            }
          }, monitor);
    }
    catch (OperationCanceledException oce) {
      //nop
    }
    catch (CoreException ex) {
      ScoutSdk.logError("unable to find @ClassId annotation references in workspace.", ex);
    }
    return result;
  }

  @Override
  public boolean belongsTo(Object family) {
    return CLASS_ID_VALIDATION_JOB_FAMILY.equals(family);
  }

  public synchronized static void install() {
    if (listener == null) {
      listener = new P_ResourceChangeListener();
      ScoutSdkCore.getJavaResourceChangedEmitter().addJavaResourceChangedListener(listener);
    }
  }

  public synchronized static void uninstall() {
    if (listener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeJavaResourceChangedListener(listener);
    }
  }

  private Map<String /*classid*/, List<IAnnotation>> getClassIdOccurrences(IProgressMonitor monitor) throws CoreException {
    Map<String, List<IAnnotation>> ids = new HashMap<String, List<IAnnotation>>();
    Set<IAnnotation> allClassIdAnnotationsInWorkspace = getAllClassIdAnnotationsInWorkspace(monitor);
    if (monitor.isCanceled()) {
      return null;
    }

    for (IAnnotation r : allClassIdAnnotationsInWorkspace) {
      if (monitor.isCanceled()) {
        return null;
      }

      if (TypeUtility.exists(r)) {
        String id = JdtUtility.getAnnotationValueString(r, "value");
        if (!StringUtility.isNullOrEmpty(id)) {
          List<IAnnotation> files = ids.get(id);
          if (files == null) {
            files = new LinkedList<IAnnotation>();
            ids.put(id, files);
          }
          files.add(r);
        }
      }
    }
    return ids;
  }

  private Set<IAnnotation> getVisibleClassIds(IAnnotation current, List<IAnnotation> matchesById) {
    Set<IAnnotation> visibleMatches = new HashSet<IAnnotation>(matchesById.size());
    for (IAnnotation m : matchesById) {
      if (m != current && TypeUtility.isOnClasspath(m, current.getJavaProject())) {
        visibleMatches.add(m);
      }
    }
    return visibleMatches;
  }

  private void createDuplicateMarkers(Map<String, List<IAnnotation>> annotations) throws CoreException {
    for (Entry<String, List<IAnnotation>> matches : annotations.entrySet()) {
      List<IAnnotation> matchesById = matches.getValue();
      if (matchesById.size() > 1) {
        for (IAnnotation duplicate : matchesById) {
          IType parent = (IType) duplicate.getAncestor(IJavaElement.TYPE);
          if (TypeUtility.exists(parent)) {
            // duplicate found: check if they can see each others
            Set<IAnnotation> visibleDuplicates = getVisibleClassIds(duplicate, matchesById);
            if (visibleDuplicates.size() > 0) {
              ISourceRange sourceRange = duplicate.getSourceRange();
              if (sourceRange != null && sourceRange.getOffset() >= 0) {
                IMarker marker = duplicate.getResource().createMarker(CLASS_ID_DUPLICATE_MARKER_ID);
                marker.setAttribute(IMarker.MESSAGE, Texts.get("DuplicateClassIdValue", matches.getKey(), parent.getFullyQualifiedName()));
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

  private void deleteDuplicateMarkers() {
    try {
      ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(CLASS_ID_DUPLICATE_MARKER_ID, true, IResource.DEPTH_INFINITE);
    }
    catch (CoreException e) {
      ScoutSdk.logError("unable to remove old class id duplicate markers", e);
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
      ScoutSdk.logError("", e);
      return new ScoutStatus("Error while updating class id duplicate markers.", e);
    }
  }

  private static final class P_ResourceChangeListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElementType() == IJavaElement.ANNOTATION) {
        IAnnotation annotation = (IAnnotation) event.getElement();
        if (annotation != null && annotation.getElementName().endsWith(Signature.getSimpleName(IRuntimeClasses.ClassId))) {
          if (TypeUtility.exists(event.getElement())) {
            execute(0);
          }
        }
      }
    }
  }

  public static synchronized void execute(long startDelay) {
    // get the class id type outside of the job
    // because with the job rule a search cannot be performed -> IllegalArgumentException: Attempted to beginRule
    IType classId = TypeUtility.getType(IRuntimeClasses.ClassId);
    if (TypeUtility.exists(classId)) {
      Job.getJobManager().cancel(CLASS_ID_VALIDATION_JOB_FAMILY);
      new ClassIdValidationJob(classId).schedule(startDelay);
    }
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
