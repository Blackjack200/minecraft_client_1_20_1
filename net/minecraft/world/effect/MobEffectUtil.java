package net.minecraft.world.effect;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class MobEffectUtil {
   public static Component formatDuration(MobEffectInstance mobeffectinstance, float f) {
      if (mobeffectinstance.isInfiniteDuration()) {
         return Component.translatable("effect.duration.infinite");
      } else {
         int i = Mth.floor((float)mobeffectinstance.getDuration() * f);
         return Component.literal(StringUtil.formatTickDuration(i));
      }
   }

   public static boolean hasDigSpeed(LivingEntity livingentity) {
      return livingentity.hasEffect(MobEffects.DIG_SPEED) || livingentity.hasEffect(MobEffects.CONDUIT_POWER);
   }

   public static int getDigSpeedAmplification(LivingEntity livingentity) {
      int i = 0;
      int j = 0;
      if (livingentity.hasEffect(MobEffects.DIG_SPEED)) {
         i = livingentity.getEffect(MobEffects.DIG_SPEED).getAmplifier();
      }

      if (livingentity.hasEffect(MobEffects.CONDUIT_POWER)) {
         j = livingentity.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
      }

      return Math.max(i, j);
   }

   public static boolean hasWaterBreathing(LivingEntity livingentity) {
      return livingentity.hasEffect(MobEffects.WATER_BREATHING) || livingentity.hasEffect(MobEffects.CONDUIT_POWER);
   }

   public static List<ServerPlayer> addEffectToPlayersAround(ServerLevel serverlevel, @Nullable Entity entity, Vec3 vec3, double d0, MobEffectInstance mobeffectinstance, int i) {
      MobEffect mobeffect = mobeffectinstance.getEffect();
      List<ServerPlayer> list = serverlevel.getPlayers((serverplayer1) -> serverplayer1.gameMode.isSurvival() && (entity == null || !entity.isAlliedTo(serverplayer1)) && vec3.closerThan(serverplayer1.position(), d0) && (!serverplayer1.hasEffect(mobeffect) || serverplayer1.getEffect(mobeffect).getAmplifier() < mobeffectinstance.getAmplifier() || serverplayer1.getEffect(mobeffect).endsWithin(i - 1)));
      list.forEach((serverplayer) -> serverplayer.addEffect(new MobEffectInstance(mobeffectinstance), entity));
      return list;
   }
}
