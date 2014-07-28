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
package org.eclipse.scout.sdk.ui.internal.view.properties.model.links;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>FileOpenLink</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class FileOpenLink extends AbstractLink {

  private final IFile m_file;
  private final String m_editorId;
  private ArrayList<Resource> m_allocatedResources = new ArrayList<Resource>();

  public FileOpenLink(IFile file, int order) {
    this(file, order, null);
  }

  public FileOpenLink(IFile file, String displayName, int order) {
    this(file, displayName, order, null);
  }

  public FileOpenLink(IFile file, int order, String editorId) {
    this(file, file.getName(), order, editorId);
  }

  public FileOpenLink(IFile file, String displayName, int order, String editorId) {
    super(displayName, order);
    m_editorId = editorId;
    m_file = file;
    Image img = ScoutSdkUi.getImage(ScoutSdkUi.File);
    IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) file.getAdapter(IWorkbenchAdapter.class);
    if (wbAdapter != null) {
      ImageDescriptor imageDescriptor = wbAdapter.getImageDescriptor(file);
      if (imageDescriptor != null) {
        img = imageDescriptor.createImage();
        m_allocatedResources.add(img);
      }
    }
    setImage(img);
  }

  @Override
  public void dispose() {
    super.dispose();
    for (Resource r : m_allocatedResources) {
      try {
        r.dispose();
      }
      catch (Exception e) {
        ScoutSdkUi.logWarning("could not dispose resource '" + r + "'.", e);
      }
    }
  }

  @Override
  public void execute() {
    try {
      IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if (StringUtility.isNullOrEmpty(m_editorId)) {
        IDE.openEditor(activePage, getFile(), true);
      }
      else {
        activePage.openEditor(new FileEditorInput(getFile()), m_editorId, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError(e);
    }
  }

  /**
   * @return the file
   */
  public IFile getFile() {
    return m_file;
  }

}
