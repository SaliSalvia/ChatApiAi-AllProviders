package com.example.data.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.AiProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApiKeySecurityTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val prefs get() = context.getSharedPreferences("sali_hcnsec_sec_store", Context.MODE_PRIVATE)

    @Before fun reset() { prefs.edit().clear().commit() }

    private class FakeCipher(private val fail: Boolean = false) : KeyCipher {
        override fun encrypt(plaintext: String): Pair<String, String> {
            if (fail) error("Keystore unavailable")
            return "encrypted-$plaintext" to "test-iv"
        }
        override fun decrypt(encryptedBase64: String, ivBase64: String): String =
            encryptedBase64.removePrefix("encrypted-")
    }

    @Test fun failedEncryptionNeverStoresPlaintextOrErasesExistingKey() {
        val good = DefaultApiKeyRepository(context) { FakeCipher() }
        good.saveProviderKey(AiProvider.HCNSEC, "old-key")
        val failing = DefaultApiKeyRepository(context) { FakeCipher(fail = true) }
        assertThrows(IllegalStateException::class.java) {
            failing.saveProviderKey(AiProvider.HCNSEC, "new-secret")
        }
        assertEquals("encrypted-old-key", prefs.getString("enc_api_key", null))
        assertEquals("old-key", good.getProviderKey(AiProvider.HCNSEC))
    }

    @Test fun oldPlaintextIsMigratedBeforeUse() {
        prefs.edit().putString("enc_api_key", "PLAIN:legacy-secret").commit()
        val repo = DefaultApiKeyRepository(context) { FakeCipher() }
        assertTrue(repo.hasAnyConfiguredProvider()) // no decryption on cold start
        assertEquals("legacy-secret", repo.getProviderKey(AiProvider.HCNSEC))
        assertEquals("encrypted-legacy-secret", prefs.getString("enc_api_key", null))
        assertTrue(repo.hasAnyConfiguredProvider())
    }

    @Test fun legacyPlaintextIsRemovedIfMigrationCannotEncrypt() {
        prefs.edit().putString("enc_api_key", "PLAIN:legacy-secret").commit()
        val repo = DefaultApiKeyRepository(context) { FakeCipher(fail = true) }
        assertNull(repo.getProviderKey(AiProvider.HCNSEC))
        assertNull(prefs.getString("enc_api_key", null))
    }

    @Test fun startupPresenceCheckDoesNotInitializeKeystore() {
        prefs.edit().putString("enc_api_key", "encrypted-value")
            .putString("enc_api_iv", "iv").commit()
        val repo = DefaultApiKeyRepository(context) { error("should not construct cipher") }
        assertTrue(repo.hasAnyConfiguredProvider())
    }
}
