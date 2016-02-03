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

  @Override
  public String getOperationName() {
    return "Create missing @ClassId annotations";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      Set<IType> startTypes = S2eUtils.resolveJdtTypes(IScoutRuntimeTypes.ITypeWithClassId);
      if (monitor.isCanceled()) {
        return;
      }

      int workByType = 10;
      SubMonitor progress = SubMonitor.convert(monitor, startTypes.size() * workByType);
      for (IType startType : startTypes) {
        if (S2eUtils.exists(startType)) {
          processBaseType(startType, progress.newChild(workByType), workingCopyManager);
        }
        if (monitor.isCanceled()) {
          return;
        }
      }
    }
    catch (OperationCanceledException e) {
      SdkLog.debug("Creation of missing @ClassId annotations has been cancelled.", e);
    }
  }

  protected void processBaseType(IType startType, SubMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    monitor.setTaskName("Search for classes...");
    ITypeHierarchy hierarchy = startType.newTypeHierarchy(null);
    monitor.worked(1);
    if (monitor.isCanceled()) {
      return;
    }

    IType[] allSubtypes = hierarchy.getAllSubtypes(startType);
    hierarchy = null;

    SubMonitor subMonitor = monitor.newChild(3);
    subMonitor.beginTask(null, allSubtypes.length);
    subMonitor.setTaskName("Search for missing annotations...");
    Map<ICompilationUnit, Set<IType>> typesWithClassId = new HashMap<>();

    int numTypes = 0;
    for (IType t : allSubtypes) {
      if (S2eUtils.exists(t) && !t.isBinary() && t.isClass() && !t.isAnonymous() && !t.isReadOnly() && !Flags.isAbstract(t.getFlags())) {
        IAnnotation annotation = S2eUtils.getAnnotation(t, IScoutRuntimeTypes.ClassId);
        if (annotation == null) {
          ICompilationUnit icu = t.getCompilationUnit();
          Set<IType> listByIcu = typesWithClassId.get(icu);
          if (listByIcu == null) {
            listByIcu = new HashSet<>();
            typesWithClassId.put(icu, listByIcu);
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
    allSubtypes = null;

    subMonitor = monitor.newChild(6);
    subMonitor.beginTask(null, numTypes);
    monitor.setTaskName("Create new annotations...");
    CachingJavaEnvironmentProvider envProvider = new CachingJavaEnvironmentProvider();
    for (Entry<ICompilationUnit, Set<IType>> e : typesWithClassId.entrySet()) {
      createClassIdsForIcu(e.getKey(), e.getValue(), subMonitor, workingCopyManager, envProvider);
      if (monitor.isCanceled()) {
        return;
      }
    }
  }

  private static void createClassIdsForIcu(ICompilationUnit icu, Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, IJavaEnvironmentProvider envProvider) throws CoreException {
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
