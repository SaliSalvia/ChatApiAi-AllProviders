# Privacy Policy — SALi-HCNSEC

_Last updated: 2026-11-02_

SALi-HCNSEC ("the app") is a provider-neutral, local-first personal AI workspace for Android. This policy explains what data the app handles, where it is stored, and who can see it.

## Summary

- **No account, no sign-up.** The app has no user accounts, no backend server of its own, and no login.
- **Your API keys never leave your device except to the provider they belong to.** Keys are encrypted at rest with AES-GCM using the Android Keystore and are sent only in Authorization headers to the provider you configured them for.
- **Your chats, projects and ZIP workspaces stay in app-private storage** on your device unless you explicitly send them to an AI provider for analysis.
- **No analytics, no ads, no tracking.** The release build ships with no analytics SDK, no crash reporting SDK, and no advertising identifiers.
- **Uninstalling the app deletes everything** the app stored on your device.

## Data the app stores (on your device only)

| Data | Where | Purpose |
| --- | --- | --- |
| Provider API keys | Android Keystore (AES-GCM encrypted) | Connect to AI providers you choose |
| Conversations, agent results | App-private database (Room) | Your chat history and workspace data |
| Imported ZIP archives + extracted files | App-private storage | Project analysis workspaces |
| Settings (language, provider configs, custom endpoints) | App-private preferences | App configuration |

## Data that leaves your device

The app sends data to a third party **only** when you take an explicit action:

1. **Chat messages and model requests** — sent directly from your device to the AI provider that owns the selected model, using your own API key. The app does not proxy requests through any intermediate server.
2. **Deep project analysis** — only after you confirm the remote-context consent dialog. The app shows you a confirmation, omits known credential-file paths, and redacts common inline secrets (best effort, not a guarantee). Only the bounded extracted context is sent, to the provider that owns the selected model.

Each request is governed by the **provider's own privacy policy and terms**, which you should review on the provider's official site. The app itself has no access to, or visibility into, what providers do with request data.

## Permissions

The app requests only the permissions it needs for core functionality (for example, reading a ZIP file you choose). It does not access contacts, location, microphone, photos, or any other sensitive data.

## Data retention and deletion

- All app data lives in the app's private storage on your device.
- Delete individual data at any time from inside the app (conversations, workspaces, provider keys).
- Uninstalling the app removes all app-private data permanently.

## Children

The app is not directed at children under 13 and is not intended for them.

## Changes

Material changes to this policy will be published in this repository and reflected in the "Last updated" date.

## Contact

Open an issue in this repository for any privacy question or data-deletion request concerning the app's on-device behavior.
