package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CelebrateVillagersSurvivedRaid extends Behavior<Villager> {
   @Nullable
   private Raid currentRaid;

   public CelebrateVillagersSurvivedRaid(int i, int j) {
      super(ImmutableMap.of(), i, j);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      BlockPos blockpos = villager.blockPosition();
      this.currentRaid = serverlevel.getRaidAt(blockpos);
      return this.currentRaid != null && this.currentRaid.isVictory() && MoveToSkySeeingSpot.hasNoBlocksAbove(serverlevel, villager, blockpos);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.currentRaid != null && !this.currentRaid.isStopped();
   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      this.currentRaid = null;
      villager.getBrain().updateActivityFromSchedule(serverlevel.getDayTime(), serverlevel.getGameTime());
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      RandomSource randomsource = villager.getRandom();
      if (randomsource.nextInt(100) == 0) {
         villager.playCelebrateSound();
      }

      if (randomsource.nextInt(200) == 0 && MoveToSkySeeingSpot.hasNoBlocksAbove(serverlevel, villager, villager.blockPosition())) {
         DyeColor dyecolor = Util.getRandom(DyeColor.values(), randomsource);
         int j = randomsource.nextInt(3);
         ItemStack itemstack = this.getFirework(dyecolor, j);
         FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(villager.level(), villager, villager.getX(), villager.getEyeY(), villager.getZ(), itemstack);
         villager.level().addFreshEntity(fireworkrocketentity);
      }

   }

   private ItemStack getFirework(DyeColor dyecolor, int i) {
      ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, 1);
      ItemStack itemstack1 = new ItemStack(Items.FIREWORK_STAR);
      CompoundTag compoundtag = itemstack1.getOrCreateTagElement("Explosion");
      List<Integer> list = Lists.newArrayList();
      list.add(dyecolor.getFireworkColor());
      compoundtag.putIntArray("Colors", list);
      compoundtag.putByte("Type", (byte)FireworkRocketItem.Shape.BURST.getId());
      CompoundTag compoundtag1 = itemstack.getOrCreateTagElement("Fireworks");
      ListTag listtag = new ListTag();
      CompoundTag compoundtag2 = itemstack1.getTagElement("Explosion");
      if (compoundtag2 != null) {
         listtag.add(compoundtag2);
      }

      compoundtag1.putByte("Flight", (byte)i);
      if (!listtag.isEmpty()) {
         compoundtag1.put("Explosions", listtag);
      }

      return itemstack;
   }
}
