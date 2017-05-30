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

var proxy = require('de.appplant.cordova.plugin.email-composer.EmailComposerProxy');

/**
 *
 * @type {{_toPlainText: Function, _isHtml: Function, getFullContentType: Function, getContentType: Function, getMailToUri: Function, supportsEMLUri: Function, getEMLUri: Function, getEMLContent: Function}}
 */
	proxy.commonUtil = {

    /**
     * convert given markup to plain text.
     *
     * @param {String} markupText
     * @return {String} plain text
     */
    _toPlainText: function(markupText){
        if (!markupText)
            return null;

        function replaceBodyTag(markupText, tagName, replacement)
        {
            markupText = markupText.replace(new RegExp("<" + tagName
                    + " ?[^>]*>", "g"), "");
            markupText = markupText.replace(new RegExp("</" + tagName + ">",
                    "g"), replacement);
            return markupText;
        }

        function replaceBrTag(markupText)
        {
            return markupText.replace(/\s*<br>\s*/g, "\n");
        }

        function replaceAnchorTag(markupText)
        {
            return markupText.replace(/<a [^>]*href="(.*)"[^>]*> *(.*) *<\/a>/g,
                    function (all, href, content)
                    {
                        // space after url makes url clickable even if its
                        // placed at the end of a line.
                        // mark url for post processing with surroundLinks()
                        return content + " _link_start_" + href + "_link_end_ ";
                    });
        }

        function surroundLinks(markupText)
        {
            return markupText.replace(/_link_start_(.*)_link_end_/g,
                    function (all, url)
                    {
                        // brackets around the url prevent line breaks in long
                        // urls in many email clients.
                        // see http://www.ietf.org/rfc/rfc2396.txt
                        // Section E., "Recommendations for Delimiting URI in Context".
                        return "<" + url + ">";
                    });
        }

        markupText = markupText.replace(/\s*\n\s*/g, " ");
        markupText = markupText.replace(/>\s*</g, "><");

        markupText = replaceAnchorTag(markupText);
        markupText = replaceBrTag(markupText);

        markupText = replaceBodyTag(markupText, "h1", "\n\n\n");
        markupText = replaceBodyTag(markupText, "h2", "\n\n");
        markupText = replaceBodyTag(markupText, "p", "\n\n");

        if($)
        {
            // use jquery to expand all entities and remove all tags
            markupText = $('<span>').html(markupText).text();
        }

        return surroundLinks(markupText);
    },

    _isHtml: function (props) {
        return (props.isHtml == true || props.isHtml == 'true');
    },

    getFullContentType: function (props) {
        // tell outlook (or others) how to decode the url parameters (UTF-8)
        // and that we want to create a plain text or html email draft
        return this.getContentType(props) + '; charset=utf-8';
    },

    getContentType: function (props) {
        // tell outlook (or others) how to decode the url parameters (UTF-8)
        // and that we want to create a plain text or html email draft

        if (this._isHtml(props))
            return 'text/html';
        else
            return 'text/plain';
    },

    getMailToUri: function (props) {

        function appendParam(name, value)
        {
            if (value == null || value == '')
                return '';
            value = ([].concat(value)).join(",");
            if (value == '')
                return '';

            return '&' + name + "=" + encodeURIComponent(value);
        }

        var isHtml = this._isHtml(props);

        // mailto links don't support html body
        if(isHtml)
        {
            // TODO: clone props to avoid modification of function call argument
            props.body = this._toPlainText(props.body);
            props.isHtml = false;
        }

        var fullContentType = this.getFullContentType(props);
        var contentType = this.getContentType(props);
        // The URI to launch
        var uriToLaunch = "mailto:" + ([].concat(props.to)).join(",");

        var options = '';

        options = options + appendParam('Content-Type', fullContentType);

        options = options + appendParam('subject', props.subject);
        options = options + appendParam('cc', props.cc);
        options = options + appendParam('bcc', props.bcc);

        // append body as last param, as it may expire the uri max length
        options = options + appendParam('body', props.body);

        // TODO: add attachments

        if (options !== '')
        {
            options = '?' + options.substring(1);
            uriToLaunch = uriToLaunch + options;
        }

        return {
            uri : uriToLaunch,
            contentType : contentType,
            close : function(){}
        };
    },

    supportsEMLUri: function () {
        return (window.URL || window.webkitURL) && Blob ? true : false
    },

    getEMLUri: function (props) {

        var eml = this.getEMLContent(props);

        var URL = window.URL || window.webkitURL;
        var blob = new Blob([eml.text], {type: eml.contentType});
        var url = URL.createObjectURL(blob);

        return {
            uri : url,
            contentType : eml.contentType,
            close : function(){
                URL.revokeObjectURL(url);
                eml.close();
            }
        };
    },
    getEMLContent: function (props) {

        function appendParam(name, value)
        {
            if (value == null || value == '')
                return '';
            value = ([].concat(value)).join(",\n ");
            if (value == '')
                return '';

            return name + ": " + value + "\n";
        }

        var isHtml = this._isHtml(props);
        var fullContentType = this.getFullContentType(props);

        var emlText = '';
        emlText = emlText + appendParam('Content-Type', fullContentType);
        emlText = emlText + appendParam('X-Unsent', "1");
        emlText = emlText + appendParam('Subject', props.subject);
        emlText = emlText + appendParam("To", props.to);
        emlText = emlText + appendParam('Cc', props.cc);
        emlText = emlText + appendParam('Bcc', props.bcc);
        emlText = emlText + appendParam('Content-Type', fullContentType);
        emlText = emlText + "\n";

        if(isHtml)
        {
            emlText = emlText + "<html>\n";
            emlText = emlText + "<body>\n";
            emlText = emlText + props.body + "\n";
            emlText = emlText + "</body>\n";
            emlText = emlText + "</html>";
        }
        else
        {
            emlText = emlText + props.body;
        }

        //TODO: add attachments

        return {
            text : emlText,
            contentType : 'message/rfc822',
            close : function(){}
        };
    }

};

