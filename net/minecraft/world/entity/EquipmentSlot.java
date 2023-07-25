package net.minecraft.world.entity;

public enum EquipmentSlot {
   MAINHAND(EquipmentSlot.Type.HAND, 0, 0, "mainhand"),
   OFFHAND(EquipmentSlot.Type.HAND, 1, 5, "offhand"),
   FEET(EquipmentSlot.Type.ARMOR, 0, 1, "feet"),
   LEGS(EquipmentSlot.Type.ARMOR, 1, 2, "legs"),
   CHEST(EquipmentSlot.Type.ARMOR, 2, 3, "chest"),
   HEAD(EquipmentSlot.Type.ARMOR, 3, 4, "head");

   private final EquipmentSlot.Type type;
   private final int index;
   private final int filterFlag;
   private final String name;

   private EquipmentSlot(EquipmentSlot.Type equipmentslot_type, int i, int j, String s) {
      this.type = equipmentslot_type;
      this.index = i;
      this.filterFlag = j;
      this.name = s;
   }

   public EquipmentSlot.Type getType() {
      return this.type;
   }

   public int getIndex() {
      return this.index;
   }

   public int getIndex(int i) {
      return i + this.index;
   }

   public int getFilterFlag() {
      return this.filterFlag;
   }

   public String getName() {
      return this.name;
   }

   public boolean isArmor() {
      return this.type == EquipmentSlot.Type.ARMOR;
   }

   public static EquipmentSlot byName(String s) {
      for(EquipmentSlot equipmentslot : values()) {
         if (equipmentslot.getName().equals(s)) {
            return equipmentslot;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + s + "'");
   }

   public static EquipmentSlot byTypeAndIndex(EquipmentSlot.Type equipmentslot_type, int i) {
      for(EquipmentSlot equipmentslot : values()) {
         if (equipmentslot.getType() == equipmentslot_type && equipmentslot.getIndex() == i) {
            return equipmentslot;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + equipmentslot_type + "': " + i);
   }

   public static enum Type {
      HAND,
      ARMOR;
   }
}
