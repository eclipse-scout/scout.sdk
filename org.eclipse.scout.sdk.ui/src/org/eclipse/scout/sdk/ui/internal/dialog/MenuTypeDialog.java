/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.dialog;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.parser.MenuTypesConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link MenuTypeDialog}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 03.06.2014
 */
@SuppressWarnings("restriction")
public class MenuTypeDialog extends TitleAreaDialog {

  private static final String TREE_NODE_TYPE = "type";
  private static final String TREE_NODE_ENUM = "enum";

  private final MenuTypesConfig m_config;
  private final IType m_menu;
  private CheckableTree m_tree;

  public MenuTypeDialog(Shell parentShell, MenuTypesConfig config, IType menu) {
    super(parentShell);
    m_config = config;
    m_menu = menu;
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    getShell().setText(Texts.get("ChooseMenuTypes"));
    setTitle(Texts.get("ChooseMenuTypes"));
    setMessage(Texts.get("ChooseTheTypesForMenu", m_menu.getFullyQualifiedName('.')));
    Composite rootArea = new Composite(parent, SWT.BORDER);

    ITreeNode root = createTree();
    m_tree = new CheckableTree(rootArea, root);
    m_tree.setChecked(TreeUtility.findNodes(root, new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        if (TREE_NODE_ENUM.equals(node.getType())) {
          IField f = (IField) node.getData();
          IType enumType = (IType) node.getParent().getData();
          return m_config.getValuesFor(enumType).contains(f.getElementName());
        }
        return false;
      }
    }));
    m_tree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        IType enumType = (IType) node.getParent().getData();
        IField f = (IField) node.getData();
        if (checkState) {
          m_config.add(enumType, f.getElementName());
        }
        else {
          m_config.remove(enumType, f.getElementName());
        }
      }
    });

    if (parent.getLayout() instanceof GridLayout) {
      rootArea.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    rootArea.setLayout(new GridLayout(1, true));
    m_tree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));

    return rootArea;
  }

  private ITreeNode createTree() {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);

    ICachedTypeHierarchy menuTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(IRuntimeClasses.IMenuType));
    Set<IType> menuTypeEnums = menuTypeHierarchy.getAllTypes(TypeFilters.getEnumTypesFilter(), TypeComparators.getTypeNameComparator());
    int i = 0;
    for (IType m : menuTypeEnums) {
      ITreeNode typeNode = TreeUtility.createNode(rootNode, TREE_NODE_TYPE, m.getElementName(), UiUtility.getTypeImageDescriptor(m, false), i++, m, false, false);
      String enumSig = SignatureCache.createTypeSignature(m.getFullyQualifiedName());
      try {
        for (IField f : m.getFields()) {
          if (f.getTypeSignature().equals(enumSig)) {
            int flags = f.getFlags();
            if (Flags.isPublic(flags) && Flags.isStatic(flags) && Flags.isFinal(flags) && !Flags.isDeprecated(flags)) {
              TreeUtility.createNode(typeNode, TREE_NODE_ENUM, f.getElementName(), JavaElementImageProvider.getFieldImageDescriptor(false, flags), i++, f, false, true);
            }
          }
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to get the available menu types", e);
      }
    }

    return rootNode;
  }

  public MenuTypesConfig openDialog() {
    if (open() == OK) {
      return m_config;
    }
    return null;
  }
}
