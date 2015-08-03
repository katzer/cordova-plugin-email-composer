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

	proxy.draftUtil = {
	
		/**
		 * The Email with the containing properties.
		 *
		 * @param {Object} props
		 *      The email properties like subject or body
		 * @return {Windows.ApplicationModel.Email.EmailMessage}
		 *      The resulting email draft
		 */
		getDraftWithProperties: function (props) {
			var mail = new Windows.ApplicationModel.Email.EmailMessage();

			// subject
			this.setSubject(props.subject, mail);
			// body
			this.setBody(props.body, props.isHtml, mail);
			// To recipients
			this.setRecipients(props.to, mail.to);
			// CC recipients
			this.setRecipients(props.cc, mail.cc);
			// BCC recipients
			this.setRecipients(props.bcc, mail.bcc);
			// attachments
			this.setAttachments(props.attachments, mail);

			return mail;
		},

	getMailTo: function (props) {
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
	},

	/**
	 * Setter for the subject.
	 *
	 * @param {String} subject
	 *      The subject
	 * @param {Windows.ApplicationModel.Email.EmailMessage} draft
	 *      The draft
	 */
	setSubject: function (subject, draft) {
		draft.subject = subject;
	},

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
	setBody: function (body, isHTML, draft) {
		draft.body = body;
	},

	/**
	 * Setter for the recipients.
	 *
	 * @param {String[]} recipients
	 *      List of mail addresses
	 * @param {Windows.ApplicationModel.Email.EmailMessage} draftAttribute
	 *      The draft.to / *.cc / *.bcc
	 */
	setRecipients: function (recipients, draftAttribute) {
		recipients.forEach(function (address) {
			draft.push(
				new Windows.ApplicationModel.Email.EmailRecipient(address));
		});
	},

	/**
	 * Setter for the attachments.
	 *
	 * @param {String[]} attachments
	 *      List of URIs
	 * @param {Windows.ApplicationModel.Email.EmailMessage} draft
	 *      The draft
	 */
	setAttachments: function (attachments, draft) {
		attachments.forEach(function (path) {
			var uri = proxy.attachmentUtil.getUriForPath(path),
				name = uri.path.split('/').reverse()[0],
				stream = Windows.Storage.Streams.RandomAccessStreamReference
							.createFromUri(uri);

			draft.attachments.push(
				new Windows.ApplicationModel.Email.
					EmailAttachment(name, stream)
			);
		});
	}
};

proxy.attachmentUtil = {

	/**
	 * The URI for an attachment path.
	 *
	 * @param {String} path
	 *      The given path to the attachment
	 *
	 * @return
	 *      The URI pointing to the given path
	 */
	getUriForPath: function (path) {
		if (path.match(/^res:/)) {
			return this.getUriForResourcePath(path);
		} else if (path.match(/^file:\/{3}/)) {
			return this.getUriForAbsolutePath(path);
		} else if (path.match(/^file:/)) {
			return this.getUriForAssetPath(path);
		} else if (path.match(/^base64:/)) {
			return this.getUriForBase64Content(path);
		}

		return new Windows.Foundation.Uri(path);
	},

	/**
	 * The URI for a file.
	 *
	 * @param {String} path
	 *      The given absolute path
	 *
	 * @return
	 *      The URI pointing to the given path
	 */
	getUriForAbsolutePath: function (path) {
		return new Windows.Foundation.Uri(path);
	},

	/**
	 * The URI for an asset.
	 *
	 * @param {String} path
	 *      The given asset path
	 *
	 * @return
	 *      The URI pointing to the given path
	 */
	getUriForAssetPath: function (path) {
		var resPath = path.replace('file:/', '/www');

		return this.getUriForPathUtil(resPath);
	},

	/**
	 * The URI for a resource.
	 *
	 * @param {String} path
	 *      The given relative path
	 *
	 * @return
	 *      The URI pointing to the given path
	 */
	getUriForResourcePath: function (path) {
		var resPath = path.replace('res:/', '/images');

		return this.getUriForPathUtil(resPath);
	},

	/**
	 * The URI for a path.
	 *
	 * @param {String} resPath
	 *      The given relative path
	 *
	 * @return
	 *      The URI pointing to the given path
	 */
	getUriForPathUtil: function (resPath) {
		var host     = document.location.host,
			protocol = document.location.protocol,
			rawUri   = protocol + '//' + host + resPath;

		return new Windows.Foundation.Uri(rawUri);
	},

	/**
	 * The URI for a base64 encoded content.
	 *
	 * @param {String} content
	 *      The given base64 encoded content
	 *
	 * @return
	 *      The URI including the given content
	 */
	getUriForBase64Content: function (content) {
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
	}


};
