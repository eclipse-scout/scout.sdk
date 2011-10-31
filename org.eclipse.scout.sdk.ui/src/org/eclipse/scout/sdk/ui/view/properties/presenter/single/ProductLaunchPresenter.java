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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.core.iproduct.IConfigurationFileInfo;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.ui.launcher.EclipseLaunchShortcut;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.FileOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>{@link ProductLaunchPresenter}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
@SuppressWarnings("restriction")
public class ProductLaunchPresenter extends AbstractPresenter {

  private String m_productName;
  private final IFile m_productFile;
  private final IScoutBundle m_bundle;

  // internal
  private ImageHyperlink m_runLink;
  private ImageHyperlink m_debugLink;
  private ImageHyperlink m_stopLink;
  private Group m_mainGroup;

  private P_LaunchListener m_launchListener;
  private P_RecomputeLaunchStateJob m_stateUpdateJob;
  private WorkspaceProductModel m_productModel;

  private final static Pattern PATTERN = Pattern.compile("name\\s*\\=\\s*(\\\")?([^\\\"]*)\\\"", Pattern.MULTILINE);

  /**
   * @param toolkit
   * @param parent
   */
  public ProductLaunchPresenter(FormToolkit toolkit, Composite parent, IFile productFile, IScoutBundle bundle) {
    super(toolkit, parent);
    m_productFile = productFile;
    m_bundle = bundle;
    String prodName = null;
    String productFileContent = "";
    /*WorkspaceProductModel productModel = null;
    try {
      productModel = ScoutSdkUtility.getProductModel(productFile, true);

      productFileContent = IOUtility.getContent(new InputStreamReader(productFile.getContents()));
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not read product file '" + productFile.getFullPath() + "'.", e);

    }*/
    Matcher m = PATTERN.matcher(productFileContent);
    if (m.find()) {
      prodName = m.group(2);
    }
    if (prodName == null) {
      prodName = productFile.getParent().getName() + " " + productFile.getName();
    }
    m_productName = prodName;
    //System.out.println("name: " + productModel.getProduct().getName() + " " + m_productName);
    create(getContainer(), productFile);
    m_launchListener = new P_LaunchListener();
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(m_launchListener);
    getContainer().addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        dispose();
      }
    });
    m_stateUpdateJob = new P_RecomputeLaunchStateJob();
    m_stateUpdateJob.schedule();
  }

  @Override
  public void dispose() {
    super.dispose();
    if (m_launchListener != null) {
      try {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(m_launchListener);
        m_launchListener = null;
      }
      catch (Exception e) {
        ScoutSdkUi.logError("could not remove launch listener on debug plugin.", e);
      }
    }
    if (m_stateUpdateJob != null) {
      m_stateUpdateJob.m_canceled = true;
      m_stateUpdateJob = null;
    }
  }

  /**
   * @param container
   * @param productFile
   */
  protected void create(Composite parent, IFile productFile) {
    m_mainGroup = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    m_mainGroup.setText(productFile.getParent().getName());

    Label l = getToolkit().createLabel(m_mainGroup, "");
    switch (getBundle().getType()) {
      case IScoutBundle.BUNDLE_SERVER:
        l.setImage(ScoutSdkUi.getImage(ScoutSdkUi.LauncherServer));
        break;
      case IScoutBundle.BUNDLE_UI_SWING:
        l.setImage(ScoutSdkUi.getImage(ScoutSdkUi.LauncherSwing));
        break;
      case IScoutBundle.BUNDLE_UI_SWT:
        l.setImage(ScoutSdkUi.getImage(ScoutSdkUi.LauncherSwt));
        break;
    }
    m_productModel = null;
    try {
      m_productModel = new WorkspaceProductModel(getProductFile(), false);
      m_productModel.load();
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning("could not load product model of '" + getProductFile() + "'.", e);
      return;
    }
    Control linkPart = createLinkPart(m_mainGroup);
    Control actionPart = createActionPart(m_mainGroup);

    //layout
    parent.setLayout(new FillLayout());
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    layout.horizontalSpacing = 0;
    m_mainGroup.setLayout(layout);
    l.setLayoutData(new GridData(GridData.FILL_VERTICAL));
    linkPart.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    GridData actionPartData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
    actionPartData.minimumWidth = actionPart.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
//    actionPartData.minimumWidth = 50;
    actionPartData.horizontalAlignment = SWT.RIGHT;
    actionPart.setLayoutData(actionPartData);
  }

  protected Control createLinkPart(Composite parent) {
    LinksPresenterModel model = new LinksPresenterModel();
    model.addGlobalLink(new FileOpenLink(getProductFile(), 10, IPDEUIConstants.PRODUCT_EDITOR_ID));
    // find config.ini
    IConfigurationFileInfo info = m_productModel.getProduct().getConfigurationFileInfo();
    String path = info.getPath(Platform.getOS());
    if (path == null) {
      path = info.getPath(null);
    }
    if (!StringUtility.isNullOrEmpty(path)) {
      IFile configIniFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
      model.addGlobalLink(new FileOpenLink(configIniFile, 20, EditorsUI.DEFAULT_TEXT_EDITOR_ID));
    }

    LinksPresenter presenter = new LinksPresenter(getToolkit(), parent, model);
    return presenter.getContainer();
  }

  protected Control createActionPart(Composite parent) {
    Composite container = getToolkit().createComposite(parent);
    m_runLink = getToolkit().createImageHyperlink(container, SWT.NONE);
    m_runLink.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolRun));
    m_runLink.setToolTipText(Texts.get("StartProduct"));
    m_runLink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        restartProduct(false);
      }
    });
    m_debugLink = getToolkit().createImageHyperlink(container, SWT.NONE);
    m_debugLink.setToolTipText(Texts.get("StartProductInDebugMode"));
    m_debugLink.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolDebug));
    m_debugLink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        restartProduct(true);
      }
    });
    m_stopLink = getToolkit().createImageHyperlink(container, SWT.NONE);
    m_stopLink.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolStop));
    m_stopLink.setToolTipText(Texts.get("StopProduct"));
    m_stopLink.setEnabled(false);
    m_stopLink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        stopProduct();
      }
    });

//    // export war
//    if (getBundle().getType() == IScoutBundle.BUNDLE_SERVER) {
//      ImageHyperlink exportWar = getToolkit().createImageHyperlink(container, SWT.NONE);
//      exportWar.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ServerBundleExport));
//      exportWar.setToolTipText("Export war file...");
//      exportWar.addHyperlinkListener(new HyperlinkAdapter() {
//        @Override
//        public void linkActivated(HyperlinkEvent e) {
//          OperationJob job = new OperationJob(new ExportServerWarOperation(m_productModel));
//          job.schedule();
//        }
//      });
//      GridData layoutData = new GridData(GridData.FILL_BOTH);
//      layoutData.heightHint = 16;
//      exportWar.setLayoutData(layoutData);
//    }

    //layout
    GridLayout layout = new GridLayout(1, false);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;

    container.setLayout(layout);
    GridData layoutData = new GridData(GridData.FILL_BOTH);

    m_runLink.setLayoutData(layoutData);
    layoutData = new GridData(GridData.FILL_BOTH);
    m_debugLink.setLayoutData(layoutData);
    layoutData = new GridData(GridData.FILL_BOTH);
    layoutData.heightHint = 16;
    m_stopLink.setLayoutData(layoutData);
    return container;
  }

  /**
   * @return the productFile
   */
  public IFile getProductFile() {
    return m_productFile;
  }

  /**
   * @return the bundle
   */
  public IScoutBundle getBundle() {
    return m_bundle;
  }

  /**
   * @return the productName
   */
  public String getProductName() {
    return m_productName;
  }

  private Job startProduct(final boolean debug) {
    Job job = new Job("starting '" + getProductName() + "' product...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        EclipseLaunchShortcut shortCut = new EclipseLaunchShortcut();
        shortCut.launch(new StructuredSelection(m_productFile), (debug) ? (ILaunchManager.DEBUG_MODE) : (ILaunchManager.RUN_MODE));
        return Status.OK_STATUS;
      }
    };
    job.setUser(false);
    job.setRule(getProductFile());
    job.schedule();
    return job;
  }

  private Job stopProduct() {
    Job job = new Job("stopping  '" + getProductName() + "' product...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
          for (ILaunch l : lm.getLaunches()) {
            ILaunchConfiguration lc = l.getLaunchConfiguration();
            if (getProductFile().getName().equals(lc.getName())) {
              l.terminate();
              for (int i = 0; i < 50; i++) {
                if (l.isTerminated()) {
                  break;
                }
                else {
                  Thread.sleep(100);
                }
              }
              recomputeLaunchState();
              break;
            }
          }
        }
        catch (Exception e) {
          ScoutSdkUi.logError("could not stop product '" + getProductFile().getName() + "'", e);
        }
        return Status.OK_STATUS;
      }
    };
    job.setUser(false);
    job.setRule(getProductFile());
    job.schedule();
    return job;
  }

  private void restartProduct(boolean debug) {
    Job stopJob = stopProduct();
    try {
      stopJob.join();
      startProduct(debug);
    }
    catch (InterruptedException e) {
      ScoutSdkUi.logError("error during stopping product '" + getProductName() + "'", e);
    }
  }

  private void recomputeLaunchState() {
    getContainer().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch l : launches) {
          if (l.getLaunchConfiguration().getName().equals(getProductFile().getName())) {
            if (l.isTerminated()) {
              m_stopLink.setEnabled(false);
              m_mainGroup.setText(getProductFile().getParent().getName());
            }
            else {
              m_stopLink.setEnabled(true);
              if (l.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
                m_mainGroup.setText(getProductFile().getParent().getName() + " - " + Texts.get("debugging") + "...");
              }
              else if (l.getLaunchMode().equals(ILaunchManager.RUN_MODE)) {
                m_mainGroup.setText(getProductFile().getParent().getName() + " - " + Texts.get("Running") + "...");
              }
              else {
                m_mainGroup.setText(getProductFile().getParent().getName() + " - " + Texts.get("UndefinedState") + "...");
              }
            }
          }
        }
      }
    });
  }

  private class P_LaunchListener implements ILaunchListener {
    @Override
    public void launchAdded(ILaunch launch) {
      recomputeLaunchState();
    }

    @Override
    public void launchChanged(ILaunch launch) {
      recomputeLaunchState();
    }

    @Override
    public void launchRemoved(ILaunch launch) {
      recomputeLaunchState();
    }
  } // end class P_LaunchListener

  private class P_RecomputeLaunchStateJob extends Job {

    private boolean m_canceled = false;

    public P_RecomputeLaunchStateJob() {
      super("");
      setSystem(true);
      setPriority(Job.DECORATE);
      setRule(getProductFile());
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (!m_canceled) {
        recomputeLaunchState();
        schedule(2500);
      }
      return Status.OK_STATUS;
    }
  } // end class P_RecomputeLaunchStateJob

}
