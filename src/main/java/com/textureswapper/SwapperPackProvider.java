package com.textureswapper;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Exposes the packs in the textureswapper folder to the client resource pack manager. */
public class SwapperPackProvider implements ResourcePackProvider {
	public static final String ID_PREFIX = "textureswapper/";
	public static final Path FOLDER = FabricLoader.getInstance().getGameDir().resolve("textureswapper");
	private static final Logger LOGGER = LoggerFactory.getLogger("TextureSwapper");
	private static final ResourcePackSource SOURCE = ResourcePackSource.create(
			name -> Text.translatable("pack.nameAndSource", name, Text.literal("TextureSwapper")).formatted(Formatting.GRAY),
			true);
	private static final ResourcePackPosition POSITION =
			new ResourcePackPosition(false, ResourcePackProfile.InsertionPosition.TOP, false);

	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		for (Path pack : listPacks()) {
			String name = pack.getFileName().toString();
			ResourcePackInfo info = new ResourcePackInfo(ID_PREFIX + name, Text.literal(name), SOURCE, Optional.empty());
			ResourcePackProfile.PackFactory factory = Files.isDirectory(pack)
					? new DirectoryResourcePack.DirectoryBackedFactory(pack)
					: new ZipResourcePack.ZipBackedFactory(pack);
			ResourcePackProfile profile = ResourcePackProfile.create(info, factory, ResourceType.CLIENT_RESOURCES, POSITION);
			if (profile != null) {
				consumer.accept(profile);
			}
		}
	}

	private static List<Path> listPacks() {
		try {
			Files.createDirectories(FOLDER);
			try (Stream<Path> stream = Files.list(FOLDER)) {
				return stream.filter(p -> Files.isDirectory(p)
								? Files.isRegularFile(p.resolve("pack.mcmeta"))
								: p.getFileName().toString().toLowerCase().endsWith(".zip"))
						.sorted()
						.toList();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to read {}", FOLDER, e);
			return List.of();
		}
	}
}