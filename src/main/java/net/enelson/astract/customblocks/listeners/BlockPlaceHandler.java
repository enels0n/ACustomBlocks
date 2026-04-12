package net.enelson.astract.customblocks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.managers.config.ConfigType;
import net.enelson.astract.customblocks.utils.Utils;

public class BlockPlaceHandler implements Listener {

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if(e.isCancelled())
			return;
		
		String id = Utils.getId(e.getItemInHand());
		if(id == null)
			return;
		
		e.setCancelled(true);

		Block block = e.getBlock();

		if (ACustomBlocks.getInstance().getBlockManager().getBlock(block.getLocation()) != null) {
			return;
		}
		
		if(id.startsWith("debug")) {
			if(!e.getPlayer().hasPermission("acustomblocks.admin")) {
				return;
			}
			
			int radius = Integer.parseInt(id.split("-")[1]);
			e.getPlayer().sendMessage("Удалено " + ACustomBlocks.getInstance().getBlockManager().debug(block.getLocation(), radius) + " сущностей в радиусе " + radius);
			return;
		}
		
		String newBlock = ACustomBlocks.getInstance().getConfigManager().getString(ConfigType.BLOCKS, id+".replacement-block");
		Material material;
		if(newBlock != null)
			material = Material.valueOf(newBlock.toUpperCase());
		else
			material = e.getItemInHand().getType();

		ACustomBlocks.getInstance().getBlockManager().addBlock(id, block.getLocation(), e.getPlayer());
		
		Bukkit.getScheduler().runTaskLater(ACustomBlocks.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(newBlock != null)
					block.setType(material);
				else
					block.setType(e.getItemInHand().getType());
			}
		}, 1);
		
		if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			e.getItemInHand().setAmount(e.getItemInHand().getAmount()-1);
	}
}
