package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo implements ArgumentTypeInfo<FloatArgumentType, FloatArgumentInfo.Template> {
   public void serializeToNetwork(FloatArgumentInfo.Template floatargumentinfo_template, FriendlyByteBuf friendlybytebuf) {
      boolean flag = floatargumentinfo_template.min != -Float.MAX_VALUE;
      boolean flag1 = floatargumentinfo_template.max != Float.MAX_VALUE;
      friendlybytebuf.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         friendlybytebuf.writeFloat(floatargumentinfo_template.min);
      }

      if (flag1) {
         friendlybytebuf.writeFloat(floatargumentinfo_template.max);
      }

   }

   public FloatArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
      byte b0 = friendlybytebuf.readByte();
      float f = ArgumentUtils.numberHasMin(b0) ? friendlybytebuf.readFloat() : -Float.MAX_VALUE;
      float f1 = ArgumentUtils.numberHasMax(b0) ? friendlybytebuf.readFloat() : Float.MAX_VALUE;
      return new FloatArgumentInfo.Template(f, f1);
   }

   public void serializeToJson(FloatArgumentInfo.Template floatargumentinfo_template, JsonObject jsonobject) {
      if (floatargumentinfo_template.min != -Float.MAX_VALUE) {
         jsonobject.addProperty("min", floatargumentinfo_template.min);
      }

      if (floatargumentinfo_template.max != Float.MAX_VALUE) {
         jsonobject.addProperty("max", floatargumentinfo_template.max);
      }

   }

   public FloatArgumentInfo.Template unpack(FloatArgumentType floatargumenttype) {
      return new FloatArgumentInfo.Template(floatargumenttype.getMinimum(), floatargumenttype.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<FloatArgumentType> {
      final float min;
      final float max;

      Template(float f, float f1) {
         this.min = f;
         this.max = f1;
      }

      public FloatArgumentType instantiate(CommandBuildContext commandbuildcontext) {
         return FloatArgumentType.floatArg(this.min, this.max);
      }

      public ArgumentTypeInfo<FloatArgumentType, ?> type() {
         return FloatArgumentInfo.this;
      }
   }
}
