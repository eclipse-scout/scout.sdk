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
  myStringInfer? = 'string';

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

  myStaticStringRefInfer = SomeClass.myStaticStringDef;
  myEnumRefInfer = SomeClass.myEnumInfer.b;

  myStringArrayDef?: string[][];
  myStringArrayInfer? = [['string']];

  myNumberArrayDef: number[];
  myNumberArrayInfer = [42];

  myArrayInfer = [];

  myStringNumberUnionDef: string | number;
  myStringNumberUnionArrayDef: (string | number)[];
  myStringNumberUnionArrayInfer = [42, 'string', 13];
  myStringArrayNumberUnionDef: string[] | number;

  myAbBcIntersectionDef: AB & BC;
  myAbBcIntersectionArrayDef: (AB & BC)[];
  myAbBcArrayIntersectionDef: AB & BC[];

  static myStaticStringDef: string = 'staticString' as const;
  static myStaticStringInfer = 'staticString' as const;

  static myEnumInfer = {
    a: 1,
    b: 2,
    c: 3
  } as const;

  someFunc(): number {
    return 3;
  }
}

type AB = {
  a: number;
  b: number;
};

type BC = {
  b: number;
  c: number;
};