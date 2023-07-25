package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
   static final Logger LOGGER = LogUtils.getLogger();

   public static Optional<Structure.GenerationStub> addPieces(Structure.GenerationContext structure_generationcontext, Holder<StructureTemplatePool> holder, Optional<ResourceLocation> optional, int i, BlockPos blockpos, boolean flag, Optional<Heightmap.Types> optional1, int j) {
      RegistryAccess registryaccess = structure_generationcontext.registryAccess();
      ChunkGenerator chunkgenerator = structure_generationcontext.chunkGenerator();
      StructureTemplateManager structuretemplatemanager = structure_generationcontext.structureTemplateManager();
      LevelHeightAccessor levelheightaccessor = structure_generationcontext.heightAccessor();
      WorldgenRandom worldgenrandom = structure_generationcontext.random();
      Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registries.TEMPLATE_POOL);
      Rotation rotation = Rotation.getRandom(worldgenrandom);
      StructureTemplatePool structuretemplatepool = holder.value();
      StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);
      if (structurepoolelement == EmptyPoolElement.INSTANCE) {
         return Optional.empty();
      } else {
         BlockPos blockpos1;
         if (optional.isPresent()) {
            ResourceLocation resourcelocation = optional.get();
            Optional<BlockPos> optional2 = getRandomNamedJigsaw(structurepoolelement, resourcelocation, blockpos, rotation, structuretemplatemanager, worldgenrandom);
            if (optional2.isEmpty()) {
               LOGGER.error("No starting jigsaw {} found in start pool {}", resourcelocation, holder.unwrapKey().map((resourcekey) -> resourcekey.location().toString()).orElse("<unregistered>"));
               return Optional.empty();
            }

            blockpos1 = optional2.get();
         } else {
            blockpos1 = blockpos;
         }

         Vec3i vec3i = blockpos1.subtract(blockpos);
         BlockPos blockpos3 = blockpos.subtract(vec3i);
         PoolElementStructurePiece poolelementstructurepiece = new PoolElementStructurePiece(structuretemplatemanager, structurepoolelement, blockpos3, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(structuretemplatemanager, blockpos3, rotation));
         BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
         int k = (boundingbox.maxX() + boundingbox.minX()) / 2;
         int l = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
         int i1;
         if (optional1.isPresent()) {
            i1 = blockpos.getY() + chunkgenerator.getFirstFreeHeight(k, l, optional1.get(), levelheightaccessor, structure_generationcontext.randomState());
         } else {
            i1 = blockpos3.getY();
         }

         int k1 = boundingbox.minY() + poolelementstructurepiece.getGroundLevelDelta();
         poolelementstructurepiece.move(0, i1 - k1, 0);
         int l1 = i1 + vec3i.getY();
         return Optional.of(new Structure.GenerationStub(new BlockPos(k, l1, l), (structurepiecesbuilder) -> {
            List<PoolElementStructurePiece> list = Lists.newArrayList();
            list.add(poolelementstructurepiece);
            if (i > 0) {
               AABB aabb = new AABB((double)(k - j), (double)(l1 - j), (double)(l - j), (double)(k + j + 1), (double)(l1 + j + 1), (double)(l + j + 1));
               VoxelShape voxelshape = Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST);
               addPieces(structure_generationcontext.randomState(), i, flag, chunkgenerator, structuretemplatemanager, levelheightaccessor, worldgenrandom, registry, poolelementstructurepiece, list, voxelshape);
               list.forEach(structurepiecesbuilder::addPiece);
            }
         }));
      }
   }

   private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement structurepoolelement, ResourceLocation resourcelocation, BlockPos blockpos, Rotation rotation, StructureTemplateManager structuretemplatemanager, WorldgenRandom worldgenrandom) {
      List<StructureTemplate.StructureBlockInfo> list = structurepoolelement.getShuffledJigsawBlocks(structuretemplatemanager, blockpos, rotation, worldgenrandom);
      Optional<BlockPos> optional = Optional.empty();

      for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : list) {
         ResourceLocation resourcelocation1 = ResourceLocation.tryParse(structuretemplate_structureblockinfo.nbt().getString("name"));
         if (resourcelocation.equals(resourcelocation1)) {
            optional = Optional.of(structuretemplate_structureblockinfo.pos());
            break;
         }
      }

      return optional;
   }

   private static void addPieces(RandomState randomstate, int i, boolean flag, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, LevelHeightAccessor levelheightaccessor, RandomSource randomsource, Registry<StructureTemplatePool> registry, PoolElementStructurePiece poolelementstructurepiece, List<PoolElementStructurePiece> list, VoxelShape voxelshape) {
      JigsawPlacement.Placer jigsawplacement_placer = new JigsawPlacement.Placer(registry, i, chunkgenerator, structuretemplatemanager, list, randomsource);
      jigsawplacement_placer.placing.addLast(new JigsawPlacement.PieceState(poolelementstructurepiece, new MutableObject<>(voxelshape), 0));

      while(!jigsawplacement_placer.placing.isEmpty()) {
         JigsawPlacement.PieceState jigsawplacement_piecestate = jigsawplacement_placer.placing.removeFirst();
         jigsawplacement_placer.tryPlacingChildren(jigsawplacement_piecestate.piece, jigsawplacement_piecestate.free, jigsawplacement_piecestate.depth, flag, levelheightaccessor, randomstate);
      }

   }

   public static boolean generateJigsaw(ServerLevel serverlevel, Holder<StructureTemplatePool> holder, ResourceLocation resourcelocation, int i, BlockPos blockpos, boolean flag) {
      ChunkGenerator chunkgenerator = serverlevel.getChunkSource().getGenerator();
      StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();
      StructureManager structuremanager = serverlevel.structureManager();
      RandomSource randomsource = serverlevel.getRandom();
      Structure.GenerationContext structure_generationcontext = new Structure.GenerationContext(serverlevel.registryAccess(), chunkgenerator, chunkgenerator.getBiomeSource(), serverlevel.getChunkSource().randomState(), structuretemplatemanager, serverlevel.getSeed(), new ChunkPos(blockpos), serverlevel, (holder1) -> true);
      Optional<Structure.GenerationStub> optional = addPieces(structure_generationcontext, holder, Optional.of(resourcelocation), i, blockpos, false, Optional.empty(), 128);
      if (optional.isPresent()) {
         StructurePiecesBuilder structurepiecesbuilder = optional.get().getPiecesBuilder();

         for(StructurePiece structurepiece : structurepiecesbuilder.build().pieces()) {
            if (structurepiece instanceof PoolElementStructurePiece) {
               PoolElementStructurePiece poolelementstructurepiece = (PoolElementStructurePiece)structurepiece;
               poolelementstructurepiece.place(serverlevel, structuremanager, chunkgenerator, randomsource, BoundingBox.infinite(), blockpos, flag);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static final class PieceState {
      final PoolElementStructurePiece piece;
      final MutableObject<VoxelShape> free;
      final int depth;

      PieceState(PoolElementStructurePiece poolelementstructurepiece, MutableObject<VoxelShape> mutableobject, int i) {
         this.piece = poolelementstructurepiece;
         this.free = mutableobject;
         this.depth = i;
      }
   }

   static final class Placer {
      private final Registry<StructureTemplatePool> pools;
      private final int maxDepth;
      private final ChunkGenerator chunkGenerator;
      private final StructureTemplateManager structureTemplateManager;
      private final List<? super PoolElementStructurePiece> pieces;
      private final RandomSource random;
      final Deque<JigsawPlacement.PieceState> placing = Queues.newArrayDeque();

      Placer(Registry<StructureTemplatePool> registry, int i, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, List<? super PoolElementStructurePiece> list, RandomSource randomsource) {
         this.pools = registry;
         this.maxDepth = i;
         this.chunkGenerator = chunkgenerator;
         this.structureTemplateManager = structuretemplatemanager;
         this.pieces = list;
         this.random = randomsource;
      }

      void tryPlacingChildren(PoolElementStructurePiece poolelementstructurepiece, MutableObject<VoxelShape> mutableobject, int i, boolean flag, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
         StructurePoolElement structurepoolelement = poolelementstructurepiece.getElement();
         BlockPos blockpos = poolelementstructurepiece.getPosition();
         Rotation rotation = poolelementstructurepiece.getRotation();
         StructureTemplatePool.Projection structuretemplatepool_projection = structurepoolelement.getProjection();
         boolean flag1 = structuretemplatepool_projection == StructureTemplatePool.Projection.RIGID;
         MutableObject<VoxelShape> mutableobject1 = new MutableObject<>();
         BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
         int j = boundingbox.minY();

         label131:
         for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : structurepoolelement.getShuffledJigsawBlocks(this.structureTemplateManager, blockpos, rotation, this.random)) {
            Direction direction = JigsawBlock.getFrontFacing(structuretemplate_structureblockinfo.state());
            BlockPos blockpos1 = structuretemplate_structureblockinfo.pos();
            BlockPos blockpos2 = blockpos1.relative(direction);
            int k = blockpos1.getY() - j;
            int l = -1;
            ResourceKey<StructureTemplatePool> resourcekey = readPoolName(structuretemplate_structureblockinfo);
            Optional<? extends Holder<StructureTemplatePool>> optional = this.pools.getHolder(resourcekey);
            if (optional.isEmpty()) {
               JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", (Object)resourcekey.location());
            } else {
               Holder<StructureTemplatePool> holder = optional.get();
               if (holder.value().size() == 0 && !holder.is(Pools.EMPTY)) {
                  JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", (Object)resourcekey.location());
               } else {
                  Holder<StructureTemplatePool> holder1 = holder.value().getFallback();
                  if (holder1.value().size() == 0 && !holder1.is(Pools.EMPTY)) {
                     JigsawPlacement.LOGGER.warn("Empty or non-existent fallback pool: {}", holder1.unwrapKey().map((resourcekey2) -> resourcekey2.location().toString()).orElse("<unregistered>"));
                  } else {
                     boolean flag2 = boundingbox.isInside(blockpos2);
                     MutableObject<VoxelShape> mutableobject2;
                     if (flag2) {
                        mutableobject2 = mutableobject1;
                        if (mutableobject1.getValue() == null) {
                           mutableobject1.setValue(Shapes.create(AABB.of(boundingbox)));
                        }
                     } else {
                        mutableobject2 = mutableobject;
                     }

                     List<StructurePoolElement> list = Lists.newArrayList();
                     if (i != this.maxDepth) {
                        list.addAll(holder.value().getShuffledTemplates(this.random));
                     }

                     list.addAll(holder1.value().getShuffledTemplates(this.random));

                     for(StructurePoolElement structurepoolelement1 : list) {
                        if (structurepoolelement1 == EmptyPoolElement.INSTANCE) {
                           break;
                        }

                        for(Rotation rotation1 : Rotation.getShuffled(this.random)) {
                           List<StructureTemplate.StructureBlockInfo> list1 = structurepoolelement1.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO, rotation1, this.random);
                           BoundingBox boundingbox1 = structurepoolelement1.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation1);
                           int j1;
                           if (flag && boundingbox1.getYSpan() <= 16) {
                              j1 = list1.stream().mapToInt((structuretemplate_structureblockinfo2) -> {
                                 if (!boundingbox1.isInside(structuretemplate_structureblockinfo2.pos().relative(JigsawBlock.getFrontFacing(structuretemplate_structureblockinfo2.state())))) {
                                    return 0;
                                 } else {
                                    ResourceKey<StructureTemplatePool> resourcekey1 = readPoolName(structuretemplate_structureblockinfo2);
                                    Optional<? extends Holder<StructureTemplatePool>> optional1 = this.pools.getHolder(resourcekey1);
                                    Optional<Holder<StructureTemplatePool>> optional2 = optional1.map((holder4) -> holder4.value().getFallback());
                                    int l4 = optional1.map((holder3) -> holder3.value().getMaxSize(this.structureTemplateManager)).orElse(0);
                                    int i5 = optional2.map((holder2) -> holder2.value().getMaxSize(this.structureTemplateManager)).orElse(0);
                                    return Math.max(l4, i5);
                                 }
                              }).max().orElse(0);
                           } else {
                              j1 = 0;
                           }

                           for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1 : list1) {
                              if (JigsawBlock.canAttach(structuretemplate_structureblockinfo, structuretemplate_structureblockinfo1)) {
                                 BlockPos blockpos3 = structuretemplate_structureblockinfo1.pos();
                                 BlockPos blockpos4 = blockpos2.subtract(blockpos3);
                                 BoundingBox boundingbox2 = structurepoolelement1.getBoundingBox(this.structureTemplateManager, blockpos4, rotation1);
                                 int k1 = boundingbox2.minY();
                                 StructureTemplatePool.Projection structuretemplatepool_projection1 = structurepoolelement1.getProjection();
                                 boolean flag3 = structuretemplatepool_projection1 == StructureTemplatePool.Projection.RIGID;
                                 int l1 = blockpos3.getY();
                                 int i2 = k - l1 + JigsawBlock.getFrontFacing(structuretemplate_structureblockinfo.state()).getStepY();
                                 int j2;
                                 if (flag1 && flag3) {
                                    j2 = j + i2;
                                 } else {
                                    if (l == -1) {
                                       l = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate);
                                    }

                                    j2 = l - l1;
                                 }

                                 int l2 = j2 - k1;
                                 BoundingBox boundingbox3 = boundingbox2.moved(0, l2, 0);
                                 BlockPos blockpos5 = blockpos4.offset(0, l2, 0);
                                 if (j1 > 0) {
                                    int i3 = Math.max(j1 + 1, boundingbox3.maxY() - boundingbox3.minY());
                                    boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(), boundingbox3.minY() + i3, boundingbox3.minZ()));
                                 }

                                 if (!Shapes.joinIsNotEmpty(mutableobject2.getValue(), Shapes.create(AABB.of(boundingbox3).deflate(0.25D)), BooleanOp.ONLY_SECOND)) {
                                    mutableobject2.setValue(Shapes.joinUnoptimized(mutableobject2.getValue(), Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST));
                                    int j3 = poolelementstructurepiece.getGroundLevelDelta();
                                    int k3;
                                    if (flag3) {
                                       k3 = j3 - i2;
                                    } else {
                                       k3 = structurepoolelement1.getGroundLevelDelta();
                                    }

                                    PoolElementStructurePiece poolelementstructurepiece1 = new PoolElementStructurePiece(this.structureTemplateManager, structurepoolelement1, blockpos5, k3, rotation1, boundingbox3);
                                    int i4;
                                    if (flag1) {
                                       i4 = j + k;
                                    } else if (flag3) {
                                       i4 = j2 + l1;
                                    } else {
                                       if (l == -1) {
                                          l = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate);
                                       }

                                       i4 = l + i2 / 2;
                                    }

                                    poolelementstructurepiece.addJunction(new JigsawJunction(blockpos2.getX(), i4 - k + j3, blockpos2.getZ(), i2, structuretemplatepool_projection1));
                                    poolelementstructurepiece1.addJunction(new JigsawJunction(blockpos1.getX(), i4 - l1 + k3, blockpos1.getZ(), -i2, structuretemplatepool_projection));
                                    this.pieces.add(poolelementstructurepiece1);
                                    if (i + 1 <= this.maxDepth) {
                                       this.placing.addLast(new JigsawPlacement.PieceState(poolelementstructurepiece1, mutableobject2, i + 1));
                                    }
                                    continue label131;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

      }

      private static ResourceKey<StructureTemplatePool> readPoolName(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo) {
         return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(structuretemplate_structureblockinfo.nbt().getString("pool")));
      }
   }
}
