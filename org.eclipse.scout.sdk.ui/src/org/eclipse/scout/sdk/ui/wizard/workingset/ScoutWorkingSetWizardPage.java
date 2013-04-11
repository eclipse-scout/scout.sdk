package org.eclipse.scout.sdk.ui.wizard.workingset;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

public class ScoutWorkingSetWizardPage extends WizardPage implements IWorkingSetPage {

  private CheckableTree m_availableBundlesTree;
  private TextField m_nameField;
  private IWorkingSet m_currentWorkingSet;

  public ScoutWorkingSetWizardPage() {
    super(Texts.get("ConfigureScoutWorkingSets"), Texts.get("ConfigureScoutWorkingSets"), null);
    setMessage(Texts.get("WorkingSetsMsg"));
  }

  @Override
  public void createControl(Composite parent) {
    Composite p = new Composite(parent, SWT.NONE);

    m_nameField = new TextField(p, Texts.get("Name") + ":", 7);
    m_nameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        validatePage();
      }
    });

    Label label = new Label(p, SWT.NONE);
    label.setText(Texts.get("Content") + ":");
    m_availableBundlesTree = new CheckableTree(p, createTree());
    m_availableBundlesTree.setDragDetect(false);

    // init values
    if (getSelection() != null) {
      m_nameField.setText(getSelection().getName());

      ArrayList<ITreeNode> checked = new ArrayList<ITreeNode>();
      for (IAdaptable a : getSelection().getElements()) {
        ITreeNode[] candidates = TreeUtility.findNodes(m_availableBundlesTree.getRootNode(), NodeFilters.getByData(a));
        if (candidates != null && candidates.length > 0) {
          for (ITreeNode candidate : candidates) {
            checked.add(candidate);
          }
        }
      }

      m_availableBundlesTree.setChecked(checked.toArray(new ITreeNode[checked.size()]));
    }

    // layout
    p.setLayout(new GridLayout(1, true));
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    p.setLayoutData(gd);

    gd = new GridData(GridData.FILL_BOTH);
    m_availableBundlesTree.setLayoutData(gd);

    gd = new GridData(GridData.FILL_HORIZONTAL);
    m_nameField.setLayoutData(gd);
    setControl(p);
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

  @Override
  public void finish() {
    String workingSetName = m_nameField.getText().trim();

    ITreeNode[] checkedNodes = m_availableBundlesTree.getCheckedNodes();
    IAdaptable[] elements = new IAdaptable[checkedNodes.length];
    for (int i = 0; i < elements.length; i++) {
      elements[i] = (IAdaptable) checkedNodes[i].getData();
    }

    if (getSelection() == null) {
      // new
      IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
      setSelection(workingSetManager.createWorkingSet(workingSetName, elements));
    }
    else {
      // modify
      getSelection().setName(workingSetName);
      getSelection().setElements(elements);
    }
  }

  private void validatePage() {
    String errorMessage = null;

    if (!StringUtility.hasText(m_nameField.getText())) {
      errorMessage = Texts.get("PleaseSpecifyAName");
    }

    if (PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(m_nameField.getText().trim()) != null) {
      errorMessage = Texts.get("NameAlreadyInUse");
    }

    setErrorMessage(errorMessage);
    setPageComplete(errorMessage == null);
  }

  @Override
  public IWorkingSet getSelection() {
    return m_currentWorkingSet;
  }

  @Override
  public void setSelection(IWorkingSet workingSet) {
    m_currentWorkingSet = workingSet;
  }
}
