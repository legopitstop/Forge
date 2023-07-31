package com.legopitstop.construction.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DebugStickItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.Collection;

public class WrenchItem extends DebugStickItem {
    public WrenchItem(Item.Properties builder) {
        super(builder);
    }
    private void handleClick(PlayerEntity player, BlockState state, IWorld worldIn, BlockPos pos, boolean rightClick, ItemStack stack) {
        if (player.canUseCommandBlock()) {
            Block block = state.getBlock();
            StateContainer<Block, BlockState> statecontainer = block.getStateContainer();
            Collection<Property<?>> collection = statecontainer.getProperties();
            String s = Registry.BLOCK.getKey(block).toString();
            if (collection.isEmpty()) {
                sendMessage(player, new TranslationTextComponent(this.getTranslationKey() + ".empty", s));
            } else {
                CompoundNBT compoundnbt = stack.getOrCreateChildTag("DebugProperty");
                String s1 = compoundnbt.getString(s);
                Property<?> property = statecontainer.getProperty(s1);
                if (rightClick) {
                    if (property == null) {
                        property = collection.iterator().next();
                    }

                    BlockState blockstate = cycleProperty(state, property, player.isSecondaryUseActive());
                    worldIn.setBlockState(pos, blockstate, 18);
                    sendMessage(player, new TranslationTextComponent(this.getTranslationKey() + ".update", property.getName(), func_195957_a(blockstate, property)));
                } else {
                    property = getAdjacentValue(collection, property, player.isSecondaryUseActive());
                    String s2 = property.getName();
                    compoundnbt.putString(s, s2);
                    sendMessage(player, new TranslationTextComponent(this.getTranslationKey() + ".select", s2, func_195957_a(state, property)));
                }

            }
        }
    }

    private static <T extends Comparable<T>> BlockState cycleProperty(BlockState state, Property<T> propertyIn, boolean backwards) {
        return state.with(propertyIn, getAdjacentValue(propertyIn.getAllowedValues(), state.get(propertyIn), backwards));
    }

    private static <T> T getAdjacentValue(Iterable<T> allowedValues, @Nullable T currentValue, boolean backwards) {
        return (T)(backwards ? Util.getElementBefore(allowedValues, currentValue) : Util.getElementAfter(allowedValues, currentValue));
    }

    private static void sendMessage(PlayerEntity player, ITextComponent text) {
        ((ServerPlayerEntity)player).func_241151_a_(text, ChatType.GAME_INFO, Util.DUMMY_UUID);
    }

    private static <T extends Comparable<T>> String func_195957_a(BlockState state, Property<T> propertyIn) {
        return propertyIn.getName(state.get(propertyIn));
    }
}
