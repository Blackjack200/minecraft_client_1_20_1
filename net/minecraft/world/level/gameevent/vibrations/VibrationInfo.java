package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record VibrationInfo(GameEvent gameEvent, float distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity) {
   public static final Codec<VibrationInfo> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BuiltInRegistries.GAME_EVENT.byNameCodec().fieldOf("game_event").forGetter(VibrationInfo::gameEvent), Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationInfo::distance), Vec3.CODEC.fieldOf("pos").forGetter(VibrationInfo::pos), UUIDUtil.CODEC.optionalFieldOf("source").forGetter((vibrationinfo1) -> Optional.ofNullable(vibrationinfo1.uuid())), UUIDUtil.CODEC.optionalFieldOf("projectile_owner").forGetter((vibrationinfo) -> Optional.ofNullable(vibrationinfo.projectileOwnerUuid()))).apply(recordcodecbuilder_instance, (gameevent, ofloat, vec3, optional, optional1) -> new VibrationInfo(gameevent, ofloat, vec3, optional.orElse((UUID)null), optional1.orElse((UUID)null))));

   public VibrationInfo(GameEvent gameevent, float f, Vec3 vec3, @Nullable UUID uuid, @Nullable UUID uuid1) {
      this(gameevent, f, vec3, uuid, uuid1, (Entity)null);
   }

   public VibrationInfo(GameEvent gameevent, float f, Vec3 vec3, @Nullable Entity entity) {
      this(gameevent, f, vec3, entity == null ? null : entity.getUUID(), getProjectileOwner(entity), entity);
   }

   @Nullable
   private static UUID getProjectileOwner(@Nullable Entity entity) {
      if (entity instanceof Projectile projectile) {
         if (projectile.getOwner() != null) {
            return projectile.getOwner().getUUID();
         }
      }

      return null;
   }

   public Optional<Entity> getEntity(ServerLevel serverlevel) {
      return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(serverlevel::getEntity));
   }

   public Optional<Entity> getProjectileOwner(ServerLevel serverlevel) {
      return this.getEntity(serverlevel).filter((entity1) -> entity1 instanceof Projectile).map((entity) -> (Projectile)entity).map(Projectile::getOwner).or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(serverlevel::getEntity));
   }
}
