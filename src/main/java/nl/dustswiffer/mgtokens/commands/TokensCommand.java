package nl.dustswiffer.mgtokens.commands;

import nl.dustswiffer.mgtokens.MGTokens;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TokensCommand implements CommandExecutor {

    private final MGTokens plugin;

    public TokensCommand(MGTokens plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String labels, String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "give":
                    if (!((sender instanceof ConsoleCommandSender) || sender.hasPermission("mgtokens.tokens.give") || sender.isOp())) {
                        sender.sendMessage(ChatColor.RED.toString() + "You can not give Mini-Game Tokens");
                        return false;
                    }
                    ChangeTokensOfPlayer(sender, args[1], args[2], "+");
                    break;
                case "take":
                    if (!((sender instanceof ConsoleCommandSender) || sender.hasPermission("mgtokens.tokens.take") || sender.isOp())) {
                        sender.sendMessage(ChatColor.RED.toString() + "You can not Take Mini-Game Tokens");
                        return false;
                    }
                    ChangeTokensOfPlayer(sender, args[1], args[2], "-");
                    break;
                case "set":
                    if (!((sender instanceof ConsoleCommandSender) || sender.hasPermission("mgtokens.tokens.set") || sender.isOp())) {
                        sender.sendMessage(ChatColor.RED.toString() + "You can not set Mini-Game Tokens");
                        return false;
                    }
                    setTokensOfPlayer(sender, args[1], args[2]);
                    break;
                default:
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED.toString() + "You need to be an player to perform this command");
                        return false;
                    }
                    getTokensOfOtherPlayer(sender, args[0]);
            }

        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED.toString() + "You need to be an player to perform this command");
                return false;
            }

            return getOwnTokens(sender);
        }
        return true;
    }

    private boolean getOwnTokens(CommandSender sender) {
        Player player = (Player) sender;

        ResultSet rs = this.plugin.executeSqlQuery("SELECT amount FROM mgtokens_accounts WHERE UUID = '" + player.getUniqueId().toString() + "' LIMIT 1 ;");

        String bal = null;
        try {
            if (rs.next()) {
                bal = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (bal == null) {
            bal = "0";
        }
        sender.sendMessage("");
        player.sendMessage(ChatColor.GREEN.toString() + "You have " + ChatColor.YELLOW.toString() + bal + ChatColor.GREEN.toString() + " Mini-Game Tokens");

        return true;
    }

    private void getTokensOfOtherPlayer(CommandSender sender, String name) {
        if (!((sender instanceof ConsoleCommandSender) || sender.hasPermission("mgtokens.tokens.others") || sender.isOp())) {
            sender.sendMessage(ChatColor.RED.toString() + "You can not inspect the Mini-Game Tokens balance of other players");
        } else {
            Player targetPlayer = this.plugin.getServer().getPlayer(name);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED.toString() + name + " is not known in our database");
                return;
            }
            ResultSet rs = this.plugin.executeSqlQuery("SELECT amount FROM mgtokens_accounts WHERE UUID = '" + targetPlayer.getUniqueId().toString() + "' LIMIT 1 ;");

            String bal = null;
            try {
                if (rs.next()) {
                    bal = rs.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW.toString() + targetPlayer.getDisplayName() + ChatColor.GREEN.toString() + " has " + ChatColor.YELLOW.toString() + bal + ChatColor.GREEN.toString() + " Mini-Game Tokens");
        }
    }

    private void ChangeTokensOfPlayer(CommandSender sender, String name, String amount, String operator) {
        Player target = this.plugin.getServer().getPlayer(name);
        if (!(amount.matches(".*\\d.*"))) {
            sender.sendMessage(ChatColor.RED.toString() + "The amount needs to be a number");
            return;
        }
        if (!(target == null)) {
            String uuid = target.getUniqueId().toString();

            ResultSet rs = this.plugin.executeSqlQuery("SELECT amount FROM mgtokens_accounts WHERE UUID = '" + uuid + "' LIMIT 1 ;");

            String bal = null;
            try {
                if (rs.next()) {
                    bal = rs.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (bal == null) {
                bal = "0";
            }
            float currentBalance = 0;
            float amountConverted = 0;
            float newBalance = 0;
            try {
                currentBalance = Float.parseFloat(bal);
                amountConverted = Float.parseFloat(amount);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            switch (operator) {
                case "+":
                    newBalance = currentBalance + amountConverted;
                    break;
                case "-":
                    if (!(currentBalance >= amountConverted)) {
                        if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.RED.toString() + target.getDisplayName() + " Does not have enough tokens");
                        }
                        return;
                    }
                    newBalance = currentBalance - amountConverted;
                    break;
            }

            int updateQuery = this.plugin.executeSqlUpdate("UPDATE mgtokens_accounts SET amount = " + newBalance + " WHERE UUID = '" + uuid + "';");
            if (updateQuery == 0) {
                sender.sendMessage("ERROR updating tokens bal of player " + target.getDisplayName());
                return;
            }

            if (sender instanceof Player) {
                if (operator.equals("+")) {
                    sender.sendMessage(ChatColor.GREEN.toString() + "Gave " + ChatColor.YELLOW.toString() + name + " " + amount + ChatColor.GREEN.toString() + " Mini-Game Tokens");
                }
                if (operator.equals("-")) {
                    sender.sendMessage(ChatColor.GREEN.toString() + "Took " + ChatColor.YELLOW.toString() + amount + ChatColor.GREEN.toString() + " Mini-Game Tokens from " + ChatColor.YELLOW.toString() + name);
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "the given player doesnt not exist!");
        }
    }

    private void setTokensOfPlayer(CommandSender sender, String name, String amount) {
        Player target = this.plugin.getServer().getPlayer(name);

        if (!(amount.matches(".*\\d.*"))) {
            sender.sendMessage(ChatColor.RED.toString() + "The amount needs to be a number");
            return;
        }

        if (!(target == null)) {
            String uuid = target.getUniqueId().toString();

            ResultSet rs = this.plugin.executeSqlQuery("SELECT * FROM mgtokens_accounts WHERE UUID = '" + uuid + "' LIMIT 1 ;");
            try {
                if (!rs.next()) {
                    createTokensAccount(uuid, amount);
                } else {
                    int updateQuery = this.plugin.executeSqlUpdate("UPDATE mgtokens_accounts SET amount = " + amount + " WHERE UUID = '" + uuid + "';");
                    if (updateQuery == 0) {
                        sender.sendMessage(ChatColor.RED.toString() + "ERROR updating tokens bal of player " + target.getDisplayName());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.GREEN.toString() + "Set Mini-Game tokens  of " + ChatColor.YELLOW.toString() + name + ChatColor.GREEN.toString() + " To " + ChatColor.YELLOW.toString() + amount);
            }
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "the given player doesnt not exist!");
        }
    }

    private void createTokensAccount(String uuid, String amount) {
        if (this.plugin.executeSqlInsert("INSERT INTO mgtokens_accounts (uuid, amount) VALUES('" + uuid + "', " + amount + ");") == 0) {
            this.plugin.getLogger().info("Could not create mgtokens account for " + uuid);
        }
    }
}