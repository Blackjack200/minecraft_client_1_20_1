package net.minecraft.world.entity.ai.attributes;

public class Attribute {
   public static final int MAX_NAME_LENGTH = 64;
   private final double defaultValue;
   private boolean syncable;
   private final String descriptionId;

   protected Attribute(String s, double d0) {
      this.defaultValue = d0;
      this.descriptionId = s;
   }

   public double getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isClientSyncable() {
      return this.syncable;
   }

   public Attribute setSyncable(boolean flag) {
      this.syncable = flag;
      return this;
   }

   public double sanitizeValue(double d0) {
      return d0;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }
}
