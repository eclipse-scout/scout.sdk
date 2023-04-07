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

import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.removeWhitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportCollector.ES6ImportDescriptor;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.testing.ExtendWithNodeModules;
import org.junit.jupiter.api.Test;

public class WidgetMapCreateOperationTest {

  @Test
  @ExtendWithNodeModules("SomeFormModel")
  public void testWidgetMap(INodeModule module) {
    var someFormModel = module.export("SomeFormModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();

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

    var operation = new WidgetMapCreateOperation();
    operation.setLiteral(someFormModel);
    operation.execute();

    assertEqualsIgnoreWhitespaces(List.of("export type SomeFormWidgetMap = {" +
        "'MainBox': GroupBox;" +
        "'LastNameField': StringField;" +
        "'FirstNameField': StringField;" +
        "'SomeMenu': Menu;" +
        "};"), operation.classSources());

    var declarationSources = operation.declarationSources();
    assertEquals(1, declarationSources.size());
    assertEqualsIgnoreWhitespaces("declare widgetMap: SomeFormWidgetMap;", declarationSources.get("widgetMap"));

    var imports = operation.importNamesForDeclarations().stream().map(ES6ImportDescriptor::nameForSource).toList();
    assertEquals(List.of("SomeFormWidgetMap"), imports);
  }

  @Test
  @ExtendWithNodeModules("SomeFormWithTableFieldModel")
  public void testWidgetMapWithTable(INodeModule module) {
    var someFormModel = module.export("SomeFormWithTableFieldModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();

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

    var operation = new WidgetMapCreateOperation();
    operation.setLiteral(someFormModel);
    operation.execute();

    assertEqualsIgnoreWhitespaces(List.of(
        "export type SomeFormWithTableFieldWidgetMap = {" +
            "'MainBox': GroupBox;" +
            "'TitleField': StringField;" +
            "'SomeTableField': TableField;" +
            "'SomeTable': SomeTable;" +
            "'FancyTableField': TableField;" +
            "'Table': FancyTableFieldTable;" +
            "} & SomeTableWidgetMap;",
        "export class SomeTable extends Table {" +
            "declare widgetMap: SomeTableWidgetMap;" +
            "declare columnMap: SomeTableColumnMap;" +
            "}",
        "export type SomeTableWidgetMap = {" +
            "'NewMenu': Menu;" +
            "'EditMenu': Menu;" +
            "};",
        "export type SomeTableColumnMap = {" +
            "'IdColumn': NumberColumn;" +
            "'NameColumn': Column;" +
            "};",
        "export class FancyTableFieldTable extends FancyTable {" +
            "declare columnMap: FancyTableFieldTableColumnMap;" +
            "}",
        "export type FancyTableFieldTableColumnMap = {" +
            "'TextColumn': Column;" +
            "} & FancyTableColumnMap;"

    ), operation.classSources());

    var declarationSources = operation.declarationSources();
    assertEquals(1, declarationSources.size());
    assertEqualsIgnoreWhitespaces("declare widgetMap: SomeFormWithTableFieldWidgetMap;", declarationSources.get("widgetMap"));

    var imports = operation.importNamesForDeclarations().stream().map(ES6ImportDescriptor::nameForSource).toList();
    assertEquals(List.of("SomeFormWithTableFieldWidgetMap"), imports);
  }

  @Test
  @ExtendWithNodeModules("SomeFormWithReferencedWidgetMapsModel")
  public void testWidgetMapWithReferencedWidgetMaps(INodeModule module) {
    var someFormModel = module.export("SomeFormWithReferencedWidgetMapsModel")
        .filter(IFunction.class::isInstance)
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();

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
    //                id: 'NotesBox',
    //                objectType: NotesBox
    //              }
    //            ]
    //          }
    //        ]
    //      }
    //    }

    var operation = new WidgetMapCreateOperation();
    operation.setLiteral(someFormModel);
    operation.execute();

    assertEqualsIgnoreWhitespaces(List.of("export type SomeFormWithReferencedWidgetMapsWidgetMap = {" +
        "'MainBox': GroupBox;" +
        "'TitleField': StringField;" +
        "'TabBox': TabBox;" +
        "'DocumentsBox': DocumentsBox;" +
        "'NotesBox': NotesBox;" +
        "} & DocumentsBoxWidgetMap & NotesBoxWidgetMap;"), operation.classSources());

    var declarationSources = operation.declarationSources();
    assertEquals(1, declarationSources.size());
    assertEqualsIgnoreWhitespaces("declare widgetMap: SomeFormWithReferencedWidgetMapsWidgetMap;", declarationSources.get("widgetMap"));

    var imports = operation.importNamesForDeclarations().stream().map(ES6ImportDescriptor::nameForSource).toList();
    assertEquals(List.of("SomeFormWithReferencedWidgetMapsWidgetMap"), imports);
  }

  @Test
  @ExtendWithNodeModules("SomePageModel")
  public void testPageObjectTypes(INodeModule module) {
    var somePageModel = module.export("SomePageModel")
        .map(IFunction.class::cast)
        .flatMap(IFunction::resultingObjectLiteral)
        .orElseThrow();

    //    {
    //      id: 'SomePage',
    //      detailForm: {
    //        id: 'Form',
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
    //        id: 'SomeTable',
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

    var operation = new WidgetMapCreateOperation();
    operation.setLiteral(somePageModel);
    operation.setPage(true);
    operation.execute();

    assertEqualsIgnoreWhitespaces(List.of(
        "export class SomePageForm extends Form {" +
            "declare widgetMap: SomePageFormWidgetMap;" +
            "}",
        "export type SomePageFormWidgetMap = {" +
            "'MainBox': GroupBox;" +
            "'LastNameField': StringField;" +
            "'FirstNameField': StringField;" +
            "'AgeField': NumberField;" +
            "};",
        "export class SomeTable extends Table {" +
            "declare widgetMap: SomeTableWidgetMap;" +
            "declare columnMap: SomeTableColumnMap;" +
            "}",
        "export type SomeTableWidgetMap = {" +
            "'NewMenu': Menu;" +
            "'EditMenu': Menu;" +
            "};",
        "export type SomeTableColumnMap = {" +
            "'IdColumn': NumberColumn;" +
            "'NameColumn': Column;" +
            "};"),
        operation.classSources());

    var declarationSources = operation.declarationSources();
    assertEquals(2, declarationSources.size());
    assertEqualsIgnoreWhitespaces("declare detailForm: SomePageForm;", declarationSources.get("detailForm"));
    assertEqualsIgnoreWhitespaces("declare detailTable: SomeTable;", declarationSources.get("detailTable"));

    var imports = operation.importNamesForDeclarations().stream().map(ES6ImportDescriptor::nameForSource).toList();
    assertEquals(List.of("SomePageForm", "SomeTable"), imports);
  }

  private static void assertEqualsIgnoreWhitespaces(CharSequence expected, CharSequence actual) {
    if (expected == null) {
      assertNull(actual);
      return;
    }

    assertEquals(removeWhitespace(expected), removeWhitespace(actual));
  }

  private static void assertEqualsIgnoreWhitespaces(List<CharSequence> expected, List<CharSequence> actual) {
    if (expected == null) {
      assertNull(actual);
      return;
    }

    assertEquals(
        expected.stream().map(CoreTestingUtils::removeWhitespace).collect(Collectors.toList()),
        actual.stream().map(CoreTestingUtils::removeWhitespace).collect(Collectors.toList()));
  }
}
