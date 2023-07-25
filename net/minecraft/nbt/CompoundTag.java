package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class CompoundTag implements Tag {
   public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
      Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
      return tag instanceof CompoundTag ? DataResult.success((CompoundTag)tag) : DataResult.error(() -> "Not a compound tag: " + tag);
   }, (compoundtag) -> new Dynamic<>(NbtOps.INSTANCE, compoundtag));
   private static final int SELF_SIZE_IN_BYTES = 48;
   private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
   public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>() {
      public CompoundTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(48L);
         if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            Map<String, Tag> map = Maps.newHashMap();

            byte b0;
            while((b0 = CompoundTag.readNamedTagType(datainput, nbtaccounter)) != 0) {
               String s = CompoundTag.readNamedTagName(datainput, nbtaccounter);
               nbtaccounter.accountBytes((long)(28 + 2 * s.length()));
               Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b0), s, datainput, i + 1, nbtaccounter);
               if (map.put(s, tag) == null) {
                  nbtaccounter.accountBytes(36L);
               }
            }

            return new CompoundTag(map);
         }
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         while(true) {
            byte b0;
            if ((b0 = datainput.readByte()) != 0) {
               TagType<?> tagtype = TagTypes.getType(b0);
               switch (streamtagvisitor.visitEntry(tagtype)) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     StringTag.skipString(datainput);
                     tagtype.skip(datainput);
                     break;
                  case SKIP:
                     StringTag.skipString(datainput);
                     tagtype.skip(datainput);
                     continue;
                  default:
                     String s = datainput.readUTF();
                     switch (streamtagvisitor.visitEntry(tagtype, s)) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           tagtype.skip(datainput);
                           break;
                        case SKIP:
                           tagtype.skip(datainput);
                           continue;
                        default:
                           switch (tagtype.parse(datainput, streamtagvisitor)) {
                              case HALT:
                                 return StreamTagVisitor.ValueResult.HALT;
                              case BREAK:
                              default:
                                 continue;
                           }
                     }
               }
            }

            if (b0 != 0) {
               while((b0 = datainput.readByte()) != 0) {
                  StringTag.skipString(datainput);
                  TagTypes.getType(b0).skip(datainput);
               }
            }

            return streamtagvisitor.visitContainerEnd();
         }
      }

      public void skip(DataInput datainput) throws IOException {
         byte b0;
         while((b0 = datainput.readByte()) != 0) {
            StringTag.skipString(datainput);
            TagTypes.getType(b0).skip(datainput);
         }

      }

      public String getName() {
         return "COMPOUND";
      }

      public String getPrettyName() {
         return "TAG_Compound";
      }
   };
   private final Map<String, Tag> tags;

   protected CompoundTag(Map<String, Tag> map) {
      this.tags = map;
   }

   public CompoundTag() {
      this(Maps.newHashMap());
   }

   public void write(DataOutput dataoutput) throws IOException {
      for(String s : this.tags.keySet()) {
         Tag tag = this.tags.get(s);
         writeNamedTag(s, tag, dataoutput);
      }

      dataoutput.writeByte(0);
   }

   public int sizeInBytes() {
      int i = 48;

      for(Map.Entry<String, Tag> map_entry : this.tags.entrySet()) {
         i += 28 + 2 * map_entry.getKey().length();
         i += 36;
         i += map_entry.getValue().sizeInBytes();
      }

      return i;
   }

   public Set<String> getAllKeys() {
      return this.tags.keySet();
   }

   public byte getId() {
      return 10;
   }

   public TagType<CompoundTag> getType() {
      return TYPE;
   }

   public int size() {
      return this.tags.size();
   }

   @Nullable
   public Tag put(String s, Tag tag) {
      return this.tags.put(s, tag);
   }

   public void putByte(String s, byte b0) {
      this.tags.put(s, ByteTag.valueOf(b0));
   }

   public void putShort(String s, short short0) {
      this.tags.put(s, ShortTag.valueOf(short0));
   }

   public void putInt(String s, int i) {
      this.tags.put(s, IntTag.valueOf(i));
   }

   public void putLong(String s, long i) {
      this.tags.put(s, LongTag.valueOf(i));
   }

   public void putUUID(String s, UUID uuid) {
      this.tags.put(s, NbtUtils.createUUID(uuid));
   }

   public UUID getUUID(String s) {
      return NbtUtils.loadUUID(this.get(s));
   }

   public boolean hasUUID(String s) {
      Tag tag = this.get(s);
      return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag)tag).getAsIntArray().length == 4;
   }

   public void putFloat(String s, float f) {
      this.tags.put(s, FloatTag.valueOf(f));
   }

   public void putDouble(String s, double d0) {
      this.tags.put(s, DoubleTag.valueOf(d0));
   }

   public void putString(String s, String s1) {
      this.tags.put(s, StringTag.valueOf(s1));
   }

   public void putByteArray(String s, byte[] abyte) {
      this.tags.put(s, new ByteArrayTag(abyte));
   }

   public void putByteArray(String s, List<Byte> list) {
      this.tags.put(s, new ByteArrayTag(list));
   }

   public void putIntArray(String s, int[] aint) {
      this.tags.put(s, new IntArrayTag(aint));
   }

   public void putIntArray(String s, List<Integer> list) {
      this.tags.put(s, new IntArrayTag(list));
   }

   public void putLongArray(String s, long[] along) {
      this.tags.put(s, new LongArrayTag(along));
   }

   public void putLongArray(String s, List<Long> list) {
      this.tags.put(s, new LongArrayTag(list));
   }

   public void putBoolean(String s, boolean flag) {
      this.tags.put(s, ByteTag.valueOf(flag));
   }

   @Nullable
   public Tag get(String s) {
      return this.tags.get(s);
   }

   public byte getTagType(String s) {
      Tag tag = this.tags.get(s);
      return tag == null ? 0 : tag.getId();
   }

   public boolean contains(String s) {
      return this.tags.containsKey(s);
   }

   public boolean contains(String s, int i) {
      int j = this.getTagType(s);
      if (j == i) {
         return true;
      } else if (i != 99) {
         return false;
      } else {
         return j == 1 || j == 2 || j == 3 || j == 4 || j == 5 || j == 6;
      }
   }

   public byte getByte(String s) {
      try {
         if (this.contains(s, 99)) {
            return ((NumericTag)this.tags.get(s)).getAsByte();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public short getShort(String s) {
      try {
         if (this.contains(s, 99)) {
            return ((NumericTag)this.tags.get(s)).getAsShort();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public int getInt(String s) {
      try {
         if (this.contains(s, 99)) {
            return ((NumericTag)this.tags.get(s)).getAsInt();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public long getLong(String s) {
      try {
         if (this.contains(s, 99)) {
            return ((NumericTag)this.tags.get(s)).getAsLong();
         }
      } catch (ClassCastException var3) {
      }

      return 0L;
   }

   public float getFloat(String s) {
      try {
         if (this.contains(s, 99)) {
            return ((NumericTag)this.tags.get(s)).getAsFloat();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0F;
   }

   public double getDouble(String s) {
      try {
         if (this.contains(s, 99)) {
            return ((NumericTag)this.tags.get(s)).getAsDouble();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0D;
   }

   public String getString(String s) {
      try {
         if (this.contains(s, 8)) {
            return this.tags.get(s).getAsString();
         }
      } catch (ClassCastException var3) {
      }

      return "";
   }

   public byte[] getByteArray(String s) {
      try {
         if (this.contains(s, 7)) {
            return ((ByteArrayTag)this.tags.get(s)).getAsByteArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(s, ByteArrayTag.TYPE, var3));
      }

      return new byte[0];
   }

   public int[] getIntArray(String s) {
      try {
         if (this.contains(s, 11)) {
            return ((IntArrayTag)this.tags.get(s)).getAsIntArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(s, IntArrayTag.TYPE, var3));
      }

      return new int[0];
   }

   public long[] getLongArray(String s) {
      try {
         if (this.contains(s, 12)) {
            return ((LongArrayTag)this.tags.get(s)).getAsLongArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(s, LongArrayTag.TYPE, var3));
      }

      return new long[0];
   }

   public CompoundTag getCompound(String s) {
      try {
         if (this.contains(s, 10)) {
            return (CompoundTag)this.tags.get(s);
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(s, TYPE, var3));
      }

      return new CompoundTag();
   }

   public ListTag getList(String s, int i) {
      try {
         if (this.getTagType(s) == 9) {
            ListTag listtag = (ListTag)this.tags.get(s);
            if (!listtag.isEmpty() && listtag.getElementType() != i) {
               return new ListTag();
            }

            return listtag;
         }
      } catch (ClassCastException var4) {
         throw new ReportedException(this.createReport(s, ListTag.TYPE, var4));
      }

      return new ListTag();
   }

   public boolean getBoolean(String s) {
      return this.getByte(s) != 0;
   }

   public void remove(String s) {
      this.tags.remove(s);
   }

   public String toString() {
      return this.getAsString();
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   private CrashReport createReport(String s, TagType<?> tagtype, ClassCastException classcastexception) {
      CrashReport crashreport = CrashReport.forThrowable(classcastexception, "Reading NBT data");
      CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
      crashreportcategory.setDetail("Tag type found", () -> this.tags.get(s).getType().getName());
      crashreportcategory.setDetail("Tag type expected", tagtype::getName);
      crashreportcategory.setDetail("Tag name", s);
      return crashreport;
   }

   public CompoundTag copy() {
      Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
      return new CompoundTag(map);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)object).tags);
      }
   }

   public int hashCode() {
      return this.tags.hashCode();
   }

   private static void writeNamedTag(String s, Tag tag, DataOutput dataoutput) throws IOException {
      dataoutput.writeByte(tag.getId());
      if (tag.getId() != 0) {
         dataoutput.writeUTF(s);
         tag.write(dataoutput);
      }
   }

   static byte readNamedTagType(DataInput datainput, NbtAccounter nbtaccounter) throws IOException {
      return datainput.readByte();
   }

   static String readNamedTagName(DataInput datainput, NbtAccounter nbtaccounter) throws IOException {
      return datainput.readUTF();
   }

   static Tag readNamedTagData(TagType<?> tagtype, String s, DataInput datainput, int i, NbtAccounter nbtaccounter) {
      try {
         return tagtype.load(datainput, i, nbtaccounter);
      } catch (IOException var8) {
         CrashReport crashreport = CrashReport.forThrowable(var8, "Loading NBT data");
         CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
         crashreportcategory.setDetail("Tag name", s);
         crashreportcategory.setDetail("Tag type", tagtype.getName());
         throw new ReportedException(crashreport);
      }
   }

   public CompoundTag merge(CompoundTag compoundtag) {
      for(String s : compoundtag.tags.keySet()) {
         Tag tag = compoundtag.tags.get(s);
         if (tag.getId() == 10) {
            if (this.contains(s, 10)) {
               CompoundTag compoundtag1 = this.getCompound(s);
               compoundtag1.merge((CompoundTag)tag);
            } else {
               this.put(s, tag.copy());
            }
         } else {
            this.put(s, tag.copy());
         }
      }

      return this;
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitCompound(this);
   }

   protected Map<String, Tag> entries() {
      return Collections.unmodifiableMap(this.tags);
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      for(Map.Entry<String, Tag> map_entry : this.tags.entrySet()) {
         Tag tag = map_entry.getValue();
         TagType<?> tagtype = tag.getType();
         StreamTagVisitor.EntryResult streamtagvisitor_entryresult = streamtagvisitor.visitEntry(tagtype);
         switch (streamtagvisitor_entryresult) {
            case HALT:
               return StreamTagVisitor.ValueResult.HALT;
            case BREAK:
               return streamtagvisitor.visitContainerEnd();
            case SKIP:
               break;
            default:
               streamtagvisitor_entryresult = streamtagvisitor.visitEntry(tagtype, map_entry.getKey());
               switch (streamtagvisitor_entryresult) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     return streamtagvisitor.visitContainerEnd();
                  case SKIP:
                     break;
                  default:
                     StreamTagVisitor.ValueResult streamtagvisitor_valueresult = tag.accept(streamtagvisitor);
                     switch (streamtagvisitor_valueresult) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           return streamtagvisitor.visitContainerEnd();
                     }
               }
         }
      }

      return streamtagvisitor.visitContainerEnd();
   }
}
