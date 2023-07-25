package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class StructureTemplate {
   public static final String PALETTE_TAG = "palette";
   public static final String PALETTE_LIST_TAG = "palettes";
   public static final String ENTITIES_TAG = "entities";
   public static final String BLOCKS_TAG = "blocks";
   public static final String BLOCK_TAG_POS = "pos";
   public static final String BLOCK_TAG_STATE = "state";
   public static final String BLOCK_TAG_NBT = "nbt";
   public static final String ENTITY_TAG_POS = "pos";
   public static final String ENTITY_TAG_BLOCKPOS = "blockPos";
   public static final String ENTITY_TAG_NBT = "nbt";
   public static final String SIZE_TAG = "size";
   private final List<StructureTemplate.Palette> palettes = Lists.newArrayList();
   private final List<StructureTemplate.StructureEntityInfo> entityInfoList = Lists.newArrayList();
   private Vec3i size = Vec3i.ZERO;
   private String author = "?";

   public Vec3i getSize() {
      return this.size;
   }

   public void setAuthor(String s) {
      this.author = s;
   }

   public String getAuthor() {
      return this.author;
   }

   public void fillFromWorld(Level level, BlockPos blockpos, Vec3i vec3i, boolean flag, @Nullable Block block) {
      if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
         BlockPos blockpos1 = blockpos.offset(vec3i).offset(-1, -1, -1);
         List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
         List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();
         List<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
         BlockPos blockpos2 = new BlockPos(Math.min(blockpos.getX(), blockpos1.getX()), Math.min(blockpos.getY(), blockpos1.getY()), Math.min(blockpos.getZ(), blockpos1.getZ()));
         BlockPos blockpos3 = new BlockPos(Math.max(blockpos.getX(), blockpos1.getX()), Math.max(blockpos.getY(), blockpos1.getY()), Math.max(blockpos.getZ(), blockpos1.getZ()));
         this.size = vec3i;

         for(BlockPos blockpos4 : BlockPos.betweenClosed(blockpos2, blockpos3)) {
            BlockPos blockpos5 = blockpos4.subtract(blockpos2);
            BlockState blockstate = level.getBlockState(blockpos4);
            if (block == null || !blockstate.is(block)) {
               BlockEntity blockentity = level.getBlockEntity(blockpos4);
               StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo;
               if (blockentity != null) {
                  structuretemplate_structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos5, blockstate, blockentity.saveWithId());
               } else {
                  structuretemplate_structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos5, blockstate, (CompoundTag)null);
               }

               addToLists(structuretemplate_structureblockinfo, list, list1, list2);
            }
         }

         List<StructureTemplate.StructureBlockInfo> list3 = buildInfoList(list, list1, list2);
         this.palettes.clear();
         this.palettes.add(new StructureTemplate.Palette(list3));
         if (flag) {
            this.fillEntityList(level, blockpos2, blockpos3.offset(1, 1, 1));
         } else {
            this.entityInfoList.clear();
         }

      }
   }

   private static void addToLists(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, List<StructureTemplate.StructureBlockInfo> list, List<StructureTemplate.StructureBlockInfo> list1, List<StructureTemplate.StructureBlockInfo> list2) {
      if (structuretemplate_structureblockinfo.nbt != null) {
         list1.add(structuretemplate_structureblockinfo);
      } else if (!structuretemplate_structureblockinfo.state.getBlock().hasDynamicShape() && structuretemplate_structureblockinfo.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
         list.add(structuretemplate_structureblockinfo);
      } else {
         list2.add(structuretemplate_structureblockinfo);
      }

   }

   private static List<StructureTemplate.StructureBlockInfo> buildInfoList(List<StructureTemplate.StructureBlockInfo> list, List<StructureTemplate.StructureBlockInfo> list1, List<StructureTemplate.StructureBlockInfo> list2) {
      Comparator<StructureTemplate.StructureBlockInfo> comparator = Comparator.comparingInt((structuretemplate_structureblockinfo2) -> structuretemplate_structureblockinfo2.pos.getY()).thenComparingInt((structuretemplate_structureblockinfo1) -> structuretemplate_structureblockinfo1.pos.getX()).thenComparingInt((structuretemplate_structureblockinfo) -> structuretemplate_structureblockinfo.pos.getZ());
      list.sort(comparator);
      list2.sort(comparator);
      list1.sort(comparator);
      List<StructureTemplate.StructureBlockInfo> list3 = Lists.newArrayList();
      list3.addAll(list);
      list3.addAll(list2);
      list3.addAll(list1);
      return list3;
   }

   private void fillEntityList(Level level, BlockPos blockpos, BlockPos blockpos1) {
      List<Entity> list = level.getEntitiesOfClass(Entity.class, new AABB(blockpos, blockpos1), (entity1) -> !(entity1 instanceof Player));
      this.entityInfoList.clear();

      for(Entity entity : list) {
         Vec3 vec3 = new Vec3(entity.getX() - (double)blockpos.getX(), entity.getY() - (double)blockpos.getY(), entity.getZ() - (double)blockpos.getZ());
         CompoundTag compoundtag = new CompoundTag();
         entity.save(compoundtag);
         BlockPos blockpos2;
         if (entity instanceof Painting) {
            blockpos2 = ((Painting)entity).getPos().subtract(blockpos);
         } else {
            blockpos2 = BlockPos.containing(vec3);
         }

         this.entityInfoList.add(new StructureTemplate.StructureEntityInfo(vec3, blockpos2, compoundtag.copy()));
      }

   }

   public List<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos blockpos, StructurePlaceSettings structureplacesettings, Block block) {
      return this.filterBlocks(blockpos, structureplacesettings, block, true);
   }

   public ObjectArrayList<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos blockpos, StructurePlaceSettings structureplacesettings, Block block, boolean flag) {
      ObjectArrayList<StructureTemplate.StructureBlockInfo> objectarraylist = new ObjectArrayList<>();
      BoundingBox boundingbox = structureplacesettings.getBoundingBox();
      if (this.palettes.isEmpty()) {
         return objectarraylist;
      } else {
         for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : structureplacesettings.getRandomPalette(this.palettes, blockpos).blocks(block)) {
            BlockPos blockpos1 = flag ? calculateRelativePosition(structureplacesettings, structuretemplate_structureblockinfo.pos).offset(blockpos) : structuretemplate_structureblockinfo.pos;
            if (boundingbox == null || boundingbox.isInside(blockpos1)) {
               objectarraylist.add(new StructureTemplate.StructureBlockInfo(blockpos1, structuretemplate_structureblockinfo.state.rotate(structureplacesettings.getRotation()), structuretemplate_structureblockinfo.nbt));
            }
         }

         return objectarraylist;
      }
   }

   public BlockPos calculateConnectedPosition(StructurePlaceSettings structureplacesettings, BlockPos blockpos, StructurePlaceSettings structureplacesettings1, BlockPos blockpos1) {
      BlockPos blockpos2 = calculateRelativePosition(structureplacesettings, blockpos);
      BlockPos blockpos3 = calculateRelativePosition(structureplacesettings1, blockpos1);
      return blockpos2.subtract(blockpos3);
   }

   public static BlockPos calculateRelativePosition(StructurePlaceSettings structureplacesettings, BlockPos blockpos) {
      return transform(blockpos, structureplacesettings.getMirror(), structureplacesettings.getRotation(), structureplacesettings.getRotationPivot());
   }

   public boolean placeInWorld(ServerLevelAccessor serverlevelaccessor, BlockPos blockpos, BlockPos blockpos1, StructurePlaceSettings structureplacesettings, RandomSource randomsource, int i) {
      if (this.palettes.isEmpty()) {
         return false;
      } else {
         List<StructureTemplate.StructureBlockInfo> list = structureplacesettings.getRandomPalette(this.palettes, blockpos).blocks();
         if ((!list.isEmpty() || !structureplacesettings.isIgnoreEntities() && !this.entityInfoList.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            BoundingBox boundingbox = structureplacesettings.getBoundingBox();
            List<BlockPos> list1 = Lists.newArrayListWithCapacity(structureplacesettings.shouldKeepLiquids() ? list.size() : 0);
            List<BlockPos> list2 = Lists.newArrayListWithCapacity(structureplacesettings.shouldKeepLiquids() ? list.size() : 0);
            List<Pair<BlockPos, CompoundTag>> list3 = Lists.newArrayListWithCapacity(list.size());
            int j = Integer.MAX_VALUE;
            int k = Integer.MAX_VALUE;
            int l = Integer.MAX_VALUE;
            int i1 = Integer.MIN_VALUE;
            int j1 = Integer.MIN_VALUE;
            int k1 = Integer.MIN_VALUE;

            for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : processBlockInfos(serverlevelaccessor, blockpos, blockpos1, structureplacesettings, list)) {
               BlockPos blockpos2 = structuretemplate_structureblockinfo.pos;
               if (boundingbox == null || boundingbox.isInside(blockpos2)) {
                  FluidState fluidstate = structureplacesettings.shouldKeepLiquids() ? serverlevelaccessor.getFluidState(blockpos2) : null;
                  BlockState blockstate = structuretemplate_structureblockinfo.state.mirror(structureplacesettings.getMirror()).rotate(structureplacesettings.getRotation());
                  if (structuretemplate_structureblockinfo.nbt != null) {
                     BlockEntity blockentity = serverlevelaccessor.getBlockEntity(blockpos2);
                     Clearable.tryClear(blockentity);
                     serverlevelaccessor.setBlock(blockpos2, Blocks.BARRIER.defaultBlockState(), 20);
                  }

                  if (serverlevelaccessor.setBlock(blockpos2, blockstate, i)) {
                     j = Math.min(j, blockpos2.getX());
                     k = Math.min(k, blockpos2.getY());
                     l = Math.min(l, blockpos2.getZ());
                     i1 = Math.max(i1, blockpos2.getX());
                     j1 = Math.max(j1, blockpos2.getY());
                     k1 = Math.max(k1, blockpos2.getZ());
                     list3.add(Pair.of(blockpos2, structuretemplate_structureblockinfo.nbt));
                     if (structuretemplate_structureblockinfo.nbt != null) {
                        BlockEntity blockentity1 = serverlevelaccessor.getBlockEntity(blockpos2);
                        if (blockentity1 != null) {
                           if (blockentity1 instanceof RandomizableContainerBlockEntity) {
                              structuretemplate_structureblockinfo.nbt.putLong("LootTableSeed", randomsource.nextLong());
                           }

                           blockentity1.load(structuretemplate_structureblockinfo.nbt);
                        }
                     }

                     if (fluidstate != null) {
                        if (blockstate.getFluidState().isSource()) {
                           list2.add(blockpos2);
                        } else if (blockstate.getBlock() instanceof LiquidBlockContainer) {
                           ((LiquidBlockContainer)blockstate.getBlock()).placeLiquid(serverlevelaccessor, blockpos2, blockstate, fluidstate);
                           if (!fluidstate.isSource()) {
                              list1.add(blockpos2);
                           }
                        }
                     }
                  }
               }
            }

            boolean flag = true;
            Direction[] adirection = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

            while(flag && !list1.isEmpty()) {
               flag = false;
               Iterator<BlockPos> iterator = list1.iterator();

               while(iterator.hasNext()) {
                  BlockPos blockpos3 = iterator.next();
                  FluidState fluidstate1 = serverlevelaccessor.getFluidState(blockpos3);

                  for(int l1 = 0; l1 < adirection.length && !fluidstate1.isSource(); ++l1) {
                     BlockPos blockpos4 = blockpos3.relative(adirection[l1]);
                     FluidState fluidstate2 = serverlevelaccessor.getFluidState(blockpos4);
                     if (fluidstate2.isSource() && !list2.contains(blockpos4)) {
                        fluidstate1 = fluidstate2;
                     }
                  }

                  if (fluidstate1.isSource()) {
                     BlockState blockstate1 = serverlevelaccessor.getBlockState(blockpos3);
                     Block block = blockstate1.getBlock();
                     if (block instanceof LiquidBlockContainer) {
                        ((LiquidBlockContainer)block).placeLiquid(serverlevelaccessor, blockpos3, blockstate1, fluidstate1);
                        flag = true;
                        iterator.remove();
                     }
                  }
               }
            }

            if (j <= i1) {
               if (!structureplacesettings.getKnownShape()) {
                  DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(i1 - j + 1, j1 - k + 1, k1 - l + 1);
                  int i2 = j;
                  int j2 = k;
                  int k2 = l;

                  for(Pair<BlockPos, CompoundTag> pair : list3) {
                     BlockPos blockpos5 = pair.getFirst();
                     discretevoxelshape.fill(blockpos5.getX() - i2, blockpos5.getY() - j2, blockpos5.getZ() - k2);
                  }

                  updateShapeAtEdge(serverlevelaccessor, i, discretevoxelshape, i2, j2, k2);
               }

               for(Pair<BlockPos, CompoundTag> pair1 : list3) {
                  BlockPos blockpos6 = pair1.getFirst();
                  if (!structureplacesettings.getKnownShape()) {
                     BlockState blockstate2 = serverlevelaccessor.getBlockState(blockpos6);
                     BlockState blockstate3 = Block.updateFromNeighbourShapes(blockstate2, serverlevelaccessor, blockpos6);
                     if (blockstate2 != blockstate3) {
                        serverlevelaccessor.setBlock(blockpos6, blockstate3, i & -2 | 16);
                     }

                     serverlevelaccessor.blockUpdated(blockpos6, blockstate3.getBlock());
                  }

                  if (pair1.getSecond() != null) {
                     BlockEntity blockentity2 = serverlevelaccessor.getBlockEntity(blockpos6);
                     if (blockentity2 != null) {
                        blockentity2.setChanged();
                     }
                  }
               }
            }

            if (!structureplacesettings.isIgnoreEntities()) {
               this.placeEntities(serverlevelaccessor, blockpos, structureplacesettings.getMirror(), structureplacesettings.getRotation(), structureplacesettings.getRotationPivot(), boundingbox, structureplacesettings.shouldFinalizeEntities());
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public static void updateShapeAtEdge(LevelAccessor levelaccessor, int i, DiscreteVoxelShape discretevoxelshape, int j, int k, int l) {
      discretevoxelshape.forAllFaces((direction, i2, j2, k2) -> {
         BlockPos blockpos = new BlockPos(j + i2, k + j2, l + k2);
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate = levelaccessor.getBlockState(blockpos);
         BlockState blockstate1 = levelaccessor.getBlockState(blockpos1);
         BlockState blockstate2 = blockstate.updateShape(direction, blockstate1, levelaccessor, blockpos, blockpos1);
         if (blockstate != blockstate2) {
            levelaccessor.setBlock(blockpos, blockstate2, i & -2);
         }

         BlockState blockstate3 = blockstate1.updateShape(direction.getOpposite(), blockstate2, levelaccessor, blockpos1, blockpos);
         if (blockstate1 != blockstate3) {
            levelaccessor.setBlock(blockpos1, blockstate3, i & -2);
         }

      });
   }

   public static List<StructureTemplate.StructureBlockInfo> processBlockInfos(ServerLevelAccessor serverlevelaccessor, BlockPos blockpos, BlockPos blockpos1, StructurePlaceSettings structureplacesettings, List<StructureTemplate.StructureBlockInfo> list) {
      List<StructureTemplate.StructureBlockInfo> list1 = new ArrayList<>();
      List<StructureTemplate.StructureBlockInfo> list2 = new ArrayList<>();

      for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : list) {
         BlockPos blockpos2 = calculateRelativePosition(structureplacesettings, structuretemplate_structureblockinfo.pos).offset(blockpos);
         StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1 = new StructureTemplate.StructureBlockInfo(blockpos2, structuretemplate_structureblockinfo.state, structuretemplate_structureblockinfo.nbt != null ? structuretemplate_structureblockinfo.nbt.copy() : null);

         for(Iterator<StructureProcessor> iterator = structureplacesettings.getProcessors().iterator(); structuretemplate_structureblockinfo1 != null && iterator.hasNext(); structuretemplate_structureblockinfo1 = iterator.next().processBlock(serverlevelaccessor, blockpos, blockpos1, structuretemplate_structureblockinfo, structuretemplate_structureblockinfo1, structureplacesettings)) {
         }

         if (structuretemplate_structureblockinfo1 != null) {
            list2.add(structuretemplate_structureblockinfo1);
            list1.add(structuretemplate_structureblockinfo);
         }
      }

      for(StructureProcessor structureprocessor : structureplacesettings.getProcessors()) {
         list2 = structureprocessor.finalizeProcessing(serverlevelaccessor, blockpos, blockpos1, list1, list2, structureplacesettings);
      }

      return list2;
   }

   private void placeEntities(ServerLevelAccessor serverlevelaccessor, BlockPos blockpos, Mirror mirror, Rotation rotation, BlockPos blockpos1, @Nullable BoundingBox boundingbox, boolean flag) {
      for(StructureTemplate.StructureEntityInfo structuretemplate_structureentityinfo : this.entityInfoList) {
         BlockPos blockpos2 = transform(structuretemplate_structureentityinfo.blockPos, mirror, rotation, blockpos1).offset(blockpos);
         if (boundingbox == null || boundingbox.isInside(blockpos2)) {
            CompoundTag compoundtag = structuretemplate_structureentityinfo.nbt.copy();
            Vec3 vec3 = transform(structuretemplate_structureentityinfo.pos, mirror, rotation, blockpos1);
            Vec3 vec31 = vec3.add((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            ListTag listtag = new ListTag();
            listtag.add(DoubleTag.valueOf(vec31.x));
            listtag.add(DoubleTag.valueOf(vec31.y));
            listtag.add(DoubleTag.valueOf(vec31.z));
            compoundtag.put("Pos", listtag);
            compoundtag.remove("UUID");
            createEntityIgnoreException(serverlevelaccessor, compoundtag).ifPresent((entity) -> {
               float f = entity.rotate(rotation);
               f += entity.mirror(mirror) - entity.getYRot();
               entity.moveTo(vec31.x, vec31.y, vec31.z, f, entity.getXRot());
               if (flag && entity instanceof Mob) {
                  ((Mob)entity).finalizeSpawn(serverlevelaccessor, serverlevelaccessor.getCurrentDifficultyAt(BlockPos.containing(vec31)), MobSpawnType.STRUCTURE, (SpawnGroupData)null, compoundtag);
               }

               serverlevelaccessor.addFreshEntityWithPassengers(entity);
            });
         }
      }

   }

   private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor serverlevelaccessor, CompoundTag compoundtag) {
      try {
         return EntityType.create(compoundtag, serverlevelaccessor.getLevel());
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public Vec3i getSize(Rotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
         default:
            return this.size;
      }
   }

   public static BlockPos transform(BlockPos blockpos, Mirror mirror, Rotation rotation, BlockPos blockpos1) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      boolean flag = true;
      switch (mirror) {
         case LEFT_RIGHT:
            k = -k;
            break;
         case FRONT_BACK:
            i = -i;
            break;
         default:
            flag = false;
      }

      int l = blockpos1.getX();
      int i1 = blockpos1.getZ();
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            return new BlockPos(l - i1 + k, j, l + i1 - i);
         case CLOCKWISE_90:
            return new BlockPos(l + i1 - k, j, i1 - l + i);
         case CLOCKWISE_180:
            return new BlockPos(l + l - i, j, i1 + i1 - k);
         default:
            return flag ? new BlockPos(i, j, k) : blockpos;
      }
   }

   public static Vec3 transform(Vec3 vec3, Mirror mirror, Rotation rotation, BlockPos blockpos) {
      double d0 = vec3.x;
      double d1 = vec3.y;
      double d2 = vec3.z;
      boolean flag = true;
      switch (mirror) {
         case LEFT_RIGHT:
            d2 = 1.0D - d2;
            break;
         case FRONT_BACK:
            d0 = 1.0D - d0;
            break;
         default:
            flag = false;
      }

      int i = blockpos.getX();
      int j = blockpos.getZ();
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            return new Vec3((double)(i - j) + d2, d1, (double)(i + j + 1) - d0);
         case CLOCKWISE_90:
            return new Vec3((double)(i + j + 1) - d2, d1, (double)(j - i) + d0);
         case CLOCKWISE_180:
            return new Vec3((double)(i + i + 1) - d0, d1, (double)(j + j + 1) - d2);
         default:
            return flag ? new Vec3(d0, d1, d2) : vec3;
      }
   }

   public BlockPos getZeroPositionWithTransform(BlockPos blockpos, Mirror mirror, Rotation rotation) {
      return getZeroPositionWithTransform(blockpos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
   }

   public static BlockPos getZeroPositionWithTransform(BlockPos blockpos, Mirror mirror, Rotation rotation, int i, int j) {
      --i;
      --j;
      int k = mirror == Mirror.FRONT_BACK ? i : 0;
      int l = mirror == Mirror.LEFT_RIGHT ? j : 0;
      BlockPos blockpos1 = blockpos;
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            blockpos1 = blockpos.offset(l, 0, i - k);
            break;
         case CLOCKWISE_90:
            blockpos1 = blockpos.offset(j - l, 0, k);
            break;
         case CLOCKWISE_180:
            blockpos1 = blockpos.offset(i - k, 0, j - l);
            break;
         case NONE:
            blockpos1 = blockpos.offset(k, 0, l);
      }

      return blockpos1;
   }

   public BoundingBox getBoundingBox(StructurePlaceSettings structureplacesettings, BlockPos blockpos) {
      return this.getBoundingBox(blockpos, structureplacesettings.getRotation(), structureplacesettings.getRotationPivot(), structureplacesettings.getMirror());
   }

   public BoundingBox getBoundingBox(BlockPos blockpos, Rotation rotation, BlockPos blockpos1, Mirror mirror) {
      return getBoundingBox(blockpos, rotation, blockpos1, mirror, this.size);
   }

   @VisibleForTesting
   protected static BoundingBox getBoundingBox(BlockPos blockpos, Rotation rotation, BlockPos blockpos1, Mirror mirror, Vec3i vec3i) {
      Vec3i vec3i1 = vec3i.offset(-1, -1, -1);
      BlockPos blockpos2 = transform(BlockPos.ZERO, mirror, rotation, blockpos1);
      BlockPos blockpos3 = transform(BlockPos.ZERO.offset(vec3i1), mirror, rotation, blockpos1);
      return BoundingBox.fromCorners(blockpos2, blockpos3).move(blockpos);
   }

   public CompoundTag save(CompoundTag compoundtag) {
      if (this.palettes.isEmpty()) {
         compoundtag.put("blocks", new ListTag());
         compoundtag.put("palette", new ListTag());
      } else {
         List<StructureTemplate.SimplePalette> list = Lists.newArrayList();
         StructureTemplate.SimplePalette structuretemplate_simplepalette = new StructureTemplate.SimplePalette();
         list.add(structuretemplate_simplepalette);

         for(int i = 1; i < this.palettes.size(); ++i) {
            list.add(new StructureTemplate.SimplePalette());
         }

         ListTag listtag = new ListTag();
         List<StructureTemplate.StructureBlockInfo> list1 = this.palettes.get(0).blocks();

         for(int j = 0; j < list1.size(); ++j) {
            StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo = list1.get(j);
            CompoundTag compoundtag1 = new CompoundTag();
            compoundtag1.put("pos", this.newIntegerList(structuretemplate_structureblockinfo.pos.getX(), structuretemplate_structureblockinfo.pos.getY(), structuretemplate_structureblockinfo.pos.getZ()));
            int k = structuretemplate_simplepalette.idFor(structuretemplate_structureblockinfo.state);
            compoundtag1.putInt("state", k);
            if (structuretemplate_structureblockinfo.nbt != null) {
               compoundtag1.put("nbt", structuretemplate_structureblockinfo.nbt);
            }

            listtag.add(compoundtag1);

            for(int l = 1; l < this.palettes.size(); ++l) {
               StructureTemplate.SimplePalette structuretemplate_simplepalette1 = list.get(l);
               structuretemplate_simplepalette1.addMapping((this.palettes.get(l).blocks().get(j)).state, k);
            }
         }

         compoundtag.put("blocks", listtag);
         if (list.size() == 1) {
            ListTag listtag1 = new ListTag();

            for(BlockState blockstate : structuretemplate_simplepalette) {
               listtag1.add(NbtUtils.writeBlockState(blockstate));
            }

            compoundtag.put("palette", listtag1);
         } else {
            ListTag listtag2 = new ListTag();

            for(StructureTemplate.SimplePalette structuretemplate_simplepalette2 : list) {
               ListTag listtag3 = new ListTag();

               for(BlockState blockstate1 : structuretemplate_simplepalette2) {
                  listtag3.add(NbtUtils.writeBlockState(blockstate1));
               }

               listtag2.add(listtag3);
            }

            compoundtag.put("palettes", listtag2);
         }
      }

      ListTag listtag4 = new ListTag();

      for(StructureTemplate.StructureEntityInfo structuretemplate_structureentityinfo : this.entityInfoList) {
         CompoundTag compoundtag2 = new CompoundTag();
         compoundtag2.put("pos", this.newDoubleList(structuretemplate_structureentityinfo.pos.x, structuretemplate_structureentityinfo.pos.y, structuretemplate_structureentityinfo.pos.z));
         compoundtag2.put("blockPos", this.newIntegerList(structuretemplate_structureentityinfo.blockPos.getX(), structuretemplate_structureentityinfo.blockPos.getY(), structuretemplate_structureentityinfo.blockPos.getZ()));
         if (structuretemplate_structureentityinfo.nbt != null) {
            compoundtag2.put("nbt", structuretemplate_structureentityinfo.nbt);
         }

         listtag4.add(compoundtag2);
      }

      compoundtag.put("entities", listtag4);
      compoundtag.put("size", this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
      return NbtUtils.addCurrentDataVersion(compoundtag);
   }

   public void load(HolderGetter<Block> holdergetter, CompoundTag compoundtag) {
      this.palettes.clear();
      this.entityInfoList.clear();
      ListTag listtag = compoundtag.getList("size", 3);
      this.size = new Vec3i(listtag.getInt(0), listtag.getInt(1), listtag.getInt(2));
      ListTag listtag1 = compoundtag.getList("blocks", 10);
      if (compoundtag.contains("palettes", 9)) {
         ListTag listtag2 = compoundtag.getList("palettes", 9);

         for(int i = 0; i < listtag2.size(); ++i) {
            this.loadPalette(holdergetter, listtag2.getList(i), listtag1);
         }
      } else {
         this.loadPalette(holdergetter, compoundtag.getList("palette", 10), listtag1);
      }

      ListTag listtag3 = compoundtag.getList("entities", 10);

      for(int j = 0; j < listtag3.size(); ++j) {
         CompoundTag compoundtag1 = listtag3.getCompound(j);
         ListTag listtag4 = compoundtag1.getList("pos", 6);
         Vec3 vec3 = new Vec3(listtag4.getDouble(0), listtag4.getDouble(1), listtag4.getDouble(2));
         ListTag listtag5 = compoundtag1.getList("blockPos", 3);
         BlockPos blockpos = new BlockPos(listtag5.getInt(0), listtag5.getInt(1), listtag5.getInt(2));
         if (compoundtag1.contains("nbt")) {
            CompoundTag compoundtag2 = compoundtag1.getCompound("nbt");
            this.entityInfoList.add(new StructureTemplate.StructureEntityInfo(vec3, blockpos, compoundtag2));
         }
      }

   }

   private void loadPalette(HolderGetter<Block> holdergetter, ListTag listtag, ListTag listtag1) {
      StructureTemplate.SimplePalette structuretemplate_simplepalette = new StructureTemplate.SimplePalette();

      for(int i = 0; i < listtag.size(); ++i) {
         structuretemplate_simplepalette.addMapping(NbtUtils.readBlockState(holdergetter, listtag.getCompound(i)), i);
      }

      List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
      List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();
      List<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();

      for(int j = 0; j < listtag1.size(); ++j) {
         CompoundTag compoundtag = listtag1.getCompound(j);
         ListTag listtag2 = compoundtag.getList("pos", 3);
         BlockPos blockpos = new BlockPos(listtag2.getInt(0), listtag2.getInt(1), listtag2.getInt(2));
         BlockState blockstate = structuretemplate_simplepalette.stateFor(compoundtag.getInt("state"));
         CompoundTag compoundtag1;
         if (compoundtag.contains("nbt")) {
            compoundtag1 = compoundtag.getCompound("nbt");
         } else {
            compoundtag1 = null;
         }

         StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos, blockstate, compoundtag1);
         addToLists(structuretemplate_structureblockinfo, list, list1, list2);
      }

      List<StructureTemplate.StructureBlockInfo> list3 = buildInfoList(list, list1, list2);
      this.palettes.add(new StructureTemplate.Palette(list3));
   }

   private ListTag newIntegerList(int... aint) {
      ListTag listtag = new ListTag();

      for(int i : aint) {
         listtag.add(IntTag.valueOf(i));
      }

      return listtag;
   }

   private ListTag newDoubleList(double... adouble) {
      ListTag listtag = new ListTag();

      for(double d0 : adouble) {
         listtag.add(DoubleTag.valueOf(d0));
      }

      return listtag;
   }

   public static final class Palette {
      private final List<StructureTemplate.StructureBlockInfo> blocks;
      private final Map<Block, List<StructureTemplate.StructureBlockInfo>> cache = Maps.newHashMap();

      Palette(List<StructureTemplate.StructureBlockInfo> list) {
         this.blocks = list;
      }

      public List<StructureTemplate.StructureBlockInfo> blocks() {
         return this.blocks;
      }

      public List<StructureTemplate.StructureBlockInfo> blocks(Block block) {
         return this.cache.computeIfAbsent(block, (block1) -> this.blocks.stream().filter((structuretemplate_structureblockinfo) -> structuretemplate_structureblockinfo.state.is(block1)).collect(Collectors.toList()));
      }
   }

   static class SimplePalette implements Iterable<BlockState> {
      public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
      private final IdMapper<BlockState> ids = new IdMapper<>(16);
      private int lastId;

      public int idFor(BlockState blockstate) {
         int i = this.ids.getId(blockstate);
         if (i == -1) {
            i = this.lastId++;
            this.ids.addMapping(blockstate, i);
         }

         return i;
      }

      @Nullable
      public BlockState stateFor(int i) {
         BlockState blockstate = this.ids.byId(i);
         return blockstate == null ? DEFAULT_BLOCK_STATE : blockstate;
      }

      public Iterator<BlockState> iterator() {
         return this.ids.iterator();
      }

      public void addMapping(BlockState blockstate, int i) {
         this.ids.addMapping(blockstate, i);
      }
   }

   public static record StructureBlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag nbt) {
      final BlockPos pos;
      final BlockState state;
      @Nullable
      final CompoundTag nbt;

      public String toString() {
         return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
      }
   }

   public static class StructureEntityInfo {
      public final Vec3 pos;
      public final BlockPos blockPos;
      public final CompoundTag nbt;

      public StructureEntityInfo(Vec3 vec3, BlockPos blockpos, CompoundTag compoundtag) {
         this.pos = vec3;
         this.blockPos = blockpos;
         this.nbt = compoundtag;
      }
   }
}
