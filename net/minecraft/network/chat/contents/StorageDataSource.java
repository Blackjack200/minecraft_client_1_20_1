package net.minecraft.network.chat.contents;

import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record StorageDataSource(ResourceLocation id) implements DataSource {
   public Stream<CompoundTag> getData(CommandSourceStack commandsourcestack) {
      CompoundTag compoundtag = commandsourcestack.getServer().getCommandStorage().get(this.id);
      return Stream.of(compoundtag);
   }

   public String toString() {
      return "storage=" + this.id;
   }
}
