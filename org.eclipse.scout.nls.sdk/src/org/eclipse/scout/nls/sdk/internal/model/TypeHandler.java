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
package org.eclipse.scout.nls.sdk.internal.model;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;

public class TypeHandler {
  private final static TypeHandler instance = new TypeHandler();

  private TypeHandler() {
  }

  public static IType getType(String fullyQualifiedName) throws JavaModelException {
    return instance.getTypeImpl(fullyQualifiedName);
  }

  private IType getTypeImpl(String fullyQualifiedName) throws JavaModelException {
    final TreeMap<Integer, IType> foundTypes = new TreeMap<Integer, IType>();
    try {
      ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>();
      for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
        elements.add(JavaCore.create(project));
      }

      IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements.toArray(new IJavaElement[elements.size()]), true);
      new SearchEngine().search(
          SearchPattern.createPattern(
              fullyQualifiedName,
              IJavaSearchConstants.TYPE,
              IJavaSearchConstants.DECLARATIONS,
              SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE
              ),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          searchScope,
          new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              if (match instanceof TypeDeclarationMatch) {
                IType type = (IType) match.getElement();
                if (type.isBinary()) {
                  foundTypes.put(2, type);
                }
                else {
                  foundTypes.put(1, type);
                  throw new CoreException(Status.OK_STATUS);
                }
              }
            }
          },
          null
          );
    }
    catch (CoreException e) {
      if (e.getStatus().isOK() && foundTypes.size() > 0) {
        // ignore
      }
      else {
        throw new JavaModelException(e);
      }
    }
    if (foundTypes.size() > 0) {
      return foundTypes.values().iterator().next();
    }
    else {
      return null;
    }
  }

  /**
   * @param name
   * @return
   * @throws JavaModelException
   */
  public static IType[] findAllSubtypes(String name, IProgressMonitor monitor) throws JavaModelException {
    return instance.findAllSubtypesImpl(name, monitor);
  }

  private IType[] findAllSubtypesImpl(String name, IProgressMonitor monitor) throws JavaModelException {
    IType superType = getTypeImpl(name);
    ITypeHierarchy nlsHierarchy = superType.newTypeHierarchy(monitor);
    return nlsHierarchy.getAllSubtypes(superType);
  }

}
