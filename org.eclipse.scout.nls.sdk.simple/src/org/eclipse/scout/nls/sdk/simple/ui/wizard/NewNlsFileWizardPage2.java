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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;
import org.eclipse.scout.nls.sdk.internal.ui.FieldValidator;
import org.eclipse.scout.nls.sdk.internal.ui.NlsUi;
import org.eclipse.scout.nls.sdk.internal.ui.TextButtonField;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.ResourceDialog;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.fields.TextProposalField;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.simple.internal.NlsSdkSimple;
import org.eclipse.scout.nls.sdk.simple.operations.NewNlsFileOperationDesc;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

@SuppressWarnings({"restriction", "deprecation"})
public class NewNlsFileWizardPage2 extends NewTypeWizardPage {

  private Composite m_rootPane;
  private TextButtonField m_containerField;
  private TextProposalField m_package;
  private TextField<String> m_className;
  private TextProposalField m_translationFolderField;
  private TextField<String> m_translationFileName;

  private final FieldValidator m_fieldValidator;
  private final P_RootContainerModifyListener m_containerFieldModifyListener;
  private final NewNlsFileOperationDesc m_desc;
  private final OptimisticLock m_lock = new OptimisticLock();
  private final PackageProposalModel m_packageProposalModel;

  public NewNlsFileWizardPage2(String pageName, NewNlsFileOperationDesc desc) {
    super(true, pageName);
    setTitle("Create a new NLS class");
    setDescription("This class will be referenced to get a translated text.");
    m_desc = desc;
    m_fieldValidator = new FieldValidator();
    m_containerFieldModifyListener = new P_RootContainerModifyListener();
    m_packageProposalModel = new PackageProposalModel();
    setPageComplete(false);
  }

  @Override
  public void createControl(Composite parent) {
    m_rootPane = new Composite(parent, SWT.NONE);

    attachGridData(createServiceGroup(m_rootPane));
    attachGridData(createTranslationGroup(m_rootPane));

    // layout
    m_rootPane.setLayout(new GridLayout(1, false));
    initUi();
  }

  private void initUi() {
    m_desc.addPropertyChangeListener(new P_DescPropertyChangedListener());
    for (Entry<String, Object> entry : m_desc.getPropertiesMap().entrySet()) {
      handlePropertyChanged(entry.getKey(), null, entry.getValue());
    }
  }

  protected Control createServiceGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Texts Class");

    m_containerField = new TextButtonField(group);
    m_fieldValidator.addField(m_containerField);
    m_containerField.setLabelText("Source Folder");
    m_containerField.setButtonText("Browse");
    m_containerField.addTextModifyListener(m_containerFieldModifyListener);
    m_containerField.addButtonSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IPackageFragmentRoot frag = chooseContainer();
        setPackageFragmentRoot(frag, false);
        if (frag == null) {
          m_desc.setSourceContainer(null);
        }
        else {
          m_desc.setSourceContainer(frag.getPath());
        }
      }
    });

    KeyStroke stoke = KeyStroke.getInstance(SWT.CONTROL, ' ');
    m_package = new TextProposalField(group, m_packageProposalModel, stoke);
    m_package.setLabelText("Package");
    m_package.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_desc.setPackage(m_package.getText());
      }
    });

    m_package.setLabelProvider(m_packageProposalModel);
    NlsUi.decorate(m_package, false);
    m_fieldValidator.addField(m_package);

    m_className = new TextField<String>(group);
    m_fieldValidator.addField(m_className);
    m_className.setLabelText("Class Name");
    m_className.addInputChangedListener(new IInputChangedListener<String>() {
      @Override
      public void inputChanged(String input) {
        m_desc.setClassName(input);
      }
    });
    m_className.setInputValidator(new IInputValidator() {
      private String m_regexp = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b";

      @Override
      public IStatus isValid(String value) {
        if (value.matches(m_regexp)) {
          return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
      }
    });

    group.setLayout(new GridLayout(1, true));
    attachGridData(m_containerField);
    attachGridData(m_package);
    attachGridData(m_className);

    return group;
  }

  private Control createTranslationGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Translation File(s)");
    ResourceProposalModel model = new ResourceProposalModel();
    List<IProject> projectList = new ArrayList<IProject>();
    if (m_desc.getPlugin() != null) {
      try {
        projectList = NlsSdkSimple.getProjectGroup(m_desc.getPlugin());
      }
      catch (FileNotFoundException e) {
        NlsCore.logWarning(e);
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }

    model.setResourceFilter(new IResourceFilter() {
      @Override
      public boolean accept(IResourceProxy proxy) {
        IResource resource = proxy.requestResource();
        if (resource instanceof IFolder) {
          IFolder folder = (IFolder) resource;
          IJavaProject jp = JavaCore.create(resource.getProject());
          try {
            if (jp.getOutputLocation().toOSString().equals(folder.getFullPath().toOSString())) {
              return false;
            }
          }
          catch (JavaModelException e) {
            NlsCore.logWarning(e);
          }
          if ("META-INF".equals(folder.getProjectRelativePath().toOSString())) {
            return false;
          }
          return true;
        }
        return false;
      }
    });
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
    m_fieldValidator.addField(m_translationFileName);
    m_translationFileName.setLabelText("Translation File Prefix");
    m_translationFileName.setToolTipText("e.g. messages for messages[_language].properties");
    m_translationFileName.addInputChangedListener(new IInputChangedListener<String>() {
      @Override
      public void inputChanged(String input) {
        m_desc.setTranlationFileName(input);
      }
    });
    m_translationFileName.setInputValidator(new IInputValidator() {
      private String m_regexp = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b";

      @Override
      public IStatus isValid(String value) {
        if (value.matches(m_regexp)) {
          return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    attachGridData(m_translationFolderField);
    attachGridData(m_translationFileName);
    return group;
  }

  protected IFolder chooseTranslationFolder() {
    ResourceDialog dialog = new ResourceDialog(getShell(), "Choose Folder", m_desc.getPlugin());
    dialog.setInitialExpansion(new IResource[]{m_desc.getPlugin()});
    dialog.setInitialSelection(new IResource[]{m_desc.getPlugin()});
    dialog.addViewerFilter(new ViewerFilter() {
      @Override
      public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
        List<Object> elList = new LinkedList<Object>();
        for (Object e : elements) {
          if (e instanceof IContainer) {
            if (parent instanceof IProject) {
              IJavaProject jp = JavaCore.create((IProject) parent);
              try {
                if (!((IContainer) e).getProjectRelativePath().lastSegment().equals(
                    jp.getOutputLocation().lastSegment())) {
                  elList.add(e);
                }
              }
              catch (JavaModelException e1) {
                NlsCore.logWarning(e1);
              }
            }
            else {
              elList.add(e);
            }
          }
        }
        return elList.toArray();
      }

      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        return element instanceof IFolder;
      }
    });
    if (dialog.open() == Window.OK) {
      IResource selection = dialog.getFirstResult();
      if (selection instanceof IFolder) {
        return (IFolder) selection;
      }

    }
    return null;
  }

  @Override
  protected IPackageFragmentRoot chooseContainer() {
    IJavaElement initElement = getPackageFragmentRoot();
    Class<?>[] acceptedClasses = new Class[]{IPackageFragmentRoot.class, IJavaProject.class};
    TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, false) {
      @Override
      public boolean isSelectedValid(Object element) {
        try {
          if (element instanceof IJavaProject) {
            IJavaProject jproject = (IJavaProject) element;
            IPath path = jproject.getProject().getFullPath();
            return (jproject.findPackageFragmentRoot(path) != null);
          }
          else if (element instanceof IPackageFragmentRoot) {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          }
          return true;
        }
        catch (JavaModelException e) {
          JavaPlugin.log(e.getStatus()); // just log, no UI in validation
        }
        return false;
      }
    };

    acceptedClasses = new Class[]{IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class};
    ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {
      @Override
      public boolean select(Viewer viewer, Object parent, Object element) {
        if (element instanceof IPackageFragmentRoot) {
          try {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          }
          catch (JavaModelException e) {
            JavaPlugin.log(e.getStatus()); // just log, no UI in validation
            return false;
          }
        }
        return super.select(viewer, parent, element);
      }
    };

    StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
    ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
    dialog.setValidator(validator);
    dialog.setSorter(new JavaElementSorter());
    dialog.setTitle(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
    dialog.setMessage(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);
    dialog.addFilter(filter);
    dialog.setInput(JavaCore.create(m_desc.getPlugin()));
    dialog.setInitialSelection(initElement);
    dialog.setHelpAvailable(false);

    if (dialog.open() == Window.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof IJavaProject) {
        IJavaProject jproject = (IJavaProject) element;
        return jproject.getPackageFragmentRoot(jproject.getProject());
      }
      else if (element instanceof IPackageFragmentRoot) {
        return (IPackageFragmentRoot) element;
      }
      return null;
    }
    return null;
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

    if (m_desc.getSourceContainer() == null) {
      m_fieldValidator.setDisabled(m_package);
    }
    m_fieldValidator.apply();

    // messages
    setMessage(null);
    if (m_desc.getSourceContainer() == null) {
      setMessage("The source container must be specified.", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    if (m_desc.getPackage() == null) {
      setMessage("The package must be specified.", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    if (m_desc.getClassName() == null) {
      setMessage("The class name must be specified.", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    if (m_desc.getTranslationFolder() == null) {
      setMessage("The translation folder must be specified.", WizardPage.WARNING);
      setPageComplete(false);
      return;
    }
    if (m_desc.getTranlationFileName() == null) {
      setMessage("The translation file name must be specified.", WizardPage.WARNING);
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

  protected void handlePropertyChanged(String name, Object oldValue, Object newValue) {
    if (name.equals(NewNlsFileOperationDesc.PROP_PLUGIN)) {
      m_containerFieldModifyListener.setPlugin((IProject) newValue);
      m_packageProposalModel.setProject((IProject) newValue);
    }
    if (name.equals(NewNlsFileOperationDesc.PROP_SRC_CONTAINER)) {
      try {
        if (m_lock.acquire()) {
          IPath path = (IPath) newValue;
          if (path == null) {
            m_containerField.setText("");
            m_desc.setSourceContainer(null);
          }
          else {
            m_containerField.setText(path.lastSegment());
            m_desc.setSourceContainer(path);
          }
        }
      }
      finally {
        m_lock.release();
      }
    }
    revalidate();
  }

  private final class P_DescPropertyChangedListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handlePropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

  private final class P_RootContainerModifyListener implements ModifyListener {
    private HashMap<String, IClasspathEntry> m_map = new HashMap<String, IClasspathEntry>();

    private P_RootContainerModifyListener() {

    }

    public void setPlugin(IProject project) {
      if (project == null) {
        return;
      }
      m_map.clear();
      IJavaProject jp = JavaCore.create(project);
      try {
        for (IClasspathEntry entry : NlsJdtUtility.getSourceLocations(jp)) {
          m_map.put(entry.getPath().toPortableString(), entry);
        }
      }
      catch (JavaModelException e) {
        NlsCore.logWarning(e);
      }
    }

    @Override
    public void modifyText(ModifyEvent e) {
      if (m_lock.acquire()) {
        try {
          String input = m_containerField.getText();
          for (IClasspathEntry entry : m_map.values()) {
            if (entry.getPath().lastSegment().equals(input)) {
              m_desc.setSourceContainer(entry.getPath());
            }
            else {
              setMessage("invalid source container", WizardPage.WARNING);
            }
          }
        }
        finally {
          m_lock.release();
        }
      }
    }
  }
}
