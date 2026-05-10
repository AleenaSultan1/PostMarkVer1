# Postmark — Android Setup

A travel journal Android app built with Jetpack Compose, Firebase, and Google Maps.

## Project structure

```
app/src/main/java/com/example/postmark/
├── MainActivity.kt              # entry point
├── auth/
│   └── AuthRepository.kt        # Firebase Auth wrapper
├── data/
│   ├── Entry.kt                 # entry data class (Firestore-serializable)
│   └── EntryRepository.kt       # Firestore CRUD
├── navigation/
│   └── PostmarkNavGraph.kt      # routes
└── ui/
    ├── components/              # shared UI (overflow menu, date format)
    ├── theme/                   # Color, Theme — postcard aesthetic
    ├── login/                   # login + register screen
    ├── list/                    # list view (also exports EntriesViewModel)
    ├── map/                     # Google Map view
    └── entry/                   # new entry composer + entry detail
```

## One-time setup

### 1. Create the Firebase project
1. Go to https://console.firebase.google.com/ and create a new project.
2. Add an Android app with package name `com.example.postmark`.
3. Download `google-services.json` and drop it into `app/` (next to `build.gradle.kts`).
4. In the Firebase console, enable:
   - **Authentication** → Sign-in method → Email/Password
   - **Firestore Database** → Create database → Start in production mode
5. Paste the contents of `firestore.rules` into Firestore → Rules and publish.

### 2. Get a Google Maps API key
1. Go to https://console.cloud.google.com/.
2. Enable the **Maps SDK for Android** for your Firebase project.
3. Create an API key, restrict it to Android apps + your package name + SHA-1.
4. In `app/src/main/AndroidManifest.xml`, replace `YOUR_MAPS_API_KEY` with the key.

### 3. Build and run
Open the project in Android Studio (Hedgehog or newer), let Gradle sync, then Run.

## Architecture notes

- **Single source of truth**: `EntriesViewModel` exposes a real-time `StateFlow<List<Entry>>` from Firestore. The list, map, and detail screens all consume from it, so a write from any screen instantly updates every other screen. No manual refresh logic.
- **Navigation**: `PostmarkNavGraph` decides start destination from the current Firebase auth state. Firebase persists the session on disk by default — the user stays logged in until they sign out. (This answers the "how long to keep user logged in?" sticky note.)
- **Auto-location**: The new entry screen requests `ACCESS_FINE_LOCATION` on open and stamps the entry with the device's current GPS via `FusedLocationProviderClient`. To turn lat/lng into "Budapest, Hungary," add a reverse-geocoding call using `android.location.Geocoder` after `fetchCurrentLocation` succeeds.
- **Security rules**: Entries live at `users/{uid}/entries/{entryId}`. The Firestore rule restricts read and write to the matching authenticated UID — no other user can ever see another user's entries.

## What's stubbed out (intentionally)

These are wired in the UI but need a small amount of code to fully implement. Each is a self-contained next step:

- **Photo upload**: The new entry screen has camera and photo album icons. Wire them to `ActivityResultContracts.GetContent()` for gallery and `ActivityResultContracts.TakePicture()` for camera, then upload the resulting Uri to Firebase Storage at `users/{uid}/photos/{entryId}/{photoId}.jpg` and store the download URLs in `entry.photoUrls`. Use Coil (`io.coil-kt:coil-compose`) to display them.
- **Filter**: The dropdown menu has a Filter item that's currently a no-op. A simple implementation: a bottom sheet with a date range and a country dropdown. Apply the filter in `EntriesViewModel` by mapping the underlying flow.
- **Reverse geocoding**: Currently the location field is empty when GPS resolves. Call `Geocoder(context).getFromLocation(lat, lng, 1)` (use the new async API on API 33+) and set `location` to `"$locality, $countryName"`.
- **User display name**: `ListScreen` hardcodes "John". Replace with `auth.currentUser?.displayName ?: auth.currentUser?.email`.

## Suggestions to keep it simple

1. **Drop the username/password if you can** — Firebase's Google sign-in or email magic link removes password reset, validation, and recovery flows. For a personal journal, friction at login matters more than login flexibility.
2. **One entry per day** — your blueprint dates entries, not timestamps them. Lean into that: opening the composer either creates today's entry or edits it. Removes the "do I add a new one?" decision.
3. **Skip "Delete All"** — destructive, no undo, easy to mis-tap. Move it behind a Settings screen with a typed confirmation, or remove it entirely. The risk of erasing years of memories outweighs the convenience.
4. **Consider a list/map toggle instead of separate screens** — both screens currently have a hamburger menu whose primary purpose is switching to the other one. A segmented control at the top of one combined screen is one tap instead of two.
