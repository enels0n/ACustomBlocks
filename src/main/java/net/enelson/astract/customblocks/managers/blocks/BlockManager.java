package net.enelson.astract.customblocks.managers.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.managers.config.ConfigType;
import net.enelson.astract.customblocks.utils.Utils;

public class BlockManager {
	private final List<CustomBlock> blocks;
	private final List<Entity> entities;
	private final ACustomBlocks plugin;
	private final BukkitTask tasker;

	public BlockManager(ACustomBlocks plugin) {
		this.plugin = plugin;
		this.blocks = new ArrayList<>();
		this.entities = new ArrayList<>();
		
		this.tasker = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				save();
			}
		}, 20 * 60, 20 * 60);

		if (this.plugin.getConfigManager().getDB().getConfigurationSection("") == null) {
			return;
		}

		for (String i : this.plugin.getConfigManager().getDB().getConfigurationSection("").getKeys(false)) {
			String id = this.plugin.getConfigManager().getDB().getString(i + ".id");
			String uuid = this.plugin.getConfigManager().getDB().getString(i + ".entityUUID");
			Location location = Utils.getDeserializedLocation(i);
			boolean oldSystem = this.plugin.getConfigManager().getDB().getBoolean(i + ".old-system");

			if (id == null || location.getWorld() == null) {
				this.plugin.getConfigManager().getDB().set(i, null);
				continue;
			}

			if (uuid != null) {
				Entity entity = Bukkit.getEntity(UUID.fromString(uuid));
				if (entity != null) {
					entity.remove();
				}
			}

			if (location.getBlock().getType().equals(Material.AIR) && !this.hasAirReplacement(id)) {
				this.plugin.getConfigManager().getDB().set(i, null);
				continue;
			}

			float rotation = (float) this.plugin.getConfigManager().getDB().getDouble(i + ".rotation");
			Entity displayEntity = oldSystem ? this.createOldEntity(location, id, rotation)
					: this.createEntity(location.getBlock(), id, rotation);
			CustomBlock block = new CustomBlock(id, location, displayEntity, displayEntity.getLocation().getYaw(),
					displayEntity.getUniqueId().toString());
			this.plugin.getConfigManager().getDB().set(i + ".entityUUID",
					displayEntity.getUniqueId().toString().toLowerCase());
			this.blocks.add(block);
		}
	}

	public CustomBlock getBlock(Location location) {
		return this.blocks.stream().filter(b -> b.getLocation().equals(location)).findFirst().orElse(null);
	}

	public CustomBlock getBlock(Entity entity) {
		return this.blocks.stream().filter(b -> b.getEntity().equals(entity)).findFirst().orElse(null);
	}

	public void addBlock(String id, Location location, Player player) {
		if (this.getBlock(location) != null) {
			return;
		}

		String path = Utils.getSerializedLocation(location);

		Entity entity;
		boolean oldSystem = this.plugin.getConfigManager().getBoolean(ConfigType.BLOCKS, id + ".old-system");
		if (oldSystem) {
			this.plugin.getConfigManager().getDB().set(path + ".old-system", true);
			entity = this.createOldEntity(location, id, this.getDirection(player, id));
		} else {
			this.plugin.getConfigManager().getDB().set(path + ".old-system", null);
			entity = this.createEntity(location.getBlock(), id, player);
		}
		
		this.blocks.add(
				new CustomBlock(id, location, entity, entity.getLocation().getYaw(), entity.getUniqueId().toString()));

		this.plugin.getConfigManager().getDB().set(path + ".id", id);
		this.plugin.getConfigManager().getDB().set(path + ".rotation", entity.getLocation().getYaw());
		this.plugin.getConfigManager().getDB().set(path + ".entityUUID",
				entity.getUniqueId().toString().toLowerCase());
	}

	public ItemStack removeBlock(CustomBlock block) {
		this.removeEntity(block.getEntity(), block.getEntityUUID());
		this.blocks.remove(block);
		this.plugin.getConfigManager().getDB().set(Utils.getSerializedLocation(block.getLocation()),
				null);
		return Utils.generateItem(block.getId(), 1);
	}

	public void breakBlock(CustomBlock block, @Nullable Player player) {
		block.getLocation().getBlock().setType(Material.AIR);

		if (player != null) {
			ItemStack item = Utils.generateItem(block.getId());
			if (player.getInventory().addItem(item).size() != 0) {
				player.getWorld().dropItem(block.getLocation(), item);
			}
		}

		this.removeBlock(block);
	}

	private ItemDisplay createEntity(Block block, String id, float rotation) {
		double posX = this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".pos-x") ? 0.5
				: this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".pos-x");
		double posY = this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".pos-y") ? 0.5
				: this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".pos-y");
		double posZ = this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".pos-z") ? 0.5
				: this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".pos-z");

		Location entityLocation = block.getLocation().add(posX, posY, posZ);
		entityLocation.setPitch(0.0f);

		entityLocation.setYaw(rotation);

		String material = this.plugin.getConfigManager().getString(ConfigType.BLOCKS, id + ".material");
		int model = this.plugin.getConfigManager().getInt(ConfigType.BLOCKS, id + ".model");
		
		ItemStack item = Utils.generateModeledItemForItemDisplay(material, model);

		ItemDisplay itemEntity = block.getWorld().spawn(entityLocation, ItemDisplay.class);
		itemEntity.setItemStack(item);

		itemEntity.setInvulnerable(true);
		itemEntity.setSilent(true);
		
		int blockLight = 6;
		int skyLight = 11;
		if (!this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".blockLight")) {
			blockLight = this.plugin.getConfigManager().getInt(ConfigType.BLOCKS, id + ".blockLight");
		}
		if (!this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".skyLight")) {
			skyLight = this.plugin.getConfigManager().getInt(ConfigType.BLOCKS, id + ".skyLight");
		}
		
		itemEntity.setBrightness(new Display.Brightness(blockLight, skyLight));

		float scaleX = (float) this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".scale-x");
		float scaleY = (float) this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".scale-y");
		float scaleZ = (float) this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".scale-z");

		scaleX = scaleX > 0 ? scaleX : 1.002f;
		scaleY = scaleY > 0 ? scaleY : 1.002f;
		scaleZ = scaleZ > 0 ? scaleZ : 1.002f;

		itemEntity.setTransformation(new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), // Translation
				new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), // Left Rotation
				new Vector3f(scaleX, scaleY, scaleZ), // Scale
				new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f) // Right Rotation
		));

		return itemEntity;
	}

	private ItemStack createItem(String id, int damage) {
		ItemStack item = new ItemStack(Material.valueOf(
				this.plugin.getConfigManager().getString(ConfigType.BLOCKS, id + ".material").toUpperCase()));
		item.setAmount(1);

		ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable damageable) {
			damageable.setDamage(damage);
		}
		int model = this.plugin.getConfigManager().getInt(ConfigType.BLOCKS, id + ".model");
		if (model > 0) {
			meta.setCustomModelData(model);
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemDisplay createEntityWithoutBlock(Location location, String id, float rotation) {
		ItemDisplay entity = this.createEntity(location.getBlock(), id, rotation);
		this.entities.add(entity);
		return entity;
	}

	private ArmorStand createOldEntity(Location location, String id, float rotation) {
		double posX = this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".pos-x") ? 0.5
				: this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".pos-x");
		double posY = this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".pos-y") ? 0.5
				: this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".pos-y");
		double posZ = this.plugin.getConfigManager().isNull(ConfigType.BLOCKS, id + ".pos-z") ? 0.5
				: this.plugin.getConfigManager().getDouble(ConfigType.BLOCKS, id + ".pos-z");

		Location entityLocation = location.clone().add(posX, posY, posZ);

		entityLocation.setYaw(rotation);
		ArmorStand stand = entityLocation.getWorld().spawn(entityLocation, ArmorStand.class);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setVisible(false);
		
		ItemStack item = this.createItem(id, this.plugin.getConfigManager().getInt(ConfigType.BLOCKS, id + ".damage"));
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		stand.getEquipment().setHelmet(item);
		
		return stand;
	}
	
	public ArmorStand createOldEntityWithoutBlock(Location location, String id, float rotation) {

		ArmorStand stand = this.createOldEntity(location, id, rotation);
		
		this.entities.add(stand);
		return stand;
	}
	
	public void removeEntityWithoutBlock(Entity entity) {
		if(this.entities.contains(entity)) {
			this.entities.remove(entity);
			this.removeEntity(entity);
		}
	}

	private ItemDisplay createEntity(Block block, String id, Player player) {
		float rotation = getDirection(player, id);

		return this.createEntity(block, id, rotation);
	}

	private float getDirection(Player player, String id) {
		float rotation = 0;
		if (this.plugin.getConfigManager().getBoolean(ConfigType.BLOCKS, id + ".use-player-rotation")) {
			int round = this.plugin.getConfigManager().getInt(ConfigType.BLOCKS, id + ".rotation-round");
			round = round > 0 ? round : 90;
			round = round <= 90 ? round : 90;

			float yaw = player.getLocation().getYaw();
			float roundedYaw = Math.round(yaw / round) * round;
			rotation = (roundedYaw + 180) % 360;
		}
		
		return rotation;
	}

	public int debug(Location location, int radius) {
		int count = 0;
		Collection<Entity> entities = location.getWorld().getNearbyEntities(location, radius, radius, radius);
		for (Entity entity : entities) {
			if (!(entity instanceof ItemDisplay))
				continue;

			if (this.getBlock(entity) == null) {
				entity.remove();
				count++;
			}
		}
		return count;
	}

	private void save() {
		this.plugin.getConfigManager().saveDB();
	}
	
	private void removeEntity(Entity entity) {
		this.removeEntity(entity, entity != null ? entity.getUniqueId().toString() : null);
	}

	private void removeEntity(Entity entity, @Nullable String entityUuid) {
		if (entity == null && entityUuid != null) {
			entity = Bukkit.getEntity(UUID.fromString(entityUuid));
		}

		if (entity == null) {
			return;
		}

		if (entity.isValid() && entity.getLocation().getChunk().isLoaded()) {
			entity.remove();
		} else {
			Chunk chunk = entity.getLocation().getChunk();
			boolean wasForceLoaded = chunk.isForceLoaded();
			if (!chunk.isLoaded()) {
				chunk.setForceLoaded(true);
				chunk.load();
			}

			Entity loadedEntity = Bukkit.getEntity(entity.getUniqueId());
			if (loadedEntity != null) {
				loadedEntity.remove();
			}

			if (!wasForceLoaded) {
				chunk.setForceLoaded(false);
			}
		}
	}

	public void deInit() {
		this.tasker.cancel();
		this.save();
		//List<Chunk> chunks = new ArrayList<>();
		this.blocks.forEach(b -> {
			this.removeEntity(b.getEntity());
			
//			if (b.getEntity().isValid() && b.getEntity().getLocation().getChunk().isLoaded()) {
//				b.getEntity().remove();
//			} else {
//				Chunk chunk = b.getLocation().getChunk();
//				chunk.setForceLoaded(true);
//				chunk.load();
//				Bukkit.getEntity(UUID.fromString(b.getEntityUUID()));
//			}
		});
		
		this.entities.forEach(e -> this.removeEntity(e));
	}
	
	public boolean isCustomBlockArmorStand(Entity entity) {
		return this.entities.contains(entity);
	}
	
	private boolean hasAirReplacement(String id) {
		String replacement = this.plugin.getConfigManager().getString(ConfigType.BLOCKS, id + ".replacement-block");
		return replacement != null && replacement.equalsIgnoreCase(Material.AIR.name());
	}
}
