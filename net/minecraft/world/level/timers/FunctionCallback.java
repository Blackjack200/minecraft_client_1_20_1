package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionCallback implements TimerCallback<MinecraftServer> {
   final ResourceLocation functionId;

   public FunctionCallback(ResourceLocation resourcelocation) {
      this.functionId = resourcelocation;
   }

   public void handle(MinecraftServer minecraftserver, TimerQueue<MinecraftServer> timerqueue, long i) {
      ServerFunctionManager serverfunctionmanager = minecraftserver.getFunctions();
      serverfunctionmanager.get(this.functionId).ifPresent((commandfunction) -> serverfunctionmanager.execute(commandfunction, serverfunctionmanager.getGameLoopSender()));
   }

   public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionCallback> {
      public Serializer() {
         super(new ResourceLocation("function"), FunctionCallback.class);
      }

      public void serialize(CompoundTag compoundtag, FunctionCallback functioncallback) {
         compoundtag.putString("Name", functioncallback.functionId.toString());
      }

      public FunctionCallback deserialize(CompoundTag compoundtag) {
         ResourceLocation resourcelocation = new ResourceLocation(compoundtag.getString("Name"));
         return new FunctionCallback(resourcelocation);
      }
   }
}
