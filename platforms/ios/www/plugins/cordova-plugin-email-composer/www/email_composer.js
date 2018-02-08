cordova.define("cordova-plugin-email-composer.EmailComposer", function(require, exports, module) {
/*
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

var exec      = require('cordova/exec'),
    ua        = navigator.userAgent.toLowerCase(),
    isAndroid = !window.Windows && ua.indexOf('android') > -1,
    mailto    = 'mailto:';

/**
 * List of all registered mail app aliases.
 */
exports.aliases = {
    gmail:   isAndroid ? 'com.google.android.gm' : 'googlegmail://co',
    outlook: isAndroid ? 'com.microsoft.office.outlook' : 'ms-outlook://compose'
};

/**
 * List of all available options with their default value.
 *
 * @return [ Object ]
 */
exports.getDefaults = function () {
    return {
        app:           mailto,
        subject:       '',
        body:          '',
        to:            [],
        cc:            [],
        bcc:           [],
        attachments:   [],
        isHtml:        true,
        chooserHeader: 'Open with'
    };
};

/**
 * Informs if the app has the needed permission.
 *
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope of the callback.
 *
 * @return [ Void ]
 */
exports.hasPermission = function(callback, scope) {
    var fn = this.createCallbackFn(callback, scope);

    if (!isAndroid) {
        if (fn) fn(true);
        return;
    }

    exec(fn, null, 'EmailComposer','hasPermission', []);
 };

/**
 * Request permission if not already granted.
 *
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope of the callback.
 *
 * @return [ Void ]
 */
exports.requestPermission = function(callback, scope) {
    var fn = this.createCallbackFn(callback, scope);

    if (!isAndroid) {
        if (fn) fn(true);
        return;
    }

    exec(fn, null, 'EmailComposer','requestPermission', []);
};

/**
 * Verifies if sending emails is supported on the device.
 *
 * @param [ String ]   app      An optional app id or uri scheme.
 *                              Defaults to mailto.
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope of the callback.
 *
 * @return [ Void ]
 */
exports.isAvailable = function (app, callback, scope) {

    if (typeof callback != 'function') {
        scope    = null;
        callback = app;
        app      = mailto;
    }

    var fn  = this.createCallbackFn(callback, scope);
        app = app || mailto;

    if (this.aliases.hasOwnProperty(app)) {
        app = this.aliases[app];
    }

    exec(fn, null, 'EmailComposer', 'isAvailable', [app]);
};

/**
 * Verifies if sending emails is supported on the device.
 *
 * @param [ String ]   app      An optional app id or uri scheme.
 *                              Defaults to mailto.
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope of the callback.
 *
 * @return [ Void ]
 */
exports.isAvailable2 = function (app, callback, scope) {

    if (typeof callback != 'function') {
        scope    = null;
        callback = app;
        app      = mailto;
    }

    var fn  = this.createCallbackFn(callback, scope), fn2;
        app = app || mailto;

    if (this.aliases.hasOwnProperty(app)) {
        app = this.aliases[app];
    }

    if (fn) {
        fn2 = function (a, b) { fn(b, a); };
    }

    exec(fn2, null, 'EmailComposer', 'isAvailable', [app]);
};

/**
 * Displays the email composer pre-filled with data.
 *
 * @param [ Object ]   options  The email properties like the body,...
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope of the callback.
 *
 * @return [ Void ]
 */
exports.open = function (options, callback, scope) {

    if (typeof options == 'function') {
        scope    = callback;
        callback = options;
        options  = {};
    }

    var fn      = this.createCallbackFn(callback, scope);
        options = this.mergeWithDefaults(options || {});

    if (!isAndroid && options.app != mailto && fn) {
        this.registerCallbackForScheme(fn);
    }

    exec(fn, null, 'EmailComposer', 'open', [options]);
};

/**
 * Adds a new mail app alias.
 *
 * @param [ String ] alias   The alias name.
 * @param [ String ] package The package name.
 *
 * @return [ Void ]
 */
exports.addAlias = function (alias, package) {
    this.aliases[alias] = package;
};

/**
 * Alias für `open()`.
 */
exports.openDraft = function () {
    this.open.apply(this, arguments);
};

/**
 * @private
 *
 * Merge settings with default values.
 *
 * @param [ Object ] options The custom options
 *
 * @retrun [ Object ] Default values merged with custom values.
 */
exports.mergeWithDefaults = function (options) {
    var defaults = this.getDefaults();

    if (options.hasOwnProperty('isHTML')) {
        options.isHtml = options.isHTML;
    }

    if (!options.hasOwnProperty('isHtml')) {
        options.isHtml = defaults.isHtml;
    }

    if (options.hasOwnProperty('app')) {
        options.app = this.aliases[options.app];
    }

    options.app           = String(options.app || defaults.app);
    options.subject       = String(options.subject || defaults.subject);
    options.body          = String(options.body || defaults.body);
    options.chooserHeader = String(options.chooserHeader || defaults.chooserHeader);
    options.to            = options.to || defaults.to;
    options.cc            = options.cc || defaults.cc;
    options.bcc           = options.bcc || defaults.bcc;
    options.attachments   = options.attachments || defaults.attachments;
    options.isHtml        = !!options.isHtml;

    if (!Array.isArray(options.to)) {
        options.to = [options.to];
    }

    if (!Array.isArray(options.cc)) {
        options.cc = [options.cc];
    }

    if (!Array.isArray(options.bcc)) {
        options.bcc = [options.bcc];
    }

    if (!Array.isArray(options.attachments)) {
        options.attachments = [options.attachments];
    }

    return options;
};

/**
 * @private
 *
 * Creates a callback, which will be executed
 * within a specific scope.
 *
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope for the function.
 *
 * @return [ Function ] The new callback function
 */
exports.createCallbackFn = function (callback, scope) {

    if (typeof callback !== 'function')
        return;

    return function () {
        callback.apply(scope || this, arguments);
    };
};

/**
 * @private
 *
 * Register an Eventlistener on resume-Event to
 * execute callback after open a draft.
 *
 * @return [ Void ]
 */
exports.registerCallbackForScheme = function (fn) {

    var callback = function () {
        fn();
        document.removeEventListener('resume',callback);
    };

    document.addEventListener('resume', callback, false);
};

});
