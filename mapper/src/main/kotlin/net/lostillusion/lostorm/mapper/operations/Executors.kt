package net.lostillusion.lostorm.mapper.operations

import mu.KotlinLogging
import net.lostillusion.lostorm.mapper.Entity
import java.sql.Connection
import java.sql.SQLException

private val LOGGER = KotlinLogging.logger {}

interface OperationExecutor<R> {
    fun execute(generator: Statement, connection: Connection): R
}

object SelectCountExecutor : OperationExecutor<Int> {
    override fun execute(generator: Statement, connection: Connection): Int {
        LOGGER.debug { "Executing query: ${generator.generateStatement()}" }
        val result = connection.prepareStatement(generator.generateStatement()).executeQuery()
        result.next()
        return result.getInt(1)
    }
}

class SelectExectutor<D : Any>(private val entity: Entity<D>) : OperationExecutor<List<D>> {
    override fun execute(generator: Statement, connection: Connection): List<D> {
        LOGGER.debug { "Executing query: ${generator.generateStatement()}" }
        val result = connection.prepareStatement(generator.generateStatement()).executeQuery()
        val values = mutableListOf<MutableList<Any?>>()
        while (result.next()) {
            val currentResult = mutableListOf<Any?>()
            entity.columns.forEach {
                try {
                    currentResult += it.valueConverter.convertToKotlin(result.getObject(it.columnName))
                } catch (e: SQLException) {
                    if (it.nullable) currentResult.add(null)
                    //TODO: Make this a unique exception
                    else throw RuntimeException("No value found for non-nullable column: ${it.columnName}")
                }
            }
            values += currentResult
        }
        return values.map(entity::createDataClass)
    }
}

object UpdateExectuor : OperationExecutor<Int> {
    override fun execute(generator: Statement, connection: Connection): Int {
        LOGGER.debug { "Executing update: ${generator.generateStatement()}" }
        return connection.prepareStatement(generator.generateStatement()).executeUpdate()
    }
}
