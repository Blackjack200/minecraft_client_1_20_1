package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityDataSerializers {
   private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = CrudeIncrementalIntIdentityHashBiMap.create(16);
   public static final EntityDataSerializer<Byte> BYTE = EntityDataSerializer.simple((friendlybytebuf, obyte) -> friendlybytebuf.writeByte(obyte), FriendlyByteBuf::readByte);
   public static final EntityDataSerializer<Integer> INT = EntityDataSerializer.simple(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
   public static final EntityDataSerializer<Long> LONG = EntityDataSerializer.simple(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong);
   public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializer.simple(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
   public static final EntityDataSerializer<String> STRING = EntityDataSerializer.simple(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
   public static final EntityDataSerializer<Component> COMPONENT = EntityDataSerializer.simple(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
   public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = EntityDataSerializer.optional(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
   public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>() {
      public void write(FriendlyByteBuf friendlybytebuf, ItemStack itemstack) {
         friendlybytebuf.writeItem(itemstack);
      }

      public ItemStack read(FriendlyByteBuf friendlybytebuf) {
         return friendlybytebuf.readItem();
      }

      public ItemStack copy(ItemStack itemstack) {
         return itemstack.copy();
      }
   };
   public static final EntityDataSerializer<BlockState> BLOCK_STATE = EntityDataSerializer.simpleId(Block.BLOCK_STATE_REGISTRY);
   public static final EntityDataSerializer<Optional<BlockState>> OPTIONAL_BLOCK_STATE = new EntityDataSerializer.ForValueType<Optional<BlockState>>() {
      public void write(FriendlyByteBuf friendlybytebuf, Optional<BlockState> optional) {
         if (optional.isPresent()) {
            friendlybytebuf.writeVarInt(Block.getId(optional.get()));
         } else {
            friendlybytebuf.writeVarInt(0);
         }

      }

      public Optional<BlockState> read(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt();
         return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
      }
   };
   public static final EntityDataSerializer<Boolean> BOOLEAN = EntityDataSerializer.simple(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
   public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer.ForValueType<ParticleOptions>() {
      public void write(FriendlyByteBuf friendlybytebuf, ParticleOptions particleoptions) {
         friendlybytebuf.writeId(BuiltInRegistries.PARTICLE_TYPE, particleoptions.getType());
         particleoptions.writeToNetwork(friendlybytebuf);
      }

      public ParticleOptions read(FriendlyByteBuf friendlybytebuf) {
         return this.readParticle(friendlybytebuf, friendlybytebuf.readById(BuiltInRegistries.PARTICLE_TYPE));
      }

      private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlybytebuf, ParticleType<T> particletype) {
         return particletype.getDeserializer().fromNetwork(particletype, friendlybytebuf);
      }
   };
   public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer.ForValueType<Rotations>() {
      public void write(FriendlyByteBuf friendlybytebuf, Rotations rotations) {
         friendlybytebuf.writeFloat(rotations.getX());
         friendlybytebuf.writeFloat(rotations.getY());
         friendlybytebuf.writeFloat(rotations.getZ());
      }

      public Rotations read(FriendlyByteBuf friendlybytebuf) {
         return new Rotations(friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat());
      }
   };
   public static final EntityDataSerializer<BlockPos> BLOCK_POS = EntityDataSerializer.simple(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
   public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializer.optional(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
   public static final EntityDataSerializer<Direction> DIRECTION = EntityDataSerializer.simpleEnum(Direction.class);
   public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.optional(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
   public static final EntityDataSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = EntityDataSerializer.optional(FriendlyByteBuf::writeGlobalPos, FriendlyByteBuf::readGlobalPos);
   public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>() {
      public void write(FriendlyByteBuf friendlybytebuf, CompoundTag compoundtag) {
         friendlybytebuf.writeNbt(compoundtag);
      }

      public CompoundTag read(FriendlyByteBuf friendlybytebuf) {
         return friendlybytebuf.readNbt();
      }

      public CompoundTag copy(CompoundTag compoundtag) {
         return compoundtag.copy();
      }
   };
   public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer.ForValueType<VillagerData>() {
      public void write(FriendlyByteBuf friendlybytebuf, VillagerData villagerdata) {
         friendlybytebuf.writeId(BuiltInRegistries.VILLAGER_TYPE, villagerdata.getType());
         friendlybytebuf.writeId(BuiltInRegistries.VILLAGER_PROFESSION, villagerdata.getProfession());
         friendlybytebuf.writeVarInt(villagerdata.getLevel());
      }

      public VillagerData read(FriendlyByteBuf friendlybytebuf) {
         return new VillagerData(friendlybytebuf.readById(BuiltInRegistries.VILLAGER_TYPE), friendlybytebuf.readById(BuiltInRegistries.VILLAGER_PROFESSION), friendlybytebuf.readVarInt());
      }
   };
   public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer.ForValueType<OptionalInt>() {
      public void write(FriendlyByteBuf friendlybytebuf, OptionalInt optionalint) {
         friendlybytebuf.writeVarInt(optionalint.orElse(-1) + 1);
      }

      public OptionalInt read(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt();
         return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
      }
   };
   public static final EntityDataSerializer<Pose> POSE = EntityDataSerializer.simpleEnum(Pose.class);
   public static final EntityDataSerializer<CatVariant> CAT_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.CAT_VARIANT);
   public static final EntityDataSerializer<FrogVariant> FROG_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.FROG_VARIANT);
   public static final EntityDataSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.PAINTING_VARIANT.asHolderIdMap());
   public static final EntityDataSerializer<Sniffer.State> SNIFFER_STATE = EntityDataSerializer.simpleEnum(Sniffer.State.class);
   public static final EntityDataSerializer<Vector3f> VECTOR3 = EntityDataSerializer.simple(FriendlyByteBuf::writeVector3f, FriendlyByteBuf::readVector3f);
   public static final EntityDataSerializer<Quaternionf> QUATERNION = EntityDataSerializer.simple(FriendlyByteBuf::writeQuaternion, FriendlyByteBuf::readQuaternion);

   public static void registerSerializer(EntityDataSerializer<?> entitydataserializer) {
      SERIALIZERS.add(entitydataserializer);
   }

   @Nullable
   public static EntityDataSerializer<?> getSerializer(int i) {
      return SERIALIZERS.byId(i);
   }

   public static int getSerializedId(EntityDataSerializer<?> entitydataserializer) {
      return SERIALIZERS.getId(entitydataserializer);
   }

   private EntityDataSerializers() {
   }

   static {
      registerSerializer(BYTE);
      registerSerializer(INT);
      registerSerializer(LONG);
      registerSerializer(FLOAT);
      registerSerializer(STRING);
      registerSerializer(COMPONENT);
      registerSerializer(OPTIONAL_COMPONENT);
      registerSerializer(ITEM_STACK);
      registerSerializer(BOOLEAN);
      registerSerializer(ROTATIONS);
      registerSerializer(BLOCK_POS);
      registerSerializer(OPTIONAL_BLOCK_POS);
      registerSerializer(DIRECTION);
      registerSerializer(OPTIONAL_UUID);
      registerSerializer(BLOCK_STATE);
      registerSerializer(OPTIONAL_BLOCK_STATE);
      registerSerializer(COMPOUND_TAG);
      registerSerializer(PARTICLE);
      registerSerializer(VILLAGER_DATA);
      registerSerializer(OPTIONAL_UNSIGNED_INT);
      registerSerializer(POSE);
      registerSerializer(CAT_VARIANT);
      registerSerializer(FROG_VARIANT);
      registerSerializer(OPTIONAL_GLOBAL_POS);
      registerSerializer(PAINTING_VARIANT);
      registerSerializer(SNIFFER_STATE);
      registerSerializer(VECTOR3);
      registerSerializer(QUATERNION);
   }
}
