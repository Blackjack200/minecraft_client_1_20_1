package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo implements ArgumentTypeInfo<DoubleArgumentType, DoubleArgumentInfo.Template> {
   public void serializeToNetwork(DoubleArgumentInfo.Template doubleargumentinfo_template, FriendlyByteBuf friendlybytebuf) {
      boolean flag = doubleargumentinfo_template.min != -Double.MAX_VALUE;
      boolean flag1 = doubleargumentinfo_template.max != Double.MAX_VALUE;
      friendlybytebuf.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         friendlybytebuf.writeDouble(doubleargumentinfo_template.min);
      }

      if (flag1) {
         friendlybytebuf.writeDouble(doubleargumentinfo_template.max);
      }

   }

   public DoubleArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
      byte b0 = friendlybytebuf.readByte();
      double d0 = ArgumentUtils.numberHasMin(b0) ? friendlybytebuf.readDouble() : -Double.MAX_VALUE;
      double d1 = ArgumentUtils.numberHasMax(b0) ? friendlybytebuf.readDouble() : Double.MAX_VALUE;
      return new DoubleArgumentInfo.Template(d0, d1);
   }

   public void serializeToJson(DoubleArgumentInfo.Template doubleargumentinfo_template, JsonObject jsonobject) {
      if (doubleargumentinfo_template.min != -Double.MAX_VALUE) {
         jsonobject.addProperty("min", doubleargumentinfo_template.min);
      }

      if (doubleargumentinfo_template.max != Double.MAX_VALUE) {
         jsonobject.addProperty("max", doubleargumentinfo_template.max);
      }

   }

   public DoubleArgumentInfo.Template unpack(DoubleArgumentType doubleargumenttype) {
      return new DoubleArgumentInfo.Template(doubleargumenttype.getMinimum(), doubleargumenttype.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<DoubleArgumentType> {
      final double min;
      final double max;

      Template(double d0, double d1) {
         this.min = d0;
         this.max = d1;
      }

      public DoubleArgumentType instantiate(CommandBuildContext commandbuildcontext) {
         return DoubleArgumentType.doubleArg(this.min, this.max);
      }

      public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
         return DoubleArgumentInfo.this;
      }
   }
}
