package net.minecraft.world.entity;

import net.minecraft.world.entity.ai.village.ReputationEventType;

public interface ReputationEventHandler {
   void onReputationEventFrom(ReputationEventType reputationeventtype, Entity entity);
}
