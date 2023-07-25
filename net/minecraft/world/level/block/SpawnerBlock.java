package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlock extends BaseEntityBlock {
   protected SpawnerBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SpawnerBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.MOB_SPAWNER, level.isClientSide ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick);
   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (flag) {
         int i = 15 + serverlevel.random.nextInt(15) + serverlevel.random.nextInt(15);
         this.popExperience(serverlevel, blockpos, i);
      }

   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public void appendHoverText(ItemStack itemstack, @Nullable BlockGetter blockgetter, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, blockgetter, list, tooltipflag);
      Optional<Component> optional = this.getSpawnEntityDisplayName(itemstack);
      if (optional.isPresent()) {
         list.add(optional.get());
      } else {
         list.add(CommonComponents.EMPTY);
         list.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
         list.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
      }

   }

   private Optional<Component> getSpawnEntityDisplayName(ItemStack itemstack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag != null && compoundtag.contains("SpawnData", 10)) {
         String s = compoundtag.getCompound("SpawnData").getCompound("entity").getString("id");
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         if (resourcelocation != null) {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation).map((entitytype) -> Component.translatable(entitytype.getDescriptionId()).withStyle(ChatFormatting.GRAY));
         }
      }

      return Optional.empty();
   }
}
