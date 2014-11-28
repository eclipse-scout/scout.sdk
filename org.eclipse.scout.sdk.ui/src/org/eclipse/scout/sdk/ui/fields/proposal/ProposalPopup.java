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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.ISearchRangeConsumer;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ProposalPopup extends Window {

  private static final String DIALOG_SETTINGS_WIDTH = "dialogSettingsWidth";
  private static final String DIALOG_SETTINGS_HEIGHT = "dialogSettingsHeight";
  private static final int POPUP_OFFSET = 2;
  private static final int MINIMUM_HEIGHT = 100;
  private static final int MINIMUM_WIDTH = 200;
  private static final int DEFAULT_WIDTH = 400;
  private static final int DEFAULT_HEIGHT = 300;

  public static final Object[] LOADING_PROPOSAL = new Object[]{new ISeparatorProposal() {
    @Override
    public String getLabel() {
      return Texts.get("Loading");
    }

    @Override
    public Image getImage() {
      return ScoutSdkUi.getImage(ScoutSdkUi.Separator);
    }
  }};

  private final Control m_proposalField;
  private final EventListenerList m_selectionListeners;
  private final OptimisticLock m_uiLock;

  private TableViewer m_tableViewer;
  private Label m_itemCountLabel;
  private ScrolledComposite m_proposalDescriptionArea;

  private Object m_selectedProposal;

  private SearchPatternInput m_input;
  private IProposalDescriptionProvider m_proposalDescriptionProvider;
  private IBaseLabelProvider m_labelProvider;
  private IContentProvider m_contentProvider;

  private P_LazyLoader m_lazyLoaderJob;
  private IDialogSettings m_dialogSettings;
  private Point m_shellSizeDif;

  public ProposalPopup(Control proposalField) {
    super(proposalField.getShell());
    m_uiLock = new OptimisticLock();
    m_selectionListeners = new EventListenerList();
    m_proposalField = proposalField;
    setShellStyle(SWT.RESIZE | SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL);
    setBlockOnOpen(false);
    // listeners
    getOwnerControl().getShell().addListener(SWT.Move, new Listener() {
      @Override
      public void handleEvent(Event event) {
        close();
      }
    });
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    // listener
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellDeactivated(ShellEvent e) {
        getShell().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (getOwnerControl() != null && !getOwnerControl().isDisposed()) {
              if (!getOwnerControl().isFocusControl()) {
                close();
              }
            }
            else {
              close();
            }
          }
        });
      }
    });
  }

  @Override
  protected Control createContents(final Composite parent) {
    Composite proposalArea = new Composite(parent, SWT.INHERIT_FORCE);
    proposalArea.setBackground(proposalArea.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    Table table = new Table(proposalArea, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_tableViewer = new TableViewer(table);
    m_tableViewer.setContentProvider(m_contentProvider);
    m_tableViewer.setLabelProvider(m_labelProvider);
    m_tableViewer.setInput(getInput());
    m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        Object newSelection = null;
        if (!event.getSelection().isEmpty()) {
          newSelection = ((IStructuredSelection) event.getSelection()).getFirstElement();
          if (newSelection instanceof ISeparatorProposal) {
            return;
          }
        }
        handleProposalSelection(newSelection);
      }
    });

    m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          if (selection.size() == 1) {
            if (selection.getFirstElement() instanceof ISeparatorProposal) {
              return;
            }
          }
          ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
          delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
          firePopupEvent(delegateEvent);
        }
      }
    });
    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        switch (e.keyCode) {
          case SWT.CR:
            break;
          case ' ':
            IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
            if (!selection.isEmpty()) {
              ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
              delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
              firePopupEvent(delegateEvent);
            }
            break;

          default:
            break;
        }
        super.keyReleased(e);
      }
    });
    table.setHeaderVisible(false);

    m_proposalDescriptionArea = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    m_proposalDescriptionArea.setBackground(m_proposalDescriptionArea.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    m_proposalDescriptionArea.setExpandHorizontal(true);
    m_proposalDescriptionArea.setExpandVertical(true);

    Group group = new Group(proposalArea, SWT.SHADOW_ETCHED_OUT);
    group.setBackground(group.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    group.setForeground(group.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    m_itemCountLabel = new Label(group, SWT.NONE);
    m_itemCountLabel.setBackground(m_itemCountLabel.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    // layout
    GridLayout parentLayout = new GridLayout(1, true);
    parentLayout.horizontalSpacing = 0;
    parentLayout.marginHeight = 0;
    parentLayout.marginWidth = 0;
    parentLayout.verticalSpacing = 0;
    parent.setLayout(parentLayout);
    proposalArea.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridData proposalDescriptionData = new GridData(GridData.FILL_BOTH);
    proposalDescriptionData.exclude = true;
    m_proposalDescriptionArea.setLayoutData(proposalDescriptionData);

    proposalArea.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(group, -2);
    table.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0, 2);
    data.right = new FormAttachment(100, -2);
    data.bottom = new FormAttachment(100, -2);
    group.setLayoutData(data);

    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    group.setLayout(gridLayout);
    GridData gd = new GridData(GridData.BEGINNING | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    m_itemCountLabel.setLayoutData(gd);

    return parent;
  }

  private void handleProposalSelection(Object proposal) {
    // refresh selected label
    Object old = m_selectedProposal;
    m_selectedProposal = null;
    if (old != null) {
      m_tableViewer.update(old, new String[]{"label"});
    }
    m_selectedProposal = proposal;
    if (m_selectedProposal != null) {
      m_tableViewer.update(m_selectedProposal, new String[]{"label"});
    }
    // update description
    updateDescription(m_selectedProposal);
    try {
      if (m_uiLock.acquire()) {
        // handleSelection
        ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_SELECTED);
        delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, m_selectedProposal);
        firePopupEvent(delegateEvent);
      }
    }
    finally {
      m_uiLock.release();
    }
  }

  public void setDialogSettings(IDialogSettings dialogSettings) {
    m_dialogSettings = dialogSettings;
  }

  public IDialogSettings getDialogSettings() {
    return m_dialogSettings;
  }

  public void updatePattern(String pattern, Object input) {
    setInput(new SearchPatternInput(input, pattern));
  }

  public SearchPatternInput getInput() {
    return m_input;
  }

  public void setInput(SearchPatternInput input) {
    m_input = input;
    if (getShell() != null && !getShell().isDisposed()) {
      try {
        m_uiLock.acquire();
        m_tableViewer.getTable().setRedraw(false);
        m_tableViewer.setInput(m_input);
        // Default to the first selection if there is no selection. This is the case, every time the popup is opened.
        if (m_tableViewer.getSelection().isEmpty()) {
          Object proposal = m_tableViewer.getElementAt(0);
          if (proposal != null) {
            m_tableViewer.setSelection(new StructuredSelection(proposal));
          }
        }
        if (m_itemCountLabel != null) {
          m_itemCountLabel.setText(m_tableViewer.getTable().getItemCount() + " " + Texts.get("ItemsFound"));
        }
        updateDescription(m_selectedProposal);
      }
      finally {
        m_tableViewer.getTable().setRedraw(true);
        m_uiLock.release();
      }
      constrainShellSize();
    }
  }

  public void setContentProvider(IContentProvider contentProvider) {
    if (contentProvider instanceof ILazyProposalContentProvider) {
      ILazyProposalContentProvider lazyProvider = (ILazyProposalContentProvider) contentProvider;
      m_contentProvider = new P_LazyContentProvider(lazyProvider);
    }
    else {
      m_contentProvider = contentProvider;
    }
    if (contentProvider instanceof IDialogSettingsProvider) {
      setDialogSettings(((IDialogSettingsProvider) contentProvider).getDialogSettings());
    }
    else {
      setDialogSettings(null);
    }
    if (m_tableViewer != null) {
      m_tableViewer.setContentProvider(m_contentProvider);
    }
  }

  public IContentProvider getContentProvider() {
    if (m_contentProvider instanceof P_LazyContentProvider) {
      return ((P_LazyContentProvider) m_contentProvider).getWrappedProvider();
    }
    else {
      return m_contentProvider;
    }
  }

  public void setLabelProvider(IBaseLabelProvider labelProvider) {
    if (labelProvider instanceof ITableLabelProvider || labelProvider instanceof ILabelProvider) {
      m_labelProvider = new P_StyledLabelProvider(labelProvider);
    }
    else {
      m_labelProvider = labelProvider;
    }
    if (m_tableViewer != null) {
      m_tableViewer.setLabelProvider(m_labelProvider);
    }
  }

  public IBaseLabelProvider getLabelProvider() {
    if (m_labelProvider instanceof P_StyledLabelProvider) {
      return ((P_StyledLabelProvider) m_labelProvider).getWrappedLabelProvider();
    }
    else {
      return m_labelProvider;
    }
  }

  public String getText(Object element) {
    if (m_labelProvider instanceof P_StyledLabelProvider) {
      return ((P_StyledLabelProvider) m_labelProvider).getText(element, 0, false);
    }
    else {
      return ((ILabelProvider) m_labelProvider).getText(element);
    }
  }

  public Object getSelectedProposal() {
    return m_selectedProposal;
  }

  @Override
  public int open() {
    Shell shell = getShell();
    if (shell == null || shell.isDisposed()) {
      shell = null;
      // create the window
      create();
      shell = getShell();
    }

    // limit the shell size to the display size
    constrainShellSize();
    shell.setVisible(true);
    return OK;
  }

  @Override
  protected void constrainShellSize() {
    Shell shell = getShell();
    try {
      shell.setRedraw(false);
      Rectangle ownerBounds = getOwnerControl().getDisplay().map(getOwnerControl().getParent(), null, getOwnerControl().getBounds());
      Rectangle displayBounds = shell.getDisplay().getBounds();
      Rectangle shellBounds = new Rectangle(ownerBounds.x + POPUP_OFFSET, ownerBounds.y + ownerBounds.height + POPUP_OFFSET, DEFAULT_WIDTH, DEFAULT_HEIGHT);

      if (getDialogSettings() != null) {
        String widthString = getDialogSettings().get(DIALOG_SETTINGS_WIDTH);
        if (!StringUtility.isNullOrEmpty(widthString)) {
          shellBounds.width = Integer.parseInt(widthString);
        }
        String heightString = getDialogSettings().get(DIALOG_SETTINGS_HEIGHT);
        if (!StringUtility.isNullOrEmpty(heightString)) {
          shellBounds.height = Integer.parseInt(heightString);
        }
      }

      // double width if description area is visible
      if (!((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
        shellBounds.width = shellBounds.width * 2;
      }

      // max screen 50% of screen size
      shellBounds.height = Math.min(displayBounds.height / 2, shellBounds.height);
      shellBounds.width = Math.min(displayBounds.width / 2, shellBounds.width);

      // check fit y axis
      int yDiff = (displayBounds.y + displayBounds.height) - (shellBounds.y + shellBounds.height);
      if (yDiff < 0) {
        if (Math.abs(yDiff) <= MINIMUM_HEIGHT) {
          // reduce to max allowed height
          shellBounds.height += yDiff;
        }
        else {
          // check on top
          shellBounds.y = shellBounds.y - (ownerBounds.height - 2 * POPUP_OFFSET) - shellBounds.height - 10;
        }
      }

      // check x axis
      int xDiff = (displayBounds.x + displayBounds.width) - (shellBounds.x + shellBounds.width);
      if (xDiff < 0) {
        // move left
        shellBounds.x = shellBounds.x + xDiff;
      }

      // check min sizes
      if (shellBounds.width < MINIMUM_WIDTH) {
        shellBounds.width = MINIMUM_WIDTH;
      }
      if (shellBounds.height < MINIMUM_HEIGHT) {
        shellBounds.height = MINIMUM_HEIGHT;
      }

      shell.setBounds(shellBounds);
      shell.layout();

      // workaround: on some operating systems (e.g. ubuntu) the shell does not have the size we just applied in setBounds(). It actually is slightly smaller.
      // therefore we remember the difference to correct it afterwards in close() where we save the last popup size.
      // this prevents the popup from getting smaller and smaller. See bugzilla 453515 for details.
      Point sizeAfterSetBounds = shell.getSize();
      int deltaX = shellBounds.width - sizeAfterSetBounds.x;
      int deltaY = shellBounds.height - sizeAfterSetBounds.y;
      if (deltaX != 0 || deltaY != 0) {
        m_shellSizeDif = new Point(deltaX / 2, deltaY);
      }
    }
    finally {
      shell.setRedraw(true);
    }
  }

  private void updateDescription(Object proposal) {
    // remove old content
    for (Control c : m_proposalDescriptionArea.getChildren()) {
      if (!c.isDisposed()) {
        c.dispose();
      }
    }
    if (getProposalDescriptionProvider() != null) {
      Control content = getProposalDescriptionProvider().createDescriptionContent(m_proposalDescriptionArea, proposal);
      if (content != null) {
        ((GridLayout) m_proposalDescriptionArea.getParent().getLayout()).numColumns = 2;
        m_proposalDescriptionArea.setContent(content);
        m_proposalDescriptionArea.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        ((GridData) m_proposalDescriptionArea.getLayoutData()).exclude = false;

      }
      else {
        ((GridLayout) m_proposalDescriptionArea.getParent().getLayout()).numColumns = 1;
        ((GridData) m_proposalDescriptionArea.getLayoutData()).exclude = true;
      }
      constrainShellSize();
    }
  }

  public void setProposalDescriptionProvider(IProposalDescriptionProvider proposalDescriptionProvider) {
    m_proposalDescriptionProvider = proposalDescriptionProvider;
  }

  public IProposalDescriptionProvider getProposalDescriptionProvider() {
    return m_proposalDescriptionProvider;
  }

  @Override
  public boolean close() {
    if (isVisible()) {
      firePopupEvent(new ProposalPopupEvent(ProposalPopupEvent.TYPE_POPUP_CLOSED));
      if (getDialogSettings() != null) {
        Point size = getShell().getSize();
        // double width if description area is visible
        if (!((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
          size.x = size.x / 2;
        }
        if (m_shellSizeDif != null) {
          size.x += m_shellSizeDif.x;
          size.y += m_shellSizeDif.y;
          m_shellSizeDif = null;
        }
        getDialogSettings().put(DIALOG_SETTINGS_WIDTH, size.x);
        getDialogSettings().put(DIALOG_SETTINGS_HEIGHT, size.y);
      }
      getShell().setVisible(false);
      return true;
    }
    return false;
  }

  /**
   *
   */
  public void dispose() {
    super.close();
  }

  public boolean isFocusOwner() {
    if (getShell() == null || getShell().isDisposed()) {
      return false;
    }
    Control focusControl = getShell().getDisplay().getFocusControl();
    return (focusControl != null && focusControl.getShell() == getShell());
  }

  public boolean isVisible() {
    return getShell() != null && !getShell().isDisposed() && getShell().isVisible();
  }

  public void addPopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.add(IProposalPopupListener.class, proposalSelectionListener);
  }

  public void removePopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.remove(IProposalPopupListener.class, proposalSelectionListener);
  }

  private void firePopupEvent(ProposalPopupEvent event) {
    for (IProposalPopupListener listener : m_selectionListeners.getListeners(IProposalPopupListener.class)) {
      try {
        listener.popupChanged(event);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("Error in IProposalPopupListener listener.", e);
      }
    }
  }

  boolean setFocus() {
    return m_tableViewer.getTable().setFocus();
  }

  protected Control getOwnerControl() {
    return m_proposalField;
  }

  private synchronized Object[] loadProposals(String pattern, IProgressMonitor monitor) {
    IContentProvider contentProvider = m_contentProvider;
    Object[] proposals = ((P_LazyContentProvider) contentProvider).getWrappedProvider().getProposals(pattern, monitor);
    return proposals;
  }

  private void setInputSync(final SearchPatternInput input, final IProgressMonitor monitor) {
    if (!m_tableViewer.getControl().isDisposed()) {
      m_tableViewer.getControl().getDisplay().syncExec(new P_InputUpdateRunnable(monitor, input, this));
    }
  }

  private static final class P_InputUpdateRunnable implements Runnable {
    private final IProgressMonitor m_monitor;
    private final SearchPatternInput m_input;
    private final ProposalPopup m_instance;

    private P_InputUpdateRunnable(IProgressMonitor monitor, SearchPatternInput input, ProposalPopup instance) {
      m_input = input;
      m_monitor = monitor;
      m_instance = instance;
    }

    @Override
    public void run() {
      if (m_monitor.isCanceled()) {
        return;
      }
      m_instance.setInput(m_input);
    }
  }

  private final class P_LazyContentProvider implements IStructuredContentProvider {

    private final ILazyProposalContentProvider m_wrappedProvider;

    public P_LazyContentProvider(ILazyProposalContentProvider wrappedProvider) {
      m_wrappedProvider = wrappedProvider;
    }

    public ILazyProposalContentProvider getWrappedProvider() {
      return m_wrappedProvider;
    }

    @Override
    public void dispose() {
      m_wrappedProvider.dispose();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      m_wrappedProvider.inputChanged(viewer, oldInput, newInput);
    }

    @Override
    public synchronized Object[] getElements(Object inputElement) {
      SearchPatternInput input = (SearchPatternInput) inputElement;
      if (input.getProposals() == null) {
        if (m_lazyLoaderJob != null) {
          m_lazyLoaderJob.cancel();
        }
        else {
          m_lazyLoaderJob = new P_LazyLoader();
        }
        m_lazyLoaderJob.schedule(input);
        return new Object[0];
      }
      else {
        return input.getProposals();
      }
    }
  }

  private final class P_LoadingProposalJob extends Job {
    private final P_LazyLoader m_parentJob;
    private final SearchPatternInput m_inputLoading;

    public P_LoadingProposalJob(P_LazyLoader parentJob, SearchPatternInput input) {
      super("loading proposal job");
      setSystem(true);
      m_parentJob = parentJob;
      m_inputLoading = new SearchPatternInput(input.getInput(), input.getPattern());
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        m_parentJob.join(250);
        synchronized (m_parentJob) {
          if (!m_parentJob.m_loaded) {
            m_inputLoading.setProposals(LOADING_PROPOSAL);
            setInputSync(m_inputLoading, monitor);
          }
        }
      }
      catch (InterruptedException e) {
        //nop
      }
      return Status.OK_STATUS;
    }
  }// end class P_LoadingProposalJob

  private final class P_LazyLoader extends JobEx {

    private SearchPatternInput m_input;
    private boolean m_loaded;

    public P_LazyLoader() {
      super("proposal loader");
      setSystem(true);
      m_loaded = false;
    }

    public void schedule(SearchPatternInput input) {
      m_input = new SearchPatternInput(input.getInput(), input.getPattern());
      new P_LoadingProposalJob(this, input).schedule();
      schedule();
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
      m_input.setProposals(loadProposals(m_input.getPattern(), monitor));
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }
      synchronized (this) {
        setInputSync(m_input, monitor);
        m_loaded = true;
      }
      return Status.OK_STATUS;
    }
  }// end class P_LazyLoader

  private final class P_StyledLabelProvider extends StyledCellLabelProvider {
    private final IBaseLabelProvider m_wrappedLabelProvider;
    private Font m_boldFont;
    private Styler m_boldStyler;

    private P_StyledLabelProvider(IBaseLabelProvider labelProvider) {
      m_wrappedLabelProvider = labelProvider;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return m_wrappedLabelProvider.isLabelProperty(element, property);
    }

    public IBaseLabelProvider getWrappedLabelProvider() {
      return m_wrappedLabelProvider;
    }

    @Override
    public void initialize(ColumnViewer viewer, ViewerColumn column) {
      super.initialize(viewer, column);
      Font defaultFont = viewer.getControl().getFont();
      FontData[] defaultFontData = defaultFont.getFontData();
      FontData[] boldFontData = new FontData[defaultFontData.length];
      for (int i = 0; i < defaultFontData.length; i++) {
        boldFontData[i] = new FontData(defaultFontData[i].getName(), defaultFontData[i].getHeight(), defaultFontData[i].getStyle() | SWT.BOLD);
      }
      m_boldFont = new Font(viewer.getControl().getDisplay(), boldFontData);
      m_boldStyler = new Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
          textStyle.font = m_boldFont;
        }
      };
    }

    @Override
    public void dispose() {
      super.dispose();
      m_wrappedLabelProvider.dispose();
      m_boldFont.dispose();
      m_boldFont = null;
    }

    @Override
    public void update(ViewerCell cell) {
      Object element = cell.getElement();
      StyledString text = new StyledString(getText(element, cell.getColumnIndex(), CompareUtility.equals(m_selectedProposal, element)));
      if (cell.getColumnIndex() == 0) {
        if (m_wrappedLabelProvider instanceof ISearchRangeConsumer) {
          ISearchRangeConsumer labelProvider = (ISearchRangeConsumer) m_wrappedLabelProvider;
          int[] matchingRegions = labelProvider.getMatchRanges(element);
          if (matchingRegions != null && matchingRegions.length > 0) {
            for (int i = 0; i < matchingRegions.length - 1; i += 2) {
              int offset = matchingRegions[i];
              int length = matchingRegions[i + 1];
              if (offset >= 0 && (offset + length) <= text.length()) {
                text.setStyle(offset, length, m_boldStyler);
              }
            }
          }
          if (labelProvider.isFormatConcatString()) {
            // package information
            int index = text.getString().indexOf(JavaElementLabels.CONCAT_STRING);
            if (index > 0) {
              text.setStyle(index, text.length() - index, StyledString.QUALIFIER_STYLER);
            }
          }
        }
      }
      cell.setText(text.getString());
      cell.setStyleRanges(text.getStyleRanges());
      cell.setImage(getImage(element, cell.getColumnIndex(), m_selectedProposal == element));
    }

    private String getText(Object element, int columnIndex, boolean selected) {
      if (element instanceof ISeparatorProposal) {
        if (m_wrappedLabelProvider instanceof ISeparatorLabelProvider) {
          return ((ISeparatorLabelProvider) m_wrappedLabelProvider).getSeparatorText((ISeparatorProposal) element);
        }
        else {
          return ((ISeparatorProposal) element).getLabel();
        }
      }
      if (selected && m_wrappedLabelProvider instanceof ISelectionStateLabelProvider) {
        return ((ISelectionStateLabelProvider) m_wrappedLabelProvider).getTextSelected(element);
      }
      else if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getText(element);
      }
      return null;
    }

    private Image getImage(Object element, int columnIndex, boolean selected) {
      if (element instanceof ISeparatorProposal) {
        if (m_wrappedLabelProvider instanceof ISeparatorLabelProvider) {
          return ((ISeparatorLabelProvider) m_wrappedLabelProvider).getSeparatorImage((ISeparatorProposal) element);
        }
        else {
          return ((ISeparatorProposal) element).getImage();
        }
      }
      if (selected && m_wrappedLabelProvider instanceof ISelectionStateLabelProvider) {
        return ((ISelectionStateLabelProvider) m_wrappedLabelProvider).getImageSelected(element);
      }
      else if (m_wrappedLabelProvider instanceof ITableLabelProvider) {
        return ((ITableLabelProvider) m_wrappedLabelProvider).getColumnImage(element, columnIndex);
      }
      else if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getImage(element);
      }
      return null;
    }
  } // end class P_StyledLabelProvider

  public static final class SearchPatternInput {
    private final String m_pattern;
    private final Object m_input;
    private Object[] m_proposals;

    public SearchPatternInput(Object input, String pattern) {
      m_input = input;
      m_pattern = pattern;
    }

    public Object getInput() {
      return m_input;
    }

    public String getPattern() {
      return m_pattern;
    }

    public Object[] getProposals() {
      return m_proposals;
    }

    public void setProposals(Object[] proposals) {
      m_proposals = proposals;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("input[").append(getInput()).append("] ");
      builder.append("pattern[").append(getPattern()).append("] ");
      builder.append("proposals[");
      Object[] proposals = getProposals();
      for (int i = 0; i < proposals.length; i++) {
        if (proposals[i] == null) {
          builder.append("null");
        }
        else {
          builder.append(proposals[i].toString());
        }
        if (i < proposals.length - 1) {
          builder.append(", ");
        }
      }
      builder.append("] ");
      return builder.toString();
    }

    @Override
    public int hashCode() {
      int hash = 0;
      if (m_input != null) {
        hash ^= m_input.hashCode();
      }
      if (m_pattern != null) {
        hash ^= m_pattern.hashCode();
      }
      if (m_proposals != null) {
        for (Object proposal : m_proposals) {
          if (proposal != null) {
            hash ^= proposal.hashCode();
          }
        }
      }
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SearchPatternInput)) {
        return false;
      }
      SearchPatternInput input = (SearchPatternInput) obj;
      return (CompareUtility.equals(input.getInput(), getInput())) && CompareUtility.equals(input.getPattern(), getPattern()) && CompareUtility.equals(input.getProposals(), getProposals());
    }
  }
}
