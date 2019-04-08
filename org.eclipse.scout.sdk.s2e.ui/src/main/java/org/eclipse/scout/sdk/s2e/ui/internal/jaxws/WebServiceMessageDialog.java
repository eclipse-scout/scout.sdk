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
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws;

import java.util.Map.Entry;

import javax.wsdl.PortType;
import javax.wsdl.Service;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.jaxws.AbstractWebServiceNewOperation;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl.WebServiceNames;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <h3>{@link WebServiceMessageDialog}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceMessageDialog extends MessageDialogWithToggle {

  public static final String HIDE_CONSUMER_MSG = "hideWebServiceConsumerInfoMessage";
  public static final String HIDE_PROVIDER_MSG = "hideWebServiceProviderInfoMessage";

  private boolean m_isCopyToClipboard;

  protected WebServiceMessageDialog(Shell parentShell, String dialogTitle, Image image, String msg, int dialogImageType, String[] dialogButtonLabels, int defaultIndex,
      String toggleMessage, boolean toggleState) {
    super(parentShell, dialogTitle, image, msg, dialogImageType, dialogButtonLabels, defaultIndex, toggleMessage, toggleState);
  }

  protected static String getScoutJaxWsDocumentationUrl() {
    String version = ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION;
    int secondDotPos = version.indexOf('.', 2);
    if (secondDotPos > 0) {
      version = version.substring(0, secondDotPos);
    }
    return "http://eclipsescout.github.io/" + version + "/technical-guide.html#webservices-with-jax-ws";
  }

  public static void open(AbstractWebServiceNewOperation op, Display d) {
    d.asyncExec(() -> {
      Shell shell = d.getActiveShell();
      if (shell == null) {
        Shell[] shells = d.getShells();
        if (shells.length > 0) {
          shell = shells[0];
        }
      }
      if (shell == null) {
        return;
      }
      open(op, shell);
    });
  }

  public static WebServiceMessageDialog open(AbstractWebServiceNewOperation op, Shell shell) {
    String message;
    String dialogTitle;
    String prefKey;
    if (op.isCreateConsumer()) {
      message = getConsumerMessage(op);
      dialogTitle = "New Web Service Consumer created";
      prefKey = HIDE_CONSUMER_MSG;
    }
    else {
      message = getProviderMessage(op);
      dialogTitle = "New Web Service Provider created";
      prefKey = HIDE_PROVIDER_MSG;
    }

    IPreferenceStore store = S2ESdkUiActivator.getDefault().getPreferenceStore();
    String doNotShowAgainString = store.getString(prefKey);
    boolean showBox = !ALWAYS.equals(doNotShowAgainString);
    if (!showBox) {
      return null;
    }

    WebServiceMessageDialog dialog = new WebServiceMessageDialog(shell, dialogTitle, null, message, INFORMATION, new String[]{IDialogConstants.OK_LABEL}, 0, "Do not show this dialog again.", false);
    int style = SWT.SHEET;
    dialog.setShellStyle(dialog.getShellStyle() | style);
    dialog.setPrefStore(store);
    dialog.setPrefKey(prefKey);
    dialog.open();
    if (dialog.isCopyToClipboard()) {
      Clipboard clipboard = new Clipboard(shell.getDisplay());
      clipboard.setContents(new Object[]{message + "\n\nJAX-WS Documentation: " + getScoutJaxWsDocumentationUrl()}, new Transfer[]{TextTransfer.getInstance()});
      clipboard.dispose();
    }
    return dialog;
  }

  @Override
  protected Control createMessageArea(Composite composite) {
    Control messageArea = super.createMessageArea(composite);
    createDocumentationHyperlink(composite);
    return messageArea;
  }

  @Override
  protected Control createCustomArea(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory
        .swtDefaults()
        .margins(0, 0)
        .applyTo(composite);
    GridDataFactory
        .defaultsFor(composite)
        .span(2, 0)
        .applyTo(composite);
    createCopyToClipboard(composite);
    return composite;
  }

  protected void createDocumentationHyperlink(Composite parent) {
    Control label = new Label(parent, SWT.NONE);
    GridDataFactory
        .fillDefaults()
        .grab(false, false)
        .applyTo(label);

    Hyperlink h = new Hyperlink(parent, SWT.WRAP);
    h.setText("See the JAX-WS Scout Documentation for details.");
    h.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    h.setUnderlined(true);
    h.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        S2eUiUtils.showUrlInBrowser(getScoutJaxWsDocumentationUrl());
      }
    });
    GridDataFactory
        .fillDefaults()
        .align(SWT.FILL, SWT.BEGINNING)
        .grab(true, false)
        .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
        .applyTo(h);
  }

  protected void createCopyToClipboard(Composite parent) {
    Button copyToClipboard = new Button(parent, SWT.CHECK | SWT.LEAD);
    copyToClipboard.setSelection(false);
    copyToClipboard.setText("Copy this message to the Clipboard.");
    copyToClipboard.setFont(parent.getFont());
    copyToClipboard.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_isCopyToClipboard = copyToClipboard.getSelection();
      }
    });
    GridDataFactory
        .defaultsFor(copyToClipboard)
        .span(2, 0)
        .applyTo(copyToClipboard);
  }

  protected boolean isCopyToClipboard() {
    return m_isCopyToClipboard;
  }

  protected static String getConsumerMessage(AbstractWebServiceNewOperation op) {
    StringBuilder msgBuilder = new StringBuilder("The new Web Service consumer has been created.\n\nUsage example:\n");
    for (Entry<Service, WebServiceNames> entry : op.getParsedWsdl().getServiceNames().entrySet()) {
      for (PortType pt : op.getParsedWsdl().getPortTypes(entry.getKey())) {
        msgBuilder.append("- BEANS.get(").append(WebServiceNames.getPortTypeClassName(pt.getQName().getLocalPart())).append(JavaTypes.CLASS_FILE_SUFFIX).append(");\n");
      }
    }
    msgBuilder.append("\nThe URL of the Web Service end point usually differs for several deployments. Therefore the following properties have been generated to configure the end point URLs:\n");
    for (String urlProperty : op.getCreatedUrlProperties()) {
      msgBuilder.append("- ").append(urlProperty).append('\n');
    }
    msgBuilder.append("Add these properties to your config.properties files.\n");
    msgBuilder.append("\nIf the Web Service requires authentication, modify the Handlers of the created Web Service Clients:\n");
    for (IType client : op.getCreatedWebServiceClients()) {
      msgBuilder.append("- ").append(client.elementName()).append('\n');
    }
    addImplementorMsg(msgBuilder);
    return msgBuilder.toString();
  }

  protected static String getProviderMessage(AbstractWebServiceNewOperation op) {
    StringBuilder msgBuilder = new StringBuilder("The new Web Service provider has been created. The business logic can be implemented in the following classes:\n");
    for (IType providerImpl : op.getCreatedProviderServiceImpls()) {
      msgBuilder.append("- ").append(providerImpl.elementName()).append('\n');
    }
    msgBuilder.append("\nBy default this Web Service is configured to use Basic Authentication against credentials stored in the config.properties file. This may be changed by modifying the corresponding Entry Point Defintions:\n");
    for (IType entryPointDef : op.getCreatedEntryPointDefinitions()) {
      msgBuilder.append("- ").append(entryPointDef.elementName()).append('\n');
    }
    addImplementorMsg(msgBuilder);
    msgBuilder.append("\nDepending on your implementor and container you may also need to add the JAX-WS servlet to your web.xml file and to register the Web Service End Points.");
    return msgBuilder.toString();
  }

  protected static void addImplementorMsg(StringBuilder builder) {
    builder.append("\nThen you have to decide which JAX-WS implementor you want to use. It is strongly recommended to use the implementor that is included in your J2EE application server if any. ");
    builder.append("As soon as the implementor is known add the 'scout.jaxws.implementor' property to your config.properties files.\n");
    builder.append("If the desired JAX-WS implementor is not included in your application server, you also have to add the necessary dependencies to your pom.xml files to include this implementor in the deployment.");
  }
}