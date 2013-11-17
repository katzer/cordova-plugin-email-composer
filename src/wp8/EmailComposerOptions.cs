using System;
using System.Linq;
using System.Runtime.Serialization;

namespace De.APPPlant.Cordova.Plugin.EmailComposer
{
    /// <summary>
    /// Represents email composer task options
    /// </summary>
    [DataContract]
    class EmailComposerOptions
    {
        /// <summary>
        /// Represents the subject of the email
        /// </summary>
        [DataMember(IsRequired = false, Name = "subject")]
        public string Subject { get; set; }

        /// <summary>
        /// Represents the email body (could be HTML code, in this case set isHtml to true)
        /// </summary>
        [DataMember(IsRequired = false, Name = "body")]
        public string Body { get; set; }

        /// <summary>
        /// Indicats if the body is HTML or plain text
        /// </summary>
        [DataMember(IsRequired = false, Name = "isHtml")]
        public bool IsHtml { get; set; }

        /// <summary>
        /// Contains all the email addresses for TO field
        /// </summary>
        [DataMember(IsRequired = false, Name = "to")]
        public string To { get; set; }

        /// <summary>
        /// Contains all the email addresses for CC field
        /// </summary>
        [DataMember(IsRequired = false, Name = "cc")]
        public string Cc { get; set; }

        /// <summary>
        /// Contains all the email addresses for BCC field
        /// </summary>
        [DataMember(IsRequired = false, Name = "bcc")]
        public string Bcc { get; set; }

        /// <summary>
        /// Contains all full paths to the files you want to attach
        /// </summary>
        [DataMember(IsRequired = false, Name = "attachments")]
        public string[] Attachments { get; set; }
    }
}
