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
		console.log(">>> Entrou!!!")
		var result = new PluginResult(args, env);
		result.ok(emailComposer.getInstance().emailComposerIsAvailable(), false); // undefined
	},
	emailComposerTest: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		result.ok(emailComposer.getInstance().emailComposerTest(), false);
	},
	emailComposerTestInput: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		args = JSON.parse(decodeURIComponent(args["input"]));
		result.ok(emailComposer.getInstance().emailComposerTestInput(result.callbackId, args), false);
	},
	// Asynchronous function calls into the plugin and returns
	emailComposerTestAsync: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		resultObjs[result.callbackId] = result;
		args = JSON.parse(decodeURIComponent(args["input"]));
		emailComposer.getInstance().emailComposerTestAsync(result.callbackId, args);
		result.noResult(true);
	},
	emailComposerProperty: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		var value;
		if (args && args["value"]) {
			value = JSON.parse(decodeURIComponent(args["value"]));
			emailComposer.getInstance().emailComposerProperty(result.callbackId, value);
			result.noResult(false);
		} else {
			result.ok(emailComposer.getInstance().emailComposerProperty(), false);
		}
	},
	// Thread methods to start and stop
	emailComposerStartThread: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		if (!threadCallback) {
			threadCallback = result.callbackId;
			resultObjs[result.callbackId] = result;
			result.ok(emailComposer.getInstance().emailComposerStartThread(result.callbackId), true);
		} else {
			result.error(emailComposer.getInstance().emailComposerStartThread(result.callbackId), false);
		}
	},
	emailComposerStopThread: function (success, fail, args, env) {
		var result = new PluginResult(args, env);
		if (!threadCallback) {
			result.error("Thread is not running", false);
		} else {
			delete resultObjs[threadCallback];
			threadCallback = null;
			result.ok(emailComposer.getInstance().emailComposerStopThread(), false);
		}
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
		var result = JNEXT.invoke(self.m_id, "isAvailable");
		if (result === "true") {
			return true;
		} else {
			return false;
		}
		// return result;
	};
	self.emailComposerTest = function () {
		return JNEXT.invoke(self.m_id, "emailComposerTest");
	};
	self.emailComposerTestInput = function (callbackId, input) {
		return JNEXT.invoke(self.m_id, "emailComposerTestInput " + callbackId + " " + input);
	};
	self.emailComposerTestAsync = function (callbackId, input) {
		return JNEXT.invoke(self.m_id, "emailComposerTestAsync " + callbackId + " " + JSON.stringify(input));
	};
	self.emailComposerProperty = function (callbackId, value) {
		if (value) {
			return JNEXT.invoke(self.m_id, "emailComposerProperty " + callbackId + " " + value);
		} else {
			return JNEXT.invoke(self.m_id, "emailComposerProperty");
		}
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

	// Thread methods
	self.emailComposerStartThread = function (callbackId) {
		return JNEXT.invoke(self.m_id, "emailComposerStartThread " + callbackId);
	};
	self.emailComposerStopThread = function () {
		return JNEXT.invoke(self.m_id, "emailComposerStopThread");
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
