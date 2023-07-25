package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem extends Item {
   public static final int ANIMATION_DURATION = 10;
   private static final int USE_DURATION = 200;
   private static final double MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0D;

   public BrushItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Player player = useoncontext.getPlayer();
      if (player != null && this.calculateHitResult(player).getType() == HitResult.Type.BLOCK) {
         player.startUsingItem(useoncontext.getHand());
      }

      return InteractionResult.CONSUME;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.BRUSH;
   }

   public int getUseDuration(ItemStack itemstack) {
      return 200;
   }

   public void onUseTick(Level level, LivingEntity livingentity, ItemStack itemstack, int i) {
      if (i >= 0 && livingentity instanceof Player player) {
         HitResult hitresult = this.calculateHitResult(livingentity);
         if (hitresult instanceof BlockHitResult blockhitresult) {
            if (hitresult.getType() == HitResult.Type.BLOCK) {
               int j = this.getUseDuration(itemstack) - i + 1;
               boolean flag = j % 10 == 5;
               if (flag) {
                  BlockPos blockpos = blockhitresult.getBlockPos();
                  BlockState blockstate = level.getBlockState(blockpos);
                  HumanoidArm humanoidarm = livingentity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                  this.spawnDustParticles(level, blockhitresult, blockstate, livingentity.getViewVector(0.0F), humanoidarm);
                  Block flag1 = blockstate.getBlock();
                  SoundEvent soundevent;
                  if (flag1 instanceof BrushableBlock) {
                     BrushableBlock brushableblock = (BrushableBlock)flag1;
                     soundevent = brushableblock.getBrushSound();
                  } else {
                     soundevent = SoundEvents.BRUSH_GENERIC;
                  }

                  level.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);
                  if (!level.isClientSide()) {
                     BlockEntity var18 = level.getBlockEntity(blockpos);
                     if (var18 instanceof BrushableBlockEntity) {
                        BrushableBlockEntity brushableblockentity = (BrushableBlockEntity)var18;
                        boolean flag1 = brushableblockentity.brush(level.getGameTime(), player, blockhitresult.getDirection());
                        if (flag1) {
                           EquipmentSlot equipmentslot = itemstack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                           itemstack.hurtAndBreak(1, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(equipmentslot));
                        }
                     }
                  }
               }

               return;
            }
         }

         livingentity.releaseUsingItem();
      } else {
         livingentity.releaseUsingItem();
      }
   }

   private HitResult calculateHitResult(LivingEntity livingentity) {
      return ProjectileUtil.getHitResultOnViewVector(livingentity, (entity) -> !entity.isSpectator() && entity.isPickable(), MAX_BRUSH_DISTANCE);
   }

   public void spawnDustParticles(Level level, BlockHitResult blockhitresult, BlockState blockstate, Vec3 vec3, HumanoidArm humanoidarm) {
      double d0 = 3.0D;
      int i = humanoidarm == HumanoidArm.RIGHT ? 1 : -1;
      int j = level.getRandom().nextInt(7, 12);
      BlockParticleOption blockparticleoption = new BlockParticleOption(ParticleTypes.BLOCK, blockstate);
      Direction direction = blockhitresult.getDirection();
      BrushItem.DustParticlesDelta brushitem_dustparticlesdelta = BrushItem.DustParticlesDelta.fromDirection(vec3, direction);
      Vec3 vec31 = blockhitresult.getLocation();

      for(int k = 0; k < j; ++k) {
         level.addParticle(blockparticleoption, vec31.x - (double)(direction == Direction.WEST ? 1.0E-6F : 0.0F), vec31.y, vec31.z - (double)(direction == Direction.NORTH ? 1.0E-6F : 0.0F), brushitem_dustparticlesdelta.xd() * (double)i * 3.0D * level.getRandom().nextDouble(), 0.0D, brushitem_dustparticlesdelta.zd() * (double)i * 3.0D * level.getRandom().nextDouble());
      }

   }

   static record DustParticlesDelta(double xd, double yd, double zd) {
      private static final double ALONG_SIDE_DELTA = 1.0D;
      private static final double OUT_FROM_SIDE_DELTA = 0.1D;

      public static BrushItem.DustParticlesDelta fromDirection(Vec3 vec3, Direction direction) {
         double d0 = 0.0D;
         BrushItem.DustParticlesDelta var10000;
         switch (direction) {
            case DOWN:
            case UP:
               var10000 = new BrushItem.DustParticlesDelta(vec3.z(), 0.0D, -vec3.x());
               break;
            case NORTH:
               var10000 = new BrushItem.DustParticlesDelta(1.0D, 0.0D, -0.1D);
               break;
            case SOUTH:
               var10000 = new BrushItem.DustParticlesDelta(-1.0D, 0.0D, 0.1D);
               break;
            case WEST:
               var10000 = new BrushItem.DustParticlesDelta(-0.1D, 0.0D, -1.0D);
               break;
            case EAST:
               var10000 = new BrushItem.DustParticlesDelta(0.1D, 0.0D, 1.0D);
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }
}
