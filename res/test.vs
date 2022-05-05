#version 330 core

layout(location=0) in vec2 i_pos;

out vec2 v_texCoord;

void main(void) {
  gl_Position = vec4(i_pos, 0, 1);
  v_texCoord = i_pos;
}
