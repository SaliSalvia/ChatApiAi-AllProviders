package com.example.data.workspace

import com.example.domain.model.WorkspaceVersionDiff
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

/** Applies an explicitly reviewed subset of B's changes onto a NEW copy of A.
 * No source directory or original ZIP is ever modified. The caller owns the staging
 * directory and must delete it if this operation fails. This does not run a build. */
class WorkspaceMergeEngine(private val maxBytes: Long = 1024L * 1024 * 1024) {
    fun apply(left: File, right: File, destination: File, diff: WorkspaceVersionDiff, selected: Set<String>) {
        require(left.isDirectory && right.isDirectory) { "Both source versions must exist" }
        require(selected.isNotEmpty()) { "Select at least one changed path" }
        require(!destination.exists()) { "Derived workspace already exists" }
        require(maxBytes > 0) { "Invalid storage limit" }
        val allowed = (diff.added + diff.removed + diff.changed).map { it.relativePath }.toSet()
        require(selected.all { it in allowed }) { "Selection contains a path outside the reviewed diff" }
        val leftRoot = left.canonicalFile.toPath()
        val rightRoot = right.canonicalFile.toPath()
        val targetRoot = destination.canonicalFile.toPath()
        require(!targetRoot.startsWith(leftRoot) && !targetRoot.startsWith(rightRoot)) {
            "Derived workspace cannot be inside an original"
        }
        var copied = 0L
        fun safePath(root: File, path: String): File {
            require(path.isNotBlank() && !path.startsWith('/') &&
                path.split('/').none { it == ".." || it == "." || it.isEmpty() } &&
                !path.contains('\\') && !path.contains('\u0000')) { "Invalid path: $path" }
            val file = File(root, path)
            require(file.canonicalFile.toPath().startsWith(root.canonicalFile.toPath())) { "Path escapes workspace" }
            return file
        }
        val removed = diff.removed.map { it.relativePath }.toSet()
        val rightHashes = (diff.added + diff.changed).associate { it.relativePath to it.rightSha256 }
        fun copyChecked(root: File, path: String, expectedHash: String? = null) {
            val source = safePath(root, path)
            require(!Files.isSymbolicLink(source.toPath()) && source.isFile) { "Unsafe source: $path" }
            val target = safePath(destination, path)
            require(!Files.isSymbolicLink(target.toPath())) { "Unsafe target: $path" }
            require(source.length() <= maxBytes - copied) { "Derived workspace exceeds storage limit" }
            require(target.parentFile?.mkdirs() == true || target.parentFile?.isDirectory == true)
            val hash = MessageDigest.getInstance("SHA-256")
            source.inputStream().buffered().use { input ->
                target.outputStream().buffered().use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        copied += count
                        require(copied <= maxBytes) { "Derived workspace exceeds storage limit" }
                        hash.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                    }
                }
            }
            if (expectedHash != null) {
                val actual = hash.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
                require(actual == expectedHash) { "Source changed during copy: $path" }
            }
        }
        try {
            require(destination.mkdirs()) { "Cannot create derived workspace" }
            left.walkTopDown().onEnter { dir ->
                require(!Files.isSymbolicLink(dir.toPath())) { "Symlink in baseline" }
                true
            }.filter { it.isFile }.forEach { file ->
                val path = file.relativeTo(left).invariantSeparatorsPath
                copyChecked(left, path)
            }
            selected.sorted().forEach { path ->
                val target = safePath(destination, path)
                if (path in removed) {
                    require(!target.isDirectory) { "Cannot remove a directory as a file" }
                    if (target.exists()) require(target.delete()) { "Could not remove $path" }
                } else {
                    // If a parent path is a file in A, do not silently overwrite it.
                    copyChecked(right, path, rightHashes[path])
                }
            }
        } catch (error: Exception) {
            destination.deleteRecursively()
            throw error
        }
    }
}
