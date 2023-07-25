package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class EndCityPieces {
   private static final int MAX_GEN_DEPTH = 8;
   static final EndCityPieces.SectionGenerator HOUSE_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
      public void init() {
      }

      public boolean generate(StructureTemplateManager structuretemplatemanager, int i, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, List<StructurePiece> list, RandomSource randomsource) {
         if (i > 8) {
            return false;
         } else {
            Rotation rotation = endcitypieces_endcitypiece.placeSettings().getRotation();
            EndCityPieces.EndCityPiece endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece, blockpos, "base_floor", rotation, true));
            int j = randomsource.nextInt(3);
            if (j == 0) {
               EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 4, -1), "base_roof", rotation, true));
            } else if (j == 1) {
               endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false));
               endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 8, -1), "second_roof", rotation, false));
               EndCityPieces.recursiveChildren(structuretemplatemanager, EndCityPieces.TOWER_GENERATOR, i + 1, endcitypieces_endcitypiece1, (BlockPos)null, list, randomsource);
            } else if (j == 2) {
               endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false));
               endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 4, -1), "third_floor_2", rotation, false));
               endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
               EndCityPieces.recursiveChildren(structuretemplatemanager, EndCityPieces.TOWER_GENERATOR, i + 1, endcitypieces_endcitypiece1, (BlockPos)null, list, randomsource);
            }

            return true;
         }
      }
   };
   static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(new Tuple<>(Rotation.NONE, new BlockPos(1, -1, 0)), new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6)));
   static final EndCityPieces.SectionGenerator TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
      public void init() {
      }

      public boolean generate(StructureTemplateManager structuretemplatemanager, int i, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, List<StructurePiece> list, RandomSource randomsource) {
         Rotation rotation = endcitypieces_endcitypiece.placeSettings().getRotation();
         EndCityPieces.EndCityPiece endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece, new BlockPos(3 + randomsource.nextInt(2), -3, 3 + randomsource.nextInt(2)), "tower_base", rotation, true));
         endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, 7, 0), "tower_piece", rotation, true));
         EndCityPieces.EndCityPiece endcitypieces_endcitypiece2 = randomsource.nextInt(3) == 0 ? endcitypieces_endcitypiece1 : null;
         int j = 1 + randomsource.nextInt(3);

         for(int k = 0; k < j; ++k) {
            endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, 4, 0), "tower_piece", rotation, true));
            if (k < j - 1 && randomsource.nextBoolean()) {
               endcitypieces_endcitypiece2 = endcitypieces_endcitypiece1;
            }
         }

         if (endcitypieces_endcitypiece2 != null) {
            for(Tuple<Rotation, BlockPos> tuple : EndCityPieces.TOWER_BRIDGES) {
               if (randomsource.nextBoolean()) {
                  EndCityPieces.EndCityPiece endcitypieces_endcitypiece3 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece2, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true));
                  EndCityPieces.recursiveChildren(structuretemplatemanager, EndCityPieces.TOWER_BRIDGE_GENERATOR, i + 1, endcitypieces_endcitypiece3, (BlockPos)null, list, randomsource);
               }
            }

            EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
         } else {
            if (i != 7) {
               return EndCityPieces.recursiveChildren(structuretemplatemanager, EndCityPieces.FAT_TOWER_GENERATOR, i + 1, endcitypieces_endcitypiece1, (BlockPos)null, list, randomsource);
            }

            EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
         }

         return true;
      }
   };
   static final EndCityPieces.SectionGenerator TOWER_BRIDGE_GENERATOR = new EndCityPieces.SectionGenerator() {
      public boolean shipCreated;

      public void init() {
         this.shipCreated = false;
      }

      public boolean generate(StructureTemplateManager structuretemplatemanager, int i, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, List<StructurePiece> list, RandomSource randomsource) {
         Rotation rotation = endcitypieces_endcitypiece.placeSettings().getRotation();
         int j = randomsource.nextInt(4) + 1;
         EndCityPieces.EndCityPiece endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece, new BlockPos(0, 0, -4), "bridge_piece", rotation, true));
         endcitypieces_endcitypiece1.setGenDepth(-1);
         int k = 0;

         for(int l = 0; l < j; ++l) {
            if (randomsource.nextBoolean()) {
               endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, k, -4), "bridge_piece", rotation, true));
               k = 0;
            } else {
               if (randomsource.nextBoolean()) {
                  endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, k, -4), "bridge_steep_stairs", rotation, true));
               } else {
                  endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, k, -8), "bridge_gentle_stairs", rotation, true));
               }

               k = 4;
            }
         }

         if (!this.shipCreated && randomsource.nextInt(10 - i) == 0) {
            EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-8 + randomsource.nextInt(8), k, -70 + randomsource.nextInt(10)), "ship", rotation, true));
            this.shipCreated = true;
         } else if (!EndCityPieces.recursiveChildren(structuretemplatemanager, EndCityPieces.HOUSE_TOWER_GENERATOR, i + 1, endcitypieces_endcitypiece1, new BlockPos(-3, k + 1, -11), list, randomsource)) {
            return false;
         }

         endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(4, k, 0), "bridge_end", rotation.getRotated(Rotation.CLOCKWISE_180), true));
         endcitypieces_endcitypiece1.setGenDepth(-1);
         return true;
      }
   };
   static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(new Tuple<>(Rotation.NONE, new BlockPos(4, -1, 0)), new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12)));
   static final EndCityPieces.SectionGenerator FAT_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
      public void init() {
      }

      public boolean generate(StructureTemplateManager structuretemplatemanager, int i, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, List<StructurePiece> list, RandomSource randomsource) {
         Rotation rotation = endcitypieces_endcitypiece.placeSettings().getRotation();
         EndCityPieces.EndCityPiece endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece, new BlockPos(-3, 4, -3), "fat_tower_base", rotation, true));
         endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, 4, 0), "fat_tower_middle", rotation, true));

         for(int j = 0; j < 2 && randomsource.nextInt(3) != 0; ++j) {
            endcitypieces_endcitypiece1 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(0, 8, 0), "fat_tower_middle", rotation, true));

            for(Tuple<Rotation, BlockPos> tuple : EndCityPieces.FAT_TOWER_BRIDGES) {
               if (randomsource.nextBoolean()) {
                  EndCityPieces.EndCityPiece endcitypieces_endcitypiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true));
                  EndCityPieces.recursiveChildren(structuretemplatemanager, EndCityPieces.TOWER_BRIDGE_GENERATOR, i + 1, endcitypieces_endcitypiece2, (BlockPos)null, list, randomsource);
               }
            }
         }

         EndCityPieces.addHelper(list, EndCityPieces.addPiece(structuretemplatemanager, endcitypieces_endcitypiece1, new BlockPos(-2, 8, -2), "fat_tower_top", rotation, true));
         return true;
      }
   };

   static EndCityPieces.EndCityPiece addPiece(StructureTemplateManager structuretemplatemanager, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, String s, Rotation rotation, boolean flag) {
      EndCityPieces.EndCityPiece endcitypieces_endcitypiece1 = new EndCityPieces.EndCityPiece(structuretemplatemanager, s, endcitypieces_endcitypiece.templatePosition(), rotation, flag);
      BlockPos blockpos1 = endcitypieces_endcitypiece.template().calculateConnectedPosition(endcitypieces_endcitypiece.placeSettings(), blockpos, endcitypieces_endcitypiece1.placeSettings(), BlockPos.ZERO);
      endcitypieces_endcitypiece1.move(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
      return endcitypieces_endcitypiece1;
   }

   public static void startHouseTower(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, List<StructurePiece> list, RandomSource randomsource) {
      FAT_TOWER_GENERATOR.init();
      HOUSE_TOWER_GENERATOR.init();
      TOWER_BRIDGE_GENERATOR.init();
      TOWER_GENERATOR.init();
      EndCityPieces.EndCityPiece endcitypieces_endcitypiece = addHelper(list, new EndCityPieces.EndCityPiece(structuretemplatemanager, "base_floor", blockpos, rotation, true));
      endcitypieces_endcitypiece = addHelper(list, addPiece(structuretemplatemanager, endcitypieces_endcitypiece, new BlockPos(-1, 0, -1), "second_floor_1", rotation, false));
      endcitypieces_endcitypiece = addHelper(list, addPiece(structuretemplatemanager, endcitypieces_endcitypiece, new BlockPos(-1, 4, -1), "third_floor_1", rotation, false));
      endcitypieces_endcitypiece = addHelper(list, addPiece(structuretemplatemanager, endcitypieces_endcitypiece, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
      recursiveChildren(structuretemplatemanager, TOWER_GENERATOR, 1, endcitypieces_endcitypiece, (BlockPos)null, list, randomsource);
   }

   static EndCityPieces.EndCityPiece addHelper(List<StructurePiece> list, EndCityPieces.EndCityPiece endcitypieces_endcitypiece) {
      list.add(endcitypieces_endcitypiece);
      return endcitypieces_endcitypiece;
   }

   static boolean recursiveChildren(StructureTemplateManager structuretemplatemanager, EndCityPieces.SectionGenerator endcitypieces_sectiongenerator, int i, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, List<StructurePiece> list, RandomSource randomsource) {
      if (i > 8) {
         return false;
      } else {
         List<StructurePiece> list1 = Lists.newArrayList();
         if (endcitypieces_sectiongenerator.generate(structuretemplatemanager, i, endcitypieces_endcitypiece, blockpos, list1, randomsource)) {
            boolean flag = false;
            int j = randomsource.nextInt();

            for(StructurePiece structurepiece : list1) {
               structurepiece.setGenDepth(j);
               StructurePiece structurepiece1 = StructurePiece.findCollisionPiece(list, structurepiece.getBoundingBox());
               if (structurepiece1 != null && structurepiece1.getGenDepth() != endcitypieces_endcitypiece.getGenDepth()) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               list.addAll(list1);
               return true;
            }
         }

         return false;
      }
   }

   public static class EndCityPiece extends TemplateStructurePiece {
      public EndCityPiece(StructureTemplateManager structuretemplatemanager, String s, BlockPos blockpos, Rotation rotation, boolean flag) {
         super(StructurePieceType.END_CITY_PIECE, 0, structuretemplatemanager, makeResourceLocation(s), s, makeSettings(flag, rotation), blockpos);
      }

      public EndCityPiece(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag) {
         super(StructurePieceType.END_CITY_PIECE, compoundtag, structuretemplatemanager, (resourcelocation) -> makeSettings(compoundtag.getBoolean("OW"), Rotation.valueOf(compoundtag.getString("Rot"))));
      }

      private static StructurePlaceSettings makeSettings(boolean flag, Rotation rotation) {
         BlockIgnoreProcessor blockignoreprocessor = flag ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
         return (new StructurePlaceSettings()).setIgnoreEntities(true).addProcessor(blockignoreprocessor).setRotation(rotation);
      }

      protected ResourceLocation makeTemplateLocation() {
         return makeResourceLocation(this.templateName);
      }

      private static ResourceLocation makeResourceLocation(String s) {
         return new ResourceLocation("end_city/" + s);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putString("Rot", this.placeSettings.getRotation().name());
         compoundtag.putBoolean("OW", this.placeSettings.getProcessors().get(0) == BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      protected void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox) {
         if (s.startsWith("Chest")) {
            BlockPos blockpos1 = blockpos.below();
            if (boundingbox.isInside(blockpos1)) {
               RandomizableContainerBlockEntity.setLootTable(serverlevelaccessor, randomsource, blockpos1, BuiltInLootTables.END_CITY_TREASURE);
            }
         } else if (boundingbox.isInside(blockpos) && Level.isInSpawnableBounds(blockpos)) {
            if (s.startsWith("Sentry")) {
               Shulker shulker = EntityType.SHULKER.create(serverlevelaccessor.getLevel());
               if (shulker != null) {
                  shulker.setPos((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D);
                  serverlevelaccessor.addFreshEntity(shulker);
               }
            } else if (s.startsWith("Elytra")) {
               ItemFrame itemframe = new ItemFrame(serverlevelaccessor.getLevel(), blockpos, this.placeSettings.getRotation().rotate(Direction.SOUTH));
               itemframe.setItem(new ItemStack(Items.ELYTRA), false);
               serverlevelaccessor.addFreshEntity(itemframe);
            }
         }

      }
   }

   interface SectionGenerator {
      void init();

      boolean generate(StructureTemplateManager structuretemplatemanager, int i, EndCityPieces.EndCityPiece endcitypieces_endcitypiece, BlockPos blockpos, List<StructurePiece> list, RandomSource randomsource);
   }
}
