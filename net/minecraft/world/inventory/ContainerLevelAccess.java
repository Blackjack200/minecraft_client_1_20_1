package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
   ContainerLevelAccess NULL = new ContainerLevelAccess() {
      public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> bifunction) {
         return Optional.empty();
      }
   };

   static ContainerLevelAccess create(final Level level, final BlockPos blockpos) {
      return new ContainerLevelAccess() {
         public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> bifunction) {
            return Optional.of(bifunction.apply(level, blockpos));
         }
      };
   }

   <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> bifunction);

   default <T> T evaluate(BiFunction<Level, BlockPos, T> bifunction, T object) {
      return this.evaluate(bifunction).orElse(object);
   }

   default void execute(BiConsumer<Level, BlockPos> biconsumer) {
      this.evaluate((level, blockpos) -> {
         biconsumer.accept(level, blockpos);
         return Optional.empty();
      });
   }
}
