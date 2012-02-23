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

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.ISearchRangeConsumer;
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
  protected static final String DIALOG_SETTINGS_WIDTH = "dialogSettingsWidth";
  protected static final String DIALOG_SETTINGS_HEIGHT = "dialogSettingsHeight";

  private static final int POPUP_OFFSET = 2;

  /*
   * The minimum pixel width for the popup. May be overridden by using
   * setInitialPopupSize.
   */
  private static final int MINIMUM_HEIGHT = 100;
  private static final int DEFAULT_WIDTH = 300;
  private static final int DEFAULT_HEIGHT = 300;

  private Control m_proposalField;
  private TableViewer m_tableViewer;
  private Label m_itemCountLabel;
  private ScrolledComposite m_proposalDescriptionArea;

  private Object m_selectedProposal;
  private List<IProposalPopupListener> m_selectionListeners = new ArrayList<IProposalPopupListener>();

  private SearchPatternInput m_input;
  private IProposalDescriptionProvider m_proposalDescriptionProvider;
  private IBaseLabelProvider m_labelProvider;
  private IContentProvider m_contentProvider;

  private P_LazyLoader m_lazyLoaderJob;
  private OptimisticLock m_uiLock = new OptimisticLock();
  private IDialogSettings m_dialogSettings;

  private Listener m_parentShellListener = new Listener() {

    @Override
    public void handleEvent(Event event) {
      close();
    }
  };

  public ProposalPopup(Control proposalField) {
    super(proposalField.getShell());
    m_proposalField = proposalField;
    int style = SWT.RESIZE | SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL;
    setShellStyle(style);
//    setShellStyle(SWT.RESIZE | SWT.ON_TOP | SWT.POP_UP);
    setBlockOnOpen(false);
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
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
        // refresh selected label
        if (m_selectedProposal != null) {
          m_tableViewer.update(m_selectedProposal, new String[]{"label"});
        }
        m_selectedProposal = null;
        if (!event.getSelection().isEmpty()) {
          m_selectedProposal = ((StructuredSelection) event.getSelection()).getFirstElement();
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
    });

    m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          // selectProposal(table.getSelectionIndex());
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

  public void setDialogSettings(IDialogSettings dialogSettings) {
    m_dialogSettings = dialogSettings;
  }

  public IDialogSettings getDialogSettings() {
    return m_dialogSettings;
  }

  public void updatePattern(String pattern, Object input) {
    String lastSearchText = null;
    if (getInput() != null) {
      lastSearchText = getInput().getPattern();
    }
    if (CompareUtility.notEquals(pattern, lastSearchText)) {
      setInput(new SearchPatternInput(input, pattern));
    }
  }

  public SearchPatternInput getInput() {
    return m_input;
  }

  public void setInput(SearchPatternInput input) {
    Object oldInput = getInput();
    if (!CompareUtility.equals(oldInput, input)) {
      m_input = input;
//      m_contentProvider.inputChanged(m_tableViewer, oldInput, input);
      if (isVisible()) {//m_tableViewer != null && !m_tableViewer.getTable().isDisposed()) {
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
        // recompute
//        getShell().layout(true, true);
//        try {
//          getShell().setRedraw(false);
//          getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
//          constrainShellSize();
        constrainShellSize();
//        adjustShellBounds(true);
//        }
//        finally {
//          getShell().setRedraw(true);
//        }
//        getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
//        constrainShellSize();
      }
    }
  }

  public void setContentProvider(IContentProvider contentProvider) {
    if (contentProvider instanceof ILazyProposalContentProvider) {
      m_contentProvider = new P_LazyContentProvider((ILazyProposalContentProvider) contentProvider);
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
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellDeactivated(ShellEvent e) {
        getShell().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            close();

          }
        });
      }
    });
    getOwnerControl().getShell().addListener(SWT.Move, m_parentShellListener);

    // provide a hook for adjusting the bounds. This is only
    // necessary when there is content driven sizing that must be
    // adjusted each time the dialog is opened.
//    adjustBounds();

    // limit the shell size to the display size
//    adjustShellBounds(false);
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
      Rectangle shellBounds = shell.getBounds();
      shellBounds = new Rectangle(shellBounds.x, shellBounds.y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
      shellBounds.x = ownerBounds.x + POPUP_OFFSET;
      shellBounds.y = ownerBounds.y + ownerBounds.height + POPUP_OFFSET;
      if (getDialogSettings() != null) {
        String withString = getDialogSettings().get(DIALOG_SETTINGS_WIDTH);
        if (!StringUtility.isNullOrEmpty(withString)) {
          shellBounds.width = Integer.parseInt(withString);
        }
        String heightString = getDialogSettings().get(DIALOG_SETTINGS_HEIGHT);
        if (!StringUtility.isNullOrEmpty(heightString)) {
          shellBounds.height = Integer.parseInt(heightString);
        }
      }
      // double width if description area is visisble
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

      // check below
      shell.setBounds(shellBounds);
      shell.layout();

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
//      getShell().layout(true);
//      adjustBounds();
      constrainShellSize();
//      adjustShellBounds(true);
//      m_proposalDescriptionArea.setExpandHorizontal(true);
//      m_proposalDescriptionArea.setExpandVertical(true);
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
    getOwnerControl().getShell().removeListener(SWT.Resize, m_parentShellListener);
    firePopupEvent(new ProposalPopupEvent(ProposalPopupEvent.TYPE_POPUP_CLOSED));
    if (getDialogSettings() != null) {
      Point size = getShell().getSize();
      // double width if description area is visisble
      if (!((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
        size.x = size.x / 2;
      }
      getDialogSettings().put(DIALOG_SETTINGS_WIDTH, size.x);
      getDialogSettings().put(DIALOG_SETTINGS_HEIGHT, size.y);
    }
    return super.close();
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
    m_selectionListeners.add(proposalSelectionListener);
  }

  public void removePopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.remove(proposalSelectionListener);
  }

  private void firePopupEvent(ProposalPopupEvent event) {
    IProposalPopupListener[] listeners;
    synchronized (m_selectionListeners) {
      listeners = m_selectionListeners.toArray(new IProposalPopupListener[m_selectionListeners.size()]);
    }
    for (IProposalPopupListener listener : listeners) {
      listener.popupChanged(event);
    }
  }

  boolean setFocus() {
    return m_tableViewer.getTable().setFocus();
  }

//  private void adjustBounds() {
//    // Get our control's location in display coordinates.
////    FormData data = (FormData) m_tableViewer.getTable().getLayoutData();
////    data.height = m_tableViewer.getTable().getItemHeight() * POPUP_CHAR_HEIGHT;
////    data.width = Math.max(m_proposalField.getSize().x, POPUP_MINIMUM_WIDTH);
////    m_tableViewer.getTable().setLayoutData(data);
//    Point location = m_proposalField.getDisplay().map(m_proposalField.getParent(), null, m_proposalField.getLocation());
//    int initialX = location.x + POPUP_OFFSET;
//    int initialY = location.y + m_proposalField.getSize().y + POPUP_OFFSET;
//
//    // If there is no specified size, force it by setting
//    // up a layout on the table.
//    Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
//    if (((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
//      size.x = POPUP_MINIMUM_WIDTH;
//    }
//    else {
//      size.x = POPUP_MINIMUM_WIDTH * 2;
//    }
//    size.y = POPUP_MINIMUM_WIDTH;
//    getShell().setBounds(initialX, initialY, size.x, size.y);
////    if (popupSize == null) {
////    FormData data = (FormData) m_tableViewer.getTable().getLayoutData();
////    data.height = m_tableViewer.getTable().getItemHeight() * POPUP_CHAR_HEIGHT;
////    data.width = Math.max(m_proposalField.getSize().x, POPUP_MINIMUM_WIDTH);
////    m_tableViewer.getTable().setLayoutData(data);
////      getShell().pack();
////      popupSize = getShell().getSize();
////    }
////    getShell().setBounds(initialX, initialY, popupSize.x, popupSize.y);
//
//  }

  protected Control getOwnerControl() {
    return m_proposalField;
  }

  private synchronized Object[] loadProposals(String pattern, IProgressMonitor monitor) {
    IContentProvider contentProvider = m_contentProvider;
    Object[] proposals = ((P_LazyContentProvider) contentProvider).getWrappedProvider().getProposals(pattern, monitor);
    return proposals;
  }

  private class P_LazyContentProvider implements IStructuredContentProvider {

    private final ILazyProposalContentProvider m_wrappedProvider;
    private Object[] m_proposals = null;

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
//      resetCache();
      m_wrappedProvider.inputChanged(viewer, oldInput, newInput);
    }

    private void setElement(Object[] elements) {
      m_proposals = elements;
    }

    @Override
    public synchronized Object[] getElements(Object inputElement) {
      SearchPatternInput input = (SearchPatternInput) inputElement;
      System.out.println("getELements '" + input.getPattern() + "'");
//      new Exception().printStackTrace();
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

//    public synchronized void resetCache() {
//      m_proposals = null;
//    }
  }

  private class P_LazyLoader extends Job {

    private SearchPatternInput m_input;

    public P_LazyLoader() {
      super("proposal loader");
      setSystem(true);
    }

    public void schedule(SearchPatternInput input) {

      m_input = new SearchPatternInput(input.getInput(), input.getPattern());
      schedule();
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
      m_input.setProposals(loadProposals(m_input.getPattern(), monitor));
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }

      TuningUtility.startTimer();
      synchronized (ProposalPopup.this) {
        if (!m_tableViewer.getControl().isDisposed()) {
          m_tableViewer.getControl().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
              TuningUtility.startTimer();
              try {
                if (monitor.isCanceled()) {
                  return;
                }
                System.out.println("DISPLAY refresh " + m_input);
                setInput(m_input);
              }
              finally {
                TuningUtility.stopTimer("DISPLAY update inner for '" + m_input.getPattern() + "'");
              }
            }
          });
        }
        TuningUtility.stopTimer("DISPLAY proposal update for '" + m_input.getPattern() + "'");
        return Status.OK_STATUS;
      }
    }
  }// end class P_LazyLoader

  private class P_StyledLabelProvider extends StyledCellLabelProvider {
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
      StyledString text = new StyledString(getText(element, cell.getColumnIndex(), m_selectedProposal == element));
      if (cell.getColumnIndex() == 0) {
        if (m_wrappedLabelProvider instanceof ISearchRangeConsumer) {
          int[] matchingRegions = ((ISearchRangeConsumer) m_wrappedLabelProvider).getMatchRanges(element);
          if (matchingRegions != null && matchingRegions.length > 0) {

            for (int i = 0; i < matchingRegions.length - 1; i += 2) {
              text.setStyle(matchingRegions[i], matchingRegions[i + 1], m_boldStyler);
            }
          }
          // package information
          int index = text.getString().indexOf(JavaElementLabels.CONCAT_STRING);
          if (index > 0) {
            text.setStyle(index, text.length() - index, StyledString.QUALIFIER_STYLER);
          }
        }
      }
      cell.setText(text.getString());
      cell.setStyleRanges(text.getStyleRanges());
      cell.setImage(getImage(element, cell.getColumnIndex(), m_selectedProposal == element));
    }

    private String getText(Object element, int columnIndex, boolean selected) {
      if (selected && m_wrappedLabelProvider instanceof ISelectionStateLabelProvider) {
        return ((ISelectionStateLabelProvider) m_wrappedLabelProvider).getTextSelected(element);
      }
      else if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getText(element);
      }
      return null;
    }

    private Image getImage(Object element, int columnIndex, boolean selected) {
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

  public static class SearchPatternInput {
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
      System.out.println("setproposals " + proposals);
      m_proposals = proposals;
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
