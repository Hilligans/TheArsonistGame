package dev.hilligans.engine2d.client.sprite;

import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.graphics.ShaderSource;
import dev.hilligans.engine.client.graphics.api.*;
import dev.hilligans.engine.client.graphics.resource.MatrixStack;
import dev.hilligans.engine.util.registry.IRegistryElement;

public interface Sprite extends IRegistryElement, IGraphicsElement {

    void build(IMeshBuilder builder);

    void draw(ISpriteEntity entity, IIndirectBuilder builder);

    void draw(ISpriteEntity entity, IGraphicsEngine<?, ?, ?> engine, GraphicsContext graphicsContext, MatrixStack matrixStack, ShaderSource shaderSource, RenderWindow renderWindow);

    default String getResourceType() {
        return "sprite";
    }
}
