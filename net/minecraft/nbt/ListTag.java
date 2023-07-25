package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
   private static final int SELF_SIZE_IN_BYTES = 37;
   public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>() {
      public ListTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(37L);
         if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            byte b0 = datainput.readByte();
            int j = datainput.readInt();
            if (b0 == 0 && j > 0) {
               throw new RuntimeException("Missing type on ListTag");
            } else {
               nbtaccounter.accountBytes(4L * (long)j);
               TagType<?> tagtype = TagTypes.getType(b0);
               List<Tag> list = Lists.newArrayListWithCapacity(j);

               for(int k = 0; k < j; ++k) {
                  list.add(tagtype.load(datainput, i + 1, nbtaccounter));
               }

               return new ListTag(list, b0);
            }
         }
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         TagType<?> tagtype = TagTypes.getType(datainput.readByte());
         int i = datainput.readInt();
         switch (streamtagvisitor.visitList(tagtype, i)) {
            case HALT:
               return StreamTagVisitor.ValueResult.HALT;
            case BREAK:
               tagtype.skip(datainput, i);
               return streamtagvisitor.visitContainerEnd();
            default:
               int j = 0;

               while(true) {
                  label45: {
                     if (j < i) {
                        switch (streamtagvisitor.visitElement(tagtype, j)) {
                           case HALT:
                              return StreamTagVisitor.ValueResult.HALT;
                           case BREAK:
                              tagtype.skip(datainput);
                              break;
                           case SKIP:
                              tagtype.skip(datainput);
                              break label45;
                           default:
                              switch (tagtype.parse(datainput, streamtagvisitor)) {
                                 case HALT:
                                    return StreamTagVisitor.ValueResult.HALT;
                                 case BREAK:
                                    break;
                                 default:
                                    break label45;
                              }
                        }
                     }

                     int k = i - 1 - j;
                     if (k > 0) {
                        tagtype.skip(datainput, k);
                     }

                     return streamtagvisitor.visitContainerEnd();
                  }

                  ++j;
               }
         }
      }

      public void skip(DataInput datainput) throws IOException {
         TagType<?> tagtype = TagTypes.getType(datainput.readByte());
         int i = datainput.readInt();
         tagtype.skip(datainput, i);
      }

      public String getName() {
         return "LIST";
      }

      public String getPrettyName() {
         return "TAG_List";
      }
   };
   private final List<Tag> list;
   private byte type;

   ListTag(List<Tag> list, byte b0) {
      this.list = list;
      this.type = b0;
   }

   public ListTag() {
      this(Lists.newArrayList(), (byte)0);
   }

   public void write(DataOutput dataoutput) throws IOException {
      if (this.list.isEmpty()) {
         this.type = 0;
      } else {
         this.type = this.list.get(0).getId();
      }

      dataoutput.writeByte(this.type);
      dataoutput.writeInt(this.list.size());

      for(Tag tag : this.list) {
         tag.write(dataoutput);
      }

   }

   public int sizeInBytes() {
      int i = 37;
      i += 4 * this.list.size();

      for(Tag tag : this.list) {
         i += tag.sizeInBytes();
      }

      return i;
   }

   public byte getId() {
      return 9;
   }

   public TagType<ListTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   private void updateTypeAfterRemove() {
      if (this.list.isEmpty()) {
         this.type = 0;
      }

   }

   public Tag remove(int i) {
      Tag tag = this.list.remove(i);
      this.updateTypeAfterRemove();
      return tag;
   }

   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public CompoundTag getCompound(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 10) {
            return (CompoundTag)tag;
         }
      }

      return new CompoundTag();
   }

   public ListTag getList(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 9) {
            return (ListTag)tag;
         }
      }

      return new ListTag();
   }

   public short getShort(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 2) {
            return ((ShortTag)tag).getAsShort();
         }
      }

      return 0;
   }

   public int getInt(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 3) {
            return ((IntTag)tag).getAsInt();
         }
      }

      return 0;
   }

   public int[] getIntArray(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 11) {
            return ((IntArrayTag)tag).getAsIntArray();
         }
      }

      return new int[0];
   }

   public long[] getLongArray(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 12) {
            return ((LongArrayTag)tag).getAsLongArray();
         }
      }

      return new long[0];
   }

   public double getDouble(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 6) {
            return ((DoubleTag)tag).getAsDouble();
         }
      }

      return 0.0D;
   }

   public float getFloat(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         if (tag.getId() == 5) {
            return ((FloatTag)tag).getAsFloat();
         }
      }

      return 0.0F;
   }

   public String getString(int i) {
      if (i >= 0 && i < this.list.size()) {
         Tag tag = this.list.get(i);
         return tag.getId() == 8 ? tag.getAsString() : tag.toString();
      } else {
         return "";
      }
   }

   public int size() {
      return this.list.size();
   }

   public Tag get(int i) {
      return this.list.get(i);
   }

   public Tag set(int i, Tag tag) {
      Tag tag1 = this.get(i);
      if (!this.setTag(i, tag)) {
         throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", tag.getId(), this.type));
      } else {
         return tag1;
      }
   }

   public void add(int i, Tag tag) {
      if (!this.addTag(i, tag)) {
         throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", tag.getId(), this.type));
      }
   }

   public boolean setTag(int i, Tag tag) {
      if (this.updateType(tag)) {
         this.list.set(i, tag);
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int i, Tag tag) {
      if (this.updateType(tag)) {
         this.list.add(i, tag);
         return true;
      } else {
         return false;
      }
   }

   private boolean updateType(Tag tag) {
      if (tag.getId() == 0) {
         return false;
      } else if (this.type == 0) {
         this.type = tag.getId();
         return true;
      } else {
         return this.type == tag.getId();
      }
   }

   public ListTag copy() {
      Iterable<Tag> iterable = (Iterable<Tag>)(TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy));
      List<Tag> list = Lists.newArrayList(iterable);
      return new ListTag(list, this.type);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof ListTag && Objects.equals(this.list, ((ListTag)object).list);
      }
   }

   public int hashCode() {
      return this.list.hashCode();
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitList(this);
   }

   public byte getElementType() {
      return this.type;
   }

   public void clear() {
      this.list.clear();
      this.type = 0;
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      switch (streamtagvisitor.visitList(TagTypes.getType(this.type), this.list.size())) {
         case HALT:
            return StreamTagVisitor.ValueResult.HALT;
         case BREAK:
            return streamtagvisitor.visitContainerEnd();
         default:
            int i = 0;

            while(i < this.list.size()) {
               Tag tag = this.list.get(i);
               switch (streamtagvisitor.visitElement(tag.getType(), i)) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     return streamtagvisitor.visitContainerEnd();
                  default:
                     switch (tag.accept(streamtagvisitor)) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           return streamtagvisitor.visitContainerEnd();
                     }
                  case SKIP:
                     ++i;
               }
            }

            return streamtagvisitor.visitContainerEnd();
      }
   }
}
