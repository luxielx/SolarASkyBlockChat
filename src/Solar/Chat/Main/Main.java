package Solar.Chat.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.wasteofplastic.askyblock.ASkyBlockAPI;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin implements Listener {
	static Main main;
	private Plugin asb;
	HashMap<UUID, Long> timers = new HashMap<>();
	static ArrayList<OfflinePlayer> disableMention = new ArrayList<>();
	
	static int chattime = 3;
	FileConfiguration config;
	public static long cooldown(int level) {

		return (long) (1000 * (chattime));

	}

	public static boolean isMentionable(Player player) {
		if (disableMention.contains(player)) {
			return false;
		} else {
			return true;
		}
	}

	public static void setMentionable(Player player, boolean bl) {
		if (bl) {

			if (disableMention.contains(player)) {
				disableMention.remove(player);
			}
		} else {

			if (!disableMention.contains(player)) {
				disableMention.add(player);
			}
		}
	}

	public boolean cooldown(int level, Player player) {
		if (!timers.containsKey(player.getUniqueId())) {
			timers.put(player.getUniqueId(), 0l);
		}

		if (System.currentTimeMillis() - timers.get(player.getUniqueId()) < cooldown(level)) {

			player.sendMessage(ChatColor.GREEN + "Bạn cần đợi " + ChatColor.RED
					+ ((cooldown(level) - System.currentTimeMillis() + timers.get(player.getUniqueId())) / 1000 + 1)
					+ ChatColor.GREEN + " giây Để chat tiếp");
			return true;
		}

		return false;
	}

	@Override
	public void onEnable() {

		config = getConfig();
		config.options().copyDefaults(true);

		saveConfig();
		main = this;
		PluginManager manager = getServer().getPluginManager();
		asb = manager.getPlugin("ASkyBlock");
		getServer().getPluginManager().registerEvents(this, this);
		if (asb == null) {
			getLogger().severe("ASkyBlock not loaded. Disabling plugin");
			getServer().getPluginManager().disablePlugin(this);
		}
		Bukkit.getServer().getPluginManager().registerEvents(new Menu(), this);
		if (config.getConfigurationSection("name.") != null) {
			for (String u : config.getConfigurationSection("name.").getKeys(false)) {

				UUID uuid = UUID.fromString(u);
				OfflinePlayer player = Bukkit.getPlayer(uuid);
				if (player != null)

					disableMention.add(player);
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chattime") && sender.hasPermission("skyblock.staff") && args.length > 0) {

			Main.chattime = Integer.parseInt(args[0]);

			sender.sendMessage("Đã set thời gian chat là " + args[0]);

		}
		if (sender instanceof Player) {

			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("caidat")) {
				Menu.openMenu(player);
			}

		}

		return true;
	}

	@Override
	public void onDisable() {
		config.set("name", null);
		for (OfflinePlayer player : disableMention) {

			config.set("name." + player.getUniqueId().toString(), player.getName());
		}
		saveConfig();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void chatformat(AsyncPlayerChatEvent e) {
		if (e.isCancelled())
			return;
		if (cooldown(1, e.getPlayer())) {
			e.setCancelled(true);
			return;
		}

		if (!e.getPlayer().isOp()) {
			timers.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
		}
		Player player = e.getPlayer();

		if (!ASkyBlockAPI.getInstance().hasIsland(player.getUniqueId())
				&& !ASkyBlockAPI.getInstance().inTeam(player.getUniqueId()))
			return;

		e.setCancelled(true);
		Essentials ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		String playerprefix = PermissionsEx.getUser(player).getPrefix().replaceAll("&", "§");
		User sender = ess.getUser(e.getPlayer());

		String suffix = PermissionsEx.getUser(player).getSuffix().replaceAll("&", "§");
		Bukkit.getScheduler().runTaskAsynchronously(Main.getMain(), new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				String message = e.getMessage();

				if (sender.isMuted()) {
					Main.getMain().getServer().getConsoleSender().sendMessage(
							ChatColor.RED + "[ChatLogger] [Muted] " + e.getPlayer().getName() + ": " + e.getMessage());
					return;
				}

				long money = sender.getMoney().longValue();

				TextComponent level = new TextComponent(ChatColor.DARK_RED + "[" + ChatColor.RED
						+ ASkyBlockAPI.getInstance().getIslandLevel(player.getUniqueId()) + ChatColor.DARK_RED + "] ");
				TextComponent prefix = new TextComponent(" " + playerprefix + " ");

				HoverEvent playerstats = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(ChatColor.YELLOW + "Số tiền: " + ChatColor.RED + money + ChatColor.GREEN
								+ "$" + "\n" + ChatColor.LIGHT_PURPLE + "Tên thật: " + ChatColor.RED + player.getName()
								+ "\n" + ChatColor.YELLOW + "Level đảo: " + ChatColor.RED
								+ ASkyBlockAPI.getInstance().getIslandLevel(player.getUniqueId())).create());
				level.setHoverEvent(playerstats);
				TextComponent name = new TextComponent(e.getPlayer().getName());
				name.setColor(ChatColor.RESET);
				name.setBold(false);
				if (sender.getNickname() != null) {
					name = new TextComponent(sender.getNickname());
				}
				name.setHoverEvent(playerstats);

				TextComponent end = new TextComponent(" >> ");
				end.setColor(ChatColor.GOLD);
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {

					if (message.contains(p.getName())) {

						message = message.replaceAll(p.getName(),
								ChatColor.GREEN + "@" + ChatColor.RED + p.getName() + suffix);
						if (!isMentionable(p)) {
							
							continue;
						}
						p.playSound(p.getLocation(), Sound.ENTITY_WOLF_HOWL, 1, 1);

						p.sendTitle(ChatColor.RED + e.getPlayer().getName(),
								ChatColor.GRAY + "Đã nhắc đến bạn trong chat");
					}
				}

				TextComponent msg = new TextComponent(suffix + message);
				msg.setColor(ChatColor.RESET);
				msg.setBold(false);
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(ChatColor.RED + "Bấm vào tui để nhắn tin đến người này").create()));
				level.addExtra(prefix);
				level.addExtra(name);
				level.addExtra(end);
				level.addExtra(msg);
				level.setClickEvent(
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w" + " " + e.getPlayer().getName() + " "));

				TextComponent staff = new TextComponent(ChatColor.RED + " ❖");
				if (e.getPlayer().hasPermission("skyblock.staff")) {
					staff.setBold(true);
					staff.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(ChatColor.AQUA + "Đây là Staff").create()));

				}

				for (Player pl : Main.getMain().getServer().getOnlinePlayers()) {
					User receiver = ess.getUser(player);
					if (receiver.isIgnoredPlayer(sender) && !receiver.isIgnoreExempt())
						continue;
					if (e.getPlayer().hasPermission("skyblock.staff")) {

						pl.spigot().sendMessage(level, staff);

					} else if (!e.getPlayer().hasPermission("skyblock.staff") || e.getPlayer().isOp() == false) {

						pl.spigot().sendMessage(level);

					}

				}

				Main.getMain().getServer().getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ChatLogger] " + e.getPlayer().getName() + ": " + e.getMessage());

			}
		});

	}

	public static Main getMain() {
		return main;
	}

}
