/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
// @ts-expect-error
import {WildcardClass} from '@eclipse-scout/sdk-export-ts';
import {Generics} from "./index";

export default (): object => ({
  objectType: WildcardClass,
  fields: [
    {
      id: 'GenericsNumber',
      objectType: Generics<number>
    },
    {
      id: 'GenericsBoolean',
      objectType: (Generics<boolean>)
    },
    {
      id: 'GenericsString',
      objectType: Generics
    },
    {
      id: 'GenericsNew',
      objectType: new Generics<Generics<WildcardClass>>
    }
  ]
});

// noinspection JSUnusedGlobalSymbols
export type SampleWidgetMap = {
  'GenericsNumber': Generics<number>;
  'GenericsBoolean': Generics<boolean>;
  'GenericsString': Generics;
};
