package com.example.data.workspace

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class WorkspaceMergeEngineTest {
    @Test fun appliesOnlyReviewedChangesWithoutMutatingInputs() {
        val temp = Files.createTempDirectory("merge-review").toFile()
        try {
            val a = temp.resolve("a").apply { mkdirs() }
            val b = temp.resolve("b").apply { mkdirs() }
            val derived = temp.resolve("derived")
            a.resolve("changed.txt").writeText("old")
            a.resolve("removed.txt").writeText("remove me")
            a.resolve("keep.txt").writeText("keep")
            b.resolve("changed.txt").writeText("new")
            b.resolve("added.txt").writeText("new file")
            b.resolve("keep.txt").writeText("keep")
            val diff = WorkspaceDiffEngine().compare("a", a, "b", b)
            WorkspaceMergeEngine().apply(a, b, derived, diff, setOf("changed.txt", "removed.txt"))
            assertEquals("new", derived.resolve("changed.txt").readText())
            assertFalse(derived.resolve("removed.txt").exists())
            assertFalse(derived.resolve("added.txt").exists())
            assertEquals("keep", derived.resolve("keep.txt").readText())
            assertEquals("old", a.resolve("changed.txt").readText())
            assertEquals("remove me", a.resolve("removed.txt").readText())
        } finally { temp.deleteRecursively() }
    }

    @Test fun refusesUnreviewedPathsAndCleansUpOnLimitFailure() {
        val temp = Files.createTempDirectory("merge-safety").toFile()
        try {
            val a = temp.resolve("a").apply { mkdirs() }
            val b = temp.resolve("b").apply { mkdirs() }
            val dest = temp.resolve("dest")
            a.resolve("keep.txt").writeText("baseline")
            b.resolve("keep.txt").writeText("changed")
            val diff = WorkspaceDiffEngine().compare("a", a, "b", b)
            assertThrows(IllegalArgumentException::class.java) {
                WorkspaceMergeEngine().apply(a, b, dest, diff, setOf("../../outside"))
            }
            assertFalse(dest.exists())
            assertThrows(IllegalArgumentException::class.java) {
                WorkspaceMergeEngine(maxBytes = 1).apply(a, b, dest, diff, setOf("keep.txt"))
            }
            assertFalse(dest.exists())
        } finally { temp.deleteRecursively() }
    }
}
