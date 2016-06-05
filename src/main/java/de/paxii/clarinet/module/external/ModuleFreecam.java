package de.paxii.clarinet.module.external;

import de.paxii.clarinet.Wrapper;
import de.paxii.clarinet.event.EventHandler;
import de.paxii.clarinet.event.events.player.PreMotionUpdateEvent;
import de.paxii.clarinet.module.Module;
import de.paxii.clarinet.module.ModuleCategory;
import lombok.Value;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Created by Lars on 26.03.2016.
 */
public class ModuleFreecam extends Module {
	private int entityID;
	private Random random;
	private Location savedLocation;

	public ModuleFreecam() {
		super("Freecam", ModuleCategory.MOVEMENT);

		this.random = new Random();
	}

	@Override
	public void onEnable() {
		Wrapper.getEventManager().register(this);

		this.entityID = -random.nextInt(99);

		EntityOtherPlayerMP decoyPlayer = new EntityOtherPlayerMP(Wrapper.getWorld(), Wrapper.getPlayer().getGameProfile());
		decoyPlayer.clonePlayer(Wrapper.getPlayer(), true);
		decoyPlayer.rotationYawHead = Wrapper.getPlayer().rotationYawHead;
		decoyPlayer.copyLocationAndAnglesFrom(Wrapper.getPlayer());
		Wrapper.getWorld().addEntityToWorld(this.entityID, decoyPlayer);

		this.savedLocation = new Location(
				Wrapper.getPlayer().getPosition(),
				Wrapper.getPlayer().rotationYaw,
				Wrapper.getPlayer().rotationYawHead,
				Wrapper.getPlayer().rotationPitch
		);
	}

	@EventHandler
	public void onPreMotionUpdate(PreMotionUpdateEvent event) {
		event.setCancelled(true);
	}

	@Override
	public void onDisable() {
		Wrapper.getPlayer().setPosition(
				this.savedLocation.getBlockPos().getX(),
				this.savedLocation.getBlockPos().getY(),
				this.savedLocation.getBlockPos().getZ()
		);
		Wrapper.getPlayer().rotationPitch = this.savedLocation.getRotationPitch();
		Wrapper.getPlayer().rotationYaw = this.savedLocation.getRotationYaw();
		Wrapper.getPlayer().rotationYawHead = this.savedLocation.getRotationYawHead();
		Wrapper.getWorld().removeEntityFromWorld(this.entityID);

		Wrapper.getEventManager().unregister(this);
	}

	@Value
	private class Location {
		private BlockPos blockPos;
		private float rotationYaw, rotationYawHead, rotationPitch;

		Location(BlockPos blockPos, float rotationYaw, float rotationYawHead, float rotationPitch) {
			this.blockPos = blockPos;
			this.rotationYaw = rotationYaw;
			this.rotationYawHead = rotationYawHead;
			this.rotationPitch = rotationPitch;
		}
	}
}

