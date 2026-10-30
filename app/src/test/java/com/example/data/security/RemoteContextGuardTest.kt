package com.example.data.security

import org.junit.Assert.*
import org.junit.Test

class RemoteContextGuardTest {
    @Test fun filtersCommonSecretFilesAtAnyDepth() {
        listOf(".env", "dir/.env.production", "dir\\.ssh\\id_rsa", "config/credentials.json", "cert.pem", "local.properties")
            .forEach { assertTrue(it, RemoteContextGuard.isSensitivePath(it)) }
        assertFalse(RemoteContextGuard.isSensitivePath("src/main/App.kt"))
    }

    @Test fun redactsCommonInlineCredentials() {
        val content = "api_key = secret123\nOPENAI_API_KEY=sk-example\nAuthorization: Bearer abc.def\npassword: hidden\n" +
            "-----BEGIN PRIVATE KEY-----\nprivatebits\n-----END PRIVATE KEY-----"
        val result = RemoteContextGuard.redact(content)
        for (secret in listOf("secret123", "sk-example", "abc.def", "hidden", "privatebits")) {
            assertFalse(secret, result.contains(secret))
        }
        assertTrue(result.contains("[REDACTED]"))
        assertTrue(result.contains("[PRIVATE KEY REDACTED]"))
    }
}
