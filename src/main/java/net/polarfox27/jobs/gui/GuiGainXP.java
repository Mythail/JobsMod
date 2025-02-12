package net.polarfox27.jobs.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.polarfox27.jobs.ModJobs;
import net.polarfox27.jobs.data.ClientJobsData;
import net.polarfox27.jobs.util.JobsUtil.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiGainXP extends Gui {

    public static final ResourceLocation TEXTURE = new ResourceLocation(ModJobs.MOD_ID, "textures/gui/gui_gain_xp.png");

    private final String job;
    private final long xp;

    /**
     * Creates the GUI
     * @param job the job for which the player gained xp
     * @param xpAdded the amount of xp the player gained
     */
    public GuiGainXP(String job, long xpAdded) {
        this.job = job;
        this.xp = xpAdded;
    }

    /**
     * Renders the GUI on the in-game GUI
     */
    public void drawScreen() {
    	GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        int render_width = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();

        long xp_progression = ClientJobsData.playerJobs.getXPByJob(job);
        long total = ClientJobsData.JOBS_LEVELS.getXPForLevel(job, ClientJobsData.playerJobs.getLevelByJob(job)+1);
        int width = (int)(150 * ((double)xp_progression /(double)total));

        String title = TextFormatting.WHITE + ClientJobsData.getJobName(job) + " (lvl " + ClientJobsData.playerJobs.getLevelByJob(job) + ") : " +
                TextFormatting.AQUA + "+" + xp + TextFormatting.WHITE + " xp";
        String xpTotal = xp_progression + "/" + total;
        int titleWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(title);
        int xpTotalWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(xpTotal);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        this.drawTexturedModalRect(render_width/2 - 90, 5, 0, 0, 180, 50);//background
        this.drawTexturedModalRect(render_width/2 - 75, 35, 0, 50, 150, 12);//progress background
        this.drawTexturedModalRect(render_width/2 - 75, 35, 0, 62, width, 12);//progressbar
        Minecraft.getMinecraft().fontRenderer.drawString(title, render_width/2 - titleWidth/2, 15, Color.white.getRGB());
        Minecraft.getMinecraft().fontRenderer.drawString(xpTotal, render_width/2 - xpTotalWidth/2, 38, Color.black.getRGB());
        GL11.glPopMatrix();
    }


    public static class GuiAddXpInfos{

        public List<Pair<String, Pair<Long, Long>>> infos = new ArrayList<>(); // Job : xp : millis

        /**
         * Checks if the GUI Gain XP should be rendered on the screen
         * @return true if the GUI should be displayed
         */
        public boolean shouldShow(){
            return !infos.isEmpty();
        }

        /**
         * Gets the data about the xp gained for a specific job
         * @param j the job for which we want the data
         * @return a triplet of data : [Job Name, XP, time remaining]
         */
        private Optional<Pair<String, Pair<Long, Long>>> getJob(String j){
            return infos.stream().filter(p -> p.getFirst().equals(j)).findFirst();
        }

        /**
         * Removes jobs that have not received xp in the last 5 seconds
         */
        public void update(){
            infos = infos.stream()
                         .filter(p -> p.getSecond().getSecond() + 5000 > System.currentTimeMillis())
                         .collect(Collectors.toList());
        }

        /**
         * Receives xp for a job and adds it to the list of job to render in the GUI
         * @param j the job for which the player received xp
         * @param xp the amount of xp received
         */
        public void addXP(String j, long xp)
        {
            long xpToSet = xp;
            long millisToSet = System.currentTimeMillis();
            if(getJob(j).isPresent()){
                xpToSet += getJob(j).get().getSecond().getFirst();
            }
            getJob(j).ifPresent(x -> infos.remove(x));
            infos.add(0, new Pair<>(j, new Pair<>(xpToSet, millisToSet)));
        }

        /**
         * Calculates which of the jobs should be rendered based on the current system millis
         * It cycles through all the jobs to render in 5s
         * @return [Job Name, XP] for the job that should be rendered
         */
        public Pair<String, Long> showJobAtTime(){
            long length = 5000/this.infos.size();
            int i = (int)((System.currentTimeMillis() - infos.get(0).getSecond().getSecond())/length);
            return new Pair<>(infos.get(i).getFirst(), infos.get(i).getSecond().getFirst());
        }
    }

}
