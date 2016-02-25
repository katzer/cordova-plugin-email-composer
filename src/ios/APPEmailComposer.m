/*
 Copyright 2013-2016 appPlant UG

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

#import "APPEmailComposer.h"
#import "APPEmailComposerImpl.h"
#import <Cordova/CDVAvailability.h>
#ifndef __CORDOVA_4_0_0
    #import <Cordova/NSData+Base64.h>
#endif
#import <MobileCoreServices/MobileCoreServices.h>

#include "TargetConditionals.h"

@interface APPEmailComposer ()

@property (nonatomic, retain) CDVInvokedUrlCommand* command;

/**
 * Implements the plugin functionality.
 */
@property (nonatomic, retain) APPEmailComposerImpl* impl;

@end

@implementation APPEmailComposer

#pragma mark -
#pragma mark Lifecycle

- (void)pluginInitialize
{
    _impl = [[APPEmailComposerImpl alloc] init];
}

#pragma mark -
#pragma mark Public

/**
 * Checks if the mail composer is able to send mails.
 *
 * @param callbackId
 *      The ID of the JS function to be called with the result
 */
- (void) isAvailable:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSString* scheme = command.arguments[0];
        NSArray* boolArray = [_impl canSendMail:scheme];
        CDVPluginResult* result;

        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                     messageAsMultipart:boolArray];

        [self.commandDelegate sendPluginResult:result
                                    callbackId:command.callbackId];
    }];
}

/**
 * Shows the email composer view with pre-filled data.
 *
 * @param properties
 *      The email properties like subject, body, attachments
 */
- (void) open:(CDVInvokedUrlCommand*)command
{
    NSDictionary* props = command.arguments[0];

    _command = command;

    [self.commandDelegate runInBackground:^{
        NSString* scheme = [props objectForKey:@"app"];

        if (![self canUseAppleMail:scheme]) {
            [self openURLFromProperties:props];
            return;
        }

        if (TARGET_IPHONE_SIMULATOR) {
            [self informAboutIssueWithSimulators];
            [self execCallback];
        }
        else {
            [self presentMailComposerFromProperties:props];
        }
    }];
}

#pragma mark -
#pragma mark MFMailComposeViewControllerDelegate

/**
 * Delegate will be called after the mail composer did finish an action
 * to dismiss the view.
 */
- (void) mailComposeController:(MFMailComposeViewController*)controller
           didFinishWithResult:(MFMailComposeResult)result
                         error:(NSError*)error
{
    [controller dismissViewControllerAnimated:YES completion:NULL];

    [self execCallback];
}

#pragma mark -
#pragma mark Private

/**
 * Displays the email draft.
 *
 * @param draft
 *      The email composer view
 */
- (void) presentMailComposerFromProperties:(NSDictionary*)props
{
    dispatch_async(dispatch_get_main_queue(), ^{
        MFMailComposeViewController* draft =
        [_impl mailComposerFromProperties:props delegateTo:self];

        [self.viewController presentViewController:draft
                                          animated:YES
                                        completion:NULL];
    });

}

/**
 * Instructs the application to open the specified URL.
 *
 * @param url
 * A mailto: compatible URL.
 */
- (void) openURLFromProperties:(NSDictionary*)props
{
    NSURL* url = [_impl urlFromProperties:props];

    [[UIApplication sharedApplication] openURL:url];
}

/**
 * If the specified app if the buil-in iMail framework can be used.
 *
 * @param scheme
 * An URL scheme.
 * @return
 * true if the scheme does refer to the email: scheme.
 */
- (BOOL) canUseAppleMail:(NSString*) scheme
{
    return [scheme hasPrefix:@"mailto"];
}

/**
 * Presents a dialog to the user to inform him about an issue with the iOS8
 * simulator in combination with the mail library.
 */
- (void) informAboutIssueWithSimulators
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[[UIAlertView alloc] initWithTitle:@"Email-Composer"
                                    message:@"Please use a physical device."
                                   delegate:NULL
                          cancelButtonTitle:@"OK"
                          otherButtonTitles:NULL] show];
    });
}

/**
 * Invokes the callback without any parameter.
 */
- (void) execCallback
{
    CDVPluginResult *result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:result
                                callbackId:_command.callbackId];
}

@end
