package net.enelson.astract.customblocks.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.managers.config.ConfigType;
import net.enelson.sopli.lib.SopLib;

public class Utils {
	public static ItemStack generateItem(String id, int amount) {
		String material = ACustomBlocks.getInstance().getConfigManager().getString(ConfigType.BLOCKS, id+".material");
		
		if(material == null)
			return null;

		int model = ACustomBlocks.getInstance().getConfigManager().getInt(ConfigType.BLOCKS, id+".model");
		String name = ACustomBlocks.getInstance().getConfigManager().getString(ConfigType.BLOCKS, id+".name");
		List<String> enchantments = ACustomBlocks.getInstance().getConfigManager().getStringList(ConfigType.BLOCKS, id+".enchantments");
		List<String> lore = ACustomBlocks.getInstance().getConfigManager().getStringList(ConfigType.BLOCKS, id+".lore");
		List<String> nbts = ACustomBlocks.getInstance().getConfigManager().getStringList(ConfigType.BLOCKS, id+".nbts");
		nbts.add("ACustomBlocks::"+id);
		String customItemKey = ACustomBlocks.getInstance().getConfigManager().getString(ConfigType.BLOCKS, id+".custom-item-key");
		String customItemKeyFallback = ACustomBlocks.getInstance().getConfigManager().getString(ConfigType.BLOCKS, id+".custom-item-key-fallback");

		ItemStack item = SopLib.getInstance().getItemUtils().createItem(material, amount, model, name, enchantments, lore, nbts);
		
		if(customItemKey != null)
			SopLib.getInstance().getItemUtils().setCustomItemKey(item, customItemKey, customItemKeyFallback);
		
		return item;
	}
	
	public static ItemStack generateItem(String id) {
		return generateItem(id, 1);
	}
	
	public static ItemStack generateModeledItemForItemDisplay(String material, int model) {
		return SopLib.getInstance().getItemUtils().createItem(material, model, null, null, null, null);
	}
	
	public static String getId(ItemStack item) {
		return SopLib.getInstance().getItemUtils().getNBT(item, "ACustomBlocks", String.class);
	}
	
	public static Location getDeserializedLocation(String s) {
		final String[] split = s.split(",");
		return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
				Double.parseDouble(split[3]));
	}

	public static String getSerializedLocation(Location loc) {
		return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
	}
	
	public static boolean canBuild(Player player, Location location) {
		if(!ACustomBlocks.getInstance().isWorldGuardEnabled())
			return true;

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(location.getWorld());
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        
        if(WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, world))
        	return true;
        
        return query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
	}
}
