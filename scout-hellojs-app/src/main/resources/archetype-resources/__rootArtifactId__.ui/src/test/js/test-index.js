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
import {JasmineScout} from '@eclipse-scout/core/src/testing';

import * as ref1 from '../../main/js/index';

Object.assign({}, ref1);

let context = require.context('./', true, /[sS]pec\.js$/);
JasmineScout.runTestSuite(context);
