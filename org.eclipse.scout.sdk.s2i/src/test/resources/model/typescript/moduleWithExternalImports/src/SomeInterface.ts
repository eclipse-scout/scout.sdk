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
import {WildcardClass as WildcardClassAlias} from '@eclipse-scout/sdk-export-ts';

// noinspection JSUnusedGlobalSymbols
export interface SomeInterface {
  myStringDef?: string;
  myNumberDef: number;
  myBooleanDef: boolean;
  myUndefinedDef: undefined;
  myNullDef: null;
  myObjectDef: object;
  myAnyDef?: any;
  myRefDef: WildcardClassAlias;
  myStringArrayDef?: string[][];
  myNumberArrayDef: number[];

  someFunc(): number;
}