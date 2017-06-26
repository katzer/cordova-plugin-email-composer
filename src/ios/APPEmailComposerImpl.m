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
#import <MessageUI/MFMailComposeViewController.h>
#import <MobileCoreServices/MobileCoreServices.h>

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
    bool canSendMail = [MFMailComposeViewController canSendMail];
    bool withScheme  = false;

    if (!scheme) {
        scheme = @"mailto:";
    } else if (![scheme hasSuffix:@":"]) {
        scheme = [scheme stringByAppendingString:@":"];
    }

    NSCharacterSet *set = [NSCharacterSet URLFragmentAllowedCharacterSet];
    scheme = [[scheme stringByAppendingString:@"test@test.de"]
                stringByAddingPercentEncodingWithAllowedCharacters:set];

    NSURL *url = [[NSURL URLWithString:scheme]
                    absoluteURL];

    withScheme = [[UIApplication sharedApplication]
                   canOpenURL:url];

    NSArray* resultArray = [NSArray arrayWithObjects:@(canSendMail),@(withScheme), nil];

    return resultArray;
}

/**
 * Instantiates an email composer view.
 *
 * @param properties The email properties like subject, body, attachments
 * @param delegateTo The mail composition view controller’s delegate.
 *
 * @return The configured email composer view
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
 * Sets the subject of the email draft.
 *
 * @param subject The subject
 * @param draft   The email composer view
 */
- (void) setSubject:(NSString*)subject
            ofDraft:(MFMailComposeViewController*)draft
{
    [draft setSubject:subject];
}

/**
 * Sets the body of the email draft.
 *
 * @param body   The body
 * @param isHTML Indicates if the body is an HTML encoded string.
 * @param draft  The email composer view
 */
- (void) setBody:(NSString*)body ofDraft:(MFMailComposeViewController*)draft
          isHTML:(BOOL)isHTML
{
    [draft setMessageBody:body isHTML:isHTML];
}

/**
 * Sets the recipients of the email draft.
 *
 * @param recipients The recipients
 * @param draft      The email composer view.
 */
- (void) setToRecipients:(NSArray*)recipients
                 ofDraft:(MFMailComposeViewController*)draft
{
    [draft setToRecipients:recipients];
}

/**
 * Sets the CC recipients of the email draft.
 *
 * @param ccRecipients The CC recipients
 * @param draft        The email composer view
 */
- (void) setCcRecipients:(NSArray*)ccRecipients
                 ofDraft:(MFMailComposeViewController*)draft
{
    [draft setCcRecipients:ccRecipients];
}

/**
 * Sets the BCC recipients of the email draft.
 *
 * @param bccRecipients The BCC recipients
 * @param draft         The email composer view.
 */
- (void) setBccRecipients:(NSArray*)bccRecipients
                  ofDraft:(MFMailComposeViewController*)draft
{
    [draft setBccRecipients:bccRecipients];
}

/**
 * Sets the attachments of the email draft.
 *
 * @param attachments The attachments
 * @param draft       The email composer view
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
 * @param path An absolute/relative path or the base64 data
 *
 * @return The data for the attachment.
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
    else if ([path hasPrefix:@"app://"])
    {
        return [self dataForAppInternalPath:path];
    }
    else if ([path hasPrefix:@"base64:"])
    {
        return [self dataFromBase64:path];
    }

    NSFileManager* fm = [NSFileManager defaultManager];

    if (![fm fileExistsAtPath:path]){
        NSLog(@"File not found: %@", path);
    }

    return [fm contentsAtPath:path];
}

/**
 * Retrieves the data for an absolute attachment path.
 *
 * @param path An absolute file path.
 *
 * @return The data for the attachment.
 */
- (NSData*) dataForAbsolutePath:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];
    NSString* absPath;

    absPath = [path stringByReplacingOccurrencesOfString:@"file://"
                                              withString:@""];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    NSData* data = [fm contentsAtPath:absPath];

    return data;
}

/**
 * Retrieves the data for a resource path.
 *
 * @param path A relative file path.
 *
 * @return The data for the attachment.
 */
- (NSData*) dataForResource:(NSString*)path
{
    NSString* imgName = [[path pathComponents].lastObject
                         stringByDeletingPathExtension];

#ifdef __CORDOVA_4_0_0
    if ([imgName isEqualToString:@"icon"]) {
        imgName = @"AppIcon60x60@3x";
    }
#endif

    UIImage* img = [UIImage imageNamed:imgName];

    if (img == NULL) {
        NSLog(@"File not found: %@", path);
    }

    NSData* data = UIImagePNGRepresentation(img);

    return data;
}

/**
 * Retrieves the data for a asset path.
 *
 * @param path A relative www file path.
 *
 * @return The data for the attachment.
 */
- (NSData*) dataForAsset:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];
    NSString* absPath;

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [[mainBundle bundlePath]
                            stringByAppendingString:@"/"];

    absPath = [path stringByReplacingOccurrencesOfString:@"file:/"
                                              withString:@"www"];

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    NSData* data = [fm contentsAtPath:absPath];

    return data;
}

/**
 * Retrieves the file URL for an internal app path.
 *
 * @param path A relative file path from main bundle dir.
 *
 * @return The data for the attachment.
 */
- (NSData*) dataForAppInternalPath:(NSString*)path
{
    NSFileManager* fm = [NSFileManager defaultManager];

    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* absPath    = [mainBundle bundlePath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    NSData* data = [fm contentsAtPath:absPath];

    return data;
}

/**
 * Retrieves the data for a base64 encoded string.
 *
 * @param base64String Base64 encoded string.
 *
 * @return The data for the attachment.
 */
- (NSData*) dataFromBase64:(NSString*)base64String
{
    NSUInteger length = [base64String length];
    NSRegularExpression *regex;
    NSString *dataString;

    regex = [NSRegularExpression regularExpressionWithPattern:@"^base64:[^/]+.."
                                                      options:NSRegularExpressionCaseInsensitive
                                                        error:NULL];

    dataString = [regex stringByReplacingMatchesInString:base64String
                                                 options:0
                                                   range:NSMakeRange(0, length)
                                            withTemplate:@""];

    NSData* data = [[NSData alloc] initWithBase64EncodedString:dataString
                                                       options:0];

    return data;
}

/**
 * Retrieves the mime type from the file extension.
 *
 * @param extension The file's extension.
 *
 * @return The coresponding MIME type.
 */
- (NSString*) getMimeTypeFromFileExtension:(NSString*)extension
{
    if (!extension)
        return nil;

    // Get the UTI from the file's extension
    CFStringRef ext  = (CFStringRef)CFBridgingRetain(extension);
    CFStringRef type = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, ext, NULL);

    // Converting UTI to a mime type
    NSString *result = (NSString*)
    CFBridgingRelease(UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType));

    CFRelease(ext);
    CFRelease(type);

    return result;
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

@end
