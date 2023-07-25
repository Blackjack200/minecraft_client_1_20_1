package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class FallingBlockEntity extends Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private BlockState blockState = Blocks.SAND.defaultBlockState();
   public int time;
   public boolean dropItem = true;
   private boolean cancelDrop;
   private boolean hurtEntities;
   private int fallDamageMax = 40;
   private float fallDamagePerDistance;
   @Nullable
   public CompoundTag blockData;
   protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

   public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entitytype, Level level) {
      super(entitytype, level);
   }

   private FallingBlockEntity(Level level, double d0, double d1, double d2, BlockState blockstate) {
      this(EntityType.FALLING_BLOCK, level);
      this.blockState = blockstate;
      this.blocksBuilding = true;
      this.setPos(d0, d1, d2);
      this.setDeltaMovement(Vec3.ZERO);
      this.xo = d0;
      this.yo = d1;
      this.zo = d2;
      this.setStartPos(this.blockPosition());
   }

   public static FallingBlockEntity fall(Level level, BlockPos blockpos, BlockState blockstate) {
      FallingBlockEntity fallingblockentity = new FallingBlockEntity(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, blockstate.hasProperty(BlockStateProperties.WATERLOGGED) ? blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : blockstate);
      level.setBlock(blockpos, blockstate.getFluidState().createLegacyBlock(), 3);
      level.addFreshEntity(fallingblockentity);
      return fallingblockentity;
   }

   public boolean isAttackable() {
      return false;
   }

   public void setStartPos(BlockPos blockpos) {
      this.entityData.set(DATA_START_POS, blockpos);
   }

   public BlockPos getStartPos() {
      return this.entityData.get(DATA_START_POS);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_START_POS, BlockPos.ZERO);
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public void tick() {
      if (this.blockState.isAir()) {
         this.discard();
      } else {
         Block block = this.blockState.getBlock();
         ++this.time;
         if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
         }

         this.move(MoverType.SELF, this.getDeltaMovement());
         if (!this.level().isClientSide) {
            BlockPos blockpos = this.blockPosition();
            boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock;
            boolean flag1 = flag && this.level().getFluidState(blockpos).is(FluidTags.WATER);
            double d0 = this.getDeltaMovement().lengthSqr();
            if (flag && d0 > 1.0D) {
               BlockHitResult blockhitresult = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
               if (blockhitresult.getType() != HitResult.Type.MISS && this.level().getFluidState(blockhitresult.getBlockPos()).is(FluidTags.WATER)) {
                  blockpos = blockhitresult.getBlockPos();
                  flag1 = true;
               }
            }

            if (!this.onGround() && !flag1) {
               if (!this.level().isClientSide && (this.time > 100 && (blockpos.getY() <= this.level().getMinBuildHeight() || blockpos.getY() > this.level().getMaxBuildHeight()) || this.time > 600)) {
                  if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                     this.spawnAtLocation(block);
                  }

                  this.discard();
               }
            } else {
               BlockState blockstate = this.level().getBlockState(blockpos);
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
               if (!blockstate.is(Blocks.MOVING_PISTON)) {
                  if (!this.cancelDrop) {
                     boolean flag2 = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level(), blockpos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                     boolean flag3 = FallingBlock.isFree(this.level().getBlockState(blockpos.below())) && (!flag || !flag1);
                     boolean flag4 = this.blockState.canSurvive(this.level(), blockpos) && !flag3;
                     if (flag2 && flag4) {
                        if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(blockpos).getType() == Fluids.WATER) {
                           this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
                        }

                        if (this.level().setBlock(blockpos, this.blockState, 3)) {
                           ((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.level().getBlockState(blockpos)));
                           this.discard();
                           if (block instanceof Fallable) {
                              ((Fallable)block).onLand(this.level(), blockpos, this.blockState, blockstate, this);
                           }

                           if (this.blockData != null && this.blockState.hasBlockEntity()) {
                              BlockEntity blockentity = this.level().getBlockEntity(blockpos);
                              if (blockentity != null) {
                                 CompoundTag compoundtag = blockentity.saveWithoutMetadata();

                                 for(String s : this.blockData.getAllKeys()) {
                                    compoundtag.put(s, this.blockData.get(s).copy());
                                 }

                                 try {
                                    blockentity.load(compoundtag);
                                 } catch (Exception var15) {
                                    LOGGER.error("Failed to load block entity from falling block", (Throwable)var15);
                                 }

                                 blockentity.setChanged();
                              }
                           }
                        } else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                           this.discard();
                           this.callOnBrokenAfterFall(block, blockpos);
                           this.spawnAtLocation(block);
                        }
                     } else {
                        this.discard();
                        if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                           this.callOnBrokenAfterFall(block, blockpos);
                           this.spawnAtLocation(block);
                        }
                     }
                  } else {
                     this.discard();
                     this.callOnBrokenAfterFall(block, blockpos);
                  }
               }
            }
         }

         this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
      }
   }

   public void callOnBrokenAfterFall(Block block, BlockPos blockpos) {
      if (block instanceof Fallable) {
         ((Fallable)block).onBrokenAfterFall(this.level(), blockpos, this);
      }

   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      if (!this.hurtEntities) {
         return false;
      } else {
         int i = Mth.ceil(f - 1.0F);
         if (i < 0) {
            return false;
         } else {
            Predicate<Entity> predicate = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
            Block flag = this.blockState.getBlock();
            DamageSource var10000;
            if (flag instanceof Fallable) {
               Fallable fallable = (Fallable)flag;
               var10000 = fallable.getFallDamageSource(this);
            } else {
               var10000 = this.damageSources().fallingBlock(this);
            }

            DamageSource damagesource1 = var10000;
            float f2 = (float)Math.min(Mth.floor((float)i * this.fallDamagePerDistance), this.fallDamageMax);
            this.level().getEntities(this, this.getBoundingBox(), predicate).forEach((entity) -> entity.hurt(damagesource1, f2));
            boolean flag = this.blockState.is(BlockTags.ANVIL);
            if (flag && f2 > 0.0F && this.random.nextFloat() < 0.05F + (float)i * 0.05F) {
               BlockState blockstate = AnvilBlock.damage(this.blockState);
               if (blockstate == null) {
                  this.cancelDrop = true;
               } else {
                  this.blockState = blockstate;
               }
            }

            return false;
         }
      }
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
      compoundtag.putInt("Time", this.time);
      compoundtag.putBoolean("DropItem", this.dropItem);
      compoundtag.putBoolean("HurtEntities", this.hurtEntities);
      compoundtag.putFloat("FallHurtAmount", this.fallDamagePerDistance);
      compoundtag.putInt("FallHurtMax", this.fallDamageMax);
      if (this.blockData != null) {
         compoundtag.put("TileEntityData", this.blockData);
      }

      compoundtag.putBoolean("CancelDrop", this.cancelDrop);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      this.blockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundtag.getCompound("BlockState"));
      this.time = compoundtag.getInt("Time");
      if (compoundtag.contains("HurtEntities", 99)) {
         this.hurtEntities = compoundtag.getBoolean("HurtEntities");
         this.fallDamagePerDistance = compoundtag.getFloat("FallHurtAmount");
         this.fallDamageMax = compoundtag.getInt("FallHurtMax");
      } else if (this.blockState.is(BlockTags.ANVIL)) {
         this.hurtEntities = true;
      }

      if (compoundtag.contains("DropItem", 99)) {
         this.dropItem = compoundtag.getBoolean("DropItem");
      }

      if (compoundtag.contains("TileEntityData", 10)) {
         this.blockData = compoundtag.getCompound("TileEntityData");
      }

      this.cancelDrop = compoundtag.getBoolean("CancelDrop");
      if (this.blockState.isAir()) {
         this.blockState = Blocks.SAND.defaultBlockState();
      }

   }

   public void setHurtsEntities(float f, int i) {
      this.hurtEntities = true;
      this.fallDamagePerDistance = f;
      this.fallDamageMax = i;
   }

   public void disableDrop() {
      this.cancelDrop = true;
   }

   public boolean displayFireAnimation() {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory crashreportcategory) {
      super.fillCrashReportCategory(crashreportcategory);
      crashreportcategory.setDetail("Immitating BlockState", this.blockState.toString());
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   protected Component getTypeName() {
      return Component.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      this.blockState = Block.stateById(clientboundaddentitypacket.getData());
      this.blocksBuilding = true;
      double d0 = clientboundaddentitypacket.getX();
      double d1 = clientboundaddentitypacket.getY();
      double d2 = clientboundaddentitypacket.getZ();
      this.setPos(d0, d1, d2);
      this.setStartPos(this.blockPosition());
   }
}
