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
export default class Widget {
  constructor() {
    // noinspection JSUnusedGlobalSymbols
    this.id = null;
    // noinspection JSUnusedGlobalSymbols
    this.objectType = null;
    // noinspection JSUnusedGlobalSymbols
    this.visible = true;
    this.name = '';
    this.fields = [];
    this.children = [];
    this.child = null;
    // noinspection JSUnusedGlobalSymbols
    this.enabledComputed = false;
    // noinspection JSUnusedGlobalSymbols
    this.state = Widget.WidgetState.B;
    this.label = null;
    this.selectedTab = null;

    // noinspection JSUnusedGlobalSymbols
    this._private = null;
    // noinspection JSUnusedGlobalSymbols
    this.$jQuery = null;

    this._addWidgetProperties(['fields', 'children', 'child', 'selectedTab', 'onlyHere']);
    this._addPreserveOnPropertyChangeProperties(['selectedTab']);
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

  _addPreserveOnPropertyChangeProperties() {
  }
}