package milkman.slackbot.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;

    public Database() {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        config.setJdbcUrl(dbUrl);
//        config.setUsername("user");
//        config.setPassword("password");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    @SneakyThrows
    public void executeInitScript() {
        var initSql = IOUtils.toString(getClass().getResource("/init.sql"));
        try (var c = ds.getConnection()) {
            c.prepareCall(initSql).execute();
        }
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
