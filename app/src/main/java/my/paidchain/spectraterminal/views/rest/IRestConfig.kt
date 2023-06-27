package my.paidchain.spectraterminal.views.rest

interface IRestConfig {
    fun onConfigUpdateSuccess(code: Int?, result: String?)
    fun onConfigUpdateFail(code: Int?, result: String?)
}