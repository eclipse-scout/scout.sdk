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
package org.eclipse.scout.nls.sdk.internal.search;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * <h4>NlsKeySearchRequestor</h4>
 */
public abstract class AbstractNlsKeySearchRequestor extends SearchRequestor {

  private final INlsProject m_project;

  protected AbstractNlsKeySearchRequestor(INlsProject project) {
    m_project = project;
  }

  @Override
  public final void acceptSearchMatch(SearchMatch match) throws CoreException {
    if (!(match.getResource() instanceof IFile)) {
      return;
    }
    if (match.getElement() instanceof IImportDeclaration) {
      return;
    }
    IFile f = (IFile) match.getResource();
    InputStream is = null;
    try {
      is = f.getContents();
      is.skip(match.getOffset());

      int in;
      StringBuilder buffer = new StringBuilder(64);
      // record till end of the statement
      while ((in = is.read()) != ';') {
        buffer.append((char) in);
      }
      acceptMatch(buffer.toString(), match);
    }
    catch (Throwable t) {
      NlsCore.logError("could not read file of ICompilationUnit '" + match.getResource().getName() + "'.", t);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (Throwable tt) {
        }
      }
    }
  }

  protected abstract void acceptMatch(String statement, SearchMatch match);

  public INlsProject getProject() {
    return m_project;
  }
}
