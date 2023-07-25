package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo implements ArgumentTypeInfo<IntegerArgumentType, IntegerArgumentInfo.Template> {
   public void serializeToNetwork(IntegerArgumentInfo.Template integerargumentinfo_template, FriendlyByteBuf friendlybytebuf) {
      boolean flag = integerargumentinfo_template.min != Integer.MIN_VALUE;
      boolean flag1 = integerargumentinfo_template.max != Integer.MAX_VALUE;
      friendlybytebuf.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         friendlybytebuf.writeInt(integerargumentinfo_template.min);
      }

      if (flag1) {
         friendlybytebuf.writeInt(integerargumentinfo_template.max);
      }

   }

   public IntegerArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
      byte b0 = friendlybytebuf.readByte();
      int i = ArgumentUtils.numberHasMin(b0) ? friendlybytebuf.readInt() : Integer.MIN_VALUE;
      int j = ArgumentUtils.numberHasMax(b0) ? friendlybytebuf.readInt() : Integer.MAX_VALUE;
      return new IntegerArgumentInfo.Template(i, j);
   }

   public void serializeToJson(IntegerArgumentInfo.Template integerargumentinfo_template, JsonObject jsonobject) {
      if (integerargumentinfo_template.min != Integer.MIN_VALUE) {
         jsonobject.addProperty("min", integerargumentinfo_template.min);
      }

      if (integerargumentinfo_template.max != Integer.MAX_VALUE) {
         jsonobject.addProperty("max", integerargumentinfo_template.max);
      }

   }

   public IntegerArgumentInfo.Template unpack(IntegerArgumentType integerargumenttype) {
      return new IntegerArgumentInfo.Template(integerargumenttype.getMinimum(), integerargumenttype.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<IntegerArgumentType> {
      final int min;
      final int max;

      Template(int i, int j) {
         this.min = i;
         this.max = j;
      }

      public IntegerArgumentType instantiate(CommandBuildContext commandbuildcontext) {
         return IntegerArgumentType.integer(this.min, this.max);
      }

      public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
         return IntegerArgumentInfo.this;
      }
   }
}
