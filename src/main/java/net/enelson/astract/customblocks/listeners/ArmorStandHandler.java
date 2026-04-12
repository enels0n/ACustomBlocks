package net.enelson.astract.customblocks.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import net.enelson.astract.customblocks.ACustomBlocks;

public class ArmorStandHandler implements Listener {

	@EventHandler
	public void onClick(PlayerInteractAtEntityEvent e) {
		if (!(e.getRightClicked() instanceof ArmorStand))
			return;

		if(ACustomBlocks.getInstance().getBlockManager().isCustomBlockArmorStand(e.getRightClicked()))
			e.setCancelled(true);
	}
	
//	@EventHandler
//	public void onClick(VehicleMoveEvent e) {
//		if (!(e.getRightClicked() instanceof ArmorStand))
//			return;
//
//		if(ACustomBlocks.getInstance().getBlockManager().isCustomBlockArmorStand(e.getRightClicked()))
//			e.setCancelled(true);
//	}
}
