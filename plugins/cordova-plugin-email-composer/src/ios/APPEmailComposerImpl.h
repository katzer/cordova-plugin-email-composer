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

#import <MessageUI/MFMailComposeViewController.h>

@interface APPEmailComposerImpl : NSObject

// Checks if the mail composer is able to send mails
- (NSArray*) canSendMail:(NSString*)scheme;
// Creates an mailto-url-sheme
- (NSURL*) urlFromProperties:(NSDictionary*)props;
// Instantiates an email composer view
- (MFMailComposeViewController*) mailComposerFromProperties:(NSDictionary*)props
                                                 delegateTo:(id)receiver;

@end
