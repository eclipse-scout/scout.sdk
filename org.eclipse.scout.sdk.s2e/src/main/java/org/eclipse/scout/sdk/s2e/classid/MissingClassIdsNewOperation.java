/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.classid;

import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.imports.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.imports.ImportCollector;
import org.eclipse.scout.sdk.core.imports.ImportValidator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.operation.AnnotationNewOperation;
import org.eclipse.scout.sdk.s2e.operation.ImportsCreateOperation;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link MissingClassIdsNewOperation}</h3>
 *
 * @since 3.10.0 2014-01-05
 */
public class MissingClassIdsNewOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  private Set<IResource> m_selection;

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress p) {
    SubMonitor progress = SubMonitor.convert(p.monitor(), 10);
    try {
      Collection<IType> candidates = findCandidates(progress.newChild(8));
      if (candidates.isEmpty()) {
        return;
      }
      processCandidates(candidates, env, progress.newChild(2));
    }
    catch (OperationCanceledException e) {
      SdkLog.debug("Creation of missing @ClassId annotations has been cancelled.", e);
    }
  }

  protected ITypeHierarchy createHierarchy(IType iTypeWithClassId, IProgressMonitor monitor) {
    if (useRegion()) {
      return createRegionHierarchy(monitor);
    }
    try {
      return iTypeWithClassId.newTypeHierarchy(monitor);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Decides whether to use a resource based type hierarchy or if the full classid hierarchy should be calculated.
   */
  protected boolean useRegion() {
    Set<IResource> selection = selection();
    if (selection.isEmpty() || selection.size() > 100) {
      return false;
    }
    for (IResource r : selection) {
      if (r.getType() != IResource.FILE) {
        return false;
      }
    }
    return true;
  }

  protected ITypeHierarchy createRegionHierarchy(IProgressMonitor monitor) {
    IRegion region = JavaCore.newRegion();
    for (IResource r : selection()) {
      if (r != null && r.isAccessible()) {
        IJavaElement element = JavaCore.create(r);
        if (JdtUtils.exists(element)) {
          region.add(element);
        }
      }
    }
    try {
      return JavaCore.newTypeHierarchy(region, null, monitor);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  protected Collection<IType> findCandidates(SubMonitor monitor) {
    Collection<IType> result = new HashSet<>();
    Set<IType> startTypes = JdtUtils.resolveJdtTypes(IScoutRuntimeTypes.ITypeWithClassId);
    monitor.setWorkRemaining(startTypes.size());
    monitor.setTaskName("Search for classes...");
    if (startTypes.isEmpty()) {
      return emptyList();
    }

    for (IType startType : startTypes) {
      ITypeHierarchy hierarchy = createHierarchy(startType, monitor.newChild(1));
      if (hierarchy == null || monitor.isCanceled()) {
        return emptyList();
      }

      addAll(result, hierarchy.getAllSubtypes(startType));

      if (monitor.isCanceled()) {
        return emptyList();
      }
    }
    return result;
  }

  protected boolean acceptType(IType candidate) {
    try {
      if (!JdtUtils.exists(candidate) || !candidate.isClass() || candidate.isBinary() || candidate.isAnonymous()) {
        return false;
      }
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to check flags of type '{}'. Skipping.", candidate.getFullyQualifiedName(), e);
      return false;
    }

    IResource resource = candidate.getResource();
    if (resource == null || !resource.isAccessible()) {
      return false;
    }

    if (selection().isEmpty()) {
      return true; // not limited to resources: accept all
    }
    return isInResources(resource);
  }

  protected boolean isInResources(IResource candidate) {
    IPath location = candidate.getLocation();
    for (IResource r : selection()) {
      if (r.getLocation().isPrefixOf(location)) {
        return true;
      }
    }
    return false;
  }

  protected void processCandidates(Collection<IType> candidates, EclipseEnvironment env, SubMonitor monitor) {
    SubMonitor subMonitor = monitor.newChild(2);
    subMonitor.setWorkRemaining(candidates.size());
    subMonitor.setTaskName("Search for missing annotations...");

    int numTypes = 0;
    Map<ICompilationUnit, Set<IType>> typesWithoutClassId = new HashMap<>();
    for (IType t : candidates) {
      if (acceptType(t)) {
        IAnnotation annotation = JdtUtils.getAnnotation(t, IScoutRuntimeTypes.ClassId);
        if (annotation == null) {
          ICompilationUnit icu = t.getCompilationUnit();
          if (typesWithoutClassId.computeIfAbsent(icu, k -> new HashSet<>()).add(t)) {
            numTypes++;
          }
        }
      }
      if (monitor.isCanceled()) {
        return;
      }
      subMonitor.worked(1);
    }

    subMonitor = monitor.newChild(6);
    subMonitor.setWorkRemaining(numTypes);
    subMonitor.setTaskName("Create new annotations...");
    for (Entry<ICompilationUnit, Set<IType>> e : typesWithoutClassId.entrySet()) {
      try {
        createClassIdsForIcu(e.getKey(), e.getValue(), env, subMonitor);
      }
      catch (CoreException e1) {
        SdkLog.warning("Unable to write compilation unit '{}'.", e.getKey().getPath(), e1);
      }
      if (monitor.isCanceled()) {
        return;
      }
    }
  }

  public static void createClassIdsForIcu(ICompilationUnit icu, Iterable<IType> types, EclipseEnvironment env, IProgressMonitor monitor) throws CoreException {
    currentWorkingCopyManager().register(icu, null);

    IJavaEnvironment environment = env.toScoutJavaEnvironment(icu.getJavaProject());
    IImportCollector collector = new CompilationUnitScopedImportCollector(new ImportCollector(environment), JdtUtils.getPackage(icu));

    IBuffer buffer = icu.getBuffer();
    IDocument sourceDoc = new Document(buffer.getContents());
    TextEdit multiEdit = new MultiTextEdit();
    IImportValidator validator = new ImportValidator(collector);
    String nl = icu.findRecommendedLineSeparator();
    for (IType t : types) {
      String newClassId = ClassIds.next(t.getFullyQualifiedName());
      AnnotationNewOperation op = new AnnotationNewOperation(ScoutAnnotationGenerator.createClassId(newClassId), t);
      TextEdit edit = op.createEdit(validator, sourceDoc, nl);
      multiEdit.addChild(edit);

      monitor.worked(1);
      if (monitor.isCanceled()) {
        return;
      }
    }

    try {
      multiEdit.apply(sourceDoc);
      buffer.setContents(sourceDoc.get());

      // create imports
      new ImportsCreateOperation(icu, collector).accept(env, EclipseEnvironment.toScoutProgress(monitor));
    }
    catch (BadLocationException e) {
      SdkLog.warning("Could not update @ClassId annotations for compilation unit '{}'.", icu.getElementName(), e);
    }
  }

  @Override
  public String toString() {
    return "Create missing @ClassId annotations";
  }

  public Set<IResource> selection() {
    if (m_selection == null) {
      return Collections.emptySet();
    }
    return m_selection;
  }

  public MissingClassIdsNewOperation withSelection(Set<IResource> selection) {
    m_selection = selection;
    return this;
  }
}
