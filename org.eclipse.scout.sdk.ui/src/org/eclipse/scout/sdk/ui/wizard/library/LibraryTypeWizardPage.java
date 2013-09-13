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
package org.eclipse.scout.sdk.ui.wizard.library;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.dialog.ScoutBundleSelectionDialog;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.viewer.ScoutBundleLableProvider;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/**
 * <h3>{@link LibraryTypeWizardPage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 06.03.2012
 */
@SuppressWarnings("restriction")
public class LibraryTypeWizardPage extends AbstractWorkspaceWizardPage {
  protected static final String PROP_BUNDLE_NAME = "bundleName";
  protected static final String PROP_UNPACK = "unpack";
  protected static final String PROP_LIBRARY_TYPE = "libarayType";
  protected static final String PROP_FRAGMENT_HOST = "fragmentHost";
  protected static final String PROP_USER_BUNDLES = "userBundles";

  public static enum LibraryType {
    Plugin,
    Fragment,
    SystemBundleFragment
  }

  private StyledTextField m_bundleNameField;
  private Button m_unpackButton;
  private Button m_pluginRadioButton;
  private Button m_systemFragmentRadioButton;
  private Button m_fragmentRadioButton;
  private StyledTextField m_hostBundleIdField;
  private Button m_fragmentHostBundleSelectionButton;
  private Button m_addUserBundleButton;
  private Button m_removeUserBundleButton;
  private TableViewer m_userBundleViewer;

  // process members

  /**
   * @param pageName
   */
  public LibraryTypeWizardPage(IScoutBundle ownerBundle) {
    super(LibraryTypeWizardPage.class.getName());

    setTitle(Texts.get("NewLibraryBundle"));
    setDescription(Texts.get("NewLibraryBundleDesc"));
    // defaults
    setLibraryType(LibraryType.Plugin);
    TreeSet<IScoutBundle> libraryUserBundles = new TreeSet<IScoutBundle>(new P_JavaProjectComparator());
    if (ownerBundle != null) {
      libraryUserBundles.add(ownerBundle);
    }
    setLibraryUserBundles(libraryUserBundles);
  }

  @Override
  protected void createContent(Composite parent) {
    m_bundleNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("BundleName"));
    m_bundleNameField.setText(getBundleName());
    m_bundleNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setBundleNameInternal(m_bundleNameField.getText());
        pingStateChanging();
      }
    });

    m_unpackButton = new Button(parent, SWT.CHECK);
    m_unpackButton.setVisible(false);
    m_unpackButton.setText(Texts.get("UnpackJarFiles"));
    m_unpackButton.setSelection(isUnpackJarFiles());
    m_unpackButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUnpackJarFilesInternal(m_unpackButton.getSelection());
        pingStateChanging();
      }
    });

    Control strategyBox = createStrategyBox(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_bundleNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_unpackButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    strategyBox.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
  }

  private Control createStrategyBox(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    final SelectionListener radioButtonListener = new P_RadioButtonSelectionListener();
    m_pluginRadioButton = new Button(group, SWT.RADIO);
    m_pluginRadioButton.setSelection(getLibraryType() == LibraryType.Plugin);
    m_pluginRadioButton.addSelectionListener(radioButtonListener);
    m_pluginRadioButton.setData(PROP_LIBRARY_TYPE, LibraryType.Plugin);
    m_pluginRadioButton.setText(Texts.get("CreatePluginForLib"));
    Control userPluginBox = createUserPluginBox(group);

    //line separator
    Label separator2 = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);

    m_fragmentRadioButton = new Button(group, SWT.RADIO);
    m_fragmentRadioButton.setSelection(getLibraryType() == LibraryType.Fragment);
    m_fragmentRadioButton.addSelectionListener(radioButtonListener);
    m_fragmentRadioButton.setData(PROP_LIBRARY_TYPE, LibraryType.Fragment);
    m_fragmentRadioButton.setText(Texts.get("CreateFragmentForLib"));

    Control fragmentBox = createFragmentBox(group);

    //line separator
    Label separator3 = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator2.setText("test");

    m_systemFragmentRadioButton = new Button(group, SWT.RADIO);
    m_systemFragmentRadioButton.setSelection(getLibraryType() == LibraryType.SystemBundleFragment);
    m_systemFragmentRadioButton.addSelectionListener(radioButtonListener);
    m_systemFragmentRadioButton.setData(PROP_LIBRARY_TYPE, LibraryType.SystemBundleFragment);
    m_systemFragmentRadioButton.setText(Texts.get("CreateSystemFragmentForLib"));

    //layout
    group.setLayout(new GridLayout(1, true));

    m_pluginRadioButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    userPluginBox.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    separator2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_fragmentRadioButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    fragmentBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    separator3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_systemFragmentRadioButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    return group;
  }

  protected Control createUserPluginBox(Composite parent) {
    Composite box = new Composite(parent, SWT.NONE);
    Table pluginTable = new Table(box, SWT.FULL_SELECTION | SWT.BORDER);
    m_userBundleViewer = new TableViewer(pluginTable);
    m_userBundleViewer.getTable().setEnabled(getLibraryType() == LibraryType.Plugin);
    m_userBundleViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        m_removeUserBundleButton.setEnabled(!event.getSelection().isEmpty());
      }
    });
    m_userBundleViewer.setLabelProvider(new P_UserBundleLabelProvider());
    m_userBundleViewer.setContentProvider(new P_UserBundleContentProvider());
    Set<IScoutBundle> libraryUserBundles = getLibraryUserBundles();
    m_userBundleViewer.setInput(libraryUserBundles.toArray(new Object[libraryUserBundles.size()]));
    Composite buttonGroup = new Composite(box, SWT.NONE);
    m_addUserBundleButton = new Button(buttonGroup, SWT.PUSH | SWT.FLAT);
    m_addUserBundleButton.setText(Texts.get("Add"));
    m_addUserBundleButton.setEnabled(getLibraryType() == LibraryType.Plugin);
    m_addUserBundleButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showUserSelectionDialog();
      }
    });
    m_removeUserBundleButton = new Button(buttonGroup, SWT.PUSH | SWT.FLAT);
    m_removeUserBundleButton.setText(Texts.get("Remove"));
    m_removeUserBundleButton.setEnabled(false);
    m_removeUserBundleButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ISelection currentSelection = m_userBundleViewer.getSelection();
        if (!currentSelection.isEmpty()) {
          Set<IScoutBundle> userBundles = getLibraryUserBundles();
          if (userBundles.remove((IScoutBundle) ((IStructuredSelection) currentSelection).getFirstElement())) {
            setLibraryUserBundles(userBundles);
          }
        }
      }
    });

    // layout
    box.setLayout(new GridLayout(2, false));
    pluginTable.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    buttonGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING));

    buttonGroup.setLayout(new GridLayout(1, true));
    m_addUserBundleButton.setLayoutData(new GridData(GridData.FILL_BOTH));
    m_removeUserBundleButton.setLayoutData(new GridData(GridData.FILL_BOTH));
    return box;
  }

  protected Control createFragmentBox(Composite parent) {
    Composite box = new Composite(parent, SWT.NONE);
    m_hostBundleIdField = getFieldToolkit().createStyledTextField(box, "Host Bundle ID");
    m_hostBundleIdField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setFragmentHostInternal(m_hostBundleIdField.getText());
        pingStateChanging();
      }
    });
    m_hostBundleIdField.setEnabled(getLibraryType() == LibraryType.Fragment);
    m_fragmentHostBundleSelectionButton = new Button(m_hostBundleIdField, SWT.PUSH | SWT.FLAT);
    m_fragmentHostBundleSelectionButton.setText(Texts.get("Bundle"));
    m_fragmentHostBundleSelectionButton.setEnabled(getLibraryType() == LibraryType.Fragment);
    m_fragmentHostBundleSelectionButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showBundleSelectionDialog();
      }
    });

    // layout
    GridLayout layout = new GridLayout(1, true);
    layout.marginLeft = 10;
    box.setLayout(layout);
    m_hostBundleIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    // adjust internal textfield layout to layout also the open button
    StyledText textComp = m_hostBundleIdField.getTextComponent();
    FormData textData = (FormData) textComp.getLayoutData();
    textData.right = new FormAttachment(m_fragmentHostBundleSelectionButton, -5);
    FormData buttonData = new FormData();
    buttonData.top = new FormAttachment(0, 0);
    buttonData.right = new FormAttachment(100, 0);
    buttonData.bottom = new FormAttachment(100, 0);
    m_fragmentHostBundleSelectionButton.setLayoutData(buttonData);
    return box;
  }

  private void showBundleSelectionDialog() {
    BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
      @Override
      public void run() {
        PluginSelectionDialog dialog = new PluginSelectionDialog(getShell().getShell(), false, false);
        dialog.setInitialPattern("**");
        dialog.create();
        if (dialog.open() == Window.OK) {
          IPluginModel model = (IPluginModel) dialog.getFirstResult();
          IPlugin plugin = model.getPlugin();
          setFragmentHost(plugin.getId());
        }
      }
    });
  }

  private void showUserSelectionDialog() {
    BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
      @Override
      public void run() {
        // find all available bundles
        Set<IScoutBundle> alreadyAddedBundles = getLibraryUserBundles();
        IScoutBundle[] workspaceBundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getWorkspaceBundlesFilter());
        List<IScoutBundle> plugins = new ArrayList<IScoutBundle>(workspaceBundles.length);
        for (IScoutBundle bundle : workspaceBundles) {
          if (!alreadyAddedBundles.contains(bundle)) {
            plugins.add(bundle);
          }
        }
        ScoutBundleSelectionDialog dialog = new ScoutBundleSelectionDialog(getControl().getShell(), plugins.toArray(new IScoutBundle[plugins.size()]), true);
        dialog.create();
        if (dialog.open() == Window.OK) {
          Set<IScoutBundle> userBundles = alreadyAddedBundles;
          Object[] result = dialog.getResult();
          for (Object o : result) {
            if (o instanceof IScoutBundle) {
              userBundles.add((IScoutBundle) o);
            }
          }
          setLibraryUserBundles(userBundles);
        }
      }
    });
  }

  public String getBundleName() {
    return getPropertyString(PROP_BUNDLE_NAME);
  }

  public void setBundleName(String bundleName) {
    try {
      setStateChanging(true);
      setBundleNameInternal(bundleName);
      if (isControlCreated()) {
        m_bundleNameField.setText(bundleName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setBundleNameInternal(String bundleName) {
    setProperty(PROP_BUNDLE_NAME, bundleName);
  }

  public boolean isUnpackJarFiles() {
    return getPropertyBool(PROP_UNPACK);
  }

  public void setUnpackJarFiles(boolean unpack) {
    try {
      setStateChanging(true);
      setUnpackJarFilesInternal(unpack);
      if (isControlCreated()) {
        m_unpackButton.setSelection(unpack);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setUnpackJarFilesInternal(boolean unpack) {
    setProperty(PROP_UNPACK, unpack);
  }

  public LibraryType getLibraryType() {
    return (LibraryType) getProperty(PROP_LIBRARY_TYPE);
  }

  public void setLibraryType(LibraryType libraryType) {
    try {
      setStateChanging(true);
      setLibraryTypeInternal(libraryType);
      if (isControlCreated()) {
        switch (libraryType) {
          case Plugin:
            m_pluginRadioButton.setSelection(true);
            break;
          case Fragment:
            m_fragmentRadioButton.setSelection(true);
            break;
          case SystemBundleFragment:
            m_systemFragmentRadioButton.setSelection(true);
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }

  }

  private void setLibraryTypeInternal(LibraryType libraryType) {
    setProperty(PROP_LIBRARY_TYPE, libraryType);
  }

  public Set<IScoutBundle> getLibraryUserBundles() {
    @SuppressWarnings("unchecked")
    Set<IScoutBundle> libraryUserBundles = (Set<IScoutBundle>) getProperty(PROP_USER_BUNDLES);
    if (libraryUserBundles == null) {
      libraryUserBundles = new TreeSet<IScoutBundle>();
    }
    return libraryUserBundles;
  }

  public void setLibraryUserBundles(Set<IScoutBundle> libraryUsers) {
    try {
      setStateChanging(true);
      setLibraryUserBundlesInternal(libraryUsers);
      if (isControlCreated()) {
        m_userBundleViewer.setInput(libraryUsers.toArray(new Object[libraryUsers.size()]));
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setLibraryUserBundlesInternal(Set<IScoutBundle> libraryUsers) {
    setPropertyAlwaysFire(PROP_USER_BUNDLES, libraryUsers);
  }

  public String getFragmentHost() {
    return getPropertyString(PROP_FRAGMENT_HOST);
  }

  public void setFragmentHost(String bundleId) {
    try {
      setStateChanging(true);
      setFragmentHostInternal(bundleId);
      if (isControlCreated()) {
        m_hostBundleIdField.setText(bundleId);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFragmentHostInternal(String bundleId) {
    setPropertyString(PROP_FRAGMENT_HOST, bundleId);
  }

  // validation
  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusBundleName());
    validateStatusFragmentGroup(multiStatus);
  }

  protected IStatus getStatusBundleName() {
    return ScoutUtility.validateNewBundleName(getBundleName());
  }

  protected void validateStatusFragmentGroup(MultiStatus multiStatus) {
    if (getLibraryType() == LibraryType.Fragment) {
      multiStatus.add(getStatusHostBundle());
    }
  }

  protected IStatus getStatusHostBundle() {
    String fragmentHost = getFragmentHost();
    if (StringUtility.isNullOrEmpty(fragmentHost)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("FragmentHostBundleNotFound"));
    }
    else if (Platform.getBundle(fragmentHost) == null && !ResourcesPlugin.getWorkspace().getRoot().getProject(fragmentHost).exists()) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("FragmentHostBundleNotExists"));
    }
    else {
      return Status.OK_STATUS;
    }
  }

  private class P_RadioButtonSelectionListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      Button button = (Button) e.widget;
      if (button.getSelection()) {
        final LibraryType libraryType = (LibraryType) button.getData(PROP_LIBRARY_TYPE);
        try {
          setStateChanging(true);
          switch (libraryType) {
            case Plugin:
              m_hostBundleIdField.setEnabled(false);
              m_fragmentHostBundleSelectionButton.setEnabled(false);
              m_userBundleViewer.getTable().setEnabled(true);
              m_addUserBundleButton.setEnabled(true);
              m_removeUserBundleButton.setEnabled(!m_userBundleViewer.getSelection().isEmpty());
              break;
            case Fragment:
              m_hostBundleIdField.setEnabled(true);
              m_fragmentHostBundleSelectionButton.setEnabled(true);
              m_userBundleViewer.getTable().setEnabled(false);
              m_addUserBundleButton.setEnabled(false);
              m_removeUserBundleButton.setEnabled(false);
              break;
            case SystemBundleFragment:
              m_hostBundleIdField.setEnabled(false);
              m_fragmentHostBundleSelectionButton.setEnabled(false);
              m_userBundleViewer.getTable().setEnabled(false);
              m_addUserBundleButton.setEnabled(false);
              m_removeUserBundleButton.setEnabled(false);
              break;
          }
          setLibraryTypeInternal(libraryType);
        }
        finally {
          setStateChanging(false);
        }
      }
    }

  }

  private class P_UserBundleLabelProvider extends ScoutBundleLableProvider implements ITableLabelProvider {

    @Override
    public String getColumnText(Object element, int columnIndex) {
      return getText(element);
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return getImage(element);
    }
  }

  private class P_UserBundleContentProvider implements IStructuredContentProvider {
    private Object[] m_elements = null;

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      m_elements = (Object[]) newInput;

    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_elements;
    }

  } // end class P_UserBundleLabelProvider

  private class P_JavaProjectComparator implements Comparator<IScoutBundle> {
    @Override
    public int compare(IScoutBundle o1, IScoutBundle o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      else if (o1 == null) {
        return -1;
      }
      else if (o2 == null) {
        return 1;
      }
      else {
        return CompareUtility.compareTo(o1.getSymbolicName(), o2.getSymbolicName());
      }
    }
  }
}
