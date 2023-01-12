/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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