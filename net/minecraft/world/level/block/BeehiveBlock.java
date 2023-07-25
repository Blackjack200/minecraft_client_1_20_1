package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeehiveBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;
   public static final int MAX_HONEY_LEVELS = 5;
   private static final int SHEARED_HONEYCOMB_COUNT = 3;

   public BeehiveBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HONEY_LEVEL, Integer.valueOf(0)).setValue(FACING, Direction.NORTH));
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return blockstate.getValue(HONEY_LEVEL);
   }

   public void playerDestroy(Level level, Player player, BlockPos blockpos, BlockState blockstate, @Nullable BlockEntity blockentity, ItemStack itemstack) {
      super.playerDestroy(level, player, blockpos, blockstate, blockentity, itemstack);
      if (!level.isClientSide && blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
         if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
            beehiveblockentity.emptyAllLivingFromHive(player, blockstate, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            level.updateNeighbourForOutputSignal(blockpos, this);
            this.angerNearbyBees(level, blockpos);
         }

         CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)player, blockstate, itemstack, beehiveblockentity.getOccupantCount());
      }

   }

   private void angerNearbyBees(Level level, BlockPos blockpos) {
      List<Bee> list = level.getEntitiesOfClass(Bee.class, (new AABB(blockpos)).inflate(8.0D, 6.0D, 8.0D));
      if (!list.isEmpty()) {
         List<Player> list1 = level.getEntitiesOfClass(Player.class, (new AABB(blockpos)).inflate(8.0D, 6.0D, 8.0D));
         int i = list1.size();

         for(Bee bee : list) {
            if (bee.getTarget() == null) {
               bee.setTarget(list1.get(level.random.nextInt(i)));
            }
         }
      }

   }

   public static void dropHoneycomb(Level level, BlockPos blockpos) {
      popResource(level, blockpos, new ItemStack(Items.HONEYCOMB, 3));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      int i = blockstate.getValue(HONEY_LEVEL);
      boolean flag = false;
      if (i >= 5) {
         Item item = itemstack.getItem();
         if (itemstack.is(Items.SHEARS)) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            dropHoneycomb(level, blockpos);
            itemstack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(interactionhand));
            flag = true;
            level.gameEvent(player, GameEvent.SHEAR, blockpos);
         } else if (itemstack.is(Items.GLASS_BOTTLE)) {
            itemstack.shrink(1);
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (itemstack.isEmpty()) {
               player.setItemInHand(interactionhand, new ItemStack(Items.HONEY_BOTTLE));
            } else if (!player.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
               player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
            }

            flag = true;
            level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);
         }

         if (!level.isClientSide() && flag) {
            player.awardStat(Stats.ITEM_USED.get(item));
         }
      }

      if (flag) {
         if (!CampfireBlock.isSmokeyPos(level, blockpos)) {
            if (this.hiveContainsBees(level, blockpos)) {
               this.angerNearbyBees(level, blockpos);
            }

            this.releaseBeesAndResetHoneyLevel(level, blockstate, blockpos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
         } else {
            this.resetHoneyLevel(level, blockstate, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return super.use(blockstate, level, blockpos, player, interactionhand, blockhitresult);
      }
   }

   private boolean hiveContainsBees(Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
         return !beehiveblockentity.isEmpty();
      } else {
         return false;
      }
   }

   public void releaseBeesAndResetHoneyLevel(Level level, BlockState blockstate, BlockPos blockpos, @Nullable Player player, BeehiveBlockEntity.BeeReleaseStatus beehiveblockentity_beereleasestatus) {
      this.resetHoneyLevel(level, blockstate, blockpos);
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
         beehiveblockentity.emptyAllLivingFromHive(player, blockstate, beehiveblockentity_beereleasestatus);
      }

   }

   public void resetHoneyLevel(Level level, BlockState blockstate, BlockPos blockpos) {
      level.setBlock(blockpos, blockstate.setValue(HONEY_LEVEL, Integer.valueOf(0)), 3);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(HONEY_LEVEL) >= 5) {
         for(int i = 0; i < randomsource.nextInt(1) + 1; ++i) {
            this.trySpawnDripParticles(level, blockpos, blockstate);
         }
      }

   }

   private void trySpawnDripParticles(Level level, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.getFluidState().isEmpty() && !(level.random.nextFloat() < 0.3F)) {
         VoxelShape voxelshape = blockstate.getCollisionShape(level, blockpos);
         double d0 = voxelshape.max(Direction.Axis.Y);
         if (d0 >= 1.0D && !blockstate.is(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.min(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(level, blockpos, voxelshape, (double)blockpos.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos1 = blockpos.below();
               BlockState blockstate1 = level.getBlockState(blockpos1);
               VoxelShape voxelshape1 = blockstate1.getCollisionShape(level, blockpos1);
               double d2 = voxelshape1.max(Direction.Axis.Y);
               if ((d2 < 1.0D || !blockstate1.isCollisionShapeFullBlock(level, blockpos1)) && blockstate1.getFluidState().isEmpty()) {
                  this.spawnParticle(level, blockpos, voxelshape, (double)blockpos.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void spawnParticle(Level level, BlockPos blockpos, VoxelShape voxelshape, double d0) {
      this.spawnFluidParticle(level, (double)blockpos.getX() + voxelshape.min(Direction.Axis.X), (double)blockpos.getX() + voxelshape.max(Direction.Axis.X), (double)blockpos.getZ() + voxelshape.min(Direction.Axis.Z), (double)blockpos.getZ() + voxelshape.max(Direction.Axis.Z), d0);
   }

   private void spawnFluidParticle(Level level, double d0, double d1, double d2, double d3, double d4) {
      level.addParticle(ParticleTypes.DRIPPING_HONEY, Mth.lerp(level.random.nextDouble(), d0, d1), d4, Mth.lerp(level.random.nextDouble(), d2, d3), 0.0D, 0.0D, 0.0D);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HONEY_LEVEL, FACING);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new BeehiveBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return level.isClientSide ? null : createTickerHelper(blockentitytype, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide && player.isCreative() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
            ItemStack itemstack = new ItemStack(this);
            int i = blockstate.getValue(HONEY_LEVEL);
            boolean flag = !beehiveblockentity.isEmpty();
            if (flag || i > 0) {
               if (flag) {
                  CompoundTag compoundtag = new CompoundTag();
                  compoundtag.put("Bees", beehiveblockentity.writeBees());
                  BlockItem.setBlockEntityData(itemstack, BlockEntityType.BEEHIVE, compoundtag);
               }

               CompoundTag compoundtag1 = new CompoundTag();
               compoundtag1.putInt("honey_level", i);
               itemstack.addTagElement("BlockStateTag", compoundtag1);
               ItemEntity itementity = new ItemEntity(level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack);
               itementity.setDefaultPickUpDelay();
               level.addFreshEntity(itementity);
            }
         }
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public List<ItemStack> getDrops(BlockState blockstate, LootParams.Builder lootparams_builder) {
      Entity entity = lootparams_builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
      if (entity instanceof PrimedTnt || entity instanceof Creeper || entity instanceof WitherSkull || entity instanceof WitherBoss || entity instanceof MinecartTNT) {
         BlockEntity blockentity = lootparams_builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         if (blockentity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
            beehiveblockentity.emptyAllLivingFromHive((Player)null, blockstate, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
         }
      }

      return super.getDrops(blockstate, lootparams_builder);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (levelaccessor.getBlockState(blockpos1).getBlock() instanceof FireBlock) {
         BlockEntity blockentity = levelaccessor.getBlockEntity(blockpos);
         if (blockentity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
            beehiveblockentity.emptyAllLivingFromHive((Player)null, blockstate, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
         }
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }
}
