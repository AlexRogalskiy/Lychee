package net.aquadc.properties.fx

import java.util.concurrent.Executor


object JavaFxApplicationThreadExecutorFactory : () -> Executor? {

    init {
        error("This class is only for compile-time.")
    }

    override fun invoke(): Executor? = error("")

}