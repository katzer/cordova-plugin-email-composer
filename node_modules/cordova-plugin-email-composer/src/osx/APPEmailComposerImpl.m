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

#import "APPEmailComposerImpl.h"
#import <Cordova/CDVAvailability.h>
#import <Cordova/NSData+Base64.h>

@implementation APPEmailComposerImpl

#pragma mark -
#pragma mark Public

/**
 * Checks if the mail composer is able to send mails and if an app is available
 * to handle the specified scheme.
 *
 * @param scheme An URL scheme, that defaults to 'mailto:
 */
- (NSArray*) canSendMail:(NSString*)scheme
{
    NSSharingService* service =
    [NSSharingService sharingServiceNamed:NSSharingServiceNameComposeEmail];

    bool canSendMail = [service canPerformWithItems:@[@"Test"]];
    bool withScheme  = false;

    if (!scheme) {
        scheme = @"mailto:";
    } else if (![scheme hasSuffix:@":"]) {
        scheme = [scheme stringByAppendingString:@":"];
    }

    NSCharacterSet *set = [NSCharacterSet URLFragmentAllowedCharacterSet];
    scheme = [[scheme stringByAppendingString:@"test@test.de"]
                stringByAddingPercentEncodingWithAllowedCharacters:set];

    NSURL* url = [NSURL URLWithString:scheme];
    NSURL* app = [[NSWorkspace sharedWorkspace] URLForApplicationToOpenURL:url];
    withScheme = app != NULL;

    return @[@(canSendMail), @(withScheme)];
}

/**
 * Instantiates an email composer view.
 *
 * @param properties The email properties like subject, body, attachments
 * @param delegateTo The mail composition view controller’s delegate.
 *
 * @return The configured email composer view
 */
- (NSArray*) mailComposerFromProperties:(NSDictionary*)props
                             delegateTo:(id)receiver
{
    BOOL isHTML = [[props objectForKey:@"isHtml"] boolValue];

    NSSharingService* draft =
    [NSSharingService sharingServiceNamed:NSSharingServiceNameComposeEmail];

    draft.subject    = [props objectForKey:@"subject"];

    draft.recipients = [[[props objectForKey:@"to"]
                        arrayByAddingObjectsFromArray:[props objectForKey:@"cc"]]
                        arrayByAddingObjectsFromArray:[props objectForKey:@"bcc"]];

    NSAttributedString* body =
    [self getBody:[props objectForKey:@"body"] isHTML:isHTML];

    NSArray* attachments =
    [self getAttachments:[props objectForKey:@"attachments"]];

    draft.delegate = receiver;

    return @[draft, body, attachments];
}

/**
 * Creates an mailto-url-sheme.
 *
 * @param properties The email properties like subject, body, attachments
 *
 * @return The configured mailto-sheme
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

    NSCharacterSet* cs   = [NSCharacterSet URLHostAllowedCharacterSet];

    if (![mailto hasSuffix:@":"]) {
        mailto = [mailto stringByAppendingString:@":"];
    }

    mailto = [mailto stringByAppendingString:
              [to componentsJoinedByString:@","]];

    if (body.length > 0) {
        query = [NSString stringWithFormat: @"%@&body=%@", query,
                 [body stringByAddingPercentEncodingWithAllowedCharacters:cs]];
    }
    if (subject.length > 0) {
        query = [NSString stringWithFormat: @"%@&subject=%@", query,
                 [subject stringByAddingPercentEncodingWithAllowedCharacters:cs]];
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

    return [NSURL URLWithString:mailto];
}

#pragma mark -
#pragma mark Private

/**
 * Sets the body of the email draft.
 *
 * @param body   The body
 * @param draft  The email composer view
 */
- (NSAttributedString*) getBody:(NSString*)body isHTML:(BOOL)isHTML
{
    if (!isHTML)
        return [[NSAttributedString alloc] initWithString:body];

    NSData* html =
    [NSData dataWithBytes:[body UTF8String]
                   length:[body lengthOfBytesUsingEncoding:NSUTF8StringEncoding]];

    NSString* www  = [[NSBundle mainBundle] pathForResource:@"www" ofType:NULL];
    NSURL* baseURL = [NSURL fileURLWithPath:www];

    return [[NSAttributedString alloc] initWithHTML:html
                                            baseURL:baseURL
                                 documentAttributes:NULL];
}

/**
 * Sets the attachments of the email draft.
 *
 * @param attachments The attachments
 * @param draft       The email composer view
 */
- (NSArray*) getAttachments:(NSArray*)attatchments
{
    NSMutableArray* uris = [[NSMutableArray alloc] init];

    if (!attatchments)
        return uris;

    for (NSString* path in attatchments)
    {
        NSURL* url = [self urlForAttachmentPath:path];

        [uris addObject:url];
    }

    return uris;
}

/**
 * Returns the URL for a given (relative) attachment path.
 *
 * @param path An absolute/relative path or the base64 data
 *
 * @return The URL for the attachment.
 */
- (NSURL<NSPasteboardWriting>*) urlForAttachmentPath:(NSString*)path
{
    if ([path hasPrefix:@"file:///"])
    {
        return [self urlForAbsolutePath:path];
    }
    else if ([path hasPrefix:@"res:"])
    {
        return [self urlForResource:path];
    }
    else if ([path hasPrefix:@"file://"])
    {
        return [self urlForAsset:path];
    }
    else if ([path hasPrefix:@"app://"])
    {
        return [self urlForAppInternalPath:path];
    }
    else if ([path hasPrefix:@"base64:"])
    {
        return [self urlFromBase64:path];
    }

    NSFileManager* fm = [NSFileManager defaultManager];

    if (![fm fileExistsAtPath:path]){
        NSLog(@"File not found: %@", path);
    }

    return [NSURL fileURLWithPath:path];
}

/**
 * Retrieves the data for an absolute attachment path.
 *
 * @param path An absolute file path.
 *
 * @return The data for the attachment.
 */
- (NSURL<NSPasteboardWriting>*) urlForAbsolutePath:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];
    NSString* absPath;

    absPath = [path stringByReplacingOccurrencesOfString:@"file://"
                                              withString:@""];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * Retrieves the data for a resource path.
 *
 * @param path A relative file path.
 *
 * @return The data for the attachment.
 */
- (NSURL<NSPasteboardWriting>*) urlForResource:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];
    NSString* absPath;

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [[mainBundle bundlePath]
                            stringByAppendingString:@"/Contents/Resources/"];

    if ([path hasPrefix:@"res://icon"]) {
        path = @"res://AppIcon.icns";
    }

    absPath = [path stringByReplacingOccurrencesOfString:@"res://"
                                              withString:@""];

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * Retrieves the file URL for a asset path.
 *
 * @param path A relative www file path.
 *
 * @return The URL to the attachment.
 */
- (NSURL<NSPasteboardWriting>*) urlForAsset:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];
    NSString* absPath;

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [[mainBundle bundlePath]
                            stringByAppendingString:@"/Contents/Resources/"];

    absPath = [path stringByReplacingOccurrencesOfString:@"file:/"
                                              withString:@"www"];

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * Retrieves the file URL for an internal app path.
 *
 * @param path A relative file path from main bundle dir.
 *
 * @return The URL for the internal path.
 */
- (NSURL<NSPasteboardWriting>*) urlForAppInternalPath:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* absPath    = [mainBundle bundlePath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * Retrieves the data for a base64 encoded string.
 *
 * @param base64String Base64 encoded string.
 *
 * @return The data for the attachment.
 */
- (NSURL<NSPasteboardWriting>*) urlFromBase64:(NSString*)base64String
{
    NSString *filename = [self getBasenameFromAttachmentPath:base64String];
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

    NSData* data = [[NSData alloc] initWithBase64EncodedString:dataString
                                                       options:0];


    return [self urlForData:data withFileName:filename];
}

/**
 * Retrieves the attachments basename.
 *
 * @param path The file path or bas64 data of the attachment.
 *
 * @return The attachments basename.
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

/**
 * Write the data into a temp file.
 *
 * @param data The data to save into a file.
 * @param name The name of the file.
 *
 * @return The file URL
 */
- (NSURL*) urlForData:(NSData*)data withFileName:(NSString*) filename
{
    NSFileManager* fm = [NSFileManager defaultManager];

    NSString* tempDir = NSTemporaryDirectory();

    [fm createDirectoryAtPath:tempDir withIntermediateDirectories:YES
                   attributes:NULL
                        error:NULL];

    NSString* absPath = [tempDir stringByAppendingPathComponent:filename];

    NSURL* url = [NSURL fileURLWithPath:absPath];
    [data writeToURL:url atomically:NO];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return url;
}

@end
