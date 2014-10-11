<#
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
#>

param(
    [Parameter(Mandatory=$true, Position=0, ValueFromPipelineByPropertyName=$true)]
    [string] $platformRoot
)

Write-Host "Applying Platform Config..."

Function UpdateManifest ($manifestFile)
{
  [xml]$manifest = Get-Content $manifestFile -Encoding UTF8

  # Replace app start page with config.xml setting.

  $startPage = $config.widget.content.src
  if (-not $startPage) {
    # If not specified, set default value
    # http://cordova.apache.org/docs/en/edge/config_ref_index.md.html#The%20config.xml%20File
    $startPage = "index.html"
  }
  $manifest.Package.Applications.Application.StartPage = "www/$startpage"

  # Update app name & version

  # update identity name
  $identityname = $config.widget.id
  if ($identityname) {
    $manifest.Package.Identity.Name = $identityname
    $manifest.Package.Applications.Application.Id = $identityname
  }

  # update app display name
  $displayname = $config.widget.name

  $manifestDisplayName = $manifest.Package.Properties.DisplayName
  if ($displayname -and $manifestDisplayName) {
    $manifestDisplayName = $displayname
  }

  $visualelems = $manifest.Package.Applications.Application.VisualElements
  if ($displayname -and $visualelems) {
    $visualelems.DisplayName = $displayname
  }

  # update publisher display name
  $publisher = $config.widget.author.InnerText
  $manifestPublisher = $manifest.Package.Properties.PublisherDisplayName
  if ($publisher -and $manifestPublisher) {
    $manifestPublisher = $publisher.InnerText
  }

  # Adjust version number as per CB-5337 Windows8 build fails due to invalid app version
  $version = $config.widget.version
  if ($version -and $version -match "\." ) {
      while ($version.Split(".").Length -lt 4) {
          $version = $version + ".0"
      }
      $manifest.Package.Identity.Version = $version
  }

  # Sort capabilities elements
  $capabilities = $manifest.Package.Capabilities
  if ($capabilities) {
    # Get sorted list of elements
    $sorted = $capabilities.ChildNodes | sort
    # Remove parent node
    $manifest.Package.RemoveChild($capabilities) | Out-Null
    # Create new node
    $capabilities = $manifest.CreateElement("Capabilities", $manifest.DocumentElement.NamespaceURI)
    # Then add sorted children
    $sorted | foreach {
        $Capabilities.AppendChild($_) | Out-Null
    }
    $manifest.Package.AppendChild($capabilities) | Out-Null
  }

  # Add domain whitelist rules
  $rules = $manifest.Package.Applications.Application.ApplicationContentUriRules

  # Remove existing rules from manifest
  if ($rules) {
    $manifest.Package.Applications.Application.RemoveChild($rules) | Out-Null
  }
  if ($acls -and ($acls -notcontains "*")) {
    $rules = $manifest.CreateElement("ApplicationContentUriRules", $manifest.DocumentElement.NamespaceURI)
    $manifest.Package.Applications.Application.AppendChild($rules) | Out-Null
    $acls | foreach {
      $elem = $manifest.CreateElement("Rule", $manifest.DocumentElement.NamespaceURI)
      $elem.SetAttribute("Match", $_)
      $elem.SetAttribute("Type", "include")
      $rules.AppendChild($elem) | Out-Null
    }
  }

  # Format splash screen background color to windows8 format
  $configSplashScreenBGColor = $config.SelectNodes('//*[local-name()="preference"][@name="SplashScreenBackgroundColor"]').value
  if($configSplashScreenBGColor) 
  {
    "Setting SplashScreenBackgroundColor = $configSplashScreenBGColor"

    $bgColor = ($configSplashScreenBGColor -replace "0x", "") -replace "#", ""

    # Double all bytes if color specified as "fff"
    if ($bgColor.Length -eq 3) {
      $bgColor = $bgColor[0] + $bgColor[0] + $bgColor[1] + $bgColor[1] + $bgColor[2] + $bgColor[2] 
    }

    # Parse hex representation to array of color bytes [b, g, r, a]
    $colorBytes = [System.BitConverter]::GetBytes(
      [int]::Parse($bgColor,
      [System.Globalization.NumberStyles]::HexNumber))

    Add-Type -AssemblyName PresentationCore

    # Create new Color object ignoring alpha, because windows 8 doesn't support it
    # see http://msdn.microsoft.com/en-us/library/windows/apps/br211471.aspx
    $color = ([System.Windows.Media.Color]::FromRgb(
      $colorBytes[2], $colorBytes[1], $colorBytes[0]
      # FromRGB method add 100% alpha, so we remove it from resulting string
      ).ToString()) -replace "#FF", "#"

    $manifest.Package.Applications.Application.VisualElements.SplashScreen.BackgroundColor = [string]$color
  }

  # Format background color to windows format

  $configBgColor = $config.SelectNodes('//*[local-name()="preference"][@name="BackgroundColor"]').value
  if ($configBgColor) {
    $bgColor = ($configBgColor -replace "0x", "") -replace "#", ""

    # Double all bytes if color specified as "fff"
    if ($bgColor.Length -eq 3) {
      $bgColor = $bgColor[0] + $bgColor[0] + $bgColor[1] + $bgColor[1] + $bgColor[2] + $bgColor[2] 
    }

    # Parse hex representation to array of color bytes [b, g, r, a]
    $colorBytes = [System.BitConverter]::GetBytes(
      [int]::Parse($bgColor, [System.Globalization.NumberStyles]::HexNumber)
    )

    Add-Type -AssemblyName PresentationCore

    # Create new Color object ignoring alpha, because windows doesn't support it
    # see http://msdn.microsoft.com/en-us/library/windows/apps/dn423310.aspx
    $color = ([System.Windows.Media.Color]::FromRgb(
      $colorBytes[2], $colorBytes[1], $colorBytes[0]
      # FromRGB method add 100% alpha, so we remove it from resulting string
      ).ToString()) -replace "#FF", "#"

    $manifest.Package.Applications.Application.VisualElements.BackgroundColor = [string]$color
  }

  # Write out manifest file

  $xmlWriter = New-Object System.Xml.XmlTextWriter($manifestFile, $null)
  $xmlWriter.Formatting = "Indented"
  $xmlWriter.Indentation = 4
  $manifest.WriteContentTo($xmlWriter)
  $xmlWriter.Close()
}

Function CopyImage($src, $dest) 
{
  $resolvedPath = $null;

  # do search relative to platform and app folders
  foreach ($testPath in @($src, "..\..\$src")) 
  {
    $testPath = join-path $platformRoot $testPath
    if (Test-Path -PathType Leaf $testPath)
    {
      $resolvedPath = $testPath;
      break
    }
  }

  if ($resolvedPath -eq $null)
  {
      Write-Host "Image doesn't exist: $src"
      return
  }
  Copy-Item $resolvedPath -Destination (join-path $platformRoot $dest)
}

Function Get-Access-Rules () 
{
  $aclsAll = ([string[]]$config.widget.access.origin)
  if ($aclsAll -eq $null) {
    return @();
  }

  $acls = @()
 
  $aclsAll | foreach {
      if ($_.StartsWith("https://","CurrentCultureIgnoreCase") -or ($_ -eq "*")) {
        $acls += $_
      } else {
        Write-Host "Access rules must begin with 'https://', the following rule is ignored: $_"
      }
    }
  return $acls
}

Function UpdateAssets ()
{
  $configFile = "$platformRoot\config.xml"
  [xml]$config = Get-Content $configFile

  # Splash screen images
  $splashScreens = $config.SelectNodes('//*[local-name()="splash"]')

  foreach ($splash in $splashScreens)
  {
    $width = $splash.getAttribute("width")
    $height= $splash.getAttribute("height")
    $src = $splash.getAttribute("src")
    if ($width -eq 620 -and $height -eq 300) {
      CopyImage $src "images/splashscreen.png"
      continue
    }
    if ($width -eq 1152 -and $height -eq 1920) {
      CopyImage $src "images/SplashScreen.scale-240.png"
      continue
    }
     Write-Host "Unknown image ($src) size, skip"
  }

  # App icons
  $configIcons= $config.SelectNodes('//*[local-name()="icon"]')

  foreach ($icon in $configIcons)
  {
    $width = $icon.getAttribute("width")
    $height= $icon.getAttribute("height")
    $src = $icon.getAttribute("src")

    if ($width -eq 150) {
      CopyImage $src "images/logo.png"
      continue
    }
    if ($width -eq 30) {
      CopyImage $src "images/smalllogo.png"
      continue
    }
    if ($width -eq 50) {
      CopyImage $src "images/storelogo.png"
      continue
    }
    if ($width -eq 120) {
      CopyImage $src "images/StoreLogo.scale-240.png"
      continue
    }

    if ($width -eq 106) {
      CopyImage $src "images/Square44x44Logo.scale-240.png"
      continue
    }
    if ($width -eq 170) {
      CopyImage $src "images/Square71x71Logo.scale-240.png"
      continue
    }
    if ($width -eq 360) {
      CopyImage $src "images/Square150x150Logo.scale-240.png"
      continue
    }
    if ($width -eq 744 -and $height -eq 360) {
      CopyImage $src "images/Wide310x150Logo.scale-240.png"
      continue
    }

     Write-Host "Unknown image ($src) size, skip"
  }

}

$configFile = "$platformRoot\config.xml"
[xml]$config = Get-Content $configFile -Encoding UTF8

# Domain whitelist rules defined in configuration file
$acls = Get-Access-Rules $config

UpdateManifest "$platformRoot\package.windows.appxmanifest"
UpdateManifest "$platformRoot\package.windows80.appxmanifest"
UpdateManifest "$platformRoot\package.phone.appxmanifest"

# replace splash screen images and icons
UpdateAssets
