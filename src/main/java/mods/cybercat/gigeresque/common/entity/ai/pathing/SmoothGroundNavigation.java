package mods.cybercat.gigeresque.common.entity.ai.pathing;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of the vanilla {@link GroundPathNavigation} with some tweaks for smoother pathfinding:
 * <ul>
 *     <li>Smoothed unit rounding to better accommodate edge-cases</li>
 *     <li>Patched {@link Path} implementation to use proper rounding</li>
 *     <li>Skip to vertical traversal first before continuing path nodes if appropriate</li>
 *     <li>Accessible {@link GroundPathNavigation#getSurfaceY()} override for extensibility</li>
 * </ul>
 * <p>
 * Override {@link Mob#createNavigation(Level)} and return a new instance of this if your entity is a ground-based walking entity
 * @see ExtendedNavigator#canPathOnto
 * @see ExtendedNavigator#canPathInto
 */
public class SmoothGroundNavigation extends GroundPathNavigation implements ExtendedNavigator {
    public SmoothGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public Mob getMob() {
        return this.mob;
    }

    @Nullable
    @Override
    public Path getPath() {
        return super.getPath();
    }

    /**
     * Patch {@link Path#getEntityPosAtNode} to use a proper rounding check
     */
    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected void followThePath() {
        final Vec3 safeSurfacePos = getTempMobPos();
        final int shortcutNode = getClosestVerticalTraversal(Mth.floor(safeSurfacePos.y));
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75f ? this.mob.getBbWidth() / 2f : 0.75f - this.mob.getBbWidth() / 2f;

        if (!attemptShortcut(shortcutNode, safeSurfacePos)) {
            if (isCloseToNextNode(0.5f) || isAboutToTraverseVertically() && isCloseToNextNode(getMaxDistanceToWaypoint()))
                this.path.advance();
        }

        doStuckDetection(safeSurfacePos);
    }

    /**
     * Helper override to allow end-users to modify the fluids an entity can swim in
     * <p>
     * If using this to modify swimmable fluids, ensure you also override {@link PathNavigation#canUpdatePath()} as well
     *
     * @return The nearest safe surface height for the entity
     */
    @Override
    public int getSurfaceY() {
        return super.getSurfaceY();
    }

    /**
     * Find the nearest node in the path that accounts for a vertical traversal (either up or down)
     * <p>
     * This can then be used to test if a collision-free traversal can be made, skipping the intermediate nodes as appropriate
     *
     * @param safeSurfaceHeight The baseline floored y-pos of where the mob should traverse to (usually the nearest ground pos or surface of the fluid it's submerged in)
     *
     * @return The node index for the nearest node representing a vertical traversal
     */
    protected int getClosestVerticalTraversal(int safeSurfaceHeight) {
        final int nodesLength = this.path.getNodeCount();

        for (int nodeIndex = this.path.getNextNodeIndex(); nodeIndex < nodesLength; nodeIndex++) {
            if (this.path.getNode(nodeIndex).y != safeSurfaceHeight)
                return nodeIndex;
        }

        return nodesLength;
    }
}