package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
   public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(false, Blocks.ACACIA_BUTTON.defaultBlockState(), Blocks.CANDLE.defaultBlockState(), Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), Blocks.GLASS.defaultBlockState());
   public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.BOOL.optionalFieldOf("debug_mode", Boolean.valueOf(false)).forGetter(CarverDebugSettings::isDebugMode), BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState), BlockState.CODEC.optionalFieldOf("water_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getWaterState), BlockState.CODEC.optionalFieldOf("lava_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getLavaState), BlockState.CODEC.optionalFieldOf("barrier_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getBarrierState)).apply(recordcodecbuilder_instance, CarverDebugSettings::new));
   private final boolean debugMode;
   private final BlockState airState;
   private final BlockState waterState;
   private final BlockState lavaState;
   private final BlockState barrierState;

   public static CarverDebugSettings of(boolean flag, BlockState blockstate, BlockState blockstate1, BlockState blockstate2, BlockState blockstate3) {
      return new CarverDebugSettings(flag, blockstate, blockstate1, blockstate2, blockstate3);
   }

   public static CarverDebugSettings of(BlockState blockstate, BlockState blockstate1, BlockState blockstate2, BlockState blockstate3) {
      return new CarverDebugSettings(false, blockstate, blockstate1, blockstate2, blockstate3);
   }

   public static CarverDebugSettings of(boolean flag, BlockState blockstate) {
      return new CarverDebugSettings(flag, blockstate, DEFAULT.getWaterState(), DEFAULT.getLavaState(), DEFAULT.getBarrierState());
   }

   private CarverDebugSettings(boolean flag, BlockState blockstate, BlockState blockstate1, BlockState blockstate2, BlockState blockstate3) {
      this.debugMode = flag;
      this.airState = blockstate;
      this.waterState = blockstate1;
      this.lavaState = blockstate2;
      this.barrierState = blockstate3;
   }

   public boolean isDebugMode() {
      return this.debugMode;
   }

   public BlockState getAirState() {
      return this.airState;
   }

   public BlockState getWaterState() {
      return this.waterState;
   }

   public BlockState getLavaState() {
      return this.lavaState;
   }

   public BlockState getBarrierState() {
      return this.barrierState;
   }
}
