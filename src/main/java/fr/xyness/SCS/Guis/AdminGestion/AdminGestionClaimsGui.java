package fr.xyness.SCS.Guis.AdminGestion;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import fr.xyness.SCS.*;
import fr.xyness.SCS.Types.CPlayer;

public class AdminGestionClaimsGui implements InventoryHolder {


    // ***************
    // *  Variables  *
    // ***************


    /** The inventory for this GUI. */
    private final Inventory inv;

    /** Instance of SimpleClaimSystem */
    private SimpleClaimSystem instance;


    // ******************
    // *  Constructors  *
    // ******************

    public AdminGestionClaimsGui(Player player, int page, String filter, SimpleClaimSystem instance) {
        this.instance = instance;
        inv = Bukkit.createInventory(this, 54, "§4[A]§r Claims (Page "+String.valueOf(page)+")");
        loadItems(player, page, filter).thenAccept(success -> {
                    if (success) {
                        instance.executeEntitySync(player, () -> player.openInventory(inv));
                    } else {
                        instance.executeEntitySync(player, () -> player.sendMessage(instance.getLanguage().getMessage("error")));
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    // ********************
    // *  Others Methods  *
    // ********************

    private CPlayer getOrCreateCPlayer(Player player) {
        CPlayer cPlayer = instance.getPlayerMain().getOrCreateCPlayer(player);
        if (cPlayer == null) {
            instance.getLogger().severe("Failed to get or create CPlayer for player: " +
                    player.getName() + " (UUID: " + player.getUniqueId() + ")");
            throw new IllegalStateException("CPlayer creation failed for player: " + player.getName());
        }

        return cPlayer;
    }

    private CompletableFuture<Boolean> loadItems(Player player, int page, String filter) {

        return CompletableFuture.supplyAsync(() -> {

            try {
                CPlayer cPlayer = getOrCreateCPlayer(player);
                if (cPlayer == null) {
                    instance.getLogger().severe("CPlayer is null after getOrCreateCPlayer for: " + player.getName());
                    return false;
                }
                cPlayer.setFilter(filter);
                cPlayer.clearMapString();
                cPlayer.setGuiPage(page);
                inv.setItem(48, backPage(page - 1, !(page > 1)));
                Map<String, Integer> owners = getOwnersByFilter(filter);
                LinkedHashMap<String, Integer> sortedOwners = owners.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));
                inv.setItem(49, createFilterItem(filter));
                int maxSlot = 44;
                int minSlot = 0;
                int itemsPerPage = maxSlot - minSlot + 1;
                int startItem = (page - 1) * itemsPerPage;
                int i = minSlot;
                int count = 0;
                for (Map.Entry<String, Integer> entry : sortedOwners.entrySet()) {
                    if (count++ < startItem) continue;
                    if (i > maxSlot) {
                        inv.setItem(50, nextPage(page + 1));
                        break;
                    }

                    String owner = entry.getKey();
                    int claimAmount = entry.getValue();
                    List<String> lore = new ArrayList<>(getLore("§7Claims: §b"+instance.getMain().getNumberSeparate(String.valueOf(claimAmount))+"\n \n§c[Left-click]§7 to display their claims\n§c[Shift-left-click]§7 to remove all their claims"));
                    cPlayer.addMapString(i, owner);
                    inv.setItem(i, createOwnerClaimItem(owner, lore));
                    i++;
                }

                return true;

            } catch (Exception e) {
                instance.getLogger().severe("Error loading items in AdminGestionClaimsGui: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

        });

    }
    private Map<String, Integer> getOwnersByFilter(String filter) {
        switch (filter) {
            case "sales":
                return instance.getMain().getClaimsOwnersWithSales();
            case "online":
                return instance.getMain().getClaimsOnlineOwners();
            case "offline":
                return instance.getMain().getClaimsOfflineOwners();
            default:
                return instance.getMain().getClaimsOwnersGui();
        }
    }
    private ItemStack createOwnerClaimItem(String owner, List<String> lore) {
        String title = "§e"+owner;
        return createPlayerHeadItem(owner, title, lore);
    }
    private ItemStack createPlayerHeadItem(String owner, String title, List<String> lore) {
        ItemStack item = instance.getPlayerMain().getPlayerHead(owner);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material != null ? material : Material.STONE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta = instance.getGuis().setItemFlag(meta);
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack createFilterItem(String filter) {
        String loreFilter = "§7Change filter\n%status_color_1%➲ All owners\n%status_color_2%➲ Owners with claims in sale\n%status_color_3%➲ Online owners\n%status_color_4%➲ Offline owners\n§7▸ §fClick to change"
                .replaceAll("%status_color_" + getStatusIndex(filter) + "%", "§a")
                .replaceAll("%status_color_[^" + getStatusIndex(filter) + "]%", "§8");
        return createItem(Material.END_CRYSTAL, "§eFilter", getLore(loreFilter));
    }
    private int getStatusIndex(String filter) {
        switch (filter) {
            case "sales":
                return 2;
            case "online":
                return 3;
            case "offline":
                return 4;
            default:
                return 1;
        }
    }

    private ItemStack backPage(int page, boolean back) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cPrevious page");
            meta.setLore(Arrays.asList(back ? "§7Go back to admin main menu" : "§7Go to the page "+String.valueOf(page),"§7▸ §fClick to access"));
            meta = instance.getGuis().setItemFlag(meta);
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack nextPage(int page) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cNext page");
            meta.setLore(Arrays.asList("§7Go to the page "+String.valueOf(page),"§7▸ §fClick to access"));
            meta = instance.getGuis().setItemFlag(meta);
            item.setItemMeta(meta);
        }
        return item;
    }
    public List<String> getLore(String lore) {
        return Arrays.asList(lore.split("\n"));
    }
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
