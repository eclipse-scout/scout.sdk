package org.eclipse.scout.sdk.ui.internal.extensions.technology;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.dialog.CheckableTreeSelectionDialog;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class Technology implements Comparable<Technology> {

  private final int TREE_TYPE_BUNDLE = 1;
  private final int TREE_TYPE_RESOURCE = 2;

  private final String m_id, m_name, m_category;
  private final ArrayList<IScoutTechnologyHandler> m_handlers;
  private final EventListenerList m_eventListeners;

  public Technology(String id, String name, String category) {
    m_handlers = new ArrayList<IScoutTechnologyHandler>();
    m_eventListeners = new EventListenerList();
    m_id = id;
    m_name = name;
    m_category = category;
  }

  public void addSelectionChangedListener(ITechnologyListener listener) {
    m_eventListeners.add(ITechnologyListener.class, listener);
  }

  public void removeSelectionChangedListener(ITechnologyListener listener) {
    m_eventListeners.remove(ITechnologyListener.class, listener);
  }

  private void fireSelectionChanged(boolean newSelection) {
    for (ITechnologyListener l : m_eventListeners.getListeners(ITechnologyListener.class)) {
      try {
        l.handleSelectionChanged(newSelection);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("error during listener notification.", e);
      }
    }
  }

  public boolean setSelection(IScoutProject project, boolean selected) {
    // collect all resources from all handlers
    HashSet<IScoutTechnologyResource> allResources = new HashSet<IScoutTechnologyResource>();
    for (IScoutTechnologyHandler handler : getHandlers(project)) {
      IScoutTechnologyResource[] resources = handler.getModifactionResourceCandidates(project);
      if (resources != null && resources.length > 0) {
        for (IScoutTechnologyResource res : resources) {
          res.setHandler(handler);
          allResources.add(res);
        }
      }
    }

    // build resources tree
    ITreeNode changesTree = getModificationResourcesTree(allResources.toArray(new IScoutTechnologyResource[allResources.size()]));

    // create selection dialog
    final CheckableTreeSelectionDialog dialog = new CheckableTreeSelectionDialog(Display.getDefault().getActiveShell(), changesTree,
          Texts.get("ChangeTechnology"), Texts.get("TechnologyResourcesToModifyDesc"));
    dialog.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (CheckableTreeSelectionDialog.PROP_CHECKED_NODES.equals(evt.getPropertyName())) {
          boolean complete = false;
          if (evt.getNewValue() instanceof ITreeNode[]) {
            ITreeNode[] checkedNodes = (ITreeNode[]) evt.getNewValue();
            complete = checkedNodes != null && checkedNodes.length > 0;
          }
          dialog.setComplete(complete);
        }
      }
    });
    dialog.setCheckedNodes(getDefaultSelectedNodes(changesTree));

    if (dialog.open() == Dialog.OK) {
      ITreeNode[] selectedNodes = dialog.getCheckedNodes();
      if (selectedNodes != null && selectedNodes.length > 0) {
        OperationJob job = new OperationJob(new P_ChangeSelectionOperation(selectedNodes, selected));
        job.schedule();
      }
      return true;
    }
    return false;
  }

  private ITreeNode[] getDefaultSelectedNodes(ITreeNode root) {
    HashSet<ITreeNode> ret = new HashSet<ITreeNode>();
    collectDefaultSelection(root, ret);
    return ret.toArray(new ITreeNode[ret.size()]);
  }

  private void collectDefaultSelection(ITreeNode node, Set<ITreeNode> collector) {
    if (node.isVisible() && node.isCheckable()) {
      if (node.getData() instanceof IScoutTechnologyResource) {
        IScoutTechnologyResource res = (IScoutTechnologyResource) node.getData();
        if (res.getDefaultSelection()) {
          collector.add(node);
        }
      }
    }
    for (ITreeNode child : node.getChildren()) {
      collectDefaultSelection(child, collector);
    }
  }

  private ITreeNode getModificationResourcesTree(IScoutTechnologyResource[] resources) {
    HashMap<String, ArrayList<IScoutTechnologyResource>> mapping = new HashMap<String, ArrayList<IScoutTechnologyResource>>();
    for (IScoutTechnologyResource res : resources) {
      ArrayList<IScoutTechnologyResource> list = mapping.get(res.getBundle().getProject().getName());
      if (list == null) {
        list = new ArrayList<IScoutTechnologyResource>();
        mapping.put(res.getBundle().getProject().getName(), list);
      }
      list.add(res);
    }

    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);

    for (Entry<String, ArrayList<IScoutTechnologyResource>> bundle : mapping.entrySet()) {
      if (bundle.getValue().size() > 0) {
        ImageDescriptor bundleImage = bundle.getValue().get(0).getBundleImage();
        ITreeNode bundleNode = createNode(rootNode, TREE_TYPE_BUNDLE, bundle.getKey(), true, false, bundleImage, null);
        for (IScoutTechnologyResource res : bundle.getValue()) {
          if (res.getResource().exists()) {

            IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) res.getResource().getAdapter(IWorkbenchAdapter.class);
            ImageDescriptor imageDescriptor = null;
            if (wbAdapter != null) {
              imageDescriptor = wbAdapter.getImageDescriptor(res.getResource());
            }

            createNode(bundleNode, TREE_TYPE_RESOURCE, res.getResource().getName(), false, true, imageDescriptor, res);
          }
        }
      }
    }

    return rootNode;
  }

  private ITreeNode createNode(ITreeNode parent, int type, String text, boolean bold, boolean checkable, ImageDescriptor img, IScoutTechnologyResource data) {
    TreeNode node = new TreeNode(type, text, data);
    node.setCheckable(checkable);
    node.setBold(bold);
    if (img != null) {
      node.setImage(img);
    }

    parent.addChild(node);
    node.setParent(parent);
    return node;
  }

  private List<IScoutTechnologyHandler> getHandlers(IScoutProject project) {
    ArrayList<IScoutTechnologyHandler> ret = new ArrayList<IScoutTechnologyHandler>(m_handlers.size());
    for (IScoutTechnologyHandler h : m_handlers) {
      if (h.isActive(project)) {
        ret.add(h);
      }
    }
    return ret;
  }

  public boolean isActive(IScoutProject project) {
    return getHandlers(project).size() > 0;
  }

  public TriState getSelection(IScoutProject project) {
    List<IScoutTechnologyHandler> handlers = getHandlers(project);
    if (handlers.size() == 0) {
      throw new InvalidParameterException("At least one handler must be defined for a technology");
    }

    TriState ret = handlers.get(0).getSelection(project);
    for (int i = 1; i < handlers.size(); i++) {
      if (ret != handlers.get(i).getSelection(project)) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  public String getName() {
    return m_name;
  }

  public String getId() {
    return m_id;
  }

  public String getCategory() {
    return m_category;
  }

  public void addAllHandlers(Collection<IScoutTechnologyHandler> c) {
    if (c == null || c.size() == 0) return;
    m_handlers.addAll(c);
  }

  @Override
  public int compareTo(Technology o) {
    int ret = m_category.compareTo(o.m_category);
    if (ret == 0) {
      ret = m_name.compareTo(o.m_name);
      if (ret == 0) {
        return m_id.compareTo(o.m_id);
      }
      else {
        return ret;
      }
    }
    else {
      return ret;
    }
  }

  private class P_ChangeSelectionOperation implements IOperation {
    private final ITreeNode[] m_selectedNodes;
    private final boolean m_newSelection;

    private P_ChangeSelectionOperation(ITreeNode[] selectedNodes, boolean newSelection) {
      m_selectedNodes = selectedNodes;
      m_newSelection = newSelection;
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      // map the checked resources to the contributing handler
      HashMap<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> resourcesToModify = new HashMap<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>>();
      for (ITreeNode selectedNode : m_selectedNodes) {
        if (selectedNode.isVisible() && selectedNode.getData() instanceof IScoutTechnologyResource) {
          IScoutTechnologyResource res = (IScoutTechnologyResource) selectedNode.getData();
          HashSet<IScoutTechnologyResource> set = resourcesToModify.get(res.getHandler());
          if (set == null) {
            set = new HashSet<IScoutTechnologyResource>();
            resourcesToModify.put(res.getHandler(), set);
          }
          set.add(res);
        }
      }

      // fire pre-selection changed for all checked resources (each handler only gets the resources that were contributed by itself)
      for (Entry<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> entry : resourcesToModify.entrySet()) {
        try {
          if (!entry.getKey().preSelectionChanged(m_newSelection, monitor)) {
            return; // cancel the execution if a handler requests an abort
          }
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("Error while preparing technology changes.", e);
        }
      }

      // fire selection changed for all checked resources (each handler only gets the resources that were contributed by itself)
      for (Entry<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> entry : resourcesToModify.entrySet()) {
        try {
          entry.getKey().selectionChanged(entry.getValue().toArray(new IScoutTechnologyResource[entry.getValue().size()]), m_newSelection, monitor, workingCopyManager);
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("Error while applying technology changes.", e);
        }
      }

      // fire post-selection changed for all checked resources (each handler only gets the resources that were contributed by itself)
      for (Entry<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> entry : resourcesToModify.entrySet()) {
        try {
          entry.getKey().postSelectionChanged(m_newSelection, monitor);
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("Error while finishing technology changes.", e);
        }
      }
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          fireSelectionChanged(m_newSelection);
        }
      });
    }

    @Override
    public String getOperationName() {
      return "change technology selection...";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }
  }
}
