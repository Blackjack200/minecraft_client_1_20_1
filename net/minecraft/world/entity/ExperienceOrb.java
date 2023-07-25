package net.minecraft.world.entity;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ExperienceOrb extends Entity {
   private static final int LIFETIME = 6000;
   private static final int ENTITY_SCAN_PERIOD = 20;
   private static final int MAX_FOLLOW_DIST = 8;
   private static final int ORB_GROUPS_PER_AREA = 40;
   private static final double ORB_MERGE_DISTANCE = 0.5D;
   private int age;
   private int health = 5;
   private int value;
   private int count = 1;
   private Player followingPlayer;

   public ExperienceOrb(Level level, double d0, double d1, double d2, int i) {
      this(EntityType.EXPERIENCE_ORB, level);
      this.setPos(d0, d1, d2);
      this.setYRot((float)(this.random.nextDouble() * 360.0D));
      this.setDeltaMovement((this.random.nextDouble() * (double)0.2F - (double)0.1F) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * (double)0.2F - (double)0.1F) * 2.0D);
      this.value = i;
   }

   public ExperienceOrb(EntityType<? extends ExperienceOrb> entitytype, Level level) {
      super(entitytype, level);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   protected void defineSynchedData() {
   }

   public void tick() {
      super.tick();
      this.xo = this.getX();
      this.yo = this.getY();
      this.zo = this.getZ();
      if (this.isEyeInFluid(FluidTags.WATER)) {
         this.setUnderwaterMovement();
      } else if (!this.isNoGravity()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
      }

      if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
         this.setDeltaMovement((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), (double)0.2F, (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F));
      }

      if (!this.level().noCollision(this.getBoundingBox())) {
         this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
      }

      if (this.tickCount % 20 == 1) {
         this.scanForEntities();
      }

      if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
         this.followingPlayer = null;
      }

      if (this.followingPlayer != null) {
         Vec3 vec3 = new Vec3(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0D - this.getY(), this.followingPlayer.getZ() - this.getZ());
         double d0 = vec3.lengthSqr();
         if (d0 < 64.0D) {
            double d1 = 1.0D - Math.sqrt(d0) / 8.0D;
            this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.1D)));
         }
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      float f = 0.98F;
      if (this.onGround()) {
         f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
      }

      this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.98D, (double)f));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
      }

      ++this.age;
      if (this.age >= 6000) {
         this.discard();
      }

   }

   protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return this.getOnPos(0.999999F);
   }

   private void scanForEntities() {
      if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0D) {
         this.followingPlayer = this.level().getNearestPlayer(this, 8.0D);
      }

      if (this.level() instanceof ServerLevel) {
         for(ExperienceOrb experienceorb : this.level().getEntities(EntityTypeTest.forClass(ExperienceOrb.class), this.getBoundingBox().inflate(0.5D), this::canMerge)) {
            this.merge(experienceorb);
         }
      }

   }

   public static void award(ServerLevel serverlevel, Vec3 vec3, int i) {
      while(i > 0) {
         int j = getExperienceValue(i);
         i -= j;
         if (!tryMergeToExisting(serverlevel, vec3, j)) {
            serverlevel.addFreshEntity(new ExperienceOrb(serverlevel, vec3.x(), vec3.y(), vec3.z(), j));
         }
      }

   }

   private static boolean tryMergeToExisting(ServerLevel serverlevel, Vec3 vec3, int i) {
      AABB aabb = AABB.ofSize(vec3, 1.0D, 1.0D, 1.0D);
      int j = serverlevel.getRandom().nextInt(40);
      List<ExperienceOrb> list = serverlevel.getEntities(EntityTypeTest.forClass(ExperienceOrb.class), aabb, (experienceorb1) -> canMerge(experienceorb1, j, i));
      if (!list.isEmpty()) {
         ExperienceOrb experienceorb = list.get(0);
         ++experienceorb.count;
         experienceorb.age = 0;
         return true;
      } else {
         return false;
      }
   }

   private boolean canMerge(ExperienceOrb experienceorb1) {
      return experienceorb1 != this && canMerge(experienceorb1, this.getId(), this.value);
   }

   private static boolean canMerge(ExperienceOrb experienceorb, int i, int j) {
      return !experienceorb.isRemoved() && (experienceorb.getId() - i) % 40 == 0 && experienceorb.value == j;
   }

   private void merge(ExperienceOrb experienceorb) {
      this.count += experienceorb.count;
      this.age = Math.min(this.age, experienceorb.age);
      experienceorb.discard();
   }

   private void setUnderwaterMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * (double)0.99F, Math.min(vec3.y + (double)5.0E-4F, (double)0.06F), vec3.z * (double)0.99F);
   }

   protected void doWaterSplashEffect() {
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (this.level().isClientSide) {
         return true;
      } else {
         this.markHurt();
         this.health = (int)((float)this.health - f);
         if (this.health <= 0) {
            this.discard();
         }

         return true;
      }
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putShort("Health", (short)this.health);
      compoundtag.putShort("Age", (short)this.age);
      compoundtag.putShort("Value", (short)this.value);
      compoundtag.putInt("Count", this.count);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      this.health = compoundtag.getShort("Health");
      this.age = compoundtag.getShort("Age");
      this.value = compoundtag.getShort("Value");
      this.count = Math.max(compoundtag.getInt("Count"), 1);
   }

   public void playerTouch(Player player) {
      if (!this.level().isClientSide) {
         if (player.takeXpDelay == 0) {
            player.takeXpDelay = 2;
            player.take(this, 1);
            int i = this.repairPlayerItems(player, this.value);
            if (i > 0) {
               player.giveExperiencePoints(i);
            }

            --this.count;
            if (this.count == 0) {
               this.discard();
            }
         }

      }
   }

   private int repairPlayerItems(Player player, int i) {
      Map.Entry<EquipmentSlot, ItemStack> map_entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
      if (map_entry != null) {
         ItemStack itemstack = map_entry.getValue();
         int j = Math.min(this.xpToDurability(this.value), itemstack.getDamageValue());
         itemstack.setDamageValue(itemstack.getDamageValue() - j);
         int k = i - this.durabilityToXp(j);
         return k > 0 ? this.repairPlayerItems(player, k) : 0;
      } else {
         return i;
      }
   }

   private int durabilityToXp(int i) {
      return i / 2;
   }

   private int xpToDurability(int i) {
      return i * 2;
   }

   public int getValue() {
      return this.value;
   }

   public int getIcon() {
      if (this.value >= 2477) {
         return 10;
      } else if (this.value >= 1237) {
         return 9;
      } else if (this.value >= 617) {
         return 8;
      } else if (this.value >= 307) {
         return 7;
      } else if (this.value >= 149) {
         return 6;
      } else if (this.value >= 73) {
         return 5;
      } else if (this.value >= 37) {
         return 4;
      } else if (this.value >= 17) {
         return 3;
      } else if (this.value >= 7) {
         return 2;
      } else {
         return this.value >= 3 ? 1 : 0;
      }
   }

   public static int getExperienceValue(int i) {
      if (i >= 2477) {
         return 2477;
      } else if (i >= 1237) {
         return 1237;
      } else if (i >= 617) {
         return 617;
      } else if (i >= 307) {
         return 307;
      } else if (i >= 149) {
         return 149;
      } else if (i >= 73) {
         return 73;
      } else if (i >= 37) {
         return 37;
      } else if (i >= 17) {
         return 17;
      } else if (i >= 7) {
         return 7;
      } else {
         return i >= 3 ? 3 : 1;
      }
   }

   public boolean isAttackable() {
      return false;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddExperienceOrbPacket(this);
   }

   public SoundSource getSoundSource() {
      return SoundSource.AMBIENT;
   }
}
