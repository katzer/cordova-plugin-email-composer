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

#ifndef EmailComposer_NDK_HPP_
#define EmailComposer_NDK_HPP_

#include <string>
#include <pthread.h>

class EmailComposer_JS;

namespace webworks {

class EmailComposer_NDK {
public:
	explicit EmailComposer_NDK(EmailComposer_JS *parent = NULL);
	virtual ~EmailComposer_NDK();

	// The extension methods are defined here
	std::string isAvailable();

	std::string emailComposerTest();

	std::string emailComposerTest(const std::string& inputString);

	std::string getEmailComposerProperty();

	void setEmailComposerProperty(const std::string& inputString);

	void emailComposerTestAsync(const std::string& callbackId, const std::string& inputString);

	std::string emailComposerStartThread(const std::string& callbackId);

	std::string emailComposerStopThread();

	bool isThreadHalt();

	void emailComposerThreadCallback();

private:
	EmailComposer_JS *m_pParent;
	int emailComposerProperty;
	int emailComposerThreadCount;
	bool threadHalt;
	std::string threadCallbackId;
	pthread_t m_thread;
	pthread_cond_t cond;
	pthread_mutex_t mutex;
};

} // namespace webworks

#endif /* EmailComposer_NDK_HPP_ */
