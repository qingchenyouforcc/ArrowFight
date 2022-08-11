package org.mmga.mycraft.arrowfight.events;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.mmga.mycraft.arrowfight.ArrowFightPlugin;
import org.mmga.mycraft.arrowfight.entities.GameObject;
import org.mmga.mycraft.arrowfight.entities.MapObject;
import org.mmga.mycraft.arrowfight.runnable.ArrowRain;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created On 2022/8/7 18:18
 *
 * @author wzp
 * @version 1.0.0
 */
public class OnTick extends BukkitRunnable {
    private static final int TICK_SEC = 20;
    int tick = 0;

    public static void replaceAir(Material block, Location location) {
        if (location.getBlock().getType().isAir()) {
            location.getBlock().setType(block);
        }
    }

    @Override
    public void run() {
        boolean runSec = tick % TICK_SEC == 0;
        tick++;
        for (MapObject mapObject : MapObject.GAMES.keySet()) {
            CopyOnWriteArrayList<GameObject> gameObjects = MapObject.GAMES.get(mapObject);
            if (gameObjects != null) {
                for (GameObject gameObject : gameObjects) {
                    gameObject.addTick();
                    World copyWorld = gameObject.getCopyWorld();
                    Collection<Arrow> entitiesByClass = copyWorld.getEntitiesByClass(Arrow.class);
                    for (Arrow byClass : entitiesByClass) {
                        PotionData basePotionData = byClass.getBasePotionData();
                        PotionType type = basePotionData.getType();
                        boolean extended = basePotionData.isExtended();
                        boolean upgraded = basePotionData.isUpgraded();
                        Location arrowLocation = byClass.getLocation();
                        if (PotionType.LUCK.equals(type)) {
                            int blockX = arrowLocation.getBlockX();
                            int blockY = arrowLocation.getBlockY();
                            int blockZ = arrowLocation.getBlockZ();
                            World world = arrowLocation.getWorld();
                            for (int x = -1; x <= 1; x++) {
                                for (int y = -1; y <= 1; y++) {
                                    for (int z = -1; z <= 1; z++) {
                                        Block block = new Location(world, blockX + x, blockY + y, blockZ + z).getBlock();
                                        Material material = block.getType();
                                        if (!Material.BEDROCK.equals(material)) {
                                            block.setType(Material.AIR);
                                        }
                                    }
                                }
                            }
                            boolean onGround = byClass.isOnGround();
                            if (onGround) {
                                byClass.remove();
                            }
                        }
                        if (PotionType.POISON.equals(type)) {
                            boolean onGround = byClass.isOnGround();
                            if (onGround) {
                                if (!extended && !upgraded) {
                                    for (int i = 0; i < 10; i++) {
                                        arrowLocation.getWorld().spawnEntity(arrowLocation, EntityType.CREEPER);
                                    }
                                } else {
                                    for (int i = 0; i < 20; i++) {
                                        arrowLocation.getWorld().spawnEntity(arrowLocation, EntityType.ZOMBIE);
                                    }

                                }
                                byClass.remove();
                            }
                        }
                        if (PotionType.INVISIBILITY.equals(type)) {
                            boolean onGround = byClass.isOnGround();
                            if (onGround) {
                                if (!extended && !upgraded) {
                                    arrowLocation.getWorld().spawnEntity(arrowLocation, EntityType.PRIMED_TNT);
                                } else {
                                    ProjectileSource shooter = byClass.getShooter();
                                    if (shooter instanceof Player) {
                                        Map<Player, GameObject.GameTeam> teamPlayers = gameObject.getTeamPlayers();
                                        GameObject.GameTeam gameTeam = teamPlayers.get(shooter);
                                        boolean has = false;
                                        Location rainMain = null;
                                        for (Player player : teamPlayers.keySet()) {
                                            GameObject.GameTeam value = teamPlayers.get(player);
                                            if (!value.equals(gameTeam)) {
                                                player.sendMessage(ChatColor.RED + "TNT雨来袭，快找个地方躲起来！");
                                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                                if (new Random().nextBoolean() && !has) {
                                                    has = true;
                                                    rainMain = player.getLocation().clone();
                                                }
                                            }
                                        }
                                        if (!has) {
                                            MapObject map = gameObject.getMapObject();
                                            if (gameTeam.equals(GameObject.GameTeam.RED)) {
                                                rainMain = map.getBlueSpawn().clone();
                                            } else {
                                                rainMain = map.getRedSpawn().clone();
                                            }
                                        }
                                        rainMain.add(0, 60, 0);
                                        int blockX = rainMain.getBlockX();
                                        int blockY = rainMain.getBlockY();
                                        int blockZ = rainMain.getBlockZ();
                                        Location rain1 = new Location(copyWorld, blockX + 2, blockY, blockZ);
                                        Location rain2 = new Location(copyWorld, blockX - 2, blockY, blockZ);
                                        Location rain3 = new Location(copyWorld, blockX, blockY, blockZ + 2);
                                        Location rain4 = new Location(copyWorld, blockX, blockY, blockZ - 2);
                                        Location rain5 = new Location(copyWorld, blockX + 2, blockY, blockZ + 2);
                                        Location rain6 = new Location(copyWorld, blockX + 2, blockY, blockZ - 2);
                                        Location rain7 = new Location(copyWorld, blockX - 2, blockY, blockZ + 2);
                                        Location rain8 = new Location(copyWorld, blockX - 2, blockY, blockZ - 2);
                                        copyWorld.spawnEntity(rainMain, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain1, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain2, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain3, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain4, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain5, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain6, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain7, EntityType.PRIMED_TNT);
                                        copyWorld.spawnEntity(rain8, EntityType.PRIMED_TNT);
                                    }


                                }
                                byClass.remove();
                            }
                        }
                        if (PotionType.SLOW_FALLING.equals(type)) {
                            boolean onGround = byClass.isOnGround();
                            if (onGround) {
                                ArrowFightPlugin arrowFightPlugin = ArrowFightPlugin.getPlugin(ArrowFightPlugin.class);
                                for (int i = 1; i < 3; i++) {
                                    new ArrowRain(arrowLocation).runTaskLater(arrowFightPlugin, i * 5);
                                }
                                byClass.remove();
                            }
                        }
                        if (PotionType.FIRE_RESISTANCE.equals(type)) {
                            if (byClass.isOnGround()) {
                                Material material;
                                if (extended) {
                                    material = Material.FIRE;
                                } else {
                                    material = Material.LAVA;
                                }
                                int blockX = arrowLocation.getBlockX();
                                int blockY = arrowLocation.getBlockY();
                                int blockZ = arrowLocation.getBlockZ();
                                replaceAir(material, arrowLocation);
                                replaceAir(material, new Location(copyWorld, blockX + 1, blockY, blockZ));
                                replaceAir(material, new Location(copyWorld, blockX - 1, blockY, blockZ));
                                replaceAir(material, new Location(copyWorld, blockX, blockY, blockZ + 1));
                                replaceAir(material, new Location(copyWorld, blockX, blockY, blockZ - 1));
                                replaceAir(material, new Location(copyWorld, blockX + 1, blockY, blockZ + 1));
                                replaceAir(material, new Location(copyWorld, blockX + 1, blockY, blockZ - 1));
                                replaceAir(material, new Location(copyWorld, blockX - 1, blockY, blockZ + 1));
                                replaceAir(material, new Location(copyWorld, blockX - 1, blockY, blockZ - 1));
                                byClass.remove();
                            }
                        }
                    }
                    if (runSec) {
                        gameObject.sec();
                    }
                }
            }
        }
    }
}
