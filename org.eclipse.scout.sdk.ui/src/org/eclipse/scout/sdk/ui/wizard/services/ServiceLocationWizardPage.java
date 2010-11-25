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
//package org.eclipse.scout.sdk.ui.wizard.services;
//
//import java.beans.PropertyChangeListener;
//import java.util.ArrayList;
//import java.util.List;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.MultiStatus;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.scout.sdk.ScoutIdeProperties;
//import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
//import org.eclipse.scout.sdk.ui.ScoutSdkUi;
//import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
//import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
//import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
//import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
//import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
//import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
//import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
//import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
//import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
//import org.eclipse.scout.sdk.ui.wizard.form.BundleLocationUtility;
//import org.eclipse.scout.sdk.ui.wizard.form.FormLocationWizardPage;
//import org.eclipse.scout.sdk.ui.wizard.form.BundleLocationUtility.BundleSet;
//import org.eclipse.scout.sdk.workspace.IScoutBundle;
//import org.eclipse.scout.sdk.workspace.IScoutClientBundle;
//import org.eclipse.scout.sdk.workspace.IScoutServerBundle;
//import org.eclipse.scout.sdk.workspace.IScoutSharedBundle;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import com.bsiag.commons.beans.BasicPropertySupport;
//
///** <h3>RemoteServiceLocationWizardPage</h3>
// *  used only if there are multiple instances of client/shared/servers to determinate the location of
// *  the service interface, service implementation and its registrations.
// *
// * @author Andreas Hoegger
// * @since 1.0.8  19.11.2009
// */
//public class ServiceLocationWizardPage extends AbstractWorkspaceWizardPage{
//
//  public static final int TYPE_PERMISSION_CREATE = 104;
//  public static final int TYPE_PERMISSION_READ = 105;
//  public static final int TYPE_PERMISSION_UPDATE = 106;
//  public static final int TYPE_SERVICE_IMPLEMENTATION = 107;
//  public static final int TYPE_SERVICE_INTERFACE = 108;
//  public static final int TYPE_SERVICE_REG_CLIENT = 109;
//  public static final int TYPE_SERVICE_REG_SERVER = 110;
//
//
//  private BasicPropertySupport m_propertySupport;
//
//  // ui fields
//  private CheckableTree m_bundleTree;
//
//  // members
//  private ITreeNode m_rootNode;
//
//  private final IScoutBundle m_serverBundle;
//
//  public ServiceLocationWizardPage(IScoutBundle serverBundle){
//    super(ServiceLocationWizardPage.class.getName());
//    m_serverBundle=serverBundle;
//    m_propertySupport=new BasicPropertySupport(this);
//    setTitle("Process Service Location.");
//    setDefaultMessage("");
//  }
//
//  private void setupTree(){
//    m_rootNode = TreeUtility.createBundleTree(getServerBundle().getScoutProject());
//    BundleSet bundleSet = BundleLocationUtility.createBundleSet(getServerBundle());
//    List<ITreeNode> checkedNodes = new ArrayList<ITreeNode>();
//    TreeUtility.findNode(m_rootNode, NodeFilters.getByData(bundleSet.getClientBundle()));
//    ITreeNode clientNode = TreeUtility.findNode(m_rootNode, NodeFilters.getByData(bundleSet.getClientBundle()));
//    // service client reg
//    ITreeNode registrationClient = createNode(clientNode, TYPE_SERVICE_REG_CLIENT, "Service Proxy Registration", ScoutSdkUi.getImage(ScoutSdkUi.IMG_OBJ_PUBLIC));
//    checkedNodes.add(registrationClient);
//
//    if(bundleSet.getSharedBundle() != null){
//      ITreeNode sharedNode = TreeUtility.findNode(m_rootNode, NodeFilters.getByData( bundleSet.getSharedBundle()));
//      // permission create
//      ITreeNode permissionCreateNode = createNode(sharedNode, TYPE_PERMISSION_CREATE, "CreatePermission", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLASS));
//      checkedNodes.add(permissionCreateNode);
//      // permission read
//      ITreeNode permissionReadNode = createNode(sharedNode,TYPE_PERMISSION_READ, "ReadPermission", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLASS));
//      checkedNodes.add(permissionReadNode);
//      // permission update
//      ITreeNode permissionUpdateNode = createNode(sharedNode, TYPE_PERMISSION_UPDATE, "UpdatePermission", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLASS));
//      checkedNodes.add(permissionUpdateNode);
//      // service interface
//      ITreeNode serviceInterfaceNode = createNode(sharedNode, TYPE_SERVICE_INTERFACE,"IService", ScoutSdkUi.getImage(ScoutSdkUi.IMG_INTERFACE));
//      checkedNodes.add(serviceInterfaceNode);
//    }
//    if(bundleSet.getServerBundle() != null){
//      ITreeNode serverNode = TreeUtility.findNode(m_rootNode, NodeFilters.getByData( bundleSet.getServerBundle()));
//      // service implementation
//      ITreeNode serviceNode = createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION,"Service", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLASS));
//      checkedNodes.add(serviceNode);
//      // service implementation
//      ITreeNode serviceRegistrationServer = createNode(serverNode, TYPE_SERVICE_REG_SERVER, "Service Registration", ScoutSdkUi.getImage(ScoutSdkUi.IMG_OBJ_PUBLIC));
//      checkedNodes.add(serviceRegistrationServer);
//    }
//  }
//
//  /**
//   * @return the rootNode
//   */
//  public ITreeNode getRootNode(){
//    return m_rootNode;
//  }
//
//  public void setServiceName(String serviceName){
//    String namePref = serviceName.replaceAll(ScoutIdeProperties.SUFFIX_PROCESS_SERVICE+"$", "");
//    TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_CREATE)).setText("Create"+namePref+ScoutIdeProperties.SUFFIX_PERMISSION);
//    TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_READ)).setText("Read"+namePref+ScoutIdeProperties.SUFFIX_PERMISSION);
//    TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_UPDATE)).setText("Update"+namePref+ScoutIdeProperties.SUFFIX_PERMISSION);
//    TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setText(namePref+ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);
//    TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setText("I"+namePref+ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);
//    m_bundleTree.getTreeViewer().refresh();
//    pingStateChanging();
//  }
//
//  private ITreeNode createNode(ITreeNode parentNode,int type, String name, Image img){
//    TreeNode node = new TreeNode(type, name);
//    node.setImage(img);
//    node.setCheckable(true);
//    node.setParent(parentNode);
//    parentNode.addChild(node);
//    return node;
//  }
//
//  public void addPropertyChangeListener(PropertyChangeListener listener){
//    m_propertySupport.addPropertyChangeListener(listener);
//  }
//
//  public void removePropertyChangeListener(PropertyChangeListener listener){
//    m_propertySupport.removePropertyChangeListener(listener);
//  }
//
//  @Override
//  protected void createContent(Composite parent){
//    setupTree();
//    m_bundleTree = new CheckableTree(parent,m_rootNode);
//    ITreeNode[] allCheckedNodes = TreeUtility.findNodes(m_rootNode, NodeFilters.getByData(null));
//    m_bundleTree.setChecked(allCheckedNodes);
//    // initialize
//    m_bundleTree.addDndListener(new P_TreeDndListener());
//    m_bundleTree.addCheckSelectionListener(new P_CheckStateListener());
//
//
//    // layout
//    parent.setLayout(new GridLayout(1, true));
//
//    GridData interfaceLocationData=new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
//    m_bundleTree.setLayoutData(interfaceLocationData);
//
//  }
//
//
//  void fillOperation(ProcessServiceNewOperation op){
//    ArrayList<IScoutBundle> clientRegBundles = new ArrayList<IScoutBundle>();
//    for(ITreeNode n : TreeUtility.findNodes(getRootNode(), NodeFilters.getByType(FormLocationWizardPage.TYPE_SERVICE_REG_CLIENT))){
//      if(m_bundleTree.isChecked(n)){
//        Object data=n.getParent().getData();
//        if(data instanceof IScoutBundle){
//          clientRegBundles.add((IScoutBundle)data);
//        }
//      }
//    }
//    op.setClientServiceRegistryBundles(clientRegBundles.toArray(new IScoutBundle[clientRegBundles.size()]));
//    if(m_bundleTree.isChecked(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_CREATE)))){
//      op.setPermissionCreateBundle(getLocationBundle(TYPE_PERMISSION_CREATE, IScoutBundle.class));
//      op.setPermissionCreateName(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_CREATE)).getText());
//    }
//    if(m_bundleTree.isChecked(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_READ)))){
//      op.setPermissionReadBundle(getLocationBundle(TYPE_PERMISSION_READ, IScoutBundle.class));
//      op.setPermissionReadName(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_READ)).getText());
//    }
//    if(m_bundleTree.isChecked(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_UPDATE)))){
//      op.setPermissionUpdateBundle(getLocationBundle(TYPE_PERMISSION_UPDATE, IScoutBundle.class));
//      op.setPermissionUpdateName(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_PERMISSION_UPDATE)).getText());
//    }
//    ArrayList<IScoutBundle> serverRegBundles = new ArrayList<IScoutBundle>();
//    for(ITreeNode n : TreeUtility.findNodes(getRootNode(), NodeFilters.getByType(FormLocationWizardPage.TYPE_SERVICE_REG_SERVER))){
//      if(m_bundleTree.isChecked(n)){
//        Object data=n.getParent().getData();
//        if(data instanceof IScoutBundle){
//          serverRegBundles.add((IScoutBundle)data);
//        }
//      }
//    }
//    op.setServerServiceRegistryBundles(serverRegBundles.toArray(new IScoutBundle[serverRegBundles.size()]));
//    if(m_bundleTree.isChecked(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)))){
//      op.setServiceImplementationBundle(getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, IScoutBundle.class));
//      op.setServiceImplementationName(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).getText());
//    }
//    if(m_bundleTree.isChecked(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)))){
//      op.setServiceInterfaceBundle(getLocationBundle(TYPE_SERVICE_INTERFACE, IScoutBundle.class));
//      op.setServiceInterfaceName(TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).getText());
//    }
//  }
//
//
//  public IScoutBundle getServerBundle(){
//    return m_serverBundle;
//  }
//
//  @Override
//  protected void validatePage(MultiStatus multiStatus){
//    try{
//      multiStatus.add(getStatusTypeNames());
//      multiStatus.add(getStatusService());
//      multiStatus.add(getStatusServiceRegistrationClient());
//      multiStatus.add(getStatusServiceRegistrationServer());
//    }
//    catch(Exception e){
//      ScoutSdkUi.logError("could not validate name field.", e);
//    }
//  }
//
//  protected IStatus getStatusTypeNames(){
//    IScoutBundle serviceImplementationBundle=getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, IScoutBundle.class);
//    if(serviceImplementationBundle != null){
//      ITreeNode serviceImplNode=TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION));
//      if(serviceImplNode !=null && isChecked(TYPE_SERVICE_IMPLEMENTATION)){
//        String fqn = serviceImplementationBundle.getPackageNameProcessService()+"."+serviceImplNode.getText();
//        if(serviceImplementationBundle.findType(fqn) != null){
//          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'"+serviceImplNode.getText()+"' already exists.");
//        }
//      }
//    }
//    IScoutBundle serviceInterfaceBundle=getLocationBundle(TYPE_SERVICE_INTERFACE, IScoutBundle.class);
//    if(serviceInterfaceBundle != null){
//      ITreeNode serviceInterfaceNode=TreeUtility.findNode(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_INTERFACE));
//      if(serviceInterfaceNode !=null && isChecked(TYPE_SERVICE_INTERFACE)){
//        String fqn = serviceInterfaceBundle.getPackageNameServicesProcess()+"."+serviceInterfaceNode.getText();
//        if(serviceInterfaceBundle.findType(fqn) != null){
//          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'"+serviceInterfaceNode.getText()+"' already exists.");
//        }
//      }
//    }
//    // permission read
//    IStatus createStatus = getStatusPermission(TYPE_PERMISSION_CREATE);
//    if(createStatus != Status.OK_STATUS){
//      return createStatus;
//    }
//    IStatus readStatus = getStatusPermission(TYPE_PERMISSION_READ);
//    if(readStatus != Status.OK_STATUS){
//      return readStatus;
//    }
//    IStatus updateStatus = getStatusPermission(TYPE_PERMISSION_UPDATE);
//    if(updateStatus != Status.OK_STATUS){
//      return updateStatus;
//    }
//    return Status.OK_STATUS;
//  }
//  protected IStatus getStatusPermission(int permissionType){
//    IScoutBundle permissionBundle=getLocationBundle(permissionType, IScoutBundle.class);
//    if(permissionBundle != null){
//      ITreeNode permissionNode=TreeUtility.findNode(m_rootNode, NodeFilters.getByType(permissionType));
//      if(permissionNode !=null && isChecked(permissionType)){
//        String fqn = permissionBundle.getPackageNameSecurity()+"."+permissionNode.getText();
//        if(permissionBundle.findType(fqn) != null){
//          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'"+permissionNode.getText()+"' already exists.");
//        }
//      }
//    }
//    return Status.OK_STATUS;
//  }
//
//
//  protected IStatus getStatusService(){
//    IScoutBundle serviceImplementationBundle=getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, IScoutBundle.class);
//    if(serviceImplementationBundle!=null && isChecked(TYPE_SERVICE_IMPLEMENTATION)){
//      IScoutBundle serviceInterfaceBundle=getLocationBundle(TYPE_SERVICE_INTERFACE, IScoutBundle.class);
//      if(serviceInterfaceBundle!=null && isChecked(TYPE_SERVICE_INTERFACE)){
//        if(!serviceImplementationBundle.isOnClasspath(serviceInterfaceBundle)){
//          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'"+getTextOfNode(TYPE_SERVICE_INTERFACE)+" is not on classpath of '"+getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)+"'.");
//        }
//      }
//      IScoutBundle permissionCreateBundle=getLocationBundle(TYPE_PERMISSION_CREATE, IScoutBundle.class);
//      if(permissionCreateBundle!=null && isChecked(TYPE_PERMISSION_CREATE)){
//        if(!serviceImplementationBundle.isOnClasspath(permissionCreateBundle)){
//          return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'"+getTextOfNode(TYPE_PERMISSION_CREATE)+" is not on classpath of '"+getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)+"'.");
//        }
//      }
//      IScoutBundle permissionReadBundle=getLocationBundle(TYPE_PERMISSION_READ, IScoutBundle.class);
//      if(permissionReadBundle!=null && isChecked(TYPE_PERMISSION_READ)){
//        if(!serviceImplementationBundle.isOnClasspath(permissionReadBundle)){
//          return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'"+getTextOfNode(TYPE_PERMISSION_READ)+" is not on classpath of '"+getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)+"'.");
//        }
//      }
//      IScoutBundle permissionUpdateBundle=getLocationBundle(TYPE_PERMISSION_UPDATE, IScoutBundle.class);
//      if(permissionUpdateBundle!=null && isChecked(TYPE_PERMISSION_UPDATE)){
//        if(!serviceImplementationBundle.isOnClasspath(permissionUpdateBundle)){
//          return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'"+getTextOfNode(TYPE_PERMISSION_UPDATE)+" is not on classpath of '"+getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)+"'.");
//        }
//      }
//    }
//    return Status.OK_STATUS;
//  }
//
//
//  protected IStatus getStatusServiceRegistrationClient(){
//    IScoutBundle serviceInterfaceBundle = getLocationBundle(TYPE_SERVICE_INTERFACE, IScoutBundle.class);
//    ITreeNode[] serviceRegistrationClientNodes = TreeUtility.findNodes(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_REG_CLIENT));
//    for(ITreeNode serviceRegNode : serviceRegistrationClientNodes){
//      if(m_bundleTree.isChecked(serviceRegNode)){
//        Object data=serviceRegNode.getParent().getData();
//        if(data instanceof IScoutBundle){
//          IScoutBundle serviceRegistrationBundle=(IScoutBundle)data;
//          if(serviceInterfaceBundle!=null&&serviceRegistrationBundle!=null){
//            if(!serviceRegistrationBundle.isOnClasspath(serviceInterfaceBundle)){
//              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'"+getTextOfNode(TYPE_SERVICE_INTERFACE)+" is not on classpath of Service Registration in '"+serviceRegistrationBundle.getBundleName()+"'.");
//            }
//          }
//        }
//      }
//    }
//    return Status.OK_STATUS;
//  }
//
//  protected IStatus getStatusServiceRegistrationServer(){
//    IScoutBundle serviceImplementationBundle = getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, IScoutBundle.class);
//    ITreeNode[] serviceRegistrationServerNodes = TreeUtility.findNodes(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_REG_SERVER));
//    for(ITreeNode serviceRegNode: serviceRegistrationServerNodes){
//      if(m_bundleTree.isChecked(serviceRegNode)){
//        Object data=serviceRegNode.getParent().getData();
//        if(data instanceof IScoutBundle){
//          IScoutBundle serviceRegistrationBundle=(IScoutBundle)data;
//          if(serviceImplementationBundle!=null&&serviceRegistrationBundle!=null){
//            if(!serviceRegistrationBundle.isOnClasspath(serviceImplementationBundle)){
//              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'"+getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)+" is not on classpath of Service Registration in '"+serviceRegistrationBundle.getBundleName()+"'.");
//            }
//          }
//        }
//      }
//    }
//
//    return Status.OK_STATUS;
//  }
//  private boolean isChecked(int type){
//    ITreeNode treeNode= TreeUtility.findNode(m_rootNode, NodeFilters.getByType(type));
//    return treeNode != null && m_bundleTree.isChecked(treeNode);
//  }
//
//  private String getTextOfNode(int type){
//    String name = "not found";
//    ITreeNode treeNode= TreeUtility.findNode(m_rootNode, NodeFilters.getByType(type));
//    if(treeNode != null){
//      name = treeNode.getText();
//    }
//    return name;
//  }
//
//  private ITreeNode getLocationNode(int type){
//    ITreeNode treeNode= TreeUtility.findNode(m_rootNode, NodeFilters.getByType(type));
//    if(treeNode != null){
//      return treeNode.getParent();
//    }
//    return null;
//  }
//
//  @SuppressWarnings("unchecked")
//  private <T extends IScoutBundle>T getLocationBundle(int nodeType, Class<T> t){
//    ITreeNode locationNode=getLocationNode(nodeType);
//    if(locationNode!=null){
//      Object data=locationNode.getData();
//      return (T)data;
//    }
//    else{
//      return null;
//    }
//  }
//
//  private class P_TreeDndListener implements ITreeDndListener{
//    @Override
//    public void dragStart(DndEvent dndEvent){
//      switch(dndEvent.node.getType()){
//        case TYPE_PERMISSION_CREATE:
//        case TYPE_PERMISSION_READ:
//        case TYPE_PERMISSION_UPDATE:
//        case TYPE_SERVICE_IMPLEMENTATION:
//        case TYPE_SERVICE_INTERFACE:
//        case TYPE_SERVICE_REG_CLIENT:
//        case TYPE_SERVICE_REG_SERVER:
//          dndEvent.doit = true;
//          break;
//
//        default:
//          dndEvent.doit = false;
//          break;
//      }
//    }
//
//    @Override
//    public void validateTarget(DndEvent dndEvent){
//        switch(dndEvent.node.getType()){
//          case TYPE_SERVICE_REG_CLIENT:
//            dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
//            break;
//          case TYPE_PERMISSION_CREATE:
//          case TYPE_PERMISSION_READ:
//          case TYPE_PERMISSION_UPDATE:
//          case TYPE_SERVICE_INTERFACE:
//            dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SHARED;
//            break;
//          case TYPE_SERVICE_IMPLEMENTATION:
//          case TYPE_SERVICE_REG_SERVER:
//            dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SERVER;
//            break;
//          default:
//            dndEvent.doit = false;
//          break;
//        }
//    }
//    @Override
//    public void dndPerformed(DndEvent dndEvent){
//      pingStateChanging();
//    }
//
//    @Override
//    public void validateDropCopy(DndEvent dndEvent){
//      switch(dndEvent.node.getType()){
//        case TYPE_SERVICE_REG_CLIENT:
//          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
//          break;
//        case TYPE_SERVICE_REG_SERVER:
//          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SERVER;
//          break;
//        default:
//          dndEvent.doit = false;
//        break;
//      }
//    }
//
//    @Override
//    public void validateDropMove(DndEvent dndEvent){
//      switch(dndEvent.node.getType()){
//        case TYPE_SERVICE_REG_CLIENT:
//          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
//          break;
//        case TYPE_PERMISSION_CREATE:
//        case TYPE_PERMISSION_READ:
//        case TYPE_PERMISSION_UPDATE:
//        case TYPE_SERVICE_INTERFACE:
//          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SHARED;
//          break;
//        case TYPE_SERVICE_IMPLEMENTATION:
//        case TYPE_SERVICE_REG_SERVER:
//          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SERVER;
//          break;
//        default:
//          dndEvent.doit = false;
//        break;
//      }
//
//    }
//  }
//
//  private class P_CheckStateListener implements ICheckStateListener{
//    @Override
//    public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState){
//      boolean update=false;
//      if(node.getType()==TYPE_SERVICE_INTERFACE){
//        for(ITreeNode proxyRegNode: TreeUtility.findNodes(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_REG_CLIENT))){
//          proxyRegNode.setEnabled(checkState);
//          if(!checkState){
//            m_bundleTree.setChecked(proxyRegNode, checkState);
//          }
//          update=true;
//        }
//      }
//      else if(node.getType()==TYPE_SERVICE_IMPLEMENTATION){
//        for(ITreeNode proxyRegNode: TreeUtility.findNodes(m_rootNode, NodeFilters.getByType(TYPE_SERVICE_REG_SERVER))){
//          proxyRegNode.setEnabled(checkState);
//          if(!checkState){
//            m_bundleTree.setChecked(proxyRegNode, checkState);
//          }
//          update=true;
//        }
//      }
//      if(update){
//        m_bundleTree.getTreeViewer().refresh();
//      }
//      pingStateChanging();
//    }
//  }
//
// }
