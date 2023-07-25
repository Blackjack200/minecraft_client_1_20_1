package net.minecraft.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class Display extends Entity {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final int NO_BRIGHTNESS_OVERRIDE = -1;
   private static final EntityDataAccessor<Integer> DATA_INTERPOLATION_START_DELTA_TICKS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Vector3f> DATA_TRANSLATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
   private static final EntityDataAccessor<Vector3f> DATA_SCALE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
   private static final EntityDataAccessor<Quaternionf> DATA_LEFT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
   private static final EntityDataAccessor<Quaternionf> DATA_RIGHT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
   private static final EntityDataAccessor<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_VIEW_RANGE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_SHADOW_RADIUS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_SHADOW_STRENGTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
   private static final IntSet RENDER_STATE_IDS = IntSet.of(DATA_TRANSLATION_ID.getId(), DATA_SCALE_ID.getId(), DATA_LEFT_ROTATION_ID.getId(), DATA_RIGHT_ROTATION_ID.getId(), DATA_BILLBOARD_RENDER_CONSTRAINTS_ID.getId(), DATA_BRIGHTNESS_OVERRIDE_ID.getId(), DATA_SHADOW_RADIUS_ID.getId(), DATA_SHADOW_STRENGTH_ID.getId());
   private static final float INITIAL_SHADOW_RADIUS = 0.0F;
   private static final float INITIAL_SHADOW_STRENGTH = 1.0F;
   private static final int NO_GLOW_COLOR_OVERRIDE = -1;
   public static final String TAG_INTERPOLATION_DURATION = "interpolation_duration";
   public static final String TAG_START_INTERPOLATION = "start_interpolation";
   public static final String TAG_TRANSFORMATION = "transformation";
   public static final String TAG_BILLBOARD = "billboard";
   public static final String TAG_BRIGHTNESS = "brightness";
   public static final String TAG_VIEW_RANGE = "view_range";
   public static final String TAG_SHADOW_RADIUS = "shadow_radius";
   public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
   public static final String TAG_WIDTH = "width";
   public static final String TAG_HEIGHT = "height";
   public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
   private final Quaternionf orientation = new Quaternionf();
   private long interpolationStartClientTick = -2147483648L;
   private int interpolationDuration;
   private float lastProgress;
   private AABB cullingBoundingBox;
   protected boolean updateRenderState;
   private boolean updateStartTick;
   private boolean updateInterpolationDuration;
   @Nullable
   private Display.RenderState renderState;

   public Display(EntityType<?> entitytype, Level level) {
      super(entitytype, level);
      this.noPhysics = true;
      this.noCulling = true;
      this.cullingBoundingBox = this.getBoundingBox();
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      super.onSyncedDataUpdated(entitydataaccessor);
      if (DATA_HEIGHT_ID.equals(entitydataaccessor) || DATA_WIDTH_ID.equals(entitydataaccessor)) {
         this.updateCulling();
      }

      if (DATA_INTERPOLATION_START_DELTA_TICKS_ID.equals(entitydataaccessor)) {
         this.updateStartTick = true;
      }

      if (DATA_INTERPOLATION_DURATION_ID.equals(entitydataaccessor)) {
         this.updateInterpolationDuration = true;
      }

      if (RENDER_STATE_IDS.contains(entitydataaccessor.getId())) {
         this.updateRenderState = true;
      }

   }

   private static Transformation createTransformation(SynchedEntityData synchedentitydata) {
      Vector3f vector3f = synchedentitydata.get(DATA_TRANSLATION_ID);
      Quaternionf quaternionf = synchedentitydata.get(DATA_LEFT_ROTATION_ID);
      Vector3f vector3f1 = synchedentitydata.get(DATA_SCALE_ID);
      Quaternionf quaternionf1 = synchedentitydata.get(DATA_RIGHT_ROTATION_ID);
      return new Transformation(vector3f, quaternionf, vector3f1, quaternionf1);
   }

   public void tick() {
      Entity entity = this.getVehicle();
      if (entity != null && entity.isRemoved()) {
         this.stopRiding();
      }

      if (this.level().isClientSide) {
         if (this.updateStartTick) {
            this.updateStartTick = false;
            int i = this.getInterpolationDelay();
            this.interpolationStartClientTick = (long)(this.tickCount + i);
         }

         if (this.updateInterpolationDuration) {
            this.updateInterpolationDuration = false;
            this.interpolationDuration = this.getInterpolationDuration();
         }

         if (this.updateRenderState) {
            this.updateRenderState = false;
            boolean flag = this.interpolationDuration != 0;
            if (flag && this.renderState != null) {
               this.renderState = this.createInterpolatedRenderState(this.renderState, this.lastProgress);
            } else {
               this.renderState = this.createFreshRenderState();
            }

            this.updateRenderSubState(flag, this.lastProgress);
         }
      }

   }

   protected abstract void updateRenderSubState(boolean flag, float f);

   protected void defineSynchedData() {
      this.entityData.define(DATA_INTERPOLATION_START_DELTA_TICKS_ID, 0);
      this.entityData.define(DATA_INTERPOLATION_DURATION_ID, 0);
      this.entityData.define(DATA_TRANSLATION_ID, new Vector3f());
      this.entityData.define(DATA_SCALE_ID, new Vector3f(1.0F, 1.0F, 1.0F));
      this.entityData.define(DATA_RIGHT_ROTATION_ID, new Quaternionf());
      this.entityData.define(DATA_LEFT_ROTATION_ID, new Quaternionf());
      this.entityData.define(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, Display.BillboardConstraints.FIXED.getId());
      this.entityData.define(DATA_BRIGHTNESS_OVERRIDE_ID, -1);
      this.entityData.define(DATA_VIEW_RANGE_ID, 1.0F);
      this.entityData.define(DATA_SHADOW_RADIUS_ID, 0.0F);
      this.entityData.define(DATA_SHADOW_STRENGTH_ID, 1.0F);
      this.entityData.define(DATA_WIDTH_ID, 0.0F);
      this.entityData.define(DATA_HEIGHT_ID, 0.0F);
      this.entityData.define(DATA_GLOW_COLOR_OVERRIDE_ID, -1);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.contains("transformation")) {
         Transformation.EXTENDED_CODEC.decode(NbtOps.INSTANCE, compoundtag.get("transformation")).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent((pair2) -> this.setTransformation(pair2.getFirst()));
      }

      if (compoundtag.contains("interpolation_duration", 99)) {
         int i = compoundtag.getInt("interpolation_duration");
         this.setInterpolationDuration(i);
      }

      if (compoundtag.contains("start_interpolation", 99)) {
         int j = compoundtag.getInt("start_interpolation");
         this.setInterpolationDelay(j);
      }

      if (compoundtag.contains("billboard", 8)) {
         Display.BillboardConstraints.CODEC.decode(NbtOps.INSTANCE, compoundtag.get("billboard")).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent((pair1) -> this.setBillboardConstraints(pair1.getFirst()));
      }

      if (compoundtag.contains("view_range", 99)) {
         this.setViewRange(compoundtag.getFloat("view_range"));
      }

      if (compoundtag.contains("shadow_radius", 99)) {
         this.setShadowRadius(compoundtag.getFloat("shadow_radius"));
      }

      if (compoundtag.contains("shadow_strength", 99)) {
         this.setShadowStrength(compoundtag.getFloat("shadow_strength"));
      }

      if (compoundtag.contains("width", 99)) {
         this.setWidth(compoundtag.getFloat("width"));
      }

      if (compoundtag.contains("height", 99)) {
         this.setHeight(compoundtag.getFloat("height"));
      }

      if (compoundtag.contains("glow_color_override", 99)) {
         this.setGlowColorOverride(compoundtag.getInt("glow_color_override"));
      }

      if (compoundtag.contains("brightness", 10)) {
         Brightness.CODEC.decode(NbtOps.INSTANCE, compoundtag.get("brightness")).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent((pair) -> this.setBrightnessOverride(pair.getFirst()));
      } else {
         this.setBrightnessOverride((Brightness)null);
      }

   }

   private void setTransformation(Transformation transformation) {
      this.entityData.set(DATA_TRANSLATION_ID, transformation.getTranslation());
      this.entityData.set(DATA_LEFT_ROTATION_ID, transformation.getLeftRotation());
      this.entityData.set(DATA_SCALE_ID, transformation.getScale());
      this.entityData.set(DATA_RIGHT_ROTATION_ID, transformation.getRightRotation());
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, createTransformation(this.entityData)).result().ifPresent((tag2) -> compoundtag.put("transformation", tag2));
      Display.BillboardConstraints.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardConstraints()).result().ifPresent((tag1) -> compoundtag.put("billboard", tag1));
      compoundtag.putInt("interpolation_duration", this.getInterpolationDuration());
      compoundtag.putFloat("view_range", this.getViewRange());
      compoundtag.putFloat("shadow_radius", this.getShadowRadius());
      compoundtag.putFloat("shadow_strength", this.getShadowStrength());
      compoundtag.putFloat("width", this.getWidth());
      compoundtag.putFloat("height", this.getHeight());
      compoundtag.putInt("glow_color_override", this.getGlowColorOverride());
      Brightness brightness = this.getBrightnessOverride();
      if (brightness != null) {
         Brightness.CODEC.encodeStart(NbtOps.INSTANCE, brightness).result().ifPresent((tag) -> compoundtag.put("brightness", tag));
      }

   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }

   public AABB getBoundingBoxForCulling() {
      return this.cullingBoundingBox;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   public Quaternionf orientation() {
      return this.orientation;
   }

   @Nullable
   public Display.RenderState renderState() {
      return this.renderState;
   }

   private void setInterpolationDuration(int i) {
      this.entityData.set(DATA_INTERPOLATION_DURATION_ID, i);
   }

   private int getInterpolationDuration() {
      return this.entityData.get(DATA_INTERPOLATION_DURATION_ID);
   }

   private void setInterpolationDelay(int i) {
      this.entityData.set(DATA_INTERPOLATION_START_DELTA_TICKS_ID, i, true);
   }

   private int getInterpolationDelay() {
      return this.entityData.get(DATA_INTERPOLATION_START_DELTA_TICKS_ID);
   }

   private void setBillboardConstraints(Display.BillboardConstraints display_billboardconstraints) {
      this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, display_billboardconstraints.getId());
   }

   private Display.BillboardConstraints getBillboardConstraints() {
      return Display.BillboardConstraints.BY_ID.apply(this.entityData.get(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID));
   }

   private void setBrightnessOverride(@Nullable Brightness brightness) {
      this.entityData.set(DATA_BRIGHTNESS_OVERRIDE_ID, brightness != null ? brightness.pack() : -1);
   }

   @Nullable
   private Brightness getBrightnessOverride() {
      int i = this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
      return i != -1 ? Brightness.unpack(i) : null;
   }

   private int getPackedBrightnessOverride() {
      return this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
   }

   private void setViewRange(float f) {
      this.entityData.set(DATA_VIEW_RANGE_ID, f);
   }

   private float getViewRange() {
      return this.entityData.get(DATA_VIEW_RANGE_ID);
   }

   private void setShadowRadius(float f) {
      this.entityData.set(DATA_SHADOW_RADIUS_ID, f);
   }

   private float getShadowRadius() {
      return this.entityData.get(DATA_SHADOW_RADIUS_ID);
   }

   private void setShadowStrength(float f) {
      this.entityData.set(DATA_SHADOW_STRENGTH_ID, f);
   }

   private float getShadowStrength() {
      return this.entityData.get(DATA_SHADOW_STRENGTH_ID);
   }

   private void setWidth(float f) {
      this.entityData.set(DATA_WIDTH_ID, f);
   }

   private float getWidth() {
      return this.entityData.get(DATA_WIDTH_ID);
   }

   private void setHeight(float f) {
      this.entityData.set(DATA_HEIGHT_ID, f);
   }

   private int getGlowColorOverride() {
      return this.entityData.get(DATA_GLOW_COLOR_OVERRIDE_ID);
   }

   private void setGlowColorOverride(int i) {
      this.entityData.set(DATA_GLOW_COLOR_OVERRIDE_ID, i);
   }

   public float calculateInterpolationProgress(float f) {
      int i = this.interpolationDuration;
      if (i <= 0) {
         return 1.0F;
      } else {
         float f1 = (float)((long)this.tickCount - this.interpolationStartClientTick);
         float f2 = f1 + f;
         float f3 = Mth.clamp(Mth.inverseLerp(f2, 0.0F, (float)i), 0.0F, 1.0F);
         this.lastProgress = f3;
         return f3;
      }
   }

   private float getHeight() {
      return this.entityData.get(DATA_HEIGHT_ID);
   }

   public void setPos(double d0, double d1, double d2) {
      super.setPos(d0, d1, d2);
      this.updateCulling();
   }

   private void updateCulling() {
      float f = this.getWidth();
      float f1 = this.getHeight();
      if (f != 0.0F && f1 != 0.0F) {
         this.noCulling = false;
         float f2 = f / 2.0F;
         double d0 = this.getX();
         double d1 = this.getY();
         double d2 = this.getZ();
         this.cullingBoundingBox = new AABB(d0 - (double)f2, d1, d2 - (double)f2, d0 + (double)f2, d1 + (double)f1, d2 + (double)f2);
      } else {
         this.noCulling = true;
      }

   }

   public void setXRot(float f) {
      super.setXRot(f);
      this.updateOrientation();
   }

   public void setYRot(float f) {
      super.setYRot(f);
      this.updateOrientation();
   }

   private void updateOrientation() {
      this.orientation.rotationYXZ(-0.017453292F * this.getYRot(), ((float)Math.PI / 180F) * this.getXRot(), 0.0F);
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      return d0 < Mth.square((double)this.getViewRange() * 64.0D * getViewScale());
   }

   public int getTeamColor() {
      int i = this.getGlowColorOverride();
      return i != -1 ? i : super.getTeamColor();
   }

   private Display.RenderState createFreshRenderState() {
      return new Display.RenderState(Display.GenericInterpolator.constant(createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), Display.FloatInterpolator.constant(this.getShadowRadius()), Display.FloatInterpolator.constant(this.getShadowStrength()), this.getGlowColorOverride());
   }

   private Display.RenderState createInterpolatedRenderState(Display.RenderState display_renderstate, float f) {
      Transformation transformation = display_renderstate.transformation.get(f);
      float f1 = display_renderstate.shadowRadius.get(f);
      float f2 = display_renderstate.shadowStrength.get(f);
      return new Display.RenderState(new Display.TransformationInterpolator(transformation, createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), new Display.LinearFloatInterpolator(f1, this.getShadowRadius()), new Display.LinearFloatInterpolator(f2, this.getShadowStrength()), this.getGlowColorOverride());
   }

   public static enum BillboardConstraints implements StringRepresentable {
      FIXED((byte)0, "fixed"),
      VERTICAL((byte)1, "vertical"),
      HORIZONTAL((byte)2, "horizontal"),
      CENTER((byte)3, "center");

      public static final Codec<Display.BillboardConstraints> CODEC = StringRepresentable.fromEnum(Display.BillboardConstraints::values);
      public static final IntFunction<Display.BillboardConstraints> BY_ID = ByIdMap.continuous(Display.BillboardConstraints::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      private final byte id;
      private final String name;

      private BillboardConstraints(byte b0, String s) {
         this.name = s;
         this.id = b0;
      }

      public String getSerializedName() {
         return this.name;
      }

      byte getId() {
         return this.id;
      }
   }

   public static class BlockDisplay extends Display {
      public static final String TAG_BLOCK_STATE = "block_state";
      private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(Display.BlockDisplay.class, EntityDataSerializers.BLOCK_STATE);
      @Nullable
      private Display.BlockDisplay.BlockRenderState blockRenderState;

      public BlockDisplay(EntityType<?> entitytype, Level level) {
         super(entitytype, level);
      }

      protected void defineSynchedData() {
         super.defineSynchedData();
         this.entityData.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
      }

      public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
         super.onSyncedDataUpdated(entitydataaccessor);
         if (entitydataaccessor.equals(DATA_BLOCK_STATE_ID)) {
            this.updateRenderState = true;
         }

      }

      private BlockState getBlockState() {
         return this.entityData.get(DATA_BLOCK_STATE_ID);
      }

      private void setBlockState(BlockState blockstate) {
         this.entityData.set(DATA_BLOCK_STATE_ID, blockstate);
      }

      protected void readAdditionalSaveData(CompoundTag compoundtag) {
         super.readAdditionalSaveData(compoundtag);
         this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundtag.getCompound("block_state")));
      }

      protected void addAdditionalSaveData(CompoundTag compoundtag) {
         super.addAdditionalSaveData(compoundtag);
         compoundtag.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
      }

      @Nullable
      public Display.BlockDisplay.BlockRenderState blockRenderState() {
         return this.blockRenderState;
      }

      protected void updateRenderSubState(boolean flag, float f) {
         this.blockRenderState = new Display.BlockDisplay.BlockRenderState(this.getBlockState());
      }

      public static record BlockRenderState(BlockState blockState) {
      }
   }

   static record ColorInterpolator(int previous, int current) implements Display.IntInterpolator {
      public int get(float f) {
         return FastColor.ARGB32.lerp(f, this.previous, this.current);
      }
   }

   @FunctionalInterface
   public interface FloatInterpolator {
      static Display.FloatInterpolator constant(float f) {
         return (f2) -> f;
      }

      float get(float f);
   }

   @FunctionalInterface
   public interface GenericInterpolator<T> {
      static <T> Display.GenericInterpolator<T> constant(T object) {
         return (f) -> object;
      }

      T get(float f);
   }

   @FunctionalInterface
   public interface IntInterpolator {
      static Display.IntInterpolator constant(int i) {
         return (f) -> i;
      }

      int get(float f);
   }

   public static class ItemDisplay extends Display {
      private static final String TAG_ITEM = "item";
      private static final String TAG_ITEM_DISPLAY = "item_display";
      private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(Display.ItemDisplay.class, EntityDataSerializers.ITEM_STACK);
      private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(Display.ItemDisplay.class, EntityDataSerializers.BYTE);
      private final SlotAccess slot = new SlotAccess() {
         public ItemStack get() {
            return ItemDisplay.this.getItemStack();
         }

         public boolean set(ItemStack itemstack) {
            ItemDisplay.this.setItemStack(itemstack);
            return true;
         }
      };
      @Nullable
      private Display.ItemDisplay.ItemRenderState itemRenderState;

      public ItemDisplay(EntityType<?> entitytype, Level level) {
         super(entitytype, level);
      }

      protected void defineSynchedData() {
         super.defineSynchedData();
         this.entityData.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
         this.entityData.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.NONE.getId());
      }

      public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
         super.onSyncedDataUpdated(entitydataaccessor);
         if (DATA_ITEM_STACK_ID.equals(entitydataaccessor) || DATA_ITEM_DISPLAY_ID.equals(entitydataaccessor)) {
            this.updateRenderState = true;
         }

      }

      ItemStack getItemStack() {
         return this.entityData.get(DATA_ITEM_STACK_ID);
      }

      void setItemStack(ItemStack itemstack) {
         this.entityData.set(DATA_ITEM_STACK_ID, itemstack);
      }

      private void setItemTransform(ItemDisplayContext itemdisplaycontext) {
         this.entityData.set(DATA_ITEM_DISPLAY_ID, itemdisplaycontext.getId());
      }

      private ItemDisplayContext getItemTransform() {
         return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID));
      }

      protected void readAdditionalSaveData(CompoundTag compoundtag) {
         super.readAdditionalSaveData(compoundtag);
         this.setItemStack(ItemStack.of(compoundtag.getCompound("item")));
         if (compoundtag.contains("item_display", 8)) {
            ItemDisplayContext.CODEC.decode(NbtOps.INSTANCE, compoundtag.get("item_display")).resultOrPartial(Util.prefix("Display entity", Display.LOGGER::error)).ifPresent((pair) -> this.setItemTransform(pair.getFirst()));
         }

      }

      protected void addAdditionalSaveData(CompoundTag compoundtag) {
         super.addAdditionalSaveData(compoundtag);
         compoundtag.put("item", this.getItemStack().save(new CompoundTag()));
         ItemDisplayContext.CODEC.encodeStart(NbtOps.INSTANCE, this.getItemTransform()).result().ifPresent((tag) -> compoundtag.put("item_display", tag));
      }

      public SlotAccess getSlot(int i) {
         return i == 0 ? this.slot : SlotAccess.NULL;
      }

      @Nullable
      public Display.ItemDisplay.ItemRenderState itemRenderState() {
         return this.itemRenderState;
      }

      protected void updateRenderSubState(boolean flag, float f) {
         this.itemRenderState = new Display.ItemDisplay.ItemRenderState(this.getItemStack(), this.getItemTransform());
      }

      public static record ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
      }
   }

   static record LinearFloatInterpolator(float previous, float current) implements Display.FloatInterpolator {
      public float get(float f) {
         return Mth.lerp(f, this.previous, this.current);
      }
   }

   static record LinearIntInterpolator(int previous, int current) implements Display.IntInterpolator {
      public int get(float f) {
         return Mth.lerpInt(f, this.previous, this.current);
      }
   }

   public static record RenderState(Display.GenericInterpolator<Transformation> transformation, Display.BillboardConstraints billboardConstraints, int brightnessOverride, Display.FloatInterpolator shadowRadius, Display.FloatInterpolator shadowStrength, int glowColorOverride) {
      final Display.GenericInterpolator<Transformation> transformation;
      final Display.FloatInterpolator shadowRadius;
      final Display.FloatInterpolator shadowStrength;
   }

   public static class TextDisplay extends Display {
      public static final String TAG_TEXT = "text";
      private static final String TAG_LINE_WIDTH = "line_width";
      private static final String TAG_TEXT_OPACITY = "text_opacity";
      private static final String TAG_BACKGROUND_COLOR = "background";
      private static final String TAG_SHADOW = "shadow";
      private static final String TAG_SEE_THROUGH = "see_through";
      private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
      private static final String TAG_ALIGNMENT = "alignment";
      public static final byte FLAG_SHADOW = 1;
      public static final byte FLAG_SEE_THROUGH = 2;
      public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
      public static final byte FLAG_ALIGN_LEFT = 8;
      public static final byte FLAG_ALIGN_RIGHT = 16;
      private static final byte INITIAL_TEXT_OPACITY = -1;
      public static final int INITIAL_BACKGROUND = 1073741824;
      private static final EntityDataAccessor<Component> DATA_TEXT_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.COMPONENT);
      private static final EntityDataAccessor<Integer> DATA_LINE_WIDTH_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.INT);
      private static final EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.INT);
      private static final EntityDataAccessor<Byte> DATA_TEXT_OPACITY_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.BYTE);
      private static final EntityDataAccessor<Byte> DATA_STYLE_FLAGS_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.BYTE);
      private static final IntSet TEXT_RENDER_STATE_IDS = IntSet.of(DATA_TEXT_ID.getId(), DATA_LINE_WIDTH_ID.getId(), DATA_BACKGROUND_COLOR_ID.getId(), DATA_TEXT_OPACITY_ID.getId(), DATA_STYLE_FLAGS_ID.getId());
      @Nullable
      private Display.TextDisplay.CachedInfo clientDisplayCache;
      @Nullable
      private Display.TextDisplay.TextRenderState textRenderState;

      public TextDisplay(EntityType<?> entitytype, Level level) {
         super(entitytype, level);
      }

      protected void defineSynchedData() {
         super.defineSynchedData();
         this.entityData.define(DATA_TEXT_ID, Component.empty());
         this.entityData.define(DATA_LINE_WIDTH_ID, 200);
         this.entityData.define(DATA_BACKGROUND_COLOR_ID, 1073741824);
         this.entityData.define(DATA_TEXT_OPACITY_ID, (byte)-1);
         this.entityData.define(DATA_STYLE_FLAGS_ID, (byte)0);
      }

      public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
         super.onSyncedDataUpdated(entitydataaccessor);
         if (TEXT_RENDER_STATE_IDS.contains(entitydataaccessor.getId())) {
            this.updateRenderState = true;
         }

      }

      private Component getText() {
         return this.entityData.get(DATA_TEXT_ID);
      }

      private void setText(Component component) {
         this.entityData.set(DATA_TEXT_ID, component);
      }

      private int getLineWidth() {
         return this.entityData.get(DATA_LINE_WIDTH_ID);
      }

      private void setLineWidth(int i) {
         this.entityData.set(DATA_LINE_WIDTH_ID, i);
      }

      private byte getTextOpacity() {
         return this.entityData.get(DATA_TEXT_OPACITY_ID);
      }

      private void setTextOpacity(byte b0) {
         this.entityData.set(DATA_TEXT_OPACITY_ID, b0);
      }

      private int getBackgroundColor() {
         return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
      }

      private void setBackgroundColor(int i) {
         this.entityData.set(DATA_BACKGROUND_COLOR_ID, i);
      }

      private byte getFlags() {
         return this.entityData.get(DATA_STYLE_FLAGS_ID);
      }

      private void setFlags(byte b0) {
         this.entityData.set(DATA_STYLE_FLAGS_ID, b0);
      }

      private static byte loadFlag(byte b0, CompoundTag compoundtag, String s, byte b1) {
         return compoundtag.getBoolean(s) ? (byte)(b0 | b1) : b0;
      }

      protected void readAdditionalSaveData(CompoundTag compoundtag) {
         super.readAdditionalSaveData(compoundtag);
         if (compoundtag.contains("line_width", 99)) {
            this.setLineWidth(compoundtag.getInt("line_width"));
         }

         if (compoundtag.contains("text_opacity", 99)) {
            this.setTextOpacity(compoundtag.getByte("text_opacity"));
         }

         if (compoundtag.contains("background", 99)) {
            this.setBackgroundColor(compoundtag.getInt("background"));
         }

         byte b0 = loadFlag((byte)0, compoundtag, "shadow", (byte)1);
         b0 = loadFlag(b0, compoundtag, "see_through", (byte)2);
         b0 = loadFlag(b0, compoundtag, "default_background", (byte)4);
         Optional<Display.TextDisplay.Align> optional = Display.TextDisplay.Align.CODEC.decode(NbtOps.INSTANCE, compoundtag.get("alignment")).resultOrPartial(Util.prefix("Display entity", Display.LOGGER::error)).map(Pair::getFirst);
         if (optional.isPresent()) {
            byte var10000;
            switch ((Display.TextDisplay.Align)optional.get()) {
               case CENTER:
                  var10000 = b0;
                  break;
               case LEFT:
                  var10000 = (byte)(b0 | 8);
                  break;
               case RIGHT:
                  var10000 = (byte)(b0 | 16);
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            b0 = var10000;
         }

         this.setFlags(b0);
         if (compoundtag.contains("text", 8)) {
            String s = compoundtag.getString("text");

            try {
               Component component = Component.Serializer.fromJson(s);
               if (component != null) {
                  CommandSourceStack commandsourcestack = this.createCommandSourceStack().withPermission(2);
                  Component component1 = ComponentUtils.updateForEntity(commandsourcestack, component, this, 0);
                  this.setText(component1);
               } else {
                  this.setText(Component.empty());
               }
            } catch (Exception var8) {
               Display.LOGGER.warn("Failed to parse display entity text {}", s, var8);
            }
         }

      }

      private static void storeFlag(byte b0, CompoundTag compoundtag, String s, byte b1) {
         compoundtag.putBoolean(s, (b0 & b1) != 0);
      }

      protected void addAdditionalSaveData(CompoundTag compoundtag) {
         super.addAdditionalSaveData(compoundtag);
         compoundtag.putString("text", Component.Serializer.toJson(this.getText()));
         compoundtag.putInt("line_width", this.getLineWidth());
         compoundtag.putInt("background", this.getBackgroundColor());
         compoundtag.putByte("text_opacity", this.getTextOpacity());
         byte b0 = this.getFlags();
         storeFlag(b0, compoundtag, "shadow", (byte)1);
         storeFlag(b0, compoundtag, "see_through", (byte)2);
         storeFlag(b0, compoundtag, "default_background", (byte)4);
         Display.TextDisplay.Align.CODEC.encodeStart(NbtOps.INSTANCE, getAlign(b0)).result().ifPresent((tag) -> compoundtag.put("alignment", tag));
      }

      protected void updateRenderSubState(boolean flag, float f) {
         if (flag && this.textRenderState != null) {
            this.textRenderState = this.createInterpolatedTextRenderState(this.textRenderState, f);
         } else {
            this.textRenderState = this.createFreshTextRenderState();
         }

         this.clientDisplayCache = null;
      }

      @Nullable
      public Display.TextDisplay.TextRenderState textRenderState() {
         return this.textRenderState;
      }

      private Display.TextDisplay.TextRenderState createFreshTextRenderState() {
         return new Display.TextDisplay.TextRenderState(this.getText(), this.getLineWidth(), Display.IntInterpolator.constant(this.getTextOpacity()), Display.IntInterpolator.constant(this.getBackgroundColor()), this.getFlags());
      }

      private Display.TextDisplay.TextRenderState createInterpolatedTextRenderState(Display.TextDisplay.TextRenderState display_textdisplay_textrenderstate, float f) {
         int i = display_textdisplay_textrenderstate.backgroundColor.get(f);
         int j = display_textdisplay_textrenderstate.textOpacity.get(f);
         return new Display.TextDisplay.TextRenderState(this.getText(), this.getLineWidth(), new Display.LinearIntInterpolator(j, this.getTextOpacity()), new Display.ColorInterpolator(i, this.getBackgroundColor()), this.getFlags());
      }

      public Display.TextDisplay.CachedInfo cacheDisplay(Display.TextDisplay.LineSplitter display_textdisplay_linesplitter) {
         if (this.clientDisplayCache == null) {
            if (this.textRenderState != null) {
               this.clientDisplayCache = display_textdisplay_linesplitter.split(this.textRenderState.text(), this.textRenderState.lineWidth());
            } else {
               this.clientDisplayCache = new Display.TextDisplay.CachedInfo(List.of(), 0);
            }
         }

         return this.clientDisplayCache;
      }

      public static Display.TextDisplay.Align getAlign(byte b0) {
         if ((b0 & 8) != 0) {
            return Display.TextDisplay.Align.LEFT;
         } else {
            return (b0 & 16) != 0 ? Display.TextDisplay.Align.RIGHT : Display.TextDisplay.Align.CENTER;
         }
      }

      public static enum Align implements StringRepresentable {
         CENTER("center"),
         LEFT("left"),
         RIGHT("right");

         public static final Codec<Display.TextDisplay.Align> CODEC = StringRepresentable.fromEnum(Display.TextDisplay.Align::values);
         private final String name;

         private Align(String s) {
            this.name = s;
         }

         public String getSerializedName() {
            return this.name;
         }
      }

      public static record CachedInfo(List<Display.TextDisplay.CachedLine> lines, int width) {
      }

      public static record CachedLine(FormattedCharSequence contents, int width) {
      }

      @FunctionalInterface
      public interface LineSplitter {
         Display.TextDisplay.CachedInfo split(Component component, int i);
      }

      public static record TextRenderState(Component text, int lineWidth, Display.IntInterpolator textOpacity, Display.IntInterpolator backgroundColor, byte flags) {
         final Display.IntInterpolator textOpacity;
         final Display.IntInterpolator backgroundColor;
      }
   }

   static record TransformationInterpolator(Transformation previous, Transformation current) implements Display.GenericInterpolator<Transformation> {
      public Transformation get(float f) {
         return (double)f >= 1.0D ? this.current : this.previous.slerp(this.current, f);
      }
   }
}
