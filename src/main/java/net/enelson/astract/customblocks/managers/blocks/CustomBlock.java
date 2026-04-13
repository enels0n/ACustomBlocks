package net.enelson.astract.customblocks.managers.blocks;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CustomBlock {

    private final String id;
    private final Location location;
    private final Entity entity;
    private final float rotationYaw;
    private final float rotationPitch;
    private final boolean usePitch;
    private final String entityUUID;

    public CustomBlock(String id, Location location, Entity entity, float rotationYaw, float rotationPitch, boolean usePitch, String entityUUID) {
        this.id = id;
        this.location = location;
        this.entity = entity;
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
        this.usePitch = usePitch;
        this.entityUUID = entityUUID;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public Entity getEntity() {
        return entity;
    }

    public float getRotation() {
        return rotationYaw;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public boolean isUsePitch() {
        return usePitch;
    }

    public String getEntityUUID() {
        return entityUUID;
    }
}
