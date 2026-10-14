# EcoTrekk – Viva Cheat Sheet (HHDP II)

Everything below is checked against the actual code. Where a feature is not built, the sheet says so and gives an honest answer. Do not claim anything marked ❌.

---

## 0. Before you walk in (5-minute checklist)

1. Start the Spring Boot backend and the ngrok tunnel. The app's base URL is hard-coded in `client/RetrofitClient.java` as `https://causal-truly-liger.ngrok-free.app/`. If the tunnel is down, every screen fails to load.
2. Use a physical phone, not an emulator. You need the accelerometer (shake), biometric, and calls.
3. Sign in once beforehand so the session exists. Add 1-2 gears to the cart so Room has data.
4. Allow location, notifications and phone permissions in advance so the demo is not stopped by dialogs.
5. Have the admin dashboard open (to show push notification and order status changes).
6. Know your package: `com.akigen.ecotrekk`, minSdk 28 (Android 9), targetSdk 36, Java 11.
7. **Test the two new features on the phone before the viva:** toggle airplane mode (the "No internet connection" pill should appear within a second or two, then "Back online" for about 2.5 s), and double-tap a gear card on the Gears tab. The receiver was verified as registered on Android 12, but the live airplane-mode toggle has to be checked by you. If the banner does not react, tell yourself the fallback: swap the receiver trigger to `NetworkCallback` (only `ConnectivityBannerController` changes).
8. The wishlist double-tap needs internet. Make sure you are online for that part of the demo.
9. **Camera demo:** the photo upload needs internet and the backend. The backend now allows 5 MB per file (`spring.servlet.multipart` in `application.yml`; restart it after changing), and the app sends a cropped image of at most 1024 px, a few hundred KB. Test "Take photo" once on the phone first so the camera app's first-run dialogs are out of the way.

---

## 1. 60-second pitch

> EcoTrekk is an M-commerce app for renting camping gear. Customers find a rental **hub** on a Google Map, browse gear by category, pick a **date range**, pay by **Stripe card or cash**, and collect from the hub. It has three parts: an **Android app (Java)**, a **Spring Boot REST backend**, and a **React admin dashboard**. The app uses **Firebase Auth**, **FCM push notifications**, **Room** for local storage with **WorkManager** background sync, **Retrofit/Gson** for the API, **Google Maps**, the **accelerometer** (shake to refresh), a **double-tap gesture** to save gear to the wishlist, an **offline banner** driven by a **BroadcastReceiver**, a **camera** option for the profile photo, and **biometric** confirmation at checkout.

---

## 2. Architecture in one picture

```
Android app (Java, Material 3)
  Activities + 5 Fragments (bottom nav)
  Retrofit + Gson  ---------------->  Spring Boot REST API  --->  MySQL
  Firebase Auth / FCM <------------>  Firebase Admin (verifies ID token, sends push)
  Room (cart, favorites, users) <--- WorkManager pull-sync every 6 h
  Stripe PaymentSheet  ------------>  Stripe (PaymentIntent created by backend)
React admin dashboard  ------------>  same REST API (+ WebSocket for live updates)
```

---

## 3. Feature → where it lives in code (your "proof" list)

| Feature | File(s) |
|---|---|
| Splash + auth routing | `activity/SplashActivity.java` |
| Login / Signup / forgot-password dialog | `LoginActivity`, `SignupActivity` |
| Bottom navigation, fragment swapping | `activity/HomeActivity.java` (`loadFragment`, `replace`) |
| Home / Gears / Hubs / Cart / Profile | `fragment/*Fragment.java` |
| Map, markers, my-location, distance | `fragment/HubsFragment.java` |
| Hub detail: map preview, call, share, directions | `activity/SingleHubActivity.java` |
| Shake-to-refresh (accelerometer) | `fragment/GearsFragment.java` (`shakeListener`) |
| Double-tap to wishlist (GestureDetector) | `adapter/GearProductAdapter.java` (gesture detector per ViewHolder), `fragment/GearsFragment.java` (`onGearDoubleTapped`), `res/layout/item_gear_product.xml` (heart badge) |
| Offline banner (BroadcastReceiver) | `network/ConnectivityBannerController.java`, `network/NetworkChangeReceiver.java`, `network/NetworkUtils.java`, `res/layout/view_connectivity_banner.xml`; installed from `EcoTrekkApplication.java`; permission `ACCESS_NETWORK_STATE` in the manifest |
| Sync as soon as the connection returns | `sync/LocalDataSyncScheduler.java` (`syncNow`) |
| Date-range picker | `fragment/CartFragment.java` (`MaterialDatePicker`) |
| Biometric + Stripe + cash order | `activity/CheckoutActivity.java` |
| Push notification | `manager/EcoTrekkFirebaseMessagingService.java`, `FcmManager.java` |
| Room DB | `database/EcoTrekkDatabase.java`, `entity/*`, `dao/*`, `repository/*` |
| Background sync | `sync/LocalDataSyncScheduler.java`, `LocalDataSyncWorker.java` |
| SharedPreferences | `manager/SessionManager.java`, `ThemePreferenceManager.java`, `FcmManager.java` |
| Retrofit API (23 endpoints) | `api/ApiService.java`, `client/RetrofitClient.java` |
| Profile photo: camera or gallery, crop, upload | `activity/PersonalInformationActivity.java` (`showPhotoSourceDialog`, `launchCamera`, `TakePicture`, `GetContent`, uCrop, `uploadProfileImage`) |
| Camera file sharing (FileProvider) | `AndroidManifest.xml` (`<provider>`, optional `uses-feature` camera), `res/xml/file_paths.xml` |
| Dark/light theme setting | `activity/AppSettingsActivity.java` |

---

## 3b. "Did you use X? Show me where." – quick lookup (SharedPreferences, receiver, intents, sensors)

Paths are under `EcoTrekk/app/src/main/java/com/akigen/ecotrekk/`. Line numbers were verified against the code on 15 Oct 2026 (the `network/` files were tidied that day, so the numbers moved). If you edit a file again, search for the method name instead. **Open these files in your IDE before the viva.**

### SharedPreferences – YES (3 places, all `MODE_PRIVATE`)
| What it stores | Where |
|---|---|
| Login session (customer id, name, email, phone, photo URL) | `manager/SessionManager.java:23` opens the prefs, `saveSession()` at line 27 |
| Dark/light theme mode | `manager/ThemePreferenceManager.java:25` saves it (`putInt`); applied at app start in `EcoTrekkApplication.java:15`; changed on `activity/AppSettingsActivity.java` |
| Push (FCM) token | `manager/FcmManager.java:139` |

> "SharedPreferences holds small private key-value data: the login session, theme mode and push token. Bigger structured data (cart, favorites) is in Room."

### BroadcastReceiver – YES (connectivity changes only)
| Piece | Where |
|---|---|
| The receiver class (`extends BroadcastReceiver`) | `network/NetworkChangeReceiver.java:9`, `onReceive` at line 22 |
| Registered in code for `CONNECTIVITY_ACTION` | `network/ConnectivityBannerController.java:71-76` (`ContextCompat.registerReceiver`, `RECEIVER_EXPORTED`) |
| Started once for the whole app | `EcoTrekkApplication.java:18` (`ConnectivityBannerController.install(this)`) |
| What happens when it fires | `ConnectivityBannerController.java:82` (`onConnectivityChanged`): offline banner on every screen; back online → one-time data sync at line 93 |
| Checking the real state | `network/NetworkUtils.java:15` (`isOnline`; checks `NET_CAPABILITY_INTERNET` at line 29) |

> "A BroadcastReceiver listens for connectivity changes. It shows a 'No internet connection' banner on every screen and syncs data when the connection comes back."

Say honestly: connectivity only, no battery or boot receivers. FCM push is a **service**, not a receiver. Demo it by toggling airplane mode (test it once before the viva).

### Intents – YES (explicit, implicit and PendingIntent)
| Type | Where |
|---|---|
| **Explicit** Splash → Home / Welcome | `activity/SplashActivity.java:133` and `:143` |
| **Explicit** Cart → Checkout (with extras) | `fragment/CartFragment.java:255` |
| **Explicit** Gears → gear details (with extras) | `fragment/GearsFragment.java:283` |
| **Explicit** Order history → order details (with extras) | `activity/OrderHistoryActivity.java:221` |
| **Implicit** share hub (`ACTION_SEND` + chooser) | `activity/SingleHubActivity.java:151` and `:154` |
| **Implicit** call hub (`ACTION_CALL`, fallback `ACTION_DIAL`) | `activity/SingleHubActivity.java:176` and `:192` |
| **Implicit** directions in Google Maps (`ACTION_VIEW`) | `activity/SingleHubActivity.java:246` (also `activity/OrderDetailsActivity.java:141`) |
| **PendingIntent** for the push notification | `manager/EcoTrekkFirebaseMessagingService.java:69-75` |
| **Activity-result** camera and gallery | `activity/PersonalInformationActivity.java:87` (`GetContent`) and `:89` (`TakePicture`) |

> "Explicit intents open screens inside my app and pass data as extras. Implicit intents ask the system to find an app: share, call, maps. A PendingIntent lets the notification open my app later."

### Sensors – YES (accelerometer only)
All in `fragment/GearsFragment.java`:
| Step | Line |
|---|---|
| `setupShakeToRefresh()` gets `SensorManager` and `TYPE_ACCELEROMETER` | 633 (accelerometer at 637) |
| `onSensorChanged` computes the movement | 644 |
| Shake detected when `accel > 12` (2 s debounce), then reloads the gear list | 653 |
| `registerListener` in `onResume` | 680 |
| `unregisterListener` in `onPause` (saves battery, avoids leaks) | 688 |

> "The accelerometer gives x, y, z. I take the magnitude, filter it, and if it passes a threshold I refresh the gear list. I register in onResume and unregister in onPause."

No light, proximity or gyroscope sensor is used.

---

## 4. Demo script (about 4 minutes, in this order)

1. **Launch** → Splash → goes to Home (shows auth routing).
2. **Hubs tab** → map with markers → allow location → distances appear → tap a hub → hub detail → **Call** (permission) and **Share** (implicit intent chooser).
3. **Gears tab** → filter by category / price (RangeSlider) → **shake the phone** → "Refreshing catalog…" snackbar → **double-tap a gear card** → heart badge pops and "Added to your wishlist" appears (double-tap the same card again → "Already in your wishlist"). A single tap opens the gear after a short delay.
4. **Open a gear** → add to cart → **Cart tab** → pick date range → change quantity (+/-).
5. **Checkout** → select hub → **biometric prompt** → Stripe card (test card `4242 4242 4242 4242`) or cash.
6. **Success screen** → Order history → order details.
7. From the admin dashboard, change the order status → **push notification** arrives → tap it → opens Order History.
8. **Offline banner:** pull down quick settings → airplane mode **on** → pill "No internet connection" appears on any screen → try a double-tap on Gears (blocked with an offline message) → airplane mode **off** → "Back online" for about 2.5 s, then data syncs.
9. **Profile photo:** Profile → Personal Information → tap the pencil → dialog "Take photo / Choose from gallery" → **Take photo** → system camera → confirm → crop screen (1:1) → "Profile image updated" and the avatar changes.
10. **Settings** → switch dark theme (SharedPreferences) to finish.

---

## 5. Concept answers (the "theory" questions)

**Activity lifecycle?** `onCreate → onStart → onResume → (running) → onPause → onStop → onDestroy`. In my code, `SingleHubActivity` forwards every lifecycle callback to the `MapView` and saves the map state in `onSaveInstanceState`. `GearsFragment` registers the sensor in `onResume` and unregisters in `onPause` to save battery.

**Fragment lifecycle and why `binding = null` in `onDestroyView`?** A fragment's view can be destroyed while the fragment object lives (back stack). Clearing the binding avoids memory leaks. I also check `isAdded() && binding != null` inside async callbacks.

**Explicit vs implicit intent?** Explicit names the target class (`new Intent(this, CheckoutActivity.class)`). Implicit names an action and the system picks the app: `ACTION_VIEW` for directions, `ACTION_SEND` + `createChooser` for sharing a hub, `ACTION_DIAL` / `ACTION_CALL` for phone.

**How do fragments talk to each other?** Home → Gears: `FragmentResult` API (`getParentFragmentManager().setFragmentResultListener`) passes the selected category. Fragment → activity: I use `requireActivity().findViewById` to change the bottom-nav tab (it works, but a shared ViewModel or interface would be cleaner).

**Why RecyclerView and ViewHolder?** It recycles item views and only binds visible rows, so lists scroll smoothly. I have 9 RecyclerViews/adapters (cart, gears, categories, featured, wishlist, hubs, orders).

**Why can't you network on the main thread?** Android throws `NetworkOnMainThreadException` and the UI would freeze. Retrofit `enqueue` runs on a worker thread and returns on the main thread. I use an `ExecutorService` plus `runOnUiThread` for Room reads, and WorkManager for background work.

**What is Room and why not raw SQLite?** Room is an abstraction over SQLite with compile-time checked queries, DAOs, and LiveData. Entities: `cart_items`, `favorites`, `users`. The cart screen observes `LiveData` so the UI updates when the table changes.

**SharedPreferences vs Room?** SharedPreferences for small key-value data (session token, theme mode, FCM token). Room for structured rows (cart, favorites).

**Runtime permissions flow?** Declare in the manifest → `checkSelfPermission` → `registerForActivityResult(RequestPermission)` → handle denial. Used for POST_NOTIFICATIONS (Android 13+), location (map), and CALL_PHONE (hub call, with `ACTION_DIAL` as the fallback).

**Notification channels?** Required from Android 8. `FcmManager.createNotificationChannel` creates one high-importance channel. The notification carries a `PendingIntent` (immutable flag).

**Why `PendingIntent.FLAG_IMMUTABLE`?** Required on Android 12+ for security.

**What is a BroadcastReceiver?** A component that reacts to system or app broadcasts (connectivity change, battery low, boot completed). It can be declared in the manifest or registered in code. Since Android 7, manifest receivers no longer get implicit broadcasts like connectivity changes, so mine is **registered in code** (`ContextCompat.registerReceiver` on the application context, in `ConnectivityBannerController.start()`), which still receives them.

**Why is the receiver registered in the Application class?** One registration for the whole process, so there is no per-activity register/unregister and no leak. `ActivityLifecycleCallbacks` then attach the banner to whichever activity is resumed, so none of the 13 activities needed to change.

**`CONNECTIVITY_ACTION` is deprecated. Why use it?** It is deprecated since Android 9 but still delivered to receivers registered in code. I used it to demonstrate a real system-broadcast receiver. The modern replacement is `ConnectivityManager.NetworkCallback` (`registerDefaultNetworkCallback`); switching would only touch `ConnectivityBannerController`. Inside the receiver I do not trust the intent; I re-check the real state with `NetworkUtils.isOnline()` (`NetworkCapabilities.NET_CAPABILITY_INTERNET`).

**Why `RECEIVER_EXPORTED` for the receiver?** `CONNECTIVITY_ACTION` is a protected system broadcast, so only the system can send it; exported is safe. On Android 12 and below, `RECEIVER_NOT_EXPORTED` attaches an extra app permission to the receiver, which I avoided.

**How does double-tap detection work?** A `GestureDetector` with a `SimpleOnGestureListener` per card. `onDoubleTap` fires for two quick taps; `onSingleTapConfirmed` fires only after the double-tap window (about 300 ms) has passed, so a single tap opens the details with a short delay. The touch listener forwards events to the detector.

**How does the camera work, and why is there no `CAMERA` permission?** I use `ActivityResultContracts.TakePicture`, which launches the phone's own camera app with `ACTION_IMAGE_CAPTURE`. The camera app, not my app, accesses the camera hardware, so my app needs no `CAMERA` permission. In fact, declaring `CAMERA` in the manifest without granting it would make that intent throw a `SecurityException`. The trade-off: I get a photo back, but I do not control a custom preview screen, and I cannot record video this way.

**What is FileProvider and why use it?** The camera app is a different app and cannot write into my private cache folder directly, and a raw `file://` path is blocked on modern Android. `FileProvider` gives it a temporary `content://` URI for one file (`cache/camera/profile_capture_<time>.jpg`), declared in the manifest and limited by `res/xml/file_paths.xml`. It is `exported="false"`, and the access is granted only for that capture.

**Why crop to 1024 px and quality 85?** A full-resolution camera photo is several MB, and the backend's multipart limit rejects large files (it returned "Maximum upload size exceeded" at the old 1 MB default). I cap the crop result with uCrop `withMaxResultSize(1024, 1024)` and JPEG quality 85, so the upload is a few hundred KB, which is faster on mobile data and fits the server's 5 MB limit comfortably. A profile photo does not need more.

**What if the activity is killed while the camera app is open?** Android can destroy my activity in the background. I save the capture file path in `onSaveInstanceState` and restore it in `onCreate`, so the captured photo still reaches the crop step.

**Why add the detector in the ViewHolder constructor, not in `onBindViewHolder`?** ViewHolders are reused, so creating a detector on every bind would allocate needlessly. The holder keeps a `currentProduct` field that bind updates, so the detector always acts on the right item.

**How does Firebase Auth work here?** Login: `signInWithEmailAndPassword`, then the ID token goes to the backend (`/api/auth/customer/firebase`), which verifies it with Firebase Admin and returns my own session. Signup: the app calls my backend `/register`, the backend creates the Firebase user and returns a **custom token**, and the app signs in with `signInWithCustomToken`.

**Why Stripe PaymentSheet?** Card data never touches my app or server (PCI scope reduction). The backend creates the PaymentIntent with the secret key, and the app only receives the client secret.

**Gson?** Converts JSON ⇄ Java objects. Used as the Retrofit converter on every call, and `toJson/fromJson` to pass an order's item list between activities.

---

## 6. "Walk me through the code" answers

**Shake detection (`GearsFragment`)**
Accelerometer gives x, y, z. I compute the magnitude `√(x²+y²+z²)`, take the change from the last reading (`delta`), and apply a low-cut filter `accel = accel*0.9 + delta`. If `accel > 12` and more than 2 seconds passed since the last shake, I refresh the catalog from the backend. The 2 s debounce prevents multiple triggers from one shake. Registered in `onResume`, unregistered in `onPause`.

**Background sync (`LocalDataSyncScheduler/Worker`)**
`PeriodicWorkRequest` every 6 hours with constraint `NetworkType.CONNECTED`, unique work name with `KEEP` policy. `doWork()` fetches cart and wishlist from the API and replaces the Room tables. It returns `retry()` on exception. When the connection comes back, `LocalDataSyncScheduler.syncNow()` enqueues a one-time run of the same worker so Room refreshes immediately.

**Offline behaviour (cart)**
`CartFragment` loads from the server first. On error it calls `loadCartItemsFromLocal()` and shows the Room copy. So **reading** works offline from the last synced data.

**Offline banner (`network/` package)**
1. `EcoTrekkApplication.onCreate()` calls `ConnectivityBannerController.install(this)`.
2. The controller registers a `NetworkChangeReceiver` for `CONNECTIVITY_ACTION` and an `ActivityLifecycleCallbacks`.
3. On each `onActivityResumed` (any EcoTrekk screen except Splash) it adds a small pill (`view_connectivity_banner.xml`) to `android.R.id.content`, padded below the status bar.
4. When the receiver reports offline, the pill slides in ("No internet connection"). When online again, it shows "Back online" for about 2.5 s and hides, and `syncNow()` refreshes Room.
5. It is a floating pill, not a full-width bar, so it does not cover back buttons; it is non-clickable, so touches pass through. It uses theme colors, so dark mode works.

**Profile photo from the camera (`PersonalInformationActivity`)**
1. The pencil button calls `showPhotoSourceDialog()`, a `MaterialAlertDialogBuilder` list: **Take photo** or **Choose from gallery**. Gallery launches `GetContent("image/*")` as before.
2. `launchCamera()` creates `cacheDir/camera/profile_capture_<time>.jpg`, gets a `content://` URI with `FileProvider.getUriForFile(...)` (authority `<package>.fileprovider`), and launches `TakePicture`. If there is no camera app, an exception is caught and a Toast says "Camera not available on this device".
3. If the camera returns success, the same `launchCrop(uri)` used by the gallery path opens uCrop (1:1, max 1024 px, JPEG 85). If it was cancelled, the temp file is deleted.
4. The crop result goes to `uploadProfileImageAfterToken` → `uploadProfileImage` (multipart "image" part + Firebase ID token) → the avatar is refreshed with Glide. The raw capture is deleted when the crop screen finishes, whether it was saved or cancelled.
5. The capture path is kept in `onSaveInstanceState`, so a restart of the activity while the camera app is open does not lose the photo.

**Double-tap to wishlist (`GearProductAdapter` + `GearsFragment`)**
1. `onDoubleTap` calls the listener set by `GearsFragment`; `onSingleTapConfirmed` opens `SingleGearActivity` as before.
2. `onGearDoubleTapped`: skip if the gear id is invalid; if offline, show "You're offline…" and stop; otherwise on a background executor check `FavoritesHelper.isFavorite()`.
3. Already a favorite → "Already in your wishlist". Otherwise `addToFavoritesWithServerSync()` saves it in Room and calls the wishlist API, then "Added to your wishlist".
4. `GearsFragment` observes `observeFavorites()` (LiveData) and passes the ids to the adapter, which shows a heart badge (with a pop animation when newly added).
5. It is **add-only**; removing is still done with the heart button on the gear detail screen.
6. For TalkBack, each card has "Add to wishlist" and "Open gear details" accessibility actions.

**Notification → screen**
FCM `onMessageReceived` builds a `NotificationCompat` with a `PendingIntent` to `SplashActivity`, with extra `target_screen`. Splash passes the extra to `HomeActivity`, whose `handleNotificationNavigation` opens `OrderHistoryActivity`.

**Checkout**
`placeOrder()` → validates billing details → `BiometricPrompt` → on success `proceedWithOrder()` → cash: `createCashOrder`, or card: Stripe `PaymentSheet`. On success the local cart is cleared and the success screen opens with the invoice code.

**Splash routing**
No Firebase user → Welcome. Firebase user + local session → Home. Firebase user, no local session → refresh the ID token → re-login with the backend. If the network is down, it still opens Home because the Firebase session is valid.

---

## 7. Criteria status and honest answers

Use calm, short answers. For anything not built, say **what you scoped**, then **how you would add it**. Items marked ✅ below were added recently, so demo them live.

### ✅ 3.3 Gestures (2 marks) – now built
> "I use two gestures. A motion gesture: shaking the phone refreshes the catalog through the accelerometer. And a touch gesture: double-tapping a gear card adds it to the wishlist, using a `GestureDetector` on each card. A single tap still opens the details."
*(Limits to admit if asked: no long-press, fling or swipe yet. Long-press on a cart item to delete and `ItemTouchHelper` swipe-to-delete are the next gestures I would add.)*

### ✅ 3.4 Broadcast receivers (3 marks) – now built
> "I register a `BroadcastReceiver` for the connectivity-change system broadcast. It is registered in code once in the Application class, and when it fires, an offline banner appears on every screen and a one-time sync runs when the connection returns. FCM push is separate: that is a service, not a receiver."
*(Limits to admit: only the connectivity broadcast is handled. I did not add receivers for `BATTERY_LOW` or `BOOT_COMPLETED`. `CONNECTIVITY_ACTION` is deprecated; `NetworkCallback` is the modern alternative.)*

### ✅ 2.5 Camera (1 mark) – now built (pictures only)
> "On the profile screen you can take a new photo or choose one from the gallery. Taking a photo uses `TakePicture` to open the phone's camera app, saving into a private cache file shared through a `FileProvider`, then the photo goes through uCrop and is uploaded to the backend. Because the system camera app does the capture, my app needs no `CAMERA` permission."
*(Limits to admit if asked: pictures only, no video recording, and no in-app camera preview. The criterion mentions videos; an in-app CameraX screen with `CAMERA` permission and `MediaStore.ACTION_VIDEO_CAPTURE` or CameraX video would be the next step.)*

### ❌ 2.7 Charts / reports (3 marks)
> "The customer app focuses on the rental flow. Reports belong to the admin side, so I did not build charts in the mobile app. For the app I would add MPAndroidChart: a bar chart of spending per month and a pie chart of orders by category, fed from the order-history API."
*(Verified: your React admin dashboard has a recharts `BarChart` in `DashboardPage.tsx`, so you can say reporting exists on the admin side. Show it if asked.)*

### ❌ 8.1 Bluetooth / Wi-Fi (3 marks)
> "I did not implement local wireless exchange; all data goes through the cloud API over Wi-Fi or mobile data. The app does read network state to show the offline banner. A fit for this app would be sharing a rental voucher to a nearby friend's phone via Bluetooth."

### ⚠️ 4.2 SQLite + sync when offline
> "Room stores cart, favorites and users locally. WorkManager pulls server data into Room every 6 hours when connected, plus once immediately when the connection returns, and the cart falls back to Room when the server is unreachable. What I did **not** build is an outbox queue for offline **writes** (changes made offline pushed later). That is why the wishlist double-tap is blocked while offline instead of queued. I would add a `syncStatus` column plus a pending-operations table, and have the worker upload pending rows before pulling."

### ⚠️ 5.2 Cloud services
> "I use Firebase Authentication and Firebase Cloud Messaging. The Firestore dependency is in Gradle but I do not use it, because order and inventory data lives in my own MySQL via Spring Boot, which gives me transactions and admin reporting. Images are hosted through the backend (Cloudinary)."
*(Be ready: if asked why Firestore is in Gradle, say it was added early and not used.)*

### ⚠️ 6.1 Google Maps – routes
> "I show hubs as markers, my-location, a custom map style, animated camera focus and distance calculation. For directions I hand off to Google Maps through an implicit intent. Drawing an in-app polyline would need the Directions API: request the route, decode the encoded polyline with `PolyUtil.decode`, and add a `PolylineOptions` to the map."

### ⚠️ 7.1 Telephony
> "I implemented calling a hub: `ACTION_CALL` when the CALL_PHONE permission is granted, otherwise `ACTION_DIAL`. I did not add SMS. I would use an `ACTION_SENDTO` with `smsto:` for composing, or `SmsManager` with the SEND_SMS permission for in-app sending."

### ⚠️ 2.1 Advanced views
> "Snackbars, FAB (auth screens), a BottomSheet for nearby hubs, chips for filters, and a +/- quantity stepper in the cart. I chose bottom navigation instead of a navigation drawer because there are five top-level destinations, which Material guidelines recommend for bottom navigation."

### ⚠️ 2.3 Dialogs
> "Date-range picker, an alert dialog with a custom view for forgot-password, a list dialog for choosing the profile photo source (camera or gallery), and the system biometric dialog. No TimePicker because rentals are by whole days."

### ⚠️ 2.4 Custom libraries
> "uCrop for image cropping, Glide for image loading and caching, Stripe PaymentSheet, AndroidX Biometric, and Retrofit/OkHttp logging. For animation I use `ViewPropertyAnimator` (banner slide, heart badge pop), not an animation library; there is no notification badge or clipboard use."

### ⚠️ 1.2 / 1.3 Orientation and lifecycle
> "The app is designed for portrait phone use; the map and Stripe flows are portrait-first. I handle lifecycle for the map and sensor, and null out fragment bindings. I did not provide landscape layouts or ViewModels; adding a ViewModel would keep state across rotation."

### ⚠️ 3.1 Threads (raw `new Thread`)
> "Network uses Retrofit's async queue and Room reads use an ExecutorService with `runOnUiThread`. A few Room writes in the repositories use plain `Thread`; I would replace them with a shared single-thread executor to avoid unmanaged threads."

### ⚠️ 3.2 Events
> "Click, text-watcher (live validation on login/signup), checked-change (theme, filters), chip-group and slider listeners, plus the double-tap gesture on gear cards. Long-click and key events are not used."

---

## 8. Likely "gotcha" questions

| Question | Safe answer |
|---|---|
| Your doc says a custom OkHttp interceptor adds the Firebase token as a Bearer header. Show me. | **Verified: it does not exist.** `RetrofitClient` has only a logging interceptor, and no `Authorization` header or `@Header` appears anywhere in the app. The Firebase ID token is sent once, in the request body, to `/api/auth/customer/firebase` at login/refresh; after that, calls identify the customer by `customerId` in the URL path. Say: "The document describes the intended design. In the current build the ID token is exchanged at login, and attaching it as a Bearer header through an OkHttp interceptor is the next hardening step." Do not claim the interceptor is implemented. |
| Isn't it insecure that any `customerId` in the URL works? | Be honest: "Yes, that is a known limitation. Server-side I would verify the Firebase ID token on each request and check that the token's user matches the `customerId`." |
| Doc says CheckoutFragment and coroutines. | "That wording came from the early design. The final app uses `CheckoutActivity` and an `ExecutorService`; Java has no coroutines." |
| Is the API HTTPS? | "It's served over an HTTPS ngrok tunnel for development. Production would be a proper HTTPS host. `network_security_config.xml` controls cleartext rules." |
| Where is the Maps API key? | "In `local.properties`, injected into the manifest by the Secrets Gradle plugin, so it is not committed." |
| Why is the session stored in SharedPreferences? | "It's private to the app. For stronger protection I would use `EncryptedSharedPreferences` or the Android Keystore." |
| BiometricPrompt on Android 9-10 with strong + device credential? | "That authenticator combination is restricted on API 28-29. I would limit the allowed authenticators on those versions." *(Only if asked. It is a real edge case since minSdk is 28.)* |
| What if the user denies permissions? | "Location denied → map still works, distance shows 'unavailable'. Notification denied → a toast explains. Call denied → falls back to the dialer." |
| Why does a single tap on a gear card feel slightly delayed? | "The gesture detector waits about 300 ms to make sure it is not the first half of a double-tap. That delay is inherent to supporting double-tap." |
| Why can't I add to the wishlist by double-tap while offline? | "The app has no offline write queue, and the periodic sync replaces local favorites with the server list, so an offline add would silently disappear. Blocking with a clear message is more honest. A queue is the next improvement." |
| Does double-tap remove from the wishlist too? | "No, it is add-only to avoid accidental removals. A second double-tap says 'Already in your wishlist'. Removing is done with the heart button on the gear detail screen." |
| Does the offline banner work on every screen? | "On every EcoTrekk screen except Splash. It is installed through activity lifecycle callbacks from the Application class, so no activity was edited. It is a floating pill so it does not cover back buttons." |
| How do you know you are offline? | "The receiver only says something changed; I then check `ConnectivityManager.getActiveNetwork()` and `NetworkCapabilities.NET_CAPABILITY_INTERNET` through `NetworkUtils.isOnline()`. I do not require validation because it lags after reconnect and would make the banner flicker." |
| What does Back online do besides showing a message? | "It enqueues a one-time `LocalDataSyncWorker` run so the cart and wishlist in Room refresh immediately." |
| Does the camera feature need the CAMERA permission? | "No. The system camera app takes the photo and hands it back, so my app never touches the camera hardware. I deliberately do not declare `CAMERA`, because declaring it without granting it would make the camera intent fail." |
| Why use a FileProvider? | "The camera app cannot write into my private cache folder, and raw `file://` paths are blocked on modern Android. FileProvider gives it a `content://` URI for just that one file." |
| Can your app record video? | "No, only pictures. A profile photo does not need video. For video I would use CameraX or `ACTION_VIDEO_CAPTURE`." |
| Why did uploading the profile photo fail with HTTP 500 at first? | "Spring's default multipart limit is 1 MB per file and a full-resolution camera photo is several MB. I fixed it on both sides: the app crops to at most 1024 px at JPEG quality 85 (a few hundred KB), and the backend now allows 5 MB (`spring.servlet.multipart.max-file-size`)." |
| What if the phone has no camera app? | "The `TakePicture` launch is wrapped in a try/catch, the manifest marks the camera as an optional feature (`required=false`), and the user sees 'Camera not available on this device'. The gallery option still works." |
| What would you do with more time? | "Offline write queue, in-app route polylines, charts, long-press and swipe gestures, ViewModel + landscape, EncryptedSharedPreferences, unit tests." |
| Are there tests? | "Only the default example tests exist. Testing was manual on a physical device (documented in Results)." *(Be honest.)* |

---

## 9. Things NOT to say

- Don't say you have charts in the mobile app, Bluetooth/Wi-Fi data exchange, SMS, polylines or Firestore usage.
- Don't say the camera records video, has an in-app preview, or uses CameraX or a `CAMERA` permission. It is the **system camera app, pictures only**.
- Don't say you have long-press, fling or swipe gestures. The touch gesture is **double-tap only** (plus shake, which is a motion gesture).
- Don't say you handle `BATTERY_LOW` or `BOOT_COMPLETED`. The receiver handles **connectivity changes only**, and it is registered in code, not in the manifest.
- Don't say the banner uses `NetworkCallback`; it uses a `BroadcastReceiver` (and say `NetworkCallback` is the modern alternative if asked).
- Don't say offline wishlist changes are queued. They are **blocked** while offline.
- Don't claim you tested the banner on every Android version. Registration was verified on Android 12; test the live airplane-mode toggle on your phone before the viva.
- Don't say the sync is two-way. It is **server → local only**.
- Don't say "Clean Architecture". Say "layered: activities/fragments → helpers/repositories → Room/Retrofit".
- Don't say the app works fully offline. Cart reads fall back to Room; ordering needs the network.

---

## 10. Last-minute memory hooks

- **Numbers:** 13 activities, 5 fragments, 9 RecyclerViews, 3 Room entities, 23 API endpoints, 6-hour sync, shake threshold 12 with 2 s debounce, double-tap window about 300 ms, "Back online" shown 2.5 s.
- **Permissions:** INTERNET, ACCESS_NETWORK_STATE, CALL_PHONE, ACCESS_FINE/COARSE_LOCATION, POST_NOTIFICATIONS. (No CAMERA permission on purpose.)
- **New this week:** `network/` package (3 classes), `GearProductAdapter` gesture detector, `LocalDataSyncScheduler.syncNow()`, camera capture with `FileProvider` (`file_paths.xml`), crop result capped at 1024 px / JPEG 85, backend multipart limit 5 MB.
- **Libraries:** Retrofit, Gson, OkHttp logging, Glide, uCrop, Room, WorkManager, Firebase (Auth, FCM), Stripe, Biometric, Google Maps, Material 3.
- **Stay calm rule:** if you don't know, say "In my implementation I did X; the alternative would be Y." Never invent.
