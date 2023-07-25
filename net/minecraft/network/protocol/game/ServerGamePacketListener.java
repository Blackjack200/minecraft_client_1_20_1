package net.minecraft.network.protocol.game;

public interface ServerGamePacketListener extends ServerPacketListener {
   void handleAnimate(ServerboundSwingPacket serverboundswingpacket);

   void handleChat(ServerboundChatPacket serverboundchatpacket);

   void handleChatCommand(ServerboundChatCommandPacket serverboundchatcommandpacket);

   void handleChatAck(ServerboundChatAckPacket serverboundchatackpacket);

   void handleClientCommand(ServerboundClientCommandPacket serverboundclientcommandpacket);

   void handleClientInformation(ServerboundClientInformationPacket serverboundclientinformationpacket);

   void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundcontainerbuttonclickpacket);

   void handleContainerClick(ServerboundContainerClickPacket serverboundcontainerclickpacket);

   void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundplacerecipepacket);

   void handleContainerClose(ServerboundContainerClosePacket serverboundcontainerclosepacket);

   void handleCustomPayload(ServerboundCustomPayloadPacket serverboundcustompayloadpacket);

   void handleInteract(ServerboundInteractPacket serverboundinteractpacket);

   void handleKeepAlive(ServerboundKeepAlivePacket serverboundkeepalivepacket);

   void handleMovePlayer(ServerboundMovePlayerPacket serverboundmoveplayerpacket);

   void handlePong(ServerboundPongPacket serverboundpongpacket);

   void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundplayerabilitiespacket);

   void handlePlayerAction(ServerboundPlayerActionPacket serverboundplayeractionpacket);

   void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundplayercommandpacket);

   void handlePlayerInput(ServerboundPlayerInputPacket serverboundplayerinputpacket);

   void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundsetcarrieditempacket);

   void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundsetcreativemodeslotpacket);

   void handleSignUpdate(ServerboundSignUpdatePacket serverboundsignupdatepacket);

   void handleUseItemOn(ServerboundUseItemOnPacket serverbounduseitemonpacket);

   void handleUseItem(ServerboundUseItemPacket serverbounduseitempacket);

   void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundteleporttoentitypacket);

   void handleResourcePackResponse(ServerboundResourcePackPacket serverboundresourcepackpacket);

   void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundpaddleboatpacket);

   void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundmovevehiclepacket);

   void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundacceptteleportationpacket);

   void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundrecipebookseenrecipepacket);

   void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundrecipebookchangesettingspacket);

   void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundseenadvancementspacket);

   void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundcommandsuggestionpacket);

   void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundsetcommandblockpacket);

   void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundsetcommandminecartpacket);

   void handlePickItem(ServerboundPickItemPacket serverboundpickitempacket);

   void handleRenameItem(ServerboundRenameItemPacket serverboundrenameitempacket);

   void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundsetbeaconpacket);

   void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundsetstructureblockpacket);

   void handleSelectTrade(ServerboundSelectTradePacket serverboundselecttradepacket);

   void handleEditBook(ServerboundEditBookPacket serverboundeditbookpacket);

   void handleEntityTagQuery(ServerboundEntityTagQuery serverboundentitytagquery);

   void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery serverboundblockentitytagquery);

   void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundsetjigsawblockpacket);

   void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundjigsawgeneratepacket);

   void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundchangedifficultypacket);

   void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundlockdifficultypacket);

   void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundchatsessionupdatepacket);
}
