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
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class FileOpenAction extends AbstractLinkAction {
  private IFile m_file;
  private FileExtensionType m_extensionType;

  public FileOpenAction() {
    super("", null);
  }

  public void init(IFile file, String linkText, ImageDescriptor icon, FileExtensionType extensionType) {
    init(file, null, linkText, icon, extensionType);
  }

  public void init(IFile file, String leadingText, String linkText, ImageDescriptor icon, FileExtensionType extensionType) {
    setLeadingText(leadingText);
    setLinkText(linkText);
    setImage(icon);
    m_file = file;
    m_extensionType = extensionType;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    try {
      if (m_extensionType == FileExtensionType.Auto) {
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), getFile(), true);
      }
      else {
        IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(m_extensionType.getExtension());
        if (desc == null) {
          IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), getFile(), true);
        }
        else {
          IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), getFile(), desc.getId(), true);
        }
      }
    }
    catch (PartInitException e) {
      JaxWsSdk.logError("could not open file '" + m_file + "'.", e);
    }
    return null;
  }

  private IFile getFile() {
    return m_file;
  }

  public static enum FileExtensionType {
    Auto, Xml("*.xml"), Txt("*.txt");
    private String m_extension;

    private FileExtensionType() {
    }

    private FileExtensionType(String extension) {
      m_extension = extension;
    }

    public String getExtension() {
      return m_extension;
    }

  }
}
