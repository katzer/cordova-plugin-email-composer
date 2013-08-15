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

- (MFMailComposeViewController *) getEmailWwithProperties:(NSDictionary *)properties;
- (void) openViewControllerForEmail: (MFMailComposeViewController *)mail;
- (void) setSubjectOfEmail:(MFMailComposeViewController *)mail subject:(NSString *)subject;
- (void) setBodyOfMail:(MFMailComposeViewController *)mail body:(NSString *)body isHTML:(BOOL)isHTML;
- (void) setRecipientsOfEmail:(MFMailComposeViewController *)mail recipients:(NSArray *)recipients;
- (void) setCcRecipientsOfEmail:(MFMailComposeViewController *)mail ccRecipients:(NSArray *)ccRecipients;
- (void) setBccRecipientsOfEmail:(MFMailComposeViewController *)mail bccRecipients:(NSArray *)bccRecipients;
- (void) setAttachmentsOfEmail:(MFMailComposeViewController *)mail attatchments:(NSArray *)attatchments;
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
 * Öffnet den Email-Kontroller mit vorausgefüllten Daten
 */
- (void) open:(CDVInvokedUrlCommand *)command
{
    NSDictionary*                properties = [command.arguments objectAtIndex:0];
    MFMailComposeViewController* mail       = [self getEmailWwithProperties:properties];

    if (!mail)
    {
        [self callbackWithCode:APP_EMAIL_NOTSENT andCallbackId:command.callbackId];
    }

    // Hack, um später den Callback aufrufen zu können
    mail.title = command.callbackId;

    [self openViewControllerForEmail:mail];
}

/**
 * Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein.
 *
 * @param {NSDictionary*} properties
 * @return {MFMailComposeViewController*}
 */
- (MFMailComposeViewController *) getEmailWwithProperties:(NSDictionary *)properties
{
    // Falls das Gerät kein Email Interface unterstützt
    if (![MFMailComposeViewController canSendMail])
    {
        return NULL;
    }

    MFMailComposeViewController* mail = [[MFMailComposeViewController alloc] init];

    mail.mailComposeDelegate = self;

    // Subject
    [self setSubjectOfEmail:mail subject:[properties objectForKey:@"subject"]];
    // Body (as HTML)
    [self setBodyOfMail:mail body:[properties objectForKey:@"body"] isHTML:[[properties objectForKey:@"isHtml"] boolValue]];
    // Recipients
    [self setRecipientsOfEmail:mail recipients:[properties objectForKey:@"recipients"]];
    // CC Recipients
    [self setCcRecipientsOfEmail:mail ccRecipients:[properties objectForKey:@"ccRecipients"]];
    // BCC Recipients
    [self setBccRecipientsOfEmail:mail bccRecipients:[properties objectForKey:@"bccRecipients"]];
    // Attachments
    [self setAttachmentsOfEmail:mail attatchments:[properties objectForKey:@"attachments"]];

    return mail;
}

/**
 * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
 */
- (void) openViewControllerForEmail: (MFMailComposeViewController *)mail
{
    [self.viewController presentViewController:mail animated:YES completion:NULL];
}

/**
 * Setzt den Subject der Mail.
 */
- (void) setSubjectOfEmail:(MFMailComposeViewController *)mail subject:(NSString *)subject
{
    [mail setSubject:subject];
}

/**
 * Setzt den Body der Mail.
 */
- (void) setBodyOfMail:(MFMailComposeViewController *)mail body:(NSString *)body isHTML:(BOOL)isHTML
{
    [mail setMessageBody:body isHTML:isHTML];
}

/**
 * Setzt die Empfänger der Mail.
 */
- (void) setRecipientsOfEmail:(MFMailComposeViewController *)mail recipients:(NSArray *)recipients
{
    [mail setToRecipients:recipients];
}

/**
 * Setzt die CC-Empfänger der Mail.
 */
- (void) setCcRecipientsOfEmail:(MFMailComposeViewController *)mail ccRecipients:(NSArray *)ccRecipients
{
    [mail setCcRecipients:ccRecipients];
}

/**
 * Setzt die BCC-Empfänger der Mail.
 */
- (void) setBccRecipientsOfEmail:(MFMailComposeViewController *)mail bccRecipients:(NSArray *)bccRecipients
{
    [mail setBccRecipients:bccRecipients];
}

/**
 * Fügt die Anhände zur Mail hinzu.
 */
- (void) setAttachmentsOfEmail:(MFMailComposeViewController *)mail attatchments:(NSArray *)attatchments
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