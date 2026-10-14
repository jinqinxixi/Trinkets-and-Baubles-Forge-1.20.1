package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 贪婪之眼的寻宝目标。
 *
 * <p>一条目标可以是方块 tag（{@code #forge:ores/coal}）、具体方块（{@code minecraft:chest}）或
 * 实体类型（{@code minecraft:chest_minecart}）；{@code entry} 是配置原文，{@code color} 是标记颜色。
 */
public record TreasureTarget(
        String entry,
        @Nullable TagKey<Block> blockTag,
        @Nullable Block block,
        @Nullable EntityType<?> entityType,
        int color
) {
    private static final int DEFAULT_COLOR = 16766720;

    public boolean isEntity() {
        return this.entityType != null;
    }

    public boolean matches(BlockState state) {
        return this.blockTag != null
                ? state.is(this.blockTag)
                : this.block != null && state.is(this.block);
    }

    public String displayName() {
        if (this.blockTag != null) {
            String path = this.blockTag.location().getPath();
            String name = path.substring(path.lastIndexOf(47) + 1).replace('_', ' ');
            return name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        } else if (this.block != null) {
            // SRG 方法名 m_7705_（即 getDescriptionId）在 String 上下文返回 translation key；
            // PLAN 第 8 节标 UNVERIFIED，此处按「返回 String」采用 getDescriptionId()，待 C 阶段编译确认。
            return this.block.getDescriptionId();
        } else {
            return this.entityType != null ? this.entityType.getDescriptionId() : "";
        }
    }

    public boolean isDisplayNameTranslatable() {
        return this.blockTag == null;
    }

    public static List<TreasureTarget> parseAll(List<? extends String> entries) {
        List<TreasureTarget> targets = new ArrayList<>();

        for (String raw : entries) {
            TreasureTarget target = parse(raw);
            if (target != null) {
                targets.add(target);
            }
        }

        return List.copyOf(targets);
    }

    @Nullable
    public static TreasureTarget parse(String raw) {
        String[] parts = raw.trim().split(";");
        String id = parts[0].trim();
        if (id.isEmpty()) {
            return null;
        } else {
            int color = parts.length > 1 ? parseColor(parts[parts.length - 1].trim(), raw) : DEFAULT_COLOR;
            if (id.startsWith("#")) {
                ResourceLocation tag = ResourceLocation.tryParse(id.substring(1));
                return tag == null ? null : new TreasureTarget(raw, TagKey.create(Registries.BLOCK, tag), null, null, color);
            } else {
                ResourceLocation location = ResourceLocation.tryParse(id);
                if (location == null) {
                    return null;
                } else if (ForgeRegistries.BLOCKS.containsKey(location)) {
                    return new TreasureTarget(raw, null, ForgeRegistries.BLOCKS.getValue(location), null, color);
                } else {
                    return ForgeRegistries.ENTITY_TYPES.containsKey(location)
                            ? new TreasureTarget(raw, null, null, ForgeRegistries.ENTITY_TYPES.getValue(location), color)
                            : null;
                }
            }
        }
    }

    private static int parseColor(String text, String raw) {
        try {
            if (text.startsWith("#")) {
                return Integer.parseInt(text.substring(1), 16);
            } else {
                return !text.startsWith("0x") && !text.startsWith("0X")
                        ? Integer.parseInt(text)
                        : Integer.parseInt(text.substring(2), 16);
            }
        } catch (NumberFormatException e) {
            TrinketsandBaublesMod.LOGGER.error("Invalid color in treasure entry: {}", raw);
            return DEFAULT_COLOR;
        }
    }
}
