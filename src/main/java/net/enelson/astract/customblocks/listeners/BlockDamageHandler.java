package net.enelson.astract.customblocks.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.managers.blocks.CustomBlock;
import net.enelson.astract.customblocks.managers.config.ConfigType;
import net.enelson.astract.customblocks.utils.Utils;

public class BlockDamageHandler implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHit(BlockDamageEvent e) {
		Block block = e.getBlock();
		CustomBlock customBlock = ACustomBlocks.getInstance().getBlockManager().getBlock(block.getLocation());
		
		if(customBlock == null)
			return;
		
		if(!ACustomBlocks.getInstance().getConfigManager().getBoolean(ConfigType.BLOCKS, customBlock.getId()+".break-by-hit"))
			return;

		if(ACustomBlocks.getInstance().getConfigManager().getBoolean(ConfigType.BLOCKS, customBlock.getId()+".break-only-admin")
				&& !e.getPlayer().hasPermission("acustomblocks.admin"))
			return;

		if(e.isCancelled() || !Utils.canBuild(e.getPlayer(), block.getLocation()))
			return;

		ACustomBlocks.getInstance().getBlockManager().breakBlock(customBlock, e.getPlayer());
	}
}
