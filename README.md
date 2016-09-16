# Android Fingerprint

Here's my sample app and backend from the _Android Fingerprint Authentication In Action_ talk presented at 
Windy City DevCon and Droidcon NYC 2016.

## What's in here?

* A sample Android app that demonstrates fingerprint scanning and posting signed requests against a backend.
* A sample backend that illustrates validating the signatures. 

## Running the Sample Backend

The Android app is configured to talk to `localhost:8080`, so forward requests into your emulator or device:

`adb reverse tcp:8080 tcp:8080`

in `./app-backend`, run `.gradlew bootRun`

The backend will run on port 8080 locally.

