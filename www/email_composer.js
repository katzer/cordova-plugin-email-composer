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

      	if (typeof options['callback'] == 'function'){
      		callbackFn = function (code) {
      			options.callback.call(options.scope || window, code);
      		};
      	};

        cordova.exec(callbackFn, null, 'EmailComposer', 'open', [options]);
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