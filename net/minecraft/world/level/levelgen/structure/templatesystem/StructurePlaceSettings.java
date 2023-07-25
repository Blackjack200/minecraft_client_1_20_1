package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructurePlaceSettings {
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private BlockPos rotationPivot = BlockPos.ZERO;
   private boolean ignoreEntities;
   @Nullable
   private BoundingBox boundingBox;
   private boolean keepLiquids = true;
   @Nullable
   private RandomSource random;
   private int palette;
   private final List<StructureProcessor> processors = Lists.newArrayList();
   private boolean knownShape;
   private boolean finalizeEntities;

   public StructurePlaceSettings copy() {
      StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
      structureplacesettings.mirror = this.mirror;
      structureplacesettings.rotation = this.rotation;
      structureplacesettings.rotationPivot = this.rotationPivot;
      structureplacesettings.ignoreEntities = this.ignoreEntities;
      structureplacesettings.boundingBox = this.boundingBox;
      structureplacesettings.keepLiquids = this.keepLiquids;
      structureplacesettings.random = this.random;
      structureplacesettings.palette = this.palette;
      structureplacesettings.processors.addAll(this.processors);
      structureplacesettings.knownShape = this.knownShape;
      structureplacesettings.finalizeEntities = this.finalizeEntities;
      return structureplacesettings;
   }

   public StructurePlaceSettings setMirror(Mirror mirror) {
      this.mirror = mirror;
      return this;
   }

   public StructurePlaceSettings setRotation(Rotation rotation) {
      this.rotation = rotation;
      return this;
   }

   public StructurePlaceSettings setRotationPivot(BlockPos blockpos) {
      this.rotationPivot = blockpos;
      return this;
   }

   public StructurePlaceSettings setIgnoreEntities(boolean flag) {
      this.ignoreEntities = flag;
      return this;
   }

   public StructurePlaceSettings setBoundingBox(BoundingBox boundingbox) {
      this.boundingBox = boundingbox;
      return this;
   }

   public StructurePlaceSettings setRandom(@Nullable RandomSource randomsource) {
      this.random = randomsource;
      return this;
   }

   public StructurePlaceSettings setKeepLiquids(boolean flag) {
      this.keepLiquids = flag;
      return this;
   }

   public StructurePlaceSettings setKnownShape(boolean flag) {
      this.knownShape = flag;
      return this;
   }

   public StructurePlaceSettings clearProcessors() {
      this.processors.clear();
      return this;
   }

   public StructurePlaceSettings addProcessor(StructureProcessor structureprocessor) {
      this.processors.add(structureprocessor);
      return this;
   }

   public StructurePlaceSettings popProcessor(StructureProcessor structureprocessor) {
      this.processors.remove(structureprocessor);
      return this;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public BlockPos getRotationPivot() {
      return this.rotationPivot;
   }

   public RandomSource getRandom(@Nullable BlockPos blockpos) {
      if (this.random != null) {
         return this.random;
      } else {
         return blockpos == null ? RandomSource.create(Util.getMillis()) : RandomSource.create(Mth.getSeed(blockpos));
      }
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   @Nullable
   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public boolean getKnownShape() {
      return this.knownShape;
   }

   public List<StructureProcessor> getProcessors() {
      return this.processors;
   }

   public boolean shouldKeepLiquids() {
      return this.keepLiquids;
   }

   public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> list, @Nullable BlockPos blockpos) {
      int i = list.size();
      if (i == 0) {
         throw new IllegalStateException("No palettes");
      } else {
         return list.get(this.getRandom(blockpos).nextInt(i));
      }
   }

   public StructurePlaceSettings setFinalizeEntities(boolean flag) {
      this.finalizeEntities = flag;
      return this;
   }

   public boolean shouldFinalizeEntities() {
      return this.finalizeEntities;
   }
}
