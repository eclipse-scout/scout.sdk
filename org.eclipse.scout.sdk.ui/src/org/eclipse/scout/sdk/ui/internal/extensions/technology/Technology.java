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
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CollectionUtility;
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
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class Technology implements Comparable<Technology> {

  private static final String TREE_TYPE_BUNDLE = "bundle";
  private static final String TREE_TYPE_RESOURCE = "resource";

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
        l.selectionChangeCompleted(newSelection);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("error during listener notification.", e);
      }
    }
  }

  public boolean setSelection(IScoutBundle project, boolean selected) throws CoreException {
    // collect all resources from all handlers
    HashSet<IScoutTechnologyResource> allResources = new HashSet<IScoutTechnologyResource>();
    for (IScoutTechnologyHandler handler : getHandlers(project)) {
      List<IScoutTechnologyResource> resources = handler.getModifactionResourceCandidates(project);
      if (resources.size() > 0) {
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
      ArrayList<IScoutTechnologyResource> list = mapping.get(res.getResource().getProject().getName());
      if (list == null) {
        list = new ArrayList<IScoutTechnologyResource>();
        mapping.put(res.getResource().getProject().getName(), list);
      }
      list.add(res);
    }

    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);

    for (Entry<String, ArrayList<IScoutTechnologyResource>> bundle : mapping.entrySet()) {
      if (bundle.getValue().size() > 0) {
        ImageDescriptor bundleImage = bundle.getValue().get(0).getBundleImage();
        ITreeNode bundleNode = TreeUtility.createNode(rootNode, TREE_TYPE_BUNDLE, bundle.getKey(), bundleImage, 0, null, true, false);
        for (IScoutTechnologyResource res : bundle.getValue()) {
          if (res.getResource().exists()) {

            IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) res.getResource().getAdapter(IWorkbenchAdapter.class);
            ImageDescriptor imageDescriptor = null;
            if (wbAdapter != null) {
              imageDescriptor = wbAdapter.getImageDescriptor(res.getResource());
            }

            TreeUtility.createNode(bundleNode, TREE_TYPE_RESOURCE, res.getResource().getName(), imageDescriptor, 0, res, false, true);
          }
        }
      }
    }

    return rootNode;
  }

  private List<IScoutTechnologyHandler> getHandlers(IScoutBundle project) {
    ArrayList<IScoutTechnologyHandler> ret = new ArrayList<IScoutTechnologyHandler>(m_handlers.size());
    for (IScoutTechnologyHandler h : m_handlers) {
      if (h.isActive(project)) {
        ret.add(h);
      }
    }
    return ret;
  }

  public boolean isActive(IScoutBundle project) {
    return getHandlers(project).size() > 0;
  }

  public TriState getSelection(IScoutBundle project) throws CoreException {
    List<IScoutTechnologyHandler> handlers = getHandlers(project);
    if (handlers.size() == 0) {
      throw new InvalidParameterException("At least one handler must be defined for a technology");
    }

    TriState ret = handlers.get(0).getSelection(project);
    for (int i = 1; i < handlers.size(); i++) {
      TriState selection = handlers.get(i).getSelection(project);
      if (selection != null) {
        if (ret == null) {
          ret = selection;
        }
        else if (ret != selection) {
          return TriState.UNDEFINED;
        }
      }
    }
    if (ret == null) {
      return TriState.UNDEFINED;
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
  public boolean equals(Object obj) {
    if (!(obj instanceof Technology)) {
      return false;
    }
    Technology other = (Technology) obj;
    return m_id.equals(other.m_id) && m_category.equals(other.m_category) && m_name.equals(other.m_name);
  }

  @Override
  public int hashCode() {
    int hash = m_id.hashCode();
    hash ^= m_category.hashCode();
    hash ^= m_name.hashCode();
    return hash;
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
    private boolean success;

    private P_ChangeSelectionOperation(ITreeNode[] selectedNodes, boolean newSelection) {
      m_selectedNodes = selectedNodes;
      m_newSelection = newSelection;
      success = false; // nothing done yet
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      try {

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

        monitor.beginTask(getOperationName(), resourcesToModify.size() * 3);

        // fire pre-selection changed for all checked resources (each handler only gets the resources that were contributed by itself)
        for (Entry<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> entry : resourcesToModify.entrySet()) {
          try {
            if (!entry.getKey().preSelectionChanged(CollectionUtility.hashSet(entry.getValue()), m_newSelection, new SubProgressMonitor(monitor, 1))) {
              return; // cancel the execution if a handler requests an abort
            }
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("Error while preparing technology changes.", e);
            return; // cancel further processing
          }
          monitor.worked(1);
        }

        // fire selection changed for all checked resources (each handler only gets the resources that were contributed by itself)
        for (Entry<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> entry : resourcesToModify.entrySet()) {
          try {
            entry.getKey().selectionChanged(CollectionUtility.hashSet(entry.getValue()), m_newSelection, new SubProgressMonitor(monitor, 1), workingCopyManager);
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("Error while applying technology changes.", e);
            return; // cancel further processing
          }
          monitor.worked(1);
        }

        // fire post-selection changed for all checked resources (each handler only gets the resources that were contributed by itself)
        for (Entry<IScoutTechnologyHandler, HashSet<IScoutTechnologyResource>> entry : resourcesToModify.entrySet()) {
          try {
            entry.getKey().postSelectionChanged(m_newSelection, new SubProgressMonitor(monitor, 1));
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("Error while finishing technology changes.", e);
            return; // cancel further processing
          }
          monitor.worked(1);
        }

        success = true;
      }
      finally {
        final boolean wasSuccessful = success;
        ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            // only change the selection if it was successful. otherwise the selection keeps the old value
            fireSelectionChanged(wasSuccessful == m_newSelection);
          }
        });
        monitor.done();
      }
    }

    @Override
    public String getOperationName() {
      return "Changing Technology Selection '" + getName() + "'...";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }
  }
}
