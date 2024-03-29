/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.extension;

import java.io.Serializable;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.extension.Extends;

import formdata.shared.services.pages.ExtendedEmptyTablePageData.ExtendedEmptyTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Extends(ExtendedEmptyTableRowData.class)
@Generated(value = "formdata.client.extensions.ThirdIntegerColumn", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ThirdIntegerColumnData implements Serializable {

  private static final long serialVersionUID = 1L;
  public static final String thirdInteger = "thirdInteger";
  private Integer m_thirdInteger;

  public Integer getThirdInteger() {
    return m_thirdInteger;
  }

  public void setThirdInteger(Integer newThirdInteger) {
    m_thirdInteger = newThirdInteger;
  }
}
