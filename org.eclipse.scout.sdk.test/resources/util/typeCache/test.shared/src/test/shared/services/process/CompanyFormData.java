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
package test.shared.services.process;

import java.lang.Long;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

/** 
  * This class is auto generated and will be refreshed automatically.
  */
public class CompanyFormData extends AbstractFormData{
  private static final long serialVersionUID=1L;

  private Long m_companyNr;

  public CompanyFormData(){}

  @FormData
  public Long getCompanyNr( ) {
    return m_companyNr;
  }

  @FormData
  public void setCompanyNr(Long companyNr) {
    m_companyNr = companyNr;
  }
}
