package slimeknights.tconstruct.library.modifiers.fluid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;

import java.util.Map;
import java.util.Optional;

/**
 * Breaks a block using a fluid.
 * Enchantments are stored by ResourceLocation for compatibility with 1.21 data-driven enchantments.
 * They are resolved to Holder<Enchantment> at runtime when the enchantment registry is available.
 */
public record BreakBlockFluidEffect(float hardness, Map<ResourceLocation,Integer> enchantments) implements FluidEffect<FluidEffectContext.Block> {
  public static final RecordLoadable<BreakBlockFluidEffect> LOADER = RecordLoadable.create(
    FloatLoadable.FROM_ZERO.defaultField("hardness", 0f, false, BreakBlockFluidEffect::hardness),
    Loadables.RESOURCE_LOCATION.mapWithValues(IntLoadable.FROM_ONE, 0).defaultField("enchantments", Map.of(), BreakBlockFluidEffect::enchantments),
    BreakBlockFluidEffect::new);

  public BreakBlockFluidEffect(float hardness) {
    this(hardness, Map.of());
  }

  /** Constructor accepting a ResourceKey for datagen compatibility with 1.21 data-driven enchantments */
  public BreakBlockFluidEffect(float hardness, ResourceKey<Enchantment> enchantmentKey, int level) {
    this(hardness, Map.of(enchantmentKey.location(), level));
  }

  @Override
  public RecordLoadable<BreakBlockFluidEffect> getLoader() {
    return LOADER;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, FluidEffectContext.Block context, FluidAction action) {
    // compare our hardness to the block's hardness
    BlockState state = context.getBlockState();
    if (state.isAir()) {
      return 0;
    }
    Level world = context.getLevel();
    BlockPos pos = context.getBlockPos();
    float requirement = state.getDestroySpeed(world, pos);
    if (requirement < 0) {
      return 0;
    }

    // disallow acting if adventure mode and no proper item stack tags
    if (context.breakRestricted()) {
      return 0;
    }

    // 0 hardness means break any block, ignoring hardness
    if (hardness == 0) {
      requirement = 1;
    } else {
      requirement /= hardness;
    }
    // if we had enough level to destroy it, return how much fluid we used
    if (requirement <= level.value()) {
      if (action.execute() && world instanceof ServerLevel server) {
        // handle enchantments by making a fake items stack
        // actual item identity doesn't matter, we are past the point of asking if we can break it
        ItemStack fakeTool = ItemStack.EMPTY;
        if (!enchantments.isEmpty()) {
          fakeTool = new ItemStack(Items.STICK);
          // in 1.21, enchantments are data-driven, resolve from registry and apply via updateEnchantments
          var enchantmentRegistry = server.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
          final ItemStack finalFakeTool = fakeTool;
          EnchantmentHelper.updateEnchantments(finalFakeTool, mutable -> {
            for (Map.Entry<ResourceLocation, Integer> entry : enchantments.entrySet()) {
              ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, entry.getKey());
              Optional<Holder.Reference<Enchantment>> holder = enchantmentRegistry.getHolder(key);
              holder.ifPresent(ref -> mutable.set(ref, entry.getValue()));
            }
          });
        }

        // ensures tile entity is fetched so its around for afterBlockBreak
        BlockEntity te = world.getBlockEntity(pos);
        Block block = state.getBlock();

        // remove the block
        Player player = context.getPlayer();
        boolean removed;
        if (player != null) {
          removed = state.onDestroyedByPlayer(world, pos, player, true, world.getFluidState(pos));
          if (removed) {
            player.awardStat(Stats.BLOCK_MINED.get(block));
          }
        } else {
          removed = world.setBlock(pos, world.getFluidState(pos).createLegacyBlock(), 3);
        }

        // drop resources
        if (removed) {
          state.getBlock().destroy(world, pos, state);

          // determine who to blame for this block breaking, projectile or original entity
          Entity source = context.getProjectile();
          if (source == null) {
            source = context.getEntity();
          }
          LootParams.Builder lootParams = new Builder(server)
            .withParameter(LootContextParams.ORIGIN, context.getHitResult().getLocation())
            .withParameter(LootContextParams.TOOL, fakeTool)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, te)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, source);
          state.getDrops(lootParams).forEach(stack -> Block.popResource(world, pos, stack));
          state.spawnAfterBreak(server, pos, fakeTool, player != null);
          world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        }
      }
      return requirement;
    }
    return 0;
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    String translationKey = FluidEffect.getTranslationKey(getLoader());
    if (enchantments.isEmpty()) {
      if (hardness == 0) {
        return Component.translatable(translationKey);
      }
      return Component.translatable(translationKey + ".hardness", hardness);
    } else {
      translationKey += ".enchanted";
      var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
      Component enchantments = enchantments().entrySet().stream().<Component>map(entry -> {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, entry.getKey());
        Optional<Holder.Reference<Enchantment>> holder = enchantmentRegistry.getHolder(key);
        if (holder.isPresent()) {
          Enchantment enchantment = holder.get().value();
          MutableComponent component = enchantment.description().copy();
          if (enchantment.getMaxLevel() != 1) {
            component.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + entry.getValue()));
          }
          return component;
        }
        // fallback: use the resource location as description
        return (Component) Component.literal(entry.getKey().toString());
      }).reduce(MERGE_COMPONENT_LIST).orElse(Component.empty());
      if (hardness == 0) {
        return Component.translatable(translationKey, enchantments);
      }
      return Component.translatable(translationKey + ".hardness", hardness, enchantments);
    }
  }
}
