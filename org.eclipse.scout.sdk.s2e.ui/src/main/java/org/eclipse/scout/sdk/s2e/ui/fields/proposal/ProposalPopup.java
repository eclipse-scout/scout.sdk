/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import javax.swing.event.EventListenerList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextEx;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.NormalizedPattern;
import org.eclipse.scout.sdk.s2e.util.OptimisticLock;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

class ProposalPopup extends Window {

  private static final String DIALOG_SETTINGS_WIDTH = "dialogSettingsWidth";
  private static final String DIALOG_SETTINGS_HEIGHT = "dialogSettingsHeight";
  private static final int POPUP_OFFSET = 2;
  private static final int MINIMUM_HEIGHT = 100;
  private static final int MINIMUM_WIDTH = 200;
  private static final int DEFAULT_WIDTH = 400;
  private static final int DEFAULT_HEIGHT = 300;

  public static final ISeparatorProposal LOADING_PROPOSAL = new ISeparatorProposal() {
    @Override
    public String getLabel() {
      return "Loading...";
    }

    @Override
    public Image getImage() {
      return S2ESdkUiActivator.getImage(ISdkIcons.Separator);
    }
  };

  private final StyledTextEx m_proposalField;
  private final EventListenerList m_selectionListeners;
  private final OptimisticLock m_uiLock;
  private final Object m_lazyLoaderJobLock;
  private final Object m_descLoaderJobLock;

  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private TableViewer m_tableViewer;
  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private Label m_itemCountLabel;
  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private ScrolledComposite m_proposalDescriptionArea;

  private Object m_selectedProposal;

  private SearchPatternInput m_input;
  private IProposalDescriptionProvider m_proposalDescriptionProvider;
  private IBaseLabelProvider m_labelProvider;
  private P_LazyContentProvider m_contentProvider;

  private P_LazyLoader m_lazyLoaderJob;
  private P_DescriptionLoader m_descLoaderJob;
  private IDialogSettings m_dialogSettings;
  private Point m_shellSizeDif;

  ProposalPopup(StyledTextEx proposalField) {
    super(proposalField.getShell());
    m_uiLock = new OptimisticLock();
    m_lazyLoaderJobLock = new Object();
    m_descLoaderJobLock = new Object();
    m_selectionListeners = new EventListenerList();
    m_proposalField = proposalField;
    setShellStyle(SWT.RESIZE | SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL);
    setBlockOnOpen(false);
    // listeners
    getOwnerControl().getShell().addListener(SWT.Move, event -> close());
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    // listener
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellDeactivated(ShellEvent e) {
        getShell().getDisplay().asyncExec(() -> {
          if (getOwnerControl() != null && !getOwnerControl().isDisposed()) {
            if (!getOwnerControl().isFocusControl()) {
              close();
            }
          }
          else {
            close();
          }
        });
      }
    });
  }

  protected void fireProposalAccepted(IStructuredSelection selection, boolean moveFocus) {
    var delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
    delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
    delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_MOVE_FOCUS, moveFocus);
    firePopupEvent(delegateEvent);
  }

  @Override
  @SuppressWarnings("findbugs:IS2_INCONSISTENT_SYNC")
  protected Control createContents(Composite parent) {
    var proposalArea = new Composite(parent, SWT.INHERIT_FORCE);
    proposalArea.setBackground(proposalArea.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    var table = new Table(proposalArea, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_tableViewer = new TableViewer(table);
    m_tableViewer.setContentProvider(m_contentProvider);
    m_tableViewer.setLabelProvider(m_labelProvider);
    m_tableViewer.setInput(getInput());
    m_tableViewer.addSelectionChangedListener(event -> {
      Object newSelection = null;
      if (!event.getSelection().isEmpty()) {
        newSelection = ((IStructuredSelection) event.getSelection()).getFirstElement();
        if (newSelection instanceof ISeparatorProposal) {
          return;
        }
      }
      handleProposalSelection(newSelection);
    });

    m_tableViewer.addDoubleClickListener(event -> {
      var selection = (IStructuredSelection) event.getSelection();
      if (!selection.isEmpty()) {
        if (selection.size() == 1 && selection.getFirstElement() instanceof ISeparatorProposal) {
          return;
        }
        fireProposalAccepted(selection, true);
      }
    });
    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (' ' == e.keyCode) {
          var selection = m_tableViewer.getStructuredSelection();
          if (!selection.isEmpty()) {
            fireProposalAccepted(selection, true);
          }
        }
      }
    });
    table.setHeaderVisible(false);

    m_proposalDescriptionArea = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    m_proposalDescriptionArea.setBackground(m_proposalDescriptionArea.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    m_proposalDescriptionArea.setExpandHorizontal(true);
    m_proposalDescriptionArea.setExpandVertical(true);

    var footer = new Composite(proposalArea, SWT.BORDER);
    footer.setBackground(footer.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_itemCountLabel = new Label(footer, SWT.NONE);
    m_itemCountLabel.setBackground(m_itemCountLabel.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    // layout
    GridLayoutFactory
        .swtDefaults()
        .spacing(0, 0)
        .margins(0, 0)
        .applyTo(m_proposalDescriptionArea);
    GridLayoutFactory
        .swtDefaults()
        .equalWidth(true)
        .spacing(0, 0)
        .margins(0, 0)
        .applyTo(parent);
    GridDataFactory
        .defaultsFor(proposalArea)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(proposalArea);
    GridDataFactory
        .defaultsFor(m_proposalDescriptionArea)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .exclude(true)
        .applyTo(m_proposalDescriptionArea);

    proposalArea.setLayout(new FormLayout());
    var data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(footer, 0);
    table.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    footer.setLayoutData(data);

    GridLayoutFactory
        .swtDefaults()
        .margins(5, 0)
        .applyTo(footer);
    GridDataFactory
        .defaultsFor(m_itemCountLabel)
        .align(SWT.FILL, SWT.BEGINNING)
        .grab(true, true)
        .applyTo(m_itemCountLabel);
    return parent;
  }

  private void handleProposalSelection(Object proposal) {
    if (Objects.equals(m_selectedProposal, proposal)) {
      return;
    }

    // refresh selected label
    var old = m_selectedProposal;
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
        var delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_SELECTED);
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

  public synchronized SearchPatternInput getInput() {
    return m_input;
  }

  @SuppressWarnings("pmd:NPathComplexity")
  public synchronized void setInput(SearchPatternInput input) {
    m_input = input;
    if (getShell() == null || getShell().isDisposed()) {
      return;
    }

    try {
      m_uiLock.acquire();
      m_tableViewer.getTable().setRedraw(false);

      m_tableViewer.setInput(m_input);
      var selection = m_tableViewer.getStructuredSelection();
      if (selection.isEmpty()) {
        var proposal = input.getInput();
        if (proposal == null && m_proposalField.getText() != null) {
          proposal = getProposalByText(m_proposalField.getText(), input.m_proposals);
          if (proposal != null) {
            // we automatically select the proposal that matches the active text -> this means a new selection has implicitly been accepted. Inform the text field
            fireProposalAccepted(new StructuredSelection(proposal), false);
          }
        }
        if (proposal != null) {
          selection = new StructuredSelection(proposal);
          m_tableViewer.setSelection(selection, true /* must reveal already even we reveal again afterwards! */);
        }
      }

      // reveal selection
      if (!selection.isEmpty()) {
        // try to scroll to items after the selected one to show the selected in the "middle" of the viewer
        var selectedRowIndex = m_tableViewer.getTable().getSelectionIndex();
        var lastRowIndex = m_tableViewer.getTable().getItemCount() - 1;
        var numOffsetRows = 3;
        int revealRowIndex;
        if (selectedRowIndex < lastRowIndex - numOffsetRows) {
          revealRowIndex = selectedRowIndex + numOffsetRows;
        }
        else {
          revealRowIndex = lastRowIndex;
        }
        if (revealRowIndex != selectedRowIndex) {
          m_tableViewer.reveal(m_tableViewer.getElementAt(revealRowIndex));
        }
      }

      // items found label
      if (m_itemCountLabel != null) {
        var itemCount = m_tableViewer.getTable().getItemCount();
        if (itemCount < 1) {
          m_itemCountLabel.setText("No items found.");
        }
        else if (itemCount == 1) {
          //noinspection AccessToStaticFieldLockedOnInstance
          if (m_tableViewer.getTable().getItem(0).getData() == LOADING_PROPOSAL) {
            m_itemCountLabel.setText("Still loading. Please wait...");
          }
          else {
            m_itemCountLabel.setText("1 item found.");
          }
        }
        else {
          m_itemCountLabel.setText(itemCount + " items found.");
        }
      }
      constrainShellSize();
    }
    finally {
      m_tableViewer.getTable().setRedraw(true);
      m_uiLock.release();
    }
  }

  protected Object getProposalByText(String text, Collection<Object> proposals) {
    if (proposals == null || proposals.isEmpty()) {
      return null;
    }
    return proposals.stream()
        .filter(o -> text.equals(getText(o)))
        .findFirst()
        .orElse(null);
  }

  public void setContentProvider(IProposalContentProvider contentProvider) {
    m_contentProvider = new P_LazyContentProvider(contentProvider);
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

  public IProposalContentProvider getContentProvider() {
    if (m_contentProvider == null) {
      return null;
    }
    return m_contentProvider.getWrappedProvider();
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
    return m_labelProvider;
  }

  public String getText(Object element) {
    if (m_labelProvider instanceof P_StyledLabelProvider) {
      return ((P_StyledLabelProvider) m_labelProvider).getText(element, false);
    }
    return ((ILabelProvider) m_labelProvider).getText(element);
  }

  public Object getSelectedProposal() {
    return m_selectedProposal;
  }

  @Override
  public int open() {
    var shell = getShell();
    if (shell == null || shell.isDisposed()) {
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
  @SuppressWarnings("pmd:NPathComplexity")
  protected void constrainShellSize() {
    var shell = getShell();
    try {
      shell.setRedraw(false);
      var ownerBounds = getOwnerControl().getDisplay().map(getOwnerControl().getParent(), null, getOwnerControl().getBounds());
      var displayBounds = shell.getDisplay().getBounds();
      var shellBounds = new Rectangle(ownerBounds.x + POPUP_OFFSET, ownerBounds.y + ownerBounds.height + POPUP_OFFSET, DEFAULT_WIDTH, DEFAULT_HEIGHT);

      if (getDialogSettings() != null) {
        var widthString = getDialogSettings().get(DIALOG_SETTINGS_WIDTH);
        if (Strings.hasText(widthString)) {
          shellBounds.width = Integer.parseInt(widthString);
        }
        var heightString = getDialogSettings().get(DIALOG_SETTINGS_HEIGHT);
        if (Strings.hasText(heightString)) {
          shellBounds.height = Integer.parseInt(heightString);
        }
      }

      // double width if description area is visible
      if (!((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
        shellBounds.width *= 2;
      }

      // max screen 50% of screen size
      shellBounds.height = Math.min(displayBounds.height / 2, shellBounds.height);
      shellBounds.width = Math.min(displayBounds.width / 2, shellBounds.width);

      // check fit y axis
      var yDiff = (displayBounds.y + displayBounds.height) - (shellBounds.y + shellBounds.height);
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
      var xDiff = (displayBounds.x + displayBounds.width) - (shellBounds.x + shellBounds.width);
      if (xDiff < 0) {
        // move left
        shellBounds.x += xDiff;
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
      var sizeAfterSetBounds = shell.getSize();
      var deltaX = shellBounds.width - sizeAfterSetBounds.x;
      var deltaY = shellBounds.height - sizeAfterSetBounds.y;
      if (deltaX != 0 || deltaY != 0) {
        m_shellSizeDif = new Point(deltaX / 2, deltaY);
      }
    }
    finally {
      shell.setRedraw(true);
    }
  }

  private void disposeDescriptions() {
    for (var c : m_proposalDescriptionArea.getChildren()) {
      if (!c.isDisposed()) {
        c.dispose();
      }
    }
    var content = m_proposalDescriptionArea.getContent();
    if (content != null && !content.isDisposed()) {
      content.dispose();
    }
    m_proposalDescriptionArea.setContent(null);
  }

  private void updateDescription(Object proposal) {
    synchronized (m_descLoaderJobLock) {
      // remove old content
      disposeDescriptions();

      if (proposal == null) {
        return;
      }

      var proposalDescriptionProvider = getProposalDescriptionProvider();
      if (proposalDescriptionProvider != null) {
        if (m_descLoaderJob != null) {
          m_descLoaderJob.cancel();
        }
        m_descLoaderJob = new P_DescriptionLoader(proposal);
        m_descLoaderJob.schedule(400L);
      }
    }
  }

  protected void layoutDescriptionArea() {
    var content = m_proposalDescriptionArea.getContent();
    if (content != null && !content.isDisposed()) {
      ((GridLayout) m_proposalDescriptionArea.getParent().getLayout()).numColumns = 2;
      m_proposalDescriptionArea.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      ((GridData) m_proposalDescriptionArea.getLayoutData()).exclude = false;
    }
    else {
      ((GridLayout) m_proposalDescriptionArea.getParent().getLayout()).numColumns = 1;
      ((GridData) m_proposalDescriptionArea.getLayoutData()).exclude = true;
    }
    constrainShellSize();
  }

  public void setProposalDescriptionProvider(IProposalDescriptionProvider proposalDescriptionProvider) {
    m_proposalDescriptionProvider = proposalDescriptionProvider;
  }

  public IProposalDescriptionProvider getProposalDescriptionProvider() {
    return m_proposalDescriptionProvider;
  }

  @Override
  public boolean close() {
    if (!isVisible()) {
      return false;
    }

    synchronized (m_descLoaderJobLock) {
      if (m_descLoaderJob != null) {
        m_descLoaderJob.cancel();
        m_descLoaderJob = null;
      }
      disposeDescriptions();
      layoutDescriptionArea();
    }
    synchronized (m_lazyLoaderJobLock) {
      if (m_lazyLoaderJob != null) {
        m_lazyLoaderJob.cancel();
        m_lazyLoaderJob = null;
      }
    }
    firePopupEvent(new ProposalPopupEvent(ProposalPopupEvent.TYPE_POPUP_CLOSED));
    if (getDialogSettings() != null) {
      var size = getShell().getSize();
      // double width if description area is visible
      if (!((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
        size.x /= 2;
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
    var focusControl = getShell().getDisplay().getFocusControl();
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
    for (var listener : m_selectionListeners.getListeners(IProposalPopupListener.class)) {
      listener.popupChanged(event);
    }
  }

  boolean setFocus() {
    var table = m_tableViewer.getTable();
    if (m_tableViewer.getSelection().isEmpty() && table.getItemCount() > 0) {
      var first = m_tableViewer.getElementAt(0);
      m_tableViewer.setSelection(new StructuredSelection(first), true);
    }
    return table.setFocus();
  }

  protected Control getOwnerControl() {
    return m_proposalField;
  }

  private final class P_LazyContentProvider implements IStructuredContentProvider {

    private final IProposalContentProvider m_wrappedProvider;

    private P_LazyContentProvider(IProposalContentProvider wrappedProvider) {
      m_wrappedProvider = wrappedProvider;
    }

    public IProposalContentProvider getWrappedProvider() {
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
    public Object[] getElements(Object inputElement) {
      var input = (SearchPatternInput) inputElement;
      var proposals = input.getProposals();
      if (proposals == null) {
        synchronized (m_lazyLoaderJobLock) {
          if (m_lazyLoaderJob != null) {
            m_lazyLoaderJob.cancel();
          }
          else {
            m_lazyLoaderJob = new P_LazyLoader();
          }
          m_lazyLoaderJob.schedule(input);
        }
        return new Object[]{LOADING_PROPOSAL};
      }
      return proposals.toArray();
    }
  }

  private final class P_DescriptionLoader extends AbstractJob {

    private final Object m_proposal;

    private P_DescriptionLoader(Object proposal) {
      super(P_DescriptionLoader.class.getName());
      setSystem(true);
      setUser(false);
      m_proposal = proposal;
    }

    @Override
    protected void execute(IProgressMonitor monitor) {
      var proposalDescriptionProvider = getProposalDescriptionProvider();
      if (proposalDescriptionProvider == null) {
        return;
      }
      if (monitor.isCanceled()) {
        return;
      }
      if (m_proposalDescriptionArea.isDisposed()) {
        return;
      }

      var contentData = proposalDescriptionProvider.createDescriptionContent(m_proposal, monitor);
      m_proposalDescriptionArea.getDisplay().asyncExec(() -> {
        synchronized (m_descLoaderJobLock) {
          if (m_proposalDescriptionArea.isDisposed()) {
            return;
          }
          if (contentData != null) {
            var content = proposalDescriptionProvider.createDescriptionControl(m_proposalDescriptionArea, contentData);
            if (content != null && !monitor.isCanceled()) {
              m_proposalDescriptionArea.setContent(content);
            }
          }
          layoutDescriptionArea();
        }
      });
    }
  }

  private final class P_LazyLoader extends AbstractJob {

    private SearchPatternInput m_searchPatternInput;

    private P_LazyLoader() {
      super(P_LazyLoader.class.getName());
      setSystem(true);
      setUser(false);
    }

    public void schedule(SearchPatternInput input) {
      m_searchPatternInput = new SearchPatternInput(input.getInput(), input.getPattern());
      schedule();
    }

    private Collection<Object> loadProposals(String pattern, IProgressMonitor monitor) {
      return m_contentProvider.getWrappedProvider().getProposals(NormalizedPattern.build(pattern), monitor);
    }

    private void setInputAsync(SearchPatternInput input, IProgressMonitor monitor) {
      var control = m_tableViewer.getControl();
      if (!control.isDisposed()) {
        control.getDisplay().asyncExec(() -> {
          if (monitor.isCanceled()) {
            return;
          }
          if (control.isDisposed()) {
            return;
          }
          setInput(input);
        });
      }
    }

    @Override
    protected void execute(IProgressMonitor monitor) {
      m_searchPatternInput.setProposals(loadProposals(m_searchPatternInput.getPattern(), monitor));
      if (monitor.isCanceled()) {
        return;
      }
      setInputAsync(m_searchPatternInput, monitor);
    }
  }

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
      var defaultFont = viewer.getControl().getFont();
      var defaultFontData = defaultFont.getFontData();
      var boldFontData = Arrays.stream(defaultFontData)
          .map(defaultFontDatum -> new FontData(defaultFontDatum.getName(), defaultFontDatum.getHeight(), defaultFontDatum.getStyle() | SWT.BOLD))
          .toArray(FontData[]::new);
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
      var element = cell.getElement();
      var text = new StyledString(getText(element, Objects.equals(m_selectedProposal, element)));
      if (cell.getColumnIndex() == 0 && m_wrappedLabelProvider instanceof ISearchRangeConsumer) {
        var labelProvider = (ISearchRangeConsumer) m_wrappedLabelProvider;
        var matchingRegions = labelProvider.getMatchRanges(element);
        if (matchingRegions != null && matchingRegions.length > 0) {
          for (var i = 0; i < matchingRegions.length - 1; i += 2) {
            var offset = matchingRegions[i];
            var length = matchingRegions[i + 1];
            if (offset >= 0 && (offset + length) <= text.length()) {
              text.setStyle(offset, length, m_boldStyler);
            }
          }
        }
      }
      cell.setText(text.getString());
      cell.setStyleRanges(text.getStyleRanges());
      cell.setImage(getImage(element, cell.getColumnIndex(), m_selectedProposal == element));
    }

    private String getText(Object element, boolean selected) {
      if (element instanceof ISeparatorProposal) {
        return ((ISeparatorProposal) element).getLabel();
      }
      if (selected && m_wrappedLabelProvider instanceof ISelectionStateLabelProvider) {
        return ((ISelectionStateLabelProvider) m_wrappedLabelProvider).getTextSelected(element);
      }
      if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getText(element);
      }
      return null;
    }

    private Image getImage(Object element, int columnIndex, boolean selected) {
      if (element instanceof ISeparatorProposal) {
        return ((ISeparatorProposal) element).getImage();
      }
      if (selected && m_wrappedLabelProvider instanceof ISelectionStateLabelProvider) {
        return ((ISelectionStateLabelProvider) m_wrappedLabelProvider).getImageSelected(element);
      }
      if (m_wrappedLabelProvider instanceof ITableLabelProvider) {
        return ((ITableLabelProvider) m_wrappedLabelProvider).getColumnImage(element, columnIndex);
      }
      if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getImage(element);
      }
      return null;
    }
  } // end class P_StyledLabelProvider

  public static final class SearchPatternInput {
    private final String m_pattern;
    private final Object m_input;
    private Collection<Object> m_proposals;

    SearchPatternInput(Object input, String pattern) {
      m_input = input;
      m_pattern = pattern;
    }

    public Object getInput() {
      return m_input;
    }

    public String getPattern() {
      return m_pattern;
    }

    public Collection<Object> getProposals() {
      return m_proposals;
    }

    public void setProposals(Collection<Object> collection) {
      m_proposals = collection;
    }

    @Override
    public String toString() {
      var builder = new StringBuilder();
      builder.append("input[").append(getInput()).append("] ");
      builder.append("pattern[").append(getPattern()).append("] ");
      builder.append("proposals[");
      var proposals = getProposals();
      if (proposals == null) {
        builder.append("null");
      }
      else {
        builder.append(proposals);
      }
      builder.append("] ");
      return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      var other = (SearchPatternInput) o;
      if (!Objects.equals(m_pattern, other.m_pattern)) {
        return false;
      }
      if (!Objects.equals(m_input, other.m_input)) {
        return false;
      }
      //noinspection NonFinalFieldReferenceInEquals
      return Objects.equals(m_proposals, other.m_proposals);
    }

    @Override
    public int hashCode() {
      var result = m_pattern != null ? m_pattern.hashCode() : 0;
      result = 31 * result + (m_input != null ? m_input.hashCode() : 0);
      //noinspection NonFinalFieldReferencedInHashCode
      result = 31 * result + (m_proposals != null ? m_proposals.hashCode() : 0);
      return result;
    }
  }
}
