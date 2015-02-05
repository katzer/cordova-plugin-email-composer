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

// Qt headers
#include <QUrl>

// Cascades headers
#include <bb/system/InvokeRequest>
#include <bb/system/InvokeManager>
#include <bb/system/InvokeTargetReply>
#include <bb/data/JsonDataAccess>
#include <bb/PpsObject>

#include "emailcomposer_ndk.hpp"
#include "emailcomposer_js.hpp"

#include <bps/navigator_invoke.h>

namespace webworks {

EmailComposer_NDK::EmailComposer_NDK(EmailComposer_JS *parent):
	m_pParent(parent) {
		m_pParent->getLog()->info("EmailComposer Created");
}

EmailComposer_NDK::~EmailComposer_NDK() {
}

std::string EmailComposer_NDK::isAvailable() {
    return "true";
}

std::string EmailComposer_NDK::open(const std::string& options) {
    bb::system::InvokeRequest request;
    request.setAction("bb.action.COMPOSE");
    request.setTarget("sys.pim.uib.email.hybridcomposer");
    request.setMimeType("message/rfc822");

    QVariantMap data;
    data["data"] = createData(options);
    bool ok;
    request.setData(bb::PpsObject::encode(data, &ok));

    bb::system::InvokeManager invokeManager;
    bb::system::InvokeTargetReply * reply = invokeManager.invoke(request);
    return "success";
}

QVariantMap EmailComposer_NDK::createData(const std::string & options) {
    bb::data::JsonDataAccess jda;
    QVariant parsedObject = jda.loadFromBuffer(QString::fromUtf8(options.c_str(), options.size()));
    QVariantMap map = parsedObject.toMap();
    QVariantMap fixedMap;
    fixedMap.insert("to", map["to"]);
    fixedMap.insert("cc", map["cc"]);
    fixedMap.insert("bcc", map["bcc"]);
    fixedMap.insert("subject", map["subject"]);
    fixedMap.insert("body", map["body"]);
    QVariantList attachments = map["attachments"].toList();
    QVariantList fixedAttachments;
    foreach(QVariant file, attachments) {
        QString filePath = file.toString();
        fixedAttachments.append(QString(QUrl(filePath).toEncoded()));
    }
    fixedMap.insert("attachment", fixedAttachments);

    return fixedMap;
}

} /* namespace webworks */
