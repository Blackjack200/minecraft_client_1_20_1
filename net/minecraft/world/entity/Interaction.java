package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Interaction extends Entity implements Attackable, Targeting {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.BOOLEAN);
   private static final String TAG_WIDTH = "width";
   private static final String TAG_HEIGHT = "height";
   private static final String TAG_ATTACK = "attack";
   private static final String TAG_INTERACTION = "interaction";
   private static final String TAG_RESPONSE = "response";
   @Nullable
   private Interaction.PlayerAction attack;
   @Nullable
   private Interaction.PlayerAction interaction;

   public Interaction(EntityType<?> entitytype, Level level) {
      super(entitytype, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_WIDTH_ID, 1.0F);
      this.entityData.define(DATA_HEIGHT_ID, 1.0F);
      this.entityData.define(DATA_RESPONSE_ID, false);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.contains("width", 99)) {
         this.setWidth(compoundtag.getFloat("width"));
      }

      if (compoundtag.contains("height", 99)) {
         this.setHeight(compoundtag.getFloat("height"));
      }

      if (compoundtag.contains("attack")) {
         Interaction.PlayerAction.CODEC.decode(NbtOps.INSTANCE, compoundtag.get("attack")).resultOrPartial(Util.prefix("Interaction entity", LOGGER::error)).ifPresent((pair1) -> this.attack = pair1.getFirst());
      } else {
         this.attack = null;
      }

      if (compoundtag.contains("interaction")) {
         Interaction.PlayerAction.CODEC.decode(NbtOps.INSTANCE, compoundtag.get("interaction")).resultOrPartial(Util.prefix("Interaction entity", LOGGER::error)).ifPresent((pair) -> this.interaction = pair.getFirst());
      } else {
         this.interaction = null;
      }

      this.setResponse(compoundtag.getBoolean("response"));
      this.setBoundingBox(this.makeBoundingBox());
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putFloat("width", this.getWidth());
      compoundtag.putFloat("height", this.getHeight());
      if (this.attack != null) {
         Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.attack).result().ifPresent((tag1) -> compoundtag.put("attack", tag1));
      }

      if (this.interaction != null) {
         Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.interaction).result().ifPresent((tag) -> compoundtag.put("interaction", tag));
      }

      compoundtag.putBoolean("response", this.getResponse());
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      super.onSyncedDataUpdated(entitydataaccessor);
      if (DATA_HEIGHT_ID.equals(entitydataaccessor) || DATA_WIDTH_ID.equals(entitydataaccessor)) {
         this.setBoundingBox(this.makeBoundingBox());
      }

   }

   public boolean canBeHitByProjectile() {
      return false;
   }

   public boolean isPickable() {
      return true;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   public boolean skipAttackInteraction(Entity entity) {
      if (entity instanceof Player player) {
         this.attack = new Interaction.PlayerAction(player.getUUID(), this.level().getGameTime());
         if (player instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverplayer, this, player.damageSources().generic(), 1.0F, 1.0F, false);
         }

         return !this.getResponse();
      } else {
         return false;
      }
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      if (this.level().isClientSide) {
         return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
      } else {
         this.interaction = new Interaction.PlayerAction(player.getUUID(), this.level().getGameTime());
         return InteractionResult.CONSUME;
      }
   }

   public void tick() {
   }

   @Nullable
   public LivingEntity getLastAttacker() {
      return this.attack != null ? this.level().getPlayerByUUID(this.attack.player()) : null;
   }

   @Nullable
   public LivingEntity getTarget() {
      return this.interaction != null ? this.level().getPlayerByUUID(this.interaction.player()) : null;
   }

   private void setWidth(float f) {
      this.entityData.set(DATA_WIDTH_ID, f);
   }

   private float getWidth() {
      return this.entityData.get(DATA_WIDTH_ID);
   }

   private void setHeight(float f) {
      this.entityData.set(DATA_HEIGHT_ID, f);
   }

   private float getHeight() {
      return this.entityData.get(DATA_HEIGHT_ID);
   }

   private void setResponse(boolean flag) {
      this.entityData.set(DATA_RESPONSE_ID, flag);
   }

   private boolean getResponse() {
      return this.entityData.get(DATA_RESPONSE_ID);
   }

   private EntityDimensions getDimensions() {
      return EntityDimensions.scalable(this.getWidth(), this.getHeight());
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.getDimensions();
   }

   protected AABB makeBoundingBox() {
      return this.getDimensions().makeBoundingBox(this.position());
   }

   static record PlayerAction(UUID player, long timestamp) {
      public static final Codec<Interaction.PlayerAction> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(UUIDUtil.CODEC.fieldOf("player").forGetter(Interaction.PlayerAction::player), Codec.LONG.fieldOf("timestamp").forGetter(Interaction.PlayerAction::timestamp)).apply(recordcodecbuilder_instance, Interaction.PlayerAction::new));
   }
}
