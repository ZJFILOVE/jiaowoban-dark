# Project instructions

- Develop this module in the attached Git worktree. Keep the main checkout separate.
- User constraint: never hard reboot the phone. Only soft reboot is permitted. Prefer restarting the target app; no system reboot was needed during initial validation.
- Restrict hooks and LSPosed scope to `edu.sjtu.infoplus.taskcenter`.
- Select an explicit ADB device serial before phone operations.
- Use JDK 17 or 21 with the checked-in Gradle wrapper and Android SDK 36.
- Test native pages and embedded services, including Shuiyuan, plus light/dark switching.
- QR/barcode images must retain black-on-white contrast and quiet zones. Check decoding without printing the decoded content. Never perform a payment or submit a business form during visual testing.
- Keep raw device screenshots, application APKs, UI dumps and full application logs out of Git; they can contain personal details or session tokens. Record only sanitized verification results.
