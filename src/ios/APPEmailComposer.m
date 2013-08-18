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

- (MFMailComposeViewController *) getViewControllerWithProperties:(NSDictionary *)properties;
- (void) openViewController: (MFMailComposeViewController *)mail;
- (void) setSubject:(NSString *)subject ofMail:(MFMailComposeViewController *)mail;
- (void) setBody:(NSString *)body ofMail:(MFMailComposeViewController *)mail isHTML:(BOOL)isHTML;
- (void) setRecipients:(NSArray *)recipients ofMail:(MFMailComposeViewController *)mail;
- (void) setCcRecipients:(NSArray *)ccRecipients ofMail:(MFMailComposeViewController *)mail;
- (void) setBccRecipients:(NSArray *)bccRecipients ofMail:(MFMailComposeViewController *)mail;
- (void) addAttachments:(NSArray *)attatchments ofMail:(MFMailComposeViewController *)mail;
- (void) mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error;
- (void) callbackWithCode:(int)code andCallbackId:(NSString *)callbackId;
- (NSString *) getMimeTypeFromFileExtension:(NSString *)extension;

@end


@implementation APPEmailComposer

/**
 * Überprüft, ob Emails versendet werden können
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
    MFMailComposeViewController* controller = [self getViewControllerWithProperties:properties];

    if (!controller)
    {
        [self callbackWithCode:APP_EMAIL_NOTSENT andCallbackId:command.callbackId];
    }

    // Hack, um später den Callback aufrufen zu können
    controller.title = command.callbackId;

    [self openViewController:controller];
}

/**
 * Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein.
 *
 * @param {NSDictionary*} properties
 * @return {MFMailComposeViewController*}
 */
- (MFMailComposeViewController *) getViewControllerWithProperties:(NSDictionary *)properties
{
    // Falls das Gerät kein Email Interface unterstützt
    if (![MFMailComposeViewController canSendMail])
    {
        return NULL;
    }

    MFMailComposeViewController* mail = [[MFMailComposeViewController alloc] init];

    mail.mailComposeDelegate = self;

    // Subject
    [self setSubject:[properties objectForKey:@"subject"] ofMail:mail];
    // Body (as HTML)
    [self setBody:[properties objectForKey:@"body"] ofMail:mail isHTML:[[properties objectForKey:@"isHtml"] boolValue]];
    // Recipients
    [self setRecipients:[properties objectForKey:@"recipients"] ofMail:mail];
    // CC Recipients
    [self setCcRecipients:[properties objectForKey:@"ccRecipients"] ofMail:mail];
    // BCC Recipients
    [self setBccRecipients:[properties objectForKey:@"bccRecipients"] ofMail:mail];
    // Attachments
    [self addAttachments:[properties objectForKey:@"attachments"] ofMail:mail];

    return mail;
}

/**
 * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
 */
- (void) openViewController: (MFMailComposeViewController *)mail
{
    [self.viewController presentViewController:mail animated:YES completion:NULL];
}

/**
 * Setzt den Subject der Mail.
 */
- (void) setSubject:(NSString *)subject ofMail:(MFMailComposeViewController *)mail
{
    [mail setSubject:subject];
}

/**
 * Setzt den Body der Mail.
 */
- (void) setBody:(NSString *)body ofMail:(MFMailComposeViewController *)mail isHTML:(BOOL)isHTML
{
    [mail setMessageBody:body isHTML:isHTML];
}

/**
 * Setzt die Empfänger der Mail.
 */
- (void) setRecipients:(NSArray *)recipients ofMail:(MFMailComposeViewController *)mail
{
    [mail setToRecipients:recipients];
}

/**
 * Setzt die CC-Empfänger der Mail.
 */
- (void) setCcRecipients:(NSArray *)ccRecipients ofMail:(MFMailComposeViewController *)mail
{
    [mail setCcRecipients:ccRecipients];
}

/**
 * Setzt die BCC-Empfänger der Mail.
 */
- (void) setBccRecipients:(NSArray *)bccRecipients ofMail:(MFMailComposeViewController *)mail
{
    [mail setBccRecipients:bccRecipients];
}

/**
 * Fügt die Anhände zur Mail hinzu.
 */
- (void) addAttachments:(NSArray *)attatchments ofMail:(MFMailComposeViewController *)mail
{
    if (attatchments)
    {
        int counter = 1;

        for (NSString* path in attatchments)
        {
            NSData* data = [[NSFileManager defaultManager] contentsAtPath:path];

            [mail addAttachmentData:data mimeType:[self getMimeTypeFromFileExtension:[path pathExtension]] fileName:[NSString stringWithFormat:@"attachment%d.%@", counter, [path pathExtension]]];

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
 *
 * @param {NSString} extension
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