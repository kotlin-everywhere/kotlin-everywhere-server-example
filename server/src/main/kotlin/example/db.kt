package example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration

private val dataSource = HikariConfig().let {
    it.jdbcUrl = BuildConfig.DATABASE_URL
    it.driverClassName = BuildConfig.DATABASE_DRIVER
    it.username = BuildConfig.DATABASE_USER_NAME
    HikariDataSource(it)
}


private val configuration = DataSourceConnectionProvider(dataSource).let {
    DefaultConfiguration().apply {
        set(it)
        set(SQLDialect.POSTGRES)
    }
}

val ctx: DSLContext
    get() = DSL.using(configuration)

