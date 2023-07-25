package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketItem extends Item {
   public static final byte[] CRAFTABLE_DURATIONS = new byte[]{1, 2, 3};
   public static final String TAG_FIREWORKS = "Fireworks";
   public static final String TAG_EXPLOSION = "Explosion";
   public static final String TAG_EXPLOSIONS = "Explosions";
   public static final String TAG_FLIGHT = "Flight";
   public static final String TAG_EXPLOSION_TYPE = "Type";
   public static final String TAG_EXPLOSION_TRAIL = "Trail";
   public static final String TAG_EXPLOSION_FLICKER = "Flicker";
   public static final String TAG_EXPLOSION_COLORS = "Colors";
   public static final String TAG_EXPLOSION_FADECOLORS = "FadeColors";
   public static final double ROCKET_PLACEMENT_OFFSET = 0.15D;

   public FireworkRocketItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      if (!level.isClientSide) {
         ItemStack itemstack = useoncontext.getItemInHand();
         Vec3 vec3 = useoncontext.getClickLocation();
         Direction direction = useoncontext.getClickedFace();
         FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(level, useoncontext.getPlayer(), vec3.x + (double)direction.getStepX() * 0.15D, vec3.y + (double)direction.getStepY() * 0.15D, vec3.z + (double)direction.getStepZ() * 0.15D, itemstack);
         level.addFreshEntity(fireworkrocketentity);
         itemstack.shrink(1);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      if (player.isFallFlying()) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         if (!level.isClientSide) {
            FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(level, itemstack, player);
            level.addFreshEntity(fireworkrocketentity);
            if (!player.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
         }

         return InteractionResultHolder.sidedSuccess(player.getItemInHand(interactionhand), level.isClientSide());
      } else {
         return InteractionResultHolder.pass(player.getItemInHand(interactionhand));
      }
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      CompoundTag compoundtag = itemstack.getTagElement("Fireworks");
      if (compoundtag != null) {
         if (compoundtag.contains("Flight", 99)) {
            list.add(Component.translatable("item.minecraft.firework_rocket.flight").append(CommonComponents.SPACE).append(String.valueOf((int)compoundtag.getByte("Flight"))).withStyle(ChatFormatting.GRAY));
         }

         ListTag listtag = compoundtag.getList("Explosions", 10);
         if (!listtag.isEmpty()) {
            for(int i = 0; i < listtag.size(); ++i) {
               CompoundTag compoundtag1 = listtag.getCompound(i);
               List<Component> list1 = Lists.newArrayList();
               FireworkStarItem.appendHoverText(compoundtag1, list1);
               if (!list1.isEmpty()) {
                  for(int j = 1; j < list1.size(); ++j) {
                     list1.set(j, Component.literal("  ").append(list1.get(j)).withStyle(ChatFormatting.GRAY));
                  }

                  list.addAll(list1);
               }
            }
         }

      }
   }

   public static void setDuration(ItemStack itemstack, byte b0) {
      itemstack.getOrCreateTagElement("Fireworks").putByte("Flight", b0);
   }

   public ItemStack getDefaultInstance() {
      ItemStack itemstack = new ItemStack(this);
      setDuration(itemstack, (byte)1);
      return itemstack;
   }

   public static enum Shape {
      SMALL_BALL(0, "small_ball"),
      LARGE_BALL(1, "large_ball"),
      STAR(2, "star"),
      CREEPER(3, "creeper"),
      BURST(4, "burst");

      private static final IntFunction<FireworkRocketItem.Shape> BY_ID = ByIdMap.continuous(FireworkRocketItem.Shape::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      private final int id;
      private final String name;

      private Shape(int i, String s) {
         this.id = i;
         this.name = s;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public static FireworkRocketItem.Shape byId(int i) {
         return BY_ID.apply(i);
      }
   }
}
