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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.ui.launcher.EclipseLaunchShortcut;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.FileOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>{@link ProductLaunchPresenter}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
@SuppressWarnings("restriction")
public class ProductLaunchPresenter extends AbstractPresenter {
  private String m_curLaunchState;

  private final String m_productName;
  private final IFile m_productFile;
  private final String m_productType;

  private ImageHyperlink m_runLink;
  private ImageHyperlink m_debugLink;
  private ImageHyperlink m_stopLink;
  private Group m_mainGroup;

  private P_LaunchListener m_launchListener;
  private P_RecomputeLaunchStateJob m_stateUpdateJob;

  private static final Object LOCK = new Object();
  private static final String MAC_OS_X_SWING_WARNING_MESSAGE_KEY = "scoutSwingMacOsXWarningKey";
  public static final String TERMINATED_MODE = "terminated";

  /**
   * @param toolkit
   * @param parent
   * @throws CoreException
   */
  public ProductLaunchPresenter(PropertyViewFormToolkit toolkit, Composite parent, IFile productFile) throws CoreException {
    super(toolkit, parent);
    m_productFile = productFile;
    m_productName = productFile.getParent().getName() + " " + productFile.getName();
    m_productType = ScoutUtility.getProductFileType(getProductFile());

    create(getContainer());

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
  protected void create(Composite parent) {
    m_mainGroup = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    m_mainGroup.setText(getProductFile().getName());

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
    linkPart.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    GridData actionPartData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
    actionPartData.minimumWidth = actionPart.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
    actionPartData.horizontalAlignment = SWT.RIGHT;
    actionPart.setLayoutData(actionPartData);
  }

  protected Control createLinkPart(Composite parent) {
    LinksPresenterModel model = new LinksPresenterModel();
    model.addGlobalLink(new FileOpenLink(getProductFile(), 10, IPDEUIConstants.PRODUCT_EDITOR_ID));

    try {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(getProductFile());
      IFile configIni = pfmh.ConfigurationFile.getFile();
      if (configIni != null) {
        model.addGlobalLink(new FileOpenLink(configIni, 20, EditorsUI.DEFAULT_TEXT_EDITOR_ID));
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning("Unable to find config.ini file for product '" + getProductFile().getLocation().toOSString() + "'.", e);
    }

    try {
      ScoutBundleUiExtension uiExt = ScoutBundleExtensionPoint.getExtension(getProductType());
      if (uiExt != null && uiExt.getProductLauncherContributor() != null) {
        uiExt.getProductLauncherContributor().contributeLinks(getProductFile(), model);
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error while loading product launcher link contributor for product '" + getProductFile().getLocation().toOSString() + "'.", e);
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
   * @return the productName
   */
  public String getProductName() {
    return m_productName;
  }

  private Job startProduct(final boolean debug) {

    showMacOsXWarnings();

    Job job = new Job("starting '" + getProductName() + "' product...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          EclipseLaunchShortcut shortCut = new EclipseLaunchShortcut();
          int iteration = 0;
          boolean success = false;
          while (!success && iteration < 10) {
            try {
              shortCut.launch(new StructuredSelection(m_productFile), (debug) ? (ILaunchManager.DEBUG_MODE) : (ILaunchManager.RUN_MODE));
              success = true;
            }
            catch (NullPointerException npe) {
              // sometimes happens in PDE. try again
              Thread.sleep(200);
            }
            iteration++;
          }
        }
        catch (Exception e) {
          ScoutSdkUi.logInfo("could not start product '" + getProductFile().getName() + "'", e);
        }
        return Status.OK_STATUS;
      }
    };
    job.setUser(false);
    job.setRule(getProductFile());
    job.schedule();
    return job;
  }

  private void showMacOsXWarnings() {
    if (Platform.OS_MACOSX.equals(Platform.getOS())) {
      try {
        IPreferenceStore store = ScoutSdkUi.getDefault().getPreferenceStore();
        ProductFileModelHelper pfmh = new ProductFileModelHelper(m_productFile);
        if (pfmh.ProductFile.existsDependency(IRuntimeClasses.ScoutUiSwingBundleId)) {
          // it is a swing product to be launched on Mac OS X: show help box
          String doNotShowAgainString = store.getString(MAC_OS_X_SWING_WARNING_MESSAGE_KEY);
          boolean doNotShowAgain = MessageDialogWithToggle.ALWAYS.equals(doNotShowAgainString);
          if (!doNotShowAgain) {
            MessageDialogWithToggle.openWarning(ScoutSdkUi.getShell(), Texts.get("MacOsXSwingWarningTitle"), Texts.get("MacOsXSwingWarningMessage"),
                Texts.get("DoNotShowAgain"), false, store, MAC_OS_X_SWING_WARNING_MESSAGE_KEY);
          }
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError(e);
      }
    }
  }

  private Job stopProduct() {
    Job job = new Job("stopping  '" + getProductName() + "' product...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
          for (ILaunch l : lm.getLaunches()) {
            ILaunchConfiguration lc = l.getLaunchConfiguration();
            if (lc != null && getProductFile().getName().equals(lc.getName())) {
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

  private void refreshUiLaunchState(String mode) {
    if (!CompareUtility.equals(mode, m_curLaunchState)) {
      m_curLaunchState = mode;
      if (!m_stopLink.isDisposed() && !m_mainGroup.isDisposed()) {
        m_stopLink.setEnabled(!TERMINATED_MODE.equals(mode));
        if (TERMINATED_MODE.equals(mode)) {
          m_mainGroup.setText(getProductFile().getName());
        }
        else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
          m_mainGroup.setText(getProductFile().getName() + " - " + Texts.get("debugging") + "...");
        }
        else if (ILaunchManager.RUN_MODE.equals(mode)) {
          m_mainGroup.setText(getProductFile().getName() + " - " + Texts.get("Running") + "...");
        }
      }
      try {
        ScoutBundleUiExtension uiExt = ScoutBundleExtensionPoint.getExtension(getProductType());
        if (uiExt != null && uiExt.getProductLauncherContributor() != null) {
          uiExt.getProductLauncherContributor().refreshLaunchState(mode);
        }
      }
      catch (Exception e) {
        ScoutSdkUi.logWarning("Error while refreshing launch state for product '" + getProductFile().getLocation().toOSString() + "'.", e);
      }
    }
  }

  private void recomputeLaunchState() {
    synchronized (LOCK) {
      if (getContainer() != null && !getContainer().isDisposed()) {
        getContainer().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
            boolean launchRunning = false;
            for (ILaunch l : launches) {
              if (l.getLaunchConfiguration() != null && l.getLaunchConfiguration().getName().equals(getProductFile().getName())) {
                launchRunning = true;
                if (l.isTerminated()) {
                  refreshUiLaunchState(TERMINATED_MODE);
                }
                else {
                  refreshUiLaunchState(l.getLaunchMode());
                }
              }
            }

            if (!launchRunning) {
              refreshUiLaunchState(TERMINATED_MODE);
            }
          }
        });
      }
    }
  }

  public String getProductType() {
    return m_productType;
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
