/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
