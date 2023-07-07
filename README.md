# FirebaseMessagingService


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
