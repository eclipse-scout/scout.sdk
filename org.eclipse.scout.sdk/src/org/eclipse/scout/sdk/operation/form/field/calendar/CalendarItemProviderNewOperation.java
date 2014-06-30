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
package org.eclipse.scout.sdk.operation.form.field.calendar;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>CalendarItemProviderNewOperation</h3> ...
 */
public class CalendarItemProviderNewOperation extends OrderedInnerTypeNewOperation {

  public CalendarItemProviderNewOperation(String typeName, IType declaringType) {
    this(typeName, declaringType, false);
  }

  public CalendarItemProviderNewOperation(String typeName, IType declaringType, boolean formatSource) {
    super(typeName, declaringType, formatSource);

    setFlags(Flags.AccPublic);
    setOrderDefinitionType(TypeUtility.getType(IRuntimeClasses.ICalendarItemProvider));
  }
}
