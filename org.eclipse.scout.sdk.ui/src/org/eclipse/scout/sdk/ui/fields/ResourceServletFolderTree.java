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
package org.eclipse.scout.sdk.ui.fields;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * <h3>{@link ResourceServletFolderTree}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class ResourceServletFolderTree {
  public static int NODE_TYPE_FOLDER = 129;

  private ITreeNode m_rootNode;

  public ResourceServletFolderTree(IScoutProject project) {
    m_rootNode = TreeUtility.createBundleTree(project);
    initTree(m_rootNode);
  }

  /**
   * @return the rootNode
   */
  public ITreeNode getRootNode() {
    return m_rootNode;
  }

  private void initTree(ITreeNode rootNode) {
    ArrayList<P_ResourceServletExtension> extensions = new ArrayList<P_ResourceServletExtension>();
    findServletExtensions(rootNode, extensions);
    TreeUtility.findNodes(rootNode, new P_FolderTreeBuilder(extensions.toArray(new P_ResourceServletExtension[extensions.size()])));

  }

  private void findServletExtensions(ITreeNode curentNode, ArrayList<P_ResourceServletExtension> extensions) {
    if (curentNode.getType() > 2 && curentNode.getType() < 8) {
      IScoutBundle bundle = (IScoutBundle) curentNode.getData();
      IPluginModelBase pluginModel = PluginRegistry.findModel(bundle.getProject());
      IPluginBase pluginBase = pluginModel.getPluginBase(false);
      if (pluginBase != null) {
        IPluginExtension[] exs = pluginBase.getExtensions();
        if (exs != null && exs.length > 0) {
          for (IPluginExtension e : exs) {
            if (CompareUtility.equals("org.eclipse.equinox.http.registry.servlets", e.getPoint()) && e.getChildCount() > 0) {
              for (IPluginObject po : e.getChildren()) {
                if (po instanceof IPluginElement) {
                  IPluginElement pe = (IPluginElement) po;
                  if (CompareUtility.equals(RuntimeClasses.ResourceServlet, pe.getAttribute("class").getValue()) && pe.getChildCount() > 0) {
                    P_ResourceServletExtension ext = new P_ResourceServletExtension();
                    for (IPluginObject resourceServletPo : pe.getChildren()) {
                      if (resourceServletPo instanceof IPluginElement && CompareUtility.equals(resourceServletPo.getName(), "init-param")) {
                        IPluginElement resourceServletPe = (IPluginElement) resourceServletPo;
                        IPluginAttribute[] atts = resourceServletPe.getAttributes();
                        if (atts != null) {
                          HashMap<String, String> props = new HashMap<String, String>(2);
                          for (IPluginAttribute at : atts) {
                            props.put(at.getName(), at.getValue());
                          }
                          String name = props.get("name");
                          String value = props.get("value");
                          if (!StringUtility.isNullOrEmpty(name) && !StringUtility.isNullOrEmpty(value)) {
                            if (CompareUtility.equals(name, "bundle-name")) {
                              ext.bundleName = value;
                            }
                            if (CompareUtility.equals(name, "bundle-path")) {
                              ext.bundlePath = value;
                            }
                          }
                        }

                      }

                    }
                    if (ext.isValid()) {
                      extensions.add(ext);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    for (ITreeNode cn : curentNode.getChildren()) {
      findServletExtensions(cn, extensions);
    }
  }

  private class P_ResourceServletExtension {
    private String bundleName;
    private String bundlePath;

    boolean isValid() {
      return bundleName != null && bundlePath != null;
    }
  }

  private class P_FolderTreeBuilder implements ITreeNodeFilter {
    private HashMap<String, P_ResourceServletExtension> m_extensions;

    public P_FolderTreeBuilder(P_ResourceServletExtension[] extensions) {
      m_extensions = new HashMap<String, P_ResourceServletExtension>();
      for (P_ResourceServletExtension e : extensions) {
        m_extensions.put(e.bundleName, e);
      }
    }

    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case IScoutBundle.BUNDLE_CLIENT:
        case IScoutBundle.BUNDLE_UI_SWING:
        case IScoutBundle.BUNDLE_UI_SWT:
          node.setVisible(false);
          break;
        case IScoutBundle.BUNDLE_SHARED:
        case IScoutBundle.BUNDLE_SERVER:
          IScoutBundle bundle = (IScoutBundle) node.getData();
          P_ResourceServletExtension ext = m_extensions.get(bundle.getBundleName());
          if (ext != null) {
            createChildNodes(node, ext);
          }
          else {
            node.setVisible(false);
          }
          break;

        default:
          break;
      }
      return true;
    }

    private void createChildNodes(ITreeNode node, P_ResourceServletExtension ext) {
      setPathVisible(node);
      IScoutBundle scoutBundle = (IScoutBundle) node.getData();
      IFolder folder = scoutBundle.getProject().getFolder(new Path(ext.bundlePath));
      createNodeForFolder(node, folder);
    }

    private void setPathVisible(ITreeNode node) {
      if (node != null && node.getType() != CheckableTree.TYPE_ROOT) {
        node.setVisible(true);
        setPathVisible(node.getParent());
      }
    }

    private void createNodeForFolder(ITreeNode parentNode, IFolder folder) {
      if (folder.exists()) {
        TreeNode folderNode = new TreeNode(NODE_TYPE_FOLDER, folder.getProjectRelativePath().toString(), folder);
        folderNode.setCheckable(false);
        folderNode.setBold(true);
        ImageDescriptor img = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FolderOpen);
        IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) folder.getAdapter(IWorkbenchAdapter.class);
        if (wbAdapter != null) {
          img = wbAdapter.getImageDescriptor(folder);
        }
        ((TreeNode) folderNode).setImage(img);
        parentNode.addChild(folderNode);
        try {
          for (IResource resource : folder.members()) {
            if (resource.getType() == IResource.FOLDER) {
              createNodeForFolder(parentNode, (IFolder) resource);
            }
          }
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not get subfolders of '" + folder.getLocationURI() + "'.", e);
        }
      }
    }

  }
}
