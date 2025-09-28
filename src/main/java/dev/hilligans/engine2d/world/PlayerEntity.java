package dev.hilligans.engine2d.world;

import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.graphics.ShaderSource;
import dev.hilligans.engine.client.graphics.api.GraphicsContext;
import dev.hilligans.engine.client.graphics.api.IGraphicsEngine;
import dev.hilligans.engine.client.graphics.api.IMeshBuilder;
import dev.hilligans.engine.client.graphics.resource.MatrixStack;
import dev.hilligans.engine.client.graphics.resource.VertexFormat;
import dev.hilligans.engine.data.BoundingBox;
import dev.hilligans.engine.data.IBoundingBox;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine2d.client.Camera2D;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine2d.client.sprite.StaticSprite;
import org.joml.Vector2d;

import java.security.SecureRandom;
import java.util.List;

public class PlayerEntity extends SpriteEntity {

    public Camera2D camera;

    public float health;
    public boolean alive = true;

    public float rot;
    public boolean firing = false;


    public float firingCapacity = 100;
    public boolean overheat = false;


    public static final float MAX_FIRING_CAPACITY = 0;
    public static final float MAX_HEALTH = 100;

    public VertexFormat format;
    public ShaderSource shaderSource;

    public PlayerEntity(EntityType entityType, Sprite sprite, Camera2D camera) {
        super(entityType, sprite);
        this.camera = camera;
        this.width = 16;
        this.height = 16;
        this.deAccel = 4f;
        this.health = MAX_HEALTH/2;

        format = camera.getWindow().getGameInstance().get("ourcraft:position_color", VertexFormat.class);
        shaderSource = camera.getWindow().getGameInstance().get("ourcraft:position_color_shader", ShaderSource.class);
        id = new SecureRandom().nextLong();
    }

    public PlayerEntity(EntityType entityType, Sprite sprite, long id, GameInstance gameInstance) {
        super(entityType, sprite);
        format = gameInstance.get("ourcraft:position_color", VertexFormat.class);
        shaderSource = gameInstance.get("ourcraft:position_color_shader", ShaderSource.class);
        this.width = 16;
        this.height = 16;
        this.deAccel = 4f;
        this.id = id;
    }

    @Override
    public IBoundingBox getEntityBoundingBox() {
        return super.getEntityBoundingBox().shrunk(4, 4, 0);
    }

    @Override
    public int getSpriteIndex() {
        return 0;
    }

    public boolean shouldFire() {
        return firing && !overheat;
    }

    public double deadlossFrametime = 0;

    @Override
    public Sprite getSprite() {
        if(!alive) {
            return new StaticSprite();
        } else {
            return super.getSprite();
        }
    }

    @Override
    public void tick() {
        if(!alive) {
            return;
        }

        World2D world = getWorld();

        List<Scene.SceneSection> sectionList = world.getScene().getOverlappingSections(getEntityBoundingBox());

        Vector2d vector2d = new Vector2d();

        this.deadlossFrametime += world.lastFrametime;
        if(deadlossFrametime < 0.01) {
            return;
        }

        double x = getX();
        double y = getY();
        double velX = (getVelX() * deadlossFrametime);
        double velY = (getVelY() * deadlossFrametime);

        this.velX -= (float) (deAccel * velX);
        this.velY -= (float) (deAccel * velY);

        double testX = getWidth() / velX;
        double testY = getHeight() / velY;

        IBoundingBox myBox = getEntityBoundingBox();

        loop:
        for(Scene.SceneSection section : sectionList) {
            for(BoundingBox boundingBox : section.section().boundingBoxes) {
                if(boundingBox.intersects(myBox.shrunk(-0.1f, -0.1f, 0.0f).moved(velX, velY, 0))) {
                    velX = 0;
                    velY = 0;
                    break loop;
                }

               // double distance = boundingBox.intersectsRay(x, y, 0, velX, velY, 0, vector2d);
                double distance = -1;
                if(distance != -1 && (distance < testX || distance < testY)) {
                    boolean iX = boundingBox.intersectsX(myBox);
                    boolean iY = boundingBox.intersectsY(myBox);

                    if(iX && iY) {
                        velX = 0;
                        velY = 0;
                        break loop;
                    }
                }
            }
        }


        boolean onFire = false;
        EntityType fire = world.gameInstance.get("arsonist:campfire", EntityType.class);

        for(Entity2D entity2D : world.entities) {
            if(entity2D.getEntityType() == fire) {
                onFire |= entity2D.getEntityBoundingBox().intersects(myBox);
            }
        }

        if(onFire) {
            double diff = 50 * deadlossFrametime;
            health -= (float) diff;
        } else {
            health += (deadlossFrametime * 25);
        }
        if(health > 100) {
            health = 100;
        }
        if(health <= 0 ){
            health = 0;
            alive = false;
        }

        if(shouldFire()) {
            firingCapacity -= 30 * deadlossFrametime;
            if(firingCapacity < 0) {
                firingCapacity = 0;
                overheat = true;
            }
        } else {
            firingCapacity += 20 * deadlossFrametime;
            if(firingCapacity >= 100) {
                firingCapacity = 100;
                overheat = false;
            }
        }

        deadlossFrametime = 0;

        this.x += velX;
        this.y += velY;
    }

    @Override
    public void drawExtra(IGraphicsEngine<?, ?, ?> engine, GraphicsContext graphicsContext, MatrixStack matrixStack, ShaderSource shaderSource, RenderWindow renderWindow) {

        if(!alive) {
            return;
        }
        IMeshBuilder builder = engine.getDefaultImpl().getMeshBuilder(format);

        float minY = (float) (getY() - 6);
        float maxY = (float) (getY() - 4);

        float minX = (float) (getX() - getWidth() / 4);
        float maxX = (float) (getX() + getWidth() + getWidth() / 4);
        float maxX2 = minX + (maxX - minX) * health / MAX_HEALTH;

        addRGBA(builder, minX, minY, maxX, maxY, 1, 0.1f, 0.1f, 1f);
        addRGBA(builder, minX, minY, maxX2, maxY, 0.1f, 1, 0.1f, 1f);


        engine.getDefaultImpl().bindPipeline(graphicsContext, this.shaderSource.program);
        engine.getDefaultImpl().uploadMatrix(graphicsContext, matrixStack, this.shaderSource);
        engine.getDefaultImpl().drawAndDestroyMesh(graphicsContext, matrixStack, builder);
        engine.getDefaultImpl().bindPipeline(graphicsContext, shaderSource.program);
    }

    public static void addRGBA(IMeshBuilder builder, float minX, float minY, float maxX, float maxY, float r, float g, float b, float a) {
        float z = 0;

        builder.addClockwiseIndices(builder.getVertexCount());
        builder.addVertices(new float[]{
                minX, minY, z, r, g, b, a,
                minX, maxY, z, r, g, b, a,
                maxX, minY, z, r, g, b, a,
                maxX, maxY, z, r, g, b, a});
    }
}
