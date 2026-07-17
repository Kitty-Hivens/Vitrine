package me.jellysquid.mods.sodium.mixin.core.pipeline;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.gl.attribute.BufferVertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.VertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VertexType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexBufferView, VertexDrain {

    @Shadow
    private ByteBuffer byteBuffer;

    @Shadow
    private IntBuffer rawIntBuffer;

    @Shadow
    private ShortBuffer rawShortBuffer;

    @Shadow
    private FloatBuffer rawFloatBuffer;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private VertexFormat vertexFormat;

    @Shadow
    private int vertexCount;

    @Unique
    private static int roundBufferSize(int amount) {
        int i = 2097152;
        if (amount == 0) {
            return i;
        } else {
            if (amount < 0) {
                i *= -1;
            }

            int j = amount % i;
            return j == 0 ? amount : amount + i - j;
        }
    }
    
    @Override
    public boolean ensureBufferCapacity(int bytes) {
    	if(vertexFormat != null) {
            // Ensure that there is always space for 1 more vertex; see BufferBuilder.next()
            bytes += vertexFormat.getSize();
        }

        if (this.vertexCount * this.vertexFormat.getSize() + bytes <= this.byteBuffer.capacity()) {
            return false;
        }

        int newSize = this.byteBuffer.capacity() + roundBufferSize(bytes);

        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", this.byteBuffer.capacity(), newSize);

        int intPosition = this.rawIntBuffer.position();

        this.byteBuffer.position(0);

        ByteBuffer byteBuffer = GLAllocation.createDirectByteBuffer(newSize);
        byteBuffer.put(this.byteBuffer);
        byteBuffer.rewind();

        this.byteBuffer = byteBuffer;

        // 1.12's BufferBuilder keeps derived views over the backing buffer. Vanilla growBuffer()
        // recreates them after swapping the buffer; if we don't, any vanilla code path that runs
        // afterwards (addVertexData, putColorMultiplier, sortVertexData) reads and writes through
        // views that still point at the orphaned old buffer.
        this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
        this.rawIntBuffer = this.byteBuffer.asIntBuffer();
        this.rawIntBuffer.position(intPosition);
        this.rawShortBuffer = this.byteBuffer.asShortBuffer();
        this.rawShortBuffer.position(intPosition << 1);

        return true;
    }

    @Override
    public ByteBuffer getDirectBuffer() {
        return this.byteBuffer;
    }

    @Override
    public int getWriterPosition() {
        return this.vertexCount * this.vertexFormat.getSize();
    }

    @Override
    public BufferVertexFormat getVertexFormat() {
        return BufferVertexFormat.from(this.vertexFormat);
    }

    @Override
    public void flush(int vertexCount, BufferVertexFormat format) {
        if (BufferVertexFormat.from(this.vertexFormat) != format) {
            throw new IllegalStateException("Mis-matched vertex format (expected: [" + format + "], currently using: [" + this.vertexFormat + "])");
        }

        this.vertexCount += vertexCount;
        //this.elementOffset += vertexCount * format.getStride();
    }

    @Override
    public <T extends VertexSink> T createSink(VertexType<T> factory) {
        BlittableVertexType<T> blittable = factory.asBlittable();

        if (blittable != null && blittable.getBufferVertexFormat() == this.getVertexFormat())  {
            return blittable.createBufferWriter(this, Vitrine.isDirectMemoryAccessEnabled());
        }

        return factory.createFallbackWriter((BufferBuilder) (Object) this);
    }
}
