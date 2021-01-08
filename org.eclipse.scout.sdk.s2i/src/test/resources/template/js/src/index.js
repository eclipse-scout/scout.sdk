/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

export {default as Widget} from './rt/Widget';
export {default as StringField} from './rt/StringField';
// noinspection JSUnusedGlobalSymbols
export {default as NameCompletionModel} from './models/NameCompletionModel'
// noinspection JSUnusedGlobalSymbols
export {default as NameCompletionUnknownObject} from './models/NameCompletionUnknownObject'
// noinspection JSUnusedGlobalSymbols
export {default as ValueCompletionObjectType} from './models/ValueCompletionObjectType'
// noinspection JSUnusedGlobalSymbols
export {default as ValueCompletionWidget} from './models/ValueCompletionWidget'
// noinspection JSUnusedGlobalSymbols
export {default as ValueCompletionEnum} from './models/ValueCompletionEnum'

import * as self from './index.js';

window.scout = Object.assign(window.scout || {}, self);