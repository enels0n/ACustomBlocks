package net.enelson.astract.customblocks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

import net.enelson.astract.customblocks.commands.CommandManager;
import net.enelson.astract.customblocks.managers.blocks.BlockManager;
import net.enelson.astract.customblocks.managers.config.ConfigManager;

public class ACustomBlocks extends JavaPlugin {

	private static ACustomBlocks plugin;
	private BlockManager blockManager;
	private ConfigManager configManager;
	private boolean wg;
	
	public void onEnable() {
		plugin = this;
		
		this.configManager = new ConfigManager(this);
		this.reloadConfig();
		
		this.blockManager = new BlockManager(this);
		
		PluginManager pluginManager = Bukkit.getPluginManager();
		try {
			String pac = "net.enelson.astract.customblocks.listeners";
			for (ClassPath.ClassInfo clazzInfo : ClassPath.from(getClassLoader()).getTopLevelClasses(pac)) {
				Class<?> clazz = Class.forName(clazzInfo.getName());
				if (Listener.class.isAssignableFrom(clazz)) {
					pluginManager.registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    this.wg = getServer().getPluginManager().getPlugin("WorldGuard") != null;
	    
		this.getCommand("acustomblocks").setExecutor(new CommandManager());
		this.getCommand("acustomblocks").setTabCompleter(new CommandManager());
	}

	public static ACustomBlocks getInstance() {
		return plugin;
	}
	
	public BlockManager getBlockManager() {
		return this.blockManager;
	}
	
	public ConfigManager getConfigManager() {
		return this.configManager;
	}
	
	public boolean isWorldGuardEnabled() {
		return this.wg;
	}
	
	public void reloadConfig() {
		this.configManager.reloadConfig();
	}

	public void reloadPlugin() {
		if (this.blockManager != null) {
			this.blockManager.deInit();
		}

		this.configManager.reloadConfig();
		this.blockManager = new BlockManager(this);
		this.wg = getServer().getPluginManager().getPlugin("WorldGuard") != null;
	}
	
	public void onDisable() {
		if (this.blockManager != null) {
			this.blockManager.deInit();
		}
	}
}
