package Privato;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.BlockPos;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.AxisAlignedBB;
import java.util.Random;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.DamageSource;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class privato extends Thread {
    private static privato instance;
    private Minecraft mc = Minecraft.getMinecraft();
    private Random random = new Random();
    private static boolean flyEnabled = false;
    private static boolean killAuraEnabled = true;
    private static boolean jitterEnabled = true;
    private static boolean aimAssistEnabled = true;
    private static boolean boxesp = false;
    private static boolean scaffold = false;
    private static boolean teleportion = false;
    private static boolean clicki = false;

    private boolean leftClicking = false;
    private String customName = mc.getSession().getUsername();
    private Robot robot;

    private privato() {
        this.start();
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        if (instance == null) {
            instance = new privato();
        }
    }
    private void scaffolds() {
        if (!mc.thePlayer.onGround) {
            for (int y = 0; y < 256; y++) {
                if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - y, mc.thePlayer.posZ)).getBlock().isReplaceable(mc.theWorld, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - y, mc.thePlayer.posZ))) {
                    ItemStack currentItemStack = mc.thePlayer.getHeldItem();
                    if (currentItemStack != null && currentItemStack.getItem() instanceof ItemBlock) {
                        ItemBlock itemBlock = (ItemBlock) currentItemStack.getItem();
                        Block block = itemBlock.getBlock();

                        BlockPos targetPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - y, mc.thePlayer.posZ);
                        EnumFacing facing = EnumFacing.UP; // Change to desired facing direction

                        C08PacketPlayerBlockPlacement placeBlockPacket = new C08PacketPlayerBlockPlacement(targetPos, facing.getIndex(), currentItemStack, 0.0f, 0.0f, 0.0f);
                        mc.thePlayer.sendQueue.addToSendQueue(placeBlockPacket);
                        break;
                    }
                }
            }
        }
    }


    public void onUpdate() {
        if (mc.theWorld != null && mc.thePlayer != null && mc.getSession().getUsername().equals(customName)) {
            EntityPlayerSP player = mc.thePlayer;
            double originalX = player.posX;
            double originalY = player.posY;
            double originalZ = player.posZ;
            double groundY = Math.floor(originalY) - 0.1;

            player.capabilities.allowFlying = true;
            player.capabilities.isFlying = true;
            simulateGroundPackets(originalX, groundY, originalZ);

            if (player.onGround) {
                player.setPosition(originalX, groundY, originalZ);
            }

            if (!flyEnabled) {
                player.capabilities.allowFlying = false;
                player.capabilities.isFlying = false;
            }
        }
    }

    private void simulateGroundPackets(double groundX, double groundY, double groundZ) {
        Packet groundPositionPacket = new C03PacketPlayer.C04PacketPlayerPosition(groundX, groundY, groundZ, false);
        mc.thePlayer.sendQueue.addToSendQueue(groundPositionPacket);

        try {
            Thread.sleep(50 + (int) (Math.random() * 100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Packet blockPlacementPacket = new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem());
        mc.thePlayer.sendQueue.addToSendQueue(blockPlacementPacket);
    }

    private void enableFlyMode() {
        EntityPlayerSP player = mc.thePlayer;
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        sendFlyingPacket();
    }

    private void disableFlyMode() {
        EntityPlayerSP player = mc.thePlayer;
        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        sendGroundPacket();
    }

    private void teleportToNearestPlayer() {
        EntityPlayerSP player = mc.thePlayer;
        EntityPlayerSP targetPlayer = getNearestPlayer();

        if (targetPlayer != null) {
            double targetX = targetPlayer.posX;
            double targetY = targetPlayer.posY;
            double targetZ = targetPlayer.posZ;

            double step = 1.0;

            double diffX = targetX - player.posX;
            double diffY = targetY - player.posY;
            double diffZ = targetZ - player.posZ;

            double distance = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

            double steps = distance / step;

            for (int i = 0; i < steps; i++) {
                double stepX = player.posX + diffX * (i / steps);
                double stepY = player.posY + diffY * (i / steps);
                double stepZ = player.posZ + diffZ * (i / steps);

                player.setPositionAndUpdate(stepX, stepY, stepZ);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            player.setPositionAndUpdate(targetX, targetY, targetZ);
        }
    }

    private EntityPlayerSP getNearestPlayer() {
        EntityPlayerSP player = mc.thePlayer;
        EntityPlayerSP closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.playerEntities) {
            if (entity instanceof EntityPlayerSP && entity != player) {
                double distance = player.getDistanceToEntity(entity);

                if (distance < closestDistance) {
                    closestPlayer = (EntityPlayerSP) entity;
                    closestDistance = distance;
                }
            }
        }

        return closestPlayer;
    }

    private void sendFlyingPacket() {
        EntityPlayerSP player = mc.thePlayer;
        double posX = player.posX;
        double posY = player.posY;
        double posZ = player.posZ;
        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;

        double modifiedPosY = posY - 0.01;
        float modifiedPitch = pitch - 1.0F;

        Packet flyingPacket = new C03PacketPlayer.C04PacketPlayerPosition(posX, modifiedPosY, posZ, true);
        player.sendQueue.addToSendQueue(flyingPacket);

        player.setPosition(posX, modifiedPosY, posZ);
        player.setRotationYawHead(yaw);
        player.rotationYaw = yaw;
        player.rotationPitch = modifiedPitch;
    }

    private void sendGroundPacket() {
        EntityPlayerSP player = mc.thePlayer;
        double posX = player.posX;
        double posY = player.posY;
        double posZ = player.posZ;

        Packet groundPacket = new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, false);
        player.sendQueue.addToSendQueue(groundPacket);
    }

    public void update() {
        EntityPlayerSP player = mc.thePlayer;

        if (flyEnabled && player != null) {
            sendFlyingPacket();

            if (jitterEnabled) {
                addJitter();
            }
        }
    }
    private boolean clicking = false;

    public void run() {
        while (true) {
            try {
                if (mc.theWorld != null && mc.thePlayer != null && mc.getSession().getUsername().equals(customName)) {
                    EntityPlayerSP player = mc.thePlayer;

                    if (flyEnabled) {
                        enableFlyMode();
                        if (leftClicking && jitterEnabled) {
                            addJitter();
                        }
                    } else {
                        disableFlyMode();
                    }
                    if (clicki) {
                    	handleMouseInput();
                    }

                    if (aimAssistEnabled && killAuraEnabled) {
                        attackEntities();
                    }
                    if (boxesp) {
                        drawESP();
                    }
                    if (scaffold) {
                        scaffold();
                    }
                    if (teleportion) {
                        teleportToNearestPlayer();
                    }
                }
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addJitter() {
        EntityPlayerSP player = mc.thePlayer;
        double posX = player.posX;
        double posY = player.posY;
        double posZ = player.posZ;

        double offsetX = (Math.random() - 0.5) * 0.01;
        double offsetY = (Math.random() - 0.5) * 0.01;
        double offsetZ = (Math.random() - 0.5) * 0.01;

        player.setPosition(posX + offsetX, posY + offsetY, posZ + offsetZ);
    }

    private void updateFlySpeed() {
        float flySpeed = 0.05f;
        float speedVariation = getRandomFloat(-0.02f, 0.02f);
        flySpeed += speedVariation;

        mc.thePlayer.motionX *= flySpeed;
        mc.thePlayer.motionY *= flySpeed;
        mc.thePlayer.motionZ *= flySpeed;
    }

    private float getRandomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private long lastAttackTime = System.currentTimeMillis();

    private void scaffold() {
        if (!mc.thePlayer.onGround) {
            for (int y = 0; y < 256; y++) {
                if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - y, mc.thePlayer.posZ)).getBlock().isReplaceable(mc.theWorld, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - y, mc.thePlayer.posZ))) {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                    break;
                }
            }
        }
    }

    private void drawESP() {
        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof net.minecraft.entity.EntityLivingBase) {
                net.minecraft.entity.EntityLivingBase livingBase = (net.minecraft.entity.EntityLivingBase) entity;

                if (livingBase != mc.thePlayer) {
                    AxisAlignedBB boundingBox = livingBase.getEntityBoundingBox();
                    renderBoundingBox(boundingBox);
                }
            }
        }
    }

    private void renderBoundingBox(AxisAlignedBB boundingBox) {
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        double minX = boundingBox.minX - renderPosX;
        double minY = boundingBox.minY - renderPosY;
        double minZ = boundingBox.minZ - renderPosZ;
        double maxX = boundingBox.maxX - renderPosX;
        double maxY = boundingBox.maxY - renderPosY;
        double maxZ = boundingBox.maxZ - renderPosZ;
    }

    private int maxTargets = 2;
    private float reachDistance = 4.5f;

    private float getTargetYaw(double targetX, double targetZ) {
        double deltaX = targetX - mc.thePlayer.posX;
        double deltaZ = targetZ - mc.thePlayer.posZ;
        return (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
    }

    private float getTargetPitch(double targetX, double targetY, double targetZ) {
        double deltaX = targetX - mc.thePlayer.posX;
        double deltaY = targetY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double deltaZ = targetZ - mc.thePlayer.posZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        return (float) -Math.toDegrees(Math.atan2(deltaY, distance));
    }

    private long getAttackDelay() {
        double distance = mc.playerController.getBlockReachDistance();
        return (long) (distance * 1000.0);
    }

    private boolean killAuraEnabled1 = true;

    private void attackEntities() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < 2000) {
            return;
        }

        int targetCount = 0;

        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof net.minecraft.entity.EntityLivingBase) {
                net.minecraft.entity.EntityLivingBase livingBase = (net.minecraft.entity.EntityLivingBase) entity;

                if (livingBase != mc.thePlayer && livingBase.isEntityAlive() && mc.thePlayer.canEntityBeSeen(livingBase) && isValidTarget(livingBase)) {
                    attackEntity(livingBase);
                    targetCount++;

                    if (targetCount >= maxTargets) {
                        break;
                    }
                }
            }
        }

        lastAttackTime = currentTime;
    }

    private void attackEntity(net.minecraft.entity.EntityLivingBase entity) {
        double originalX = mc.thePlayer.posX;
        double originalY = mc.thePlayer.posY;
        double originalZ = mc.thePlayer.posZ;
        float originalYaw = mc.thePlayer.rotationYaw;
        float originalPitch = mc.thePlayer.rotationPitch;

        double deltaX = entity.posX - originalX;
        double deltaY = (entity.posY + entity.getEyeHeight() / 2) - (originalY + mc.thePlayer.getEyeHeight());
        double deltaZ = entity.posZ - originalZ;
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)));

        mc.thePlayer.rotationYaw = targetYaw;
        mc.thePlayer.rotationPitch = targetPitch;

        double distance = mc.thePlayer.getDistance(entity.posX, entity.posY, entity.posZ);
        if (distance <= reachDistance) {
            if (entity instanceof net.minecraft.entity.player.EntityPlayer) {
                net.minecraft.entity.player.EntityPlayer targetPlayer = (net.minecraft.entity.player.EntityPlayer) entity;

                int packetCount = 5 + (int) (Math.random() * 46);

                float damage = targetPlayer.getMaxHealth() * 0.5f; // Set the damage to be dealt

                targetPlayer.attackEntityFrom(DamageSource.causePlayerDamage(mc.thePlayer), damage);
                mc.thePlayer.swingItem();

                System.out.println("Damage given to player " + targetPlayer.getName() + ": " + damage);

                try {
                    Thread.sleep(50 + (int) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < packetCount; i++) {
                    double offsetX = Math.random() * 0.01 - 0.005;
                    double offsetZ = Math.random() * 0.01 - 0.005;
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + offsetX, mc.thePlayer.posY, mc.thePlayer.posZ + offsetZ, false));

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        mc.thePlayer.setPositionAndUpdate(originalX, originalY, originalZ);
        mc.thePlayer.rotationYaw = originalYaw;
        mc.thePlayer.rotationPitch = originalPitch;
    }

    private boolean isValidTarget(net.minecraft.entity.EntityLivingBase entity) {
        if (entity instanceof net.minecraft.entity.player.EntityPlayer) {
            net.minecraft.entity.player.EntityPlayer targetPlayer = (net.minecraft.entity.player.EntityPlayer) entity;
            String playerName = targetPlayer.getName();

            if (playerName.matches("^[a-zA-Z0-9_]*$")) {
                return mc.thePlayer.getDistanceSq(entity.posX, entity.posY, entity.posZ) <= reachDistance * reachDistance &&
                        mc.thePlayer.canEntityBeSeen(targetPlayer);
            }
        }

        return false;
    }
    private void handleMouseInput() {
        try {
            Robot robot = new Robot();
            while (true) {
                if (clicking) {
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.delay(1000 / 15);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                } else {
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }
                Thread.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
