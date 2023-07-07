# FirebaseMessagingService

#Permissions

```
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```


#Update Manifest File


```
     <service
            android:name=".fcm.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@drawable/logo" />
        <!-- Set color used with incoming notification messages. This is used when no color is set for the incoming
             notification message. See README(https://goo.gl/6BKBk7) for more. -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_color"
            android:resource="@color/parrot" />
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="@string/default_notification_channel_id" />
```
#RequestPermissions in MainActivity.kt

```
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

 private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "Notification permission denied, you won't see notifications",Toast.LENGTH_SHORT).show()
        }
    }

```


#NetworkManager.kt
```
import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(@ApplicationContext private val context: Context) {
    @Singleton
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI || connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
    }
}

```

#Single Instance of FCMRepo.kt In Hilt Dagger Module

```
@InstallIn(SingletonComponent::class)
@Module
class HiltModule {

   @Provides
    @Singleton
    fun getFcmRepo(
        api: Api,
        networkManager: NetworkManager,
        preferenceHelper: PreferenceHelper   //optional
    ): FCMRepo {
        return FCMRepo(api, networkManager, preferenceHelper)
    }
}
```


#Application Class

```
@Inject
    lateinit var fcmRepo: FCMRepo


```

#onCreate of Application class

```
 @SuppressLint("HardwareIds")
val androidId =Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "androidID"

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, " Application getInstance FCM TOken   |  $token")
            fcmRepo.sendFcmTokenToServer(token, androidId)

        })

```
