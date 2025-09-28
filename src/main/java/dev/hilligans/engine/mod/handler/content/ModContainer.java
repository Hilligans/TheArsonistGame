package dev.hilligans.engine.mod.handler.content;

import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.application.IApplication;
import dev.hilligans.engine.client.graphics.*;
import dev.hilligans.engine.client.graphics.resource.VertexFormat;
import dev.hilligans.engine.client.input.Input;
import dev.hilligans.engine.client.input.InputHandlerProvider;
import dev.hilligans.engine.client.graphics.util.Texture;
import dev.hilligans.engine.client.graphics.api.IGraphicsEngine;
import dev.hilligans.engine.client.graphics.api.ILayoutEngine;
import dev.hilligans.engine.client.graphics.api.IModel;
import dev.hilligans.engine.command.ICommand;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.mod.handler.ModClass;
import dev.hilligans.engine.network.Protocol;
import dev.hilligans.engine.network.engine.INetworkEngine;
import dev.hilligans.engine.network.packet.PacketType;
import dev.hilligans.engine.resource.loaders.ResourceLoader;
import dev.hilligans.engine.resource.registry.loaders.RegistryLoader;
import dev.hilligans.engine.authentication.IAuthenticationScheme;
import dev.hilligans.engine.test.ITest;
import dev.hilligans.engine.util.registry.IRegistryElement;
import dev.hilligans.engine.util.registry.Registry;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ModContainer {

    public final ModClass modClass;
    public Registry<Registry<?>> registries;
    public GameInstance gameInstance;
    public URLClassLoader classLoader;
    public Path path;


    public Registry<IGraphicsEngine<?,?,?>> graphicsEngineRegistry;
    public Registry<Protocol> protocolRegistry;
    public Registry<ResourceLoader<?>> resourceLoaderRegistry;
    public Registry<RegistryLoader> registryLoaderRegistry;
    public Registry<RenderTarget> renderTargetRegistry;
    public Registry<RenderPipeline> renderPipelineRegistry;
    public Registry<RenderTaskSource> renderTaskRegistry;
    public Registry<Input> inputRegistry;
    public Registry<VertexFormat> vertexFormatRegistry;
    public Registry<InputHandlerProvider> inputHandlerProviderRegistry;
    public Registry<Texture> textureRegistry;
    public Registry<ShaderSource> shaderSourceRegistry;
    public Registry<ILayoutEngine<?>> layoutEngineRegistry;
    public Registry<EntityType> entityTypeRegistry;
    public Registry<INetworkEngine<?, ?>> networkEngineRegistry;
    public Registry<ICommand> commandRegistry;
    public Registry<IAuthenticationScheme<?>> authenticationSchemeRegistry;
    public Registry<ITest> testRegistry;
    public Registry<IApplication> applicationRegistry;

    public ModContainer(Class<? extends ModClass> clazz, URLClassLoader classLoader, Path path) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        this.modClass = clazz.getConstructor().newInstance();
        this.classLoader = classLoader;
        this.path = path;
    }

    public ModContainer(ModClass modClass) {
        this.modClass = modClass;
    }

    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.registries = gameInstance.REGISTRIES.duplicate();

        for(Registry<?> registry : registries.ELEMENTS) {
            registry.mapping = false;
        }

        graphicsEngineRegistry = (Registry<IGraphicsEngine<?, ?, ?>>) registries.getExcept("ourcraft:graphics_engine");
        protocolRegistry = (Registry<Protocol>) registries.getExcept("ourcraft:protocol");
        resourceLoaderRegistry = (Registry<ResourceLoader<?>>) registries.getExcept("ourcraft:resource_loader");
        registryLoaderRegistry = (Registry<RegistryLoader>) registries.getExcept("ourcraft:registry_loader");
        renderTargetRegistry = (Registry<RenderTarget>) registries.getExcept("ourcraft:render_target");
        renderPipelineRegistry = (Registry<RenderPipeline>) registries.getExcept("ourcraft:render_pipeline");
        renderTaskRegistry = (Registry<RenderTaskSource>) registries.getExcept("ourcraft:render_task");
        inputRegistry = (Registry<Input>) registries.getExcept("ourcraft:key_bind");
        vertexFormatRegistry = (Registry<VertexFormat>) registries.getExcept("ourcraft:vertex_format");
        inputHandlerProviderRegistry = (Registry<InputHandlerProvider>) registries.getExcept("ourcraft:input");
        textureRegistry = (Registry<Texture>) registries.getExcept("ourcraft:texture");
        shaderSourceRegistry = (Registry<ShaderSource>) registries.getExcept("ourcraft:shader");
        layoutEngineRegistry = (Registry<ILayoutEngine<?>>) registries.getExcept("ourcraft:layout_engine");
        entityTypeRegistry = (Registry<EntityType>) registries.getExcept("ourcraft:entity_type");
        networkEngineRegistry = (Registry<INetworkEngine<?,?>>) registries.getExcept("ourcraft:network_engine");
        commandRegistry = (Registry<ICommand>) registries.getExcept("ourcraft:command");
        authenticationSchemeRegistry = (Registry<IAuthenticationScheme<?>>) registries.getExcept("ourcraft:authentication_scheme");
        testRegistry = (Registry<ITest>) registries.getExcept("ourcraft:test");
        applicationRegistry = (Registry<IApplication>) registries.getExcept("ourcraft:application");
    }

    public String getModID() {
        return modClass.getModID();
    }

    public GameInstance getGameInstance() {
        return gameInstance;
    }

    public <T extends IRegistryElement> void register(String type, T... data) {
        for(T val : data) {
            val.assignOwner(this);
        }
        registries.getExcept(type).putAllGen(data);
    }

    public <T extends IRegistryElement> void registerCore(String type, T... data) {
        for(T val : data) {
            val.assignOwner(this);
        }
        Registry<?> registry = gameInstance.REGISTRIES.getExcept(type);
        registry.setCoreRegistry();
        registry.putAllGen(data);
    }

    public void registerTexture(Texture... textures) {
        for(Texture texture : textures) {
            texture.assignOwner(this);
          //  this.textures.add(texture);
        }
        textureRegistry.putAll(textures);
    }

    public void registerModel(IModel... models) {
        for(IModel iModel : models) {
        //    this.models.add(iModel);
        }
    }

    @SafeVarargs
    public final void registerPacket(PacketType<?>... packets) {
        ModContainer self = this;
        for(PacketType<?> packetType : packets) {
            Protocol protocol = protocolRegistry.computeIfAbsent("ourcraft:Play", (s -> new Protocol(s.split(":")[1]).setSource(self)));
            protocol.register(packetType);
        }
    }

    @SafeVarargs
    public final void registerPacket(String protocolName, PacketType<?>... packets) {
        ModContainer self = this;
        for(PacketType<?> packetType : packets) {
            Protocol protocol = protocolRegistry.computeIfAbsent(protocolName, (s -> new Protocol(s.split(":")[1]).setSource(self)));
            protocol.register(packetType);
        }
    }

    public final void registerPacket(String protocolName, int id, PacketType<?> packet) {
        ModContainer self = this;
        Protocol protocol = protocolRegistry.computeIfAbsent(protocolName, (s -> new Protocol(s.split(":")[1]).setSource(self)));
        protocol.register(packet,id);
    }

    public void registerResourceLoader(ResourceLoader<?>... resourceLoaders) {
        for(ResourceLoader<?> resourceLoader : resourceLoaders) {
            resourceLoader.gameInstance = gameInstance;
            resourceLoader.assignOwner(this);
        }
        resourceLoaderRegistry.putAll(resourceLoaders);
    }

    public void registerGraphicsEngine(IGraphicsEngine<?,?,?>... graphicsEngines) {
        for(IGraphicsEngine<?,?,?> graphicsEngine : graphicsEngines) {
            graphicsEngine.assignOwner(this);
        }
        graphicsEngineRegistry.putAll(graphicsEngines);
    }

    public void registerRenderTarget(RenderTarget... renderTargets) {
        for(RenderTarget renderTarget : renderTargets) {
            renderTarget.assignOwner(this);
        }
        renderTargetRegistry.putAll(renderTargets);
        //this.renderTargets.addAll(Arrays.asList(renderTargets));
    }

    public void registerRenderPipelines(RenderPipeline... renderPipelines) {
        for(RenderPipeline renderPipeline : renderPipelines) {
            renderPipeline.assignOwner(this);
        }
        renderPipelineRegistry.putAll(renderPipelines);
        //this.renderPipelines.addAll(Arrays.asList(renderPipelines));
    }

    public void registerRenderTask(RenderTaskSource... renderTasks) {
        for(RenderTaskSource renderTask : renderTasks) {
            renderTask.assignOwner(this);
        }
        renderTaskRegistry.putAll(renderTasks);
        //this.renderTasks.addAll(Arrays.asList(renderTasks));
    }

    public void registerVertexFormat(VertexFormat... vertexFormats) {
        for(VertexFormat vertexFormat : vertexFormats) {
            vertexFormat.assignOwner(this);
        }
        vertexFormatRegistry.putAll(vertexFormats);
        //this.vertexFormats.addAll(Arrays.asList(vertexFormats));
    }

    public void registerInputHandlerProviders(InputHandlerProvider... providers) {
        for(InputHandlerProvider provider : providers) {
            provider.assignOwner(this);
        }
        inputHandlerProviderRegistry.putAll(providers);
        //this.inputHandlerProviders.addAll(Arrays.asList(providers));
    }

    public void registerKeybinds(Input... inputs) {
        for(Input input : inputs) {
            input.assignOwner(this);
        }
        inputRegistry.putAll(inputs);
        //this.keybinds.addAll(Arrays.asList(inputs));
    }

    public void registerShader(ShaderSource... shaderSources) {
        for(ShaderSource shaderSource : shaderSources) {
            shaderSource.assignOwner(this);
        }
        shaderSourceRegistry.putAll(shaderSources);
        //this.shaders.addAll(Arrays.asList(shaderSources));
    }

    public void registerLayoutEngine(ILayoutEngine<?>... layoutEngines) {
        for(ILayoutEngine<?> layoutEngine : layoutEngines) {
            layoutEngine.assignOwner(this);
        }
        layoutEngineRegistry.putAll(layoutEngines);
        //this.layoutEngines.addAll(Arrays.asList(layoutEngines));
    }

    public void registerEntityType(EntityType... entityTypes) {
        for(EntityType entityType : entityTypes) {
            entityType.assignOwner(this);
        }
        entityTypeRegistry.putAll(entityTypes);
    }

    public void registerNetworkEngine(INetworkEngine<?, ?>... networkEngines) {
        for(INetworkEngine<?, ?> networkEngine : networkEngines) {
            networkEngine.assignOwner(this);
        }
        networkEngineRegistry.putAll(networkEngines);
    }

    public void registerCommands(ICommand... commands) {
        for(ICommand command : commands) {
            command.assignOwner(this);
        }
        commandRegistry.putAll(commands);
    }

    public void registerAuthenticationScheme(IAuthenticationScheme<?>... schemes) {
        for(IAuthenticationScheme<?> scheme : schemes) {
            scheme.assignOwner(this);
        }
        authenticationSchemeRegistry.putAll(schemes);
    }

    public void registerTest(ITest... tests) {
        for(ITest test : tests) {
            test.assignOwner(this);
        }
        testRegistry.putAll(tests);
    }

    public void registerApplication(IApplication... applications) {
        for(IApplication application : applications) {
            application.assignOwner(this);
        }
        applicationRegistry.putAll(applications);
    }

    @Override
    public int hashCode() {
        return registries.hashCode();
    }
}
