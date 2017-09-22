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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws;

import java.util.Map.Entry;

import javax.wsdl.PortType;
import javax.wsdl.Service;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl.WebServiceNames;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <h3>{@link WebServiceMessageDialog}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceMessageDialog extends MessageDialogWithToggle {

  public static final String SCOUT_JAX_WS_DOCUMENTATION_URL = "https://eclipsescout.github.io/7.1/technical-guide.html#webservices-with-jax-ws";
  public static final String HIDE_CONSUMER_MSG = "hideWebServiceConsumerInfoMessage";
  public static final String HIDE_PROVIDER_MSG = "hideWebServiceProviderInfoMessage";

  private boolean m_isCopyToClipboard;

  protected WebServiceMessageDialog(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
    super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex, toggleMessage, toggleState);
  }

  public static WebServiceMessageDialog open(Shell shell, WebServiceNewOperation op) {
    String message = null;
    String dialogTitle = null;
    String prefKey = null;
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
      clipboard.setContents(new Object[]{message + "\n\nJAX-WS Documentation: " + SCOUT_JAX_WS_DOCUMENTATION_URL}, new Transfer[]{TextTransfer.getInstance()});
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
    Label label = new Label(parent, SWT.NONE);
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
        S2eUiUtils.showUrlInBrowser(SCOUT_JAX_WS_DOCUMENTATION_URL);
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
    final Button copyToClipboard = new Button(parent, SWT.CHECK | SWT.LEAD);
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

  protected static String getConsumerMessage(WebServiceNewOperation op) {
    StringBuilder msgBuilder = new StringBuilder("The new Web Service consumer has been created.\n\nUsage example:\n");
    for (Entry<Service, WebServiceNames> entry : op.getParsedWsdl().getServiceNames().entrySet()) {
      for (PortType pt : op.getParsedWsdl().getPortTypes(entry.getKey())) {
        msgBuilder.append("- BEANS.get(").append(entry.getValue().getPortTypeClassName(pt.getQName().getLocalPart())).append(SuffixConstants.SUFFIX_STRING_class).append(");\n");
      }
    }
    msgBuilder.append("\nThe URL of the Web Service end point usually differs for several deployments. Therefore the following properties have been generated to configure the end point URLs:\n");
    for (String urlProperty : op.getCreatedUrlProperties()) {
      msgBuilder.append("- ").append(urlProperty).append("\n");
    }
    msgBuilder.append("Add these properties to your config.properties files.\n");
    msgBuilder.append("\nIf the Web Service requires authentication, modify the Handlers of the created Web Service Clients:\n");
    for (IType client : op.getCreatedWebServiceClients()) {
      msgBuilder.append("- ").append(client.getElementName()).append("\n");
    }
    addImplementorMsg(msgBuilder);
    return msgBuilder.toString();
  }

  protected static String getProviderMessage(WebServiceNewOperation op) {
    StringBuilder msgBuilder = new StringBuilder("The new Web Service provider has been created. The business logic can be implemented in the following classes:\n");
    for (IType providerImpl : op.getCreatedProviderServiceImpls()) {
      msgBuilder.append("- ").append(providerImpl.getElementName()).append("\n");
    }
    msgBuilder.append("\nBy default this Web Service is configured to use Basic Authentication against credentials stored in the config.properties file. This may be changed by modifying the corresponding Entry Point Defintions:\n");
    for (IType entryPointDef : op.getCreatedEntryPointDefinitions()) {
      msgBuilder.append("- ").append(entryPointDef.getElementName()).append("\n");
    }
    addImplementorMsg(msgBuilder);
    msgBuilder.append("\nDepending on your implementor and container you may also need to add the JAX-WS servlet to your web.xml file and to register the Web Service End Points.");
    return msgBuilder.toString();
  }

  protected static void addImplementorMsg(StringBuilder builder) {
    builder.append("\nThen you have to decide which JAX-WS implementor you want to use. It is strongly recommended to use the implementor that is included in your J2EE application server if any. ");
    builder.append("As soon as the implementor is known add the 'jaxws.implementor' property to your config.properties files.\n");
    builder.append("If the desired JAX-WS implementor is not included in your application server, you also have to add the necessary dependencies to your pom.xml files to include this implementor in the deployment.");
  }
}
