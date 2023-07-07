package com.zapp.app.fcm

data class TokenRequest(val fcmToken: String, val userId: Int, val deviceId: String)
