/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.util.listener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public abstract class AbstractTypeChangedListener implements IResourceChangeListener {

  private IType m_type;

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    if (!shouldAnalayseForChange(event)) {
      return;
    }

    // there is only interest in POST_CHANGE events
    if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
      return;
    }

    if (m_type == null) {
      return;
    }

    // if type does not exist anymore, it was removed (That is because delta kind 'removed' does not work)
    if (!m_type.exists()) {
      typeChanged();
      return;
    }

    try {
      IResourceDelta rootDelta = event.getDelta();
      rootDelta.accept(new IResourceDeltaVisitor() {
        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
          if (m_type == null) {
            return false;
          }

          if (delta.getKind() == IResourceDelta.ADDED || (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0)) { // it is crucial to exclude marker update events
            IResource resource = delta.getResource();

            if (resource.getType() == IResource.FILE &&
                resource.getFileExtension() != null &&
                "java".equalsIgnoreCase(resource.getFileExtension()) &&
                resource.getName().endsWith(m_type.getElementName() + ".java") &&
                resource.getProject() == m_type.getJavaProject().getProject()) {

              ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);
              if (cu == null) {
                JaxWsSdk.logError("Compilation unit of file '" + resource.getName() + "' could not be determined.");
                return true;
              }

              for (IType typeChanged : cu.getTypes()) {
                if (TypeUtility.exists(typeChanged)) {
                  typeChanged = TypeUtility.getToplevelType(typeChanged);
                }
                if (TypeUtility.exists(typeChanged) && (CompareUtility.equals(typeChanged, m_type))) {
                  typeChanged();
                  return false;
                }
              }
            }
          }
          return true;
        }
      });
    }
    catch (Exception e) {
      JaxWsSdk.logError("Unexpected error occured while intercepting resource changed event.", e);
    }
  }

  protected boolean shouldAnalayseForChange(IResourceChangeEvent event) {
    return true;
  }

  protected abstract void typeChanged();

  public IType getType() {
    return m_type;
  }

  public void setType(IType type) {
    m_type = type;
  }
}
