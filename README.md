## Running the Sample Backend

in `./app-backend`, run `.gradlew bootRun`

The backend will run on port 8080 locally.

The Android app is configured to talk to `localhost:8080`, so forward requests into your emulator or device:

`adb reverse tcp:8080 tcp:8080`
