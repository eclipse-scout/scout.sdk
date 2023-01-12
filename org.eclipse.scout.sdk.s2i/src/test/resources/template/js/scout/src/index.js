/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

export {default as Widget} from './rt/Widget';
export {default as StringField} from './rt/StringField';

import * as self from './index.js';

// noinspection JSUnresolvedVariable,JSUnresolvedFunction
ObjectFactory.get().registerNamespace('scout', self);