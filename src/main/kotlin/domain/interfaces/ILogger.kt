package domain.interfaces

interface ILogger {
    fun log(message: String)

    fun error(message: String, throwable: Throwable? = null){
        log("ERROR: $message")
        throwable?.let{
            log(it.stackTraceToString())
        }
    }
}