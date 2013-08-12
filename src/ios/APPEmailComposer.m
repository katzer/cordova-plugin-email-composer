/**
 *  APPEmailComposer.m
 *  Cordova Email Composer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

#define APP_EMAIL_CANCELLED 0 // email composition cancelled (cancel button pressed and draft not saved)
#define APP_EMAIL_SAVED 1     // email saved (cancel button pressed but draft saved)
#define APP_EMAIL_SENT 2      // email sent
#define APP_EMAIL_FAILED 3    // send failed
#define APP_EMAIL_NOTSENT 4   // email not sent (something wrong happened)


#import "APPEmailComposer.h"
#import <MobileCoreServices/MobileCoreServices.h>


@interface APPEmailComposer (Private)

- (void) send:(CDVInvokedUrlCommand *)command;
- (NSString *) getMimeTypeFromFileExtension:(NSString *)extension;

@end


@implementation APPEmailComposer

/**
 * Öffnet den Email-Kontroller mit vorausgefüllten Daten
 */
-(void) send:(CDVInvokedUrlCommand *)command
{
    NSDictionary *parameters = [command.arguments objectAtIndex:0];

    [self showEmailComposerWithParameters:parameters];
}

-(void) showEmailComposerWithParameters:(NSDictionary*)parameters {

    MFMailComposeViewController *mailComposer = [[MFMailComposeViewController alloc] init];
    mailComposer.mailComposeDelegate = self;

	// set subject
    @try {
        NSString* subject = [parameters objectForKey:@"subject"];
        if (subject) {
            [mailComposer setSubject:subject];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"EmailComposer - Cannot set subject; error: %@", exception);
    }

    // set body
    @try {
        NSString* body = [parameters objectForKey:@"body"];
        BOOL isHTML = [[parameters objectForKey:@"bIsHTML"] boolValue];
        if(body) {
            [mailComposer setMessageBody:body isHTML:isHTML];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"EmailComposer - Cannot set body; error: %@", exception);
    }

	// Set recipients
    @try {
        NSArray* toRecipientsArray = [parameters objectForKey:@"toRecipients"];
        if(toRecipientsArray) {
            [mailComposer setToRecipients:toRecipientsArray];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"EmailComposer - Cannot set TO recipients; error: %@", exception);
    }

    @try {
        NSArray* ccRecipientsArray = [parameters objectForKey:@"ccRecipients"];
        if(ccRecipientsArray) {
            [mailComposer setCcRecipients:ccRecipientsArray];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"EmailComposer - Cannot set CC recipients; error: %@", exception);
    }

    @try {
        NSArray* bccRecipientsArray = [parameters objectForKey:@"bccRecipients"];
        if(bccRecipientsArray) {
            [mailComposer setBccRecipients:bccRecipientsArray];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"EmailComposer - Cannot set BCC recipients; error: %@", exception);
    }

	@try {
        int counter = 1;
        NSArray *attachmentPaths = [parameters objectForKey:@"attachments"];
        if (attachmentPaths) {
            for (NSString* path in attachmentPaths) {
                @try {
                    NSData *data = [[NSFileManager defaultManager] contentsAtPath:path];
                    [mailComposer addAttachmentData:data mimeType:[self getMimeTypeFromFileExtension:[path pathExtension]] fileName:[NSString stringWithFormat:@"attachment%d.%@", counter, [path pathExtension]]];
                    counter++;
                }
                @catch (NSException *exception) {
                    DLog(@"Cannot attach file at path %@; error: %@", path, exception);
                }
            }
        }
    }
    @catch (NSException *exception) {
        NSLog(@"EmailComposer - Cannot set attachments; error: %@", exception);
    }

    if (mailComposer != nil) {
        [self.viewController presentModalViewController:mailComposer animated:YES];
    } else {
        [self returnWithCode:RETURN_CODE_EMAIL_NOTSENT];
    }
    [mailComposer release];
}


// Dismisses the email composition interface when users tap Cancel or Send.
// Proceeds to update the message field with the result of the operation.
- (void)mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error {
    // Notifies users about errors associated with the interface
	int webviewResult = 0;

    switch (result) {
        case MFMailComposeResultCancelled:
			webviewResult = RETURN_CODE_EMAIL_CANCELLED;
            break;
        case MFMailComposeResultSaved:
			webviewResult = RETURN_CODE_EMAIL_SAVED;
            break;
        case MFMailComposeResultSent:
			webviewResult =RETURN_CODE_EMAIL_SENT;
            break;
        case MFMailComposeResultFailed:
            webviewResult = RETURN_CODE_EMAIL_FAILED;
            break;
        default:
			webviewResult = RETURN_CODE_EMAIL_NOTSENT;
            break;
    }

    [controller dismissModalViewControllerAnimated:YES];
    [self returnWithCode:webviewResult];
}

// Call the callback with the specified code
-(void) returnWithCode:(int)code {
    [self writeJavascript:[NSString stringWithFormat:@"window.plugins.emailComposer._didFinishWithResult(%d);", code]];
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

    CFStringRef pathExtension, type;

    // Get the UTI from the file's extension
    pathExtension = (CFStringRef)extension;
    type          = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension, NULL);

    // Converting UTI to a mime type
   return (NSString *)UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType);
}

@end