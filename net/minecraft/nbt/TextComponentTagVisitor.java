package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor implements TagVisitor {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int INLINE_LIST_THRESHOLD = 8;
   private static final ByteCollection INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
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
   private Component result = CommonComponents.EMPTY;

   public TextComponentTagVisitor(String s, int i) {
      this.indentation = s;
      this.depth = i;
   }

   public Component visit(Tag tag) {
      tag.accept(this);
      return this.result;
   }

   public void visitString(StringTag stringtag) {
      String s = StringTag.quoteAndEscape(stringtag.getAsString());
      String s1 = s.substring(0, 1);
      Component component = Component.literal(s.substring(1, s.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
      this.result = Component.literal(s1).append(component).append(s1);
   }

   public void visitByte(ByteTag bytetag) {
      Component component = Component.literal("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      this.result = Component.literal(String.valueOf((Object)bytetag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public void visitShort(ShortTag shorttag) {
      Component component = Component.literal("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      this.result = Component.literal(String.valueOf((Object)shorttag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public void visitInt(IntTag inttag) {
      this.result = Component.literal(String.valueOf((Object)inttag.getAsNumber())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public void visitLong(LongTag longtag) {
      Component component = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      this.result = Component.literal(String.valueOf((Object)longtag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public void visitFloat(FloatTag floattag) {
      Component component = Component.literal("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      this.result = Component.literal(String.valueOf(floattag.getAsFloat())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public void visitDouble(DoubleTag doubletag) {
      Component component = Component.literal("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      this.result = Component.literal(String.valueOf(doubletag.getAsDouble())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public void visitByteArray(ByteArrayTag bytearraytag) {
      Component component = Component.literal("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      MutableComponent mutablecomponent = Component.literal("[").append(component).append(";");
      byte[] abyte = bytearraytag.getAsByteArray();

      for(int i = 0; i < abyte.length; ++i) {
         MutableComponent mutablecomponent1 = Component.literal(String.valueOf((int)abyte[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
         mutablecomponent.append(" ").append(mutablecomponent1).append(component);
         if (i != abyte.length - 1) {
            mutablecomponent.append(ELEMENT_SEPARATOR);
         }
      }

      mutablecomponent.append("]");
      this.result = mutablecomponent;
   }

   public void visitIntArray(IntArrayTag intarraytag) {
      Component component = Component.literal("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      MutableComponent mutablecomponent = Component.literal("[").append(component).append(";");
      int[] aint = intarraytag.getAsIntArray();

      for(int i = 0; i < aint.length; ++i) {
         mutablecomponent.append(" ").append(Component.literal(String.valueOf(aint[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
         if (i != aint.length - 1) {
            mutablecomponent.append(ELEMENT_SEPARATOR);
         }
      }

      mutablecomponent.append("]");
      this.result = mutablecomponent;
   }

   public void visitLongArray(LongArrayTag longarraytag) {
      Component component = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      MutableComponent mutablecomponent = Component.literal("[").append(component).append(";");
      long[] along = longarraytag.getAsLongArray();

      for(int i = 0; i < along.length; ++i) {
         Component component1 = Component.literal(String.valueOf(along[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
         mutablecomponent.append(" ").append(component1).append(component);
         if (i != along.length - 1) {
            mutablecomponent.append(ELEMENT_SEPARATOR);
         }
      }

      mutablecomponent.append("]");
      this.result = mutablecomponent;
   }

   public void visitList(ListTag listtag) {
      if (listtag.isEmpty()) {
         this.result = Component.literal("[]");
      } else if (INLINE_ELEMENT_TYPES.contains(listtag.getElementType()) && listtag.size() <= 8) {
         String s = ELEMENT_SEPARATOR + " ";
         MutableComponent mutablecomponent = Component.literal("[");

         for(int i = 0; i < listtag.size(); ++i) {
            if (i != 0) {
               mutablecomponent.append(s);
            }

            mutablecomponent.append((new TextComponentTagVisitor(this.indentation, this.depth)).visit(listtag.get(i)));
         }

         mutablecomponent.append("]");
         this.result = mutablecomponent;
      } else {
         MutableComponent mutablecomponent1 = Component.literal("[");
         if (!this.indentation.isEmpty()) {
            mutablecomponent1.append("\n");
         }

         for(int j = 0; j < listtag.size(); ++j) {
            MutableComponent mutablecomponent2 = Component.literal(Strings.repeat(this.indentation, this.depth + 1));
            mutablecomponent2.append((new TextComponentTagVisitor(this.indentation, this.depth + 1)).visit(listtag.get(j)));
            if (j != listtag.size() - 1) {
               mutablecomponent2.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? " " : "\n");
            }

            mutablecomponent1.append(mutablecomponent2);
         }

         if (!this.indentation.isEmpty()) {
            mutablecomponent1.append("\n").append(Strings.repeat(this.indentation, this.depth));
         }

         mutablecomponent1.append("]");
         this.result = mutablecomponent1;
      }
   }

   public void visitCompound(CompoundTag compoundtag) {
      if (compoundtag.isEmpty()) {
         this.result = Component.literal("{}");
      } else {
         MutableComponent mutablecomponent = Component.literal("{");
         Collection<String> collection = compoundtag.getAllKeys();
         if (LOGGER.isDebugEnabled()) {
            List<String> list = Lists.newArrayList(compoundtag.getAllKeys());
            Collections.sort(list);
            collection = list;
         }

         if (!this.indentation.isEmpty()) {
            mutablecomponent.append("\n");
         }

         MutableComponent mutablecomponent1;
         for(Iterator<String> iterator = collection.iterator(); iterator.hasNext(); mutablecomponent.append(mutablecomponent1)) {
            String s = iterator.next();
            mutablecomponent1 = Component.literal(Strings.repeat(this.indentation, this.depth + 1)).append(handleEscapePretty(s)).append(NAME_VALUE_SEPARATOR).append(" ").append((new TextComponentTagVisitor(this.indentation, this.depth + 1)).visit(compoundtag.get(s)));
            if (iterator.hasNext()) {
               mutablecomponent1.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? " " : "\n");
            }
         }

         if (!this.indentation.isEmpty()) {
            mutablecomponent.append("\n").append(Strings.repeat(this.indentation, this.depth));
         }

         mutablecomponent.append("}");
         this.result = mutablecomponent;
      }
   }

   protected static Component handleEscapePretty(String s) {
      if (SIMPLE_VALUE.matcher(s).matches()) {
         return Component.literal(s).withStyle(SYNTAX_HIGHLIGHTING_KEY);
      } else {
         String s1 = StringTag.quoteAndEscape(s);
         String s2 = s1.substring(0, 1);
         Component component = Component.literal(s1.substring(1, s1.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
         return Component.literal(s2).append(component).append(s2);
      }
   }

   public void visitEnd(EndTag endtag) {
      this.result = CommonComponents.EMPTY;
   }
}
