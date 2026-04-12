package net.enelson.astract.customblocks.commands.subcommands;

import org.bukkit.command.CommandSender;

import net.enelson.astract.customblocks.ACustomBlocks;

public class ReloadCommand {
	public ReloadCommand(CommandSender sender) {
		if (sender.hasPermission("acustomblocks.admin")) {
			ACustomBlocks.getInstance().reloadPlugin();
			sender.sendMessage("The plugin has been reloaded.");
			return;
		}

		sender.sendMessage("You do not have permission to use this command.");
	}
}
