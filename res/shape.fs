#version 460 core

layout (location=0) out vec4 color;

in vec2 v_texCoord;
in vec3 v_color;

uniform sampler2D u_spriteMap;

void main(void) {
	vec4 newColor = texture(u_spriteMap, v_texCoord);
	float bw = (newColor.r + newColor.g + newColor.b)/3.;
	color = vec4(bw * v_color, newColor.a);
}
