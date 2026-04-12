package net.enelson.astract.customblocks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import net.enelson.astract.customblocks.ACustomBlocks;

public class ExplodeHandler implements Listener {

//    private final Map<TNTPrimed, Player> tntMap = new HashMap<>();
//    private final Map<Creeper, Player> creeperMap = new HashMap<>();
    
//    @EventHandler
//    public void onPlayerInteract(PlayerInteractEvent e) {
//        // Сохраняем игрока, если он ставит TNT
//        if (e.getMaterial() == Material.TNT) {
//            TNTPrimed tnt = e.getPlayer().getWorld().spawn(e.getPlayer().getLocation(), TNTPrimed.class);
//            tntMap.put(tnt, e.getPlayer());
//        }
//    }
//
//    @EventHandler
//    public void onExplosionPrime(ExplosionPrimeEvent event) {
//        // Сохраняем игрока, если взрывается крипер
//        if (event.getEntity() instanceof Creeper) {
//            Creeper creeper = (Creeper) event.getEntity();
//            // Здесь вам нужно определить, как вы хотите получить игрока, который активировал крипера
//            // Например, если это был игрок, который его заспавнил или активировал
//            // В этом примере пропустим это, но в реальной ситуации вам нужно будет это реализовать
//        }
//    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
    	e.blockList().removeIf(b -> ACustomBlocks.getInstance().getBlockManager().getBlock(b.getLocation()) != null);
//    	List<Block> blocks = new ArrayList<>();
//
//    	for(Block block : e.blockList()) {
//    		CustomBlock customBlock = ACustomBlocks.getInstance().getBlockManager().getBlock(block.getLocation());
//    		if(customBlock != null) {
//    			blocks.add(block);
//    			if(!ACustomBlocks.getInstance().getConfigManager().getBoolean(ConfigType.BLOCKS, customBlock.getId()+".can-exploded"))
//    				return;
//    			
//    			if(ACustomBlocks.getInstance().getConfigManager().getBoolean(ConfigType.BLOCKS, customBlock.getId()+".break-only-admin")
//    					&& !e.getPlayer().hasPermission("acustomblocks.admin"))
//    				
//    			ACustomBlocks.getInstance().getBlockManager().breakBlock(customBlock, null);
//    		}
//    	}
//        e.blockList().removeIf(block -> protectedBlocks.contains(block.getLocation()));
    }
}
