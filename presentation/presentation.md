# Android Fingerprint Authentication In Action

## Ben Oberkfell

### @benlikestocode
### http://benlikestoco.de

---

# On Tap

* Why/Where to Use Fingerprint
* How to Authenticate
* Designing It Right

^We're going to talk about how and where to use fingerprint on Android, how to do it in code, and how to design it right.

---

![](images/nexus6p.jpg)

^Show of hands, how many of you have a phone that supports fingerprint?  Keep them up.

---

![](images/carlsjr.jpg)

^Keep them up still if you've left a one star review for lack of fingerprint support. 

---

![](images/carlsjr.jpg)

# Real One Star Reviews From Real People

* When is the fingerprint update coming?

* It is inconvenient for me to enter my long password with lower, uppercase letters and numbers every time.

* Please add touch ID authentication. I am sick and tired of entering my password 10 times in a day.

^Because you're likely to start getting them. 

---

![fit](images/version-dashboard.png)

^Almost 1/5 of Android devices have M on them now. 

---

# [fit]Bottom Line
# [fit]This stuff is becoming table stakes, so let's learn how to do it!

^Bottom line is this is going to become table stakes, so let's learn how to do it.  If you're already here
in the room you probably don't need any further convincing from me.

---

## Where Would You Want To Use Fingerprint?

^Where would you want to use your fingerprint?

---

## Logins

### Save the Trouble of Entering Passwords

^You definitely want to avoid your users having to enter passwords over and over again.

---

## Protect Critical User Flows

### Things that Cost Money, Personal Security, etc

^You've seen how Google Play requests a fingerprint before making a purchase. 

How many of you have had your kids accidentally buy something?  Fingerprint helps!

---

# The Hard Part

## How Do You Communicate a Successful Fingerprint Scan?

^The question is, if you protect an interaction with fingerprint, how do you communicate this?

---

# Would You Trust This?   

```javascript
POST /dominos
{
    "item": "Pepperoni Pizza",
    "quantity": 500,
    "deliveryAddress" : "1600 Pennsylvania Avenue",
    "fingerprintValidated" : true
}
```

If the goal is to protect the order flow, nope! :pizza:

^We can't just send over in our JSON request a key saying "hey we got a good fingerprint."  How can you trust that? I could just be 
posting that with curl.

---

![](images/embosser.jpg)

^Well, what do we do in real life when we need to certify something?  I just sold my house last week.  I signed a bazillion documents
and a notary used their seal on them.  The notary's seal indicates they checked your ID and observed you sign the document.  
Their credentials are verifiable.  So that notary seal is a verifiable piece of proof that it was me who signed all those papers.

---

# What's In The Fingerprint Scanner?
If a device has a fingerprint reader and provides a developer-facing API, per the Compatibility Definition Document it has to 

* have a hardware backed KeyStore
* gate keys usage via fingerprint

^The Compatibility Definition Document is Google's list of qualifiers for devices. You can do any AOSP build you want, but 
in order for Google to allow a device to have the Play Store on it, it has to meet these criteria.

---

# What Does This Mean For Us?

We can create encryption keys where the key cannot be used without authenticating with the fingerprint reader. 

---

# What Does This Mean For Us?

We can create encryption keys where the key cannot be used without authenticating with the fingerprint reader.

... which means if we can use the key, we know we successfully scanned an authorized fingerprint. 

---

# What Does This Mean For Us?

We can create encryption keys where the key cannot be used without authenticating with the fingerprint reader.

... which means if we can use the key, we know we successfully scanned an authorized fingerprint. 

... so how do we use this for authentication?

---

# Step 1
## Create a Public/Private Key Pair
### Make it require authentication to use
---

# Step 2
## Register the Public Key with Your Backend
### And associate it with your user
---

# Step 3
## Sign Your Critical Requests with the Private Key

---

# Step 4
## Your Backend Verifies the Signed Request

---

# Step 5
## Happy Customers

---

#Create the Key Pair

```java

keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
keyPairGenerator.initialize(
            new KeyGenParameterSpec.Builder("DemoKey", PURPOSE_SIGN)
                    .setKeySize(2048)
                    .setDigests(DIGEST_SHA256)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    // Here's the most important part. This enforces authentication before the private
                    // key can be used.
                    .setUserAuthenticationRequired(true)
                .build());

keyPairGenerator.generateKeyPair();

```

^So here with this block of code we create a key suitable for signing.  The kicker here to pay attention to
is the setUserAuthenticationRequired.  This means we can't use this key without the fingerprint reader giving us 
the high sign.              

---

#Signing A Request

```java
public String signString(String data) {
    KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
    keyStore.load(null);
    PublicKey publicKey = keyStore.getCertificate(KEY_NAME).getPublicKey();
    PrivateKey privateKey = keyStore.getKey(KEY_NAME, null);
    
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(getPrivateKey());   
    signature.update(dataToSign.getBytes("UTF8"))

    byte[] bytes = cryptoObject.getSignature().sign();
    return Base64.encodeToString(bytes, Base64.NO_WRAP);
}

```

... but this won't work since we're not authenticated!

^Here's how we'd go ahead and sign this.  We'd get our key back out, create a Signature object and give it the 
key and the value of what we want to sign, and then we get back base64 encoded representation of the signature.

^But this will fail outright since we don't have authentication done.

---

#Authenticating First

Some Prereqs

* We'd use the `FingerprintManager` class to manage the fingerprint sensor.
* But to make life easy for pre-API 23, use `FingerprintManagerCompat`.
* Call for the `USE_FINGERPRINT` permission in your manifest.  It'll be auto-granted.

^So the FingerprintManager class helps us with the fingerprint reader.  We can use this to determine whether
there is fingerprint hardware available and whether there are fingerprints enrolled.

^FingerprintManagerCompat also exists for pre-M, use it.  It makes your life easier.

---

#Checking hardware

* Check for whether fingerprint reader exists

```java
boolean hasHardware = fingerprintManager.isHardwareDetected();
```

* Check for fingerprints added to the device 

```java
boolean hasFingerprints = fingerprintManager.hasEnrolledFingerprints();
```

^FingerprintManager has a couple methods for helping you decide whether to show fingerprint options at all,
or to tell your user to go enroll some fingerprints.

---

#Authenticating First

```java

public void requestFingerprintAuth(Signature signature, 
                                   FingerprintManagerCompat.AuthenticationCallback callback) {

    CryptoObject cryptoObject = new CryptoObject(signature);
    CancellationSignal cancellationSignal = new CancellationSignal();

    FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(context);

    fingerprintManager.authenticate(cryptoObject, flags, cancellationSignal, callback, null); 

}
``` 

^ Here is how we light up the fingerprint scanner and start listening for fingerprints.  We 
create a CryptoObject to pass into the fingerprint managers authenticate method, and that wraps
the Signature.  We get a callback for events that happen.

---

#Authenticating First

* This will start the scanner listening for fingerprints.
* `CryptoObject` wraps your crypto purpose (signing, encrypting, or message authentication code)
* `CancellationSignal` is a means of telling the fingerprint scanner to stop listening
* The `AuthenticationCallback` tells us what happened.

---
# Caveat Emptor 

__Stop listening__ when your app is not foregrounded & active. You can interfere with other apps or the lock screen if you keep listening.

^You can interfere with other apps if your app is not front and center and you are still listening for fingerprints.  Cancel when you are 
not active.

---

#Reacting to Successful Authentication

```java
FingerprintManagerCompat.AuthenticationCallback callback = 
    new FingerprintManagerCompat.AuthenticationCallback() {        
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                CryptoObject cryptoObject = result.getCryptoObject();

                // NOW we can use this Signature to sign!
                Signature signature = cryptoObject.getSignature();
            }

            // ....
}
```
^The CryptoObject we get back here indicates that the signature and its key have been blessed by authentication.
Now you can take this signature object out of it and use it to sign a value.

---

#Handling Unsuccessful Authentication

There are a few kinds of unsuccessful authentication:

* Failed. The wrong finger was scanned.
* Hard errors. The scanner gives up.  E.g. hardware or fingerprint lockout errors.
* Soft errors. The scanner is still live. Fingerprint scanned too fast, etc.

^You can handle an incorrect finger scan, a scan error, or a hard error that stops the process.  

^One caveat here is that too many failures will invoke a hardware lockout on the scanner.  The only way to unlock is 
to lock the device and re-authenticate using the PIN/Password/Pattern. 

^Your user could have locked the fingerprint out in another app and this will still impact you.

---

# Error callbacks

* Your `AuthenticationCallback` implementation deals with this too
* You get callbacks that give a message ID (which is referred to by a constant) and also some help/error text text.
* See `FingerprintManager`'s javadoc for these constants if you don't want to use Google's text 

^Google gives you a bunch of help for free here, in keeping the user experience as consistent as possible.  Use it.

---

#Unsuccessful Authentication

```java
FingerprintManagerCompat.AuthenticationCallback callback = 
    new FingerprintManagerCompat.AuthenticationCallback() {

        // ....
            // Bad Fingerprint
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                fingerprintView.onError("Fingerprint Not Recognized");
            }

            // Hard Error
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationHelp(errMsgId, errString);
                if (!canceled) {
                    fingerprintView.onError(errString.toString());
                }
            }

            // Soft Error
            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                if (!canceled) {
                    fingerprintView.onError(helpString.toString());
                }
            }
    }
}
```

^So here we have some examples of handling these errors.  I'm just passing these strings back over
into my UI.

---

# Okay, so we can scan fingerprint and sign things.
## How does that help us?

---

![right](images/enroll-fingerprint.png)

# Register the Public Key With Your Backend

Simple example

```javascript
{
    'registerRequest' : {
        'username' : 'jdoe',
        'password' : 'p4ss4w0rd',
        'timestamp' : 1474000105154,
        'publicKey' : 'PUBLIC_KEY_BASE_64'
    },
    'signature' : 'SIGNATURE_BASE_64'
}
```

^Here is a really stupid simple and contrived example.  You may want to do this differently, for instance you authenticate normally with user name and password,
get a session token, and then pass that session token in instead.

---

# Register the Public Key

Be sure to sign your registration request, and validate it in the backend, to prove the user has the ownership of the private key and can use it.

^You want to make sure the user has ownership of the private key, so when you post your public key, make sure to sign the whole request.  Validate it 
in your backend, too.

---

# Validate these

```kotlin
fun isRequestValidlySigned(request: SignedRequest<T>, publicKey: String) : Boolean {
        val pubKeyBytes = Base64.getDecoder().decode(publicKey)
        val signatureBytes = Base64.getDecoder().decode(request.signature)

        val kf = KeyFactory.getInstance("RSA")
        val key = kf.generatePublic(X509EncodedKeySpec(pubKeyBytes))
        val verify = Signature.getInstance("SHA256withRSA")
        verify.initVerify(key)
        verify.update(request.payload.messageForVerification().toByteArray())
        return verify.verify(signatureBytes)
    }
```

^So here in our backend we're decoding the public key and the signature, and verifying it.  It looks very similar to what we did on the 
Android side because Java crypto.

---

# Now Make Protected Requests

```javascript
{
    'purchaseRequest' : {
        "item": "Pepperoni Pizza",
        "quantity": 500,
        "deliveryAddress" : "1600 Pennsylvania Avenue",
        'timestamp' : 1474000105154,
    },
    'signature' : 'SIGNATURE_BASE_64'
}
```

If the signature validates against the data provided, then you know the user applied their fingerprint successfully.

^ So here we can actually order a pizza, and we'll post this to our backend.  We've authenticated in our user flow,
signed the request, and send this up.

---

# Login instead of a purchase?

You're likely already getting a session token from your login.

Just add an API endpoint that takes a signed request for a session token. 

^To handle logins, add an endpoint that allows you to use a signed request to get a session token.  You probably
already have some mechanism for doing this with username and password.

---

# Normalizing Data

You may find yourself needing to transform and normalize the data when signing and validating.

For instance: 

```
Pepperoni Pizza|500|1600 Pennsylvania Avenue|1474000105154
```

^Signing can be tough with json.  You likely will need to normalize the data in some fashion, especially if your 
backend automatically deserializes it for you.  Whatever you do, do the same thing on both the client and the backend. 

---


#Preventing Replay Attacks

Use a cryptographic "nonce."

The timestamp in the request prevents replaying the same request later.  Tampering with the timestamp would invalidate the signature.

You could also use other business data available to you if you can verify that the value hasn't been used multiple times.

^If we aren't careful, we could allow the same request to be re-posted and next thing you know we've ordered a pizza the user did not
authorize.  We should add a piece of uniquely validatable data to ensure you can't replay the request.

---

![right](images/invalidated-key.png)

#Handling Key Invalidation

* If the device has its lockscreen disabled or a new fingerprint is added, all authenticated keys are invalidated.
* Attempting to prepare a `Signature` with such a key will result in an `InvalidKeyException`.
* Don't use the key anymore. You have to re-create a new keypair and enroll it in your backend.

^If a user adds a new fingerprint, old keys tied to fingerprint get invalidated.  Same goes for if they turn off their lockscreen.

---

#Be Friendly

You should still _allow_ the user to use their password in lieu of fingerprint.  Put a "Use Password" button in the fingerprint dialog.

^Allow the user to use a password in lieu of fingerprint.  Maybe you're wearing gloves or your hands are wet.

---

#Now onto UI

---

![](images/material-fingerprint-screenshot.png)

^The material design guidelines have a great piece on how to design your fingerprint dialog.

---

![fit](images/fingerprint-dialog-components.png)

^You have to build this dialog yourself, it's not provided for you by the framework.  However they have fairly strict
guidelines to ensure a consistent experience.

^The header should indicate what you're authenticating for -- signing in, etc.

---

# Important to know

* Use the phrase "confirm fingerprint." It's what's used elsewhere.
* Take the error messages from Android for consistency sake.
* Users will be told to expect the fingerprint symbol. Display it as a standard icon (40dp circle with 24dp image). 

---

# Other Important Takeaways We Learned

* The "blessed" `CryptoObject` from the `FingerprintManager` success callback can be used only once.
* We needed to post that back to the main thread before we oculd use the `CryptoObject`.
* Use `FingerprintManagerCompat`!

---

# Sample Code

## http://tinyurl.com/android-fingerprint

---

# Questions?
## Thank You!

---

# Image Credits (Creative Commons)

* Nexus 6p https://flic.kr/p/C7keQW
* Notary Embosser https://flic.kr/p/dNMmAi