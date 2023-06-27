package my.paidchain.spectraterminaldemo.common.secureElement

interface ISecureElement {
    fun onKeySuccess()
    fun onKeyFail(reason: String)
}