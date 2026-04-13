package net.enelson.astract.customblocks.managers.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.enelson.astract.customblocks.ACustomBlocks;
import net.enelson.astract.customblocks.managers.config.ConfigManager;
import net.enelson.astract.customblocks.managers.config.ConfigType;
import net.enelson.astract.customblocks.utils.Utils;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.customblocks.CustomBlockVisualOptions;
import net.enelson.sopli.lib.customblocks.CustomBlockVisualService;

public class BlockManager {

    private final ACustomBlocks plugin;
    private final ConfigManager configManager;
    private final CustomBlockVisualService visualService;

    private final Map<String, CustomBlock> blocksByLocation = new LinkedHashMap<String, CustomBlock>();
    private final Map<UUID, CustomBlock> blocksByEntityUuid = new LinkedHashMap<UUID, CustomBlock>();

    public BlockManager(ACustomBlocks plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.visualService = SopLib.getInstance().getCustomBlockVisualService();

        plugin.getLogger().info("VisualService: " + visualService.getClass().getName());
        loadFromDb();
    }

    public Entity createEntityWithoutBlock(Location location, ItemStack item, String id, CustomBlockVisualOptions options) {
        return visualService.createEntityWithoutBlock(location, item, id, options);
    }

    public void removeEntityWithoutBlock(Entity entity) {
        visualService.removeEntityWithoutBlock(entity);
    }

    public boolean isCustomBlockArmorStand(Entity entity) {
        return visualService.isManagedEntity(entity);
    }

    public CustomBlock getBlock(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return blocksByLocation.get(key(location));
    }

    public CustomBlock getBlock(Entity entity) {
        if (entity == null) {
            return null;
        }
        return blocksByEntityUuid.get(entity.getUniqueId());
    }

    public void addBlock(String id, Location location, Player player) {
        if (id == null || location == null || location.getWorld() == null) {
            return;
        }

        ItemStack item = Utils.generateItem(id);
        if (item == null || item.getType() == Material.AIR) {
            plugin.getLogger().warning("Failed to create visual item for custom block id: " + id);
            return;
        }

        boolean useYaw = configManager.getBoolean(ConfigType.BLOCKS, id + ".use-yaw");
        boolean usePitch = configManager.getBoolean(ConfigType.BLOCKS, id + ".use-pitch");

        double yawStep = readRotationStep(id + ".yaw-rotation-round", 90.0d);
        double pitchStep = readRotationStep(id + ".pitch-rotation-round", 0.0d);

        float yaw = 0.0f;
        if (useYaw && player != null) {
            float playerYaw = player.getLocation().getYaw();
            float roundedYaw = roundRotation(playerYaw, yawStep);
            yaw = (roundedYaw + 180.0f) % 360.0f;
        }

        float pitch = usePitch && player != null
                ? roundRotation(player.getLocation().getPitch(), pitchStep)
                : 0.0f;

        CustomBlockVisualOptions options = readVisualOptions(id, yaw, pitch, usePitch);

        Location base = location.getBlock().getLocation();
        Location spawn = base.clone().add(
                options.getOffsetX(),
                options.getOffsetY(),
                options.getOffsetZ()
        );

        CustomBlockVisualOptions spawnOptions = CustomBlockVisualOptions.of(
                options.getYaw(),
                options.getPitch(),
                options.isUsePitch(),
                0.0d,
                0.0d,
                0.0d,
                options.getScaleX(),
                options.getScaleY(),
                options.getScaleZ()
        );

        Entity entity = createEntityWithoutBlock(spawn, item, id, spawnOptions);

        CustomBlock customBlock = new CustomBlock(
                id,
                base,
                entity,
                yaw,
                pitch,
                usePitch,
                entity != null ? entity.getUniqueId().toString() : null
        );

        put(customBlock);
        save();
    }

    public void breakBlock(CustomBlock block, Player player) {
        if (block == null) {
            return;
        }

        removeStoredBlock(block);

        Location location = block.getLocation();
        if (location != null && location.getWorld() != null) {
            location.getBlock().setType(Material.AIR);
        }

        if (location != null && location.getWorld() != null) {
            boolean shouldDrop = player == null || player.getGameMode() != GameMode.CREATIVE;
            if (shouldDrop) {
                ItemStack drop = Utils.generateItem(block.getId());
                if (drop != null) {
                    location.getWorld().dropItemNaturally(location.clone().add(0.5, 0.2, 0.5), drop);
                }
            }
        }

        save();
    }

    public int debug(Location center, int radius) {
        if (center == null || center.getWorld() == null) {
            return 0;
        }

        int removed = 0;
        Collection<Entity> entities = center.getWorld().getNearbyEntities(center, radius, radius, radius);
        for (Entity entity : entities) {
            if (visualService.isManagedEntity(entity)) {
                entity.remove();
                removed++;
            }
        }
        return removed;
    }

    public void save() {
        YamlConfiguration db = configManager.getDB();
        for (String key : db.getKeys(false)) {
            db.set(key, null);
        }

        for (CustomBlock block : blocksByLocation.values()) {
            String base = key(block.getLocation());
            db.set(base + ".id", block.getId());
            db.set(base + ".location", Utils.getSerializedLocation(block.getLocation()));
            db.set(base + ".yaw", block.getRotationYaw());
            db.set(base + ".pitch", block.getRotationPitch());
            db.set(base + ".usePitch", block.isUsePitch());
            db.set(base + ".entityUUID", block.getEntityUUID());
        }

        configManager.saveDB();
    }

    public void deInit() {
        for (CustomBlock block : new ArrayList<CustomBlock>(blocksByLocation.values())) {
            if (block.getEntity() != null) {
                visualService.removeEntityWithoutBlock(block.getEntity());
            } else if (block.getEntityUUID() != null) {
                try {
                    visualService.removeEntityWithoutBlock(UUID.fromString(block.getEntityUUID()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        save();
        blocksByLocation.clear();
        blocksByEntityUuid.clear();
    }

    private void loadFromDb() {
        YamlConfiguration db = configManager.getDB();
        ConfigurationSection root = db.getConfigurationSection("");
        if (root == null) {
            return;
        }

        for (String entryKey : root.getKeys(false)) {
            String id = db.getString(entryKey + ".id");
            String locationSerialized = db.getString(entryKey + ".location");
            float yaw = (float) db.getDouble(entryKey + ".yaw", db.getDouble(entryKey + ".rotation"));
            float pitch = (float) db.getDouble(entryKey + ".pitch");
            boolean usePitch = db.getBoolean(entryKey + ".usePitch");
            String entityUuid = db.getString(entryKey + ".entityUUID");

            if (id == null || locationSerialized == null) {
                continue;
            }

            Location location;
            try {
                location = Utils.getDeserializedLocation(locationSerialized);
            } catch (Exception ex) {
                plugin.getLogger().warning("Skipping broken db entry: " + entryKey);
                continue;
            }

            Entity entity = null;
            if (entityUuid != null && !entityUuid.isEmpty()) {
                try {
                    entity = org.bukkit.Bukkit.getEntity(UUID.fromString(entityUuid));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (entity == null) {
                ItemStack item = Utils.generateItem(id);
                if (item != null && item.getType() != Material.AIR) {
                    CustomBlockVisualOptions options = readVisualOptions(id, yaw, pitch, usePitch);

                    Location spawn = location.getBlock().getLocation().clone().add(
                            options.getOffsetX(),
                            options.getOffsetY(),
                            options.getOffsetZ()
                    );

                    CustomBlockVisualOptions spawnOptions = CustomBlockVisualOptions.of(
                            options.getYaw(),
                            options.getPitch(),
                            options.isUsePitch(),
                            0.0d,
                            0.0d,
                            0.0d,
                            options.getScaleX(),
                            options.getScaleY(),
                            options.getScaleZ()
                    );

                    entity = createEntityWithoutBlock(spawn, item, id, spawnOptions);
                    if (entity != null) {
                        entityUuid = entity.getUniqueId().toString();
                    }
                }
            }

            put(new CustomBlock(
                    id,
                    location.getBlock().getLocation(),
                    entity,
                    yaw,
                    pitch,
                    usePitch,
                    entityUuid
            ));
        }
    }

    private void put(CustomBlock customBlock) {
        blocksByLocation.put(key(customBlock.getLocation()), customBlock);
        if (customBlock.getEntity() != null) {
            blocksByEntityUuid.put(customBlock.getEntity().getUniqueId(), customBlock);
        }
    }

    private void removeStoredBlock(CustomBlock customBlock) {
        blocksByLocation.remove(key(customBlock.getLocation()));

        if (customBlock.getEntity() != null) {
            blocksByEntityUuid.remove(customBlock.getEntity().getUniqueId());
            visualService.removeEntityWithoutBlock(customBlock.getEntity());
        } else if (customBlock.getEntityUUID() != null) {
            try {
                UUID uuid = UUID.fromString(customBlock.getEntityUUID());
                visualService.removeEntityWithoutBlock(uuid);
                blocksByEntityUuid.remove(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private CustomBlockVisualOptions readVisualOptions(String id, float yaw, float pitch, boolean usePitch) {
        double posX = readPosition(id + ".pos-x", 0.5d);
        double posY = readPosition(id + ".pos-y", 0.5d);
        double posZ = readPosition(id + ".pos-z", 0.5d);

        float scaleX = (float) readScale(id + ".scale-x", 1.002d);
        float scaleY = (float) readScale(id + ".scale-y", 1.002d);
        float scaleZ = (float) readScale(id + ".scale-z", 1.002d);

        return CustomBlockVisualOptions.of(
                yaw,
                usePitch ? pitch : 0.0f,
                usePitch,
                posX,
                posY,
                posZ,
                scaleX,
                scaleY,
                scaleZ
        );
    }

    private double readPosition(String path, double fallback) {
        YamlConfiguration blocks = configManager.getConfig(ConfigType.BLOCKS);
        return blocks.contains(path) ? blocks.getDouble(path) : fallback;
    }

    private double readScale(String path, double fallback) {
        YamlConfiguration blocks = configManager.getConfig(ConfigType.BLOCKS);
        return blocks.contains(path) ? blocks.getDouble(path) : fallback;
    }

    private double readRotationStep(String path, double fallback) {
        YamlConfiguration blocks = configManager.getConfig(ConfigType.BLOCKS);
        return blocks.contains(path) ? blocks.getDouble(path) : fallback;
    }

    private float roundRotation(float value, double step) {
        if (step <= 0.0d) {
            return value;
        }
        return (float) (Math.round(value / step) * step);
    }

    private String key(Location location) {
        return Utils.getSerializedLocation(location.getBlock().getLocation());
    }

    public ACustomBlocks getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}