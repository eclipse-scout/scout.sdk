/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.classid;

import java.util.function.BiConsumer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.operation.AnnotationNewOperation;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.ui.IMarkerResolution;

/**
 * <h3>{@link ClassIdDuplicateResolution}</h3>
 *
 * @since 4.0.0 2014-05-21
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
  public void run(IMarker marker) {
    var parent = (IType) m_annotation.getAncestor(IJavaElement.TYPE);
    if (JdtUtils.exists(parent)) {
      var newId = ClassIds.next(parent.getFullyQualifiedName());
      if (Strings.hasText(newId)) {
        EclipseEnvironment
            .runInEclipseEnvironment(createUpdateAnnotationInJavaSourceOperation(parent, newId))
            .thenRun(() -> removeMarker(marker));
      }
    }
  }

  protected static void removeMarker(IMarker marker) {
    try {
      marker.delete();
    }
    catch (CoreException e) {
      var resource = marker.getResource();
      if (resource == null) {
        SdkLog.debug("Unable to delete marker", e);
      }
      else {
        SdkLog.debug("Unable to delete marker on '{}'.", resource.getFullPath().toOSString(), e);
      }
    }
  }

  protected static BiConsumer<EclipseEnvironment, EclipseProgress> createUpdateAnnotationInJavaSourceOperation(IType annotationOwner, CharSequence newId) {
    return new AnnotationNewOperation(ScoutAnnotationGenerator.createClassId(newId), annotationOwner);
  }
}
