package net.novauniverse.games.chickenout.extension;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.novauniverse.games.chickenout.NovaChickenOut;
import net.novauniverse.games.chickenout.game.ChickenOut;
import net.novauniverse.games.chickenout.game.ChickenOutCountdownType;
import net.zeeraa.novacore.commons.utils.Callback;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameStartEvent;

public class ChickenOutExtension extends JavaPlugin implements Listener {
	private static ChickenOutExtension instance;

	public static ChickenOutExtension getInstance() {
		return instance;
	}

	private BossBar levelBar;
	private BossBar timeBar;

	@Override
	public void onEnable() {
		ChickenOutExtension.instance = this;

		levelBar = Bukkit.createBossBar("null", BarColor.RED, BarStyle.SOLID);
		timeBar = Bukkit.createBossBar("time", BarColor.BLUE, BarStyle.SOLID);

		levelBar.setVisible(false);
		timeBar.setVisible(false);

		Bukkit.getPluginManager().registerEvents(this, this);

		NovaChickenOut.getInstance().getGame().addTimerDecrementCallback(new Callback() {
			@Override
			public void execute() {
				updateTimeBar();
			}
		});

		NovaChickenOut.getInstance().getGame().addLevelChangeCallback(new Callback() {
			@Override
			public void execute() {
				updateLevelBar();
			}
		});
	}

	public void updateLevelBar() {
		levelBar.setTitle(ChatColor.RED + "" + ChatColor.BOLD + "Level " + NovaChickenOut.getInstance().getGame().getLevel());
	}

	public void updateTimeBar() {
		ChickenOut chickenOut = NovaChickenOut.getInstance().getGame();
		if (chickenOut.getCountdownType() == ChickenOutCountdownType.FINAL) {
			int totalTime = chickenOut.getConfig().getFinalRoundTime();
			int timeLeft = chickenOut.getFinalTimeLeft();

			double barValue = (double) timeLeft / (double) totalTime;
			if (barValue < 0) {
				barValue = 0;
			}
			timeBar.setProgress(barValue);
			timeBar.setTitle(ChatColor.RED + "Game ends in: " + TextUtils.secondsToTime(timeLeft));
		} else {
			int totalTime = chickenOut.getConfig().getLevelTime();
			int timeLeft = chickenOut.getRoundTimeLeft();

			double barValue = (double) timeLeft / (double) totalTime;
			double inverted = 1 - barValue;
			timeBar.setProgress(inverted);
			timeBar.setTitle(ChatColor.RED + "Next level in: " + TextUtils.secondsToTime(timeLeft));
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGameStart(GameStartEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updateLevelBar();
				updateTimeBar();

				levelBar.setVisible(true);
				timeBar.setVisible(true);
			}
		}.runTaskLater(this, 5L);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		levelBar.addPlayer(player);
		timeBar.addPlayer(player);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		levelBar.removePlayer(player);
		timeBar.removePlayer(player);
	}
}