package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable {
   public int time;
   public float flip;
   public float oFlip;
   public float flipT;
   public float flipA;
   public float open;
   public float oOpen;
   public float rot;
   public float oRot;
   public float tRot;
   private static final RandomSource RANDOM = RandomSource.create();
   private Component name;

   public EnchantmentTableBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.ENCHANTING_TABLE, blockpos, blockstate);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (this.hasCustomName()) {
         compoundtag.putString("CustomName", Component.Serializer.toJson(this.name));
      }

   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("CustomName", 8)) {
         this.name = Component.Serializer.fromJson(compoundtag.getString("CustomName"));
      }

   }

   public static void bookAnimationTick(Level level, BlockPos blockpos, BlockState blockstate, EnchantmentTableBlockEntity enchantmenttableblockentity) {
      enchantmenttableblockentity.oOpen = enchantmenttableblockentity.open;
      enchantmenttableblockentity.oRot = enchantmenttableblockentity.rot;
      Player player = level.getNearestPlayer((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, 3.0D, false);
      if (player != null) {
         double d0 = player.getX() - ((double)blockpos.getX() + 0.5D);
         double d1 = player.getZ() - ((double)blockpos.getZ() + 0.5D);
         enchantmenttableblockentity.tRot = (float)Mth.atan2(d1, d0);
         enchantmenttableblockentity.open += 0.1F;
         if (enchantmenttableblockentity.open < 0.5F || RANDOM.nextInt(40) == 0) {
            float f = enchantmenttableblockentity.flipT;

            do {
               enchantmenttableblockentity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
            } while(f == enchantmenttableblockentity.flipT);
         }
      } else {
         enchantmenttableblockentity.tRot += 0.02F;
         enchantmenttableblockentity.open -= 0.1F;
      }

      while(enchantmenttableblockentity.rot >= (float)Math.PI) {
         enchantmenttableblockentity.rot -= ((float)Math.PI * 2F);
      }

      while(enchantmenttableblockentity.rot < -(float)Math.PI) {
         enchantmenttableblockentity.rot += ((float)Math.PI * 2F);
      }

      while(enchantmenttableblockentity.tRot >= (float)Math.PI) {
         enchantmenttableblockentity.tRot -= ((float)Math.PI * 2F);
      }

      while(enchantmenttableblockentity.tRot < -(float)Math.PI) {
         enchantmenttableblockentity.tRot += ((float)Math.PI * 2F);
      }

      float f1;
      for(f1 = enchantmenttableblockentity.tRot - enchantmenttableblockentity.rot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
      }

      while(f1 < -(float)Math.PI) {
         f1 += ((float)Math.PI * 2F);
      }

      enchantmenttableblockentity.rot += f1 * 0.4F;
      enchantmenttableblockentity.open = Mth.clamp(enchantmenttableblockentity.open, 0.0F, 1.0F);
      ++enchantmenttableblockentity.time;
      enchantmenttableblockentity.oFlip = enchantmenttableblockentity.flip;
      float f2 = (enchantmenttableblockentity.flipT - enchantmenttableblockentity.flip) * 0.4F;
      float f3 = 0.2F;
      f2 = Mth.clamp(f2, -0.2F, 0.2F);
      enchantmenttableblockentity.flipA += (f2 - enchantmenttableblockentity.flipA) * 0.9F;
      enchantmenttableblockentity.flip += enchantmenttableblockentity.flipA;
   }

   public Component getName() {
      return (Component)(this.name != null ? this.name : Component.translatable("container.enchant"));
   }

   public void setCustomName(@Nullable Component component) {
      this.name = component;
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }
}
