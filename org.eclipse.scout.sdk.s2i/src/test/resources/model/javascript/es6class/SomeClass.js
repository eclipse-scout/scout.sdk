/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection JSUnusedGlobalSymbols
class SomeClass {

  constructor() {
    /**
     *  @type {string}
     */
    this.myStringOpt = "null";
    /** @type {boolean} */
    this.myBoolean = false;
    this.myUndefined = undefined;
    this.myNull = null;
    /**
     * @type {object}
     */
    this.myObject = {};
    this.myAnyOpt = null;
  }

  /**
   * @type {number}
   */
  myNumber = 5;

  static MY_ENUM = 'myEnum';

  someFunc() {
    return 3;
  }
}