package com.crocoware.andengine.entity.sprite.stretch;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.vbo.HighPerformanceSpriteVertexBufferObject;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.DrawType;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributesBuilder;

import android.opengl.GLES20;

/**
 * A Sprite which can be stretched
 * 
 * @author Benoit Chauvet (Orabig) www.crocoware.com
 * 
 */
public class PerspectiveSprite extends Sprite {
	private float stretchX = 0.0f;
	private float stretchY = 0.0f;

	public float getStretchX() {
		return stretchX;
	}

	public void setStretchX(float pinchX) {
		this.stretchX = pinchX;
		onUpdateVertices();
		onUpdateTextureCoordinates();
	}

	public float getStretchY() {
		return stretchY;
	}

	public void setStretchY(float pinchY) {
		this.stretchY = pinchY;
		onUpdateVertices();
		onUpdateTextureCoordinates();
	}

	public static final VertexBufferObjectAttributes VERTEXBUFFEROBJECTATTRIBUTES_FIXED = new VertexBufferObjectAttributesBuilder(
			2)
			.add(ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION,
					ShaderProgramConstants.ATTRIBUTE_POSITION, 2,
					GLES20.GL_FLOAT, false)
			.add(ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES_LOCATION,
					ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES, 4,
					GLES20.GL_FLOAT, false).build();

	private final static int VERTEX_SIZE_FIXED = 2 + 4;// X Y
														// Texture(vec4)
	private final static int SPRITE_SIZE_FIXED = VERTEX_SIZE_FIXED
			* Sprite.VERTICES_PER_SPRITE;

	private final static int TEXTURECOORDINATES_INDEX_FIXED_U = Sprite.COLOR_INDEX;
	private final static int TEXTURECOORDINATES_INDEX_FIXED_V = TEXTURECOORDINATES_INDEX_FIXED_U + 1;
	private final static int TEXTURECOORDINATES_INDEX_FIXED_Q = TEXTURECOORDINATES_INDEX_FIXED_V + 2;

	public PerspectiveSprite(final float pX, final float pY,
			final float pWidth, final float pHeight,
			final ITextureRegion pTextureRegion,
			final VertexBufferObjectManager pVertexBufferObjectManager,
			final DrawType pDrawType) {
		super(pX, pY, pWidth, pHeight, pTextureRegion, new QuadVbo(
				pVertexBufferObjectManager, SPRITE_SIZE_FIXED, pDrawType, true,
				VERTEXBUFFEROBJECTATTRIBUTES_FIXED), PerspectiveShader
				.getInstance());
	}

	static class QuadVbo extends HighPerformanceSpriteVertexBufferObject {

		public QuadVbo(VertexBufferObjectManager pVertexBufferObjectManager,
				int pCapacity, DrawType pDrawType, boolean pAutoDispose,
				VertexBufferObjectAttributes pVertexBufferObjectAttributes) {
			super(pVertexBufferObjectManager, pCapacity, pDrawType,
					pAutoDispose, pVertexBufferObjectAttributes);
		}

		@Override
		public void onUpdateColor(final Sprite pSprite) {
		}

		@Override
		public void onUpdateVertices(final Sprite pSprite) {
			PerspectiveSprite me = (PerspectiveSprite) pSprite;
			final float[] bufferData = this.mBufferData;

			final float pX = me.getStretchX();
			final float pY = me.getStretchY();
			final float oX = 0.5f;
			final float oY = 0.5f;
			final float w = pSprite.getWidth();
			final float h = pSprite.getHeight();
			final float wx1 = pX < 0 ? (1 + pX) * w : w;
			final float wx2 = pX > 0 ? (1 - pX) * w : w;
			final float hy1 = pY < 0 ? (1 + pY) * h : h;
			final float hy2 = pY > 0 ? (1 - pY) * h : h;

			final float x1 = pX < 0 ? oX * w - wx1 / 2 : 0;
			final float y1 = pY < 0 ? oY * h - hy1 / 2 : 0;
			final float x2 = pX < 0 ? x1 + wx1 : w;
			final float x3 = pX > 0 ? oX * w + wx2 / 2 : w;
			final float y3 = pY > 0 ? oY * h + hy2 / 2 : h;
			final float y2 = pY > 0 ? y3 - hy2 : 0;
			final float x4 = pX > 0 ? x3 - wx2 : 0;
			final float y4 = pY < 0 ? y1 + hy1 : h;

			final float w2 = w / 2;
			final float h2 = h / 2;

			bufferData[0 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_X] = x1 - w2;
			bufferData[0 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_Y] = y1 - h2;

			bufferData[1 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_X] = x4 - w2;
			bufferData[1 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_Y] = y4 - h2;

			bufferData[2 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_X] = x2 - w2;
			bufferData[2 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_Y] = y2 - h2;

			bufferData[3 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_X] = x3 - w2;
			bufferData[3 * VERTEX_SIZE_FIXED + Sprite.VERTEX_INDEX_Y] = y3 - h2;

			this.setDirtyOnHardware();
		}

		@Override
		public void onUpdateTextureCoordinates(final Sprite pSprite) {
			PerspectiveSprite me = (PerspectiveSprite) pSprite;
			final float[] bufferData = this.mBufferData;

			final ITextureRegion textureRegion = pSprite.getTextureRegion(); // TODO
																				// Optimize
																				// with
																				// field
																				// access?

			final float u;
			final float v;
			final float u2;
			final float v2;

			if (pSprite.isFlippedVertical()) { // TODO Optimize with field
												// access?
				if (pSprite.isFlippedHorizontal()) { // TODO Optimize with field
														// access?
					u = textureRegion.getU2();
					u2 = textureRegion.getU();
					v = textureRegion.getV2();
					v2 = textureRegion.getV();
				} else {
					u = textureRegion.getU();
					u2 = textureRegion.getU2();
					v = textureRegion.getV2();
					v2 = textureRegion.getV();
				}
			} else {
				if (pSprite.isFlippedHorizontal()) { // TODO Optimize with field
														// access?
					u = textureRegion.getU2();
					u2 = textureRegion.getU();
					v = textureRegion.getV();
					v2 = textureRegion.getV2();
				} else {
					u = textureRegion.getU();
					u2 = textureRegion.getU2();
					v = textureRegion.getV();
					v2 = textureRegion.getV2();
				}
			}

			for (int i = 0; i < 4; i++)
				bufferData[i * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V + 1] = 0;

			if (textureRegion.isRotated()) {
				bufferData[0 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u2;
				bufferData[0 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v;
				bufferData[0 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = 1.0f;

				bufferData[1 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u;
				bufferData[1 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v;
				bufferData[1 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = 1.0f;

				bufferData[2 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u2;
				bufferData[2 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v2;
				bufferData[2 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = 1.0f;

				bufferData[3 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u;
				bufferData[3 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v2;
				bufferData[3 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = 1.0f;
			} else {

				float tr = me.getStretchX() < 0 ? 1 + me.getStretchX() : 1.0f;
				float br = me.getStretchX() > 0 ? 1 - me.getStretchX() : 1.0f;

				float lr = me.getStretchY() < 0 ? 1 + me.getStretchY() : 1.0f;
				float rr = me.getStretchY() > 0 ? 1 - me.getStretchY() : 1.0f;

				float ltl = tr * lr;
				bufferData[0 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u * ltl;
				bufferData[0 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v * ltl;
				bufferData[0 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = ltl;

				float lbl = br * lr;
				bufferData[1 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u * lbl;
				bufferData[1 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v2 * lbl;
				bufferData[1 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = lbl;

				float ltr = tr * rr;
				bufferData[2 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u2 * ltr;
				bufferData[2 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v * ltr;
				bufferData[2 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = ltr;

				float lbr = br * rr;
				bufferData[3 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_U] = u2 * lbr;
				bufferData[3 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_V] = v2 * lbr;
				bufferData[3 * VERTEX_SIZE_FIXED
						+ TEXTURECOORDINATES_INDEX_FIXED_Q] = lbr;

			}

			this.setDirtyOnHardware();
		}

	}
}
