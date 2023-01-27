/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

export type EnumObject<T> = T[keyof T];

export class EnumLikeTypes {

  static Const = {
    FIRST: 'a',
    SECOND: 'b',
    THIRD: 'c'
  } as const;

  static NoConst = {
    first: 1,
    second: 2,
    third: 3
  };

}

export type ConstEnumType = EnumObject<typeof EnumLikeTypes.Const>;

export type NoConstEnumType = EnumObject<typeof EnumLikeTypes.NoConst>;
