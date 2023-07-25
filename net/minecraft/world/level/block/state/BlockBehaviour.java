package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour implements FeatureElement {
   protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
   protected final boolean hasCollision;
   protected final float explosionResistance;
   protected final boolean isRandomlyTicking;
   protected final SoundType soundType;
   protected final float friction;
   protected final float speedFactor;
   protected final float jumpFactor;
   protected final boolean dynamicShape;
   protected final FeatureFlagSet requiredFeatures;
   protected final BlockBehaviour.Properties properties;
   @Nullable
   protected ResourceLocation drops;

   public BlockBehaviour(BlockBehaviour.Properties blockbehaviour_properties) {
      this.hasCollision = blockbehaviour_properties.hasCollision;
      this.drops = blockbehaviour_properties.drops;
      this.explosionResistance = blockbehaviour_properties.explosionResistance;
      this.isRandomlyTicking = blockbehaviour_properties.isRandomlyTicking;
      this.soundType = blockbehaviour_properties.soundType;
      this.friction = blockbehaviour_properties.friction;
      this.speedFactor = blockbehaviour_properties.speedFactor;
      this.jumpFactor = blockbehaviour_properties.jumpFactor;
      this.dynamicShape = blockbehaviour_properties.dynamicShape;
      this.requiredFeatures = blockbehaviour_properties.requiredFeatures;
      this.properties = blockbehaviour_properties;
   }

   /** @deprecated */
   @Deprecated
   public void updateIndirectNeighbourShapes(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, int i, int j) {
   }

   /** @deprecated */
   @Deprecated
   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      switch (pathcomputationtype) {
         case LAND:
            return !blockstate.isCollisionShapeFullBlock(blockgetter, blockpos);
         case WATER:
            return blockgetter.getFluidState(blockpos).is(FluidTags.WATER);
         case AIR:
            return !blockstate.isCollisionShapeFullBlock(blockgetter, blockpos);
         default:
            return false;
      }
   }

   /** @deprecated */
   @Deprecated
   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return blockstate;
   }

   /** @deprecated */
   @Deprecated
   public boolean skipRendering(BlockState blockstate, BlockState blockstate1, Direction direction) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      DebugPackets.sendNeighborsUpdatePacket(level, blockpos);
   }

   /** @deprecated */
   @Deprecated
   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
   }

   /** @deprecated */
   @Deprecated
   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (blockstate.hasBlockEntity() && !blockstate.is(blockstate1.getBlock())) {
         level.removeBlockEntity(blockpos);
      }

   }

   /** @deprecated */
   @Deprecated
   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      return InteractionResult.PASS;
   }

   /** @deprecated */
   @Deprecated
   public boolean triggerEvent(BlockState blockstate, Level level, BlockPos blockpos, int i, int j) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   /** @deprecated */
   @Deprecated
   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean isSignalSource(BlockState blockstate) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public FluidState getFluidState(BlockState blockstate) {
      return Fluids.EMPTY.defaultFluidState();
   }

   /** @deprecated */
   @Deprecated
   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return false;
   }

   public float getMaxHorizontalOffset() {
      return 0.25F;
   }

   public float getMaxVerticalOffset() {
      return 0.2F;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   /** @deprecated */
   @Deprecated
   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate;
   }

   /** @deprecated */
   @Deprecated
   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate;
   }

   /** @deprecated */
   @Deprecated
   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return blockstate.canBeReplaced() && (blockplacecontext.getItemInHand().isEmpty() || !blockplacecontext.getItemInHand().is(this.asItem()));
   }

   /** @deprecated */
   @Deprecated
   public boolean canBeReplaced(BlockState blockstate, Fluid fluid) {
      return blockstate.canBeReplaced() || !blockstate.isSolid();
   }

   /** @deprecated */
   @Deprecated
   public List<ItemStack> getDrops(BlockState blockstate, LootParams.Builder lootparams_builder) {
      ResourceLocation resourcelocation = this.getLootTable();
      if (resourcelocation == BuiltInLootTables.EMPTY) {
         return Collections.emptyList();
      } else {
         LootParams lootparams = lootparams_builder.withParameter(LootContextParams.BLOCK_STATE, blockstate).create(LootContextParamSets.BLOCK);
         ServerLevel serverlevel = lootparams.getLevel();
         LootTable loottable = serverlevel.getServer().getLootData().getLootTable(resourcelocation);
         return loottable.getRandomItems(lootparams);
      }
   }

   /** @deprecated */
   @Deprecated
   public long getSeed(BlockState blockstate, BlockPos blockpos) {
      return Mth.getSeed(blockpos);
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getOcclusionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.getShape(blockgetter, blockpos);
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getBlockSupportShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return this.getCollisionShape(blockstate, blockgetter, blockpos, CollisionContext.empty());
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getInteractionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Shapes.empty();
   }

   /** @deprecated */
   @Deprecated
   public int getLightBlock(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      if (blockstate.isSolidRender(blockgetter, blockpos)) {
         return blockgetter.getMaxLightLevel();
      } else {
         return blockstate.propagatesSkylightDown(blockgetter, blockpos) ? 0 : 1;
      }
   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      return null;
   }

   /** @deprecated */
   @Deprecated
   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public float getShadeBrightness(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.isCollisionShapeFullBlock(blockgetter, blockpos) ? 0.2F : 1.0F;
   }

   /** @deprecated */
   @Deprecated
   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return 0;
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return Shapes.block();
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.hasCollision ? blockstate.getShape(blockgetter, blockpos) : Shapes.empty();
   }

   /** @deprecated */
   @Deprecated
   public boolean isCollisionShapeFullBlock(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Block.isShapeFullBlock(blockstate.getCollisionShape(blockgetter, blockpos));
   }

   /** @deprecated */
   @Deprecated
   public boolean isOcclusionShapeFullBlock(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Block.isShapeFullBlock(blockstate.getOcclusionShape(blockgetter, blockpos));
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getVisualShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.getCollisionShape(blockstate, blockgetter, blockpos, collisioncontext);
   }

   /** @deprecated */
   @Deprecated
   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.tick(blockstate, serverlevel, blockpos, randomsource);
   }

   /** @deprecated */
   @Deprecated
   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
   }

   /** @deprecated */
   @Deprecated
   public float getDestroyProgress(BlockState blockstate, Player player, BlockGetter blockgetter, BlockPos blockpos) {
      float f = blockstate.getDestroySpeed(blockgetter, blockpos);
      if (f == -1.0F) {
         return 0.0F;
      } else {
         int i = player.hasCorrectToolForDrops(blockstate) ? 30 : 100;
         return player.getDestroySpeed(blockstate) / f / (float)i;
      }
   }

   /** @deprecated */
   @Deprecated
   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
   }

   /** @deprecated */
   @Deprecated
   public void attack(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
   }

   /** @deprecated */
   @Deprecated
   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return 0;
   }

   /** @deprecated */
   @Deprecated
   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
   }

   /** @deprecated */
   @Deprecated
   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return 0;
   }

   public final ResourceLocation getLootTable() {
      if (this.drops == null) {
         ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(this.asBlock());
         this.drops = resourcelocation.withPrefix("blocks/");
      }

      return this.drops;
   }

   /** @deprecated */
   @Deprecated
   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
   }

   public abstract Item asItem();

   protected abstract Block asBlock();

   public MapColor defaultMapColor() {
      return this.properties.mapColor.apply(this.asBlock().defaultBlockState());
   }

   public float defaultDestroyTime() {
      return this.properties.destroyTime;
   }

   public abstract static class BlockStateBase extends StateHolder<Block, BlockState> {
      private final int lightEmission;
      private final boolean useShapeForLightOcclusion;
      private final boolean isAir;
      private final boolean ignitedByLava;
      /** @deprecated */
      @Deprecated
      private final boolean liquid;
      /** @deprecated */
      @Deprecated
      private boolean legacySolid;
      private final PushReaction pushReaction;
      private final MapColor mapColor;
      private final float destroySpeed;
      private final boolean requiresCorrectToolForDrops;
      private final boolean canOcclude;
      private final BlockBehaviour.StatePredicate isRedstoneConductor;
      private final BlockBehaviour.StatePredicate isSuffocating;
      private final BlockBehaviour.StatePredicate isViewBlocking;
      private final BlockBehaviour.StatePredicate hasPostProcess;
      private final BlockBehaviour.StatePredicate emissiveRendering;
      private final Optional<BlockBehaviour.OffsetFunction> offsetFunction;
      private final boolean spawnParticlesOnBreak;
      private final NoteBlockInstrument instrument;
      private final boolean replaceable;
      @Nullable
      protected BlockBehaviour.BlockStateBase.Cache cache;
      private FluidState fluidState = Fluids.EMPTY.defaultFluidState();
      private boolean isRandomlyTicking;

      protected BlockStateBase(Block block, ImmutableMap<Property<?>, Comparable<?>> immutablemap, MapCodec<BlockState> mapcodec) {
         super(block, immutablemap, mapcodec);
         BlockBehaviour.Properties blockbehaviour_properties = block.properties;
         this.lightEmission = blockbehaviour_properties.lightEmission.applyAsInt(this.asState());
         this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(this.asState());
         this.isAir = blockbehaviour_properties.isAir;
         this.ignitedByLava = blockbehaviour_properties.ignitedByLava;
         this.liquid = blockbehaviour_properties.liquid;
         this.pushReaction = blockbehaviour_properties.pushReaction;
         this.mapColor = blockbehaviour_properties.mapColor.apply(this.asState());
         this.destroySpeed = blockbehaviour_properties.destroyTime;
         this.requiresCorrectToolForDrops = blockbehaviour_properties.requiresCorrectToolForDrops;
         this.canOcclude = blockbehaviour_properties.canOcclude;
         this.isRedstoneConductor = blockbehaviour_properties.isRedstoneConductor;
         this.isSuffocating = blockbehaviour_properties.isSuffocating;
         this.isViewBlocking = blockbehaviour_properties.isViewBlocking;
         this.hasPostProcess = blockbehaviour_properties.hasPostProcess;
         this.emissiveRendering = blockbehaviour_properties.emissiveRendering;
         this.offsetFunction = blockbehaviour_properties.offsetFunction;
         this.spawnParticlesOnBreak = blockbehaviour_properties.spawnParticlesOnBreak;
         this.instrument = blockbehaviour_properties.instrument;
         this.replaceable = blockbehaviour_properties.replaceable;
      }

      private boolean calculateSolid() {
         if ((this.owner).properties.forceSolidOn) {
            return true;
         } else if ((this.owner).properties.forceSolidOff) {
            return false;
         } else if (this.cache == null) {
            return false;
         } else {
            VoxelShape voxelshape = this.cache.collisionShape;
            if (voxelshape.isEmpty()) {
               return false;
            } else {
               AABB aabb = voxelshape.bounds();
               if (aabb.getSize() >= 0.7291666666666666D) {
                  return true;
               } else {
                  return aabb.getYsize() >= 1.0D;
               }
            }
         }
      }

      public void initCache() {
         this.fluidState = this.owner.getFluidState(this.asState());
         this.isRandomlyTicking = this.owner.isRandomlyTicking(this.asState());
         if (!this.getBlock().hasDynamicShape()) {
            this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
         }

         this.legacySolid = this.calculateSolid();
      }

      public Block getBlock() {
         return this.owner;
      }

      public Holder<Block> getBlockHolder() {
         return this.owner.builtInRegistryHolder();
      }

      /** @deprecated */
      @Deprecated
      public boolean blocksMotion() {
         Block block = this.getBlock();
         return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && this.isSolid();
      }

      /** @deprecated */
      @Deprecated
      public boolean isSolid() {
         return this.legacySolid;
      }

      public boolean isValidSpawn(BlockGetter blockgetter, BlockPos blockpos, EntityType<?> entitytype) {
         return this.getBlock().properties.isValidSpawn.test(this.asState(), blockgetter, blockpos, entitytype);
      }

      public boolean propagatesSkylightDown(BlockGetter blockgetter, BlockPos blockpos) {
         return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this.asState(), blockgetter, blockpos);
      }

      public int getLightBlock(BlockGetter blockgetter, BlockPos blockpos) {
         return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this.asState(), blockgetter, blockpos);
      }

      public VoxelShape getFaceOcclusionShape(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return this.cache != null && this.cache.occlusionShapes != null ? this.cache.occlusionShapes[direction.ordinal()] : Shapes.getFaceShape(this.getOcclusionShape(blockgetter, blockpos), direction);
      }

      public VoxelShape getOcclusionShape(BlockGetter blockgetter, BlockPos blockpos) {
         return this.getBlock().getOcclusionShape(this.asState(), blockgetter, blockpos);
      }

      public boolean hasLargeCollisionShape() {
         return this.cache == null || this.cache.largeCollisionShape;
      }

      public boolean useShapeForLightOcclusion() {
         return this.useShapeForLightOcclusion;
      }

      public int getLightEmission() {
         return this.lightEmission;
      }

      public boolean isAir() {
         return this.isAir;
      }

      public boolean ignitedByLava() {
         return this.ignitedByLava;
      }

      /** @deprecated */
      @Deprecated
      public boolean liquid() {
         return this.liquid;
      }

      public MapColor getMapColor(BlockGetter blockgetter, BlockPos blockpos) {
         return this.mapColor;
      }

      public BlockState rotate(Rotation rotation) {
         return this.getBlock().rotate(this.asState(), rotation);
      }

      public BlockState mirror(Mirror mirror) {
         return this.getBlock().mirror(this.asState(), mirror);
      }

      public RenderShape getRenderShape() {
         return this.getBlock().getRenderShape(this.asState());
      }

      public boolean emissiveRendering(BlockGetter blockgetter, BlockPos blockpos) {
         return this.emissiveRendering.test(this.asState(), blockgetter, blockpos);
      }

      public float getShadeBrightness(BlockGetter blockgetter, BlockPos blockpos) {
         return this.getBlock().getShadeBrightness(this.asState(), blockgetter, blockpos);
      }

      public boolean isRedstoneConductor(BlockGetter blockgetter, BlockPos blockpos) {
         return this.isRedstoneConductor.test(this.asState(), blockgetter, blockpos);
      }

      public boolean isSignalSource() {
         return this.getBlock().isSignalSource(this.asState());
      }

      public int getSignal(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return this.getBlock().getSignal(this.asState(), blockgetter, blockpos, direction);
      }

      public boolean hasAnalogOutputSignal() {
         return this.getBlock().hasAnalogOutputSignal(this.asState());
      }

      public int getAnalogOutputSignal(Level level, BlockPos blockpos) {
         return this.getBlock().getAnalogOutputSignal(this.asState(), level, blockpos);
      }

      public float getDestroySpeed(BlockGetter blockgetter, BlockPos blockpos) {
         return this.destroySpeed;
      }

      public float getDestroyProgress(Player player, BlockGetter blockgetter, BlockPos blockpos) {
         return this.getBlock().getDestroyProgress(this.asState(), player, blockgetter, blockpos);
      }

      public int getDirectSignal(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return this.getBlock().getDirectSignal(this.asState(), blockgetter, blockpos, direction);
      }

      public PushReaction getPistonPushReaction() {
         return this.pushReaction;
      }

      public boolean isSolidRender(BlockGetter blockgetter, BlockPos blockpos) {
         if (this.cache != null) {
            return this.cache.solidRender;
         } else {
            BlockState blockstate = this.asState();
            return blockstate.canOcclude() ? Block.isShapeFullBlock(blockstate.getOcclusionShape(blockgetter, blockpos)) : false;
         }
      }

      public boolean canOcclude() {
         return this.canOcclude;
      }

      public boolean skipRendering(BlockState blockstate, Direction direction) {
         return this.getBlock().skipRendering(this.asState(), blockstate, direction);
      }

      public VoxelShape getShape(BlockGetter blockgetter, BlockPos blockpos) {
         return this.getShape(blockgetter, blockpos, CollisionContext.empty());
      }

      public VoxelShape getShape(BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
         return this.getBlock().getShape(this.asState(), blockgetter, blockpos, collisioncontext);
      }

      public VoxelShape getCollisionShape(BlockGetter blockgetter, BlockPos blockpos) {
         return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(blockgetter, blockpos, CollisionContext.empty());
      }

      public VoxelShape getCollisionShape(BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
         return this.getBlock().getCollisionShape(this.asState(), blockgetter, blockpos, collisioncontext);
      }

      public VoxelShape getBlockSupportShape(BlockGetter blockgetter, BlockPos blockpos) {
         return this.getBlock().getBlockSupportShape(this.asState(), blockgetter, blockpos);
      }

      public VoxelShape getVisualShape(BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
         return this.getBlock().getVisualShape(this.asState(), blockgetter, blockpos, collisioncontext);
      }

      public VoxelShape getInteractionShape(BlockGetter blockgetter, BlockPos blockpos) {
         return this.getBlock().getInteractionShape(this.asState(), blockgetter, blockpos);
      }

      public final boolean entityCanStandOn(BlockGetter blockgetter, BlockPos blockpos, Entity entity) {
         return this.entityCanStandOnFace(blockgetter, blockpos, entity, Direction.UP);
      }

      public final boolean entityCanStandOnFace(BlockGetter blockgetter, BlockPos blockpos, Entity entity, Direction direction) {
         return Block.isFaceFull(this.getCollisionShape(blockgetter, blockpos, CollisionContext.of(entity)), direction);
      }

      public Vec3 getOffset(BlockGetter blockgetter, BlockPos blockpos) {
         return this.offsetFunction.map((blockbehaviour_offsetfunction) -> blockbehaviour_offsetfunction.evaluate(this.asState(), blockgetter, blockpos)).orElse(Vec3.ZERO);
      }

      public boolean hasOffsetFunction() {
         return !this.offsetFunction.isEmpty();
      }

      public boolean triggerEvent(Level level, BlockPos blockpos, int i, int j) {
         return this.getBlock().triggerEvent(this.asState(), level, blockpos, i, j);
      }

      /** @deprecated */
      @Deprecated
      public void neighborChanged(Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
         this.getBlock().neighborChanged(this.asState(), level, blockpos, block, blockpos1, flag);
      }

      public final void updateNeighbourShapes(LevelAccessor levelaccessor, BlockPos blockpos, int i) {
         this.updateNeighbourShapes(levelaccessor, blockpos, i, 512);
      }

      public final void updateNeighbourShapes(LevelAccessor levelaccessor, BlockPos blockpos, int i, int j) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(Direction direction : BlockBehaviour.UPDATE_SHAPE_ORDER) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction);
            levelaccessor.neighborShapeChanged(direction.getOpposite(), this.asState(), blockpos_mutableblockpos, blockpos, i, j);
         }

      }

      public final void updateIndirectNeighbourShapes(LevelAccessor levelaccessor, BlockPos blockpos, int i) {
         this.updateIndirectNeighbourShapes(levelaccessor, blockpos, i, 512);
      }

      public void updateIndirectNeighbourShapes(LevelAccessor levelaccessor, BlockPos blockpos, int i, int j) {
         this.getBlock().updateIndirectNeighbourShapes(this.asState(), levelaccessor, blockpos, i, j);
      }

      public void onPlace(Level level, BlockPos blockpos, BlockState blockstate, boolean flag) {
         this.getBlock().onPlace(this.asState(), level, blockpos, blockstate, flag);
      }

      public void onRemove(Level level, BlockPos blockpos, BlockState blockstate, boolean flag) {
         this.getBlock().onRemove(this.asState(), level, blockpos, blockstate, flag);
      }

      public void tick(ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
         this.getBlock().tick(this.asState(), serverlevel, blockpos, randomsource);
      }

      public void randomTick(ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
         this.getBlock().randomTick(this.asState(), serverlevel, blockpos, randomsource);
      }

      public void entityInside(Level level, BlockPos blockpos, Entity entity) {
         this.getBlock().entityInside(this.asState(), level, blockpos, entity);
      }

      public void spawnAfterBreak(ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
         this.getBlock().spawnAfterBreak(this.asState(), serverlevel, blockpos, itemstack, flag);
      }

      public List<ItemStack> getDrops(LootParams.Builder lootparams_builder) {
         return this.getBlock().getDrops(this.asState(), lootparams_builder);
      }

      public InteractionResult use(Level level, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
         return this.getBlock().use(this.asState(), level, blockhitresult.getBlockPos(), player, interactionhand, blockhitresult);
      }

      public void attack(Level level, BlockPos blockpos, Player player) {
         this.getBlock().attack(this.asState(), level, blockpos, player);
      }

      public boolean isSuffocating(BlockGetter blockgetter, BlockPos blockpos) {
         return this.isSuffocating.test(this.asState(), blockgetter, blockpos);
      }

      public boolean isViewBlocking(BlockGetter blockgetter, BlockPos blockpos) {
         return this.isViewBlocking.test(this.asState(), blockgetter, blockpos);
      }

      public BlockState updateShape(Direction direction, BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
         return this.getBlock().updateShape(this.asState(), direction, blockstate, levelaccessor, blockpos, blockpos1);
      }

      public boolean isPathfindable(BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
         return this.getBlock().isPathfindable(this.asState(), blockgetter, blockpos, pathcomputationtype);
      }

      public boolean canBeReplaced(BlockPlaceContext blockplacecontext) {
         return this.getBlock().canBeReplaced(this.asState(), blockplacecontext);
      }

      public boolean canBeReplaced(Fluid fluid) {
         return this.getBlock().canBeReplaced(this.asState(), fluid);
      }

      public boolean canBeReplaced() {
         return this.replaceable;
      }

      public boolean canSurvive(LevelReader levelreader, BlockPos blockpos) {
         return this.getBlock().canSurvive(this.asState(), levelreader, blockpos);
      }

      public boolean hasPostProcess(BlockGetter blockgetter, BlockPos blockpos) {
         return this.hasPostProcess.test(this.asState(), blockgetter, blockpos);
      }

      @Nullable
      public MenuProvider getMenuProvider(Level level, BlockPos blockpos) {
         return this.getBlock().getMenuProvider(this.asState(), level, blockpos);
      }

      public boolean is(TagKey<Block> tagkey) {
         return this.getBlock().builtInRegistryHolder().is(tagkey);
      }

      public boolean is(TagKey<Block> tagkey, Predicate<BlockBehaviour.BlockStateBase> predicate) {
         return this.is(tagkey) && predicate.test(this);
      }

      public boolean is(HolderSet<Block> holderset) {
         return holderset.contains(this.getBlock().builtInRegistryHolder());
      }

      public Stream<TagKey<Block>> getTags() {
         return this.getBlock().builtInRegistryHolder().tags();
      }

      public boolean hasBlockEntity() {
         return this.getBlock() instanceof EntityBlock;
      }

      @Nullable
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockEntityType<T> blockentitytype) {
         return this.getBlock() instanceof EntityBlock ? ((EntityBlock)this.getBlock()).getTicker(level, this.asState(), blockentitytype) : null;
      }

      public boolean is(Block block) {
         return this.getBlock() == block;
      }

      public FluidState getFluidState() {
         return this.fluidState;
      }

      public boolean isRandomlyTicking() {
         return this.isRandomlyTicking;
      }

      public long getSeed(BlockPos blockpos) {
         return this.getBlock().getSeed(this.asState(), blockpos);
      }

      public SoundType getSoundType() {
         return this.getBlock().getSoundType(this.asState());
      }

      public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
         this.getBlock().onProjectileHit(level, blockstate, blockhitresult, projectile);
      }

      public boolean isFaceSturdy(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return this.isFaceSturdy(blockgetter, blockpos, direction, SupportType.FULL);
      }

      public boolean isFaceSturdy(BlockGetter blockgetter, BlockPos blockpos, Direction direction, SupportType supporttype) {
         return this.cache != null ? this.cache.isFaceSturdy(direction, supporttype) : supporttype.isSupporting(this.asState(), blockgetter, blockpos, direction);
      }

      public boolean isCollisionShapeFullBlock(BlockGetter blockgetter, BlockPos blockpos) {
         return this.cache != null ? this.cache.isCollisionShapeFullBlock : this.getBlock().isCollisionShapeFullBlock(this.asState(), blockgetter, blockpos);
      }

      protected abstract BlockState asState();

      public boolean requiresCorrectToolForDrops() {
         return this.requiresCorrectToolForDrops;
      }

      public boolean shouldSpawnParticlesOnBreak() {
         return this.spawnParticlesOnBreak;
      }

      public NoteBlockInstrument instrument() {
         return this.instrument;
      }

      static final class Cache {
         private static final Direction[] DIRECTIONS = Direction.values();
         private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
         protected final boolean solidRender;
         final boolean propagatesSkylightDown;
         final int lightBlock;
         @Nullable
         final VoxelShape[] occlusionShapes;
         protected final VoxelShape collisionShape;
         protected final boolean largeCollisionShape;
         private final boolean[] faceSturdy;
         protected final boolean isCollisionShapeFullBlock;

         Cache(BlockState blockstate) {
            Block block = blockstate.getBlock();
            this.solidRender = blockstate.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            this.propagatesSkylightDown = block.propagatesSkylightDown(blockstate, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            this.lightBlock = block.getLightBlock(blockstate, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (!blockstate.canOcclude()) {
               this.occlusionShapes = null;
            } else {
               this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
               VoxelShape voxelshape = block.getOcclusionShape(blockstate, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

               for(Direction direction : DIRECTIONS) {
                  this.occlusionShapes[direction.ordinal()] = Shapes.getFaceShape(voxelshape, direction);
               }
            }

            this.collisionShape = block.getCollisionShape(blockstate, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
            if (!this.collisionShape.isEmpty() && blockstate.hasOffsetFunction()) {
               throw new IllegalStateException(String.format(Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", BuiltInRegistries.BLOCK.getKey(block)));
            } else {
               this.largeCollisionShape = Arrays.stream(Direction.Axis.values()).anyMatch((direction_axis) -> this.collisionShape.min(direction_axis) < 0.0D || this.collisionShape.max(direction_axis) > 1.0D);
               this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

               for(Direction direction1 : DIRECTIONS) {
                  for(SupportType supporttype : SupportType.values()) {
                     this.faceSturdy[getFaceSupportIndex(direction1, supporttype)] = supporttype.isSupporting(blockstate, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction1);
                  }
               }

               this.isCollisionShapeFullBlock = Block.isShapeFullBlock(blockstate.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
            }
         }

         public boolean isFaceSturdy(Direction direction, SupportType supporttype) {
            return this.faceSturdy[getFaceSupportIndex(direction, supporttype)];
         }

         private static int getFaceSupportIndex(Direction direction, SupportType supporttype) {
            return direction.ordinal() * SUPPORT_TYPE_COUNT + supporttype.ordinal();
         }
      }
   }

   public interface OffsetFunction {
      Vec3 evaluate(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos);
   }

   public static enum OffsetType {
      NONE,
      XZ,
      XYZ;
   }

   public static class Properties {
      Function<BlockState, MapColor> mapColor = (blockstate6) -> MapColor.NONE;
      boolean hasCollision = true;
      SoundType soundType = SoundType.STONE;
      ToIntFunction<BlockState> lightEmission = (blockstate5) -> 0;
      float explosionResistance;
      float destroyTime;
      boolean requiresCorrectToolForDrops;
      boolean isRandomlyTicking;
      float friction = 0.6F;
      float speedFactor = 1.0F;
      float jumpFactor = 1.0F;
      ResourceLocation drops;
      boolean canOcclude = true;
      boolean isAir;
      boolean ignitedByLava;
      /** @deprecated */
      @Deprecated
      boolean liquid;
      /** @deprecated */
      @Deprecated
      boolean forceSolidOff;
      boolean forceSolidOn;
      PushReaction pushReaction = PushReaction.NORMAL;
      boolean spawnParticlesOnBreak = true;
      NoteBlockInstrument instrument = NoteBlockInstrument.HARP;
      boolean replaceable;
      BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn = (blockstate4, blockgetter4, blockpos4, entitytype) -> blockstate4.isFaceSturdy(blockgetter4, blockpos4, Direction.UP) && blockstate4.getLightEmission() < 14;
      BlockBehaviour.StatePredicate isRedstoneConductor = (blockstate3, blockgetter3, blockpos3) -> blockstate3.isCollisionShapeFullBlock(blockgetter3, blockpos3);
      BlockBehaviour.StatePredicate isSuffocating = (blockstate2, blockgetter2, blockpos2) -> blockstate2.blocksMotion() && blockstate2.isCollisionShapeFullBlock(blockgetter2, blockpos2);
      BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
      BlockBehaviour.StatePredicate hasPostProcess = (blockstate1, blockgetter1, blockpos1) -> false;
      BlockBehaviour.StatePredicate emissiveRendering = (blockstate, blockgetter, blockpos) -> false;
      boolean dynamicShape;
      FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
      Optional<BlockBehaviour.OffsetFunction> offsetFunction = Optional.empty();

      private Properties() {
      }

      public static BlockBehaviour.Properties of() {
         return new BlockBehaviour.Properties();
      }

      public static BlockBehaviour.Properties copy(BlockBehaviour blockbehaviour) {
         BlockBehaviour.Properties blockbehaviour_properties = new BlockBehaviour.Properties();
         blockbehaviour_properties.destroyTime = blockbehaviour.properties.destroyTime;
         blockbehaviour_properties.explosionResistance = blockbehaviour.properties.explosionResistance;
         blockbehaviour_properties.hasCollision = blockbehaviour.properties.hasCollision;
         blockbehaviour_properties.isRandomlyTicking = blockbehaviour.properties.isRandomlyTicking;
         blockbehaviour_properties.lightEmission = blockbehaviour.properties.lightEmission;
         blockbehaviour_properties.mapColor = blockbehaviour.properties.mapColor;
         blockbehaviour_properties.soundType = blockbehaviour.properties.soundType;
         blockbehaviour_properties.friction = blockbehaviour.properties.friction;
         blockbehaviour_properties.speedFactor = blockbehaviour.properties.speedFactor;
         blockbehaviour_properties.dynamicShape = blockbehaviour.properties.dynamicShape;
         blockbehaviour_properties.canOcclude = blockbehaviour.properties.canOcclude;
         blockbehaviour_properties.isAir = blockbehaviour.properties.isAir;
         blockbehaviour_properties.ignitedByLava = blockbehaviour.properties.ignitedByLava;
         blockbehaviour_properties.liquid = blockbehaviour.properties.liquid;
         blockbehaviour_properties.forceSolidOff = blockbehaviour.properties.forceSolidOff;
         blockbehaviour_properties.forceSolidOn = blockbehaviour.properties.forceSolidOn;
         blockbehaviour_properties.pushReaction = blockbehaviour.properties.pushReaction;
         blockbehaviour_properties.requiresCorrectToolForDrops = blockbehaviour.properties.requiresCorrectToolForDrops;
         blockbehaviour_properties.offsetFunction = blockbehaviour.properties.offsetFunction;
         blockbehaviour_properties.spawnParticlesOnBreak = blockbehaviour.properties.spawnParticlesOnBreak;
         blockbehaviour_properties.requiredFeatures = blockbehaviour.properties.requiredFeatures;
         blockbehaviour_properties.emissiveRendering = blockbehaviour.properties.emissiveRendering;
         blockbehaviour_properties.instrument = blockbehaviour.properties.instrument;
         blockbehaviour_properties.replaceable = blockbehaviour.properties.replaceable;
         return blockbehaviour_properties;
      }

      public BlockBehaviour.Properties mapColor(DyeColor dyecolor) {
         this.mapColor = (blockstate) -> dyecolor.getMapColor();
         return this;
      }

      public BlockBehaviour.Properties mapColor(MapColor mapcolor) {
         this.mapColor = (blockstate) -> mapcolor;
         return this;
      }

      public BlockBehaviour.Properties mapColor(Function<BlockState, MapColor> function) {
         this.mapColor = function;
         return this;
      }

      public BlockBehaviour.Properties noCollission() {
         this.hasCollision = false;
         this.canOcclude = false;
         return this;
      }

      public BlockBehaviour.Properties noOcclusion() {
         this.canOcclude = false;
         return this;
      }

      public BlockBehaviour.Properties friction(float f) {
         this.friction = f;
         return this;
      }

      public BlockBehaviour.Properties speedFactor(float f) {
         this.speedFactor = f;
         return this;
      }

      public BlockBehaviour.Properties jumpFactor(float f) {
         this.jumpFactor = f;
         return this;
      }

      public BlockBehaviour.Properties sound(SoundType soundtype) {
         this.soundType = soundtype;
         return this;
      }

      public BlockBehaviour.Properties lightLevel(ToIntFunction<BlockState> tointfunction) {
         this.lightEmission = tointfunction;
         return this;
      }

      public BlockBehaviour.Properties strength(float f, float f1) {
         return this.destroyTime(f).explosionResistance(f1);
      }

      public BlockBehaviour.Properties instabreak() {
         return this.strength(0.0F);
      }

      public BlockBehaviour.Properties strength(float f) {
         this.strength(f, f);
         return this;
      }

      public BlockBehaviour.Properties randomTicks() {
         this.isRandomlyTicking = true;
         return this;
      }

      public BlockBehaviour.Properties dynamicShape() {
         this.dynamicShape = true;
         return this;
      }

      public BlockBehaviour.Properties noLootTable() {
         this.drops = BuiltInLootTables.EMPTY;
         return this;
      }

      public BlockBehaviour.Properties dropsLike(Block block) {
         this.drops = block.getLootTable();
         return this;
      }

      public BlockBehaviour.Properties ignitedByLava() {
         this.ignitedByLava = true;
         return this;
      }

      public BlockBehaviour.Properties liquid() {
         this.liquid = true;
         return this;
      }

      public BlockBehaviour.Properties forceSolidOn() {
         this.forceSolidOn = true;
         return this;
      }

      /** @deprecated */
      @Deprecated
      public BlockBehaviour.Properties forceSolidOff() {
         this.forceSolidOff = true;
         return this;
      }

      public BlockBehaviour.Properties pushReaction(PushReaction pushreaction) {
         this.pushReaction = pushreaction;
         return this;
      }

      public BlockBehaviour.Properties air() {
         this.isAir = true;
         return this;
      }

      public BlockBehaviour.Properties isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> blockbehaviour_stateargumentpredicate) {
         this.isValidSpawn = blockbehaviour_stateargumentpredicate;
         return this;
      }

      public BlockBehaviour.Properties isRedstoneConductor(BlockBehaviour.StatePredicate blockbehaviour_statepredicate) {
         this.isRedstoneConductor = blockbehaviour_statepredicate;
         return this;
      }

      public BlockBehaviour.Properties isSuffocating(BlockBehaviour.StatePredicate blockbehaviour_statepredicate) {
         this.isSuffocating = blockbehaviour_statepredicate;
         return this;
      }

      public BlockBehaviour.Properties isViewBlocking(BlockBehaviour.StatePredicate blockbehaviour_statepredicate) {
         this.isViewBlocking = blockbehaviour_statepredicate;
         return this;
      }

      public BlockBehaviour.Properties hasPostProcess(BlockBehaviour.StatePredicate blockbehaviour_statepredicate) {
         this.hasPostProcess = blockbehaviour_statepredicate;
         return this;
      }

      public BlockBehaviour.Properties emissiveRendering(BlockBehaviour.StatePredicate blockbehaviour_statepredicate) {
         this.emissiveRendering = blockbehaviour_statepredicate;
         return this;
      }

      public BlockBehaviour.Properties requiresCorrectToolForDrops() {
         this.requiresCorrectToolForDrops = true;
         return this;
      }

      public BlockBehaviour.Properties destroyTime(float f) {
         this.destroyTime = f;
         return this;
      }

      public BlockBehaviour.Properties explosionResistance(float f) {
         this.explosionResistance = Math.max(0.0F, f);
         return this;
      }

      public BlockBehaviour.Properties offsetType(BlockBehaviour.OffsetType blockbehaviour_offsettype) {
         switch (blockbehaviour_offsettype) {
            case XYZ:
               this.offsetFunction = Optional.of((blockstate1, blockgetter1, blockpos1) -> {
                  Block block1 = blockstate1.getBlock();
                  long j = Mth.getSeed(blockpos1.getX(), 0, blockpos1.getZ());
                  double d2 = ((double)((float)(j >> 4 & 15L) / 15.0F) - 1.0D) * (double)block1.getMaxVerticalOffset();
                  float f1 = block1.getMaxHorizontalOffset();
                  double d3 = Mth.clamp(((double)((float)(j & 15L) / 15.0F) - 0.5D) * 0.5D, (double)(-f1), (double)f1);
                  double d4 = Mth.clamp(((double)((float)(j >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D, (double)(-f1), (double)f1);
                  return new Vec3(d3, d2, d4);
               });
               break;
            case XZ:
               this.offsetFunction = Optional.of((blockstate, blockgetter, blockpos) -> {
                  Block block = blockstate.getBlock();
                  long i = Mth.getSeed(blockpos.getX(), 0, blockpos.getZ());
                  float f = block.getMaxHorizontalOffset();
                  double d0 = Mth.clamp(((double)((float)(i & 15L) / 15.0F) - 0.5D) * 0.5D, (double)(-f), (double)f);
                  double d1 = Mth.clamp(((double)((float)(i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D, (double)(-f), (double)f);
                  return new Vec3(d0, 0.0D, d1);
               });
               break;
            default:
               this.offsetFunction = Optional.empty();
         }

         return this;
      }

      public BlockBehaviour.Properties noParticlesOnBreak() {
         this.spawnParticlesOnBreak = false;
         return this;
      }

      public BlockBehaviour.Properties requiredFeatures(FeatureFlag... afeatureflag) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
         return this;
      }

      public BlockBehaviour.Properties instrument(NoteBlockInstrument noteblockinstrument) {
         this.instrument = noteblockinstrument;
         return this;
      }

      public BlockBehaviour.Properties replaceable() {
         this.replaceable = true;
         return this;
      }
   }

   public interface StateArgumentPredicate<A> {
      boolean test(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, A object);
   }

   public interface StatePredicate {
      boolean test(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos);
   }
}
