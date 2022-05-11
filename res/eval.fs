#version 460 core

#define PRECISION 100

in vec2 v_texCoord;
in vec3 v_color;

uniform sampler2D u_spriteMap;
uniform sampler2D u_targetTexture;
uniform sampler2D u_currentTexture;
uniform vec2 u_resolution;

struct OutputData {
	int totalProgress;
};

layout(std430, binding = 1) buffer outputBuffer
{
    OutputData outputArray[];
};

int colorDistance(vec3 c1, vec3 c2) {
	c1 = abs(c2-c1);
	return int(PRECISION*dot(c1, vec3(1))); // alpha is not accounted for
}

void main(void) {
	vec3 currentColor = texture(u_currentTexture, gl_FragCoord.xy/u_resolution).rgb;
	vec3 targetColor = texture(u_targetTexture, gl_FragCoord.xy/u_resolution).rgb;
	vec3 replacementColor = texture(u_spriteMap, v_texCoord).rgb;
	float bw = (replacementColor.r + replacementColor.g + replacementColor.b)/3.;
	vec3 newColor = bw*v_color;
	int improvement = colorDistance(targetColor, currentColor) - colorDistance(targetColor, newColor);
	atomicAdd(outputArray[gl_PrimitiveID/2].totalProgress, improvement);
	discard;
}
