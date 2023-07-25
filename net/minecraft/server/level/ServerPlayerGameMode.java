package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerPlayerGameMode {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected ServerLevel level;
   protected final ServerPlayer player;
   private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
   @Nullable
   private GameType previousGameModeForPlayer;
   private boolean isDestroyingBlock;
   private int destroyProgressStart;
   private BlockPos destroyPos = BlockPos.ZERO;
   private int gameTicks;
   private boolean hasDelayedDestroy;
   private BlockPos delayedDestroyPos = BlockPos.ZERO;
   private int delayedTickStart;
   private int lastSentState = -1;

   public ServerPlayerGameMode(ServerPlayer serverplayer) {
      this.player = serverplayer;
      this.level = serverplayer.serverLevel();
   }

   public boolean changeGameModeForPlayer(GameType gametype) {
      if (gametype == this.gameModeForPlayer) {
         return false;
      } else {
         this.setGameModeForPlayer(gametype, this.previousGameModeForPlayer);
         this.player.onUpdateAbilities();
         this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player));
         this.level.updateSleepingPlayerList();
         return true;
      }
   }

   protected void setGameModeForPlayer(GameType gametype, @Nullable GameType gametype1) {
      this.previousGameModeForPlayer = gametype1;
      this.gameModeForPlayer = gametype;
      gametype.updatePlayerAbilities(this.player.getAbilities());
   }

   public GameType getGameModeForPlayer() {
      return this.gameModeForPlayer;
   }

   @Nullable
   public GameType getPreviousGameModeForPlayer() {
      return this.previousGameModeForPlayer;
   }

   public boolean isSurvival() {
      return this.gameModeForPlayer.isSurvival();
   }

   public boolean isCreative() {
      return this.gameModeForPlayer.isCreative();
   }

   public void tick() {
      ++this.gameTicks;
      if (this.hasDelayedDestroy) {
         BlockState blockstate = this.level.getBlockState(this.delayedDestroyPos);
         if (blockstate.isAir()) {
            this.hasDelayedDestroy = false;
         } else {
            float f = this.incrementDestroyProgress(blockstate, this.delayedDestroyPos, this.delayedTickStart);
            if (f >= 1.0F) {
               this.hasDelayedDestroy = false;
               this.destroyBlock(this.delayedDestroyPos);
            }
         }
      } else if (this.isDestroyingBlock) {
         BlockState blockstate1 = this.level.getBlockState(this.destroyPos);
         if (blockstate1.isAir()) {
            this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
            this.lastSentState = -1;
            this.isDestroyingBlock = false;
         } else {
            this.incrementDestroyProgress(blockstate1, this.destroyPos, this.destroyProgressStart);
         }
      }

   }

   private float incrementDestroyProgress(BlockState blockstate, BlockPos blockpos, int i) {
      int j = this.gameTicks - i;
      float f = blockstate.getDestroyProgress(this.player, this.player.level(), blockpos) * (float)(j + 1);
      int k = (int)(f * 10.0F);
      if (k != this.lastSentState) {
         this.level.destroyBlockProgress(this.player.getId(), blockpos, k);
         this.lastSentState = k;
      }

      return f;
   }

   private void debugLogging(BlockPos blockpos, boolean flag, int i, String s) {
   }

   public void handleBlockBreakAction(BlockPos blockpos, ServerboundPlayerActionPacket.Action serverboundplayeractionpacket_action, Direction direction, int i, int j) {
      if (this.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(blockpos)) > ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) {
         this.debugLogging(blockpos, false, j, "too far");
      } else if (blockpos.getY() >= i) {
         this.player.connection.send(new ClientboundBlockUpdatePacket(blockpos, this.level.getBlockState(blockpos)));
         this.debugLogging(blockpos, false, j, "too high");
      } else {
         if (serverboundplayeractionpacket_action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            if (!this.level.mayInteract(this.player, blockpos)) {
               this.player.connection.send(new ClientboundBlockUpdatePacket(blockpos, this.level.getBlockState(blockpos)));
               this.debugLogging(blockpos, false, j, "may not interact");
               return;
            }

            if (this.isCreative()) {
               this.destroyAndAck(blockpos, j, "creative destroy");
               return;
            }

            if (this.player.blockActionRestricted(this.level, blockpos, this.gameModeForPlayer)) {
               this.player.connection.send(new ClientboundBlockUpdatePacket(blockpos, this.level.getBlockState(blockpos)));
               this.debugLogging(blockpos, false, j, "block action restricted");
               return;
            }

            this.destroyProgressStart = this.gameTicks;
            float f = 1.0F;
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (!blockstate.isAir()) {
               blockstate.attack(this.level, blockpos, this.player);
               f = blockstate.getDestroyProgress(this.player, this.player.level(), blockpos);
            }

            if (!blockstate.isAir() && f >= 1.0F) {
               this.destroyAndAck(blockpos, j, "insta mine");
            } else {
               if (this.isDestroyingBlock) {
                  this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                  this.debugLogging(blockpos, false, j, "abort destroying since another started (client insta mine, server disagreed)");
               }

               this.isDestroyingBlock = true;
               this.destroyPos = blockpos.immutable();
               int k = (int)(f * 10.0F);
               this.level.destroyBlockProgress(this.player.getId(), blockpos, k);
               this.debugLogging(blockpos, true, j, "actual start of destroying");
               this.lastSentState = k;
            }
         } else if (serverboundplayeractionpacket_action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            if (blockpos.equals(this.destroyPos)) {
               int l = this.gameTicks - this.destroyProgressStart;
               BlockState blockstate1 = this.level.getBlockState(blockpos);
               if (!blockstate1.isAir()) {
                  float f1 = blockstate1.getDestroyProgress(this.player, this.player.level(), blockpos) * (float)(l + 1);
                  if (f1 >= 0.7F) {
                     this.isDestroyingBlock = false;
                     this.level.destroyBlockProgress(this.player.getId(), blockpos, -1);
                     this.destroyAndAck(blockpos, j, "destroyed");
                     return;
                  }

                  if (!this.hasDelayedDestroy) {
                     this.isDestroyingBlock = false;
                     this.hasDelayedDestroy = true;
                     this.delayedDestroyPos = blockpos;
                     this.delayedTickStart = this.destroyProgressStart;
                  }
               }
            }

            this.debugLogging(blockpos, true, j, "stopped destroying");
         } else if (serverboundplayeractionpacket_action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            this.isDestroyingBlock = false;
            if (!Objects.equals(this.destroyPos, blockpos)) {
               LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, blockpos);
               this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
               this.debugLogging(blockpos, true, j, "aborted mismatched destroying");
            }

            this.level.destroyBlockProgress(this.player.getId(), blockpos, -1);
            this.debugLogging(blockpos, true, j, "aborted destroying");
         }

      }
   }

   public void destroyAndAck(BlockPos blockpos, int i, String s) {
      if (this.destroyBlock(blockpos)) {
         this.debugLogging(blockpos, true, i, s);
      } else {
         this.player.connection.send(new ClientboundBlockUpdatePacket(blockpos, this.level.getBlockState(blockpos)));
         this.debugLogging(blockpos, false, i, s);
      }

   }

   public boolean destroyBlock(BlockPos blockpos) {
      BlockState blockstate = this.level.getBlockState(blockpos);
      if (!this.player.getMainHandItem().getItem().canAttackBlock(blockstate, this.level, blockpos, this.player)) {
         return false;
      } else {
         BlockEntity blockentity = this.level.getBlockEntity(blockpos);
         Block block = blockstate.getBlock();
         if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
            this.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            return false;
         } else if (this.player.blockActionRestricted(this.level, blockpos, this.gameModeForPlayer)) {
            return false;
         } else {
            block.playerWillDestroy(this.level, blockpos, blockstate, this.player);
            boolean flag = this.level.removeBlock(blockpos, false);
            if (flag) {
               block.destroy(this.level, blockpos, blockstate);
            }

            if (this.isCreative()) {
               return true;
            } else {
               ItemStack itemstack = this.player.getMainHandItem();
               ItemStack itemstack1 = itemstack.copy();
               boolean flag1 = this.player.hasCorrectToolForDrops(blockstate);
               itemstack.mineBlock(this.level, blockstate, blockpos, this.player);
               if (flag && flag1) {
                  block.playerDestroy(this.level, this.player, blockpos, blockstate, blockentity, itemstack1);
               }

               return true;
            }
         }
      }
   }

   public InteractionResult useItem(ServerPlayer serverplayer, Level level, ItemStack itemstack, InteractionHand interactionhand) {
      if (this.gameModeForPlayer == GameType.SPECTATOR) {
         return InteractionResult.PASS;
      } else if (serverplayer.getCooldowns().isOnCooldown(itemstack.getItem())) {
         return InteractionResult.PASS;
      } else {
         int i = itemstack.getCount();
         int j = itemstack.getDamageValue();
         InteractionResultHolder<ItemStack> interactionresultholder = itemstack.use(level, serverplayer, interactionhand);
         ItemStack itemstack1 = interactionresultholder.getObject();
         if (itemstack1 == itemstack && itemstack1.getCount() == i && itemstack1.getUseDuration() <= 0 && itemstack1.getDamageValue() == j) {
            return interactionresultholder.getResult();
         } else if (interactionresultholder.getResult() == InteractionResult.FAIL && itemstack1.getUseDuration() > 0 && !serverplayer.isUsingItem()) {
            return interactionresultholder.getResult();
         } else {
            if (itemstack != itemstack1) {
               serverplayer.setItemInHand(interactionhand, itemstack1);
            }

            if (this.isCreative() && itemstack1 != ItemStack.EMPTY) {
               itemstack1.setCount(i);
               if (itemstack1.isDamageableItem() && itemstack1.getDamageValue() != j) {
                  itemstack1.setDamageValue(j);
               }
            }

            if (itemstack1.isEmpty()) {
               serverplayer.setItemInHand(interactionhand, ItemStack.EMPTY);
            }

            if (!serverplayer.isUsingItem()) {
               serverplayer.inventoryMenu.sendAllDataToRemote();
            }

            return interactionresultholder.getResult();
         }
      }
   }

   public InteractionResult useItemOn(ServerPlayer serverplayer, Level level, ItemStack itemstack, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockPos blockpos = blockhitresult.getBlockPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (!blockstate.getBlock().isEnabled(level.enabledFeatures())) {
         return InteractionResult.FAIL;
      } else if (this.gameModeForPlayer == GameType.SPECTATOR) {
         MenuProvider menuprovider = blockstate.getMenuProvider(level, blockpos);
         if (menuprovider != null) {
            serverplayer.openMenu(menuprovider);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      } else {
         boolean flag = !serverplayer.getMainHandItem().isEmpty() || !serverplayer.getOffhandItem().isEmpty();
         boolean flag1 = serverplayer.isSecondaryUseActive() && flag;
         ItemStack itemstack1 = itemstack.copy();
         if (!flag1) {
            InteractionResult interactionresult = blockstate.use(level, serverplayer, interactionhand, blockhitresult);
            if (interactionresult.consumesAction()) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverplayer, blockpos, itemstack1);
               return interactionresult;
            }
         }

         if (!itemstack.isEmpty() && !serverplayer.getCooldowns().isOnCooldown(itemstack.getItem())) {
            UseOnContext useoncontext = new UseOnContext(serverplayer, interactionhand, blockhitresult);
            InteractionResult interactionresult1;
            if (this.isCreative()) {
               int i = itemstack.getCount();
               interactionresult1 = itemstack.useOn(useoncontext);
               itemstack.setCount(i);
            } else {
               interactionresult1 = itemstack.useOn(useoncontext);
            }

            if (interactionresult1.consumesAction()) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverplayer, blockpos, itemstack1);
            }

            return interactionresult1;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public void setLevel(ServerLevel serverlevel) {
      this.level = serverlevel;
   }
}
