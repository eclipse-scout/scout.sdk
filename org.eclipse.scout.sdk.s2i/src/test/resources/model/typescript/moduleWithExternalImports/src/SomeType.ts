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
import {WildcardClass as WildcardClassAlias} from '@eclipse-scout/sdk-export-ts';
import {Generics} from "./index";

// noinspection JSUnusedGlobalSymbols
export type SomeType = {
  myStringDef?: string;
  myNumberDef: number;
  myBooleanDef: boolean;
  myUndefinedDef: undefined;
  myNullDef: null;
  myObjectDef: object;
  myAnyDef?: any;
  myRefDef: WildcardClassAlias;
  myRefGenericsNumberDef: Generics<number>;
  myRefGenericsBooleanDef: (Generics<boolean>);
  myRefGenericsStringDef: Generics;
  myStringArrayDef?: string[][];
  myNumberArrayDef: number[];
  myStringNumberUnionDef: string | number;
  myStringNumberUnionArrayDef: (string | number)[];
  myStringArrayNumberUnionDef: string[] | number;
  myAbBcIntersectionDef: AB & BC;
  myAbBcIntersectionArrayDef: (AB & BC)[];
  myAbBcArrayIntersectionDef: AB & BC[];
};

type AB = {
  a: number;
  b: number;
};

type BC = {
  b: number;
  c: number;
};