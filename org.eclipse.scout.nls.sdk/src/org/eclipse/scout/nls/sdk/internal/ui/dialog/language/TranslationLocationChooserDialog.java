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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.language;

//package com.bsiag.nls.ui.dialog.language;
//
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.dialogs.TitleAreaDialog;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//
//import com.bsiag.nls.jdt.INlsFolder;
//import com.bsiag.nls.ui.dialog.ITranslationLocationChooserModel;
//import com.bsiag.nls.ui.smartfield.ISmartFieldListener;
//import com.bsiag.nls.ui.smartfield.SmartField;
//import com.bsiag.nls.ui.wizard.pages.internal.TranslationLocationSmartFieldModel;
//
//public class TranslationLocationChooserDialog extends TitleAreaDialog {
//
//  public static final int APPLY_ALL_ID = 129;
//  private String m_dialogTitle;
//  private String m_titleText = "";
//
//  private SmartField m_folderSelection;
//  private ITranslationLocationChooserModel m_model;
//
//  public TranslationLocationChooserDialog(Shell parentShell, String title, ITranslationLocationChooserModel model) {
//    super(parentShell);
//    setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
//            | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
//    m_dialogTitle = title;
//    m_model = model;
//  }
//
//  @Override
//  protected void configureShell(Shell newShell) {
//    super.configureShell(newShell);
//    newShell.setText(m_dialogTitle);
//  }
//
//  @Override
//  protected Control createContents(Composite parent) {
//    Control c =  super.createContents(parent);
//    super.setTitle(m_titleText);
//    return c;
//  }
//
//  @Override
//  protected Control createDialogArea(Composite parent) {
//    Composite rootArea = new Composite(parent,SWT.NONE);
//
//    m_folderSelection = new SmartField(rootArea, SWT.NONE);
//    m_folderSelection.setLabel("Location");
////    m_folderSelection.setValue(NlsPlugin.getPossibleTranslationFileLocations(m, folderPath))
//    m_folderSelection.setSmartFieldModel(new TranslationLocationSmartFieldModel(m_model.getProject(),m_model.getPath()));
//    m_folderSelection.addSmartFieldListener(new ISmartFieldListener(){
//      public void itemSelected(Object item) {
//        m_model.setTranslationLocation((INlsFolder)item);
//        getButton(Dialog.OK).setEnabled(true);
//        getButton(APPLY_ALL_ID).setEnabled(true);
//      }
//    });
//
//    Label l = new Label(rootArea,SWT.NONE);
//    l.setBackground(l.getDisplay().getSystemColor(SWT.COLOR_BLUE));
//    attachGridData(l);
//    //
//    attachGridData(rootArea);
//    rootArea.setLayout(new GridLayout(1, true));
//    attachGridData(m_folderSelection);
//    return rootArea;
//  }
//
//  @Override
//  protected void createButtonsForButtonBar(Composite parent) {
//    super.createButtonsForButtonBar(parent);
//    createButton(parent, APPLY_ALL_ID, "applay all",
//            true);
//    getButton(Dialog.OK).setEnabled(false);
//    getButton(APPLY_ALL_ID).setEnabled(false);
//  }
//  @Override
//  protected void buttonPressed(int buttonId) {
//   if(buttonId == APPLY_ALL_ID){
//     m_model.setRememberTranslationLocation(true);
//     close();
//   }
//    super.buttonPressed(buttonId);
//  }
//
//
//  @Override
//  public void setTitle(String newTitle) {
//    if(getContents() != null && !getContents().isDisposed()){
//      setTitle(newTitle);
//      return;
//    }
//    if(newTitle == null){
//     newTitle = "";
//    }
//    m_titleText = newTitle;
//  }
//
//
//  private void attachGridData(Control c){
//    GridData data = new GridData();
//    data.grabExcessHorizontalSpace = true;
//    data.horizontalAlignment = SWT.FILL;
//    c.setLayoutData(data);
//  }
//
//
// }
