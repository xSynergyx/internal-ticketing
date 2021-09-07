# Internal Ticketing
A technical support ticket Android app designed to keep track of open issues, improve productivity and communication at the local library.

## Purpose
The main purpose of this project is to practice multiple skills while improving the workplace.
* Increase productivity and communication internally
* Improve overall app development skills
* Build upon Java knowledge 
* Explore database design and management
* Practice Git commands

## Requirements
1. openssl
2. keytool

## Libraries and APIs
* Microsoft Graph API

## Getting SHA-1 Certificate Fingerprint
* In Android Studio go into the gradle tab
* Then head to Project > Tasks > android and run the signingReport

### Alternatively
* Generate a signed bundle/APK
* Create new Keystore and key (Alternatively, use a previously set up keystore or debug.keystore)
	* Fill out key details (alias, passowrd, validity, etc)
	* Press okay.
	* Continue to signing the app if needed. Otherwise hit cancel as the keystore has already been generated
* Run this command ```keytool -keystore path-to-debug-or-production-keystore -list -v```
	* Note: Should be in the same directory as keytool.exe or provide its path instead of the "keytool" command. e.g. ```path-to-keytool.exe -keystore path-to-debug-or-production-keystore -list -v```
* Copy the SHA-1 certificate fingerprint