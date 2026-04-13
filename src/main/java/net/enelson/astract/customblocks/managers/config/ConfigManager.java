package net.enelson.astract.customblocks.managers.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import net.enelson.astract.customblocks.ACustomBlocks;

public class ConfigManager {
	private ACustomBlocks plugin;
	private YamlConfiguration config;
	private YamlConfiguration blocks;
	private YamlConfiguration db;
	private YamlConfiguration locale;
	
	public ConfigManager(ACustomBlocks plugin) {
		this.plugin = plugin;
		this.reloadConfig();
	}
	
	public void reloadConfig() {
		File file = new File(plugin.getDataFolder(), "config.yml");
		if (!file.exists()) plugin.saveResource("config.yml", true);
		this.config = YamlConfiguration.loadConfiguration(file);
		
		file = new File(plugin.getDataFolder(), "lang.yml");
		if (!file.exists()) plugin.saveResource("lang.yml", true);
		this.locale = YamlConfiguration.loadConfiguration(file);
		
		file = new File(plugin.getDataFolder(), "blocks.yml");
		if (!file.exists()) plugin.saveResource("blocks.yml", true);
		this.blocks = YamlConfiguration.loadConfiguration(file);
		
		file = new File(plugin.getDataFolder(), "db.yml");
		if (!file.exists()) plugin.saveResource("db.yml", true);
		this.db = YamlConfiguration.loadConfiguration(file);
	}
	
	public boolean getBoolean(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return this.config.getBoolean(path);
			case BLOCKS:
				return this.blocks.getBoolean(path);
			default:
				return false;
		}
	}
	
	public int getInt(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return this.config.getInt(path);
			case BLOCKS:
				return this.blocks.getInt(path);
			default:
				return 0;
		}
	}
	
	public String getString(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return this.config.getString(path);
			case BLOCKS:
				return this.blocks.getString(path);
			case LOCALE:
				return this.locale.getString(path);
		}
		return null;
	}
	
	public float getFloat(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return (float) this.config.getDouble(path);
			case BLOCKS:
				return (float) this.blocks.getDouble(path);
			case LOCALE:
				return (float) this.locale.getDouble(path);
		}
		return 0f;
	}
	
	public double getDouble(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return this.config.getDouble(path);
			case BLOCKS:
				return this.blocks.getDouble(path);
			case LOCALE:
				return this.locale.getDouble(path);
		}
		return 0;
	}
	
	public boolean isNull(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return this.config.get(path) == null;
			case BLOCKS:
				return this.blocks.get(path) == null;
			case LOCALE:
				return this.locale.get(path) == null;
		}
		return true;
	}
	
	public List<String> getStringList(@NotNull ConfigType type, @NotNull String path) {
		switch(type) {
			case CONFIG:
				return this.config.getStringList(path);
			case BLOCKS:
				return this.blocks.getStringList(path);
			case LOCALE:
				return this.locale.getStringList(path);
		}
		return null;
	}
	
	public Set<String> getBlocksID() {
		if (this.blocks.getConfigurationSection("") == null) {
			return Collections.emptySet();
		}
		return this.blocks.getConfigurationSection("").getKeys(false);
	}
	
	public YamlConfiguration getDB() {
		return this.db;
	}
	
	public void saveDB() {
		File file = new File(plugin.getDataFolder(), "db.yml");
		if (!file.exists()) plugin.saveResource("db.yml", true);
		try {
			this.db.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public YamlConfiguration getConfig(@NotNull ConfigType type) {
	    switch (type) {
	        case CONFIG:
	            return this.config;
	        case BLOCKS:
	            return this.blocks;
	        case LOCALE:
	            return this.locale;
	        default:
	            return this.db;
	    }
	}
}
