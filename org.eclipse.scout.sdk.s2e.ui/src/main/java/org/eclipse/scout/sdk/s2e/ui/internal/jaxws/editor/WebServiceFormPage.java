/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import static java.util.Collections.singletonList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.BindingClassUpdate;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.EntryPointDefinitionUpdate;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.WebServiceClientUpdate;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.WebServiceImplementationUpdate;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractCompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * <h3>{@link WebServiceFormPage}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceFormPage extends FormPage {

  public static final String UPDATE_FOR_ALL_SERVICES_QUESTION_KEY = "doNotShowWsChangeGenerateElementForAll";

  private final WebServiceFormPageInput m_input;
  private final ILabelProvider m_decoratingWorkbenchLabelProvider;

  // stores the values of the UI fields so that it can be read from outside UI thread
  private String m_package;
  private final Map<Control, IType> m_portTypes;
  private final Map<Control, String> m_portTypeNames;
  private final Map<Control, IType> m_webServices;
  private final Map<Control, String> m_webServiceNames;
  private final Map<Control, String> m_entryPointPackageNames;
  private final Map<Control, String> m_entryPointNames;

  // ui fields
  private final List<StyledTextField> m_portTypeNameFields;
  private ProposalTextField m_packageField;
  private final List<StyledTextField> m_webServiceNameFields;
  private final List<StyledTextField> m_entryPointNameFields;
  private final List<ProposalTextField> m_entryPointPackageFields;

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public WebServiceFormPage(FormEditor editor, WebServiceFormPageInput input) {
    super(Ensure.notNull(editor), Ensure.notNull(input).getWsdl().getFileName().toString(), input.getDisplayName());
    m_input = input;
    m_portTypes = new HashMap<>();
    m_portTypeNames = new HashMap<>();
    m_webServices = new HashMap<>();
    m_webServiceNames = new HashMap<>();
    m_entryPointPackageNames = new HashMap<>();
    m_entryPointNames = new HashMap<>();
    m_entryPointNameFields = new ArrayList<>();
    m_entryPointPackageFields = new ArrayList<>();
    m_portTypeNameFields = new ArrayList<>();
    m_webServiceNameFields = new ArrayList<>();
    m_decoratingWorkbenchLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
  }

  @Override
  public WebServiceEditor getEditor() {
    return (WebServiceEditor) super.getEditor();
  }

  @Override
  protected void createFormContent(IManagedForm managedForm) {
    super.createFormContent(managedForm);
    var scrolledForm = managedForm.getForm();
    getFormToolkit().decorateFormHeading(scrolledForm.getForm());
    createActionToolBar(scrolledForm.getToolBarManager());
    scrolledForm.setText(getInput().getDisplayName());
    scrolledForm.updateToolBar();

    var body = scrolledForm.getBody();
    createBody(body);
    setEnabled(true);
    scrolledForm.reflow(true);
    if (getInput().hasProviderElements()) {
      // if it is a provider: validate annotation processing settings
      validateAnnotationProcessingSettings();
    }
    PlatformUI.getWorkbench().getHelpSystem().setHelp(body, IScoutHelpContextIds.SCOUT_WEB_SERVICE_EDITOR_PAGE);
  }

  protected void createOverviewImage(Composite parent, String imgName, Iterable<P_ImageArea> areas, int horizontalSpan) {
    var overviewImage = S2ESdkUiActivator.getImage(imgName);
    var imageWidth = overviewImage.getBounds().width;
    var label = getFormToolkit().createLabel(parent, "", SWT.CENTER);
    label.setImage(overviewImage);
    label.addMouseMoveListener(e -> {
      for (var area : areas) {
        if (area.contains(e.x, e.y, imageWidth, label.getSize().x)) {
          parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
          return;
        }
      }
      parent.setCursor(null);// reset to default
    });
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        if (e.button != 1) {
          return;
        }
        for (var area : areas) {
          if (area.contains(e.x, e.y, imageWidth, label.getSize().x)) {
            var element = area.m_elementToShow;
            if (element instanceof IJavaElement) {
              S2eUiUtils.openInEditor((IJavaElement) element, true);
            }
            else if (element instanceof Path) {
              S2eUiUtils.openInEditor((Path) element, true);
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

    var allPortTypes = getInput().getAllPortTypes();
    if (!allPortTypes.isEmpty()) {
      var labelColWidth = 180;
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

  protected void createConsumerGroup(Composite p, Collection<IType> allPortTypes, int labelColWidth) {
    var numPortTypes = allPortTypes.size();
    var firstPortType = allPortTypes.iterator().next();
    var webServices = getInput().getWebServices();
    var firstWebService = webServices.iterator().next();

    var parent = createSection(getFormToolkit(), p, "Consumer");
    GridLayoutFactory
        .swtDefaults()
        .numColumns(numPortTypes)
        .equalWidth(true)
        .applyTo(parent);

    Collection<P_ImageArea> clickAreas = new ArrayList<>(4);
    clickAreas.add(new P_ImageArea(10, 60, 180, 33, getInput().getWsdl()));
    clickAreas.add(new P_ImageArea(460, 110, 170, 50, getInput().getWebServiceClient(firstPortType)));
    clickAreas.add(new P_ImageArea(460, 10, 170, 50, firstPortType));
    clickAreas.add(new P_ImageArea(345, 160, 95, 50, firstWebService));
    createOverviewImage(parent, ISdkIcons.WsConsumerOverview, clickAreas, numPortTypes);

    createLabeledHyperlink(parent, "WSDL", getInput().getWsdl(), null, labelColWidth, 0);
    for (var i = 0; i < numPortTypes - 1; i++) {
      getFormToolkit().createLabel(parent, "");
    }
    for (var portType : allPortTypes) {
      createLabeledHyperlink(parent, "Web Service Client", null, getInput().getWebServiceClient(portType), labelColWidth, 0);
    }
    for (var portType : allPortTypes) {
      createPortTypeNameField(parent, portType, labelColWidth);
    }

    for (var webservice : webServices) {
      var numPortTypesInThisService = getInput().getPortTypes(webservice).size();
      var webServiceNameField = FieldToolkit.createStyledTextField(parent, "Web Service", TextField.TYPE_HYPERLINK | TextField.TYPE_IMAGE, labelColWidth);
      GridDataFactory
          .defaultsFor(webServiceNameField)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .span(numPortTypesInThisService, 1)
          .applyTo(webServiceNameField);
      webServiceNameField.setReadOnlySuffix(ISdkConstants.SUFFIX_WS_SERVICE);
      webServiceNameField.setText(webservice.getElementName());

      m_webServices.put(webServiceNameField, webservice);
      m_webServiceNames.put(webServiceNameField, webServiceNameField.getText());
      webServiceNameField.addModifyListener(e -> {
        m_webServiceNames.put(webServiceNameField, webServiceNameField.getText());
        clearMessage();
        getManagedForm().dirtyStateChanged();
      });
      webServiceNameField.setImage(m_decoratingWorkbenchLabelProvider.getImage(webservice));
      webServiceNameField.getLabelComponent().setToolTipText(webservice.getFullyQualifiedName());
      webServiceNameField.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          S2eUiUtils.openInEditor(webservice, true);
        }
      });
      m_webServiceNameFields.add(webServiceNameField);
    }

    createPackageField(parent, firstPortType, labelColWidth, numPortTypes);
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected void createProviderGroup(Composite p, Collection<IType> allPortTypes, int labelColWidth) {
    var numPortTypes = allPortTypes.size();
    var firstPortType = allPortTypes.iterator().next();
    var parent = createSection(getFormToolkit(), p, "Provider");
    GridLayoutFactory
        .swtDefaults()
        .numColumns(numPortTypes)
        .equalWidth(true)
        .applyTo(parent);

    // overview image
    Collection<P_ImageArea> clickAreas = new ArrayList<>(5);
    clickAreas.add(new P_ImageArea(11, 10, 180, 33, getInput().getWsdl()));
    clickAreas.add(new P_ImageArea(545, 159, 196, 50, getInput().getServiceImplementation(firstPortType)));
    clickAreas.add(new P_ImageArea(445, 49, 170, 50, firstPortType));
    clickAreas.add(new P_ImageArea(385, 159, 95, 50, getInput().getEntryPoint(firstPortType)));
    clickAreas.add(new P_ImageArea(10, 154, 180, 60, getInput().getEntryPointDefinition(firstPortType)));
    createOverviewImage(parent, ISdkIcons.WsProviderOverview, clickAreas, numPortTypes);

    createLabeledHyperlink(parent, "WSDL", getInput().getWsdl(), null, labelColWidth, 0);
    for (var i = 0; i < numPortTypes - 1; i++) {
      getFormToolkit().createLabel(parent, "");
    }

    for (var portType : allPortTypes) {
      createLabeledHyperlink(parent, "Web Service Implementation", null, getInput().getServiceImplementation(portType), labelColWidth, 0);
    }
    for (var portType : allPortTypes) {
      createPortTypeNameField(parent, portType, labelColWidth);
    }
    createPackageField(parent, firstPortType, labelColWidth, numPortTypes);

    for (var portType : allPortTypes) {
      createLabeledHyperlink(parent, "Entry Point Definition", null, getInput().getEntryPointDefinition(portType), labelColWidth, 20);
    }

    // entry point name
    for (var portType : allPortTypes) {
      var entryPointNameField = FieldToolkit.createStyledTextField(parent, "Entry Point", TextField.TYPE_HYPERLINK | TextField.TYPE_IMAGE, labelColWidth);
      GridDataFactory
          .defaultsFor(entryPointNameField)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(entryPointNameField);
      var nameInDefinition = getInput().getEntryPointNameFromDefinition(portType);
      if (Strings.hasText(nameInDefinition)) {
        entryPointNameField.setReadOnlySuffix(ISdkConstants.SUFFIX_WS_ENTRY_POINT);
        entryPointNameField.setText(nameInDefinition);
      }
      var entryPoint = getInput().getEntryPoint(portType);
      m_portTypes.put(entryPointNameField, portType);
      m_entryPointNames.put(entryPointNameField, entryPointNameField.getText());
      entryPointNameField.addModifyListener(e -> {
        m_entryPointNames.put(entryPointNameField, entryPointNameField.getText());
        clearMessage();
        getManagedForm().dirtyStateChanged();
      });
      if (JdtUtils.exists(entryPoint)) {
        entryPointNameField.setImage(m_decoratingWorkbenchLabelProvider.getImage(entryPoint));
        entryPointNameField.getLabelComponent().setToolTipText(entryPoint.getFullyQualifiedName());
        entryPointNameField.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent e) {
            S2eUiUtils.openInEditor(entryPoint, true);
          }
        });
      }
      m_entryPointNameFields.add(entryPointNameField);
    }

    // entry point package
    for (var portType : allPortTypes) {
      var entryPointPackageField = FieldToolkit.createPackageField(parent, "Entry Point Package", getInput().getJavaProject(), labelColWidth, TextField.TYPE_LABEL | TextField.TYPE_IMAGE);
      GridDataFactory
          .defaultsFor(entryPointPackageField)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(entryPointPackageField);
      var packageInDefinition = getInput().getEntryPointPackageFromDefinition(portType);
      if (Strings.hasText(packageInDefinition)) {
        entryPointPackageField.setText(packageInDefinition);
      }
      m_portTypes.put(entryPointPackageField, portType);
      m_entryPointPackageNames.put(entryPointPackageField, entryPointPackageField.getText());
      entryPointPackageField.setImage(m_decoratingWorkbenchLabelProvider.getImage(portType.getPackageFragment()));
      entryPointPackageField.addModifyListener(e -> {
        m_entryPointPackageNames.put(entryPointPackageField, entryPointPackageField.getText());
        clearMessage();
        getManagedForm().dirtyStateChanged();
      });
      m_entryPointPackageFields.add(entryPointPackageField);
    }

    for (var portType : allPortTypes) {
      createHandlersList(parent, portType, labelColWidth, numPortTypes);
    }
    for (var portType : allPortTypes) {
      var authMethodFromDefinition = getInput().getAuthMethodFromDefinition(portType);
      if (JdtUtils.exists(authMethodFromDefinition)) {
        createLabeledHyperlink(parent, "Authentication Method", null, authMethodFromDefinition, labelColWidth, 0);
      }
    }
    for (var portType : allPortTypes) {
      var authVerifierFromDefinition = getInput().getAuthVerifierFromDefinition(portType);
      if (JdtUtils.exists(authVerifierFromDefinition)) {
        createLabeledHyperlink(parent, "Authentication Verifier", null, authVerifierFromDefinition, labelColWidth, 0);
      }
    }
  }

  protected void createHandlersList(Composite p, IType portType, int labelColWidth, int numPortTypes) {
    var handlers = getInput().getHandlers(portType);
    if (handlers.isEmpty() && numPortTypes <= 1) {
      return; // don't draw anything if we have only one column and there are no handlers
    }
    var parent = getFormToolkit().createComposite(p);
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.BEGINNING)
        .grab(true, false)
        .indent(0, 20)
        .applyTo(parent);
    parent.setLayout(new FormLayout());
    if (!handlers.isEmpty()) {
      var label = getFormToolkit().createLabel(parent, "Handlers", SWT.TRAIL);

      var handlersComposite = getFormToolkit().createComposite(parent);
      GridLayoutFactory
          .swtDefaults()
          .margins(0, 0)
          .applyTo(handlersComposite);

      for (var handler : handlers) {
        var link = getFormToolkit().createImageHyperlink(handlersComposite, SWT.NONE);
        link.setUnderlined(true);
        link.setImage(m_decoratingWorkbenchLabelProvider.getImage(handler));
        link.setToolTipText(handler.getFullyQualifiedName());
        link.setText(handler.getElementName());
        link.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent e) {
            S2eUiUtils.openInEditor(handler, true);
          }
        });
      }

      var labelData = new FormData();
      labelData.top = new FormAttachment(0, 2);
      labelData.left = new FormAttachment(0, 0);
      labelData.right = new FormAttachment(0, labelColWidth);
      labelData.bottom = new FormAttachment(100, 0);
      label.setLayoutData(labelData);

      var textData = new FormData();
      textData.top = new FormAttachment(0, 0);
      textData.left = new FormAttachment(label, 5);
      textData.right = new FormAttachment(100, 0);
      textData.bottom = new FormAttachment(100, 0);
      handlersComposite.setLayoutData(textData);
    }
  }

  protected void setEnabled(boolean enabled) {
    var managedForm = getManagedForm();
    if (managedForm == null) {
      return;
    }
    var form = managedForm.getForm();
    if (form.isDisposed()) {
      return;
    }

    form.setEnabled(enabled);
    form.getBody().setEnabled(enabled);

    var jaxwsBindingFileExists = !getInput().getJaxWsBindingFiles().isEmpty();
    for (var field : m_portTypeNameFields) {
      field.setEnabled(enabled && jaxwsBindingFileExists);
    }
    m_packageField.setEnabled(enabled && jaxwsBindingFileExists);
    for (var field : m_webServiceNameFields) {
      field.setEnabled(enabled && jaxwsBindingFileExists);
    }
    for (var field : m_entryPointNameFields) {
      field.setEnabled(enabled && JdtUtils.exists(getInput().getEntryPointDefinition(getPortType(field))));
    }
    for (var field : m_entryPointPackageFields) {
      field.setEnabled(enabled && JdtUtils.exists(getInput().getEntryPointDefinition(getPortType(field))));
    }
  }

  protected IType getPortType(Control control) {
    return m_portTypes.get(control);
  }

  protected IType getWebService(Control control) {
    return m_webServices.get(control);
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected void createBindingGroup(Composite p) {
    var parent = createSection(getFormToolkit(), p, "Bindings");
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);

    for (var f : getInput().getBindings()) {
      var field = getFormToolkit().createImageHyperlink(parent, SWT.NONE);
      field.setUnderlined(true);
      field.setText(f.getFileName().toString());
      field.setImage(m_decoratingWorkbenchLabelProvider.getImage(f));
      GridDataFactory
          .defaultsFor(field)
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(field);
      field.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          S2eUiUtils.openInEditor(f, true);
        }
      });
    }
  }

  protected void createPackageField(Composite parent, IType portType, int labelColWidth, int horizontalSpan) {
    m_packageField = FieldToolkit.createPackageField(parent, "Package", getInput().getJavaProject(), labelColWidth, TextField.TYPE_LABEL | TextField.TYPE_IMAGE);
    GridDataFactory
        .defaultsFor(m_packageField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .span(horizontalSpan, 1)
        .applyTo(m_packageField);
    var packageInBinding = JavaTypes.qualifier(portType.getFullyQualifiedName());
    if (Strings.hasText(packageInBinding)) {
      m_packageField.setText(packageInBinding);
    }
    setPackage(m_packageField.getText());
    m_packageField.setImage(m_decoratingWorkbenchLabelProvider.getImage(portType.getPackageFragment()));
    m_portTypes.put(m_packageField, portType);
    m_packageField.addModifyListener(e -> {
      clearMessage();
      setPackage(m_packageField.getText());
      getManagedForm().dirtyStateChanged();
    });
  }

  protected void createPortTypeNameField(Composite parent, IType portType, int labelColWidth) {
    var portTypeNameField = FieldToolkit.createStyledTextField(parent, "Port Type (EPI)", TextField.TYPE_HYPERLINK | TextField.TYPE_IMAGE, labelColWidth);
    GridDataFactory
        .defaultsFor(portTypeNameField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(portTypeNameField);
    var nameInBinding = portType.getElementName();
    if (Strings.hasText(nameInBinding)) {
      portTypeNameField.setReadOnlyPrefix("I");
      portTypeNameField.setReadOnlySuffix(ISdkConstants.SUFFIX_WS_PORT_TYPE);
      portTypeNameField.setText(nameInBinding);
    }
    m_portTypes.put(portTypeNameField, portType);
    m_portTypeNames.put(portTypeNameField, portTypeNameField.getText());
    portTypeNameField.getLabelComponent().setToolTipText(portType.getFullyQualifiedName());
    portTypeNameField.setImage(m_decoratingWorkbenchLabelProvider.getImage(portType));
    portTypeNameField.addModifyListener(e -> {
      clearMessage();
      m_portTypeNames.put(portTypeNameField, portTypeNameField.getText());
      getManagedForm().dirtyStateChanged();
    });
    portTypeNameField.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        S2eUiUtils.openInEditor(portType, true);
      }
    });
    m_portTypeNameFields.add(portTypeNameField);
  }

  public boolean isPortTypeNameChanged() {
    return m_portTypeNameFields.stream()
        .anyMatch(field -> !Objects.equals(m_portTypeNames.get(field), getPortType(field).getElementName()));
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
    return isEntryPointPackageChanged();
  }

  protected boolean isEntryPointPackageChanged(TextField field) {
    return !Objects.equals(m_entryPointPackageNames.get(field), Strings.notEmpty(getInput().getEntryPointPackageFromDefinition(getPortType(field))).orElse(""));
  }

  public boolean isEntryPointPackageChanged() {
    return m_entryPointPackageFields.stream()        .anyMatch(this::isEntryPointPackageChanged);
  }

  protected boolean isEntryPointNameChanged(TextField field) {
    return !Objects.equals(m_entryPointNames.get(field), Strings.notEmpty(getInput().getEntryPointNameFromDefinition(getPortType(field))).orElse(""));
  }

  public boolean isEntryPointNameChanged() {
    return m_entryPointNameFields.stream().anyMatch(this::isEntryPointNameChanged);
  }

  public boolean isWebServiceNameChanged() {
    for (var field : m_webServiceNameFields) {
      IJavaElement webService = getWebService(field);
      if (!Objects.equals(m_webServiceNames.get(field), webService.getElementName())) {
        return true;
      }
    }
    return false;
  }

  public boolean isPackageChanged() {
    return m_packageField != null && !Objects.equals(getPackage(), JavaTypes.qualifier(getPortType(m_packageField).getFullyQualifiedName()));
  }

  protected StyledTextField getWebServiceNameField(IType portType) {
    var webService = getInput().getWebService(portType);
    return m_webServiceNameFields.stream()
        .filter(field -> webService.equals(getWebService(field)))
        .findFirst()
        .orElse(null);
  }

  protected StyledTextField getPortTypeNameField(IType portType) {
    return m_portTypeNameFields.stream()
        .filter(field -> portType.equals(getPortType(field)))
        .findFirst()
        .orElse(null);
  }

  protected void fillOperation(WebServiceUpdateOperation op, EclipseEnvironment adapter) {
    var jaxWsBindingFiles = getInput().getJaxWsBindingFiles();
    var newPackage = getPackage();
    if (!jaxWsBindingFiles.isEmpty() && (isPortTypeNameChanged() || isWebServiceNameChanged() || isPackageChanged())) {
      op.setJaxwsBindingFiles(jaxWsBindingFiles);
      op.setPackage(newPackage);
      for (var webServiceField : m_webServiceNameFields) {
        var webService = getWebService(webServiceField);
        op.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getWebServiceXPath(getInput().getWebServiceNameInWsdl(webService).getLocalPart()), m_webServiceNames.get(webServiceField)));
      }

      for (var portTypeNameField : m_portTypeNameFields) {
        var portType = getPortType(portTypeNameField);
        var webServiceClient = getInput().getWebServiceClient(portType);
        var newPortTypeName = m_portTypeNames.get(portTypeNameField);

        if (JdtUtils.exists(webServiceClient)) {
          var newWebServiceName = m_webServiceNames.get(getWebServiceNameField(portType));
          var sourceFolder = adapter.toScoutSourceFolder(JdtUtils.getSourceFolder(webServiceClient));
          op.addWebServiceClientUpdate(new WebServiceClientUpdate(adapter.toScoutType(webServiceClient),
              newPackage,
              newPortTypeName,
              newWebServiceName,
              sourceFolder));
        }

        var serviceImpl = getInput().getServiceImplementation(portType);
        if (JdtUtils.exists(serviceImpl)) {
          var sourceFolder = adapter.toScoutSourceFolder(JdtUtils.getSourceFolder(serviceImpl));
          op.addWebServiceImplementationUpdate(new WebServiceImplementationUpdate(adapter.toScoutType(serviceImpl),
              newPackage,
              newPortTypeName,
              sourceFolder));
        }

        op.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getPortTypeXPath(getInput().getPortTypeNameInWsdl(portType).getLocalPart()), newPortTypeName));
      }
    }

    for (var i = 0; i < m_entryPointNameFields.size(); i++) {
      var entryPointNameField = m_entryPointNameFields.get(i);
      var entryPointPckField = m_entryPointPackageFields.get(i);
      var portType = getPortType(entryPointNameField);
      var entryPointDefinition = getInput().getEntryPointDefinition(portType);
      if (JdtUtils.exists(entryPointDefinition)) {
        var isChanged = isPortTypeNameChanged() || isPackageChanged() || isEntryPointNameChanged(entryPointNameField) || isEntryPointPackageChanged(entryPointPckField);
        if (isChanged) {
          var sourceFolder = adapter.toScoutSourceFolder(JdtUtils.getSourceFolder(entryPointDefinition));
          op.addEntryPointDefinitionUpdate(new EntryPointDefinitionUpdate(adapter.toScoutType(entryPointDefinition),
              m_entryPointPackageNames.get(entryPointPckField),
              m_entryPointNames.get(entryPointNameField),
              m_portTypeNames.get(getPortTypeNameField(portType)),
              newPackage,
              sourceFolder));
        }
      }
    }
  }

  protected void clearMessage() {
    getManagedForm().getForm().setMessage("", IMessageProvider.NONE);
  }

  protected void setMessage(IStatus status) {
    var severity = switch (status.getSeverity()) {
      case IStatus.ERROR -> IMessageProvider.ERROR;
      case IStatus.INFO -> IMessageProvider.INFORMATION;
      case IStatus.WARNING -> IMessageProvider.WARNING;
      default -> IMessageProvider.NONE;
    };
    getManagedForm().getForm().setMessage(status.getMessage(), severity);
  }

  protected void validateAnnotationProcessingSettings() {
    var javaProject = getInput().getJavaProject();
    var processAnnotations = javaProject.getOption(WebServiceNewOperation.PROCESS_ANNOTATIONS_KEY, false);
    if (!WebServiceNewOperation.PROCESS_ANNOTATIONS_VALUE.equals(processAnnotations)) {
      setMessage(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Annotation processing is not correctly enabled. Open preference file 'org.eclipse.jdt.core.prefs' in '" + javaProject.getElementName()
          + "/.settings/' and set property '" + WebServiceNewOperation.PROCESS_ANNOTATIONS_KEY + "' to '" + WebServiceNewOperation.PROCESS_ANNOTATIONS_VALUE + "'."));
      return;
    }

    var aptPluginPreferenceNode = new ProjectScope(javaProject.getProject()).getNode(WebServiceNewOperation.JDT_APT_SETTINGS_NODE);
    var aptEnabled = aptPluginPreferenceNode.getBoolean(WebServiceNewOperation.APT_ENABLED_KEY, false);
    if (aptEnabled != WebServiceNewOperation.APT_ENABLED_VALUE) {
      setMessage(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Annotation processing is not correctly enabled. Open the properties of project '" + javaProject.getElementName()
          + "' -> 'Java Compiler' -> 'Annotation Processing' and check 'Enable annotation processing'."));
      return;
    }

    var aptReconcileEnabled = aptPluginPreferenceNode.getBoolean(WebServiceNewOperation.APT_RECONCILE_ENABLED_KEY, false);
    if (aptReconcileEnabled != WebServiceNewOperation.APT_RECONCILE_ENABLED_VALUE) {
      setMessage(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Annotation processing is not correctly enabled. Open the properties of project '" + javaProject.getElementName()
          + "' -> 'Java Compiler' -> 'Annotation Processing' and check 'Enable processing in editor'."));
      return;
    }

    if (!javaProject.getProject().getFile(WebServiceNewOperation.FACTORY_PATH_FILE_NAME).exists()) {
      setMessage(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Annotation processing is not correctly enabled. The '" + WebServiceNewOperation.FACTORY_PATH_FILE_NAME + "' file is missing."));
    }
  }

  protected boolean isValidJavaName(Iterable<StyledTextField> fields) {
    for (var field : fields) {
      if (field.isEnabled()) {
        var status = AbstractCompilationUnitNewWizardPage.validateJavaName(field.getText(), field.getReadOnlySuffix());
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
        var status = AbstractCompilationUnitNewWizardPage.validatePackageName(field.getText());
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
    if (!isValidPackage(singletonList(m_packageField))) {
      return false;
    }
    return isValidPackage(m_entryPointPackageFields);
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected void createLabeledHyperlink(Composite p, String labelText, Path file, IType element, int labelWidth, int topOffset) {
    var parent = getFormToolkit().createComposite(p);
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(parent);
    parent.setLayout(new FormLayout());

    var label = getFormToolkit().createLabel(parent, labelText, SWT.TRAIL);
    var fieldTopOffset = topOffset;
    var labelYOffset = 2;

    Control field;
    if ((file != null && Files.isReadable(file) && Files.isRegularFile(file)) || JdtUtils.exists(element)) {
      var link = getFormToolkit().createImageHyperlink(parent, SWT.NONE);
      field = link;
      link.setUnderlined(true);

      if (file != null) {
        // file link
        link.setImage(m_decoratingWorkbenchLabelProvider.getImage(file));
        link.setToolTipText(file.toString());
        link.setText(file.getFileName().toString());
        link.addHyperlinkListener(new HyperlinkAdapter() {
          @Override
          public void linkActivated(HyperlinkEvent event) {
            S2eUiUtils.openInEditor(file, true);
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
            S2eUiUtils.openInEditor(element, true);
          }
        });
      }
    }
    else {
      // error label
      var lbl = getFormToolkit().createLabel(parent, "Not found");
      field = lbl;
      lbl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
      fieldTopOffset += labelYOffset;
    }

    // layout
    var labelData = new FormData();
    labelData.top = new FormAttachment(0, labelYOffset + topOffset);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(0, labelWidth);
    label.setLayoutData(labelData);

    var textData = new FormData();
    textData.top = new FormAttachment(0, fieldTopOffset);
    textData.left = new FormAttachment(label, 5);
    field.setLayoutData(textData);
  }

  protected void createActionToolBar(IContributionManager manager) {
    // rebuild stub action
    IAction rebuildAction = new Action("Rebuild all artifacts") {
      @Override
      public void run() {
        getEditor().rebuildAllArtifacts();
      }
    };
    rebuildAction.setToolTipText("Rebuild all artifacts of this project");
    rebuildAction.setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Wsdl));
    manager.add(rebuildAction);

    // add new web service action
    IAction addServiceAction = new Action("Create new Web Service") {
      @Override
      public void run() {
        getEditor().startNewWebServiceWizard();
      }
    };
    addServiceAction.setToolTipText("Create new Web Service");
    addServiceAction.setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.WebServiceAdd));
    manager.add(addServiceAction);

    // refresh editor action
    IAction refreshAction = new Action("Refresh Editor") {
      @Override
      public void run() {
        getEditor().reload(WebServiceFormPage.this.getId());
      }
    };
    refreshAction.setToolTipText("Refresh the content of this editor.");
    refreshAction.setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Refresh));
    manager.add(refreshAction);
  }

  protected static Composite createSection(FormToolkit toolkit, Composite parent, String sectionLabel) {
    var section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
    section.setText(sectionLabel);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(section);
    GridDataFactory
        .defaultsFor(section)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(section);

    var result = toolkit.createComposite(section);
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

  protected String getPackage() {
    return m_package;
  }

  protected void setPackage(String pck) {
    m_package = pck;
  }

  private static final class P_ImageArea {
    private final Rectangle m_area;
    private final Object m_elementToShow;

    private P_ImageArea(int topLeftX, int topLeftY, int width, int height, Object elementToShow) {
      m_area = new Rectangle(topLeftX, topLeftY, width, height);
      m_elementToShow = elementToShow;
    }

    private boolean contains(int x, int y, int imageWidth, int containerWidth) {
      var leftOffset = (containerWidth - imageWidth) / 2;
      return m_area.contains(x - leftOffset, y);
    }
  }
}
