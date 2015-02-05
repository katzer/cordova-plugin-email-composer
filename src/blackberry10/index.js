/*
* Copyright (c) 2013 BlackBerry Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

var emailComposer,
	resultObjs = {},
	threadCallback = null,
   _utils = require("../../lib/utils");

module.exports = {

	// Code can be declared and used outside the module.exports object,
	// but any functions to be called by client.js need to be declared
	// here in this object.

	// These methods call into JNEXT.EmailComposer which handles the
	// communication through the JNEXT plugin to emailcomposer_js.cpp
	isAvailable: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		result.ok(emailComposer.getInstance().emailComposerIsAvailable(), false);
	},
	open: function(success, fail, args, env) {
		var result = new PluginResult(args, env);
		var options = JSON.parse(decodeURIComponent(args[0]));
		console.log(options);
		result.ok(emailComposer.getInstance().emailComposerOpen(result.callbackId, options), false);
	}
};

///////////////////////////////////////////////////////////////////
// JavaScript wrapper for JNEXT plugin for connection
///////////////////////////////////////////////////////////////////

JNEXT.EmailComposer = function () {
	var self = this,
		hasInstance = false;

	self.getId = function () {
		return self.m_id;
	};

	self.init = function () {
		if (!JNEXT.require("libEmailComposer")) {
			return false;
		}

		self.m_id = JNEXT.createObject("libEmailComposer.EmailComposer_JS");

		if (self.m_id === "") {
			return false;
		}

		JNEXT.registerEvents(self);
	};

	// ************************
	// Enter your methods here
	// ************************

	// calls into InvokeMethod(string command) in emailcomposer_js.cpp
	self.emailComposerIsAvailable = function() {
		var result = JNEXT.invoke(self.m_id, "emailComposerIsAvailable");
		return (result === "true");
	};
	self.emailComposerOpen = function(callbackId, options) {
		var result = JNEXT.invoke(self.m_id, "emailComposerOpen " + callbackId + " " + JSON.stringify(options));
		console.log('result was: ' + result);
		return result;
	};
	// Fired by the Event framework (used by asynchronous callbacks)
	self.onEvent = function (strData) {
		var arData = strData.split(" "),
			callbackId = arData[0],
			result = resultObjs[callbackId],
			data = arData.slice(1, arData.length).join(" ");

		if (result) {
			if (callbackId != threadCallback) {
				result.callbackOk(data, false);
				delete resultObjs[callbackId];
			} else {
				result.callbackOk(data, true);
			}
		}
	};
	// ************************
	// End of methods to edit
	// ************************
	self.m_id = "";

	self.getInstance = function () {
		if (!hasInstance) {
			hasInstance = true;
			self.init();
		}
		return self;
	};

};

emailComposer = new JNEXT.EmailComposer();
