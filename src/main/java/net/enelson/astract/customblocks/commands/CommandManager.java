package net.enelson.astract.customblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.commands.subcommands.AboutCommand;
import net.enelson.astract.customblocks.commands.subcommands.GiveCommand;
import net.enelson.astract.customblocks.commands.subcommands.ReloadCommand;

public class CommandManager implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}

		if (args[0].equalsIgnoreCase("about")) {
			new AboutCommand(sender);
		} else if (args[0].equalsIgnoreCase("reload")) {
			new ReloadCommand(sender);
		} else if (args[0].equalsIgnoreCase("give")) {
			new GiveCommand(sender, this.removeElement(args, 0));
		} else {
			return false;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		// Команда /acustomblocks about доступна для всех
		if (args.length == 1 && args[0].equalsIgnoreCase("about")) {
			completions.add("about");
			return completions;
		}

		// Команды для администраторов
		if (sender.hasPermission("acustomblocks.admin")) {
			if (args.length == 1) {
				if(!args[0].equals("") && "give".startsWith(args[0]))
					completions.add("give");
				else if(!args[0].equals("") && "reload".startsWith(args[0]))
					completions.add("reload");
				else
					completions.addAll(Arrays.asList("reload", "give"));
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("give")) {
					return null;
				}
			} else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
				// Добавляем список ID блоков
				completions.addAll(ACustomBlocks.getInstance().getConfigManager().getBlocksID());
			} else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
				// Добавляем допустимые значения для количества
				completions.addAll(Arrays.asList("1", "16", "32", "64"));
			}
		}

		// Убираем дубликаты и возвращаем
		return completions.stream().distinct().collect(Collectors.toList());
	}

	private String[] removeElement(String[] arr, int index) {
		String[] copyArray = new String[arr.length - 1];
		System.arraycopy(arr, 0, copyArray, 0, index);
		System.arraycopy(arr, index + 1, copyArray, index, arr.length - index - 1);
		return copyArray;
	}
}
