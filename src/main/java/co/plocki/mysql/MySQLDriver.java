package co.plocki.mysql;

import co.plocki.json.JSONFile;
import co.plocki.json.JSONValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONObject;

public class MySQLDriver {

    private final HikariDataSource asyncSource;

    public MySQLDriver() {

        JSONObject cred = new JSONObject();
        cred.put("host", "localhost:3306");
        cred.put("database", "database");
        cred.put("user", "root");
        cred.put("password", "password");

        JSONObject options = new JSONObject();
        options.put("cachePrepStmts", "true");
        options.put("prepStmtCacheSize", "250");
        options.put("prepStmtCacheSqlLimit", "2048");

        String src = "config/mysql.json";

        JSONFile file = new JSONFile(src,
                new JSONValue() {
                    @Override
                    public JSONObject object() {
                        return cred;
                    }

                    @Override
                    public String objectName() {
                        return "credentials";
                    }
                },
                new JSONValue() {
                    @Override
                    public JSONObject object() {
                        return options;
                    }

                    @Override
                    public String objectName() {
                        return "options";
                    }
                });
        if(file.isNew()) {
            System.out.println("Please update the mysql.json file with your credentials.");
            asyncSource = null;
            return;
        }

        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(300000);
        config.setMaximumPoolSize(500);
        config.setJdbcUrl("jdbc:mysql://" + file.get("credentials").getString("host") + "/" + file.get("credentials").getString("database"));
        config.setUsername(file.get("credentials").getString("user"));
        config.setPassword(file.get("credentials").getString("password"));
        config.addDataSourceProperty("cachePrepStmts", file.get("options").getString("cachePrepStmts"));
        config.addDataSourceProperty("prepStmtCacheSize", file.get("options").getString("prepStmtCacheSize"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", file.get("options").getString("prepStmtCacheSqlLimit"));

        asyncSource = new HikariDataSource(config);
    }


    public HikariDataSource getHikariDataSource() {
        return asyncSource;
    }

    public void close() {
        asyncSource.close();
    }

}