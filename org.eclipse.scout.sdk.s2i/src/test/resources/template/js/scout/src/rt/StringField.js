/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget} from './../index';

export default class StringField extends Widget {

  static DEFAULT_MAX_LENGTH = 4;

  static DEFAULT_FIELD_STYLE = StringField.FieldStyle.ALTERNATIVE;

  constructor() {
    super();
    // noinspection JSUnusedGlobalSymbols
    this.maxLength = StringField.DEFAULT_MAX_LENGTH;
    // noinspection JSUnusedGlobalSymbols
    this.fieldStyle = StringField.DEFAULT_FIELD_STYLE;
    this._init()
  }

  static FieldStyle = {
    CLASSIC: 'classic',
    ALTERNATIVE: 'alternative'
  };

  _init(model) {
  }
}