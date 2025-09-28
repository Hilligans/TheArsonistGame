package dev.hilligans.arsonist.entity;

import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.util.IByteArray;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine2d.world.SpriteEntity;

public class FireEntity extends SpriteEntity {

    public long eol;

    public FireEntity(GameInstance gameInstance) {
        super(gameInstance.getExcept("arsonist:campfire", EntityType.class),
                gameInstance.getExcept("arsonist:FireSprite", Sprite.class));
        this.frameDelay = 100;
        this.spriteCount = 6;
        this.eol = System.currentTimeMillis() + 3000;
    }

    public FireEntity(EntityType entityType, float x, float y, long eol) {
        super(entityType, null);
        this.x = x;
        this.y = y;
        this.frameDelay = 100;
        this.spriteCount = 6;
        this.eol = eol;
    }

    public FireEntity(EntityType entityType, Sprite sprite, float x, float y, long eol) {
        super(entityType, sprite);
        this.x = x;
        this.y = y;
        this.frameDelay = 100;
        this.spriteCount = 6;
        this.eol = eol;
    }

    public FireEntity(GameInstance gameInstance, double x, double y) {
        this(gameInstance);
        this.x = x;
        this.y = y;
        this.frameDelay = 100;
        this.spriteCount = 6;
    }

    @Override
    public boolean shouldRemove() {
        return this.world2D.frameStartTime > this.eol;
    }

    @Override
    public void encode(IByteArray array) {
        super.encode(array);
        array.writeLong(eol);
    }
}
