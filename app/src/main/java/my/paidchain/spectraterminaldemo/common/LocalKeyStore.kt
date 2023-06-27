package my.paidchain.spectraterminaldemo.common

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.security.auth.x500.X500Principal

class LocalKeyStore {
    companion object{
        fun getDBKey(context: Context): String{

            var sRet: String = ""

            val nRet: Int = createLabel(getAndroidID(context))
            if(nRet == 1){
                sRet = getPrivateKey(getAndroidID(context))
            }

            return sRet
        }
        private fun getAndroidID(_c: Context): String {
            return Settings.Secure.getString(_c.contentResolver, Settings.Secure.ANDROID_ID)
                .uppercase(
                    Locale.getDefault()
                )
        }
        private fun createLabel(input: String): Int {
            var nRet = 1
            var kpg: KeyPairGenerator
            val nLabelSize: Int
            nLabelSize = 2048
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val nBefore = keyStore.size()
                val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
                // Create the keys if necessary
                if (!keyStore.containsAlias(input)) {

                    //KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Logger.doLog(sModule, "I", "AndroidKeystore for API >= 23");
                        val spec = KeyGenParameterSpec.Builder(
                            input,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                        )
                            .setDigests(
                                KeyProperties.DIGEST_SHA256,
                                KeyProperties.DIGEST_SHA512,
                                KeyProperties.DIGEST_MD5,
                                KeyProperties.DIGEST_NONE
                            )
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .build()

                        //   KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                        generator.initialize(spec)
                    } else {
                        //Logger.doLog(sModule, "I", "AndroidKeystore for API < 23");
                        val notBefore = Calendar.getInstance()
                        val notAfter = Calendar.getInstance()
                        notAfter.add(Calendar.YEAR, 100)

                        val spec = KeyGenParameterSpec.Builder(
                            input,
                            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                        )
                            .setCertificateSubject(X500Principal("CN=PAIDCHAINDBKEY, OU=IT, O=PAIDCHAIN, C=MY"))
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .setCertificateNotBefore(notBefore.time)
                            .setCertificateNotAfter(notAfter.time)
                            .setKeyValidityStart(notBefore.time)
                            .setKeyValidityEnd(notAfter.time)
                            .setKeySize(2048)
                            .setCertificateSerialNumber(BigInteger.valueOf(1))
                            .build()
                        generator.initialize(spec)
                    }
                    val keyPair = generator.generateKeyPair()


                } else {

                }
            } catch (e: Exception) {
                nRet = 0
            }
            return nRet
        }
        private fun getPrivateKey(input: String): String {
            var sRet = ""
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val privateKeyEntry = keyStore.getEntry(input, null) as KeyStore.PrivateKeyEntry
                val publicKey = privateKeyEntry.certificate.publicKey as RSAPublicKey

                val bRet = privateKeyEntry.certificate.encoded
                var nRet: Int
                val strBuff = StringBuilder("")
                for (x in bRet.size - 1 downTo bRet.size - 128) {
                    nRet = bRet[x].toInt() and 0xFF
                    val sByte = Integer.toHexString(nRet)
                    strBuff.append(String.format("%2s", sByte).replace(' ', '0'))
                }
                sRet = strBuff.toString()
            } catch (ee: java.lang.Exception) {

            }
            return sRet
        }
    }

}