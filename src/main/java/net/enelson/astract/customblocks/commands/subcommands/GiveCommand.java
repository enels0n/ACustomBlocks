package net.enelson.astract.customblocks.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.enelson.astract.customblocks.utils.Utils;

public class GiveCommand {
	public GiveCommand(CommandSender sender, String[] args) {
		if (!sender.hasPermission("acustomblocks.admin")) {
			sender.sendMessage("You do not have permission to use this command.");
			return;
		}

		if (args.length != 2 && args.length != 3) {
			sender.sendMessage("Usage: /acustomblocks give <player> <blockId> [amount]");
			return;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player == null) {
			sender.sendMessage("Player not found.");
			return;
		}

		int amount = 0;
		if (args.length == 3) {
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex) {
				sender.sendMessage("Amount must be a number.");
				return;
			}
		}

		amount = amount > 0 ? amount : 1;

		ItemStack item = Utils.generateItem(args[1], amount);
		if (item == null) {
			sender.sendMessage("Block ID not found or configured incorrectly.");
			return;
		}

		if (player.getInventory().addItem(item).size() != 0) {
			player.getWorld().dropItem(player.getLocation(), item);
		}

		sender.sendMessage("Given " + amount + " item(s) of " + args[1] + " to " + player.getName() + ".");
	}
}
