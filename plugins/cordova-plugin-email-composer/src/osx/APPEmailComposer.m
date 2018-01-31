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

#import "APPEmailComposer.h"
#import "APPEmailComposerImpl.h"

@interface APPEmailComposer ()

// Reference is needed because of the async delegate
@property (nonatomic, strong) CDVInvokedUrlCommand* command;
// Implements the core functionality
@property (nonatomic, strong) APPEmailComposerImpl* impl;

@end

@implementation APPEmailComposer

@synthesize command, impl;

#pragma mark -
#pragma mark Lifecycle

/**
 * Initialize the core impl object which does the main stuff.
 */
- (void) pluginInitialize
{
    self.impl = [[APPEmailComposerImpl alloc] init];
}

#pragma mark -
#pragma mark Public

/**
 * Check if the mail composer is able to send mails.
 */
- (void) isAvailable:(CDVInvokedUrlCommand*)cmd
{
    [self.commandDelegate runInBackground:^{
        NSString* scheme   = [cmd argumentAtIndex:0];
        NSArray* boolArray = [self.impl canSendMail:scheme];
        CDVPluginResult* result;

        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                     messageAsMultipart:boolArray];

        [self.commandDelegate sendPluginResult:result
                                    callbackId:cmd.callbackId];
    }];
}

/**
 * Show the email composer view with pre-filled data.
 */
- (void) open:(CDVInvokedUrlCommand*)cmd
{
    NSDictionary* props = [cmd argumentAtIndex:0];

    self.command = cmd;

    [self.commandDelegate runInBackground:^{
        NSString* scheme = [props objectForKey:@"app"];

        if (![self canUseAppleMail:scheme]) {
            [self openURLFromProperties:props];
            return;
        }

        [self presentMailComposerFromProperties:props];
    }];
}

#pragma mark -
#pragma mark NSSharingServicePickerDelegate

- (void) sharingService:(NSSharingService *)sharingService
          didShareItems:(NSArray *)items
{
    [self execCallback];
    self.command = NULL;
}

- (void) sharingService:(NSSharingService *)sharingService
    didFailToShareItems:(NSArray *)items
                  error:(NSError *)error
{
    [self sharingService:sharingService didShareItems:items];
}

#pragma mark -
#pragma mark Private

/**
 * Displays the email draft.
 */
- (void) presentMailComposerFromProperties:(NSDictionary*)props
{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSArray* res =
        [self.impl mailComposerFromProperties:props delegateTo:self];

        NSSharingService* draft     = res[0];
        NSAttributedString* body    = res[1];
        NSMutableArray* attachments = res[2];

        if (!draft) {
            [self execCallback];
            return;
        }

        [attachments insertObject:body atIndex:0];

        [draft performWithItems:attachments];
    });

}

/**
 * Instructs the application to open the specified URL.
 */
- (void) openURLFromProperties:(NSDictionary*)props
{
    NSURL* url = [self.impl urlFromProperties:props];

    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSWorkspace sharedWorkspace] openURL:url];
    });
}

/**
 * If the specified app if the buil-in iMail framework can be used.
 */
- (BOOL) canUseAppleMail:(NSString*) scheme
{
    return [scheme hasPrefix:@"mailto"];
}

/**
 * Invokes the callback without any parameter.
 */
- (void) execCallback
{
    CDVPluginResult *result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:result
                                callbackId:self.command.callbackId];
}

@end
