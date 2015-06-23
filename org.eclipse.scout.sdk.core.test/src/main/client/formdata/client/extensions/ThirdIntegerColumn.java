/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.client.extensions;

import org.eclipse.scout.commons.annotations.Data;
import org.eclipse.scout.commons.annotations.Extends;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;

import formdata.client.ui.desktop.outline.pages.ExtendedEmptyTablePage;
import formdata.shared.extension.ThirdIntegerColumnData;

@Order(3000.0)
@Data(ThirdIntegerColumnData.class)
@Extends(ExtendedEmptyTablePage.class)
public class ThirdIntegerColumn extends AbstractIntegerColumn {

}
