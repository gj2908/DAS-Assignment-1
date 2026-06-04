package com.example.core_sdk.network

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

internal object CryptoUtils {
    private const val ALGORITHM = "AES"
    private const val KEY = "1234567812345678" // Use a secure 16-byte key in production

    fun encrypt(data: String): String {
        val secretKey = SecretKeySpec(KEY.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
}
