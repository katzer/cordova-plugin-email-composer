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

#import "APPEmailComposerImpl.h"
#import "Cordova/NSData+Base64.h"
#import "Cordova/CDVAvailability.h"
#import <MessageUI/MFMailComposeViewController.h>
#import <MobileCoreServices/MobileCoreServices.h>

#include "TargetConditionals.h"

/**
 * Implements the interface methods of the plugin.
 */
@implementation APPEmailComposerImpl

#pragma mark -
#pragma mark Public

/**
 * Checks if the mail composer is able to send mails and if an app is available
 * to handle the specified scheme.
 *
 * @param scheme
 * An URL scheme, that defaults to 'mailto:
 */
- (NSArray*) canSendMail:(NSString*)scheme
{
    bool canSendMail = [MFMailComposeViewController canSendMail];

    bool withScheme = false;
    scheme = [[scheme stringByAppendingString:@":test@test.de"] 
                stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding ];

    NSURL *url = [[NSURL URLWithString:scheme]
                    absoluteURL];

    withScheme = [[UIApplication sharedApplication]
                   canOpenURL:url];

    if (TARGET_IPHONE_SIMULATOR && [scheme hasPrefix:@"mailto:"]) {
        canSendMail = true;
    } else {
        canSendMail = canSendMail||withScheme;
    }
    
    NSArray* resultArray = [NSArray arrayWithObjects:@(canSendMail),@(withScheme), nil];

    return resultArray;
}

/**
 * Instantiates an email composer view.
 *
 * @param properties
 * The email properties like subject, body, attachments
 * @param delegateTo
 * The mail composition view controller’s delegate.
 * @return
 * The configured email composer view
 */
- (MFMailComposeViewController*) mailComposerFromProperties:(NSDictionary*)props
                                                 delegateTo:(id)receiver
{
    BOOL isHTML = [[props objectForKey:@"isHtml"] boolValue];

    MFMailComposeViewController* draft;

    draft = [[MFMailComposeViewController alloc] init];

    // Subject
    [self setSubject:[props objectForKey:@"subject"] ofDraft:draft];
    // Body (as HTML)
    [self setBody:[props objectForKey:@"body"] ofDraft:draft isHTML:isHTML];
    // Recipients
    [self setToRecipients:[props objectForKey:@"to"] ofDraft:draft];
    // CC Recipients
    [self setCcRecipients:[props objectForKey:@"cc"] ofDraft:draft];
    // BCC Recipients
    [self setBccRecipients:[props objectForKey:@"bcc"] ofDraft:draft];
    // Attachments
    [self setAttachments:[props objectForKey:@"attachments"] ofDraft:draft];

    draft.mailComposeDelegate = receiver;

    return draft;
}

/**
 * Creates an mailto-url-sheme.
 *
 * @param properties
 * The email properties like subject, body, attachments
 * @return
 * The configured mailto-sheme
 */
- (NSURL*) urlFromProperties:(NSDictionary*)props
{
    NSString* mailto     = [props objectForKey:@"app"];
    NSString* query      = @"";

    NSString* subject    = [props objectForKey:@"subject"];
    NSString* body       = [props objectForKey:@"body"];
    NSArray* to          = [props objectForKey:@"to"];
    NSArray* cc          = [props objectForKey:@"cc"];
    NSArray* bcc         = [props objectForKey:@"bcc"];

    NSArray* attachments = [props objectForKey:@"attachments"];

    if (![mailto hasSuffix:@":"]) {
        mailto = [mailto stringByAppendingString:@":"];
    }

    mailto = [mailto stringByAppendingString:
              [to componentsJoinedByString:@","]];

    if (body.length > 0) {
        query = [NSString stringWithFormat: @"%@&body=%@",
                   query, body];
    }
    if (subject.length > 0) {
        query = [NSString stringWithFormat: @"%@&subject=%@",
                   query, body];
    }

    if (cc.count > 0) {
        query = [NSString stringWithFormat: @"%@&cc=%@",
                   query, [cc componentsJoinedByString:@","]];
    }

    if (bcc.count > 0) {
        query = [NSString stringWithFormat: @"%@&bcc=%@",
                   query, [cc componentsJoinedByString:@","]];
    }

    if (attachments.count > 0) {
        NSLog(@"The 'mailto' URI Scheme (RFC 2368) does not support attachments.");
    }

    if (query.length > 0) {
        query = [@"?" stringByAppendingString:query];
    }

    mailto = [mailto stringByAppendingString:query];

    return [[NSURL URLWithString:mailto] absoluteURL];
}

#pragma mark -
#pragma mark Private

/**
 * Sets the subject of the email draft.
 *
 * @param subject
 * The subject of the email.
 * @param draft
 * The email composer view.
 */
- (void) setSubject:(NSString*)subject
            ofDraft:(MFMailComposeViewController*)draft
{
    [draft setSubject:subject];
}

/**
 * Sets the body of the email draft.
 *
 * @param body
 * The body of the email.
 * @param isHTML
 * Indicates if the body is an HTML encoded string.
 * @param draft
 * The email composer view.
 */
- (void) setBody:(NSString*)body ofDraft:(MFMailComposeViewController*)draft
          isHTML:(BOOL)isHTML
{
    [draft setMessageBody:body isHTML:isHTML];
}

/**
 * Sets the recipients of the email draft.
 *
 * @param recipients
 * The recipients of the email.
 * @param draft
 * The email composer view.
 */
- (void) setToRecipients:(NSArray*)recipients
                 ofDraft:(MFMailComposeViewController*)draft
{
    [draft setToRecipients:recipients];
}

/**
 * Sets the CC recipients of the email draft.
 *
 * @param ccRecipients
 * The CC recipients of the email.
 * @param draft
 * The email composer view.
 */
- (void) setCcRecipients:(NSArray*)ccRecipients
                 ofDraft:(MFMailComposeViewController*)draft
{
    [draft setCcRecipients:ccRecipients];
}

/**
 * Sets the BCC recipients of the email draft.
 *
 * @param bccRecipients
 * The BCC recipients of the email.
 * @param draft
 * The email composer view.
 */
- (void) setBccRecipients:(NSArray*)bccRecipients
                  ofDraft:(MFMailComposeViewController*)draft
{
    [draft setBccRecipients:bccRecipients];
}

/**
 * Sets the attachments of the email draft.
 *
 * @param attachments
 * The attachments of the email.
 * @param draft
 * The email composer view.
 */
- (void) setAttachments:(NSArray*)attatchments
                ofDraft:(MFMailComposeViewController*)draft
{
    if (attatchments)
    {
        for (NSString* path in attatchments)
        {
            NSData* data = [self getDataForAttachmentPath:path];

            NSString* basename = [self getBasenameFromAttachmentPath:path];
            NSString* pathExt  = [basename pathExtension];
            NSString* fileName = [basename pathComponents].lastObject;
            NSString* mimeType = [self getMimeTypeFromFileExtension:pathExt];

            // Couldn't find mimeType, must be some type of binary data
            if (mimeType == nil) mimeType = @"application/octet-stream";

            [draft addAttachmentData:data mimeType:mimeType fileName:fileName];
        }
    }
}

/**
 * Returns the data for a given (relative) attachment path.
 *
 * @param path
 * An absolute/relative path or the base64 data.
 * @return
 * The data for the attachment.
 */
- (NSData*) getDataForAttachmentPath:(NSString*)path
{
    if ([path hasPrefix:@"file:///"])
    {
        return [self dataForAbsolutePath:path];
    }
    else if ([path hasPrefix:@"res:"])
    {
        return [self dataForResource:path];
    }
    else if ([path hasPrefix:@"file://"])
    {
        return [self dataForAsset:path];
    }
    else if ([path hasPrefix:@"base64:"])
    {
        return [self dataFromBase64:path];
    }

    NSFileManager* fileManager = [NSFileManager defaultManager];

    if (![fileManager fileExistsAtPath:path]){
        NSLog(@"File not found: %@", path);
    }

    return [fileManager contentsAtPath:path];
}

/**
 * Retrieves the data for an absolute attachment path.
 *
 * @param path
 * An absolute file path.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataForAbsolutePath:(NSString*)path
{
    NSFileManager* fileManager = [NSFileManager defaultManager];
    NSString* absPath;

    absPath = [path stringByReplacingOccurrencesOfString:@"file://"
                                              withString:@""];

    if (![fileManager fileExistsAtPath:absPath])
    {
        NSLog(@"File not found: %@", absPath);
    }

    NSData* data = [fileManager contentsAtPath:absPath];

    return data;
}

/**
 * Retrieves the data for a resource path.
 *
 * @param path
 * A relative file path.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataForResource:(NSString*)path
{
    NSFileManager* fileManager = [NSFileManager defaultManager];
    NSString* absPath;

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [[mainBundle bundlePath]
                            stringByAppendingString:@"/"];

    absPath = [path pathComponents].lastObject;

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fileManager fileExistsAtPath:absPath]){
        NSLog(@"File not found: %@", absPath);
    }

    NSData* data = [fileManager contentsAtPath:absPath];

    return data;
}

/**
 * Retrieves the data for a asset path.
 *
 * @param path
 * A relative www file path.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataForAsset:(NSString*)path
{
    NSFileManager* fileManager = [NSFileManager defaultManager];
    NSString* absPath;

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [[mainBundle bundlePath]
                            stringByAppendingString:@"/"];

    absPath = [path stringByReplacingOccurrencesOfString:@"file:/"
                                              withString:@"www"];

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fileManager fileExistsAtPath:absPath]){
        NSLog(@"File not found: %@", absPath);
    }

    NSData* data = [fileManager contentsAtPath:absPath];

    return data;
}

/**
 * Retrieves the data for a base64 encoded string.
 *
 * @param base64String
 * Base64 encoded string.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataFromBase64:(NSString*)base64String
{
    NSUInteger length = [base64String length];
    NSRegularExpression *regex;
    NSString *dataString;

    regex = [NSRegularExpression regularExpressionWithPattern:@"^base64:[^/]+.."
                                                      options:NSRegularExpressionCaseInsensitive
                                                        error:Nil];

    dataString = [regex stringByReplacingMatchesInString:base64String
                                                 options:0
                                                   range:NSMakeRange(0, length)
                                            withTemplate:@""];

#ifndef __CORDOVA_3_8_0
    NSData* data = [NSData dataFromBase64String:dataString];
#else
    NSData* data = [NSData cdv_dataFromBase64String:dataString];
#endif

    return data;
}

/**
 * Retrieves the mime type from the file extension.
 *
 * @param extension
 * The file's extension.
 * @return
 * The coresponding MIME type.
 */
- (NSString*) getMimeTypeFromFileExtension:(NSString*)extension
{
    if (!extension) {
        return nil;
    }

    // Get the UTI from the file's extension
    CFStringRef ext = (CFStringRef)CFBridgingRetain(extension);
    CFStringRef type = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, ext, NULL);

    ext = NULL;

    // Converting UTI to a mime type
    return (NSString*)CFBridgingRelease(UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType));
}

/**
 * Retrieves the attachments basename.
 *
 * @param path
 * The file path or bas64 data of the attachment.
 * @return
 * The attachments basename.
 */
- (NSString*) getBasenameFromAttachmentPath:(NSString*)path
{
    if ([path hasPrefix:@"base64:"])
    {
        NSString* pathWithoutPrefix;

        pathWithoutPrefix = [path stringByReplacingOccurrencesOfString:@"base64:"
                                                            withString:@""];

        return [pathWithoutPrefix substringToIndex:
                [pathWithoutPrefix rangeOfString:@"//"].location];
    }

    return path;
}

@end
