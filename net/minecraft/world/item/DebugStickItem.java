package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem extends Item {
   public DebugStickItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   public boolean canAttackBlock(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      if (!level.isClientSide) {
         this.handleInteraction(player, blockstate, level, blockpos, false, player.getItemInHand(InteractionHand.MAIN_HAND));
      }

      return false;
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Player player = useoncontext.getPlayer();
      Level level = useoncontext.getLevel();
      if (!level.isClientSide && player != null) {
         BlockPos blockpos = useoncontext.getClickedPos();
         if (!this.handleInteraction(player, level.getBlockState(blockpos), level, blockpos, true, useoncontext.getItemInHand())) {
            return InteractionResult.FAIL;
         }
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   private boolean handleInteraction(Player player, BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, boolean flag, ItemStack itemstack) {
      if (!player.canUseGameMasterBlocks()) {
         return false;
      } else {
         Block block = blockstate.getBlock();
         StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
         Collection<Property<?>> collection = statedefinition.getProperties();
         String s = BuiltInRegistries.BLOCK.getKey(block).toString();
         if (collection.isEmpty()) {
            message(player, Component.translatable(this.getDescriptionId() + ".empty", s));
            return false;
         } else {
            CompoundTag compoundtag = itemstack.getOrCreateTagElement("DebugProperty");
            String s1 = compoundtag.getString(s);
            Property<?> property = statedefinition.getProperty(s1);
            if (flag) {
               if (property == null) {
                  property = collection.iterator().next();
               }

               BlockState blockstate1 = cycleState(blockstate, property, player.isSecondaryUseActive());
               levelaccessor.setBlock(blockpos, blockstate1, 18);
               message(player, Component.translatable(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockstate1, property)));
            } else {
               property = getRelative(collection, property, player.isSecondaryUseActive());
               String s2 = property.getName();
               compoundtag.putString(s, s2);
               message(player, Component.translatable(this.getDescriptionId() + ".select", s2, getNameHelper(blockstate, property)));
            }

            return true;
         }
      }
   }

   private static <T extends Comparable<T>> BlockState cycleState(BlockState blockstate, Property<T> property, boolean flag) {
      return blockstate.setValue(property, getRelative(property.getPossibleValues(), blockstate.getValue(property), flag));
   }

   private static <T> T getRelative(Iterable<T> iterable, @Nullable T object, boolean flag) {
      return (T)(flag ? Util.findPreviousInIterable(iterable, object) : Util.findNextInIterable(iterable, object));
   }

   private static void message(Player player, Component component) {
      ((ServerPlayer)player).sendSystemMessage(component, true);
   }

   private static <T extends Comparable<T>> String getNameHelper(BlockState blockstate, Property<T> property) {
      return property.getName(blockstate.getValue(property));
   }
}
