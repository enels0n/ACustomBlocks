package net.enelson.astract.customblocks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.managers.blocks.CustomBlock;
import net.enelson.astract.customblocks.managers.config.ConfigType;

public class BlockBreakHandler implements Listener {

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if(e.isCancelled())
			return;
		
		CustomBlock block = ACustomBlocks.getInstance().getBlockManager().getBlock(e.getBlock().getLocation());
		if(block == null)
			return;
		
		e.setCancelled(true);

		if(ACustomBlocks.getInstance().getConfigManager().getBoolean(ConfigType.BLOCKS, block.getId()+".break-only-admin")
				&& !e.getPlayer().hasPermission("acustomblocks.admin"))
			return;
			
		ACustomBlocks.getInstance().getBlockManager().breakBlock(block, e.getPlayer());
	}
}
