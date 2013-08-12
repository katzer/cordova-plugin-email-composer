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
    send: function (options) {
        cordova.exec(null, null, 'EmailComposer', 'send', [options]);
    }
};

var plugin = new EmailComposer();

module.exports = plugin;