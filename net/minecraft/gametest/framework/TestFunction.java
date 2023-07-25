package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public class TestFunction {
   private final String batchName;
   private final String testName;
   private final String structureName;
   private final boolean required;
   private final int maxAttempts;
   private final int requiredSuccesses;
   private final Consumer<GameTestHelper> function;
   private final int maxTicks;
   private final long setupTicks;
   private final Rotation rotation;

   public TestFunction(String s, String s1, String s2, int i, long j, boolean flag, Consumer<GameTestHelper> consumer) {
      this(s, s1, s2, Rotation.NONE, i, j, flag, 1, 1, consumer);
   }

   public TestFunction(String s, String s1, String s2, Rotation rotation, int i, long j, boolean flag, Consumer<GameTestHelper> consumer) {
      this(s, s1, s2, rotation, i, j, flag, 1, 1, consumer);
   }

   public TestFunction(String s, String s1, String s2, Rotation rotation, int i, long j, boolean flag, int k, int l, Consumer<GameTestHelper> consumer) {
      this.batchName = s;
      this.testName = s1;
      this.structureName = s2;
      this.rotation = rotation;
      this.maxTicks = i;
      this.required = flag;
      this.requiredSuccesses = k;
      this.maxAttempts = l;
      this.function = consumer;
      this.setupTicks = j;
   }

   public void run(GameTestHelper gametesthelper) {
      this.function.accept(gametesthelper);
   }

   public String getTestName() {
      return this.testName;
   }

   public String getStructureName() {
      return this.structureName;
   }

   public String toString() {
      return this.testName;
   }

   public int getMaxTicks() {
      return this.maxTicks;
   }

   public boolean isRequired() {
      return this.required;
   }

   public String getBatchName() {
      return this.batchName;
   }

   public long getSetupTicks() {
      return this.setupTicks;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public boolean isFlaky() {
      return this.maxAttempts > 1;
   }

   public int getMaxAttempts() {
      return this.maxAttempts;
   }

   public int getRequiredSuccesses() {
      return this.requiredSuccesses;
   }
}
