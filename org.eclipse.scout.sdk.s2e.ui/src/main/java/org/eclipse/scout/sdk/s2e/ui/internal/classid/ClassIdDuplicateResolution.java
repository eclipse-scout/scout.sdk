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
package org.eclipse.scout.sdk.s2e.ui.internal.classid;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.s2e.job.WorkspaceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.AnnotationNewOperation;
import org.eclipse.scout.sdk.s2e.workspace.IWorkspaceBlockingOperation;
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
    return "Update with new @ClassId value";
  }

  @Override
  public void run(final IMarker marker) {
    final IType parent = (IType) m_annotation.getAncestor(IJavaElement.TYPE);
    if (JdtUtils.exists(parent)) {
      List<IWorkspaceBlockingOperation> ops = new LinkedList<>();
      String newId = ClassIdGenerators.generateNewId(new ClassIdGenerationContext(parent));
      if (StringUtils.isNotBlank(newId)) {
        ops.add(createUpdateAnnotationInJavaSourceOperation(parent, newId));
      }

      if (!ops.isEmpty()) {
        WorkspaceBlockingOperationJob j = new WorkspaceBlockingOperationJob(ops);
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

  private static IWorkspaceBlockingOperation createUpdateAnnotationInJavaSourceOperation(IType annotationOwner, String newId) {
    return new AnnotationNewOperation(ScoutAnnotationSourceBuilderFactory.createClassIdAnnotation(newId), annotationOwner);
  }
}