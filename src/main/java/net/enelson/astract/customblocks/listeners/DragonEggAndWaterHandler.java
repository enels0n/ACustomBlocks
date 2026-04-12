package net.enelson.astract.customblocks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import net.enelson.astract.customblocks.ACustomBlocks;

public class DragonEggAndWaterHandler implements Listener {
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent e) {
		if (ACustomBlocks.getInstance().getBlockManager().getBlock(e.getBlock().getLocation()) != null
				|| ACustomBlocks.getInstance().getBlockManager().getBlock(e.getToBlock().getLocation()) != null)
			e.setCancelled(true);
	}
}
