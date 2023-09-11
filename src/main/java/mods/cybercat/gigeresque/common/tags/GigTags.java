package mods.cybercat.gigeresque.common.tags;

import mods.cybercat.gigeresque.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;

public class GigTags {

	/* BLOCKS */
	public static final TagKey<Block> ALIEN_REPELLENTS = TagKey.create(Registries.BLOCK, Constants.modResource("alien_repellents"));
	public static final TagKey<Block> DESTRUCTIBLE_LIGHT = TagKey.create(Registries.BLOCK, Constants.modResource("destructible_light"));
	public static final TagKey<Block> ACID_RESISTANT = TagKey.create(Registries.BLOCK, Constants.modResource("acid_resistant"));
	public static final TagKey<Block> DUNGEON_BLOCKS = TagKey.create(Registries.BLOCK, Constants.modResource("dungeon_blocks"));
	public static final TagKey<Block> DUNGEON_STAIRS = TagKey.create(Registries.BLOCK, Constants.modResource("dungeon_stairs"));
	public static final TagKey<Block> WEAK_BLOCKS = TagKey.create(Registries.BLOCK, Constants.modResource("weak_block"));
	public static final TagKey<Block> SPORE_REPLACE = TagKey.create(Registries.BLOCK, Constants.modResource("spore_replace"));
	public static final TagKey<Block> NEST_BLOCKS = TagKey.create(Registries.BLOCK, Constants.modResource("nest_blocks"));

	/* DUNGEONS */
	public static final TagKey<Structure> GIG_EXPLORER_MAPS = TagKey.create(Registries.STRUCTURE, Constants.modResource("gig_explorer_maps"));
	
	/* MOBS */
	public static final TagKey<EntityType<?>> AQUATIC_HOSTS = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("aquaticalienhost"));
	public static final TagKey<EntityType<?>> CLASSIC_HOSTS = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("classicalienhost"));
	public static final TagKey<EntityType<?>> RUNNER_HOSTS = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("runnerhost"));
	public static final TagKey<EntityType<?>> MUTANT_SMALL_HOSTS = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("smallmutanthost"));
	public static final TagKey<EntityType<?>> MUTANT_LARGE_HOSTS = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("largemutanthost"));
	public static final TagKey<EntityType<?>> NEOHOST = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("neohost"));
	public static final TagKey<EntityType<?>> DNAIMMUNE = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("dnaimmune"));
	public static final TagKey<EntityType<?>> FACEHUGGER_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("facehuggerblacklist"));
	public static final TagKey<EntityType<?>> XENO_EXECUTE_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, Constants.modResource("xenoexecuteblacklist"));
	
	/* SPAWN BIOMES */
	public static final TagKey<Biome> EGGSPAWN_BIOMES = TagKey.create(Registries.BIOME, Constants.modResource("eggbiomes"));
	
	/* GAMEEVENT TAGS */
	public static final TagKey<GameEvent> ALIEN_CAN_LISTEN = TagKey.create(Registries.GAME_EVENT, Constants.modResource("alien_can_listen"));
}
