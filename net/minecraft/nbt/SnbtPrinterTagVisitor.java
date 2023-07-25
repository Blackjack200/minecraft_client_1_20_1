package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.Util;

public class SnbtPrinterTagVisitor implements TagVisitor {
   private static final Map<String, List<String>> KEY_ORDER = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
      hashmap.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
      hashmap.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
   });
   private static final Set<String> NO_INDENTATION = Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
   private static final String ELEMENT_SEPARATOR = String.valueOf(',');
   private static final String LIST_OPEN = "[";
   private static final String LIST_CLOSE = "]";
   private static final String LIST_TYPE_SEPARATOR = ";";
   private static final String ELEMENT_SPACING = " ";
   private static final String STRUCT_OPEN = "{";
   private static final String STRUCT_CLOSE = "}";
   private static final String NEWLINE = "\n";
   private final String indentation;
   private final int depth;
   private final List<String> path;
   private String result = "";

   public SnbtPrinterTagVisitor() {
      this("    ", 0, Lists.newArrayList());
   }

   public SnbtPrinterTagVisitor(String s, int i, List<String> list) {
      this.indentation = s;
      this.depth = i;
      this.path = list;
   }

   public String visit(Tag tag) {
      tag.accept(this);
      return this.result;
   }

   public void visitString(StringTag stringtag) {
      this.result = StringTag.quoteAndEscape(stringtag.getAsString());
   }

   public void visitByte(ByteTag bytetag) {
      this.result = bytetag.getAsNumber() + "b";
   }

   public void visitShort(ShortTag shorttag) {
      this.result = shorttag.getAsNumber() + "s";
   }

   public void visitInt(IntTag inttag) {
      this.result = String.valueOf((Object)inttag.getAsNumber());
   }

   public void visitLong(LongTag longtag) {
      this.result = longtag.getAsNumber() + "L";
   }

   public void visitFloat(FloatTag floattag) {
      this.result = floattag.getAsFloat() + "f";
   }

   public void visitDouble(DoubleTag doubletag) {
      this.result = doubletag.getAsDouble() + "d";
   }

   public void visitByteArray(ByteArrayTag bytearraytag) {
      StringBuilder stringbuilder = (new StringBuilder("[")).append("B").append(";");
      byte[] abyte = bytearraytag.getAsByteArray();

      for(int i = 0; i < abyte.length; ++i) {
         stringbuilder.append(" ").append((int)abyte[i]).append("B");
         if (i != abyte.length - 1) {
            stringbuilder.append(ELEMENT_SEPARATOR);
         }
      }

      stringbuilder.append("]");
      this.result = stringbuilder.toString();
   }

   public void visitIntArray(IntArrayTag intarraytag) {
      StringBuilder stringbuilder = (new StringBuilder("[")).append("I").append(";");
      int[] aint = intarraytag.getAsIntArray();

      for(int i = 0; i < aint.length; ++i) {
         stringbuilder.append(" ").append(aint[i]);
         if (i != aint.length - 1) {
            stringbuilder.append(ELEMENT_SEPARATOR);
         }
      }

      stringbuilder.append("]");
      this.result = stringbuilder.toString();
   }

   public void visitLongArray(LongArrayTag longarraytag) {
      String s = "L";
      StringBuilder stringbuilder = (new StringBuilder("[")).append("L").append(";");
      long[] along = longarraytag.getAsLongArray();

      for(int i = 0; i < along.length; ++i) {
         stringbuilder.append(" ").append(along[i]).append("L");
         if (i != along.length - 1) {
            stringbuilder.append(ELEMENT_SEPARATOR);
         }
      }

      stringbuilder.append("]");
      this.result = stringbuilder.toString();
   }

   public void visitList(ListTag listtag) {
      if (listtag.isEmpty()) {
         this.result = "[]";
      } else {
         StringBuilder stringbuilder = new StringBuilder("[");
         this.pushPath("[]");
         String s = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
         if (!s.isEmpty()) {
            stringbuilder.append("\n");
         }

         for(int i = 0; i < listtag.size(); ++i) {
            stringbuilder.append(Strings.repeat(s, this.depth + 1));
            stringbuilder.append((new SnbtPrinterTagVisitor(s, this.depth + 1, this.path)).visit(listtag.get(i)));
            if (i != listtag.size() - 1) {
               stringbuilder.append(ELEMENT_SEPARATOR).append(s.isEmpty() ? " " : "\n");
            }
         }

         if (!s.isEmpty()) {
            stringbuilder.append("\n").append(Strings.repeat(s, this.depth));
         }

         stringbuilder.append("]");
         this.result = stringbuilder.toString();
         this.popPath();
      }
   }

   public void visitCompound(CompoundTag compoundtag) {
      if (compoundtag.isEmpty()) {
         this.result = "{}";
      } else {
         StringBuilder stringbuilder = new StringBuilder("{");
         this.pushPath("{}");
         String s = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
         if (!s.isEmpty()) {
            stringbuilder.append("\n");
         }

         Collection<String> collection = this.getKeys(compoundtag);
         Iterator<String> iterator = collection.iterator();

         while(iterator.hasNext()) {
            String s1 = iterator.next();
            Tag tag = compoundtag.get(s1);
            this.pushPath(s1);
            stringbuilder.append(Strings.repeat(s, this.depth + 1)).append(handleEscapePretty(s1)).append(NAME_VALUE_SEPARATOR).append(" ").append((new SnbtPrinterTagVisitor(s, this.depth + 1, this.path)).visit(tag));
            this.popPath();
            if (iterator.hasNext()) {
               stringbuilder.append(ELEMENT_SEPARATOR).append(s.isEmpty() ? " " : "\n");
            }
         }

         if (!s.isEmpty()) {
            stringbuilder.append("\n").append(Strings.repeat(s, this.depth));
         }

         stringbuilder.append("}");
         this.result = stringbuilder.toString();
         this.popPath();
      }
   }

   private void popPath() {
      this.path.remove(this.path.size() - 1);
   }

   private void pushPath(String s) {
      this.path.add(s);
   }

   protected List<String> getKeys(CompoundTag compoundtag) {
      Set<String> set = Sets.newHashSet(compoundtag.getAllKeys());
      List<String> list = Lists.newArrayList();
      List<String> list1 = KEY_ORDER.get(this.pathString());
      if (list1 != null) {
         for(String s : list1) {
            if (set.remove(s)) {
               list.add(s);
            }
         }

         if (!set.isEmpty()) {
            set.stream().sorted().forEach(list::add);
         }
      } else {
         list.addAll(set);
         Collections.sort(list);
      }

      return list;
   }

   public String pathString() {
      return String.join(".", this.path);
   }

   protected static String handleEscapePretty(String s) {
      return SIMPLE_VALUE.matcher(s).matches() ? s : StringTag.quoteAndEscape(s);
   }

   public void visitEnd(EndTag endtag) {
   }
}
