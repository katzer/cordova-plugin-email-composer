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
     */
    open: function (options) {
      var callbackFn = null;

      	if (typeof options['callback'] == 'function'){
      		callbackFn = function (code) {
      			options.callback.call(options.scope || window, code);
      		};
      	};

        cordova.exec(callbackFn, null, 'EmailComposer', 'open', [options]);
    }
};

var plugin = new EmailComposer();

module.exports = plugin;