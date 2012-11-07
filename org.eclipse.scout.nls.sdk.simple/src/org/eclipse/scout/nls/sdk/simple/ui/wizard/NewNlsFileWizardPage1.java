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
package org.eclipse.scout.nls.sdk.simple.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.FieldValidator;
import org.eclipse.scout.nls.sdk.internal.ui.TextButtonField;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldListener;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldModel;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.SmartField;
import org.eclipse.scout.nls.sdk.simple.operations.NewNlsFileOperationDesc;
import org.eclipse.scout.nls.sdk.util.concurrent.Lock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

@SuppressWarnings("restriction")
public class NewNlsFileWizardPage1 extends NewTypeWizardPage {

  private NewNlsFileOperationDesc m_desc;
  private Composite m_rootPane;
  private FieldValidator m_fieldValidator = new FieldValidator();

  private TextField<String> m_fileName;
  private SmartField m_nlsParentFile;
  private TextButtonField m_nlsParentPluginField;
  private P_ParentPluginModifyListener m_parentModifyListener;
  private TextButtonField m_pluginField;
  private Lock m_lock = new Lock();

  // private TextProposalField m_plugin;

  public NewNlsFileWizardPage1(String pageName, NewNlsFileOperationDesc desc) {
    super(true, "new NLS File");
    setTitle("Create a NLS file");
    setDescription("This file is provides an easy handling of translations");
    m_desc = desc;
    m_parentModifyListener = new P_ParentPluginModifyListener();
    setPageComplete(false);
  }

  @Override
  public void createControl(Composite parent) {
    m_rootPane = new Composite(parent, SWT.NONE);
    m_pluginField = new TextButtonField(m_rootPane);
    m_fieldValidator.addField(m_pluginField);
    m_pluginField.setLabelText("Plugin");
    m_pluginField.setButtonText("Browse");
    m_pluginField.addTextModifyListener(new P_PluginModifyListener());
    m_pluginField.addButtonSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IPluginModelBase model = choosePlugin();
        if (model != null) {
          IProject project = model.getUnderlyingResource().getProject();
          m_pluginField.setText(project.getName());
          m_desc.setPlugin(project);
        }
      }
    });
    m_fileName = new TextField<String>(m_rootPane, TextField.VALIDATE_ON_MODIFY);
    m_fieldValidator.addField(m_fileName);
    m_fileName.setLabelText("File Name");
    m_fileName.addInputChangedListener(new IInputChangedListener<String>() {
      @Override
      public void inputChanged(String input) {
        m_desc.setFileName(input);
      }
    });
    m_fileName.setInputValidator(new IInputValidator() {
      private String m_regexp = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b";

      @Override
      public IStatus isValid(String value) {
        if (value.matches(m_regexp)) {
          return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
      }
    });
    Control bindingType = createDynStatRadioBox(m_rootPane);

    // laoyut
    m_rootPane.setLayout(new GridLayout(1, false));
    attachGridData(m_pluginField);
    attachGridData(m_fileName);
    attachGridData(bindingType);
    initUi();

  }

  private Control createDynStatRadioBox(Composite parent) {
    Group checkBoxGroup = new Group(parent, SWT.NONE);
    checkBoxGroup.setText("Inherit Texts of");

    m_nlsParentPluginField = new TextButtonField(checkBoxGroup);
    m_fieldValidator.addField(m_nlsParentPluginField);
    m_nlsParentPluginField.setLabelText("Parent Plugin");
    m_nlsParentPluginField.setButtonText("Browse");
    m_nlsParentPluginField.addTextModifyListener(m_parentModifyListener);
    m_nlsParentPluginField.addButtonSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IPluginModelBase model = chooseParentPlugin();
        if (model != null) {
          m_nlsParentPluginField.setText(model.getBundleDescription().getName());
          m_desc.setParentPlugin(model);
        }
      }
    });

    m_nlsParentFile = new SmartField(checkBoxGroup, SWT.NONE);
    m_fieldValidator.addField(m_nlsParentFile);
    m_nlsParentFile.setLabel("Parent file");
    m_nlsParentFile.setSmartFieldModel(new P_NlsFileSmartFieldModel());
    m_nlsParentFile.addSmartFieldListener(new ISmartFieldListener() {
      @Override
      public void itemSelected(Object item) {
        m_desc.setParentFile((IFile) item);
      }
    });
    // layout
    checkBoxGroup.setLayout(new GridLayout(2, true));
    // attachGridData(m_dynamicBinding);
    // attachGridData(m_staticBinding);
    GridData data = new GridData();
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    m_nlsParentPluginField.setLayoutData(data);
    data = new GridData();
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    m_nlsParentFile.setLayoutData(data);
    return checkBoxGroup;
  }

  private void initUi() {
    m_desc.addPropertyChangeListener(new P_DescPropertyChangedListener());
    for (Entry<String, Object> entry : m_desc.getPropertiesMap().entrySet()) {
      handlePropertyChanged(entry.getKey(), null, entry.getValue());
    }
    revalidate();
  }

  protected IPluginModel choosePlugin() {
    PluginSelectionDialog dialog = new PluginSelectionDialog(getShell(), PDECore.getDefault().getModelManager()
        .getWorkspaceModels(), false);
    dialog.create();
    if (dialog.open() == Window.OK) {
      Object[] models = dialog.getResult();
      if (models.length > 0) {
        return (IPluginModel) models[0];
      }
    }
    return null;
  }

  protected IPluginModel chooseParentPlugin() {
    PluginSelectionDialog dialog = new PluginSelectionDialog(PDEPlugin.getActiveWorkbenchShell(),
        getAvailablePlugins(PDECore.getDefault().getModelManager().findModel(m_desc.getPlugin())), false);
    dialog.create();
    if (dialog.open() == Window.OK) {
      Object[] models = dialog.getResult();
      if (models.length > 0) {
        return (IPluginModel) models[0];
      }
    }
    return null;
  }

  private IPluginModelBase[] getAvailablePlugins(IPluginModelBase model) {
    /**
     * @rn imo
     */
    // IPluginModelBase[] plugins = PDECore.getDefault().getModelManager().getPluginsOnly();
    IPluginModelBase[] plugins = PDECore.getDefault().getModelManager().getActiveModels(false);

    // HashSet existingImports = PluginSelectionDialog.getExistingImports(model.getPluginBase());
    ArrayList<IPluginModelBase> result = new ArrayList<IPluginModelBase>();
    for (int i = 0; i < plugins.length; i++) {
      result.add(plugins[i]);
    }
    return result.toArray(new IPluginModelBase[result.size()]);
  }

  @Override
  public Control getControl() {
    return m_rootPane;
  }

  @Override
  public void setVisible(boolean visible) {
    if (m_rootPane != null && !m_rootPane.isDisposed()) {
      m_rootPane.setVisible(visible);
    }
  }

  private void revalidate() {
    // enable disable
    m_fieldValidator.reset();
    if (m_desc.getParentPlugin() == null) {
      m_fieldValidator.setDisabled(m_nlsParentFile);
    }
    m_fieldValidator.apply();

    // messages
    setMessage(null);
    if (m_desc.getPlugin() == null) {
      setMessage("Plugin must be specified!", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    if (m_desc.getFileName() == null) {
      setMessage("NLS File must be specified!", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    if (m_desc.getParentPlugin() != null && m_desc.getParentFile() == null) {
      setMessage("Parent file must be specified", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    setPageComplete(true);
  }

  private void attachGridData(Control c) {
    GridData data = new GridData();
    data.horizontalAlignment = SWT.FILL;
    data.grabExcessHorizontalSpace = true;
    c.setLayoutData(data);
  }

  private void handlePropertyChanged(String name, Object oldValue, Object newValue) {
    if (name.equals(NewNlsFileOperationDesc.PROP_PARENT_PLUGIN) && m_lock.acquire()) {
      try {
        IPluginModelBase p = (IPluginModelBase) newValue;
        if (p == null) {
          m_nlsParentPluginField.setText("");
        }
        else {
          m_nlsParentPluginField.setText(p.getBundleDescription().getName());
        }
      }
      finally {
        m_lock.release();
      }
    }
    else if (name.equals(NewNlsFileOperationDesc.PROP_PLUGIN)) {
      IProject p = (IProject) newValue;
      m_parentModifyListener.setPlugin(p);
      if (m_lock.acquire()) {
        try {

          if (p == null) {
            m_pluginField.setText("");
          }
          else {
            m_pluginField.setText(p.getName());
          }
        }
        finally {
          m_lock.release();
        }
      }
    }
    // if (name.equals(NewNlsFileOperationDesc.PROP_BINDING_DYNAMIC)) {
    // if (((Boolean) newValue).booleanValue()) {
    // m_dynamicBinding.setSelection(true);
    // m_nlsParentPluginField.setVisible(true);
    // m_nlsParentFile.setVisible(true);
    // } else {
    // m_staticBinding.setSelection(true);
    // m_nlsParentPluginField.setVisible(false);
    // m_nlsParentFile.setVisible(false);
    // }
    // }
    revalidate();
  }

  private class P_DescPropertyChangedListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handlePropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

  private class P_NlsFileSmartFieldModel implements ISmartFieldModel {
    public P_NlsFileSmartFieldModel() {
    }

    @Override
    public Image getImage(Object item) {
      return null;
    }

    @Override
    public List<Object> getProposals(String pattern) {
      List<IFile> files = new LinkedList<IFile>();
      Assert.isTrue(m_desc.getParentPlugin() != null);

      IResource r = m_desc.getParentPlugin().getUnderlyingResource();
      if (r != null) {
        IProject project = r.getProject();
        try {
          IJavaProject jp = JavaCore.create(project);
          for (Object o : jp.getNonJavaResources()) {
            if (o instanceof IFile && ((IFile) o).getFileExtension().equalsIgnoreCase("nls")) {
              files.add((IFile) o);
            }
          }
        }
        catch (CoreException e) {
          NlsCore.logWarning(e);
        }
      }
      return new ArrayList<Object>(files);
    }

    @Override
    public String getText(Object item) {
      return ((IFile) item).getName();
    }

  } // end class P_NlsFileSmartFieldModel

  private class P_PluginModifyListener implements ModifyListener {
    HashMap<String, IProject> m_projects = new HashMap<String, IProject>();

    P_PluginModifyListener() {
      // find all plugin projects in workspace
      for (IPluginModelBase model : PluginRegistry.getWorkspaceModels()) {
        IProject project = model.getUnderlyingResource().getProject();
        if (project != null) {
          m_projects.put(project.getName(), project);
        }
      }

    }

    @Override
    public void modifyText(ModifyEvent e) {
      if (m_lock.acquire()) {
        try {
          String input = m_pluginField.getText();
          if (m_projects.keySet().contains(input)) {
            m_desc.setPlugin(m_projects.get(input));
          }
          else {
            m_desc.setPlugin(null);
            setMessage("invalid plugin name", WizardPage.WARNING);
          }
        }
        finally {
          m_lock.release();
        }
      }
    }

  } // end class P_PluginModifyListener

  private class P_ParentPluginModifyListener implements ModifyListener {
    HashMap<String, IPluginModelBase> m_projects = new HashMap<String, IPluginModelBase>();

    P_ParentPluginModifyListener() {
      setPlugin(m_desc.getPlugin());
    }

    @Override
    public void modifyText(ModifyEvent e) {
      if (m_lock.acquire()) {
        try {
          String input = m_pluginField.getText();
          if (m_projects.keySet().contains(input)) {
            m_desc.setParentPlugin(m_projects.get(input));
          }
          else {
            m_desc.setPlugin(null);
            setMessage("invalid plugin name", WizardPage.WARNING);
          }
        }
        finally {
          m_lock.release();
        }
      }
    }

    private void setPlugin(IProject plugin) {
      if (plugin != null) {
        m_projects.clear();
        for (IPluginModelBase model : getAvailablePlugins(PDECore.getDefault().getModelManager().findModel(plugin))) {

          m_projects.put(model.getBundleDescription().getName(), model);
        }
      }
    }
  } // end class P_ParentPluginModifyListener
}
