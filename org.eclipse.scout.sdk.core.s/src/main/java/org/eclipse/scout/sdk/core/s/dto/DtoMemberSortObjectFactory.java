/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.generator.type.SortedMemberEntry;

/**
 * Factory to create member sort order objects for DTOs
 */
public final class DtoMemberSortObjectFactory {

  private DtoMemberSortObjectFactory() {
  }

  /**
   * Creates a sort order object array for the FormData Property getter.<br>
   * (e.g. {@code public PersonNrProperty getPersonNrProperty()})
   *
   * @param propertyName
   *          The name of the property. E.g. "PersonNrProperty".
   * @return The sort order for the FormData Property getter
   */
  public static Object[] forMethodFormDataProperty(String propertyName) {
    return SortedMemberEntry.createDefaultMethodPos(propertyName, 20);
  }

  /**
   * Creates a sort order object array for the FormData legacy Property getter and setter.<br>
   * (e.g. {@code public Long getPersonNr()})
   *
   * @param propertyName
   *          The name of the property. E.g. "PersonNr".
   * @return The sort order for the FormData legacy Property getter and setter
   */
  public static Object[] forMethodFormDataPropertyLegacy(String propertyName) {
    return forMethodFormDataFormField(propertyName);
  }

  /**
   * Creates a sort order object array for the FormData FormField getter.<br>
   * (e.g. {@code public LastName getLastName()})
   *
   * @param name
   *          The name of the FormField. E.g. "LastName" without getter or setter prefix and without "Field" Suffix.
   * @return The sort order for the FormField getter
   */
  public static Object[] forMethodFormDataFormField(String name) {
    return SortedMemberEntry.createDefaultMethodPos(name, 10);
  }

  /**
   * Creates a sort order object array for the methods in a AbstractTableFieldBeanData.<br>
   * (e.g. {@code addRow()} or {@code getRowType()})
   * 
   * @param name
   *          The name of the method (e.g. "getRowType").
   * @return The sort order for the TableFieldBeanData method
   */
  public static Object[] forMethodTableData(String name) {
    return forMethodFormDataFormField(name);
  }

  /**
   * Creates a sort order object array for the PropertyData type.<br>
   * (e.g. {@code public static class PersonNrProperty extends AbstractPropertyData<Long>})
   *
   * @param propertyName
   *          The name of the property. E.g. "PersonNrProperty".
   * @return The sort order for the FormData Property type
   */
  public static Object[] forTypeFormDataProperty(String propertyName) {
    return forTypeFormDataFormField(propertyName);
  }

  /**
   * Creates a sort order object array for the FormFieldData type.<br>
   * (e.g. {@code public static class LastName extends AbstractValueFieldData<String>})
   *
   * @param name
   *          The name of the FormField. E.g. "LastName" without "Field" Suffix.
   * @return The sort order for the FormField type
   */
  public static Object[] forTypeFormDataFormField(String name) {
    return SortedMemberEntry.createDefaultTypePos(name);
  }

  /**
   * Creates a sort order object array for the AbstractTableRowData type within a AbstractTableFieldBeanData.
   * 
   * @param name
   *          The name of the TableRowData type
   * @return The sort order for the RowData type.
   */
  public static Object[] forTypeTableRowData(String name) {
    return forTypeFormDataFormField(name);
  }
}
