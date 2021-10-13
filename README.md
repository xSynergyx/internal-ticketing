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
4. PostgreSQL
5. Node.js
6. Express.js

## Libraries and APIs
* Microsoft Graph API
* Microsoft Authentication Library


## Getting SHA-1 Certificate Fingerprint
* In Android Studio go into the gradle tab
* Then head to [ProjectName] > Tasks > android and run the signingReport

### Alternatively
* Generate a signed bundle/APK
* Create new Keystore and key (Alternatively, use a previously set up keystore or debug.keystore)
	* Fill out key details (alias, passowrd, validity, etc) and press okay.
	* Continue to signing the app if needed. Otherwise hit cancel as the keystore has already been generated
* Run this command ```keytool -keystore path-to-debug-or-production-keystore -list -v```
	* Note: We should be in the same directory as keytool.exe or provide its path instead of the "keytool" command. e.g. ```path-to-keytool.exe -keystore path-to-debug-or-production-keystore -list -v```
* Copy the SHA-1 certificate fingerprint

## Connect to Microsoft Graph API

### I followed [this tutorial](https://docs.microsoft.com/en-us/azure/active-directory/develop/tutorial-v2-android) for authenticating with Microsoft Azure AD
* Head to the [Azure Active Directory admin center](https://login.microsoftonline.com/organizations/oauth2/v2.0/authorize?redirect_uri=https%3A%2F%2Faad.portal.azure.com%2Fsignin%2Findex%2F&response_type=code%20id_token&scope=https%3A%2F%2Fmanagement.core.windows.net%2F%2Fuser_impersonation%20openid%20email%20profile&state=OpenIdConnect.AuthenticationProperties%3Dc7ytCNP61TAWu2B1xJoVi1gSCdgixcAvVfzleU3fIV5BZRiSMuFFGil3cYFEg6s8SFh1YaxS0BBwLfTWVZLm9eM4zj_h4rCWpZjFSrJJamVcgNYQwJKTg9XgAJr1xfR-UzoZU579onnQkTicSuc2Aa5Wqsc3FjhP259GQRCVIV7ICnVeIJt5HnlfPDwmlArwxDCeUor77P4R4Qtnwh8CPWajxApECGeAOmqxNYlDb09PJgT4QcDzR9FJBclgyEFgMRZdxNuOiBurwJhhLLo1wt5D7WLjRs93Ziv5IC3232U_i7xtsr6snPT15udR981DdF27kg08E4QT4EEAhxB6pMqe6zXSsOyULz2QiD3xQkdKk3hqcMuETGOleVUVj4X6&response_mode=form_post&nonce=637672513817645611.YjA2MjYzNjgtN2JiMy00ZTRmLTkzMDQtODVmN2I2MTRlODE0ZmRkNmZmOGEtMzk0YS00OWEwLWE4OWItMzdiOGNjNjVmYTM0&client_id=c44b4083-3bb0-49c1-b47d-974e53cbdf3c&site_id=501430&client-request-id=5cab1b65-00e0-4118-b736-e318fc1cdacb&x-client-SKU=ID_NET472&x-client-ver=6.11.0.0)
* Click Azure Active Directory > App Registrations > New Registration
* For a mobile app the redirect URI should be msauth://[yourpackagename]/[yoursignaturehash]
* We'll need the auth config json file, the browser tab activity in our Android Manifest file, and the microsoft authentication library dependency
* All of the code needed to connect and make a Graph API call is in the tutorial

## Setting up a cloud server
If you're following along to set up this project from start to finish you may select any server and RDBMS to work with your app.
For simplicity and to get the project going quickly, I've decided to go with Digital Ocean's cloud services for now.
They have some great and easy-to-follow tutorials
* I chose to connect to the server using SSH. I used [this tutorial](https://docs.digitalocean.com/products/droplets/how-to/connect-with-ssh/putty/) to create the key with PuTTY. You may also connect using [OpenSSH](https://docs.digitalocean.com/products/droplets/how-to/connect-with-ssh/openssh/)
* [This tutorial](https://www.digitalocean.com/community/tutorials/initial-server-setup-with-ubuntu-20-04) shows how to quickly set up your server, create a user and set up a firewall
* Finally, [this tutorial](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-20-04) shows you how to install PostgreSQL and use some of the basic commands in the terminal

## Set up the database
Note: If you followed the tutorial on seting up a server with DigitalOcean and have a firewall up, you'll have to open a port to receive HTTP requests
* Set up [Express.js](http://expressjs.com/en/starter/installing.html)
* Connect [Express to PostgreSQL](https://expressjs.com/en/guide/database-integration.html#postgresql)

<details>
<summary>RESTful Web Service</summary>
* <code>curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt install nodejs</code>
* ```npm install pg-promise```
* ```npm install dotenv```
</details>