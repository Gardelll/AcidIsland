package world.bentobox.acidisland.goals;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.CraftWorld;
import world.bentobox.acidisland.AcidIsland;

/**
 * WaterAvoidingRandomStrollGoal
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-02-26 02:42
 */
public class NeverInWaterRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
    private final AcidIsland addon;

    public NeverInWaterRandomStrollGoal(AcidIsland addon, PathfinderMob mob, double speedModifier, float probability) {
        super(mob, speedModifier, probability);
        this.addon = addon;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        CraftWorld world = mob.level().getWorld();
        if (!(world.equals(addon.getOverWorld()) || world.equals(addon.getNetherWorld()) || world.equals(addon.getEndWorld()))) {
            return super.getPosition();
        }
        Vec3 pos = LandRandomPos.getPos(this.mob, 15, 7);
        return pos == null ? DefaultRandomPos.getPos(this.mob, 10, 7) : pos;
    }
}