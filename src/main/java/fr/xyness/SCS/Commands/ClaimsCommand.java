package fr.xyness.SCS.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import fr.xyness.SCS.SimpleClaimSystem;
import fr.xyness.SCS.Guis.ClaimsGui;
import fr.xyness.SCS.Guis.Bedrock.BClaimsGui;
import fr.xyness.SCS.Types.CPlayer;


public class ClaimsCommand implements CommandExecutor {
	
	
    // ***************
    // *  Variables  *
    // ***************

    private SimpleClaimSystem instance;

    // ******************
    // *  Constructors  *
    // ******************

    public ClaimsCommand(SimpleClaimSystem instance) {
    	this.instance = instance;
    }
    
    
    // ******************
    // *  Main command  *
    // ******************

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
        	sender.sendMessage(instance.getLanguage().getMessage("command-only-by-players"));
            return false;
        }

        // Get data
        Player player = (Player) sender;
        CPlayer cPlayer = instance.getPlayerMain().getCPlayer(player.getUniqueId());
        if (cPlayer == null) {
            player.sendMessage(instance.getLanguage().getMessage("error"));
            return true;
        }
        // Open the claims GUI for the player
        cPlayer.setGuiPage(1);
        if(instance.getSettings().getBooleanSetting("floodgate")) {
        	if(FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
        		new BClaimsGui(player,instance,"all");
        		return true;
        	}
        }
        new ClaimsGui(player, 1, "all",instance);
        
        return true;
    }

}
