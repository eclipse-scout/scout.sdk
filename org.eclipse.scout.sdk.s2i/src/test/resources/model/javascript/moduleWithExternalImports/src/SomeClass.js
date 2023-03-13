/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {WildcardClass as WildcardClassAlias, AnotherClass} from '@eclipse-scout/sdk-export-js';

// noinspection JSUnusedGlobalSymbols
export class SomeClass {

  constructor() {
    /**
     *  @type {string}
     */
    this.myStringDef = null;
    this.myStringInfer = 'string';

    /** @type {boolean} */
    this.myBooleanDef = null;
    this.myBooleanInfer = false;

    this.myNumberInfer = null;

    /** @type {undefined} */
    this.myUndefinedDef = null;
    this.myUndefinedInfer = undefined;

    /** @type {null} */
    this.myNullDef = null;
    this.myNullInfer = null;

    /** @type {object} */
    this.myObjectDef = null;
    this.myObjectInfer = {};

    /** @type {*} */
    this.myAnyDef = null;
    this.myAnyInfer = null;

    /** @type {WildcardClassAlias} */
    this.myRefDef = null;
    this.myRefInfer = new AnotherClass();
    this.myRefInfer = new WildcardClassAlias();
    this.myRefInfer = new AnotherClass();

    /**
     *  @type {string[][]}
     */
    this.myStringArrayDef = null;
    this.myStringArrayInfer = [[]];
    this.myStringArrayInfer = [['string']];
    this.myStringArrayInfer = null;

    /**
     * @type {number[]}
     */
    this.myNumberArrayDef = null;
    this.myNumberArrayInfer = [42];

    this.myArrayInfer = [];

    /**
     *  @type {string | number}
     */
    this.myStringNumberUnionDef = null;
    /**
     *  @type {(string | number)[]}
     */
    this.myStringNumberUnionArrayDef = null;
    this.myStringNumberUnionArrayInfer = [42, 'string', 13];
    /**
     *  @type {string[] | number}
     */
    this.myStringArrayNumberUnionDef = null;
    /**
     *  @type {AB & BC}
     */
    this.myAbBcIntersectionDef = null;
    /**
     *  @type {(AB & BC)[]}
     */
    this.myAbBcIntersectionArrayDef = null;
    /**
     *  @type {AB & BC[]}
     */
    this.myAbBcArrayIntersectionDef = null;
  }

  /**
   * @type {number}
   */
  myNumberDef = null;
  myNumberInfer = 42;

  myBooleanInfer = null;

  myStaticStringRefInfer = SomeClass.myStaticStringDef;
  myEnumRefInfer = SomeClass.myEnumInfer.b;

  /**
   * @type {string}
   */
  static myStaticStringDef = 'staticString';
  static myStaticStringInfer = 'staticString';

  static myEnumInfer = {
    a: 1,
    b: 2,
    c: 3
  };

  someFunc() {
    return 3;
  }
}