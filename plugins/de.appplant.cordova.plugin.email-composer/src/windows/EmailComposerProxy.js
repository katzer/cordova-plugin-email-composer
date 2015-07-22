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
    var props = args[0];

    try {
        var email = exports.getDraftWithProperties(props);

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
    } catch (e) {
        var mailTo = exports.getMailTo(props);
        Windows.System.Launcher.launchUriAsync(mailTo).then(
           function (mailToSuccess) {
               if (mailToSuccess) {
                   success();
               }
           });
    }
};

/**
 * The Email with the containing properties.
 *
 * @param {Object} props
 *      The email properties like subject or body
 * @return {Windows.ApplicationModel.Email.EmailMessage}
 *      The resulting email draft
 */
exports.getDraftWithProperties = function (props) {
    var mail = new Windows.ApplicationModel.Email.EmailMessage();

    // subject
    exports.setSubject(props.subject, mail);
    // body
    exports.setBody(props.body, props.isHtml, mail);
    // To recipients
    exports.setRecipients(props.to, mail.to);
    // CC recipients
    exports.setRecipients(props.cc, mail.cc);
    // BCC recipients
    exports.setRecipients(props.bcc, mail.bcc);
    // attachments
    exports.setAttachments(props.attachments, mail);

    return mail;
};

exports.getMailTo = function (props) {
    // The URI to launch
    var uriToLaunch = "mailto:" + props.to;

    var options = '';
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
        options = '?' + options.substring(1);
        uriToLaunch = uriToLaunch + options;
    }

    // Create a Uri object from a URI string
    var uri = new Windows.Foundation.Uri(uriToLaunch);

    return uri;
};

/**
 * Setter for the subject.
 *
 * @param {String} subject
 *      The subject
 * @param {Windows.ApplicationModel.Email.EmailMessage} draft
 *      The draft
 */
exports.setSubject = function (subject, draft) {
    draft.subject = subject;
};

/**
 * Setter for the body.
 *
 * @param {String} body
 *      The body
 * @param isHTML
 *      Indicates the encoding
 *      (HTML or plain text)
 * @param {Windows.ApplicationModel.Email.EmailMessage} draft
 *      The draft
 */
exports.setBody = function (body, isHTML, draft) {
    draft.body = body;
};

/**
 * Setter for the recipients.
 *
 * @param {String[]} recipients
 *      List of mail addresses
 * @param {Windows.ApplicationModel.Email.EmailMessage} draftAttribute
 *      The draft.to / *.cc / *.bcc
 */
exports.setRecipients = function (recipients, draftAttribute) {
    recipients.forEach(function (address) {
        draft.push(
            new Windows.ApplicationModel.Email.EmailRecipient(address));
    });
};

/**
 * Setter for the attachments.
 *
 * @param {String[]} attachments
 *      List of URIs
 * @param {Windows.ApplicationModel.Email.EmailMessage} draft
 *      The draft
 */
exports.setAttachments = function (attachments, draft) {
    attachments.forEach(function (path) {
        var uri = exports.getUriForPath(path),
            name = uri.path.split('/').reverse()[0],
            stream = Windows.Storage.Streams.RandomAccessStreamReference
                        .createFromUri(uri);

        draft.attachments.push(
            new Windows.ApplicationModel.Email.
                EmailAttachment(name, stream)
        );
    });
};

/**
 * The URI for an attachment path.
 *
 * @param {String} path
 *      The given path to the attachment
 *
 * @return
 *      The URI pointing to the given path
 */
exports.getUriForPath = function (path) {
    if (path.match(/^res:/)) {
        return exports.getUriForResourcePath(path);
    } else if (path.match(/^file:\/{3}/)) {
        return exports.getUriForAbsolutePath(path);
    } else if (path.match(/^file:/)) {
        return exports.getUriForAssetPath(path);
    } else if (path.match(/^base64:/)) {
        return exports.getUriForBase64Content(path);
    }

    return new Windows.Foundation.Uri(path);
};

/**
 * The URI for a file.
 *
 * @param {String} path
 *      The given absolute path
 *
 * @return
 *      The URI pointing to the given path
 */
exports.getUriForAbsolutePath = function (path) {
    return new Windows.Foundation.Uri(path);
};

/**
 * The URI for an asset.
 *
 * @param {String} path
 *      The given asset path
 *
 * @return
 *      The URI pointing to the given path
 */
exports.getUriForAssetPath = function (path) {
    var resPath = path.replace('file:/', '/www');

    return exports.getUriForPathUtil(resPath);
};

/**
 * The URI for a resource.
 *
 * @param {String} path
 *      The given relative path
 *
 * @return
 *      The URI pointing to the given path
 */
exports.getUriForResourcePath = function (path) {
    var resPath = path.replace('res:/', '/images');

    return exports.getUriForPathUtil(resPath);
};

/**
 * The URI for a path.
 *
 * @param {String} resPath
 *      The given relative path
 *
 * @return
 *      The URI pointing to the given path
 */
exports.getUriForPathUtil = function (resPath) {
    var host     = document.location.host,
        protocol = document.location.protocol,
        rawUri   = protocol + '//' + host + resPath;

    return new Windows.Foundation.Uri(rawUri);
};

/**
 * The URI for a base64 encoded content.
 *
 * @param {String} content
 *      The given base64 encoded content
 *
 * @return
 *      The URI including the given content
 */
exports.getUriForBase64Content = function (content) {
    var match = content.match(/^base64:([^\/]+)\/\/(.*)/),
        base64 = match[2],
        name = match[1],
        buffer = Windows.Security.Cryptography.CryptographicBuffer.decodeFromBase64String(base64),
        rwplus = Windows.Storage.CreationCollisionOption.openIfExists,
        folder = Windows.Storage.ApplicationData.current.temporaryFolder,
        uri    = new Windows.Foundation.Uri('ms-appdata:///temp/' + name);

    folder.createFileAsync(name, rwplus).done(function (file) {
        Windows.Storage.FileIO.writeBufferAsync(file, buffer);
    });

    return uri;
};

require('cordova/exec/proxy').add('EmailComposer', exports);
