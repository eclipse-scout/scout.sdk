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
package org.eclipse.scout.sdk.operation.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.annotation.AnnotationNewOperation;
import org.eclipse.scout.sdk.operation.jdt.icu.ImportsCreateOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link ClassIdNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 05.01.2014
 */
public class ClassIdNewOperation implements IOperation {

  private final IScoutBundle m_bundle;

  public ClassIdNewOperation(IScoutBundle startBundle) {
    m_bundle = startBundle;
  }

  @Override
  public String getOperationName() {
    return "Create missing @ClassId annotations";
  }

  @Override
  public void validate() {
    if (m_bundle == null) {
      throw new IllegalArgumentException("Bundle cannot be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      monitor.beginTask("Search for classes...", 10);
      Set<IScoutBundle> allBundlesToProcess = m_bundle.getChildBundles(ScoutBundleFilters.getWorkspaceBundlesFilter(), true);
      HashSet<String> bundleSymbolicNames = new HashSet<String>(allBundlesToProcess.size());
      for (IScoutBundle b : allBundlesToProcess) {
        bundleSymbolicNames.add(b.getSymbolicName());
      }
      if (monitor.isCanceled()) {
        return;
      }

      IType startType = TypeUtility.getType(IRuntimeClasses.ITypeWithClassId);
      if (TypeUtility.exists(startType)) {
        ITypeHierarchy hierarchy = TypeUtility.getTypeHierarchy(startType);
        monitor.worked(1);
        if (monitor.isCanceled()) {
          return;
        }

        Set<IType> allSubtypes = hierarchy.getAllSubtypes(startType);
        hierarchy = null;
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask(null, allSubtypes.size());
        HashMap<ICompilationUnit, HashSet<IType>> typesWithClassId = new HashMap<ICompilationUnit, HashSet<IType>>(1000);
        int numTypes = 0;
        for (IType t : allSubtypes) {
          if (TypeUtility.exists(t) && !t.isBinary() && t.isClass() && !t.isAnonymous() && !t.isReadOnly() && !Flags.isAbstract(t.getFlags())) {
            IScoutBundle scoutBundle = ScoutTypeUtility.getScoutBundle(t);
            if (scoutBundle != null && bundleSymbolicNames.contains(scoutBundle.getSymbolicName())) {
              IAnnotation annotation = JdtUtility.getAnnotation(t, IRuntimeClasses.ClassId);
              if (annotation == null) {
                ICompilationUnit icu = t.getCompilationUnit();
                HashSet<IType> listByIcu = typesWithClassId.get(icu);
                if (listByIcu == null) {
                  listByIcu = new HashSet<IType>();
                  typesWithClassId.put(icu, listByIcu);
                }
                if (listByIcu.add(t)) {
                  numTypes++;
                }
              }
            }
          }
          if (monitor.isCanceled()) {
            return;
          }
          subMonitor.worked(1);
        }
        allSubtypes = null;

        subMonitor = new SubProgressMonitor(monitor, 8);
        subMonitor.beginTask(null, numTypes);
        monitor.setTaskName("Create new annotations...");
        for (Entry<ICompilationUnit, HashSet<IType>> e : typesWithClassId.entrySet()) {
          createClassIdsForIcu(e.getKey(), e.getValue(), subMonitor, workingCopyManager);
          if (monitor.isCanceled()) {
            return;
          }
        }
      }
      monitor.done();
    }
    catch (OperationCanceledException e) {
      // nop
    }
  }

  public static void createClassIdsForIcu(ICompilationUnit icu, Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    workingCopyManager.register(icu, null);

    IBuffer buffer = icu.getBuffer();
    Document sourceDoc = new Document(buffer.getContents());
    MultiTextEdit multiEdit = new MultiTextEdit();
    IImportValidator validator = new ImportValidator(icu);
    String NL = ResourceUtility.getLineSeparator(icu);
    for (IType t : types) {
      if (!t.isAnonymous() && !t.isBinary()) {
        AnnotationNewOperation op = new AnnotationNewOperation(AnnotationSourceBuilderFactory.createClassIdAnnotation(t), t);
        TextEdit edit = op.createEdit(validator, sourceDoc, NL);
        multiEdit.addChild(edit);
        if (monitor != null) {
          monitor.worked(1);
          if (monitor.isCanceled()) {
            return;
          }
        }
      }
    }

    try {
      multiEdit.apply(sourceDoc);
      buffer.setContents(sourceDoc.get());
      new ImportsCreateOperation(icu, validator).run(new NullProgressMonitor(), workingCopyManager);
    }
    catch (BadLocationException e) {
      ScoutSdk.logWarning("Could not update @ClassId annotations for compilation unit '" + icu.getElementName() + "'.", e);
    }
  }
}
