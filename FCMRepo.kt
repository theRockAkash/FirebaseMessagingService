package com.zapp.app.fcm

import android.util.Log
import com.zapp.app.retrofit.Api
import com.zapp.app.utils.Constants.TAG
import com.zapp.app.utils.NetworkManager
import com.zapp.app.utils.PreferenceHelper
import com.zapp.app.utils.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FCMRepo(
    private val api: Api,
    private val networkManager: NetworkManager,
    private val preferenceHelper: PreferenceHelper
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun sendFcmTokenToServer(token: String, deviceId: String) {
        Log.w(TAG, "request sendFcmTokenToServer: | $token")
        preferenceHelper.saveString(token, PreferenceHelper.FCM_TOKEN)
        if (!networkManager.isNetworkAvailable()) {
            Log.w(TAG, "sendFcmTokenToServer: No Internet")
            return
        }

        GlobalScope.launch {
            val response = api.sendFcmTokenTOServer(
                TokenRequest(
                    token,
                    preferenceHelper.getInt(PreferenceHelper.USER_ID),
                    deviceId
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.w(TAG, "sendFcmTokenToServer: success")
            } else Log.w(
                TAG,
                "sendFcmTokenToServer: failed- ${Utils.getError(response.errorBody())}",
            )
        }
    }
}