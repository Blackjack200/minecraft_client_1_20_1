package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

public class BlockElementFace {
   public static final int NO_TINT = -1;
   public final Direction cullForDirection;
   public final int tintIndex;
   public final String texture;
   public final BlockFaceUV uv;

   public BlockElementFace(@Nullable Direction direction, int i, String s, BlockFaceUV blockfaceuv) {
      this.cullForDirection = direction;
      this.tintIndex = i;
      this.texture = s;
      this.uv = blockfaceuv;
   }

   protected static class Deserializer implements JsonDeserializer<BlockElementFace> {
      private static final int DEFAULT_TINT_INDEX = -1;

      public BlockElementFace deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         Direction direction = this.getCullFacing(jsonobject);
         int i = this.getTintIndex(jsonobject);
         String s = this.getTexture(jsonobject);
         BlockFaceUV blockfaceuv = jsondeserializationcontext.deserialize(jsonobject, BlockFaceUV.class);
         return new BlockElementFace(direction, i, s, blockfaceuv);
      }

      protected int getTintIndex(JsonObject jsonobject) {
         return GsonHelper.getAsInt(jsonobject, "tintindex", -1);
      }

      private String getTexture(JsonObject jsonobject) {
         return GsonHelper.getAsString(jsonobject, "texture");
      }

      @Nullable
      private Direction getCullFacing(JsonObject jsonobject) {
         String s = GsonHelper.getAsString(jsonobject, "cullface", "");
         return Direction.byName(s);
      }
   }
}
