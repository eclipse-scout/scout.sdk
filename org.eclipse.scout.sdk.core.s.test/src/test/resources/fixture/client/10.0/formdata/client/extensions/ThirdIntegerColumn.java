/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.extensions;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.Extends;

import formdata.client.ui.desktop.outline.pages.ExtendedEmptyTablePage;
import formdata.shared.extension.ThirdIntegerColumnData;

@Order(3000.0)
@Data(ThirdIntegerColumnData.class)
@Extends(ExtendedEmptyTablePage.class)
public class ThirdIntegerColumn extends AbstractIntegerColumn {

}
