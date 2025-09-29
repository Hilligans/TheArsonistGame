package dev.hilligans.arsonist;

import dev.hilligans.arsonist.entity.FireEntity;
import dev.hilligans.arsonist.entity.Flame;
import dev.hilligans.arsonist.network.CUpdatePlayer;
import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.application.IClientApplication;
import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.graphics.Screen;
import dev.hilligans.engine.client.graphics.api.IGraphicsEngine;
import dev.hilligans.engine.data.BoundingBox;
import dev.hilligans.engine.data.IBoundingBox;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.network.Protocol;
import dev.hilligans.engine.network.engine.INetworkEngine;
import dev.hilligans.engine.network.engine.NetworkSocket;
import dev.hilligans.engine.util.ThreadContext;
import dev.hilligans.engine2d.client.Camera2D;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine2d.world.PlayerEntity;
import dev.hilligans.engine2d.world.Scene;
import dev.hilligans.engine2d.world.SpriteEntity;
import dev.hilligans.engine2d.world.World2D;
import org.joml.Vector3d;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client2D implements IClientApplication {

    public GameInstance gameInstance;

    public RenderWindow window;
    public Screen openScreen;

    public World2D world;
    public ConcurrentHashMap<Long, PlayerEntity> players = new ConcurrentHashMap<>();

    public float mouseX;
    public float mouseY;

    public EntityCamera2D camera2D;
    public PlayerEntity playerEntity;
    public Flame flame;

    public NetworkSocket<?> socket;

    public boolean rendering = false;

    public World2D getWorld() {
        return world;
    }

    @Override
    public Screen getOpenScreen() {
        return openScreen;
    }

    long lastSendTime = 0;

    @Override
    public void tick(ThreadContext threadContext) {
        if(world.networkEntity != null) {
            if(lastSendTime + 25 < world.frameStartTime) {
                lastSendTime = world.frameStartTime;
                CUpdatePlayer.encode(world.networkEntity, playerEntity);
            }
        }
    }

    public void respawn() {
        playerEntity.alive = true;
        rendering = false;
        playerEntity.health = 100;

        List<Scene.SceneSection> sectionList = world.getScene().getOverlappingSections(playerEntity.getEntityBoundingBox());
        Random random = new Random();
        out:
        while(true) {
            int x = random.nextInt(900) + 10;
            int y = random.nextInt(500) + 10;

            playerEntity.setPosition(x, y, 0);

            IBoundingBox boundingBox = playerEntity.getEntityBoundingBox();
            boolean valid = true;
            label:
            for(Scene.SceneSection section : sectionList) {
                for(BoundingBox boundingBox1 : section.section().boundingBoxes) {
                    if(boundingBox1.intersects(boundingBox)) {
                        valid = false;
                        break label;
                    }
                }
            }

            if(valid) {
                break;
            }
        }

        rendering = true;
        if(world.networkEntity != null) {
            CUpdatePlayer.encode(world.networkEntity, playerEntity);
        }
    }

    @Override
    public void openScreen(Screen screen) {
        this.openScreen = screen;
    }

    @Override
    public RenderWindow getRenderWindow() {
        return window;
    }

    @Override
    public GameInstance getGameInstance() {
        return gameInstance;
    }

    @Override
    public void postCoreStartApplication(GameInstance gameInstance) {
    }

    public void connectNetwork() {
        try {
            Thread.sleep(3000);
            socket = gameInstance.getExcept("ourcraft:nettyEngine", INetworkEngine.class)
                    .openClient(gameInstance.getExcept("arsonist:play", Protocol.class), this, "5.161.116.70", "25588");

            socket.onConnected((e) -> world.networkEntity = e);
            socket.connectSocket();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startApplication(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        IGraphicsEngine<?, ?, ?> engine = gameInstance.get("ourcraft:openglEngine", IGraphicsEngine.class);

        AtomicBoolean waiting = new AtomicBoolean(true);
        Thread thread = new Thread(() -> {
            try {
                window = engine.startEngine();
                window.camera = new EntityCamera2D(window, 600, 400);
                waiting.set(false);
                window.setClient(this);

                window.setClearColor(0.2f, 0.3f, 0.3f, 1.0f);

                window.setRenderPipeline("engine2D:pipeline2d");
                engine.createRenderLoop(gameInstance, window).run();
                engine.close();

                gameInstance.THREAD_PROVIDER.EXECUTOR.shutdownNow();
                System.exit(0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread thread1 = new Thread(this::connectNetwork);
        thread1.start();

        world = new World2D(gameInstance, "engine2D:test_scene");

    //    world.addEntity(new SpriteEntity(
     //           gameInstance.getExcept("engine2D:entity_type", EntityType.class),
      //          gameInstance.getExcept("engine2D:test_sprite", Sprite.class)) {
       // });

        SpriteEntity entity = new FireEntity(gameInstance);
        entity.x = 50;
        world.addEntity(entity);

        thread.start();

        while (waiting.get()) {}
        playerEntity = new PlayerEntity(
                gameInstance.getExcept("engine2D:entity_type", EntityType.class),
                gameInstance.getExcept("engine2D:test_sprite", Sprite.class),
                (Camera2D) getRenderWindow().getCamera());

        this.camera2D = (EntityCamera2D) getRenderWindow().getCamera();
        this.camera2D.entity = playerEntity;

        world.addEntity(playerEntity);
        flame = new Flame(gameInstance, playerEntity);
        world.addEntity(flame);
        respawn();
    }

    public float getWorldMouseX() {
        Vector3d vec = camera2D.getPosition();
        float width = camera2D.getWindowWidth();

        return (float) (vec.x + (mouseX - camera2D.getInsetX())/camera2D.getScale()) - camera2D.worldWidth/2;
    }

    public float getWorldMouseY() {
        Vector3d vec = camera2D.getPosition();
        float height = camera2D.getWindowHeight();

        return (float) ((vec.y + (mouseY - camera2D.getInsetY()) / camera2D.getScale())) - camera2D.worldHeight/2;
    }

    public void updateFlameRot() {
        if(flame != null) {
            flame.parent.rot = (float) Math.atan2(getWorldMouseY() - playerEntity.getY(), this.getWorldMouseX() - playerEntity.getX());
        }
    }

    public void setVelX(float x) {
        if(this.playerEntity != null) {
            this.playerEntity.velX = x;
        }
    }

    public void setVelY(float y) {
        if(this.playerEntity != null) {
            this.playerEntity.velY = y;
        }
    }

    @Override
    public String getResourceName() {
        return "client2D";
    }

    @Override
    public String getResourceOwner() {
        return "engine2D";
    }
}
