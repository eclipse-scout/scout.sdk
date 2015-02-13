/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPresenterValueChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public abstract class AbstractPropertyPresenter<T> extends AbstractPresenter {

  public static final int DEFAULT_LABEL_WIDTH = 120;

  private T m_value;
  protected int m_presenterId;
  private Composite m_container;
  private String m_labelText;
  private ImageDescriptor m_iconImageDescriptor;
  private String m_iconTooltip;
  private String m_tooltip;
  private String m_resetTooltip;
  private List<IPresenterValueChangedListener> m_valueChangedListeners;
  private boolean m_acceptNullValue;
  private boolean m_boldLabelText;
  private boolean m_useLinkAsLabel;
  private boolean m_resetLinkVisible;
  private boolean m_linkAlwaysEnabled;

  private int m_labelWidth;
  private Label m_label;
  private Hyperlink m_link;
  protected Control m_content;
  private Label m_statusIcon;
  private Label m_icon;
  protected ImageHyperlink m_resetLink;

  private MarkerType m_markerType;
  protected IScoutBundle m_bundle;
  private String m_markerGroupUUID;

  private String m_customInfo;
  private int m_customSeverity;

  private int m_stateChanging;

  public AbstractPropertyPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    this(parent, toolkit, DEFAULT_LABEL_WIDTH, true);
  }

  public AbstractPropertyPresenter(Composite parent, PropertyViewFormToolkit toolkit, boolean initialize) {
    this(parent, toolkit, DEFAULT_LABEL_WIDTH, initialize);
  }

  public AbstractPropertyPresenter(Composite parent, PropertyViewFormToolkit toolkit, int labelWidth, boolean initialize) {
    super(toolkit, parent);
    m_labelWidth = labelWidth;
    m_valueChangedListeners = new LinkedList<>();
    m_resetTooltip = Texts.get("Remove");
    if (initialize) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    createPresenter();
  }

  private Control createPresenter() {
    m_container = getContainer();

    m_label = new Label(m_container, SWT.NONE);
    m_label.setText(StringUtility.join("", m_labelText, ":"));
    m_label.setToolTipText(m_tooltip);

    m_link = getToolkit().createHyperlink(m_container, StringUtility.join("", m_labelText, ":"), SWT.NONE);
    m_link.setToolTipText(m_tooltip);
    m_link.setUnderlined(true);
    m_link.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(HyperlinkEvent event) {
        try {
          execLinkAction();
        }
        catch (CoreException e) {
          JaxWsSdk.logError(e);
        }
      }
    });

    m_icon = new Label(m_container, SWT.NONE);
    if (getIconImageDescriptor() != null) {
      m_icon.setImage(getIconImageDescriptor().createImage());
    }
    m_icon.setToolTipText(StringUtility.nvl(getIconTooltip(), ""));

    // content
    m_content = createContent(m_container);

    m_statusIcon = new Label(m_container, SWT.NONE);
    m_statusIcon.setImage(ScoutSdkUi.getImage(ScoutSdkUi.StatusError));

    m_resetLink = getToolkit().createImageHyperlink(m_container, SWT.NONE);
    m_resetLink.setToolTipText(StringUtility.nvl(m_resetTooltip, ""));
    m_resetLink.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolRemove));
    m_resetLink.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(HyperlinkEvent event) {
        try {
          execResetAction();
        }
        catch (CoreException e) {
          JaxWsSdk.logError(e);
        }
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.marginWidth = 0;
    layout.numColumns = 5;
    layout.marginBottom = 0;
    layout.marginTop = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 2;
    layout.makeColumnsEqualWidth = false;
    m_container.setLayout(layout);

    boolean labelVisible = !m_useLinkAsLabel;
    GridData gd = new GridData();
    gd.widthHint = m_labelWidth;
    gd.horizontalAlignment = SWT.LEFT;
    gd.grabExcessHorizontalSpace = false;
    gd.exclude = !labelVisible;
    gd.horizontalIndent = 1; // as link takes more place on the left side
    m_label.setLayoutData(gd);
    m_label.setVisible(labelVisible);

    boolean linkVisible = m_useLinkAsLabel;
    gd = new GridData();
    gd.widthHint = m_labelWidth;
    gd.horizontalAlignment = SWT.LEFT;
    gd.grabExcessHorizontalSpace = false;
    gd.exclude = !linkVisible;
    m_link.setLayoutData(gd);
    m_link.setVisible(linkVisible);

    gd = new GridData();
    gd.widthHint = 16;
    gd.horizontalAlignment = SWT.RIGHT;
    m_icon.setLayoutData(gd);
    m_icon.setVisible(getIconImageDescriptor() != null);

    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gd.horizontalIndent = 5;
    gd.grabExcessHorizontalSpace = true;
    m_content.setLayoutData(gd);

    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    m_statusIcon.setLayoutData(gd);

    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    gd.exclude = !m_resetLinkVisible;
    gd.horizontalIndent = 5;
    m_resetLink.setLayoutData(gd);
    m_resetLink.setVisible(m_resetLinkVisible);

    setUseLinkAsLabel(m_useLinkAsLabel);
    setBoldLabelText(m_boldLabelText);

    return m_container;
  }

  public void setPresenterId(int presenterId) {
    m_presenterId = presenterId;
  }

  public void setLabel(String label) {
    m_labelText = label;
    if (isControlCreated()) {
      m_label.setText(StringUtility.nvl(m_labelText, ""));
      m_link.setText(StringUtility.nvl(m_labelText, ""));
    }
  }

  public String getLabel() {
    return m_labelText;
  }

  public ImageDescriptor getIconImageDescriptor() {
    return m_iconImageDescriptor;
  }

  public void setIconImageDescriptor(ImageDescriptor iconImageDescriptor) {
    m_iconImageDescriptor = iconImageDescriptor;
    if (isControlCreated()) {
      if (iconImageDescriptor != null) {
        m_icon.setImage(iconImageDescriptor.createImage());
      }
      else {
        m_icon.setImage(null);
      }
      m_icon.setToolTipText(StringUtility.nvl(m_iconTooltip, ""));
      m_icon.setVisible(iconImageDescriptor != null);
      m_icon.getParent().layout(true);
    }
  }

  public String getIconTooltip() {
    return m_iconTooltip;
  }

  public void setIconTooltip(String iconTooltip) {
    m_iconTooltip = iconTooltip;
    if (isControlCreated()) {
      m_icon.setToolTipText(StringUtility.nvl(iconTooltip, ""));
    }
  }

  public void setTooltip(String tooltip) {
    m_tooltip = tooltip;
    if (isControlCreated()) {
      m_label.setToolTipText(StringUtility.nvl(tooltip, ""));
      m_link.setToolTipText(StringUtility.nvl(tooltip, ""));
    }
  }

  public void setResetTooltip(String resetTooltip) {
    m_resetTooltip = resetTooltip;
    if (isControlCreated()) {
      m_resetLink.setToolTipText(StringUtility.nvl(resetTooltip, ""));
    }
  }

  public boolean isUseLinkAsLabel() {
    return m_useLinkAsLabel;
  }

  public void setUseLinkAsLabel(boolean useLinkAsLabel) {
    if (m_useLinkAsLabel == useLinkAsLabel) {
      return;
    }

    m_useLinkAsLabel = useLinkAsLabel;
    if (isControlCreated()) {
      ((GridData) m_label.getLayoutData()).exclude = m_useLinkAsLabel;
      m_label.setVisible(!m_useLinkAsLabel); // required as exclude does not hide the control properly (bug)
      ((GridData) m_link.getLayoutData()).exclude = !m_useLinkAsLabel;
      m_link.setVisible(m_useLinkAsLabel); // required as exclude does not hide the control properly (bug)
      m_container.layout();
    }
  }

  public boolean isLinkAlwaysEnabled() {
    return m_linkAlwaysEnabled;
  }

  public void setLinkAlwaysEnabled(boolean linkAlwaysEnabled) {
    m_linkAlwaysEnabled = linkAlwaysEnabled;
    if (linkAlwaysEnabled) {
      if (isControlCreated()) {
        m_link.setEnabled(true);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isLinkAlwaysEnabled()) {
      super.setEnabled(enabled);
    }
    else {
      m_content.setEnabled(enabled);
      m_label.setEnabled(enabled);
      m_icon.setEnabled(enabled);
      m_statusIcon.setEnabled(enabled);
    }
  }

  public boolean isResetLinkVisible() {
    return m_resetLinkVisible;
  }

  public void setResetLinkVisible(boolean resetLinkVisible) {
    if (m_resetLinkVisible == resetLinkVisible) {
      return;
    }
    m_resetLinkVisible = resetLinkVisible;
    if (isControlCreated()) {
      ((GridData) m_resetLink.getLayoutData()).exclude = !m_resetLinkVisible;
      m_resetLink.setVisible(m_resetLinkVisible);
      m_container.layout();
    }
  }

  public void setMarkerType(MarkerType markerType) {
    m_markerType = markerType;
  }

  public MarkerType getMarkerType() {
    return m_markerType;
  }

  public void setMarkerGroupUUID(String markerGroupUUID) {
    m_markerGroupUUID = markerGroupUUID;
  }

  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public void addValueChangedListener(IPresenterValueChangedListener listener) {
    m_valueChangedListeners.add(listener);
  }

  public void removeValueChangedListener(IPresenterValueChangedListener listener) {
    m_valueChangedListeners.remove(listener);
  }

  public void setInput(final T value) {
    if (m_container == null) {
      createPresenter();
    }
    if (m_container.isDisposed()) {
      return;
    }

    clearInfo();
    try {
      m_value = value;
      setInputInternal(value);
    }
    finally {
      updateInfo();
    }
  }

  public void setInfo(int severity, String info) {
    m_customSeverity = severity;
    m_customInfo = info;
    updateInfo();
  }

  public void clearInfo() {
    clearInfo(true);
  }

  public void clearInfo(boolean updateUi) {
    m_customInfo = null;
    m_customSeverity = IMarker.SEVERITY_INFO;

    if (updateUi) {
      updateInfo();
    }
  }

  public void updateInfo() {
    if (isDisposed()) {
      return;
    }
    String info = null;
    int severity = IMarker.SEVERITY_INFO;

    List<SeverityEntry> statusList = new ArrayList<>();
    statusList.add(new SeverityEntry(NumberUtility.nvl(m_customSeverity, IMarker.SEVERITY_INFO), m_customInfo));

    // marker status
    if (m_markerType != null && m_bundle != null) {
      IMarker[] markers = MarkerUtility.getMarkers(m_bundle, m_markerType, m_markerGroupUUID);

      for (IMarker marker : markers) {
        statusList.add(new SeverityEntry(marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO), marker.getAttribute(IMarker.MESSAGE, null)));
      }
    }

    try {
      updateSeverity(statusList);
    }
    catch (CoreException e) {
      JaxWsSdk.logError(e);
    }

    for (SeverityEntry severityEntry : statusList) {
      severity = Math.max(severity, severityEntry.getSeverity());
      if (StringUtility.hasText(severityEntry.getMessage())) {
        info = StringUtility.join("\n" + info, severityEntry.getMessage());
      }
    }

    // visualize status
    Image image;
    switch (severity) {
      case IMarker.SEVERITY_WARNING:
        image = ScoutSdkUi.getImage(ScoutSdkUi.StatusWarning);
        break;
      case IMarker.SEVERITY_ERROR:
        image = ScoutSdkUi.getImage(ScoutSdkUi.StatusError);
        break;
      default:
        image = ScoutSdkUi.getImage(ScoutSdkUi.StatusInfo);
        break;
    }

    if (!StringUtility.hasText(info)) {
      info = null;
    }

    GridData gridDataIcon = (GridData) m_statusIcon.getLayoutData();

    if (severity == IMarker.SEVERITY_INFO && info == null) {
      // clear info
      if (!gridDataIcon.exclude) {
        gridDataIcon.exclude = true;
        m_statusIcon.setVisible(false);
        m_container.layout();
      }
    }
    else {
      m_statusIcon.setImage(image);
      m_statusIcon.setToolTipText(StringUtility.nvl(info, ""));

      if (gridDataIcon.exclude) {
        gridDataIcon.exclude = false;
        m_statusIcon.setVisible(true);
        m_container.layout();
      }
    }
  }

  public T getValue() {
    return m_value;
  }

  public boolean isAcceptNullValue() {
    return m_acceptNullValue;
  }

  public void setAcceptNullValue(boolean acceptNullValue) {
    m_acceptNullValue = acceptNullValue;
  }

  public boolean isBoldLabelText() {
    return m_boldLabelText;
  }

  public void setBoldLabelText(boolean boldLabelText) {
    if (m_boldLabelText == boldLabelText) {
      return;
    }
    m_boldLabelText = boldLabelText;
    if (isControlCreated()) {
      m_link.setFont(getFont(JFaceResources.DIALOG_FONT, boldLabelText));
      m_label.setFont(getFont(JFaceResources.DIALOG_FONT, boldLabelText));
    }
  }

  /**
   * Sets the value in the presenter's model and notifies attached listeners
   *
   * @param newValue
   */
  protected void setValueFromUI(T newValue) {
    setValueFromUI(newValue, false);
  }

  /**
   * Sets the value in the presenter's model and notifies attached listeners
   *
   * @param newValue
   * @param force
   *          to force set the value without equality check of new and old value
   */
  protected void setValueFromUI(T newValue, boolean force) {
    if (!isStateChanging() && (force || !CompareUtility.equals(getValue(), newValue)) && (newValue != null || isAcceptNullValue())) {
      m_value = newValue;
      for (IPresenterValueChangedListener listener : m_valueChangedListeners.toArray(new IPresenterValueChangedListener[m_valueChangedListeners.size()])) {
        try {
          listener.propertyChanged(m_presenterId, newValue);
        }
        catch (Exception t) {
          JaxWsSdk.logError("Error in presenter value changed listener.", t);
        }
      }
    }
    clearInfo();
  }

  /**
   * is called to update the presenter's UI representation
   *
   * @param input
   * @return
   */
  protected abstract void setInputInternal(T input);

  protected abstract Control createContent(Composite parent);

  protected boolean isControlCreated() {
    return m_container != null && !m_container.isDisposed();
  }

  protected boolean isStateChanging() {
    return m_stateChanging > 0;
  }

  public void setStateChanging(boolean b) {
    if (b) {
      m_stateChanging++;
    }
    else {
      m_stateChanging--;
    }
  }

  protected void execLinkAction() throws CoreException {
  }

  protected void execResetAction() throws CoreException {
  }

  protected void updateSeverity(List<SeverityEntry> statusList) throws CoreException {
  }

  public static class SeverityEntry {
    private int m_severity;
    private String m_message;

    public SeverityEntry(int severity, String message) {
      m_severity = severity;
      m_message = message;
    }

    public int getSeverity() {
      return m_severity;
    }

    public void setSeverity(int severity) {
      m_severity = severity;
    }

    public String getMessage() {
      return m_message;
    }

    public void setMessage(String message) {
      m_message = message;
    }
  }
}
