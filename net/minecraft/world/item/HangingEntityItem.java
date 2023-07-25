package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends Item {
   private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
   private final EntityType<? extends HangingEntity> type;

   public HangingEntityItem(EntityType<? extends HangingEntity> entitytype, Item.Properties item_properties) {
      super(item_properties);
      this.type = entitytype;
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      BlockPos blockpos = useoncontext.getClickedPos();
      Direction direction = useoncontext.getClickedFace();
      BlockPos blockpos1 = blockpos.relative(direction);
      Player player = useoncontext.getPlayer();
      ItemStack itemstack = useoncontext.getItemInHand();
      if (player != null && !this.mayPlace(player, direction, itemstack, blockpos1)) {
         return InteractionResult.FAIL;
      } else {
         Level level = useoncontext.getLevel();
         HangingEntity hangingentity;
         if (this.type == EntityType.PAINTING) {
            Optional<Painting> optional = Painting.create(level, blockpos1, direction);
            if (optional.isEmpty()) {
               return InteractionResult.CONSUME;
            }

            hangingentity = optional.get();
         } else if (this.type == EntityType.ITEM_FRAME) {
            hangingentity = new ItemFrame(level, blockpos1, direction);
         } else {
            if (this.type != EntityType.GLOW_ITEM_FRAME) {
               return InteractionResult.sidedSuccess(level.isClientSide);
            }

            hangingentity = new GlowItemFrame(level, blockpos1, direction);
         }

         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null) {
            EntityType.updateCustomEntityTag(level, player, hangingentity, compoundtag);
         }

         if (hangingentity.survives()) {
            if (!level.isClientSide) {
               hangingentity.playPlacementSound();
               level.gameEvent(player, GameEvent.ENTITY_PLACE, hangingentity.position());
               level.addFreshEntity(hangingentity);
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.CONSUME;
         }
      }
   }

   protected boolean mayPlace(Player player, Direction direction, ItemStack itemstack, BlockPos blockpos) {
      return !direction.getAxis().isVertical() && player.mayUseItemAt(blockpos, direction, itemstack);
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, level, list, tooltipflag);
      if (this.type == EntityType.PAINTING) {
         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null && compoundtag.contains("EntityTag", 10)) {
            CompoundTag compoundtag1 = compoundtag.getCompound("EntityTag");
            Painting.loadVariant(compoundtag1).ifPresentOrElse((holder) -> {
               holder.unwrapKey().ifPresent((resourcekey) -> {
                  list.add(Component.translatable(resourcekey.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
                  list.add(Component.translatable(resourcekey.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
               });
               list.add(Component.translatable("painting.dimensions", Mth.positiveCeilDiv(holder.value().getWidth(), 16), Mth.positiveCeilDiv(holder.value().getHeight(), 16)));
            }, () -> list.add(TOOLTIP_RANDOM_VARIANT));
         } else if (tooltipflag.isCreative()) {
            list.add(TOOLTIP_RANDOM_VARIANT);
         }
      }

   }
}
