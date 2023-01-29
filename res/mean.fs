#version 460 core

in vec2 v_texCoord;

uniform sampler2D u_spriteMap;
uniform sampler2D u_targetTexture;
uniform vec2 u_resolution;

struct OutputData {
	int totalImpactCount;
    int totalRed;
    int totalGreen;
    int totalBlue;
};

layout(std430, binding = 0) buffer outputBuffer
{
    OutputData outputArray[];
};

void main(void) {
	vec4 targetColor = texture(u_targetTexture, gl_FragCoord.xy/u_resolution);
	vec4 newColor = texture(u_spriteMap, v_texCoord);
	float impact = 255. * newColor.a;
	impact = 255.;
	atomicAdd(outputArray[gl_PrimitiveID/2].totalImpactCount, int(impact));
	atomicAdd(outputArray[gl_PrimitiveID/2].totalRed,   int(impact * targetColor.r));
	atomicAdd(outputArray[gl_PrimitiveID/2].totalGreen, int(impact * targetColor.g));
	atomicAdd(outputArray[gl_PrimitiveID/2].totalBlue,  int(impact * targetColor.b));
	discard;
}
