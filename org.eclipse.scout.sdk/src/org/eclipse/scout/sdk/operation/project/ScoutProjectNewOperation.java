package org.eclipse.scout.sdk.operation.project;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

@SuppressWarnings("restriction")
public class ScoutProjectNewOperation extends AbstractScoutProjectNewOperation {
  private final static String EXT_TAG_NAME = "operation";
  private final static String EXT_NAME = "newProjectOperation";
  private final static String EXT_ATTR_CLASS_NAME = "class";
  private final static String EXT_ATTR_ID_NAME = "id";
  private final static String EXT_ATTR_REF_NAME = "referenceId";
  private final static String EXT_ATTR_REF_TYPE_NAME = "execAfterReference";

  public ScoutProjectNewOperation() {
  }

  @Override
  public void init() {
  }

  @Override
  public boolean isRelevant() {
    return true;
  }

  @Override
  public String getOperationName() {
    return "Create new Scout Project";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // collect all registered operations
    HashMap<String, P_OperationElement> ops = new HashMap<String, P_OperationElement>();
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXT_NAME);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if (EXT_TAG_NAME.equals(element.getName())) {
          P_OperationElement op = new P_OperationElement(element);
          ops.put(op.id, op);
        }
      }
    }

    // build tree based on the references and remember root nodes and invalid nodes
    ArrayList<P_OperationElement> roots = new ArrayList<P_OperationElement>();
    ArrayList<P_OperationElement> invalidNodes = new ArrayList<P_OperationElement>();
    for (P_OperationElement op : ops.values()) {
      if (op.referenceId == null) {
        roots.add(op);
      }
      else {
        P_OperationElement refElement = ops.get(op.referenceId);
        if (refElement != null) {
          if (op.execAfterReference) {
            refElement.children.add(op);
          }
          else {
            op.children.add(refElement);
          }
        }
        else {
          invalidNodes.add(op); // the reference of this item could not be found -> execute at the end
          ScoutSdk.logWarning("New Project Operation reference '" + op.referenceId + "' defined by '" + op.op.getClass().getName() + "' could not be found.");
        }
      }
    }

    if (roots.size() == 0) {
      throw new IllegalArgumentException("not operation root node could be found! Check for cycles in the reference graph.");
    }

    // traverse the tree collecting all items in a level-order-traversal into a flat list
    // if several root nodes have been found (independent trees), the trees are executed in serial order (complete first tree, complete second tree, ...)
    ArrayList<P_OperationElement> nodes = new ArrayList<P_OperationElement>();
    breadthFirstTraverse(roots, nodes);

    // add all invalid nodes to the end assuming that no-one depends on them but they may depend on others (which are not present)
    nodes.addAll(invalidNodes);

    // execute the operations
    execOperations(monitor, workingCopyManager, nodes.toArray(new P_OperationElement[nodes.size()]));
  }

  private void breadthFirstTraverse(ArrayList<P_OperationElement> nodes, ArrayList<P_OperationElement> collector) {
    for (P_OperationElement node : nodes) {
      Deque<P_OperationElement> deck = new ArrayDeque<P_OperationElement>();
      deck.addLast(node);
      while (!deck.isEmpty()) {
        P_OperationElement el = deck.removeFirst();
        for (P_OperationElement child : el.children) {
          deck.addLast(child);
        }
        collector.add(el);
      }
    }
  }

  private void execOperations(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, P_OperationElement[] ops) throws CoreException, IllegalArgumentException {
    // put system properties
    getProperties().setProperty(IScoutProjectNewOperation.PROP_CREATED_BUNDLES, new ArrayList<IJavaProject>());
    getProperties().setProperty(IScoutProjectNewOperation.PROP_OS, ScoutSdk.getDefault().getBundle().getBundleContext().getProperty(ICoreConstants.OSGI_OS));
    getProperties().setProperty(IScoutProjectNewOperation.PROP_WS, ScoutSdk.getDefault().getBundle().getBundleContext().getProperty(ICoreConstants.OSGI_WS));
    getProperties().setProperty(IScoutProjectNewOperation.PROP_ARCH, ScoutSdk.getDefault().getBundle().getBundleContext().getProperty(ICoreConstants.OSGI_ARCH));
    getProperties().setProperty(IScoutProjectNewOperation.PROP_LOCALHOST, getHostName());
    getProperties().setProperty(IScoutProjectNewOperation.PROP_CURRENT_DATE, SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT).format(new Date()));
    getProperties().setProperty(IScoutProjectNewOperation.PROP_USER_NAME, System.getProperty("user.name"));

    for (P_OperationElement opElement : ops) {
      // execute the pipeline as defined in IScoutProjectNewOperation
      IScoutProjectNewOperation o = opElement.op;
      o.setProperties(getProperties());
      if (o.isRelevant()) {
        o.init();
        o.validate();
        o.run(monitor, workingCopyManager);
      }
    }
    ResourcesPlugin.getWorkspace().checkpoint(false);
  }

  private static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName().toLowerCase();
    }
    catch (UnknownHostException e) {
      return null;
    }
  }

  private static class P_OperationElement {
    private final IScoutProjectNewOperation op;
    private final String id;
    private final String referenceId;
    private final boolean execAfterReference;
    private final ArrayList<P_OperationElement> children;

    private P_OperationElement(IConfigurationElement element) throws CoreException {
      op = (IScoutProjectNewOperation) element.createExecutableExtension(EXT_ATTR_CLASS_NAME);
      id = cleanString(element.getAttribute(EXT_ATTR_ID_NAME));
      referenceId = cleanString(element.getAttribute(EXT_ATTR_REF_NAME));
      children = new ArrayList<P_OperationElement>();

      String execAfter = cleanString(element.getAttribute(EXT_ATTR_REF_TYPE_NAME));
      boolean after = true;
      if (execAfter != null) {
        if ("false".equalsIgnoreCase(execAfter)) {
          after = false;
        }
      }
      execAfterReference = after;
    }

    private static String cleanString(String input) {
      if (input == null) return null;
      input = input.trim();
      if (input.length() == 0) return null;
      return input;
    }
  }
}
