package my.paidchain.spectraterminaldemo.common

enum class Errors(val value: String) {
    Unauthorised("Unauthorised"),
    NotReady("NotReady"),
    NotSigned("NotSigned"),
    NotOrigin("NotOrigin"),
    NotInitialized("NotInitialized"),
    NotConfigured("NotConfigured"),
    NotAvailable("NotAvailable"),
    NotSupported("NotSupported"),
    NotFound("NotFound"),
    Forbidden("Forbidden"),
    ParameterMissing("ParameterMissing"),
    InvalidParameter("InvalidParameter"),
    InvalidFormat("InvalidFormat"),
    Mismatched("Mismatched"),
    DatabaseError("DatabaseError"),
    ConnectionError("ConnectionError"),
    InvalidRequest("InvalidRequest"),
    InvalidResponse("InvalidResponse"),
    UnexpectedResponse("UnexpectedResponse"),
    InvalidMessage("InvalidMessage"),
    UnexpectedMessage("UnexpectedMessage"),
    NumericFault("NumericFault"),
    MissingNew("MissingNew"),
    Duplicated("Duplicated"),
    Failed("Failed"),
    Unknown("Unknown"),
    Censored("Censored"),
    Expired("Expired"),
    InvalidSignature("InvalidSignature"),
    Timeout("Timeout"),
    CorrelationError("CorrelationError"),
    CreationError("CreationError"),
    TerminationError("TerminationError"),
    PublishError("PublishError"),
    SubscribeError("SubscribeError"),
    IntercallError("IntercallError"),
    UnexpectedError("UnexpectedError"),
    ModelingError("ModelingError"),
    ValidationError("ValidationError"),
    RegistrationError("RegistrationError"),
    Cancelled("Cancelled"),
    OutOfRange("OutOfRange"),
    UIThreadError("UIThreadError")
}

class ContextAwareError : Throwable {
    val code: String
    val reason: String
    val params: Map<String, Any?>

    companion object {
        fun throwFromError(error: Throwable, code: String? = null, params: Map<String, Any?>? = null): Nothing {
            throw createFromError(error, code, params)
        }

        fun createFromError(error: Throwable, code: String? = null, params: Map<String, Any?>? = null): ContextAwareError {
            if (error is ContextAwareError) {
                if (null != params) {
                    error.params.plus(params)
                }
                return error
            }

            return ContextAwareError(code ?: Errors.Failed.value, message = error.message ?: "<EMPTY_MESSAGE>", params, error)
        }
    }

    constructor(code: String, message: String, params: Map<String, Any?>? = null) : super(message) {
        this.code = code
        this.params = params ?: mapOf()
        this.reason = params?.map { param ->
            try {
                "${param.key}=${param.value?.toString()}"
            } catch (error: Throwable) {
                ""
            }
        }?.joinToString() ?: ""

        printStackTrace()
    }

    private constructor(code: String, message: String, params: Map<String, Any?>? = null, origin: Throwable? = null) : super(message) {
        this.code = code
        this.params = params ?: mapOf()
        this.reason = params?.map { param ->
            try {
                "${param.key}=${param.value?.toString()}"
            } catch (error: Throwable) {
                ""
            }
        }?.joinToString() ?: ""

        if (null == origin) {
            printStackTrace()
        } else {
            origin.printStackTrace()
        }
    }

    override fun toString(): String {
        if (reason.isEmpty()) {
            return "$code: $message"
        }
        return "$code: $message ($reason)"
    }
}
