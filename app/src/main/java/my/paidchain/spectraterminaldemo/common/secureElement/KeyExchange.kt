package my.paidchain.spectraterminaldemo.common.secureElement

import my.paidchain.spectraterminaldemo.common.sha256
import my.paidchain.spectraterminaldemo.common.sha256ToBytes
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class KeyExchange constructor(val keyPair: KeyPair) {
    companion object {
        fun generateKeyPair(keySize: Int): KeyPair {
            val keyGen = KeyPairGenerator.getInstance("DH")
            keyGen.initialize(keySize)
            return keyGen.generateKeyPair()
        }
    }

    val fingerprint: String
        get() {
            return _fingerprint ?: run {
                _fingerprint = sha256(keyPair.public.encoded)
                _fingerprint!!
            }
        }
    private var _fingerprint: String? = null

    fun sharedSecret(targetPublicKey: PublicKey, keySize: Int, iv: ByteArray): ByteArray {
        val keyAgreement = KeyAgreement.getInstance("DH")

        keyAgreement.init(keyPair.private)
        keyAgreement.doPhase(targetPublicKey, true)

        val sharedSecret = sha256ToBytes(keyAgreement.generateSecret())
        val iterations = 256

        val keySpec = PBEKeySpec(String(sharedSecret, Charsets.UTF_8).toCharArray(), iv, iterations, keySize)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val derivedKey = secretKeyFactory.generateSecret(keySpec)

        return derivedKey.encoded
    }

    fun decodeDhPublicKey(encoded: ByteArray): PublicKey {
        val keyFactory = KeyFactory.getInstance("DH")
        val publicKeySpec = X509EncodedKeySpec(encoded)

        return keyFactory.generatePublic(publicKeySpec)
    }
}