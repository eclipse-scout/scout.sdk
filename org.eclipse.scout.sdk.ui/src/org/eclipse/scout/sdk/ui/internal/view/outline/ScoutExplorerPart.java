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
package org.eclipse.scout.sdk.ui.internal.view.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.LRUCache;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OptionalWorkspaceBlockingRule;
import org.eclipse.scout.sdk.ui.action.AbstractFilterMenuContributionItem;
import org.eclipse.scout.sdk.ui.action.LinkWithEditorAction;
import org.eclipse.scout.sdk.ui.action.ScoutBundlePresentationActionGroup;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.internal.dialog.workingset.ConfigureScoutWorkingSetsDialog;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport.BundlePresentation;
import org.eclipse.scout.sdk.ui.internal.view.outline.clipboard.ExplorerCopyAndPasteSupport;
import org.eclipse.scout.sdk.ui.internal.view.outline.dnd.ExplorerDndSupport;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.FilterOutlineJob;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.LoadInitialOutlineJob;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.RefreshOutlineSubTreeJob;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.BundleNodeGroupTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectsTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutWorkingSetTablePage;
import org.eclipse.scout.sdk.ui.view.outline.DirtyUpdateManager;
import org.eclipse.scout.sdk.ui.view.outline.IContentProviderListener;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.ViewContentProvider;
import org.eclipse.scout.sdk.ui.view.outline.ViewLabelProvider;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPageFilter;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

public class ScoutExplorerPart extends ViewPart implements IScoutExplorerPart {
  /**
   * <p>
   * Used to track changes to the {@link #isLinkingEnabled}&nbsp;property.
   * </p>
   */
  public static final int IS_LINKING_ENABLED_PROPERTY = 1;
  private static final String LINKING_ENABLED = "OutlineView.LINKING_ENABLED"; //$NON-NLS-1$

  private TreeViewer m_viewer;
  private ViewContentProvider m_viewContentProvider;

  private final DirtyUpdateManager m_dirtyManager;
  private final Set<IContributionItem> m_debugMenus;
  private final P_ReloadNodeJob m_reloadJob;
  private final LRUCache<String/* path */, IPageFilter> m_pageFilterCache;

  private Object m_pageFilterCacheLock = new Object();
  private P_OutlineSelectionProvider m_outlineSelectionProvider;
  private boolean m_linkingEnabled;
  private LinkWithEditorAction m_linkWithEditorAction;

  public ScoutExplorerPart() {
    m_pageFilterCache = new LRUCache<String, IPageFilter>(1000, -1);
    m_reloadJob = new P_ReloadNodeJob();
    m_dirtyManager = new DirtyUpdateManager(this);
    m_debugMenus = new HashSet<IContributionItem>();
  }

  @Override
  public TreeViewer getTreeViewer() {
    return m_viewer;
  }

  @Override
  public ViewContentProvider getViewContentProvider() {
    return m_viewContentProvider;
  }

  /**
   * This is a callback that will allow us
   * to create the viewer and initialize it.
   */
  @Override
  public void createPartControl(final Composite parent) {
    Tree tree = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

    m_viewer = new TreeViewer(tree);
    m_viewer.addFilter(new P_PageFilter());

    m_outlineSelectionProvider = new P_OutlineSelectionProvider();
    getSite().setSelectionProvider(m_outlineSelectionProvider);
    m_viewer.setUseHashlookup(true);
    m_viewContentProvider = new ViewContentProvider();
    m_viewContentProvider.addContentProviderListener(new IContentProviderListener() {
      @Override
      public void handleChildrenLoaded(IPage page) {
        m_viewer.refresh(page);
      }
    });
    m_viewer.setLabelProvider(new ViewLabelProvider(parent, this));
    m_viewer.setContentProvider(m_viewContentProvider);
    m_viewer.setSorter(null);
    m_viewer.setInput(new InvisibleRootNode(this));
    createToolbar();
    hookDragAndDrop(m_viewer);
    hookContextMenu();
    hookSelectionAction();
    hookDoubleClickAction();
    hookKeyActions();
    hookViewSiteMenu();
    // add context help
    PlatformUI.getWorkbench().getHelpSystem().setHelp(tree, ScoutSdkUi.PLUGIN_ID + ".doc.outline");

    IContextService serivce = (IContextService) getSite().getService(IContextService.class);
    serivce.activateContext("org.eclipse.scout.sdk.explorer.context");

    final BooleanHolder alreadyNotified = new BooleanHolder(false);
    IScoutWorkspaceListener l = new IScoutWorkspaceListener() {
      @Override
      public void workspaceChanged(ScoutWorkspaceEvent event) {
        switch (event.getType()) {
          case ScoutWorkspaceEvent.TYPE_WORKSPACE_INITIALIZED: {
            alreadyNotified.setValue(true);
            ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(this);
            new LoadInitialOutlineJob(ScoutExplorerPart.this).schedule();
            break;
          }
        }
      }
    };
    ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(l);

    if (ScoutSdkCore.getScoutWorkspace().isInitialized() && !alreadyNotified.getValue().booleanValue()) {
      // workspace has already been initialized but the listener never fired (it has been initialized before we attached the listener) -> it will not fire again -> remove
      ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(l);
      new LoadInitialOutlineJob(ScoutExplorerPart.this).schedule();
    }
  }

  @Override
  public void expandAndSelectProjectLevel() {
    IPage root = getRootPage();
    if (root == null) {
      return;
    }
    try {
      m_viewContentProvider.setLoadSync(true);
      final Holder<IPage> firstBundleGroup = new Holder<IPage>(IPage.class, null);
      final ArrayList<IPage> expandedPages = new ArrayList<IPage>();
      INodeVisitor visitor = new INodeVisitor() {
        @Override
        public int visit(IPage page) {
          if (page instanceof InvisibleRootNode) {
            return CONTINUE;
          }
          else if (page instanceof ProjectsTablePage) {
            expandedPages.add(page); // top level workspace bundle group
            return CONTINUE;
          }
          else if (page instanceof BundleNodeGroupTablePage) {
            IScoutBundle b = page.getParent().getScoutBundle();
            if (page.getParent() instanceof ProjectsTablePage || (b != null && b.isBinary())) {
              expandedPages.add(page);
            }
            if (!page.getScoutBundle().isBinary() && firstBundleGroup.getValue() == null) {
              firstBundleGroup.setValue(page);
            }
            return CONTINUE;
          }
          else if (page instanceof AbstractBundleNodeTablePage) {
            if (page.getScoutBundle() != null) {
              if ((!page.getScoutBundle().isBinary() && !BundlePresentation.FLAT.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation()) && !BundlePresentation.FLAT_GROUPS.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation()))
                  || (page.getScoutBundle().isBinary() && BundlePresentation.HIERARCHICAL.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation()))) {
                expandedPages.add(page);
              }
              if (page.getScoutBundle().isBinary()) {
                return CONTINUE;
              }
            }
            return CANCEL_SUBTREE;
          }
          else if (page instanceof ScoutWorkingSetTablePage) {
            expandedPages.add(page);
            return CANCEL_SUBTREE;
          }
          else {
            return CANCEL_SUBTREE;
          }
        }
      };
      root.accept(visitor);
      if (expandedPages.size() > 0) {
        m_viewer.setExpandedElements(expandedPages.toArray(new IPage[expandedPages.size()]));
        if (firstBundleGroup.getValue() == null) {
          m_viewer.setSelection(new StructuredSelection(expandedPages.get(0)));
        }
        else {
          m_viewer.setSelection(new StructuredSelection(firstBundleGroup.getValue()));
        }
      }
    }
    finally {
      m_viewContentProvider.setLoadSync(false);
    }
  }

  @Override
  public IPage getRootPage() {
    return (IPage) getTreeViewer().getInput();
  }

  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    setLinkingEnabled(false);
  }

  @Override
  public void saveState(IMemento memento) {
    memento.putInteger(LINKING_ENABLED, (m_linkingEnabled) ? 1 : 0);
    super.saveState(memento);
  }

  @Override
  public void dispose() {
    if (m_linkWithEditorAction != null) {
      m_linkWithEditorAction.dispose();
    }
    super.dispose();
  }

  public void setLinkingEnabled(boolean linkingEnabled) {
    m_linkingEnabled = linkingEnabled;
    firePropertyChange(IS_LINKING_ENABLED_PROPERTY);
  }

  public boolean isLinkingEnabled() {
    return m_linkingEnabled;
  }

  /**
   * This method might be called from any thread.
   * The page is added to the dirty structure pages list.
   * A {@link RefreshOutlineSubTreeJob} is queued after some time to reload the affected nodes
   */
  @Override
  public void markStructureDirty(IPage newPage) {
    m_dirtyManager.notifyStructureDirty(newPage);
  }

  @Override
  public void markFilterChanged(IPage page) {
    new FilterOutlineJob(ScoutExplorerPart.this, page).schedule();
  }

  @Override
  public IPageFilter getPageFilter(IPage page) {
    String key = getPageNodePath(page);
    synchronized (m_pageFilterCacheLock) {
      return m_pageFilterCache.get(key);
    }
  }

  @Override
  public void addPageFilter(IPage page, IPageFilter filter) {
    String key = getPageNodePath(page);
    synchronized (m_pageFilterCacheLock) {
      IPageFilter oldFilter = m_pageFilterCache.get(key);
      if (!CompareUtility.equals(oldFilter, filter)) {
        m_pageFilterCache.put(key, filter);
      }
      m_reloadJob.reloadDelayed(page);
    }
  }

  private String getPageNodePath(IPage page) {
    StringBuilder b = new StringBuilder();
    IPage tmp = page;
    while (tmp != null) {
      b.insert(0, "/" + tmp.getName());
      tmp = tmp.getParent();
    }
    return b.toString();
  }

  @SuppressWarnings("restriction")
  private void createToolbar() {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    m_linkWithEditorAction = new LinkWithEditorAction(this);
    m_linkWithEditorAction.updateLinkingEnabled(isLinkingEnabled());
    mgr.add(m_linkWithEditorAction);
    mgr.add(new org.eclipse.jdt.internal.ui.actions.CollapseAllAction(getTreeViewer()));
  }

  @SuppressWarnings("restriction")
  private void hookViewSiteMenu() {
    IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
    mgr.add(m_linkWithEditorAction);
    mgr.add(new org.eclipse.jdt.internal.ui.actions.CollapseAllAction(getTreeViewer()));
    mgr.add(new Separator());

    mgr.add(new Action(Texts.get("ConfigureScoutWorkingSets") + "...", ScoutSdkUi.getImageDescriptor(SdkIcons.ScoutWorkingSet)) {
      @Override
      public void run() {
        ConfigureScoutWorkingSetsDialog d = new ConfigureScoutWorkingSetsDialog(getSite().getShell());
        d.open();
      }
    });
    mgr.add(new Separator());

    MenuManager bundleTypes = new MenuManager(Texts.get("BundleTypes"));
    for (ScoutBundleUiExtension e : ScoutBundleExtensionPoint.getExtensions()) {
      final String bundleType = e.getBundleType();
      bundleTypes.add(new AbstractFilterMenuContributionItem(e.getBundleName(), !ScoutExplorerSettingsSupport.get().isBundleTypeHidden(bundleType)) {
        @Override
        protected void run(boolean selected) {
          if (selected) {
            ScoutExplorerSettingsSupport.get().removeHiddenBundleType(bundleType);
          }
          else {
            ScoutExplorerSettingsSupport.get().addHiddenBundleType(bundleType);
          }
        }
      });
    }
    mgr.add(bundleTypes);
    mgr.add(new AbstractFilterMenuContributionItem(Texts.get("ShowFragments"), ScoutExplorerSettingsSupport.get().isShowFragments()) {
      @Override
      protected void run(boolean selected) {
        ScoutExplorerSettingsSupport.get().setShowFragments(selected);
      }
    });
    mgr.add(new AbstractFilterMenuContributionItem(Texts.get("ShowExternalBundles"), ScoutExplorerSettingsSupport.get().isShowBinaryBundles()) {
      @Override
      protected void run(boolean selected) {
        ScoutExplorerSettingsSupport.get().setShowBinaryBundles(selected);
      }
    });
    mgr.add(new Separator());

    mgr.add(new ScoutBundlePresentationActionGroup());
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    Menu menu = menuMgr.createContextMenu(m_viewer.getControl());
    m_viewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, m_viewer);
    menuMgr.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {
        ScoutExplorerPart.this.fillContextMenu(manager);
      }
    });
  }

  private void fillContextMenu(IMenuManager manager) {
    if (m_debugMenus != null) {
      for (IContributionItem a : m_debugMenus) {
        manager.remove(a);
      }
      m_debugMenus.clear();
    }

    if (m_viewer.getSelection() instanceof IStructuredSelection) {
      IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
      if (selection.size() == 1) {
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof AbstractPage) {
          ArrayList<Action> debugActions = new ArrayList<Action>();
          AbstractPage page = (AbstractPage) firstElement;
          page.addDebugMenus(debugActions);
          if (debugActions.size() > 0) {
            Separator sep = new Separator();
            m_debugMenus.add(sep);
            manager.add(sep);
            for (Action a : debugActions) {
              ActionContributionItem item = new ActionContributionItem(a);
              manager.add(item);
              m_debugMenus.add(item);
            }
          }
        }
      }
    }
    // Other plug-ins can contribute their actions here
  }

  private void hookSelectionAction() {
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent e) {
        handleNodeSelection((StructuredSelection) e.getSelection());
      }
    });
  }

  private void hookDragAndDrop(TreeViewer viewer) {
    new ExplorerDndSupport(viewer);
  }

  private void hookKeyActions() {
    m_viewer.getControl().addKeyListener(new KeyListener() {
      @Override
      public void keyPressed(KeyEvent e) {
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        ISelection sel = m_viewer.getSelection();
        if (sel instanceof StructuredSelection) {
          List list = ((IStructuredSelection) sel).toList();
          if (list.size() == 1) {
            Object elem = list.get(0);
            if (elem instanceof IPage) {
              final IPage page = (IPage) elem;
              ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  if (e.keyCode == SWT.F5) {
                    // act on F5
                    page.refresh((e.stateMask == SWT.SHIFT));
                  }
                  else if (e.keyCode == 'v' && e.stateMask == SWT.CONTROL) {
                    // act on CONTROL + V
                    ExplorerCopyAndPasteSupport.performPaste(m_viewer, page);
                  }
                  else if (e.keyCode == 'c' && e.stateMask == SWT.CONTROL) {
                    // act on CONTROL + C
                    ExplorerCopyAndPasteSupport.performCopy(m_viewer, page);
                  }
                }
              });
            }
          }
        }
      }
    });
  }

  private void hookDoubleClickAction() {
    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent e) {
        handleNodeAction((StructuredSelection) e.getSelection());

      }
    });
  }

  private void handleNodeSelection(StructuredSelection selection) {
    for (Iterator<?> it = selection.iterator(); it.hasNext();) {
      Object o = it.next();
      if (o instanceof AbstractPage) {
        ((AbstractPage) o).handleSelectionDelegate();
      }
    }
  }

  private void handleNodeAction(IStructuredSelection selection) {
    if (selection.size() == 1) {
      Object firstElement = selection.getFirstElement();
      if (firstElement instanceof AbstractPage) {
        if (!((AbstractPage) firstElement).handleDoubleClickedDelegate()) {
          m_viewer.setExpandedState(firstElement, !m_viewer.getExpandedState(firstElement));
        }
      }
    }
  }

  @Override
  public void setSelection(IStructuredSelection selection) {
    getTreeViewer().setSelection(selection);
  }

  @Override
  public IStructuredSelection getSelection() {
    return (IStructuredSelection) getTreeViewer().getSelection();
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus() {
    m_viewer.getControl().setFocus();
  }

  private class P_OutlineSelectionProvider implements ISelectionProvider, ISelectionChangedListener {
    private final List<ISelectionChangedListener> m_selectionListeners;
    private final OptimisticLock m_selectionLock;

    public P_OutlineSelectionProvider() {
      m_selectionLock = new OptimisticLock();
      m_selectionListeners = Collections.synchronizedList(new ArrayList<ISelectionChangedListener>());
      m_viewer.addSelectionChangedListener(this);
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
      m_selectionListeners.add(listener);
    }

    @Override
    public ISelection getSelection() {
      return m_viewer.getSelection();
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
      m_selectionListeners.remove(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
      if (CompareUtility.notEquals(getTreeViewer().getSelection(), selection)) {
        try {
          if (m_selectionLock.acquire()) {
            m_viewer.setSelection(selection, true);
            setSelectionWithoutLock(selection);
          }
        }
        finally {
          m_selectionLock.release();
        }
      }
    }

    private void setSelectionWithoutLock(ISelection selection) {
      for (ISelectionChangedListener l : m_selectionListeners.toArray(new ISelectionChangedListener[m_selectionListeners.size()])) {
        l.selectionChanged(new SelectionChangedEvent(this, selection));
      }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      if (m_viewer.getData(RefreshOutlineSubTreeJob.SELECTION_PREVENTER) != null) {
        return;
      }
      try {
        if (m_selectionLock.acquire()) {
          setSelectionWithoutLock(event.getSelection());
        }
      }
      finally {
        m_selectionLock.release();
      }
    }
  } // end class P_OutlineSelectionProvider

  private class P_ReloadNodeJob extends Job {
    private IPage m_page;

    /**
     * @param name
     */
    public P_ReloadNodeJob() {
      super("reload node");
      setRule(new OptionalWorkspaceBlockingRule(false));
    }

    public synchronized void reloadDelayed(IPage page) {
      cancel();
      m_page = page;
      schedule(300);
    }

    @Override
    protected synchronized IStatus run(IProgressMonitor monitor) {
      getTreeViewer().getTree().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (m_page != null) {
            getTreeViewer().refresh(m_page);
          }
        }
      });
      return Status.OK_STATUS;
    }
  } // end class P_ReloadNodeJob

  private class P_PageFilter extends ViewerFilter {
    @Override
    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
      if (parent instanceof IPage) {
        IPage parentPage = (IPage) parent;
        IPageFilter filter = null;
        synchronized (m_pageFilterCacheLock) {
          filter = m_pageFilterCache.get(getPageNodePath(parentPage));
        }
        if (filter != null) {
          int size = elements.length;
          ArrayList<Object> out = new ArrayList<Object>(size);
          for (int i = 0; i < size; ++i) {
            if (elements[i] instanceof IPage) {
              IPage element = (IPage) elements[i];
              if (filter.accept(element)) {
                out.add(element);
              }
            }
            else {
              // e.g. "Loading..." node
              out.add(elements[i]);
            }
          }
          return out.toArray(new Object[out.size()]);
        }
      }
      return elements;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      // will  not be accessed
      return false;
    }

  }

  public static final class InvisibleRootNode extends AbstractPage {

    private final ScoutExplorerPart m_explorerPart;

    public InvisibleRootNode(ScoutExplorerPart explorerPart) {
      m_explorerPart = explorerPart;
      loadChildren();
    }

    @Override
    public boolean isInitiallyLoaded() {
      return true;
    }

    @Override
    public String getPageId() {
      return IScoutPageConstants.INVISIBLE_ROOT_NODE;
    }

    @Override
    protected void loadChildrenImpl() {
      new ProjectsTablePage(this);
      new LibrariesTablePage(this);
    }

    @Override
    public ScoutExplorerPart getOutlineView() {
      return m_explorerPart;
    }
  }
}
