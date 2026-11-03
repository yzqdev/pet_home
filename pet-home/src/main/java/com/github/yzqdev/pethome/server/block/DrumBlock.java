package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 指挥鼓：右键把方块状态 {@code command}（0 游走/1 停留/2 跟随）循环 +1，并对 65×65×65 范围内
 * 放置者自己的宠物批量下发当前指令（受击生物短暂发光标记）；也支持红石信号触发 （{@code neighborChanged}）与踩踏音效。放置者 UUID 存在 {@link
 * DrumBlockEntity} 供红石路径取用。
 */
public class DrumBlock extends BaseEntityBlock {

  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
  public static final IntegerProperty COMMAND = IntegerProperty.create("command", 0, 2);
  private static final Random random = new Random();

  public DrumBlock() {
    super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(1F).noOcclusion());
    this.registerDefaultState(
        this.stateDefinition.any().setValue(COMMAND, 0).setValue(POWERED, Boolean.valueOf(false)));
  }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> context) {
    context.add(COMMAND, POWERED);
  }

  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable("tooltips.pet_home.drum.desc").withStyle(ChatFormatting.GREEN));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return null;
  }

  /** 右键（非潜行）：指令循环切换 + 批量下发 + 鼓声；潜行右键放行给原版交互。 */
  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (player.isShiftKeyDown()) {
      return InteractionResult.PASS;
    } else {
      int currentCommand = state.getValue(COMMAND);
      level.setBlockAndUpdate(pos, state.cycle(COMMAND));
      int count = issueCommand(level, pos, currentCommand, player.getUUID());
      if (count > 0) {
        player.displayClientMessage(
            Component.translatable("message.pet_home.drum_command_" + currentCommand, count), true);
      }
      player.playSound(PHSoundRegistry.DRUM, 3, 0.3F + 0.4F * random.nextFloat());
      level.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, pos);
      return InteractionResult.SUCCESS;
    }
  }

  /** 记录放置者 UUID（BE），红石触发时以他为指令 issuer。 */
  public void setPlacedBy(
      Level level,
      BlockPos pos,
      BlockState state,
      @Nullable LivingEntity livingEntity,
      ItemStack stack) {
    BlockEntity blockentity = level.getBlockEntity(pos);
    if (livingEntity != null && blockentity instanceof DrumBlockEntity drum) {
      drum.setPlacerUUID(livingEntity.getUUID());
    }
  }

  /**
   * 对鼓周围 33×33×33 方块范围内的 {@code issuer} 自己的宠物下发指令： 可指挥生物 {@code setCommand}；原版宠物同时同步坐下姿态（指令 0
   * 时不改动原版宠物）； 全部目标获得 3 秒发光。@return 受影响宠物数（右键提示用）
   */
  public int issueCommand(Level level, BlockPos pos, int command, UUID issuer) {
    int count = 0;
    if (issuer != null) {
      Predicate<Entity> tames =
          (animal) ->
              TameableUtils.isTamed((LivingEntity) animal)
                  && TameableUtils.getOwnerUUIDOf(animal) != null
                  && TameableUtils.getOwnerUUIDOf(animal).equals(issuer);

      AABB area = AABB.encapsulatingFullBlocks(pos.offset(-32, -32, -32), pos.offset(32, 32, 32));
      for (Animal animal :
          level.getEntitiesOfClass(Animal.class, area, EntitySelector.NO_SPECTATORS.and(tames))) {
        if (animal instanceof IComandableMob) {
          ((IComandableMob) animal).setCommand(command);
          count++;
        }
        if (animal instanceof TamableAnimal) {
          if (command != 0) {
            ((TamableAnimal) animal).setOrderedToSit(command == 1);
            ((TamableAnimal) animal).setInSittingPose(command == 1);
            if (!(animal instanceof IComandableMob)) {
              count++;
            }
          }
        }
        animal.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
      }
    }
    return count;
  }

  /** 红石上升沿触发一次批量指令（issuer 取放置者 UUID），并维护 {@code POWERED} 状态。 */
  public void neighborChanged(
      BlockState state, Level level, BlockPos pos, Block block, BlockPos pos2, boolean b) {
    boolean flag = level.hasNeighborSignal(pos);
    if (flag != state.getValue(POWERED)) {
      if (flag) {
        UUID uuid = null;
        if (level.getBlockEntity(pos) instanceof DrumBlockEntity drum) {
          uuid = drum.getPlacerUUID();
        }
        this.issueCommand(level, pos, state.getValue(COMMAND), uuid);
        level.playSound(
            null,
            pos,
            PHSoundRegistry.DRUM,
            SoundSource.BLOCKS,
            3,
            0.3F + 0.4F * random.nextFloat());
        level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
      }
      level.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(flag)), 3);
    }
  }

  public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float f) {
    entity.playSound(PHSoundRegistry.DRUM, 3, 0.6F + 0.4F * random.nextFloat());
    super.fallOn(level, state, pos, entity, f);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new DrumBlockEntity(pos, state);
  }
}
