package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SPAWN_TIME = 200;
   private static final int COOLDOWN_TIME = 40;
   private static final int ATTENTION_INTERVAL = 2400;
   private static final int EVENT_COOLDOWN = 1;
   private static final int GATEWAY_HEIGHT_ABOVE_SURFACE = 10;
   private long age;
   private int teleportCooldown;
   @Nullable
   private BlockPos exitPortal;
   private boolean exactTeleport;

   public TheEndGatewayBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.END_GATEWAY, blockpos, blockstate);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putLong("Age", this.age);
      if (this.exitPortal != null) {
         compoundtag.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
      }

      if (this.exactTeleport) {
         compoundtag.putBoolean("ExactTeleport", true);
      }

   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.age = compoundtag.getLong("Age");
      if (compoundtag.contains("ExitPortal", 10)) {
         BlockPos blockpos = NbtUtils.readBlockPos(compoundtag.getCompound("ExitPortal"));
         if (Level.isInSpawnableBounds(blockpos)) {
            this.exitPortal = blockpos;
         }
      }

      this.exactTeleport = compoundtag.getBoolean("ExactTeleport");
   }

   public static void beamAnimationTick(Level level, BlockPos blockpos, BlockState blockstate, TheEndGatewayBlockEntity theendgatewayblockentity) {
      ++theendgatewayblockentity.age;
      if (theendgatewayblockentity.isCoolingDown()) {
         --theendgatewayblockentity.teleportCooldown;
      }

   }

   public static void teleportTick(Level level, BlockPos blockpos, BlockState blockstate, TheEndGatewayBlockEntity theendgatewayblockentity) {
      boolean flag = theendgatewayblockentity.isSpawning();
      boolean flag1 = theendgatewayblockentity.isCoolingDown();
      ++theendgatewayblockentity.age;
      if (flag1) {
         --theendgatewayblockentity.teleportCooldown;
      } else {
         List<Entity> list = level.getEntitiesOfClass(Entity.class, new AABB(blockpos), TheEndGatewayBlockEntity::canEntityTeleport);
         if (!list.isEmpty()) {
            teleportEntity(level, blockpos, blockstate, list.get(level.random.nextInt(list.size())), theendgatewayblockentity);
         }

         if (theendgatewayblockentity.age % 2400L == 0L) {
            triggerCooldown(level, blockpos, blockstate, theendgatewayblockentity);
         }
      }

      if (flag != theendgatewayblockentity.isSpawning() || flag1 != theendgatewayblockentity.isCoolingDown()) {
         setChanged(level, blockpos, blockstate);
      }

   }

   public static boolean canEntityTeleport(Entity entity) {
      return EntitySelector.NO_SPECTATORS.test(entity) && !entity.getRootVehicle().isOnPortalCooldown();
   }

   public boolean isSpawning() {
      return this.age < 200L;
   }

   public boolean isCoolingDown() {
      return this.teleportCooldown > 0;
   }

   public float getSpawnPercent(float f) {
      return Mth.clamp(((float)this.age + f) / 200.0F, 0.0F, 1.0F);
   }

   public float getCooldownPercent(float f) {
      return 1.0F - Mth.clamp(((float)this.teleportCooldown - f) / 40.0F, 0.0F, 1.0F);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   private static void triggerCooldown(Level level, BlockPos blockpos, BlockState blockstate, TheEndGatewayBlockEntity theendgatewayblockentity) {
      if (!level.isClientSide) {
         theendgatewayblockentity.teleportCooldown = 40;
         level.blockEvent(blockpos, blockstate.getBlock(), 1, 0);
         setChanged(level, blockpos, blockstate);
      }

   }

   public boolean triggerEvent(int i, int j) {
      if (i == 1) {
         this.teleportCooldown = 40;
         return true;
      } else {
         return super.triggerEvent(i, j);
      }
   }

   public static void teleportEntity(Level level, BlockPos blockpos, BlockState blockstate, Entity entity, TheEndGatewayBlockEntity theendgatewayblockentity) {
      if (level instanceof ServerLevel serverlevel && !theendgatewayblockentity.isCoolingDown()) {
         theendgatewayblockentity.teleportCooldown = 100;
         if (theendgatewayblockentity.exitPortal == null && level.dimension() == Level.END) {
            BlockPos blockpos1 = findOrCreateValidTeleportPos(serverlevel, blockpos);
            blockpos1 = blockpos1.above(10);
            LOGGER.debug("Creating portal at {}", (Object)blockpos1);
            spawnGatewayPortal(serverlevel, blockpos1, EndGatewayConfiguration.knownExit(blockpos, false));
            theendgatewayblockentity.exitPortal = blockpos1;
         }

         if (theendgatewayblockentity.exitPortal != null) {
            BlockPos blockpos2 = theendgatewayblockentity.exactTeleport ? theendgatewayblockentity.exitPortal : findExitPosition(level, theendgatewayblockentity.exitPortal);
            Entity entity2;
            if (entity instanceof ThrownEnderpearl) {
               Entity entity1 = ((ThrownEnderpearl)entity).getOwner();
               if (entity1 instanceof ServerPlayer) {
                  CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)entity1, blockstate);
               }

               if (entity1 != null) {
                  entity2 = entity1;
                  entity.discard();
               } else {
                  entity2 = entity;
               }
            } else {
               entity2 = entity.getRootVehicle();
            }

            entity2.setPortalCooldown();
            entity2.teleportToWithTicket((double)blockpos2.getX() + 0.5D, (double)blockpos2.getY(), (double)blockpos2.getZ() + 0.5D);
         }

         triggerCooldown(level, blockpos, blockstate, theendgatewayblockentity);
      }
   }

   private static BlockPos findExitPosition(Level level, BlockPos blockpos) {
      BlockPos blockpos1 = findTallestBlock(level, blockpos.offset(0, 2, 0), 5, false);
      LOGGER.debug("Best exit position for portal at {} is {}", blockpos, blockpos1);
      return blockpos1.above();
   }

   private static BlockPos findOrCreateValidTeleportPos(ServerLevel serverlevel, BlockPos blockpos) {
      Vec3 vec3 = findExitPortalXZPosTentative(serverlevel, blockpos);
      LevelChunk levelchunk = getChunk(serverlevel, vec3);
      BlockPos blockpos1 = findValidSpawnInChunk(levelchunk);
      if (blockpos1 == null) {
         BlockPos blockpos2 = BlockPos.containing(vec3.x + 0.5D, 75.0D, vec3.z + 0.5D);
         LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", (Object)blockpos2);
         serverlevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((registry) -> registry.getHolder(EndFeatures.END_ISLAND)).ifPresent((holder_reference) -> holder_reference.value().place(serverlevel, serverlevel.getChunkSource().getGenerator(), RandomSource.create(blockpos2.asLong()), blockpos2));
         blockpos1 = blockpos2;
      } else {
         LOGGER.debug("Found suitable block to teleport to: {}", (Object)blockpos1);
      }

      return findTallestBlock(serverlevel, blockpos1, 16, true);
   }

   private static Vec3 findExitPortalXZPosTentative(ServerLevel serverlevel, BlockPos blockpos) {
      Vec3 vec3 = (new Vec3((double)blockpos.getX(), 0.0D, (double)blockpos.getZ())).normalize();
      int i = 1024;
      Vec3 vec31 = vec3.scale(1024.0D);

      for(int j = 16; !isChunkEmpty(serverlevel, vec31) && j-- > 0; vec31 = vec31.add(vec3.scale(-16.0D))) {
         LOGGER.debug("Skipping backwards past nonempty chunk at {}", (Object)vec31);
      }

      for(int var6 = 16; isChunkEmpty(serverlevel, vec31) && var6-- > 0; vec31 = vec31.add(vec3.scale(16.0D))) {
         LOGGER.debug("Skipping forward past empty chunk at {}", (Object)vec31);
      }

      LOGGER.debug("Found chunk at {}", (Object)vec31);
      return vec31;
   }

   private static boolean isChunkEmpty(ServerLevel serverlevel, Vec3 vec3) {
      return getChunk(serverlevel, vec3).getHighestFilledSectionIndex() == -1;
   }

   private static BlockPos findTallestBlock(BlockGetter blockgetter, BlockPos blockpos, int i, boolean flag) {
      BlockPos blockpos1 = null;

      for(int j = -i; j <= i; ++j) {
         for(int k = -i; k <= i; ++k) {
            if (j != 0 || k != 0 || flag) {
               for(int l = blockgetter.getMaxBuildHeight() - 1; l > (blockpos1 == null ? blockgetter.getMinBuildHeight() : blockpos1.getY()); --l) {
                  BlockPos blockpos2 = new BlockPos(blockpos.getX() + j, l, blockpos.getZ() + k);
                  BlockState blockstate = blockgetter.getBlockState(blockpos2);
                  if (blockstate.isCollisionShapeFullBlock(blockgetter, blockpos2) && (flag || !blockstate.is(Blocks.BEDROCK))) {
                     blockpos1 = blockpos2;
                     break;
                  }
               }
            }
         }
      }

      return blockpos1 == null ? blockpos : blockpos1;
   }

   private static LevelChunk getChunk(Level level, Vec3 vec3) {
      return level.getChunk(Mth.floor(vec3.x / 16.0D), Mth.floor(vec3.z / 16.0D));
   }

   @Nullable
   private static BlockPos findValidSpawnInChunk(LevelChunk levelchunk) {
      ChunkPos chunkpos = levelchunk.getPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 30, chunkpos.getMinBlockZ());
      int i = levelchunk.getHighestSectionPosition() + 16 - 1;
      BlockPos blockpos1 = new BlockPos(chunkpos.getMaxBlockX(), i, chunkpos.getMaxBlockZ());
      BlockPos blockpos2 = null;
      double d0 = 0.0D;

      for(BlockPos blockpos3 : BlockPos.betweenClosed(blockpos, blockpos1)) {
         BlockState blockstate = levelchunk.getBlockState(blockpos3);
         BlockPos blockpos4 = blockpos3.above();
         BlockPos blockpos5 = blockpos3.above(2);
         if (blockstate.is(Blocks.END_STONE) && !levelchunk.getBlockState(blockpos4).isCollisionShapeFullBlock(levelchunk, blockpos4) && !levelchunk.getBlockState(blockpos5).isCollisionShapeFullBlock(levelchunk, blockpos5)) {
            double d1 = blockpos3.distToCenterSqr(0.0D, 0.0D, 0.0D);
            if (blockpos2 == null || d1 < d0) {
               blockpos2 = blockpos3;
               d0 = d1;
            }
         }
      }

      return blockpos2;
   }

   private static void spawnGatewayPortal(ServerLevel serverlevel, BlockPos blockpos, EndGatewayConfiguration endgatewayconfiguration) {
      Feature.END_GATEWAY.place(endgatewayconfiguration, serverlevel, serverlevel.getChunkSource().getGenerator(), RandomSource.create(), blockpos);
   }

   public boolean shouldRenderFace(Direction direction) {
      return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), direction, this.getBlockPos().relative(direction));
   }

   public int getParticleAmount() {
      int i = 0;

      for(Direction direction : Direction.values()) {
         i += this.shouldRenderFace(direction) ? 1 : 0;
      }

      return i;
   }

   public void setExitPosition(BlockPos blockpos, boolean flag) {
      this.exactTeleport = flag;
      this.exitPortal = blockpos;
   }
}
