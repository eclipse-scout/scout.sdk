/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.BindingClassUpdate;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.EntryPointDefinitionUpdate;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.WebServiceClientUpdate;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.WebServiceImplementationUpdate;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * <h3>{@link WebServiceFormPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceFormPage extends FormPage {

  protected static final String PROP_PORT_TYPE = "portType";
  protected static final String PROP_WEB_SERVICE = "webService";

  public static final String UPDATE_FOR_ALL_SERVICES_QUESTION_KEY = "doNotShowWsChangeGenerateElementForAll";

  private final WebServiceFormPageInput m_input;
  private final ILabelProvider m_decoratingWorkbenchLabelProvider;
  private final FieldToolkit m_fieldToolkit;

  // ui fields
  private final List<StyledTextField> m_portTypeNameFields;
  private ProposalTextField m_packageField;
  private final List<StyledTextField> m_webServiceNameFields;
  private final List<StyledTextField> m_entryPointNameFields;
  private final List<ProposalTextField> m_entryPointPackageFields;

  public WebServiceFormPage(FormEditor editor, WebServiceFormPageInput input) {
    super(Validate.notNull(editor), Validate.notNull(input).getWsdl().getName(), input.getDisplayName());
    m_input = input;
    m_decoratingWorkbenchLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
    m_fieldToolkit = new FieldToolkit();
    m_entryPointNameFields = new LinkedList<>();
    m_entryPointPackageFields = new LinkedList<>();
    m_portTypeNameFields = new LinkedList<>();
    m_webServiceNameFields = new LinkedList<>();
  }

  @Override
  public WebServiceEditor getEditor() {
    return (WebServiceEditor) super.getEditor();
  }

  @Override
  protected void createFormContent(IManagedForm managedForm) {
    super.createFormContent(managedForm);
    ScrolledForm scrolledForm = managedForm.getForm();
    getFormToolkit().decorateFormHeading(scrolledForm.getForm());
    createActionToolBar(scrolledForm.getToolBarManager());
    scrolledForm.setText(getInput().getDisplayName());
    scrolledForm.updateToolBar();

    Composite body = scrolledForm.getBody();
    createBody(body);
    setEnabled(true);
    scrolledForm.reflow(true);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(body, IScoutHelpContextIds.SCOUT_WEB_SERVICE_EDITOR_PAGE);
  }

  protected void createOverviewImage(final Composite parent, String imgName, final Iterable<P_ImageArea> areas, int horizontalSpan) {
    Image overviewImage = S2ESdkUiActivator.getImage(imgName);
    final int imageWidth = overviewImage.getBounds().width;
    final Label label = getFormToolkit().createLabel(parent, "", SWT.CENTER);
    label.setImage(overviewImage);
    label.addMouseMoveListener(new MouseMoveListener() {
      @Override
      public void mouseMove(MouseEvent e) {
        for (P_ImageArea area : areas) {
          if (area.contains(e.x, e.y, imageWidth, label.getSize().x)) {
            parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            return;
          }
        }
        parent.setCursor(null);// reset to default
      }
    });
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        if (e.button != 1) {
          return;
        }
        for (P_ImageArea area : areas) {
          if (area.contains(e.x, e.y, imageWidth, label.getSize().x)) {
            Object element = area.m_elementToShow;
            if (element instanceof IJavaElement) {
              S2eUiUtils.openInEditor((IJavaElement) element);
            }
            else if (element instanceof IFile) {
              S2eUiUtils.openInEditor((IFile) element);
            }
            return;
          }
        }
      }
    });
    GridDataFactory
        .defaultsFor(label)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .span(horizontalSpan, 1)
        .applyTo(label);
  }

  protected void createBody(Composite parent) {
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);
    int labelColWidth = 180;

    Set<IType> allPortTypes = getInput().getAllPortTypes();
    if (!allPortTypes.isEmpty()) {
      if (getInput().hasProviderElements()) {
        createProviderGroup(parent, allPortTypes, labelColWidth);
      }
      else {
        createConsumerGroup(parent, allPortTypes, labelColWidth);
      }
    }
    if (!getInput().getBindings().isEmpty()) {
      createBindingGroup(parent);
    }
  }

  protected void createConsumerGroup(Composite p, Set<IType> allPortTypes, int labelColWidth) {
    final int numPortTypes = allPortTypes.size();
    IType firstPortType = allPortTypes.iterator().next();
    Set<IType> webServices = getInput().getWebServices();
    IType firstWebService = webServices.iterator().next();

    Composite parent = createSection(getFormToolkit(), p, "Consumer");
    GridLayoutFactory
        .swtDefaults()
        .numColumns(numPortTypes)
        .equalWidth(true)
        .applyTo(parent);

    List<P_ImageArea> clickAreas = new LinkedList<>();
    clickAreas.add(new P_ImageArea(10, 60, 180, 33, getInput().getWsdl()));
    clickAreas.add(new P_ImageArea(460, 110, 170, 50, getInput().getWebServiceClient(firstPortType)));
    clickAreas.add(new P_ImageArea(460, 10, 170, 50, firstPortType));
    clickAreas.add(new P_ImageArea(345, 160, 95, 50, firstWebService));
    createOverviewImage(parent, ISdkIcons.WsConsumerOverview, clickAreas, numPortTypes);

    createLabeledHyperlink(parent, "WSDL", getInput().getWsdl(), null, labelColWidth, 0);
    for (int i = 0; i < numPortTypes - 1; i++) {
      getFormToolkit().createLabel(parent, "");
    }
    for (IType portType : allPortTypes) {
      createLabeledHyperlink(parent, "Web Service Client", null, getInput().getWebServiceClient(portType), labelColWidth, 0);
    }
    for (IType portType : allPortTypes) {
      createPortTypeNameField(parent, portType, labelColWidth);
    }

    for (IType webservice : webServices) {
      int numPortTypesInThisService = getInput().getPortTypes(webservice).size();
      final StyledTextField webServiceNameField = getFieldToolkit().createStyledTextField(parent, "Web Service", TextField.TYPE_HYPERLINK | TextField.TYPE_IMAGE, labelColWidth);
      GridDataFactory
          .defaultsFor(webServiceNameField)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .span(numPortTypesInThisService, 1)
          .applyTo(webServiceNameField);
      webServiceNameField.setReadOnlySuffix(ISdkProperties.SUFFIX_WS_SERVICE);
      webServiceNameField.setText(webservice.getElementName());
      webServiceNameField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          clearMessage();
          getManagedForm().dirtyStateChanged();
        }
      });
      webServiceNameField.setData(PROP_WEB_SERVICE, webservice);
      webServiceNameField.setImage(m_decoratingWorkbenchLabelProvider.getImage(webservice));
      webServiceNameField.getLabelComponent().setToolTipText(webservice.getFullyQualifiedName());
      final IType currentWs = webservice;
      webServiceNameField.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          S2eUiUtils.openInEditor(currentWs);
        }
      });
      m_webServiceNameFields.add(webServiceNameField);
    }

    createPackageField(parent, firstPortType, labelColWidth, numPortTypes);
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected void createProviderGroup(Composite p, Set<IType> allPortTypes, int labelColWidth) {
    final int numPortTypes = allPortTypes.size();
    IType firstPortType = allPortTypes.iterator().next();
    Composite parent = createSection(getFormToolkit(), p, "Provider");
    GridLayoutFactory
        .swtDefaults()
        .numColumns(numPortTypes)
        .equalWidth(true)
        .applyTo(parent);

    // overview image
    List<P_ImageArea> clickAreas = new LinkedList<>();
    clickAreas.add(new P_ImageArea(11, 10, 180, 33, getInput().getWsdl()));
    clickAreas.add(new P_ImageArea(545, 159, 196, 50, getInput().getServiceImplementation(firstPortType)));
    clickAreas.add(new P_ImageArea(445, 49, 170, 50, firstPortType));
    clickAreas.add(new P_ImageArea(385, 159, 95, 50, getInput().getEntryPoint(firstPortType)));
    clickAreas.add(new P_ImageArea(10, 154, 180, 60, getInput().getEntryPointDefinition(firstPortType)));
    createOverviewImage(parent, ISdkIcons.WsProviderOverview, clickAreas, numPortTypes);

    createLabeledHyperlink(parent, "WSDL", getInput().getWsdl(), null, labelColWidth, 0);
    for (int i = 0; i < numPortTypes - 1; i++) {
      getFormToolkit().createLabel(parent, "");
    }

    for (IType portType : allPortTypes) {
      createLabeledHyperlink(parent, "Web Service Implementation", null, getInput().getServiceImplementation(portType), labelColWidth, 0);
    }
    for (IType portType : allPortTypes) {
      createPortTypeNameField(parent, portType, labelColWidth);
    }
    createPackageField(parent, firstPortType, labelColWidth, numPortTypes);

    for (IType portType : allPortTypes) {
      createLabeledHyperlink(parent, "Entry Point Definition", null, getInput().getEntryPointDefinition(portType), labelColWidth, 20);
    }

    // entry point name
    for (IType portType : allPortTypes) {
      StyledTextField entryPointNameField = getFieldToolkit().createStyledTextField(parent, "Entry Point", TextField.TYPE_HYPERLINK | TextField.TYPE_IMAGE, labelColWidth);
      GridDataFactory
          .defaultsFor(entryPointNameField)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(entryPointNameField);
      String nameInDefinition = getInput().getEntryPointNameFromDefinition(portType);
      if (StringUtils.isNotBlank(nameInDefinition)) {
        entryPointNameField.setReadOnlySuffix(ISdkProperties.SUFFIX_WS_ENTRY_POINT);
        entryPointNameField.setText(nameInDefinition);
      }
      final IType entryPoint = getInput().getEntryPoint(portType);
      entryPointNameField.setData(PROP_PORT_TYPE, portType);
      entryPointNameField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          clearMessage();
          getManagedForm().dirtyStateChanged();
        }
      });
      if (S2eUtils.exists(entryPoint)) {
        entryPointNameField.setImage(m_decoratingWorkbenchLabelProvider.getImage(entryPoint));
        entryPointNameField.getLabelComponent().setToolTipText(entryPoint.getFullyQualifiedName());
        entryPointNameField.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent e) {
            S2eUiUtils.openInEditor(entryPoint);
          }
        });
      }
      m_entryPointNameFields.add(entryPointNameField);
    }

    // entry point package
    for (IType portType : allPortTypes) {
      ProposalTextField entryPointPackageField = getFieldToolkit().createPackageField(parent, "Entry Point Package", getInput().getJavaProject(), labelColWidth, TextField.TYPE_LABEL | TextField.TYPE_IMAGE);
      GridDataFactory
          .defaultsFor(entryPointPackageField)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(entryPointPackageField);
      String packageInDefinition = getInput().getEntryPointPackageFromDefinition(portType);
      if (StringUtils.isNotBlank(packageInDefinition)) {
        entryPointPackageField.setText(packageInDefinition);
      }
      entryPointPackageField.setData(PROP_PORT_TYPE, portType);
      entryPointPackageField.setImage(m_decoratingWorkbenchLabelProvider.getImage(portType.getPackageFragment()));
      entryPointPackageField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          clearMessage();
          getManagedForm().dirtyStateChanged();
        }
      });
      m_entryPointPackageFields.add(entryPointPackageField);
    }

    for (IType portType : allPortTypes) {
      createHandlersList(parent, portType, labelColWidth, numPortTypes);
    }
    for (IType portType : allPortTypes) {
      IType authMethodFromDefinition = getInput().getAuthMethodFromDefinition(portType);
      if (S2eUtils.exists(authMethodFromDefinition)) {
        createLabeledHyperlink(parent, "Authentication Method", null, authMethodFromDefinition, labelColWidth, 0);
      }
    }
    for (IType portType : allPortTypes) {
      IType authVerifierFromDefinition = getInput().getAuthVerifierFromDefinition(portType);
      if (S2eUtils.exists(authVerifierFromDefinition)) {
        createLabeledHyperlink(parent, "Authentication Verifier", null, authVerifierFromDefinition, labelColWidth, 0);
      }
    }
  }

  protected void createHandlersList(Composite p, IType portType, int labelColWidth, int numPortTypes) {
    List<IType> handlers = getInput().getHandlers(portType);
    if (handlers.isEmpty() && numPortTypes <= 1) {
      return; // don't draw anything if we have only one column and there are no handlers
    }
    Composite parent = getFormToolkit().createComposite(p);
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.BEGINNING)
        .grab(true, false)
        .indent(0, 20)
        .applyTo(parent);
    parent.setLayout(new FormLayout());
    if (!handlers.isEmpty()) {
      Label label = getFormToolkit().createLabel(parent, "Handlers", SWT.TRAIL);

      Composite handlersComposite = getFormToolkit().createComposite(parent);
      GridLayoutFactory
          .swtDefaults()
          .margins(0, 0)
          .applyTo(handlersComposite);

      for (IType handler : handlers) {
        ImageHyperlink link = getFormToolkit().createImageHyperlink(handlersComposite, SWT.NONE);
        link.setUnderlined(true);
        link.setImage(m_decoratingWorkbenchLabelProvider.getImage(handler));
        link.setToolTipText(handler.getFullyQualifiedName());
        link.setText(handler.getElementName());
        final IType currentHandler = handler;
        link.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent e) {
            S2eUiUtils.openInEditor(currentHandler);
          }
        });
      }

      FormData labelData = new FormData();
      labelData.top = new FormAttachment(0, 2);
      labelData.left = new FormAttachment(0, 0);
      labelData.right = new FormAttachment(0, labelColWidth);
      labelData.bottom = new FormAttachment(100, 0);
      label.setLayoutData(labelData);

      FormData textData = new FormData();
      textData.top = new FormAttachment(0, 0);
      textData.left = new FormAttachment(label, 5);
      textData.right = new FormAttachment(100, 0);
      textData.bottom = new FormAttachment(100, 0);
      handlersComposite.setLayoutData(textData);
    }
  }

  protected void setEnabled(boolean enabled) {
    IManagedForm managedForm = getManagedForm();
    if (managedForm == null) {
      return;
    }
    ScrolledForm form = managedForm.getForm();
    if (form.isDisposed()) {
      return;
    }

    form.setEnabled(enabled);
    form.getBody().setEnabled(enabled);

    boolean jaxwsBindingFileExists = !getInput().getJaxWsBindingFiles().isEmpty();
    for (StyledTextField field : m_portTypeNameFields) {
      field.setEnabled(enabled && jaxwsBindingFileExists);
    }
    m_packageField.setEnabled(enabled && jaxwsBindingFileExists);
    for (StyledTextField field : m_webServiceNameFields) {
      field.setEnabled(enabled && jaxwsBindingFileExists);
    }
    for (StyledTextField field : m_entryPointNameFields) {
      field.setEnabled(enabled && S2eUtils.exists(getInput().getEntryPointDefinition(getPortType(field))));
    }
    for (ProposalTextField field : m_entryPointPackageFields) {
      field.setEnabled(enabled && S2eUtils.exists(getInput().getEntryPointDefinition(getPortType(field))));
    }
  }

  protected IType getPortType(Control control) {
    return (IType) control.getData(PROP_PORT_TYPE);
  }

  protected void createBindingGroup(Composite p) {
    Composite parent = createSection(getFormToolkit(), p, "Bindings");
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);

    for (IFile f : getInput().getBindings()) {
      final IFile file = f;
      ImageHyperlink field = getFormToolkit().createImageHyperlink(parent, SWT.NONE);
      field.setUnderlined(true);
      field.setText(f.getName());
      field.setImage(m_decoratingWorkbenchLabelProvider.getImage(f));
      GridDataFactory
          .defaultsFor(field)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(field);
      field.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          S2eUiUtils.openInEditor(file);
        }
      });
    }
  }

  protected void createPackageField(Composite parent, IType portType, int labelColWidth, int horizontalSpan) {
    m_packageField = getFieldToolkit().createPackageField(parent, "Package", getInput().getJavaProject(), labelColWidth, TextField.TYPE_LABEL | TextField.TYPE_IMAGE);
    GridDataFactory
        .defaultsFor(m_packageField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .span(horizontalSpan, 1)
        .applyTo(m_packageField);
    String packageInBinding = Signature.getQualifier(portType.getFullyQualifiedName());
    if (StringUtils.isNotBlank(packageInBinding)) {
      m_packageField.setText(packageInBinding);
    }
    m_packageField.setImage(m_decoratingWorkbenchLabelProvider.getImage(portType.getPackageFragment()));
    m_packageField.setData(PROP_PORT_TYPE, portType);
    m_packageField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        clearMessage();
        getManagedForm().dirtyStateChanged();
      }
    });
  }

  protected void createPortTypeNameField(Composite parent, final IType portType, int labelColWidth) {
    StyledTextField portTypeNameField = getFieldToolkit().createStyledTextField(parent, "Port Type (EPI)", TextField.TYPE_HYPERLINK | TextField.TYPE_IMAGE, labelColWidth);
    GridDataFactory
        .defaultsFor(portTypeNameField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(portTypeNameField);
    String nameInBinding = portType.getElementName();
    if (StringUtils.isNotBlank(nameInBinding)) {
      portTypeNameField.setReadOnlyPrefix("I");
      portTypeNameField.setReadOnlySuffix(ISdkProperties.SUFFIX_WS_PORT_TYPE);
      portTypeNameField.setText(nameInBinding);
    }
    portTypeNameField.setData(PROP_PORT_TYPE, portType);
    portTypeNameField.getLabelComponent().setToolTipText(portType.getFullyQualifiedName());
    portTypeNameField.setImage(m_decoratingWorkbenchLabelProvider.getImage(portType));
    portTypeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        clearMessage();
        getManagedForm().dirtyStateChanged();
      }
    });
    portTypeNameField.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        S2eUiUtils.openInEditor(portType);
      }
    });
    m_portTypeNameFields.add(portTypeNameField);
  }

  public boolean isPortTypeNameChanged() {
    for (StyledTextField field : m_portTypeNameFields) {
      if (!Objects.equals(field.getText(), getPortType(field).getElementName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isDirty() {
    if (isPortTypeNameChanged()) {
      return true;
    }
    if (isPackageChanged()) {
      return true;
    }
    if (isWebServiceNameChanged()) {
      return true;
    }
    if (isEntryPointNameChanged()) {
      return true;
    }
    if (isEntryPointPackageChanged()) {
      return true;
    }

    return false;
  }

  protected boolean isEntryPointPackageChanged(ProposalTextField field) {
    return !Objects.equals(field.getText(), StringUtils.defaultIfEmpty(getInput().getEntryPointPackageFromDefinition(getPortType(field)), ""));
  }

  public boolean isEntryPointPackageChanged() {
    for (ProposalTextField field : m_entryPointPackageFields) {
      if (isEntryPointPackageChanged(field)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isEntryPointNameChanged(StyledTextField field) {
    return !Objects.equals(field.getText(), StringUtils.defaultIfEmpty(getInput().getEntryPointNameFromDefinition(getPortType(field)), ""));
  }

  public boolean isEntryPointNameChanged() {
    for (StyledTextField field : m_entryPointNameFields) {
      if (isEntryPointNameChanged(field)) {
        return true;
      }
    }
    return false;
  }

  public boolean isWebServiceNameChanged() {
    for (StyledTextField field : m_webServiceNameFields) {
      IType webService = (IType) field.getData(PROP_WEB_SERVICE);
      if (!Objects.equals(field.getText(), webService.getElementName())) {
        return true;
      }
    }
    return false;
  }

  public boolean isPackageChanged() {
    return m_packageField != null && !Objects.equals(m_packageField.getText(), Signature.getQualifier(getPortType(m_packageField).getFullyQualifiedName()));
  }

  protected StyledTextField getWebServiceNameField(IType portType) {
    IType webService = getInput().getWebService(portType);
    for (StyledTextField field : m_webServiceNameFields) {
      if (webService.equals(field.getData(PROP_WEB_SERVICE))) {
        return field;
      }
    }
    return null;
  }

  protected StyledTextField getPortTypeNameField(IType portType) {
    for (StyledTextField field : m_portTypeNameFields) {
      if (portType.equals(getPortType(field))) {
        return field;
      }
    }
    return null;
  }

  protected void fillOperation(WebServiceUpdateOperation op) {
    List<IFile> jaxWsBindingFiles = getInput().getJaxWsBindingFiles();
    if (!jaxWsBindingFiles.isEmpty() && (isPortTypeNameChanged() || isWebServiceNameChanged() || isPackageChanged())) {
      op.setJaxwsBindingFiles(jaxWsBindingFiles);
      op.setPackage(m_packageField.getText());
      for (StyledTextField webServiceField : m_webServiceNameFields) {
        IType webService = (IType) webServiceField.getData(PROP_WEB_SERVICE);
        op.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getWebServiceXPath(getInput().getWebServiceNameInWsdl(webService).getLocalPart()), webServiceField.getText()));
      }
      for (StyledTextField portTypeNameField : m_portTypeNameFields) {
        IType portType = getPortType(portTypeNameField);

        IType webServiceClient = getInput().getWebServiceClient(portType);
        if (S2eUtils.exists(webServiceClient)) {
          op.addWebServiceClientUpdate(new WebServiceClientUpdate(webServiceClient, m_packageField.getText(), portTypeNameField.getText(), getWebServiceNameField(portType).getText()));
        }

        IType serviceImpl = getInput().getServiceImplementation(portType);
        if (S2eUtils.exists(serviceImpl)) {
          op.addWebServiceImplementationUpdate(new WebServiceImplementationUpdate(serviceImpl, m_packageField.getText(), portTypeNameField.getText()));
        }

        op.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getPortTypeXPath(getInput().getPortTypeNameInWsdl(portType).getLocalPart()), portTypeNameField.getText()));
      }
    }

    for (int i = 0; i < m_entryPointNameFields.size(); i++) {
      StyledTextField entryPointNameField = m_entryPointNameFields.get(i);
      ProposalTextField entryPointPackageField = m_entryPointPackageFields.get(i);
      IType portType = getPortType(entryPointNameField);
      IType entryPointDefinition = getInput().getEntryPointDefinition(portType);
      if (S2eUtils.exists(entryPointDefinition)) {
        boolean isChanged = isPortTypeNameChanged() || isPackageChanged() || isEntryPointNameChanged(entryPointNameField) || isEntryPointPackageChanged(entryPointPackageField);
        if (isChanged) {
          EntryPointDefinitionUpdate up = new EntryPointDefinitionUpdate(entryPointDefinition, entryPointPackageField.getText(), entryPointNameField.getText(), getPortTypeNameField(portType).getText(), m_packageField.getText());
          op.addEntryPointDefinitionUpdate(up);
        }
      }
    }
  }

  protected void clearMessage() {
    getManagedForm().getForm().setMessage("", IMessageProvider.NONE);
  }

  protected void setMessage(IStatus status) {
    int severity = IMessageProvider.NONE;
    switch (status.getSeverity()) {
      case IStatus.ERROR:
        severity = IMessageProvider.ERROR;
        break;
      case IStatus.INFO:
        severity = IMessageProvider.INFORMATION;
        break;
      case IStatus.WARNING:
        severity = IMessageProvider.WARNING;
        break;
      default:
        severity = IMessageProvider.NONE;
        break;
    }
    getManagedForm().getForm().setMessage(status.getMessage(), severity);
  }

  protected boolean isValidJavaName(Iterable<StyledTextField> fields) {
    for (StyledTextField field : fields) {
      if (field.isEnabled()) {
        IStatus status = CompilationUnitNewWizardPage.validateJavaName(field.getText(), field.getReadOnlySuffix());
        if (!status.isOK()) {
          setMessage(status);
          return false;
        }
      }
    }
    return true;
  }

  protected boolean isValidPackage(Iterable<? extends TextField> fields) {
    for (TextField field : fields) {
      if (field.isEnabled()) {
        IStatus status = CompilationUnitNewWizardPage.validatePackageName(field.getText());
        if (!status.isOK()) {
          setMessage(status);
          return false;
        }
      }
    }
    return true;
  }

  public boolean isValid() {
    if (!isValidJavaName(m_portTypeNameFields)) {
      return false;
    }
    if (!isValidJavaName(m_webServiceNameFields)) {
      return false;
    }
    if (!isValidJavaName(m_entryPointNameFields)) {
      return false;
    }
    if (!isValidPackage(Collections.singletonList(m_packageField))) {
      return false;
    }
    if (!isValidPackage(m_entryPointPackageFields)) {
      return false;
    }
    return true;
  }

  protected void createLabeledHyperlink(Composite p, String labelText, final IFile file, final IType element, int labelWidth, int topOffset) {
    Composite parent = getFormToolkit().createComposite(p);
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(parent);
    parent.setLayout(new FormLayout());

    Label label = getFormToolkit().createLabel(parent, labelText, SWT.TRAIL);
    int fieldTopOffset = topOffset;
    int labelYOffset = 2;

    Control field = null;
    if ((file != null && file.exists()) || S2eUtils.exists(element)) {
      ImageHyperlink link = getFormToolkit().createImageHyperlink(parent, SWT.NONE);
      field = link;
      link.setUnderlined(true);

      if (file != null) {
        // file link
        link.setImage(m_decoratingWorkbenchLabelProvider.getImage(file));
        link.setToolTipText(file.getFullPath().toString());
        link.setText(file.getName());
        link.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent event) {
            S2eUiUtils.openInEditor(file);
          }
        });
      }
      else {
        // type link
        link.setImage(m_decoratingWorkbenchLabelProvider.getImage(element));
        link.setToolTipText(element.getFullyQualifiedName());
        link.setText(element.getElementName());
        link.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent event) {
            S2eUiUtils.openInEditor(element);
          }
        });
      }
    }
    else {
      // error label
      Label lbl = getFormToolkit().createLabel(parent, "Not found");
      field = lbl;
      lbl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
      fieldTopOffset += labelYOffset;
    }

    // layout
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, labelYOffset + topOffset);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(0, labelWidth);
    label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, fieldTopOffset);
    textData.left = new FormAttachment(label, 5);
    field.setLayoutData(textData);
  }

  protected void createActionToolBar(IToolBarManager manager) {
    // rebuild stub action
    Action rebuildAction = new Action("Rebuild all artifacts") {
      @Override
      public void run() {
        getEditor().rebuildAllArtifacts();
      }
    };
    rebuildAction.setToolTipText("Rebuild all artifacts of this project");
    rebuildAction.setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Wsdl));
    manager.add(rebuildAction);

    // add new web service action
    Action addServiceAction = new Action("Create new Web Service") {
      @Override
      public void run() {
        getEditor().startNewWebServiceWizard();
      }
    };
    addServiceAction.setToolTipText("Create new Web Service");
    addServiceAction.setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.WebServiceAdd));
    manager.add(addServiceAction);

    // refresh editor action
    Action refreshAction = new Action("Refresh Editor") {
      @Override
      public void run() {
        getEditor().reload(WebServiceFormPage.this.getId());
      }
    };
    refreshAction.setToolTipText("Refresh the content of this editor.");
    refreshAction.setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Refresh));
    manager.add(refreshAction);
  }

  protected Composite createSection(FormToolkit toolkit, Composite parent, String sectionLabel) {
    Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
    section.setText(sectionLabel);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(section);
    GridDataFactory
        .defaultsFor(section)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(section);

    Composite result = toolkit.createComposite(section);
    section.setClient(result);
    GridDataFactory
        .defaultsFor(result)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(result);
    return result;
  }

  public WebServiceFormPageInput getInput() {
    return m_input;
  }

  public FormToolkit getFormToolkit() {
    return getManagedForm().getToolkit();
  }

  public FieldToolkit getFieldToolkit() {
    return m_fieldToolkit;
  }

  private static final class P_ImageArea {
    private final Rectangle m_area;
    private final Object m_elementToShow;

    public P_ImageArea(int topLeftX, int topLeftY, int width, int height, Object elementToShow) {
      super();
      m_area = new Rectangle(topLeftX, topLeftY, width, height);
      m_elementToShow = elementToShow;
    }

    private boolean contains(int x, int y, int imageWidth, int containerWidth) {
      int leftOffset = (containerWidth - imageWidth) / 2;
      return m_area.contains(x - leftOffset, y);
    }
  }
}
