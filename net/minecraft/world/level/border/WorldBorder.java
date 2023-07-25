package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder {
   public static final double MAX_SIZE = 5.9999968E7D;
   public static final double MAX_CENTER_COORDINATE = 2.9999984E7D;
   private final List<BorderChangeListener> listeners = Lists.newArrayList();
   private double damagePerBlock = 0.2D;
   private double damageSafeZone = 5.0D;
   private int warningTime = 15;
   private int warningBlocks = 5;
   private double centerX;
   private double centerZ;
   int absoluteMaxSize = 29999984;
   private WorldBorder.BorderExtent extent = new WorldBorder.StaticBorderExtent(5.9999968E7D);
   public static final WorldBorder.Settings DEFAULT_SETTINGS = new WorldBorder.Settings(0.0D, 0.0D, 0.2D, 5.0D, 5, 15, 5.9999968E7D, 0L, 0.0D);

   public boolean isWithinBounds(BlockPos blockpos) {
      return (double)(blockpos.getX() + 1) > this.getMinX() && (double)blockpos.getX() < this.getMaxX() && (double)(blockpos.getZ() + 1) > this.getMinZ() && (double)blockpos.getZ() < this.getMaxZ();
   }

   public boolean isWithinBounds(ChunkPos chunkpos) {
      return (double)chunkpos.getMaxBlockX() > this.getMinX() && (double)chunkpos.getMinBlockX() < this.getMaxX() && (double)chunkpos.getMaxBlockZ() > this.getMinZ() && (double)chunkpos.getMinBlockZ() < this.getMaxZ();
   }

   public boolean isWithinBounds(double d0, double d1) {
      return d0 > this.getMinX() && d0 < this.getMaxX() && d1 > this.getMinZ() && d1 < this.getMaxZ();
   }

   public boolean isWithinBounds(double d0, double d1, double d2) {
      return d0 > this.getMinX() - d2 && d0 < this.getMaxX() + d2 && d1 > this.getMinZ() - d2 && d1 < this.getMaxZ() + d2;
   }

   public boolean isWithinBounds(AABB aabb) {
      return aabb.maxX > this.getMinX() && aabb.minX < this.getMaxX() && aabb.maxZ > this.getMinZ() && aabb.minZ < this.getMaxZ();
   }

   public BlockPos clampToBounds(double d0, double d1, double d2) {
      return BlockPos.containing(Mth.clamp(d0, this.getMinX(), this.getMaxX()), d1, Mth.clamp(d2, this.getMinZ(), this.getMaxZ()));
   }

   public double getDistanceToBorder(Entity entity) {
      return this.getDistanceToBorder(entity.getX(), entity.getZ());
   }

   public VoxelShape getCollisionShape() {
      return this.extent.getCollisionShape();
   }

   public double getDistanceToBorder(double d0, double d1) {
      double d2 = d1 - this.getMinZ();
      double d3 = this.getMaxZ() - d1;
      double d4 = d0 - this.getMinX();
      double d5 = this.getMaxX() - d0;
      double d6 = Math.min(d4, d5);
      d6 = Math.min(d6, d2);
      return Math.min(d6, d3);
   }

   public boolean isInsideCloseToBorder(Entity entity, AABB aabb) {
      double d0 = Math.max(Mth.absMax(aabb.getXsize(), aabb.getZsize()), 1.0D);
      return this.getDistanceToBorder(entity) < d0 * 2.0D && this.isWithinBounds(entity.getX(), entity.getZ(), d0);
   }

   public BorderStatus getStatus() {
      return this.extent.getStatus();
   }

   public double getMinX() {
      return this.extent.getMinX();
   }

   public double getMinZ() {
      return this.extent.getMinZ();
   }

   public double getMaxX() {
      return this.extent.getMaxX();
   }

   public double getMaxZ() {
      return this.extent.getMaxZ();
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public void setCenter(double d0, double d1) {
      this.centerX = d0;
      this.centerZ = d1;
      this.extent.onCenterChange();

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderCenterSet(this, d0, d1);
      }

   }

   public double getSize() {
      return this.extent.getSize();
   }

   public long getLerpRemainingTime() {
      return this.extent.getLerpRemainingTime();
   }

   public double getLerpTarget() {
      return this.extent.getLerpTarget();
   }

   public void setSize(double d0) {
      this.extent = new WorldBorder.StaticBorderExtent(d0);

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderSizeSet(this, d0);
      }

   }

   public void lerpSizeBetween(double d0, double d1, long i) {
      this.extent = (WorldBorder.BorderExtent)(d0 == d1 ? new WorldBorder.StaticBorderExtent(d1) : new WorldBorder.MovingBorderExtent(d0, d1, i));

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderSizeLerping(this, d0, d1, i);
      }

   }

   protected List<BorderChangeListener> getListeners() {
      return Lists.newArrayList(this.listeners);
   }

   public void addListener(BorderChangeListener borderchangelistener) {
      this.listeners.add(borderchangelistener);
   }

   public void removeListener(BorderChangeListener borderchangelistener) {
      this.listeners.remove(borderchangelistener);
   }

   public void setAbsoluteMaxSize(int i) {
      this.absoluteMaxSize = i;
      this.extent.onAbsoluteMaxSizeChange();
   }

   public int getAbsoluteMaxSize() {
      return this.absoluteMaxSize;
   }

   public double getDamageSafeZone() {
      return this.damageSafeZone;
   }

   public void setDamageSafeZone(double d0) {
      this.damageSafeZone = d0;

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderSetDamageSafeZOne(this, d0);
      }

   }

   public double getDamagePerBlock() {
      return this.damagePerBlock;
   }

   public void setDamagePerBlock(double d0) {
      this.damagePerBlock = d0;

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderSetDamagePerBlock(this, d0);
      }

   }

   public double getLerpSpeed() {
      return this.extent.getLerpSpeed();
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public void setWarningTime(int i) {
      this.warningTime = i;

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderSetWarningTime(this, i);
      }

   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }

   public void setWarningBlocks(int i) {
      this.warningBlocks = i;

      for(BorderChangeListener borderchangelistener : this.getListeners()) {
         borderchangelistener.onBorderSetWarningBlocks(this, i);
      }

   }

   public void tick() {
      this.extent = this.extent.update();
   }

   public WorldBorder.Settings createSettings() {
      return new WorldBorder.Settings(this);
   }

   public void applySettings(WorldBorder.Settings worldborder_settings) {
      this.setCenter(worldborder_settings.getCenterX(), worldborder_settings.getCenterZ());
      this.setDamagePerBlock(worldborder_settings.getDamagePerBlock());
      this.setDamageSafeZone(worldborder_settings.getSafeZone());
      this.setWarningBlocks(worldborder_settings.getWarningBlocks());
      this.setWarningTime(worldborder_settings.getWarningTime());
      if (worldborder_settings.getSizeLerpTime() > 0L) {
         this.lerpSizeBetween(worldborder_settings.getSize(), worldborder_settings.getSizeLerpTarget(), worldborder_settings.getSizeLerpTime());
      } else {
         this.setSize(worldborder_settings.getSize());
      }

   }

   interface BorderExtent {
      double getMinX();

      double getMaxX();

      double getMinZ();

      double getMaxZ();

      double getSize();

      double getLerpSpeed();

      long getLerpRemainingTime();

      double getLerpTarget();

      BorderStatus getStatus();

      void onAbsoluteMaxSizeChange();

      void onCenterChange();

      WorldBorder.BorderExtent update();

      VoxelShape getCollisionShape();
   }

   class MovingBorderExtent implements WorldBorder.BorderExtent {
      private final double from;
      private final double to;
      private final long lerpEnd;
      private final long lerpBegin;
      private final double lerpDuration;

      MovingBorderExtent(double d0, double d1, long i) {
         this.from = d0;
         this.to = d1;
         this.lerpDuration = (double)i;
         this.lerpBegin = Util.getMillis();
         this.lerpEnd = this.lerpBegin + i;
      }

      public double getMinX() {
         return Mth.clamp(WorldBorder.this.getCenterX() - this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
      }

      public double getMinZ() {
         return Mth.clamp(WorldBorder.this.getCenterZ() - this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
      }

      public double getMaxX() {
         return Mth.clamp(WorldBorder.this.getCenterX() + this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
      }

      public double getMaxZ() {
         return Mth.clamp(WorldBorder.this.getCenterZ() + this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
      }

      public double getSize() {
         double d0 = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
         return d0 < 1.0D ? Mth.lerp(d0, this.from, this.to) : this.to;
      }

      public double getLerpSpeed() {
         return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
      }

      public long getLerpRemainingTime() {
         return this.lerpEnd - Util.getMillis();
      }

      public double getLerpTarget() {
         return this.to;
      }

      public BorderStatus getStatus() {
         return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
      }

      public void onCenterChange() {
      }

      public void onAbsoluteMaxSizeChange() {
      }

      public WorldBorder.BorderExtent update() {
         return (WorldBorder.BorderExtent)(this.getLerpRemainingTime() <= 0L ? WorldBorder.this.new StaticBorderExtent(this.to) : this);
      }

      public VoxelShape getCollisionShape() {
         return Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), BooleanOp.ONLY_FIRST);
      }
   }

   public static class Settings {
      private final double centerX;
      private final double centerZ;
      private final double damagePerBlock;
      private final double safeZone;
      private final int warningBlocks;
      private final int warningTime;
      private final double size;
      private final long sizeLerpTime;
      private final double sizeLerpTarget;

      Settings(double d0, double d1, double d2, double d3, int i, int j, double d4, long k, double d5) {
         this.centerX = d0;
         this.centerZ = d1;
         this.damagePerBlock = d2;
         this.safeZone = d3;
         this.warningBlocks = i;
         this.warningTime = j;
         this.size = d4;
         this.sizeLerpTime = k;
         this.sizeLerpTarget = d5;
      }

      Settings(WorldBorder worldborder) {
         this.centerX = worldborder.getCenterX();
         this.centerZ = worldborder.getCenterZ();
         this.damagePerBlock = worldborder.getDamagePerBlock();
         this.safeZone = worldborder.getDamageSafeZone();
         this.warningBlocks = worldborder.getWarningBlocks();
         this.warningTime = worldborder.getWarningTime();
         this.size = worldborder.getSize();
         this.sizeLerpTime = worldborder.getLerpRemainingTime();
         this.sizeLerpTarget = worldborder.getLerpTarget();
      }

      public double getCenterX() {
         return this.centerX;
      }

      public double getCenterZ() {
         return this.centerZ;
      }

      public double getDamagePerBlock() {
         return this.damagePerBlock;
      }

      public double getSafeZone() {
         return this.safeZone;
      }

      public int getWarningBlocks() {
         return this.warningBlocks;
      }

      public int getWarningTime() {
         return this.warningTime;
      }

      public double getSize() {
         return this.size;
      }

      public long getSizeLerpTime() {
         return this.sizeLerpTime;
      }

      public double getSizeLerpTarget() {
         return this.sizeLerpTarget;
      }

      public static WorldBorder.Settings read(DynamicLike<?> dynamiclike, WorldBorder.Settings worldborder_settings) {
         double d0 = Mth.clamp(dynamiclike.get("BorderCenterX").asDouble(worldborder_settings.centerX), -2.9999984E7D, 2.9999984E7D);
         double d1 = Mth.clamp(dynamiclike.get("BorderCenterZ").asDouble(worldborder_settings.centerZ), -2.9999984E7D, 2.9999984E7D);
         double d2 = dynamiclike.get("BorderSize").asDouble(worldborder_settings.size);
         long i = dynamiclike.get("BorderSizeLerpTime").asLong(worldborder_settings.sizeLerpTime);
         double d3 = dynamiclike.get("BorderSizeLerpTarget").asDouble(worldborder_settings.sizeLerpTarget);
         double d4 = dynamiclike.get("BorderSafeZone").asDouble(worldborder_settings.safeZone);
         double d5 = dynamiclike.get("BorderDamagePerBlock").asDouble(worldborder_settings.damagePerBlock);
         int j = dynamiclike.get("BorderWarningBlocks").asInt(worldborder_settings.warningBlocks);
         int k = dynamiclike.get("BorderWarningTime").asInt(worldborder_settings.warningTime);
         return new WorldBorder.Settings(d0, d1, d5, d4, j, k, d2, i, d3);
      }

      public void write(CompoundTag compoundtag) {
         compoundtag.putDouble("BorderCenterX", this.centerX);
         compoundtag.putDouble("BorderCenterZ", this.centerZ);
         compoundtag.putDouble("BorderSize", this.size);
         compoundtag.putLong("BorderSizeLerpTime", this.sizeLerpTime);
         compoundtag.putDouble("BorderSafeZone", this.safeZone);
         compoundtag.putDouble("BorderDamagePerBlock", this.damagePerBlock);
         compoundtag.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
         compoundtag.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
         compoundtag.putDouble("BorderWarningTime", (double)this.warningTime);
      }
   }

   class StaticBorderExtent implements WorldBorder.BorderExtent {
      private final double size;
      private double minX;
      private double minZ;
      private double maxX;
      private double maxZ;
      private VoxelShape shape;

      public StaticBorderExtent(double d0) {
         this.size = d0;
         this.updateBox();
      }

      public double getMinX() {
         return this.minX;
      }

      public double getMaxX() {
         return this.maxX;
      }

      public double getMinZ() {
         return this.minZ;
      }

      public double getMaxZ() {
         return this.maxZ;
      }

      public double getSize() {
         return this.size;
      }

      public BorderStatus getStatus() {
         return BorderStatus.STATIONARY;
      }

      public double getLerpSpeed() {
         return 0.0D;
      }

      public long getLerpRemainingTime() {
         return 0L;
      }

      public double getLerpTarget() {
         return this.size;
      }

      private void updateBox() {
         this.minX = Mth.clamp(WorldBorder.this.getCenterX() - this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
         this.minZ = Mth.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
         this.maxX = Mth.clamp(WorldBorder.this.getCenterX() + this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
         this.maxZ = Mth.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
         this.shape = Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), BooleanOp.ONLY_FIRST);
      }

      public void onAbsoluteMaxSizeChange() {
         this.updateBox();
      }

      public void onCenterChange() {
         this.updateBox();
      }

      public WorldBorder.BorderExtent update() {
         return this;
      }

      public VoxelShape getCollisionShape() {
         return this.shape;
      }
   }
}
