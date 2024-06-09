package mods.cybercat.gigeresque.common.entity.ai.pathing;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of the vanilla {@link WallClimberNavigation} with some tweaks for smoother pathfinding:
 * <ul>
 *     <li>Patched {@link Path} implementation to use proper rounding</li>
 *     <li>Accessible {@link GroundPathNavigation#getSurfaceY()} override for extensibility</li>
 * </ul>
 * <p>
 * Override {@link Mob#createNavigation(Level)} and return a new instance of this if your entity is a ground-based walking entity
 */
public class SmoothWallClimberNavigation extends WallClimberNavigation implements ExtendedNavigator {
    public SmoothWallClimberNavigation(Mob mob, Level level) {
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
    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
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
}