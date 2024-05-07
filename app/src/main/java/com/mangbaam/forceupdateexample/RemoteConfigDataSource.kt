package com.mangbaam.forceupdateexample

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.get
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteConfigDataSource(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    suspend fun getValue(key: String): FirebaseRemoteConfigValue? = suspendCoroutine { continuation ->
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(remoteConfig[key].also {
                    Log.d("MANGBAAM-RemoteConfigDataSource(getValue)", it.asString())
                })
            } else {
                continuation.resume(null)
            }
        }
    }
    suspend fun getString(key: String, defaultValue: String): String = getValue(key)?.asString() ?: defaultValue
}
