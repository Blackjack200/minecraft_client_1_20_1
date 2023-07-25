package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class FurnaceMenu extends AbstractFurnaceMenu {
   public FurnaceMenu(int i, Inventory inventory) {
      super(MenuType.FURNACE, RecipeType.SMELTING, RecipeBookType.FURNACE, i, inventory);
   }

   public FurnaceMenu(int i, Inventory inventory, Container container, ContainerData containerdata) {
      super(MenuType.FURNACE, RecipeType.SMELTING, RecipeBookType.FURNACE, i, inventory, container, containerdata);
   }
}
