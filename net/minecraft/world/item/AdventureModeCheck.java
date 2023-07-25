package net.minecraft.world.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AdventureModeCheck {
   private final String tagName;
   @Nullable
   private BlockInWorld lastCheckedBlock;
   private boolean lastResult;
   private boolean checksBlockEntity;

   public AdventureModeCheck(String s) {
      this.tagName = s;
   }

   private static boolean areSameBlocks(BlockInWorld blockinworld, @Nullable BlockInWorld blockinworld1, boolean flag) {
      if (blockinworld1 != null && blockinworld.getState() == blockinworld1.getState()) {
         if (!flag) {
            return true;
         } else if (blockinworld.getEntity() == null && blockinworld1.getEntity() == null) {
            return true;
         } else {
            return blockinworld.getEntity() != null && blockinworld1.getEntity() != null ? Objects.equals(blockinworld.getEntity().saveWithId(), blockinworld1.getEntity().saveWithId()) : false;
         }
      } else {
         return false;
      }
   }

   public boolean test(ItemStack itemstack, Registry<Block> registry, BlockInWorld blockinworld) {
      if (areSameBlocks(blockinworld, this.lastCheckedBlock, this.checksBlockEntity)) {
         return this.lastResult;
      } else {
         this.lastCheckedBlock = blockinworld;
         this.checksBlockEntity = false;
         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null && compoundtag.contains(this.tagName, 9)) {
            ListTag listtag = compoundtag.getList(this.tagName, 8);

            for(int i = 0; i < listtag.size(); ++i) {
               String s = listtag.getString(i);

               try {
                  BlockPredicateArgument.Result blockpredicateargument_result = BlockPredicateArgument.parse(registry.asLookup(), new StringReader(s));
                  this.checksBlockEntity |= blockpredicateargument_result.requiresNbt();
                  if (blockpredicateargument_result.test(blockinworld)) {
                     this.lastResult = true;
                     return true;
                  }
               } catch (CommandSyntaxException var9) {
               }
            }
         }

         this.lastResult = false;
         return false;
      }
   }
}
