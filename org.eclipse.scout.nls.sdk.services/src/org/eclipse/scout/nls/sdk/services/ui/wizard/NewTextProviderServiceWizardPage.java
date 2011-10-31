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
package org.eclipse.scout.nls.sdk.services.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.IResourceFilter;
import org.eclipse.scout.nls.sdk.internal.ui.NlsUi;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.fields.TextProposalField;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.services.NlsSdkService;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.nls.sdk.services.model.ws.project.ServiceNlsProjectProvider;
import org.eclipse.scout.nls.sdk.services.operation.NewNlsServiceModel;
import org.eclipse.scout.nls.sdk.simple.NlsSdkSimple;
import org.eclipse.scout.nls.sdk.simple.ui.wizard.ResourceProposalModel;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class NewTextProviderServiceWizardPage extends AbstractWorkspaceWizardPage {

  private StyledTextField m_className;
  private TextProposalField m_translationFolderField;
  private TextField<String> m_translationFileName;

  private final NewNlsServiceModel m_desc;
  private final NlsServiceType[] m_existingServicesInPlugin;

  public NewTextProviderServiceWizardPage(String pageName, NewNlsServiceModel desc) {
    super(pageName);
    setTitle("Create a new Text Provider Service");
    setDescription("Creates a new Text Provider Service.");
    m_desc = desc;
    m_existingServicesInPlugin = getTextProviderServicesInSamePlugin();
  }

  private NlsServiceType[] getTextProviderServicesInSamePlugin() {
    IType[] candidates;
    try {
      candidates = ServiceNlsProjectProvider.getRegisteredTextProviderTypes();
      ArrayList<NlsServiceType> ret = new ArrayList<NlsServiceType>(candidates.length);

      for (IType t : candidates) {
        if (m_desc.getBundle().getProject().equals(t.getJavaProject().getProject())) {
          NlsServiceType type = new NlsServiceType(t);
          if (type.getTranslationsFolderName() != null) {
            ret.add(type);
          }
        }
      }
      return ret.toArray(new NlsServiceType[ret.size()]);
    }
    catch (JavaModelException e) {
      NlsCore.logWarning(e);
      return null;
    }
  }

  @Override
  public void createContent(Composite parent) {
    parent.setLayout(new GridLayout(1, true));
    createServiceGroup(parent);
    createTranslationGroup(parent);
    createLanguagesGroup(parent);
    initDefaultValues();
    initUi();
  }

  private void initDefaultValues() {
    m_translationFileName.setValue("Texts");
    m_translationFolderField.setText("resources/texts");
  }

  private void initUi() {
    m_desc.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        revalidate();
      }
    });
    revalidate();
  }

  private void createServiceGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Text Provider Service Class");

    m_className = getFieldToolkit().createStyledTextField(group, "Service Name");
    m_className.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_TEXT_SERVICE);
    m_className.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_desc.setClassName(m_className.getText());
      }
    });

    group.setLayout(new GridLayout(1, true));
    attachGridData(group);
    attachGridData(m_className);
  }

  private Control createTranslationGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Translations");

    ResourceProposalModel model = new ResourceProposalModel();
    model.setResourceFilter(new IResourceFilter() {
      @Override
      public boolean accept(IProject project, IResource resource) {
        if (resource instanceof IFolder) {
          IFolder folder = (IFolder) resource;
          IJavaProject jp = JavaCore.create(project);
          try {
            if (jp.getOutputLocation().toOSString().equals(folder.getFullPath().toOSString())) {
              return false;
            }
          }
          catch (JavaModelException e) {
            NlsCore.logWarning(e);
          }
          if (folder.getProjectRelativePath().toOSString().equals("META-INF")) {
            return false;
          }
          return true;
        }
        return false;
      }
    });

    List<IProject> projectList = new ArrayList<IProject>();
    if (m_desc.getBundle().getProject() != null) {
      try {
        projectList = NlsSdkSimple.getProjectGroup(m_desc.getBundle().getProject());
      }
      catch (Exception e) {
        NlsCore.logWarning(e);
      }
    }
    model.setProjects(projectList.toArray(new IProject[projectList.size()]));

    KeyStroke stoke = KeyStroke.getInstance(SWT.CONTROL, ' ');
    m_translationFolderField = new TextProposalField(group, model, stoke);
    m_translationFolderField.setLabelText("Translations Folder");
    m_translationFolderField.setLabelProvider(model);
    m_translationFolderField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_desc.setTranslationFolder(m_translationFolderField.getText());
      }
    });
    NlsUi.decorate(m_translationFolderField, false);

    m_translationFileName = new TextField<String>(group, TextField.VALIDATE_ON_MODIFY);
    m_translationFileName.setLabelText("Translation File Prefix");
    m_translationFileName.setToolTipText("e.g. messages for messages[_language].properties");
    m_translationFileName.addInputChangedListener(new IInputChangedListener<String>() {
      @Override
      public void inputChanged(String input) {
        m_desc.setTranlationFileName(input);
      }
    });
    m_translationFileName.setInputValidator(new IInputValidator() {
      @Override
      public IStatus isValid(String value) {
        if (Regex.REGEX_JAVAFIELD.matcher(value).matches()) {
          return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    attachGridData(group);
    attachGridData(m_translationFolderField);
    attachGridData(m_translationFileName);
    return group;
  }

  private Control createLanguagesGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Languages");

    String[][] langs = {{"Default (English)", null}, {"German", "de"}, {"French", "fr"}, {"Italian", "it"}, {"Spanish", "es"}};

    for (final String[] entry : langs) {
      final Button chk = new Button(group, SWT.CHECK);
      chk.setText(entry[0]);
      chk.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (chk.getSelection()) {
            m_desc.addLanguage(entry[1]);
          }
          else {
            m_desc.removeLanguage(entry[1]);
          }
        }
      });
      if (entry[1] == null) {
        chk.setEnabled(false);
        chk.setSelection(true);
      }
      attachGridData(chk);
    }

    group.setLayout(new GridLayout(5, true));
    attachGridData(group);
    return group;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (m_desc.getClassName() == null) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The class name must be specified."));
    }
    else if (!Regex.REGEX_JAVAFIELD.matcher(m_desc.getClassName()).matches()) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The service class name is invalid."));
    }

    if (m_desc.getTranslationFolder() == null) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The translation folder must be specified."));
    }

    if (m_desc.getTranlationFileName() == null) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The translation file name must be specified."));
    }

    for (NlsServiceType existing : m_existingServicesInPlugin) {
      if (CompareUtility.equals(cleanFolder(existing.getTranslationsFolderName()), cleanFolder(m_desc.getTranslationFolder())) &&
          CompareUtility.equals(existing.getTranslationsPrefix(), m_desc.getTranlationFileName())) {
        multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "A service for the given translations does already exist."));
        break;
      }
    }

    multiStatus.add(Status.OK_STATUS);
  }

  private static String cleanFolder(String f) {
    if (f != null && f.startsWith("/")) {
      f = f.substring(1);
    }
    if (f != null && f.endsWith("/")) {
      f = f.substring(0, f.length() - 1);
    }
    return f;
  }

  private void attachGridData(Control c) {
    c.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }
}
