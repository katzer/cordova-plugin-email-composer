/**
 *  APPEmailComposer.m
 *  Cordova Email Composition Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

#define APP_EMAIL_CANCELLED 0  // Email composition cancelled (cancel button pressed and draft not saved)
#define APP_EMAIL_SAVED     1  // Email saved (cancel button pressed but draft saved)
#define APP_EMAIL_SENT      2  // Email sent
#define APP_EMAIL_FAILED    3  // Send failed
#define APP_EMAIL_NOTSENT   4  // Email not sent (something wrong happened)


#import "APPEmailComposer.h"
#import <MobileCoreServices/MobileCoreServices.h>


@interface APPEmailComposer (Private)

- (MFMailComposeViewController *) getDraftWithProperties:(NSDictionary *)properties;
- (void) openDraft: (MFMailComposeViewController *)draft;
- (void) setSubject:(NSString *)subject ofDraft:(MFMailComposeViewController *)draft;
- (void) setBody:(NSString *)body ofDraft:(MFMailComposeViewController *)draft isHTML:(BOOL)isHTML;
- (void) setRecipients:(NSArray *)recipients ofDraft:(MFMailComposeViewController *)draft;
- (void) setCcRecipients:(NSArray *)ccRecipients ofDraft:(MFMailComposeViewController *)draft;
- (void) setBccRecipients:(NSArray *)bccRecipients ofDraft:(MFMailComposeViewController *)draft;
- (void) setAttachments:(NSArray *)attatchments ofDraft:(MFMailComposeViewController *)draft;
- (void) mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error;
- (void) callbackWithCode:(int)code andCallbackId:(NSString *)callbackId;
- (NSString *) getMimeTypeFromFileExtension:(NSString *)extension;

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
- (void) open:(CDVInvokedUrlCommand *)command
{
    NSDictionary*                properties = [command.arguments objectAtIndex:0];
    MFMailComposeViewController* controller = [self getDraftWithProperties:properties];

    if (!controller)
    {
        [self callbackWithCode:APP_EMAIL_NOTSENT andCallbackId:command.callbackId];
    }

    // Hack, um später den Callback aufrufen zu können
    controller.title = command.callbackId;

    [self openDraft:controller];
}

/**
 * Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein.
 */
- (MFMailComposeViewController *) getDraftWithProperties:(NSDictionary *)properties
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
    [self setRecipients:[properties objectForKey:@"recipients"] ofDraft:draft];
    // CC Recipients
    [self setCcRecipients:[properties objectForKey:@"ccRecipients"] ofDraft:draft];
    // BCC Recipients
    [self setBccRecipients:[properties objectForKey:@"bccRecipients"] ofDraft:draft];
    // Attachments
    [self setAttachments:[properties objectForKey:@"attachments"] ofDraft:draft];

    return draft;
}

/**
 * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
 */
- (void) openDraft: (MFMailComposeViewController *)draft
{
    [self.viewController presentViewController:draft animated:YES completion:NULL];
}

/**
 * Setzt den Subject der Mail.
 */
- (void) setSubject:(NSString *)subject ofDraft:(MFMailComposeViewController *)draft
{
    [draft setSubject:subject];
}

/**
 * Setzt den Body der Mail.
 */
- (void) setBody:(NSString *)body ofDraft:(MFMailComposeViewController *)draft isHTML:(BOOL)isHTML
{
    [draft setMessageBody:body isHTML:isHTML];
}

/**
 * Setzt die Empfänger der Mail.
 */
- (void) setRecipients:(NSArray *)recipients ofDraft:(MFMailComposeViewController *)draft
{
    [draft setToRecipients:recipients];
}

/**
 * Setzt die CC-Empfänger der Mail.
 */
- (void) setCcRecipients:(NSArray *)ccRecipients ofDraft:(MFMailComposeViewController *)draft
{
    [draft setCcRecipients:ccRecipients];
}

/**
 * Setzt die BCC-Empfänger der Mail.
 */
- (void) setBccRecipients:(NSArray *)bccRecipients ofDraft:(MFMailComposeViewController *)draft
{
    [draft setBccRecipients:bccRecipients];
}

/**
 * Fügt die Anhände zur Mail hinzu.
 */
- (void) setAttachments:(NSArray *)attatchments ofDraft:(MFMailComposeViewController *)draft
{
    if (attatchments)
    {
        int counter = 1;

        for (NSString* path in attatchments)
        {
            NSData* data = [[NSFileManager defaultManager] contentsAtPath:path];

            [draft addAttachmentData:data mimeType:[self getMimeTypeFromFileExtension:[path pathExtension]] fileName:[NSString stringWithFormat:@"attachment%d.%@", counter, [path pathExtension]]];

            counter++;
        }
    }
}


/**
 * @delegate
 */
- (void) mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error
{
    NSString* callbackId = controller.title;

    [controller dismissViewControllerAnimated:YES completion:nil];

    switch (result)
    {
        case MFMailComposeResultCancelled:
            [self callbackWithCode:APP_EMAIL_CANCELLED andCallbackId:callbackId];
            break;
        case MFMailComposeResultSaved:
            [self callbackWithCode:APP_EMAIL_SAVED andCallbackId:callbackId];
            break;
        case MFMailComposeResultSent:
            [self callbackWithCode:APP_EMAIL_SENT andCallbackId:callbackId];
            break;
        case MFMailComposeResultFailed:
            [self callbackWithCode:APP_EMAIL_FAILED andCallbackId:callbackId];
            break;
        default:
            [self callbackWithCode:APP_EMAIL_NOTSENT andCallbackId:callbackId];
            break;
    }
}

/**
 * Calls the callback with the specified code.
 */
- (void) callbackWithCode:(int)code andCallbackId:(NSString *)callbackId
{
    CDVPluginResult* pluginResult;

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                     messageAsString:[@(code) stringValue]];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

/**
 * Retrieves the mime type from the file extension.
 */
- (NSString *) getMimeTypeFromFileExtension:(NSString *)extension
{
    if (!extension)
        return nil;

    // Get the UTI from the file's extension
    CFStringRef pathExtension = (CFStringRef)CFBridgingRetain(extension);
    CFStringRef type          = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension, NULL);

    // Converting UTI to a mime type
    return (NSString *)CFBridgingRelease(UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType));
}

@end