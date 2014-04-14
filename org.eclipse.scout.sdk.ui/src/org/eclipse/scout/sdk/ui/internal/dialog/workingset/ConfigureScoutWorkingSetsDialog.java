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
package org.eclipse.scout.sdk.ui.internal.dialog.workingset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link ConfigureScoutWorkingSetsDialog}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 04.04.2013
 */
public class ConfigureScoutWorkingSetsDialog extends TitleAreaDialog {

  private final OptimisticLock m_lock;

  private SashForm m_sashForm;
  private CheckboxTableViewer m_workingSetsViewer;
  private CheckableTree m_availableBundlesTree;
  private Button m_addWorkingSetButton;
  private Button m_removeWorkingSetButton;
  private Button m_renameWorkingSetButton;
  private Button m_workingSetUpButton;
  private Button m_workingSetDownButton;

  private String m_currentWorkingSet;
  private HashMap<String /* workingset name */, IAdaptable[] /*elements*/> m_selection;
  private HashSet<String /* workingset name */> m_initialSets;

  public ConfigureScoutWorkingSetsDialog(Shell parentShell) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
    m_lock = new OptimisticLock();
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Texts.get("ConfigureScoutWorkingSets"));
  }

  @Override
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    setTitle(Texts.get("ConfigureScoutWorkingSets"));
    setMessage(Texts.get("WorkingSetsMsg"));
    return c;
  }

  private String[] getAllWorkingSets() {
    return (String[]) m_workingSetsViewer.getInput();
  }

  private Set<String> getCheckedWorkingSets() {
    Object[] checked = m_workingSetsViewer.getCheckedElements();
    HashSet<String> checkedSets = new HashSet<String>(checked.length);
    for (Object o : checked) {
      checkedSets.add((String) o);
    }
    return checkedSets;
  }

  private IAdaptable[] getSelectionFor(String workingSet) {
    return m_selection.get(workingSet);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    m_sashForm = new SashForm(parent, SWT.HORIZONTAL);
    m_sashForm.setLayout(new GridLayout());
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    m_sashForm.setLayoutData(gd);

    createWorkingSetList(m_sashForm);
    createWorkingSetContent(m_sashForm);

    String[] sets = getAllWorkingSets();
    if (sets.length > 0) {
      m_workingSetsViewer.setSelection(new StructuredSelection(sets[0]), true);
      handleWorkingSetSelectionChanged(sets[0]);
    }
    else {
      handleWorkingSetSelectionChanged(null);
    }

    return m_sashForm;
  }

  private void handleWorkingSetSelectionChanged(String set) {
    try {
      m_lock.acquire();
      m_currentWorkingSet = set;
      if (set != null) {
        ITreeNode[] checked = TreeUtility.findNodes(m_availableBundlesTree.getRootNode(), NodeFilters.getByData((Object[]) getSelectionFor(set)));
        m_availableBundlesTree.setChecked(checked);
      }
      for (ITreeNode n : TreeUtility.findNodes(m_availableBundlesTree.getRootNode(), NodeFilters.getAcceptAll())) {
        if (n != m_availableBundlesTree.getRootNode()) {
          n.setVisible(set != null);
        }
      }
      m_availableBundlesTree.getTreeViewer().refresh();
      boolean isCustomWorkingSetSelected = set != null && !ScoutExplorerSettingsSupport.OTHER_PROJECTS_WORKING_SET_NAME.equals(set);
      if (m_removeWorkingSetButton != null && !m_removeWorkingSetButton.isDisposed()) {
        m_removeWorkingSetButton.setEnabled(isCustomWorkingSetSelected);
      }
      if (m_renameWorkingSetButton != null && !m_renameWorkingSetButton.isDisposed()) {
        m_renameWorkingSetButton.setEnabled(isCustomWorkingSetSelected);
      }

      String[] sets = getAllWorkingSets();
      if (m_workingSetDownButton != null && !m_workingSetDownButton.isDisposed()) {
        m_workingSetDownButton.setEnabled(set != null && sets.length > 0 && !set.equals(sets[sets.length - 1]));
      }
      if (m_workingSetUpButton != null && !m_workingSetUpButton.isDisposed()) {
        m_workingSetUpButton.setEnabled(set != null && sets.length > 0 && sets[0] != set);
      }
      if (m_availableBundlesTree != null && !m_availableBundlesTree.isDisposed()) {
        m_availableBundlesTree.setVisible(!ScoutExplorerSettingsSupport.OTHER_PROJECTS_WORKING_SET_NAME.equals(set));
      }
    }
    finally {
      m_lock.release();
    }
  }

  private void createWorkingSetList(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    // get list of all sets
    IWorkingSet[] scoutWorkingSets = ScoutExplorerSettingsSupport.get().getScoutWorkingSets(true);
    String[] workingSetNames = new String[scoutWorkingSets.length];
    m_selection = new HashMap<String, IAdaptable[]>();
    m_initialSets = new HashSet<String>(workingSetNames.length);
    for (int i = 0; i < workingSetNames.length; i++) {
      workingSetNames[i] = scoutWorkingSets[i].getLabel();
      m_selection.put(scoutWorkingSets[i].getLabel(), scoutWorkingSets[i].getElements());
      m_initialSets.add(workingSetNames[i]);
    }

    // get all checked sets
    IWorkingSet[] checkedSets = ScoutExplorerSettingsSupport.get().getScoutWorkingSets(false);
    String[] checkedWorkingSetNames = new String[checkedSets.length];
    for (int i = 0; i < checkedWorkingSetNames.length; i++) {
      checkedWorkingSetNames[i] = checkedSets[i].getLabel();
    }

    Label label = new Label(composite, SWT.NONE);
    label.setText(Texts.get("WorkingSets") + ":");
    m_workingSetsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.MULTI);
    m_workingSetsViewer.setContentProvider(new P_WorkingSetsContentProvider());
    m_workingSetsViewer.setLabelProvider(new P_WorkingSetsLabelProvider());
    m_workingSetsViewer.setComparator(null);
    m_workingSetsViewer.setInput(workingSetNames);
    m_workingSetsViewer.setCheckedElements(checkedWorkingSetNames);
    m_workingSetsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
          Object element = ((IStructuredSelection) selection).getFirstElement();
          if (element instanceof String) {
            handleWorkingSetSelectionChanged((String) element);
          }
        }
      }
    });

    Composite buttonContainer = new Composite(composite, SWT.NONE);
    Composite buttonContainerLeft = new Composite(buttonContainer, SWT.NONE);
    Composite buttonContainerRight = new Composite(buttonContainer, SWT.NONE);
    m_addWorkingSetButton = new Button(buttonContainerLeft, SWT.PUSH | SWT.FLAT);
    m_addWorkingSetButton.setText(Texts.get("New") + "...");
    m_addWorkingSetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        NewScoutWorkingSetDialog d = new NewScoutWorkingSetDialog(getShell(), getAllWorkingSets());
        if (d.open() == OK) {
          String[] sets = getAllWorkingSets();
          String[] newSet = new String[sets.length + 1];
          System.arraycopy(sets, 0, newSet, 0, sets.length);
          newSet[newSet.length - 1] = d.getWorkingSetName();
          m_workingSetsViewer.setInput(newSet);
          m_workingSetsViewer.setSelection(new StructuredSelection(d.getWorkingSetName()), true);
          handleWorkingSetSelectionChanged(d.getWorkingSetName());
          m_selection.put(d.getWorkingSetName(), new IAdaptable[]{});
          m_workingSetsViewer.setChecked(d.getWorkingSetName(), true);
        }
      }
    });
    m_removeWorkingSetButton = new Button(buttonContainerLeft, SWT.PUSH | SWT.FLAT);
    m_removeWorkingSetButton.setText(Texts.get("Remove"));
    m_removeWorkingSetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] sets = getAllWorkingSets();
        String[] newSet = new String[sets.length - 1];
        for (int i = 0; i < sets.length; i++) {
          if (CompareUtility.equals(sets[i], m_currentWorkingSet)) {
            System.arraycopy(sets, 0, newSet, 0, i);
            System.arraycopy(sets, i + 1, newSet, i, sets.length - i - 1);
            break;
          }
        }
        m_workingSetsViewer.setInput(newSet);
        if (newSet.length > 0) {
          m_workingSetsViewer.setSelection(new StructuredSelection(newSet[0]), true);
          handleWorkingSetSelectionChanged(newSet[0]);
        }
        else {
          m_workingSetsViewer.setSelection(null, false);
          handleWorkingSetSelectionChanged(null);
        }
      }
    });
    m_renameWorkingSetButton = new Button(buttonContainerLeft, SWT.PUSH | SWT.FLAT);
    m_renameWorkingSetButton.setText(Texts.get("RenameWithPopup"));
    m_renameWorkingSetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        NewScoutWorkingSetDialog d = new NewScoutWorkingSetDialog(getShell(), getAllWorkingSets()) {
          @Override
          protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Texts.get("RenameScoutWorkingSet"));
          }
        };
        d.setWorkingSetName(m_currentWorkingSet);
        if (d.open() == OK) {
          String[] sets = getAllWorkingSets();
          boolean isChecked = m_workingSetsViewer.getChecked(m_currentWorkingSet);
          for (int i = 0; i < sets.length; i++) {
            if (CompareUtility.equals(sets[i], m_currentWorkingSet)) {
              sets[i] = d.getWorkingSetName();
              break;
            }
          }
          m_selection.put(d.getWorkingSetName(), m_selection.remove(m_currentWorkingSet));
          m_workingSetsViewer.setInput(sets);
          m_workingSetsViewer.setSelection(new StructuredSelection(d.getWorkingSetName()), true);
          m_workingSetsViewer.setChecked(d.getWorkingSetName(), isChecked);
          handleWorkingSetSelectionChanged(d.getWorkingSetName());
        }
      }
    });

    m_workingSetUpButton = new Button(buttonContainerRight, SWT.PUSH | SWT.FLAT);
    m_workingSetUpButton.setText(Texts.get("Up"));
    m_workingSetUpButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] sets = getAllWorkingSets();
        String[] newSet = new String[sets.length];
        if (sets.length > 0) {
          for (int i = sets.length - 1; i >= 0; i--) {
            if (CompareUtility.equals(sets[i], m_currentWorkingSet)) {
              System.arraycopy(sets, 0, newSet, 0, i - 1);
              newSet[i] = sets[i - 1];
              newSet[i - 1] = sets[i];
              if (i < sets.length - 1) {
                System.arraycopy(sets, i + 1, newSet, i + 1, sets.length - i - 1);
              }
              break;
            }
          }
        }
        m_workingSetsViewer.setInput(newSet);
        m_workingSetsViewer.setSelection(new StructuredSelection(m_currentWorkingSet), true);
      }
    });
    m_workingSetDownButton = new Button(buttonContainerRight, SWT.PUSH | SWT.FLAT);
    m_workingSetDownButton.setText(Texts.get("Down"));
    m_workingSetDownButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] sets = getAllWorkingSets();
        String[] newSet = new String[sets.length];
        if (sets.length > 0) {
          for (int i = 0; i < sets.length; i++) {
            if (CompareUtility.equals(sets[i], m_currentWorkingSet)) {
              System.arraycopy(sets, 0, newSet, 0, i);
              newSet[i] = sets[i + 1];
              newSet[i + 1] = sets[i];
              System.arraycopy(sets, i + 2, newSet, i + 2, sets.length - i - 2);
              break;
            }
          }
        }
        m_workingSetsViewer.setInput(newSet);
        m_workingSetsViewer.setSelection(new StructuredSelection(m_currentWorkingSet), true);
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 8;
    layout.marginHeight = 8;
    composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(gd);

    GridLayout l = new GridLayout(2, true);
    l.verticalSpacing = 0;
    l.horizontalSpacing = 0;
    l.marginHeight = 0;
    l.marginWidth = 0;
    buttonContainer.setLayout(l);
    l = new GridLayout(3, false);
    l.verticalSpacing = 0;
    l.marginHeight = 0;
    l.marginWidth = 0;
    l.horizontalSpacing = 0;
    buttonContainerLeft.setLayout(l);
    buttonContainerRight.setLayout(l);

    GridData d = new GridData(SWT.FILL, SWT.TOP, true, false);
    buttonContainer.setLayoutData(d);

    d = new GridData(SWT.RIGHT, SWT.TOP, true, false);
    buttonContainerRight.setLayoutData(d);

    gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 400;
    m_workingSetsViewer.getControl().setLayoutData(gd);
  }

  private ITreeNode createTree() {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);
    IScoutBundle[] allBundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getAllBundlesFilter(), ScoutBundleComparators.getSymbolicNameAscComparator());

    for (IScoutBundle b : allBundles) {
      ITreeNode bundleNode = TreeUtility.createBundleTreeNode(rootNode, b);
      if (bundleNode != null) {
        bundleNode.setOrderNr(0); // no explicit order. order by name
        bundleNode.setCheckable(true);
      }
    }
    return rootNode;
  }

  private void createWorkingSetContent(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    Label label = new Label(composite, SWT.NONE);
    label.setText(Texts.get("AvailableBundles") + ":");
    m_availableBundlesTree = new CheckableTree(composite, createTree());
    m_availableBundlesTree.setDragDetect(false);
    m_availableBundlesTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        if (!m_lock.isAcquired()) {
          IAdaptable changedBundle = (IAdaptable) node.getData();
          IAdaptable[] old = getSelectionFor(m_currentWorkingSet);
          IAdaptable[] newElements;
          if (checkState) {
            newElements = new IAdaptable[old.length + 1];
            System.arraycopy(old, 0, newElements, 0, old.length);
            newElements[newElements.length - 1] = changedBundle;
          }
          else {
            newElements = new IAdaptable[old.length - 1];
            int pos = 0;
            for (IAdaptable existing : old) {
              if (CompareUtility.notEquals(existing, changedBundle)) {
                newElements[pos++] = existing;
              }
            }
          }
          m_selection.put(m_currentWorkingSet, newElements);
        }
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 8;
    layout.marginHeight = 8;
    composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 400;
    gd.widthHint = 500;
    m_availableBundlesTree.setLayoutData(gd);

    gd = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(gd);
  }

  @Override
  protected void okPressed() {
    IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
    IWorkingSet[] existings = ScoutExplorerSettingsSupport.get().getScoutWorkingSets(true);

    Set<String> visibleSets = getCheckedWorkingSets();
    Set<IWorkingSet> hiddenSets = new HashSet<IWorkingSet>();
    Set<IWorkingSet> setOrder = new LinkedHashSet<IWorkingSet>();
    for (String workingSet : getAllWorkingSets()) {
      IWorkingSet currentSet = null;

      // try to find an existing set
      for (IWorkingSet s : existings) {
        if (workingSet.equals(s.getLabel())) {
          currentSet = s;
          break;
        }
      }

      if (currentSet == null) {
        // create new
        currentSet = workingSetManager.createWorkingSet(workingSet, getSelectionFor(workingSet));
        currentSet.setId(ScoutExplorerSettingsSupport.SCOUT_WOKRING_SET_ID);
        workingSetManager.addWorkingSet(currentSet);
      }
      else {
        currentSet.setName(workingSet);
        currentSet.setElements(getSelectionFor(workingSet));
      }

      // remove the one we have from the list of initial items.
      m_initialSets.remove(workingSet);

      // collect all hidden sets
      if (!visibleSets.contains(currentSet.getLabel())) {
        hiddenSets.add(currentSet);
      }

      setOrder.add(currentSet);
    }

    // items that where available initially, but are no longer have been removed
    for (String name : m_initialSets) {
      workingSetManager.removeWorkingSet(workingSetManager.getWorkingSet(name));
    }

    // store which ones are visible
    ScoutExplorerSettingsSupport.get().setHiddenScoutWorkingSets(hiddenSets.toArray(new IWorkingSet[hiddenSets.size()]));
    ScoutExplorerSettingsSupport.get().setWorkingSetsOrder(setOrder.toArray(new IWorkingSet[setOrder.size()]));

    super.okPressed();
  }

  private class P_WorkingSetsContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
      //nop
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      //nop
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return (Object[]) inputElement;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      return null;
    }

    @Override
    public Object getParent(Object element) {
      return null;
    }

    @Override
    public boolean hasChildren(Object element) {
      return false;
    }
  }

  private class P_WorkingSetsLabelProvider extends LabelProvider {
    @Override
    public Image getImage(Object element) {
      return ScoutSdkUi.getImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ScoutWorkingSet));
    }

    @Override
    public String getText(Object element) {
      return element.toString();
    }
  }
}
