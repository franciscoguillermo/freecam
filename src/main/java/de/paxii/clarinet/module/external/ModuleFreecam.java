package de.paxii.clarinet.module.external;

import de.paxii.clarinet.Wrapper;
import de.paxii.clarinet.event.EventHandler;
import de.paxii.clarinet.event.events.entity.EntityMoveEvent;
import de.paxii.clarinet.event.events.game.IngameTickEvent;
import de.paxii.clarinet.event.events.player.PlayerUpdateWalkingEvent;
import de.paxii.clarinet.event.events.player.PreMotionUpdateEvent;
import de.paxii.clarinet.module.Module;
import de.paxii.clarinet.module.ModuleCategory;
import de.paxii.clarinet.util.module.settings.ValueBase;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

import lombok.Value;

/**
 * Created by Lars on 26.03.2016.
 */
public class ModuleFreecam extends Module {
  private int entityID;
  private Random random;
  private Location savedLocation;

  public ModuleFreecam() {
    super("Freecam", ModuleCategory.MOVEMENT);

    this.setVersion("1.2");
    this.setBuildVersion(16006);

    this.getModuleValues().put("speed", new ValueBase("Freecam Speed", 10.0F, 1.0F, 20.0F, "Speed"));

    this.random = new Random();
  }

  @Override
  public void onEnable() {
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

    Wrapper.getEventManager().register(this);
  }

  @EventHandler
  public void onIngameTick(IngameTickEvent event) {
    float flightSpeed = this.getValueBase("speed").getValue() / 4;

    Wrapper.getPlayer().jumpMovementFactor = flightSpeed;

    Wrapper.getPlayer().motionY = 0.0D;
    Wrapper.getPlayer().motionX = 0.0D;
    Wrapper.getPlayer().motionZ = 0.0D;

    if (Wrapper.getGameSettings().keyBindJump.isKeyDown()) {
      Wrapper.getPlayer().motionY += flightSpeed / 1.2;
    } else if (Wrapper.getGameSettings().keyBindSneak.isKeyDown()) {
      Wrapper.getPlayer().motionY -= flightSpeed / 1.2;
    }
  }

  @EventHandler
  public void onPreMotionUpdate(PreMotionUpdateEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityMove(EntityMoveEvent moveEvent) {
    if (moveEvent.getEntity() == Wrapper.getPlayer()) {
      moveEvent.setX(moveEvent.getX() * this.getValueBase("speed").getValue());
      moveEvent.setZ(moveEvent.getZ() * this.getValueBase("speed").getValue());
      moveEvent.setNoClip(true);
    }
  }

  @EventHandler
  public void onUpdateWalking(PlayerUpdateWalkingEvent walkingEvent) {
    walkingEvent.setCancelled(true);
  }

  @Override
  public void onDisable() {
    Wrapper.getEventManager().unregister(this);

    Wrapper.getPlayer().setPosition(
            this.savedLocation.getBlockPos().getX(),
            this.savedLocation.getBlockPos().getY(),
            this.savedLocation.getBlockPos().getZ()
    );
    Wrapper.getPlayer().rotationPitch = this.savedLocation.getRotationPitch();
    Wrapper.getPlayer().rotationYaw = this.savedLocation.getRotationYaw();
    Wrapper.getPlayer().rotationYawHead = this.savedLocation.getRotationYawHead();
    Wrapper.getWorld().removeEntityFromWorld(this.entityID);
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

