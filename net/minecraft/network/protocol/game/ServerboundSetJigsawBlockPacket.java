package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
   private final BlockPos pos;
   private final ResourceLocation name;
   private final ResourceLocation target;
   private final ResourceLocation pool;
   private final String finalState;
   private final JigsawBlockEntity.JointType joint;

   public ServerboundSetJigsawBlockPacket(BlockPos blockpos, ResourceLocation resourcelocation, ResourceLocation resourcelocation1, ResourceLocation resourcelocation2, String s, JigsawBlockEntity.JointType jigsawblockentity_jointtype) {
      this.pos = blockpos;
      this.name = resourcelocation;
      this.target = resourcelocation1;
      this.pool = resourcelocation2;
      this.finalState = s;
      this.joint = jigsawblockentity_jointtype;
   }

   public ServerboundSetJigsawBlockPacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.name = friendlybytebuf.readResourceLocation();
      this.target = friendlybytebuf.readResourceLocation();
      this.pool = friendlybytebuf.readResourceLocation();
      this.finalState = friendlybytebuf.readUtf();
      this.joint = JigsawBlockEntity.JointType.byName(friendlybytebuf.readUtf()).orElse(JigsawBlockEntity.JointType.ALIGNED);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeResourceLocation(this.name);
      friendlybytebuf.writeResourceLocation(this.target);
      friendlybytebuf.writeResourceLocation(this.pool);
      friendlybytebuf.writeUtf(this.finalState);
      friendlybytebuf.writeUtf(this.joint.getSerializedName());
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSetJigsawBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public ResourceLocation getTarget() {
      return this.target;
   }

   public ResourceLocation getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public JigsawBlockEntity.JointType getJoint() {
      return this.joint;
   }
}
