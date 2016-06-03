/*
    Copyright 2013-2016 appPlant UG

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
    isAndroid = navigator.userAgent.toLowerCase().indexOf('android') > -1,
    mailto    = 'mailto:';

/**
 * List of all registered mail app aliases.
 */
exports.aliases = {
    gmail: isAndroid ? 'com.google.android.gm' : 'googlemail:'
};

/**
 * List of all available options with their default value.
 *
 * @return {Object}
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
 * Verifies if sending emails is supported on the device.
 *
 * @param {String?} app
 *      An optional app id or uri scheme. Defaults to mailto.
 * @param {Function} callback
 *      A callback function to be called with the result
 * @param {Object} scope
 *      The scope of the callback
 */
exports.isAvailable = function (app, callback, scope) {

    if (typeof callback != 'function'){
        scope    = null;
        callback = app;
        app      = mailto;
    }

    app = app || mailto;

    if (this.aliases.hasOwnProperty(app)){
        app = this.aliases[app];
    }

    var fn = this.createCallbackFn(callback, scope);

    exec(fn, null, 'EmailComposer', 'isAvailable', [app]);
};

/**
 * Displays the email composer pre-filled with data.
 *
 * @param {Object} options
 *      Different properties of the email like the body, subject
 * @param {Function} callback
 *      A callback function to be called with the result
 * @param {Object?} scope
 *      The scope of the callback
 */
exports.open = function (options, callback, scope) {
    var fn = this.createCallbackFn(callback, scope),
        me = this;

    options = this.mergeWithDefaults(options || {});

    var onAvailable = function (isPossible, withScheme) {

        if (!isPossible)
            return fn();

        if (!withScheme) {
            if (window.console) { console.log('Cannot open app'); }
            options.app = mailto;
        }

        if (!isAndroid && options.app != mailto) {
            me.registerCallbackForScheme(fn);
        }

        exec(fn, null, 'EmailComposer', 'open', [options]);
    };

    exec(onAvailable, null, 'EmailComposer', 'isAvailable', [options.app]);
};

/**
 * Adds a new mail app alias.
 *
 * @param {String} alias
 *      The alias name
 * @param {String} package
 *      The package name
 */
exports.addAlias = function (alias, package) {
    this.aliases[alias] = package;
};

/**
 * @depreacted
 */
exports.isServiceAvailable = function () {
    console.log('`email.isServiceAvailable` is deprecated.' +
                ' Please use `email.isAvailable` instead.');

    this.isAvailable.apply(this, arguments);
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
 * @param {Object} options
 *      The custom options
 *
 * @retrun {Object}
 *      Default values merged
 *      with custom values
 */
exports.mergeWithDefaults = function (options) {
    var defaults = this.getDefaults();

    if (options.hasOwnProperty('isHTML')) {
        options.isHtml = options.isHTML;
    }

    if (options.hasOwnProperty('app')) {
        var package = this.aliases[options.app];

        options.app = package || options.app;
    }

    for (var key in defaults) {

        if (!options.hasOwnProperty(key)) {
            options[key] = defaults[key];
            continue;
        }

        var custom_  = options[key],
            default_ = defaults[key];

        if (custom_ === null || custom_ === undefined) {
            options[key] = default_;
            continue;
        }

        if (typeof default_ != typeof custom_) {

            if (typeof default_ == 'string') {
                options[key] = custom_.join('');
            }

            else if (typeof default_ == 'object') {
                options[key] = [custom_.toString()];
            }
        }
    }

    return options;
};

/**
 * @private
 *
 * Creates a callback, which will be executed
 * within a specific scope.
 *
 * @param {Function} callbackFn
 *      The callback function
 * @param {Object} scope
 *      The scope for the function
 *
 * @return {Function}
 *      The new callback function
 */
exports.createCallbackFn = function (callbackFn, scope) {

    if (typeof callbackFn != 'function')
        return;

    return function () {
        callbackFn.apply(scope || this, arguments);
    };
};

/**
 * @private
 *
 * Register an Eventlistener on resume-Event to
 * execute callback after open a draft.
 */
exports.registerCallbackForScheme = function(fn) {

    var callback = function () {
        fn();
        document.removeEventListener('resume',callback);
    };

    document.addEventListener('resume', callback, false);
};
