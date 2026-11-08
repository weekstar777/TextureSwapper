package com.textureswapper;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Swapper {
	private static final String VANILLA = "";

	private Swapper() {
	}

	public static int swap(FabricClientCommandSource source) {
		MinecraftClient client = source.getClient();
		if (client.getOverlay() != null) {
			source.sendError(Text.literal("材质正在加载中，请稍后再试"));
			return 0;
		}

		ResourcePackManager manager = client.getResourcePackManager();
		manager.scanPacks();

		List<String> packIds = manager.getIds().stream()
				.filter(id -> id.startsWith(SwapperPackProvider.ID_PREFIX))
				.toList();
		if (packIds.isEmpty()) {
			source.sendError(Text.literal("textureswapper 文件夹中没有可用的材质包：" + SwapperPackProvider.FOLDER));
			return 0;
		}

		List<String> enabled = new ArrayList<>(manager.getEnabledIds());
		String current = enabled.stream()
				.filter(id -> id.startsWith(SwapperPackProvider.ID_PREFIX))
				.findFirst()
				.orElse(VANILLA);

		List<String> candidates = new ArrayList<>(packIds);
		candidates.add(VANILLA);
		candidates.remove(current);
		String next = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

		enabled.removeIf(id -> id.startsWith(SwapperPackProvider.ID_PREFIX));
		if (!next.equals(VANILLA)) {
			enabled.add(next);
		}
		manager.setEnabledProfiles(enabled);
		client.options.refreshResourcePacks(manager);

		if (next.equals(VANILLA)) {
			source.sendFeedback(Text.literal("已恢复原版材质").formatted(Formatting.GREEN));
		} else {
			source.sendFeedback(Text.literal("已切换材质包：")
					.append(Text.literal(next.substring(SwapperPackProvider.ID_PREFIX.length())).formatted(Formatting.YELLOW))
					.formatted(Formatting.GREEN));
		}
		return 1;
	}
}