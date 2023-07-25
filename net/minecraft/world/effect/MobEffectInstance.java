package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int INFINITE_DURATION = -1;
   private final MobEffect effect;
   private int duration;
   private int amplifier;
   private boolean ambient;
   private boolean visible;
   private boolean showIcon;
   @Nullable
   private MobEffectInstance hiddenEffect;
   private final Optional<MobEffectInstance.FactorData> factorData;

   public MobEffectInstance(MobEffect mobeffect) {
      this(mobeffect, 0, 0);
   }

   public MobEffectInstance(MobEffect mobeffect, int i) {
      this(mobeffect, i, 0);
   }

   public MobEffectInstance(MobEffect mobeffect, int i, int j) {
      this(mobeffect, i, j, false, true);
   }

   public MobEffectInstance(MobEffect mobeffect, int i, int j, boolean flag, boolean flag1) {
      this(mobeffect, i, j, flag, flag1, flag1);
   }

   public MobEffectInstance(MobEffect mobeffect, int i, int j, boolean flag, boolean flag1, boolean flag2) {
      this(mobeffect, i, j, flag, flag1, flag2, (MobEffectInstance)null, mobeffect.createFactorData());
   }

   public MobEffectInstance(MobEffect mobeffect, int i, int j, boolean flag, boolean flag1, boolean flag2, @Nullable MobEffectInstance mobeffectinstance, Optional<MobEffectInstance.FactorData> optional) {
      this.effect = mobeffect;
      this.duration = i;
      this.amplifier = j;
      this.ambient = flag;
      this.visible = flag1;
      this.showIcon = flag2;
      this.hiddenEffect = mobeffectinstance;
      this.factorData = optional;
   }

   public MobEffectInstance(MobEffectInstance mobeffectinstance) {
      this.effect = mobeffectinstance.effect;
      this.factorData = this.effect.createFactorData();
      this.setDetailsFrom(mobeffectinstance);
   }

   public Optional<MobEffectInstance.FactorData> getFactorData() {
      return this.factorData;
   }

   void setDetailsFrom(MobEffectInstance mobeffectinstance) {
      this.duration = mobeffectinstance.duration;
      this.amplifier = mobeffectinstance.amplifier;
      this.ambient = mobeffectinstance.ambient;
      this.visible = mobeffectinstance.visible;
      this.showIcon = mobeffectinstance.showIcon;
   }

   public boolean update(MobEffectInstance mobeffectinstance) {
      if (this.effect != mobeffectinstance.effect) {
         LOGGER.warn("This method should only be called for matching effects!");
      }

      int i = this.duration;
      boolean flag = false;
      if (mobeffectinstance.amplifier > this.amplifier) {
         if (mobeffectinstance.isShorterDurationThan(this)) {
            MobEffectInstance mobeffectinstance1 = this.hiddenEffect;
            this.hiddenEffect = new MobEffectInstance(this);
            this.hiddenEffect.hiddenEffect = mobeffectinstance1;
         }

         this.amplifier = mobeffectinstance.amplifier;
         this.duration = mobeffectinstance.duration;
         flag = true;
      } else if (this.isShorterDurationThan(mobeffectinstance)) {
         if (mobeffectinstance.amplifier == this.amplifier) {
            this.duration = mobeffectinstance.duration;
            flag = true;
         } else if (this.hiddenEffect == null) {
            this.hiddenEffect = new MobEffectInstance(mobeffectinstance);
         } else {
            this.hiddenEffect.update(mobeffectinstance);
         }
      }

      if (!mobeffectinstance.ambient && this.ambient || flag) {
         this.ambient = mobeffectinstance.ambient;
         flag = true;
      }

      if (mobeffectinstance.visible != this.visible) {
         this.visible = mobeffectinstance.visible;
         flag = true;
      }

      if (mobeffectinstance.showIcon != this.showIcon) {
         this.showIcon = mobeffectinstance.showIcon;
         flag = true;
      }

      return flag;
   }

   private boolean isShorterDurationThan(MobEffectInstance mobeffectinstance) {
      return !this.isInfiniteDuration() && (this.duration < mobeffectinstance.duration || mobeffectinstance.isInfiniteDuration());
   }

   public boolean isInfiniteDuration() {
      return this.duration == -1;
   }

   public boolean endsWithin(int i) {
      return !this.isInfiniteDuration() && this.duration <= i;
   }

   public int mapDuration(Int2IntFunction int2intfunction) {
      return !this.isInfiniteDuration() && this.duration != 0 ? int2intfunction.applyAsInt(this.duration) : this.duration;
   }

   public MobEffect getEffect() {
      return this.effect;
   }

   public int getDuration() {
      return this.duration;
   }

   public int getAmplifier() {
      return this.amplifier;
   }

   public boolean isAmbient() {
      return this.ambient;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public boolean showIcon() {
      return this.showIcon;
   }

   public boolean tick(LivingEntity livingentity, Runnable runnable) {
      if (this.hasRemainingDuration()) {
         int i = this.isInfiniteDuration() ? livingentity.tickCount : this.duration;
         if (this.effect.isDurationEffectTick(i, this.amplifier)) {
            this.applyEffect(livingentity);
         }

         this.tickDownDuration();
         if (this.duration == 0 && this.hiddenEffect != null) {
            this.setDetailsFrom(this.hiddenEffect);
            this.hiddenEffect = this.hiddenEffect.hiddenEffect;
            runnable.run();
         }
      }

      this.factorData.ifPresent((mobeffectinstance_factordata) -> mobeffectinstance_factordata.tick(this));
      return this.hasRemainingDuration();
   }

   private boolean hasRemainingDuration() {
      return this.isInfiniteDuration() || this.duration > 0;
   }

   private int tickDownDuration() {
      if (this.hiddenEffect != null) {
         this.hiddenEffect.tickDownDuration();
      }

      return this.duration = this.mapDuration((i) -> i - 1);
   }

   public void applyEffect(LivingEntity livingentity) {
      if (this.hasRemainingDuration()) {
         this.effect.applyEffectTick(livingentity, this.amplifier);
      }

   }

   public String getDescriptionId() {
      return this.effect.getDescriptionId();
   }

   public String toString() {
      String s;
      if (this.amplifier > 0) {
         s = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
      } else {
         s = this.getDescriptionId() + ", Duration: " + this.describeDuration();
      }

      if (!this.visible) {
         s = s + ", Particles: false";
      }

      if (!this.showIcon) {
         s = s + ", Show Icon: false";
      }

      return s;
   }

   private String describeDuration() {
      return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof MobEffectInstance)) {
         return false;
      } else {
         MobEffectInstance mobeffectinstance = (MobEffectInstance)object;
         return this.duration == mobeffectinstance.duration && this.amplifier == mobeffectinstance.amplifier && this.ambient == mobeffectinstance.ambient && this.effect.equals(mobeffectinstance.effect);
      }
   }

   public int hashCode() {
      int i = this.effect.hashCode();
      i = 31 * i + this.duration;
      i = 31 * i + this.amplifier;
      return 31 * i + (this.ambient ? 1 : 0);
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putInt("Id", MobEffect.getId(this.getEffect()));
      this.writeDetailsTo(compoundtag);
      return compoundtag;
   }

   private void writeDetailsTo(CompoundTag compoundtag) {
      compoundtag.putByte("Amplifier", (byte)this.getAmplifier());
      compoundtag.putInt("Duration", this.getDuration());
      compoundtag.putBoolean("Ambient", this.isAmbient());
      compoundtag.putBoolean("ShowParticles", this.isVisible());
      compoundtag.putBoolean("ShowIcon", this.showIcon());
      if (this.hiddenEffect != null) {
         CompoundTag compoundtag1 = new CompoundTag();
         this.hiddenEffect.save(compoundtag1);
         compoundtag.put("HiddenEffect", compoundtag1);
      }

      this.factorData.ifPresent((mobeffectinstance_factordata) -> MobEffectInstance.FactorData.CODEC.encodeStart(NbtOps.INSTANCE, mobeffectinstance_factordata).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("FactorCalculationData", tag)));
   }

   @Nullable
   public static MobEffectInstance load(CompoundTag compoundtag) {
      int i = compoundtag.getInt("Id");
      MobEffect mobeffect = MobEffect.byId(i);
      return mobeffect == null ? null : loadSpecifiedEffect(mobeffect, compoundtag);
   }

   private static MobEffectInstance loadSpecifiedEffect(MobEffect mobeffect, CompoundTag compoundtag) {
      int i = compoundtag.getByte("Amplifier");
      int j = compoundtag.getInt("Duration");
      boolean flag = compoundtag.getBoolean("Ambient");
      boolean flag1 = true;
      if (compoundtag.contains("ShowParticles", 1)) {
         flag1 = compoundtag.getBoolean("ShowParticles");
      }

      boolean flag2 = flag1;
      if (compoundtag.contains("ShowIcon", 1)) {
         flag2 = compoundtag.getBoolean("ShowIcon");
      }

      MobEffectInstance mobeffectinstance = null;
      if (compoundtag.contains("HiddenEffect", 10)) {
         mobeffectinstance = loadSpecifiedEffect(mobeffect, compoundtag.getCompound("HiddenEffect"));
      }

      Optional<MobEffectInstance.FactorData> optional;
      if (compoundtag.contains("FactorCalculationData", 10)) {
         optional = MobEffectInstance.FactorData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.getCompound("FactorCalculationData"))).resultOrPartial(LOGGER::error);
      } else {
         optional = Optional.empty();
      }

      return new MobEffectInstance(mobeffect, j, Math.max(i, 0), flag, flag1, flag2, mobeffectinstance, optional);
   }

   public int compareTo(MobEffectInstance mobeffectinstance) {
      int i = 32147;
      return (this.getDuration() <= 32147 || mobeffectinstance.getDuration() <= 32147) && (!this.isAmbient() || !mobeffectinstance.isAmbient()) ? ComparisonChain.start().compareFalseFirst(this.isAmbient(), mobeffectinstance.isAmbient()).compareFalseFirst(this.isInfiniteDuration(), mobeffectinstance.isInfiniteDuration()).compare(this.getDuration(), mobeffectinstance.getDuration()).compare(this.getEffect().getColor(), mobeffectinstance.getEffect().getColor()).result() : ComparisonChain.start().compare(this.isAmbient(), mobeffectinstance.isAmbient()).compare(this.getEffect().getColor(), mobeffectinstance.getEffect().getColor()).result();
   }

   public static class FactorData {
      public static final Codec<MobEffectInstance.FactorData> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("padding_duration").forGetter((mobeffectinstance_factordata6) -> mobeffectinstance_factordata6.paddingDuration), Codec.FLOAT.fieldOf("factor_start").orElse(0.0F).forGetter((mobeffectinstance_factordata5) -> mobeffectinstance_factordata5.factorStart), Codec.FLOAT.fieldOf("factor_target").orElse(1.0F).forGetter((mobeffectinstance_factordata4) -> mobeffectinstance_factordata4.factorTarget), Codec.FLOAT.fieldOf("factor_current").orElse(0.0F).forGetter((mobeffectinstance_factordata3) -> mobeffectinstance_factordata3.factorCurrent), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_active").orElse(0).forGetter((mobeffectinstance_factordata2) -> mobeffectinstance_factordata2.ticksActive), Codec.FLOAT.fieldOf("factor_previous_frame").orElse(0.0F).forGetter((mobeffectinstance_factordata1) -> mobeffectinstance_factordata1.factorPreviousFrame), Codec.BOOL.fieldOf("had_effect_last_tick").orElse(false).forGetter((mobeffectinstance_factordata) -> mobeffectinstance_factordata.hadEffectLastTick)).apply(recordcodecbuilder_instance, MobEffectInstance.FactorData::new));
      private final int paddingDuration;
      private float factorStart;
      private float factorTarget;
      private float factorCurrent;
      private int ticksActive;
      private float factorPreviousFrame;
      private boolean hadEffectLastTick;

      public FactorData(int i, float f, float f1, float f2, int j, float f3, boolean flag) {
         this.paddingDuration = i;
         this.factorStart = f;
         this.factorTarget = f1;
         this.factorCurrent = f2;
         this.ticksActive = j;
         this.factorPreviousFrame = f3;
         this.hadEffectLastTick = flag;
      }

      public FactorData(int i) {
         this(i, 0.0F, 1.0F, 0.0F, 0, 0.0F, false);
      }

      public void tick(MobEffectInstance mobeffectinstance) {
         this.factorPreviousFrame = this.factorCurrent;
         boolean flag = !mobeffectinstance.endsWithin(this.paddingDuration);
         ++this.ticksActive;
         if (this.hadEffectLastTick != flag) {
            this.hadEffectLastTick = flag;
            this.ticksActive = 0;
            this.factorStart = this.factorCurrent;
            this.factorTarget = flag ? 1.0F : 0.0F;
         }

         float f = Mth.clamp((float)this.ticksActive / (float)this.paddingDuration, 0.0F, 1.0F);
         this.factorCurrent = Mth.lerp(f, this.factorStart, this.factorTarget);
      }

      public float getFactor(LivingEntity livingentity, float f) {
         if (livingentity.isRemoved()) {
            this.factorPreviousFrame = this.factorCurrent;
         }

         return Mth.lerp(f, this.factorPreviousFrame, this.factorCurrent);
      }
   }
}
