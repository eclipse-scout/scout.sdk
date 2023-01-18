/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.jaxws.AbstractWebServiceNewOperation;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.operation.jaxws.RebuildArtifactsOperation;
import org.eclipse.scout.sdk.s2e.ui.internal.jaxws.WebServiceNewWizard;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>{@link WebServiceEditor}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceEditor extends FormEditor {

  public static final String WEB_SERVICE_EDITOR_ID = "org.eclipse.scout.sdk.s2e.ui.jaxwsEditor";
  public static final String WEB_SERVICE_FILE_EXTENSION = "jaxws";
  private IJavaProject m_project;
  private IFile m_jaxwsFile;

  @Override
  protected void addPages() {
    var input = getEditorInput();
    if (!(input instanceof FileEditorInput)) {
      showError("Invalid input for Web Service Editor: " + input, new IllegalArgumentException());
      return;
    }
    m_jaxwsFile = ((IFileEditorInput) input).getFile();
    if (getJaxwsFile() == null || !getJaxwsFile().exists()) {
      showError("Invalid file input for Web Service Editor: " + getJaxwsFile(), new IllegalArgumentException());
      return;
    }
    m_project = JavaCore.create(getJaxwsFile().getProject());
    if (!JdtUtils.exists(getJavaProject())) {
      showError("Invalid project input for Web Service Editor: " + getJavaProject(), new IllegalArgumentException());
      return;
    }

    if (!S2eTier.wrap(ScoutTier.Server).test(getJavaProject())) {
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
    runInEclipseEnvironment(new EditorOperationWrapper(new RebuildArtifactsOperation(getJavaProject())));
  }

  protected void startNewWebServiceWizard() {
    var wiz = new WebServiceNewWizard();
    IStructuredSelection selection = new StructuredSelection(getJaxwsFile());
    wiz.init(PlatformUI.getWorkbench(), selection);
    wiz.getFinishTask()
        .withUiAction((op, d) -> d.asyncExec(() -> reload(op.getCreatedWsdlFile().getFileName().toString())));

    var wizPage = wiz.getWebServiceNewWizardPage();
    wizPage.setIsCreateNewProject(false);
    wizPage.setExistingJaxWsProject(getJavaProject());

    Window dialog = new WizardDialog(getContainer().getShell(), wiz);
    dialog.open();
  }

  protected boolean collectWebServicesAsync(Widget shell, Collection<WebServiceFormPageInput> webServices, IProgressMonitor monitor) {
    var result = findWebServices(monitor);
    var canceled = monitor.isCanceled();
    if (canceled) {
      shell.getDisplay().asyncExec(() -> close(false));
    }
    else {
      webServices.addAll(result);
    }
    return canceled;
  }

  protected void reload(String idToActivate) {
    removeAllPages();

    // find web services
    Collection<WebServiceFormPageInput> webServices = new TreeSet<>();
    var shell = getSite().getShell();
    var isCanceled = new boolean[1];
    try {
      new ProgressMonitorDialog(shell).run(true, true, monitor -> isCanceled[0] = collectWebServicesAsync(shell, webServices, monitor));
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
    for (var input : webServices) {
      addPageSafe(new WebServiceFormPage(this, input));
    }

    // activate the best page
    if (getContainer().isDisposed()) {
      return;
    }
    if (idToActivate != null) {
      var activatedPage = setActivePage(idToActivate);
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
    var result = pages.get(index);
    if (result instanceof IFormPage) {
      return (IFormPage) result;
    }
    return null;
  }

  protected void removeAllPages() {
    while (getPageCount() > 0) {
      var page = getPage(0);
      removePage(page.getIndex());
      if (!page.isEditor()) {
        page.dispose();
      }
    }
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected Set<WebServiceFormPageInput> findWebServices(IProgressMonitor monitor) {
    var progress = SubMonitor.convert(monitor, "Loading contents", 100);
    if (progress.isCanceled()) {
      return emptySet();
    }

    var wsdlFolder = AbstractWebServiceNewOperation.getWsdlRootFolder(getJavaProject().getProject().getLocation().toFile().toPath());
    progress.worked(5);

    var scope = JdtUtils.createJavaSearchScope(getJavaProject());
    var scoutApi = ApiHelper.requireScoutApiFor(getJavaProject());
    var webServiceEntryPointAnnotatedTypes = JdtUtils.findAllTypesAnnotatedWith(scoutApi.WebServiceEntryPoint().fqn(), scope, progress.newChild(10));
    var webServiceClientAnnotatedTypes = JdtUtils.findAllTypesAnnotatedWith(scoutApi.WebServiceClient().fqn(), scope, progress.newChild(10));
    var webServiceAnnotatedTypes = JdtUtils.findAllTypesAnnotatedWith(scoutApi.WebService().fqn(), scope, progress.newChild(10));
    if (progress.isCanceled()) {
      return emptySet();
    }

    try (var content = Files.walk(wsdlFolder)) {
      Set<WebServiceFormPageInput> services = content
          .filter(JaxWsUtils::isWsdlFile)
          .map(f -> new P_PreloadedWebServiceData(f, getJavaProject(), scoutApi, webServiceEntryPointAnnotatedTypes, webServiceClientAnnotatedTypes, webServiceAnnotatedTypes))
          .collect(TreeSet::new, Set::add, Set::addAll);

      progress.worked(5);
      if (progress.isCanceled() || services.isEmpty()) {
        return emptySet();
      }

      var progressPerItem = 50 / services.size();
      for (var s : services) {
        s.load(progress.newChild(progressPerItem));
        if (progress.isCanceled()) {
          return emptySet();
        }
      }

      services.removeIf(data -> data.getAllPortTypes().isEmpty());

      return services;
    }
    catch (IOException | JavaModelException e) {
      throw new SdkException(e);
    }
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    var allPages = getAllPages();
    Collection<WebServiceFormPage> pagesToSave = new ArrayList<>(allPages.size());
    var isValid = true;
    for (var page : allPages) {
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

    runInEclipseEnvironment(new EditorOperationWrapper((e, p) -> savePages(pagesToSave, e, p)));
  }

  protected void savePages(Collection<WebServiceFormPage> pagesToSave, EclipseEnvironment env, EclipseProgress progress) {
    pagesToSave.stream()
        .map(page -> pageToOperation(env, page))
        .forEach(op -> op.accept(env, progress));

    // explicitly flush all modified java files to disk so that the Maven compiler can see the changes
    currentWorkingCopyManager().checkpoint(progress.monitor());

    new RebuildArtifactsOperation(getJavaProject()).accept(env, progress);
  }

  protected static BiConsumer<IEnvironment, IProgress> pageToOperation(EclipseEnvironment env, WebServiceFormPage page) {
    var op = new WebServiceUpdateOperation();
    page.fillOperation(op, env);
    return op;
  }

  public List<WebServiceFormPage> getAllPages() {
    return pages.stream()
        .filter(o -> o instanceof WebServiceFormPage)
        .map(o -> (WebServiceFormPage) o)
        .collect(toList());
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

  protected class EditorOperationWrapper implements BiConsumer<EclipseEnvironment, EclipseProgress> {

    private final BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> m_wrappedOperation;

    public EditorOperationWrapper(BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> operation) {
      m_wrappedOperation = Ensure.notNull(operation);
    }

    @Override
    public void accept(EclipseEnvironment env, EclipseProgress progress) {
      var allPages = getAllPages();
      var c = getContainer();
      var idToActivate = getActivePageInstance().getId();

      c.getDisplay().syncExec(() -> {
        c.setCursor(c.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        setEnabled(allPages, false);
      });

      try {
        getWrappedOperation().accept(env, progress);
      }
      finally {
        refreshAfterSave(c, idToActivate, allPages);
      }
    }

    protected void refreshAfterSave(Control c, String idToActivate, Iterable<WebServiceFormPage> allPages) {
      if (c.isDisposed()) {
        return;
      }

      c.getDisplay().asyncExec(() -> {
        if (c.isDisposed()) {
          return;
        }
        c.setCursor(null);
        reload(idToActivate);
        editorDirtyStateChanged();
        setEnabled(allPages, true);
      });
    }

    protected void setEnabled(Iterable<WebServiceFormPage> allPages, boolean enabled) {
      getContainer().setEnabled(enabled);
      for (var page : allPages) {
        page.setEnabled(enabled);
      }
    }

    public BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> getWrappedOperation() {
      return m_wrappedOperation;
    }
  }

  /**
   * <h3>{@link P_PreloadedWebServiceData}</h3> Preload the annotated {@link IType}s once for all web services
   */
  @SuppressWarnings("squid:S2160")
  private static final class P_PreloadedWebServiceData extends WebServiceFormPageInput {

    private final Set<IType> m_typesAnnotatedWithWebServiceEntryPoint;
    private final Set<IType> m_typesAnnotatedWithWebServiceClient;
    private final Set<IType> m_typesAnnotatedWithWebService;

    private P_PreloadedWebServiceData(Path wsdl, IJavaProject javaProject, IScoutApi api, Set<IType> typesAnnotatedWithWebServiceEntryPoint, Set<IType> typesAnnotatedWithWebServiceClient,
        Set<IType> typesAnnotatedWithWebService) {
      super(wsdl, javaProject, api);
      m_typesAnnotatedWithWebServiceEntryPoint = typesAnnotatedWithWebServiceEntryPoint;
      m_typesAnnotatedWithWebServiceClient = typesAnnotatedWithWebServiceClient;
      m_typesAnnotatedWithWebService = typesAnnotatedWithWebService;
    }

    @Override
    protected Set<IType> findAllTypesAnnotatedWith(String fqn) {
      var scoutApi = getScoutApi();
      if (scoutApi.WebServiceEntryPoint().fqn().equals(fqn)) {
        return m_typesAnnotatedWithWebServiceEntryPoint;
      }
      if (scoutApi.WebServiceClient().fqn().equals(fqn)) {
        return m_typesAnnotatedWithWebServiceClient;
      }
      if (scoutApi.WebService().fqn().equals(fqn)) {
        return m_typesAnnotatedWithWebService;
      }
      throw new IllegalArgumentException();
    }
  }
}
