package net.polarfox27.jobs.events.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.polarfox27.jobs.data.ClientJobsData;
import net.polarfox27.jobs.gui.GuiGainXP;
import net.polarfox27.jobs.gui.containers.JobsCraftingMenu;

@EventBusSubscriber
public class GuiEvents {

    /**
     * Render the Gain XP interface if needed. Is executed at every frame.
     * @param e the Render Event
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientTick(RenderGameOverlayEvent e) {
        if(Minecraft.getInstance().player == null)
            return;
        if(e.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            ClientJobsData.addXPInfos.update();
            if(!ClientJobsData.addXPInfos.shouldShow())
                return;
            Pair<String, Long> toShow = ClientJobsData.addXPInfos.showJobAtTime();
            if(ClientJobsData.playerJobs.isMax(toShow.getFirst()))
                return;
            GuiGainXP gui = new GuiGainXP(toShow.getFirst(), toShow.getSecond());
            gui.render(e.getMatrixStack(), 0, 0, 0.0f);
        }
    }

    /**
     * Cancels the opening of the regular crafting interface and opens the custom one from the Jobs mod.
     * @param e the Click Event
     */
    @SubscribeEvent
    public void onOpenCraftingTable(RightClickBlock e) {
        if(e.getWorld().getBlockState(e.getPos()).getBlock() == Blocks.CRAFTING_TABLE) {
            e.setCanceled(true);
            if(!e.getWorld().isClientSide) {
                openUpdatedCraftingTable(e.getPlayer(), e.getWorld(), e.getPos());
            }
        }
    }


    /**
     * Opens the custom crafting interface of the Jobs mod.
     * @param player the player opening the interface
     */
    private void openUpdatedCraftingTable(Player player, Level level, BlockPos pos){
        MenuProvider provider = new SimpleMenuProvider((id, inv, p_52231_) ->
                new JobsCraftingMenu(id, inv, ContainerLevelAccess.create(level, pos)),
                new TranslatableComponent("container.crafting"));
        player.openMenu(provider);
        player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
    }
}
