package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
   public static final ParticleOptions.Deserializer<ItemParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ItemParticleOption>() {
      public ItemParticleOption fromCommand(ParticleType<ItemParticleOption> particletype, StringReader stringreader) throws CommandSyntaxException {
         stringreader.expect(' ');
         ItemParser.ItemResult itemparser_itemresult = ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), stringreader);
         ItemStack itemstack = (new ItemInput(itemparser_itemresult.item(), itemparser_itemresult.nbt())).createItemStack(1, false);
         return new ItemParticleOption(particletype, itemstack);
      }

      public ItemParticleOption fromNetwork(ParticleType<ItemParticleOption> particletype, FriendlyByteBuf friendlybytebuf) {
         return new ItemParticleOption(particletype, friendlybytebuf.readItem());
      }
   };
   private final ParticleType<ItemParticleOption> type;
   private final ItemStack itemStack;

   public static Codec<ItemParticleOption> codec(ParticleType<ItemParticleOption> particletype) {
      return ItemStack.CODEC.xmap((itemstack) -> new ItemParticleOption(particletype, itemstack), (itemparticleoption) -> itemparticleoption.itemStack);
   }

   public ItemParticleOption(ParticleType<ItemParticleOption> particletype, ItemStack itemstack) {
      this.type = particletype;
      this.itemStack = itemstack;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeItem(this.itemStack);
   }

   public String writeToString() {
      return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + (new ItemInput(this.itemStack.getItemHolder(), this.itemStack.getTag())).serialize();
   }

   public ParticleType<ItemParticleOption> getType() {
      return this.type;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }
}
