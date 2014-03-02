/*
 Copyright 2013-2014 appPlant UG

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
#import "NSData+Base64.h"
#import <MobileCoreServices/MobileCoreServices.h>


@interface APPEmailComposer (Private)

// Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein
- (MFMailComposeViewController*) getDraftWithProperties:(NSDictionary*)properties;
// Zeigt den ViewController zum Versenden/Bearbeiten der Mail an
- (void) openDraft: (MFMailComposeViewController*)draft;
// Setzt den Betreff der Mail
- (void) setSubject:(NSString*)subject ofDraft:(MFMailComposeViewController*)draft;
// Setzt den Text der Mail
- (void) setBody:(NSString*)body ofDraft:(MFMailComposeViewController*)draft isHTML:(BOOL)isHTML;
// Setzt die Empfänger der Mail
- (void) setToRecipients:(NSArray*)recipients ofDraft:(MFMailComposeViewController*)draft;
// Setzt die CC-Empfänger der Mail
- (void) setCcRecipients:(NSArray*)ccRecipients ofDraft:(MFMailComposeViewController*)draft;
// Setzt die BCC-Empfänger der Mail
- (void) setBccRecipients:(NSArray*)bccRecipients ofDraft:(MFMailComposeViewController*)draft;
// Fügt Anhänge zur Mail inhzu
- (void) setAttachments:(NSArray*)attatchments ofDraft:(MFMailComposeViewController*)draft;
// Wird aufgerufen, nachdem der Composer View beendet wurde
- (void) mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error;
// Retrieves the mime type from the file extension
- (NSString*) getMimeTypeFromFileExtension:(NSString*)extension;
// Returns the data for a given (relative) attachments path
- (NSData*) getDataForAttachmentPath:(NSString*)path;
// Retrieves the attachments basename.
- (NSString*) getBasenameFromAttachmentPath:(NSString*)path;

@end


@implementation APPEmailComposer

/**
 * Überprüft, ob Emails versendet werden können.
 */
- (void) isServiceAvailable:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    bool             canSendMail  = [MFMailComposeViewController canSendMail];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                    messageAsBool:canSendMail];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * Öffnet den Email-Kontroller mit vorausgefüllten Daten.
 */
- (void) open:(CDVInvokedUrlCommand*)command
{
    NSDictionary*                properties = [command.arguments objectAtIndex:0];
    MFMailComposeViewController* controller = [self getDraftWithProperties:properties];

    if (!controller)
    {
        return;
    }

    [self openDraft:controller];
    [self commandDelegate];
}

/**
 * Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein.
 */
- (MFMailComposeViewController*) getDraftWithProperties:(NSDictionary*)properties
{
    // Falls das Gerät kein Email Interface unterstützt
    if (![MFMailComposeViewController canSendMail])
    {
        return NULL;
    }

    MFMailComposeViewController* draft = [[MFMailComposeViewController alloc] init];

    draft.mailComposeDelegate = self;

    // Subject
    [self setSubject:[properties objectForKey:@"subject"] ofDraft:draft];
    // Body (as HTML)
    [self setBody:[properties objectForKey:@"body"] ofDraft:draft isHTML:[[properties objectForKey:@"isHtml"] boolValue]];
    // Recipients
    [self setToRecipients:[properties objectForKey:@"to"] ofDraft:draft];
    // CC Recipients
    [self setCcRecipients:[properties objectForKey:@"cc"] ofDraft:draft];
    // BCC Recipients
    [self setBccRecipients:[properties objectForKey:@"bcc"] ofDraft:draft];
    // Attachments
    [self setAttachments:[properties objectForKey:@"attachments"] ofDraft:draft];

    return draft;
}

/**
 * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
 */
- (void) openDraft: (MFMailComposeViewController*)draft
{
    [self.commandDelegate runInBackground:^{
        [self.viewController presentViewController:draft animated:YES completion:NULL];
    }];
}

/**
 * Setzt den Betreff der Mail.
 */
- (void) setSubject:(NSString*)subject ofDraft:(MFMailComposeViewController*)draft
{
    [draft setSubject:subject];
}

/**
 * Setzt den Text der Mail.
 */
- (void) setBody:(NSString*)body ofDraft:(MFMailComposeViewController*)draft isHTML:(BOOL)isHTML
{
    [draft setMessageBody:body isHTML:isHTML];
}

/**
 * Setzt die Empfänger der Mail.
 */
- (void) setToRecipients:(NSArray*)recipients ofDraft:(MFMailComposeViewController*)draft
{
    [draft setToRecipients:recipients];
}

/**
 * Setzt die CC-Empfänger der Mail.
 */
- (void) setCcRecipients:(NSArray*)ccRecipients ofDraft:(MFMailComposeViewController*)draft
{
    [draft setCcRecipients:ccRecipients];
}

/**
 * Setzt die BCC-Empfänger der Mail.
 */
- (void) setBccRecipients:(NSArray*)bccRecipients ofDraft:(MFMailComposeViewController*)draft
{
    [draft setBccRecipients:bccRecipients];
}

/**
 * Fügt die Anhände zur Mail hinzu.
 */
- (void) setAttachments:(NSArray*)attatchments ofDraft:(MFMailComposeViewController*)draft
{
    if (attatchments)
    {
        for (NSString* path in attatchments)
        {
            NSData* data       = [self getDataForAttachmentPath:path];

            NSString* basename = [self getBasenameFromAttachmentPath:path];
            NSString* pathExt  = [basename pathExtension];
            NSString* fileName = [basename pathComponents].lastObject;
            NSString* mimeType = [self getMimeTypeFromFileExtension:pathExt];

            [draft addAttachmentData:data mimeType:mimeType fileName:fileName];
        }
    }
}

/**
 * @delegate
 *
 * Wird aufgerufen, nachdem der Composer View beendet wurde
 */
- (void) mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error
{
    [controller dismissViewControllerAnimated:YES completion:nil];
}

/**
 * Retrieves the mime type from the file extension.
 */
- (NSString*) getMimeTypeFromFileExtension:(NSString*)extension
{
    if (!extension)
    {
        return nil;
    }

    // Get the UTI from the file's extension
    CFStringRef pathExtension = (CFStringRef)CFBridgingRetain(extension);
    CFStringRef type          = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension, NULL);

    // Converting UTI to a mime type
    return (NSString*)CFBridgingRelease(UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType));
}

/**
 * Retrieves the attachments basename.
 */
- (NSString*) getBasenameFromAttachmentPath:(NSString*)path
{
    if ([path hasPrefix:@"base64:"])
    {
        NSString* pathWithoutPrefix = [path stringByReplacingOccurrencesOfString:@"base64:" withString:@""];

        return [pathWithoutPrefix substringToIndex:[pathWithoutPrefix rangeOfString:@"//"].location];
    }

    return path;

}

/**
 * Returns the data for a given (relative) attachments path.
 */
- (NSData*) getDataForAttachmentPath:(NSString*)path
{
    NSFileManager* fileManager = [NSFileManager defaultManager];

    if ([path hasPrefix:@"absolute://"])
    {
        NSString* absolutePath = [path stringByReplacingOccurrencesOfString:@"absolute://" withString:@"/"];

        return [fileManager contentsAtPath:absolutePath];
    }
    else if ([path hasPrefix:@"relative://"])
    {
        NSString* bundlePath   = [[[NSBundle mainBundle] bundlePath] stringByAppendingString:@"/"];
        NSString* absolutePath = [path stringByReplacingOccurrencesOfString:@"relative://" withString:@""];

        absolutePath = [bundlePath stringByAppendingString:absolutePath];

        return [fileManager contentsAtPath:absolutePath];
    }
    else if ([path hasPrefix:@"base64:"])
    {
        NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"^base64:[^/]+.." options:NSRegularExpressionCaseInsensitive error:Nil];
        NSString *dataString = [regex stringByReplacingMatchesInString:path options:0 range:NSMakeRange(0, [path length]) withTemplate:@""];

        return [NSData dataFromBase64String:dataString];
    }

    return [fileManager contentsAtPath:path];
}

@end