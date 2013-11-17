/**
 *  email_composer.js
 *  Cordova EmailComposer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
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