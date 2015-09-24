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
package org.eclipse.scout.sdk.s2e.internal.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedHandler;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedOperation;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 *
 */
public class DtoTypeChangedHandler implements ITypeChangedHandler {

  @Override
  public List<ITypeChangedOperation> createOperations(IType jdtType, IJavaEnvironmentProvider envProvider) throws CoreException {
    if (!acceptType(jdtType)) {
      return Collections.emptyList();
    }
    return Collections.<ITypeChangedOperation> singletonList(new DtoTypeUpdateOperation(jdtType, envProvider));
  }

  @Override
  public List<ITypeChangedOperation> createAllOperations(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) throws CoreException {
    ArrayList<ITypeChangedOperation> list = new ArrayList<>();
    for (IType jdtType : findAllCandidates(scope)) {
      list.addAll(createOperations(jdtType, envProvider));
    }
    return list;
  }

  protected Collection<IType> findAllCandidates(IJavaSearchScope scope) throws JavaModelException, CoreException {
    HashSet<IType> collector = new HashSet<>();
    for (org.eclipse.jdt.core.IType candidate : JdtUtils.findAllTypesAnnotatedWith(IRuntimeClasses.Data, scope, new NullProgressMonitor())) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (org.eclipse.jdt.core.IType candidate : JdtUtils.findAllTypesAnnotatedWith(IRuntimeClasses.FormData, scope, new NullProgressMonitor())) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (org.eclipse.jdt.core.IType candidate : JdtUtils.findAllTypesAnnotatedWith(IRuntimeClasses.PageData, scope, new NullProgressMonitor())) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    return collector;
  }

  protected boolean acceptType(IType jdtType) throws CoreException {
    //fast check before doing expensive source parsing
    return JdtUtils.exists(jdtType) &&
        !jdtType.isAnonymous() &&
        !jdtType.isBinary() &&
        jdtType.getDeclaringType() == null &&
        Flags.isPublic(jdtType.getFlags()) &&
        JdtUtils.getFirstAnnotationInSupertypeHierarchy(jdtType, IRuntimeClasses.Data, IRuntimeClasses.FormData, IRuntimeClasses.PageData) != null;
  }

}