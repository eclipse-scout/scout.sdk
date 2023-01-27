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
export class SomeClass {
  myStringDef?: string;
  myStringInfer? = "string";

  myNumberDef: number;
  myNumberInfer = 42;

  myBooleanDef: boolean;
  myBooleanInfer = false;

  myUndefinedDef: undefined;
  myUndefinedInfer = undefined;

  myNullDef: null;
  myNullInfer = null;

  myObjectDef: object;
  myObjectInfer = {};

  myAnyDef?: any;
  myAnyInfer? = null;

  myRefDef: WildcardClassAlias;
  myRefInfer = new WildcardClassAlias();

  static MY_ENUM = 'myEnum';

  someFunc(): number {
    return 3;
  }
}