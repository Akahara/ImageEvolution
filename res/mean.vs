#version 330 core

layout(location=0) in vec2 i_vertex; // -1 to 1
layout(location=1) in vec2 i_position;
layout(location=2) in vec4 i_textureCoords;
layout(location=3) in float i_scale;
layout(location=4) in float i_rotation;

out vec2 v_texCoord;

mat2 R2D(float theta) {
  float c = cos(theta), s = sin(theta);
  return mat2(c, -s, s, c);
}

void main(void) {
  vec2 pos = i_position + R2D(i_rotation) * i_vertex * i_scale;
  gl_Position = vec4(pos, 0, 1);
  v_texCoord = i_textureCoords.zw * (i_vertex+1)*.5 + i_textureCoords.xw;
}