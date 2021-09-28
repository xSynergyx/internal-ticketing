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
1. Android Studio
2. openssl
3. keytool

## Libraries and APIs
* Microsoft Graph API
* Microsoft Authentication Library

## Getting SHA-1 Certificate Fingerprint
* In Android Studio go into the gradle tab
* Then head to Project > Tasks > android and run the signingReport

### Alternatively
* Generate a signed bundle/APK
* Create new Keystore and key (Alternatively, use a previously set up keystore or debug.keystore)
	* Fill out key details (alias, passowrd, validity, etc) and press okay.
	* Continue to signing the app if needed. Otherwise hit cancel as the keystore has already been generated
* Run this command ```keytool -keystore path-to-debug-or-production-keystore -list -v```
	* Note: We should be in the same directory as keytool.exe or provide its path instead of the "keytool" command. e.g. ```path-to-keytool.exe -keystore path-to-debug-or-production-keystore -list -v```
* Copy the SHA-1 certificate fingerprint

## Connect to Microsoft Graph API

### I followed [this](https://docs.microsoft.com/en-us/azure/active-directory/develop/tutorial-v2-android) tutorial for authenticating with Microsoft Azure AD.
* Head to the [Azure Active Directory admin center](https://login.microsoftonline.com/organizations/oauth2/v2.0/authorize?redirect_uri=https%3A%2F%2Faad.portal.azure.com%2Fsignin%2Findex%2F&response_type=code%20id_token&scope=https%3A%2F%2Fmanagement.core.windows.net%2F%2Fuser_impersonation%20openid%20email%20profile&state=OpenIdConnect.AuthenticationProperties%3Dc7ytCNP61TAWu2B1xJoVi1gSCdgixcAvVfzleU3fIV5BZRiSMuFFGil3cYFEg6s8SFh1YaxS0BBwLfTWVZLm9eM4zj_h4rCWpZjFSrJJamVcgNYQwJKTg9XgAJr1xfR-UzoZU579onnQkTicSuc2Aa5Wqsc3FjhP259GQRCVIV7ICnVeIJt5HnlfPDwmlArwxDCeUor77P4R4Qtnwh8CPWajxApECGeAOmqxNYlDb09PJgT4QcDzR9FJBclgyEFgMRZdxNuOiBurwJhhLLo1wt5D7WLjRs93Ziv5IC3232U_i7xtsr6snPT15udR981DdF27kg08E4QT4EEAhxB6pMqe6zXSsOyULz2QiD3xQkdKk3hqcMuETGOleVUVj4X6&response_mode=form_post&nonce=637672513817645611.YjA2MjYzNjgtN2JiMy00ZTRmLTkzMDQtODVmN2I2MTRlODE0ZmRkNmZmOGEtMzk0YS00OWEwLWE4OWItMzdiOGNjNjVmYTM0&client_id=c44b4083-3bb0-49c1-b47d-974e53cbdf3c&site_id=501430&client-request-id=5cab1b65-00e0-4118-b736-e318fc1cdacb&x-client-SKU=ID_NET472&x-client-ver=6.11.0.0)
* Click Azure Active Directory > App Registrations > New Registration
* For a mobile app the redirect URI should be msauth://[yourpackagename]/[yoursignaturehash]
* We'll need the auth config json file, the browser tab activity in our Android Manifest file, and the microsoft authentication library dependency
* All of the code needed to connect and make a Graph API call is in the tutorial

## Set up the database