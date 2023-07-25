package net.minecraft.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class KeyMapping implements Comparable<KeyMapping> {
   private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
   private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.newHashMap();
   private static final Set<String> CATEGORIES = Sets.newHashSet();
   public static final String CATEGORY_MOVEMENT = "key.categories.movement";
   public static final String CATEGORY_MISC = "key.categories.misc";
   public static final String CATEGORY_MULTIPLAYER = "key.categories.multiplayer";
   public static final String CATEGORY_GAMEPLAY = "key.categories.gameplay";
   public static final String CATEGORY_INVENTORY = "key.categories.inventory";
   public static final String CATEGORY_INTERFACE = "key.categories.ui";
   public static final String CATEGORY_CREATIVE = "key.categories.creative";
   private static final Map<String, Integer> CATEGORY_SORT_ORDER = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("key.categories.movement", 1);
      hashmap.put("key.categories.gameplay", 2);
      hashmap.put("key.categories.inventory", 3);
      hashmap.put("key.categories.creative", 4);
      hashmap.put("key.categories.multiplayer", 5);
      hashmap.put("key.categories.ui", 6);
      hashmap.put("key.categories.misc", 7);
   });
   private final String name;
   private final InputConstants.Key defaultKey;
   private final String category;
   private InputConstants.Key key;
   private boolean isDown;
   private int clickCount;

   public static void click(InputConstants.Key inputconstants_key) {
      KeyMapping keymapping = MAP.get(inputconstants_key);
      if (keymapping != null) {
         ++keymapping.clickCount;
      }

   }

   public static void set(InputConstants.Key inputconstants_key, boolean flag) {
      KeyMapping keymapping = MAP.get(inputconstants_key);
      if (keymapping != null) {
         keymapping.setDown(flag);
      }

   }

   public static void setAll() {
      for(KeyMapping keymapping : ALL.values()) {
         if (keymapping.key.getType() == InputConstants.Type.KEYSYM && keymapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
            keymapping.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keymapping.key.getValue()));
         }
      }

   }

   public static void releaseAll() {
      for(KeyMapping keymapping : ALL.values()) {
         keymapping.release();
      }

   }

   public static void resetToggleKeys() {
      for(KeyMapping keymapping : ALL.values()) {
         if (keymapping instanceof ToggleKeyMapping togglekeymapping) {
            togglekeymapping.reset();
         }
      }

   }

   public static void resetMapping() {
      MAP.clear();

      for(KeyMapping keymapping : ALL.values()) {
         MAP.put(keymapping.key, keymapping);
      }

   }

   public KeyMapping(String s, int i, String s1) {
      this(s, InputConstants.Type.KEYSYM, i, s1);
   }

   public KeyMapping(String s, InputConstants.Type inputconstants_type, int i, String s1) {
      this.name = s;
      this.key = inputconstants_type.getOrCreate(i);
      this.defaultKey = this.key;
      this.category = s1;
      ALL.put(s, this);
      MAP.put(this.key, this);
      CATEGORIES.add(s1);
   }

   public boolean isDown() {
      return this.isDown;
   }

   public String getCategory() {
      return this.category;
   }

   public boolean consumeClick() {
      if (this.clickCount == 0) {
         return false;
      } else {
         --this.clickCount;
         return true;
      }
   }

   private void release() {
      this.clickCount = 0;
      this.setDown(false);
   }

   public String getName() {
      return this.name;
   }

   public InputConstants.Key getDefaultKey() {
      return this.defaultKey;
   }

   public void setKey(InputConstants.Key inputconstants_key) {
      this.key = inputconstants_key;
   }

   public int compareTo(KeyMapping keymapping) {
      return this.category.equals(keymapping.category) ? I18n.get(this.name).compareTo(I18n.get(keymapping.name)) : CATEGORY_SORT_ORDER.get(this.category).compareTo(CATEGORY_SORT_ORDER.get(keymapping.category));
   }

   public static Supplier<Component> createNameSupplier(String s) {
      KeyMapping keymapping = ALL.get(s);
      return keymapping == null ? () -> Component.translatable(s) : keymapping::getTranslatedKeyMessage;
   }

   public boolean same(KeyMapping keymapping) {
      return this.key.equals(keymapping.key);
   }

   public boolean isUnbound() {
      return this.key.equals(InputConstants.UNKNOWN);
   }

   public boolean matches(int i, int j) {
      if (i == InputConstants.UNKNOWN.getValue()) {
         return this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == j;
      } else {
         return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == i;
      }
   }

   public boolean matchesMouse(int i) {
      return this.key.getType() == InputConstants.Type.MOUSE && this.key.getValue() == i;
   }

   public Component getTranslatedKeyMessage() {
      return this.key.getDisplayName();
   }

   public boolean isDefault() {
      return this.key.equals(this.defaultKey);
   }

   public String saveString() {
      return this.key.getName();
   }

   public void setDown(boolean flag) {
      this.isDown = flag;
   }
}
