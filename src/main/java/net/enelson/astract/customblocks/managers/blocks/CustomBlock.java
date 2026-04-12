package net.enelson.astract.customblocks.managers.blocks;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CustomBlock {
	
	private String id;
	private Location blockLocation;
	private Entity entity;
	private String entityUUID;
	private float rotation;
	
	CustomBlock(String id, Location blockLocation, Entity entity, float rotation, String entityUUID) {
		this.id = id;
		this.blockLocation = blockLocation;
		this.entity = entity;
		this.rotation = rotation;
		this.entityUUID = entityUUID;
	}
	
	public Location getLocation() {
		return this.blockLocation;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Entity getEntity() {
		return this.entity;
	}
	
	public String getEntityUUID() {
		return this.entityUUID;
	}
	
	public float getRotation() {
		return this.rotation;
	}
}
