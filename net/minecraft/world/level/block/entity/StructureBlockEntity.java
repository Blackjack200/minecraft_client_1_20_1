package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureBlockEntity extends BlockEntity {
   private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
   public static final int MAX_OFFSET_PER_AXIS = 48;
   public static final int MAX_SIZE_PER_AXIS = 48;
   public static final String AUTHOR_TAG = "author";
   private ResourceLocation structureName;
   private String author = "";
   private String metaData = "";
   private BlockPos structurePos = new BlockPos(0, 1, 0);
   private Vec3i structureSize = Vec3i.ZERO;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private StructureMode mode;
   private boolean ignoreEntities = true;
   private boolean powered;
   private boolean showAir;
   private boolean showBoundingBox = true;
   private float integrity = 1.0F;
   private long seed;

   public StructureBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.STRUCTURE_BLOCK, blockpos, blockstate);
      this.mode = blockstate.getValue(StructureBlock.MODE);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putString("name", this.getStructureName());
      compoundtag.putString("author", this.author);
      compoundtag.putString("metadata", this.metaData);
      compoundtag.putInt("posX", this.structurePos.getX());
      compoundtag.putInt("posY", this.structurePos.getY());
      compoundtag.putInt("posZ", this.structurePos.getZ());
      compoundtag.putInt("sizeX", this.structureSize.getX());
      compoundtag.putInt("sizeY", this.structureSize.getY());
      compoundtag.putInt("sizeZ", this.structureSize.getZ());
      compoundtag.putString("rotation", this.rotation.toString());
      compoundtag.putString("mirror", this.mirror.toString());
      compoundtag.putString("mode", this.mode.toString());
      compoundtag.putBoolean("ignoreEntities", this.ignoreEntities);
      compoundtag.putBoolean("powered", this.powered);
      compoundtag.putBoolean("showair", this.showAir);
      compoundtag.putBoolean("showboundingbox", this.showBoundingBox);
      compoundtag.putFloat("integrity", this.integrity);
      compoundtag.putLong("seed", this.seed);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.setStructureName(compoundtag.getString("name"));
      this.author = compoundtag.getString("author");
      this.metaData = compoundtag.getString("metadata");
      int i = Mth.clamp(compoundtag.getInt("posX"), -48, 48);
      int j = Mth.clamp(compoundtag.getInt("posY"), -48, 48);
      int k = Mth.clamp(compoundtag.getInt("posZ"), -48, 48);
      this.structurePos = new BlockPos(i, j, k);
      int l = Mth.clamp(compoundtag.getInt("sizeX"), 0, 48);
      int i1 = Mth.clamp(compoundtag.getInt("sizeY"), 0, 48);
      int j1 = Mth.clamp(compoundtag.getInt("sizeZ"), 0, 48);
      this.structureSize = new Vec3i(l, i1, j1);

      try {
         this.rotation = Rotation.valueOf(compoundtag.getString("rotation"));
      } catch (IllegalArgumentException var11) {
         this.rotation = Rotation.NONE;
      }

      try {
         this.mirror = Mirror.valueOf(compoundtag.getString("mirror"));
      } catch (IllegalArgumentException var10) {
         this.mirror = Mirror.NONE;
      }

      try {
         this.mode = StructureMode.valueOf(compoundtag.getString("mode"));
      } catch (IllegalArgumentException var9) {
         this.mode = StructureMode.DATA;
      }

      this.ignoreEntities = compoundtag.getBoolean("ignoreEntities");
      this.powered = compoundtag.getBoolean("powered");
      this.showAir = compoundtag.getBoolean("showair");
      this.showBoundingBox = compoundtag.getBoolean("showboundingbox");
      if (compoundtag.contains("integrity")) {
         this.integrity = compoundtag.getFloat("integrity");
      } else {
         this.integrity = 1.0F;
      }

      this.seed = compoundtag.getLong("seed");
      this.updateBlockState();
   }

   private void updateBlockState() {
      if (this.level != null) {
         BlockPos blockpos = this.getBlockPos();
         BlockState blockstate = this.level.getBlockState(blockpos);
         if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(blockpos, blockstate.setValue(StructureBlock.MODE, this.mode), 2);
         }

      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public boolean usedBy(Player player) {
      if (!player.canUseGameMasterBlocks()) {
         return false;
      } else {
         if (player.getCommandSenderWorld().isClientSide) {
            player.openStructureBlock(this);
         }

         return true;
      }
   }

   public String getStructureName() {
      return this.structureName == null ? "" : this.structureName.toString();
   }

   public String getStructurePath() {
      return this.structureName == null ? "" : this.structureName.getPath();
   }

   public boolean hasStructureName() {
      return this.structureName != null;
   }

   public void setStructureName(@Nullable String s) {
      this.setStructureName(StringUtil.isNullOrEmpty(s) ? null : ResourceLocation.tryParse(s));
   }

   public void setStructureName(@Nullable ResourceLocation resourcelocation) {
      this.structureName = resourcelocation;
   }

   public void createdBy(LivingEntity livingentity) {
      this.author = livingentity.getName().getString();
   }

   public BlockPos getStructurePos() {
      return this.structurePos;
   }

   public void setStructurePos(BlockPos blockpos) {
      this.structurePos = blockpos;
   }

   public Vec3i getStructureSize() {
      return this.structureSize;
   }

   public void setStructureSize(Vec3i vec3i) {
      this.structureSize = vec3i;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public void setMirror(Mirror mirror) {
      this.mirror = mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public void setRotation(Rotation rotation) {
      this.rotation = rotation;
   }

   public String getMetaData() {
      return this.metaData;
   }

   public void setMetaData(String s) {
      this.metaData = s;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public void setMode(StructureMode structuremode) {
      this.mode = structuremode;
      BlockState blockstate = this.level.getBlockState(this.getBlockPos());
      if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
         this.level.setBlock(this.getBlockPos(), blockstate.setValue(StructureBlock.MODE, structuremode), 2);
      }

   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public void setIgnoreEntities(boolean flag) {
      this.ignoreEntities = flag;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public void setIntegrity(float f) {
      this.integrity = f;
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long i) {
      this.seed = i;
   }

   public boolean detectSize() {
      if (this.mode != StructureMode.SAVE) {
         return false;
      } else {
         BlockPos blockpos = this.getBlockPos();
         int i = 80;
         BlockPos blockpos1 = new BlockPos(blockpos.getX() - 80, this.level.getMinBuildHeight(), blockpos.getZ() - 80);
         BlockPos blockpos2 = new BlockPos(blockpos.getX() + 80, this.level.getMaxBuildHeight() - 1, blockpos.getZ() + 80);
         Stream<BlockPos> stream = this.getRelatedCorners(blockpos1, blockpos2);
         return calculateEnclosingBoundingBox(blockpos, stream).filter((boundingbox) -> {
            int j = boundingbox.maxX() - boundingbox.minX();
            int k = boundingbox.maxY() - boundingbox.minY();
            int l = boundingbox.maxZ() - boundingbox.minZ();
            if (j > 1 && k > 1 && l > 1) {
               this.structurePos = new BlockPos(boundingbox.minX() - blockpos.getX() + 1, boundingbox.minY() - blockpos.getY() + 1, boundingbox.minZ() - blockpos.getZ() + 1);
               this.structureSize = new Vec3i(j - 1, k - 1, l - 1);
               this.setChanged();
               BlockState blockstate = this.level.getBlockState(blockpos);
               this.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
               return true;
            } else {
               return false;
            }
         }).isPresent();
      }
   }

   private Stream<BlockPos> getRelatedCorners(BlockPos blockpos, BlockPos blockpos1) {
      return BlockPos.betweenClosedStream(blockpos, blockpos1).filter((blockpos2) -> this.level.getBlockState(blockpos2).is(Blocks.STRUCTURE_BLOCK)).map(this.level::getBlockEntity).filter((blockentity1) -> blockentity1 instanceof StructureBlockEntity).map((blockentity) -> (StructureBlockEntity)blockentity).filter((structureblockentity) -> structureblockentity.mode == StructureMode.CORNER && Objects.equals(this.structureName, structureblockentity.structureName)).map(BlockEntity::getBlockPos);
   }

   private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos blockpos, Stream<BlockPos> stream) {
      Iterator<BlockPos> iterator = stream.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BlockPos blockpos1 = iterator.next();
         BoundingBox boundingbox = new BoundingBox(blockpos1);
         if (iterator.hasNext()) {
            iterator.forEachRemaining(boundingbox::encapsulate);
         } else {
            boundingbox.encapsulate(blockpos);
         }

         return Optional.of(boundingbox);
      }
   }

   public boolean saveStructure() {
      return this.saveStructure(true);
   }

   public boolean saveStructure(boolean flag) {
      if (this.mode == StructureMode.SAVE && !this.level.isClientSide && this.structureName != null) {
         BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
         ServerLevel serverlevel = (ServerLevel)this.level;
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

         StructureTemplate structuretemplate;
         try {
            structuretemplate = structuretemplatemanager.getOrCreate(this.structureName);
         } catch (ResourceLocationException var8) {
            return false;
         }

         structuretemplate.fillFromWorld(this.level, blockpos, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
         structuretemplate.setAuthor(this.author);
         if (flag) {
            try {
               return structuretemplatemanager.save(this.structureName);
            } catch (ResourceLocationException var7) {
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean loadStructure(ServerLevel serverlevel) {
      return this.loadStructure(serverlevel, true);
   }

   public static RandomSource createRandom(long i) {
      return i == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(i);
   }

   public boolean loadStructure(ServerLevel serverlevel, boolean flag) {
      if (this.mode == StructureMode.LOAD && this.structureName != null) {
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

         Optional<StructureTemplate> optional;
         try {
            optional = structuretemplatemanager.get(this.structureName);
         } catch (ResourceLocationException var6) {
            return false;
         }

         return !optional.isPresent() ? false : this.loadStructure(serverlevel, flag, optional.get());
      } else {
         return false;
      }
   }

   public boolean loadStructure(ServerLevel serverlevel, boolean flag, StructureTemplate structuretemplate) {
      BlockPos blockpos = this.getBlockPos();
      if (!StringUtil.isNullOrEmpty(structuretemplate.getAuthor())) {
         this.author = structuretemplate.getAuthor();
      }

      Vec3i vec3i = structuretemplate.getSize();
      boolean flag1 = this.structureSize.equals(vec3i);
      if (!flag1) {
         this.structureSize = vec3i;
         this.setChanged();
         BlockState blockstate = serverlevel.getBlockState(blockpos);
         serverlevel.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
      }

      if (flag && !flag1) {
         return false;
      } else {
         StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
         if (this.integrity < 1.0F) {
            structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
         }

         BlockPos blockpos1 = blockpos.offset(this.structurePos);
         structuretemplate.placeInWorld(serverlevel, blockpos1, blockpos1, structureplacesettings, createRandom(this.seed), 2);
         return true;
      }
   }

   public void unloadStructure() {
      if (this.structureName != null) {
         ServerLevel serverlevel = (ServerLevel)this.level;
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();
         structuretemplatemanager.remove(this.structureName);
      }
   }

   public boolean isStructureLoadable() {
      if (this.mode == StructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
         ServerLevel serverlevel = (ServerLevel)this.level;
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

         try {
            return structuretemplatemanager.get(this.structureName).isPresent();
         } catch (ResourceLocationException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isPowered() {
      return this.powered;
   }

   public void setPowered(boolean flag) {
      this.powered = flag;
   }

   public boolean getShowAir() {
      return this.showAir;
   }

   public void setShowAir(boolean flag) {
      this.showAir = flag;
   }

   public boolean getShowBoundingBox() {
      return this.showBoundingBox;
   }

   public void setShowBoundingBox(boolean flag) {
      this.showBoundingBox = flag;
   }

   public static enum UpdateType {
      UPDATE_DATA,
      SAVE_AREA,
      LOAD_AREA,
      SCAN_AREA;
   }
}
