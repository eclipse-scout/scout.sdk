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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.FolderSelectionDialog;
import org.eclipse.jdt.ui.IPackagesViewPart;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

@SuppressWarnings("restriction")
public class FolderPresenter extends AbstractPropertyPresenter<IFolder> {

  private Composite m_composite;
  private Text m_textField;
  private Button m_button;
  private boolean m_showBrowseButton;
  private String m_messageFolderDialog;
  private String m_titleFolderDialog;

  public FolderPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    this(parent, toolkit, DEFAULT_LABEL_WIDTH, true);
  }

  public FolderPresenter(Composite parent, PropertyViewFormToolkit toolkit, int labelWidth, boolean initialize) {
    super(parent, toolkit, labelWidth, false);
    setLabel(Texts.get("Folder"));
    setMessageFolderDialog(Texts.get("ChooseFolder"));
    setTitleFolderDialog(Texts.get("ChooseFolder"));
    setUseLinkAsLabel(true);
    setShowBrowseButton(true);
    if (initialize) {
      callInitializer();
    }
  }

  @Override
  protected final Control createContent(Composite parent) {
    m_composite = getToolkit().createComposite(parent, SWT.NONE);
    m_textField = getToolkit().createText(m_composite, "", SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
    m_textField.setEditable(false);
    m_textField.setBackground(JaxWsSdkUtility.getColorLightGray());

    m_button = new Button(m_composite, SWT.PUSH | SWT.FLAT);
    m_button.setText(getConfiguredBrowseButtonLabel());
    m_button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IFolder folder = execBrowseAction();
        if (folder != null) {
          setInputInternal(folder);
          setValueFromUI(folder);
        }
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.marginWidth = 0;
    layout.numColumns = 2;
    layout.marginBottom = 0;
    layout.marginRight = 0;
    layout.marginLeft = 0;
    layout.marginTop = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.makeColumnsEqualWidth = false;
    m_composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    m_textField.setLayoutData(gd);

    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    gd.widthHint = 50;
    gd.horizontalIndent = 5;
    gd.exclude = !isShowBrowseButton();
    m_button.setLayoutData(gd);

    return m_composite;
  }

  @Override
  protected void setInputInternal(IFolder input) {
    if (input != null) {
      m_textField.setText(input.getProjectRelativePath().toString());
      m_textField.setToolTipText(input.getProjectRelativePath().toString());
    }
    else {
      m_textField.setText("");
      m_textField.setToolTipText(null);
    }

    setUseLinkAsLabel(input != null && input.exists());
  }

  @Override
  protected void execLinkAction() throws CoreException {
    IFolder folder = getValue();
    if (folder != null && folder.exists()) {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      IPackagesViewPart viewPart;
      viewPart = (IPackagesViewPart) page.showView(JavaUI.ID_PACKAGES);
      viewPart.selectAndReveal(folder);
    }
    else {
      JaxWsSdk.logInfo("Could not find folder");
    }
  }

  protected IFolder execBrowseAction() {
    ILabelProvider labelProvider = new WorkbenchLabelProvider();
    ITreeContentProvider contentProvider = new WorkbenchContentProvider();
    FolderSelectionDialog dialog = new FolderSelectionDialog(ScoutSdkUi.getShell(), labelProvider, contentProvider);
    dialog.setTitle(getTitleFolderDialog());
    dialog.setMessage(getMessageFolderDialog());
    dialog.addFilter(createFilter());
    dialog.setHelpAvailable(false);
    dialog.setAllowMultiple(false);
    dialog.setInput(m_bundle.getProject());
    dialog.setInitialSelection(getValue());
    dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

    if (dialog.open() == Window.OK) {
      return (IFolder) dialog.getFirstResult();
    }
    return null;
  }

  public boolean isShowBrowseButton() {
    return m_showBrowseButton;
  }

  public void setShowBrowseButton(boolean showBrowseButton) {
    if (m_showBrowseButton == showBrowseButton) {
      return;
    }
    m_showBrowseButton = showBrowseButton;

    m_showBrowseButton = showBrowseButton;
    if (isControlCreated()) {
      ((GridData) m_button.getLayoutData()).exclude = !showBrowseButton;
      m_composite.layout();
    }
  }

  protected ViewerFilter createFilter() {
    return new FolderFilter(m_bundle);
  }

  protected String getConfiguredBrowseButtonLabel() {
    return Texts.get("Browse");
  }

  public String getMessageFolderDialog() {
    return m_messageFolderDialog;
  }

  public void setMessageFolderDialog(String messageFolderDialog) {
    m_messageFolderDialog = messageFolderDialog;
  }

  public String getTitleFolderDialog() {
    return m_titleFolderDialog;
  }

  public void setTitleFolderDialog(String titleFolderDialog) {
    m_titleFolderDialog = titleFolderDialog;
  }

  public static class FolderFilter extends ViewerFilter {

    private IScoutBundle m_bundle;
    private Set<IPath> m_unsupportedFolders;

    public FolderFilter(IScoutBundle bundle) {
      m_unsupportedFolders = new HashSet<IPath>();
      m_bundle = bundle;

      try {
        for (IClasspathEntry classpathEntry : m_bundle.getJavaProject().getRawClasspath()) {
          IPath outputLocation = classpathEntry.getOutputLocation();
          // skip output directories of classpath entry
          if (outputLocation != null) {
            m_unsupportedFolders.add(toProjectRelativePath(outputLocation));
          }

          // skip source folders
          if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
            m_unsupportedFolders.add(toProjectRelativePath(classpathEntry.getPath()));
          }
        }

        // skip default output directory
        IPath defaultOutputLocation = m_bundle.getJavaProject().getOutputLocation();
        if (defaultOutputLocation != null) {
          m_unsupportedFolders.add(toProjectRelativePath(defaultOutputLocation));
        }
      }
      catch (Exception e) {
        JaxWsSdk.logError(e);
      }

    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (!(element instanceof IFolder)) {
        return false;
      }
      IFolder folder = (IFolder) element;
      // exclude hidden folders
      if (folder.getName().startsWith(".")) {
        return false;
      }

      // exclude source folders
      if (m_unsupportedFolders.contains(folder.getProjectRelativePath())) {
        return false;
      }

      return true;
    }

    private IPath toProjectRelativePath(IPath path) {
      IFolder folder = m_bundle.getProject().getFolder(path);
      if (folder != null && folder.exists()) {
        return folder.getProjectRelativePath();
      }

      folder = m_bundle.getProject().getWorkspace().getRoot().getFolder(path);
      if (folder != null && folder.exists()) {
        return folder.getProjectRelativePath();
      }
      return path;
    }
  }
}
