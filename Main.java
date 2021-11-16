package com.badflar.BSMP.bsmplore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



public class Main extends JavaPlugin implements Listener {
	
	public boolean LoreMode = false;
	
	public Random rand = new Random();
	
	public boolean NeedApplySmokeyLore = false;
	public boolean NeedRemoveSmokeyLore = false;
	
	public boolean NeedRemoveBadflarLore = false;
	public org.bukkit.entity.LivingEntity BadflarMorph;
	
	Map <UUID, Double> oxygenAmounts = new HashMap<UUID, Double>();
	
	public long barrCooldown = 0;
	
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			int tick = 0;
			public void run() {
				tick += 1;
				if (LoreMode) {
					Player Tinchue = Bukkit.getPlayer("Tinchue");
					if (Tinchue != null) waterBreather(Tinchue, tick);
				}
			}
		}, 0L, 0L);
		
		barrCooldown = (System.currentTimeMillis() / 1000);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	
	// ------------------ OnCommand -------------------- //
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("loretoggle")) {
			if (LoreMode) {
				LoreMode = false;
				NeedApplySmokeyLore= false;
				RemoveSmokeyLore();
				RemoveBadflarMorph();
			} else {
				LoreMode = true;
				NeedRemoveSmokeyLore= false;
				NeedRemoveBadflarLore = false;
				ApplySmokeyLore();
			}
		}
		
		return false;
	}
	
	// ------------------ OnRespawn -------------------- //
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player Tinchue = Bukkit.getPlayer("Tinchue");
		if (Tinchue == event.getPlayer() && LoreMode) {
			oxygenAmounts.put(event.getPlayer().getUniqueId(), (double) 300);
		}
	}
	
	// ------------------ OnPlayerJoin -------------------- //
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Player Smokey = Bukkit.getPlayer("SmokeyDreams");
		if (player == Smokey) {
			if (NeedApplySmokeyLore) {
				ApplySmokeyLore();
			}
			else if (NeedRemoveSmokeyLore) {
				RemoveSmokeyLore();
			}
		}
		return;
	}
	
	// ------------------ OnPlayerInteract -------------------- //
	
	@EventHandler
	public void onPlayerRightClick(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) && LoreMode) {
			Player p = e.getPlayer();
			Player Barr = Bukkit.getPlayer("Barrboat");
			if (p.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD) && p == Barr) {
				if ((System.currentTimeMillis() / 1000) - barrCooldown > 7) {
					Block b = p.getTargetBlock((Set<Material>) null, 20);
					p.setHealth(p.getHealth() - 5);
					p.teleport(b.getLocation().add(0, 1, 0).setDirection(p.getLocation().getDirection()));
					barrCooldown = System.currentTimeMillis() / 1000;
				}
			}
		}
	}
	
	// ------------------ OnEntityDamageByEntityEvent -------------------- //
	
	@EventHandler
	public void onEntityDamgeByEntityEvent(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player whoWasHit = (Player) e.getEntity();
			Player whoHit = (Player) e.getDamager();
			
			Player Barr = Bukkit.getPlayer("Barrboat");


			if (whoHit.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD) && LoreMode && whoHit == Barr) {
				whoWasHit.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10, 10, false, false, false));
			}
		}
		
		return;
	}
	
	// ------------------ OnPlayerInteractEntity -------------------- //
	
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		Player Badflar = Bukkit.getPlayer("Badflar");
		Player PolTV = Bukkit.getPlayer("Foxzentena");
		
		if (LoreMode && p == PolTV && p.getItemInHand().getType() == Material.AIR) {
			if (e.getRightClicked().getType() == EntityType.CHICKEN) {
				e.getRightClicked().addPassenger(PolTV);
			}
		}
		
		if (LoreMode && p == Badflar && p.isSneaking() == true) {
			EntityType mobType = e.getRightClicked().getType();
			if (mobType != null) {
				Badflar.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false, false));
				LivingEntity mobInstance = (LivingEntity)e.getRightClicked();
				mobInstance.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 999999, false, false, false));
				mobInstance.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 20, false, false, false));
				mobInstance.setCollidable(false);
				
				BadflarMorph = mobInstance;
				BadflarMorph.setInvulnerable(true);
				BadflarMorph.setGravity(false);
				BadflarMorph.setCollidable(false);
				BadflarMorph.setAI(false);
				p.hidePlayer(this, Badflar);
				p.spigot().setCollidesWithEntities(false);
				Location mobLoc = new Location(mobInstance.getWorld(), mobInstance.getLocation().getX(), mobInstance.getLocation().getY(), mobInstance.getLocation().getZ());
				Location playerLoc = p.getLocation();
				for(int i = 0; i < 360; i+=5) {	
					Location flameloc = playerLoc;
					flameloc.setZ(flameloc.getZ() + Math.cos(i)*5);
					flameloc.setX(flameloc.getX() + Math.sin(i)*5);
					playerLoc.getWorld().playEffect(flameloc, Effect.STEP_SOUND, 51);
				}
				p.playSound(p.getLocation(), Sound.ENTITY_HORSE_ARMOR, 10, 2);
				p.teleport(mobLoc);
			}
		}
		return;
	}
	
	// ------------------ OnPlayerDropItemEvent -------------------- //
	
	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
		Player Badflar = Bukkit.getPlayer("Badflar");
		
		if (LoreMode && e.getItemDrop().getItemStack().getType() == Material.POPPY && BadflarMorph != null) {
			Badflar.removePotionEffect(PotionEffectType.INVISIBILITY);
			BadflarMorph.removePotionEffect(PotionEffectType.SLOW);
			BadflarMorph.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			BadflarMorph.setInvulnerable(false);
			BadflarMorph.setGravity(true);
			BadflarMorph.setCollidable(true);
			BadflarMorph.setAI(true);
			Badflar.showPlayer(this, Badflar);
			Badflar.spigot().setCollidesWithEntities(true);
			BadflarMorph = null;
		} 
		
		return;
	}
	
	// ------------------ OnPlayerMoveEvent -------------------- //
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e) {
		
		Player player = (Player) e.getPlayer();
		Player Badflar = Bukkit.getPlayer("Badflar");
		
		if (LoreMode && player == Badflar && BadflarMorph != null) {
			Location MorphTarget = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
			BadflarMorph.setInvulnerable(true);
			BadflarMorph.setGravity(false);
			BadflarMorph.setCollidable(false);
			player.setCollidable(false);
		
			BadflarMorph.teleport(MorphTarget);
			BadflarMorph.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
			BadflarMorph.getLocation().setDirection(player.getLocation().getDirection());
		}
	
		
		return;
	}
	
	// ------------------ SMOKEY -------------------- //
	
	public void ApplySmokeyLore() {
		Player Smokey = Bukkit.getPlayer("SmokeyDreams");
		if (Smokey != null) {
			Smokey.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 2, false, false, false));
			NeedApplySmokeyLore = false;
		} else {
			NeedApplySmokeyLore = true;
		}
		return;
	}
	
	public void RemoveSmokeyLore() {
		Player Smokey = Bukkit.getPlayer("SmokeyDreams");
		if (Smokey != null) {
			Smokey.removePotionEffect(PotionEffectType.JUMP);
			NeedRemoveSmokeyLore = false;
		} else {
			NeedRemoveSmokeyLore = true;
		}
		return;
	}
	
	// ------------------ TINCHUE -------------------- //
	
	public boolean isWaterSource(Block block) {
		if (block.getType() == Material.WATER) {
			BlockData blockData = block.getBlockData();
			if (blockData instanceof Levelled) {
				Levelled lv = (Levelled)blockData;
				if (lv.getLevel() == lv.getMaximumLevel()) {
					return true;
				}
				
			}
		}
		return false;
	}
	
	public boolean inWaterColumn(Player player) {
		Location location = player.getEyeLocation();
		if (isWaterSource(location.getBlock())) {
			while(location.getBlockY() > 0) {
				location.add(0, -1, 0);
				if (location.getBlock().getType().equals(Material.SOUL_SAND)) {
					return true;
				}
				if(!isWaterSource(location.getBlock())) {
					return false;
				}
			}
		}
		
		return false;
	}
	
	public void waterBreather (Player player, int tick) {
		if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
			player.removePotionEffect(PotionEffectType.FAST_DIGGING);
			if(!oxygenAmounts.containsKey(player.getUniqueId())) {
				oxygenAmounts.put(player.getUniqueId(), (double) 300);
			}
			
			if (!player.getEyeLocation().getBlock().getType().equals(Material.WATER) || inWaterColumn(player)) {
				if(!player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
					oxygenAmounts.put(player.getUniqueId(), oxygenAmounts.get(player.getUniqueId()) - .05);
				}
				
				if (player.getInventory().getHelmet() != null) {
					ItemStack helmet = player.getInventory().getHelmet();
					if (helmet.getEnchantments().containsKey(Enchantment.OXYGEN)) {
						int respirationlevel = helmet.getEnchantments().get(Enchantment.OXYGEN);
						if (Math.random()>(respirationlevel/(respirationlevel+1))) {
							oxygenAmounts.put(player.getUniqueId(), oxygenAmounts.get(player.getUniqueId()) + 1);
						}
					}
				}
				
				if (oxygenAmounts.get(player.getUniqueId()) <= -20) {
					player.damage(2);
					oxygenAmounts.put(player.getUniqueId(), (double) 0);
				}
			} else {
				if (tick % 4 == 0) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 999999, 12, false, false, false));
					oxygenAmounts.put(player.getUniqueId(), oxygenAmounts.get(player.getUniqueId()) + 30);
					if (oxygenAmounts.get(player.getUniqueId()) > 300) {
						oxygenAmounts.put(player.getUniqueId(), (double) 300);
					}
				}
			};
			player.setRemainingAir((int) Math.round(oxygenAmounts.get(player.getUniqueId())));
		}
	}
	
	// ------------------ BADFLAR -------------------- //
	
	public void RemoveBadflarMorph() {
		Player Badflar = Bukkit.getPlayer("Badflar");
		if (Badflar != null && BadflarMorph != null) {
			Badflar.removePotionEffect(PotionEffectType.INVISIBILITY);
			BadflarMorph.removePotionEffect(PotionEffectType.SLOW);
			BadflarMorph.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			BadflarMorph.setInvulnerable(false);
			BadflarMorph.setGravity(true);
			BadflarMorph.setCollidable(true);
			BadflarMorph = null;
			Badflar.showPlayer(this, Badflar);
			Badflar.spigot().setCollidesWithEntities(false);
			
			NeedRemoveBadflarLore = false;
		} else if (Badflar == null && BadflarMorph != null) {
			NeedRemoveBadflarLore = true;
		}
		
		return;
	}
}
