package me.vetovius.regionbattle.SQLiteDB;

import me.vetovius.regionbattle.RegionBattle;

import java.sql.*;
import java.util.logging.Logger;

public class DBUtils {
    private static final Logger LOGGER = Logger.getLogger( DBUtils.class.getName() );

    public static final String dbName = "regionbattle.db";

    public static String connectionString;

    public static void createNewDatabase(RegionBattle pluginInstance) { //called from onEnable

        connectionString = "jdbc:sqlite:" + pluginInstance.getDataFolder().getAbsolutePath() + "/" + dbName;

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
               LOGGER.info("The driver name is " + meta.getDriverName());
                LOGGER.info("A new database has been created.");
            }

        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }

        createTables(); //create tables
    }

    public static void createTables() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS playerdata (\n"
                + " uuid blob PRIMARY KEY,\n"
                + " username text NOT NULL,\n"
                + " totalSpendInStore numeric"
                + ");";

        try (Connection conn = DriverManager.getConnection(connectionString);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }



}
