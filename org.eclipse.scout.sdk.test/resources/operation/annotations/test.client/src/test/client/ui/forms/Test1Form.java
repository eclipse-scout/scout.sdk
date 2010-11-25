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
package test.client.ui.forms;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.commons.annotations.Order;

public class Test1Form extends AbstractForm{

  private String m_stringMember;
  public Test1Form() throws ProcessingException{
    super();
  }

  /** Some java doc
   * @param stringMember
   */
  public void setStringMember(String stringMember){
    m_stringMember = stringMember;
  }

  public String getStringMember(){
    return m_stringMember;
  }

  public MainBox getMainBox(){
      return getFieldByClass(MainBox.class);
  }


  @Order(10.0)
  public class MainBox extends AbstractGroupBox{
  }
}
