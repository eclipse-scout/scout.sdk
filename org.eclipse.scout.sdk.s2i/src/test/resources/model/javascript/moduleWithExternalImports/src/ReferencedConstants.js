/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {EnumInClass, HAlign, RoundingMode} from '@eclipse-scout/sdk-enum-js';
import {SomeClass, TestClass, WithTypeRef} from './index';

// noinspection JSUnusedGlobalSymbols
export const ReferencedValue = SomeClass.myStaticStringDef;
// noinspection JSUnusedGlobalSymbols
export const ReferencedValueProp = RoundingMode.HALF_UP;
// noinspection JSUnusedGlobalSymbols
export const ReferencedEnum = EnumInClass.NestedEnum;
// noinspection JSUnusedGlobalSymbols
export const ReferencedEnumProp = HAlign.NEXT;
// noinspection JSUnusedGlobalSymbols
export const ReferencedType = TestClass.wild;
// noinspection JSUnusedGlobalSymbols
export const ReferencedTypeProp = WithTypeRef.wild;
