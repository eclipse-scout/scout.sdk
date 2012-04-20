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
package org.eclipse.scout.sdk.ui.fields.bundletree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * <h3>BundleTree</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 29.01.2010
 */
public class CheckableTree extends Composite {
  public static final int TYPE_ROOT = -1;
  private static final int TEXT_MARGIN = 2;

  private final ITreeNode m_rootNode;
  private final HashMap<ITreeNode, Rectangle> m_checkableNodeBounds = new HashMap<ITreeNode, Rectangle>();
  private final HashSet<ITreeNode> m_checkedNodes = new HashSet<ITreeNode>();
  private final Image m_imgCheckboxYes = ScoutSdkUi.getImage(ScoutSdkUi.CheckboxYes);
  private final Image m_imgCheckboxNo = ScoutSdkUi.getImage(ScoutSdkUi.CheckboxNo);
  private final Image m_imgCheckboxYesDisabled = ScoutSdkUi.getImage(ScoutSdkUi.CheckboxYesDisabled);
  private final Image m_imgCheckboxNoDisabled = ScoutSdkUi.getImage(ScoutSdkUi.CheckboxNoDisabled);
  private final EventListenerList m_eventListeners = new EventListenerList();
  private final ArrayList<ITreeNodeFilter> m_filters = new ArrayList<ITreeNodeFilter>();
  private final HashMap<ImageDescriptor, Image> m_icons = new HashMap<ImageDescriptor, Image>();

  private Tree m_tree;
  private TreeViewer m_viewer;
  private P_TreePaintListener m_paintListener;

  /**
   * @param parent
   * @param style
   */
  public CheckableTree(Composite parent, ITreeNode rootNode) {
    super(parent, SWT.NONE);
    m_rootNode = rootNode;
    m_filters.add(NodeFilters.getVisible());
    setLayout(new FillLayout());
    createControl(this);
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        for (Image img : m_icons.values()) {
          if (img != null && !img.isDisposed()) {
            img.dispose();
          }
        }
        m_icons.clear();
      }
    });
  }

  protected void createControl(Composite parent) {
    m_tree = new Tree(parent, SWT.BORDER);
    m_viewer = new TreeViewer(m_tree);
    m_viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
    installListeners();
    P_TreeModel model = new P_TreeModel();
    m_viewer.setContentProvider(model);
    m_viewer.setLabelProvider(model);
    m_viewer.setInput(model);
  }

  protected void installListeners() {
    P_CheckboxListener checkboxListener = new P_CheckboxListener();
    m_tree.addListener(SWT.MouseDown, checkboxListener);
    m_tree.addListener(SWT.MouseUp, checkboxListener);
    if (m_paintListener == null) {
      m_paintListener = new P_TreePaintListener();
      m_tree.addListener(SWT.MeasureItem, m_paintListener);
      m_tree.addListener(SWT.EraseItem, m_paintListener);
      m_tree.addListener(SWT.PaintItem, m_paintListener);
    }

    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        if (event.getSelection() != null && event.getSelection() instanceof IStructuredSelection) {
          IStructuredSelection selection = (IStructuredSelection) event.getSelection();
          if (selection.size() == 1) {
            ITreeNode node = (ITreeNode) selection.getFirstElement();
            if (node.isEnabled()) {
              invertCheckStateFromUi(node);
            }
          }
        }
      }
    });
    m_tree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == ' ') {
          IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
          if (selection.size() == 1) {
            ITreeNode node = (ITreeNode) selection.getFirstElement();
            if (node.isEnabled()) {
              invertCheckStateFromUi(node);
            }
          }
        }
      }
    });
    // dnd
    int operations = DND.DROP_MOVE | DND.DROP_COPY;
    DragSource source = new DragSource(m_tree, operations);
    Transfer[] types = new Transfer[]{LocalSelectionTransfer.getTransfer()};
    source.setTransfer(types);
    source.addDragListener(new P_DragSourceListener());

    operations = DND.DROP_MOVE | DND.DROP_COPY;
    P_DropTargetListener adapter = new P_DropTargetListener(m_viewer);
    adapter.setExpandEnabled(false);
    types = new Transfer[]{LocalSelectionTransfer.getTransfer()};
    m_viewer.addDropSupport(operations, types, adapter);
  }

  public void addDndListener(ITreeDndListener listener) {
    m_eventListeners.add(ITreeDndListener.class, listener);
  }

  public void removeDndListener(ITreeDndListener listener) {
    m_eventListeners.remove(ITreeDndListener.class, listener);
  }

  public void addCheckSelectionListener(ICheckStateListener listener) {
    m_eventListeners.add(ICheckStateListener.class, listener);
  }

  public void removeCheckSelectionListener(ICheckStateListener listener) {
    m_eventListeners.remove(ICheckStateListener.class, listener);
  }

  protected void fireNodeCheckstateChanged(ITreeNode node, boolean checked) {
    for (ICheckStateListener l : m_eventListeners.getListeners(ICheckStateListener.class)) {
      l.fireNodeCheckStateChanged(node, checked);
    }
  }

  public void addTreeNodeFilter(ITreeNodeFilter filter) {
    m_filters.add(filter);
  }

  public boolean removeTreeNodeFilter(ITreeNodeFilter filter) {
    return m_filters.remove(filter);
  }

  public ITreeNodeFilter[] getTreeNodeFilters() {
    return m_filters.toArray(new ITreeNodeFilter[m_filters.size()]);
  }

  public TreeViewer getTreeViewer() {
    return m_viewer;
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    Point computeSize = super.computeSize(hint, hint2, changed);
    computeSize.y = (int) (computeSize.y * 1.2);
    return computeSize;
  }

  /**
   * @return the rootNode
   */
  public ITreeNode getRootNode() {
    return m_rootNode;
  }

  public boolean isChecked(ITreeNode node) {
    return m_checkedNodes.contains(node);
  }

  public ITreeNode[] getCheckedNodes() {
    return m_checkedNodes.toArray(new ITreeNode[m_checkedNodes.size()]);
  }

  protected void invertCheckStateFromUi(ITreeNode node) {
    setChecked(node, !isChecked(node));
  }

  public void setChecked(ITreeNode[] nodes) {
    ArrayList<ITreeNode> nodesToCheck = new ArrayList<ITreeNode>(Arrays.asList(nodes));
    ArrayList<ITreeNode> checkedNodes = new ArrayList<ITreeNode>(m_checkedNodes);
    // remove already checked
    for (Iterator<ITreeNode> it = checkedNodes.iterator(); it.hasNext();) {
      ITreeNode n = it.next();
      if (nodesToCheck.remove(n)) {
        it.remove();
      }
    }
    // uncheck other
    for (ITreeNode n : checkedNodes) {
      setChecked(n, false);
    }
    // check new
    for (ITreeNode n : nodesToCheck) {
      setChecked(n, true);
    }
  }

  public boolean setChecked(ITreeNode node, boolean checked) {
    boolean result = false;
    if (checked) {
      result = m_checkedNodes.add(node);
    }
    else {
      result = m_checkedNodes.remove(node);
    }
    if (result) {
      fireNodeCheckstateChanged(node, checked);
      m_tree.redraw();
    }
    return result;
  }

  public ITreeNode getParent(ITreeNode childNode) {
    return childNode.getParent();
  }

  public ITreeNode[] getChildren(ITreeNode parentNode) {
    return sortChildren(parentNode.getChildren(NodeFilters.getCombinedFilter(getTreeNodeFilters())));
  }

  protected ITreeNode[] sortChildren(ITreeNode[] children) {
    TreeMap<CompositeObject, ITreeNode> nodes = new TreeMap<CompositeObject, ITreeNode>();
    for (ITreeNode n : children) {
      nodes.put(new CompositeObject(new Long(n.getOrderNr()), n.getText(), n), n);
    }
    return nodes.values().toArray(new ITreeNode[nodes.values().size()]);
  }

  public class P_TreeModel extends LabelProvider implements ITreeContentProvider, IFontProvider {

    private Font m_boldFont;

    @Override
    public Object[] getElements(Object inputElement) {
      if (getRootNode().isVisible()) {
        return new Object[]{getRootNode()};
      }
      else {
        return CheckableTree.this.getChildren(getRootNode());
      }
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      return CheckableTree.this.getChildren((ITreeNode) parentElement);
    }

    @Override
    public Object getParent(Object element) {
      return CheckableTree.this.getParent((ITreeNode) element);
    }

    @Override
    public boolean hasChildren(Object element) {
      return CheckableTree.this.getChildren((ITreeNode) element).length > 0;
    }

    @Override
    public void dispose() {
      if (m_boldFont != null && !m_boldFont.isDisposed()) {
        m_boldFont.dispose();
      }
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public String getText(Object element) {
      return ((ITreeNode) element).getText();
    }

    @Override
    public Font getFont(Object element) {
      if (((ITreeNode) element).isBold()) {
        if (m_boldFont == null) {
          Font originalFont = m_tree.getFont();
          FontData fontData[] = originalFont.getFontData();
          // Adding the bold attribute
          for (int i = 0; i < fontData.length; i++) {
            fontData[i].setStyle(fontData[i].getStyle() | SWT.BOLD);
          }
          m_boldFont = new Font(m_tree.getDisplay(), fontData);
        }
        return m_boldFont;
      }
      return null;
    }

  } // end class P_TreeModel

  public static class P_DependentFilter implements IScoutBundleFilter {
    private final int m_bundleType;

    public P_DependentFilter(int bundleType) {
      m_bundleType = bundleType;
    }

    @Override
    public boolean accept(IScoutBundle bundle) {
      if (m_bundleType == bundle.getType()) {
        return true;
      }
      return false;
    }
  } // end class P_DependentFilter

  private class P_TreePaintListener implements Listener {

    @Override
    public void handleEvent(Event event) {
      TreeItem item = (TreeItem) event.item;
      ITreeNode node = (ITreeNode) item.getData();
      if (node == null) {
        return;
      }
      switch (event.type) {
        case SWT.MeasureItem: {
          Point size = new Point(2 * TEXT_MARGIN, 2 * TEXT_MARGIN);
          if (node.getImage() != null) {
            Image img = m_icons.get(node.getImage());
            if (img == null) {
              img = node.getImage().createImage();
              m_icons.put(node.getImage(), img);
            }
            size.x += (img.getBounds().width + TEXT_MARGIN);
            size.y = Math.max(size.y, 2 * TEXT_MARGIN + img.getBounds().height);
          }
          if (node.isCheckable()) {
            size.x += (16 + TEXT_MARGIN);
            size.y = Math.max(size.y, 2 * TEXT_MARGIN + 16);
          }

          String text = item.getText(event.index);
          Point textSize = event.gc.textExtent(text);
          size.x += (textSize.x + TEXT_MARGIN);
          size.y = Math.max(size.y, textSize.y + 2 * TEXT_MARGIN);

          event.width = size.x;
          event.height = size.y;
          break;
        }
        case SWT.PaintItem: {
          int x = event.x + TEXT_MARGIN;
          int y = event.y;

          if (node.isCheckable()) {
            Image img = m_imgCheckboxNo;
            if (node.isEnabled()) {
              if (isChecked(node)) {
                img = m_imgCheckboxYes;
              }
              else {
                img = m_imgCheckboxNo;
              }
            }
            else {
              if (isChecked(node)) {
                img = m_imgCheckboxYesDisabled;
              }
              else {
                img = m_imgCheckboxNoDisabled;
              }
            }

            Rectangle bounds = m_checkableNodeBounds.get(node);
            if (bounds == null) {
              bounds = new Rectangle(0, 0, 0, 0);
            }
            bounds.height = img.getBounds().height + 2 * TEXT_MARGIN;
            bounds.width = img.getBounds().width + 2 * TEXT_MARGIN;
            bounds.x = x - TEXT_MARGIN;
            bounds.y = y;
            m_checkableNodeBounds.put(node, bounds);
            event.gc.drawImage(img, x, y + TEXT_MARGIN);
            x += (img.getBounds().width + TEXT_MARGIN);
          }

          if (node.getImage() != null) {
            Image img = m_icons.get(node.getImage());
            if (img == null) {
              img = node.getImage().createImage();
              m_icons.put(node.getImage(), img);
            }
            event.gc.drawImage(img, x, y + TEXT_MARGIN);
            x += (img.getBounds().width + TEXT_MARGIN);
          }
          String text = item.getText(event.index);
          /* center column 1 vertically */
          int yOffset = 0;
          Point size = event.gc.textExtent(text);
          yOffset = Math.max(TEXT_MARGIN, (event.height - size.y) / 2);
          event.gc.drawText(text, x, y + yOffset, true);
          break;
        }
        case SWT.EraseItem: {
          event.detail &= ~SWT.FOREGROUND;
          break;
        }

      }
    }
  } // end class P_TreeListener

  private class P_CheckboxListener implements Listener {
    private Point m_mousedownPosition = null;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MouseDown: {
          m_mousedownPosition = new Point(event.x, event.y);
          break;
        }
        case SWT.MouseUp: {
          Point point = new Point(event.x, event.y);
          if (m_mousedownPosition != null && Math.abs(point.x - m_mousedownPosition.x) < 5 && Math.abs(point.y - m_mousedownPosition.y) < 5) {
            for (Entry<ITreeNode, Rectangle> e : m_checkableNodeBounds.entrySet()) {
              if (e.getValue().contains(point) && e.getKey().isEnabled()) {
                invertCheckStateFromUi(e.getKey());
              }
            }
          }
          break;

        }
      }
    }
  } // end class P_CheckboxListener

  private class P_DragSourceListener implements DragSourceListener {
    @Override
    public void dragStart(DragSourceEvent event) {
      StructuredSelection selection = (StructuredSelection) m_viewer.getSelection();
      if (selection.isEmpty()) {
        event.doit = false;
      }
      else {
        if ((event.detail & DND.DROP_COPY) != 0) {
          event.doit = false;
          return;
        }
        for (ITreeDndListener l : m_eventListeners.getListeners(ITreeDndListener.class)) {
          if (!l.isDragableNode((ITreeNode) selection.getFirstElement())) {
            event.doit = false;
            break;
          }
        }
      }
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
      LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
      if (transfer.isSupportedType(event.dataType)) {
        transfer.setSelection(m_viewer.getSelection());
        event.data = transfer;
      }
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
      m_viewer.refresh();
      m_viewer.expandAll();
    }
  } // end class P_DragSourceListener

  private class P_DropTargetListener extends ViewerDropAdapter {
    protected P_DropTargetListener(Viewer viewer) {
      super(viewer);
    }

    @Override
    protected ITreeNode getSelectedObject() {
      return (ITreeNode) super.getSelectedObject();
    }

    private int m_lastOperation = 0;

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
      if ((operation & DND.DROP_COPY) != 0) {
        m_lastOperation = DND.DROP_COPY;
      }
      else if ((operation & DND.DROP_MOVE) != 0) {
        m_lastOperation = DND.DROP_MOVE;
      }
      return isValid(getSelectedObject().getParent(), (ITreeNode) target, getSelectedObject(), m_lastOperation);
    }

    private boolean isValid(ITreeNode source, ITreeNode destination, ITreeNode node, int operation) {
      DndEvent dndEvent = new DndEvent(CheckableTree.this);
      dndEvent.doit = false;
      dndEvent.node = node;
      dndEvent.sourceParent = source;
      dndEvent.targetParent = destination;
      dndEvent.operation = m_lastOperation;
      for (ITreeDndListener l : m_eventListeners.getListeners(ITreeDndListener.class)) {
        l.validateTarget(dndEvent);
      }
      return dndEvent.doit;

    }

    @Override
    public boolean performDrop(Object data) {
      ITreeNode selectedNode = getSelectedObject();
      ITreeNode newParentNode = (ITreeNode) getCurrentTarget();
      ITreeNode oldParent = selectedNode.getParent();
      if ((m_lastOperation & DND.DROP_MOVE) != 0) {
        oldParent.removeChild(selectedNode);
        selectedNode.setParent(null);
      }
      else if ((m_lastOperation & DND.DROP_COPY) != 0) {
        if (selectedNode.getChildren().size() > 0) {
          throw new IllegalStateException("in case of copy a node can not have children.");
        }
        selectedNode = new TreeNode(selectedNode);
      }
      if (newParentNode.getChildren(NodeFilters.getByType(selectedNode.getType())).length == 0) {
        newParentNode.addChild(selectedNode);
        selectedNode.setParent(newParentNode);
      }

      DndEvent dndEvent = new DndEvent(CheckableTree.this);
      dndEvent.node = getSelectedObject();
      dndEvent.sourceParent = dndEvent.node.getParent();
      dndEvent.targetParent = newParentNode;
      dndEvent.doit = true;
      for (ITreeDndListener l : m_eventListeners.getListeners(ITreeDndListener.class)) {
        l.dndPerformed(dndEvent);
      }
      return true;
    }

    @Override
    public void dragOver(DropTargetEvent event) {
      super.dragOver(event);
      if (!isValid(getSelectedObject().getParent(), (ITreeNode) getCurrentTarget(), getSelectedObject(), m_lastOperation)) {
        event.feedback &= ~DND.FEEDBACK_SELECT;
      }
    }

    @Override
    protected int determineLocation(DropTargetEvent event) {
      if (isValid(getSelectedObject().getParent(), (ITreeNode) getCurrentTarget(), getSelectedObject(), m_lastOperation)) {
        return LOCATION_ON;
      }
      else {
        return LOCATION_NONE;
      }
    }

  } // end class P_DropTargetListener
}
