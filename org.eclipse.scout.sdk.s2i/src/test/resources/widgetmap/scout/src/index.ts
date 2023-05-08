/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

export * from './widget/Widget';
export * from './form/Form';
export * from './form/fields/FormField';
export * from './form/fields/stringfield/StringField';
export * from './form/fields/groupbox/GroupBox';
export * from './types';

import * as self from './index';

export default self;

// @ts-expect-error
ObjectFactory.get().registerNamespace('scout', self);
