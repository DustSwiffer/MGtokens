package nl.dustswiffer.mgtokens;

import nl.dustswiffer.mgtokens.commands.TokensCommand;
import nl.dustswiffer.mgtokens.listeners.PlayerJoinListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public final class MGTokens extends JavaPlugin {

    //region Global variables
    FileConfiguration config = getConfig();

    String host, port, database, username, password;
    public static Connection connection;
    //endregion

    //region default methods
    @Override
    public void onEnable() {
        setupConfigs();
        connectDatabase();
        getLogger().info("Plugin is loaded");
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getCommand("tokens").setExecutor(new TokensCommand(this));
    }

    @Override
    public void onDisable() {
    }
    //endregion

    private void setupConfigs() {
        // Setting up the default config.yml
        config.options().copyDefaults();
        saveDefaultConfig();
    }

    //region Database
    private void connectDatabase() {
        host = getConfig().getString("host");
        port = getConfig().getString("port");
        database = getConfig().getString("database");
        username = getConfig().getString("username");
        password = getConfig().getString("password");
        try {
            openDatabaseConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void openDatabaseConnection() throws SQLException, ClassNotFoundException {

        if (connection != null && !connection.isClosed()) {
            return;
        }

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" +
                        this.host + ":" +
                        this.port + "/" +
                        this.database,
                this.username,
                this.password);
    }

    public ResultSet executeSqlQuery(String query) {
        ResultSet resultSet = null;
        try {
            openDatabaseConnection();
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    public int executeSqlUpdate(String query) {
        int result = 0;
        try {
            openDatabaseConnection();
            Statement statement = connection.createStatement();
            result = statement.executeUpdate(query);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int executeSqlInsert(String query) {
        int result = 0;
        try {
            openDatabaseConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeUpdate();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    //endregion
}
