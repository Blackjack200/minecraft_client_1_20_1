package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;

public class FilterMask {
   public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(FilterMask.Type::values).dispatch(FilterMask::type, FilterMask.Type::codec);
   public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.Type.FULLY_FILTERED);
   public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.Type.PASS_THROUGH);
   public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.filtered")));
   static final Codec<FilterMask> PASS_THROUGH_CODEC = Codec.unit(PASS_THROUGH);
   static final Codec<FilterMask> FULLY_FILTERED_CODEC = Codec.unit(FULLY_FILTERED);
   static final Codec<FilterMask> PARTIALLY_FILTERED_CODEC = ExtraCodecs.BIT_SET.xmap(FilterMask::new, FilterMask::mask);
   private static final char HASH = '#';
   private final BitSet mask;
   private final FilterMask.Type type;

   private FilterMask(BitSet bitset, FilterMask.Type filtermask_type) {
      this.mask = bitset;
      this.type = filtermask_type;
   }

   private FilterMask(BitSet bitset) {
      this.mask = bitset;
      this.type = FilterMask.Type.PARTIALLY_FILTERED;
   }

   public FilterMask(int i) {
      this(new BitSet(i), FilterMask.Type.PARTIALLY_FILTERED);
   }

   private FilterMask.Type type() {
      return this.type;
   }

   private BitSet mask() {
      return this.mask;
   }

   public static FilterMask read(FriendlyByteBuf friendlybytebuf) {
      FilterMask.Type filtermask_type = friendlybytebuf.readEnum(FilterMask.Type.class);
      FilterMask var10000;
      switch (filtermask_type) {
         case PASS_THROUGH:
            var10000 = PASS_THROUGH;
            break;
         case FULLY_FILTERED:
            var10000 = FULLY_FILTERED;
            break;
         case PARTIALLY_FILTERED:
            var10000 = new FilterMask(friendlybytebuf.readBitSet(), FilterMask.Type.PARTIALLY_FILTERED);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static void write(FriendlyByteBuf friendlybytebuf, FilterMask filtermask) {
      friendlybytebuf.writeEnum(filtermask.type);
      if (filtermask.type == FilterMask.Type.PARTIALLY_FILTERED) {
         friendlybytebuf.writeBitSet(filtermask.mask);
      }

   }

   public void setFiltered(int i) {
      this.mask.set(i);
   }

   @Nullable
   public String apply(String s) {
      String var10000;
      switch (this.type) {
         case PASS_THROUGH:
            var10000 = s;
            break;
         case FULLY_FILTERED:
            var10000 = null;
            break;
         case PARTIALLY_FILTERED:
            char[] achar = s.toCharArray();

            for(int i = 0; i < achar.length && i < this.mask.length(); ++i) {
               if (this.mask.get(i)) {
                  achar[i] = '#';
               }
            }

            var10000 = new String(achar);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   @Nullable
   public Component applyWithFormatting(String s) {
      MutableComponent var10000;
      switch (this.type) {
         case PASS_THROUGH:
            var10000 = Component.literal(s);
            break;
         case FULLY_FILTERED:
            var10000 = null;
            break;
         case PARTIALLY_FILTERED:
            MutableComponent mutablecomponent = Component.empty();
            int i = 0;
            boolean flag = this.mask.get(0);

            while(true) {
               int j = flag ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
               j = j < 0 ? s.length() : j;
               if (j == i) {
                  return mutablecomponent;
               }

               if (flag) {
                  mutablecomponent.append(Component.literal(StringUtils.repeat('#', j - i)).withStyle(FILTERED_STYLE));
               } else {
                  mutablecomponent.append(s.substring(i, j));
               }

               flag = !flag;
               i = j;
            }
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isEmpty() {
      return this.type == FilterMask.Type.PASS_THROUGH;
   }

   public boolean isFullyFiltered() {
      return this.type == FilterMask.Type.FULLY_FILTERED;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         FilterMask filtermask = (FilterMask)object;
         return this.mask.equals(filtermask.mask) && this.type == filtermask.type;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.mask.hashCode();
      return 31 * i + this.type.hashCode();
   }

   static enum Type implements StringRepresentable {
      PASS_THROUGH("pass_through", () -> FilterMask.PASS_THROUGH_CODEC),
      FULLY_FILTERED("fully_filtered", () -> FilterMask.FULLY_FILTERED_CODEC),
      PARTIALLY_FILTERED("partially_filtered", () -> FilterMask.PARTIALLY_FILTERED_CODEC);

      private final String serializedName;
      private final Supplier<Codec<FilterMask>> codec;

      private Type(String s, Supplier<Codec<FilterMask>> supplier) {
         this.serializedName = s;
         this.codec = supplier;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      private Codec<FilterMask> codec() {
         return this.codec.get();
      }
   }
}
