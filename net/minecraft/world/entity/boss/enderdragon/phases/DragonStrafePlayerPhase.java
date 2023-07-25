package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DragonStrafePlayerPhase extends AbstractDragonPhaseInstance {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int FIREBALL_CHARGE_AMOUNT = 5;
   private int fireballCharge;
   @Nullable
   private Path currentPath;
   @Nullable
   private Vec3 targetLocation;
   @Nullable
   private LivingEntity attackTarget;
   private boolean holdingPatternClockwise;

   public DragonStrafePlayerPhase(EnderDragon enderdragon) {
      super(enderdragon);
   }

   public void doServerTick() {
      if (this.attackTarget == null) {
         LOGGER.warn("Skipping player strafe phase because no player was found");
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
      } else {
         if (this.currentPath != null && this.currentPath.isDone()) {
            double d0 = this.attackTarget.getX();
            double d1 = this.attackTarget.getZ();
            double d2 = d0 - this.dragon.getX();
            double d3 = d1 - this.dragon.getZ();
            double d4 = Math.sqrt(d2 * d2 + d3 * d3);
            double d5 = Math.min((double)0.4F + d4 / 80.0D - 1.0D, 10.0D);
            this.targetLocation = new Vec3(d0, this.attackTarget.getY() + d5, d1);
         }

         double d6 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         if (d6 < 100.0D || d6 > 22500.0D) {
            this.findNewTarget();
         }

         double d7 = 64.0D;
         if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0D) {
            if (this.dragon.hasLineOfSight(this.attackTarget)) {
               ++this.fireballCharge;
               Vec3 vec3 = (new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0D, this.attackTarget.getZ() - this.dragon.getZ())).normalize();
               Vec3 vec31 = (new Vec3((double)Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180F)), 0.0D, (double)(-Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180F))))).normalize();
               float f = (float)vec31.dot(vec3);
               float f1 = (float)(Math.acos((double)f) * (double)(180F / (float)Math.PI));
               f1 += 0.5F;
               if (this.fireballCharge >= 5 && f1 >= 0.0F && f1 < 10.0F) {
                  double d8 = 1.0D;
                  Vec3 vec32 = this.dragon.getViewVector(1.0F);
                  double d9 = this.dragon.head.getX() - vec32.x * 1.0D;
                  double d10 = this.dragon.head.getY(0.5D) + 0.5D;
                  double d11 = this.dragon.head.getZ() - vec32.z * 1.0D;
                  double d12 = this.attackTarget.getX() - d9;
                  double d13 = this.attackTarget.getY(0.5D) - d10;
                  double d14 = this.attackTarget.getZ() - d11;
                  if (!this.dragon.isSilent()) {
                     this.dragon.level().levelEvent((Player)null, 1017, this.dragon.blockPosition(), 0);
                  }

                  DragonFireball dragonfireball = new DragonFireball(this.dragon.level(), this.dragon, d12, d13, d14);
                  dragonfireball.moveTo(d9, d10, d11, 0.0F, 0.0F);
                  this.dragon.level().addFreshEntity(dragonfireball);
                  this.fireballCharge = 0;
                  if (this.currentPath != null) {
                     while(!this.currentPath.isDone()) {
                        this.currentPath.advance();
                     }
                  }

                  this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
               }
            } else if (this.fireballCharge > 0) {
               --this.fireballCharge;
            }
         } else if (this.fireballCharge > 0) {
            --this.fireballCharge;
         }

      }
   }

   private void findNewTarget() {
      if (this.currentPath == null || this.currentPath.isDone()) {
         int i = this.dragon.findClosestNode();
         int j = i;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.holdingPatternClockwise = !this.holdingPatternClockwise;
            j = i + 6;
         }

         if (this.holdingPatternClockwise) {
            ++j;
         } else {
            --j;
         }

         if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
            j %= 12;
            if (j < 0) {
               j += 12;
            }
         } else {
            j -= 12;
            j &= 7;
            j += 12;
         }

         this.currentPath = this.dragon.findPath(i, j, (Node)null);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isDone()) {
         Vec3i vec3i = this.currentPath.getNextNodePos();
         this.currentPath.advance();
         double d0 = (double)vec3i.getX();
         double d1 = (double)vec3i.getZ();

         double d2;
         do {
            d2 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(d2 < (double)vec3i.getY());

         this.targetLocation = new Vec3(d0, d2, d1);
      }

   }

   public void begin() {
      this.fireballCharge = 0;
      this.targetLocation = null;
      this.currentPath = null;
      this.attackTarget = null;
   }

   public void setTarget(LivingEntity livingentity) {
      this.attackTarget = livingentity;
      int i = this.dragon.findClosestNode();
      int j = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
      int k = this.attackTarget.getBlockX();
      int l = this.attackTarget.getBlockZ();
      double d0 = (double)k - this.dragon.getX();
      double d1 = (double)l - this.dragon.getZ();
      double d2 = Math.sqrt(d0 * d0 + d1 * d1);
      double d3 = Math.min((double)0.4F + d2 / 80.0D - 1.0D, 10.0D);
      int i1 = Mth.floor(this.attackTarget.getY() + d3);
      Node node = new Node(k, i1, l);
      this.currentPath = this.dragon.findPath(i, j, node);
      if (this.currentPath != null) {
         this.currentPath.advance();
         this.navigateToNextPathNode();
      }

   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonStrafePlayerPhase> getPhase() {
      return EnderDragonPhase.STRAFE_PLAYER;
   }
}
