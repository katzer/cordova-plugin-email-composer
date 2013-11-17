/**
 *  EmailComposer.cs
 *  Cordova EmailComposer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 07/10/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

using De.APPPlant.Cordova.Plugin.EmailComposer;
using Microsoft.Phone.Tasks;
using System;
using System.Linq;
using WPCordovaClassLib.Cordova;
using WPCordovaClassLib.Cordova.Commands;
using WPCordovaClassLib.Cordova.JSON;

namespace Cordova.Extension.Commands
{
    /// <summary>
    /// Implementes access to email composer task
    /// http://msdn.microsoft.com/en-us/library/windowsphone/develop/hh394003(v=vs.105).aspx
    /// </summary>
    public class EmailComposer : BaseCommand
    {
        /// <summary>
        /// Überprüft, ob Emails versendet werden können.
        /// </summary>
        public void isServiceAvailable (string jsonArgs)
        {
            DispatchCommandResult(new PluginResult(PluginResult.Status.OK, true));
        }

        /// <summary>
        /// Öffnet den Email-Kontroller mit vorausgefüllten Daten.
        /// </summary>
        public void open (string jsonArgs)
        {
            string[] args                = JsonHelper.Deserialize<string[]>(jsonArgs);
            EmailComposerOptions options = JsonHelper.Deserialize<EmailComposerOptions>(args[0]);
            EmailComposeTask draft       = new EmailComposeTask();

            draft.Subject = options.Subject;
            draft.Body    = options.Body;
            draft.To      = options.Recipients;
            draft.Cc      = options.CcRecipients;
            draft.Bcc     = options.BccRecipients;

            draft.Show();

            DispatchCommandResult();
        }
    }
}
