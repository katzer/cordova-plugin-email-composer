/* globals Windows: true */

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

var WinLauncher = Windows.System.Launcher,
    WinMail     = Windows.ApplicationModel.Email;

/**
 * Verifies if sending emails is supported on the device.
 *
 * @param {Function} success
 *      Success callback function
 * @param {Function} error
 *      Error callback function
 * @param {Array} args
 *      Interface arguments
 */
exports.isAvailable = function (success, error, args) {
    success(true);
};

/**
 * Displays the email composer pre-filled with data.
 *
 * @param {Function} success
 *      Success callback function
 * @param {Function} error
 *      Error callback function
 * @param {Array} args
 *      Interface arguments
 */
exports.open = function (success, error, args) {
    var props = args[0],
        impl  = exports.impl;

    if (WinMail) {
        impl.getDraftWithProperties(props)
            .then(WinMail.EmailManager.showComposeNewEmailAsync)
            .done(success, error);
    } else {
        var mailTo = impl.getMailTo(props);

        WinLauncher
            .launchUriAsync(mailTo)
            .done(success, error);
    }
};

require('cordova/exec/proxy').add('EmailComposer', exports);
