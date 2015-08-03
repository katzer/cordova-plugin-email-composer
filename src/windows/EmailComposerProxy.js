/* globals Windows: true */

/*
    Copyright 2013-2015 appPlant UG

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
exports.isAvailable = function (success) {
    success(true,false);
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
    var props = args[0];

    if (!(Windows.ApplicationModel.Email==undefined)) {
        var email = exports.draftUtil.getDraftWithProperties(props);

        Windows.ApplicationModel.Email.EmailManager
            .showComposeNewEmailAsync(email)
            .done(function () {
                if (Debug.debuggerEnabled) {
                    success();
                    console.log('degugmode');
                } else {
                    Windows.UI.WebUI.WebUIApplication.addEventListener("resuming", function () {
                        success();
                    }, false);
                }
                
            });
    } else {
        var mailTo = exports.draftUtil.getMailTo(props);
        Windows.System.Launcher.launchUriAsync(mailTo).then(
           function (mailToSuccess) {
               if (mailToSuccess) {
                   success();
               }
           });
    }
};

require('cordova/exec/proxy').add('EmailComposer', exports);