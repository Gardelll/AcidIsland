package world.bentobox.acidisland.listeners;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.entity.ai.PaperVanillaGoal;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import java.lang.reflect.Field;
import java.util.Set;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.pathfinder.PathType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import world.bentobox.acidisland.AcidIsland;
import world.bentobox.acidisland.goals.NeverInWaterRandomStrollGoal;
import world.bentobox.acidisland.goals.WaterPanicGoal;

/**
 * Avoid run into water
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-02-26
 */
public class WaterPanic implements Listener {
    private static final Set<EntityType> WHITE_LIST = Set.of(
        EntityType.POLAR_BEAR,
        EntityType.TURTLE,
        EntityType.DROWNED,
        EntityType.GUARDIAN,
        EntityType.ELDER_GUARDIAN,
        EntityType.SNOW_GOLEM
    );

    private final AcidIsland addon;

    public WaterPanic(AcidIsland addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Creature creature) {
                injectMob(creature);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Creature creature) {
            injectMob(creature);
        }
    }

    public void injectMob(Creature creature) {
        if (creature instanceof WaterMob) {
            return;
        }
        EntityType type = creature.getType();
        if (WHITE_LIST.contains(type)) {
            return;
        }
        CraftCreature craftCreature = (CraftCreature) creature;
        net.minecraft.world.entity.PathfinderMob handle = craftCreature.getHandle();
        if (handle == null) {
            return;
        }
        MobGoals mobGoals = Bukkit.getMobGoals();
        Goal<@org.jetbrains.annotations.NotNull Creature> oldWaterAvoidingRandomStrollGoal = mobGoals.getGoal(creature, VanillaGoal.WATER_AVOIDING_RANDOM_STROLL);
        if (oldWaterAvoidingRandomStrollGoal != null) {
            WaterAvoidingRandomStrollGoal oldWaterAvoidingRandomStrollGoalHandle = (WaterAvoidingRandomStrollGoal) ((PaperVanillaGoal<?>) oldWaterAvoidingRandomStrollGoal).getHandle();
            try {
                Field speedModifierField = RandomStrollGoal.class.getDeclaredField("speedModifier");
                Field probabilityField = WaterAvoidingRandomStrollGoal.class.getDeclaredField("probability");
                speedModifierField.setAccessible(true);
                probabilityField.setAccessible(true);
                Double speedModifier = (Double) speedModifierField.get(oldWaterAvoidingRandomStrollGoalHandle);
                Float probability = (Float) probabilityField.get(oldWaterAvoidingRandomStrollGoalHandle);
                mobGoals.removeGoal(creature, oldWaterAvoidingRandomStrollGoal);
                mobGoals.addGoal(creature, 5, new PaperVanillaGoal<>(new NeverInWaterRandomStrollGoal(addon, handle, speedModifier, probability)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        mobGoals.addGoal(creature, 3, new PaperVanillaGoal<>(new WaterPanicGoal(addon, handle, 1)));
        CraftWorld world = handle.level().getWorld();
        if (world.equals(addon.getOverWorld()) || world.equals(addon.getNetherWorld()) || world.equals(addon.getEndWorld())) {
            handle.setPathfindingMalus(PathType.WATER, -1);
        }
    }
}
