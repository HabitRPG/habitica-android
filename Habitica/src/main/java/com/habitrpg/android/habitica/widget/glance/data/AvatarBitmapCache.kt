package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.views.AvatarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

object AvatarBitmapCache {
    private const val FILENAME = "widget_avatar.png"
    private const val HASH_FILENAME = "widget_avatar.hash"
    private const val RENDER_TIMEOUT_MS = 10_000L
    private const val HASH_VERSION = "v2"

    fun cachedFile(context: Context): File =
        File(context.applicationContext.filesDir, FILENAME)

    private fun hashFile(context: Context): File =
        File(context.applicationContext.filesDir, HASH_FILENAME)

    fun cachedBitmap(context: Context): Bitmap? {
        val file = cachedFile(context)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (t: Throwable) {
            null
        }
    }

    suspend fun refreshIfNeeded(context: Context, user: User?) {
        if (user == null) return
        val newHash = hashOfUser(user)
        val current = readHash(context)
        if (current == newHash && cachedFile(context).exists()) return
        val bitmap = renderAvatar(context, user) ?: return
        withContext(Dispatchers.IO) {
            try {
                FileOutputStream(cachedFile(context)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                hashFile(context).writeText(newHash)
            } catch (t: Throwable) {
            }
        }
    }

    private fun readHash(context: Context): String? = try {
        hashFile(context).takeIf { it.exists() }?.readText()
    } catch (t: Throwable) { null }

    private fun hashOfUser(user: User): String {
        val prefs = user.preferences
        val equipped = user.items?.gear?.equipped
        val costume = user.items?.gear?.costume
        val parts = listOf(
            HASH_VERSION,
            prefs?.background, prefs?.chair, prefs?.skin, prefs?.shirt, prefs?.size,
            prefs?.sleep?.toString(), prefs?.costume?.toString(),
            prefs?.hair?.color, prefs?.hair?.base?.toString(), prefs?.hair?.bangs?.toString(),
            prefs?.hair?.mustache?.toString(), prefs?.hair?.beard?.toString(), prefs?.hair?.flower?.toString(),
            equipped?.armor, equipped?.back, equipped?.body, equipped?.head, equipped?.shield,
            equipped?.weapon, equipped?.eyeWear, equipped?.headAccessory,
            costume?.armor, costume?.back, costume?.body, costume?.head, costume?.shield,
            costume?.weapon, costume?.eyeWear, costume?.headAccessory,
            user.items?.currentMount, user.items?.currentPet,
            user.stats?.buffs?.seafoam?.toString(),
            user.stats?.buffs?.shinySeed?.toString(),
            user.stats?.buffs?.snowball?.toString(),
            user.stats?.buffs?.spookySparkles?.toString(),
            user.stats?.habitClass,
        )
        return parts.joinToString("|") { it ?: "" }.hashCode().toString()
    }

    private suspend fun renderAvatar(context: Context, user: User): Bitmap? =
        withContext(Dispatchers.Main) {
            withTimeoutOrNull(RENDER_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val avatarView = AvatarView(
                        context.applicationContext,
                        showBackground = true,
                        showMount = true,
                        showPet = true,
                    )
                    avatarView.setAvatar(user)
                    avatarView.onAvatarImageReady { bitmap ->
                        if (cont.isActive) {
                            cont.resume(bitmap)
                        }
                    }
                }
            }
        }
}
