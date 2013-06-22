package com.crocoware.andengine.entity.sprite.stretch;

import org.andengine.opengl.shader.PositionColorTextureCoordinatesShaderProgram;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.shader.exception.ShaderProgramLinkException;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;

import android.opengl.GLES20;

/**
 * 
 * Shader used by StretchSprite
 * 
 * @author Benoit Chauvet (Orabig) www.crocoware.com
 * 
 */
public class StretchShader extends ShaderProgram {

	private StretchShader() {
		super(StretchShader.VERTEXSHADER, StretchShader.FRAGMENTSHADER);
	}

	private static StretchShader INSTANCE;

	public static StretchShader getInstance() {
		if (StretchShader.INSTANCE == null) {
			StretchShader.INSTANCE = new StretchShader();
		}
		return StretchShader.INSTANCE;
	}
	
	// Sorry about not using constants, but the programs become unreadable if I do.

	public static final String VERTEXSHADER = ""//
			+ "     uniform    mat4    u_modelViewProjectionMatrix;               "

			+ "     attribute  vec4    a_position;                                "
			+ "     attribute  vec2    a_shift;                                   "
			+ "     attribute  vec2    a_size;                                    "

			+ "     varying    vec2    v_shift;                                   "
			+ "     varying    vec2    v_size;                                    "

			+ "  void main() {                                                    "
			+ "    gl_Position = u_modelViewProjectionMatrix * a_position;        "
			+ "    v_shift  =    a_shift;                                         "
			+ "    v_size   =    a_size;                                          "
			+ "  }                                                                ";

	public static final String FRAGMENTSHADER = ""//
			+ " precision lowp float;                                             "

			+ " uniform          sampler2D  u_texture_0;                          "

			+ " varying   vec2       v_shift;                                     "
			+ " varying   vec2       v_size;                                      "

			+ "  void main() {                                                    "
			+ "      gl_FragColor =  texture2D( u_texture_0                       "
			+ "                                   , v_shift/v_size);              "
			+ "                                                                   "
			+ "  }                                                                ";

	@Override
	protected void link(final GLState pGLState)
			throws ShaderProgramLinkException {
		GLES20.glBindAttribLocation(this.mProgramID,
				ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION,
				ShaderProgramConstants.ATTRIBUTE_POSITION);
		GLES20.glBindAttribLocation(this.mProgramID,
				StretchSprite.ATTRIBUTE_SHIFT_LOCATION, "a_shift");
		GLES20.glBindAttribLocation(this.mProgramID,
				StretchSprite.ATTRIBUTE_SIZE_LOCATION, "a_size");

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

}
