package net.steve.darkfantasy.block.entity;

import net.steve.darkfantasy.entity.custom.GnomeEntity;
import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.init.ModDataComponents;
import net.steve.darkfantasy.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Home block for gnomes, modeled on the beehive's in/out behavior but simplified to an
 * occupant <em>count</em> rather than storing each gnome's full state. Wandering gnomes
 * walk in (see {@code GnomeEntity}'s burrow goal) up to {@link #CAPACITY}; the burrow then
 * emerges one gnome every {@link #RELEASE_INTERVAL} ticks into a free adjacent space. A
 * released gnome is a fresh spawn — acceptable for this mob, and far simpler/safer than
 * serializing entities into the block like the vanilla hive does.
 */
public class GnomeBurrowBlockEntity extends BlockEntity {
    public static final int CAPACITY = 3;
    /** Ticks between emergences while occupied (~30s). */
    private static final int RELEASE_INTERVAL = 600;
    /** Shorter retry when occupied but every emerge spot is blocked. */
    private static final int BLOCKED_RETRY = 100;

    private int occupants = 0;
    private int releaseCooldown = RELEASE_INTERVAL;

    public GnomeBurrowBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GNOME_BURROW_BE.get(), pos, state);
    }

    public boolean isFull() {
        return occupants >= CAPACITY;
    }

    public int getOccupants() {
        return occupants;
    }

    /** A wandering gnome enters the burrow. Returns true if there was room. */
    public boolean tryEnter() {
        if (occupants >= CAPACITY) return false;
        occupants++;
        setChanged();
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    6, 0.2, 0.2, 0.2, 0.01);
            level.playSound(null, worldPosition, SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 0.7F, 0.9F);
        }
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.occupants = input.getIntOr("Occupants", 0);
        this.releaseCooldown = input.getIntOr("ReleaseCooldown", RELEASE_INTERVAL);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Occupants", this.occupants);
        output.putInt("ReleaseCooldown", this.releaseCooldown);
    }

    // ---- Item round-trip: a mined/picked burrow remembers its gnomes ---------

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        int stored = components.getOrDefault(ModDataComponents.GNOME_COUNT.get(), 0);
        this.occupants = Math.max(0, Math.min(CAPACITY, stored));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.GNOME_COUNT.get(), this.occupants);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        // The count travels via the GNOME_COUNT component, not raw block-entity NBT.
        output.discard("Occupants");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GnomeBurrowBlockEntity be) {
        if (be.occupants <= 0) return;
        if (--be.releaseCooldown > 0) return;
        if (!(level instanceof ServerLevel server)) return;

        BlockPos spot = findEmergeSpot(level, pos);
        if (spot == null) {
            be.releaseCooldown = BLOCKED_RETRY; // come back once a space opens up
            return;
        }

        GnomeEntity gnome = ModEntities.GNOME.get().spawn(server, spot, EntitySpawnReason.MOB_SUMMONED);
        be.releaseCooldown = RELEASE_INTERVAL;
        if (gnome == null) return;

        be.occupants--;
        be.setChanged();
        server.sendParticles(ParticleTypes.POOF,
                spot.getX() + 0.5, spot.getY() + 0.2, spot.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.01);
        level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 0.7F, 1.1F);
    }

    /** First air space adjacent to the burrow (above, then horizontally) a gnome can pop out into. */
    private static @Nullable BlockPos findEmergeSpot(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).isAir()) return above;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos cand = pos.relative(d);
            if (level.getBlockState(cand).isAir()) return cand;
        }
        return null;
    }
}
