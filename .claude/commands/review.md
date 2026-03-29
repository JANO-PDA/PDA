---
description: Deep review of code quality and feature status
---
Review the entire codebase:
1. Check every feature against FEATURES.md — is it truly working?
2. Run ./gradlew lintDebug and report issues
3. Look for: unused code, missing error handling, broken features, accessibility issues
4. Update CURRENT_STATE.md and FEATURES.md with accurate status
5. Present a summary: what works, what's broken, what's missing
