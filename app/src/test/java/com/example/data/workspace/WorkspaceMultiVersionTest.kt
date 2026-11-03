package com.example.data.workspace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkspaceMultiVersionTest {
    @Test
    fun multipleVersionsKeepCodeMarkdownDocxAndPdfInputs() = runBlocking {
        val manager = ZipWorkspaceManager(ApplicationProvider.getApplicationContext<Context>())
        val v1 = manager.extractZipToWorkspace(versionZip("v1", "legacy"), "project-v1.zip").getOrThrow()
        val v2 = manager.extractZipToWorkspace(versionZip("v2", "secure"), "project-v2.zip").getOrThrow()

        assertTrue(v1.workspaceId != v2.workspaceId)
        assertTrue(v1.archivePath?.let { java.io.File(it).isFile } == true)
        assertTrue(v1.archiveSha256?.matches(Regex("[0-9a-f]{64}")) == true)
        assertTrue(manager.getStoredArchive(v2.workspaceId)?.isFile == true)
        assertTrue(v1.totalFiles >= 5)
        assertTrue(v2.totalFiles >= 5)

        val v1Chunks = manager.getRelevantWorkspaceChunks(v1.workspaceId, "architecture secure documentation", 20)
        val v2Chunks = manager.getRelevantWorkspaceChunks(v2.workspaceId, "architecture secure documentation", 20)
        val combined = (v1Chunks + v2Chunks).joinToString("\n") { it.content }

        assertTrue(combined.contains("legacy") || combined.contains("secure"))
        assertTrue(combined.contains("DOCX evidence"))
        assertTrue(combined.contains("Markdown evidence"))
        assertTrue(v1Chunks.any { it.filePath.endsWith("report.pdf") })
        assertTrue(v2Chunks.any { it.filePath.endsWith("report.pdf") })

        val deepContext = manager.buildDeepAnalysisContext(
            listOf(v1.workspaceId, v2.workspaceId),
            "compare architecture secure documentation",
            maxChunksPerWorkspace = 20
        )
        assertTrue(deepContext.contains("PROJECT VERSION 1"))
        assertTrue(deepContext.contains("PROJECT VERSION 2"))
        assertTrue(deepContext.contains("Main.kt"))
        assertTrue(deepContext.contains("DOCX evidence"))
    }

    @Test
    fun deepContextIncludesManifestForAllTwentyCandidates() = runBlocking {
        val manager = ZipWorkspaceManager(ApplicationProvider.getApplicationContext<Context>())
        val versions = (1..20).map { index ->
            manager.extractZipToWorkspace(
                smallVersionZip(index),
                "candidate-$index.zip"
            ).getOrThrow()
        }

        val context = manager.buildDeepAnalysisContext(
            workspaceIds = versions.map { it.workspaceId },
            userQuery = "compare architecture and features",
            maxChunksPerWorkspace = 4,
            maxTotalChars = 12_000
        )

        versions.forEach { version ->
            assertTrue(
                "Every candidate must appear in the fair manifest: ${version.workspaceId}",
                context.contains(version.workspaceId)
            )
        }
        assertTrue(context.contains("Candidates requested: 20"))
        assertTrue(context.contains("VERSION 20"))
        assertTrue(context.length <= 12_000)
    }

    @Test
    fun zipSlipEntryIsRejectedAndPartialWorkspaceIsCleaned() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = ZipWorkspaceManager(context)
        val before = File(context.filesDir, "sali_workspaces").listFiles()?.size ?: 0
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            add(zip, "../escaped.txt", "must not escape workspace")
        }

        val result = manager.extractZipToWorkspace(output.toByteArray().inputStream(), "malicious.zip")

        assertTrue("Path traversal archive must fail", result.isFailure)
        val after = File(context.filesDir, "sali_workspaces").listFiles()?.size ?: 0
        assertTrue("Failed import must not leave a partial workspace", after <= before)
        assertTrue("No file may be written outside the workspace", !File(context.filesDir, "escaped.txt").exists())
    }

    @Test
    fun ambiguousRepeatedSeparatorPathIsRejected() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = ZipWorkspaceManager(context)
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            add(zip, "src//Main.kt", "ambiguous path")
        }

        val result = manager.extractZipToWorkspace(output.toByteArray().inputStream(), "ambiguous.zip")
        assertTrue("Ambiguous normalized path must be rejected", result.isFailure)
    }

    @Test
    fun invalidOrTruncatedZipLeavesNoWorkspaceOrArchive() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = ZipWorkspaceManager(context)
        val roots = File(context.filesDir, "sali_workspaces")
        val archives = File(context.filesDir, "sali_archives")
        val beforeRoots = roots.listFiles().orEmpty().map { it.name }.toSet()
        val beforeArchives = archives.listFiles().orEmpty().map { it.name }.toSet()
        val zipBytes = ByteArrayOutputStream().also { output ->
            ZipOutputStream(output).use { zip -> add(zip, "README.md", "test") }
        }.toByteArray()
        listOf("not a zip".toByteArray(), zipBytes.copyOf(zipBytes.size - 16)).forEach { bad ->
            assertTrue(manager.extractZipToWorkspace(bad.inputStream(), "bad.zip").isFailure)
        }
        assertTrue(beforeRoots == roots.listFiles().orEmpty().map { it.name }.toSet())
        assertTrue(beforeArchives == archives.listFiles().orEmpty().map { it.name }.toSet())
    }

    @Test
    fun excessivelyDeepEntryIsRejectedBeforeFileTreeCanOverflow() = runBlocking {
        val manager = ZipWorkspaceManager(ApplicationProvider.getApplicationContext<Context>())
        val zip = ByteArrayOutputStream().also { output ->
            ZipOutputStream(output).use { archive ->
                add(archive, (1..65).joinToString("/") { "d" } + "/src.kt", "code")
            }
        }
        assertTrue(manager.extractZipToWorkspace(zip.toByteArray().inputStream(), "deep.zip").isFailure)
    }

    @Test
    fun contextRanksRelevantFilesAndOmitsCredentialFiles() = runBlocking {
        val manager = ZipWorkspaceManager(ApplicationProvider.getApplicationContext<Context>())
        val zip = ByteArrayOutputStream().also { output ->
            ZipOutputStream(output).use { archive ->
                repeat(140) { add(archive, "a/filler$it.kt", "fun irrelevant$it() = Unit") }
                add(archive, "z/payment-api.kt", "fun paymentApi() = \"payment evidence\"")
                add(archive, ".env", "OPENAI_API_KEY=never-send-me")
                add(archive, "z/credentials.json", "{\"secret\":\"never-send-me\"}")
            }
        }
        val version = manager.extractZipToWorkspace(zip.toByteArray().inputStream(), "ranking.zip").getOrThrow()
        val chunks = manager.getRelevantWorkspaceChunks(version.workspaceId, "payment", 3)
        assertTrue(chunks.any { it.filePath == "z/payment-api.kt" && "payment evidence" in it.content })
        assertTrue(chunks.none { "never-send-me" in it.content })
    }

    @Test
    fun exportCannotEscapeCacheAndCleansPartialFileOnFailure() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = ZipWorkspaceManager(context)
        val version = manager.extractZipToWorkspace(smallVersionZip(1), "export.zip").getOrThrow()
        val exported = manager.exportWorkspaces(listOf(version.workspaceId), "../../outside")
        assertTrue(exported.canonicalFile.toPath().startsWith(context.cacheDir.canonicalFile.toPath()))
        assertTrue(exported.isFile)
        exported.delete()
        val before = context.cacheDir.listFiles().orEmpty().map { it.name }.toSet()
        assertTrue(runCatching { manager.exportWorkspaces(listOf("../outside"), "unsafe") }.isFailure)
        assertTrue(before == context.cacheDir.listFiles().orEmpty().map { it.name }.toSet())
    }

    private fun smallVersionZip(index: Int): java.io.InputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            add(zip, "README.md", "Candidate $index architecture notes")
            add(zip, "src/Main.kt", "fun candidate$index() = \"feature-$index\"")
        }
        return output.toByteArray().inputStream()
    }

    private fun versionZip(version: String, feature: String): java.io.InputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            add(zip, "src/Main.kt", "fun main() = println(\"$feature\")")
            add(zip, "README.md", "Markdown evidence for $version architecture")
            add(zip, "docs/report.pdf", "PDF evidence for $version")
            addBytes(zip, "docs/notes.docx", docxBytes("DOCX evidence for $version"))
            add(zip, "config.json", "{\"version\":\"$version\",\"feature\":\"$feature\"}")
        }
        return output.toByteArray().inputStream()
    }

    private fun docxBytes(text: String): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { docx ->
            add(docx, "word/document.xml", "<w:document><w:body><w:p><w:r><w:t>$text</w:t></w:r></w:p></w:body></w:document>")
        }
        return output.toByteArray()
    }

    private fun addBytes(zip: ZipOutputStream, path: String, content: ByteArray) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(content)
        zip.closeEntry()
    }

    private fun add(zip: ZipOutputStream, path: String, content: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(content.toByteArray())
        zip.closeEntry()
    }
}
