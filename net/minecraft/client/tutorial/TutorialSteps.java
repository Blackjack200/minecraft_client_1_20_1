package net.minecraft.client.tutorial;

import java.util.function.Function;

public enum TutorialSteps {
   MOVEMENT("movement", MovementTutorialStepInstance::new),
   FIND_TREE("find_tree", FindTreeTutorialStepInstance::new),
   PUNCH_TREE("punch_tree", PunchTreeTutorialStepInstance::new),
   OPEN_INVENTORY("open_inventory", OpenInventoryTutorialStep::new),
   CRAFT_PLANKS("craft_planks", CraftPlanksTutorialStep::new),
   NONE("none", CompletedTutorialStepInstance::new);

   private final String name;
   private final Function<Tutorial, ? extends TutorialStepInstance> constructor;

   private <T extends TutorialStepInstance> TutorialSteps(String s, Function<Tutorial, T> function) {
      this.name = s;
      this.constructor = function;
   }

   public TutorialStepInstance create(Tutorial tutorial) {
      return this.constructor.apply(tutorial);
   }

   public String getName() {
      return this.name;
   }

   public static TutorialSteps getByName(String s) {
      for(TutorialSteps tutorialsteps : values()) {
         if (tutorialsteps.name.equals(s)) {
            return tutorialsteps;
         }
      }

      return NONE;
   }
}
