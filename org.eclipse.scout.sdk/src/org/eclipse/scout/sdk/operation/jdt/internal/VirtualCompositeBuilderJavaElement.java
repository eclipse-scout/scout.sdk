/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.jdt.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;

/**
 * <h3>{@link VirtualCompositeBuilderJavaElement}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.02.2013
 */
public class VirtualCompositeBuilderJavaElement implements IJavaElement {

  public static final int TYPE_VIRTUAL_COMPOSER_BUILDER = 99;

  @Override
  public Object getAdapter(Class adapter) {
    return null;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public IJavaElement getAncestor(int ancestorType) {
    return null;
  }

  @Override
  public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
    return null;
  }

  @Override
  public IResource getCorrespondingResource() throws JavaModelException {
    return null;
  }

  @Override
  public String getElementName() {
    return null;
  }

  @Override
  public int getElementType() {
    return TYPE_VIRTUAL_COMPOSER_BUILDER;
  }

  @Override
  public String getHandleIdentifier() {
    return null;
  }

  @Override
  public IJavaModel getJavaModel() {
    return null;
  }

  @Override
  public IJavaProject getJavaProject() {
    return null;
  }

  @Override
  public IOpenable getOpenable() {
    return null;
  }

  @Override
  public IJavaElement getParent() {
    return null;
  }

  @Override
  public IPath getPath() {
    return null;
  }

  @Override
  public IJavaElement getPrimaryElement() {
    return null;
  }

  @Override
  public IResource getResource() {
    return null;
  }

  @Override
  public ISchedulingRule getSchedulingRule() {
    return null;
  }

  @Override
  public IResource getUnderlyingResource() throws JavaModelException {
    return null;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public boolean isStructureKnown() throws JavaModelException {
    return false;
  }

}
