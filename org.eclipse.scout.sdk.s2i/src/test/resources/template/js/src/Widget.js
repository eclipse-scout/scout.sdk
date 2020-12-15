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
export default class Widget {
  constructor() {
    this.id = null;
    this.objectType = null;
    this.visible = true;
    this.name = '';
    this.children = [];
    this.child = null;
    this.state = Widget.WidgetState.B;
    this.label = null;

    this._private = null;
    this.$jQuery = null;

    this._addWidgetProperties(['children', 'child', 'onlyHere']);
    this._init()
  }

  static WidgetState = {
    A: 'a',
    B: 'b',
    C: 'c'
  };

  _init(model) {
    this.resolveTextKeys(['label']);
  }

  resolveTextKeys() {
  }

  _addWidgetProperties() {
  }
}