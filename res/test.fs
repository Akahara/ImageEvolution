#version 460 core

layout(location=0) out vec4 color;

in vec2 v_texCoord;

uniform vec4 u_color;

#define OUTPUT
#define PRECISION 10

layout(std140, binding = 0) uniform mainBuffer {
	int imgWidth;
	vec4[] targetImage;
};

layout(std430, binding = 1) buffer currentImageBuffer {
	vec4[] currentImage;
};

layout(std430, binding = 2) buffer differenceBuffer
{
    int totalDiff;
};

int colorDistance(vec4 c1, vec4 c2) {
	c1 = abs(c2-c1);
	return int(PRECISION*(c1.x+c1.y+c1.z));
}

void main(void) {
	int x = int(gl_FragCoord.x);
	int y = int(gl_FragCoord.y);
	int idx = x+imgWidth*y;
	vec4 targetColor = targetImage[idx];
	vec4 currentColor = currentImage[idx];
	vec4 newColor = u_color;
	int improvement = colorDistance(targetColor, currentColor) - colorDistance(targetColor, newColor);
	atomicAdd(totalDiff, improvement);
#ifdef OUTPUT
	color = newColor;
#else
	discard;
#endif
}
