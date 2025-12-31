package com.farzane.securenote.core.util

import kotlin.code
import kotlin.collections.indices
import kotlin.experimental.xor
import kotlin.text.toCharArray

/**
 * A simple helper object for encrypting and decrypting text.
 * This is used to protect the note content when it's saved to the database.
 */
object EncryptionHelper {
    // A secret key used for the encryption algorithm.
    // In a real production app, this key should be stored securely (e.g., in Android Keystore)
    // and not hardcoded directly in the source code.
    private const val SECRET_KEY = "MySuperSecretKeyForSecureNoteApp"

    /**
     * Scrambles or unscrambles a string using a simple XOR cipher.
     *
     * This function is reversible: calling it once encrypts the text,
     * and calling it a second time with the scrambled text will decrypt it.
     *
     * @param input The string to be encrypted or decrypted.
     * @return The processed (scrambled or unscrambled) string.
     */
    fun encryptDecrypt(input: String): String {
        val key = SECRET_KEY.toCharArray()
        val inputChars = input.toCharArray()
        val output = CharArray(inputChars.size)

        for (i in inputChars.indices) {
            // The XOR operation flips the bits of the character based on the key.
            output[i] = (inputChars[i].code xor key[i % key.size].code).toChar()
        }
        return String(output)
    }
}
