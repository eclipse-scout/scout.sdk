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
package org.eclipse.scout.nls.sdk.simple.ui.wizard;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldModel;
import org.eclipse.scout.nls.sdk.simple.internal.NlsSdkSimple;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

public class TranslationLocationSmartFieldModel implements ISmartFieldModel {

  private final List<Object> m_folders;
  private final IProject m_project;
  private final IPath m_path;

  public TranslationLocationSmartFieldModel(IProject project, IPath path) {
    m_project = project;
    m_path = path;

    m_folders = new LinkedList<>();
    List<INlsFolder> folds = new LinkedList<>();
    try {
      List<IProject> projects = NlsSdkSimple.getProjectGroup(m_project);
      for (IProject toCheck : projects) {
        folds.addAll(NlsSdkSimple.getFoldersOfProject(toCheck, m_path, true));
      }
      for (INlsFolder folder : folds) {
        m_folders.add(folder);
      }
    }
    catch (FileNotFoundException e) {
      NlsCore.logWarning(e);
    }
    catch (CoreException e) {
      NlsCore.logWarning(e);
    }
  }

  @Override
  public Image getImage(Object item) {
    switch (((INlsFolder) item).getType()) {
      case INlsFolder.TYPE_PACKAGE_FOLDER:
        return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
      case INlsFolder.TYPE_SIMPLE_FOLDER:
        return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
      default:
        return null;
    }
  }

  @Override
  public List<Object> getProposals(String pattern) {
    return m_folders;
  }

  @Override
  public String getText(Object item) {
    return ((INlsFolder) item).getFolder().getProject().getName() + "/" + ((INlsFolder) item).getFolder().getProjectRelativePath();
  }
}
