package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.FriendlyByteBuf;

public interface ArgumentTypeInfo<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> {
   void serializeToNetwork(T argumenttypeinfo_template, FriendlyByteBuf friendlybytebuf);

   T deserializeFromNetwork(FriendlyByteBuf friendlybytebuf);

   void serializeToJson(T argumenttypeinfo_template, JsonObject jsonobject);

   T unpack(A argumenttype);

   public interface Template<A extends ArgumentType<?>> {
      A instantiate(CommandBuildContext commandbuildcontext);

      ArgumentTypeInfo<A, ?> type();
   }
}
