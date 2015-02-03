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

#include <string>
#include <sstream>
#include <pthread.h>
#include <json/reader.h>
#include <json/writer.h>
#include "emailcomposer_ndk.hpp"
#include "emailcomposer_js.hpp"

#include <bps/navigator_invoke.h>

namespace webworks {

EmailComposer_NDK::EmailComposer_NDK(EmailComposer_JS *parent):
	m_pParent(parent),
	emailComposerProperty(50),
	emailComposerThreadCount(1),
	threadHalt(true),
	m_thread(0) {
		pthread_cond_t cond  = PTHREAD_COND_INITIALIZER;
		pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
		m_pParent->getLog()->info("EmailComposer Created");
}

EmailComposer_NDK::~EmailComposer_NDK() {
}

// These methods are the true native code we intend to reach from WebWorks
std::string EmailComposer_NDK::isAvailable() {
//    Json::Value root;
//    root["result"] = "true";
//    Json::FastWriter writer;
//    return writer.write(root);
    return "true";
}

std::string EmailComposer_NDK::open(const std::string& options) {
    bps_initialize();
    navigator_invoke_invocation_t *invoke = NULL;
    navigator_invoke_invocation_create(&invoke);
    // set invocation action and type
    navigator_invoke_invocation_set_action(invoke, "bb.action.COMPOSE");
    navigator_invoke_invocation_set_target(invoke, "sys.pim.uib.email.hybridcomposer");
    navigator_invoke_invocation_set_type(invoke, "message/rfc822");

    std::string uri = createUri(options);
    navigator_invoke_invocation_set_uri(invoke, uri.c_str());

    // invoke the target
    navigator_invoke_invocation_send(invoke);
    // clean up resources
    int result = navigator_invoke_invocation_destroy(invoke);
    // checks the result of the invocation
    if (result == BPS_SUCCESS) {
        return "success";
    } else {
        return "fail";
    }
}

std::string EmailComposer_NDK::createUri(const std::string & options) {
    std::string resultUri = ""; // will contain the resulting URI
    Json::Value root; // will contain the root value
    Json::Reader reader;
    bool parsing_success = reader.parse(options, root);
    const Json::Value defalt_result;

    bool toIsArray = root["to"].isArray();
    Json::Value toValue = root["to"]; // array
    resultUri.append("mailto:");
    for (unsigned int index = 0; index < toValue.size(); ++index) { // iterates
        std::string email = toValue[index].asString();
        // if not the first email, add a comma
        if (index != 0) {
            std::string comma = ",";
            email = comma.append(email);
        }
        resultUri.append(email);
    }

    Json::Value cc_value = root["cc"];
    if (!cc_value.empty()) resultUri.append("?cc=");
    for (unsigned int index = 0; index < cc_value.size(); ++index) {
        std::string email = cc_value[index].asString();
        // if not the first email, add a comma
        if (index != 0) {
            std::string comma = ",";
            email = comma.append(email);
        }
        resultUri.append(email);
    }

    Json::Value bcc_value = root["bcc"];
    if (!bcc_value.empty()) resultUri.append("&bcc=");
    for (unsigned int index = 0; index < bcc_value.size(); ++index) {
        std::string email = bcc_value[index].asString();
        // if not the first email, add a comma
        if (index != 0) {
            std::string comma = ",";
            email = comma.append(email);
        }
        resultUri.append(email);
    }

    Json::Value subject_value = root["subject"];
    if (!subject_value.empty()) {
        resultUri.append("&subject=");
        resultUri.append(subject_value.asString());
    }

    Json::Value body_value = root["body"];
    if (!body_value.empty()) {
        resultUri.append("&body=");
        resultUri.append(body_value.asString());
    }

    Json::Value attachment_value = root["attachments"];
    if (!attachment_value.empty()) resultUri.append("&attachment=");
    for (unsigned int index = 0; index < attachment_value.size(); ++index) {
        std::string attachment = attachment_value[index].asString();
        if (index != 0) {
            std::string comma = ",";
            attachment = comma.append(attachment);
        }
        resultUri.append(attachment);
    }

    std::string msg = "Returning ";
    msg.append(resultUri);
    m_pParent->getLog()->debug(msg.c_str());
    return resultUri;
}

std::string EmailComposer_NDK::emailComposerTest() {
	m_pParent->getLog()->debug("testString");
	return "EmailComposer Test Function";
}

// Take in input and return a value
std::string EmailComposer_NDK::emailComposerTest(const std::string& inputString) {
	m_pParent->getLog()->debug("testStringInput");
	return "EmailComposer Test Function, got: " + inputString;
}

// Get an integer property
std::string EmailComposer_NDK::getEmailComposerProperty() {
	m_pParent->getLog()->debug("getEmailComposerProperty");
	stringstream ss;
	ss << emailComposerProperty;
	return ss.str();
}

// set an integer property
void EmailComposer_NDK::setEmailComposerProperty(const std::string& inputString) {
	m_pParent->getLog()->debug("setEmailComposerProperty");
	emailComposerProperty = (int) strtoul(inputString.c_str(), NULL, 10);
}

// Asynchronous callback with JSON data input and output
void EmailComposer_NDK::emailComposerTestAsync(const std::string& callbackId, const std::string& inputString) {
	m_pParent->getLog()->debug("Async Test");
	// Parse the arg string as JSON
	Json::FastWriter writer;
	Json::Reader reader;
	Json::Value root;
	bool parse = reader.parse(inputString, root);

	if (!parse) {
		m_pParent->getLog()->error("Parse Error");
		Json::Value error;
		error["result"] = "Cannot parse JSON object";
		m_pParent->NotifyEvent(callbackId + " " + writer.write(error));
	} else {
		root["result"] = root["value1"].asInt() + root["value2"].asInt();
		m_pParent->NotifyEvent(callbackId + " " + writer.write(root));
	}
}

// Thread functions
// The following functions are for controlling a Thread in the extension

// The actual thread (must appear before the startThread method)
// Loops and runs the callback method
void* EmailComposerThread(void* parent) {
	EmailComposer_NDK *pParent = static_cast<EmailComposer_NDK *>(parent);

	// Loop calls the callback function and continues until stop is set
	while (!pParent->isThreadHalt()) {
		sleep(1);
		pParent->emailComposerThreadCallback();
	}

	return NULL;
}

// Starts the thread and returns a message on status
std::string EmailComposer_NDK::emailComposerStartThread(const std::string& callbackId) {
	if (!m_thread) {
		int rc;
	    rc = pthread_mutex_lock(&mutex);
	    threadHalt = false;
	    rc = pthread_cond_signal(&cond);
	    rc = pthread_mutex_unlock(&mutex);

		pthread_attr_t thread_attr;
		pthread_attr_init(&thread_attr);
		pthread_attr_setdetachstate(&thread_attr, PTHREAD_CREATE_JOINABLE);

		pthread_create(&m_thread, &thread_attr, EmailComposerThread,
				static_cast<void *>(this));
		pthread_attr_destroy(&thread_attr);
		threadCallbackId = callbackId;
		m_pParent->getLog()->info("Thread Started");
		return "Thread Started";
	} else {
		m_pParent->getLog()->warn("Thread Started but already running");
		return "Thread Running";
	}
}

// Sets the stop value
std::string EmailComposer_NDK::emailComposerStopThread() {
	int rc;
	// Request thread to set prevent sleep to false and terminate
	rc = pthread_mutex_lock(&mutex);
	threadHalt = true;
	rc = pthread_cond_signal(&cond);
	rc = pthread_mutex_unlock(&mutex);

    // Wait for the thread to terminate.
    void *exit_status;
    rc = pthread_join(m_thread, &exit_status) ;

	// Clean conditional variable and mutex
	pthread_cond_destroy(&cond);
	pthread_mutex_destroy(&mutex);

	m_thread = 0;
	threadHalt = true;
	m_pParent->getLog()->info("Thread Stopped");
	return "Thread stopped";
}

// The callback method that sends an event through JNEXT
void EmailComposer_NDK::emailComposerThreadCallback() {
	Json::FastWriter writer;
	Json::Value root;
	root["threadCount"] = emailComposerThreadCount++;
	m_pParent->NotifyEvent(threadCallbackId + " " + writer.write(root));
}

// getter for the stop value
bool EmailComposer_NDK::isThreadHalt() {
	int rc;
	bool isThreadHalt;
	rc = pthread_mutex_lock(&mutex);
	isThreadHalt = threadHalt;
	rc = pthread_mutex_unlock(&mutex);
	return isThreadHalt;
}

} /* namespace webworks */
