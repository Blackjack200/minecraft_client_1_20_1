package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SignBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TEXT_LINE_WIDTH = 90;
   private static final int TEXT_LINE_HEIGHT = 10;
   @Nullable
   private UUID playerWhoMayEdit;
   private SignText frontText = this.createDefaultSignText();
   private SignText backText = this.createDefaultSignText();
   private boolean isWaxed;

   public SignBlockEntity(BlockPos blockpos, BlockState blockstate) {
      this(BlockEntityType.SIGN, blockpos, blockstate);
   }

   public SignBlockEntity(BlockEntityType blockentitytype, BlockPos blockpos, BlockState blockstate) {
      super(blockentitytype, blockpos, blockstate);
   }

   protected SignText createDefaultSignText() {
      return new SignText();
   }

   public boolean isFacingFrontText(Player player) {
      Block vec3 = this.getBlockState().getBlock();
      if (vec3 instanceof SignBlock signblock) {
         Vec3 vec3 = signblock.getSignHitboxCenterPosition(this.getBlockState());
         double d0 = player.getX() - ((double)this.getBlockPos().getX() + vec3.x);
         double d1 = player.getZ() - ((double)this.getBlockPos().getZ() + vec3.z);
         float f = signblock.getYRotationDegrees(this.getBlockState());
         float f1 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
         return Mth.degreesDifferenceAbs(f, f1) <= 90.0F;
      } else {
         return false;
      }
   }

   public SignText getTextFacingPlayer(Player player) {
      return this.getText(this.isFacingFrontText(player));
   }

   public SignText getText(boolean flag) {
      return flag ? this.frontText : this.backText;
   }

   public SignText getFrontText() {
      return this.frontText;
   }

   public SignText getBackText() {
      return this.backText;
   }

   public int getTextLineHeight() {
      return 10;
   }

   public int getMaxTextLineWidth() {
      return 90;
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.frontText).resultOrPartial(LOGGER::error).ifPresent((tag1) -> compoundtag.put("front_text", tag1));
      SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.backText).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("back_text", tag));
      compoundtag.putBoolean("is_waxed", this.isWaxed);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("front_text")) {
         SignText.DIRECT_CODEC.parse(NbtOps.INSTANCE, compoundtag.getCompound("front_text")).resultOrPartial(LOGGER::error).ifPresent((signtext1) -> this.frontText = this.loadLines(signtext1));
      }

      if (compoundtag.contains("back_text")) {
         SignText.DIRECT_CODEC.parse(NbtOps.INSTANCE, compoundtag.getCompound("back_text")).resultOrPartial(LOGGER::error).ifPresent((signtext) -> this.backText = this.loadLines(signtext));
      }

      this.isWaxed = compoundtag.getBoolean("is_waxed");
   }

   private SignText loadLines(SignText signtext) {
      for(int i = 0; i < 4; ++i) {
         Component component = this.loadLine(signtext.getMessage(i, false));
         Component component1 = this.loadLine(signtext.getMessage(i, true));
         signtext = signtext.setMessage(i, component, component1);
      }

      return signtext;
   }

   private Component loadLine(Component component) {
      Level var3 = this.level;
      if (var3 instanceof ServerLevel serverlevel) {
         try {
            return ComponentUtils.updateForEntity(createCommandSourceStack((Player)null, serverlevel, this.worldPosition), component, (Entity)null, 0);
         } catch (CommandSyntaxException var4) {
         }
      }

      return component;
   }

   public void updateSignText(Player player, boolean flag, List<FilteredText> list) {
      if (!this.isWaxed() && player.getUUID().equals(this.getPlayerWhoMayEdit()) && this.level != null) {
         this.updateText((signtext) -> this.setMessages(player, list, signtext), flag);
         this.setAllowedPlayerEditor((UUID)null);
         this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
      } else {
         LOGGER.warn("Player {} just tried to change non-editable sign", (Object)player.getName().getString());
      }
   }

   public boolean updateText(UnaryOperator<SignText> unaryoperator, boolean flag) {
      SignText signtext = this.getText(flag);
      return this.setText(unaryoperator.apply(signtext), flag);
   }

   private SignText setMessages(Player player, List<FilteredText> list, SignText signtext) {
      for(int i = 0; i < list.size(); ++i) {
         FilteredText filteredtext = list.get(i);
         Style style = signtext.getMessage(i, player.isTextFilteringEnabled()).getStyle();
         if (player.isTextFilteringEnabled()) {
            signtext = signtext.setMessage(i, Component.literal(filteredtext.filteredOrEmpty()).setStyle(style));
         } else {
            signtext = signtext.setMessage(i, Component.literal(filteredtext.raw()).setStyle(style), Component.literal(filteredtext.filteredOrEmpty()).setStyle(style));
         }
      }

      return signtext;
   }

   public boolean setText(SignText signtext, boolean flag) {
      return flag ? this.setFrontText(signtext) : this.setBackText(signtext);
   }

   private boolean setBackText(SignText signtext) {
      if (signtext != this.backText) {
         this.backText = signtext;
         this.markUpdated();
         return true;
      } else {
         return false;
      }
   }

   private boolean setFrontText(SignText signtext) {
      if (signtext != this.frontText) {
         this.frontText = signtext;
         this.markUpdated();
         return true;
      } else {
         return false;
      }
   }

   public boolean canExecuteClickCommands(boolean flag, Player player) {
      return this.isWaxed() && this.getText(flag).hasAnyClickCommands(player);
   }

   public boolean executeClickCommandsIfPresent(Player player, Level level, BlockPos blockpos, boolean flag) {
      boolean flag1 = false;

      for(Component component : this.getText(flag).getMessages(player.isTextFilteringEnabled())) {
         Style style = component.getStyle();
         ClickEvent clickevent = style.getClickEvent();
         if (clickevent != null && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            player.getServer().getCommands().performPrefixedCommand(createCommandSourceStack(player, level, blockpos), clickevent.getValue());
            flag1 = true;
         }
      }

      return flag1;
   }

   private static CommandSourceStack createCommandSourceStack(@Nullable Player player, Level level, BlockPos blockpos) {
      String s = player == null ? "Sign" : player.getName().getString();
      Component component = (Component)(player == null ? Component.literal("Sign") : player.getDisplayName());
      return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(blockpos), Vec2.ZERO, (ServerLevel)level, 2, s, component, level.getServer(), player);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void setAllowedPlayerEditor(@Nullable UUID uuid) {
      this.playerWhoMayEdit = uuid;
   }

   @Nullable
   public UUID getPlayerWhoMayEdit() {
      return this.playerWhoMayEdit;
   }

   private void markUpdated() {
      this.setChanged();
      this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
   }

   public boolean isWaxed() {
      return this.isWaxed;
   }

   public boolean setWaxed(boolean flag) {
      if (this.isWaxed != flag) {
         this.isWaxed = flag;
         this.markUpdated();
         return true;
      } else {
         return false;
      }
   }

   public boolean playerIsTooFarAwayToEdit(UUID uuid) {
      Player player = this.level.getPlayerByUUID(uuid);
      return player == null || player.distanceToSqr((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ()) > 64.0D;
   }

   public static void tick(Level level, BlockPos blockpos, BlockState blockstate, SignBlockEntity signblockentity) {
      UUID uuid = signblockentity.getPlayerWhoMayEdit();
      if (uuid != null) {
         signblockentity.clearInvalidPlayerWhoMayEdit(signblockentity, level, uuid);
      }

   }

   private void clearInvalidPlayerWhoMayEdit(SignBlockEntity signblockentity, Level level, UUID uuid) {
      if (signblockentity.playerIsTooFarAwayToEdit(uuid)) {
         signblockentity.setAllowedPlayerEditor((UUID)null);
      }

   }
}
