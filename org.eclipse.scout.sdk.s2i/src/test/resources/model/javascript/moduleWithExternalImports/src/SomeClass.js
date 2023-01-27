/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {WildcardClass as WildcardClassAlias} from '@eclipse-scout/sdk-export-js';

// noinspection JSUnusedGlobalSymbols
export class SomeClass {

  constructor() {
    /**
     *  @type {string}
     */
    this.myStringDef = null;
    this.myStringInfer = "string";

    /** @type {boolean} */
    this.myBooleanDef = null;
    this.myBooleanInfer = false;

    /** @type {undefined} */
    this.myUndefinedDef = null;
    this.myUndefinedInfer = undefined;

    /** @type {null} */
    this.myNullDef = null;
    this.myNullInfer = null;

    /** @type {object} */
    this.myObjectDef = null;
    this.myObjectInfer = {};

    /** @type {any} */
    this.myAnyDef = null;
    this.myAnyInfer = null;

    /** @type {WildcardClassAlias} */
    this.myRefDef = null;
    this.myRefInfer = new WildcardClassAlias();
  }

  /**
   * @type {number}
   */
  myNumberDef = null;
  myNumberInfer = 42;

  static MY_ENUM = 'myEnum';

  someFunc() {
    return 3;
  }
}