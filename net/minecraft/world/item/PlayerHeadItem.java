package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class PlayerHeadItem extends StandingAndWallBlockItem {
   public static final String TAG_SKULL_OWNER = "SkullOwner";

   public PlayerHeadItem(Block block, Block block1, Item.Properties item_properties) {
      super(block, block1, item_properties, Direction.DOWN);
   }

   public Component getName(ItemStack itemstack) {
      if (itemstack.is(Items.PLAYER_HEAD) && itemstack.hasTag()) {
         String s = null;
         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag.contains("SkullOwner", 8)) {
            s = compoundtag.getString("SkullOwner");
         } else if (compoundtag.contains("SkullOwner", 10)) {
            CompoundTag compoundtag1 = compoundtag.getCompound("SkullOwner");
            if (compoundtag1.contains("Name", 8)) {
               s = compoundtag1.getString("Name");
            }
         }

         if (s != null) {
            return Component.translatable(this.getDescriptionId() + ".named", s);
         }
      }

      return super.getName(itemstack);
   }

   public void verifyTagAfterLoad(CompoundTag compoundtag) {
      super.verifyTagAfterLoad(compoundtag);
      if (compoundtag.contains("SkullOwner", 8) && !Util.isBlank(compoundtag.getString("SkullOwner"))) {
         GameProfile gameprofile = new GameProfile((UUID)null, compoundtag.getString("SkullOwner"));
         SkullBlockEntity.updateGameprofile(gameprofile, (gameprofile1) -> compoundtag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameprofile1)));
      }

   }
}
