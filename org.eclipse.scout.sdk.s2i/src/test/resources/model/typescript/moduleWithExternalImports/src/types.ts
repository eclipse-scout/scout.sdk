/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection JSUnusedGlobalSymbols
export class Foo {
  alignment: Alignment;
  orientation: 'top' | 'right' | 'bottom' | 'left';
  nestedEnum: NestedEnum;
  topLevelEnum: TopLevelEnum;
  objectLiteralTypeEnum: ObjectLiteralTypeEnum;
  realEnum: RealEnum;

  static NestedEnumLike = {
    A: 1,
    B: 2,
    C: 3
  } as const;
}

export const TopLevelEnumLike = {
  FOO: 'foo',
  BAR: 'bar'
};

export type ObjectLiteralType = {
  DEFAULT: 1,
  ALTERNATIVE: 2
};

// noinspection JSUnusedGlobalSymbols
export enum RealEnum {
  X = 'x',
  Y = 'y',
  Z = 'z'
}

export type EnumObject<T> = T[keyof T];
export type NestedEnum = EnumObject<typeof Foo.NestedEnumLike>;
export type TopLevelEnum = EnumObject<typeof TopLevelEnumLike>;
export type ObjectLiteralTypeEnum = EnumObject<ObjectLiteralType>;

export type Alignment = -1 | 0 | 1;
