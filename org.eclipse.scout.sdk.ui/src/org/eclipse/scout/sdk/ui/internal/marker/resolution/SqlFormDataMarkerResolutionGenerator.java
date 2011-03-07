package org.eclipse.scout.sdk.ui.internal.marker.resolution;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.annotations.SqlBindingIgnoreValidation;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.annotation.IgnoreSqlBindingAnnotationCreateOperation;
import org.eclipse.scout.sdk.sql.binding.SqlBindingMarker;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.jdt.JdtUiUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class SqlFormDataMarkerResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      return new IMarkerResolution[]{new P_AddSqlBindingIgnoreAnnotationResolution((String) marker.getAttribute(SqlBindingMarker.BIND_VARIABLE))};
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not get resolutions", e);
    }
    return new IMarkerResolution[0];
  }

  private class P_AddSqlBindingIgnoreAnnotationResolution implements IMarkerResolution {
    private final String m_binding;

    public P_AddSqlBindingIgnoreAnnotationResolution(String binding) {
      m_binding = binding;

    }

    @Override
    public String getLabel() {
      return "Add Ignore Annotation for '" + m_binding + "'.";
    }

    @Override
    public void run(IMarker marker) {
      String icuName = "";
      String bindVar = "";
      try {
        ICompilationUnit icu = (ICompilationUnit) JavaCore.create((IFile) marker.getResource());
        if (icu.getTypes().length > 0) {
          bindVar = (String) marker.getAttribute(SqlBindingMarker.BIND_VARIABLE);
          icuName = icu.getElementName();
          Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
          IJavaElement element = icu.getElementAt(start);
          if (TypeUtility.exists(element)) {
            if (element.getElementType() != IJavaElement.METHOD) {
              element = element.getAncestor(IJavaElement.METHOD);
            }
          }
          if (TypeUtility.exists(element) && element.getElementType() == IJavaElement.METHOD) {
            IgnoreSqlBindingAnnotationCreateOperation op = new IgnoreSqlBindingAnnotationCreateOperation((IMethod) element, Signature.createTypeSignature(SqlBindingIgnoreValidation.class.getName(), true), bindVar);
            OperationJob job = new OperationJob(op);
            job.schedule();
            JdtUiUtility.showJavaElementInEditor(element, false);
          }
        }
      }
      catch (Exception e) {
        ScoutSdkUi.logError("could not applay annotation resolution for bindvar '" + bindVar + "' in  '" + icuName + "'.");
      }

    }
  }

}
