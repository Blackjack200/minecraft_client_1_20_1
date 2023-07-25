package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;

public class CollectToTag implements StreamTagVisitor {
   private String lastId = "";
   @Nullable
   private Tag rootTag;
   private final Deque<Consumer<Tag>> consumerStack = new ArrayDeque<>();

   @Nullable
   public Tag getResult() {
      return this.rootTag;
   }

   protected int depth() {
      return this.consumerStack.size();
   }

   private void appendEntry(Tag tag) {
      this.consumerStack.getLast().accept(tag);
   }

   public StreamTagVisitor.ValueResult visitEnd() {
      this.appendEntry(EndTag.INSTANCE);
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(String s) {
      this.appendEntry(StringTag.valueOf(s));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(byte b0) {
      this.appendEntry(ByteTag.valueOf(b0));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(short short0) {
      this.appendEntry(ShortTag.valueOf(short0));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(int i) {
      this.appendEntry(IntTag.valueOf(i));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(long i) {
      this.appendEntry(LongTag.valueOf(i));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(float f) {
      this.appendEntry(FloatTag.valueOf(f));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(double d0) {
      this.appendEntry(DoubleTag.valueOf(d0));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(byte[] abyte) {
      this.appendEntry(new ByteArrayTag(abyte));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(int[] aint) {
      this.appendEntry(new IntArrayTag(aint));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(long[] along) {
      this.appendEntry(new LongArrayTag(along));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visitList(TagType<?> tagtype, int i) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.EntryResult visitElement(TagType<?> tagtype, int i) {
      this.enterContainerIfNeeded(tagtype);
      return StreamTagVisitor.EntryResult.ENTER;
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype) {
      return StreamTagVisitor.EntryResult.ENTER;
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype, String s) {
      this.lastId = s;
      this.enterContainerIfNeeded(tagtype);
      return StreamTagVisitor.EntryResult.ENTER;
   }

   private void enterContainerIfNeeded(TagType<?> tagtype) {
      if (tagtype == ListTag.TYPE) {
         ListTag listtag = new ListTag();
         this.appendEntry(listtag);
         this.consumerStack.addLast(listtag::add);
      } else if (tagtype == CompoundTag.TYPE) {
         CompoundTag compoundtag = new CompoundTag();
         this.appendEntry(compoundtag);
         this.consumerStack.addLast((tag) -> compoundtag.put(this.lastId, tag));
      }

   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      this.consumerStack.removeLast();
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagtype) {
      if (tagtype == ListTag.TYPE) {
         ListTag listtag = new ListTag();
         this.rootTag = listtag;
         this.consumerStack.addLast(listtag::add);
      } else if (tagtype == CompoundTag.TYPE) {
         CompoundTag compoundtag = new CompoundTag();
         this.rootTag = compoundtag;
         this.consumerStack.addLast((tag1) -> compoundtag.put(this.lastId, tag1));
      } else {
         this.consumerStack.addLast((tag) -> this.rootTag = tag);
      }

      return StreamTagVisitor.ValueResult.CONTINUE;
   }
}
