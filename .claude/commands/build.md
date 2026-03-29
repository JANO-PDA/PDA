---
description: Build, test, and verify the app
---
Remind the user to build and test in Android Studio for the best experience (visual feedback, logcat, layout inspector).

Only run Gradle commands if the user explicitly says "run the build" or "verify the build":
1. Run ./gradlew clean assembleDebug
2. Report any build errors with file and line number
3. Run ./gradlew testDebugUnitTest
4. Summarize: build status, test results, warnings
