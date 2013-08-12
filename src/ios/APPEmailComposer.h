/**
 *  APPEmailComposer.h
 *  Cordova Email Composition Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

#import <Foundation/Foundation.h>
#import <MessageUI/MFMailComposeViewController.h>
#import <Cordova/CDVPlugin.h>

@interface APPEmailComposer : CDVPlugin <MFMailComposeViewControllerDelegate> {

}

// Öffnet den Email-Kontroller mit vorausgefüllten Daten
- (void) send:(CDVInvokedUrlCommand*)command;

@end