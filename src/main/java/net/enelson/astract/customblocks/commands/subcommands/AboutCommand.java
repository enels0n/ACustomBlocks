package net.enelson.astract.customblocks.commands.subcommands;

import org.bukkit.command.CommandSender;

public class AboutCommand {
	public AboutCommand(CommandSender sender) {
		sender.sendMessage("ACustomBlocks is running.");
	}
}
