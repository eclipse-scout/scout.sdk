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
package org.eclipse.scout.sdk.ui.classid;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerationContext;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.annotation.AnnotationNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.ui.extensions.quickassist.ClassIdDocumentationSupport;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IMarkerResolution;

/**
 * <h3>{@link ClassIdDuplicateResolution}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 21.05.2014
 */
public class ClassIdDuplicateResolution implements IMarkerResolution {

  private final IAnnotation m_annotation;

  public ClassIdDuplicateResolution(IAnnotation annotation) {
    m_annotation = annotation;
  }

  @Override
  public String getLabel() {
    return Texts.get("UpdateWithNewClassIdValue");
  }

  @Override
  public void run(final IMarker marker) {
    final IType parent = (IType) m_annotation.getAncestor(IJavaElement.TYPE);
    if (TypeUtility.exists(parent)) {
      ClassIdDocumentationSupport support = new ClassIdDocumentationSupport(parent);
      INlsEntry nlsEntry = support.getNlsEntry();
      boolean migrateDocumentation = false;
      boolean doMigration = true;
      if (nlsEntry != null) {
        // the id has documentation assigned
        // either the doc is correct and belongs to this class -> must be migrated too
        // or the doc belongs to another class with this id -> ignore
        MessageBox msgbox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
        msgbox.setText(Texts.get("KeepAssignedDocumentation"));
        msgbox.setMessage(Texts.get("ThisClassIdHasADocumentationEntryAssigned"));
        int answer = msgbox.open();
        doMigration = answer != SWT.CANCEL;
        migrateDocumentation = answer == SWT.YES;
      }

      List<IOperation> ops = new LinkedList<IOperation>();
      if (doMigration) {
        String newId = ClassIdGenerators.generateNewId(new ClassIdGenerationContext(parent));
        ops.add(createUpdateAnnotationInJavaSourceOperation(parent, newId));
        if (migrateDocumentation) {
          ops.add(createUpdateClassIdInDocumentationOperation(nlsEntry, newId));
        }
      }

      if (!ops.isEmpty()) {
        OperationJob j = new OperationJob(ops);
        j.addJobChangeListener(new JobChangeAdapter() {
          @Override
          public void done(IJobChangeEvent event) {
            try {
              marker.delete();
            }
            catch (CoreException e) {
              //nop
            }
            ClassIdValidationJob.executeAsync(0); // the modification of the annotation does not cause an annotation modify event to be triggered
          }
        });
        j.schedule();
      }
    }
  }

  private IOperation createUpdateClassIdInDocumentationOperation(final INlsEntry nlsEntry, final String newId) {
    return new IOperation() {

      @Override
      public void validate() throws IllegalArgumentException {
      }

      @Override
      public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
        nlsEntry.getProject().updateKey(nlsEntry, newId, monitor);
      }

      @Override
      public String getOperationName() {
        return "Update NLS Key to new ClassId";
      }
    };
  }

  private IOperation createUpdateAnnotationInJavaSourceOperation(IType annotationOwner, String newId) {
    return new AnnotationNewOperation(AnnotationSourceBuilderFactory.createClassIdAnnotation(newId), annotationOwner);
  }
}
