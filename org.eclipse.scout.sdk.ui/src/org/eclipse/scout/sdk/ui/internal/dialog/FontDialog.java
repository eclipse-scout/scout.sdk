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
package org.eclipse.scout.sdk.ui.internal.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>{@link FontDialog}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 17.11.2010
 */
public class FontDialog extends TitleAreaDialog {

  private SystemFonts m_fonts;
  private FontSpec m_fontSpec;
  private FilteredTable m_fontNameTable;
  private FilteredTable m_fontStyleTable;
  private Button m_defaultFontStyleButton;
  private Button m_defaultFontNameButton;
  private Label m_preview;
  private Font m_defaultFont;
  private Font m_currentPreviewFont;
  private Composite m_rootArea;
  private Button m_defaultFontSizeButton;
  private Text m_sizeField;
  private final String m_title;
  private final String m_message;

  public FontDialog(Shell parentShell, FontSpec initialSpec) {
    this(parentShell, initialSpec, Texts.get("Font"), Texts.get("FontDialogHelpMsg"));
  }

  /**
   * @param parentShell
   */
  public FontDialog(Shell parentShell, FontSpec initialSpec, String title, String message) {
    super(parentShell);
    m_title = title;
    m_message = message;
    setShellStyle(getShellStyle() | SWT.RESIZE);
    if (initialSpec == null) {
      m_fontSpec = new FontSpec();
    }
    else {
      m_fontSpec = new FontSpec(initialSpec);
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    if (!StringUtility.isNullOrEmpty(m_title)) {
      setTitle(m_title);
    }
    if (!StringUtility.isNullOrEmpty(m_message)) {
      setMessage(m_message);
    }
    m_fonts = new SystemFonts(getShell().getDisplay().getFontList(null, true));

    m_rootArea = new Composite(parent, SWT.NONE);
    Control nameArea = createFontNameArea(m_rootArea);
    Control styleArea = createFontStyleArea(m_rootArea);
    Control sizeAndPreviewArea = createSizeAndPreview(m_rootArea);

    //layout
    m_rootArea.setLayout(new GridLayout(3, true));
    GridData nameAreaData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    nameAreaData.heightHint = 250;
    nameArea.setLayoutData(nameAreaData);
    GridData sizeAreaData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    sizeAreaData.heightHint = 250;
    styleArea.setLayoutData(sizeAreaData);
    GridData sizeAndPreviewAreaData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    sizeAndPreviewAreaData.heightHint = 250;
    sizeAndPreviewArea.setLayoutData(sizeAndPreviewAreaData);
    if (parent.getLayout() instanceof GridLayout) {
      m_rootArea.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    updateFontSpec();
    return m_rootArea;
  }

  private Control createFontNameArea(Composite parent) {
    Group nameGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
    nameGroup.setText(Texts.get("Name"));
    m_defaultFontNameButton = new Button(nameGroup, SWT.CHECK);
    String fontName = m_fontSpec.getName();
    m_defaultFontNameButton.setSelection(fontName == null);
    m_defaultFontNameButton.setText(Texts.get("UseDefault"));
    m_defaultFontNameButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_fontNameTable.setEnabled(!((Button) e.widget).getSelection());
        updateFontSpec();
      }
    });
    m_fontNameTable = new FilteredTable(nameGroup, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_fontNameTable.setEnabled(fontName != null);
    P_FontNameProvider provider = new P_FontNameProvider(m_fontNameTable.getDisplay());
    m_fontNameTable.getViewer().setLabelProvider(provider);
    m_fontNameTable.getViewer().setContentProvider(provider);
    m_fontNameTable.getViewer().setInput(provider);
    if (fontName != null) {
      m_fontNameTable.getViewer().setSelection(new StructuredSelection(fontName));
      m_fontNameTable.getViewer().reveal(fontName);
    }
    m_fontNameTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        updateFontSpec();
      }
    });
    //layout
    nameGroup.setLayout(new GridLayout(1, true));
    m_defaultFontNameButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_fontNameTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL));
    return nameGroup;
  }

  private Control createFontStyleArea(Composite parent) {
    Group styleGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
    styleGroup.setText("Style");
    m_defaultFontStyleButton = new Button(styleGroup, SWT.CHECK);
    m_defaultFontStyleButton.setSelection(m_fontSpec.getStyle() == null);
    m_defaultFontStyleButton.setText(Texts.get("UseDefault"));
    m_defaultFontStyleButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_fontStyleTable.setEnabled(!((Button) e.widget).getSelection());
        updateFontSpec();
      }
    });
    m_fontStyleTable = new FilteredTable(styleGroup, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_fontStyleTable.setEnabled(m_fontSpec.getStyle() != null);
    P_FontSizeProvider provider = new P_FontSizeProvider();
    m_fontStyleTable.getViewer().setLabelProvider(provider);
    m_fontStyleTable.getViewer().setContentProvider(provider);
    m_fontStyleTable.getViewer().setInput(provider);
    if (m_fontSpec.getStyle() != null) {
      m_fontStyleTable.getViewer().setSelection(new StructuredSelection(m_fontSpec.getStyle()));
    }
    m_fontStyleTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        updateFontSpec();
      }
    });
    //layout
    styleGroup.setLayout(new GridLayout(1, true));
    m_defaultFontStyleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_fontStyleTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL));
    return styleGroup;
  }

  private Control createSizeAndPreview(Composite parent) {
    Composite area = new Composite(parent, SWT.None);
    Group sizeGroup = new Group(area, SWT.SHADOW_ETCHED_IN);
    sizeGroup.setText("Size");
    m_defaultFontSizeButton = new Button(sizeGroup, SWT.CHECK);
    m_defaultFontSizeButton.setSelection(m_fontSpec.getHeight() == null);
    m_defaultFontSizeButton.setText(Texts.get("UseDefault"));
    m_defaultFontSizeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_sizeField.setEnabled(!((Button) e.widget).getSelection());
        updateFontSpec();
      }
    });
    m_sizeField = new Text(sizeGroup, SWT.BORDER | SWT.RIGHT);
    m_sizeField.setEnabled(m_fontSpec.getHeight() != null);
    if (m_fontSpec.getHeight() != null) {
      m_sizeField.setText("" + m_fontSpec.getHeight());
    }
    m_sizeField.addVerifyListener(new VerifyListener() {
      @Override
      public void verifyText(VerifyEvent e) {
        switch (e.keyCode) {
          case SWT.DEL:
          case SWT.BS:
            return;
        }
        if (m_sizeField.getText().length() > 2) {
          e.doit = false;
        }
        String regexp = "[0-9]";
        char c = e.character;
        if (!("" + c).matches(regexp)) {
          e.doit = false;
        }
      }
    });
    m_sizeField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        updateFontSpec();
      }
    });

    Group previewGroup = new Group(area, SWT.SHADOW_ETCHED_IN);
    previewGroup.setText(Texts.get("Preview"));
    m_preview = new Label(previewGroup, SWT.WRAP);

    m_preview.setText("ABCDEFGHIJklmnopqrs 123456789");
    m_defaultFont = m_preview.getFont();
    m_preview.setBackground(m_preview.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    //layout
    GridLayout areaLayout = new GridLayout(1, true);
    areaLayout.horizontalSpacing = 0;
    areaLayout.marginHeight = 0;
    areaLayout.marginWidth = 0;
    areaLayout.verticalSpacing = 0;
    area.setLayout(areaLayout);
    sizeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    sizeGroup.setLayout(new GridLayout(1, true));
    m_defaultFontSizeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    previewGroup.setLayout(new GridLayout(1, true));
    GridData previewFieldData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);

    m_preview.setLayoutData(previewFieldData);

    return area;
  }

  private void updateFontSpec() {
    // fontname
    if (m_defaultFontNameButton.getSelection()) {
      m_fontSpec.setName(null);
    }
    else {
      IStructuredSelection selection = (IStructuredSelection) m_fontNameTable.getViewer().getSelection();
      String fontName = null;
      if (!selection.isEmpty()) {
        fontName = (String) selection.getFirstElement();
      }
      m_fontSpec.setName(fontName);
    }
    // style
    if (m_defaultFontStyleButton.getSelection()) {
      m_fontSpec.setStyle(null);
    }
    else {
      IStructuredSelection selection = (IStructuredSelection) m_fontStyleTable.getViewer().getSelection();
      Integer style = null;
      if (!selection.isEmpty()) {
        style = (Integer) selection.getFirstElement();
      }
      m_fontSpec.setStyle(style);
    }
    // size
    if (m_defaultFontSizeButton.getSelection()) {
      m_fontSpec.setHeight(null);
    }
    else {
      String text = m_sizeField.getText();
      if (!StringUtility.isNullOrEmpty(text)) {
        try {
          int size = Integer.parseInt(text);
          m_fontSpec.setHeight(size);
        }
        catch (Exception e) {
          ScoutSdkUi.logWarning("could not parse '" + text + "' to integer.", e);
        }
      }
    }

    // update viewer
    FontData fd = m_defaultFont.getFontData()[0];
    if (!StringUtility.isNullOrEmpty(m_fontSpec.getName())) {
      fd.setName(m_fontSpec.getName());
    }
    if (m_fontSpec.getHeight() != null) {
      fd.setHeight(m_fontSpec.getHeight());
    }
    if (m_fontSpec.getStyle() != null) {
      fd.setStyle(m_fontSpec.getStyle());
    }
    if (m_currentPreviewFont != null) {
      m_currentPreviewFont.dispose();
    }
    m_currentPreviewFont = new Font(m_preview.getDisplay(), fd);
    m_preview.setFont(m_currentPreviewFont);
    m_rootArea.layout(true);
  }

  public FontSpec openDialog() {
    if (open() == OK) {
      return m_fontSpec;
    }
    return null;
  }

  @Override
  public int open() {
    return super.open();
  }

  private class P_FontNameProvider implements ITableLabelProvider, IStructuredContentProvider {

    public P_FontNameProvider(Device device) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_fonts.getAllFontNames();
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      return (String) element;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

  } // end class P_FontNameProvider

  private class P_FontSizeProvider implements ITableLabelProvider, IStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      return new Integer[]{new Integer(SWT.BOLD), new Integer(SWT.ITALIC), new Integer(SWT.BOLD | SWT.ITALIC), new Integer(SWT.NORMAL)};
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      Integer style = (Integer) element;
      StringBuilder styleBuilder = new StringBuilder();
      if (style != null) {
        if ((style & SWT.BOLD) != 0) {
          styleBuilder.append("BOLD");
        }
        if ((style & SWT.ITALIC) != 0) {
          if (styleBuilder.length() > 0) {
            styleBuilder.append(" ");
          }
          styleBuilder.append("ITALIC");
        }
        if (styleBuilder.length() == 0) {
          styleBuilder.append("NORMAL");
        }
      }
      return styleBuilder.toString();
    }

    @Override
    public void dispose() {
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }
  }
}
