/*
    Copyright 2013-2014 appPlant UG

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

var EmailComposer = function () {

};

EmailComposer.prototype = {
    /**
     * Öffnet den Email-Kontroller mit vorausgefüllten Daten.
     *
     * @param {Object?} options
     */
    open: function (options) {
        var callbackFn = null,
            options    = options || {};

        var defaults = {
            subject:     null,
            body:        null,
            to:          null,
            cc:          null,
            bcc:         null,
            attachments: null,
            isHtml:      true
        }

        for (var key in defaults) {
            if (options[key] !== undefined) {
                defaults[key] = options[key];
            }
        }

        cordova.exec(null, null, 'EmailComposer', 'open', [options]);
    },

    /**
     * Alias für `open()`.
     */
    openDraft: function () {
        this.open.apply(this, arguments);
    },

    /**
     * Gibt an, ob Emails versendet werden können.
     *
     * @param {Function} callback
     * @param {Object?}  scope (default: window)
     */
    isServiceAvailable: function (callback, scope) {
        var callbackFn = function () {
            callback.apply(scope || window, arguments);
        };

        cordova.exec(callbackFn, null, 'EmailComposer', 'isServiceAvailable', []);
    }

};

var plugin = new EmailComposer();

module.exports = plugin;