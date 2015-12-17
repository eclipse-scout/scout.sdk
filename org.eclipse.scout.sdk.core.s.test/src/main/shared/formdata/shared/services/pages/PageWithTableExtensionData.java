/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.pages;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.extension.Extends;

import formdata.shared.services.pages.BaseTablePageData.BaseTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Extends(BaseTableRowData.class)
@Generated(value = "formdata.client.ui.desktop.outline.pages.PageWithTableExtension", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class PageWithTableExtensionData implements Serializable {

  private static final long serialVersionUID = 1L;
  public static final String bigDecimalTest = "bigDecimalTest";
  private BigDecimal m_bigDecimalTest;

  public BigDecimal getBigDecimalTest() {
    return m_bigDecimalTest;
  }

  public void setBigDecimalTest(BigDecimal bigDecimalTest) {
    m_bigDecimalTest = bigDecimalTest;
  }
}
