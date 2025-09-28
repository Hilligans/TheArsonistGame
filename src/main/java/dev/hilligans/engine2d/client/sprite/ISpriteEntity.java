package dev.hilligans.engine2d.client.sprite;

import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.graphics.ShaderSource;
import dev.hilligans.engine.client.graphics.api.GraphicsContext;
import dev.hilligans.engine.client.graphics.api.IGraphicsEngine;
import dev.hilligans.engine.client.graphics.resource.MatrixStack;
import dev.hilligans.engine.entity.IEntity;

public interface ISpriteEntity extends IEntity {

    Sprite getSprite();
    int getSpriteIndex();

    float getWidth();
    float getHeight();

    default void drawExtra(IGraphicsEngine<?, ?, ?> engine, GraphicsContext graphicsContext, MatrixStack matrixStack, ShaderSource shaderSource, RenderWindow renderWindow) {
    }
}
