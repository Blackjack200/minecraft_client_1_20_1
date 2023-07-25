package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class Block extends BlockBehaviour implements ItemLike {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Holder.Reference<Block> builtInRegistryHolder = BuiltInRegistries.BLOCK.createIntrusiveHolder(this);
   public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
   private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>() {
      public Boolean load(VoxelShape voxelshape) {
         return !Shapes.joinIsNotEmpty(Shapes.block(), voxelshape, BooleanOp.NOT_SAME);
      }
   });
   public static final int UPDATE_NEIGHBORS = 1;
   public static final int UPDATE_CLIENTS = 2;
   public static final int UPDATE_INVISIBLE = 4;
   public static final int UPDATE_IMMEDIATE = 8;
   public static final int UPDATE_KNOWN_SHAPE = 16;
   public static final int UPDATE_SUPPRESS_DROPS = 32;
   public static final int UPDATE_MOVE_BY_PISTON = 64;
   public static final int UPDATE_NONE = 4;
   public static final int UPDATE_ALL = 3;
   public static final int UPDATE_ALL_IMMEDIATE = 11;
   public static final float INDESTRUCTIBLE = -1.0F;
   public static final float INSTANT = 0.0F;
   public static final int UPDATE_LIMIT = 512;
   protected final StateDefinition<Block, BlockState> stateDefinition;
   private BlockState defaultBlockState;
   @Nullable
   private String descriptionId;
   @Nullable
   private Item item;
   private static final int CACHE_SIZE = 2048;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(2048, 0.25F) {
         protected void rehash(int i) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });

   public static int getId(@Nullable BlockState blockstate) {
      if (blockstate == null) {
         return 0;
      } else {
         int i = BLOCK_STATE_REGISTRY.getId(blockstate);
         return i == -1 ? 0 : i;
      }
   }

   public static BlockState stateById(int i) {
      BlockState blockstate = BLOCK_STATE_REGISTRY.byId(i);
      return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
   }

   public static Block byItem(@Nullable Item item) {
      return item instanceof BlockItem ? ((BlockItem)item).getBlock() : Blocks.AIR;
   }

   public static BlockState pushEntitiesUp(BlockState blockstate, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos) {
      VoxelShape voxelshape = Shapes.joinUnoptimized(blockstate.getCollisionShape(levelaccessor, blockpos), blockstate1.getCollisionShape(levelaccessor, blockpos), BooleanOp.ONLY_SECOND).move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
      if (voxelshape.isEmpty()) {
         return blockstate1;
      } else {
         for(Entity entity : levelaccessor.getEntities((Entity)null, voxelshape.bounds())) {
            double d0 = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0D, 1.0D, 0.0D), List.of(voxelshape), -1.0D);
            entity.teleportRelative(0.0D, 1.0D + d0, 0.0D);
         }

         return blockstate1;
      }
   }

   public static VoxelShape box(double d0, double d1, double d2, double d3, double d4, double d5) {
      return Shapes.box(d0 / 16.0D, d1 / 16.0D, d2 / 16.0D, d3 / 16.0D, d4 / 16.0D, d5 / 16.0D);
   }

   public static BlockState updateFromNeighbourShapes(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate1 = blockstate;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : UPDATE_SHAPE_ORDER) {
         blockpos_mutableblockpos.setWithOffset(blockpos, direction);
         blockstate1 = blockstate1.updateShape(direction, levelaccessor.getBlockState(blockpos_mutableblockpos), levelaccessor, blockpos, blockpos_mutableblockpos);
      }

      return blockstate1;
   }

   public static void updateOrDestroy(BlockState blockstate, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, int i) {
      updateOrDestroy(blockstate, blockstate1, levelaccessor, blockpos, i, 512);
   }

   public static void updateOrDestroy(BlockState blockstate, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, int i, int j) {
      if (blockstate1 != blockstate) {
         if (blockstate1.isAir()) {
            if (!levelaccessor.isClientSide()) {
               levelaccessor.destroyBlock(blockpos, (i & 32) == 0, (Entity)null, j);
            }
         } else {
            levelaccessor.setBlock(blockpos, blockstate1, i & -33, j);
         }
      }

   }

   public Block(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      StateDefinition.Builder<Block, BlockState> statedefinition_builder = new StateDefinition.Builder<>(this);
      this.createBlockStateDefinition(statedefinition_builder);
      this.stateDefinition = statedefinition_builder.create(Block::defaultBlockState, BlockState::new);
      this.registerDefaultState(this.stateDefinition.any());
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         String s = this.getClass().getSimpleName();
         if (!s.endsWith("Block")) {
            LOGGER.error("Block classes should end with Block and {} doesn't.", (Object)s);
         }
      }

   }

   public static boolean isExceptionForConnection(BlockState blockstate) {
      return blockstate.getBlock() instanceof LeavesBlock || blockstate.is(Blocks.BARRIER) || blockstate.is(Blocks.CARVED_PUMPKIN) || blockstate.is(Blocks.JACK_O_LANTERN) || blockstate.is(Blocks.MELON) || blockstate.is(Blocks.PUMPKIN) || blockstate.is(BlockTags.SHULKER_BOXES);
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return this.isRandomlyTicking;
   }

   public static boolean shouldRenderFace(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction, BlockPos blockpos1) {
      BlockState blockstate1 = blockgetter.getBlockState(blockpos1);
      if (blockstate.skipRendering(blockstate1, direction)) {
         return false;
      } else if (blockstate1.canOcclude()) {
         Block.BlockStatePairKey block_blockstatepairkey = new Block.BlockStatePairKey(blockstate, blockstate1, direction);
         Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block_blockstatepairkey);
         if (b0 != 127) {
            return b0 != 0;
         } else {
            VoxelShape voxelshape = blockstate.getFaceOcclusionShape(blockgetter, blockpos, direction);
            if (voxelshape.isEmpty()) {
               return true;
            } else {
               VoxelShape voxelshape1 = blockstate1.getFaceOcclusionShape(blockgetter, blockpos1, direction.getOpposite());
               boolean flag = Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);
               if (object2bytelinkedopenhashmap.size() == 2048) {
                  object2bytelinkedopenhashmap.removeLastByte();
               }

               object2bytelinkedopenhashmap.putAndMoveToFirst(block_blockstatepairkey, (byte)(flag ? 1 : 0));
               return flag;
            }
         }
      } else {
         return true;
      }
   }

   public static boolean canSupportRigidBlock(BlockGetter blockgetter, BlockPos blockpos) {
      return blockgetter.getBlockState(blockpos).isFaceSturdy(blockgetter, blockpos, Direction.UP, SupportType.RIGID);
   }

   public static boolean canSupportCenter(LevelReader levelreader, BlockPos blockpos, Direction direction) {
      BlockState blockstate = levelreader.getBlockState(blockpos);
      return direction == Direction.DOWN && blockstate.is(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : blockstate.isFaceSturdy(levelreader, blockpos, direction, SupportType.CENTER);
   }

   public static boolean isFaceFull(VoxelShape voxelshape, Direction direction) {
      VoxelShape voxelshape1 = voxelshape.getFaceShape(direction);
      return isShapeFullBlock(voxelshape1);
   }

   public static boolean isShapeFullBlock(VoxelShape voxelshape) {
      return SHAPE_FULL_BLOCK_CACHE.getUnchecked(voxelshape);
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return !isShapeFullBlock(blockstate.getShape(blockgetter, blockpos)) && blockstate.getFluidState().isEmpty();
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
   }

   public void destroy(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
   }

   public static List<ItemStack> getDrops(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, @Nullable BlockEntity blockentity) {
      LootParams.Builder lootparams_builder = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity);
      return blockstate.getDrops(lootparams_builder);
   }

   public static List<ItemStack> getDrops(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, @Nullable BlockEntity blockentity, @Nullable Entity entity, ItemStack itemstack) {
      LootParams.Builder lootparams_builder = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, itemstack).withOptionalParameter(LootContextParams.THIS_ENTITY, entity).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity);
      return blockstate.getDrops(lootparams_builder);
   }

   public static void dropResources(BlockState blockstate, Level level, BlockPos blockpos) {
      if (level instanceof ServerLevel) {
         getDrops(blockstate, (ServerLevel)level, blockpos, (BlockEntity)null).forEach((itemstack) -> popResource(level, blockpos, itemstack));
         blockstate.spawnAfterBreak((ServerLevel)level, blockpos, ItemStack.EMPTY, true);
      }

   }

   public static void dropResources(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, @Nullable BlockEntity blockentity) {
      if (levelaccessor instanceof ServerLevel) {
         getDrops(blockstate, (ServerLevel)levelaccessor, blockpos, blockentity).forEach((itemstack) -> popResource((ServerLevel)levelaccessor, blockpos, itemstack));
         blockstate.spawnAfterBreak((ServerLevel)levelaccessor, blockpos, ItemStack.EMPTY, true);
      }

   }

   public static void dropResources(BlockState blockstate, Level level, BlockPos blockpos, @Nullable BlockEntity blockentity, @Nullable Entity entity, ItemStack itemstack) {
      if (level instanceof ServerLevel) {
         getDrops(blockstate, (ServerLevel)level, blockpos, blockentity, entity, itemstack).forEach((itemstack1) -> popResource(level, blockpos, itemstack1));
         blockstate.spawnAfterBreak((ServerLevel)level, blockpos, itemstack, true);
      }

   }

   public static void popResource(Level level, BlockPos blockpos, ItemStack itemstack) {
      double d0 = (double)EntityType.ITEM.getHeight() / 2.0D;
      double d1 = (double)blockpos.getX() + 0.5D + Mth.nextDouble(level.random, -0.25D, 0.25D);
      double d2 = (double)blockpos.getY() + 0.5D + Mth.nextDouble(level.random, -0.25D, 0.25D) - d0;
      double d3 = (double)blockpos.getZ() + 0.5D + Mth.nextDouble(level.random, -0.25D, 0.25D);
      popResource(level, () -> new ItemEntity(level, d1, d2, d3, itemstack), itemstack);
   }

   public static void popResourceFromFace(Level level, BlockPos blockpos, Direction direction, ItemStack itemstack) {
      int i = direction.getStepX();
      int j = direction.getStepY();
      int k = direction.getStepZ();
      double d0 = (double)EntityType.ITEM.getWidth() / 2.0D;
      double d1 = (double)EntityType.ITEM.getHeight() / 2.0D;
      double d2 = (double)blockpos.getX() + 0.5D + (i == 0 ? Mth.nextDouble(level.random, -0.25D, 0.25D) : (double)i * (0.5D + d0));
      double d3 = (double)blockpos.getY() + 0.5D + (j == 0 ? Mth.nextDouble(level.random, -0.25D, 0.25D) : (double)j * (0.5D + d1)) - d1;
      double d4 = (double)blockpos.getZ() + 0.5D + (k == 0 ? Mth.nextDouble(level.random, -0.25D, 0.25D) : (double)k * (0.5D + d0));
      double d5 = i == 0 ? Mth.nextDouble(level.random, -0.1D, 0.1D) : (double)i * 0.1D;
      double d6 = j == 0 ? Mth.nextDouble(level.random, 0.0D, 0.1D) : (double)j * 0.1D + 0.1D;
      double d7 = k == 0 ? Mth.nextDouble(level.random, -0.1D, 0.1D) : (double)k * 0.1D;
      popResource(level, () -> new ItemEntity(level, d2, d3, d4, itemstack, d5, d6, d7), itemstack);
   }

   private static void popResource(Level level, Supplier<ItemEntity> supplier, ItemStack itemstack) {
      if (!level.isClientSide && !itemstack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         ItemEntity itementity = supplier.get();
         itementity.setDefaultPickUpDelay();
         level.addFreshEntity(itementity);
      }
   }

   protected void popExperience(ServerLevel serverlevel, BlockPos blockpos, int i) {
      if (serverlevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         ExperienceOrb.award(serverlevel, Vec3.atCenterOf(blockpos), i);
      }

   }

   public float getExplosionResistance() {
      return this.explosionResistance;
   }

   public void wasExploded(Level level, BlockPos blockpos, Explosion explosion) {
   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState();
   }

   public void playerDestroy(Level level, Player player, BlockPos blockpos, BlockState blockstate, @Nullable BlockEntity blockentity, ItemStack itemstack) {
      player.awardStat(Stats.BLOCK_MINED.get(this));
      player.causeFoodExhaustion(0.005F);
      dropResources(blockstate, level, blockpos, blockentity, player, itemstack);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
   }

   public boolean isPossibleToRespawnInThis(BlockState blockstate) {
      return !blockstate.isSolid() && !blockstate.liquid();
   }

   public MutableComponent getName() {
      return Component.translatable(this.getDescriptionId());
   }

   public String getDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("block", BuiltInRegistries.BLOCK.getKey(this));
      }

      return this.descriptionId;
   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      entity.causeFallDamage(f, 1.0F, entity.damageSources().fall());
   }

   public void updateEntityAfterFallOn(BlockGetter blockgetter, Entity entity) {
      entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(this);
   }

   public float getFriction() {
      return this.friction;
   }

   public float getSpeedFactor() {
      return this.speedFactor;
   }

   public float getJumpFactor() {
      return this.jumpFactor;
   }

   protected void spawnDestroyParticles(Level level, Player player, BlockPos blockpos, BlockState blockstate) {
      level.levelEvent(player, 2001, blockpos, getId(blockstate));
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      this.spawnDestroyParticles(level, player, blockpos, blockstate);
      if (blockstate.is(BlockTags.GUARDED_BY_PIGLINS)) {
         PiglinAi.angerNearbyPiglins(player, false);
      }

      level.gameEvent(GameEvent.BLOCK_DESTROY, blockpos, GameEvent.Context.of(player, blockstate));
   }

   public void handlePrecipitation(BlockState blockstate, Level level, BlockPos blockpos, Biome.Precipitation biome_precipitation) {
   }

   public boolean dropFromExplosion(Explosion explosion) {
      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
   }

   public StateDefinition<Block, BlockState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(BlockState blockstate) {
      this.defaultBlockState = blockstate;
   }

   public final BlockState defaultBlockState() {
      return this.defaultBlockState;
   }

   public final BlockState withPropertiesOf(BlockState blockstate) {
      BlockState blockstate1 = this.defaultBlockState();

      for(Property<?> property : blockstate.getBlock().getStateDefinition().getProperties()) {
         if (blockstate1.hasProperty(property)) {
            blockstate1 = copyProperty(blockstate, blockstate1, property);
         }
      }

      return blockstate1;
   }

   private static <T extends Comparable<T>> BlockState copyProperty(BlockState blockstate, BlockState blockstate1, Property<T> property) {
      return blockstate1.setValue(property, blockstate.getValue(property));
   }

   public SoundType getSoundType(BlockState blockstate) {
      return this.soundType;
   }

   public Item asItem() {
      if (this.item == null) {
         this.item = Item.byBlock(this);
      }

      return this.item;
   }

   public boolean hasDynamicShape() {
      return this.dynamicShape;
   }

   public String toString() {
      return "Block{" + BuiltInRegistries.BLOCK.getKey(this) + "}";
   }

   public void appendHoverText(ItemStack itemstack, @Nullable BlockGetter blockgetter, List<Component> list, TooltipFlag tooltipflag) {
   }

   protected Block asBlock() {
      return this;
   }

   protected ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> function) {
      return this.stateDefinition.getPossibleStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), function));
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Block> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   protected void tryDropExperience(ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, IntProvider intprovider) {
      if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
         int i = intprovider.sample(serverlevel.random);
         if (i > 0) {
            this.popExperience(serverlevel, blockpos, i);
         }
      }

   }

   public static final class BlockStatePairKey {
      private final BlockState first;
      private final BlockState second;
      private final Direction direction;

      public BlockStatePairKey(BlockState blockstate, BlockState blockstate1, Direction direction) {
         this.first = blockstate;
         this.second = blockstate1;
         this.direction = direction;
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (!(object instanceof Block.BlockStatePairKey)) {
            return false;
         } else {
            Block.BlockStatePairKey block_blockstatepairkey = (Block.BlockStatePairKey)object;
            return this.first == block_blockstatepairkey.first && this.second == block_blockstatepairkey.second && this.direction == block_blockstatepairkey.direction;
         }
      }

      public int hashCode() {
         int i = this.first.hashCode();
         i = 31 * i + this.second.hashCode();
         return 31 * i + this.direction.hashCode();
      }
   }
}
