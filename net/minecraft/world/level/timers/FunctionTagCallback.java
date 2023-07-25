package net.minecraft.world.level.timers;

import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionTagCallback implements TimerCallback<MinecraftServer> {
   final ResourceLocation tagId;

   public FunctionTagCallback(ResourceLocation resourcelocation) {
      this.tagId = resourcelocation;
   }

   public void handle(MinecraftServer minecraftserver, TimerQueue<MinecraftServer> timerqueue, long i) {
      ServerFunctionManager serverfunctionmanager = minecraftserver.getFunctions();

      for(CommandFunction commandfunction : serverfunctionmanager.getTag(this.tagId)) {
         serverfunctionmanager.execute(commandfunction, serverfunctionmanager.getGameLoopSender());
      }

   }

   public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionTagCallback> {
      public Serializer() {
         super(new ResourceLocation("function_tag"), FunctionTagCallback.class);
      }

      public void serialize(CompoundTag compoundtag, FunctionTagCallback functiontagcallback) {
         compoundtag.putString("Name", functiontagcallback.tagId.toString());
      }

      public FunctionTagCallback deserialize(CompoundTag compoundtag) {
         ResourceLocation resourcelocation = new ResourceLocation(compoundtag.getString("Name"));
         return new FunctionTagCallback(resourcelocation);
      }
   }
}
