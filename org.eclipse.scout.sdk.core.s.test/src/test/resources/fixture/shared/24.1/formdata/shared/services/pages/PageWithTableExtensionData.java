/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.pages;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.annotation.Generated;

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

  public void setBigDecimalTest(BigDecimal newBigDecimalTest) {
    m_bigDecimalTest = newBigDecimalTest;
  }
}
