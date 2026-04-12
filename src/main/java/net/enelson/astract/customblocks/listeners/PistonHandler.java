package net.enelson.astract.customblocks.listeners;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import net.enelson.astract.customblocks.ACustomBlocks;

public class PistonHandler implements Listener {

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if(this.checkCustomBlocks(e.getBlocks()))
        	e.setCancelled(true);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(this.checkCustomBlocks(e.getBlocks()))
        	e.setCancelled(true);
    }
    
    private boolean checkCustomBlocks(List<Block> blocks) {
        for(Block block : blocks) {
        	if(ACustomBlocks.getInstance().getBlockManager().getBlock(block.getLocation()) != null) {
        		return true;
        	}
        }
        return false;
    }
}
