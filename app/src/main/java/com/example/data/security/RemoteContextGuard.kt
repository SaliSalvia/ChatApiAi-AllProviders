package com.example.data.security

/** Defense in depth for automatically collected project context. This is not a
 * substitute for the user's review: arbitrary secrets can appear in any file. */
object RemoteContextGuard {
    private val sensitiveNames = setOf(
        "id_rsa", "id_ed25519", "credentials", "credentials.json", "service-account.json",
        "google-services.json", "release.keystore", "debug.keystore", "local.properties"
    )
    private val sensitiveExtensions = setOf("pem", "p12", "pfx", "jks", "keystore", "key", "asc")
    private val assignment = Regex("""(?i)(\b(?:[a-z0-9_-]*?(?:api[_-]?key|secret(?:[_-]?key)?|token|password|authorization|private[_-]?key))\b\s*[:=]\s*["']?)([^\s"',;]+)""")
    private val bearer = Regex("(?i)(\\bBearer\\s+)[A-Za-z0-9._~+/=-]+")
    private val privateBlock = Regex("-----BEGIN [A-Z ]*PRIVATE KEY-----[\\s\\S]*?-----END [A-Z ]*PRIVATE KEY-----")

    fun isSensitivePath(path: String): Boolean {
        val parts = path.replace('\\', '/').split('/').map { it.lowercase() }
        val name = parts.lastOrNull().orEmpty()
        return parts.any { it == ".ssh" || it == ".aws" || it == ".gnupg" } ||
            name == ".env" || name.startsWith(".env.") ||
            name in sensitiveNames || name.endsWith(".mobileprovision") ||
            name.substringAfterLast('.', "") in sensitiveExtensions
    }

    fun redact(text: String): String = privateBlock.replace(
        assignment.replace(bearer.replace(text, "$1[REDACTED]"), "$1[REDACTED]")
    ) { "[PRIVATE KEY REDACTED]" }
}
