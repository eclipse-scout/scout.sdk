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
package a;

import org.eclipse.scout.commons.annotations.FormData;

public class BeanProperties{

  private String m_propertyString;
  private String m_notAnnotatedGetter;
  private String m_notAnnotatedSetter;
  private String m_missingGetter;
  private String m_missingSetter;
  private String noPrefix;
  private String o_otherPrefix;
  private String u_notDeclaredPrefix;
  private String m_preAndSuffixString_suffix;
  private String m_preAndUnknownSuffixString_unknownSuffix;

  private Long m_propertyLong;
  private short m_propertyShort;
  private int propertyInt;
  private boolean m_propetySimpleBoolean;
  private Boolean m_propertyObjectBoolean;

  @FormData
  public String getPropertyString(){
    return m_propertyString;
  }

  @FormData
  public void setPropertyString(String propertyString){
    m_propertyString=propertyString;
  }

  public String getNotAnnotatedGetter(){
    return m_notAnnotatedGetter;
  }

  @FormData
  public void setNotAnnotatedGetter(String notAnnotatedGetter){
    m_notAnnotatedGetter=notAnnotatedGetter;
  }

  @FormData
  public String getNotAnnotatedSetter(){
    return m_notAnnotatedSetter;
  }

  public void setNotAnnotatedSetter(String notAnnotatedSetter){
    m_notAnnotatedSetter=notAnnotatedSetter;
  }

  @FormData
  public String getNoPrefix(){
    return noPrefix;
  }

  @FormData
  public void setNoPrefix(String noPrefix){
    this.noPrefix=noPrefix;
  }

  @FormData
  public String getOtherPrefix(){
    return o_otherPrefix;
  }

  @FormData
  public void setOtherPrefix(String otherPrefix){
    o_otherPrefix=otherPrefix;
  }

  @FormData
  public Long getPropertyLong(){
    return m_propertyLong;
  }

  @FormData
  public void setPropertyLong(Long propertyLong){
    m_propertyLong=propertyLong;
  }

  @FormData
  public short getPropertyShort(){
    return m_propertyShort;
  }

  @FormData
  public void setPropertyShort(short propertyShort){
    m_propertyShort=propertyShort;
  }

  @FormData
  public int getPropertyInt(){
    return propertyInt;
  }

  @FormData
  public void setPropertyInt(int propertyInt){
    this.propertyInt=propertyInt;
  }

  @FormData
  public boolean isPropetySimpleBoolean(){
    return m_propetySimpleBoolean;
  }

  @FormData
  public void setPropetySimpleBoolean(boolean propetySimpleBoolean){
    m_propetySimpleBoolean=propetySimpleBoolean;
  }

  @FormData
  public Boolean getPropertyObjectBoolean(){
    return m_propertyObjectBoolean;
  }

  @FormData
  public void setPropertyObjectBoolean(Boolean propertyObjectBoolean){
    m_propertyObjectBoolean=propertyObjectBoolean;
  }

  @FormData
  public String getMissingSetter(){
    return m_missingSetter;
  }

  @FormData
  public void setMissingGetter(String missingGetter){
    m_missingGetter=missingGetter;
  }

  @FormData
  public String getNotDeclaredPrefix(){
    return u_notDeclaredPrefix;
  }

  @FormData
  public void setNotDeclaredPrefix(String uNotDeclaredPrefix){
    u_notDeclaredPrefix=uNotDeclaredPrefix;
  }

  @FormData
  public String getPreAndSuffixString(){
    return m_preAndSuffixString_suffix;
  }

  @FormData
  public void setPreAndSuffixString(String preAndSuffixString){
    m_preAndSuffixString_suffix=preAndSuffixString;
  }

  @FormData
  public String getPreAndUnknownSuffixString_unknownSuffix(){
    return m_preAndUnknownSuffixString_unknownSuffix;
  }

  @FormData
  public void setPreAndUnknownSuffixString_unknownSuffix(String preAndUnknownSuffixStringUnknownSuffix){
    m_preAndUnknownSuffixString_unknownSuffix=preAndUnknownSuffixStringUnknownSuffix;
  }
}
