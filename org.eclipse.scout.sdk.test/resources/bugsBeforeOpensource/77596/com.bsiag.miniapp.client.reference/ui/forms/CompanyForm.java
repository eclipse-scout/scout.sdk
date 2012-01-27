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
package com.bsiag.miniapp.client.ui.forms;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.holders.LongHolder;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.commons.holders.StringHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.platform.OfflineState;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRowMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.AbstractImageField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.jdbc.ILegacySqlQueryService;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.service.SERVICES;

import com.bsiag.miniapp.client.ClientSession;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.AlreadyExists_Button;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.CancelButton;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.OkButton;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox.AddressTableField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox.CompanyTypeField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox.LanguageField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox.RatingField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox.RegionField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.DetailBox.SectorField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox.ActiveField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox.CompanyNoField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox.CompanyShortNameField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox.LogoField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox.MainAccountManagerField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.GroupBox.NameField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.AdditionalInformationBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.ChangesBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.DocumentsBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.FinancialFiguresBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.NoteBox;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.AdditionalInformationBox.AdditionalInformationTableField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.ChangesBox.ChangesTableField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.DocumentsBox.DocumentTableField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.FinancialFiguresBox.FiguresTableField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.FinancialFiguresBox.FiguresYtDTableField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.FinancialFiguresBox.InvoicesDueField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.FinancialFiguresBox.OpenBillsField;
import com.bsiag.miniapp.client.ui.forms.CompanyForm.MainBox.TabBox.NoteBox.NotesField;
import com.bsiag.miniapp.shared.Icons;
import com.bsiag.miniapp.shared.Texts;

@FormData
public class CompanyForm extends AbstractForm{
  private static Logger logger=LoggerFactory.getLogger(CompanyForm.class);
  public static final String PROP_TEST = "abc";

  private Long m_companyNr;
  private boolean m_internal=false;
  private Long m_smartPriv=null;
  private boolean m_dataChangedTriggerDisabled;

  @FormData
  public Long getCompanyNr(){
    return m_companyNr;
  }

  @FormData
  public void setCompanyNr(Long value){
    m_companyNr=value;
  }


  public void aSimpleDummyMethod(){

  }
  @FormData
  public boolean isInternal(){
    return m_internal;
  }

  @FormData
  public void setInternal(boolean value){
    m_internal=value;
  }


  public boolean isDataChangedTriggerDisabled(){
    return m_dataChangedTriggerDisabled;
  }

  public void setDataChangedTriggerDisabled(boolean dataChangedTriggerDisabled){
    m_dataChangedTriggerDisabled=dataChangedTriggerDisabled;
  }

  public CompanyForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle(){
    return "Company";
  }

  @Override
  protected String getConfiguredDoc(){
    return "This is the CompanyForm form";
  }

  public MainBox getMainBox(){
    return (MainBox)getRootGroupBox();
  }

  public GroupBox getGroupBox(){
    return getFieldByClass(GroupBox.class);
  }

  public CompanyShortNameField getCompanyShortNameField(){
    return getFieldByClass(CompanyShortNameField.class);
  }

  public NameField getNameField(){
    return getFieldByClass(NameField.class);
  }

  public CompanyNoField getCompanyNoField(){
    return getFieldByClass(CompanyNoField.class);
  }

  public MainAccountManagerField getMainAccountManagerField(){
    return getFieldByClass(MainAccountManagerField.class);
  }

  public ActiveField getActiveField(){
    return getFieldByClass(ActiveField.class);
  }

  public DetailBox getDetailBox(){
    return getFieldByClass(DetailBox.class);
  }

  public RegionField getRegionField(){
    return getFieldByClass(RegionField.class);
  }

  public SectorField getSectorField(){
    return getFieldByClass(SectorField.class);
  }

  public RatingField getRatingField(){
    return getFieldByClass(RatingField.class);
  }

  public LogoField getLogoField(){
    return getFieldByClass(LogoField.class);
  }

  public LanguageField getLanguageField(){
    return getFieldByClass(LanguageField.class);
  }

  public CompanyTypeField getCompanyTypeField(){
    return getFieldByClass(CompanyTypeField.class);
  }

  public AddressTableField getAddressTableField(){
    return getFieldByClass(AddressTableField.class);
  }

  public TabBox getTabBox(){
    return getFieldByClass(TabBox.class);
  }

  public NoteBox getNoteBox(){
    return getFieldByClass(NoteBox.class);
  }

  public NotesField getNotesField(){
    return getFieldByClass(NotesField.class);
  }

  public FinancialFiguresBox getFinancialFiguresBox(){
    return getFieldByClass(FinancialFiguresBox.class);
  }

  public OpenBillsField getOpenBillsField(){
    return getFieldByClass(OpenBillsField.class);
  }

  public FiguresYtDTableField getFiguresYtDTableField(){
    return getFieldByClass(FiguresYtDTableField.class);
  }

  public InvoicesDueField getInvoicesDueField(){
    return getFieldByClass(InvoicesDueField.class);
  }

  public FiguresTableField getFiguresTableField(){
    return getFieldByClass(FiguresTableField.class);
  }

  public AdditionalInformationBox getAdditionalInformationBox(){
    return getFieldByClass(AdditionalInformationBox.class);
  }

  public AdditionalInformationTableField getAdditionalInformationTableField(){
    return getFieldByClass(AdditionalInformationTableField.class);
  }

  public DocumentsBox getDocumentsBox(){
    return getFieldByClass(DocumentsBox.class);
  }

  public DocumentTableField getDocumentTableField(){
    return getFieldByClass(DocumentTableField.class);
  }

  public ChangesBox getChangesBox(){
    return getFieldByClass(ChangesBox.class);
  }

  public ChangesTableField getChangesTableField(){
    return getFieldByClass(ChangesTableField.class);
  }

  public AlreadyExists_Button getAlreadyExists_Button(){
    return getFieldByClass(AlreadyExists_Button.class);
  }

  public OkButton getOkButton(){
    return getFieldByClass(OkButton.class);
  }

  public CancelButton getCancelButton(){
    return getFieldByClass(CancelButton.class);
  }

  public void startModify() throws ProcessingException{
    startInternal(new ModifyHandler());
  }

  public void startNew() throws ProcessingException{
    startInternal(new NewHandler());
  }

  @Order(10.0f)
  public class MainBox extends AbstractGroupBox{

    @Order(10.0f)
    public class GroupBox extends AbstractGroupBox{

      @Order(10.0f)
      public class CompanyShortNameField extends AbstractStringField{

        @Override
        protected int getConfiguredMaxLength(){
          return 60;
        }

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("CompanyShortName");
        }

        @Override
        protected boolean getConfiguredMandatory(){
          return true;
        }

        @Override
        protected boolean getConfiguredFormatUpper(){
          return true;
        }
      }

      @Order(20.0f)
      public class NameField extends AbstractStringField{

        @Override
        protected int getConfiguredMaxLength(){
          return 250;
        }

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Name");
        }

        @Override
        protected boolean getConfiguredMandatory(){
          return true;
        }

        @Override
        protected int getConfiguredGridH(){
          return 2;
        }

        @Override
        protected double getConfiguredGridWeightY(){
          return 0;
        }

        @Override
        protected boolean getConfiguredMultilineText(){
          return true;
        }

        @Override
        protected boolean getConfiguredWrapText(){
          return true;
        }
      }

      @Order(30.0f)
      public class CompanyNoField extends AbstractStringField{

        @Override
        protected int getConfiguredMaxLength(){
          return 60;
        }

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("CompanyNo");
        }
      }

      @Order(40.0f)
      public class MainAccountManagerField extends AbstractSmartField<Long>{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("MainAccountManager");
        }

        @Override
        protected boolean getConfiguredMandatory(){
          return true;
        }
      }

      @Order(50.0f)
      public class ActiveField extends AbstractBooleanField{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Active");
        }
      }

      @Order(60.0f)
      public class LogoField extends AbstractImageField{

        @Override
        protected int getConfiguredGridH(){
          return 6;
        }

        @Override
        protected double getConfiguredGridWeightY(){
          return 0;
        }

        @Override
        protected int getConfiguredHorizontalAlignment(){
          return 1;
        }

        @Override
        protected int getConfiguredVerticalAlignment(){
          return -1;
        }

        @Override
        protected boolean getConfiguredLabelVisible(){
          return false;
        }


        @Override
        protected boolean getConfiguredAutoFit(){
          return true;
        }

        @Override
        protected boolean getConfiguredFocusVisible(){
          return false;
        }

        @Override
        protected int getConfiguredDropType(){
          return IDNDSupport.TYPE_FILE_TRANSFER;
        }

        @Override
        protected int getConfiguredDragType(){
          return IDNDSupport.TYPE_FILE_TRANSFER;
        }

        @Override
        protected TransferObject execDragRequest() throws ProcessingException{
          DocumentTableField.Table table=getDocumentTableField().getTable();
          ITableRow row=findImageTableRow();
          if(row != null){
            byte[] content=(byte[])getImage();
            String imageId=table.getNameColumn().getValue(row);
            if(content != null && imageId != null){
              return new FileListTransferObject(IOUtility.createTempFile(imageId, content));
            }
          }
          return null;
        }

        @Override
        protected void execDropRequest(TransferObject t) throws ProcessingException{
          if(t.isFileList()){
            FileListTransferObject fileTransferable=(FileListTransferObject)t;
            if(fileTransferable.getFiles() != null && fileTransferable.getFiles().length == 1){
              // getfirst
              File f=fileTransferable.getFiles()[0];
              if(f != null && f.exists() && f.isFile()){
                if(Pattern.matches("[^\\.]*\\.(gif|png|jpg|jpeg)", f.getName().toLowerCase())){
                  byte[] content=null;
                  try{
                    content=IOUtility.getContent(new FileInputStream(f), true);
                  }
                  catch(Exception e){
                    // nop
                    return;
                  }
                  insertImage(content, f.getAbsolutePath());
                }
              }
            }
          }
          else if(t.isImage()){
            ImageTransferObject imageTransferable=(ImageTransferObject)t;
            if(imageTransferable.getImage() instanceof BufferedImage){
              insertImage((BufferedImage)imageTransferable.getImage());
            }
          }
        }

        private ITableRow findImageTableRow() throws ProcessingException{
          DocumentTableField.Table table=getDocumentTableField().getTable();
          for(ITableRow row : table.getRows()){
            String text=table.getDescriptionColumn().getValue(row);
            if(text != null && text.toLowerCase().matches("logo")){
              return row;
            }
          }
          return null;
        }

        public void updateImageLater(){
          new ClientSyncJob("load image for company", ClientSession.get()){
            private int laterCount=2;

            @Override
            protected void runVoid(IProgressMonitor monitor) throws Throwable{
              if(laterCount > 0){
                laterCount--;
                this.schedule();
              }
              else{
              }
            }
          }.schedule();
        }



        public void insertImage(BufferedImage image) throws ProcessingException{
          ByteArrayOutputStream buffer=new ByteArrayOutputStream(25000);
          ImageWriter writer=ImageIO.getImageWritersByFormatName("jpeg").next();
          try{
            writer.setOutput(ImageIO.createImageOutputStream(buffer));
            writer.write(image);
          }
          catch(IOException e){
            throw new ProcessingException("unable to drop image", e);
          }
          finally{
            writer.dispose();
          }
          byte[] data=buffer.toByteArray();
          try{
            buffer.close();
          }
          catch(Exception e){
          }
          buffer=null;
          String name=StringUtility.concatenateTokens(getCompanyShortNameField().getValue(), "_", "logo", ".jpeg").replace(" ", "_");
          insertImage(data, name);
        }

        public void insertImage(byte[] content, String path) throws ProcessingException{
          try{
            BufferedImage img=ImageIO.read(new ByteArrayInputStream(content));
            int w=img.getWidth();
            int h=img.getHeight();
            if(w >= 16 && h > 100){
              float fl=100f / h;
              w=(int)(fl * w);
              h=(int)(fl * h);
              BufferedImage scaledImg=new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
              Graphics2D gScaledImg=scaledImg.createGraphics();
              gScaledImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
              gScaledImg.drawImage(img, 0, 0, w, h, null);
              gScaledImg.dispose();
              ByteArrayOutputStream out=new ByteArrayOutputStream();
              ImageIO.write(scaledImg, "png", out);
              content=out.toByteArray();
            }
          }
          catch(Exception e){
            // nop
            return;
          }
          setImage(content);
          DocumentTableField.Table table=getDocumentTableField().getTable();
          try{
            table.setTableChanging(true);
            //
            ITableRow row=findImageTableRow();
            if(row != null){
              if(row.isStatusNonchanged()){
                row.setStatusUpdated();
              }
            }
            else{
              row=table.addRow(table.createRow(), true);
              table.getDescriptionColumn().setValue(row, "logo");
            }
            table.getNameColumn().setValue(row, path);
            table.getRegisteredOnColumn().setValue(row, new Date());
            table.getContentColumn().setValue(row, content);
            table.getChangedOnColumn().setValue(row, new Date());
          }
          finally{
            table.setTableChanging(false);
          }
        }

        @Order(10)
        public class AddMenu extends AbstractMenu{
          @Override
          protected String getConfiguredText(){
            return Texts.get("AddLogoMenu");
          }

          @Override
          protected void execPrepareAction() throws ProcessingException{
            setVisible(getLogoField().isEnabled());
          }

          @Override
          protected void execAction() throws ProcessingException{
            getTabBox().setSelectedTab(getDocumentsBox());
            FileChooser chooser=new FileChooser(null, new String[]{"gif", "png", "jpg", "jpeg"}, true);
            File[] files=chooser.startChooser();
            if(files.length > 0){
              byte[] content=null;
              try{
                content=IOUtility.getContent(new FileInputStream(files[0]), true);
              }
              catch(Exception e){
                // nop
                return;
              }
              insertImage(content, files[0].getAbsolutePath());
            }
          }
        }

        @Order(20)
        public class DeleteMenu extends AbstractMenu{
          @Override
          protected String getConfiguredText(){
            return Texts.get("Delete2");
          }

          @Override
          protected void execPrepareAction() throws ProcessingException{
            setVisible(getLogoField().isEnabled() && getLogoField().getImage() != null);
          }

          @Override
          protected void execAction() throws ProcessingException{
            setImage(null);
            ITableRow row=findImageTableRow();
            if(row != null){
              row.delete();
            }
          }
        }
      }
    }

    @Order(20.0f)
    public class DetailBox extends AbstractGroupBox{

      @Override
      protected String getConfiguredLabel(){
        return Texts.get("Detail");
      }

      @Order(10.0f)
      public class RegionField extends AbstractSmartField<Long>{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Region");
        }

        @Override
        protected boolean getConfiguredMandatory(){
          return true;
        }


      }

      @Order(20.0f)
      public class SectorField extends AbstractSmartField<Long>{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Sector");
        }

        @Override
        protected boolean getConfiguredMandatory(){
          return true;
        }



      }

      @Order(30.0f)
      public class RatingField extends AbstractSmartField<Long>{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Rating");
        }


      }

      @Order(40.0f)
      public class LanguageField extends AbstractSmartField<Long>{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Language");
        }


      }

      @Order(50.0f)
      public class CompanyTypeField extends AbstractListBox<Long>{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("CompanyType");
        }

        @Override
        protected boolean getConfiguredFilterSelectedRows(){
          return true;
        }



        @Override
        protected int getConfiguredGridH(){
          return 4;
        }
      }

      @Order(60.0f)
      public class AddressTableField extends AbstractTableField<AddressTableField.Table>{

        @Override
        protected boolean getConfiguredLabelVisible(){
          return false;
        }

        @Override
        protected int getConfiguredGridW(){
          return FULL_WIDTH;
        }

        @Override
        protected int getConfiguredGridH(){
          return 4;
        }

        @Order(10.0f)
        public class Table extends AbstractTable{

          @Override
          protected Class<? extends IMenu> getConfiguredDefaultMenu(){
            return EditMenu.class;
          }

          @Override
          protected boolean getConfiguredAutoResizeColumns(){
            return true;
          }

          public AddressTypeColumn getAddressTypeColumn(){
            return getColumnSet().getColumnByClass(AddressTypeColumn.class);
          }

          public AdditionalNameColumn getAdditionalNameColumn(){
            return getColumnSet().getColumnByClass(AdditionalNameColumn.class);
          }

          public StreetColumn getStreetColumn(){
            return getColumnSet().getColumnByClass(StreetColumn.class);
          }

          public POBoxColumn getPOBoxColumn(){
            return getColumnSet().getColumnByClass(POBoxColumn.class);
          }

          public CityColumn getCityColumn(){
            return getColumnSet().getColumnByClass(CityColumn.class);
          }

          public PhoneColumn getPhoneColumn(){
            return getColumnSet().getColumnByClass(PhoneColumn.class);
          }

          public FaxColumn getFaxColumn(){
            return getColumnSet().getColumnByClass(FaxColumn.class);
          }

          public EMailColumn getEMailColumn(){
            return getColumnSet().getColumnByClass(EMailColumn.class);
          }

          public WwwColumn getWwwColumn(){
            return getColumnSet().getColumnByClass(WwwColumn.class);
          }

          @Order(10.0f)
          public class AddressTypeColumn extends AbstractSmartColumn<Long>{

            @Override
            protected boolean getConfiguredPrimaryKey(){
              return true;
            }

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("AddressType");
            }

            @Override
            protected int getConfiguredWidth(){
              return 120;
            }
          }

          @Order(20.0f)
          public class AdditionalNameColumn extends AbstractStringColumn{

            @Override
            protected boolean getConfiguredDisplayable(){
              return false;
            }

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("AdditionalName");
            }
          }

          @Order(30.0f)
          public class StreetColumn extends AbstractStringColumn{

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("Street");
            }

            @Override
            protected int getConfiguredWidth(){
              return 150;
            }
          }

          @Order(40.0f)
          public class POBoxColumn extends AbstractStringColumn{

            @Override
            protected boolean getConfiguredDisplayable(){
              return false;
            }

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("POBox");
            }
          }

          @Order(50.0f)
          public class CityColumn extends AbstractSmartColumn<Long>{



            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("City");
            }

            @Override
            protected int getConfiguredWidth(){
              return 165;
            }
          }

          @Order(60.0f)
          public class PhoneColumn extends AbstractStringColumn{

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("Phone");
            }

            @Override
            protected int getConfiguredWidth(){
              return 120;
            }
          }

          @Order(70.0f)
          public class FaxColumn extends AbstractStringColumn{

            @Override
            protected boolean getConfiguredDisplayable(){
              return false;
            }

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("Fax");
            }
          }

          @Order(80.0f)
          public class EMailColumn extends AbstractStringColumn{

            @Override
            protected boolean getConfiguredDisplayable(){
              return false;
            }

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("EMail");
            }
          }

          @Order(90.0f)
          public class WwwColumn extends AbstractStringColumn{

            @Override
            protected String getConfiguredHeaderText(){
              return Texts.get("Www");
            }

            @Override
            protected int getConfiguredWidth(){
              return 140;
            }
          }

          @Order(100.0f)
          public class NewMenu extends AbstractMenu{

            @Override
            protected String getConfiguredText(){
              return Texts.get("NewMenu");
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction(){
              return false;
            }

          }

          @Order(110.0f)
          public class EditMenu extends AbstractMenu{

            @Override
            protected String getConfiguredText(){
              return Texts.get("Edit");
            }

            @Override
            protected boolean getConfiguredInheritAccessibility(){
              return false;
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction(){
              return true;
            }

            @Override
            protected boolean getConfiguredMultiSelectionAction(){
              return false;
            }


          }

          @Order(120.0f)
          public class DeleteMenu extends AbstractMenu{

            @Override
            protected String getConfiguredText(){
              return Texts.get("DeleteMenu");
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction(){
              return true;
            }

            @Override
            protected boolean getConfiguredMultiSelectionAction(){
              return true;
            }

            @Override
            protected void execPrepareAction() throws ProcessingException{

            }

            @Override
            protected void execAction() throws ProcessingException{
            }
          }

          @Order(130.0f)
          public class TwixTelMenu extends AbstractMenu{

            @Override
            protected String getConfiguredText(){
              return Texts.get("TwixTel");
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction(){
              return true;
            }

            @Override
            protected boolean getConfiguredMultiSelectionAction(){
              return false;
            }

            @Override
            protected void execInitAction(){
            }


          }

          @Order(140.0f)
          public class SeparatorMenu extends AbstractMenu{

            @Override
            protected boolean getConfiguredSeparator(){
              return true;
            }
          }

          @Order(150.0f)
          public class CTICallMenu extends AbstractMenu{

            @Override
            protected String getConfiguredText(){
              return Texts.get("Call2");
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction(){
              return true;
            }

            @Override
            protected boolean getConfiguredMultiSelectionAction(){
              return false;
            }

            @Override
            protected boolean getConfiguredInheritAccessibility(){
              return false;
            }

            @Override
            protected void execInitAction(){
            }

            @Override
            protected void execAction() throws ProcessingException{
            }
          }

          @Order(160f)
          public class ShowInGoogleMapsMenu extends AbstractMenu{

            @Override
            protected String getConfiguredText(){
              return Texts.get("ShowInGoogleMapsMenu");
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction(){
              return true;
            }

            @Override
            protected boolean getConfiguredMultiSelectionAction(){
              return false;
            }

            @Override
            protected boolean getConfiguredInheritAccessibility(){
              return false;
            }


          }

        }
      }
    }

    @Order(30.0f)
    public class TabBox extends AbstractTabBox{

      @Order(10.0f)
      public class NoteBox extends AbstractGroupBox{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Notes");
        }

        @Order(10.0f)
        public class NotesField extends AbstractStringField{

          @Override
          protected int getConfiguredMaxLength(){
            return 2000;
          }

          @Override
          protected boolean getConfiguredLabelVisible(){
            return false;
          }

          @Override
          protected boolean getConfiguredMultilineText(){
            return true;
          }

          @Override
          protected boolean getConfiguredWrapText(){
            return true;
          }

          @Override
          protected int getConfiguredGridW(){
            return 2;
          }

          @Override
          protected int getConfiguredGridH(){
            return 7;
          }
        }
      }

      @Order(20.0f)
      public class FinancialFiguresBox extends AbstractGroupBox{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("FinancialFigures");
        }

        @Order(10.0f)
        public class OpenBillsField extends AbstractDoubleField{

          @Override
          protected Double getConfiguredMinimumValue(){
            return -999999999.0;
          }

          @Override
          protected Double getConfiguredMaximumValue(){
            return 999999999.0;
          }

          @Override
          protected String getConfiguredLabel(){
            return Texts.get("OpenBills");
          }

          @Override
          protected boolean getConfiguredEnabled(){
            return false;
          }
        }

        @Order(20.0f)
        public class FiguresYtDTableField extends AbstractTableField<FiguresYtDTableField.Table>{

          @Override
          protected String getConfiguredLabel(){
            return Texts.get("Figures");
          }

          @Override
          protected int getConfiguredGridH(){
            return 6;
          }

          @Override
          protected double getConfiguredGridWeightY(){
            return 1.0;
          }

          @Override
          protected void execInitField() throws ProcessingException{
            setLabel("01.01.-" + DateUtility.format(new Date(), "dd.MM"));
          }

          @Override
          protected boolean execIsEmpty() throws ProcessingException{
            return getTable().getPotentialColumn().isEmpty() &&
                getTable().getBudgetColumn().isEmpty() &&
                getTable().getTurnoverColumn().isEmpty();
          }

          @Order(10.0f)
          public class Table extends AbstractTable{

            @Override
            protected boolean getConfiguredAutoResizeColumns(){
              return true;
            }

            public YearColumn getYearColumn(){
              return getColumnSet().getColumnByClass(YearColumn.class);
            }

            public PotentialColumn getPotentialColumn(){
              return getColumnSet().getColumnByClass(PotentialColumn.class);
            }

            public BudgetColumn getBudgetColumn(){
              return getColumnSet().getColumnByClass(BudgetColumn.class);
            }

            public TurnoverColumn getTurnoverColumn(){
              return getColumnSet().getColumnByClass(TurnoverColumn.class);
            }

            @Order(10.0f)
            public class YearColumn extends AbstractLongColumn{

              @Override
              protected boolean getConfiguredGroupingUsed(){
                return false;
              }

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Year");
              }

              @Override
              protected int getConfiguredWidth(){
                return 40;
              }
            }

            @Order(20.0f)
            public class PotentialColumn extends AbstractLongColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Potential");
              }

              @Override
              protected String getConfiguredHeaderTooltipText(){
                return Texts.get("ExclVAT");
              }

              @Override
              protected int getConfiguredWidth(){
                return 69;
              }
            }

            @Order(30.0f)
            public class BudgetColumn extends AbstractLongColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Budget");
              }

              @Override
              protected String getConfiguredHeaderTooltipText(){
                return Texts.get("ExclVAT");
              }

              @Override
              protected int getConfiguredWidth(){
                return 69;
              }
            }

            @Order(40.0f)
            public class TurnoverColumn extends AbstractLongColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Turnover");
              }

              @Override
              protected String getConfiguredHeaderTooltipText(){
                return Texts.get("InvoicesWrittenExclVAT");
              }

              @Override
              protected int getConfiguredWidth(){
                return 69;
              }
            }
          }
        }

        @Order(30.0f)
        public class InvoicesDueField extends AbstractDoubleField{

          @Override
          protected Double getConfiguredMinimumValue(){
            return -999999999.0;
          }

          @Override
          protected Double getConfiguredMaximumValue(){
            return 999999999.0;
          }

          @Override
          protected String getConfiguredLabel(){
            return Texts.get("InvoicesDue");
          }

          @Override
          protected boolean getConfiguredEnabled(){
            return false;
          }
        }

        @Order(40.0f)
        public class FiguresTableField extends AbstractTableField<FiguresTableField.Table>{

          @Override
          protected String getConfiguredLabel(){
            return Texts.get("Figures");
          }

          @Override
          protected int getConfiguredGridH(){
            return 6;
          }

          @Override
          protected double getConfiguredGridWeightY(){
            return 1.0;
          }

          @Override
          protected void execInitField() throws ProcessingException{
            setLabel("01.01.-31.12.");
          }

          @Override
          protected boolean execIsEmpty() throws ProcessingException{
            return getTable().getPotentialColumn().isEmpty() &&
                getTable().getBudgetColumn().isEmpty() &&
                getTable().getTurnoverColumn().isEmpty();
          }

          @Order(10.0f)
          public class Table extends AbstractTable{

            @Override
            protected Class<? extends IMenu> getConfiguredDefaultMenu(){
              return EditMenu.class;
            }

            @Override
            protected boolean getConfiguredAutoResizeColumns(){
              return true;
            }

            public YearColumn getYearColumn(){
              return getColumnSet().getColumnByClass(YearColumn.class);
            }

            public PotentialColumn getPotentialColumn(){
              return getColumnSet().getColumnByClass(PotentialColumn.class);
            }

            public BudgetColumn getBudgetColumn(){
              return getColumnSet().getColumnByClass(BudgetColumn.class);
            }

            public TurnoverColumn getTurnoverColumn(){
              return getColumnSet().getColumnByClass(TurnoverColumn.class);
            }

            @Order(10.0f)
            public class YearColumn extends AbstractLongColumn{

              @Override
              protected boolean getConfiguredGroupingUsed(){
                return false;
              }

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Year");
              }

              @Override
              protected int getConfiguredWidth(){
                return 40;
              }
            }

            @Order(20.0f)
            public class PotentialColumn extends AbstractLongColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Potential");
              }

              @Override
              protected String getConfiguredHeaderTooltipText(){
                return Texts.get("ExclVAT");
              }

              @Override
              protected int getConfiguredWidth(){
                return 69;
              }
            }

            @Order(30.0f)
            public class BudgetColumn extends AbstractLongColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Budget");
              }

              @Override
              protected String getConfiguredHeaderTooltipText(){
                return Texts.get("ExclVAT");
              }

              @Override
              protected int getConfiguredWidth(){
                return 69;
              }
            }

            @Order(40.0f)
            public class TurnoverColumn extends AbstractLongColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Turnover");
              }

              @Override
              protected String getConfiguredHeaderTooltipText(){
                return Texts.get("InvoicesWrittenExclVAT");
              }

              @Override
              protected int getConfiguredWidth(){
                return 69;
              }
            }

            @Order(50.0f)
            public class EditMenu extends AbstractMenu{

              @Override
              protected String getConfiguredText(){
                return Texts.get("Edit");
              }

              @Override
              protected boolean getConfiguredSingleSelectionAction(){
                return true;
              }

              @Override
              protected boolean getConfiguredMultiSelectionAction(){
                return false;
              }


            }
          }
        }
      }

      @Order(30.0f)
      public class AdditionalInformationBox extends AbstractGroupBox{
        @Override
        protected String getConfiguredLabel(){
          return Texts.get("AdditionalInformation");
        }

        @Order(10.0f)
        public class AdditionalInformationTableField extends AbstractTableField<AdditionalInformationTableField.Table>{

          @Override
          protected boolean getConfiguredLabelVisible(){
            return false;
          }

          @Override
          protected int getConfiguredGridW(){
            return 2;
          }

          @Override
          protected int getConfiguredGridH(){
            return 7;
          }

          @Override
          protected boolean execIsEmpty() throws ProcessingException{
            return getTable().getDisplayColumn().isEmpty();
          }

          @Order(10.0f)
          public class Table extends AbstractTable{

            @Override
            protected Class<? extends IMenu> getConfiguredDefaultMenu(){
              return EditMenu.class;
            }

            @Override
            protected boolean getConfiguredAutoResizeColumns(){
              return true;
            }

            public AdditionalInformationColumn getAdditionalInformationColumn(){
              return getColumnSet().getColumnByClass(AdditionalInformationColumn.class);
            }

            public DisplayColumn getDisplayColumn(){
              return getColumnSet().getColumnByClass(DisplayColumn.class);
            }

            public TextColumn getTextColumn(){
              return getColumnSet().getColumnByClass(TextColumn.class);
            }

            public NumberColumn getNumberColumn(){
              return getColumnSet().getColumnByClass(NumberColumn.class);
            }

            public DateColumn getDateColumn(){
              return getColumnSet().getColumnByClass(DateColumn.class);
            }

            @Order(20.0f)
            public class AdditionalInformationColumn extends AbstractSmartColumn<Long>{

              @Override
              protected boolean getConfiguredPrimaryKey(){
                return true;
              }



              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("AdditionalInformation");
              }

              @Override
              protected int getConfiguredWidth(){
                return 200;
              }
            }

            @Order(40.0f)
            public class DisplayColumn extends AbstractStringColumn{
              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Display");
              }

              @Override
              protected int getConfiguredWidth(){
                return 485;
              }
            }

            @Order(50.0f)
            public class TextColumn extends AbstractStringColumn{
              @Override
              protected boolean getConfiguredDisplayable(){
                return false;
              }

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Text");
              }
            }

            @Order(60.0f)
            public class NumberColumn extends AbstractDoubleColumn{
              @Override
              protected boolean getConfiguredDisplayable(){
                return false;
              }

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Number");
              }
            }

            @Order(70.0f)
            public class DateColumn extends AbstractDateColumn{
              @Override
              protected boolean getConfiguredDisplayable(){
                return false;
              }

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Date");
              }
            }

            @Order(100.0f)
            public class EditMenu extends AbstractMenu{
              @Override
              protected String getConfiguredText(){
                return Texts.get("Edit");
              }

              @Override
              protected boolean getConfiguredSingleSelectionAction(){
                return true;
              }

              @Override
              protected boolean getConfiguredMultiSelectionAction(){
                return false;
              }

              @Override
              protected boolean getConfiguredInheritAccessibility(){
                return false;
              }


            }
          }
        }
      }

      @Order(40.0f)
      public class DocumentsBox extends AbstractGroupBox{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Documents");
        }

        @Order(10.0f)
        @FormData("CREATE EXTERNAL")
        public class DocumentTableField extends AbstractDocumentTableField{
        }
      }

      @Order(50.0f)
      public class ChangesBox extends AbstractGroupBox{

        @Override
        protected String getConfiguredLabel(){
          return Texts.get("Changes");
        }

        @Order(10.0f)
        public class ChangesTableField extends AbstractTableField<ChangesTableField.Table>{

          @Override
          protected boolean getConfiguredLabelVisible(){
            return false;
          }

          @Override
          protected int getConfiguredGridW(){
            return 2;
          }

          @Override
          protected int getConfiguredGridH(){
            return 7;
          }

          @Order(10.0f)
          public class Table extends AbstractTable{

            @Override
            protected boolean getConfiguredAutoResizeColumns(){
              return true;
            }

            public TypeColumn getTypeColumn(){
              return getColumnSet().getColumnByClass(TypeColumn.class);
            }

            public PersonColumn getPersonColumn(){
              return getColumnSet().getColumnByClass(PersonColumn.class);
            }

            public DateColumn getDateColumn(){
              return getColumnSet().getColumnByClass(DateColumn.class);
            }

            public MainAccountManagerColumn getMainAccountManagerColumn(){
              return getColumnSet().getColumnByClass(MainAccountManagerColumn.class);
            }

            @Order(10.0f)
            public class TypeColumn extends AbstractSmartColumn<Long>{



              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Type");
              }

              @Override
              protected int getConfiguredWidth(){
                return 120;
              }
            }

            @Order(20.0f)
            public class PersonColumn extends AbstractStringColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Person");
              }

              @Override
              protected int getConfiguredWidth(){
                return 240;
              }
            }

            @Order(30.0f)
            public class DateColumn extends AbstractDateColumn{

              @Override
              protected boolean getConfiguredHasTime(){
                return true;
              }

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("Date");
              }

              @Override
              protected int getConfiguredWidth(){
                return 90;
              }
            }

            @Order(40.0f)
            public class MainAccountManagerColumn extends AbstractStringColumn{

              @Override
              protected String getConfiguredHeaderText(){
                return Texts.get("MainAccountManager");
              }

              @Override
              protected int getConfiguredWidth(){
                return 240;
              }
            }
          }
        }
      }
    }

    @Order(40.0f)
    public class AlreadyExists_Button extends AbstractButton{

      @Override
      protected int getConfiguredDisplayStyle(){
        return DISPLAY_STYLE_LINK;
      }



      @Override
      protected String getConfiguredLabel(){
        return Texts.get("AlreadyExists_");
      }

      @Override
      protected void execInitField() throws ProcessingException{
        setEnabled(OfflineState.isOnlineDefault());
      }
    }

    @Order(50.0f)
    public class OkButton extends AbstractOkButton{
    }

    @Order(60.0f)
    public class CancelButton extends AbstractCancelButton{
    }
  }

  @Order(20.0f)
  public class ModifyHandler extends AbstractFormHandler{

    @Override
    protected void execLoad() throws ProcessingException{

    }

    @Override
    protected void execStore() throws ProcessingException{
    }
  }

  @Order(30.0f)
  public class NewHandler extends AbstractFormHandler{

    @Override
    protected void execLoad() throws ProcessingException{
    }

    @Override
    protected void execStore() throws ProcessingException{
    }
  }
}
