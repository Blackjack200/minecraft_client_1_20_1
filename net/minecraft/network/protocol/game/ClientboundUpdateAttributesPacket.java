package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

   public ClientboundUpdateAttributesPacket(int i, Collection<AttributeInstance> collection) {
      this.entityId = i;
      this.attributes = Lists.newArrayList();

      for(AttributeInstance attributeinstance : collection) {
         this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(attributeinstance.getAttribute(), attributeinstance.getBaseValue(), attributeinstance.getModifiers()));
      }

   }

   public ClientboundUpdateAttributesPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityId = friendlybytebuf.readVarInt();
      this.attributes = friendlybytebuf.readList((friendlybytebuf1) -> {
         ResourceLocation resourcelocation = friendlybytebuf1.readResourceLocation();
         Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(resourcelocation);
         double d0 = friendlybytebuf1.readDouble();
         List<AttributeModifier> list = friendlybytebuf1.readList((friendlybytebuf2) -> new AttributeModifier(friendlybytebuf2.readUUID(), "Unknown synced attribute modifier", friendlybytebuf2.readDouble(), AttributeModifier.Operation.fromValue(friendlybytebuf2.readByte())));
         return new ClientboundUpdateAttributesPacket.AttributeSnapshot(attribute, d0, list);
      });
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.entityId);
      friendlybytebuf.writeCollection(this.attributes, (friendlybytebuf1, clientboundupdateattributespacket_attributesnapshot) -> {
         friendlybytebuf1.writeResourceLocation(BuiltInRegistries.ATTRIBUTE.getKey(clientboundupdateattributespacket_attributesnapshot.getAttribute()));
         friendlybytebuf1.writeDouble(clientboundupdateattributespacket_attributesnapshot.getBase());
         friendlybytebuf1.writeCollection(clientboundupdateattributespacket_attributesnapshot.getModifiers(), (friendlybytebuf2, attributemodifier) -> {
            friendlybytebuf2.writeUUID(attributemodifier.getId());
            friendlybytebuf2.writeDouble(attributemodifier.getAmount());
            friendlybytebuf2.writeByte(attributemodifier.getOperation().toValue());
         });
      });
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleUpdateAttributes(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public List<ClientboundUpdateAttributesPacket.AttributeSnapshot> getValues() {
      return this.attributes;
   }

   public static class AttributeSnapshot {
      private final Attribute attribute;
      private final double base;
      private final Collection<AttributeModifier> modifiers;

      public AttributeSnapshot(Attribute attribute, double d0, Collection<AttributeModifier> collection) {
         this.attribute = attribute;
         this.base = d0;
         this.modifiers = collection;
      }

      public Attribute getAttribute() {
         return this.attribute;
      }

      public double getBase() {
         return this.base;
      }

      public Collection<AttributeModifier> getModifiers() {
         return this.modifiers;
      }
   }
}
