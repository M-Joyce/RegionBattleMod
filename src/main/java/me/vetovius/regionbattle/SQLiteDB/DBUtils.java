package me.vetovius.regionbattle.SQLiteDB;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.entity.Player;

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
                + " uuid text PRIMARY KEY,\n"
                + " username text NOT NULL,\n"
                + " totalSpendInStore numeric"
                + ");";

        try (Connection conn = DriverManager.getConnection(connectionString);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql); //create table from sql statement
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }
    }


    public static void updatePlayerSpend(Player player, float amount) {
        // SQL statement updating player total spend, or inserting a row if not row for player exists
        String sql = "INSERT INTO playerdata (uuid, username, totalSpendInStore) \n" +
                "VALUES (?,?,?)\n" +
                "ON CONFLICT(uuid) DO UPDATE \n" +
                "SET totalSpendInStore = totalSpendInStore + ? WHERE uuid = ?";

        try (Connection conn = DriverManager.getConnection(connectionString);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName());
            pstmt.setFloat(3, amount);
            pstmt.setFloat(4, amount);
            pstmt.setString(5, player.getUniqueId().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static float getPlayerSpend(Player player) {
        // SQL statement updating player total spend, or inserting a row if not row for player exists
        String sql = "SELECT totalSpendInStore FROM playerdata WHERE uuid = ?";
        float totalSpendInStore = 0.0f;

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            // set the value
            pstmt.setString(1,player.getUniqueId().toString());
            ResultSet rs  = pstmt.executeQuery();

            // loop through the result set
            while (rs.next()) {
                totalSpendInStore = rs.getFloat("totalSpendInStore");
            }
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }

        return totalSpendInStore;
    }

}
