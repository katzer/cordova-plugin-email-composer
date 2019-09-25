/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
        app.pluginInitialize();
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement    = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement  = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },
    // Initialize plugin
    pluginInitialize: function() {
        document.getElementById('granted').onclick      = app.hasPermission;
        document.getElementById('request').onclick      = app.askPermission;
        document.getElementById('avail').onclick        = app.hasMailAccount;
        document.getElementById('installed').onclick    = app.hasMailClient;
        document.getElementById('open').onclick         = app.openMailClient;
        document.getElementById('gm-installed').onclick = app.hasGmailClient;
        document.getElementById('gm-open').onclick      = app.openGmailClient;
        document.getElementById('plain').onclick        = app.openPlainDraft;
        document.getElementById('html').onclick         = app.openRichDraft;
        document.getElementById('assets').onclick       = app.openDraftWithAssets;
        document.getElementById('camera').onclick       = app.openDraftWithImage;
    },
    // Check permissions to read accounts
    hasPermission: function () {
        plugin().hasPermission(plugin().permission.READ_EXTERNAL_STORAGE, showToast);
    },
    // Request permissions to read accounts
    askPermission: function () {
        plugin().requestPermission(plugin().permission.READ_EXTERNAL_STORAGE, showToast);
    },
    // Check if mail account exist
    hasMailAccount: function () {
        plugin().requestPermission(plugin().permission.READ_ACCOUNTS, function (granted) {
            plugin().hasAccount(showToast);
        });
    },
    // Check if mail client exist
    hasMailClient: function () {
        plugin().hasClient(showToast);
    },
    // Open mail client
    openMailClient: function () {
       plugin().open(showToast);
    },
    // Check if gmail client exist
    hasGmailClient: function () {
        plugin().hasClient('gmail', showToast);
    },
    // Open gmail client
    openGmailClient: function () {
       plugin().open({ app: 'gmail' }, showToast);
    },
    // Open draft with plain body
    openPlainDraft: function () {
        app.openDraft(false);
    },
    // Open draft with HTML body
    openRichDraft: function () {
        app.openDraft(true);
    },
    // Open draft
    openDraft: function (isHtml) {
        plugin().open({
            to:      'to@email.de',
            cc:      ['cc1@email.de', 'cc2@email.de'],
            bcc:     ['bcc1@email.de', 'bcc2@email.de'],
            subject: isHtml ? 'Body with HTML and CSS3' : 'Body with plain text',
            body:    app.getBody(isHtml),
            isHtml:  isHtml
        }, showToast);
    },
    // Open draft with attachments
    openDraftWithAssets: function () {
        var attachments = [
            'file://img/logo.png',
            (device.platform === 'windows') ? 'res://Square310x310Logo.scale-100.png' : 'res://icon.png',
            'base64:icon2.png//iVBORw0KGgoAAAANSUhEUgAAAHIAAAByCAIAAAAAvxIqAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDpEQUYxOTdDOThCMjE2ODExQTRBM0Q5OTE3QTBBN0U3MCIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDoyNUVFMTA4NDM1N0ExMUUxQjRCNDlCQ0U2Qzg1RThFOSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDoyNUVFMTA4MzM1N0ExMUUxQjRCNDlCQ0U2Qzg1RThFOSIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M1LjEgTWFjaW50b3NoIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MkNGMkFDMDYzNTIyNjgxMUE0QTNEOTkxN0EwQTdFNzAiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6REFGMTk3Qzk4QjIxNjgxMUE0QTNEOTkxN0EwQTdFNzAiLz4gPC9yZGY6RGV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',
            'file://README.pdf'
        ];

        var mapFn = function (uri, index) {
            return ++index + '. ' + uri.substring(0, 40);
        };

        plugin().open({
            subject:       'Cordova Icons',
            body:          attachments.map(mapFn).join("\n\n"),
            attachments:   attachments,
            isHtml:        false
        }, showToast);
    },
    // Open draft with a picture taken from cam
    openDraftWithImage: function (e, source) {
        var options = {
            destinationType: Camera.DestinationType.FILE_URI,
            sourceType: source === undefined ? Camera.PictureSourceType.CAMERA : source
        };

        var failure = function () {
            if (source === undefined)
                chooseAttachment(e, Camera.PictureSourceType.PHOTOLIBRARY);
        };

        navigator.camera.getPicture(function(uri) {
            var fn = function () {
                    plugin().open({
                        subject: 'Attachment from file system',
                        body: uri,
                        attachments: uri
                    }, showToast);
                };

            setTimeout(fn, 500);
       }, failure, options);
    },
    // Tiny sweet helper which returns a HTML body
    getBody: function (isHtml) {
        var css = [
            '<style>',
                '.ribbon p {',
                    'color: white;',
                    'font-family: Helvetica;',
                    'font-size: 17px;',
                    'font-weight: bold;',
                    'line-height: 60px;',
                    'text-align: center;',
                    'text-shadow: 0 2px 0 #83ac17;',
                    'width: 100%;',
                '}',
                '#ribbon {',
                    'top: 15px;',
                    'position: relative;',
                    'display: block;',
                    'margin: 0 auto;',
                    'height: 60px;',
                    'width: 250px;',
                    'background-color: #a5cc39;',
                '}',
                '.ribbon:before, .ribbon:after {',
                    'content: " ";',
                    'height: 0px;',
                    'position: absolute;',
                    'top:10px;',
                    'z-index: -1;',
                    'border-bottom: 30px solid #94bd28;',
                    'border-top: 30px solid #94bd28;',
                '}',
                '.ribbon:before {',
                    'border-left: 15px solid transparent;',
                    'border-right: 15px solid #94bd28;',
                    'left:-20px;',
                '}',
                '.ribbon:after {',
                    'border-left: 15px solid #94bd28;',
                    'border-right: 15px solid transparent;',
                    'right: -20px;',
                '}',
                '.sub-curls {',
                    'position: absolute;',
                    'bottom: -15px;',
                    'height: 15px;',
                    'width: 100%;',
                '}',
                '.sub-curls:before {',
                    'content: " ";',
                    'position: absolute;',
                    'left: 0;',
                    'border-left: 10px solid transparent;',
                    'border-top: 10px solid #729b06;',
                '}',
                '.sub-curls:after {',
                    'content: " ";',
                    'position: absolute;',
                    'right: 0;',
                    'border-right: 10px solid transparent;',
                    'border-top: 10px solid #729b06;',
                '}',
            '</style>'
        ];

        var html = [
            '<div id="ribbon" class="ribbon">',
                '<p><b>Simple Ribbon with CSS3</b></p>',
                '<div class="sub-curls"></div>',
            '</div>'
        ];

        if (!isHtml) {
            return ['Hello', 'this is just a plain text email body!'];
        }

        return css.concat(html);
   }
};

plugin = function() { return cordova.plugins.email; };

var dialog;

showToast = function (text) {
    var isMac = navigator.userAgent.toLowerCase().includes('macintosh');
        text  = text !== null ? text : 'finished or canceled';

    setTimeout(function () {
        if (window.Windows !== undefined) {
            showWinDialog(text);
        } else
        if (!isMac && window.plugins && window.plugins.toast) {
            window.plugins.toast.showShortBottom(String(text));
        }
        else {
            alert(text);
        }
    }, 500);
};

showWinDialog = function (text) {
    if (dialog) {
        dialog.content = text;
        return;
    }

    dialog = new Windows.UI.Popups.MessageDialog(text);

    dialog.showAsync().done(function () {
        dialog = null;
    });
};

if (window.hasOwnProperty('Windows')) {
    alert = showWinDialog;
}

app.initialize();
