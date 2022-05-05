#version 330 core

layout(location=0) out vec4 color;

in vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 u_resolution;

void main(void) {
  color = texture(u_texture, v_texCoord);
  //color = vec4(1);
  //color = vec4(vec3(.5), 1);
}
