package net.minecraft.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FriendlyByteBuf extends ByteBuf {
   private static final int MAX_VARINT_SIZE = 5;
   private static final int MAX_VARLONG_SIZE = 10;
   public static final int DEFAULT_NBT_QUOTA = 2097152;
   private final ByteBuf source;
   public static final short MAX_STRING_LENGTH = Short.MAX_VALUE;
   public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
   private static final int PUBLIC_KEY_SIZE = 256;
   private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
   private static final int MAX_PUBLIC_KEY_LENGTH = 512;
   private static final Gson GSON = new Gson();

   public FriendlyByteBuf(ByteBuf bytebuf) {
      this.source = bytebuf;
   }

   public static int getVarIntSize(int i) {
      for(int j = 1; j < 5; ++j) {
         if ((i & -1 << j * 7) == 0) {
            return j;
         }
      }

      return 5;
   }

   public static int getVarLongSize(long i) {
      for(int j = 1; j < 10; ++j) {
         if ((i & -1L << j * 7) == 0L) {
            return j;
         }
      }

      return 10;
   }

   /** @deprecated */
   @Deprecated
   public <T> T readWithCodec(DynamicOps<Tag> dynamicops, Codec<T> codec) {
      CompoundTag compoundtag = this.readAnySizeNbt();
      return Util.getOrThrow(codec.parse(dynamicops, compoundtag), (s) -> new DecoderException("Failed to decode: " + s + " " + compoundtag));
   }

   /** @deprecated */
   @Deprecated
   public <T> void writeWithCodec(DynamicOps<Tag> dynamicops, Codec<T> codec, T object) {
      Tag tag = Util.getOrThrow(codec.encodeStart(dynamicops, object), (s) -> new EncoderException("Failed to encode: " + s + " " + object));
      this.writeNbt((CompoundTag)tag);
   }

   public <T> T readJsonWithCodec(Codec<T> codec) {
      JsonElement jsonelement = GsonHelper.fromJson(GSON, this.readUtf(), JsonElement.class);
      DataResult<T> dataresult = codec.parse(JsonOps.INSTANCE, jsonelement);
      return Util.getOrThrow(dataresult, (s) -> new DecoderException("Failed to decode json: " + s));
   }

   public <T> void writeJsonWithCodec(Codec<T> codec, T object) {
      DataResult<JsonElement> dataresult = codec.encodeStart(JsonOps.INSTANCE, object);
      this.writeUtf(GSON.toJson(Util.getOrThrow(dataresult, (s) -> new EncoderException("Failed to encode: " + s + " " + object))));
   }

   public <T> void writeId(IdMap<T> idmap, T object) {
      int i = idmap.getId(object);
      if (i == -1) {
         throw new IllegalArgumentException("Can't find id for '" + object + "' in map " + idmap);
      } else {
         this.writeVarInt(i);
      }
   }

   public <T> void writeId(IdMap<Holder<T>> idmap, Holder<T> holder, FriendlyByteBuf.Writer<T> friendlybytebuf_writer) {
      switch (holder.kind()) {
         case REFERENCE:
            int i = idmap.getId(holder);
            if (i == -1) {
               throw new IllegalArgumentException("Can't find id for '" + holder.value() + "' in map " + idmap);
            }

            this.writeVarInt(i + 1);
            break;
         case DIRECT:
            this.writeVarInt(0);
            friendlybytebuf_writer.accept((T)this, holder.value());
      }

   }

   @Nullable
   public <T> T readById(IdMap<T> idmap) {
      int i = this.readVarInt();
      return idmap.byId(i);
   }

   public <T> Holder<T> readById(IdMap<Holder<T>> idmap, FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      int i = this.readVarInt();
      if (i == 0) {
         return Holder.direct(friendlybytebuf_reader.apply((T)this));
      } else {
         Holder<T> holder = idmap.byId(i - 1);
         if (holder == null) {
            throw new IllegalArgumentException("Can't find element with id " + i);
         } else {
            return holder;
         }
      }
   }

   public static <T> IntFunction<T> limitValue(IntFunction<T> intfunction, int i) {
      return (k) -> {
         if (k > i) {
            throw new DecoderException("Value " + k + " is larger than limit " + i);
         } else {
            return intfunction.apply(k);
         }
      };
   }

   public <T, C extends Collection<T>> C readCollection(IntFunction<C> intfunction, FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      int i = this.readVarInt();
      C collection = intfunction.apply(i);

      for(int j = 0; j < i; ++j) {
         collection.add(friendlybytebuf_reader.apply((T)this));
      }

      return collection;
   }

   public <T> void writeCollection(Collection<T> collection, FriendlyByteBuf.Writer<T> friendlybytebuf_writer) {
      this.writeVarInt(collection.size());

      for(T object : collection) {
         friendlybytebuf_writer.accept((T)this, object);
      }

   }

   public <T> List<T> readList(FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      return this.readCollection(Lists::newArrayListWithCapacity, friendlybytebuf_reader);
   }

   public IntList readIntIdList() {
      int i = this.readVarInt();
      IntList intlist = new IntArrayList();

      for(int j = 0; j < i; ++j) {
         intlist.add(this.readVarInt());
      }

      return intlist;
   }

   public void writeIntIdList(IntList intlist) {
      this.writeVarInt(intlist.size());
      intlist.forEach(this::writeVarInt);
   }

   public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> intfunction, FriendlyByteBuf.Reader<K> friendlybytebuf_reader, FriendlyByteBuf.Reader<V> friendlybytebuf_reader1) {
      int i = this.readVarInt();
      M map = intfunction.apply(i);

      for(int j = 0; j < i; ++j) {
         K object = friendlybytebuf_reader.apply((K)this);
         V object1 = friendlybytebuf_reader1.apply((V)this);
         map.put(object, object1);
      }

      return map;
   }

   public <K, V> Map<K, V> readMap(FriendlyByteBuf.Reader<K> friendlybytebuf_reader, FriendlyByteBuf.Reader<V> friendlybytebuf_reader1) {
      return this.readMap(Maps::newHashMapWithExpectedSize, friendlybytebuf_reader, friendlybytebuf_reader1);
   }

   public <K, V> void writeMap(Map<K, V> map, FriendlyByteBuf.Writer<K> friendlybytebuf_writer, FriendlyByteBuf.Writer<V> friendlybytebuf_writer1) {
      this.writeVarInt(map.size());
      map.forEach((object, object1) -> {
         friendlybytebuf_writer.accept((K)this, object);
         friendlybytebuf_writer1.accept((V)this, object1);
      });
   }

   public void readWithCount(Consumer<FriendlyByteBuf> consumer) {
      int i = this.readVarInt();

      for(int j = 0; j < i; ++j) {
         consumer.accept(this);
      }

   }

   public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumset, Class<E> oclass) {
      E[] aenum = oclass.getEnumConstants();
      BitSet bitset = new BitSet(aenum.length);

      for(int i = 0; i < aenum.length; ++i) {
         bitset.set(i, enumset.contains(aenum[i]));
      }

      this.writeFixedBitSet(bitset, aenum.length);
   }

   public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> oclass) {
      E[] aenum = oclass.getEnumConstants();
      BitSet bitset = this.readFixedBitSet(aenum.length);
      EnumSet<E> enumset = EnumSet.noneOf(oclass);

      for(int i = 0; i < aenum.length; ++i) {
         if (bitset.get(i)) {
            enumset.add(aenum[i]);
         }
      }

      return enumset;
   }

   public <T> void writeOptional(Optional<T> optional, FriendlyByteBuf.Writer<T> friendlybytebuf_writer) {
      if (optional.isPresent()) {
         this.writeBoolean(true);
         friendlybytebuf_writer.accept((T)this, optional.get());
      } else {
         this.writeBoolean(false);
      }

   }

   public <T> Optional<T> readOptional(FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      return this.readBoolean() ? Optional.of(friendlybytebuf_reader.apply((T)this)) : Optional.empty();
   }

   @Nullable
   public <T> T readNullable(FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      return (T)(this.readBoolean() ? friendlybytebuf_reader.apply((T)this) : null);
   }

   public <T> void writeNullable(@Nullable T object, FriendlyByteBuf.Writer<T> friendlybytebuf_writer) {
      if (object != null) {
         this.writeBoolean(true);
         friendlybytebuf_writer.accept((T)this, object);
      } else {
         this.writeBoolean(false);
      }

   }

   public <L, R> void writeEither(Either<L, R> either, FriendlyByteBuf.Writer<L> friendlybytebuf_writer, FriendlyByteBuf.Writer<R> friendlybytebuf_writer1) {
      either.ifLeft((object1) -> {
         this.writeBoolean(true);
         friendlybytebuf_writer.accept((L)this, object1);
      }).ifRight((object) -> {
         this.writeBoolean(false);
         friendlybytebuf_writer1.accept((R)this, object);
      });
   }

   public <L, R> Either<L, R> readEither(FriendlyByteBuf.Reader<L> friendlybytebuf_reader, FriendlyByteBuf.Reader<R> friendlybytebuf_reader1) {
      return this.readBoolean() ? Either.left(friendlybytebuf_reader.apply((L)this)) : Either.right(friendlybytebuf_reader1.apply((R)this));
   }

   public byte[] readByteArray() {
      return this.readByteArray(this.readableBytes());
   }

   public FriendlyByteBuf writeByteArray(byte[] abyte) {
      this.writeVarInt(abyte.length);
      this.writeBytes(abyte);
      return this;
   }

   public byte[] readByteArray(int i) {
      int j = this.readVarInt();
      if (j > i) {
         throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
      } else {
         byte[] abyte = new byte[j];
         this.readBytes(abyte);
         return abyte;
      }
   }

   public FriendlyByteBuf writeVarIntArray(int[] aint) {
      this.writeVarInt(aint.length);

      for(int i : aint) {
         this.writeVarInt(i);
      }

      return this;
   }

   public int[] readVarIntArray() {
      return this.readVarIntArray(this.readableBytes());
   }

   public int[] readVarIntArray(int i) {
      int j = this.readVarInt();
      if (j > i) {
         throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + i);
      } else {
         int[] aint = new int[j];

         for(int k = 0; k < aint.length; ++k) {
            aint[k] = this.readVarInt();
         }

         return aint;
      }
   }

   public FriendlyByteBuf writeLongArray(long[] along) {
      this.writeVarInt(along.length);

      for(long i : along) {
         this.writeLong(i);
      }

      return this;
   }

   public long[] readLongArray() {
      return this.readLongArray((long[])null);
   }

   public long[] readLongArray(@Nullable long[] along) {
      return this.readLongArray(along, this.readableBytes() / 8);
   }

   public long[] readLongArray(@Nullable long[] along, int i) {
      int j = this.readVarInt();
      if (along == null || along.length != j) {
         if (j > i) {
            throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + i);
         }

         along = new long[j];
      }

      for(int k = 0; k < along.length; ++k) {
         along[k] = this.readLong();
      }

      return along;
   }

   @VisibleForTesting
   public byte[] accessByteBufWithCorrectSize() {
      int i = this.writerIndex();
      byte[] abyte = new byte[i];
      this.getBytes(0, abyte);
      return abyte;
   }

   public BlockPos readBlockPos() {
      return BlockPos.of(this.readLong());
   }

   public FriendlyByteBuf writeBlockPos(BlockPos blockpos) {
      this.writeLong(blockpos.asLong());
      return this;
   }

   public ChunkPos readChunkPos() {
      return new ChunkPos(this.readLong());
   }

   public FriendlyByteBuf writeChunkPos(ChunkPos chunkpos) {
      this.writeLong(chunkpos.toLong());
      return this;
   }

   public SectionPos readSectionPos() {
      return SectionPos.of(this.readLong());
   }

   public FriendlyByteBuf writeSectionPos(SectionPos sectionpos) {
      this.writeLong(sectionpos.asLong());
      return this;
   }

   public GlobalPos readGlobalPos() {
      ResourceKey<Level> resourcekey = this.readResourceKey(Registries.DIMENSION);
      BlockPos blockpos = this.readBlockPos();
      return GlobalPos.of(resourcekey, blockpos);
   }

   public void writeGlobalPos(GlobalPos globalpos) {
      this.writeResourceKey(globalpos.dimension());
      this.writeBlockPos(globalpos.pos());
   }

   public Vector3f readVector3f() {
      return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
   }

   public void writeVector3f(Vector3f vector3f) {
      this.writeFloat(vector3f.x());
      this.writeFloat(vector3f.y());
      this.writeFloat(vector3f.z());
   }

   public Quaternionf readQuaternion() {
      return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
   }

   public void writeQuaternion(Quaternionf quaternionf) {
      this.writeFloat(quaternionf.x);
      this.writeFloat(quaternionf.y);
      this.writeFloat(quaternionf.z);
      this.writeFloat(quaternionf.w);
   }

   public Component readComponent() {
      Component component = Component.Serializer.fromJson(this.readUtf(262144));
      if (component == null) {
         throw new DecoderException("Received unexpected null component");
      } else {
         return component;
      }
   }

   public FriendlyByteBuf writeComponent(Component component) {
      return this.writeUtf(Component.Serializer.toJson(component), 262144);
   }

   public <T extends Enum<T>> T readEnum(Class<T> oclass) {
      return (oclass.getEnumConstants())[this.readVarInt()];
   }

   public FriendlyByteBuf writeEnum(Enum<?> oenum) {
      return this.writeVarInt(oenum.ordinal());
   }

   public int readVarInt() {
      int i = 0;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (b0 & 127) << j++ * 7;
         if (j > 5) {
            throw new RuntimeException("VarInt too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   public long readVarLong() {
      long i = 0L;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (long)(b0 & 127) << j++ * 7;
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   public FriendlyByteBuf writeUUID(UUID uuid) {
      this.writeLong(uuid.getMostSignificantBits());
      this.writeLong(uuid.getLeastSignificantBits());
      return this;
   }

   public UUID readUUID() {
      return new UUID(this.readLong(), this.readLong());
   }

   public FriendlyByteBuf writeVarInt(int i) {
      while((i & -128) != 0) {
         this.writeByte(i & 127 | 128);
         i >>>= 7;
      }

      this.writeByte(i);
      return this;
   }

   public FriendlyByteBuf writeVarLong(long i) {
      while((i & -128L) != 0L) {
         this.writeByte((int)(i & 127L) | 128);
         i >>>= 7;
      }

      this.writeByte((int)i);
      return this;
   }

   public FriendlyByteBuf writeNbt(@Nullable CompoundTag compoundtag) {
      if (compoundtag == null) {
         this.writeByte(0);
      } else {
         try {
            NbtIo.write(compoundtag, new ByteBufOutputStream(this));
         } catch (IOException var3) {
            throw new EncoderException(var3);
         }
      }

      return this;
   }

   @Nullable
   public CompoundTag readNbt() {
      return this.readNbt(new NbtAccounter(2097152L));
   }

   @Nullable
   public CompoundTag readAnySizeNbt() {
      return this.readNbt(NbtAccounter.UNLIMITED);
   }

   @Nullable
   public CompoundTag readNbt(NbtAccounter nbtaccounter) {
      int i = this.readerIndex();
      byte b0 = this.readByte();
      if (b0 == 0) {
         return null;
      } else {
         this.readerIndex(i);

         try {
            return NbtIo.read(new ByteBufInputStream(this), nbtaccounter);
         } catch (IOException var5) {
            throw new EncoderException(var5);
         }
      }
   }

   public FriendlyByteBuf writeItem(ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         this.writeBoolean(false);
      } else {
         this.writeBoolean(true);
         Item item = itemstack.getItem();
         this.writeId(BuiltInRegistries.ITEM, item);
         this.writeByte(itemstack.getCount());
         CompoundTag compoundtag = null;
         if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
            compoundtag = itemstack.getTag();
         }

         this.writeNbt(compoundtag);
      }

      return this;
   }

   public ItemStack readItem() {
      if (!this.readBoolean()) {
         return ItemStack.EMPTY;
      } else {
         Item item = this.readById(BuiltInRegistries.ITEM);
         int i = this.readByte();
         ItemStack itemstack = new ItemStack(item, i);
         itemstack.setTag(this.readNbt());
         return itemstack;
      }
   }

   public String readUtf() {
      return this.readUtf(32767);
   }

   public String readUtf(int i) {
      int j = getMaxEncodedUtfLength(i);
      int k = this.readVarInt();
      if (k > j) {
         throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
      } else if (k < 0) {
         throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
      } else {
         String s = this.toString(this.readerIndex(), k, StandardCharsets.UTF_8);
         this.readerIndex(this.readerIndex() + k);
         if (s.length() > i) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + s.length() + " > " + i + ")");
         } else {
            return s;
         }
      }
   }

   public FriendlyByteBuf writeUtf(String s) {
      return this.writeUtf(s, 32767);
   }

   public FriendlyByteBuf writeUtf(String s, int i) {
      if (s.length() > i) {
         throw new EncoderException("String too big (was " + s.length() + " characters, max " + i + ")");
      } else {
         byte[] abyte = s.getBytes(StandardCharsets.UTF_8);
         int j = getMaxEncodedUtfLength(i);
         if (abyte.length > j) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + j + ")");
         } else {
            this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
         }
      }
   }

   private static int getMaxEncodedUtfLength(int i) {
      return i * 3;
   }

   public ResourceLocation readResourceLocation() {
      return new ResourceLocation(this.readUtf(32767));
   }

   public FriendlyByteBuf writeResourceLocation(ResourceLocation resourcelocation) {
      this.writeUtf(resourcelocation.toString());
      return this;
   }

   public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> resourcekey) {
      ResourceLocation resourcelocation = this.readResourceLocation();
      return ResourceKey.create(resourcekey, resourcelocation);
   }

   public void writeResourceKey(ResourceKey<?> resourcekey) {
      this.writeResourceLocation(resourcekey.location());
   }

   public Date readDate() {
      return new Date(this.readLong());
   }

   public FriendlyByteBuf writeDate(Date date) {
      this.writeLong(date.getTime());
      return this;
   }

   public Instant readInstant() {
      return Instant.ofEpochMilli(this.readLong());
   }

   public void writeInstant(Instant instant) {
      this.writeLong(instant.toEpochMilli());
   }

   public PublicKey readPublicKey() {
      try {
         return Crypt.byteToPublicKey(this.readByteArray(512));
      } catch (CryptException var2) {
         throw new DecoderException("Malformed public key bytes", var2);
      }
   }

   public FriendlyByteBuf writePublicKey(PublicKey publickey) {
      this.writeByteArray(publickey.getEncoded());
      return this;
   }

   public BlockHitResult readBlockHitResult() {
      BlockPos blockpos = this.readBlockPos();
      Direction direction = this.readEnum(Direction.class);
      float f = this.readFloat();
      float f1 = this.readFloat();
      float f2 = this.readFloat();
      boolean flag = this.readBoolean();
      return new BlockHitResult(new Vec3((double)blockpos.getX() + (double)f, (double)blockpos.getY() + (double)f1, (double)blockpos.getZ() + (double)f2), direction, blockpos, flag);
   }

   public void writeBlockHitResult(BlockHitResult blockhitresult) {
      BlockPos blockpos = blockhitresult.getBlockPos();
      this.writeBlockPos(blockpos);
      this.writeEnum(blockhitresult.getDirection());
      Vec3 vec3 = blockhitresult.getLocation();
      this.writeFloat((float)(vec3.x - (double)blockpos.getX()));
      this.writeFloat((float)(vec3.y - (double)blockpos.getY()));
      this.writeFloat((float)(vec3.z - (double)blockpos.getZ()));
      this.writeBoolean(blockhitresult.isInside());
   }

   public BitSet readBitSet() {
      return BitSet.valueOf(this.readLongArray());
   }

   public void writeBitSet(BitSet bitset) {
      this.writeLongArray(bitset.toLongArray());
   }

   public BitSet readFixedBitSet(int i) {
      byte[] abyte = new byte[Mth.positiveCeilDiv(i, 8)];
      this.readBytes(abyte);
      return BitSet.valueOf(abyte);
   }

   public void writeFixedBitSet(BitSet bitset, int i) {
      if (bitset.length() > i) {
         throw new EncoderException("BitSet is larger than expected size (" + bitset.length() + ">" + i + ")");
      } else {
         byte[] abyte = bitset.toByteArray();
         this.writeBytes(Arrays.copyOf(abyte, Mth.positiveCeilDiv(i, 8)));
      }
   }

   public GameProfile readGameProfile() {
      UUID uuid = this.readUUID();
      String s = this.readUtf(16);
      GameProfile gameprofile = new GameProfile(uuid, s);
      gameprofile.getProperties().putAll(this.readGameProfileProperties());
      return gameprofile;
   }

   public void writeGameProfile(GameProfile gameprofile) {
      this.writeUUID(gameprofile.getId());
      this.writeUtf(gameprofile.getName());
      this.writeGameProfileProperties(gameprofile.getProperties());
   }

   public PropertyMap readGameProfileProperties() {
      PropertyMap propertymap = new PropertyMap();
      this.readWithCount((friendlybytebuf) -> {
         Property property = this.readProperty();
         propertymap.put(property.getName(), property);
      });
      return propertymap;
   }

   public void writeGameProfileProperties(PropertyMap propertymap) {
      this.writeCollection(propertymap.values(), FriendlyByteBuf::writeProperty);
   }

   public Property readProperty() {
      String s = this.readUtf();
      String s1 = this.readUtf();
      if (this.readBoolean()) {
         String s2 = this.readUtf();
         return new Property(s, s1, s2);
      } else {
         return new Property(s, s1);
      }
   }

   public void writeProperty(Property property) {
      this.writeUtf(property.getName());
      this.writeUtf(property.getValue());
      if (property.hasSignature()) {
         this.writeBoolean(true);
         this.writeUtf(property.getSignature());
      } else {
         this.writeBoolean(false);
      }

   }

   public int capacity() {
      return this.source.capacity();
   }

   public ByteBuf capacity(int i) {
      return this.source.capacity(i);
   }

   public int maxCapacity() {
      return this.source.maxCapacity();
   }

   public ByteBufAllocator alloc() {
      return this.source.alloc();
   }

   public ByteOrder order() {
      return this.source.order();
   }

   public ByteBuf order(ByteOrder byteorder) {
      return this.source.order(byteorder);
   }

   public ByteBuf unwrap() {
      return this.source.unwrap();
   }

   public boolean isDirect() {
      return this.source.isDirect();
   }

   public boolean isReadOnly() {
      return this.source.isReadOnly();
   }

   public ByteBuf asReadOnly() {
      return this.source.asReadOnly();
   }

   public int readerIndex() {
      return this.source.readerIndex();
   }

   public ByteBuf readerIndex(int i) {
      return this.source.readerIndex(i);
   }

   public int writerIndex() {
      return this.source.writerIndex();
   }

   public ByteBuf writerIndex(int i) {
      return this.source.writerIndex(i);
   }

   public ByteBuf setIndex(int i, int j) {
      return this.source.setIndex(i, j);
   }

   public int readableBytes() {
      return this.source.readableBytes();
   }

   public int writableBytes() {
      return this.source.writableBytes();
   }

   public int maxWritableBytes() {
      return this.source.maxWritableBytes();
   }

   public boolean isReadable() {
      return this.source.isReadable();
   }

   public boolean isReadable(int i) {
      return this.source.isReadable(i);
   }

   public boolean isWritable() {
      return this.source.isWritable();
   }

   public boolean isWritable(int i) {
      return this.source.isWritable(i);
   }

   public ByteBuf clear() {
      return this.source.clear();
   }

   public ByteBuf markReaderIndex() {
      return this.source.markReaderIndex();
   }

   public ByteBuf resetReaderIndex() {
      return this.source.resetReaderIndex();
   }

   public ByteBuf markWriterIndex() {
      return this.source.markWriterIndex();
   }

   public ByteBuf resetWriterIndex() {
      return this.source.resetWriterIndex();
   }

   public ByteBuf discardReadBytes() {
      return this.source.discardReadBytes();
   }

   public ByteBuf discardSomeReadBytes() {
      return this.source.discardSomeReadBytes();
   }

   public ByteBuf ensureWritable(int i) {
      return this.source.ensureWritable(i);
   }

   public int ensureWritable(int i, boolean flag) {
      return this.source.ensureWritable(i, flag);
   }

   public boolean getBoolean(int i) {
      return this.source.getBoolean(i);
   }

   public byte getByte(int i) {
      return this.source.getByte(i);
   }

   public short getUnsignedByte(int i) {
      return this.source.getUnsignedByte(i);
   }

   public short getShort(int i) {
      return this.source.getShort(i);
   }

   public short getShortLE(int i) {
      return this.source.getShortLE(i);
   }

   public int getUnsignedShort(int i) {
      return this.source.getUnsignedShort(i);
   }

   public int getUnsignedShortLE(int i) {
      return this.source.getUnsignedShortLE(i);
   }

   public int getMedium(int i) {
      return this.source.getMedium(i);
   }

   public int getMediumLE(int i) {
      return this.source.getMediumLE(i);
   }

   public int getUnsignedMedium(int i) {
      return this.source.getUnsignedMedium(i);
   }

   public int getUnsignedMediumLE(int i) {
      return this.source.getUnsignedMediumLE(i);
   }

   public int getInt(int i) {
      return this.source.getInt(i);
   }

   public int getIntLE(int i) {
      return this.source.getIntLE(i);
   }

   public long getUnsignedInt(int i) {
      return this.source.getUnsignedInt(i);
   }

   public long getUnsignedIntLE(int i) {
      return this.source.getUnsignedIntLE(i);
   }

   public long getLong(int i) {
      return this.source.getLong(i);
   }

   public long getLongLE(int i) {
      return this.source.getLongLE(i);
   }

   public char getChar(int i) {
      return this.source.getChar(i);
   }

   public float getFloat(int i) {
      return this.source.getFloat(i);
   }

   public double getDouble(int i) {
      return this.source.getDouble(i);
   }

   public ByteBuf getBytes(int i, ByteBuf bytebuf) {
      return this.source.getBytes(i, bytebuf);
   }

   public ByteBuf getBytes(int i, ByteBuf bytebuf, int j) {
      return this.source.getBytes(i, bytebuf, j);
   }

   public ByteBuf getBytes(int i, ByteBuf bytebuf, int j, int k) {
      return this.source.getBytes(i, bytebuf, j, k);
   }

   public ByteBuf getBytes(int i, byte[] abyte) {
      return this.source.getBytes(i, abyte);
   }

   public ByteBuf getBytes(int i, byte[] abyte, int j, int k) {
      return this.source.getBytes(i, abyte, j, k);
   }

   public ByteBuf getBytes(int i, ByteBuffer bytebuffer) {
      return this.source.getBytes(i, bytebuffer);
   }

   public ByteBuf getBytes(int i, OutputStream outputstream, int j) throws IOException {
      return this.source.getBytes(i, outputstream, j);
   }

   public int getBytes(int i, GatheringByteChannel gatheringbytechannel, int j) throws IOException {
      return this.source.getBytes(i, gatheringbytechannel, j);
   }

   public int getBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
      return this.source.getBytes(i, filechannel, j, k);
   }

   public CharSequence getCharSequence(int i, int j, Charset charset) {
      return this.source.getCharSequence(i, j, charset);
   }

   public ByteBuf setBoolean(int i, boolean flag) {
      return this.source.setBoolean(i, flag);
   }

   public ByteBuf setByte(int i, int j) {
      return this.source.setByte(i, j);
   }

   public ByteBuf setShort(int i, int j) {
      return this.source.setShort(i, j);
   }

   public ByteBuf setShortLE(int i, int j) {
      return this.source.setShortLE(i, j);
   }

   public ByteBuf setMedium(int i, int j) {
      return this.source.setMedium(i, j);
   }

   public ByteBuf setMediumLE(int i, int j) {
      return this.source.setMediumLE(i, j);
   }

   public ByteBuf setInt(int i, int j) {
      return this.source.setInt(i, j);
   }

   public ByteBuf setIntLE(int i, int j) {
      return this.source.setIntLE(i, j);
   }

   public ByteBuf setLong(int i, long j) {
      return this.source.setLong(i, j);
   }

   public ByteBuf setLongLE(int i, long j) {
      return this.source.setLongLE(i, j);
   }

   public ByteBuf setChar(int i, int j) {
      return this.source.setChar(i, j);
   }

   public ByteBuf setFloat(int i, float f) {
      return this.source.setFloat(i, f);
   }

   public ByteBuf setDouble(int i, double d0) {
      return this.source.setDouble(i, d0);
   }

   public ByteBuf setBytes(int i, ByteBuf bytebuf) {
      return this.source.setBytes(i, bytebuf);
   }

   public ByteBuf setBytes(int i, ByteBuf bytebuf, int j) {
      return this.source.setBytes(i, bytebuf, j);
   }

   public ByteBuf setBytes(int i, ByteBuf bytebuf, int j, int k) {
      return this.source.setBytes(i, bytebuf, j, k);
   }

   public ByteBuf setBytes(int i, byte[] abyte) {
      return this.source.setBytes(i, abyte);
   }

   public ByteBuf setBytes(int i, byte[] abyte, int j, int k) {
      return this.source.setBytes(i, abyte, j, k);
   }

   public ByteBuf setBytes(int i, ByteBuffer bytebuffer) {
      return this.source.setBytes(i, bytebuffer);
   }

   public int setBytes(int i, InputStream inputstream, int j) throws IOException {
      return this.source.setBytes(i, inputstream, j);
   }

   public int setBytes(int i, ScatteringByteChannel scatteringbytechannel, int j) throws IOException {
      return this.source.setBytes(i, scatteringbytechannel, j);
   }

   public int setBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
      return this.source.setBytes(i, filechannel, j, k);
   }

   public ByteBuf setZero(int i, int j) {
      return this.source.setZero(i, j);
   }

   public int setCharSequence(int i, CharSequence charsequence, Charset charset) {
      return this.source.setCharSequence(i, charsequence, charset);
   }

   public boolean readBoolean() {
      return this.source.readBoolean();
   }

   public byte readByte() {
      return this.source.readByte();
   }

   public short readUnsignedByte() {
      return this.source.readUnsignedByte();
   }

   public short readShort() {
      return this.source.readShort();
   }

   public short readShortLE() {
      return this.source.readShortLE();
   }

   public int readUnsignedShort() {
      return this.source.readUnsignedShort();
   }

   public int readUnsignedShortLE() {
      return this.source.readUnsignedShortLE();
   }

   public int readMedium() {
      return this.source.readMedium();
   }

   public int readMediumLE() {
      return this.source.readMediumLE();
   }

   public int readUnsignedMedium() {
      return this.source.readUnsignedMedium();
   }

   public int readUnsignedMediumLE() {
      return this.source.readUnsignedMediumLE();
   }

   public int readInt() {
      return this.source.readInt();
   }

   public int readIntLE() {
      return this.source.readIntLE();
   }

   public long readUnsignedInt() {
      return this.source.readUnsignedInt();
   }

   public long readUnsignedIntLE() {
      return this.source.readUnsignedIntLE();
   }

   public long readLong() {
      return this.source.readLong();
   }

   public long readLongLE() {
      return this.source.readLongLE();
   }

   public char readChar() {
      return this.source.readChar();
   }

   public float readFloat() {
      return this.source.readFloat();
   }

   public double readDouble() {
      return this.source.readDouble();
   }

   public ByteBuf readBytes(int i) {
      return this.source.readBytes(i);
   }

   public ByteBuf readSlice(int i) {
      return this.source.readSlice(i);
   }

   public ByteBuf readRetainedSlice(int i) {
      return this.source.readRetainedSlice(i);
   }

   public ByteBuf readBytes(ByteBuf bytebuf) {
      return this.source.readBytes(bytebuf);
   }

   public ByteBuf readBytes(ByteBuf bytebuf, int i) {
      return this.source.readBytes(bytebuf, i);
   }

   public ByteBuf readBytes(ByteBuf bytebuf, int i, int j) {
      return this.source.readBytes(bytebuf, i, j);
   }

   public ByteBuf readBytes(byte[] abyte) {
      return this.source.readBytes(abyte);
   }

   public ByteBuf readBytes(byte[] abyte, int i, int j) {
      return this.source.readBytes(abyte, i, j);
   }

   public ByteBuf readBytes(ByteBuffer bytebuffer) {
      return this.source.readBytes(bytebuffer);
   }

   public ByteBuf readBytes(OutputStream outputstream, int i) throws IOException {
      return this.source.readBytes(outputstream, i);
   }

   public int readBytes(GatheringByteChannel gatheringbytechannel, int i) throws IOException {
      return this.source.readBytes(gatheringbytechannel, i);
   }

   public CharSequence readCharSequence(int i, Charset charset) {
      return this.source.readCharSequence(i, charset);
   }

   public int readBytes(FileChannel filechannel, long i, int j) throws IOException {
      return this.source.readBytes(filechannel, i, j);
   }

   public ByteBuf skipBytes(int i) {
      return this.source.skipBytes(i);
   }

   public ByteBuf writeBoolean(boolean flag) {
      return this.source.writeBoolean(flag);
   }

   public ByteBuf writeByte(int i) {
      return this.source.writeByte(i);
   }

   public ByteBuf writeShort(int i) {
      return this.source.writeShort(i);
   }

   public ByteBuf writeShortLE(int i) {
      return this.source.writeShortLE(i);
   }

   public ByteBuf writeMedium(int i) {
      return this.source.writeMedium(i);
   }

   public ByteBuf writeMediumLE(int i) {
      return this.source.writeMediumLE(i);
   }

   public ByteBuf writeInt(int i) {
      return this.source.writeInt(i);
   }

   public ByteBuf writeIntLE(int i) {
      return this.source.writeIntLE(i);
   }

   public ByteBuf writeLong(long i) {
      return this.source.writeLong(i);
   }

   public ByteBuf writeLongLE(long i) {
      return this.source.writeLongLE(i);
   }

   public ByteBuf writeChar(int i) {
      return this.source.writeChar(i);
   }

   public ByteBuf writeFloat(float f) {
      return this.source.writeFloat(f);
   }

   public ByteBuf writeDouble(double d0) {
      return this.source.writeDouble(d0);
   }

   public ByteBuf writeBytes(ByteBuf bytebuf) {
      return this.source.writeBytes(bytebuf);
   }

   public ByteBuf writeBytes(ByteBuf bytebuf, int i) {
      return this.source.writeBytes(bytebuf, i);
   }

   public ByteBuf writeBytes(ByteBuf bytebuf, int i, int j) {
      return this.source.writeBytes(bytebuf, i, j);
   }

   public ByteBuf writeBytes(byte[] abyte) {
      return this.source.writeBytes(abyte);
   }

   public ByteBuf writeBytes(byte[] abyte, int i, int j) {
      return this.source.writeBytes(abyte, i, j);
   }

   public ByteBuf writeBytes(ByteBuffer bytebuffer) {
      return this.source.writeBytes(bytebuffer);
   }

   public int writeBytes(InputStream inputstream, int i) throws IOException {
      return this.source.writeBytes(inputstream, i);
   }

   public int writeBytes(ScatteringByteChannel scatteringbytechannel, int i) throws IOException {
      return this.source.writeBytes(scatteringbytechannel, i);
   }

   public int writeBytes(FileChannel filechannel, long i, int j) throws IOException {
      return this.source.writeBytes(filechannel, i, j);
   }

   public ByteBuf writeZero(int i) {
      return this.source.writeZero(i);
   }

   public int writeCharSequence(CharSequence charsequence, Charset charset) {
      return this.source.writeCharSequence(charsequence, charset);
   }

   public int indexOf(int i, int j, byte b0) {
      return this.source.indexOf(i, j, b0);
   }

   public int bytesBefore(byte b0) {
      return this.source.bytesBefore(b0);
   }

   public int bytesBefore(int i, byte b0) {
      return this.source.bytesBefore(i, b0);
   }

   public int bytesBefore(int i, int j, byte b0) {
      return this.source.bytesBefore(i, j, b0);
   }

   public int forEachByte(ByteProcessor byteprocessor) {
      return this.source.forEachByte(byteprocessor);
   }

   public int forEachByte(int i, int j, ByteProcessor byteprocessor) {
      return this.source.forEachByte(i, j, byteprocessor);
   }

   public int forEachByteDesc(ByteProcessor byteprocessor) {
      return this.source.forEachByteDesc(byteprocessor);
   }

   public int forEachByteDesc(int i, int j, ByteProcessor byteprocessor) {
      return this.source.forEachByteDesc(i, j, byteprocessor);
   }

   public ByteBuf copy() {
      return this.source.copy();
   }

   public ByteBuf copy(int i, int j) {
      return this.source.copy(i, j);
   }

   public ByteBuf slice() {
      return this.source.slice();
   }

   public ByteBuf retainedSlice() {
      return this.source.retainedSlice();
   }

   public ByteBuf slice(int i, int j) {
      return this.source.slice(i, j);
   }

   public ByteBuf retainedSlice(int i, int j) {
      return this.source.retainedSlice(i, j);
   }

   public ByteBuf duplicate() {
      return this.source.duplicate();
   }

   public ByteBuf retainedDuplicate() {
      return this.source.retainedDuplicate();
   }

   public int nioBufferCount() {
      return this.source.nioBufferCount();
   }

   public ByteBuffer nioBuffer() {
      return this.source.nioBuffer();
   }

   public ByteBuffer nioBuffer(int i, int j) {
      return this.source.nioBuffer(i, j);
   }

   public ByteBuffer internalNioBuffer(int i, int j) {
      return this.source.internalNioBuffer(i, j);
   }

   public ByteBuffer[] nioBuffers() {
      return this.source.nioBuffers();
   }

   public ByteBuffer[] nioBuffers(int i, int j) {
      return this.source.nioBuffers(i, j);
   }

   public boolean hasArray() {
      return this.source.hasArray();
   }

   public byte[] array() {
      return this.source.array();
   }

   public int arrayOffset() {
      return this.source.arrayOffset();
   }

   public boolean hasMemoryAddress() {
      return this.source.hasMemoryAddress();
   }

   public long memoryAddress() {
      return this.source.memoryAddress();
   }

   public String toString(Charset charset) {
      return this.source.toString(charset);
   }

   public String toString(int i, int j, Charset charset) {
      return this.source.toString(i, j, charset);
   }

   public int hashCode() {
      return this.source.hashCode();
   }

   public boolean equals(Object object) {
      return this.source.equals(object);
   }

   public int compareTo(ByteBuf bytebuf) {
      return this.source.compareTo(bytebuf);
   }

   public String toString() {
      return this.source.toString();
   }

   public ByteBuf retain(int i) {
      return this.source.retain(i);
   }

   public ByteBuf retain() {
      return this.source.retain();
   }

   public ByteBuf touch() {
      return this.source.touch();
   }

   public ByteBuf touch(Object object) {
      return this.source.touch(object);
   }

   public int refCnt() {
      return this.source.refCnt();
   }

   public boolean release() {
      return this.source.release();
   }

   public boolean release(int i) {
      return this.source.release(i);
   }

   @FunctionalInterface
   public interface Reader<T> extends Function<FriendlyByteBuf, T> {
      default FriendlyByteBuf.Reader<Optional<T>> asOptional() {
         return (friendlybytebuf) -> friendlybytebuf.readOptional(this);
      }
   }

   @FunctionalInterface
   public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {
      default FriendlyByteBuf.Writer<Optional<T>> asOptional() {
         return (friendlybytebuf, optional) -> friendlybytebuf.writeOptional(optional, this);
      }
   }
}
