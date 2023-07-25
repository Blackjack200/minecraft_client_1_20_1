package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WrittenBookItem extends Item {
   public static final int TITLE_LENGTH = 16;
   public static final int TITLE_MAX_LENGTH = 32;
   public static final int PAGE_EDIT_LENGTH = 1024;
   public static final int PAGE_LENGTH = 32767;
   public static final int MAX_PAGES = 100;
   public static final int MAX_GENERATION = 2;
   public static final String TAG_TITLE = "title";
   public static final String TAG_FILTERED_TITLE = "filtered_title";
   public static final String TAG_AUTHOR = "author";
   public static final String TAG_PAGES = "pages";
   public static final String TAG_FILTERED_PAGES = "filtered_pages";
   public static final String TAG_GENERATION = "generation";
   public static final String TAG_RESOLVED = "resolved";

   public WrittenBookItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundtag) {
      if (!WritableBookItem.makeSureTagIsValid(compoundtag)) {
         return false;
      } else if (!compoundtag.contains("title", 8)) {
         return false;
      } else {
         String s = compoundtag.getString("title");
         return s.length() > 32 ? false : compoundtag.contains("author", 8);
      }
   }

   public static int getGeneration(ItemStack itemstack) {
      return itemstack.getTag().getInt("generation");
   }

   public static int getPageCount(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      return compoundtag != null ? compoundtag.getList("pages", 8).size() : 0;
   }

   public Component getName(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null) {
         String s = compoundtag.getString("title");
         if (!StringUtil.isNullOrEmpty(s)) {
            return Component.literal(s);
         }
      }

      return super.getName(itemstack);
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      if (itemstack.hasTag()) {
         CompoundTag compoundtag = itemstack.getTag();
         String s = compoundtag.getString("author");
         if (!StringUtil.isNullOrEmpty(s)) {
            list.add(Component.translatable("book.byAuthor", s).withStyle(ChatFormatting.GRAY));
         }

         list.add(Component.translatable("book.generation." + compoundtag.getInt("generation")).withStyle(ChatFormatting.GRAY));
      }

   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(Blocks.LECTERN)) {
         return LecternBlock.tryPlaceBook(useoncontext.getPlayer(), level, blockpos, blockstate, useoncontext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      player.openItemGui(itemstack, interactionhand);
      player.awardStat(Stats.ITEM_USED.get(this));
      return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
   }

   public static boolean resolveBookComponents(ItemStack itemstack, @Nullable CommandSourceStack commandsourcestack, @Nullable Player player) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && !compoundtag.getBoolean("resolved")) {
         compoundtag.putBoolean("resolved", true);
         if (!makeSureTagIsValid(compoundtag)) {
            return false;
         } else {
            ListTag listtag = compoundtag.getList("pages", 8);
            ListTag listtag1 = new ListTag();

            for(int i = 0; i < listtag.size(); ++i) {
               String s = resolvePage(commandsourcestack, player, listtag.getString(i));
               if (s.length() > 32767) {
                  return false;
               }

               listtag1.add(i, (Tag)StringTag.valueOf(s));
            }

            if (compoundtag.contains("filtered_pages", 10)) {
               CompoundTag compoundtag1 = compoundtag.getCompound("filtered_pages");
               CompoundTag compoundtag2 = new CompoundTag();

               for(String s1 : compoundtag1.getAllKeys()) {
                  String s2 = resolvePage(commandsourcestack, player, compoundtag1.getString(s1));
                  if (s2.length() > 32767) {
                     return false;
                  }

                  compoundtag2.putString(s1, s2);
               }

               compoundtag.put("filtered_pages", compoundtag2);
            }

            compoundtag.put("pages", listtag1);
            return true;
         }
      } else {
         return false;
      }
   }

   private static String resolvePage(@Nullable CommandSourceStack commandsourcestack, @Nullable Player player, String s) {
      Component component1;
      try {
         component1 = Component.Serializer.fromJsonLenient(s);
         component1 = ComponentUtils.updateForEntity(commandsourcestack, component1, player, 0);
      } catch (Exception var5) {
         component1 = Component.literal(s);
      }

      return Component.Serializer.toJson(component1);
   }

   public boolean isFoil(ItemStack itemstack) {
      return true;
   }
}
