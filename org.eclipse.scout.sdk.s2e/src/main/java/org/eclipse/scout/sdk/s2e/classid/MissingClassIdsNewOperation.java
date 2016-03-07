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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importcollector.ImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.operation.AnnotationNewOperation;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.ImportsCreateOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link MissingClassIdsNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 05.01.2014
 */
public class MissingClassIdsNewOperation implements IOperation {

  public MissingClassIdsNewOperation() {
  }

  @Override
  public String getOperationName() {
    return "Create missing @ClassId annotations";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, 10);
    try {
      Collection<IType> candidates = findCandidates(progress.newChild(2));
      if (candidates.isEmpty()) {
        return;
      }
      processCandidates(candidates, progress.newChild(8), workingCopyManager);
    }
    catch (OperationCanceledException e) {
      SdkLog.debug("Creation of missing @ClassId annotations has been cancelled.", e);
    }
  }

  protected ITypeHierarchy createHierarchy(IType iTypeWithClassId, SubMonitor monitor) throws CoreException {
    return iTypeWithClassId.newTypeHierarchy(monitor);
  }

  protected Collection<IType> findCandidates(SubMonitor monitor) throws CoreException {
    Set<IType> result = new HashSet<>();
    Set<IType> startTypes = S2eUtils.resolveJdtTypes(IScoutRuntimeTypes.ITypeWithClassId);
    monitor.setWorkRemaining(startTypes.size());
    monitor.setTaskName("Search for classes...");
    if (startTypes.isEmpty()) {
      return Collections.emptyList();
    }

    for (IType startType : startTypes) {
      ITypeHierarchy hierarchy = createHierarchy(startType, monitor.newChild(1));
      if (hierarchy == null || monitor.isCanceled()) {
        return Collections.emptyList();
      }

      for (IType t : hierarchy.getAllSubtypes(startType)) {
        result.add(t);
      }

      if (monitor.isCanceled()) {
        return Collections.emptyList();
      }
    }
    return result;
  }

  protected void processCandidates(Collection<IType> candidates, SubMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor subMonitor = monitor.newChild(2);
    subMonitor.setWorkRemaining(candidates.size());
    subMonitor.setTaskName("Search for missing annotations...");

    int numTypes = 0;
    Map<ICompilationUnit, Set<IType>> typesWithoutClassId = new HashMap<>();
    for (IType t : candidates) {
      if (S2eUtils.exists(t) && !t.isBinary() && t.isClass() && !t.isAnonymous() && !t.isReadOnly() && !Flags.isAbstract(t.getFlags())) {
        IAnnotation annotation = S2eUtils.getAnnotation(t, IScoutRuntimeTypes.ClassId);
        if (annotation == null) {
          ICompilationUnit icu = t.getCompilationUnit();
          Set<IType> listByIcu = typesWithoutClassId.get(icu);
          if (listByIcu == null) {
            listByIcu = new HashSet<>();
            typesWithoutClassId.put(icu, listByIcu);
          }
          if (listByIcu.add(t)) {
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
    CachingJavaEnvironmentProvider envProvider = new CachingJavaEnvironmentProvider();
    for (Entry<ICompilationUnit, Set<IType>> e : typesWithoutClassId.entrySet()) {
      createClassIdsForIcu(e.getKey(), e.getValue(), subMonitor, workingCopyManager, envProvider);
      if (monitor.isCanceled()) {
        return;
      }
    }
  }

  public static void createClassIdsForIcu(ICompilationUnit icu, Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, IJavaEnvironmentProvider envProvider) throws CoreException {
    workingCopyManager.register(icu, null);

    IJavaEnvironment environment = envProvider.get(icu.getJavaProject());
    IImportCollector collector = new CompilationUnitScopedImportCollector(new ImportCollector(environment), S2eUtils.getPackage(icu));

    IBuffer buffer = icu.getBuffer();
    Document sourceDoc = new Document(buffer.getContents());
    MultiTextEdit multiEdit = new MultiTextEdit();
    IImportValidator validator = new ImportValidator(collector);
    String nl = icu.findRecommendedLineSeparator();
    for (IType t : types) {
      String newClassId = ClassIdGenerators.generateNewId(new ClassIdGenerationContext(t));
      AnnotationNewOperation op = new AnnotationNewOperation(ScoutAnnotationSourceBuilderFactory.createClassId(newClassId), t);
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
      new ImportsCreateOperation(icu, collector).run(new NullProgressMonitor(), workingCopyManager);
    }
    catch (BadLocationException e) {
      SdkLog.warning("Could not update @ClassId annotations for compilation unit '{}'.", icu.getElementName(), e);
    }
  }
}
