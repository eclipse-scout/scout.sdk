package org.eclipse.scout.sdk.operation.project;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

@SuppressWarnings("restriction")
public class ScoutProjectNewOperation extends AbstractScoutProjectNewOperation {
  private final static String EXT_TAG_NAME = "operation";
  private final static String EXT_NAME = "newProjectOperation";
  private final static String EXT_ATTR_CLASS_NAME = "class";
  private final static String EXT_ATTR_ID_NAME = "id";
  private final static String EXT_ATTR_REF_NAME = "referenceId";
  private final static String EXT_ATTR_REF_TYPE_NAME = "execAfterReference";
  private final static String EXEC_ENV_PREFIX = "JavaSE-";
  private final static String MIN_JVM_VERSION = "1.6";

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
    return "Creating new Scout project...";
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

    // build graph based on the references and remember invalid nodes
    ArrayList<P_OperationElement> invalidNodes = new ArrayList<P_OperationElement>();
    ArrayList<P_OperationElement> validNodes = new ArrayList<P_OperationElement>(ops.size());
    for (P_OperationElement op : ops.values()) {
      if (op.referenceId == null) {
        validNodes.add(op);
      }
      else {
        P_OperationElement refElement = ops.get(op.referenceId);
        if (refElement != null) {
          if (op.execAfterReference) {
            refElement.children.add(op);
            op.parent = refElement;
          }
          else {
            op.children.add(refElement);
            refElement.parent = op;
          }
          validNodes.add(op);
        }
        else {
          invalidNodes.add(op); // the reference of this item could not be found -> execute at the end
          ScoutSdk.logWarning("New Project Operation reference '" + op.referenceId + "' defined by '" + op.op.getClass().getName() + "' could not be found.");
        }
      }
    }

    // find root nodes and check that at least one root exists
    Collection<P_OperationElement> roots = getRootOperations(validNodes);
    if (roots.size() == 0) {
      throw new IllegalArgumentException("not operation root node could be found! Check the new project operation references.");
    }

    // traverse the tree collecting all items in a level-order-traversal into a flat list
    // if several root nodes have been found (independent trees), the trees are executed in serial order (complete first tree, complete second tree, ...)
    LinkedHashSet<P_OperationElement> collector = new LinkedHashSet<P_OperationElement>(ops.size());
    for (P_OperationElement root : roots) {
      breadthFirstTraverse(root, collector);
    }

    // add all invalid nodes to the end assuming that no one depends on them but they may depend on others (which are not present)
    collector.addAll(invalidNodes);

    /**
     * Workaround: required because java files created with
     * org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation do not fire all events!
     * Also used in org.eclipse.scout.sdk.operation.project.add.ScoutProjectAddOperation
     * Can be removed when InstallJavaFileOperation has been removed.
     */
    try {
      for (IPrimaryTypeTypeHierarchy h : ScoutSdkCore.getHierarchyCache().getAllCachedHierarchies()) {
        h.invalidate();
      }
    }
    catch (Exception e) {
      //nop
    }

    // execute the operations
    execOperations(monitor, workingCopyManager, collector.toArray(new P_OperationElement[collector.size()]));

    // async post processing
    OperationJob postProcessJob = new OperationJob(new P_PostProcessOperation(getCreatedBundlesList()));
    postProcessJob.schedule(200);
  }

  private Collection<P_OperationElement> getRootOperations(Collection<P_OperationElement> nodes) {
    ArrayList<P_OperationElement> roots = new ArrayList<P_OperationElement>();
    for (P_OperationElement el : nodes) {
      if (el.parent == null) {
        roots.add(el);
      }
    }
    return roots;
  }

  private void breadthFirstTraverse(P_OperationElement root, Collection<P_OperationElement> collector) {
    Deque<P_OperationElement> deck = new ArrayDeque<P_OperationElement>();
    deck.addLast(root);
    while (!deck.isEmpty()) {
      P_OperationElement el = deck.removeFirst();
      for (P_OperationElement child : el.children) {
        if (collector.contains(child)) {
          throw new IllegalArgumentException("Cycle detected in new scout project operation tree.");
        }
        deck.addLast(child);
      }
      collector.add(el);
    }
  }

  protected String computeExecutionEnvironment() {

    String execEnv = EXEC_ENV_PREFIX + MIN_JVM_VERSION;
    IVMInstall defaultVm = JavaRuntime.getDefaultVMInstall();
    if (defaultVm != null) {
      for (IExecutionEnvironment env : JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()) {
        String executionEnvId = env.getId();
        if (executionEnvId.startsWith(EXEC_ENV_PREFIX) && env.isStrictlyCompatible(defaultVm)) {
          if (executionEnvId.compareTo(execEnv) > 0) {
            execEnv = executionEnvId; // take the newest
          }
        }
      }
    }
    return execEnv;
  }

  protected void putInitialProperties() {
    getProperties().setProperty(PROP_EXEC_ENV, computeExecutionEnvironment());
    getProperties().setProperty(PROP_OS, ScoutSdk.getDefault().getBundle().getBundleContext().getProperty(ICoreConstants.OSGI_OS));
    getProperties().setProperty(PROP_WS, ScoutSdk.getDefault().getBundle().getBundleContext().getProperty(ICoreConstants.OSGI_WS));
    getProperties().setProperty(PROP_ARCH, ScoutSdk.getDefault().getBundle().getBundleContext().getProperty(ICoreConstants.OSGI_ARCH));
    getProperties().setProperty(PROP_LOCALHOST, getHostName());
    getProperties().setProperty(PROP_CURRENT_DATE, SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT).format(new Date()));
    getProperties().setProperty(PROP_USER_NAME, ScoutUtility.getUsername());
    getProperties().setProperty(CreateSharedPluginOperation.PROP_TEXT_SERVICE_NAME, "Default");
  }

  private void execOperations(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, P_OperationElement[] ops) throws CoreException, IllegalArgumentException {
    putInitialProperties();
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

  private static class P_PostProcessOperation implements IOperation {

    private final List<IJavaProject> m_projectsToPostProcess;

    private P_PostProcessOperation(List<IJavaProject> projectsToPostProcess) {
      m_projectsToPostProcess = projectsToPostProcess;
    }

    @Override
    public String getOperationName() {
      return "Post Processing created projects...";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      try {
        JdtUtility.waitForIndexesReady();
        if (m_projectsToPostProcess != null && m_projectsToPostProcess.size() > 0) {
          // format all projects so that they match the workspace settings
          for (IJavaProject jp : m_projectsToPostProcess) {
            formatProject(monitor, workingCopyManager, jp);
          }
        }
      }
      catch (Exception e) {
        ScoutSdk.logError("Cannot post process projects.", e);
      }
    }

    private static void formatProject(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, IJavaProject p) throws CoreException {
      for (IPackageFragment pck : p.getPackageFragments()) {
        for (ICompilationUnit u : pck.getCompilationUnits()) {

          if (!workingCopyManager.register(u, monitor)) {
            // it is already registered. perform a reconcile.
            workingCopyManager.reconcile(u, monitor);
          }

          JavaElementFormatOperation fo = new JavaElementFormatOperation(u, true);
          fo.validate();
          fo.run(monitor, workingCopyManager);
        }
      }
    }
  }

  private static class P_OperationElement {
    private final IScoutProjectNewOperation op;
    private final String id;
    private final String referenceId;
    private final boolean execAfterReference;
    private final ArrayList<P_OperationElement> children;
    private P_OperationElement parent;

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
