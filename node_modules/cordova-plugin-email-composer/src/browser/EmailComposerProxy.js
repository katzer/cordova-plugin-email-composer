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

/**
 * Verifies if sending emails is supported on the device.
 *
 * @param [ Function ] success Success callback
 * @param [ Function ] error   Error callback
 * @param [ Array ]    args    Interface arguments
 *
 * @return [ Void ]
 */
exports.isAvailable = function (success, error, args) {
    success(true, false);
};

/**
 * Displays the email composer pre-filled with data.
 *
 * @param [ Function ] success Success callback
 * @param [ Function ] error   Error callback
 * @param [ Array ]    args    Interface arguments
 *
 * @return [ Void ]
 */
exports.open = function (success, error, args) {
    var props   = args[0],
        mailto  = 'mailto:' + props.to,
        options = '';

    if (props.subject !== '') {
        options = options + '&subject=' + props.subject;
    }

    if (props.body !== '') {
        options = options + '&body=' + props.body;
    }

    if (props.cc !== '') {
        options = options + '&cc=' + props.cc;
    }

    if (props.bcc !== '') {
        options = options + '&bcc=' + props.bcc;
    }

    if (options !== '') {
        mailto = mailto + '?' + options.substring(1);
    }

    window.location.href = mailto;

    success();
};

require('cordova/exec/proxy').add('EmailComposer', exports);
