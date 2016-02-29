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
package org.eclipse.scout.sdk.core.sourcebuilder;

import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * <h3>{@link SortedMemberKeyFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 10.04.2013
 */
public final class SortedMemberKeyFactory {

  public static final int FIELD_SERIAL_VERSION_UID = 10;
  /**
   * public static fields
   */
  public static final int FIELD_CONSTANT = 20;
  public static final int FIELD_CONSTANT_TABLE_COLUMN_ID = 22;

  /**
   * private fields
   */
  public static final int FIELD_MEMBER = 30;

  public static final int METHOD_CONSTRUCTOR = 10;
  public static final int METHOD_PROPERTY_ACCESS = 20;
  public static final int METHOD_GET_CONFIGURED = 30;
  public static final int METHOD_EXEC = 40;
  public static final int METHOD_START_FORM = 50;
  public static final int METHOD_FIELD_GETTER = 60;
  public static final int METHOD_FORM_DATA_COLUMN_ACCESS = 70;
  public static final int METHOD_ANY = Integer.MAX_VALUE;

  public static final int TYPE_FORM_FIELD = 10;
  public static final int TYPE_FORM_FIELD_BUTTONS = 20;
  public static final int TYPE_FORM_HANDLER = 30;
  public static final int TYPE_TABLE = 40;
  public static final int TYPE_TABLE_COLUMN = 50;
  public static final int TYPE_TREE = 60;
  public static final int TYPE_ACTIVITYMAP = 70;

  public static final int TYPE_FORM_DATA_PROPERTY = 200;

  public static final int ORDER_INFINITE = Integer.MAX_VALUE;

  private SortedMemberKeyFactory() {
  }

  public static CompositeObject createFieldSerialVersionUidKey(IFieldSourceBuilder sourceBuilder) {
    return new CompositeObject(FIELD_SERIAL_VERSION_UID, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createFieldConstantKey(IFieldSourceBuilder sourceBuilder) {
    return new CompositeObject(FIELD_CONSTANT, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createFieldConstantTableColumnIdKey(IFieldSourceBuilder sourceBuilder, int value) {
    return new CompositeObject(FIELD_CONSTANT_TABLE_COLUMN_ID, value, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createFieldMemberKey(IFieldSourceBuilder sourceBuilder) {
    return new CompositeObject(FIELD_MEMBER, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodConstructorKey(IMethodSourceBuilder sourceBuilder) {
    return new CompositeObject(METHOD_CONSTRUCTOR, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodPropertyKey(IMethodSourceBuilder sourceBuilder) {
    String key2 = sourceBuilder.getElementName().replaceFirst("(get|set|is)", "");
    return new CompositeObject(METHOD_PROPERTY_ACCESS, key2, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodGetConfiguredKey(IMethodSourceBuilder sourceBuilder) {
    return new CompositeObject(METHOD_GET_CONFIGURED, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodExecKey(IMethodSourceBuilder sourceBuilder) {
    return new CompositeObject(METHOD_EXEC, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodStartFormKey(IMethodSourceBuilder sourceBuilder) {
    return new CompositeObject(METHOD_START_FORM, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodFormFieldGetterKey(IMethodSourceBuilder sourceBuilder) {
    return new CompositeObject(METHOD_FIELD_GETTER, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodFormDataColumnAccessKey(IMethodSourceBuilder sourceBuilder) {
    String key2 = sourceBuilder.getElementName().replaceFirst("(get|set|is)", "");
    return new CompositeObject(METHOD_FORM_DATA_COLUMN_ACCESS, key2, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createMethodAnyKey(IMethodSourceBuilder sourceBuilder) {
    return new CompositeObject(METHOD_ANY, sourceBuilder.getElementName(), sourceBuilder.getParameters().size(), sourceBuilder);
  }

  public static CompositeObject createTypeFormFieldKey(ITypeSourceBuilder sourceBuilder, double orderNr) {
    return new CompositeObject(TYPE_FORM_FIELD, orderNr, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createTypeFormHandlerKey(ITypeSourceBuilder sourceBuilder) {
    return new CompositeObject(TYPE_FORM_HANDLER, ORDER_INFINITE, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createTypeTableKey(ITypeSourceBuilder sourceBuilder) {
    return new CompositeObject(TYPE_TABLE, ORDER_INFINITE, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createTypeTableColumnKey(ITypeSourceBuilder sourceBuilder, double order) {
    return new CompositeObject(TYPE_TABLE_COLUMN, order, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createTypeTeeKey(ITypeSourceBuilder sourceBuilder) {
    return new CompositeObject(TYPE_TREE, ORDER_INFINITE, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createTypeActivityMapKey(ITypeSourceBuilder sourceBuilder) {
    return new CompositeObject(TYPE_ACTIVITYMAP, ORDER_INFINITE, sourceBuilder.getElementName(), sourceBuilder);
  }

  public static CompositeObject createTypeFormDataPropertyKey(ITypeSourceBuilder sourceBuilder) {
    return new CompositeObject(TYPE_FORM_DATA_PROPERTY, ORDER_INFINITE, sourceBuilder.getElementName(), sourceBuilder);
  }

}
