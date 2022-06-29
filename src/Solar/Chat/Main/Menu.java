package Solar.Chat.Main;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import net.md_5.bungee.api.ChatColor;

public class Menu implements Listener{

	public static void openMenu(Player player) {
		Inventory inv = Bukkit.createInventory(null, 9, ChatColor.BLUE + "Cài Đặt");
		inv.setItem(0, disableMentionButtion(player));
		inv.setItem(4, nomesssage(player));
		while(inv.firstEmpty() != -1){
			inv.setItem(inv.firstEmpty(), nullGlass());
		}
		player.openInventory(inv);

	}

	private static ItemStack nullGlass() {
		ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE , 1 , (short) 14);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(" ");
		is.setItemMeta(im);
				return is;
	}

	private static ItemStack nomesssage(Player player) {
		Essentials ess = (Essentials) Main.getMain().getServer().getPluginManager().getPlugin("Essentials");
		User sender = ess.getUser(player);
		ItemStack is;
		if (sender.isTeleportEnabled()) {
			is = new ItemStack(Material.WOOL, 1, (short) 5);
			ItemMeta im = is.getItemMeta();
			im.setLore(Arrays
					.asList(new String[] { ChatColor.RED + "Bấm vào để không cho phép người khác dịch chuyển đến" }));
			im.setDisplayName(ChatColor.GREEN + "Cho phép gửi lời mời dịch chuyển");
			is.setItemMeta(im);
		} else {
			is = new ItemStack(Material.WOOL, 1, (short) 14);

			ItemMeta im = is.getItemMeta();
			im.setLore(Arrays
					.asList(new String[] { ChatColor.GREEN + "Bấm vào để cho phép người khác dịch chuyển đến" }));
			im.setDisplayName(ChatColor.RED + "Không phép gửi lời mời dịch chuyển");
			is.setItemMeta(im);

		}
		return is;
	}

	private static ItemStack disableMentionButtion(Player player) {
		ItemStack is;

		if (Main.isMentionable(player)) {
			is = new ItemStack(Material.WOOL, 1, (short) 5);
			ItemMeta im = is.getItemMeta();
			im.setLore(Arrays
					.asList(new String[] { ChatColor.RED + "Bấm vào để tắt thông báo khi được nhắc đến trong chat" }));
			im.setDisplayName(ChatColor.GREEN + "Thông Báo khi được nhắc đến");
			is.setItemMeta(im);
		} else {
			is = new ItemStack(Material.WOOL, 1, (short) 14);

			ItemMeta im = is.getItemMeta();
			im.setLore(Arrays
					.asList(new String[] { ChatColor.GREEN + "Bấm vào để mở thông báo khi được nhắc đến trong chat" }));
			im.setDisplayName(ChatColor.RED + "Không Thông Báo khi được nhắc đến");
			is.setItemMeta(im);
		}

		return is;

	}
	@EventHandler
	public void playerClick(InventoryClickEvent e){
		if (e.getClickedInventory() == null) {

			return;
		}
		if (e.getCurrentItem().getType() == Material.AIR) {
			return;

		}
		if(e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Cài Đặt")){
			Player player = (Player) e.getWhoClicked();
			e.setCancelled(true);
			if(e.getCurrentItem().getType() == Material.WOOL && e.getCurrentItem().getDurability() == 5){
				if(e.getCurrentItem().hasItemMeta()){
					if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Thông Báo khi được nhắc đến")){
						if(Main.isMentionable(player)){
							Main.setMentionable(player, false);
							openMenu(player);
						}
					}
					if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Cho phép gửi lời mời dịch chuyển")){
						Essentials ess = (Essentials) Main.getMain().getServer().getPluginManager().getPlugin("Essentials");
						User sender = ess.getUser(player);
						if(sender.isTeleportEnabled()){
							sender.setTeleportEnabled(false);
							openMenu(player);
						}
					}
				}
			}
			if(e.getCurrentItem().getType() == Material.WOOL && e.getCurrentItem().getDurability() == 14){
				if(e.getCurrentItem().hasItemMeta()){
					if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Không Thông Báo khi được nhắc đến")){
						if(!Main.isMentionable(player)){
							Main.setMentionable(player, true);
							openMenu(player);
						}
					}if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Không phép gửi lời mời dịch chuyển")){
						Essentials ess = (Essentials) Main.getMain().getServer().getPluginManager().getPlugin("Essentials");
						User sender = ess.getUser(player);
						if(!sender.isTeleportEnabled()){
							sender.setTeleportEnabled(true);
							openMenu(player);
						}
					}
				}
			}
		}
	}

}
