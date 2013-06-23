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
import android.util.Log;

/**
 * 
 * This sprite can be stretched.
 * 
 * Use setStretchX/Y or setPosition() methods
 * 
 * @author Benoit Chauvet (Orabig) www.crocoware.com
 * 
 */
public class StretchSprite extends Sprite {
	private float stretchX = 0.0f;
	private float stretchY = 0.0f;

	public final static int ATTRIBUTE_SHIFT_LOCATION = 1;
	public final static int ATTRIBUTE_SIZE_LOCATION = 3;

	public static final VertexBufferObjectAttributes VERTEXBUFFEROBJECTATTRIBUTES_FIXED = new VertexBufferObjectAttributesBuilder(
			3)
			.add(ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION,
					ShaderProgramConstants.ATTRIBUTE_POSITION, 4,
					GLES20.GL_FLOAT, false)
			.add(ATTRIBUTE_SHIFT_LOCATION, "a_shift", 2, GLES20.GL_FLOAT, false)
			.add(ATTRIBUTE_SIZE_LOCATION, "a_size", 2, GLES20.GL_FLOAT, false)
			.build();

	public final static int VERTEX_SIZE_FIXED = 4 + 2 + 2;// X Y 0 1
	// shift(2) + size(2)
	private final static int SPRITE_SIZE_FIXED = VERTEX_SIZE_FIXED
			* Sprite.VERTICES_PER_SPRITE;

	public StretchSprite(final float pX, final float pY, final float pWidth,
			final float pHeight, final ITextureRegion pTextureRegion,
			final VertexBufferObjectManager pVertexBufferObjectManager,
			final DrawType pDrawType) {
		super(pX, pY, pWidth, pHeight, pTextureRegion, new QuadVbo(
				pVertexBufferObjectManager, SPRITE_SIZE_FIXED, pDrawType, true,
				VERTEXBUFFEROBJECTATTRIBUTES_FIXED), StretchShader
				.getInstance());
	}

	private boolean isDefinedByCoords = false;
	private float xa, xb, xc, xd, ya, yb, yc, yd;

	// documenter ici : ordre = 14
	// 23
	// TODO : tester si le sprite est a l’envers
	public void setPosition(float x1, float y1, float x2, float y2, float x3,
			float y3, float x4, float y4) {
		isDefinedByCoords = true;
		float xm = (x1 + x2 + x3 + x4) / 4;
		float ym = (y1 + y2 + y3 + y4) / 4;
		setPosition(xm, ym);
		xa = x1 - xm;
		ya = y1 - ym;
		xb = x2 - xm;
		yb = y2 - ym;
		xc = x3 - xm;
		yc = y3 - ym;
		xd = x4 - xm;
		yd = y4 - ym;

		fixPosition();

		onUpdateVertices();
	}

	/**
	 * When the orientation of the sprite is badly defined, then the display is
	 * corrupted. We must then apply a 90° effet
	 */
	private void fixPosition() {
		float ddy,dby,dbx,ddx;
		ddx=xd-xa;
		dbx=xb-xa;
		ddy=yd-ya;
		dby=yb-ya;
		if ((ddx+ddy)*(dbx+dby)>0) {
			float w;
			w = xa;
			xa = xb;
			xb = xc;
			xc = xd;
			xd = w;
			w = ya;
			ya = yb;
			yb = yc;
			yc = yd;
			yd = w;
		}
	}

	public float getStretchX() {
		return stretchX;
	}

	public void setStretchX(float stretch) {
		isDefinedByCoords = false;
		this.stretchX = stretch;
		onUpdateVertices();
	}

	public float getStretchY() {
		return stretchY;
	}

	public void setStretchY(float stretch) {
		isDefinedByCoords = false;
		this.stretchY = stretch;
		onUpdateVertices();
	}

	static class QuadVbo extends HighPerformanceSpriteVertexBufferObject {

		public QuadVbo(VertexBufferObjectManager pVertexBufferObjectManager,
				int pCapacity, DrawType pDrawType, boolean pAutoDispose,
				VertexBufferObjectAttributes pVertexBufferObjectAttributes) {
			super(pVertexBufferObjectManager, pCapacity, pDrawType,
					pAutoDispose, pVertexBufferObjectAttributes);
		}

		private Vertex v0 = new Vertex();
		private Vertex v1 = new Vertex();
		private Vertex v2 = new Vertex();
		private Vertex v3 = new Vertex();

		@Override
		public void onUpdateVertices(final Sprite pSprite) {
			StretchSprite me = (StretchSprite) pSprite;

			final float dw = me.getWidth() / 2;
			final float dh = me.getHeight() / 2;

			// Standard vertex coordinates
			float ax, ay, bx, by, cx, cy, dx, dy;
			if (me.isDefinedByCoords) {
				ax = me.xa;
				ay = me.ya;
				bx = me.xb;
				by = me.yb;
				cx = me.xc;
				cy = me.yc;
				dx = me.xd;
				dy = me.yd;
			} else {
				ax = bx = -dw;
				ay = dy = -dh;
				by = cy = +dh;
				cx = dx = +dw;
				// Compute new coordinates from pinchX and pinchY values
				if (me.stretchX > 0) {
					dx = (1 - me.stretchX) * dw;
					ax = -dx;
				} else if (me.stretchX < 0) {
					cx = (1 + me.stretchX) * dw;
					bx = -cx;
				}

				if (me.stretchY > 0) {
					by = (1 - me.stretchY) * dh;
					ay = -by;
				} else if (me.stretchY < 0) {
					cy = (1 + me.stretchY) * dh;
					dy = -cy;
				}
			}
			// Set vertices positions
			v0.setXY(ax, ay);
			v1.setXY(bx, by);
			v3.setXY(cx, cy);
			v2.setXY(dx, dy);

			// Set Quad size
			float bay = by - ay;
			final float dax = dx - ax;
			final float cbx = cx - bx;
			final float cdy = cy - dy;

			v0.setSize(dax, bay);
			v1.setSize(cbx, bay);
			v3.setSize(cbx, cdy);
			v2.setSize(dax, cdy);

			float U1;
			float V1;
			float U2;
			float V2;

			final ITextureRegion region = pSprite.getTextureRegion();
			if (pSprite.isFlippedVertical()) {
				if (pSprite.isFlippedHorizontal()) {
					U1 = region.getU2();
					U2 = region.getU();
					V1 = region.getV2();
					V2 = region.getV();
				} else {
					U1 = region.getU();
					U2 = region.getU2();
					V1 = region.getV2();
					V2 = region.getV();
				}
			} else {
				if (pSprite.isFlippedHorizontal()) {
					U1 = region.getU2();
					U2 = region.getU();
					V1 = region.getV();
					V2 = region.getV2();
				} else {
					U1 = region.getU();
					U2 = region.getU2();
					V1 = region.getV();
					V2 = region.getV2();
				}
			}

			// Set Quad texture position
			v0.setShift(dax * U1, bay * V1);
			v1.setShift(cbx * U1, bay * V2);
			v3.setShift(cbx * U2, cdy * V2);
			v2.setShift(dax * U2, cdy * V1);

			v0.fillBuffer(this.mBufferData, 0);
			v1.fillBuffer(this.mBufferData, 1);
			v2.fillBuffer(this.mBufferData, 2);
			v3.fillBuffer(this.mBufferData, 3);

			this.setDirtyOnHardware();
		}

		@Override
		public void onUpdateTextureCoordinates(final Sprite pSprite) {
			onUpdateVertices(pSprite);
		}

		@Override
		public void onUpdateColor(final Sprite pSprite) {
			// Color is not used
		}
	}

	private static class Vertex {
		// Vertex data : position
		private float x;
		private float y;
		// shift
		private float sx, sy;
		// size
		private float zx, zy;

		// Setters
		public void setXY(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public void setShift(float u, float v) {
			this.sx = u;
			this.sy = v;
		}

		public void setSize(float u, float v) {
			this.zx = u;
			this.zy = v;
		}

		public void fillBuffer(float[] buffer, int idx) {
			int offset = idx * StretchSprite.VERTEX_SIZE_FIXED;
			buffer[offset++] = x;
			buffer[offset++] = y;
			buffer[offset++] = 0;
			buffer[offset++] = 1;
			buffer[offset++] = sx;
			buffer[offset++] = sy;
			buffer[offset++] = zx;
			buffer[offset++] = zy;
		}
	}
}