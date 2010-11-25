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
package a.client.form.fields;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public abstract class AbstractTemplateStringField extends AbstractStringField{

  @Override
  protected String getConfiguredLabel(){
    return "Label";
  }

  @Override
  protected String execValidateValue(String rawValue) throws ProcessingException{
    if (rawValue != null && rawValue.length() < 4) {
      throw new VetoException("Illegal format");
    }
    return super.execValidateValue(rawValue);
  }
}
