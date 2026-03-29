---
name: feature-audit
description: Use when reviewing or auditing features to verify they actually work as documented.
---
For each feature:
1. Read what FEATURES.md claims
2. Find the actual source code
3. Trace the code path — does it actually execute?
4. Check for: dead code, commented-out logic, placeholder implementations
5. Update FEATURES.md with real status
