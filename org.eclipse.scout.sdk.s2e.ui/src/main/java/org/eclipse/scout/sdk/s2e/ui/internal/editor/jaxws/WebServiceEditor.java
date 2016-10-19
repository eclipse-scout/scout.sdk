/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.jaxws.RebuildArtifactsOperation;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation;
import org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws.WebServiceNewWizard;
import org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws.WebServiceNewWizard.IWebServiceNewWizardFinishedAction;
import org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws.WebServiceNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>{@link WebServiceEditor}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceEditor extends FormEditor {

  public static final String WEB_SERVICE_EDITOR_ID = "org.eclipse.scout.sdk.s2e.ui.jaxwsEditor";
  public static final String WEB_SERVICE_FILE_EXTENSION = "jaxws";
  private IJavaProject m_project;
  private IFile m_jaxwsFile;

  @Override
  protected void addPages() {
    IEditorInput input = getEditorInput();
    if (!(input instanceof FileEditorInput)) {
      showError("Invalid input for Web Service Editor: " + input, new IllegalArgumentException());
      return;
    }
    m_jaxwsFile = ((FileEditorInput) input).getFile();
    if (getJaxwsFile() == null || !getJaxwsFile().exists()) {
      showError("Invalid file input for Web Service Editor: " + getJaxwsFile(), new IllegalArgumentException());
      return;
    }
    m_project = JavaCore.create(getJaxwsFile().getProject());
    if (!S2eUtils.exists(getJavaProject())) {
      showError("Invalid project input for Web Service Editor: " + getJavaProject(), new IllegalArgumentException());
      return;
    }

    if (!ScoutTier.Server.test(getJavaProject())) {
      showError("Invalid project type: " + getJavaProject().getElementName() + " is not a server project", new IllegalArgumentException());
      return;
    }

    setPartName(getJavaProject().getProject().getName() + " Web Services");

    String idToActivate = null;
    if (input instanceof WebServiceEditorInput) {
      idToActivate = ((WebServiceEditorInput) input).getPageIdToActivate();
    }
    reload(idToActivate);
  }

  protected void addPageSafe(IFormPage page) {
    if (getContainer().isDisposed()) {
      return;
    }

    try {
      addPage(page);
    }
    catch (PartInitException e) {
      SdkLog.error("Unable to add page to editor.", e);
    }
  }

  protected void showError(String msg, Throwable t) {
    addPageSafe(new ErrorFormPage(this, msg, t));
    SdkLog.error(msg, t);
  }

  protected void rebuildAllArtifacts() {
    RebuildArtifactsOperation op = new RebuildArtifactsOperation();
    op.setJavaProject(getJavaProject());
    runOperations(Collections.singletonList(op));
  }

  protected void startNewWebServiceWizard() {
    WebServiceNewWizard wiz = new WebServiceNewWizard();
    IStructuredSelection selection = new StructuredSelection(getJaxwsFile());
    wiz.init(PlatformUI.getWorkbench(), selection);
    WebServiceNewWizardPage wizPage = wiz.getWebServiceNewWizardPage();
    wizPage.setIsCreateNewProject(false);
    wizPage.setExistingJaxWsProject(getJavaProject());
    wiz.setFinishAction(new IWebServiceNewWizardFinishedAction() {
      @Override
      public void operationFinished(WebServiceNewOperation operation, Display d) {
        final String pageId = operation.getCreatedWsdlFile().getName();
        d.asyncExec(new Runnable() {
          @Override
          public void run() {
            reload(pageId);
          }
        });
      }
    });

    WizardDialog dialog = new WizardDialog(getContainer().getShell(), wiz);
    dialog.open();
  }

  protected void reload(String idToActivate) {
    removeAllPages();

    // find web services
    final Set<WebServiceFormPageInput> webServices = new TreeSet<>();
    final Shell shell = getSite().getShell();
    final boolean[] isCanceled = new boolean[1];
    try {
      new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {
        @Override
        @SuppressWarnings("squid:S1141")
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          try {
            Set<WebServiceFormPageInput> result = findWebServices(monitor);
            isCanceled[0] = monitor.isCanceled();
            if (isCanceled[0]) {
              shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  WebServiceEditor.this.close(false);
                }
              });
            }
            else {
              webServices.addAll(result);
            }
          }
          catch (CoreException e) {
            throw new InvocationTargetException(e);
          }
        }
      });
    }
    catch (InvocationTargetException | InterruptedException e) {
      showError("Unable to search for web services in project: " + getJavaProject().getElementName(), e);
      return;
    }
    if (isCanceled[0]) {
      addPageSafe(new ErrorFormPage(this, null, null));
      return;
    }
    if (webServices.isEmpty()) {
      showError("No web services found in project " + getJavaProject().getElementName(), null);
      return;
    }

    // add pages
    for (WebServiceFormPageInput input : webServices) {
      addPageSafe(new WebServiceFormPage(this, input));
    }

    // activate the best page
    if (getContainer().isDisposed()) {
      return;
    }
    if (idToActivate != null) {
      IFormPage activatedPage = setActivePage(idToActivate);
      if (activatedPage != null) {
        return; // successfully activated requested page
      }
    }
    if (getPageCount() > 0) {
      setActivePage(0);
    }
    else {
      showError("No pages found.", null);
    }
  }

  protected IFormPage getPage(int index) {
    Object result = pages.get(index);
    if (result instanceof IFormPage) {
      return (IFormPage) result;
    }
    return null;
  }

  protected void removeAllPages() {
    while (getPageCount() > 0) {
      IFormPage page = getPage(0);
      removePage(page.getIndex());
      if (!page.isEditor()) {
        page.dispose();
      }
    }
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected Set<WebServiceFormPageInput> findWebServices(IProgressMonitor monitor) throws CoreException {
    final SubMonitor progress = SubMonitor.convert(monitor, "Loading contents", 100);
    if (progress.isCanceled()) {
      return Collections.emptySet();
    }

    IFolder wsdlFolder = WebServiceNewOperation.getWsdlRootFolder(getJavaProject().getProject());
    if (!wsdlFolder.exists()) {
      return Collections.emptySet();
    }
    progress.worked(5);

    IJavaSearchScope scope = S2eUtils.createJavaSearchScope(new IJavaElement[]{getJavaProject()});
    final Set<IType> webServiceEntryPointAnnotatedTypes = S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.WebServiceEntryPoint, scope, progress.newChild(10));
    final Set<IType> webServiceClientAnnotatedTypes = S2eUtils.findAllTypesAnnotatedWith(IJavaRuntimeTypes.WebServiceClient, scope, progress.newChild(10));
    final Set<IType> webServiceAnnotatedTypes = S2eUtils.findAllTypesAnnotatedWith(IJavaRuntimeTypes.WebService, scope, progress.newChild(10));
    if (progress.isCanceled()) {
      return Collections.emptySet();
    }

    final Set<WebServiceFormPageInput> services = new TreeSet<>();
    wsdlFolder.accept(new IResourceProxyVisitor() {
      @Override
      public boolean visit(IResourceProxy proxy) throws CoreException {
        if (proxy.getType() == IResource.FILE && proxy.getName().toLowerCase().endsWith(JaxWsUtils.WSDL_FILE_EXTENSION)) {
          WebServiceFormPageInput webServiceData = new P_PreloadedWebServiceData((IFile) proxy.requestResource(), getJavaProject(), webServiceEntryPointAnnotatedTypes, webServiceClientAnnotatedTypes, webServiceAnnotatedTypes);
          services.add(webServiceData);
        }
        return true;
      }
    }, IResource.DEPTH_INFINITE);
    progress.worked(5);
    if (progress.isCanceled() || services.isEmpty()) {
      return Collections.emptySet();
    }

    int progressPerItem = 50 / services.size();
    for (WebServiceFormPageInput s : services) {
      s.load(progress.newChild(progressPerItem));
      if (progress.isCanceled()) {
        return Collections.emptySet();
      }
    }

    Iterator<WebServiceFormPageInput> it = services.iterator();
    while (it.hasNext()) {
      WebServiceFormPageInput data = it.next();
      if (data.getAllPortTypes().isEmpty()) {
        it.remove();
      }
    }

    return services;
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    final List<WebServiceFormPage> allPages = getAllPages();
    List<WebServiceFormPage> pagesToSave = new ArrayList<>(allPages.size());
    boolean isValid = true;
    for (WebServiceFormPage page : allPages) {
      if (page.isDirty()) {
        if (page.isValid()) {
          pagesToSave.add(page);
        }
        else {
          setActivePage(page.getIndex());
          isValid = false;
          break;
        }
      }
    }
    if (!isValid || pagesToSave.isEmpty()) {
      return;
    }

    List<IOperation> updateOps = new ArrayList<>(pagesToSave.size() + 1);
    for (WebServiceFormPage page : pagesToSave) {
      WebServiceUpdateOperation op = new WebServiceUpdateOperation();
      page.fillOperation(op);
      updateOps.add(op);
    }

    RebuildArtifactsOperation op = new RebuildArtifactsOperation();
    op.setJavaProject(getJavaProject());
    updateOps.add(op);

    runOperations(updateOps);
  }

  protected void runOperations(Iterable<? extends IOperation> ops) {
    final List<WebServiceFormPage> allPages = getAllPages();
    setEnabled(allPages, false);
    final String idToActivate = getActivePageInstance().getId();
    final Composite c = getContainer();
    final Display d = c.getDisplay();
    c.setCursor(d.getSystemCursor(SWT.CURSOR_WAIT));
    ResourceBlockingOperationJob j = new ResourceBlockingOperationJob(ops, getJavaProject().getProject());
    j.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        if (c.isDisposed()) {
          return;
        }

        c.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (c.isDisposed()) {
              return;
            }
            c.setCursor(null);
            reload(idToActivate);
            editorDirtyStateChanged();
            setEnabled(allPages, true);
          }
        });
      }
    });
    j.schedule();
  }

  protected void setEnabled(List<WebServiceFormPage> allPages, boolean enabled) {
    getContainer().setEnabled(enabled);
    for (WebServiceFormPage page : allPages) {
      page.setEnabled(enabled);
    }
  }

  public List<WebServiceFormPage> getAllPages() {
    List<WebServiceFormPage> result = new ArrayList<>(pages.size());
    for (Object o : pages) {
      if (o instanceof WebServiceFormPage) {
        result.add((WebServiceFormPage) o);
      }
    }
    return result;
  }

  @Override
  public void doSaveAs() {
    // nop
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  public IJavaProject getJavaProject() {
    return m_project;
  }

  public IFile getJaxwsFile() {
    return m_jaxwsFile;
  }

  /**
   * <h3>{@link P_PreloadedWebServiceData}</h3> Preload the annotated {@link IType}s once for all web services
   */
  @SuppressWarnings("squid:S2160")
  private static final class P_PreloadedWebServiceData extends WebServiceFormPageInput {

    private final Set<IType> m_typesAnnotatedWithWebServiceEntryPoint;
    private final Set<IType> m_typesAnnotatedWithWebServiceClient;
    private final Set<IType> m_typesAnnotatedWithWebService;

    public P_PreloadedWebServiceData(IFile wsdl, IJavaProject javaProject, Set<IType> typesAnnotatedWithWebServiceEntryPoint, Set<IType> typesAnnotatedWithWebServiceClient, Set<IType> typesAnnotatedWithWebService) {
      super(wsdl, javaProject);
      m_typesAnnotatedWithWebServiceEntryPoint = typesAnnotatedWithWebServiceEntryPoint;
      m_typesAnnotatedWithWebServiceClient = typesAnnotatedWithWebServiceClient;
      m_typesAnnotatedWithWebService = typesAnnotatedWithWebService;
    }

    @Override
    protected Set<IType> findAllTypesAnnotatedWith(String fqn) throws CoreException {
      switch (fqn) {
        case IScoutRuntimeTypes.WebServiceEntryPoint:
          return m_typesAnnotatedWithWebServiceEntryPoint;
        case IJavaRuntimeTypes.WebServiceClient:
          return m_typesAnnotatedWithWebServiceClient;
        case IJavaRuntimeTypes.WebService:
          return m_typesAnnotatedWithWebService;
        default:
          throw new IllegalArgumentException();
      }
    }
  }
}
