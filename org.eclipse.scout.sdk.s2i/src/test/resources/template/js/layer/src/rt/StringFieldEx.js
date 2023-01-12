/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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