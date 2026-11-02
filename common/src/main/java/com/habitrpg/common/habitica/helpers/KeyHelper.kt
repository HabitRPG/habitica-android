package com.habitrpg.common.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.habitrpg.shared.habitica.HLogger
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.UnrecoverableKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.security.cert.CertificateException

// https://stackoverflow.com/a/42716982
class KeyHelper
    @Throws(
        NoSuchPaddingException::class,
        NoSuchProviderException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        KeyStoreException::class,
        CertificateException::class,
        IOException::class,
    )
    constructor(
        ctx: Context,
        var sharedPreferences: SharedPreferences,
        var keyStore: KeyStore?,
    ) {
        private val aesKeyFromKS: Key?
            @Throws(
                NoSuchProviderException::class,
                NoSuchAlgorithmException::class,
                InvalidAlgorithmParameterException::class,
                KeyStoreException::class,
                CertificateException::class,
                IOException::class,
                UnrecoverableKeyException::class,
            )
            get() {
                return keyStore?.getKey(KEY_ALIAS, null) as? SecretKey
            }

        init {
            this.generateEncryptKey(ctx)
        }

        @Throws(
            NoSuchProviderException::class,
            NoSuchAlgorithmException::class,
            InvalidAlgorithmParameterException::class,
            KeyStoreException::class,
            CertificateException::class,
            IOException::class,
        )
        private fun generateEncryptKey(ctx: Context) {
            keyStore?.load(null)

            if (keyStore?.containsAlias(KEY_ALIAS) == false) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                keyGenerator.init(
                    KeyGenParameterSpec
                        .Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build(),
                )
                keyGenerator.generateKey()
            }
        }

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            NoSuchProviderException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class,
            UnsupportedEncodingException::class,
        )
        fun encrypt(input: String): String {
            val c: Cipher
            val publicIV = getRandomIV()

            c = Cipher.getInstance(AES_MODE_M)
            try {
                c.init(
                    Cipher.ENCRYPT_MODE,
                    aesKeyFromKS,
                    GCMParameterSpec(128, Base64.decode(publicIV, Base64.DEFAULT)),
                )
            } catch (e: Exception) {
                HLogger.logException("KeyHelper", "Error encrypting", e)
            }
            val encodedBytes = c.doFinal(input.toByteArray(charset("UTF-8")))
            return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
        }

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            NoSuchProviderException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class,
            UnsupportedEncodingException::class,
        )
        fun decrypt(encrypted: String): String? {
            val publicIV = getRandomIV()
            val c = Cipher.getInstance(AES_MODE_M)
            try {
                c.init(
                    Cipher.DECRYPT_MODE,
                    aesKeyFromKS,
                    GCMParameterSpec(128, Base64.decode(publicIV, Base64.DEFAULT)),
                )
            } catch (e: Exception) {
                HLogger.logException("KeyHelper", "Error decrypting", e)
            }

            return try {
                val decodedValue =
                    Base64.decode(encrypted.toByteArray(charset("UTF-8")), Base64.DEFAULT)
                val decryptedVal = c.doFinal(decodedValue)
                String(decryptedVal)
            } catch (_: IllegalArgumentException) {
                null
            } catch (_: GeneralSecurityException) {
                null
            } catch (_: IllegalStateException) {
                null
            }
        }

        private fun getRandomIV(): String {
            var publicIV = sharedPreferences.getString(PUBLIC_IV, null)

            if (publicIV == null) {
                val random = SecureRandom()
                val generated = random.generateSeed(12)
                publicIV = Base64.encodeToString(generated, Base64.DEFAULT)
                sharedPreferences.edit {
                    putString(PUBLIC_IV, publicIV)
                }
            }
            return publicIV ?: ""
        }

        companion object {
            private const val AES_MODE_M = "AES/GCM/NoPadding"

            private const val KEY_ALIAS = "KEY"
            private const val ANDROID_KEY_STORE = "AndroidKeyStore"
            const val PUBLIC_IV = "PUBLIC_IV"
            private var keyHelper: KeyHelper? = null

            fun getInstance(
                ctx: Context,
                sharedPreferences: SharedPreferences,
                keyStore: KeyStore,
            ): KeyHelper? {
                if (keyHelper == null) {
                    try {
                        keyHelper = KeyHelper(ctx, sharedPreferences, keyStore)
                    } catch (e: NoSuchPaddingException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    } catch (e: NoSuchProviderException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    } catch (e: NoSuchAlgorithmException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    } catch (e: InvalidAlgorithmParameterException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    } catch (e: KeyStoreException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    } catch (e: CertificateException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    } catch (e: IOException) {
                        HLogger.logException("KeyHelper", "Error initializing", e)
                    }
                }
                return keyHelper
            }
        }
    }
