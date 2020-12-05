package nl.dustswiffer.mgtokens.listeners;

import nl.dustswiffer.mgtokens.MGTokens;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    private final MGTokens plugin;

    public PlayerJoinListener(MGTokens plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ResultSet rs = this.plugin.executeSqlQuery("SELECT * FROM mgtokens_accounts WHERE UUID = '" + player.getUniqueId().toString() + "' LIMIT 1 ;");
        try {
            if (!rs.next()) {
                createTokensAccount(player.getUniqueId().toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTokensAccount(String uuid) {
        if (this.plugin.executeSqlInsert("INSERT INTO mgtokens_accounts (uuid, amount) VALUES('" + uuid + "', 0);") == 0) {
            this.plugin.getLogger().info("Could not create mgtokens account for " + uuid);
        }
    }
}
