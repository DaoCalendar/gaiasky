#version 330 core

out vec4 fragColor;

// Renders a depth map for the shadow mapping algorithm
void main() {
    fragColor = vec4(gl_FragCoord.z, 0.0, 0.0, 1.0);
}
