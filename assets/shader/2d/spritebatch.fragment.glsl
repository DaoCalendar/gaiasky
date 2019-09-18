#version 330 core

uniform sampler2D u_texture;

in vec4 v_color;
in vec2 v_texCoords;

layout (location = 0) out vec4 fragColor;

void main() {
    fragColor = v_color * texture(u_texture, v_texCoords);
}
