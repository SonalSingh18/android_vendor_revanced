//
// Copyright (C) 2025 kenway214
// SPDX-License-Identifier: Apache-2.0
//

package com.android.revanced

import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemProperties
import android.util.Log

object ReVancedManager {
    private const val TAG = "ReVancedManager"
    private const val PROPERTY_REVANCED_ENABLED = "persist.sys.revan.mod"
    private const val DEFAULT_ENABLED = true

    const val PACKAGE_YOUTUBE = "com.google.android.youtube"
    const val PACKAGE_YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"

    // Fallback sources when the packages are not visible to the package manager
    // (disabled for the current user, or not scanned yet on first boot).
    private val APK_PATHS = mapOf(
        PACKAGE_YOUTUBE to "/product/app/YouTube/$PACKAGE_YOUTUBE.apk",
        PACKAGE_YOUTUBE_MUSIC to "/product/app/YTMusic/$PACKAGE_YOUTUBE_MUSIC.apk",
    )

    fun isEnabled(): Boolean =
        SystemProperties.getBoolean(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED)

    fun setEnabled(enabled: Boolean): Boolean = try {
        SystemProperties.set(PROPERTY_REVANCED_ENABLED, if (enabled) "true" else "false")
        Log.i(TAG, "ReVanced ${if (enabled) "enabled" else "disabled"}")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to set ReVanced state: ${e.message}")
        false
    }

    fun getPropertyValue(): String =
        SystemProperties.get(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED.toString())

    /** Returns the version name of the installed app, or null if it cannot be determined. */
    fun getAppVersion(context: Context, packageName: String): String? {
        val pm = context.packageManager
        try {
            return pm.getPackageInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
                .versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Package $packageName not found, falling back to the prebuilt apk")
        }

        val path = APK_PATHS[packageName] ?: return null
        val version = pm.getPackageArchiveInfo(path, 0)?.versionName
        if (version == null) {
            Log.e(TAG, "Failed to read the version of $path")
        }
        return version
    }
}
