package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class TropicalFish extends AbstractSchoolingFish implements VariantHolder<TropicalFish.Pattern> {
   public static final String BUCKET_VARIANT_TAG = "BucketVariantTag";
   private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
   public static final List<TropicalFish.Variant> COMMON_VARIANTS = List.of(new TropicalFish.Variant(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), new TropicalFish.Variant(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), new TropicalFish.Variant(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), new TropicalFish.Variant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), new TropicalFish.Variant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), new TropicalFish.Variant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), new TropicalFish.Variant(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE), new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));
   private boolean isSchool = true;

   public TropicalFish(EntityType<? extends TropicalFish> entitytype, Level level) {
      super(entitytype, level);
   }

   public static String getPredefinedName(int i) {
      return "entity.minecraft.tropical_fish.predefined." + i;
   }

   static int packVariant(TropicalFish.Pattern tropicalfish_pattern, DyeColor dyecolor, DyeColor dyecolor1) {
      return tropicalfish_pattern.getPackedId() & '\uffff' | (dyecolor.getId() & 255) << 16 | (dyecolor1.getId() & 255) << 24;
   }

   public static DyeColor getBaseColor(int i) {
      return DyeColor.byId(i >> 16 & 255);
   }

   public static DyeColor getPatternColor(int i) {
      return DyeColor.byId(i >> 24 & 255);
   }

   public static TropicalFish.Pattern getPattern(int i) {
      return TropicalFish.Pattern.byId(i & '\uffff');
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Variant", this.getPackedVariant());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setPackedVariant(compoundtag.getInt("Variant"));
   }

   private void setPackedVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i);
   }

   public boolean isMaxGroupSizeReached(int i) {
      return !this.isSchool;
   }

   private int getPackedVariant() {
      return this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public DyeColor getBaseColor() {
      return getBaseColor(this.getPackedVariant());
   }

   public DyeColor getPatternColor() {
      return getPatternColor(this.getPackedVariant());
   }

   public TropicalFish.Pattern getVariant() {
      return getPattern(this.getPackedVariant());
   }

   public void setVariant(TropicalFish.Pattern tropicalfish_pattern) {
      int i = this.getPackedVariant();
      DyeColor dyecolor = getBaseColor(i);
      DyeColor dyecolor1 = getPatternColor(i);
      this.setPackedVariant(packVariant(tropicalfish_pattern, dyecolor, dyecolor1));
   }

   public void saveToBucketTag(ItemStack itemstack) {
      super.saveToBucketTag(itemstack);
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      compoundtag.putInt("BucketVariantTag", this.getPackedVariant());
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(Items.TROPICAL_FISH_BUCKET);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.TROPICAL_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.TROPICAL_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.TROPICAL_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.TROPICAL_FISH_FLOP;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      spawngroupdata = super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
      if (mobspawntype == MobSpawnType.BUCKET && compoundtag != null && compoundtag.contains("BucketVariantTag", 3)) {
         this.setPackedVariant(compoundtag.getInt("BucketVariantTag"));
         return spawngroupdata;
      } else {
         RandomSource randomsource = serverlevelaccessor.getRandom();
         TropicalFish.Variant tropicalfish_variant;
         if (spawngroupdata instanceof TropicalFish.TropicalFishGroupData) {
            TropicalFish.TropicalFishGroupData tropicalfish_tropicalfishgroupdata = (TropicalFish.TropicalFishGroupData)spawngroupdata;
            tropicalfish_variant = tropicalfish_tropicalfishgroupdata.variant;
         } else if ((double)randomsource.nextFloat() < 0.9D) {
            tropicalfish_variant = Util.getRandom(COMMON_VARIANTS, randomsource);
            spawngroupdata = new TropicalFish.TropicalFishGroupData(this, tropicalfish_variant);
         } else {
            this.isSchool = false;
            TropicalFish.Pattern[] atropicalfish_pattern = TropicalFish.Pattern.values();
            DyeColor[] adyecolor = DyeColor.values();
            TropicalFish.Pattern tropicalfish_pattern = Util.getRandom(atropicalfish_pattern, randomsource);
            DyeColor dyecolor = Util.getRandom(adyecolor, randomsource);
            DyeColor dyecolor1 = Util.getRandom(adyecolor, randomsource);
            tropicalfish_variant = new TropicalFish.Variant(tropicalfish_pattern, dyecolor, dyecolor1);
         }

         this.setPackedVariant(tropicalfish_variant.getPackedId());
         return spawngroupdata;
      }
   }

   public static boolean checkTropicalFishSpawnRules(EntityType<TropicalFish> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      return levelaccessor.getFluidState(blockpos.below()).is(FluidTags.WATER) && levelaccessor.getBlockState(blockpos.above()).is(Blocks.WATER) && (levelaccessor.getBiome(blockpos).is(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterAnimal.checkSurfaceWaterAnimalSpawnRules(entitytype, levelaccessor, mobspawntype, blockpos, randomsource));
   }

   public static enum Base {
      SMALL(0),
      LARGE(1);

      final int id;

      private Base(int i) {
         this.id = i;
      }
   }

   public static enum Pattern implements StringRepresentable {
      KOB("kob", TropicalFish.Base.SMALL, 0),
      SUNSTREAK("sunstreak", TropicalFish.Base.SMALL, 1),
      SNOOPER("snooper", TropicalFish.Base.SMALL, 2),
      DASHER("dasher", TropicalFish.Base.SMALL, 3),
      BRINELY("brinely", TropicalFish.Base.SMALL, 4),
      SPOTTY("spotty", TropicalFish.Base.SMALL, 5),
      FLOPPER("flopper", TropicalFish.Base.LARGE, 0),
      STRIPEY("stripey", TropicalFish.Base.LARGE, 1),
      GLITTER("glitter", TropicalFish.Base.LARGE, 2),
      BLOCKFISH("blockfish", TropicalFish.Base.LARGE, 3),
      BETTY("betty", TropicalFish.Base.LARGE, 4),
      CLAYFISH("clayfish", TropicalFish.Base.LARGE, 5);

      public static final Codec<TropicalFish.Pattern> CODEC = StringRepresentable.fromEnum(TropicalFish.Pattern::values);
      private static final IntFunction<TropicalFish.Pattern> BY_ID = ByIdMap.sparse(TropicalFish.Pattern::getPackedId, values(), KOB);
      private final String name;
      private final Component displayName;
      private final TropicalFish.Base base;
      private final int packedId;

      private Pattern(String s, TropicalFish.Base tropicalfish_base, int i) {
         this.name = s;
         this.base = tropicalfish_base;
         this.packedId = tropicalfish_base.id | i << 8;
         this.displayName = Component.translatable("entity.minecraft.tropical_fish.type." + this.name);
      }

      public static TropicalFish.Pattern byId(int i) {
         return BY_ID.apply(i);
      }

      public TropicalFish.Base base() {
         return this.base;
      }

      public int getPackedId() {
         return this.packedId;
      }

      public String getSerializedName() {
         return this.name;
      }

      public Component displayName() {
         return this.displayName;
      }
   }

   static class TropicalFishGroupData extends AbstractSchoolingFish.SchoolSpawnGroupData {
      final TropicalFish.Variant variant;

      TropicalFishGroupData(TropicalFish tropicalfish, TropicalFish.Variant tropicalfish_variant) {
         super(tropicalfish);
         this.variant = tropicalfish_variant;
      }
   }

   public static record Variant(TropicalFish.Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
      public int getPackedId() {
         return TropicalFish.packVariant(this.pattern, this.baseColor, this.patternColor);
      }
   }
}
