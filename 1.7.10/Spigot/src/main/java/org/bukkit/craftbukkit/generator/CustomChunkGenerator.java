package org.bukkit.craftbukkit.generator;

import java.util.List;
import java.util.Random;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkPosition;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.IProgressUpdate;
import net.minecraft.server.World;
import net.minecraft.server.WorldGenStronghold;
import net.minecraft.server.WorldServer;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class CustomChunkGenerator extends InternalChunkGenerator {
	private final ChunkGenerator generator;
	private final WorldServer world;
	private final Random random;
	private final WorldGenStronghold strongholdGen = new WorldGenStronghold();

	private static class CustomBiomeGrid implements BiomeGrid {
		BiomeBase[] biome;

		@Override
		public Biome getBiome(int x, int z) {
			return CraftBlock.biomeBaseToBiome(biome[z << 4 | x]);
		}

		@Override
		public void setBiome(int x, int z, Biome bio) {
			biome[z << 4 | x] = CraftBlock.biomeToBiomeBase(bio);
		}
	}

	public CustomChunkGenerator(World world, long seed, ChunkGenerator generator) {
		this.world = (WorldServer) world;
		this.generator = generator;

		random = new Random(seed);
	}

	@Override
	public boolean isChunkLoaded(int x, int z) {
		return true;
	}

	@Override
	public Chunk getOrCreateChunk(int x, int z) {
		random.setSeed(x * 341873128712L + z * 132897987541L);

		Chunk chunk;

		// Get default biome data for chunk
		CustomBiomeGrid biomegrid = new CustomBiomeGrid();
		biomegrid.biome = new BiomeBase[256];
		world.getWorldChunkManager().getBiomeBlock(biomegrid.biome, x << 4, z << 4, 16, 16);

		// Try extended block method (1.2+)
		short[][] xbtypes = generator.generateExtBlockSections(world.getWorld(), random, x, z, biomegrid);
		if (xbtypes != null) {
			chunk = new Chunk(world, x, z);

			ChunkSection[] csect = chunk.getSections();
			int scnt = Math.min(csect.length, xbtypes.length);

			// Loop through returned sections
			for (int sec = 0; sec < scnt; sec++) {
				if (xbtypes[sec] == null) {
					continue;
				}
				byte[] secBlkID = new byte[4096]; // Allocate blk ID bytes
				byte[] secExtBlkID = null; // Delay getting extended ID nibbles
				short[] bdata = xbtypes[sec];
				// Loop through data, 2 blocks at a time
				for (int i = 0, j = 0; i < bdata.length; i += 2, j++) {
					short b1 = bdata[i];
					short b2 = bdata[i + 1];
					byte extb = (byte) (b1 >> 8 | b2 >> 4 & 0xF0);

					secBlkID[i] = (byte) b1;
					secBlkID[i + 1] = (byte) b2;

					if (extb != 0) { // If extended block ID data
						if (secExtBlkID == null) { // Allocate if needed
							secExtBlkID = new byte[2048];
						}
						secExtBlkID[j] = extb;
					}
				}
				// Build chunk section
				csect[sec] = new ChunkSection(sec << 4, true, secBlkID, secExtBlkID);
			}
		} else { // Else check for byte-per-block section data
			byte[][] btypes = generator.generateBlockSections(world.getWorld(), random, x, z, biomegrid);

			if (btypes != null) {
				chunk = new Chunk(world, x, z);

				ChunkSection[] csect = chunk.getSections();
				int scnt = Math.min(csect.length, btypes.length);

				for (int sec = 0; sec < scnt; sec++) {
					if (btypes[sec] == null) {
						continue;
					}
					csect[sec] = new ChunkSection(sec << 4, true, btypes[sec], null);
				}
			} else { // Else, fall back to pre 1.2 method
				@SuppressWarnings("deprecation")
				byte[] types = generator.generate(world.getWorld(), random, x, z);
				int ydim = types.length / 256;
				int scnt = ydim / 16;

				chunk = new Chunk(world, x, z); // Create empty chunk

				ChunkSection[] csect = chunk.getSections();

				scnt = Math.min(scnt, csect.length);
				// Loop through sections
				for (int sec = 0; sec < scnt; sec++) {
					ChunkSection cs = null; // Add sections when needed
					byte[] csbytes = null;

					for (int cy = 0; cy < 16; cy++) {
						int cyoff = cy | sec << 4;

						for (int cx = 0; cx < 16; cx++) {
							int cxyoff = cx * ydim * 16 + cyoff;

							for (int cz = 0; cz < 16; cz++) {
								byte blk = types[cxyoff + cz * ydim];

								if (blk != 0) { // If non-empty
									if (cs == null) { // If no section yet, get one
										cs = csect[sec] = new ChunkSection(sec << 4, true);
										csbytes = cs.getIdArray();
									}
									csbytes[cy << 8 | cz << 4 | cx] = blk;
								}
							}
						}
					}
					// If section built, finish prepping its state
					if (cs != null) {
						cs.recalcBlockCounts();
					}
				}
			}
		}
		// Set biome grid
		byte[] biomeIndex = chunk.m();
		for (int i = 0; i < biomeIndex.length; i++) {
			biomeIndex[i] = (byte) (biomegrid.biome[i].id & 0xFF);
		}
		// Initialize lighting
		chunk.initLighting();

		return chunk;
	}

	@Override
	public void getChunkAt(IChunkProvider icp, int i, int i1) {
		// Nothing!
	}

	@Override
	public boolean saveChunks(boolean bln, IProgressUpdate ipu) {
		return true;
	}

	@Override
	public boolean unloadChunks() {
		return false;
	}

	@Override
	public boolean canSave() {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public byte[] generate(org.bukkit.World world, Random random, int x, int z) {
		return generator.generate(world, random, x, z);
	}

	@Override
	public byte[][] generateBlockSections(org.bukkit.World world, Random random, int x, int z, BiomeGrid biomes) {
		return generator.generateBlockSections(world, random, x, z, biomes);
	}

	@Override
	public short[][] generateExtBlockSections(org.bukkit.World world, Random random, int x, int z, BiomeGrid biomes) {
		return generator.generateExtBlockSections(world, random, x, z, biomes);
	}

	@Override
	public Chunk getChunkAt(int x, int z) {
		return getOrCreateChunk(x, z);
	}

	@Override
	public boolean canSpawn(org.bukkit.World world, int x, int z) {
		return generator.canSpawn(world, x, z);
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(org.bukkit.World world) {
		return generator.getDefaultPopulators(world);
	}

	@Override
	public List<?> getMobsFor(EnumCreatureType type, int x, int y, int z) {
		BiomeBase biomebase = world.getBiome(x, z);

		return biomebase == null ? null : biomebase.getMobs(type);
	}

	@Override
	public ChunkPosition findNearestMapFeature(World world, String type, int x, int y, int z) {
		return "Stronghold".equals(type) && strongholdGen != null ? strongholdGen.getNearestGeneratedFeature(world, x, y, z) : null;
	}

	@Override
	public void recreateStructures(int i, int j) {
	}

	@Override
	public int getLoadedChunks() {
		return 0;
	}

	@Override
	public String getName() {
		return "CustomChunkGenerator";
	}

	@Override
	public void c() {
	}
}
