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
public class EmptyPrimitivePropertyForm extends AbstractForm{

  private byte m_byte;
  private short m_short;
  private int m_int;
  private long m_long;
  private float m_float;
  private double m_double;
  private char m_char;
  private boolean m_boolean;
  private Object m_object;

  public EmptyPrimitivePropertyForm() throws ProcessingException{
    super();
  }

  @FormData
  public byte getByte(){
    return m_byte;
  }

  @FormData
  public void setByte(byte b){
    m_byte=b;
  }

  @FormData
  public short getShort(){
    return m_short;
  }

  @FormData
  public void setShort(short s){
    m_short=s;
  }

  @FormData
  public int getInt(){
    return m_int;
  }

  @FormData
  public void setInt(int i){
    m_int=i;
  }

  @FormData
  public long getLong(){
    return m_long;
  }

  @FormData
  public void setLong(long l){
    m_long=l;
  }

  @FormData
  public float getFloat(){
    return m_float;
  }

  @FormData
  public void setFloat(float f){
    m_float=f;
  }

  @FormData
  public double getDouble(){
    return m_double;
  }

  @FormData
  public void setDouble(double d){
    m_double=d;
  }

  @FormData
  public char getChar(){
    return m_char;
  }

  @FormData
  public void setChar(char c){
    m_char=c;
  }

  @FormData
  public boolean isBoolean(){
    return m_boolean;
  }

  @FormData
  public void setBoolean(boolean b){
    m_boolean=b;
  }

  @FormData
  public Object getObject(){
    return m_object;
  }

  @FormData
  public void setObject(Object object){
    m_object=object;
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public class MainBox extends AbstractGroupBox {

  }
}
