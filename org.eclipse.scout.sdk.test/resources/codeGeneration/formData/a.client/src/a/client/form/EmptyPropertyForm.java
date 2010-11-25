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
package a.client.form;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

@FormData
public class EmptyPropertyForm extends AbstractForm{

  private String m_property;
  private Object[] m_oneDimension;
  private Object[][] m_twoDimensions;
  private Object[][][] m_threeDimensions;


  public EmptyPropertyForm() throws ProcessingException{
    super();
  }

  @FormData
  public String getProperty(){
    return m_property;
  }

  @FormData
  public void setProperty(String property){
    m_property=property;
  }

  @FormData
  public Object[] getOneDimension(){
    return m_oneDimension;
  }

  @FormData
  public void setOneDimension(Object[] oneDimension){
    m_oneDimension=oneDimension;
  }

  @FormData
  public Object[][] getTwoDimensions(){
    return m_twoDimensions;
  }

  @FormData
  public void setTwoDimensions(Object[][] twoDimensions){
    m_twoDimensions=twoDimensions;
  }

  @FormData
  public Object[][][] getThreeDimensions(){
    return m_threeDimensions;
  }

  @FormData
  public void setThreeDimensions(Object[][][] threeDimensions){
    m_threeDimensions=threeDimensions;
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public class MainBox extends AbstractGroupBox {

  }
}
