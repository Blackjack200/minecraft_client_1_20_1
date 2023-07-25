package net.minecraft.client.gui.screens;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;

public class MenuScreens {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> SCREENS = Maps.newHashMap();

   public static <T extends AbstractContainerMenu> void create(@Nullable MenuType<T> menutype, Minecraft minecraft, int i, Component component) {
      if (menutype == null) {
         LOGGER.warn("Trying to open invalid screen with name: {}", (Object)component.getString());
      } else {
         MenuScreens.ScreenConstructor<T, ?> menuscreens_screenconstructor = getConstructor(menutype);
         if (menuscreens_screenconstructor == null) {
            LOGGER.warn("Failed to create screen for menu type: {}", (Object)BuiltInRegistries.MENU.getKey(menutype));
         } else {
            menuscreens_screenconstructor.fromPacket(component, menutype, minecraft, i);
         }
      }
   }

   @Nullable
   private static <T extends AbstractContainerMenu> MenuScreens.ScreenConstructor<T, ?> getConstructor(MenuType<T> menutype) {
      return SCREENS.get(menutype);
   }

   private static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> menutype, MenuScreens.ScreenConstructor<M, U> menuscreens_screenconstructor) {
      MenuScreens.ScreenConstructor<?, ?> menuscreens_screenconstructor1 = SCREENS.put(menutype, menuscreens_screenconstructor);
      if (menuscreens_screenconstructor1 != null) {
         throw new IllegalStateException("Duplicate registration for " + BuiltInRegistries.MENU.getKey(menutype));
      }
   }

   public static boolean selfTest() {
      boolean flag = false;

      for(MenuType<?> menutype : BuiltInRegistries.MENU) {
         if (!SCREENS.containsKey(menutype)) {
            LOGGER.debug("Menu {} has no matching screen", (Object)BuiltInRegistries.MENU.getKey(menutype));
            flag = true;
         }
      }

      return flag;
   }

   static {
      register(MenuType.GENERIC_9x1, ContainerScreen::new);
      register(MenuType.GENERIC_9x2, ContainerScreen::new);
      register(MenuType.GENERIC_9x3, ContainerScreen::new);
      register(MenuType.GENERIC_9x4, ContainerScreen::new);
      register(MenuType.GENERIC_9x5, ContainerScreen::new);
      register(MenuType.GENERIC_9x6, ContainerScreen::new);
      register(MenuType.GENERIC_3x3, DispenserScreen::new);
      register(MenuType.ANVIL, AnvilScreen::new);
      register(MenuType.BEACON, BeaconScreen::new);
      register(MenuType.BLAST_FURNACE, BlastFurnaceScreen::new);
      register(MenuType.BREWING_STAND, BrewingStandScreen::new);
      register(MenuType.CRAFTING, CraftingScreen::new);
      register(MenuType.ENCHANTMENT, EnchantmentScreen::new);
      register(MenuType.FURNACE, FurnaceScreen::new);
      register(MenuType.GRINDSTONE, GrindstoneScreen::new);
      register(MenuType.HOPPER, HopperScreen::new);
      register(MenuType.LECTERN, LecternScreen::new);
      register(MenuType.LOOM, LoomScreen::new);
      register(MenuType.MERCHANT, MerchantScreen::new);
      register(MenuType.SHULKER_BOX, ShulkerBoxScreen::new);
      register(MenuType.SMITHING, SmithingScreen::new);
      register(MenuType.SMOKER, SmokerScreen::new);
      register(MenuType.CARTOGRAPHY_TABLE, CartographyTableScreen::new);
      register(MenuType.STONECUTTER, StonecutterScreen::new);
   }

   interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
      default void fromPacket(Component component, MenuType<T> menutype, Minecraft minecraft, int i) {
         U screen = this.create(menutype.create(i, minecraft.player.getInventory()), minecraft.player.getInventory(), component);
         minecraft.player.containerMenu = screen.getMenu();
         minecraft.setScreen(screen);
      }

      U create(T abstractcontainermenu, Inventory inventory, Component component);
   }
}
