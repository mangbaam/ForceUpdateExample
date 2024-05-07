package com.mangbaam.forceupdateexample

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * @return true 이면 업데이트 필요
 */
fun checkShouldUpdate(currentVersion: String, minVersion: String): Boolean {
    if (!Regex("""^\d+\.\d+(\.\d+)?$""").matches(currentVersion)) return false
    if (!Regex("""^\d+\.\d+(\.\d+)?$""").matches(minVersion)) return false

    return currentVersion.split('.').map(String::toInt).let {
        val min = minVersion.split('.').map(String::toInt)
        !KotlinVersion(it[0], it[1], it.getOrNull(2) ?: 0)
            .isAtLeast(min[0], min[1], min.getOrNull(2) ?: 0)
    }
}

class ConfigRepository(
    private val remoteConfigDataSource: RemoteConfigDataSource,
) {
    fun shouldUpdate(): Flow<Boolean> = flow {
        remoteConfigDataSource.getString(KEY_MIN_VERSION, "0.0.0").let { minVersion ->
            val currentVersion = BuildConfig.APP_VERSION
            Log.d("mangbaam_ConfigRepository", "checkMinVersion: current: $currentVersion, min: $minVersion")
            emit(checkShouldUpdate(currentVersion, minVersion))
        }
    }

    companion object {
        const val KEY_MIN_VERSION = "MinVersion"
    }
}
