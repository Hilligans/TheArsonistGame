package dev.hilligans.arsonist.entity;

import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine2d.client.sprite.ISpriteEntity;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine2d.client.sprite.StaticSprite;
import dev.hilligans.engine2d.world.PlayerEntity;
import dev.hilligans.engine2d.world.SpriteEntity;
import org.json.JSONObject;

public class Flame extends SpriteEntity {

    public PlayerEntity parent;
    public boolean isVisible = true;
    public long lastFlameTime = System.currentTimeMillis();
    public float lastFlameRot = 0;
    public double lastFlameX;
    public double lastFlameY;

    public Flame(GameInstance gameInstance, PlayerEntity parent) {
        super(gameInstance.getExcept("arsonist:flame", EntityType.class),
                gameInstance.getExcept("arsonist:FlameSprite", Sprite.class));

        this.width = 64;
        this.height = 64;
        this.frameDelay = 100;
        this.spriteCount = 6;
        this.parent = parent;
    }

    public static final long flameDelay = 200;

    @Override
    public double getX() {
        return parent.getX();
    }

    @Override
    public double getY() {
        return parent.getY();
    }

    @Override
    public Sprite getSprite() {
        if(parent.shouldFire()) {
            return super.getSprite();
        } else {
            return new StaticSprite();
        }
    }

    @Override
    public void tick() {
        if(parent.shouldFire() && parent.alive) {
            if(world2D.frameStartTime > lastFlameTime + flameDelay) {
                this.lastFlameTime = System.currentTimeMillis();
                if(Math.abs(this.lastFlameRot - this.parent.rot) < 0.01 && Math.abs(this.lastFlameX - this.getX()) < 5 && Math.abs(this.lastFlameY - this.getY()) < 5 && !(world2D.frameStartTime > lastFlameTime + 10*flameDelay)) {
                    return;
                }

                this.lastFlameRot = parent.rot;

                double x = (Math.cos(parent.rot) * this.width);
                double y = (Math.sin(parent.rot) * this.width);

                int count = 6;
                for(int i = 4; i <= count; i++) {
                    double actX = x * i/count + this.getX();
                    double actY = y * i/count + this.getY() - height/3;

                    this.world2D.queueEntity(new FireEntity(this.world2D.gameInstance, actX, actY));
                }
            }
        }
    }
}
