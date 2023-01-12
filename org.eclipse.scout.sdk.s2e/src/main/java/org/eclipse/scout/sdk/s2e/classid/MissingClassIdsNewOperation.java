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

import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.imports.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.imports.ImportCollector;
import org.eclipse.scout.sdk.core.imports.ImportValidator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutInterfaceApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.operation.AnnotationNewOperation;
import org.eclipse.scout.sdk.s2e.operation.ImportsCreateOperation;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
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
    var progress = SubMonitor.convert(p.monitor(), 10);
    try {
      var candidates = findCandidates(progress.newChild(8));
      if (candidates.isEmpty()) {
        return;
      }
      processCandidates(candidates, env, progress.newChild(2));
    }
    catch (OperationCanceledException e) {
      SdkLog.debug("Creation of missing @ClassId annotations has been canceled.", e);
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
    var selection = selection();
    if (selection.isEmpty() || selection.size() > 100) {
      return false;
    }
    return selection.stream().noneMatch(r -> r.getType() != IResource.FILE);
  }

  protected ITypeHierarchy createRegionHierarchy(IProgressMonitor monitor) {
    var region = JavaCore.newRegion();
    for (var r : selection()) {
      if (r != null && r.isAccessible()) {
        var element = JavaCore.create(r);
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
    var startTypes = ScoutApi.allKnown()
        .map(IScoutInterfaceApi::ITypeWithClassId)
        .map(ITypeNameSupplier::fqn)
        .distinct()
        .map(JdtUtils::resolveJdtTypes)
        .flatMap(Collection::stream)
        .collect(toSet());
    monitor.setWorkRemaining(startTypes.size());
    monitor.setTaskName("Search for classes...");
    if (startTypes.isEmpty()) {
      return emptyList();
    }

    Collection<IType> result = new HashSet<>();
    for (var startType : startTypes) {
      var hierarchy = createHierarchy(startType, monitor.newChild(1));
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

    var resource = candidate.getResource();
    if (resource == null || !resource.isAccessible()) {
      return false;
    }

    if (selection().isEmpty()) {
      return true; // not limited to resources: accept all
    }
    return isInResources(resource);
  }

  protected boolean isInResources(IResource candidate) {
    var location = candidate.getLocation();
    return selection().stream().anyMatch(r -> r.getLocation().isPrefixOf(location));
  }

  protected void processCandidates(Collection<IType> candidates, EclipseEnvironment env, SubMonitor monitor) {
    var subMonitor = monitor.newChild(2);
    subMonitor.setWorkRemaining(candidates.size());
    subMonitor.setTaskName("Search for missing annotations...");

    var numTypes = 0;
    Map<ICompilationUnit, Set<IType>> typesWithoutClassId = new HashMap<>();
    for (var t : candidates) {
      if (acceptType(t)) {
        var classIdFqn = ApiHelper.requireScoutApiFor(t, env).ClassId().fqn();
        var annotation = JdtUtils.getAnnotation(t, classIdFqn);
        if (annotation == null) {
          var icu = t.getCompilationUnit();
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
    for (var e : typesWithoutClassId.entrySet()) {
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

    var environment = env.toScoutJavaEnvironment(icu.getJavaProject());
    IImportCollector collector = new CompilationUnitScopedImportCollector(new ImportCollector(new JavaBuilderContext(environment)), JdtUtils.getPackage(icu));

    var buffer = icu.getBuffer();
    IDocument sourceDoc = new Document(buffer.getContents());
    TextEdit multiEdit = new MultiTextEdit();
    IImportValidator validator = new ImportValidator(collector);
    var nl = icu.findRecommendedLineSeparator();
    for (var t : types) {
      var newClassId = ClassIds.next(t.getFullyQualifiedName());
      var op = new AnnotationNewOperation(ScoutAnnotationGenerator.createClassId(newClassId), t);
      var edit = op.createEdit(validator, sourceDoc, nl);
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
      return emptySet();
    }
    return m_selection;
  }

  public MissingClassIdsNewOperation withSelection(Set<IResource> selection) {
    m_selection = selection;
    return this;
  }
}
