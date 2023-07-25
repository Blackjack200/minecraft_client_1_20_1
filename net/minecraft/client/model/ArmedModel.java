package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public interface ArmedModel {
   void translateToHand(HumanoidArm humanoidarm, PoseStack posestack);
}
