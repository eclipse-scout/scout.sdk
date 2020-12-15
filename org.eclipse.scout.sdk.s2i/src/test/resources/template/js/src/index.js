/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

export {default as Widget} from './Widget';
export {default as StringField} from './StringField';
export {default as NameCompletionModel} from './NameCompletionModel'
export {default as NameCompletionUnknownObject} from './NameCompletionUnknownObject'
export {default as ValueCompletionObjectType} from './ValueCompletionObjectType'
export {default as ValueCompletionWidget} from './ValueCompletionWidget'
export {default as ValueCompletionEnum} from './ValueCompletionEnum'

import * as self from './index.js';

export default self;
window.scout = Object.assign(window.scout || {}, self);