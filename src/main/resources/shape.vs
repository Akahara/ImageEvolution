#version 330 core

const vec2[] uvs = vec2[](
	vec2(0,0),
	vec2(1,0),
	vec2(1,1),
	vec2(0,1));

layout(location=0) in vec2 i_position; // -1 to 1
layout(location=1) in vec3 i_color;

out vec2 v_texCoord;
out vec3 v_color;

void main(void) {
  gl_Position = vec4(i_position, 0, 1);
  v_texCoord = uvs[gl_VertexID];
  v_color = i_color;
}