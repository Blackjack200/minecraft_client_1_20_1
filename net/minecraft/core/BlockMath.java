package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class BlockMath {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL = Util.make(Maps.newEnumMap(Direction.class), (enummap) -> {
      enummap.put(Direction.SOUTH, Transformation.identity());
      enummap.put(Direction.EAST, new Transformation((Vector3f)null, (new Quaternionf()).rotateY(((float)Math.PI / 2F)), (Vector3f)null, (Quaternionf)null));
      enummap.put(Direction.WEST, new Transformation((Vector3f)null, (new Quaternionf()).rotateY((-(float)Math.PI / 2F)), (Vector3f)null, (Quaternionf)null));
      enummap.put(Direction.NORTH, new Transformation((Vector3f)null, (new Quaternionf()).rotateY((float)Math.PI), (Vector3f)null, (Quaternionf)null));
      enummap.put(Direction.UP, new Transformation((Vector3f)null, (new Quaternionf()).rotateX((-(float)Math.PI / 2F)), (Vector3f)null, (Quaternionf)null));
      enummap.put(Direction.DOWN, new Transformation((Vector3f)null, (new Quaternionf()).rotateX(((float)Math.PI / 2F)), (Vector3f)null, (Quaternionf)null));
   });
   public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL = Util.make(Maps.newEnumMap(Direction.class), (enummap) -> {
      for(Direction direction : Direction.values()) {
         enummap.put(direction, VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction).inverse());
      }

   });

   public static Transformation blockCenterToCorner(Transformation transformation) {
      Matrix4f matrix4f = (new Matrix4f()).translation(0.5F, 0.5F, 0.5F);
      matrix4f.mul(transformation.getMatrix());
      matrix4f.translate(-0.5F, -0.5F, -0.5F);
      return new Transformation(matrix4f);
   }

   public static Transformation blockCornerToCenter(Transformation transformation) {
      Matrix4f matrix4f = (new Matrix4f()).translation(-0.5F, -0.5F, -0.5F);
      matrix4f.mul(transformation.getMatrix());
      matrix4f.translate(0.5F, 0.5F, 0.5F);
      return new Transformation(matrix4f);
   }

   public static Transformation getUVLockTransform(Transformation transformation, Direction direction, Supplier<String> supplier) {
      Direction direction1 = Direction.rotate(transformation.getMatrix(), direction);
      Transformation transformation1 = transformation.inverse();
      if (transformation1 == null) {
         LOGGER.warn(supplier.get());
         return new Transformation((Vector3f)null, (Quaternionf)null, new Vector3f(0.0F, 0.0F, 0.0F), (Quaternionf)null);
      } else {
         Transformation transformation2 = VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL.get(direction).compose(transformation1).compose(VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction1));
         return blockCenterToCorner(transformation2);
      }
   }
}
