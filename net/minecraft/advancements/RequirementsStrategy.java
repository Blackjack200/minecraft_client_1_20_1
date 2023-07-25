package net.minecraft.advancements;

import java.util.Collection;

public interface RequirementsStrategy {
   RequirementsStrategy AND = (collection) -> {
      String[][] astring = new String[collection.size()][];
      int i = 0;

      for(String s : collection) {
         astring[i++] = new String[]{s};
      }

      return astring;
   };
   RequirementsStrategy OR = (collection) -> new String[][]{collection.toArray(new String[0])};

   String[][] createRequirements(Collection<String> collection);
}
