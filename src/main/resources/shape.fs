#version 460 core

layout (location=0) out vec4 color;

in vec2 v_texCoord;
in vec3 v_color;

uniform vec2 u_resolution;
uniform sampler2D u_spriteMap;
uniform sampler2D u_currentTexture;

void main(void) {
	vec4 newColor = texture(u_spriteMap, v_texCoord);
	vec4 currentColor = texture(u_currentTexture, gl_FragCoord.xy/u_resolution);
	float bw = (newColor.r + newColor.g + newColor.b)/3.;
	color = vec4(bw * v_color * newColor.a + currentColor.rgb * (1-newColor.a), 1);
}
