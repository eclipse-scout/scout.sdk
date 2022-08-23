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
// noinspection NpmUsedModulesInstalled
import {StringField} from '@eclipse-scout/core';

export default class StringFieldEx extends StringField {

  static DEFAULT_MIN_LENGTH = 0;

  constructor() {
    super();
    // noinspection JSUnusedGlobalSymbols
    this.minLength = StringFieldEx.DEFAULT_MIN_LENGTH;
  }
}