package net.minecraft.client.player;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.MinecartCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.BubbleColumnAmbientSoundHandler;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundHandler;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundInstances;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class LocalPlayer extends AbstractClientPlayer {
   public static final Logger LOGGER = LogUtils.getLogger();
   private static final int POSITION_REMINDER_INTERVAL = 20;
   private static final int WATER_VISION_MAX_TIME = 600;
   private static final int WATER_VISION_QUICK_TIME = 100;
   private static final float WATER_VISION_QUICK_PERCENT = 0.6F;
   private static final double SUFFOCATING_COLLISION_CHECK_SCALE = 0.35D;
   private static final double MINOR_COLLISION_ANGLE_THRESHOLD_RADIAN = (double)0.13962634F;
   private static final float DEFAULT_SNEAKING_MOVEMENT_FACTOR = 0.3F;
   public final ClientPacketListener connection;
   private final StatsCounter stats;
   private final ClientRecipeBook recipeBook;
   private final List<AmbientSoundHandler> ambientSoundHandlers = Lists.newArrayList();
   private int permissionLevel = 0;
   private double xLast;
   private double yLast1;
   private double zLast;
   private float yRotLast;
   private float xRotLast;
   private boolean lastOnGround;
   private boolean crouching;
   private boolean wasShiftKeyDown;
   private boolean wasSprinting;
   private int positionReminder;
   private boolean flashOnSetHealth;
   @Nullable
   private String serverBrand;
   public Input input;
   protected final Minecraft minecraft;
   protected int sprintTriggerTime;
   public float yBob;
   public float xBob;
   public float yBobO;
   public float xBobO;
   private int jumpRidingTicks;
   private float jumpRidingScale;
   public float spinningEffectIntensity;
   public float oSpinningEffectIntensity;
   private boolean startedUsingItem;
   @Nullable
   private InteractionHand usingItemHand;
   private boolean handsBusy;
   private boolean autoJumpEnabled = true;
   private int autoJumpTime;
   private boolean wasFallFlying;
   private int waterVisionTime;
   private boolean showDeathScreen = true;

   public LocalPlayer(Minecraft minecraft, ClientLevel clientlevel, ClientPacketListener clientpacketlistener, StatsCounter statscounter, ClientRecipeBook clientrecipebook, boolean flag, boolean flag1) {
      super(clientlevel, clientpacketlistener.getLocalGameProfile());
      this.minecraft = minecraft;
      this.connection = clientpacketlistener;
      this.stats = statscounter;
      this.recipeBook = clientrecipebook;
      this.wasShiftKeyDown = flag;
      this.wasSprinting = flag1;
      this.ambientSoundHandlers.add(new UnderwaterAmbientSoundHandler(this, minecraft.getSoundManager()));
      this.ambientSoundHandlers.add(new BubbleColumnAmbientSoundHandler(this));
      this.ambientSoundHandlers.add(new BiomeAmbientSoundsHandler(this, minecraft.getSoundManager(), clientlevel.getBiomeManager()));
   }

   public boolean hurt(DamageSource damagesource, float f) {
      return false;
   }

   public void heal(float f) {
   }

   public boolean startRiding(Entity entity, boolean flag) {
      if (!super.startRiding(entity, flag)) {
         return false;
      } else {
         if (entity instanceof AbstractMinecart) {
            this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)entity, true));
            this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)entity, false));
         }

         return true;
      }
   }

   public void removeVehicle() {
      super.removeVehicle();
      this.handsBusy = false;
   }

   public float getViewXRot(float f) {
      return this.getXRot();
   }

   public float getViewYRot(float f) {
      return this.isPassenger() ? super.getViewYRot(f) : this.getYRot();
   }

   public void tick() {
      if (this.level().hasChunkAt(this.getBlockX(), this.getBlockZ())) {
         super.tick();
         if (this.isPassenger()) {
            this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround()));
            this.connection.send(new ServerboundPlayerInputPacket(this.xxa, this.zza, this.input.jumping, this.input.shiftKeyDown));
            Entity entity = this.getRootVehicle();
            if (entity != this && entity.isControlledByLocalInstance()) {
               this.connection.send(new ServerboundMoveVehiclePacket(entity));
               this.sendIsSprintingIfNeeded();
            }
         } else {
            this.sendPosition();
         }

         for(AmbientSoundHandler ambientsoundhandler : this.ambientSoundHandlers) {
            ambientsoundhandler.tick();
         }

      }
   }

   public float getCurrentMood() {
      for(AmbientSoundHandler ambientsoundhandler : this.ambientSoundHandlers) {
         if (ambientsoundhandler instanceof BiomeAmbientSoundsHandler) {
            return ((BiomeAmbientSoundsHandler)ambientsoundhandler).getMoodiness();
         }
      }

      return 0.0F;
   }

   private void sendPosition() {
      this.sendIsSprintingIfNeeded();
      boolean flag = this.isShiftKeyDown();
      if (flag != this.wasShiftKeyDown) {
         ServerboundPlayerCommandPacket.Action serverboundplayercommandpacket_action = flag ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
         this.connection.send(new ServerboundPlayerCommandPacket(this, serverboundplayercommandpacket_action));
         this.wasShiftKeyDown = flag;
      }

      if (this.isControlledCamera()) {
         double d0 = this.getX() - this.xLast;
         double d1 = this.getY() - this.yLast1;
         double d2 = this.getZ() - this.zLast;
         double d3 = (double)(this.getYRot() - this.yRotLast);
         double d4 = (double)(this.getXRot() - this.xRotLast);
         ++this.positionReminder;
         boolean flag1 = Mth.lengthSquared(d0, d1, d2) > Mth.square(2.0E-4D) || this.positionReminder >= 20;
         boolean flag2 = d3 != 0.0D || d4 != 0.0D;
         if (this.isPassenger()) {
            Vec3 vec3 = this.getDeltaMovement();
            this.connection.send(new ServerboundMovePlayerPacket.PosRot(vec3.x, -999.0D, vec3.z, this.getYRot(), this.getXRot(), this.onGround()));
            flag1 = false;
         } else if (flag1 && flag2) {
            this.connection.send(new ServerboundMovePlayerPacket.PosRot(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot(), this.onGround()));
         } else if (flag1) {
            this.connection.send(new ServerboundMovePlayerPacket.Pos(this.getX(), this.getY(), this.getZ(), this.onGround()));
         } else if (flag2) {
            this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround()));
         } else if (this.lastOnGround != this.onGround()) {
            this.connection.send(new ServerboundMovePlayerPacket.StatusOnly(this.onGround()));
         }

         if (flag1) {
            this.xLast = this.getX();
            this.yLast1 = this.getY();
            this.zLast = this.getZ();
            this.positionReminder = 0;
         }

         if (flag2) {
            this.yRotLast = this.getYRot();
            this.xRotLast = this.getXRot();
         }

         this.lastOnGround = this.onGround();
         this.autoJumpEnabled = this.minecraft.options.autoJump().get();
      }

   }

   private void sendIsSprintingIfNeeded() {
      boolean flag = this.isSprinting();
      if (flag != this.wasSprinting) {
         ServerboundPlayerCommandPacket.Action serverboundplayercommandpacket_action = flag ? ServerboundPlayerCommandPacket.Action.START_SPRINTING : ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
         this.connection.send(new ServerboundPlayerCommandPacket(this, serverboundplayercommandpacket_action));
         this.wasSprinting = flag;
      }

   }

   public boolean drop(boolean flag) {
      ServerboundPlayerActionPacket.Action serverboundplayeractionpacket_action = flag ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS : ServerboundPlayerActionPacket.Action.DROP_ITEM;
      ItemStack itemstack = this.getInventory().removeFromSelected(flag);
      this.connection.send(new ServerboundPlayerActionPacket(serverboundplayeractionpacket_action, BlockPos.ZERO, Direction.DOWN));
      return !itemstack.isEmpty();
   }

   public void swing(InteractionHand interactionhand) {
      super.swing(interactionhand);
      this.connection.send(new ServerboundSwingPacket(interactionhand));
   }

   public void respawn() {
      this.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
      KeyMapping.resetToggleKeys();
   }

   protected void actuallyHurt(DamageSource damagesource, float f) {
      if (!this.isInvulnerableTo(damagesource)) {
         this.setHealth(this.getHealth() - f);
      }
   }

   public void closeContainer() {
      this.connection.send(new ServerboundContainerClosePacket(this.containerMenu.containerId));
      this.clientSideCloseContainer();
   }

   public void clientSideCloseContainer() {
      super.closeContainer();
      this.minecraft.setScreen((Screen)null);
   }

   public void hurtTo(float f) {
      if (this.flashOnSetHealth) {
         float f1 = this.getHealth() - f;
         if (f1 <= 0.0F) {
            this.setHealth(f);
            if (f1 < 0.0F) {
               this.invulnerableTime = 10;
            }
         } else {
            this.lastHurt = f1;
            this.invulnerableTime = 20;
            this.setHealth(f);
            this.hurtDuration = 10;
            this.hurtTime = this.hurtDuration;
         }
      } else {
         this.setHealth(f);
         this.flashOnSetHealth = true;
      }

   }

   public void onUpdateAbilities() {
      this.connection.send(new ServerboundPlayerAbilitiesPacket(this.getAbilities()));
   }

   public boolean isLocalPlayer() {
      return true;
   }

   public boolean isSuppressingSlidingDownLadder() {
      return !this.getAbilities().flying && super.isSuppressingSlidingDownLadder();
   }

   public boolean canSpawnSprintParticle() {
      return !this.getAbilities().flying && super.canSpawnSprintParticle();
   }

   public boolean canSpawnSoulSpeedParticle() {
      return !this.getAbilities().flying && super.canSpawnSoulSpeedParticle();
   }

   protected void sendRidingJump() {
      this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP, Mth.floor(this.getJumpRidingScale() * 100.0F)));
   }

   public void sendOpenInventory() {
      this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
   }

   public void setServerBrand(@Nullable String s) {
      this.serverBrand = s;
   }

   @Nullable
   public String getServerBrand() {
      return this.serverBrand;
   }

   public StatsCounter getStats() {
      return this.stats;
   }

   public ClientRecipeBook getRecipeBook() {
      return this.recipeBook;
   }

   public void removeRecipeHighlight(Recipe<?> recipe) {
      if (this.recipeBook.willHighlight(recipe)) {
         this.recipeBook.removeHighlight(recipe);
         this.connection.send(new ServerboundRecipeBookSeenRecipePacket(recipe));
      }

   }

   protected int getPermissionLevel() {
      return this.permissionLevel;
   }

   public void setPermissionLevel(int i) {
      this.permissionLevel = i;
   }

   public void displayClientMessage(Component component, boolean flag) {
      this.minecraft.getChatListener().handleSystemMessage(component, flag);
   }

   private void moveTowardsClosestSpace(double d0, double d1) {
      BlockPos blockpos = BlockPos.containing(d0, this.getY(), d1);
      if (this.suffocatesAt(blockpos)) {
         double d2 = d0 - (double)blockpos.getX();
         double d3 = d1 - (double)blockpos.getZ();
         Direction direction = null;
         double d4 = Double.MAX_VALUE;
         Direction[] adirection = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

         for(Direction direction1 : adirection) {
            double d5 = direction1.getAxis().choose(d2, 0.0D, d3);
            double d6 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d5 : d5;
            if (d6 < d4 && !this.suffocatesAt(blockpos.relative(direction1))) {
               d4 = d6;
               direction = direction1;
            }
         }

         if (direction != null) {
            Vec3 vec3 = this.getDeltaMovement();
            if (direction.getAxis() == Direction.Axis.X) {
               this.setDeltaMovement(0.1D * (double)direction.getStepX(), vec3.y, vec3.z);
            } else {
               this.setDeltaMovement(vec3.x, vec3.y, 0.1D * (double)direction.getStepZ());
            }
         }

      }
   }

   private boolean suffocatesAt(BlockPos blockpos) {
      AABB aabb = this.getBoundingBox();
      AABB aabb1 = (new AABB((double)blockpos.getX(), aabb.minY, (double)blockpos.getZ(), (double)blockpos.getX() + 1.0D, aabb.maxY, (double)blockpos.getZ() + 1.0D)).deflate(1.0E-7D);
      return this.level().collidesWithSuffocatingBlock(this, aabb1);
   }

   public void setExperienceValues(float f, int i, int j) {
      this.experienceProgress = f;
      this.totalExperience = i;
      this.experienceLevel = j;
   }

   public void sendSystemMessage(Component component) {
      this.minecraft.gui.getChat().addMessage(component);
   }

   public void handleEntityEvent(byte b0) {
      if (b0 >= 24 && b0 <= 28) {
         this.setPermissionLevel(b0 - 24);
      } else {
         super.handleEntityEvent(b0);
      }

   }

   public void setShowDeathScreen(boolean flag) {
      this.showDeathScreen = flag;
   }

   public boolean shouldShowDeathScreen() {
      return this.showDeathScreen;
   }

   public void playSound(SoundEvent soundevent, float f, float f1) {
      this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), f, f1, false);
   }

   public void playNotifySound(SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
      this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundevent, soundsource, f, f1, false);
   }

   public boolean isEffectiveAi() {
      return true;
   }

   public void startUsingItem(InteractionHand interactionhand) {
      ItemStack itemstack = this.getItemInHand(interactionhand);
      if (!itemstack.isEmpty() && !this.isUsingItem()) {
         super.startUsingItem(interactionhand);
         this.startedUsingItem = true;
         this.usingItemHand = interactionhand;
      }
   }

   public boolean isUsingItem() {
      return this.startedUsingItem;
   }

   public void stopUsingItem() {
      super.stopUsingItem();
      this.startedUsingItem = false;
   }

   public InteractionHand getUsedItemHand() {
      return Objects.requireNonNullElse(this.usingItemHand, InteractionHand.MAIN_HAND);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      super.onSyncedDataUpdated(entitydataaccessor);
      if (DATA_LIVING_ENTITY_FLAGS.equals(entitydataaccessor)) {
         boolean flag = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
         InteractionHand interactionhand = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         if (flag && !this.startedUsingItem) {
            this.startUsingItem(interactionhand);
         } else if (!flag && this.startedUsingItem) {
            this.stopUsingItem();
         }
      }

      if (DATA_SHARED_FLAGS_ID.equals(entitydataaccessor) && this.isFallFlying() && !this.wasFallFlying) {
         this.minecraft.getSoundManager().play(new ElytraOnPlayerSoundInstance(this));
      }

   }

   @Nullable
   public PlayerRideableJumping jumpableVehicle() {
      Entity var2 = this.getControlledVehicle();
      if (var2 instanceof PlayerRideableJumping playerrideablejumping) {
         if (playerrideablejumping.canJump()) {
            return playerrideablejumping;
         }
      }

      return null;
   }

   public float getJumpRidingScale() {
      return this.jumpRidingScale;
   }

   public boolean isTextFilteringEnabled() {
      return this.minecraft.isTextFilteringEnabled();
   }

   public void openTextEdit(SignBlockEntity signblockentity, boolean flag) {
      if (signblockentity instanceof HangingSignBlockEntity hangingsignblockentity) {
         this.minecraft.setScreen(new HangingSignEditScreen(hangingsignblockentity, flag, this.minecraft.isTextFilteringEnabled()));
      } else {
         this.minecraft.setScreen(new SignEditScreen(signblockentity, flag, this.minecraft.isTextFilteringEnabled()));
      }

   }

   public void openMinecartCommandBlock(BaseCommandBlock basecommandblock) {
      this.minecraft.setScreen(new MinecartCommandBlockEditScreen(basecommandblock));
   }

   public void openCommandBlock(CommandBlockEntity commandblockentity) {
      this.minecraft.setScreen(new CommandBlockEditScreen(commandblockentity));
   }

   public void openStructureBlock(StructureBlockEntity structureblockentity) {
      this.minecraft.setScreen(new StructureBlockEditScreen(structureblockentity));
   }

   public void openJigsawBlock(JigsawBlockEntity jigsawblockentity) {
      this.minecraft.setScreen(new JigsawBlockEditScreen(jigsawblockentity));
   }

   public void openItemGui(ItemStack itemstack, InteractionHand interactionhand) {
      if (itemstack.is(Items.WRITABLE_BOOK)) {
         this.minecraft.setScreen(new BookEditScreen(this, itemstack, interactionhand));
      }

   }

   public void crit(Entity entity) {
      this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
   }

   public void magicCrit(Entity entity) {
      this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
   }

   public boolean isShiftKeyDown() {
      return this.input != null && this.input.shiftKeyDown;
   }

   public boolean isCrouching() {
      return this.crouching;
   }

   public boolean isMovingSlowly() {
      return this.isCrouching() || this.isVisuallyCrawling();
   }

   public void serverAiStep() {
      super.serverAiStep();
      if (this.isControlledCamera()) {
         this.xxa = this.input.leftImpulse;
         this.zza = this.input.forwardImpulse;
         this.jumping = this.input.jumping;
         this.yBobO = this.yBob;
         this.xBobO = this.xBob;
         this.xBob += (this.getXRot() - this.xBob) * 0.5F;
         this.yBob += (this.getYRot() - this.yBob) * 0.5F;
      }

   }

   protected boolean isControlledCamera() {
      return this.minecraft.getCameraEntity() == this;
   }

   public void resetPos() {
      this.setPose(Pose.STANDING);
      if (this.level() != null) {
         for(double d0 = this.getY(); d0 > (double)this.level().getMinBuildHeight() && d0 < (double)this.level().getMaxBuildHeight(); ++d0) {
            this.setPos(this.getX(), d0, this.getZ());
            if (this.level().noCollision(this)) {
               break;
            }
         }

         this.setDeltaMovement(Vec3.ZERO);
         this.setXRot(0.0F);
      }

      this.setHealth(this.getMaxHealth());
      this.deathTime = 0;
   }

   public void aiStep() {
      if (this.sprintTriggerTime > 0) {
         --this.sprintTriggerTime;
      }

      if (!(this.minecraft.screen instanceof ReceivingLevelScreen)) {
         this.handleNetherPortalClient();
      }

      boolean flag = this.input.jumping;
      boolean flag1 = this.input.shiftKeyDown;
      boolean flag2 = this.hasEnoughImpulseToStartSprinting();
      this.crouching = !this.getAbilities().flying && !this.isSwimming() && this.canEnterPose(Pose.CROUCHING) && (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
      float f = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(this), 0.0F, 1.0F);
      this.input.tick(this.isMovingSlowly(), f);
      this.minecraft.getTutorial().onInput(this.input);
      if (this.isUsingItem() && !this.isPassenger()) {
         this.input.leftImpulse *= 0.2F;
         this.input.forwardImpulse *= 0.2F;
         this.sprintTriggerTime = 0;
      }

      boolean flag3 = false;
      if (this.autoJumpTime > 0) {
         --this.autoJumpTime;
         flag3 = true;
         this.input.jumping = true;
      }

      if (!this.noPhysics) {
         this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35D, this.getZ() + (double)this.getBbWidth() * 0.35D);
         this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35D, this.getZ() - (double)this.getBbWidth() * 0.35D);
         this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35D, this.getZ() - (double)this.getBbWidth() * 0.35D);
         this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35D, this.getZ() + (double)this.getBbWidth() * 0.35D);
      }

      if (flag1) {
         this.sprintTriggerTime = 0;
      }

      boolean flag4 = this.canStartSprinting();
      boolean flag5 = this.isPassenger() ? this.getVehicle().onGround() : this.onGround();
      boolean flag6 = !flag1 && !flag2;
      if ((flag5 || this.isUnderWater()) && flag6 && flag4) {
         if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
            this.sprintTriggerTime = 7;
         } else {
            this.setSprinting(true);
         }
      }

      if ((!this.isInWater() || this.isUnderWater()) && flag4 && this.minecraft.options.keySprint.isDown()) {
         this.setSprinting(true);
      }

      if (this.isSprinting()) {
         boolean flag7 = !this.input.hasForwardImpulse() || !this.hasEnoughFoodToStartSprinting();
         boolean flag8 = flag7 || this.horizontalCollision && !this.minorHorizontalCollision || this.isInWater() && !this.isUnderWater();
         if (this.isSwimming()) {
            if (!this.onGround() && !this.input.shiftKeyDown && flag7 || !this.isInWater()) {
               this.setSprinting(false);
            }
         } else if (flag8) {
            this.setSprinting(false);
         }
      }

      boolean flag9 = false;
      if (this.getAbilities().mayfly) {
         if (this.minecraft.gameMode.isAlwaysFlying()) {
            if (!this.getAbilities().flying) {
               this.getAbilities().flying = true;
               flag9 = true;
               this.onUpdateAbilities();
            }
         } else if (!flag && this.input.jumping && !flag3) {
            if (this.jumpTriggerTime == 0) {
               this.jumpTriggerTime = 7;
            } else if (!this.isSwimming()) {
               this.getAbilities().flying = !this.getAbilities().flying;
               flag9 = true;
               this.onUpdateAbilities();
               this.jumpTriggerTime = 0;
            }
         }
      }

      if (this.input.jumping && !flag9 && !flag && !this.getAbilities().flying && !this.isPassenger() && !this.onClimbable()) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
         if (itemstack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemstack) && this.tryToStartFallFlying()) {
            this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
         }
      }

      this.wasFallFlying = this.isFallFlying();
      if (this.isInWater() && this.input.shiftKeyDown && this.isAffectedByFluids()) {
         this.goDownInWater();
      }

      if (this.isEyeInFluid(FluidTags.WATER)) {
         int i = this.isSpectator() ? 10 : 1;
         this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
      } else if (this.waterVisionTime > 0) {
         this.isEyeInFluid(FluidTags.WATER);
         this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
      }

      if (this.getAbilities().flying && this.isControlledCamera()) {
         int j = 0;
         if (this.input.shiftKeyDown) {
            --j;
         }

         if (this.input.jumping) {
            ++j;
         }

         if (j != 0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)((float)j * this.getAbilities().getFlyingSpeed() * 3.0F), 0.0D));
         }
      }

      PlayerRideableJumping playerrideablejumping = this.jumpableVehicle();
      if (playerrideablejumping != null && playerrideablejumping.getJumpCooldown() == 0) {
         if (this.jumpRidingTicks < 0) {
            ++this.jumpRidingTicks;
            if (this.jumpRidingTicks == 0) {
               this.jumpRidingScale = 0.0F;
            }
         }

         if (flag && !this.input.jumping) {
            this.jumpRidingTicks = -10;
            playerrideablejumping.onPlayerJump(Mth.floor(this.getJumpRidingScale() * 100.0F));
            this.sendRidingJump();
         } else if (!flag && this.input.jumping) {
            this.jumpRidingTicks = 0;
            this.jumpRidingScale = 0.0F;
         } else if (flag) {
            ++this.jumpRidingTicks;
            if (this.jumpRidingTicks < 10) {
               this.jumpRidingScale = (float)this.jumpRidingTicks * 0.1F;
            } else {
               this.jumpRidingScale = 0.8F + 2.0F / (float)(this.jumpRidingTicks - 9) * 0.1F;
            }
         }
      } else {
         this.jumpRidingScale = 0.0F;
      }

      super.aiStep();
      if (this.onGround() && this.getAbilities().flying && !this.minecraft.gameMode.isAlwaysFlying()) {
         this.getAbilities().flying = false;
         this.onUpdateAbilities();
      }

   }

   protected void tickDeath() {
      ++this.deathTime;
      if (this.deathTime == 20) {
         this.remove(Entity.RemovalReason.KILLED);
      }

   }

   private void handleNetherPortalClient() {
      this.oSpinningEffectIntensity = this.spinningEffectIntensity;
      float f = 0.0F;
      if (this.isInsidePortal) {
         if (this.minecraft.screen != null && !this.minecraft.screen.isPauseScreen() && !(this.minecraft.screen instanceof DeathScreen)) {
            if (this.minecraft.screen instanceof AbstractContainerScreen) {
               this.closeContainer();
            }

            this.minecraft.setScreen((Screen)null);
         }

         if (this.spinningEffectIntensity == 0.0F) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
         }

         f = 0.0125F;
         this.isInsidePortal = false;
      } else if (this.hasEffect(MobEffects.CONFUSION) && !this.getEffect(MobEffects.CONFUSION).endsWithin(60)) {
         f = 0.006666667F;
      } else if (this.spinningEffectIntensity > 0.0F) {
         f = -0.05F;
      }

      this.spinningEffectIntensity = Mth.clamp(this.spinningEffectIntensity + f, 0.0F, 1.0F);
      this.processPortalCooldown();
   }

   public void rideTick() {
      super.rideTick();
      this.handsBusy = false;
      Entity var2 = this.getControlledVehicle();
      if (var2 instanceof Boat boat) {
         boat.setInput(this.input.left, this.input.right, this.input.up, this.input.down);
         this.handsBusy |= this.input.left || this.input.right || this.input.up || this.input.down;
      }

   }

   public boolean isHandsBusy() {
      return this.handsBusy;
   }

   @Nullable
   public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect mobeffect) {
      if (mobeffect == MobEffects.CONFUSION) {
         this.oSpinningEffectIntensity = 0.0F;
         this.spinningEffectIntensity = 0.0F;
      }

      return super.removeEffectNoUpdate(mobeffect);
   }

   public void move(MoverType movertype, Vec3 vec3) {
      double d0 = this.getX();
      double d1 = this.getZ();
      super.move(movertype, vec3);
      this.updateAutoJump((float)(this.getX() - d0), (float)(this.getZ() - d1));
   }

   public boolean isAutoJumpEnabled() {
      return this.autoJumpEnabled;
   }

   protected void updateAutoJump(float f, float f1) {
      if (this.canAutoJump()) {
         Vec3 vec3 = this.position();
         Vec3 vec31 = vec3.add((double)f, 0.0D, (double)f1);
         Vec3 vec32 = new Vec3((double)f, 0.0D, (double)f1);
         float f2 = this.getSpeed();
         float f3 = (float)vec32.lengthSqr();
         if (f3 <= 0.001F) {
            Vec2 vec2 = this.input.getMoveVector();
            float f4 = f2 * vec2.x;
            float f5 = f2 * vec2.y;
            float f6 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
            float f7 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
            vec32 = new Vec3((double)(f4 * f7 - f5 * f6), vec32.y, (double)(f5 * f7 + f4 * f6));
            f3 = (float)vec32.lengthSqr();
            if (f3 <= 0.001F) {
               return;
            }
         }

         float f8 = Mth.invSqrt(f3);
         Vec3 vec33 = vec32.scale((double)f8);
         Vec3 vec34 = this.getForward();
         float f9 = (float)(vec34.x * vec33.x + vec34.z * vec33.z);
         if (!(f9 < -0.15F)) {
            CollisionContext collisioncontext = CollisionContext.of(this);
            BlockPos blockpos = BlockPos.containing(this.getX(), this.getBoundingBox().maxY, this.getZ());
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (blockstate.getCollisionShape(this.level(), blockpos, collisioncontext).isEmpty()) {
               blockpos = blockpos.above();
               BlockState blockstate1 = this.level().getBlockState(blockpos);
               if (blockstate1.getCollisionShape(this.level(), blockpos, collisioncontext).isEmpty()) {
                  float f10 = 7.0F;
                  float f11 = 1.2F;
                  if (this.hasEffect(MobEffects.JUMP)) {
                     f11 += (float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.75F;
                  }

                  float f12 = Math.max(f2 * 7.0F, 1.0F / f8);
                  Vec3 vec36 = vec31.add(vec33.scale((double)f12));
                  float f13 = this.getBbWidth();
                  float f14 = this.getBbHeight();
                  AABB aabb = (new AABB(vec3, vec36.add(0.0D, (double)f14, 0.0D))).inflate((double)f13, 0.0D, (double)f13);
                  Vec3 vec35 = vec3.add(0.0D, (double)0.51F, 0.0D);
                  vec36 = vec36.add(0.0D, (double)0.51F, 0.0D);
                  Vec3 vec37 = vec33.cross(new Vec3(0.0D, 1.0D, 0.0D));
                  Vec3 vec38 = vec37.scale((double)(f13 * 0.5F));
                  Vec3 vec39 = vec35.subtract(vec38);
                  Vec3 vec310 = vec36.subtract(vec38);
                  Vec3 vec311 = vec35.add(vec38);
                  Vec3 vec312 = vec36.add(vec38);
                  Iterable<VoxelShape> iterable = this.level().getCollisions(this, aabb);
                  Iterator<AABB> iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap((voxelshape1) -> voxelshape1.toAabbs().stream()).iterator();
                  float f15 = Float.MIN_VALUE;

                  while(iterator.hasNext()) {
                     AABB aabb1 = iterator.next();
                     if (aabb1.intersects(vec39, vec310) || aabb1.intersects(vec311, vec312)) {
                        f15 = (float)aabb1.maxY;
                        Vec3 vec313 = aabb1.getCenter();
                        BlockPos blockpos1 = BlockPos.containing(vec313);

                        for(int i = 1; (float)i < f11; ++i) {
                           BlockPos blockpos2 = blockpos1.above(i);
                           BlockState blockstate2 = this.level().getBlockState(blockpos2);
                           VoxelShape voxelshape;
                           if (!(voxelshape = blockstate2.getCollisionShape(this.level(), blockpos2, collisioncontext)).isEmpty()) {
                              f15 = (float)voxelshape.max(Direction.Axis.Y) + (float)blockpos2.getY();
                              if ((double)f15 - this.getY() > (double)f11) {
                                 return;
                              }
                           }

                           if (i > 1) {
                              blockpos = blockpos.above();
                              BlockState blockstate3 = this.level().getBlockState(blockpos);
                              if (!blockstate3.getCollisionShape(this.level(), blockpos, collisioncontext).isEmpty()) {
                                 return;
                              }
                           }
                        }
                        break;
                     }
                  }

                  if (f15 != Float.MIN_VALUE) {
                     float f16 = (float)((double)f15 - this.getY());
                     if (!(f16 <= 0.5F) && !(f16 > f11)) {
                        this.autoJumpTime = 1;
                     }
                  }
               }
            }
         }
      }
   }

   protected boolean isHorizontalCollisionMinor(Vec3 vec3) {
      float f = this.getYRot() * ((float)Math.PI / 180F);
      double d0 = (double)Mth.sin(f);
      double d1 = (double)Mth.cos(f);
      double d2 = (double)this.xxa * d1 - (double)this.zza * d0;
      double d3 = (double)this.zza * d1 + (double)this.xxa * d0;
      double d4 = Mth.square(d2) + Mth.square(d3);
      double d5 = Mth.square(vec3.x) + Mth.square(vec3.z);
      if (!(d4 < (double)1.0E-5F) && !(d5 < (double)1.0E-5F)) {
         double d6 = d2 * vec3.x + d3 * vec3.z;
         double d7 = Math.acos(d6 / Math.sqrt(d4 * d5));
         return d7 < (double)0.13962634F;
      } else {
         return false;
      }
   }

   private boolean canAutoJump() {
      return this.isAutoJumpEnabled() && this.autoJumpTime <= 0 && this.onGround() && !this.isStayingOnGroundSurface() && !this.isPassenger() && this.isMoving() && (double)this.getBlockJumpFactor() >= 1.0D;
   }

   private boolean isMoving() {
      Vec2 vec2 = this.input.getMoveVector();
      return vec2.x != 0.0F || vec2.y != 0.0F;
   }

   private boolean canStartSprinting() {
      return !this.isSprinting() && this.hasEnoughImpulseToStartSprinting() && this.hasEnoughFoodToStartSprinting() && !this.isUsingItem() && !this.hasEffect(MobEffects.BLINDNESS) && (!this.isPassenger() || this.vehicleCanSprint(this.getVehicle())) && !this.isFallFlying();
   }

   private boolean vehicleCanSprint(Entity entity) {
      return entity.canSprint() && entity.isControlledByLocalInstance();
   }

   private boolean hasEnoughImpulseToStartSprinting() {
      double d0 = 0.8D;
      return this.isUnderWater() ? this.input.hasForwardImpulse() : (double)this.input.forwardImpulse >= 0.8D;
   }

   private boolean hasEnoughFoodToStartSprinting() {
      return this.isPassenger() || (float)this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly;
   }

   public float getWaterVision() {
      if (!this.isEyeInFluid(FluidTags.WATER)) {
         return 0.0F;
      } else {
         float f = 600.0F;
         float f1 = 100.0F;
         if ((float)this.waterVisionTime >= 600.0F) {
            return 1.0F;
         } else {
            float f2 = Mth.clamp((float)this.waterVisionTime / 100.0F, 0.0F, 1.0F);
            float f3 = (float)this.waterVisionTime < 100.0F ? 0.0F : Mth.clamp(((float)this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
            return f2 * 0.6F + f3 * 0.39999998F;
         }
      }
   }

   public void onGameModeChanged(GameType gametype) {
      if (gametype == GameType.SPECTATOR) {
         this.setDeltaMovement(this.getDeltaMovement().with(Direction.Axis.Y, 0.0D));
      }

   }

   public boolean isUnderWater() {
      return this.wasUnderwater;
   }

   protected boolean updateIsUnderwater() {
      boolean flag = this.wasUnderwater;
      boolean flag1 = super.updateIsUnderwater();
      if (this.isSpectator()) {
         return this.wasUnderwater;
      } else {
         if (!flag && flag1) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.AMBIENT, 1.0F, 1.0F, false);
            this.minecraft.getSoundManager().play(new UnderwaterAmbientSoundInstances.UnderwaterAmbientSoundInstance(this));
         }

         if (flag && !flag1) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.AMBIENT, 1.0F, 1.0F, false);
         }

         return this.wasUnderwater;
      }
   }

   public Vec3 getRopeHoldPosition(float f) {
      if (this.minecraft.options.getCameraType().isFirstPerson()) {
         float f1 = Mth.lerp(f * 0.5F, this.getYRot(), this.yRotO) * ((float)Math.PI / 180F);
         float f2 = Mth.lerp(f * 0.5F, this.getXRot(), this.xRotO) * ((float)Math.PI / 180F);
         double d0 = this.getMainArm() == HumanoidArm.RIGHT ? -1.0D : 1.0D;
         Vec3 vec3 = new Vec3(0.39D * d0, -0.6D, 0.3D);
         return vec3.xRot(-f2).yRot(-f1).add(this.getEyePosition(f));
      } else {
         return super.getRopeHoldPosition(f);
      }
   }

   public void updateTutorialInventoryAction(ItemStack itemstack, ItemStack itemstack1, ClickAction clickaction) {
      this.minecraft.getTutorial().onInventoryAction(itemstack, itemstack1, clickaction);
   }

   public float getVisualRotationYInDegrees() {
      return this.getYRot();
   }
}
