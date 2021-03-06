package wtf.nebula.impl.module.render;

import me.bush.eventbus.annotation.EventListener;
import net.minecraft.src.*;
import wtf.nebula.event.ReloadRendersEvent;
import wtf.nebula.event.RenderBlockEvent;
import wtf.nebula.event.RenderWorldEvent;
import wtf.nebula.event.TickEvent;
import wtf.nebula.impl.module.Module;
import wtf.nebula.impl.module.ModuleCategory;
import wtf.nebula.impl.value.Value;
import wtf.nebula.util.render.ColorUtil;
import wtf.nebula.util.render.RenderUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;

public class Searcher extends Module {
    public static Set<Integer> blocks = new HashSet<>();

    public Searcher() {
        super("Searcher", ModuleCategory.RENDER);
    }

    public final Value<Boolean> tracers = new Value<>("Tracers", false);

    // a cache of every block we need to render
    private final Map<Vec3, Integer> renderBlocks = new ConcurrentHashMap<>();

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        renderBlocks.clear();
    }

    @EventListener
    public void onTick(TickEvent event) {
        renderBlocks.forEach((vec, id) -> {
            if (!blocks.contains(id)) {
                renderBlocks.remove(vec);
            }
        });
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        for (Entry<Vec3, Integer> entry : renderBlocks.entrySet()) {

            Vec3 vec3 = entry.getKey();
            int atPos = mc.theWorld.getBlockId((int) vec3.xCoord, (int) vec3.yCoord, (int) vec3.zCoord);

            // unloaded the block / block is invalid / block has been removed
            if (atPos == 0) {
                continue;
            }

            // block has updated
            if (atPos != entry.getValue()) {
                renderBlocks.remove(vec3);
                continue;
            }

            Block block = Block.blocksList[atPos];

            // getSelectedBoundingBoxFromPool
            AxisAlignedBB bb = block.getSelectedBoundingBoxFromPool(mc.theWorld,
                    (int) vec3.xCoord, (int) vec3.yCoord, (int) vec3.zCoord);

            if (bb != null) {

                glPushMatrix();

                glDisable(GL_ALPHA_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                glDisable(GL_TEXTURE_2D);
                glDisable(GL_DEPTH_TEST);

                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

                glLineWidth(1.5f);

                bb = bb.offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);

                ColorUtil.setColor(0xFFFFFFFF);
                mc.renderGlobal.drawOutlinedBoundingBox(bb);
                ColorUtil.setColor(0x90FFFFFF);
                RenderUtil.drawFilledBoundingBox(bb);

                if (tracers.getValue()) {
                    glBegin(GL_LINES);

                    ColorUtil.setColor(0xFFFFFFFF);

                    glVertex3d(ActiveRenderInfo.objectX, ActiveRenderInfo.objectY, ActiveRenderInfo.objectZ);
                    glVertex3d((vec3.xCoord + 0.5) - RenderManager.renderPosX,
                            (vec3.yCoord + 0.5) - RenderManager.renderPosY,
                            (vec3.zCoord + 0.5) - RenderManager.renderPosZ);

                    glEnd();
                }

                glLineWidth(1.0f);
                glDisable(GL_LINE_SMOOTH);

                glEnable(GL_DEPTH_TEST);
                glEnable(GL_TEXTURE_2D);
                glDisable(GL_BLEND);

                glEnable(GL_ALPHA_TEST);

                glPopMatrix();
            }
        }
    }

    @EventListener
    public void onRenderBlock(RenderBlockEvent event) {
        if (blocks.contains(event.getBlock().blockID)) {
            renderBlocks.put(event.getPos(), event.getBlock().blockID);
        }
    }

    @EventListener
    public void onReloadRenders(ReloadRendersEvent event) {
        renderBlocks.clear();
    }
}
