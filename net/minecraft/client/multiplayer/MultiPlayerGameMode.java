package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class MultiPlayerGameMode {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final ClientPacketListener connection;
   private BlockPos destroyBlockPos = new BlockPos(-1, -1, -1);
   private ItemStack destroyingItem = ItemStack.EMPTY;
   private float destroyProgress;
   private float destroyTicks;
   private int destroyDelay;
   private boolean isDestroying;
   private GameType localPlayerMode = GameType.DEFAULT_MODE;
   @Nullable
   private GameType previousLocalPlayerMode;
   private int carriedIndex;

   public MultiPlayerGameMode(Minecraft minecraft, ClientPacketListener clientpacketlistener) {
      this.minecraft = minecraft;
      this.connection = clientpacketlistener;
   }

   public void adjustPlayer(Player player) {
      this.localPlayerMode.updatePlayerAbilities(player.getAbilities());
   }

   public void setLocalMode(GameType gametype, @Nullable GameType gametype1) {
      this.localPlayerMode = gametype;
      this.previousLocalPlayerMode = gametype1;
      this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.getAbilities());
   }

   public void setLocalMode(GameType gametype) {
      if (gametype != this.localPlayerMode) {
         this.previousLocalPlayerMode = this.localPlayerMode;
      }

      this.localPlayerMode = gametype;
      this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.getAbilities());
   }

   public boolean canHurtPlayer() {
      return this.localPlayerMode.isSurvival();
   }

   public boolean destroyBlock(BlockPos blockpos) {
      if (this.minecraft.player.blockActionRestricted(this.minecraft.level, blockpos, this.localPlayerMode)) {
         return false;
      } else {
         Level level = this.minecraft.level;
         BlockState blockstate = level.getBlockState(blockpos);
         if (!this.minecraft.player.getMainHandItem().getItem().canAttackBlock(blockstate, level, blockpos, this.minecraft.player)) {
            return false;
         } else {
            Block block = blockstate.getBlock();
            if (block instanceof GameMasterBlock && !this.minecraft.player.canUseGameMasterBlocks()) {
               return false;
            } else if (blockstate.isAir()) {
               return false;
            } else {
               block.playerWillDestroy(level, blockpos, blockstate, this.minecraft.player);
               FluidState fluidstate = level.getFluidState(blockpos);
               boolean flag = level.setBlock(blockpos, fluidstate.createLegacyBlock(), 11);
               if (flag) {
                  block.destroy(level, blockpos, blockstate);
               }

               return flag;
            }
         }
      }
   }

   public boolean startDestroyBlock(BlockPos blockpos, Direction direction) {
      if (this.minecraft.player.blockActionRestricted(this.minecraft.level, blockpos, this.localPlayerMode)) {
         return false;
      } else if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockpos)) {
         return false;
      } else {
         if (this.localPlayerMode.isCreative()) {
            BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockpos, blockstate, 1.0F);
            this.startPrediction(this.minecraft.level, (j) -> {
               this.destroyBlock(blockpos);
               return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockpos, direction, j);
            });
            this.destroyDelay = 5;
         } else if (!this.isDestroying || !this.sameDestroyTarget(blockpos)) {
            if (this.isDestroying) {
               this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction));
            }

            BlockState blockstate1 = this.minecraft.level.getBlockState(blockpos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockpos, blockstate1, 0.0F);
            this.startPrediction(this.minecraft.level, (i) -> {
               boolean flag = !blockstate1.isAir();
               if (flag && this.destroyProgress == 0.0F) {
                  blockstate1.attack(this.minecraft.level, blockpos, this.minecraft.player);
               }

               if (flag && blockstate1.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), blockpos) >= 1.0F) {
                  this.destroyBlock(blockpos);
               } else {
                  this.isDestroying = true;
                  this.destroyBlockPos = blockpos;
                  this.destroyingItem = this.minecraft.player.getMainHandItem();
                  this.destroyProgress = 0.0F;
                  this.destroyTicks = 0.0F;
                  this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, this.getDestroyStage());
               }

               return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockpos, direction, i);
            });
         }

         return true;
      }
   }

   public void stopDestroyBlock() {
      if (this.isDestroying) {
         BlockState blockstate = this.minecraft.level.getBlockState(this.destroyBlockPos);
         this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, this.destroyBlockPos, blockstate, -1.0F);
         this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN));
         this.isDestroying = false;
         this.destroyProgress = 0.0F;
         this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, -1);
         this.minecraft.player.resetAttackStrengthTicker();
      }

   }

   public boolean continueDestroyBlock(BlockPos blockpos, Direction direction) {
      this.ensureHasSentCarriedItem();
      if (this.destroyDelay > 0) {
         --this.destroyDelay;
         return true;
      } else if (this.localPlayerMode.isCreative() && this.minecraft.level.getWorldBorder().isWithinBounds(blockpos)) {
         this.destroyDelay = 5;
         BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
         this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockpos, blockstate, 1.0F);
         this.startPrediction(this.minecraft.level, (j) -> {
            this.destroyBlock(blockpos);
            return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockpos, direction, j);
         });
         return true;
      } else if (this.sameDestroyTarget(blockpos)) {
         BlockState blockstate1 = this.minecraft.level.getBlockState(blockpos);
         if (blockstate1.isAir()) {
            this.isDestroying = false;
            return false;
         } else {
            this.destroyProgress += blockstate1.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), blockpos);
            if (this.destroyTicks % 4.0F == 0.0F) {
               SoundType soundtype = blockstate1.getSoundType();
               this.minecraft.getSoundManager().play(new SimpleSoundInstance(soundtype.getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, SoundInstance.createUnseededRandom(), blockpos));
            }

            ++this.destroyTicks;
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockpos, blockstate1, Mth.clamp(this.destroyProgress, 0.0F, 1.0F));
            if (this.destroyProgress >= 1.0F) {
               this.isDestroying = false;
               this.startPrediction(this.minecraft.level, (i) -> {
                  this.destroyBlock(blockpos);
                  return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockpos, direction, i);
               });
               this.destroyProgress = 0.0F;
               this.destroyTicks = 0.0F;
               this.destroyDelay = 5;
            }

            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, this.getDestroyStage());
            return true;
         }
      } else {
         return this.startDestroyBlock(blockpos, direction);
      }
   }

   private void startPrediction(ClientLevel clientlevel, PredictiveAction predictiveaction) {
      BlockStatePredictionHandler blockstatepredictionhandler = clientlevel.getBlockStatePredictionHandler().startPredicting();

      try {
         int i = blockstatepredictionhandler.currentSequence();
         Packet<ServerGamePacketListener> packet = predictiveaction.predict(i);
         this.connection.send(packet);
      } catch (Throwable var7) {
         if (blockstatepredictionhandler != null) {
            try {
               blockstatepredictionhandler.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (blockstatepredictionhandler != null) {
         blockstatepredictionhandler.close();
      }

   }

   public float getPickRange() {
      return this.localPlayerMode.isCreative() ? 5.0F : 4.5F;
   }

   public void tick() {
      this.ensureHasSentCarriedItem();
      if (this.connection.getConnection().isConnected()) {
         this.connection.getConnection().tick();
      } else {
         this.connection.getConnection().handleDisconnection();
      }

   }

   private boolean sameDestroyTarget(BlockPos blockpos) {
      ItemStack itemstack = this.minecraft.player.getMainHandItem();
      return blockpos.equals(this.destroyBlockPos) && ItemStack.isSameItemSameTags(itemstack, this.destroyingItem);
   }

   private void ensureHasSentCarriedItem() {
      int i = this.minecraft.player.getInventory().selected;
      if (i != this.carriedIndex) {
         this.carriedIndex = i;
         this.connection.send(new ServerboundSetCarriedItemPacket(this.carriedIndex));
      }

   }

   public InteractionResult useItemOn(LocalPlayer localplayer, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      this.ensureHasSentCarriedItem();
      if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockhitresult.getBlockPos())) {
         return InteractionResult.FAIL;
      } else {
         MutableObject<InteractionResult> mutableobject = new MutableObject<>();
         this.startPrediction(this.minecraft.level, (i) -> {
            mutableobject.setValue(this.performUseItemOn(localplayer, interactionhand, blockhitresult));
            return new ServerboundUseItemOnPacket(interactionhand, blockhitresult, i);
         });
         return mutableobject.getValue();
      }
   }

   private InteractionResult performUseItemOn(LocalPlayer localplayer, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockPos blockpos = blockhitresult.getBlockPos();
      ItemStack itemstack = localplayer.getItemInHand(interactionhand);
      if (this.localPlayerMode == GameType.SPECTATOR) {
         return InteractionResult.SUCCESS;
      } else {
         boolean flag = !localplayer.getMainHandItem().isEmpty() || !localplayer.getOffhandItem().isEmpty();
         boolean flag1 = localplayer.isSecondaryUseActive() && flag;
         if (!flag1) {
            BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
            if (!this.connection.isFeatureEnabled(blockstate.getBlock().requiredFeatures())) {
               return InteractionResult.FAIL;
            }

            InteractionResult interactionresult = blockstate.use(this.minecraft.level, localplayer, interactionhand, blockhitresult);
            if (interactionresult.consumesAction()) {
               return interactionresult;
            }
         }

         if (!itemstack.isEmpty() && !localplayer.getCooldowns().isOnCooldown(itemstack.getItem())) {
            UseOnContext useoncontext = new UseOnContext(localplayer, interactionhand, blockhitresult);
            InteractionResult interactionresult1;
            if (this.localPlayerMode.isCreative()) {
               int i = itemstack.getCount();
               interactionresult1 = itemstack.useOn(useoncontext);
               itemstack.setCount(i);
            } else {
               interactionresult1 = itemstack.useOn(useoncontext);
            }

            return interactionresult1;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public InteractionResult useItem(Player player, InteractionHand interactionhand) {
      if (this.localPlayerMode == GameType.SPECTATOR) {
         return InteractionResult.PASS;
      } else {
         this.ensureHasSentCarriedItem();
         this.connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround()));
         MutableObject<InteractionResult> mutableobject = new MutableObject<>();
         this.startPrediction(this.minecraft.level, (i) -> {
            ServerboundUseItemPacket serverbounduseitempacket = new ServerboundUseItemPacket(interactionhand, i);
            ItemStack itemstack = player.getItemInHand(interactionhand);
            if (player.getCooldowns().isOnCooldown(itemstack.getItem())) {
               mutableobject.setValue(InteractionResult.PASS);
               return serverbounduseitempacket;
            } else {
               InteractionResultHolder<ItemStack> interactionresultholder = itemstack.use(this.minecraft.level, player, interactionhand);
               ItemStack itemstack1 = interactionresultholder.getObject();
               if (itemstack1 != itemstack) {
                  player.setItemInHand(interactionhand, itemstack1);
               }

               mutableobject.setValue(interactionresultholder.getResult());
               return serverbounduseitempacket;
            }
         });
         return mutableobject.getValue();
      }
   }

   public LocalPlayer createPlayer(ClientLevel clientlevel, StatsCounter statscounter, ClientRecipeBook clientrecipebook) {
      return this.createPlayer(clientlevel, statscounter, clientrecipebook, false, false);
   }

   public LocalPlayer createPlayer(ClientLevel clientlevel, StatsCounter statscounter, ClientRecipeBook clientrecipebook, boolean flag, boolean flag1) {
      return new LocalPlayer(this.minecraft, clientlevel, this.connection, statscounter, clientrecipebook, flag, flag1);
   }

   public void attack(Player player, Entity entity) {
      this.ensureHasSentCarriedItem();
      this.connection.send(ServerboundInteractPacket.createAttackPacket(entity, player.isShiftKeyDown()));
      if (this.localPlayerMode != GameType.SPECTATOR) {
         player.attack(entity);
         player.resetAttackStrengthTicker();
      }

   }

   public InteractionResult interact(Player player, Entity entity, InteractionHand interactionhand) {
      this.ensureHasSentCarriedItem();
      this.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), interactionhand));
      return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : player.interactOn(entity, interactionhand);
   }

   public InteractionResult interactAt(Player player, Entity entity, EntityHitResult entityhitresult, InteractionHand interactionhand) {
      this.ensureHasSentCarriedItem();
      Vec3 vec3 = entityhitresult.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
      this.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), interactionhand, vec3));
      return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : entity.interactAt(player, vec3, interactionhand);
   }

   public void handleInventoryMouseClick(int i, int j, int k, ClickType clicktype, Player player) {
      AbstractContainerMenu abstractcontainermenu = player.containerMenu;
      if (i != abstractcontainermenu.containerId) {
         LOGGER.warn("Ignoring click in mismatching container. Click in {}, player has {}.", i, abstractcontainermenu.containerId);
      } else {
         NonNullList<Slot> nonnulllist = abstractcontainermenu.slots;
         int l = nonnulllist.size();
         List<ItemStack> list = Lists.newArrayListWithCapacity(l);

         for(Slot slot : nonnulllist) {
            list.add(slot.getItem().copy());
         }

         abstractcontainermenu.clicked(j, k, clicktype, player);
         Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();

         for(int i1 = 0; i1 < l; ++i1) {
            ItemStack itemstack = list.get(i1);
            ItemStack itemstack1 = nonnulllist.get(i1).getItem();
            if (!ItemStack.matches(itemstack, itemstack1)) {
               int2objectmap.put(i1, itemstack1.copy());
            }
         }

         this.connection.send(new ServerboundContainerClickPacket(i, abstractcontainermenu.getStateId(), j, k, clicktype, abstractcontainermenu.getCarried().copy(), int2objectmap));
      }
   }

   public void handlePlaceRecipe(int i, Recipe<?> recipe, boolean flag) {
      this.connection.send(new ServerboundPlaceRecipePacket(i, recipe, flag));
   }

   public void handleInventoryButtonClick(int i, int j) {
      this.connection.send(new ServerboundContainerButtonClickPacket(i, j));
   }

   public void handleCreativeModeItemAdd(ItemStack itemstack, int i) {
      if (this.localPlayerMode.isCreative() && this.connection.isFeatureEnabled(itemstack.getItem().requiredFeatures())) {
         this.connection.send(new ServerboundSetCreativeModeSlotPacket(i, itemstack));
      }

   }

   public void handleCreativeModeItemDrop(ItemStack itemstack) {
      if (this.localPlayerMode.isCreative() && !itemstack.isEmpty() && this.connection.isFeatureEnabled(itemstack.getItem().requiredFeatures())) {
         this.connection.send(new ServerboundSetCreativeModeSlotPacket(-1, itemstack));
      }

   }

   public void releaseUsingItem(Player player) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
      player.releaseUsingItem();
   }

   public boolean hasExperience() {
      return this.localPlayerMode.isSurvival();
   }

   public boolean hasMissTime() {
      return !this.localPlayerMode.isCreative();
   }

   public boolean hasInfiniteItems() {
      return this.localPlayerMode.isCreative();
   }

   public boolean hasFarPickRange() {
      return this.localPlayerMode.isCreative();
   }

   public boolean isServerControlledInventory() {
      return this.minecraft.player.isPassenger() && this.minecraft.player.getVehicle() instanceof HasCustomInventoryScreen;
   }

   public boolean isAlwaysFlying() {
      return this.localPlayerMode == GameType.SPECTATOR;
   }

   @Nullable
   public GameType getPreviousPlayerMode() {
      return this.previousLocalPlayerMode;
   }

   public GameType getPlayerMode() {
      return this.localPlayerMode;
   }

   public boolean isDestroying() {
      return this.isDestroying;
   }

   public int getDestroyStage() {
      return this.destroyProgress > 0.0F ? (int)(this.destroyProgress * 10.0F) : -1;
   }

   public void handlePickItem(int i) {
      this.connection.send(new ServerboundPickItemPacket(i));
   }
}
