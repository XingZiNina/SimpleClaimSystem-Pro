package fr.xyness.SCS.Support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.flowpowered.math.vector.Vector2i;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
<<<<<<< HEAD
import org.bukkit.Chunk;
import org.bukkit.World;

import com.flowpowered.math.vector.Vector2i;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
=======
import org.bukkit.World;


import de.bluecolored.bluemap.api.BlueMapAPI;
>>>>>>> d8179fc0bbc720d32276ff1a971477b739e6cb1d
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import fr.xyness.SCS.SimpleClaimSystem;
import fr.xyness.SCS.Types.Claim;

public class ClaimBluemap {
	
	
	// ***************
	// *  Variables  *
	// ***************
<<<<<<< HEAD

	private final BlueMapAPI api;

	private final Map<World, MarkerSet> markerSets = new HashMap<>();

	private final SimpleClaimSystem instance;
=======
	
	
	/** The BlueMap API instance. */
	private final BlueMapAPI api;
	
	/** A map storing the MarkerSets for each world. */
	private final Map<World, MarkerSet> markerSets = new HashMap<>();
	
    /** Instance of SimpleClaimSystem */
    private final SimpleClaimSystem instance;
>>>>>>> d8179fc0bbc720d32276ff1a971477b739e6cb1d
	
    
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
<<<<<<< HEAD
		// Get info from the claim
=======
	    // Get info from the claim
>>>>>>> d8179fc0bbc720d32276ff1a971477b739e6cb1d
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
<<<<<<< HEAD
		Color fillColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-fill-color"), 16));
		Color strokeColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-border-color"), 16));
=======
	    Color fillColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-fill-color"), 16));
	    Color strokeColor = new Color((int) Long.parseLong("80" + instance.getSettings().getSetting("bluemap-claim-border-color"), 16));
>>>>>>> d8179fc0bbc720d32276ff1a971477b739e6cb1d

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
    	// Get new name
		String t = instance.getSettings().getSetting("bluemap-claim-hover-text")
    			.replace("%claim-name%", claim.getName())
    			.replace("%owner%", claim.getOwner());

		// Get the marker
		String markerId = "claim_" + claim.getId();
		MarkerSet markerSet = markerSets.get(claim.getLocation().getWorld());
		if (markerSet == null) return;
		markerSet.getMarkers().entrySet().stream()
				.filter(ent -> ent.getKey().startsWith(markerId))
				.forEach(ent -> {
					ShapeMarker marker = (ShapeMarker) ent.getValue();
					marker.setLabel(t);
					marker.setDetail(t);
				});
	}

<<<<<<< HEAD
	public void deleteMarker(Set<Chunk> chunks) {
		chunks.parallelStream().forEach(chunk -> {
			String markerId = "chunk_" + chunk.getX() + "_" + chunk.getZ();
			MarkerSet markerSet = markerSets.get(chunk.getWorld());
			if (markerSet == null) return;
			markerSet.remove(markerId);
		});
=======
	/**
	 * Deletes the marker for the specified chunks from the BlueMap.
	 *
	 * @param claim The chunks to delete the marker for.
	 */
	public void deleteMarker(Claim claim) {
		String markerId = "claim_" + claim.getId();
		MarkerSet markerSet = markerSets.get(claim.getLocation().getWorld());
		if (markerSet == null) return;
		markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(markerId));
>>>>>>> d8179fc0bbc720d32276ff1a971477b739e6cb1d
	}
	
}
