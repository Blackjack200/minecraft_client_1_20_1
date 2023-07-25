package net.minecraft.world.entity.decoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity implements VariantHolder<Holder<PaintingVariant>> {
   private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(Painting.class, EntityDataSerializers.PAINTING_VARIANT);
   private static final ResourceKey<PaintingVariant> DEFAULT_VARIANT = PaintingVariants.KEBAB;
   public static final String VARIANT_TAG = "variant";

   private static Holder<PaintingVariant> getDefaultVariant() {
      return BuiltInRegistries.PAINTING_VARIANT.getHolderOrThrow(DEFAULT_VARIANT);
   }

   public Painting(EntityType<? extends Painting> entitytype, Level level) {
      super(entitytype, level);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_PAINTING_VARIANT_ID, getDefaultVariant());
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_PAINTING_VARIANT_ID.equals(entitydataaccessor)) {
         this.recalculateBoundingBox();
      }

   }

   public void setVariant(Holder<PaintingVariant> holder) {
      this.entityData.set(DATA_PAINTING_VARIANT_ID, holder);
   }

   public Holder<PaintingVariant> getVariant() {
      return this.entityData.get(DATA_PAINTING_VARIANT_ID);
   }

   public static Optional<Painting> create(Level level, BlockPos blockpos, Direction direction) {
      Painting painting = new Painting(level, blockpos);
      List<Holder<PaintingVariant>> list = new ArrayList<>();
      BuiltInRegistries.PAINTING_VARIANT.getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(list::add);
      if (list.isEmpty()) {
         return Optional.empty();
      } else {
         painting.setDirection(direction);
         list.removeIf((holder2) -> {
            painting.setVariant(holder2);
            return !painting.survives();
         });
         if (list.isEmpty()) {
            return Optional.empty();
         } else {
            int i = list.stream().mapToInt(Painting::variantArea).max().orElse(0);
            list.removeIf((holder1) -> variantArea(holder1) < i);
            Optional<Holder<PaintingVariant>> optional = Util.getRandomSafe(list, painting.random);
            if (optional.isEmpty()) {
               return Optional.empty();
            } else {
               painting.setVariant(optional.get());
               painting.setDirection(direction);
               return Optional.of(painting);
            }
         }
      }
   }

   private static int variantArea(Holder<PaintingVariant> holder) {
      return holder.value().getWidth() * holder.value().getHeight();
   }

   private Painting(Level level, BlockPos blockpos) {
      super(EntityType.PAINTING, level, blockpos);
   }

   public Painting(Level level, BlockPos blockpos, Direction direction, Holder<PaintingVariant> holder) {
      this(level, blockpos);
      this.setVariant(holder);
      this.setDirection(direction);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      storeVariant(compoundtag, this.getVariant());
      compoundtag.putByte("facing", (byte)this.direction.get2DDataValue());
      super.addAdditionalSaveData(compoundtag);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      Holder<PaintingVariant> holder = loadVariant(compoundtag).orElseGet(Painting::getDefaultVariant);
      this.setVariant(holder);
      this.direction = Direction.from2DDataValue(compoundtag.getByte("facing"));
      super.readAdditionalSaveData(compoundtag);
      this.setDirection(this.direction);
   }

   public static void storeVariant(CompoundTag compoundtag, Holder<PaintingVariant> holder) {
      compoundtag.putString("variant", holder.unwrapKey().orElse(DEFAULT_VARIANT).location().toString());
   }

   public static Optional<Holder<PaintingVariant>> loadVariant(CompoundTag compoundtag) {
      return Optional.ofNullable(ResourceLocation.tryParse(compoundtag.getString("variant"))).map((resourcelocation) -> ResourceKey.create(Registries.PAINTING_VARIANT, resourcelocation)).flatMap(BuiltInRegistries.PAINTING_VARIANT::getHolder);
   }

   public int getWidth() {
      return this.getVariant().value().getWidth();
   }

   public int getHeight() {
      return this.getVariant().value().getHeight();
   }

   public void dropItem(@Nullable Entity entity) {
      if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
         if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().instabuild) {
               return;
            }
         }

         this.spawnAtLocation(Items.PAINTING);
      }
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
   }

   public void moveTo(double d0, double d1, double d2, float f, float f1) {
      this.setPos(d0, d1, d2);
   }

   public void lerpTo(double d0, double d1, double d2, float f, float f1, int i, boolean flag) {
      this.setPos(d0, d1, d2);
   }

   public Vec3 trackingPosition() {
      return Vec3.atLowerCornerOf(this.pos);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      this.setDirection(Direction.from3DDataValue(clientboundaddentitypacket.getData()));
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.PAINTING);
   }
}
