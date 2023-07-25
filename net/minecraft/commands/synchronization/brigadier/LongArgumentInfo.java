package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo implements ArgumentTypeInfo<LongArgumentType, LongArgumentInfo.Template> {
   public void serializeToNetwork(LongArgumentInfo.Template longargumentinfo_template, FriendlyByteBuf friendlybytebuf) {
      boolean flag = longargumentinfo_template.min != Long.MIN_VALUE;
      boolean flag1 = longargumentinfo_template.max != Long.MAX_VALUE;
      friendlybytebuf.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         friendlybytebuf.writeLong(longargumentinfo_template.min);
      }

      if (flag1) {
         friendlybytebuf.writeLong(longargumentinfo_template.max);
      }

   }

   public LongArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
      byte b0 = friendlybytebuf.readByte();
      long i = ArgumentUtils.numberHasMin(b0) ? friendlybytebuf.readLong() : Long.MIN_VALUE;
      long j = ArgumentUtils.numberHasMax(b0) ? friendlybytebuf.readLong() : Long.MAX_VALUE;
      return new LongArgumentInfo.Template(i, j);
   }

   public void serializeToJson(LongArgumentInfo.Template longargumentinfo_template, JsonObject jsonobject) {
      if (longargumentinfo_template.min != Long.MIN_VALUE) {
         jsonobject.addProperty("min", longargumentinfo_template.min);
      }

      if (longargumentinfo_template.max != Long.MAX_VALUE) {
         jsonobject.addProperty("max", longargumentinfo_template.max);
      }

   }

   public LongArgumentInfo.Template unpack(LongArgumentType longargumenttype) {
      return new LongArgumentInfo.Template(longargumenttype.getMinimum(), longargumenttype.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<LongArgumentType> {
      final long min;
      final long max;

      Template(long i, long j) {
         this.min = i;
         this.max = j;
      }

      public LongArgumentType instantiate(CommandBuildContext commandbuildcontext) {
         return LongArgumentType.longArg(this.min, this.max);
      }

      public ArgumentTypeInfo<LongArgumentType, ?> type() {
         return LongArgumentInfo.this;
      }
   }
}
