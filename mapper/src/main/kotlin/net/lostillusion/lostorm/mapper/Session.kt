package net.lostillusion.lostorm.mapper

import net.lostillusion.lostorm.mapper.operations.Operation
import java.sql.Connection
import java.sql.DriverManager

class Session(private val host: String, private val user: String, private val pass: String, driver: String) {
    init {
        Class.forName(driver)
    }

    fun <T> connection(action: Connection.() -> T) =
        DriverManager.getConnection(host, user, pass).use(action)

    operator fun invoke(function: Session.() -> Any) = function()
}

infix fun <R> Session.transaction(operation: () -> Operation<*, R>) = Transaction(operation(), this).commit()