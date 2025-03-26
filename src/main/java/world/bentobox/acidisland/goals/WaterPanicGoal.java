package world.bentobox.acidisland.goals;

import io.papermc.paper.util.MCUtil;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.Nullable;
import world.bentobox.acidisland.AcidIsland;

/**
 * WaterPanicGoal
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-02-26 03:40
 */
public class WaterPanicGoal extends FleeSunGoal {
    private final AcidIsland addon;

    public WaterPanicGoal(AcidIsland addon, PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);
        this.addon = addon;
    }

    @Override
    public boolean canUse() {
        CraftWorld world = mob.level().getWorld();
        if (!(world.equals(addon.getOverWorld()) || world.equals(addon.getNetherWorld()) || world.equals(addon.getEndWorld()))) {
            return false;
        }
        Location location = MCUtil.toLocation(mob.level(), mob.blockPosition());
        BlockState blockState = location.getBlock().getState();
        Material type = blockState.getType();
        if ((mob.isInWater() || type == Material.WATER_CAULDRON) && !mob.isInBubbleColumn()) {
            return setWantedPos();
        }

        return (type.equals(Material.SNOW) || type.equals(Material.POWDER_SNOW) || type.equals(Material.POWDER_SNOW_CAULDRON))
            && addon.getSettings().isAcidDamageSnow()
            && setWantedPos();
    }

    @Override
    protected @Nullable Vec3 getHidePos() {
        Optional<BlockPos> closestLand = BlockPos.findClosestMatch(this.mob.blockPosition(), 15, 9,
            blockPos -> blockIsSafe(MCUtil.toLocation(mob.level(), blockPos).getBlock()));
        return closestLand.map(Vec3::atBottomCenterOf).orElseGet(() -> DefaultRandomPos.getPos(this.mob, 10, 7));
    }

    private boolean blockIsSafe(Block block) {
        Block upperBlock = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
        Block upper2Block = block.getWorld().getBlockAt(block.getX(), block.getY() + 2, block.getZ());
        Block bellowBlock = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
        return bellowBlock.isSolid() && !blockIsWater(bellowBlock) && block.getType() == Material.AIR && upperBlock.getType() == Material.AIR && upper2Block.getType() == Material.AIR;
    }

    private boolean blockIsWater(Block block) {
        BlockState blockState = block.getState();
        Material type = blockState.getType();
        if (type == Material.WATER || type == Material.WATER_CAULDRON
            || addon.getSettings().isAcidDamageSnow()
            && (type.equals(Material.SNOW) || type.equals(Material.POWDER_SNOW) || type.equals(Material.POWDER_SNOW_CAULDRON))) {
            return true;
        }
        BlockData blockData = blockState.getBlockData();
        if (blockData instanceof Waterlogged) {
            return ((Waterlogged) blockData).isWaterlogged();
        }
        return false;
    }
}
