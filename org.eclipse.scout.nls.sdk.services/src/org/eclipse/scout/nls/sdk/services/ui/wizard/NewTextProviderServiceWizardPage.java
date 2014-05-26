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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.NlsUi;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.fields.TextProposalField;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.services.internal.NlsSdkService;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.nls.sdk.services.model.ws.project.ServiceNlsProjectProvider;
import org.eclipse.scout.nls.sdk.simple.internal.NlsSdkSimple;
import org.eclipse.scout.nls.sdk.simple.ui.wizard.ResourceProposalModel;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
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

  // properties
  private static final String PROP_TRANSLATION_FOLDER = "translationFolder";
  private static final String PROP_TRANSLATION_FILE = "translationFile";
  private static final String PROP_SUPER_TYPE = "superType";
  private static final String PROP_CLASS_NAME = "className";
  private static final String PROP_TARGET_PACKAGE = "targetPackage";

  // ui fields
  private ProposalTextField m_superTypeField;
  private StyledTextField m_className;
  private TextProposalField m_translationFolderField;
  private TextField<String> m_translationFileName;
  private EntityTextField m_entityField;

  // process members
  private final IScoutBundle m_bundle;
  private final Set<String> m_languagesToCreate;
  private final List<NlsServiceType> m_existingServicesInPlugin;
  private final IType m_defaultProposal;

  public NewTextProviderServiceWizardPage(IScoutBundle bundle) {
    super(NewTextProviderServiceWizardPage.class.getName());
    setTitle("Create a new Text Provider Service");
    setDescription("Creates a new Text Provider Service.");
    setTargetPackage(DefaultTargetPackage.get(bundle, NlsServiceType.TEXT_SERVICE_PACKAGE_ID));
    m_bundle = bundle;
    m_languagesToCreate = new HashSet<String>();
    m_existingServicesInPlugin = getTextProviderServicesInSamePlugin();
    m_defaultProposal = RuntimeClasses.getSuperType(IRuntimeClasses.ITextProviderService, ScoutUtility.getJavaProject(bundle));
  }

  private List<NlsServiceType> getTextProviderServicesInSamePlugin() {
    try {
      Set<IType> candidates = ServiceNlsProjectProvider.getRegisteredTextProviderTypes();
      ArrayList<NlsServiceType> ret = new ArrayList<NlsServiceType>(candidates.size());
      for (IType t : candidates) {
        if (m_bundle.getProject().equals(t.getJavaProject().getProject())) {
          NlsServiceType type = new NlsServiceType(t);
          if (type.getTranslationsFolderName() != null) {
            ret.add(type);
          }
        }
      }
      return ret;
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
  }

  private void initDefaultValues() {
    m_translationFileName.setValue("Texts");
    m_translationFolderField.setText("resources/texts");
    m_languagesToCreate.add(null); // default language
    m_superTypeField.acceptProposal(m_defaultProposal);
    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField.setText(getTargetPackage());
    }
  }

  private void createServiceGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Text Provider Service Class");

    IType iTextProviderService = TypeUtility.getType(IRuntimeClasses.ITextProviderService);
    m_superTypeField = getFieldToolkit().createJavaElementProposalField(group, "Super Class",
        new JavaElementAbstractTypeContentProvider(iTextProviderService, ScoutUtility.getJavaProject(m_bundle), m_defaultProposal));
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    m_className = getFieldToolkit().createStyledTextField(group, "Service Name");
    m_className.setReadOnlySuffix(SdkProperties.SUFFIX_TEXT_SERVICE);
    m_className.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setClassNameInternal(m_className.getText());
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(group, Texts.get("EntityTextField"), m_bundle);
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal(m_entityField.getText());
          pingStateChanging();
        }
      });
      attachGridData(m_entityField);
    }

    group.setLayout(new GridLayout(1, true));
    attachGridData(group);
    attachGridData(m_superTypeField);
    attachGridData(m_className);
  }

  private Control createTranslationGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Translations");

    ResourceProposalModel model = new ResourceProposalModel();
    model.setResourceFilter(new IResourceFilter() {
      @Override
      public boolean accept(IResourceProxy proxy) {
        IResource resource = proxy.requestResource();
        if (resource instanceof IFolder) {
          IFolder folder = (IFolder) resource;
          IJavaProject jp = JavaCore.create(folder.getProject());
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
    if (m_bundle.getProject() != null) {
      try {
        projectList = NlsSdkSimple.getProjectGroup(m_bundle.getProject());
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
        setTranslationFolderInternal(m_translationFolderField.getText());
        pingStateChanging();
      }
    });
    NlsUi.decorate(m_translationFolderField, false);

    m_translationFileName = new TextField<String>(group, TextField.VALIDATE_ON_MODIFY);
    m_translationFileName.setLabelText("Translation File Prefix");
    m_translationFileName.setToolTipText("e.g. messages for messages[_language].properties");
    m_translationFileName.addInputChangedListener(new IInputChangedListener<String>() {
      @Override
      public void inputChanged(String input) {
        setTranlationFileNameInternal(input);
        pingStateChanging();
      }
    });
    m_translationFileName.setInputValidator(new IInputValidator() {
      @Override
      public IStatus isValid(String value) {
        if (IRegEx.JAVAFIELD.matcher(value).matches()) {
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

    String[][] langs = {{Language.LANGUAGE_DEFAULT.getLocale().getLanguage(), null}, {"German", "de"}, {"French", "fr"}, {"Italian", "it"}, {"Spanish", "es"}};

    for (final String[] entry : langs) {
      final Button chk = new Button(group, SWT.CHECK);
      chk.setText(entry[0]);
      chk.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (chk.getSelection()) {
            m_languagesToCreate.add(entry[1]);
          }
          else {
            m_languagesToCreate.remove(entry[1]);
          }
          pingStateChanging();
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
    if (StringUtility.isNullOrEmpty(getClassName())) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The class name must be specified."));
    }
    else if (TypeUtility.existsType(m_bundle.getPackageName(getTargetPackage()) + "." + getClassName())) {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Name already used. Choose another name."));
    }
    else if (!IRegEx.JAVAFIELD.matcher(getClassName()).matches()) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The service class name is invalid."));
    }

    if (StringUtility.isNullOrEmpty(getTranslationFolder())) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The translation folder must be specified."));
    }

    if (StringUtility.isNullOrEmpty(getTranlationFileName())) {
      multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "The translation file name must be specified."));
    }

    if (m_existingServicesInPlugin != null) {
      for (NlsServiceType existing : m_existingServicesInPlugin) {
        if (CompareUtility.equals(cleanFolder(existing.getTranslationsFolderName()), cleanFolder(getTranslationFolder())) &&
            CompareUtility.equals(existing.getTranslationsPrefix(), getTranlationFileName())) {
          multiStatus.add(new Status(IStatus.ERROR, NlsSdkService.PLUGIN_ID, "A service for the given translations does already exist."));
          break;
        }
      }
    }

    multiStatus.add(ScoutUtility.validatePackageName(getTargetPackage()));
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

  public void setClassName(String input) {
    try {
      setStateChanging(true);
      setClassNameInternal(input);
      if (isControlCreated()) {
        m_className.setText(input);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setClassNameInternal(String input) {
    if (input == null || input.equals("")) {
      input = null;
    }
    setPropertyString(PROP_CLASS_NAME, input);
  }

  public String getClassName() {
    return getPropertyString(PROP_CLASS_NAME);
  }

  public void setTranslationFolder(String folder) {
    try {
      setStateChanging(true);
      setTranslationFolderInternal(folder);
      if (isControlCreated()) {
        m_translationFolderField.setText(folder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTranslationFolderInternal(String folder) {
    setPropertyString(PROP_TRANSLATION_FOLDER, folder);
  }

  public String getTranslationFolder() {
    return getPropertyString(PROP_TRANSLATION_FOLDER);
  }

  public void setTranlationFileName(String filename) {
    try {
      setStateChanging(true);
      setTranlationFileNameInternal(filename);
      if (isControlCreated()) {
        m_translationFileName.setValue(filename);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTranlationFileNameInternal(String filename) {
    setPropertyString(PROP_TRANSLATION_FILE, filename);
  }

  public String getTranlationFileName() {
    return getPropertyString(PROP_TRANSLATION_FILE);
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      setSuperTypeInternal(superType);
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSuperTypeInternal(IType type) {
    setProperty(PROP_SUPER_TYPE, type);
  }

  public IType getSuperType() {
    return (IType) getProperty(PROP_SUPER_TYPE);
  }

  public String[] getLanguages() {
    return m_languagesToCreate.toArray(new String[m_languagesToCreate.size()]);
  }

  public String getTargetPackage() {
    return (String) getProperty(PROP_TARGET_PACKAGE);
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated() && m_entityField != null) {
        m_entityField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    setProperty(PROP_TARGET_PACKAGE, targetPackage);
  }
}
