package com.hkm.stickhub.cloud

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CloudVaultCredentialsTest {
    @Test
    fun recoveryCodeRoundTripsVaultIdAndSecret() {
        val original = CloudVaultCredentials.generate()

        val parsed = CloudVaultCredentials.parseRecoveryCode(original.recoveryCode)

        assertEquals(original.vaultId, parsed.vaultId)
        assertArrayEquals(original.secret, parsed.secret)
        assertEquals(original.secretHashHex, parsed.secretHashHex)
    }

    @Test
    fun generatedCredentialsAreUnique() {
        val first = CloudVaultCredentials.generate()
        val second = CloudVaultCredentials.generate()

        assertNotEquals(first.vaultId, second.vaultId)
        assertNotEquals(first.recoveryCode, second.recoveryCode)
    }

    @Test
    fun malformedRecoveryCodeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            CloudVaultCredentials.parseRecoveryCode("SH1.not-a-vault.not-a-secret")
        }
    }
}
