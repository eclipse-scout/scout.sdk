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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerationContext;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.annotation.AnnotationNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.util.type.TypeUtility;
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
      String newId = ClassIdGenerators.generateNewId(new ClassIdGenerationContext(parent));
      OperationJob j = new OperationJob(createUpdateAnnotationInJavaSourceOperation(parent, newId));
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

  private IOperation createUpdateAnnotationInJavaSourceOperation(IType annotationOwner, String newId) {
    return new AnnotationNewOperation(AnnotationSourceBuilderFactory.createClassIdAnnotation(newId), annotationOwner);
  }
}
