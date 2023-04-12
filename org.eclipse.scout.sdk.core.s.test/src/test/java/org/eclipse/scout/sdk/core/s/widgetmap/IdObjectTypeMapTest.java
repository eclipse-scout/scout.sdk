/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.testing.ExtendWithNodeModules;
import org.junit.jupiter.api.Test;

public class IdObjectTypeMapTest {

  @Test
  @ExtendWithNodeModules("SomeFormModel")
  public void testWidgetMap(INodeModule module) {
    var groupBoxObjectType = createObjectType("GroupBox", module);
    var stringFieldObjectType = createObjectType("StringField", module);
    var menuObjectType = createObjectType("Menu", module);

    var someFormModel = module.export("SomeFormModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();
    var widgetMap = WidgetMap.create("SomeFormModel", someFormModel, null).orElseThrow();

    //    {
    //      id: 'SomeForm',
    //      rootGroupBox: {
    //        id: 'MainBox',
    //        objectType: GroupBox,
    //        fields: [
    //          {
    //            id: 'LastNameField',
    //            objectType:StringField
    //          },
    //          {
    //            id: 'FirstNameField',
    //            objectType: StringField,
    //            errorStatus: {
    //              id: 'ErrorStatus',
    //              objectType: Status
    //            }
    //          },
    //          {
    //            objectType: NumberField,
    //            menus: [
    //              {
    //                id: 'SomeMenu',
    //                objectType: Menu
    //              }
    //            ]
    //          }
    //        ]
    //      }
    //    }

    assertEquals("SomeFormWidgetMap", widgetMap.name());

    assertEquals(4, widgetMap.elements().size());
    assertEquals(List.of("MainBox", "LastNameField", "FirstNameField", "SomeMenu"), widgetMap.elements().keySet().stream().toList());

    assertEquals(0, widgetMap.idObjectTypeMapReferences().size());

    var mainBox = widgetMap.elements().get("MainBox");
    assertNotNull(mainBox);
    assertObjectType(groupBoxObjectType, mainBox.objectType());

    var lastNameField = widgetMap.elements().get("LastNameField");
    assertNotNull(lastNameField);
    assertObjectType(stringFieldObjectType, lastNameField.objectType());

    var firstNameField = widgetMap.elements().get("FirstNameField");
    assertNotNull(firstNameField);
    assertObjectType(stringFieldObjectType, firstNameField.objectType());

    var someMenu = widgetMap.elements().get("SomeMenu");
    assertNotNull(someMenu);
    assertObjectType(menuObjectType, someMenu.objectType());
  }

  @Test
  @ExtendWithNodeModules("SomeFormWithTableFieldModel")
  public void testWidgetMapWithTable(INodeModule module) {
    var tableClass = findClassByObjectType("Table", module);
    var fancyTableClass = findClassByObjectType("FancyTable", module);

    var groupBoxObjectType = createObjectType("GroupBox", module);
    var stringFieldObjectType = createObjectType("StringField", module);
    var tableFieldObjectType = createObjectType("TableField", module);
    var menuObjectType = createObjectType("Menu", module);
    var columnObjectType = createObjectType("Column", module);
    var numberColumnObjectType = createObjectType("NumberColumn", module);

    var someFormModel = module.export("SomeFormWithTableFieldModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();
    var widgetMap = WidgetMap.create("SomeFormWithTableFieldModel", someFormModel, null).orElseThrow();

    //    {
    //      id: 'SomeForm',
    //      rootGroupBox: {
    //        id: 'MainBox',
    //        objectType: GroupBox,
    //        fields: [
    //          {
    //            id: 'TitleField',
    //            objectType: StringField
    //          },
    //          {
    //            id: 'SomeTableField',
    //            objectType: TableField,
    //            table: {
    //              id: 'SomeTable',
    //              objectType: Table,
    //              columns: [
    //                {
    //                  id: 'IdColumn',
    //                  objectType: NumberColumn
    //                },
    //                {
    //                  id: 'NameColumn',
    //                  objectType: Column
    //                }
    //              ],
    //              menus: [
    //                {
    //                  id: 'NewMenu',
    //                  objectType: Menu
    //                },
    //                {
    //                  id: 'EditMenu',
    //                  objectType: Menu
    //                }
    //              ]
    //            }
    //          },
    //          {
    //            id: 'FancyTableField',
    //            objectType: TableField,
    //            table: {
    //              id: 'Table',
    //              objectType: FancyTable,
    //              columns: [
    //                {
    //                  id: 'TextColumn',
    //                  objectType: Column
    //                }
    //              ]
    //            }
    //          }
    //        ]
    //      }
    //    }

    assertEquals("SomeFormWithTableFieldWidgetMap", widgetMap.name());

    assertEquals(6, widgetMap.elements().size());
    assertEquals(List.of("MainBox", "TitleField", "SomeTableField", "SomeTable", "FancyTableField", "Table"), widgetMap.elements().keySet().stream().toList());

    assertEquals(1, widgetMap.idObjectTypeMapReferences().size());

    var mainBox = widgetMap.elements().get("MainBox");
    assertNotNull(mainBox);
    assertObjectType(groupBoxObjectType, mainBox.objectType());

    var titleField = widgetMap.elements().get("TitleField");
    assertNotNull(titleField);
    assertObjectType(stringFieldObjectType, titleField.objectType());

    var someTableField = widgetMap.elements().get("SomeTableField");
    assertNotNull(someTableField);
    assertObjectType(tableFieldObjectType, someTableField.objectType());

    var someTable = widgetMap.elements().get("SomeTable");
    assertNotNull(someTable);
    assertObjectType(createObjectType(tableClass).withNewClassName("SomeTable"), someTable.objectType());

    var someTableWidgetMap = someTable.objectType().widgetMap().orElse(null);
    assertNotNull(someTableWidgetMap);
    assertIdObjectTypeMapReference(IdObjectTypeMapReference.create(someTableWidgetMap).orElseThrow(), widgetMap.idObjectTypeMapReferences().stream().findFirst().orElseThrow());

    assertEquals("SomeTableWidgetMap", someTableWidgetMap.name());

    assertEquals(2, someTableWidgetMap.elements().size());
    assertEquals(List.of("NewMenu", "EditMenu"), someTableWidgetMap.elements().keySet().stream().toList());

    var newMenu = someTableWidgetMap.elements().get("NewMenu");
    assertNotNull(newMenu);
    assertObjectType(menuObjectType, newMenu.objectType());

    var editMenu = someTableWidgetMap.elements().get("EditMenu");
    assertNotNull(editMenu);
    assertObjectType(menuObjectType, editMenu.objectType());

    var someTableColumnMap = someTable.objectType().columnMap().orElse(null);
    assertNotNull(someTableColumnMap);

    assertEquals("SomeTableColumnMap", someTableColumnMap.name());

    assertEquals(2, someTableColumnMap.elements().size());
    assertEquals(List.of("IdColumn", "NameColumn"), someTableColumnMap.elements().keySet().stream().toList());

    var idColumn = someTableColumnMap.elements().get("IdColumn");
    assertNotNull(idColumn);
    assertObjectType(numberColumnObjectType, idColumn.objectType());

    var nameColumn = someTableColumnMap.elements().get("NameColumn");
    assertNotNull(nameColumn);
    assertObjectType(columnObjectType, nameColumn.objectType());

    var fancyTableField = widgetMap.elements().get("FancyTableField");
    assertNotNull(fancyTableField);
    assertObjectType(tableFieldObjectType, fancyTableField.objectType());

    var table = widgetMap.elements().get("Table");
    assertNotNull(table);
    assertObjectType(createObjectType(fancyTableClass).withNewClassName("FancyTableFieldTable"), table.objectType());

    var fancyTableFieldTableColumnMap = table.objectType().columnMap().orElse(null);
    assertNotNull(fancyTableFieldTableColumnMap);

    assertEquals("FancyTableFieldTableColumnMap", fancyTableFieldTableColumnMap.name());

    assertEquals(1, fancyTableFieldTableColumnMap.elements().size());
    assertEquals(List.of("TextColumn"), fancyTableFieldTableColumnMap.elements().keySet().stream().toList());

    assertEquals(1, fancyTableFieldTableColumnMap.idObjectTypeMapReferences().size());
    assertIdObjectTypeMapReference(IdObjectTypeMapReference.create(findClassByObjectType("FancyTableColumnMap", module)).orElseThrow(), fancyTableFieldTableColumnMap.idObjectTypeMapReferences().stream().findFirst().orElseThrow());

    var textColumn = fancyTableFieldTableColumnMap.elements().get("TextColumn");
    assertNotNull(textColumn);
    assertObjectType(columnObjectType, textColumn.objectType());
  }

  @Test
  @ExtendWithNodeModules("SomeFormWithReferencedWidgetMapsModel")
  public void testWidgetMapWithReferencedWidgetMaps(INodeModule module) {
    var groupBoxObjectType = createObjectType("GroupBox", module);
    var stringFieldObjectType = createObjectType("StringField", module);
    var tabBoxObjectType = createObjectType("TabBox", module);
    var documentsBoxObjectType = createObjectType("DocumentsBox", module);
    var notesBoxObjectType = createObjectType("NotesBox", module);

    var someFormModel = module.export("SomeFormWithReferencedWidgetMapsModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();
    var widgetMap = WidgetMap.create("SomeFormWithReferencedWidgetMapsModel", someFormModel, null).orElseThrow();

    //    {
    //      id: 'SomeForm',
    //      rootGroupBox: {
    //        id: 'MainBox',
    //        objectType: GroupBox,
    //        fields: [
    //          {
    //            id: 'TitleField',
    //            objectType: StringField
    //          },
    //          {
    //            id: 'TabBox',
    //            objectType: TabBox,
    //            tabItems: [
    //              {
    //                id: 'DocumentsBox',
    //                objectType: DocumentsBox
    //              },
    //              {
    //                id: 'DocumentsBox2',
    //                objectType: DocumentsBox
    //              },
    //              {
    //                id: 'NotesBox',
    //                objectType: NotesBox
    //              }
    //            ]
    //          }
    //        ]
    //      }
    //    }

    assertEquals("SomeFormWithReferencedWidgetMapsWidgetMap", widgetMap.name());

    assertEquals(6, widgetMap.elements().size());
    assertEquals(List.of("MainBox", "TitleField", "TabBox", "DocumentsBox", "DocumentsBox2", "NotesBox"), widgetMap.elements().keySet().stream().toList());

    assertEquals(2, widgetMap.idObjectTypeMapReferences().size());
    var idObjectTypeMapReferences = widgetMap.idObjectTypeMapReferences().stream().toList();
    assertIdObjectTypeMapReference(IdObjectTypeMapReference.create(findClassByObjectType("DocumentsBoxWidgetMap", module)).orElseThrow(), idObjectTypeMapReferences.get(0));
    assertIdObjectTypeMapReference(IdObjectTypeMapReference.create(findClassByObjectType("NotesBoxWidgetMap", module)).orElseThrow(), idObjectTypeMapReferences.get(1));

    var mainBox = widgetMap.elements().get("MainBox");
    assertNotNull(mainBox);
    assertObjectType(groupBoxObjectType, mainBox.objectType());

    var titleField = widgetMap.elements().get("TitleField");
    assertNotNull(titleField);
    assertObjectType(stringFieldObjectType, titleField.objectType());

    var tabBox = widgetMap.elements().get("TabBox");
    assertNotNull(tabBox);
    assertObjectType(tabBoxObjectType, tabBox.objectType());

    var documentsBox = widgetMap.elements().get("DocumentsBox");
    assertNotNull(documentsBox);
    assertObjectType(documentsBoxObjectType, documentsBox.objectType());

    var documentsBox2 = widgetMap.elements().get("DocumentsBox2");
    assertNotNull(documentsBox2);
    assertObjectType(documentsBoxObjectType, documentsBox2.objectType());

    var notesBox = widgetMap.elements().get("NotesBox");
    assertNotNull(notesBox);
    assertObjectType(notesBoxObjectType, notesBox.objectType());
  }

  @Test
  @ExtendWithNodeModules("SomeFormWithSuperWidgetMapModel")
  public void testWidgetMapWithSuperWidgetMap(INodeModule module) {
    var groupBoxObjectType = createObjectType("GroupBox", module);
    var stringFieldObjectType = createObjectType("StringField", module);

    var someFormModel = module.export("SomeFormWithSuperWidgetMapModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();
    var someForm = module.export("SomeFormWithSuperWidgetMap")
        .filter(IES6Class.class::isInstance)
        .map(IES6Class.class::cast)
        .orElseThrow();
    var widgetMap = WidgetMap.create("SomeFormWithSuperWidgetMapModel", someFormModel, someForm).orElseThrow();

    //    {
    //      id: 'SomeForm',
    //      rootGroupBox: {
    //        id: 'MainBox',
    //        objectType: GroupBox,
    //        fields: [
    //          {
    //            id: 'TitleField',
    //            objectType: StringField
    //          }
    //        ]
    //      }
    //    }

    assertEquals("SomeFormWithSuperWidgetMapWidgetMap", widgetMap.name());

    assertEquals(2, widgetMap.elements().size());
    assertEquals(List.of("MainBox", "TitleField"), widgetMap.elements().keySet().stream().toList());

    assertEquals(1, widgetMap.idObjectTypeMapReferences().size());
    var idObjectTypeMapReferences = widgetMap.idObjectTypeMapReferences().stream().toList();
    assertIdObjectTypeMapReference(IdObjectTypeMapReference.create(findClassByObjectType("SomeSuperClass2WidgetMap", module)).orElseThrow(), idObjectTypeMapReferences.get(0));

    var mainBox = widgetMap.elements().get("MainBox");
    assertNotNull(mainBox);
    assertObjectType(groupBoxObjectType, mainBox.objectType());

    var titleField = widgetMap.elements().get("TitleField");
    assertNotNull(titleField);
    assertObjectType(stringFieldObjectType, titleField.objectType());
  }

  @Test
  @ExtendWithNodeModules("SomeEmptyFormWithSuperWidgetMapModel")
  public void testEmptyWidgetMapWithSuperWidgetMap(INodeModule module) {
    var someFormModel = module.export("SomeEmptyFormWithSuperWidgetMapModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();
    var someForm = module.export("SomeEmptyFormWithSuperWidgetMap")
        .filter(IES6Class.class::isInstance)
        .map(IES6Class.class::cast)
        .orElseThrow();
    var widgetMap = WidgetMap.create("SomeEmptyFormWithSuperWidgetMapModel", someFormModel, someForm).orElseThrow();

    //    {
    //      id: 'SomeForm'
    //    }

    assertEquals("SomeEmptyFormWithSuperWidgetMapWidgetMap", widgetMap.name());

    assertEquals(0, widgetMap.elements().size());

    assertEquals(1, widgetMap.idObjectTypeMapReferences().size());
    var idObjectTypeMapReferences = widgetMap.idObjectTypeMapReferences().stream().toList();
    assertIdObjectTypeMapReference(IdObjectTypeMapReference.create(findClassByObjectType("SomeSuperClass2WidgetMap", module)).orElseThrow(), idObjectTypeMapReferences.get(0));
  }

  @Test
  @ExtendWithNodeModules("SomePageModel")
  public void testPageObjectTypes(INodeModule module) {
    assertSomePageModel(module, "SomePageForm", "SomeTable");
  }

  @Test
  @ExtendWithNodeModules("SomePageModelWithoutTopLevelId")
  public void testPageObjectTypesWithoutTopLevelId(INodeModule module) {
    assertSomePageModel(module, "SomePageForm", "SomePageTable");
  }

  private static void assertSomePageModel(INodeModule module, String expectedDetailFormName, String expectedDetailTableName) {
    var formClass = findClassByObjectType("Form", module);
    var tableClass = findClassByObjectType("Table", module);

    var groupBoxObjectType = createObjectType("GroupBox", module);
    var stringFieldObjectType = createObjectType("StringField", module);
    var numberFieldObjectType = createObjectType("NumberField", module);
    var menuObjectType = createObjectType("Menu", module);
    var columnObjectType = createObjectType("Column", module);
    var numberColumnObjectType = createObjectType("NumberColumn", module);

    var somePageModel = module.export("SomePageModel")
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();

    //    {
    //      id: 'SomePage',
    //      detailForm: {
    //        id: 'Form', // OPTIONAL
    //        objectType: Form,
    //        rootGroupBox: {
    //          id: 'MainBox',
    //          objectType: GroupBox,
    //          fields: [
    //            {
    //              id: 'LastNameField',
    //              objectType:StringField
    //            },
    //            {
    //              id: 'FirstNameField',
    //              objectType: StringField
    //            },
    //            {
    //              id: 'AgeField'
    //              objectType: NumberField
    //            }
    //          ]
    //        }
    //      },
    //      detailTable: {
    //        id: 'SomeTable', // OPTIONAL
    //        objectType: Table,
    //        columns: [
    //          {
    //            id: 'IdColumn',
    //            objectType: NumberColumn
    //          },
    //          {
    //            id: 'NameColumn',
    //            objectType: Column
    //          }
    //        ],
    //        menus: [
    //          {
    //            id: 'NewMenu',
    //            objectType: Menu
    //          },
    //          {
    //            id: 'EditMenu',
    //            objectType: Menu
    //          }
    //        ]
    //      }
    //    }

    var somePageForm = IdObjectTypeMapUtils.createDetailFormForPage("SomePageModel", somePageModel).orElseThrow();
    assertObjectType(createObjectType(formClass).withNewClassName(expectedDetailFormName), somePageForm);

    var somePageFormWidgetMap = somePageForm.widgetMap().orElseThrow();
    assertTrue(somePageForm.columnMap().isEmpty());

    assertEquals(expectedDetailFormName + "WidgetMap", somePageFormWidgetMap.name());

    assertEquals(4, somePageFormWidgetMap.elements().size());
    assertEquals(List.of("MainBox", "LastNameField", "FirstNameField", "AgeField"), somePageFormWidgetMap.elements().keySet().stream().toList());

    assertEquals(0, somePageFormWidgetMap.idObjectTypeMapReferences().size());

    var mainBox = somePageFormWidgetMap.elements().get("MainBox");
    assertNotNull(mainBox);
    assertObjectType(groupBoxObjectType, mainBox.objectType());

    var lastNameField = somePageFormWidgetMap.elements().get("LastNameField");
    assertNotNull(lastNameField);
    assertObjectType(stringFieldObjectType, lastNameField.objectType());

    var firstNameField = somePageFormWidgetMap.elements().get("FirstNameField");
    assertNotNull(firstNameField);
    assertObjectType(stringFieldObjectType, firstNameField.objectType());

    var ageField = somePageFormWidgetMap.elements().get("AgeField");
    assertNotNull(ageField);
    assertObjectType(numberFieldObjectType, ageField.objectType());

    var someTable = IdObjectTypeMapUtils.createDetailTableForPage("SomePageModel", somePageModel).orElseThrow();
    assertObjectType(createObjectType(tableClass).withNewClassName(expectedDetailTableName), someTable);

    var someTableWidgetMap = someTable.widgetMap().orElseThrow();
    var someTableColumnMap = someTable.columnMap().orElseThrow();

    assertEquals(expectedDetailTableName + "WidgetMap", someTableWidgetMap.name());

    assertEquals(2, someTableWidgetMap.elements().size());
    assertEquals(List.of("NewMenu", "EditMenu"), someTableWidgetMap.elements().keySet().stream().toList());

    assertEquals(0, someTableWidgetMap.idObjectTypeMapReferences().size());

    var newMenu = someTableWidgetMap.elements().get("NewMenu");
    assertNotNull(newMenu);
    assertObjectType(menuObjectType, newMenu.objectType());

    var editMenu = someTableWidgetMap.elements().get("EditMenu");
    assertNotNull(editMenu);
    assertObjectType(menuObjectType, editMenu.objectType());

    assertEquals(expectedDetailTableName + "ColumnMap", someTableColumnMap.name());

    assertEquals(2, someTableColumnMap.elements().size());
    assertEquals(List.of("IdColumn", "NameColumn"), someTableColumnMap.elements().keySet().stream().toList());

    assertEquals(0, someTableColumnMap.idObjectTypeMapReferences().size());

    var idColumn = someTableColumnMap.elements().get("IdColumn");
    assertNotNull(idColumn);
    assertObjectType(numberColumnObjectType, idColumn.objectType());

    var nameColumn = someTableColumnMap.elements().get("NameColumn");
    assertNotNull(nameColumn);
    assertObjectType(columnObjectType, nameColumn.objectType());
  }

  private static void assertObjectType(ObjectType expected, ObjectType actual) {
    assertNotNull(actual);
    assertSame(expected.es6Class(), actual.es6Class());
    assertEquals(expected.newClassName(), actual.newClassName());
  }

  private static void assertIdObjectTypeMapReference(IdObjectTypeMapReference expected, IdObjectTypeMapReference actual) {
    assertNotNull(actual);
    expected.es6Class().ifPresent(es6Class -> assertSame(es6Class, actual.es6Class().orElseThrow()));
    expected.idObjectTypeMap().ifPresent(widgetMap -> assertSame(widgetMap, actual.idObjectTypeMap().orElseThrow()));
  }

  private static IES6Class findClassByObjectType(String objectType, INodeModule module) {
    return module.elements()
        .withRecursive(true)
        .stream()
        .filter(e -> objectType.equals(e.name()))
        .filter(IES6Class.class::isInstance)
        .findAny()
        .map(IES6Class.class::cast)
        .orElseThrow();
  }

  private static ObjectType createObjectType(String objectType, INodeModule module) {
    return createObjectType(findClassByObjectType(objectType, module));
  }

  private static ObjectType createObjectType(IES6Class es6Class) {
    return ObjectType.create(es6Class, new HashSet<>()).orElseThrow();
  }
}
