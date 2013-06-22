package com.crocoware.andengine.entity.sprite.stretch;

import org.andengine.opengl.shader.PositionColorTextureCoordinatesShaderProgram;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.shader.exception.ShaderProgramLinkException;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;

import android.opengl.GLES20;

/**
 * Shader used by PerspectiveSprite
 * 
 *  @author Benoit Chauvet (Orabig) www.crocoware.com
 * 
 */
public class PerspectiveShader extends ShaderProgram {
	// ===========================================================
	// Constants
	// ===========================================================

	private static PerspectiveShader INSTANCE;

	public static final String VERTEXSHADER = "uniform mat4 "
			+ ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX + ";\n"
			+ "attribute vec4 " + ShaderProgramConstants.ATTRIBUTE_POSITION
			+ ";\n" + "attribute vec4 "
			+ ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES + ";\n"
			+ "varying vec4 "
			+ ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n"
			+ "void main() {\n"
			+ ShaderProgramConstants.VARYING_TEXTURECOORDINATES + " = "
			+ ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES + ";\n"
			+ "	gl_Position = "
			+ ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX + " * "
			+ ShaderProgramConstants.ATTRIBUTE_POSITION + ";\n" + "}";

	public static final String FRAGMENTSHADER = "precision lowp float;\n"
			+ "uniform sampler2D " + ShaderProgramConstants.UNIFORM_TEXTURE_0
			+ ";\n" + "varying mediump vec4 "
			+ ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n"
			+ "void main() {\n" + "	gl_FragColor =  texture2DProj("
			+ ShaderProgramConstants.UNIFORM_TEXTURE_0 + ", "
			+ ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ");\n" + "}";

	// ===========================================================
	// Fields
	// ===========================================================

	public static int sUniformModelViewPositionMatrixLocation = ShaderProgramConstants.LOCATION_INVALID;
	public static int sUniformTexture0Location = ShaderProgramConstants.LOCATION_INVALID;

	// ===========================================================
	// Constructors
	// ===========================================================

	private PerspectiveShader() {
		super(PerspectiveShader.VERTEXSHADER,
				PerspectiveShader.FRAGMENTSHADER);
	}

	public static PerspectiveShader getInstance() {
		if (PerspectiveShader.INSTANCE == null) {
			PerspectiveShader.INSTANCE = new PerspectiveShader();
		}
		return PerspectiveShader.INSTANCE;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void link(final GLState pGLState)
			throws ShaderProgramLinkException {
		GLES20.glBindAttribLocation(this.mProgramID,
				ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION,
				ShaderProgramConstants.ATTRIBUTE_POSITION);
		GLES20.glBindAttribLocation(this.mProgramID,
				ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES_LOCATION,
				ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES);

		super.link(pGLState);

		PositionColorTextureCoordinatesShaderProgram.sUniformModelViewPositionMatrixLocation = this
				.getUniformLocation(ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX);
		PositionColorTextureCoordinatesShaderProgram.sUniformTexture0Location = this
				.getUniformLocation(ShaderProgramConstants.UNIFORM_TEXTURE_0);
	}

	@Override
	public void bind(final GLState pGLState,
			final VertexBufferObjectAttributes pVertexBufferObjectAttributes) {
		super.bind(pGLState, pVertexBufferObjectAttributes);

		GLES20.glUniformMatrix4fv(
				PositionColorTextureCoordinatesShaderProgram.sUniformModelViewPositionMatrixLocation,
				1, false, pGLState.getModelViewProjectionGLMatrix(), 0);
		GLES20.glUniform1i(
				PositionColorTextureCoordinatesShaderProgram.sUniformTexture0Location,
				0);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
