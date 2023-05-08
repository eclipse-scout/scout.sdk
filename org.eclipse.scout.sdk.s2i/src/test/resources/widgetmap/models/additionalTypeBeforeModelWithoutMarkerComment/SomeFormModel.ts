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

export type Some = any;

export default () => ({
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    fields: [
      {
        id: 'DetailBox',
        objectType: GroupBox,
        fields: [
          {
            id: 'NameField',
            objectType: StringField
          }
        ]
      }]
  }
});

export type SomeFormWidgetMap = {
  'MainBox': GroupBox;
};
