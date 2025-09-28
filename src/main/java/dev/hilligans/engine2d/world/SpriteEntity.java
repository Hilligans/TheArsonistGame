package dev.hilligans.engine2d.world;

import dev.hilligans.engine.client.graphics.api.GraphicsContext;
import dev.hilligans.engine2d.client.sprite.ISpriteEntity;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine.entity.EntityType;

public class SpriteEntity extends Entity2D implements ISpriteEntity {

    public Sprite sprite;
    public int spriteIndex;
    public int frameDelay = 400;
    public int spriteCount = 4;

    public SpriteEntity(EntityType entityType, Sprite sprite) {
        super(entityType);
        this.sprite = sprite;
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public int getSpriteIndex() {
        return spriteIndex;
    }

    @Override
    public void tickVisuals(GraphicsContext graphicsContext) {
        long time = graphicsContext.getFrameStartTime();
        spriteIndex = (int) ((time / frameDelay) % spriteCount);
    }
}
