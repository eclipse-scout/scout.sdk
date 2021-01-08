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