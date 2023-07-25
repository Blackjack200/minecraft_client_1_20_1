package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
   public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid), Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter((entitypositionsource) -> entitypositionsource.yOffset)).apply(recordcodecbuilder_instance, (uuid, ofloat) -> new EntityPositionSource(Either.right(Either.left(uuid)), ofloat)));
   private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
   final float yOffset;

   public EntityPositionSource(Entity entity, float f) {
      this(Either.left(entity), f);
   }

   EntityPositionSource(Either<Entity, Either<UUID, Integer>> either, float f) {
      this.entityOrUuidOrId = either;
      this.yOffset = f;
   }

   public Optional<Vec3> getPosition(Level level) {
      if (this.entityOrUuidOrId.left().isEmpty()) {
         this.resolveEntity(level);
      }

      return this.entityOrUuidOrId.left().map((entity) -> entity.position().add(0.0D, (double)this.yOffset, 0.0D));
   }

   private void resolveEntity(Level level) {
      this.entityOrUuidOrId.map(Optional::of, (either) -> Optional.ofNullable(either.map((uuid) -> {
            Entity var10000;
            if (level instanceof ServerLevel serverlevel) {
               var10000 = serverlevel.getEntity(uuid);
            } else {
               var10000 = null;
            }

            return var10000;
         }, level::getEntity))).ifPresent((entity) -> this.entityOrUuidOrId = Either.left(entity));
   }

   private UUID getUuid() {
      return this.entityOrUuidOrId.map(Entity::getUUID, (either) -> either.map(Function.identity(), (integer) -> {
            throw new RuntimeException("Unable to get entityId from uuid");
         }));
   }

   int getId() {
      return this.entityOrUuidOrId.map(Entity::getId, (either) -> either.map((uuid) -> {
            throw new IllegalStateException("Unable to get entityId from uuid");
         }, Function.identity()));
   }

   public PositionSourceType<?> getType() {
      return PositionSourceType.ENTITY;
   }

   public static class Type implements PositionSourceType<EntityPositionSource> {
      public EntityPositionSource read(FriendlyByteBuf friendlybytebuf) {
         return new EntityPositionSource(Either.right(Either.right(friendlybytebuf.readVarInt())), friendlybytebuf.readFloat());
      }

      public void write(FriendlyByteBuf friendlybytebuf, EntityPositionSource entitypositionsource) {
         friendlybytebuf.writeVarInt(entitypositionsource.getId());
         friendlybytebuf.writeFloat(entitypositionsource.yOffset);
      }

      public Codec<EntityPositionSource> codec() {
         return EntityPositionSource.CODEC;
      }
   }
}
