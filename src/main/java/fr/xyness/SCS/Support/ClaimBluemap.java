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
import com.technicjelle.BMUtils.Cheese;
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
	
	
	// ***************
	// *  Variables  *
	// ***************

	private final BlueMapAPI api;

	private final Map<World, MarkerSet> markerSets = new HashMap<>();

	private final SimpleClaimSystem instance;
	
    
	// ******************
	// *  Constructors  *
	// ******************

	public ClaimBluemap(BlueMapAPI api, SimpleClaimSystem instance) {
		this.api = api;
		this.instance = instance;
		load();
	}

	// ********************
	// *  Others Methods  *
	// ********************

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
		// Get info from the claim
		String hoverText = instance.getSettings().getSetting("bluemap-claim-hover-text")
				.replace("%claim-name%", claim.getName())
				.replace("%owner%", claim.getOwner());
		String markerId = "claim_" + claim.getId();
		MarkerSet markerSet = markerSets.get(claim.getLocation().getWorld());
		if (markerSet == null) return;
		markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(markerId));

		// Get claim coordinates
		Vector2i[] chunkCoordinates = claim.getChunks().stream()
				.map(chunk -> new Vector2i(chunk.getX(), chunk.getZ()))
				.toArray(Vector2i[]::new);
		Collection<Cheese> cheeses = Cheese.createPlatterFromChunks(chunkCoordinates);

		// Get the claim color
		Color fillColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-fill-color"), 16));
		Color strokeColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-border-color"), 16));

		// Create the marker
		AtomicInteger index = new AtomicInteger();
		cheeses.forEach( cheese -> {
			ShapeMarker marker = ShapeMarker.builder()
					.label(hoverText)
					.detail(hoverText)
					.depthTestEnabled(false)
					.shape(cheese.getShape(), 64)
					.holes(cheese.getHoles().toArray(Shape[]::new))
					.fillColor(fillColor)
					.lineColor(strokeColor)
					.lineWidth(5)
					.build();

			markerSet.getMarkers().put(markerId + "_" + index.getAndIncrement(), marker);
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
