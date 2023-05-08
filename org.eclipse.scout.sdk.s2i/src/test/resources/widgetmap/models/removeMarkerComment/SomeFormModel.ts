/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// @ts-expect-error
import {GroupBox, StringField} from '@eclipse-scout/core';

export default () => ({
  rootGroupBox: {
    objectType: GroupBox,
    fields: [
      {
        objectType: GroupBox,
        fields: [
          {
            objectType: StringField
          }
        ]
      }]
  }
});

/* **************************************************************************
* GENERATED WIDGET MAPS
* **************************************************************************/

export type SomeFormWidgetMap = {
  'MainBox': GroupBox;
  'DetailBox': GroupBox;
  'NameField': StringField;
};
