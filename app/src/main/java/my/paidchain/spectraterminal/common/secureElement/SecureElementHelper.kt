package my.paidchain.spectraterminal.common.secureElement

import android.content.Context
import com.spectratech.controllers.*
import mu.KotlinLogging
import my.paidchain.spectraterminal.common.Misc.Companion.toHex
import java.lang.ref.WeakReference
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec


class SecureElementHelper(val context: Context) {

    private var iSecureElement = WeakReference<ISecureElement>(null)
    private var keyType: KeyType = KeyType.EMPTY
    private var ksn: ByteArray? = null
    private var keyData: ByteArray? = null

    private val logger = KotlinLogging.logger{}
    enum class KeyType(val value: Int) {
        EMPTY(0),
        PIN(1),
        TLE(2),
        TMK(3),

    }

    enum class A(val value: Int) {
        PIN(1),
        TLE(2),
    }

    private val TAG = "SecureElementHelper"
    private var mKeyDllController: KeyDllController? = null;

    private val keyIndex = mutableMapOf<String, Int>()

    fun injectKey(keyType: KeyType, ksn: ByteArray?, keyData: ByteArray?){

        this.keyType = keyType
        this.ksn = ksn
        this.keyData = keyData

        mKeyDllController = KeyDllController.getControllerInstance(context, MyDelegate())
        SPDeviceController.enableDebugLog(true)
        mKeyDllController?.connectController()

    }
    fun addListener(iSecureElement: ISecureElement){
        this.iSecureElement = WeakReference(iSecureElement)
    }
    fun startInject(): Int?{

        //currently fixed for TLE
        var nRet: Int? = 0

        logger.info { "callKeyInjectionRawDukptPIN: Start" }
        val tmkIdx: Short = -1

        val keyIdx: Short = 1 //(short) Str.toInt("2");
        logger.info {"key callKeyInjectionRawDukptPIN, tmkIdx $tmkIdx, keyIdx $keyIdx"}

        var encryptCipher: Cipher? = null

        // cal kvc 1st
        //36729134
        var kvc = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        try {
            encryptCipher = Cipher.getInstance("DESede/ECB/NoPadding")
            encryptCipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyData, "DESede"))
            kvc = encryptCipher.doFinal(kvc)



        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }

        //kvc = kvc.copyOfRange(0, 3)
        //kvc = Utils.hexString2Bytes("9B142618");
        //kvc = Utils.hexString2Bytes("9B142618");
        val algorithm = PEDDLL.VALUE_KEY_ALGORITHM_DUKPT
        val usage = PEDDLL.VALUE_KEY_USAGE_PIN

        logger.info { "key callKeyInjectionRawDukptPIN, KCV ${kvc.toHex()}" }
        logger.info { "key callKeyInjectionRawDukptPIN, KSN ${ksn?.toHex()}" }
        logger.info { "key callKeyInjectionRawDukptPIN, Key ${keyData?.toHex()}" }

        val resultCode: Int? = mKeyDllController?.keyInject(
            context,
            ksn,
            tmkIdx,
            keyIdx,
            keyData,
            algorithm,
            usage,
            kvc
        )

        logger.info { "key callKeyInjectionRawDukptPIN result code:$resultCode"  }



        nRet = resultCode

        return nRet
    }
    private inner class MyDelegate: KeyDllController.KeyDllDelegate {
        private val logger = KotlinLogging.logger {}
        override fun onError(p0: ControllerError.Error?, p1: String?) {
            logger.info { "onError:$p0 $p1" }
        }

        override fun onControllerConnected() {
            logger.info { "onControllerConnected" }

            val nTLEKeyRet: Int? = startInject()
            if(nTLEKeyRet == -1){
                iSecureElement.get()?.onKeySuccess()
            }else {
                iSecureElement.get()?.onKeyFail(nTLEKeyRet.toString())
            }
            logger.info { "kye ret: $nTLEKeyRet" }
            mKeyDllController?.disconnectController()
        }

        override fun onControllerDisconnected() {
            logger.info { "onControllerDisconnected" }
        }

        override fun onDeviceInfoReceived(p0: Hashtable<String, String>?) {
            logger.info { "onError:$p0" }
        }

        override fun onMessageReceived(p0: ControllerMessage.MessageText?) {
            logger.info { "onError:$p0" }
        }

    }

}
