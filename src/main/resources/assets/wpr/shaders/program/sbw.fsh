#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    // Perceptual luminance (human eye weighting)
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    // Hard threshold — every pixel snaps to pure black or white, no gray
    float bw = step(0.25, luminance);

    fragColor = vec4(vec3(bw), 1.0);
}
