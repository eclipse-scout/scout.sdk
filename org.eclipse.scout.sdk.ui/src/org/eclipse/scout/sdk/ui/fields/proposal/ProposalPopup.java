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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ProposalPopup extends Window {

  private static final int POPUP_OFFSET = 2;
  /*
   * The character height hint for the popup. May be overridden by using
   * setInitialPopupSize.
   */
  private static final int POPUP_CHAR_HEIGHT = 10;

  /*
   * The minimum pixel width for the popup. May be overridden by using
   * setInitialPopupSize.
   */
  private static final int POPUP_MINIMUM_WIDTH = 300;

  private TableViewer m_tableViewer;
  private Label m_itemCountLabel;
  private Button m_showAllProposals;
  private Control m_proposalField;
  private Point popupSize;
  private IContentProposalEx[] m_proposals;
  private boolean m_ignoreClose;
  private List<IProposalPopupListener> m_selectionListeners = new ArrayList<IProposalPopupListener>();
  // private int m_selectionIndex=-1;
  private boolean m_expertModusChecked;
  private final boolean m_supportsEpertModus;

  private IProposalDescriptionProvider m_proposalDescriptionProvider;
  private ScrolledComposite m_proposalDescriptionArea;

  public ProposalPopup(Control proposalField, boolean supportsExpertModus, boolean expertModusChecked) {
    super(proposalField.getShell());
    m_proposalField = proposalField;
    m_supportsEpertModus = supportsExpertModus;
    m_expertModusChecked = expertModusChecked;
    setShellStyle(SWT.RESIZE | SWT.ON_TOP);
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
    m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        IContentProposalEx selectedProposal = null;
        if (!selection.isEmpty() && selection.getFirstElement() instanceof IContentProposalEx) {
          selectedProposal = (IContentProposalEx) selection.getFirstElement();
        }
        // update description
        updateDescription(selectedProposal);
        // selectProposal((int)selection.getFirstElement());
        ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_SELECTED);
        delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selectedProposal);
        firePopupEvent(delegateEvent);

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
        // TODO Auto-generated method stub
        super.keyReleased(e);
      }
    });
    table.setHeaderVisible(false);

    m_proposalDescriptionArea = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    m_proposalDescriptionArea.setBackground(m_proposalDescriptionArea.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    m_proposalDescriptionArea.setExpandHorizontal(true);
    m_proposalDescriptionArea.setExpandVertical(true);
//    m_proposalDescriptionArea.addControlListener(new ControlAdapter() {
//      @Override
//      public void controlResized(ControlEvent e) {
//        Rectangle r = m_proposalDescriptionArea.getClientArea();
//        Point minSize = new Point(0,0);
//        if(m_proposalDescriptionArea.getContent() != null)
//        m_proposalDescriptionArea.setMinSize(m_proposalDescriptionArea.getContent().computeSize(r.width, SWT.DEFAULT));
//      }
//    });
//    m_proposalDescriptionArea.setBackground(m_proposalDescriptionArea.getDisplay().getSystemColor(SWT.COLOR_RED));

    Group group = new Group(proposalArea, SWT.SHADOW_ETCHED_OUT);
    group.setBackground(group.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    group.setForeground(group.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    m_itemCountLabel = new Label(group, SWT.NONE);
    m_itemCountLabel.setBackground(m_itemCountLabel.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_showAllProposals = new Button(group, SWT.CHECK);
    m_showAllProposals.setBackground(m_showAllProposals.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_showAllProposals.setText(Texts.get("ProposalField_checkBox_showAllProposals"));
    m_showAllProposals.setEnabled(m_supportsEpertModus);
    m_showAllProposals.setSelection(m_expertModusChecked);

    m_showAllProposals.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ProposalPopupEvent event = new ProposalPopupEvent(ProposalPopupEvent.TYPE_SEARCH_SHORTENED);
        event.setData(ProposalPopupEvent.IDENTIFIER_SELECTION_SEARCH_SHORTENED, new Boolean(m_showAllProposals.getSelection()));
        firePopupEvent(event);
      }
    });
    // layout
    GridLayout parentLayout = new GridLayout(1, true);
    parentLayout.horizontalSpacing = 0;
    parentLayout.marginHeight = 0;
    parentLayout.marginWidth = 0;
    parentLayout.verticalSpacing = 0;
    parent.setLayout(parentLayout);
    GridData proposalAreaData = new GridData(GridData.FILL_BOTH);
    proposalAreaData.heightHint = POPUP_MINIMUM_WIDTH;
    proposalAreaData.widthHint = POPUP_MINIMUM_WIDTH;
    proposalArea.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridData proposalDescriptionData = new GridData(GridData.FILL_BOTH);
    proposalDescriptionData.heightHint = POPUP_MINIMUM_WIDTH;
    proposalDescriptionData.widthHint = POPUP_MINIMUM_WIDTH;
    proposalDescriptionData.exclude = true;
    m_proposalDescriptionArea.setLayoutData(proposalDescriptionData);

//    m_proposalDescriptionArea.setLayout(new FillLayout());

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
    gd = new GridData(GridData.END);
    m_showAllProposals.setLayoutData(gd);

    return parent;
  }

  public void setProposals(IContentProposalEx[] proposals) {

    // m_selectionIndex=-1;
    if (proposals == null) {
      proposals = new IContentProposalEx[0];
    }
    m_proposals = proposals;
    try {
      m_tableViewer.getTable().setRedraw(false);
      m_tableViewer.getTable().setItemCount(proposals.length);
      TableItem[] items = m_tableViewer.getTable().getItems();
      for (int i = 0; i < items.length; i++) {
        TableItem item = items[i];
        IContentProposalEx proposal = proposals[i];
        int selectionIndex = m_tableViewer.getTable().getSelectionIndex();
        boolean selected = i == selectionIndex;
        item.setText(getText(proposal, selected));
        item.setImage(getImage(proposal, selected));
        item.setData(proposal);
      }
    }
    finally {
      m_tableViewer.getTable().setRedraw(true);

    }
    // Default to the first selection if there is no selection. This is the case, every time the popup is opened.
    if (m_tableViewer.getSelection().isEmpty()) {
      Object proposal = m_tableViewer.getElementAt(0);
      m_tableViewer.setSelection(new StructuredSelection(proposal));
//      m_tableViewer.getTable().select(0);
    }
    m_itemCountLabel.setText(proposals.length + " items found");
    updateDescription(getSelectedProposal());
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

    // provide a hook for adjusting the bounds. This is only
    // necessary when there is content driven sizing that must be
    // adjusted each time the dialog is opened.
    adjustBounds();

    // limit the shell size to the display size
    constrainShellSize();

    shell.setVisible(true);
    return OK;
  }

  private void updateDescription(IContentProposalEx proposal) {
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
      adjustBounds();
//      m_proposalDescriptionArea.setExpandHorizontal(true);
//      m_proposalDescriptionArea.setExpandVertical(true);
    }
  }

  public void setIgnoreClose(boolean ignore) {
    m_ignoreClose = ignore;
  }

  public void setProposalDescriptionProvider(IProposalDescriptionProvider proposalDescriptionProvider) {
    m_proposalDescriptionProvider = proposalDescriptionProvider;
  }

  public IProposalDescriptionProvider getProposalDescriptionProvider() {
    return m_proposalDescriptionProvider;
  }

  @Override
  public boolean close() {
    if (m_ignoreClose) {
      return false;
    }
    else {
      firePopupEvent(new ProposalPopupEvent(ProposalPopupEvent.TYPE_POPUP_CLOSED));
      return super.close();
    }
  }

  public boolean isFocusOwner() {
    Control focusControl = getShell().getDisplay().getFocusControl();
    return (focusControl != null && focusControl.getShell() == getShell());
  }

  public void addPopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.add(proposalSelectionListener);
  }

  public void removePopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.remove(proposalSelectionListener);
  }

  public void setExpertMode(boolean searchExpertMode) {
    m_expertModusChecked = searchExpertMode;
    m_showAllProposals.setSelection(searchExpertMode);
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

  IContentProposalEx getSelectedProposal() {
    IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
    if (!selection.isEmpty()) {
      // ProposalPopupEvent delegateEvent=new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
      // delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
      // firePopupEvent(delegateEvent);
      return (IContentProposalEx) selection.getFirstElement();
    }
    return null;
  }

  private String getText(IContentProposalEx proposal, boolean selected) {
    return proposal.getLabel(selected, m_expertModusChecked);
  }

  private Image getImage(IContentProposalEx proposal, boolean selected) {
    return proposal.getImage(selected, m_expertModusChecked);
  }

  boolean setFocus() {
    return m_tableViewer.getTable().setFocus();
  }

  private void adjustBounds() {
    // Get our control's location in display coordinates.
//    FormData data = (FormData) m_tableViewer.getTable().getLayoutData();
//    data.height = m_tableViewer.getTable().getItemHeight() * POPUP_CHAR_HEIGHT;
//    data.width = Math.max(m_proposalField.getSize().x, POPUP_MINIMUM_WIDTH);
//    m_tableViewer.getTable().setLayoutData(data);
    Point location = m_proposalField.getDisplay().map(m_proposalField.getParent(), null, m_proposalField.getLocation());
    int initialX = location.x + POPUP_OFFSET;
    int initialY = location.y + m_proposalField.getSize().y + POPUP_OFFSET;

    // If there is no specified size, force it by setting
    // up a layout on the table.
    Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    if (((GridData) m_proposalDescriptionArea.getLayoutData()).exclude) {
      size.x = POPUP_MINIMUM_WIDTH;
    }
    else {
      size.x = POPUP_MINIMUM_WIDTH * 2;
    }
    size.y = POPUP_MINIMUM_WIDTH;
    getShell().setBounds(initialX, initialY, size.x, size.y);
//    if (popupSize == null) {
//    FormData data = (FormData) m_tableViewer.getTable().getLayoutData();
//    data.height = m_tableViewer.getTable().getItemHeight() * POPUP_CHAR_HEIGHT;
//    data.width = Math.max(m_proposalField.getSize().x, POPUP_MINIMUM_WIDTH);
//    m_tableViewer.getTable().setLayoutData(data);
//      getShell().pack();
//      popupSize = getShell().getSize();
//    }
//    getShell().setBounds(initialX, initialY, popupSize.x, popupSize.y);

  }

}
