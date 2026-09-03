package com.hkm.stickhub.ui.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryStartupRefreshPolicyTest {

    @Test
    fun requestsExactlyOneInitialRefreshForEachLibraryComposition() {
        val policy = LibraryStartupRefreshPolicy()

        assertTrue(policy.claimInitialRefresh())
        assertFalse(policy.claimInitialRefresh())
    }
}
