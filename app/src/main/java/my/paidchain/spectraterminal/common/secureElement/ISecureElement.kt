package my.paidchain.spectraterminal.common.secureElement

interface ISecureElement {
    fun onKeySuccess()
    fun onKeyFail(reason: String)
}