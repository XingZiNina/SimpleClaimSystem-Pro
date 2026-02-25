package fr.xyness.SCS.Support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import fr.xyness.SCS.SimpleClaimSystem;
import fr.xyness.SCS.Types.Claim;

public class ClaimBluemap {

	private final BlueMapAPI api;
	private final Map<World, MarkerSet> markerSets = new HashMap<>();
	private final SimpleClaimSystem instance;

	public ClaimBluemap(BlueMapAPI api, SimpleClaimSystem instance) {
		this.api = api;
		this.instance = instance;
		load();
	}
	public void load() {
		Set<Claim> claims = instance.getMain().getAllClaims();
		instance.executeAsync(() -> {
			for (World w : Bukkit.getWorlds()) {
				MarkerSet markerSet = MarkerSet.builder()
						.label("Claims")
						.build();
				markerSets.put(w, markerSet);
				for(Claim claim : claims) {
					if (claim.getLocation().getWorld().equals(w)) {
						createClaimZone(claim);
					}
				}
				api.getWorld(w).ifPresent(world -> {
					for (BlueMapMap map : world.getMaps()) {
						map.getMarkerSets().put("Claims", markerSet);
					}
				});
			}
		});
		instance.getLogger().info("Claims added to BlueMap.");
	}
	public void createClaimZone(Claim claim) {
		try {
			createClaimZoneInternal(claim);
		} catch (NoClassDefFoundError e) {
			instance.getLogger().severe("BMUtils library is missing! Cannot create BlueMap markers. Please check your build configuration (shadowJar).");
		} catch (Exception e) {
			instance.getLogger().severe("Error creating claim zone for '" + claim.getName() + "': " + e.getMessage());
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createClaimZoneInternal(Claim claim) throws Exception {
		String hoverText = instance.getSettings().getSetting("bluemap-claim-hover-text")
				.replace("%claim-name%", claim.getName())
				.replace("%owner%", claim.getOwner());
		String markerId = "claim_" + claim.getId();
		MarkerSet markerSet = markerSets.get(claim.getLocation().getWorld());
		if (markerSet == null) return;
		markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(markerId));

		Vector2i[] chunkCoordinates = claim.getChunks().stream()
				.map(chunk -> new Vector2i(chunk.getX(), chunk.getZ()))
				.toArray(Vector2i[]::new);

		Class<?> cheeseClass = Class.forName("com.technicjelle.BMUtils.Cheese");
		java.lang.reflect.Method createPlatterMethod = cheeseClass.getMethod("createPlatterFromChunks", Vector2i[].class);
		Collection cheeses = (Collection) createPlatterMethod.invoke(null, (Object) chunkCoordinates);
		Color fillColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-fill-color"), 16));
		Color strokeColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-border-color"), 16));
		AtomicInteger index = new AtomicInteger();
		cheeses.forEach(cheese -> {
			try {
				Class<?> cheeseObjClass = cheese.getClass();
				Object shape = cheeseObjClass.getMethod("getShape").invoke(cheese);
				Collection holes = (Collection) cheeseObjClass.getMethod("getHoles").invoke(cheese);
				Shape[] holesArray = ((Collection<Shape>) holes).toArray(new Shape[0]);
				ShapeMarker marker = ShapeMarker.builder()
						.label(hoverText)
						.detail(hoverText)
						.depthTestEnabled(false)
						.shape((Shape) shape, 64)
						.holes(holesArray)
						.fillColor(fillColor)
						.lineColor(strokeColor)
						.lineWidth(5)
						.build();
				markerSet.getMarkers().put(markerId + "_" + index.getAndIncrement(), marker);
			} catch (Exception e) {
				instance.getLogger().warning("Failed to create marker shape: " + e.getMessage());
			}
		});
	}
	public void updateName(Claim claim) {
		String t = instance.getSettings().getSetting("bluemap-claim-hover-text")
				.replace("%claim-name%", claim.getName())
				.replace("%owner%", claim.getOwner());
		claim.getChunks().forEach(chunk -> {
			String markerId = "chunk_" + chunk.getX() + "_" + chunk.getZ();
			MarkerSet markerSet = markerSets.get(chunk.getWorld());
			if (markerSet == null) return;
			ExtrudeMarker marker = (ExtrudeMarker) markerSet.get(markerId);
			if (marker != null) {
				marker.setLabel(t);
				marker.setDetail(t);
			}
		});
	}
	public void deleteMarker(Set<Chunk> chunks) {
		chunks.parallelStream().forEach(chunk -> {
			String markerId = "chunk_" + chunk.getX() + "_" + chunk.getZ();
			MarkerSet markerSet = markerSets.get(chunk.getWorld());
			if (markerSet == null) return;
			markerSet.remove(markerId);
		});
	}
}